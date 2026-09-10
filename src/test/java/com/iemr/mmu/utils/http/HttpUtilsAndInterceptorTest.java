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
package com.iemr.mmu.utils.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.sessionobject.SessionObject;
import com.iemr.mmu.utils.validator.Validator;

class HttpUtilsAndInterceptorTest {

	@Nested
	@DisplayName("HttpUtils")
	class HttpUtilsTests {

		private HttpUtils httpUtils;
		private RestTemplate restTemplate;

		@BeforeEach
		void setUp() {
			httpUtils = new HttpUtils();
			restTemplate = mock(RestTemplate.class);
			ReflectionTestUtils.setField(httpUtils, "rest", restTemplate);
		}

		private void stubExchange(HttpMethod method, String body) {
			when(restTemplate.exchange(anyString(), eq(method), any(), eq(String.class)))
					.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
		}

		@Test
		void get_returnsTheResponseBodyAndRecordsTheStatus() {
			stubExchange(HttpMethod.GET, "body");

			assertEquals("body", httpUtils.get("http://service/thing"));
			assertEquals(HttpStatus.OK, httpUtils.getStatus());
		}

		@Test
		void get_sendsTheAuthorizationAndContentTypeItWasGiven() {
			stubExchange(HttpMethod.GET, "body");
			HashMap<String, Object> header = new HashMap<>();
			header.put("Authorization", "session-key");
			header.put("Content-Type", "application/json");

			assertEquals("body", httpUtils.get("http://service/thing", header));
		}

		@Test
		void get_fallsBackToJsonWhenNoContentTypeWasGiven() {
			stubExchange(HttpMethod.GET, "body");

			assertEquals("body", httpUtils.get("http://service/thing", new HashMap<>()));
		}

		@Test
		void post_returnsTheResponseBody() {
			stubExchange(HttpMethod.POST, "body");

			assertEquals("body", httpUtils.post("http://service/thing", "{}"));
		}

		@Test
		void post_sendsTheAuthorizationItWasGiven() {
			stubExchange(HttpMethod.POST, "body");
			HashMap<String, Object> header = new HashMap<>();
			header.put("Authorization", "session-key");

			assertEquals("body", httpUtils.post("http://service/thing", "{}", header));
		}

		@Test
		void setStatus_isReadBackByGetStatus() {
			httpUtils.setStatus(HttpStatus.NOT_FOUND);

			assertEquals(HttpStatus.NOT_FOUND, httpUtils.getStatus());
		}
	}

	@Nested
	@DisplayName("HttpInterceptor")
	class HttpInterceptorTests {

		private HttpInterceptor interceptor;
		private Validator validator;
		private SessionObject sessionObject;

		@BeforeEach
		void setUp() {
			interceptor = new HttpInterceptor();
			validator = mock(Validator.class);
			sessionObject = mock(SessionObject.class);
			interceptor.setValidator(validator);
			interceptor.setSessionObject(sessionObject);
			ReflectionTestUtils.setField(interceptor, "allowedOrigins", "https://mmu.example.org");
		}

		private MockHttpServletRequest request(String method, String uri, String authorization) {
			MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
			request.setRequestURI(uri);
			if (authorization != null) {
				request.addHeader("Authorization", authorization);
			}
			return request;
		}

		@Test
		void preHandle_letsAnUnauthenticatedRequestThrough() throws Exception {
			assertTrue(interceptor.preHandle(request("POST", "/ANC/save", null), new MockHttpServletResponse(),
					null));
			verify(validator, never()).checkKeyExists(anyString(), anyString());
		}

		@Test
		void preHandle_skipsValidationForThePublicEndpoints() throws Exception {
			for (String endpoint : java.util.List.of("userAuthenticate", "superUserAuthenticate", "userLogout",
					"changePassword", "swagger-ui.html", "api-docs", "startMasterDownload")) {
				assertTrue(interceptor.preHandle(request("POST", "/user/" + endpoint, "session-key"),
						new MockHttpServletResponse(), null));
			}
			verify(validator, never()).checkKeyExists(anyString(), anyString());
		}

		@Test
		void preHandle_stopsTheErrorEndpoint() throws Exception {
			assertFalse(interceptor.preHandle(request("POST", "/error", "session-key"),
					new MockHttpServletResponse(), null));
		}

		@Test
		void preHandle_validatesTheSessionOfEveryOtherRequest() throws Exception {
			MockHttpServletRequest request = request("POST", "/ANC/save", "session-key");
			request.addHeader("X-FORWARDED-FOR", "10.0.0.1");

			assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), null));
			verify(validator).checkKeyExists("session-key", "10.0.0.1");
		}

		@Test
		void preHandle_fallsBackToTheRemoteAddressWhenThereIsNoForwardedHeader() throws Exception {
			MockHttpServletRequest request = request("POST", "/ANC/save", "session-key");
			request.setRemoteAddr("10.0.0.2");

			assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), null));
			verify(validator).checkKeyExists("session-key", "10.0.0.2");
		}

		@Test
		void preHandle_stopsAndAnswersWhenTheSessionIsExpired() throws Exception {
			MockHttpServletRequest request = request("POST", "/ANC/save", "session-key");
			request.addHeader("Origin", "https://mmu.example.org");
			MockHttpServletResponse response = new MockHttpServletResponse();
			doThrow(new IEMRException("Session is expired. Please login again.")).when(validator)
					.checkKeyExists(anyString(), anyString());

			assertFalse(interceptor.preHandle(request, response, null));
			assertEquals("https://mmu.example.org", response.getHeader("Access-Control-Allow-Origin"));
			assertTrue(response.getContentAsString().contains("Session is expired"));
		}

		@Test
		void preHandle_omitsTheCorsHeadersForAnUnknownOrigin() throws Exception {
			MockHttpServletRequest request = request("POST", "/ANC/save", "session-key");
			request.addHeader("Origin", "https://attacker.example.org");
			MockHttpServletResponse response = new MockHttpServletResponse();
			doThrow(new IEMRException("Session is expired.")).when(validator).checkKeyExists(anyString(),
					anyString());

			assertFalse(interceptor.preHandle(request, response, null));
			assertNull(response.getHeader("Access-Control-Allow-Origin"));
		}

		@Test
		void preHandle_skipsValidationForAPreflight() throws Exception {
			assertTrue(interceptor.preHandle(request("OPTIONS", "/ANC/save", "session-key"),
					new MockHttpServletResponse(), null));
			verify(validator, never()).checkKeyExists(anyString(), anyString());
		}

		@Test
		void postHandle_refreshesTheSessionOfAnAuthenticatedRequest() throws Exception {
			when(sessionObject.getSessionObject("session-key")).thenReturn("{}");

			interceptor.postHandle(request("POST", "/ANC/save", "session-key"), new MockHttpServletResponse(), null,
					null);

			verify(sessionObject).updateSessionObject("session-key", "{}");
		}

		@Test
		void postHandle_leavesAnUnauthenticatedRequestAlone() throws Exception {
			interceptor.postHandle(request("POST", "/ANC/save", null), new MockHttpServletResponse(), null, null);

			verify(sessionObject, never()).updateSessionObject(anyString(), anyString());
		}

		@Test
		void postHandle_swallowsAFailedSessionRefresh() throws Exception {
			when(sessionObject.getSessionObject("session-key"))
					.thenThrow(new com.iemr.mmu.utils.redis.RedisSessionException("redis down"));

			interceptor.postHandle(request("POST", "/ANC/save", "session-key"), new MockHttpServletResponse(), null,
					null);
		}

		@Test
		void afterCompletion_doesNothingToTheResponse() throws Exception {
			MockHttpServletResponse response = new MockHttpServletResponse();

			interceptor.afterCompletion(request("POST", "/ANC/save", null), response, null, null);

			assertEquals(200, response.getStatus());
		}
	}
}
