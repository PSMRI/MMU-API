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
package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;

class GetDataFromVanAndSyncToDBImplTest {

	@Mock
	private DataSyncRepositoryCentral dataSyncRepositoryCentral;

	@InjectMocks
	private GetDataFromVanAndSyncToDBImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	/** A one-row upload request for the given table. */
	private String request(String tableName, String rowJson) {
		return "{\"schemaName\":\"db_iemr\",\"tableName\":\"" + tableName + "\","
				+ "\"vanAutoIncColumnName\":\"VanSerialNo\",\"serverColumns\":\"VanSerialNo,VanID,BeneficiaryRegID\","
				+ "\"syncedBy\":\"nurse\",\"facilityID\":5,\"syncData\":[" + rowJson + "]}";
	}

	private String row(String tableName) {
		return "{\"tableName\":\"" + tableName + "\",\"VanSerialNo\":1,\"VanID\":2,\"BeneficiaryRegID\":3}";
	}

	private SyncUploadDataDigester digester(String tableName, List<Map<String, Object>> syncData) {
		SyncUploadDataDigester digester = new SyncUploadDataDigester();
		digester.setSchemaName("db_iemr");
		digester.setTableName(tableName);
		digester.setVanAutoIncColumnName("VanSerialNo");
		digester.setServerColumns("VanSerialNo,VanID,BeneficiaryRegID");
		digester.setSyncedBy("nurse");
		digester.setSyncData(syncData);
		return digester;
	}

	@Nested
	@DisplayName("syncing an uploaded batch")
	class SyncBatch {

		@Test
		void syncDataToServer_rejectsARequestWithoutATableName() throws Exception {
			String request = "{\"schemaName\":\"db_iemr\",\"syncData\":[]}";

			assertEquals("Error: Invalid sync request.", service.syncDataToServer(request, "auth"));
		}

		@Test
		void syncDataToServer_marksTheProvisionedBeneficiaryIdMappings() throws Exception {
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			String request = "{\"schemaName\":\"db_iemr\",\"tableName\":\"m_beneficiaryregidmapping\","
					+ "\"serverColumns\":\"SyncedBy\",\"syncedBy\":\"nurse\","
					+ "\"syncData\":[{\"BenRegId\":1,\"BeneficiaryID\":2,\"VanID\":3}]}";

			assertEquals("Sync successful for m_beneficiaryregidmapping.", service.syncDataToServer(request, "auth"));
		}

		@Test
		void syncDataToServer_reportsAPartiallyProvisionedBeneficiaryIdMapping() throws Exception {
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[0]);

			String request = "{\"schemaName\":\"db_iemr\",\"tableName\":\"m_beneficiaryregidmapping\","
					+ "\"serverColumns\":\"SyncedBy\",\"syncedBy\":\"nurse\","
					+ "\"syncData\":[{\"BenRegId\":1,\"BeneficiaryID\":2,\"VanID\":3}]}";

			assertEquals("Sync failed for m_beneficiaryregidmapping.", service.syncDataToServer(request, "auth"));
		}

		@Test
		void syncDataToServer_skipsIncompleteBeneficiaryIdMappings() throws Exception {
			String request = "{\"schemaName\":\"db_iemr\",\"tableName\":\"m_beneficiaryregidmapping\","
					+ "\"serverColumns\":\"SyncedBy\",\"syncedBy\":\"nurse\",\"syncData\":[{\"BenRegId\":1}]}";

			assertEquals("Sync successful for m_beneficiaryregidmapping.", service.syncDataToServer(request, "auth"));
			verify(dataSyncRepositoryCentral, org.mockito.Mockito.never()).syncDataToCentralDB(anyString(),
					anyString(), any(), anyString(), any());
		}

		@Test
		void syncDataToServer_reportsAFailedBeneficiaryIdMappingUpdate() throws Exception {
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenThrow(new RuntimeException("db down"));

			String request = "{\"schemaName\":\"db_iemr\",\"tableName\":\"m_beneficiaryregidmapping\","
					+ "\"serverColumns\":\"SyncedBy\",\"syncedBy\":\"nurse\","
					+ "\"syncData\":[{\"BenRegId\":1,\"BeneficiaryID\":2,\"VanID\":3}]}";

			assertEquals("Sync failed for m_beneficiaryregidmapping.", service.syncDataToServer(request, "auth"));
		}

		@Test
		void syncDataToServer_insertsARowThatIsNotOnTheServerYet() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(0);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			String response = service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth");

			assertTrue(response.contains("\"success\":true"), response);
			ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
			verify(dataSyncRepositoryCentral).syncDataToCentralDB(anyString(), anyString(), any(), query.capture(),
					any());
			assertTrue(query.getValue().startsWith("INSERT INTO db_iemr.t_benvisitdetail"), query.getValue());
		}

		@Test
		void syncDataToServer_updatesARowThatIsAlreadyOnTheServer() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(1);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			String response = service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth");

			assertTrue(response.contains("\"success\":true"), response);
			ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
			verify(dataSyncRepositoryCentral).syncDataToCentralDB(anyString(), anyString(), any(), query.capture(),
					any());
			assertTrue(query.getValue().contains("AND VanID = ?"), query.getValue());
		}

		@Test
		void syncDataToServer_reportsARowTheServerRejected() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(0);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 0 });

			String response = service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth");

			assertTrue(response.contains("Insert failed"), response);
		}

		@Test
		void syncDataToServer_reportsARowThatMatchedNothingOnUpdate() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(1);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 0 });

			assertTrue(service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth")
					.contains("No matching row"));
		}

		@Test
		void syncDataToServer_reportsAnInsertThatFailedForTheWholeBatch() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(0);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenThrow(new RuntimeException("Duplicate entry '1' for key 'PRIMARY'"));

			assertTrue(service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth")
					.contains("INSERT: Duplicate key: PRIMARY"));
		}

		@Test
		void syncDataToServer_reportsAnUpdateThatFailedForTheWholeBatch() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(1);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenThrow(new RuntimeException("Database connection timeout"));

			assertTrue(service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth")
					.contains("UPDATE: Database connection timeout"));
		}

		@Test
		void syncDataToServer_reportsARowWhoseExistenceCouldNotBeChecked() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("db down"));

			assertTrue(service.syncDataToServer(request("t_benvisitdetail", row("t_benvisitdetail")), "auth")
					.contains("Record check failed"));
		}

		@Test
		void syncDataToServer_fallsBackToAGenericSyncForATableOutsideTheKnownGroups() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(0);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			assertTrue(service.syncDataToServer(request("t_unknown_table", row("t_other_table")), "auth")
					.contains("Data sync completed"));
		}

		@Test
		void syncDataToServer_readsTheDateFormattedColumnNamesTheVanSends() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(0);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			String rowWithFormattedDate = "{\"tableName\":\"t_benvisitdetail\",\"VanSerialNo\":1,\"VanID\":2,"
					+ "\"date_format(BeneficiaryRegID,'%Y')\":3}";

			assertTrue(service.syncDataToServer(request("t_benvisitdetail", rowWithFormattedDate), "auth")
					.contains("\"success\":true"));
		}

		@Test
		void syncDataToServer_marksAStockRowAsProcessedForItsOwnFacility() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(1);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			String stockRow = "{\"tableName\":\"t_itemstockentry\",\"VanSerialNo\":1,\"VanID\":2,"
					+ "\"FacilityID\":5,\"SyncFacilityID\":5}";

			assertTrue(service.syncDataToServer(request("t_itemstockentry", stockRow), "auth")
					.contains("\"success\":true"));
			ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
			verify(dataSyncRepositoryCentral).syncDataToCentralDB(anyString(), anyString(), any(), query.capture(),
					any());
			assertTrue(query.getValue().contains("AND SyncFacilityID = ?"), query.getValue());
		}

		@Test
		void syncDataToServer_marksAnIndentRowAsProcessedForItsOwnFacility() throws Exception {
			when(dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyInt())).thenReturn(0);
			when(dataSyncRepositoryCentral.syncDataToCentralDB(anyString(), anyString(), any(), anyString(), any()))
					.thenReturn(new int[] { 1 });

			for (String tableAndKey : List.of("t_indent:FromFacilityID", "t_indentorder:FromFacilityID",
					"t_indentissue:ToFacilityID", "t_stocktransfer:TransferToFacilityID")) {
				String table = tableAndKey.split(":")[0];
				String key = tableAndKey.split(":")[1];
				String stockRow = "{\"tableName\":\"" + table + "\",\"VanSerialNo\":1,\"VanID\":2,\"" + key
						+ "\":5}";

				assertTrue(service.syncDataToServer(request(table, stockRow), "auth").contains("Data sync completed"));
			}
		}
	}

	@Nested
	@DisplayName("sync queries and helpers")
	class QueriesAndHelpers {

		@Test
		void getQueryToUpdateDataToServerDB_matchesOnTheVanForAClinicalTable() {
			String query = service.getQueryToUpdateDataToServerDB("db_iemr", "VanSerialNo,VanID", "t_benvisitdetail");

			assertTrue(query.startsWith("UPDATE db_iemr.t_benvisitdetail SET"), query);
			assertTrue(query.contains("VanSerialNo = ?, VanID = ?"), query);
			assertTrue(query.contains("AND VanID = ?"), query);
		}

		@Test
		void getQueryToUpdateDataToServerDB_matchesOnTheFacilityForAStockTable() {
			assertTrue(service.getQueryToUpdateDataToServerDB("db_iemr", "VanSerialNo", "t_patientissue")
					.contains("AND SyncFacilityID = ?"));
		}

		@Test
		void getQueryToUpdateDataToServerDB_toleratesATableWithoutColumns() {
			assertTrue(service.getQueryToUpdateDataToServerDB("db_iemr", null, "t_benvisitdetail")
					.contains("UPDATE db_iemr.t_benvisitdetail SET  WHERE VanSerialNo = ?"));
		}

		@Test
		void update_I_BeneficiaryDetails_for_processed_in_batches_reportsTheBatchOutcome() {
			when(dataSyncRepositoryCentral.getBatchForBenDetails(any(), anyString(), anyInt(), anyInt()))
					.thenReturn(new ArrayList<>());

			assertEquals("data sync passed",
					service.update_I_BeneficiaryDetails_for_processed_in_batches(
							digester("i_beneficiarydetails", new ArrayList<>())));
		}

		@Test
		void update_I_BeneficiaryDetails_for_processed_in_batches_reportsAFailedRead() {
			when(dataSyncRepositoryCentral.getBatchForBenDetails(any(), anyString(), anyInt(), anyInt()))
					.thenThrow(new RuntimeException("db down"));

			assertTrue(service
					.update_I_BeneficiaryDetails_for_processed_in_batches(
							digester("i_beneficiarydetails", new ArrayList<>()))
					.contains("Error fetching data"));
		}
	}
}
