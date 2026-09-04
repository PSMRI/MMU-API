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
package com.iemr.mmu.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.http.AuthorizationHeaderRequestWrapper;
import com.iemr.mmu.utils.mapper.RoleAuthenticationFilter;
import com.iemr.mmu.utils.redis.RedisStorage;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

class JwtUserIdValidationFilterTest {

	private JwtAuthenticationUtil jwtAuthenticationUtil;
	private JwtUserIdValidationFilter filter;
	private FilterChain chain;

	@BeforeEach
	void setUp() {
		jwtAuthenticationUtil = mock(JwtAuthenticationUtil.class);
		filter = new JwtUserIdValidationFilter(jwtAuthenticationUtil, "https://mmu.example.org,http://localhost:*");
		chain = mock(FilterChain.class);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
		UserAgentContext.clear();
	}

	private MockHttpServletRequest request(String method, String uri) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		request.setRequestURI(uri);
		return request;
	}

	@Test
	@DisplayName("a preflight from an allowed origin is answered with the CORS headers")
	void doFilter_answersAPreflightFromAnAllowedOrigin() throws Exception {
		MockHttpServletRequest request = request("OPTIONS", "/ANC/save");
		request.addHeader("Origin", "https://mmu.example.org");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
		assertEquals("https://mmu.example.org", response.getHeader("Access-Control-Allow-Origin"));
		assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
		verify(chain, never()).doFilter(any(), any());
	}

	@Test
	@DisplayName("a wildcard port on localhost is an allowed origin")
	void doFilter_allowsAWildcardLocalhostPort() throws Exception {
		MockHttpServletRequest request = request("OPTIONS", "/ANC/save");
		request.addHeader("Origin", "http://localhost:4200");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
	}

	@Test
	@DisplayName("a preflight without an Origin header is refused")
	void doFilter_refusesAPreflightWithoutAnOrigin() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request("OPTIONS", "/ANC/save"), response, chain);

		assertEquals(403, response.getStatus());
	}

	@Test
	@DisplayName("a preflight from an unknown origin is refused")
	void doFilter_refusesAPreflightFromAnUnknownOrigin() throws Exception {
		MockHttpServletRequest request = request("OPTIONS", "/ANC/save");
		request.addHeader("Origin", "https://attacker.example.org");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(403, response.getStatus());
	}

	@Test
	@DisplayName("a request from an unknown origin is refused")
	void doFilter_refusesARequestFromAnUnknownOrigin() throws Exception {
		MockHttpServletRequest request = request("POST", "/ANC/save");
		request.addHeader("Origin", "https://attacker.example.org");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(403, response.getStatus());
	}

	@Test
	@DisplayName("public endpoints are let through without a token")
	void doFilter_letsPublicEndpointsThrough() throws Exception {
		for (String path : List.of("/user/userAuthenticate", "/user/logOutUserFromConcurrentSession",
				"/swagger-ui/index.html", "/v3/api-docs", "/user/refreshToken", "/public/thing", "/version",
				"/health")) {
			FilterChain freshChain = mock(FilterChain.class);

			filter.doFilter(request("POST", path), new MockHttpServletResponse(), freshChain);

			verify(freshChain).doFilter(any(), any());
		}
	}

	@Test
	@DisplayName("a valid JWT cookie lets the request through and clears any userId cookie")
	void doFilter_letsAValidJwtCookieThrough() throws Exception {
		MockHttpServletRequest request = request("POST", "/ANC/save");
		request.setCookies(new Cookie("Jwttoken", "token"), new Cookie("userId", "7"));
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(jwtAuthenticationUtil.validateUserIdAndJwtToken("token")).thenReturn(true);

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(any(AuthorizationHeaderRequestWrapper.class), any());
		assertEquals(0, response.getCookie("userId").getMaxAge());
	}

	@Test
	@DisplayName("a valid JWT header lets the request through")
	void doFilter_letsAValidJwtHeaderThrough() throws Exception {
		MockHttpServletRequest request = request("POST", "/ANC/save");
		request.addHeader("JwtToken", "token");
		when(jwtAuthenticationUtil.validateUserIdAndJwtToken("token")).thenReturn(true);

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		verify(chain).doFilter(any(AuthorizationHeaderRequestWrapper.class), any());
	}

	@Test
	@DisplayName("a mobile client with an Authorization header is let through")
	void doFilter_letsAMobileClientThrough() throws Exception {
		MockHttpServletRequest request = request("POST", "/ANC/save");
		request.addHeader("User-Agent", "okhttp/4.9.0");
		request.addHeader("Authorization", "session-key");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		verify(chain).doFilter(any(), any());
	}

	@Test
	@DisplayName("a request with no recognisable token is refused")
	void doFilter_refusesARequestWithNoToken() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request("POST", "/ANC/save"), response, chain);

		assertEquals(401, response.getStatus());
	}

	@Test
	@DisplayName("a token that fails validation is refused")
	void doFilter_refusesATokenThatFailsValidation() throws Exception {
		MockHttpServletRequest request = request("POST", "/ANC/save");
		request.setCookies(new Cookie("Jwttoken", "token"));
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(jwtAuthenticationUtil.validateUserIdAndJwtToken("token"))
				.thenThrow(new IEMRException("Invalid User ID."));

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
	}

	@Test
	@DisplayName("no configured origins means no origin is allowed")
	void doFilter_refusesEveryOriginWhenNoneIsConfigured() throws Exception {
		JwtUserIdValidationFilter noOriginsFilter = new JwtUserIdValidationFilter(jwtAuthenticationUtil, "  ");
		MockHttpServletRequest request = request("OPTIONS", "/ANC/save");
		request.addHeader("Origin", "https://mmu.example.org");
		MockHttpServletResponse response = new MockHttpServletResponse();

		noOriginsFilter.doFilter(request, response, chain);

		assertEquals(403, response.getStatus());
	}

	@Test
	@DisplayName("the Authorization header wrapper reports the header it was given")
	void authorizationHeaderRequestWrapper_reportsTheHeaderItWasGiven() {
		MockHttpServletRequest request = request("POST", "/ANC/save");
		request.addHeader("Accept", "application/json");

		AuthorizationHeaderRequestWrapper wrapper = new AuthorizationHeaderRequestWrapper(request, "session-key");

		assertEquals("session-key", wrapper.getHeader("authorization"));
		assertEquals("session-key", wrapper.getHeaders("Authorization").nextElement());
		assertEquals("application/json", wrapper.getHeader("Accept"));
		assertTrue(java.util.Collections.list(wrapper.getHeaderNames()).contains("Authorization"));
	}
}
