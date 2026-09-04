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
package com.iemr.mmu.utils.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

class RedisStorageTest {

	@Mock
	private LettuceConnectionFactory connectionFactory;
	@Mock
	private RedisConnection redisConnection;
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private ListOperations<String, String> listOperations;

	@InjectMocks
	private RedisStorage redisStorage;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(connectionFactory.getConnection()).thenReturn(redisConnection);
	}

	@Test
	@DisplayName("a session is stored only when the key is still free")
	void setObject_storesTheSessionWhenTheKeyIsFree() {
		when(redisConnection.get("key".getBytes())).thenReturn(null);

		assertEquals("key", redisStorage.setObject("key", "value", 60));
		verify(redisConnection).set(any(), any(), any(), any());
	}

	@Test
	@DisplayName("an existing session is left untouched")
	void setObject_leavesAnExistingSessionUntouched() {
		when(redisConnection.get("key".getBytes())).thenReturn("existing".getBytes());

		assertEquals("key", redisStorage.setObject("key", "value", 60));
		verify(redisConnection, org.mockito.Mockito.never()).set(any(), any(), any(), any());
	}

	@Test
	@DisplayName("reading a session refreshes its expiry")
	void getObject_refreshesTheExpiryOfAStoredSession() throws Exception {
		when(redisConnection.get("key".getBytes())).thenReturn("value".getBytes());

		assertEquals("value", redisStorage.getObject("key", 60));
		verify(redisConnection).expire("key".getBytes(), 60);
	}

	@Test
	@DisplayName("reading a session that is not stored fails")
	void getObject_failsWhenTheSessionIsNotStored() {
		when(redisConnection.get("key".getBytes())).thenReturn(null);

		assertThrows(RedisSessionException.class, () -> redisStorage.getObject("key", 60));
	}

	@Test
	@DisplayName("deleting a session reports how many keys were removed")
	void deleteObject_reportsHowManyKeysWereRemoved() throws Exception {
		when(redisConnection.del("key".getBytes())).thenReturn(1L);

		assertEquals(1L, redisStorage.deleteObject("key"));
	}

	@Test
	@DisplayName("updating a stored session rewrites its value")
	void updateObject_rewritesAStoredSession() throws Exception {
		when(redisConnection.get("key".getBytes())).thenReturn("value".getBytes());

		assertEquals("key", redisStorage.updateObject("key", "new value", 60));
		verify(redisConnection).set(any(), any(), any(), any());
	}

	@Test
	@DisplayName("updating a session that is not stored fails")
	void updateObject_failsWhenTheSessionIsNotStored() {
		when(redisConnection.get("key".getBytes())).thenReturn(null);

		assertThrows(RedisSessionException.class, () -> redisStorage.updateObject("key", "new value", 60));
	}

	@Test
	@DisplayName("caching a user's roles replaces whatever was cached before")
	void cacheUserRoles_replacesTheCachedRoles() {
		when(redisTemplate.opsForList()).thenReturn(listOperations);

		redisStorage.cacheUserRoles(7L, List.of("ROLE_NURSE"));

		verify(redisTemplate).delete("roles:7");
		verify(listOperations).rightPushAll("roles:7", List.of("ROLE_NURSE"));
	}

	@Test
	@DisplayName("a cache write that fails is not propagated")
	void cacheUserRoles_swallowsAFailedCacheWrite() {
		when(redisTemplate.opsForList()).thenThrow(new RuntimeException("redis down"));

		redisStorage.cacheUserRoles(7L, List.of("ROLE_NURSE"));
	}

	@Test
	@DisplayName("cached roles are read back for the user")
	void getUserRoleFromCache_readsTheCachedRoles() {
		when(redisTemplate.opsForList()).thenReturn(listOperations);
		when(listOperations.range("roles:7", 0, -1)).thenReturn(List.of("ROLE_NURSE"));

		assertEquals(List.of("ROLE_NURSE"), redisStorage.getUserRoleFromCache(7L));
	}

	@Test
	@DisplayName("a cache read that fails reports no roles")
	void getUserRoleFromCache_reportsNoRolesWhenTheCacheIsUnreachable() {
		when(redisTemplate.opsForList()).thenThrow(new RuntimeException("redis down"));

		assertNull(redisStorage.getUserRoleFromCache(7L));
	}

	@Test
	@DisplayName("a Redis session failure carries its message and cause")
	void redisSessionException_carriesItsMessageAndCause() {
		assertEquals("boom", new RedisSessionException("boom").getMessage());
		assertEquals("boom", new RedisSessionException("boom", new RuntimeException("cause")).getMessage());
	}
}
