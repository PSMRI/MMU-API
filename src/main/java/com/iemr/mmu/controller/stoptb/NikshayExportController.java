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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.iemr.mmu.service.stoptb.NikshayExportService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/stopTb/nikshay", headers = "Authorization")
@PreAuthorize("hasRole('NURSE') || hasRole('PHARMACIST') || hasRole('LABTECHNICIAN') || hasRole('DOCTOR') || hasRole('LAB_TECHNICIAN') || hasRole('TC_SPECIALIST') || hasRole('ONCOLOGIST') || hasRole('RADIOLOGIST')")
public class NikshayExportController {
	private static final Logger logger = LoggerFactory.getLogger(NikshayExportController.class);
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

	@Autowired
	private NikshayExportService nikshayExportService;

	@Operation(summary = "Download a Stop TB camp's beneficiaries as a CSV formatted for the Nikshay ID Generator")
	@GetMapping(value = "/exportBeneficiariesCsv")
	public ResponseEntity<?> exportBeneficiariesCsv(@RequestParam("fromDate") String fromDateStr,
			@RequestParam("toDate") String toDateStr, @RequestParam("vanID") Integer vanID,
			@RequestParam("servicePointID") Integer servicePointID) {

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
		try {
			excludedCount = nikshayExportService.countAlreadyGenerated(vanID, servicePointID, fromDate, toDate);
		} catch (Exception e) {
			logger.error("Error preparing Nikshay beneficiary export", e);
			return ResponseEntity.status(500).body("Could not prepare the export");
		}

		StreamingResponseBody body = outputStream -> {
			try {
				nikshayExportService.streamBeneficiariesCsv(vanID, servicePointID, fromDate, toDate, outputStream);
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
				.header("X-Excluded-Existing-Nikshay-Id-Count", String.valueOf(excludedCount)).body(body);
	}
}
