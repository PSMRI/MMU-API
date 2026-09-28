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
package com.iemr.mmu.utils.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.iemr.mmu.data.login.Users;
import com.iemr.mmu.utils.JwtAuthenticationUtil;
import com.iemr.mmu.utils.JwtUtil;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.redis.RedisStorage;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

class RoleAuthenticationFilterTest {

	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private RedisStorage redisService;
	@Mock
	private JwtAuthenticationUtil userService;

	@InjectMocks
	private RoleAuthenticationFilter filter;

	private FilterChain chain;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		chain = mock(FilterChain.class);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private MockHttpServletRequest requestWithJwtCookie(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ANC/save");
		request.setCookies(new Cookie("Jwttoken", token));
		return request;
	}

	private Claims claimsWithUserId(Object userId) {
		Claims claims = mock(Claims.class);
		when(claims.get("userId")).thenReturn(userId);
		return claims;
	}

	@Test
	@DisplayName("cached roles are turned into authorities without hitting the user service")
	void doFilterInternal_usesTheCachedRoles() throws Exception {
		Claims claims = claimsWithUserId("7");
		when(jwtUtil.extractAllClaims("token")).thenReturn(claims);
		when(redisService.getUserRoleFromCache(7L)).thenReturn(new ArrayList<>(List.of("ROLE_NURSE")));

		filter.doFilter(requestWithJwtCookie("token"), new MockHttpServletResponse(), chain);

		assertEquals("7", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		assertEquals("ROLE_NURSE",
				SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next()
						.getAuthority());
		verify(userService, never()).getUserRoles(anyLong());
	}

	@Test
	@DisplayName("roles read from the user service are normalised and cached")
	void doFilterInternal_normalisesAndCachesFreshlyReadRoles() throws Exception {
		Claims claims = claimsWithUserId("7");
		when(jwtUtil.extractAllClaims("token")).thenReturn(claims);
		when(redisService.getUserRoleFromCache(7L)).thenReturn(new ArrayList<>());
		when(userService.getUserRoles(7L))
				.thenReturn(new ArrayList<>(Arrays.asList(" lab technician ", null)));

		filter.doFilter(requestWithJwtCookie("token"), new MockHttpServletResponse(), chain);

		assertEquals("ROLE_LAB_TECHNICIAN",
				SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next()
						.getAuthority());
		verify(redisService).cacheUserRoles(7L, List.of("ROLE_LAB_TECHNICIAN"));
	}

	@Test
	@DisplayName("the JWT header is used when there is no cookie")
	void doFilterInternal_fallsBackToTheJwtHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ANC/save");
		request.addHeader("Jwttoken", "token");
		Claims claims = claimsWithUserId("7");
		when(jwtUtil.extractAllClaims("token")).thenReturn(claims);
		when(redisService.getUserRoleFromCache(7L)).thenReturn(new ArrayList<>(List.of("ROLE_NURSE")));

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("a request without a token is passed straight through")
	void doFilterInternal_passesARequestWithoutATokenStraightThrough() throws Exception {
		filter.doFilter(new MockHttpServletRequest("POST", "/ANC/save"), new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		// The early return and the finally block both forward the request.
		verify(chain, org.mockito.Mockito.times(2)).doFilter(any(), any());
	}

	@Test
	@DisplayName("a token with no readable claims is passed straight through")
	void doFilterInternal_passesATokenWithNoClaimsStraightThrough() throws Exception {
		when(jwtUtil.extractAllClaims("token")).thenReturn(null);

		filter.doFilter(requestWithJwtCookie("token"), new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("a token without a user id is passed straight through")
	void doFilterInternal_passesATokenWithoutAUserIdStraightThrough() throws Exception {
		Claims claims = claimsWithUserId(null);
		when(jwtUtil.extractAllClaims("token")).thenReturn(claims);

		filter.doFilter(requestWithJwtCookie("token"), new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("a token with a non-numeric user id is passed straight through")
	void doFilterInternal_passesANonNumericUserIdStraightThrough() throws Exception {
		Claims claims = claimsWithUserId("not-a-number");
		when(jwtUtil.extractAllClaims("token")).thenReturn(claims);

		filter.doFilter(requestWithJwtCookie("token"), new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("a failure while reading the roles clears the security context")
	void doFilterInternal_clearsTheSecurityContextWhenTheRolesCannotBeRead() throws Exception {
		Claims claims = claimsWithUserId("7");
		when(jwtUtil.extractAllClaims("token")).thenReturn(claims);
		when(redisService.getUserRoleFromCache(7L)).thenReturn(new ArrayList<>());
		when(userService.getUserRoles(7L)).thenThrow(new IEMRException("No role found for userId : 7"));

		filter.doFilter(requestWithJwtCookie("token"), new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(chain).doFilter(any(), any());
	}
}
