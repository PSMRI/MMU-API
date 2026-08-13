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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.iemr.mmu.service.stoptb.NikshayExportService;
import com.iemr.mmu.service.stoptb.NikshayImportService;
import com.iemr.mmu.service.stoptb.NikshayImportService.ImportSummary;
import com.iemr.mmu.utils.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/stopTb/nikshay", headers = "Authorization")
@PreAuthorize("hasRole('NURSE') || hasRole('PHARMACIST') || hasRole('LABTECHNICIAN') || hasRole('DOCTOR') || hasRole('LAB_TECHNICIAN') || hasRole('TC_SPECIALIST') || hasRole('ONCOLOGIST') || hasRole('RADIOLOGIST')")
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

	@Operation(summary = "Download a Stop TB camp's beneficiaries as a CSV formatted for the Nikshay ID Generator")
	@GetMapping(value = "/exportBeneficiariesCsv")
	public ResponseEntity<?> exportBeneficiariesCsv(@RequestParam("fromDate") String fromDateStr,
			@RequestParam("toDate") String toDateStr, @RequestParam("vanID") Integer vanID,
			@RequestParam("servicePointID") Integer servicePointID, HttpServletRequest request) {

		LocalDate fromDate;
		LocalDate toDate;
		try {
			fromDate = LocalDate.parse(fromDateStr, DATE_FMT);
			toDate = LocalDate.parse(toDateStr, DATE_FMT);
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().body("fromDate/toDate must be in YYYY-MM-DD format");
		}
		if (toDate.isBefore(fromDate)) {
			return ResponseEntity.badRequest().body("toDate must be on or after fromDate");
		}
		if (vanID == null || servicePointID == null) {
			return ResponseEntity.badRequest().body("vanID and servicePointID are required");
		}

		int excludedCount;
		Long batchId;
		try {
			excludedCount = nikshayExportService.countAlreadyGenerated(vanID, servicePointID, fromDate, toDate);
			// Created synchronously, ahead of streaming, so its ID can go out as a
			// response header — headers can't change once the streamed body starts.
			batchId = nikshayExportService.createExportBatch(vanID, servicePointID, fromDate, toDate,
					currentUsername(request));
		} catch (Exception e) {
			logger.error("Error preparing Nikshay beneficiary export", e);
			return ResponseEntity.status(500).body("Could not prepare the export");
		}

		StreamingResponseBody body = outputStream -> {
			try {
				nikshayExportService.streamBeneficiariesCsv(vanID, servicePointID, fromDate, toDate, outputStream,
						batchId);
			} catch (Exception e) {
				// The HTTP status/headers are already committed by the time streaming
				// starts, so a mid-stream failure can only be logged, not surfaced
				// as a clean error response.
				logger.error("Error streaming Nikshay beneficiary CSV", e);
			}
		};

		String filename = "nikshay-beneficiaries-" + fromDate + "-to-" + toDate + ".csv";
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.header("X-Excluded-Existing-Nikshay-Id-Count", String.valueOf(excludedCount))
				.header("X-Nikshay-Export-Batch-Id", String.valueOf(batchId)).body(body);
	}

	@Operation(summary = "Upload the Nikshay ID Generator app's results CSV to write generated Nikshay IDs "
			+ "back onto the beneficiaries from a prior export (identified by batchId)")
	@PostMapping(value = "/importResultsCsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> importResultsCsv(@RequestParam("batchId") Long batchId,
			@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body("A results CSV file is required");
		}
		try {
			ImportSummary summary = nikshayImportService.importResults(batchId, file.getInputStream(),
					currentUsername(request));
			return ResponseEntity.ok(summary);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			logger.error("Error importing Nikshay results CSV for batchId {}", batchId, e);
			return ResponseEntity.status(500).body("Could not import the results file");
		}
	}
}
