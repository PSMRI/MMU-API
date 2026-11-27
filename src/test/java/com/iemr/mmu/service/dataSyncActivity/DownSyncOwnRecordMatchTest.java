package com.iemr.mmu.service.dataSyncActivity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncRecordAck;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncTableDetail;
import com.iemr.mmu.repo.syncActivity_syncLayer.DownSyncTableDetailRepo;

/**
 * Down-sync sends a van rows it uploaded itself. Those rows have no CentralID on the laptop, so they must be found by
 * VanSerialNo (or, for a visit, VisitCode) and updated in place, never inserted a second time.
 */
class DownSyncOwnRecordMatchTest {

	private static final String SCHEMA = "db_iemr";
	private static final String VISIT_TABLE = "t_benvisitdetail";
	private static final String PK = "BenVisitID";
	private static final int VAN_ID = 454;

	private DownSyncDataFromServerImpl service;
	private DataSyncRepository repo;

	@BeforeEach
	void setUp() {
		service = new DownSyncDataFromServerImpl();
		repo = mock(DataSyncRepository.class);
		ReflectionTestUtils.setField(service, "dataSyncRepository", repo);
		ReflectionTestUtils.setField(service, "downSyncTableDetailRepo", mock(DownSyncTableDetailRepo.class));

		when(repo.resolveLastModColumn(anyString(), anyString())).thenReturn("LastModDate");
		when(repo.getForeignKeysOfTable(anyString(), anyString())).thenReturn(new ArrayList<>());
		// Mockito answers an unstubbed Map with an empty map; the real lookups return null when nothing matches
		when(repo.getLocalRecordForDownSync(anyString(), anyString(), anyString(), any(), any(), anyString(),
				anyBoolean())).thenReturn(null);
		when(repo.getOwnLocalRecordForDownSync(anyString(), anyString(), anyString(), any(), any(), any(), any(),
				anyString())).thenReturn(null);
	}

	private DownSyncTableDetail table(String name) {
		DownSyncTableDetail detail = new DownSyncTableDetail();
		detail.setSchemaName(SCHEMA);
		detail.setTableName(name);
		detail.setVanAutoIncColumnName(PK);
		detail.setTableType("TRANSACTIONAL");
		return detail;
	}

	/** A central visit the way central sends it: numbers arrive as Double through Gson. */
	private Map<String, Object> centralVisit(Long vanSerialNo) {
		Map<String, Object> row = new HashMap<>();
		row.put(PK, 7734d);
		row.put("VanID", (double) VAN_ID);
		row.put("VanSerialNo", vanSerialNo == null ? null : vanSerialNo.doubleValue());
		row.put("VisitCode", 10045400002420d);
		row.put("BeneficiaryRegID", 55001d);
		row.put("LastModDate", "2026-09-28 11:29:23");
		return row;
	}

	private Map<String, Object> ownLocalRow(long localID) {
		Map<String, Object> row = new HashMap<>();
		row.put(PK, localID);
		row.put("CentralID", null);
		row.put("Processed", "P");
		row.put("LastModDate", Timestamp.valueOf("2026-09-28 11:29:23"));
		return row;
	}

	@SuppressWarnings("unchecked")
	private List<DownSyncRecordAck> downSync(DownSyncTableDetail detail, Map<String, Object> centralRow) {
		List<String> columns = new ArrayList<>(centralRow.keySet());
		return (List<DownSyncRecordAck>) ReflectionTestUtils.invokeMethod(service, "saveTransactionalData", detail,
				columns, columns, new ArrayList<>(Arrays.asList(centralRow)), VAN_ID);
	}

	@Test
	void ownVisitMatchedByVanSerialNoIsNotInsertedAgain() {
		when(repo.getOwnLocalRecordForDownSync(eq(SCHEMA), eq(VISIT_TABLE), eq(PK), eq(2420L), eq(VAN_ID), isNull(),
				isNull(), eq("LastModDate"))).thenReturn(ownLocalRow(2420L));

		List<DownSyncRecordAck> acks = downSync(table(VISIT_TABLE), centralVisit(2420L));

		verify(repo, never()).insertDownSyncRecordInLocal(anyString(), any());
		verify(repo).stampCentralIDInLocal(SCHEMA, VISIT_TABLE, PK, 2420L, 7734L);
		assertEquals(1, acks.size());
		assertTrue(acks.get(0).isSuccess());
		assertEquals(2420L, acks.get(0).getVanSerialNo(), "central must be told the original laptop ID");
	}

	@Test
	void visitWithoutVanSerialNoIsMatchedByVisitCode() {
		when(repo.getOwnLocalRecordForDownSync(eq(SCHEMA), eq(VISIT_TABLE), eq(PK), isNull(), eq(VAN_ID),
				eq(10045400002420L), eq(55001L), eq("LastModDate"))).thenReturn(ownLocalRow(2420L));

		List<DownSyncRecordAck> acks = downSync(table(VISIT_TABLE), centralVisit(null));

		verify(repo, never()).insertDownSyncRecordInLocal(anyString(), any());
		assertEquals(2420L, acks.get(0).getVanSerialNo());
	}

	@Test
	void visitCodeIsNotUsedOutsideTheVisitTable() {
		// child tables repeat VisitCode on many rows, so it cannot identify one of them
		downSync(table("t_phy_vitals"), centralVisit(null));

		verify(repo).getOwnLocalRecordForDownSync(eq(SCHEMA), eq("t_phy_vitals"), eq(PK), isNull(), eq(VAN_ID),
				isNull(), isNull(), eq("LastModDate"));
	}

	@Test
	void rowNotOnTheLaptopIsStillInserted() {
		when(repo.insertDownSyncRecordInLocal(anyString(), any())).thenReturn(900L);

		List<DownSyncRecordAck> acks = downSync(table(VISIT_TABLE), centralVisit(2420L));

		verify(repo).insertDownSyncRecordInLocal(anyString(), any());
		assertEquals(900L, acks.get(0).getVanSerialNo());
	}

	@Test
	void rowAlreadyLinkedByCentralIDDoesNotLookForOwnRow() {
		Map<String, Object> linked = ownLocalRow(16211L);
		linked.put("CentralID", 7734L);
		when(repo.getLocalRecordForDownSync(eq(SCHEMA), eq(VISIT_TABLE), eq(PK), eq(7734L), eq(VAN_ID),
				eq("LastModDate"), eq(false))).thenReturn(linked);

		downSync(table(VISIT_TABLE), centralVisit(2420L));

		verify(repo, never()).getOwnLocalRecordForDownSync(anyString(), anyString(), anyString(), any(), any(), any(),
				any(), anyString());
		verify(repo, never()).insertDownSyncRecordInLocal(anyString(), any());
	}

	@Test
	void rowOfAnotherVanIsNotMatchedToThisVansRows() {
		Map<String, Object> otherVan = centralVisit(2420L);
		otherVan.put("VanID", 455d);
		when(repo.insertDownSyncRecordInLocal(anyString(), any())).thenReturn(901L);

		downSync(table(VISIT_TABLE), otherVan);

		verify(repo, never()).getOwnLocalRecordForDownSync(anyString(), anyString(), anyString(), any(), any(), any(),
				any(), anyString());
	}
}
