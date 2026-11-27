package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncRecordAck;

/**
 * Central side of the down-sync fix: an acknowledgement never replaces a VanSerialNo central already holds, and a row
 * uploaded without its VanSerialNo is not sent back to the laptop (it could not be matched there).
 */
class DownSyncCentralKeyTest {

	private DataSyncRepositoryCentralDownload repo;

	@BeforeEach
	void setUp() {
		repo = new DataSyncRepositoryCentralDownload();
		ReflectionTestUtils.setField(repo, "dataSource", mock(DataSource.class));
	}

	private MockedConstruction<JdbcTemplate> database(boolean tableHasSyncColumns) {
		return mockConstruction(JdbcTemplate.class, (mock, context) -> {
			when(mock.queryForList(anyString(), eq(String.class), any(Object[].class)))
					.thenReturn(tableHasSyncColumns ? new ArrayList<>(Collections.singletonList("X"))
							: new ArrayList<>());
			when(mock.queryForList(anyString(), any(Object[].class))).thenReturn(new ArrayList<>());
			when(mock.batchUpdate(anyString(), anyList())).thenReturn(new int[] { 1 });
		});
	}

	@Test
	void ackDoesNotOverwriteAnExistingVanSerialNo() throws Exception {
		try (MockedConstruction<JdbcTemplate> db = database(true)) {
			repo.updateDownSyncFlagPostDownload("db_iemr", "t_benvisitdetail", "BenVisitID",
					Arrays.asList(DownSyncRecordAck.success(7734L, 16211L)));

			ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
			verify(db.constructed().get(db.constructed().size() - 1)).batchUpdate(sql.capture(), anyList());
			assertTrue(sql.getValue().contains("VanSerialNo = COALESCE(VanSerialNo, ?)"), sql.getValue());
		}
	}

	@Test
	void uploadedRowWithoutVanSerialNoIsNotSentBack() throws Exception {
		try (MockedConstruction<JdbcTemplate> db = database(true)) {
			repo.getDownSyncDataFromTable("db_iemr", "t_prescribeddrug", null, "TRANSACTIONAL", 454, "LastModDate",
					"PrescribedDrugID", null, 1000);

			ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
			verify(db.constructed().get(0)).queryForList(sql.capture(), any(Object[].class));
			assertTrue(sql.getValue().contains("NOT ( VanSerialNo IS NULL AND SyncedDate IS NOT NULL )"),
					sql.getValue());
		}
	}

	@Test
	void visitsAreStillSentBackSinceTheLaptopMatchesThemByVisitCode() throws Exception {
		try (MockedConstruction<JdbcTemplate> db = database(true)) {
			repo.getDownSyncDataFromTable("db_iemr", "t_benvisitdetail", null, "TRANSACTIONAL", 454, "LastModDate",
					"BenVisitID", null, 1000);

			ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
			verify(db.constructed().get(0)).queryForList(sql.capture(), any(Object[].class));
			assertFalse(sql.getValue().contains("SyncedDate"), sql.getValue());
		}
	}

	@Test
	void tableWithoutSyncColumnsIsQueriedAsBefore() throws Exception {
		try (MockedConstruction<JdbcTemplate> db = database(false)) {
			repo.getDownSyncDataFromTable("db_iemr", "t_somecentraltable", null, "TRANSACTIONAL", 454,
					"LastModDate", "ID", null, 1000);

			ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
			verify(db.constructed().get(0)).queryForList(sql.capture(), any(Object[].class));
			assertFalse(sql.getValue().contains("SyncedDate"), sql.getValue());
		}
	}
}
