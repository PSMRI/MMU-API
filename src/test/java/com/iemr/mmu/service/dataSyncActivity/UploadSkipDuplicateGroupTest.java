package com.iemr.mmu.service.dataSyncActivity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUtilityClass;

class UploadSkipDuplicateGroupTest {

	private static SyncUtilityClass table(int group, String name) {
		SyncUtilityClass t = new SyncUtilityClass();
		t.setSyncTableGroupID(group);
		t.setTableName(name);
		return t;
	}

	private static List<String> names(List<SyncUtilityClass> tables) {
		return tables.stream().map(SyncUtilityClass::getTableName).collect(Collectors.toList());
	}

	// as on the laptops: inventory tables in group 5 (old lists) and group 9 (complete)
	private final List<SyncUtilityClass> all = List.of(table(4, "t_Prescription"), table(5, "t_Lab_TestResult"),
			table(5, "t_ItemStockEntry"), table(5, "t_ItemStockExit"), table(5, "t_PatientIssue"),
			table(9, "t_itemstockentry"), table(9, "t_itemstockexit"), table(9, "t_patientissue"),
			table(9, "t_stockadjustment"));

	@Test
	void inventoryTablesAreSkippedInGroup5() {
		List<SyncUtilityClass> group5 = all.stream().filter(t -> t.getSyncTableGroupID() == 5).collect(Collectors.toList());

		assertEquals(List.of("t_Lab_TestResult"), names(UploadDataToServerImpl.skipTablesUploadedByLaterGroup(group5, 5, all)));
	}

	@Test
	void group9StillUploadsThem() {
		List<SyncUtilityClass> group9 = all.stream().filter(t -> t.getSyncTableGroupID() == 9).collect(Collectors.toList());

		assertEquals(names(group9), names(UploadDataToServerImpl.skipTablesUploadedByLaterGroup(group9, 9, all)));
	}

	@Test
	void tablesInOneGroupAreUnchanged() {
		List<SyncUtilityClass> group4 = List.of(table(4, "t_Prescription"));

		assertEquals(List.of("t_Prescription"), names(UploadDataToServerImpl.skipTablesUploadedByLaterGroup(group4, 4, all)));
	}
}
