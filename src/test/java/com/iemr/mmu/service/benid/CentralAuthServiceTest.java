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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class CentralAuthServiceTest {

	private static final String LOGIN = "http://central/common-api/user/userAuthenticate";
	private static final String REFRESH = "http://central/common-api/user/refreshToken";
	private static final String SESSION = "http://central/common-api/user/getLoginResponse";

	@TempDir
	Path tokenDir;

	private CentralAuthService service;
	private MockRestServiceServer central;

	@BeforeEach
	void setUp() {
		service = newService(true, 403);
	}

	private CentralAuthService newService(boolean enforce, int vanID) {
		CentralAuthService s = new CentralAuthService();
		ReflectionTestUtils.setField(s, "enforceVanID", enforce);
		ReflectionTestUtils.setField(s, "vanID", vanID);
		ReflectionTestUtils.setField(s, "centralLoginUrl", LOGIN);
		ReflectionTestUtils.setField(s, "centralRefreshTokenUrl", REFRESH);
		ReflectionTestUtils.setField(s, "centralSessionUrl", SESSION);
		ReflectionTestUtils.setField(s, "tokenDir", tokenDir.toString());
		ReflectionTestUtils.setField(s, "tokenSecret", "test-secret");
		ReflectionTestUtils.setField(s, "connectTimeoutMs", 1000);
		ReflectionTestUtils.setField(s, "readTimeoutMs", 1000);
		s.init();
		RestTemplate rt = (RestTemplate) ReflectionTestUtils.getField(s, "restTemplate");
		central = MockRestServiceServer.bindTo(rt).build();
		return s;
	}

	private void expectLoginOk(String refreshToken) {
		central.expect(requestTo(LOGIN)).andExpect(method(HttpMethod.POST))
				.andExpect(header("User-Agent", "okhttp/4.12.0"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"doLogout\":false")))
				.andRespond(withSuccess("{\"statusCode\":200,\"data\":{\"refreshToken\":\"" + refreshToken
						+ "\",\"key\":\"k\"}}", MediaType.APPLICATION_JSON));
	}

	private void expectLoginRejected() {
		central.expect(requestTo(LOGIN)).andRespond(withSuccess(
				"{\"statusCode\":5002,\"errorMessage\":\"Invalid username or password\"}", MediaType.APPLICATION_JSON));
	}

	@Test
	void disabledWithoutVanId() {
		CentralAuthService off = newService(true, 0);
		assertFalse(off.isEnabled());
		assertEquals(CentralAuthService.State.DISABLED, off.getState());
		assertFalse(off.capture("stoptb1", "pw", true));
		central.verify(); // nothing sent to central
	}

	@Test
	void disabledWhenEnforceFlagOff() {
		CentralAuthService off = newService(false, 403);
		assertFalse(off.isEnabled());
		assertFalse(off.capture("stoptb1", "pw", true));
		central.verify();
	}

	@Test
	void captureStoresOnlyEncryptedTokenWithOwnerOnlyPermissions() throws Exception {
		expectLoginOk("RT-secret-123");

		assertTrue(service.capture("stoptb1", "encrypted-pw", false));

		Path file = tokenDir.resolve("central-refresh-token");
		String stored = Files.readString(file, StandardCharsets.UTF_8);
		assertFalse(stored.contains("RT-secret-123"), "token must not be stored in plain text");
		assertFalse(stored.contains("encrypted-pw"), "password must never be stored");
		assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(file)));
		assertEquals(CentralAuthService.State.CONNECTED, service.getState());
		central.verify();
	}

	@Test
	void tokenIsReadBackAfterRestart() {
		expectLoginOk("RT-1");
		service.capture("stoptb1", "pw", false);

		CentralAuthService restarted = newService(true, 403);
		assertTrue(restarted.hasToken());
		assertEquals(CentralAuthService.State.CONNECTED, restarted.getState());
	}

	@Test
	void corruptTokenFileMeansNewLoginNeeded() throws Exception {
		Files.writeString(tokenDir.resolve("central-refresh-token"), "not-a-valid-token");
		CentralAuthService s = newService(true, 403);
		assertFalse(s.hasToken());
		assertEquals(CentralAuthService.State.NOT_CONNECTED, s.getState());
	}

	@Test
	void secondLoginIsSkippedWhileTokenWorks() {
		expectLoginOk("RT-1");
		service.capture("stoptb1", "pw", false);

		assertFalse(service.capture("stoptb1", "pw", false));
		central.verify(); // only one central login
	}

	@Test
	void rejectedPasswordIsNeverSentAgainWithoutReconnect() {
		expectLoginRejected();

		assertFalse(service.capture("stoptb1", "wrong", false));
		assertEquals(CentralAuthService.State.PASSWORD_MISMATCH, service.getState());
		// next laptop login with the same user must not reach central (account lock risk)
		assertFalse(service.capture("stoptb1", "wrong", false));
		central.verify();
	}

	@Test
	void dataSyncReconnectRetriesAfterRejection() {
		expectLoginRejected();
		expectLoginOk("RT-2");

		service.capture("stoptb1", "wrong", false);
		assertTrue(service.capture("stoptb1", "central-pw", true));
		assertEquals(CentralAuthService.State.CONNECTED, service.getState());
		central.verify();
	}

	@Test
	void centralDownIsNotTreatedAsWrongPassword() {
		central.expect(requestTo(LOGIN)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
		expectLoginOk("RT-3");

		assertFalse(service.capture("stoptb1", "pw", false));
		// network problem: the next login tries again
		assertTrue(service.capture("stoptb1", "pw", false));
		central.verify();
	}

	@Test
	void sessionKeyFromRefreshTokenAndTokenRotation() throws Exception {
		expectLoginOk("RT-old");
		central.expect(requestTo(REFRESH))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("RT-old")))
				.andRespond(withSuccess("{\"jwtToken\":\"JWT-1\",\"refreshToken\":\"RT-new\"}",
						MediaType.APPLICATION_JSON));
		central.expect(requestTo(SESSION)).andExpect(header("Jwttoken", "JWT-1"))
				.andRespond(withSuccess("{\"statusCode\":200,\"data\":{\"key\":\"SESSION-KEY\"}}",
						MediaType.APPLICATION_JSON));
		// the next refresh must use the rotated token
		central.expect(requestTo(REFRESH))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("RT-new")))
				.andRespond(withSuccess("{\"jwtToken\":\"JWT-2\"}", MediaType.APPLICATION_JSON));
		central.expect(requestTo(SESSION)).andRespond(withSuccess(
				"{\"statusCode\":200,\"data\":\"{\\\"key\\\":\\\"KEY-2\\\"}\"}", MediaType.APPLICATION_JSON));

		service.capture("stoptb1", "pw", false);
		String fileBefore = Files.readString(tokenDir.resolve("central-refresh-token"));

		assertEquals("SESSION-KEY", service.getCentralSessionKey());
		assertFalse(fileBefore.equals(Files.readString(tokenDir.resolve("central-refresh-token"))),
				"rotated refresh token must be saved");
		assertEquals("KEY-2", service.getCentralSessionKey());
		central.verify();
	}

	@Test
	void revokedTokenIsDeletedAndReconnectNeeded() {
		expectLoginOk("RT-1");
		central.expect(requestTo(REFRESH)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

		service.capture("stoptb1", "pw", false);

		assertNull(assertDoesNotThrowValue());
		assertFalse(service.hasToken());
		assertFalse(Files.exists(tokenDir.resolve("central-refresh-token")));
		assertEquals(CentralAuthService.State.RECONNECT_NEEDED, service.getState());
		central.verify();
	}

	@Test
	void noTokenMeansNoCentralCall() throws Exception {
		central.expect(ExpectedCount.never(), requestTo(REFRESH));
		assertNull(service.getCentralSessionKey());
		central.verify();
	}

	private String assertDoesNotThrowValue() {
		try {
			return service.getCentralSessionKey();
		} catch (Exception e) {
			throw new AssertionError("revoked token must not throw", e);
		}
	}

	@Test
	void rejectedPasswordIsRememberedAfterRestart() {
		expectLoginRejected();
		service.capture("stoptb1", "wrong", false);
		central.verify();

		CentralAuthService restarted = newService(true, 403);
		// laptop login after the restart must not reach central with the rejected password
		assertFalse(restarted.capture("stoptb1", "wrong", false));
		central.verify();
	}

	@Test
	void successfulReconnectClearsTheRejectedMarker() {
		expectLoginRejected();
		expectLoginOk("RT-9");
		service.capture("stoptb1", "wrong", false);
		assertTrue(service.capture("stoptb1", "central-pw", true));
		assertFalse(Files.exists(tokenDir.resolve("central-login-rejected")));
		central.verify();
	}

	@Test
	void rejectedMarkerHoldsNoPassword() throws Exception {
		expectLoginRejected();
		service.capture("stoptb1", "secret-pw", false);
		String marker = Files.readString(tokenDir.resolve("central-login-rejected"));
		assertEquals("stoptb1", marker.trim());
		assertFalse(marker.contains("secret-pw"));
	}
}
