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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;

class DataSyncRepositoryCentralQueryTest {

	private DataSource dataSource;

	@BeforeEach
	void setUp() {
		dataSource = Mockito.mock(DataSource.class);
	}

	private List<Map<String, Object>> oneRow() {
		Map<String, Object> row = new HashMap<>();
		row.put("VanSerialNo", 1);
		return new ArrayList<>(Collections.singletonList(row));
	}

	/** A JdbcTemplate that answers every read with the given rows. */
	private MockedConstruction<JdbcTemplate> databaseReturning(List<Map<String, Object>> rows) {
		return mockConstruction(JdbcTemplate.class, (mock, context) -> {
			when(mock.queryForList(anyString(), org.mockito.ArgumentMatchers.any(Object[].class))).thenReturn(rows);
			when(mock.queryForList(anyString())).thenReturn(rows);
			
			when(mock.batchUpdate(anyString(), any(List.class))).thenReturn(new int[] { 1 });
		});
	}

	private MockedConstruction<JdbcTemplate> databaseFailing() {
		return mockConstruction(JdbcTemplate.class, (mock, context) -> {
			when(mock.queryForList(anyString(), org.mockito.ArgumentMatchers.any(Object[].class)))
					.thenThrow(new RuntimeException("db down"));
			when(mock.queryForList(anyString())).thenThrow(new RuntimeException("db down"));
			
			when(mock.batchUpdate(anyString(), any(List.class))).thenThrow(new RuntimeException("db down"));
		});
	}

	@Nested
	@DisplayName("DataSyncRepositoryCentral")
	class Central {

		private DataSyncRepositoryCentral repository;

		@BeforeEach
		void setUp() {
			repository = new DataSyncRepositoryCentral();
			ReflectionTestUtils.setField(repository, "dataSource", dataSource);
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_matchesAClinicalRowOnItsVan() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_benvisitdetail", "1", "2",
						"VanSerialNo", 0));

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0)).queryForList(query.capture(), org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getValue().endsWith("AND VanID = ?"), query.getValue());
			}
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_matchesAStockRowOnItsFacility() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_indent", "1", "2",
						"VanSerialNo", 5));

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0)).queryForList(query.capture(), org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getValue().endsWith("AND SyncFacilityID = ?"), query.getValue());
			}
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_reportsARowThatIsNotOnTheServerYet() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(new ArrayList<>())) {
				assertEquals(0, repository.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_benvisitdetail", "1", "2",
						"VanSerialNo", 0));
			}
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_rejectsAnIdentifierThatIsNotWhitelisted() {
			assertThrows(IllegalArgumentException.class, () -> repository
					.checkRecordIsAlreadyPresentOrNot("evil_schema", "t_benvisitdetail", "1", "2", "VanSerialNo", 0));
			assertThrows(IllegalArgumentException.class, () -> repository
					.checkRecordIsAlreadyPresentOrNot("db_iemr", "evil_table", "1", "2", "VanSerialNo", 0));
			assertThrows(IllegalArgumentException.class, () -> repository
					.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_benvisitdetail", "1", "2", "van;drop", 0));
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_reportsADatabaseThatCannotBeRead() {
			try (MockedConstruction<JdbcTemplate> db = databaseFailing()) {
				RuntimeException thrown = assertThrows(RuntimeException.class, () -> repository
						.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_benvisitdetail", "1", "2", "VanSerialNo", 0));
				assertTrue(thrown.getMessage().contains("Failed to check record existence"));
			}
		}

		@Test
		void syncDataToCentralDB_appliesTheBatchAndReportsTheRowCounts() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertArrayEquals(new int[] { 1 }, repository.syncDataToCentralDB("db_iemr", "t_benvisitdetail",
						"VanSerialNo", "INSERT INTO db_iemr.t_benvisitdetail(VanSerialNo) VALUES (?)",
						new ArrayList<>(Collections.singletonList(new Object[] { 1 }))));
			}
		}

		@Test
		void syncDataToCentralDB_reportsABatchTheDatabaseRejected() {
			try (MockedConstruction<JdbcTemplate> db = databaseFailing()) {
				RuntimeException thrown = assertThrows(RuntimeException.class,
						() -> repository.syncDataToCentralDB("db_iemr", "t_benvisitdetail", "VanSerialNo", "INSERT",
								new ArrayList<>(Collections.singletonList(new Object[] { 1 }))));
				assertTrue(thrown.getMessage().contains("Batch sync failed"));
			}
		}

		@Test
		void getMasterDataFromTable_readsEveryRowOfAnAllVanMaster() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo,VanID", "A",
						null, 1, 2).size());
			}
		}

		@Test
		void getMasterDataFromTable_narrowsAVanMasterToItsVan() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", "V", null, 1, 2);

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0)).queryForList(query.capture(), org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getValue().endsWith("WHERE VanID = ?"), query.getValue());
			}
		}

		@Test
		void getMasterDataFromTable_narrowsAProviderMasterToItsProvider() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", "P", null, 1, 2);

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0)).queryForList(query.capture(), org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getValue().endsWith("WHERE ProviderServiceMapID = ?"), query.getValue());
			}
		}

		@Test
		void getMasterDataFromTable_readsOnlyWhatChangedSinceTheLastDownload() {
			Timestamp lastDownload = Timestamp.valueOf("2024-01-01 00:00:00");

			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", "V", lastDownload, 1, 2);
				repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", "P", lastDownload, 1, 2);
				repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", "A", lastDownload, 1, 2);

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0), Mockito.times(3)).queryForList(query.capture(),
						org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getAllValues().get(2).endsWith("WHERE LastModDate >= ?"), query.getValue());
			}
		}

		@Test
		void getMasterDataFromTable_readsTheWholeTableWhenNoMasterTypeWasGiven() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository
						.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", null, null, 1, 2).size());
			}
		}

		@Test
		void getMasterDataFromTable_rejectsADateFormattedColumn() {
			// The column list is split on commas first, so a date_format column is never
			// seen whole: with its format argument the format arrives as a column name of
			// its own, and without one the closing bracket fails the identifier check.
			assertThrows(IllegalArgumentException.class, () -> repository.getMasterDataFromTable("db_iemr",
					"t_benvisitdetail", "date_format(CreatedDate,\'%Y\')", "A", null, 1, 2));
			assertThrows(IllegalArgumentException.class, () -> repository.getMasterDataFromTable("db_iemr",
					"t_benvisitdetail", "date_format(CreatedDate)", "A", null, 1, 2));
		}

		@Test
		void getMasterDataFromTable_rejectsAnIdentifierThatIsNotWhitelisted() {
			assertThrows(IllegalArgumentException.class,
					() -> repository.getMasterDataFromTable("evil", "t_benvisitdetail", "VanSerialNo", "A", null, 1, 2));
			assertThrows(IllegalArgumentException.class,
					() -> repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo;drop", "A", null, 1, 2));
			assertThrows(IllegalArgumentException.class,
					() -> repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "  ", "A", null, 1, 2));
		}

		@Test
		void getMasterDataFromTable_reportsADatabaseThatCannotBeRead() {
			try (MockedConstruction<JdbcTemplate> db = databaseFailing()) {
				RuntimeException thrown = assertThrows(RuntimeException.class, () -> repository
						.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", "A", null, 1, 2));
				assertTrue(thrown.getMessage().contains("Failed to fetch master data"));
			}
		}

		private SyncUploadDataDigester digester(String schema, String table, String columns) {
			SyncUploadDataDigester digester = new SyncUploadDataDigester();
			digester.setSchemaName(schema);
			digester.setTableName(table);
			digester.setServerColumns(columns);
			return digester;
		}

		@Test
		void getBatchForBenDetails_readsOnePageOfBeneficiaryDetails() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository.getBatchForBenDetails(
						digester("db_identity", "i_beneficiarydetails", "BeneficiaryDetailsId"), " WHERE VanID = 1 ",
						10, 0).size());
			}
		}

		@Test
		void getBatchForBenDetails_rejectsAnIdentifierThatIsNotWhitelisted() {
			assertThrows(IllegalArgumentException.class, () -> repository
					.getBatchForBenDetails(digester("evil", "i_beneficiarydetails", "Id"), " WHERE 1=1 ", 10, 0));
		}

		@Test
		void getBatchForBenDetails_reportsADatabaseThatCannotBeRead() {
			try (MockedConstruction<JdbcTemplate> db = databaseFailing()) {
				RuntimeException thrown = assertThrows(RuntimeException.class,
						() -> repository.getBatchForBenDetails(
								digester("db_identity", "i_beneficiarydetails", "BeneficiaryDetailsId"),
								" WHERE VanID = 1 ", 10, 0));
				assertTrue(thrown.getMessage().contains("Failed to fetch batch data"));
			}
		}
	}

	@Nested
	@DisplayName("DataSyncRepositoryCentralDownload")
	class Download {

		private DataSyncRepositoryCentralDownload repository;

		@BeforeEach
		void setUp() {
			repository = new DataSyncRepositoryCentralDownload();
			ReflectionTestUtils.setField(repository, "dataSource", dataSource);
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_matchesAClinicalRowOnItsVan() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_benvisitdetail", "1", "2",
						"VanSerialNo", 0));

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0)).queryForList(query.capture(), org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getValue().endsWith("VanID = ?"), query.getValue());
			}
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_matchesAStockRowOnItsFacility() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertEquals(1, repository.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_indentissue", "1", "2",
						"VanSerialNo", 5));

				ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
				verify(db.constructed().get(0)).queryForList(query.capture(), org.mockito.ArgumentMatchers.any(Object[].class));
				assertTrue(query.getValue().endsWith("SyncFacilityID = ?"), query.getValue());
			}
		}

		@Test
		void checkRecordIsAlreadyPresentOrNot_reportsARowThatIsNotOnTheServerYet() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(new ArrayList<>())) {
				assertEquals(0, repository.checkRecordIsAlreadyPresentOrNot("db_iemr", "t_benvisitdetail", "1", "2",
						"VanSerialNo", 0));
			}
		}

		@Test
		void syncDataToCentralDB_appliesAnInsertBatch() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertArrayEquals(new int[] { 1 }, repository.syncDataToCentralDB("db_iemr", "t_benvisitdetail",
						"VanSerialNo", "INSERT INTO db_iemr.t_benvisitdetail(VanSerialNo) VALUES (?)",
						new ArrayList<>(Collections.singletonList(new Object[] { 1 }))));
			}
		}

		@Test
		void syncDataToCentralDB_appliesAnUpdateBatch() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertArrayEquals(new int[] { 1 },
						repository.syncDataToCentralDB("db_iemr", "t_benvisitdetail", "VanSerialNo,VanID",
								"UPDATE db_iemr.t_benvisitdetail SET VanSerialNo = ?",
								new ArrayList<>(Collections.singletonList(new Object[] { 1 }))));
			}
		}

		@Test
		void syncDataToCentralDB_appliesAnUpdateBatchWithoutServerColumns() {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertArrayEquals(new int[] { 1 }, repository.syncDataToCentralDB("db_iemr", "t_benvisitdetail", null,
						"UPDATE db_iemr.t_benvisitdetail SET VanSerialNo = ?",
						new ArrayList<>(Collections.singletonList(new Object[] { 1 }))));
			}
		}

		@Test
		void getMasterDataFromTable_readsEachMasterTypeWithAndWithoutALastDownloadDate() throws Exception {
			Timestamp lastDownload = Timestamp.valueOf("2024-01-01 00:00:00");

			try (MockedConstruction<JdbcTemplate> db = mockConstruction(JdbcTemplate.class, (mock, context) -> {
				when(mock.queryForList(anyString())).thenReturn(oneRow());
				when(mock.queryForList(anyString(), org.mockito.ArgumentMatchers.any(Object[].class)))
						.thenReturn(oneRow());
			})) {
				for (String masterType : List.of("A", "V", "P")) {
					assertEquals(1, repository
							.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", masterType, lastDownload, 1, 2)
							.size());
					assertEquals(1, repository
							.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", masterType, null, 1, 2).size());
				}
			}
		}

		@Test
		void getMasterDataFromTable_readsNothingWhenNoMasterTypeWasGiven() throws Exception {
			try (MockedConstruction<JdbcTemplate> db = databaseReturning(oneRow())) {
				assertTrue(repository.getMasterDataFromTable("db_iemr", "t_benvisitdetail", "VanSerialNo", null, null, 1, 2)
						.isEmpty());
			}
		}
	}
}
