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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

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

import com.iemr.mmu.repo.login.MasterVanRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentIssueRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentOrderRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.ItemStockEntryRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.StockTransferRepo;
import com.iemr.mmu.utils.CookieUtil;

class DownloadDataFromServerTransactionalImplTest {

	@Mock
	private MasterVanRepo masterVanRepo;
	@Mock
	private IndentRepo indentRepo;
	@Mock
	private IndentOrderRepo indentOrderRepo;
	@Mock
	private IndentIssueRepo indentIssueRepo;
	@Mock
	private StockTransferRepo stockTransferRepo;
	@Mock
	private ItemStockEntryRepo itemStockEntryRepo;
	@Mock
	private CookieUtil cookieUtil;

	@InjectMocks
	private DownloadDataFromServerTransactionalImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(service, "dataSyncTransactionDownloadUrl", "http://central/download");
		ReflectionTestUtils.setField(service, "dataSyncProcessedFlagUpdate", "http://central/flag");
	}

	/** The central server's download payload plus its acknowledgement of the flag update. */
	private MockedConstruction<RestTemplate> centralAnswering(String downloadBody) {
		return mockConstruction(RestTemplate.class, (mock, context) -> {
			when(mock.exchange(eq("http://central/download"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(downloadBody, HttpStatus.OK));
			when(mock.exchange(eq("http://central/flag"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>("{\"statusCode\":200,\"data\":{}}", HttpStatus.OK));
		});
	}

	@Test
	@DisplayName("an empty download leaves the van's stock tables untouched")
	void downloadTransactionalData_leavesTheStockTablesUntouchedWhenThereIsNothingToDownload() throws Exception {
		when(masterVanRepo.getFacilityID(1)).thenReturn(5);

		try (MockedConstruction<RestTemplate> rest = centralAnswering("{\"data\":[]}")) {
			assertEquals(1, service.downloadTransactionalData(1, "auth", "token"));
		}
		verify(indentRepo, never()).saveAll(any());
	}

	@Test
	@DisplayName("a downloaded indent is stored against the van's own row when one already exists")
	void downloadTransactionalData_reusesTheVanRowOfAnAlreadyDownloadedIndent() throws Exception {
		when(masterVanRepo.getFacilityID(1)).thenReturn(5);
		when(indentRepo.searchBySyncFacilityIDAndVanSerialNo(anyInt(), anyLong())).thenReturn(9L);
		when(indentIssueRepo.searchBySyncFacilityIDAndVanSerialNo(anyInt(), anyLong())).thenReturn(null);
		when(itemStockEntryRepo.searchBySyncFacilityIDAndVanSerialNo(anyInt(), anyLong())).thenReturn(null);

		String payload = "{\"data\":[{\"indentID\":1,\"indentIssueID\":1,\"itemStockEntryID\":1,"
				+ "\"syncFacilityID\":5,\"vanSerialNo\":2}]}";

		try (MockedConstruction<RestTemplate> rest = centralAnswering(payload)) {
			assertEquals(1, service.downloadTransactionalData(1, "auth", "token"));
		}
		verify(indentRepo).saveAll(any());
		verify(indentIssueRepo).saveAll(any());
		verify(itemStockEntryRepo).saveAll(any());
	}

	@Test
	@DisplayName("a van without a facility mapping cannot download anything")
	void downloadTransactionalData_failsForAVanWithoutAFacilityMapping() {
		when(masterVanRepo.getFacilityID(1)).thenReturn(null);

		Exception thrown = assertThrows(Exception.class, () -> service.downloadTransactionalData(1, "auth", "token"));
		assertEquals("Facility mapping for this van is either missing/wrong...", thrown.getMessage());
	}

	@Test
	@DisplayName("a rejected flag update still leaves the downloaded rows stored")
	void downloadTransactionalData_storesTheRowsEvenWhenTheFlagUpdateIsRejected() throws Exception {
		when(masterVanRepo.getFacilityID(1)).thenReturn(5);

		String payload = "{\"data\":[{\"indentID\":1,\"syncFacilityID\":5,\"vanSerialNo\":2}]}";

		try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class, (mock, context) -> {
			when(mock.exchange(eq("http://central/download"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(payload, HttpStatus.OK));
			when(mock.exchange(eq("http://central/flag"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>("{\"statusCode\":500}", HttpStatus.OK));
		})) {
			assertEquals(1, service.downloadTransactionalData(1, "auth", "token"));
		}
		verify(indentRepo).saveAll(any());
	}

	@Test
	@DisplayName("a flag update with no body still leaves the downloaded rows stored")
	void downloadTransactionalData_storesTheRowsEvenWhenTheFlagUpdateHasNoBody() throws Exception {
		when(masterVanRepo.getFacilityID(1)).thenReturn(5);

		String payload = "{\"data\":[{\"indentID\":1,\"syncFacilityID\":5,\"vanSerialNo\":2}]}";

		try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class, (mock, context) -> {
			when(mock.exchange(eq("http://central/download"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(payload, HttpStatus.OK));
			when(mock.exchange(eq("http://central/flag"), eq(HttpMethod.POST), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(HttpStatus.OK));
		})) {
			assertEquals(1, service.downloadTransactionalData(1, "auth", "token"));
		}
	}
}
