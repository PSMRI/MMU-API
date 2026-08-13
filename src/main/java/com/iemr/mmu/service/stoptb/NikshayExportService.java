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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.mmu.repo.stoptb.NikshayExportRepository;
import com.iemr.mmu.repo.stoptb.NikshayExportRepository.NikshayRawRow;

/**
 * Builds the Nikshay ID Generator CSV for a Stop TB camp.
 *
 * Column order/semantics follow the ID Generator's own data template
 * (Nikshaya/nikshay-enrollment-app/docs/DATA_TEMPLATE.md): 20 fixed columns.
 * The desktop app's own pre-flight validation (data.ts's REQUIRED_COLUMNS)
 * treats every column except state/district/tu as non-blank-required —
 * stricter than the template doc's "requested" language — so every
 * categorical field below resolves to a real value: an exact
 * (case-insensitive) match against Nikshay's allowed-value list when AMRIT's
 * data lines up, otherwise a safe always-valid fallback (e.g. "Unknown"),
 * never a guessed/fuzzy mapping onto the wrong specific label.
 *
 * Known gaps, best-effort until resolved elsewhere:
 * - healthFacility/village: best-effort AMRIT names (m_parkingplace /
 *   m_DistrictBranchMapping). These are NOT yet verified against Nikshay's
 *   own separate location hierarchy/master list, which is what the portal
 *   actually validates against — this needs the Nikshay location resolver
 *   work (see Common-API's NikshayAddressResolver, unmerged as of writing).
 * - occupation/area: no reliable AMRIT-to-Nikshay label mapping exists yet,
 *   so these always fall back to "Unknown" (a valid value for both).
 * - symptoms: AMRIT has no structured Stop TB symptom checklist wired up yet
 *   (that data lives in the generic Dynamic Form response tables, whose
 *   question mapping isn't resolved). Best-effort: "Asymptomatic" when no
 *   chief complaint was recorded, "Others" otherwise.
 * - gender has no safe generic fallback (no "Unknown" option in Nikshay's
 *   3-value list) — left blank on an unmapped value rather than guessed,
 *   which will surface as a clear per-row error in the ID Generator app.
 */
@Service
public class NikshayExportService {

	private static final String[] CSV_HEADER = { "typeOfCaseFinding", "caste", "firstName", "middleLastName", "age",
			"gender", "primaryPhone", "address", "state", "district", "tu", "healthFacility", "village", "pincode",
			"area", "maritalStatus", "occupation", "socioeconomicStatus", "symptoms", "hivStatus" };

	private static final Set<String> GENDER_VALUES = setOf("Male", "Female", "Transgender");
	private static final Set<String> CASTE_VALUES = setOf("SC", "ST", "Other");
	private static final Set<String> MARITAL_VALUES = setOf("Single", "Married", "Unknown");
	private static final Set<String> SOCIOECONOMIC_VALUES = setOf("APL", "BPL", "Unknown");
	private static final Set<String> HIV_VALUES = setOf("Positive", "Reactive", "Non Reactive / Negative", "Unknown");
	// Verbatim from the ID Generator's own allowed-values list (docs/DATA_TEMPLATE.md) —
	// spellings/typos are copied exactly as the portal defines them.
	private static final Set<String> OCCUPATION_VALUES = setOf("Legislators and Senior officials",
			"Corporate Manager", "General Manager",
			"Physical, mathematical and engineering science professional",
			"Life sciences and health professional", "Teaching professional", "Other professional", "Office Clerk",
			"Customer Services Clerks", "Personal Protective Service Providers",
			"Models, Sales Persons and Demonstrators", "Market oriented skilled agriculutre and fishery workers",
			"Subsitence agriculture and fishery workers", "Extraction and building trade workers",
			"Metal, Machinery and related trades workers",
			"Precision, handicraft, printing and related trade workers",
			"Other Craft and related traders and workers", "Stationary Plant and related Operators",
			"Machine Operators and Assembler", "Drivers and Mobile Plant Operators",
			"Sales and Services elementry occupations", "Agriculture, fishery and related labour",
			"Laborers in mining, construction, manufecturing and transport", "New Workers seeking employment",
			"Workers reporting occupation unidentifiable or inadequately", "Workers no reporting any occupation",
			"House Wife", "Unknown");

	@Autowired
	private NikshayExportRepository nikshayExportRepository;

	private static Set<String> setOf(String... values) {
		return new HashSet<>(Arrays.asList(values));
	}

	public int countAlreadyGenerated(Integer vanID, Integer servicePointID, LocalDate fromDate, LocalDate toDate) {
		return nikshayExportRepository.countAlreadyGenerated(vanID, servicePointID, fromDate, toDate);
	}

	/** Creates the export batch header row up front, before the CSV starts streaming,
	 * so its ID is available for a response header the caller can hand back on import. */
	public Long createExportBatch(Integer vanID, Integer servicePointID, LocalDate fromDate, LocalDate toDate,
			String createdBy) {
		return nikshayExportRepository.createExportBatch(vanID, servicePointID, fromDate, toDate, createdBy);
	}

	/** Writes the CSV (header + one row per pending beneficiary) directly to
	 * {@code outputStream} as rows arrive from the database — never buffers
	 * the whole file in memory. Caller owns closing {@code outputStream}.
	 *
	 * Also records each row's beneficiary/diagnostics identity against
	 * {@code batchId}, in CSV row order — the only way to match a beneficiary
	 * back up once the Nikshay ID Generator app's results file comes back,
	 * since that app strips any column not in its own fixed template. */
	public void streamBeneficiariesCsv(Integer vanID, Integer servicePointID, LocalDate fromDate, LocalDate toDate,
			OutputStream outputStream, Long batchId) {
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
		try {
			writer.write(String.join(",", CSV_HEADER));
			writer.write("\r\n");

			int[] rowIndex = { 0 };
			nikshayExportRepository.streamPendingBeneficiaries(vanID, servicePointID, fromDate, toDate, row -> {
				try {
					writer.write(toCsvLine(row));
					writer.write("\r\n");
					nikshayExportRepository.addBatchRow(batchId, rowIndex[0]++, row.benRegId(), row.diagnosticsId());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			writer.flush();
			nikshayExportRepository.finalizeBatchRowCount(batchId, rowIndex[0]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String toCsvLine(NikshayRawRow row) {
		String[] values = {
				"Passive", // typeOfCaseFinding - no Active/Passive signal in AMRIT yet; matches the portal's own default
				matchOrDefault(row.caste(), CASTE_VALUES, "Other"),
				nullToEmpty(row.firstName()),
				nullToEmpty(row.middleLastName()),
				ageOrBlank(row.age()),
				matchOrBlank(row.gender(), GENDER_VALUES),
				validPhoneOrBlank(row.phone()),
				nullToEmpty(row.address()),
				nullToEmpty(row.stateName()), // auto-filled from the Nikshay operator's own login on submit either way
				nullToEmpty(row.districtName()), // same
				"", // tu - no AMRIT equivalent; exempt from the app's own required-column check
				nullToEmpty(row.healthFacility()),
				nullToEmpty(row.village()),
				validPincodeOrBlank(row.pincode()),
				"Unknown", // area - no reliable AMRIT-to-Nikshay mapping available yet
				matchOrDefault(row.maritalStatus(), MARITAL_VALUES, "Unknown"),
				matchOrDefault(row.occupation(), OCCUPATION_VALUES, "Unknown"),
				matchOrDefault(row.socioeconomicStatus(), SOCIOECONOMIC_VALUES, "Unknown"),
				symptomsFrom(row.chiefComplaint()),
				hivStatusFrom(row.hivStatus(), row.isHivPos()),
		};
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(csvEscape(values[i]));
		}
		return sb.toString();
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s.trim();
	}

	private static String matchOrBlank(String raw, Set<String> allowed) {
		if (raw == null) {
			return "";
		}
		String trimmed = raw.trim();
		for (String candidate : allowed) {
			if (candidate.equalsIgnoreCase(trimmed)) {
				return candidate;
			}
		}
		return "";
	}

	private static String matchOrDefault(String raw, Set<String> allowed, String fallback) {
		String matched = matchOrBlank(raw, allowed);
		return matched.isEmpty() ? fallback : matched;
	}

	private static String ageOrBlank(Integer age) {
		return (age != null && age >= 1 && age <= 99) ? String.valueOf(age) : "";
	}

	private static String validPhoneOrBlank(String raw) {
		if (raw == null) {
			return "";
		}
		String digits = raw.replaceAll("[^0-9]", "");
		if (digits.length() > 10) {
			digits = digits.substring(digits.length() - 10);
		}
		return digits.matches("[1-9][0-9]{9}") ? digits : "";
	}

	private static String validPincodeOrBlank(String raw) {
		if (raw == null) {
			return "";
		}
		String trimmed = raw.trim();
		return trimmed.matches("[0-9]{6}") ? trimmed : "";
	}

	private static String symptomsFrom(String chiefComplaint) {
		return (chiefComplaint == null || chiefComplaint.trim().isEmpty()) ? "Asymptomatic" : "Others";
	}

	private static String hivStatusFrom(String rawHivStatus, Boolean isHivPos) {
		String matched = matchOrBlank(rawHivStatus, HIV_VALUES);
		if (!matched.isEmpty()) {
			return matched;
		}
		if (Boolean.TRUE.equals(isHivPos)) {
			return "Positive";
		}
		if (Boolean.FALSE.equals(isHivPos)) {
			return "Non Reactive / Negative";
		}
		return "Unknown";
	}

	private static String csvEscape(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		boolean needsQuoting = value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0
				|| value.indexOf('\r') >= 0;
		String escaped = value.replace("\"", "\"\"");
		return needsQuoting ? "\"" + escaped + "\"" : escaped;
	}
}
