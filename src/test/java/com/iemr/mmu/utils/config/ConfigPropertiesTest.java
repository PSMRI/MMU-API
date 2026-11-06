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
package com.iemr.mmu.utils.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigPropertiesTest {

	@Test
	@DisplayName("a property that is not in the file reads back as null")
	void getPropertyByName_returnsNothingForAnUnknownProperty() {
		assertNull(ConfigProperties.getPropertyByName("no.such.property"));
	}

	@Test
	@DisplayName("an unparseable numeric property falls back to its zero value")
	void theNumericReadersFallBackToZeroForAnUnparseableProperty() {
		assertEquals(0, ConfigProperties.getInteger("no.such.property"));
		assertEquals(0L, ConfigProperties.getLong("no.such.property"));
	}

	@Test
	@DisplayName("a numeric property that is present is read as a number")
	void theNumericReadersReadAStoredNumber() {
		assertEquals(1800, ConfigProperties.getInteger("iemr.session.expiry.time"));
		assertEquals(1800L, ConfigProperties.getLong("iemr.session.expiry.time"));
		assertEquals(1800F, ConfigProperties.getFloat("iemr.session.expiry.time"));
	}

	@Test
	@DisplayName("a missing boolean property reads back as false")
	void getBoolean_returnsFalseForAMissingProperty() {
		assertFalse(ConfigProperties.getBoolean("no.such.property"));
	}

	@Test
	@DisplayName("the Redis connection settings are readable")
	void theRedisConnectionSettingsAreReadable() {
		ConfigProperties.getRedisUrl();

		assertEquals(0, ConfigProperties.getRedisPort());
	}

	@Test
	@DisplayName("the session expiry settings are readable")
	void theSessionExpirySettingsAreReadable() {
		assertFalse(ConfigProperties.getExtendExpiryTime());
		org.junit.jupiter.api.Assertions.assertTrue(ConfigProperties.getSessionExpiryTime() > 0);
	}
}
