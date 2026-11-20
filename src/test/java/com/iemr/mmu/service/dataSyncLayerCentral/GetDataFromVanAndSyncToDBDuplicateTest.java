package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.service.dataSyncActivity.SyncResult;

/**
 * Central must not store a van row twice (Kurupam van 415, i_beneficiarydetails_rmnch, 24 Oct: the same upload
 * arrived twice). With a unique key on (VanID, VanSerialNo) the second insert fails on a duplicate key; the row is
 * then updated instead, and the upload still succeeds.
 */
class GetDataFromVanAndSyncToDBDuplicateTest {

	private static final String SCHEMA = "db_identity";
	private static final String TABLE = "i_beneficiarydetails_rmnch";
	private static final String COLUMNS = "BeneficiaryRegID,VanSerialNo,VanID";

	private GetDataFromVanAndSyncToDBImpl service;
	private DataSyncRepositoryCentral repo;

	@BeforeEach
	void setUp() {
		service = new GetDataFromVanAndSyncToDBImpl();
		repo = mock(DataSyncRepositoryCentral.class);
		ReflectionTestUtils.setField(service, "dataSyncRepositoryCentral", repo);
	}

	private static Map<String, Object> rmnch(long regID, int vanSerialNo) {
		Map<String, Object> m = new HashMap<>();
		m.put("BeneficiaryRegID", regID);
		m.put("VanSerialNo", vanSerialNo);
		m.put("VanID", 415);
		return m;
	}

	private boolean upload(List<SyncResult> results, Map<String, Object>... rows) {
		SyncUploadDataDigester digester = new SyncUploadDataDigester();
		digester.setSchemaName(SCHEMA);
		digester.setTableName(TABLE);
		digester.setVanAutoIncColumnName("VanSerialNo");
		digester.setServerColumns(COLUMNS);
		digester.setSyncedBy("KURUPAMvan1");
		digester.setSyncData(List.of(rows));
		return ReflectionTestUtils.invokeMethod(service, "performGenericTableSync", digester, results);
	}

	private static RuntimeException duplicateKeyError() {
		return new RuntimeException("Batch sync failed",
				new DuplicateKeyException("Duplicate entry '415-1128' for key 'uk_rmnch_van_serial'"));
	}

	@Test
	void rowAlreadyOnCentralIsUpdatedNotInsertedAgain() {
		// both rows look new at the first check; meanwhile the other request inserted 1128
		when(repo.checkRecordIsAlreadyPresentOrNot(SCHEMA, TABLE, "1128", "415", "VanSerialNo", 0)).thenReturn(0, 1);
		when(repo.checkRecordIsAlreadyPresentOrNot(SCHEMA, TABLE, "1129", "415", "VanSerialNo", 0)).thenReturn(0, 0);
		when(repo.syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("INSERT"), anyList()))
				.thenThrow(duplicateKeyError()) // the batch
				.thenReturn(new int[] { 1 }); // 1129 on its own
		when(repo.syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("UPDATE"), anyList()))
				.thenReturn(new int[] { 1 });

		List<SyncResult> results = new ArrayList<>();
		boolean ok = upload(results, rmnch(66808, 1128), rmnch(66809, 1129));

		assertTrue(ok);
		assertEquals(2, results.size());
		assertTrue(results.get(0).isSuccess());
		assertTrue(results.get(1).isSuccess());
		// 1128 updated once, 1129 inserted once on its own after the failed batch
		verify(repo, times(1)).syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("UPDATE"),
				anyList());
		verify(repo, times(2)).syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("INSERT"),
				anyList());
	}

	@Test
	void otherInsertErrorsStillFailTheWholeBatch() {
		when(repo.checkRecordIsAlreadyPresentOrNot(anyString(), anyString(), anyString(), anyString(), anyString(),
				anyInt())).thenReturn(0);
		when(repo.syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("INSERT"), anyList()))
				.thenThrow(new RuntimeException("Batch sync failed",
						new DataIntegrityViolationException("Column 'BeneficiaryRegID' cannot be null")));

		List<SyncResult> results = new ArrayList<>();
		boolean ok = upload(results, rmnch(66808, 1128));

		assertFalse(ok);
		assertFalse(results.get(0).isSuccess());
		verify(repo, never()).syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("UPDATE"),
				anyList());
	}

	@Test
	void duplicateOfAnotherRowIsReportedNotOverwritten() {
		// key clash, but the row of this van is not on central: something else holds the key
		when(repo.checkRecordIsAlreadyPresentOrNot(SCHEMA, TABLE, "1128", "415", "VanSerialNo", 0)).thenReturn(0);
		when(repo.syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("INSERT"), anyList()))
				.thenThrow(duplicateKeyError());

		List<SyncResult> results = new ArrayList<>();
		boolean ok = upload(results, rmnch(66808, 1128));

		assertFalse(ok);
		assertFalse(results.get(0).isSuccess());
		assertTrue(results.get(0).getReason().startsWith("INSERT: "));
		verify(repo, never()).syncDataToCentralDB(eq(SCHEMA), eq(TABLE), eq(COLUMNS), startsWith("UPDATE"),
				anyList());
	}

	@Test
	void duplicateKeyIsRecognisedThroughWrappedExceptions() {
		assertTrue(GetDataFromVanAndSyncToDBImpl.isDuplicateKey(duplicateKeyError()));
		assertTrue(GetDataFromVanAndSyncToDBImpl.isDuplicateKey(new RuntimeException("Batch sync failed",
				new SQLException("Duplicate entry '415-1128' for key 'uk_rmnch_van_serial'", "23000", 1062))));
		// a missing parent row is a constraint error too, but not a duplicate
		assertFalse(GetDataFromVanAndSyncToDBImpl.isDuplicateKey(new RuntimeException("Batch sync failed",
				new SQLException("Cannot add or update a child row", "23000", 1452))));
		assertFalse(GetDataFromVanAndSyncToDBImpl.isDuplicateKey(new RuntimeException("timeout")));
	}
}
