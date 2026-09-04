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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncDownloadMaster;
import com.iemr.mmu.data.syncActivity_syncLayer.TempVan;
import com.iemr.mmu.repo.syncActivity_syncLayer.SyncDownloadMasterRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.TempVanRepo;
import com.iemr.mmu.utils.CookieUtil;

class DownloadDataFromServerImplTest {

	@Mock
	private SyncDownloadMasterRepo syncDownloadMasterRepo;
	@Mock
	private DataSyncRepository dataSyncRepository;
	@Mock
	private TempVanRepo tempVanRepo;
	@Mock
	private CookieUtil cookieUtil;

	@InjectMocks
	private DownloadDataFromServerImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(service, "dataSyncDownloadUrl", "http://central/master");
		ReflectionTestUtils.setField(service, "benGenUrlCentral", "http://central/benGen");
		ReflectionTestUtils.setField(service, "benImportUrlLocal", "http://local/benImport");
		// The download counters are static, so each test starts from a settled state.
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "progressCounter", 0);
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "totalCounter", 0);
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "failedCounter", 0);
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "failedMasters", new StringBuilder());
	}

	private ArrayList<SyncDownloadMaster> oneMasterTable() {
		SyncDownloadMaster master = new SyncDownloadMaster();
		master.setDownloadMasterTableID(1);
		master.setSchemaName("db_iemr");
		master.setTableName("m_gender");
		master.setVanColumnName("GenderID,GenderName");
		ArrayList<SyncDownloadMaster> masters = new ArrayList<>();
		masters.add(master);
		return masters;
	}

	private MockedConstruction<RestTemplate> centralAnswering(String body) {
		return mockConstruction(RestTemplate.class,
				(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
						.thenReturn(new ResponseEntity<>(body, HttpStatus.OK)));
	}

	@Test
	@DisplayName("a master download is accepted and the progress counters are reset")
	void downloadMasterDataFromServer_acceptsTheRequestAndResetsTheProgressCounters() throws Exception {
		when(syncDownloadMasterRepo.getDownloadTables()).thenReturn(oneMasterTable());

		assertEquals(" Master download started ", service.downloadMasterDataFromServer("auth", "token", 1, 2));

		// The download itself runs on its own thread pool; what the caller sees is the
		// reset progress counters.
		assertEquals(1, ReflectionTestUtils.getField(DownloadDataFromServerImpl.class, "totalCounter"));
		assertEquals(0, ReflectionTestUtils.getField(DownloadDataFromServerImpl.class, "failedCounter"));
	}

	@Test
	@DisplayName("a second download request while one is running reports that it is in progress")
	void downloadMasterDataFromServer_reportsADownloadThatIsStillRunning() throws Exception {
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "totalCounter", 5);
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "progressCounter", 1);

		assertEquals("inProgress", service.downloadMasterDataFromServer("auth", "token", 1, 2));
	}

	@Test
	@DisplayName("the download progress is reported as a percentage")
	void getDownloadStatus_reportsTheProgressAsAPercentage() {
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "totalCounter", 4);
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "progressCounter", 1);
		ReflectionTestUtils.setField(DownloadDataFromServerImpl.class, "failedCounter", 2);

		Map<String, Object> status = service.getDownloadStatus();

		assertEquals(25.0, status.get("percentage"));
		assertEquals(2, status.get("failedMasterCount"));
	}

	@Test
	@DisplayName("the van assigned to this installation is reported")
	void getVanDetailsForMasterDownload_reportsTheOnlyVanOfThisInstallation() throws Exception {
		TempVan van = new TempVan();
		van.setVanID(1);
		when(tempVanRepo.getVanID()).thenReturn(new ArrayList<>(Collections.singletonList(van)));

		assertTrue(service.getVanDetailsForMasterDownload().contains("\"vanID\":1"));
	}

	@Test
	@DisplayName("more than one configured van is reported as a configuration error")
	void getVanDetailsForMasterDownload_failsWhenMoreThanOneVanIsConfigured() {
		when(tempVanRepo.getVanID()).thenReturn(new ArrayList<>(List.of(new TempVan(), new TempVan())));

		Exception thrown = assertThrows(Exception.class, () -> service.getVanDetailsForMasterDownload());
		assertTrue(thrown.getMessage().contains("more than 1 van"));
	}

	@Test
	@DisplayName("a generated beneficiary id block is imported into the van")
	void callCentralAPIToGenerateBenIDAndimportToLocal_importsTheGeneratedIdBlock() throws Exception {
		String central = "{\"statusCode\":200,\"data\":[{\"beneficiaryID\":1}]}";
		String local = "{\"statusCode\":200,\"data\":{}}";

		try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class, (mock, context) -> {
			when(mock.exchange(eq("http://central/benGen"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(central, HttpStatus.OK));
			when(mock.exchange(eq("http://local/benImport"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(local, HttpStatus.OK));
		})) {
			assertEquals(2, service.callCentralAPIToGenerateBenIDAndimportToLocal("{}", "auth", "serverAuth", "token"));
		}
	}

	@Test
	@DisplayName("a block the van could not import stops short of a completed import")
	void callCentralAPIToGenerateBenIDAndimportToLocal_stopsShortWhenTheVanCannotImport() throws Exception {
		String central = "{\"statusCode\":200,\"data\":[{\"beneficiaryID\":1}]}";

		try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class, (mock, context) -> {
			when(mock.exchange(eq("http://central/benGen"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(central, HttpStatus.OK));
			when(mock.exchange(eq("http://local/benImport"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>("{\"statusCode\":500}", HttpStatus.OK));
		})) {
			assertEquals(1, service.callCentralAPIToGenerateBenIDAndimportToLocal("{}", "auth", "serverAuth", "token"));
		}
	}

	@Test
	@DisplayName("a central server that generates no ids leaves nothing to import")
	void callCentralAPIToGenerateBenIDAndimportToLocal_leavesNothingToImportWhenNoIdWasGenerated() throws Exception {
		try (MockedConstruction<RestTemplate> rest = centralAnswering("{\"statusCode\":500}")) {
			assertEquals(0, service.callCentralAPIToGenerateBenIDAndimportToLocal("{}", "auth", "serverAuth", "token"));
		}
	}

	@Test
	@DisplayName("a central server that cannot be reached is reported to the caller")
	void callCentralAPIToGenerateBenIDAndimportToLocal_reportsACentralServerThatCannotBeReached() {
		try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
				(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
						.thenThrow(new RuntimeException("connection refused")))) {

			Exception thrown = assertThrows(Exception.class, () -> service
					.callCentralAPIToGenerateBenIDAndimportToLocal("{}", "auth", "serverAuth", "token"));
			assertTrue(thrown.getMessage().contains("Error while generating"));
		}
	}
}
