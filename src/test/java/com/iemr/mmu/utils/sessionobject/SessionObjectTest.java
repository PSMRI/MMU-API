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
package com.iemr.mmu.utils.sessionobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.iemr.mmu.utils.redis.RedisSessionException;
import com.iemr.mmu.utils.redis.RedisStorage;

class SessionObjectTest {

	private RedisStorage objectStore;
	private SessionObject sessionObject;

	@BeforeEach
	void setUp() {
		objectStore = mock(RedisStorage.class);
		sessionObject = new SessionObject();
		sessionObject.setObjectStore(objectStore);
	}

	@Test
	@DisplayName("reading a session delegates to the store with the configured expiry")
	void getSessionObject_readsFromTheStore() throws Exception {
		when(objectStore.getObject(anyString(), anyInt())).thenReturn("{}");

		assertEquals("{}", sessionObject.getSessionObject("key"));
	}

	@Test
	@DisplayName("writing a session delegates to the store")
	void setSessionObject_writesToTheStore() throws Exception {
		when(objectStore.setObject(anyString(), anyString(), anyInt())).thenReturn("key");

		assertEquals("key", sessionObject.setSessionObject("key", "{}"));
	}

	@Test
	@DisplayName("updating a session also refreshes the concurrent-session entry for the user")
	void updateSessionObject_refreshesTheConcurrentSessionEntry() throws Exception {
		when(objectStore.updateObject(anyString(), anyString(), anyInt())).thenReturn("key");

		assertEquals("key", sessionObject.updateSessionObject("key", "{\"userName\":\" Nurse1 \"}"));
		verify(objectStore).updateObject(org.mockito.ArgumentMatchers.eq("nurse1"),
				org.mockito.ArgumentMatchers.eq("key"), anyInt());
	}

	@Test
	@DisplayName("updating a session with no user name only refreshes the session itself")
	void updateSessionObject_skipsTheConcurrentSessionEntryWithoutAUserName() throws Exception {
		when(objectStore.updateObject(anyString(), anyString(), anyInt())).thenReturn("key");

		assertEquals("key", sessionObject.updateSessionObject("key", "{}"));
		verify(objectStore, never()).updateObject(org.mockito.ArgumentMatchers.eq("nurse1"), anyString(), anyInt());
	}

	@Test
	@DisplayName("an unparseable session value does not stop the update")
	void updateSessionObject_toleratesAnUnparseableValue() throws Exception {
		when(objectStore.updateObject(anyString(), anyString(), anyInt())).thenReturn("key");

		assertEquals("key", sessionObject.updateSessionObject("key", "not-json"));
	}

	@Test
	@DisplayName("deleting a session delegates to the store")
	void deleteSessionObject_deletesFromTheStore() throws Exception {
		sessionObject.deleteSessionObject("key");

		verify(objectStore).deleteObject("key");
	}

	@Test
	@DisplayName("a store failure is passed on to the caller")
	void getSessionObject_passesOnAStoreFailure() throws Exception {
		when(objectStore.getObject(anyString(), anyInt())).thenThrow(new RedisSessionException("redis down"));

		assertThrows(RedisSessionException.class, () -> sessionObject.getSessionObject("key"));
	}
}
