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
package com.iemr.mmu.service.benid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class BenIdAutoTopUpServiceTest {

	private static final String GENERATE = "http://central/common-api/beneficiary/generateBeneficiaryIDs";
	private static final String IMPORT = "http://local/id/saveGeneratedBenIDToLocalServer";
	private static final String COUNT = "http://local/id/checkAvailablBenIDLocalServer";

	@TempDir
	Path dataDir;

	private BenIdAutoTopUpService service;
	private CentralAuthService centralAuth;
	private MockRestServiceServer server;

	@BeforeEach
	void setUp() throws Exception {
		centralAuth = mock(CentralAuthService.class);
		when(centralAuth.isEnabled()).thenReturn(true);
		when(centralAuth.getVanID()).thenReturn(403);
		when(centralAuth.getState()).thenReturn(CentralAuthService.State.CONNECTED);
		when(centralAuth.getCentralSessionKey()).thenReturn("SESSION-KEY");

		service = new BenIdAutoTopUpService();
		ReflectionTestUtils.setField(service, "centralAuthService", centralAuth);
		ReflectionTestUtils.setField(service, "benGenUrlCentral", GENERATE);
		ReflectionTestUtils.setField(service, "benImportUrlLocal", IMPORT);
		ReflectionTestUtils.setField(service, "benCheckUrlLocal", COUNT);
		ReflectionTestUtils.setField(service, "lowerLimit", 2000L);
		ReflectionTestUtils.setField(service, "batchSize", 1000L);
		ReflectionTestUtils.setField(service, "bootstrapSize", 10000L);
		ReflectionTestUtils.setField(service, "maxPerRequest", 1000L);
		ReflectionTestUtils.setField(service, "dataDir", dataDir.toString());
		ReflectionTestUtils.setField(service, "retryDelaysMs", new long[] { 0L });
		ReflectionTestUtils.setField(service, "connectTimeoutMs", 1000);
		ReflectionTestUtils.setField(service, "readTimeoutMs", 1000);
		service.init();
		server = MockRestServiceServer.bindTo((RestTemplate) ReflectionTestUtils.getField(service, "restTemplate"))
				.build();
	}

	@AfterEach
	void releaseLock() {
		while (BenIdAutoTopUpService.GENERATION_LOCK.isHeldByCurrentThread()) {
			BenIdAutoTopUpService.GENERATION_LOCK.unlock();
		}
	}

	private void expectCount(long n) {
		server.expect(requestTo(COUNT))
				.andRespond(withSuccess("{\"statusCode\":200,\"data\":{\"response\":\"" + n + "\"}}",
						MediaType.APPLICATION_JSON));
	}

	private static String ids(int n) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 1; i <= n; i++) {
			sb.append(i > 1 ? "," : "").append("{\"benRegId\":").append(5000 + i)
					.append(",\"beneficiaryId\":").append(900000000000L + i).append("}");
		}
		return sb.append("]").toString();
	}

	private void expectGenerate(long requested, int returned) {
		server.expect(requestTo(GENERATE)).andExpect(header("Authorization", "SESSION-KEY"))
				.andExpect(content().string(Matchers.containsString("\"benIDRequired\":" + requested)))
				.andExpect(content().string(Matchers.containsString("\"vanID\":403")))
				.andRespond(withSuccess("{\"statusCode\":200,\"data\":" + ids(returned) + "}",
						MediaType.APPLICATION_JSON));
	}

	private void expectImportOk() {
		server.expect(requestTo(IMPORT)).andExpect(content().string(Matchers.containsString("\"vanID\":403")))
				.andRespond(withSuccess("{\"statusCode\":200,\"data\":{\"response\":\"ok\"}}",
						MediaType.APPLICATION_JSON));
	}

	private void run() {
		ReflectionTestUtils.invokeMethod(service, "runOnce", true);
	}

	private Path pending() {
		return dataDir.resolve("benid-pending-import.json");
	}

	@Test
	void doesNothingWhenDisabled() throws Exception {
		when(centralAuth.isEnabled()).thenReturn(false);
		server.expect(ExpectedCount.never(), requestTo(COUNT));
		run();
		server.verify();
		verify(centralAuth, never()).getCentralSessionKey();
	}

	@Test
	void emptyPoolGetsBootstrapSize() {
		ReflectionTestUtils.setField(service, "bootstrapSize", 800L);
		expectCount(0);
		expectGenerate(800, 3);
		expectImportOk();
		expectCount(3);
		run();
		server.verify();
		assertFalse(Files.exists(pending()), "pending file removed after a successful import");
	}

	@Test
	void lowPoolGetsBatchSize() {
		expectCount(1999);
		expectGenerate(1000, 2);
		expectImportOk();
		expectCount(2001);
		run();
		server.verify();
	}

	@Test
	void healthyPoolDoesNotCallCentral() throws Exception {
		expectCount(2000);
		server.expect(ExpectedCount.never(), requestTo(GENERATE));
		run();
		server.verify();
		verify(centralAuth, never()).getCentralSessionKey();
	}

	@Test
	void noCentralTokenWaitsForLogin() throws Exception {
		when(centralAuth.getCentralSessionKey()).thenReturn(null);
		expectCount(0);
		server.expect(ExpectedCount.never(), requestTo(GENERATE));
		run();
		server.verify();
		assertTrue(((String) ReflectionTestUtils.getField(service, "lastError")).contains("waiting for a user login"));
	}

	@Test
	void centralErrorLeavesNothingBehind() {
		expectCount(0);
		server.expect(requestTo(GENERATE)).andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
		run();
		server.verify();
		assertFalse(Files.exists(pending()));
		assertTrue(ReflectionTestUtils.getField(service, "lastError") != null);
	}

	@Test
	void failedLocalImportKeepsIdsAndImportsThemNextRun() throws Exception {
		ReflectionTestUtils.setField(service, "bootstrapSize", 800L);
		expectCount(0);
		expectGenerate(800, 3);
		server.expect(requestTo(IMPORT)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
		// next run: pending IDs are imported first, then the pool is healthy
		expectImportOk();
		expectCount(2500);
		run();
		assertTrue(Files.exists(pending()), "IDs received from central must be kept");
		assertEquals(3, Files.readString(pending()).split("benRegId").length - 1);

		run();
		server.verify();
		assertFalse(Files.exists(pending()));
	}

	@Test
	void skipsWhileManualGenerationRuns() {
		BenIdAutoTopUpService.GENERATION_LOCK.lock();
		try {
			Thread other = new Thread(this::run);
			server.expect(ExpectedCount.never(), requestTo(COUNT));
			other.start();
			other.join(5000);
			server.verify();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			BenIdAutoTopUpService.GENERATION_LOCK.unlock();
		}
	}

	@Test
	void bigDownloadIsSplitIntoRequestsOfAtMost1000() {
		ReflectionTestUtils.setField(service, "bootstrapSize", 2500L);
		expectCount(0);
		expectGenerate(1000, 2);
		expectImportOk();
		expectGenerate(1000, 2);
		expectImportOk();
		expectGenerate(500, 2);
		expectImportOk();
		expectCount(2500);
		run();
		server.verify();
	}

	@Test
	void pendingFileThatKeepsFailingIsSetAsideAndTopUpsContinue() throws Exception {
		Files.writeString(pending(), "[{\"benRegId\":12");  // half-written by a power cut
		for (int i = 0; i < 3; i++) {
			server.expect(requestTo(IMPORT)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
		}
		expectCount(2500);
		run();
		run();
		assertTrue(Files.exists(pending()), "kept while retries are left");
		run();
		server.verify();
		assertFalse(Files.exists(pending()), "set aside after 3 failures");
		try (java.util.stream.Stream<Path> files = Files.list(dataDir)) {
			assertTrue(files.anyMatch(f -> f.getFileName().toString().startsWith("benid-pending-import.failed-")));
		}
	}

	@Test
	void retryWaitIsSpreadPlusMinus20Percent() {
		ReflectionTestUtils.setField(service, "retryDelaysMs", new long[] { 10000L });
		for (int i = 0; i < 20; i++) {
			long before = System.currentTimeMillis();
			ReflectionTestUtils.invokeMethod(service, "onFailure", new RuntimeException("down"));
			long wait = (long) ReflectionTestUtils.getField(service, "nextAttemptAt") - before;
			assertTrue(wait >= 7990 && wait <= 12010, "wait " + wait + " ms is outside 8-12 s");
		}
	}
}
