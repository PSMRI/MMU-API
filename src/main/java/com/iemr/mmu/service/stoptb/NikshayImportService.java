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
package com.iemr.mmu.service.stoptb;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.mmu.repo.stoptb.NikshayExportRepository;

/**
 * Imports the Nikshay ID Generator desktop app's results CSV, writing the
 * portal-generated Nikshay IDs back onto the right beneficiaries.
 *
 * Confirmed against a real results file (2026-08-18): the ID Generator app
 * does NOT pass through any AMRIT-side beneficiary ID — the results CSV only
 * has the original template columns (typeOfCaseFinding, caste, firstName,
 * ..., hivStatus) plus its own generatedId/status/durationSec/error columns.
 * There is deliberately no assumption that result-row order matches the
 * uploaded file's row order either (unconfirmed, and a single
 * reordered/dropped row would silently corrupt every row after it if wrong).
 *
 * Instead, each row is matched back to a beneficiary by content —
 * normalized 10-digit primaryPhone plus a case-insensitive firstName match
 * (see NikshayExportRepository.findMatchingBeneficiaryIds). This is a
 * heuristic, not an exact key: anything other than exactly one match (zero,
 * or more than one — e.g. a shared family phone number) is left for manual
 * review rather than guessed.
 *
 * Row status handling:
 * - "success": generatedId is the new Nikshay ID — written as-is.
 * - "skipped" (portal-detected duplicate): generatedId is one or more
 *   existing patient IDs, space-separated. A single ID is written the same
 *   as a success; more than one is ambiguous and left for manual review
 *   rather than guessed.
 * - "failed": never written; surfaced in the response for visibility.
 */
@Service
public class NikshayImportService {

	private static final List<String> REQUIRED_COLUMNS = List.of("primaryPhone", "firstName", "middleLastName",
			"generatedId", "status");

	public record ImportRowResult(int rowIndex, Long benRegId, String firstName, String middleLastName,
			String status, String generatedId, String note) {
	}

	public record ImportSummary(int csvRowCount, int updated, int failed, int needsReview,
			List<ImportRowResult> needsReviewRows, List<ImportRowResult> failedRows) {
	}

	@Autowired
	private NikshayExportRepository nikshayExportRepository;

	public ImportSummary importResults(LocalDate visitDate, InputStream csvInputStream, String modifiedBy)
			throws Exception {
		List<CSVRecord> records;
		boolean hasErrorColumn;
		CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build();
		try (CSVParser parser = new CSVParser(new InputStreamReader(csvInputStream, StandardCharsets.UTF_8),
				format)) {
			Map<String, Integer> header = parser.getHeaderMap();
			for (String required : REQUIRED_COLUMNS) {
				if (!header.containsKey(required)) {
					throw new IllegalArgumentException("Results CSV is missing required column: " + required);
				}
			}
			hasErrorColumn = header.containsKey("error");
			records = parser.getRecords();
		}

		int updated = 0;
		List<ImportRowResult> needsReview = new ArrayList<>();
		List<ImportRowResult> failedRows = new ArrayList<>();

		for (int i = 0; i < records.size(); i++) {
			CSVRecord record = records.get(i);
			String firstName = record.get("firstName");
			String middleLastName = record.get("middleLastName");
			String status = record.get("status").trim();
			String generatedId = record.get("generatedId").trim();

			String phoneDigits = normalizePhone(record.get("primaryPhone"));
			if (phoneDigits == null) {
				failedRows.add(new ImportRowResult(i, null, firstName, middleLastName, status, generatedId,
						"Row has a missing/invalid primaryPhone — cannot match it back to a beneficiary."));
				continue;
			}

			List<Long> candidates = nikshayExportRepository.findMatchingBeneficiaryIds(phoneDigits, firstName);
			if (candidates.size() != 1) {
				String note = candidates.isEmpty()
						? "No beneficiary found matching this phone number and first name."
						: "More than one beneficiary matches this phone number and first name (e.g. a shared "
								+ "family phone) — needs manual confirmation.";
				needsReview.add(new ImportRowResult(i, null, firstName, middleLastName, status, generatedId, note));
				continue;
			}
			Long benRegId = candidates.get(0);

			if ("success".equalsIgnoreCase(status) || "skipped".equalsIgnoreCase(status)) {
				String[] tokens = generatedId.isEmpty() ? new String[0] : generatedId.split("\\s+");
				if (tokens.length == 1) {
					writeNikshayId(visitDate, benRegId, tokens[0], modifiedBy);
					updated++;
				} else {
					String note = tokens.length == 0 ? "Row marked " + status + " but has no generatedId."
							: "Multiple possible existing Nikshay IDs (" + generatedId
									+ ") — needs manual confirmation.";
					needsReview.add(
							new ImportRowResult(i, benRegId, firstName, middleLastName, status, generatedId, note));
				}
			} else {
				String error = hasErrorColumn ? record.get("error") : "";
				failedRows.add(
						new ImportRowResult(i, benRegId, firstName, middleLastName, status, generatedId, error));
			}
		}

		return new ImportSummary(records.size(), updated, failedRows.size(), needsReview.size(), needsReview,
				failedRows);
	}

	/** Same normalization as the export's validPhoneOrBlank, so a phone number
	 * round-trips consistently: strips non-digits, keeps the last 10, requires
	 * a valid-looking Indian mobile number. Null if it doesn't. */
	private static String normalizePhone(String raw) {
		if (raw == null) {
			return null;
		}
		String digits = raw.replaceAll("[^0-9]", "");
		if (digits.length() > 10) {
			digits = digits.substring(digits.length() - 10);
		}
		return digits.matches("[1-9][0-9]{9}") ? digits : null;
	}

	private void writeNikshayId(LocalDate visitDate, Long benRegId, String nikshayId, String modifiedBy) {
		Long suspectedId = nikshayExportRepository.findLatestSuspectedId(benRegId);
		if (suspectedId != null) {
			nikshayExportRepository.updateNikshayId(suspectedId, nikshayId, modifiedBy);
		} else {
			nikshayExportRepository.insertSuspectedWithNikshayId(benRegId, visitDate, nikshayId, modifiedBy);
		}
	}
}
