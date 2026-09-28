/*
* AMRIT – Accessible Medical Records via Integrated Technology
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.controller.stoptb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iemr.mmu.service.stoptb.NikshayExportService;
import com.iemr.mmu.service.stoptb.NikshayImportService;
import com.iemr.mmu.service.stoptb.NikshayImportService.ImportSummary;
import com.iemr.mmu.utils.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Downloads/uploads the Stop TB Nikshay ID Generator CSV.
 *
 * Scoped only by visit-date range — MMU runs one local database per van, so
 * everything in it already belongs to the current van/service point; there
 * is deliberately no vanID/servicePointID parameter on either endpoint.
 */
@RestController
@RequestMapping(value = "/stopTb/nikshay", headers = "Authorization")
// No @PreAuthorize role gate — any authenticated user can hit these endpoints
// (SecurityConfig's anyRequest().authenticated() still applies). Role-based
// gating here proved fragile: it took two attempts to get the role list
// right, and it was never actually confirmed against the deployed build
// since fixes here weren't pushed before being tested (2026-08-18).
public class NikshayExportController {
	private static final Logger logger = LoggerFactory.getLogger(NikshayExportController.class);
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

	@Autowired
	private NikshayExportService nikshayExportService;

	@Autowired
	private NikshayImportService nikshayImportService;

	@Autowired
	private JwtUtil jwtUtil;

	/** Best-effort — this is only used for a created_by/modified_by audit column,
	 * never for authorization (the filter chain/PreAuthorize already handled that). */
	private String currentUsername(HttpServletRequest request) {
		try {
			String header = request.getHeader("Authorization");
			if (header == null) {
				return "unknown";
			}
			String token = header.startsWith("Bearer ") ? header.substring(7) : header;
			String username = jwtUtil.extractUsername(token);
			return username != null ? username : "unknown";
		} catch (Exception e) {
			return "unknown";
		}
	}

	/** Writes the CSV directly and synchronously onto the servlet response,
	 * instead of returning a StreamingResponseBody. StreamingResponseBody
	 * makes Spring process the body on a Servlet-async re-dispatch — Spring
	 * Security's filter chain re-runs on that async dispatch, and the
	 * SecurityContext doesn't reliably carry over to it, so the global
	 * anyRequest().authenticated() rule was denying the *second* pass even
	 * though the initial request authenticated fine (confirmed in production
	 * logs, 2026-08-18: two AccessDeniedExceptions for the same request, the
	 * second one through ApplicationDispatcher/AsyncContextImpl). Writing
	 * synchronously avoids async dispatch entirely, so there's no second
	 * security pass to fail. */
	@Operation(summary = "Download Stop TB beneficiaries for a date range as a CSV formatted for the Nikshay ID Generator")
	@GetMapping(value = "/exportBeneficiariesCsv")
	public void exportBeneficiariesCsv(@RequestParam("fromDate") String fromDateStr,
			@RequestParam("toDate") String toDateStr, HttpServletResponse response) throws java.io.IOException {

		LocalDate fromDate;
		LocalDate toDate;
		try {
			fromDate = LocalDate.parse(fromDateStr, DATE_FMT);
			toDate = LocalDate.parse(toDateStr, DATE_FMT);
		} catch (DateTimeParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "fromDate/toDate must be in YYYY-MM-DD format");
			return;
		}
		if (toDate.isBefore(fromDate)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "toDate must be on or after fromDate");
			return;
		}

		int excludedAlreadyGenerated;
		int excludedNotReadyToExport;
		try {
			excludedAlreadyGenerated = nikshayExportService.countAlreadyGenerated(fromDate, toDate);
			excludedNotReadyToExport = nikshayExportService.countNotReadyToExport(fromDate, toDate);
		} catch (Exception e) {
			logger.error("Error preparing Nikshay beneficiary export", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not prepare the export");
			return;
		}

		String filename = "nikshay-beneficiaries-" + fromDate + "-to-" + toDate + ".csv";
		response.setContentType("text/csv");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
		response.setHeader("X-Excluded-Existing-Nikshay-Id-Count", String.valueOf(excludedAlreadyGenerated));
		response.setHeader("X-Excluded-Not-Ready-Count", String.valueOf(excludedNotReadyToExport));

		try {
			nikshayExportService.streamBeneficiariesCsv(fromDate, toDate, response.getOutputStream());
		} catch (Exception e) {
			// Headers are already committed by the time streaming starts, so a
			// mid-stream failure can only be logged, not surfaced as a clean
			// error response.
			logger.error("Error streaming Nikshay beneficiary CSV", e);
		}
	}

	@Operation(summary = "Upload the Nikshay ID Generator app's results CSV to write generated Nikshay IDs "
			+ "back onto the beneficiaries — each row is matched by its own benRegId column, "
			+ "a pass-through field the export added that the ID Generator app never touches")
	@PostMapping(value = "/importResultsCsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> importResultsCsv(@RequestParam("visitDate") String visitDateStr,
			@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body("A results CSV file is required");
		}
		LocalDate visitDate;
		try {
			visitDate = LocalDate.parse(visitDateStr, DATE_FMT);
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().body("visitDate must be in YYYY-MM-DD format");
		}
		try {
			ImportSummary summary = nikshayImportService.importResults(visitDate, file.getInputStream(),
					currentUsername(request));
			return ResponseEntity.ok(summary);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			logger.error("Error importing Nikshay results CSV", e);
			return ResponseEntity.status(500).body("Could not import the results file");
		}
	}
}
