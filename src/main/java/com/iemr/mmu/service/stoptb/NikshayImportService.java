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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.mmu.repo.stoptb.NikshayExportRepository;
import com.iemr.mmu.repo.stoptb.NikshayExportRepository.ExportBatch;
import com.iemr.mmu.repo.stoptb.NikshayExportRepository.ExportBatchRow;

/**
 * Imports the Nikshay ID Generator desktop app's results CSV, writing the
 * portal-generated Nikshay IDs back onto the beneficiaries a prior export
 * (identified by {@code batchId}) streamed out.
 *
 * Matching is purely by row position: results file row i corresponds to
 * export batch row i. That app's own CSV template has no room for an
 * AMRIT-internal identifier — reading strips any column outside its fixed
 * 20-column list — but it does guarantee it never reorders or drops rows
 * between the input it read and the results it writes (confirmed from its
 * own source: output is built as {@code rows.map((r, i) => ...)}), which is
 * what makes position-based matching safe as long as the row count matches
 * the original export exactly.
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

	private static final List<String> REQUIRED_COLUMNS = List.of("firstName", "middleLastName", "generatedId",
			"status");

	public record ImportRowResult(int rowIndex, Long benRegId, String firstName, String middleLastName,
			String status, String generatedId, String note) {
	}

	public record ImportSummary(int csvRowCount, int batchRowCount, int updated, int failed, int needsReview,
			List<ImportRowResult> needsReviewRows, List<ImportRowResult> failedRows) {
	}

	@Autowired
	private NikshayExportRepository nikshayExportRepository;

	public ImportSummary importResults(Long batchId, InputStream csvInputStream, String modifiedBy)
			throws Exception {
		ExportBatch batch = nikshayExportRepository.getBatch(batchId);
		if (batch == null) {
			throw new IllegalArgumentException("Unknown batchId: " + batchId);
		}
		List<ExportBatchRow> batchRows = nikshayExportRepository.getBatchRows(batchId);

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

		if (records.size() != batchRows.size()) {
			throw new IllegalArgumentException("Results CSV has " + records.size() + " row(s) but export batch "
					+ batchId + " has " + batchRows.size()
					+ " — this file doesn't match that export (wrong file, or rows were added/removed).");
		}

		int updated = 0;
		List<ImportRowResult> needsReview = new ArrayList<>();
		List<ImportRowResult> failedRows = new ArrayList<>();

		for (int i = 0; i < records.size(); i++) {
			CSVRecord record = records.get(i);
			ExportBatchRow batchRow = batchRows.get(i);

			String status = record.get("status").trim();
			String generatedId = record.get("generatedId").trim();
			String firstName = record.get("firstName");
			String middleLastName = record.get("middleLastName");

			if ("success".equalsIgnoreCase(status) || "skipped".equalsIgnoreCase(status)) {
				String[] tokens = generatedId.isEmpty() ? new String[0] : generatedId.split("\\s+");
				if (tokens.length == 1) {
					writeNikshayId(batch, batchRow, tokens[0], modifiedBy);
					updated++;
				} else {
					String note = tokens.length == 0 ? "Row marked " + status + " but has no generatedId."
							: "Multiple possible existing Nikshay IDs (" + generatedId + ") — needs manual confirmation.";
					needsReview.add(new ImportRowResult(i, batchRow.benRegId(), firstName, middleLastName, status,
							generatedId, note));
				}
			} else {
				String error = hasErrorColumn ? record.get("error") : "";
				failedRows.add(new ImportRowResult(i, batchRow.benRegId(), firstName, middleLastName, status,
						generatedId, error));
			}
		}

		return new ImportSummary(records.size(), batchRows.size(), updated, failedRows.size(), needsReview.size(),
				needsReview, failedRows);
	}

	private void writeNikshayId(ExportBatch batch, ExportBatchRow batchRow, String nikshayId, String modifiedBy) {
		if (batchRow.diagnosticsId() != null) {
			nikshayExportRepository.updateNikshayId(batchRow.diagnosticsId(), nikshayId, modifiedBy);
		} else {
			nikshayExportRepository.insertDiagnosticsWithNikshayId(batchRow.benRegId(), batch.vanID(),
					batch.servicePointID(), batch.fromDate(), nikshayId, modifiedBy);
		}
	}
}
