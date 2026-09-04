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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SqlIdentifierValidatorTest {

	@Test
	void validatedSchemaNameShouldReturnTheAllowListedConstant() {
		assertEquals("db_iemr", SqlIdentifierValidator.validatedSchemaName("DB_IEMR"));
		assertEquals("db_identity", SqlIdentifierValidator.validatedSchemaName("db_identity"));
	}

	@Test
	void validatedSchemaNameShouldRejectAnUnknownSchema() {
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedSchemaName("db_unknown"));
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedSchemaName(null));
	}

	@Test
	void validatedTableNameShouldAcceptAPlainIdentifier() {
		assertEquals("t_pnccare", SqlIdentifierValidator.validatedTableName("t_pnccare"));
	}

	@Test
	void validatedTableNameShouldRejectAnInjectedPayload() {
		assertThrows(IllegalArgumentException.class,
				() -> SqlIdentifierValidator.validatedTableName("t_pnccare; DROP TABLE t_pnccare --"));
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedTableName("t_pnc care"));
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedTableName("1_pnccare"));
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedTableName(""));
	}

	@Test
	void validatedColumnListShouldReturnTheTrimmedList() {
		assertEquals("ID, VanID, VanSerialNo",
				SqlIdentifierValidator.validatedColumnList(" ID , VanID ,VanSerialNo "));
	}

	@Test
	void validatedColumnListShouldRejectAnInjectedColumn() {
		assertThrows(IllegalArgumentException.class,
				() -> SqlIdentifierValidator.validatedColumnList("ID, VanID, (SELECT password FROM m_user)"));
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedColumnList("  "));
		assertThrows(IllegalArgumentException.class, () -> SqlIdentifierValidator.validatedColumnList(null));
	}

	@Test
	void isValidIdentifierShouldRejectAnOverlyLongName() {
		assertTrue(SqlIdentifierValidator.isValidIdentifier("a".repeat(64)));
		assertFalse(SqlIdentifierValidator.isValidIdentifier("a".repeat(65)));
	}
}
