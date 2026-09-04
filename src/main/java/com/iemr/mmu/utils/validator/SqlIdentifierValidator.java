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

import java.util.Set;
import java.util.regex.Pattern;

/***
 * Validates the schema, table and column names that the data-sync layer has to
 * concatenate into its dynamic SQL, because a schema/table/column name cannot be
 * supplied as a prepared-statement parameter. Every such identifier reaching a
 * query must first be passed through this class, so that nothing but a plain SQL
 * identifier can ever become part of a statement.
 */
public final class SqlIdentifierValidator {

	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	private static final int MAX_IDENTIFIER_LENGTH = 64;

	private static final Set<String> VALID_SCHEMAS = Set.of("public", "db_iemr", "db_identity", "apl_db_iemr",
			"apl_db_identity", "db_iemr_sync", "db_identity_sync");

	private SqlIdentifierValidator() {
		// utility class
	}

	public static boolean isValidIdentifier(String identifier) {
		return identifier != null && identifier.length() <= MAX_IDENTIFIER_LENGTH
				&& IDENTIFIER_PATTERN.matcher(identifier).matches();
	}

	/***
	 * Returns the allow-listed constant matching the given schema name, so that the
	 * value concatenated into a query never originates from the caller.
	 */
	public static String validatedSchemaName(String schemaName) {
		if (schemaName != null) {
			for (String allowedSchema : VALID_SCHEMAS) {
				if (allowedSchema.equalsIgnoreCase(schemaName)) {
					return allowedSchema;
				}
			}
		}
		throw new IllegalArgumentException("Invalid schema name provided for the data-sync query");
	}

	public static String validatedIdentifier(String identifier, String identifierType) {
		if (!isValidIdentifier(identifier)) {
			throw new IllegalArgumentException(
					"Invalid " + identifierType + " provided for the data-sync query");
		}
		return identifier;
	}

	public static String validatedTableName(String tableName) {
		return validatedIdentifier(tableName, "table name");
	}

	public static String validatedColumnName(String columnName) {
		return validatedIdentifier(columnName, "column name");
	}

	/***
	 * Validates a comma separated column list, as used in the SELECT clause of the
	 * sync queries. Returns the trimmed, comma separated list built back from the
	 * validated names.
	 */
	public static String validatedColumnList(String columnNames) {
		if (columnNames == null || columnNames.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid column list provided for the data-sync query");
		}

		StringBuilder validatedColumns = new StringBuilder();
		for (String columnName : columnNames.split(",")) {
			if (validatedColumns.length() > 0) {
				validatedColumns.append(", ");
			}
			validatedColumns.append(validatedColumnName(columnName.trim()));
		}
		return validatedColumns.toString();
	}
}
