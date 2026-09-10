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
package com.iemr.mmu.annotation.sqlinjection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SQLInjectionSafeConstraintValidatorTest {

	private final SQLInjectionSafeConstraintValidator validator = new SQLInjectionSafeConstraintValidator();

	@ParameterizedTest(name = "\"{0}\" is rejected")
	@ValueSource(strings = { "SELECT name FROM users", "INSERT INTO users values", "UPDATE users set",
			"DELETE FROM users", "UPSERT users set", "SAVEPOINT before_change", "CALL some_procedure",
			"ROLLBACK to savepoint", "KILL 12", "DROP everything", "CREATE TABLE users",
			"ALTER TABLE users", "TRUNCATE TABLE users", "LOCK TABLE users", "UNLOCK TABLE users",
			"RELEASE SAVEPOINT s", "DESC users", "DESCRIBE users", "name; DROP", "name -- comment",
			"name /* comment */" })
	void isValid_rejectsAnythingThatLooksLikeSql(String dataString) {
		assertFalse(validator.isValid(dataString, null));
	}

	@ParameterizedTest(name = "\"{0}\" is accepted")
	@ValueSource(strings = { "Asha Devi", "PHC Alpha", "9999999999", "report.pdf" })
	void isValid_acceptsOrdinaryText(String dataString) {
		validator.initialize(null);

		assertTrue(validator.isValid(dataString, null));
	}

	@ParameterizedTest(name = "an empty value is accepted")
	@NullAndEmptySource
	void isValid_acceptsAnEmptyValue(String dataString) {
		assertTrue(validator.isValid(dataString, null));
	}
}
