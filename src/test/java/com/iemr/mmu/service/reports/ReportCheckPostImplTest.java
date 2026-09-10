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
package com.iemr.mmu.service.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.iemr.mmu.repo.reports.ReportMasterRepo;

class ReportCheckPostImplTest {

	@Mock
	private ReportMasterRepo reportMasterRepo;

	@InjectMocks
	private ReportCheckPostImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// The output mapper's Gson builder is initialised by its constructor.
		new com.iemr.mmu.utils.mapper.OutputMapper();
	}

	/** A report request for the given report, with every parameter the reports need. */
	private String request(int reportID, Integer vanID) {
		return "{\"reportID\":" + reportID + ",\"fromDate\":\"2024-01-01T00:00:00.000\","
				+ "\"toDate\":\"2024-01-31T00:00:00.000\",\"vanID\":" + vanID + ",\"providerServiceMapID\":4}";
	}

	/** One report row, wide enough for any of the report mappers. */
	private ArrayList<Object[]> oneRow() {
		ArrayList<Object[]> rows = new ArrayList<>();
		rows.add(new Object[60]);
		return rows;
	}

	private void stubEveryReportQuery(ArrayList<Object[]> rows) {
		when(reportMasterRepo.get_report_PatientAttended(any(), any(), anyInt(), any())).thenReturn(rows);
		when(reportMasterRepo.get_report_TestConducted(any(), any(), anyInt(), any())).thenReturn(rows);
		when(reportMasterRepo.get_report_LabTestResult(any(), any(), anyInt(), any())).thenReturn(rows);
		when(reportMasterRepo.get_report_PatientInfo(any(), any(), anyInt(), any())).thenReturn(rows);
		when(reportMasterRepo.get_report_SP_ChildrenCases(any(), any(), anyInt(), any())).thenReturn(rows);
		when(reportMasterRepo.get_report_SP_ANC(any(), any(), anyInt(), any())).thenReturn(rows);
		when(reportMasterRepo.get_report_SP_ANCHighRisk(any(), any(), anyInt(), any())).thenReturn(rows);
	}

	@Test
	@DisplayName("the report master list is served for a service")
	void getReportMaster_servesTheReportsOfAService() throws Exception {
		when(reportMasterRepo.findByServiceIDAndDeletedOrderByReportNameAsc(2, false)).thenReturn(new ArrayList<>());

		assertEquals("[]", service.getReportMaster(2));
	}

	@ParameterizedTest(name = "report {0} is served from its own query")
	@ValueSource(ints = { 1, 2, 5, 6, 7, 8, 9 })
	void reportHandler_servesEveryConfiguredReport(int reportID) throws Exception {
		stubEveryReportQuery(oneRow());

		assertTrue(service.reportHandler(request(reportID, 3)).startsWith("["));
	}

	@ParameterizedTest(name = "report {0} is served as an empty report when nothing matched")
	@ValueSource(ints = { 1, 2, 5, 6, 7, 8, 9 })
	void reportHandler_servesAnEmptyReportWhenNothingMatched(int reportID) throws Exception {
		stubEveryReportQuery(new ArrayList<>());

		assertEquals("[]", service.reportHandler(request(reportID, 3)));
	}

	@Test
	@DisplayName("a van id of zero widens the report to every van")
	void reportHandler_widensTheReportToEveryVanForAVanIdOfZero() throws Exception {
		stubEveryReportQuery(new ArrayList<>());

		assertEquals("[]", service.reportHandler(request(1, 0)));
		verify(reportMasterRepo).get_report_PatientAttended(any(), any(), anyInt(),
				org.mockito.ArgumentMatchers.isNull());
	}

	@Test
	@DisplayName("an unknown report id is served as an empty response")
	void reportHandler_servesAnEmptyResponseForAnUnknownReport() throws Exception {
		assertEquals("", service.reportHandler(request(99, 3)));
	}

	@Test
	@DisplayName("a request without a report id is rejected")
	void reportHandler_rejectsARequestWithoutAReportId() {
		Exception thrown = assertThrows(Exception.class, () -> service.reportHandler("{}"));
		assertEquals("Invalid/NULL report ID", thrown.getMessage());
	}

	@ParameterizedTest(name = "report {0} is rejected when a parameter is missing")
	@ValueSource(ints = { 1, 2, 5, 6, 7, 8, 9 })
	void reportHandler_rejectsAReportWithAMissingParameter(int reportID) {
		String incomplete = "{\"reportID\":" + reportID + ",\"providerServiceMapID\":4}";

		Exception thrown = assertThrows(Exception.class, () -> service.reportHandler(incomplete));
		assertEquals("Some parameter/parameters is/are missing.", thrown.getMessage());
	}
}
