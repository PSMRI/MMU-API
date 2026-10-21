package com.iemr.mmu.service.dataSyncActivity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncDownloadMaster;

class DownloadMastersFullForLineTest {

	private static final Timestamp COPIED_DATE = Timestamp.valueOf("2026-10-16 13:17:31");

	private static SyncDownloadMaster table(String masterType) {
		SyncDownloadMaster t = new SyncDownloadMaster();
		t.setMasterType(masterType);
		t.setLastDownloadDate(COPIED_DATE);
		return t;
	}

	@Test
	void serviceLineMastersAreDownloadedInFull() {
		SyncDownloadMaster parkingPlace = table("P");

		DownloadDataFromServerImpl.fullDownloadForLineAndVanMasters(parkingPlace);

		assertNull(parkingPlace.getLastDownloadDate());
	}

	@Test
	void vanMastersAreDownloadedInFull() {
		SyncDownloadMaster vanServicePoint = table("v");

		DownloadDataFromServerImpl.fullDownloadForLineAndVanMasters(vanServicePoint);

		assertNull(vanServicePoint.getLastDownloadDate());
	}

	@Test
	void mastersSharedByAllLinesStayIncremental() {
		SyncDownloadMaster villages = table("A");

		DownloadDataFromServerImpl.fullDownloadForLineAndVanMasters(villages);

		assertEquals(COPIED_DATE, villages.getLastDownloadDate());
	}
}
