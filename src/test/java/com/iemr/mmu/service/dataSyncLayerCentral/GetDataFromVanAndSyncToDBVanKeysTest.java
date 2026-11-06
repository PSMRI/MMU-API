package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class GetDataFromVanAndSyncToDBVanKeysTest {

	// server list of t_itemstockentry (van list has ItemStockEntryID in VanSerialNo's place)
	private static final List<String> STOCK_ENTRY_SERVER = Arrays.asList("FacilityID", "ItemID", "BatchNo",
			"QuantityInHand", "VanSerialNo", "VanID", "SyncFacilityID");

	@Test
	void vanSerialNoTakesTheVanRowIdWhenNotSentByName() {
		Map<String, Object> record = new HashMap<>(Map.of("ItemStockEntryID", 6, "FacilityID", 428, "VanID", 428));

		GetDataFromVanAndSyncToDBImpl.fillVanKeys(record, STOCK_ENTRY_SERVER, "ItemStockEntryID");

		assertEquals(6, record.get("VanSerialNo"));
	}

	@Test
	void syncFacilityIdTakesTheRowStoreWhenNotSent() {
		Map<String, Object> record = new HashMap<>(Map.of("ItemStockExitID", 9, "FacilityID", 428));

		GetDataFromVanAndSyncToDBImpl.fillVanKeys(record, Arrays.asList("ItemStockEntryID", "FacilityID",
				" VanSerialNo", "SyncFacilityID"), "ItemStockExitID");

		assertEquals(428, record.get("SyncFacilityID"));
		assertEquals(9, record.get("VanSerialNo"));
	}

	@Test
	void valuesSentByTheVanAreKept() {
		Map<String, Object> record = new HashMap<>(
				Map.of("PatientIssueID", 3, "VanSerialNo", 3, "FacilityID", 428, "SyncFacilityID", 428));

		GetDataFromVanAndSyncToDBImpl.fillVanKeys(record, Arrays.asList("VanSerialNo", "SyncFacilityID"),
				"PatientIssueID");

		assertEquals(3, record.get("VanSerialNo"));
		assertEquals(428, record.get("SyncFacilityID"));
	}

	@Test
	void columnsNotOnTheServerAreNotAdded() {
		Map<String, Object> record = new HashMap<>(Map.of("BenVisitID", 1991, "VanID", 428));

		GetDataFromVanAndSyncToDBImpl.fillVanKeys(record, Arrays.asList("BeneficiaryRegID", "VanSerialNo", "VanID"),
				"BenVisitID");

		assertEquals(1991, record.get("VanSerialNo"));
		assertFalse(record.containsKey("SyncFacilityID"));
	}

	@Test
	void missingVanIdLeavesVanSerialNoEmpty() {
		Map<String, Object> record = new HashMap<>(Map.of("FacilityID", 428));

		GetDataFromVanAndSyncToDBImpl.fillVanKeys(record, STOCK_ENTRY_SERVER, "ItemStockEntryID");

		assertNull(record.get("VanSerialNo"));
	}
}
