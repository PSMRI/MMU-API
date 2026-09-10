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
package com.iemr.mmu.utils.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.redis.RedisSessionException;
import com.iemr.mmu.utils.sessionobject.SessionObject;

class ValidatorTest {

	private SessionObject sessionObject;
	private Validator validator;

	@BeforeEach
	void setUp() {
		sessionObject = mock(SessionObject.class);
		validator = new Validator();
		validator.setSessionObject(sessionObject);
	}

	private JSONObject loginResponse(String ipAddress) throws Exception {
		JSONObject response = new JSONObject();
		response.put("loginIPAddress", ipAddress);
		return response;
	}

	@Test
	@DisplayName("a fresh login stores the session and reports success")
	void updateCacheObj_storesTheSessionOfAFreshLogin() throws Exception {
		when(sessionObject.getSessionObject("key")).thenReturn(null);

		JSONObject result = validator.updateCacheObj(loginResponse("10.0.0.1"), "key", "ipKey");

		assertEquals("key", result.get("key"));
		assertEquals("login success", result.get("sessionStatus"));
		verify(sessionObject).setSessionObject(anyString(), anyString());
	}

	@Test
	@DisplayName("a session store that is unreachable still reports the login outcome")
	void updateCacheObj_reportsTheLoginOutcomeWhenTheStoreIsUnreachable() throws Exception {
		when(sessionObject.getSessionObject("key")).thenThrow(new RedisSessionException("redis down"));

		JSONObject result = validator.updateCacheObj(loginResponse("10.0.0.1"), "key", "ipKey");

		assertEquals("login success", result.get("sessionStatus"));
	}

	@Test
	@DisplayName("a login from a second IP is reported when IP validation is on")
	void updateCacheObj_reportsALoginFromASecondIpAddress() throws Exception {
		ReflectionTestUtils.setField(Validator.class, "enableIPValidation", true);
		try {
			when(sessionObject.getSessionObject("key"))
					.thenReturn(new JSONObject().put("loginIPAddress", "10.0.0.9").toString());

			JSONObject result = validator.updateCacheObj(loginResponse("10.0.0.1"), "key", "ipKey");

			assertTrue(((String) result.get("sessionStatus")).contains("10.0.0.9"));
		} finally {
			ReflectionTestUtils.setField(Validator.class, "enableIPValidation", false);
		}
	}

	@Test
	@DisplayName("the stored session is read back for a key")
	void getSessionObject_readsTheStoredSession() throws Exception {
		when(sessionObject.getSessionObject("key")).thenReturn("{}");

		assertEquals("{}", validator.getSessionObject("key"));
	}

	@Test
	@DisplayName("a stored session passes the key check")
	void checkKeyExists_acceptsAStoredSession() throws Exception {
		when(sessionObject.getSessionObject("key"))
				.thenReturn(new JSONObject().put("loginIPAddress", "10.0.0.1").toString());

		validator.checkKeyExists("key", "10.0.0.1");
	}

	@Test
	@DisplayName("a session that is not stored fails the key check")
	void checkKeyExists_rejectsASessionThatIsNotStored() throws Exception {
		when(sessionObject.getSessionObject("key")).thenReturn(null);

		IEMRException thrown = assertThrows(IEMRException.class, () -> validator.checkKeyExists("key", "10.0.0.1"));
		assertEquals("Session is expired. Please login again.", thrown.getMessage());
	}

	@Test
	@DisplayName("a session opened from another IP fails the key check when IP validation is on")
	void checkKeyExists_rejectsASessionFromAnotherIpAddress() throws Exception {
		ReflectionTestUtils.setField(Validator.class, "enableIPValidation", true);
		try {
			when(sessionObject.getSessionObject("key"))
					.thenReturn(new JSONObject().put("loginIPAddress", "10.0.0.9").toString());

			assertThrows(IEMRException.class, () -> validator.checkKeyExists("key", "10.0.0.1"));
		} finally {
			ReflectionTestUtils.setField(Validator.class, "enableIPValidation", false);
		}
	}
}
