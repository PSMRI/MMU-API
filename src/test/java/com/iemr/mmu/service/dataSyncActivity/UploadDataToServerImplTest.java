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
package com.iemr.mmu.service.dataSyncActivity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.iemr.mmu.data.syncActivity_syncLayer.DataSyncGroups;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUtilityClass;
import com.iemr.mmu.repo.syncActivity_syncLayer.DataSyncGroupsRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.SyncUtilityClassRepo;
import com.iemr.mmu.repo.login.MasterVanRepo;
import com.iemr.mmu.utils.CookieUtil;

class UploadDataToServerImplTest {

	@Mock
	private DataSyncRepository dataSyncRepository;
	@Mock
	private DataSyncGroupsRepo dataSyncGroupsRepo;
	@Mock
	private MasterVanRepo masterVanRepo;
	@Mock
	private SyncUtilityClassRepo syncutilityClassRepo;
	@Mock
	private CookieUtil cookieUtil;

	@InjectMocks
	private UploadDataToServerImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(service, "dataSyncUploadUrl", "http://central/sync");
		ReflectionTestUtils.setField(service, "BATCH_SIZE", 2);
	}

	private DataSyncGroups group() {
		DataSyncGroups group = new DataSyncGroups();
		group.setSyncTableGroupID(1);
		group.setSyncTableGroupName("Beneficiary");
		return group;
	}

	private SyncUtilityClass table(String tableName) {
		SyncUtilityClass table = new SyncUtilityClass();
		table.setSchemaName("db_iemr");
		table.setTableName(tableName);
		table.setVanColumnName("VanSerialNo,BeneficiaryRegID");
		table.setServerColumnName("VanSerialNo,BeneficiaryRegID");
		table.setVanAutoIncColumnName("VanSerialNo");
		return table;
	}

	private List<Map<String, Object>> rows(int count) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			Map<String, Object> row = new HashMap<>();
			row.put("VanSerialNo", i);
			rows.add(row);
		}
		return rows;
	}

	/** The central server's per-record acknowledgement for a batch of van serial numbers. */
	private String recordsResponse(boolean... outcomes) {
		StringBuilder records = new StringBuilder();
		for (int i = 0; i < outcomes.length; i++) {
			if (i > 0) {
				records.append(",");
			}
			records.append("{\"vanSerialNo\":\"").append(i + 1).append("\",\"success\":").append(outcomes[i])
					.append(outcomes[i] ? "" : ",\"reason\":\"duplicate row\"").append("}");
		}
		return "{\"statusCode\":200,\"errorMessage\":\"Success\",\"data\":{\"records\":[" + records + "]}}";
	}

	/** A server that acknowledges every row of whatever batch it is sent. */
	private MockedConstruction<RestTemplate> serverAcknowledgingEveryRow() {
		return mockConstruction(RestTemplate.class, (mock, context) -> when(
				mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenAnswer(invocation -> {
					org.springframework.http.HttpEntity<?> request = invocation.getArgument(2);
					com.google.gson.JsonArray syncData = com.google.gson.JsonParser
							.parseString(String.valueOf(request.getBody())).getAsJsonObject()
							.getAsJsonArray("syncData");
					boolean[] outcomes = new boolean[syncData.size()];
					java.util.Arrays.fill(outcomes, true);
					return new ResponseEntity<>(recordsResponse(outcomes), HttpStatus.OK);
				}));
	}

	private MockedConstruction<RestTemplate> serverAnswering(String body) {
		return mockConstruction(RestTemplate.class,
				(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
						.thenReturn(new ResponseEntity<>(body, HttpStatus.OK)));
	}

	@Nested
	@DisplayName("uploading a van's data")
	class Upload {

		@Test
		void getDataToSyncToServer_reportsThatThereWasNothingToSync() throws Exception {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(List.of(table("i_beneficiary")));
			when(dataSyncRepository.getDataForGivenSchemaAndTable(anyString(), anyString(), anyString()))
					.thenReturn(new ArrayList<>());

			assertEquals("No data to sync", service.getDataToSyncToServer(1, "nurse", "auth", "token"));
		}

		@Test
		void getDataToSyncToServer_reportsACompletelySuccessfulSync() throws Exception {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(List.of(table("i_beneficiary")));
			when(dataSyncRepository.getDataForGivenSchemaAndTable(anyString(), anyString(), anyString()))
					.thenReturn(rows(2));

			try (MockedConstruction<RestTemplate> rest = serverAnswering(recordsResponse(true, true))) {
				String response = service.getDataToSyncToServer(1, "nurse", "auth", "token");

				assertTrue(response.contains("Data sync completed successfully"));
				assertTrue(response.contains("\"status\" : \"completed\""));
			}
			verify(dataSyncRepository).updateProcessedFlagInVan(eq("db_iemr"), eq("i_beneficiary"), any(),
					eq("VanSerialNo"), eq("nurse"), eq("P"), eq("null"));
		}

		@Test
		void getDataToSyncToServer_reportsAPartiallySuccessfulTable() throws Exception {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(List.of(table("i_beneficiary")));
			when(dataSyncRepository.getDataForGivenSchemaAndTable(anyString(), anyString(), anyString()))
					.thenReturn(rows(2));

			try (MockedConstruction<RestTemplate> rest = serverAnswering(recordsResponse(true, false))) {
				String response = service.getDataToSyncToServer(1, "nurse", "auth", "token");

				assertTrue(response.contains("\"status\" : \"partial\""), response);
				assertTrue(response.contains("\"failedRecords\" : 1"), response);
			}
			verify(dataSyncRepository).updateProcessedFlagInVan(anyString(), anyString(), any(), anyString(),
					anyString(), eq("F"), eq("duplicate row"));
		}

		@Test
		void getDataToSyncToServer_reportsATableThatFailedEntirely() throws Exception {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(List.of(table("i_beneficiary")));
			when(dataSyncRepository.getDataForGivenSchemaAndTable(anyString(), anyString(), anyString()))
					.thenReturn(rows(2));

			try (MockedConstruction<RestTemplate> rest = serverAnswering(recordsResponse(false, false))) {
				String response = service.getDataToSyncToServer(1, "nurse", "auth", "token");

				assertTrue(response.contains("Data sync completed with failures"), response);
				assertTrue(response.contains("\"status\" : \"failed\""), response);
			}
		}

		@Test
		void getDataToSyncToServer_splitsALargeTableIntoBatchesAndARemainder() throws Exception {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(List.of(table("i_beneficiary")));
			when(dataSyncRepository.getDataForGivenSchemaAndTable(anyString(), anyString(), anyString()))
					.thenReturn(rows(3));

			try (MockedConstruction<RestTemplate> rest = serverAcknowledgingEveryRow()) {
				String response = service.getDataToSyncToServer(1, "nurse", "auth", "token");

				assertTrue(response.contains("Data sync completed successfully"), response);
				assertTrue(response.contains("\"totalRecords\" : 3"), response);
			}
		}

		@Test
		void getDataToSyncToServer_stopsTheTableWhenTheServerReportsAnError() throws Exception {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(List.of(table("i_beneficiary")));
			when(dataSyncRepository.getDataForGivenSchemaAndTable(anyString(), anyString(), anyString()))
					.thenReturn(rows(2));

			String error = "{\"statusCode\":500,\"errorMessage\":\"Central server unavailable\"}";
			try (MockedConstruction<RestTemplate> rest = serverAnswering(error)) {
				String response = service.getDataToSyncToServer(1, "nurse", "auth", "token");

				assertTrue(response.contains("Data sync completed with failures"), response);
				assertTrue(response.contains("\"status\" : \"failed\""), response);
			}
			verify(dataSyncRepository).updateProcessedFlagInVan(anyString(), anyString(), any(), anyString(),
					anyString(), eq("F"), eq("Central server unavailable"));
		}
	}

	@Nested
	@DisplayName("sending one batch to the central server")
	class SendBatch {

		@Test
		void syncDataToServer_reportsSuccessForEveryAcknowledgedRecord() throws Exception {
			when(masterVanRepo.getFacilityID(1)).thenReturn(5);

			try (MockedConstruction<RestTemplate> rest = serverAnswering(recordsResponse(true, true))) {
				Map<String, Object> result = service.syncDataToServer(1, "db_iemr", "i_beneficiary", "VanSerialNo",
						"VanSerialNo", rows(2), "nurse", "auth", "token");

				assertEquals("Data successfully synced", result.get("status"));
				assertEquals(2, result.get("successCount"));
				assertEquals(0, result.get("failCount"));
			}
		}

		@Test
		void syncDataToServer_reportsAPartialSuccess() throws Exception {
			try (MockedConstruction<RestTemplate> rest = serverAnswering(recordsResponse(true, false))) {
				assertEquals("Partial success", service.syncDataToServer(1, "db_iemr", "i_beneficiary",
						"VanSerialNo", "VanSerialNo", rows(2), "nurse", "auth", "token").get("status"));
			}
		}

		@Test
		void syncDataToServer_acceptsTheBeneficiaryIdMappingAcknowledgement() throws Exception {
			String body = "{\"statusCode\":200,\"errorMessage\":\"Success\","
					+ "\"data\":{\"response\":\"Data sync success\"}}";

			try (MockedConstruction<RestTemplate> rest = serverAnswering(body)) {
				Map<String, Object> result = service.syncDataToServer(1, "db_iemr", "m_beneficiaryregidmapping",
						"VanSerialNo", "VanSerialNo", rows(2), "nurse", "auth", "token");

				assertEquals("Data successfully synced", result.get("status"));
				assertEquals(2, result.get("successCount"));
			}
		}

		@Test
		void syncDataToServer_reportsAFailedBeneficiaryIdMapping() throws Exception {
			String body = "{\"statusCode\":200,\"errorMessage\":\"Success\",\"data\":{\"response\":\"rejected\"}}";

			try (MockedConstruction<RestTemplate> rest = serverAnswering(body)) {
				Map<String, Object> result = service.syncDataToServer(1, "db_iemr", "m_beneficiaryregidmapping",
						"VanSerialNo", "VanSerialNo", rows(2), "nurse", "auth", "token");

				assertEquals("Sync failed", result.get("status"));
				assertEquals(2, result.get("failCount"));
			}
		}

		@Test
		void syncDataToServer_reportsAnUnparseableServerResponse() throws Exception {
			try (MockedConstruction<RestTemplate> rest = serverAnswering("not json")) {
				Map<String, Object> result = service.syncDataToServer(1, "db_iemr", "i_beneficiary", "VanSerialNo",
						"VanSerialNo", rows(2), "nurse", "auth", "token");

				assertEquals("Sync failed", result.get("status"));
			}
			verify(dataSyncRepository).updateProcessedFlagInVan(anyString(), anyString(), any(), anyString(),
					anyString(), eq("F"), eq("Invalid server response"));
		}

		@Test
		void syncDataToServer_reportsAnEmptyServerResponse() throws Exception {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>(HttpStatus.OK)))) {

				assertEquals("Sync failed", service.syncDataToServer(1, "db_iemr", "i_beneficiary", "VanSerialNo",
						"VanSerialNo", rows(2), "nurse", "auth", "token").get("status"));
			}
			verify(dataSyncRepository).updateProcessedFlagInVan(anyString(), anyString(), any(), anyString(),
					anyString(), eq("F"), eq("Empty server response"));
		}

		@Test
		void syncDataToServer_reportsAServerThatCannotBeReached() throws Exception {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenThrow(new ResourceAccessException("connection refused")))) {

				assertEquals("Sync failed", service.syncDataToServer(1, "db_iemr", "i_beneficiary", "VanSerialNo",
						"VanSerialNo", rows(2), "nurse", "auth", "token").get("status"));
			}
		}

		@Test
		void syncDataToServer_reportsAnUnexpectedFailure() throws Exception {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenThrow(new IllegalStateException("boom")))) {

				assertEquals("Sync failed", service.syncDataToServer(1, "db_iemr", "i_beneficiary", "VanSerialNo",
						"VanSerialNo", rows(2), "nurse", "auth", "token").get("status"));
			}
		}
	}

	@Nested
	@DisplayName("sync helpers")
	class Helpers {

		@Test
		void getVanAndServerColumnList_readsTheTablesOfAGroup() throws Exception {
			List<SyncUtilityClass> tables = List.of(table("i_beneficiary"));
			when(syncutilityClassRepo.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(1, false))
					.thenReturn(tables);

			assertEquals(tables, service.getVanAndServerColumnList(1));
		}

		@Test
		void getVanSerialNoListForSyncedData_joinsTheVanSerialNumbersWithCommas() throws Exception {
			assertEquals("1,2,3", service.getVanSerialNoListForSyncedData("VanSerialNo", rows(3)).toString());
			assertEquals("", service.getVanSerialNoListForSyncedData("VanSerialNo", new ArrayList<>()).toString());
		}

		@Test
		void getDataSyncGroupDetails_serialisesTheConfiguredGroups() {
			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>(List.of(group())));
			assertTrue(service.getDataSyncGroupDetails().contains("Beneficiary"));

			when(dataSyncGroupsRepo.findByDeleted(false)).thenReturn(null);
			assertNull(service.getDataSyncGroupDetails());
		}

		@Test
		void syncResult_carriesTheOutcomeOfOneTable() {
			SyncResult result = new SyncResult("db_iemr", "i_beneficiary", "1", "nurse", false, "duplicate row");

			assertEquals("db_iemr", result.getSchemaName());
			assertEquals("i_beneficiary", result.getTableName());
			assertEquals("1", result.getVanSerialNo());
			assertEquals("nurse", result.getSyncedBy());
			assertEquals(false, result.isSuccess());
			assertEquals("duplicate row", result.getReason());

			result.setSuccess(true);
			assertEquals(true, result.isSuccess());

			SyncResult same = new SyncResult("db_iemr", "i_beneficiary", "1", "nurse", true, "duplicate row");
			assertEquals(same, result);
			assertEquals(same.hashCode(), result.hashCode());
			assertTrue(result.toString().contains("i_beneficiary"));

			result.setSchemaName("db_identity");
			result.setTableName("m_beneficiaryregidmapping");
			result.setVanSerialNo("2");
			result.setSyncedBy("doctor");
			result.setReason(null);
			org.junit.jupiter.api.Assertions.assertNotEquals(same, result);
		}
	}
}
