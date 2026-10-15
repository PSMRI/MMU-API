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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.service.benid.BenIdAutoTopUpService;
import com.iemr.mmu.service.benid.CentralAuthService;

class AutoSyncServiceTest {

	private AutoSyncService service;
	private CentralAuthService centralAuth;
	private UploadDataToServerImpl upload;
	private DownloadDataFromServerImpl download;
	private DataSyncRepository repo;
	private BenIdAutoTopUpService benId;

	@BeforeEach
	void setUp() throws Exception {
		centralAuth = mock(CentralAuthService.class);
		upload = mock(UploadDataToServerImpl.class);
		download = mock(DownloadDataFromServerImpl.class);
		repo = mock(DataSyncRepository.class);
		benId = mock(BenIdAutoTopUpService.class);
		when(centralAuth.isEnabled()).thenReturn(true);
		when(centralAuth.getVanID()).thenReturn(403);
		when(centralAuth.getConnectedUser()).thenReturn("stoptb1");
		when(centralAuth.getCentralSessionKey()).thenReturn("SESSION-KEY");
		when(repo.getVanProviderServiceMapID(403)).thenReturn(1726);

		service = new AutoSyncService();
		ReflectionTestUtils.setField(service, "centralAuthService", centralAuth);
		ReflectionTestUtils.setField(service, "uploadDataToServerImpl", upload);
		ReflectionTestUtils.setField(service, "downloadDataFromServerImpl", download);
		ReflectionTestUtils.setField(service, "dataSyncRepository", repo);
		ReflectionTestUtils.setField(service, "benIdAutoTopUpService", benId);
		ReflectionTestUtils.setField(service, "uploadEnabled", true);
		ReflectionTestUtils.setField(service, "masterDownloadEnabled", true);
	}

	@Test
	void uploadUsesCentralKeyAndConnectedOperator() throws Exception {
		when(upload.getDataToSyncToServer(403, "stoptb1", "SESSION-KEY", null)).thenReturn("Data successfully synced");

		service.uploadOnce();

		verify(upload).getDataToSyncToServer(403, "stoptb1", "SESSION-KEY", null);
		verify(benId).triggerNow();
		assertEquals("Data successfully synced", service.status().get("lastUploadResult"));
	}

	@Test
	void noCentralConnectionSkipsUpload() throws Exception {
		when(centralAuth.getCentralSessionKey()).thenReturn(null);

		service.uploadOnce();

		verify(upload, never()).getDataToSyncToServer(anyInt(), anyString(), anyString(), any());
		assertEquals("No central connection - waiting for a user login", service.status().get("lastError"));
	}

	@Test
	void masterDownloadUsesVanServiceLine() throws Exception {
		when(download.downloadMasterDataFromServer("SESSION-KEY", null, 403, 1726)).thenReturn("success");

		service.masterDownloadOnce();

		verify(download).downloadMasterDataFromServer("SESSION-KEY", null, 403, 1726);
		assertEquals("success", service.status().get("lastMasterDownloadResult"));
	}

	@Test
	void manualMasterDownloadRunningIsNotCountedAsARun() throws Exception {
		when(download.downloadMasterDataFromServer("SESSION-KEY", null, 403, 1726)).thenReturn("inProgress");

		service.masterDownloadOnce();

		assertEquals(0L, service.status().getLong("lastMasterDownloadAt"));
	}

	@Test
	void statusSumsRejectedRows() throws Exception {
		when(repo.getSyncFailures()).thenReturn(List.of(
				Map.of("tableName", "t_patientissue", "reason", "Duplicate", "failedRows", 2L),
				Map.of("tableName", "t_prescription", "reason", "Timeout", "failedRows", 3L)));

		JSONObject status = service.status();

		assertEquals(5L, status.getLong("failedRows"));
		assertEquals(2, status.getJSONArray("failures").length());
	}

	@Test
	void disabledLaptopDoesNothing() throws Exception {
		when(centralAuth.isEnabled()).thenReturn(false);

		service.scheduledUpload();
		service.scheduledMasterDownload();

		verify(centralAuth, never()).getCentralSessionKey();
	}
}
