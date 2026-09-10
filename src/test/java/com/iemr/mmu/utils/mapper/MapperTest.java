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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;
import com.iemr.mmu.data.registrar.BeneficiaryData;

class MapperTest {

	@Test
	@DisplayName("the input mapper reads the ISO date format used by the UI")
	void inputMapper_readsTheIsoDateFormat() throws Exception {
		BeneficiaryData beneficiary = InputMapper.gson()
				.fromJson("{\"firstName\":\"Asha\",\"dob\":\"1990-05-04T00:00:00.000\"}", BeneficiaryData.class);

		assertEquals("Asha", beneficiary.getFirstName());
		assertNotNull(beneficiary.getDob());
	}

	@Test
	@DisplayName("the input mapper reads a JSON element as well as a string")
	void inputMapper_readsAJsonElement() throws Exception {
		BeneficiaryData beneficiary = InputMapper.gson()
				.fromJson(JsonParser.parseString("{\"firstName\":\"Asha\"}"), BeneficiaryData.class);

		assertEquals("Asha", beneficiary.getFirstName());
	}

	@Test
	@DisplayName("the flagged input mapper reads the long-form date format")
	void inputMapper_readsTheLongFormDateFormat() throws Exception {
		BeneficiaryData beneficiary = InputMapper.gson(1)
				.fromJson("{\"dob\":\"May 04, 1990 00:00:00\"}", BeneficiaryData.class, 1);

		assertNotNull(beneficiary.getDob());
	}

	@Test
	@DisplayName("the output mapper serialises nulls and exposed fields only")
	void outputMapper_serialisesNullsAndExposedFieldsOnly() {
		new OutputMapper();

		assertNotNull(OutputMapper.gson());
		assertEquals("{}", OutputMapper.gson().toJson(new Object()));
	}
}
