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
package com.iemr.mmu.service.dataSyncLayerCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * A van row is matched on central by (VanSerialNo, VanID). Some diagnostic rows reached central without that key
 * (central's own poll, manual fixes, old FLW builds): the van's upload then finds nothing, the insert hits the unique
 * key on external_order_id and the row is rejected on every sync.
 *
 * This finds central's row by the table's own unique key (external_order_id, plus result_type / document_type), so
 * the van row can be synced onto it like a normal update. The van's vanSerialNo / vanID are written only when
 * central's row has no key yet; a key central already has is left as it is.
 *
 * Only the three diagnostic tables in TABLE_KEYS are handled; every other table syncs as before.
 */
@Service
public class VanKeyFallback {

	private static final Logger logger = LoggerFactory.getLogger(VanKeyFallback.class);
	static final String AUDIT = "[VAN-KEY-FALLBACK]";

	private static final String VAN_SERIAL_NO = "vanSerialNo";
	private static final String VAN_ID = "vanID";

	/** A table's business key: columns always needed, and columns used when the van sends them. */
	record BusinessKey(List<String> required, List<String> optional) {
	}

	// tb_diagnostic_result: V146 made the key (external_order_id, result_type); laptops still on the old schema do
	// not send result_type and are matched on external_order_id alone (refused if that finds several results)
	static final Map<String, BusinessKey> TABLE_KEYS = Map.of(
			"tb_diagnostic_order", new BusinessKey(List.of("external_order_id"), List.of()),
			"tb_diagnostic_result", new BusinessKey(List.of("external_order_id"), List.of("result_type")),
			"tb_diagnostic_document", new BusinessKey(List.of("external_order_id", "document_type"), List.of()));

	private static final Set<String> VALID_SCHEMAS = Set.of("db_iemr");

	enum Status {
		/** Table not covered, or the van row lacks its key values: sync goes on as before. */
		NOT_APPLICABLE,
		/** No central row has this business key: the van row is inserted as before. */
		NOT_FOUND,
		/** One central row has this business key: the van row is synced onto it. */
		FOUND,
		/** Several central rows match: cannot tell which one is this van's. */
		REFUSED
	}

	/**
	 * @param centralId id of central's row (FOUND)
	 * @param writeKey  true when central's row has no key yet and takes the van's vanSerialNo / vanID
	 * @param keyText   the business key, for logs and messages
	 */
	record Outcome(Status status, Object centralId, boolean writeKey, String keyText, String reason) {
		static final Outcome NOT_APPLICABLE = new Outcome(Status.NOT_APPLICABLE, null, false, null, null);

		static Outcome notFound(String keyText) {
			return new Outcome(Status.NOT_FOUND, null, false, keyText, null);
		}

		static Outcome found(Object centralId, boolean writeKey, String keyText) {
			return new Outcome(Status.FOUND, centralId, writeKey, keyText, null);
		}

		static Outcome refused(String keyText, String reason) {
			return new Outcome(Status.REFUSED, null, false, keyText, reason);
		}
	}

	@Autowired
	private DataSource dataSource;

	// a row created on central has no vanID (a van upload always carries one); may a van write its key on it?
	@Value("${dataSync.vanKeyFallback.adoptCentralRows:true}")
	private boolean adoptCentralRows = true;

	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate jdbc() {
		if (jdbcTemplate == null)
			jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate;
	}

	public boolean handles(String tableName) {
		return tableName != null && TABLE_KEYS.containsKey(tableName.toLowerCase());
	}

	private static boolean coversSchema(String schemaName) {
		return schemaName != null && VALID_SCHEMAS.contains(schemaName.toLowerCase());
	}

	public Outcome find(String schemaName, String tableName, Map<String, Object> record, String vanSerialNo,
			String vanID) {
		if (!handles(tableName) || !coversSchema(schemaName) || isEmpty(vanSerialNo) || isEmpty(vanID))
			return Outcome.NOT_APPLICABLE;
		String table = table(schemaName, tableName);

		BusinessKey key = TABLE_KEYS.get(tableName.toLowerCase());
		List<String> columns = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		for (String column : key.required()) {
			Object value = record.get(column);
			if (value == null)
				return Outcome.NOT_APPLICABLE;
			columns.add(column);
			values.add(value);
		}
		for (String column : key.optional()) {
			Object value = record.get(column);
			if (value != null) {
				columns.add(column);
				values.add(value);
			}
		}
		String keyText = describe(columns, values);

		// table comes from the schema allowlist + TABLE_KEYS, columns from TABLE_KEYS; values are bound
		List<Map<String, Object>> candidates = jdbc().queryForList("SELECT id, vanID, vanSerialNo FROM " + table // NOSONAR
				+ " WHERE " + String.join(" = ? AND ", columns) + " = ?", values.toArray());

		if (candidates.isEmpty())
			return Outcome.notFound(keyText);
		if (candidates.size() > 1)
			return Outcome.refused(keyText,
					candidates.size() + " rows on central match " + keyText + "; cannot tell which one is this van's");

		Map<String, Object> central = candidates.get(0);
		boolean writeKey = canTakeKey(central, vanID, adoptCentralRows)
				&& !keyUsedByAnotherRow(table, vanSerialNo, vanID, central.get("id"));
		return Outcome.found(central.get("id"), writeKey, keyText);
	}

	/**
	 * The normal sync UPDATE (same data columns, same values), matched on central's id instead of the van key.
	 * vanSerialNo / vanID are set only with writeKey, to the exact key the next upload looks up (the van's row ID and
	 * VanID), and then only while the row still has no key (another upload may have given it one meanwhile). Without
	 * writeKey central's own key stays as it is.
	 */
	public String updateQuery(String schemaName, String tableName, List<String> serverColumns, boolean writeKey) {
		StringBuilder set = new StringBuilder();
		for (String column : serverColumns) {
			if (!isVanKeyColumn(column))
				set.append(set.length() == 0 ? "" : ", ").append(column.trim()).append(" = ?");
		}
		if (writeKey)
			set.append(set.length() == 0 ? "" : ", ").append("vanSerialNo = ?, vanID = ?");
		String query = "UPDATE " + table(schemaName, tableName) + " SET " + set + " WHERE id = ?";
		return writeKey ? query + " AND vanSerialNo IS NULL" : query;
	}

	/** Values for updateQuery, in the same order: data values, then the van key (writeKey), then central's id. */
	public Object[] updateParams(List<String> serverColumns, Object[] values, boolean writeKey, String vanSerialNo,
			String vanID, Object centralId) {
		List<Object> params = new ArrayList<>();
		for (int i = 0; i < serverColumns.size(); i++) {
			if (!isVanKeyColumn(serverColumns.get(i)))
				params.add(values[i]);
		}
		if (writeKey) {
			params.add(vanSerialNo);
			params.add(vanID);
		}
		params.add(centralId);
		return params.toArray();
	}

	void logSynced(String schemaName, String tableName, Outcome found, String vanSerialNo, String vanID) {
		if (found.writeKey())
			logger.info("{} {}.{} id={} {}: synced by business key, key written -> (vanID={}, vanSerialNo={})", AUDIT,
					schemaName, tableName, found.centralId(), found.keyText(), vanID, vanSerialNo);
		else
			logger.info("{} {}.{} id={} {}: synced by business key, central key kept (van {} / no. {} not written)",
					AUDIT, schemaName, tableName, found.centralId(), found.keyText(), vanID, vanSerialNo);
	}

	/** Central's row has no key yet and is this van's (or central-created, when allowed). */
	static boolean canTakeKey(Map<String, Object> central, String vanID, boolean adoptCentralRows) {
		if (central.get("vanSerialNo") != null)
			return false;
		Object centralVanID = central.get("vanID");
		if (centralVanID == null)
			return adoptCentralRows;
		return sameId(centralVanID, vanID);
	}

	private boolean keyUsedByAnotherRow(String table, String vanSerialNo, String vanID, Object centralId) {
		Integer taken = jdbc().queryForObject("SELECT COUNT(*) FROM " + table // NOSONAR
				+ " WHERE vanSerialNo = ? AND vanID = ? AND id <> ?", Integer.class, vanSerialNo, vanID, centralId);
		return taken != null && taken > 0;
	}

	private static boolean isVanKeyColumn(String column) {
		String name = column.trim();
		return VAN_SERIAL_NO.equalsIgnoreCase(name) || VAN_ID.equalsIgnoreCase(name);
	}

	private String table(String schemaName, String tableName) {
		if (schemaName == null || !VALID_SCHEMAS.contains(schemaName.toLowerCase()) || !handles(tableName))
			throw new IllegalArgumentException("Invalid table for van key fallback: " + schemaName + "." + tableName);
		return schemaName + "." + tableName.toLowerCase();
	}

	private static boolean sameId(Object a, String b) {
		try {
			return Long.parseLong(String.valueOf(a).trim()) == Long.parseLong(b.trim());
		} catch (NumberFormatException e) {
			return String.valueOf(a).trim().equals(b.trim());
		}
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim());
	}

	private static String describe(List<String> columns, List<Object> values) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < columns.size(); i++)
			text.append(i == 0 ? "" : ", ").append(columns.get(i)).append('=').append(values.get(i));
		return text.toString();
	}
}
