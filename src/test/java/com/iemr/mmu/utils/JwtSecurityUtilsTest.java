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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.data.login.Users;
import com.iemr.mmu.repo.login.UserLoginRepo;
import com.iemr.mmu.utils.exception.IEMRException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

class JwtSecurityUtilsTest {

	/** Long enough for HMAC-SHA; the filter and util derive the key from it. */
	private static final String SECRET = "a-very-long-jwt-signing-secret-for-mmu-api-tests-0123456789";

	private static SecretKey signingKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes());
	}

	private static String tokenWith(String userId, String jti) {
		var builder = Jwts.builder().subject("nurse1").claim("userId", userId)
				.expiration(new Date(System.currentTimeMillis() + 60_000));
		if (jti != null) {
			builder.id(jti);
		}
		return builder.signWith(signingKey()).compact();
	}

	@Nested
	@DisplayName("JwtUtil")
	class JwtUtilTests {

		@Mock
		private TokenDenylist tokenDenylist;

		@InjectMocks
		private JwtUtil jwtUtil;

		@BeforeEach
		void setUp() {
			MockitoAnnotations.openMocks(this);
			ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET);
		}

		@Test
		void validateToken_returnsTheClaimsOfAValidToken() {
			Claims claims = jwtUtil.validateToken(tokenWith("7", null));

			assertNotNull(claims);
			assertEquals("nurse1", claims.getSubject());
		}

		@Test
		void validateToken_rejectsADenylistedToken() {
			when(tokenDenylist.isTokenDenylisted("jti-1")).thenReturn(true);

			assertNull(jwtUtil.validateToken(tokenWith("7", "jti-1")));
		}

		@Test
		void validateToken_keepsATokenThatIsNotDenylisted() {
			when(tokenDenylist.isTokenDenylisted("jti-1")).thenReturn(false);

			assertNotNull(jwtUtil.validateToken(tokenWith("7", "jti-1")));
		}

		@Test
		void validateToken_rejectsAMalformedToken() {
			assertNull(jwtUtil.validateToken("not-a-token"));
		}

		@Test
		void extractUsername_readsTheSubjectOfTheToken() {
			assertEquals("nurse1", jwtUtil.extractUsername(tokenWith("7", null)));
		}

		@Test
		void getUserIdFromToken_readsTheUserIdClaim() {
			assertEquals("7", jwtUtil.getUserIdFromToken(tokenWith("7", null)));
			assertNull(jwtUtil.getUserIdFromToken("not-a-token"));
		}

		@Test
		void extractAllClaims_failsForAMalformedToken() {
			assertThrows(Exception.class, () -> jwtUtil.extractAllClaims("not-a-token"));
		}

		@Test
		void validateToken_failsWhenNoSigningSecretIsConfigured() {
			ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", "");

			assertNull(jwtUtil.validateToken(tokenWith("7", null)));
			assertThrows(IllegalStateException.class, () -> jwtUtil.extractAllClaims(tokenWith("7", null)));
		}
	}

	@Nested
	@DisplayName("TokenDenylist")
	class TokenDenylistTests {

		@Mock
		private RedisTemplate<String, Object> redisTemplate;
		@Mock
		private ValueOperations<String, Object> valueOperations;

		@InjectMocks
		private TokenDenylist tokenDenylist;

		@BeforeEach
		void setUp() {
			MockitoAnnotations.openMocks(this);
		}

		@Test
		void addTokenToDenylist_storesTheTokenIdUnderItsExpiry() {
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);

			tokenDenylist.addTokenToDenylist("jti-1", 1000L);

			verify(valueOperations).set(anyString(), eq(" "), eq(1000L), eq(TimeUnit.MILLISECONDS));
		}

		@Test
		void addTokenToDenylist_ignoresAMissingTokenId() {
			tokenDenylist.addTokenToDenylist(null, 1000L);
			tokenDenylist.addTokenToDenylist("  ", 1000L);

			verify(redisTemplate, never()).opsForValue();
		}

		@Test
		void addTokenToDenylist_rejectsAnExpiryThatIsNotInTheFuture() {
			assertThrows(IllegalArgumentException.class, () -> tokenDenylist.addTokenToDenylist("jti-1", null));
			assertThrows(IllegalArgumentException.class, () -> tokenDenylist.addTokenToDenylist("jti-1", 0L));
		}

		@Test
		void addTokenToDenylist_reportsAStoreThatIsUnreachable() {
			when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

			assertThrows(RuntimeException.class, () -> tokenDenylist.addTokenToDenylist("jti-1", 1000L));
		}

		@Test
		void isTokenDenylisted_readsTheStoredTokenId() {
			when(redisTemplate.hasKey(anyString())).thenReturn(true);
			assertTrue(tokenDenylist.isTokenDenylisted("jti-1"));

			when(redisTemplate.hasKey(anyString())).thenReturn(false);
			assertFalse(tokenDenylist.isTokenDenylisted("jti-1"));
		}

		@Test
		void isTokenDenylisted_treatsAMissingTokenIdAsAllowed() {
			assertFalse(tokenDenylist.isTokenDenylisted(null));
			assertFalse(tokenDenylist.isTokenDenylisted("  "));
		}

		@Test
		void isTokenDenylisted_treatsAnUnreachableStoreAsAllowed() {
			when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));

			assertFalse(tokenDenylist.isTokenDenylisted("jti-1"));
		}
	}

	@Nested
	@DisplayName("JwtAuthenticationUtil")
	class JwtAuthenticationUtilTests {

		private CookieUtil cookieUtil;
		private JwtUtil jwtUtil;
		private RedisTemplate<String, Object> redisTemplate;
		private ValueOperations<String, Object> valueOperations;
		private UserLoginRepo userLoginRepo;
		private JwtAuthenticationUtil authenticationUtil;

		@SuppressWarnings("unchecked")
		@BeforeEach
		void setUp() {
			cookieUtil = mock(CookieUtil.class);
			jwtUtil = mock(JwtUtil.class);
			redisTemplate = mock(RedisTemplate.class);
			valueOperations = mock(ValueOperations.class);
			userLoginRepo = mock(UserLoginRepo.class);

			authenticationUtil = new JwtAuthenticationUtil(cookieUtil, jwtUtil);
			ReflectionTestUtils.setField(authenticationUtil, "redisTemplate", redisTemplate);
			ReflectionTestUtils.setField(authenticationUtil, "userLoginRepo", userLoginRepo);
		}

		private Claims claimsFor(String subject, String userId) {
			Claims claims = mock(Claims.class);
			when(claims.getSubject()).thenReturn(subject);
			when(claims.get("userId", String.class)).thenReturn(userId);
			return claims;
		}

		@Test
		void validateJwtToken_returnsTheUsernameOfAValidToken() {
			HttpServletRequest request = mock(HttpServletRequest.class);
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(java.util.Optional.of("token"));
			Claims claims = claimsFor("nurse1", "7");
			when(jwtUtil.validateToken("token")).thenReturn(claims);

			assertEquals("nurse1", authenticationUtil.validateJwtToken(request).getBody());
		}

		@Test
		void validateJwtToken_rejectsARequestWithoutAToken() {
			HttpServletRequest request = mock(HttpServletRequest.class);
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(java.util.Optional.empty());

			assertEquals(HttpStatus.UNAUTHORIZED, authenticationUtil.validateJwtToken(request).getStatusCode());
		}

		@Test
		void validateJwtToken_rejectsAnInvalidToken() {
			HttpServletRequest request = mock(HttpServletRequest.class);
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(java.util.Optional.of("token"));
			when(jwtUtil.validateToken("token")).thenReturn(null);

			assertTrue(authenticationUtil.validateJwtToken(request).getBody().contains("Invalid JWT Token"));
		}

		@Test
		void validateJwtToken_rejectsATokenWithoutAUsername() {
			HttpServletRequest request = mock(HttpServletRequest.class);
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(java.util.Optional.of("token"));
			Claims claims = claimsFor(null, "7");
			when(jwtUtil.validateToken("token")).thenReturn(claims);

			assertTrue(authenticationUtil.validateJwtToken(request).getBody().contains("Username is missing"));
		}

		@Test
		void validateUserIdAndJwtToken_acceptsAUserThatIsAlreadyCached() throws Exception {
			Users cachedUser = new Users();
			cachedUser.setUserID(7L);
			Claims claims = claimsFor("nurse1", "7");
			when(jwtUtil.validateToken("token")).thenReturn(claims);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get("user_7")).thenReturn(cachedUser);
			when(userLoginRepo.getRoleNamebyUserId(7L))
					.thenReturn(new java.util.ArrayList<>(java.util.List.of("Nurse")));

			assertTrue(authenticationUtil.validateUserIdAndJwtToken("token"));
		}

		@Test
		void validateUserIdAndJwtToken_cachesAUserThatWasReadFromTheDatabase() throws Exception {
			Users storedUser = new Users();
			storedUser.setUserID(7L);
			storedUser.setUserName("nurse1");
			Claims claims = claimsFor("nurse1", "7");
			when(jwtUtil.validateToken("token")).thenReturn(claims);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get("user_7")).thenReturn(null);
			when(userLoginRepo.getUserByUserID(7L)).thenReturn(storedUser);

			assertTrue(authenticationUtil.validateUserIdAndJwtToken("token"));
			verify(valueOperations).set(eq("user_7"), any(), eq(30L), eq(TimeUnit.MINUTES));
		}

		@Test
		void validateUserIdAndJwtToken_rejectsAnInvalidToken() {
			when(jwtUtil.validateToken("token")).thenReturn(null);

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> authenticationUtil.validateUserIdAndJwtToken("token"));
			assertTrue(thrown.getMessage().contains("Invalid JWT token"));
		}

		@Test
		void validateUserIdAndJwtToken_rejectsAnUnknownUser() {
			Claims claims = claimsFor("nurse1", "7");
			when(jwtUtil.validateToken("token")).thenReturn(claims);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get("user_7")).thenReturn(null);
			when(userLoginRepo.getUserByUserID(7L)).thenReturn(null);

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> authenticationUtil.validateUserIdAndJwtToken("token"));
			assertTrue(thrown.getMessage().contains("Invalid User ID"));
		}

		@Test
		void getUserRoles_returnsTheRolesOfTheUser() throws Exception {
			when(userLoginRepo.getRoleNamebyUserId(7L))
					.thenReturn(new java.util.ArrayList<>(java.util.List.of("Nurse")));

			assertEquals(java.util.List.of("Nurse"), authenticationUtil.getUserRoles(7L));
		}

		@Test
		void getUserRoles_rejectsAnInvalidUserId() {
			assertThrows(IEMRException.class, () -> authenticationUtil.getUserRoles(null));
			assertThrows(IEMRException.class, () -> authenticationUtil.getUserRoles(0L));
		}

		@Test
		void getUserRoles_failsWhenTheUserHasNoRole() {
			when(userLoginRepo.getRoleNamebyUserId(7L)).thenReturn(new java.util.ArrayList<>());

			assertThrows(IEMRException.class, () -> authenticationUtil.getUserRoles(7L));
		}
	}

	@Nested
	@DisplayName("UserAgentContext")
	class UserAgentContextTests {

		@Test
		void theUserAgentIsHeldPerThreadUntilItIsCleared() {
			UserAgentContext.setUserAgent("okhttp/4.9");
			assertEquals("okhttp/4.9", UserAgentContext.getUserAgent());

			UserAgentContext.clear();
			assertNull(UserAgentContext.getUserAgent());
		}
	}

	@Nested
	@DisplayName("MediaTypeUtils")
	class MediaTypeUtilsTests {

		@Test
		void getMediaTypeForFileName_readsTheTypeFromTheServletContext() {
			jakarta.servlet.ServletContext servletContext = mock(jakarta.servlet.ServletContext.class);
			when(servletContext.getMimeType("report.pdf")).thenReturn("application/pdf");

			assertEquals("application/pdf",
					MediaTypeUtils.getMediaTypeForFileName(servletContext, "report.pdf").toString());
		}

		@Test
		void getMediaTypeForFileName_fallsBackToOctetStreamForAnUnknownType() {
			jakarta.servlet.ServletContext servletContext = mock(jakarta.servlet.ServletContext.class);
			when(servletContext.getMimeType("report.xyz")).thenReturn(null);

			assertEquals("application/octet-stream",
					MediaTypeUtils.getMediaTypeForFileName(servletContext, "report.xyz").toString());
		}
	}

	@Nested
	@DisplayName("RestTemplateUtil")
	class RestTemplateUtilTests {

		@Test
		void createRequestEntity_sendsTheAuthorizationAsABearerTokenAndTheJwtAsACookie() {
			org.springframework.http.HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("{}", "auth",
					"jwt");

			assertEquals("Bearer auth", entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			assertEquals("Jwttoken=jwt", entity.getHeaders().getFirst(HttpHeaders.COOKIE));
			assertTrue(entity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE).contains("charset=utf-8"));
		}

		@Test
		void createRequestEntity_sendsTheRawAuthorizationForADataSyncCall() {
			org.springframework.http.HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("{}", "auth",
					"datasync");

			assertEquals("auth", entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			assertNull(entity.getHeaders().getFirst(HttpHeaders.COOKIE));
		}

		@Test
		void createRequestEntity_readsTheJwtFromTheCurrentRequestWhenNoneWasPassed() {
			org.springframework.mock.web.MockHttpServletRequest request =
					new org.springframework.mock.web.MockHttpServletRequest();
			request.setCookies(new jakarta.servlet.http.Cookie("Jwttoken", "from-cookie"));
			org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
					new org.springframework.web.context.request.ServletRequestAttributes(request));
			try {
				org.springframework.http.HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("{}",
						"auth", "");

				assertEquals("Jwttoken=from-cookie", entity.getHeaders().getFirst(HttpHeaders.COOKIE));
			} finally {
				org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
			}
		}

		@Test
		void createRequestEntity_omitsTheCookieWhenThereIsNoCurrentRequest() {
			org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();

			org.springframework.http.HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("{}", "", "");

			assertNull(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			assertNull(entity.getHeaders().getFirst(HttpHeaders.COOKIE));
		}
	}
}
