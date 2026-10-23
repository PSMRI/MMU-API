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
package com.iemr.mmu.service.dataSyncActivity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUtilityClass;
import com.iemr.mmu.repo.syncActivity_syncLayer.SyncUtilityClassRepo;
import com.iemr.mmu.utils.validator.SqlIdentifierValidator;

/***
 * 
 * @author NE298657
 *
 */

@Service
public class DataSyncRepository {
	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SyncUtilityClassRepo syncutilityClassRepo;

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);

	}

	// ---------------------------- Upload repository
	public List<Map<String, Object>> getDataForGivenSchemaAndTable(String schema, String table, String columnNames)
			throws Exception {
		jdbcTemplate = getJdbcTemplate();
		String baseQuery;
		List<Map<String, Object>> resultSetList = new ArrayList<>();

		if (table != null && table.equalsIgnoreCase("m_beneficiaryregidmapping")) {
			baseQuery = " SELECT " + columnNames + " FROM " + schema + "." + table
					+ " WHERE provisioned is true AND processed != 'P' AND vanID is not null ";
		} else {
			if (table != null && (table.equalsIgnoreCase("t_patientissue")
					|| table.equalsIgnoreCase("t_physicalstockentry") || table.equalsIgnoreCase("t_stockadjustment")
					|| table.equalsIgnoreCase("t_saitemmapping") || table.equalsIgnoreCase("t_stocktransfer")
					|| table.equalsIgnoreCase("t_patientreturn") || table.equalsIgnoreCase("t_facilityconsumption")
					|| table.equalsIgnoreCase("t_indent") || table.equalsIgnoreCase("t_indentorder")
					|| table.equalsIgnoreCase("t_indentissue") || table.equalsIgnoreCase("t_itemstockentry")
					|| table.equalsIgnoreCase("t_itemstockexit"))) {

				baseQuery = " SELECT " + columnNames + " FROM " + schema + "." + table
						+ " WHERE processed != 'P' AND SyncFacilityID is not null  ";

			} else {
				baseQuery = " SELECT " + columnNames + " FROM " + schema + "." + table
						+ " WHERE processed != 'P' AND vanID is not null ";
			}

		}

		logger.info("Select Query started:");
		logger.info("Table Name: {}", table);

		logger.info("Select Query: {}", baseQuery);

		resultSetList = jdbcTemplate.queryForList(baseQuery);
		return resultSetList;
	}

	public int updateProcessedFlagInVan(String schemaName, String tableName, List<String> vanSerialNos,
			String autoIncreamentColumn, String user, String status, String reason) throws Exception {
		jdbcTemplate = getJdbcTemplate();
		String query = "";

		logger.info("Updating processed flag in table: " + tableName + " for vanSerialNos: " + vanSerialNos);
	
		if (tableName != null && tableName.toLowerCase().equals("i_ben_flow_outreach")) {
			query = "UPDATE " + schemaName + "." + tableName
					+ " SET processed = ?, SyncedDate = ?, Syncedby = ? , SyncFailureReason = ? "
					+ "WHERE " + autoIncreamentColumn + " IN (" + String.join(",", vanSerialNos) + ")";
		} else {
			query = "UPDATE " + schemaName + "." + tableName
					+ " SET processed = ?, SyncedDate = ?, Syncedby = ? , SyncFailureReason = ? "
					+ "WHERE " + autoIncreamentColumn + " IN (" + String.join(",", vanSerialNos) + ")";
		}

		Timestamp syncedDate = new Timestamp(System.currentTimeMillis());
		int updatedRows = jdbcTemplate.update(query, status, syncedDate, user, reason);
		return updatedRows;

	}

	/** Service line (MMU) of the van, used for an automatic master download. */
	public Integer getVanProviderServiceMapID(int vanID) {
		List<Integer> psm = getJdbcTemplate().queryForList(
				"SELECT ProviderServiceMapID FROM db_iemr.m_van WHERE VanID = ? AND Deleted = false", Integer.class,
				vanID);
		return psm.isEmpty() ? null : psm.get(0);
	}

	/**
	 * Rows central rejected on upload (Processed = 'F'), per sync table and reason. They are sent again on every
	 * upload until central accepts them.
	 */
	public List<Map<String, Object>> getSyncFailures() {
		jdbcTemplate = getJdbcTemplate();
		List<Map<String, Object>> result = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for (SyncUtilityClass table : syncutilityClassRepo.findByDeletedOrderBySyncTableDetailID(false)) {
			if (Boolean.TRUE.equals(table.getIsMaster()) || table.getSchemaName() == null
					|| table.getTableName() == null)
				continue;
			String qualified = SqlIdentifierValidator.validatedSchemaName(table.getSchemaName()) + "."
					+ SqlIdentifierValidator.validatedTableName(table.getTableName());
			if (!seen.add(qualified.toLowerCase()))
				continue;
			try {
				List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT '" + table.getTableName()
						+ "' AS tableName, SyncFailureReason AS reason, COUNT(*) AS failedRows, DATE_FORMAT(MAX(SyncedDate), '%d-%m-%Y %H:%i') AS lastTried "
						+ "FROM " + qualified + " WHERE Processed = 'F' GROUP BY SyncFailureReason");
				result.addAll(rows);
			} catch (Exception e) {
				logger.warn("Sync failure check skipped for {}: {}", qualified, e.getMessage());
			}
		}
		return result;
	}

	// ---------------------------------- End of Upload repository

	// ---------------------------------- Download Repository

	/**
	 * Down-sync copies the beneficiary tables but not i_ben_flow_outreach, and the tab download
	 * (FLW-API getBeneficiaryData) only serves beneficiaries that have a worklist row. So a
	 * beneficiary registered elsewhere and down-synced here never reached this laptop's tabs.
	 * Creates the missing worklist row for this van's beneficiaries, filled as TM-API does at
	 * registration. Processed = 'P' so it is not uploaded back: central already has its own row.
	 */
	public int createMissingWorklistRows(int vanID, int providerServiceMapID) {
		jdbcTemplate = getJdbcTemplate();

		String village = "(SELECT h.%s FROM db_identity.i_beneficiarydetails_rmnch r "
				+ " JOIN db_identity.i_householddetails h ON h.houseoldId = r.houseoldId "
				+ " WHERE r.BeneficiaryRegID = bm.BenRegId LIMIT 1)";

		String insert = "INSERT INTO db_iemr.i_ben_flow_outreach "
				+ " (beneficiary_reg_id, beneficiary_id, visit_no, ben_name, ben_age, ben_dob, ben_gender, "
				+ "  ben_gender_val, ben_age_val, ben_phone_no, father_name, spouse_name, nurse_flag, doctor_flag, "
				+ "  pharmacist_flag, doctor_signature_flag, BenArrivedFlag, created_by, created_date, modified_by, "
				+ "  modified_date, registrationDate, districtID, district, villageID, village, "
				+ "  providerServiceMapID, vanID, Processed, deleted) "
				+ " SELECT * FROM ( SELECT bm.BenRegId, rg.BeneficiaryID, 1 AS visit_no, "
				+ "  TRIM(CONCAT_WS(' ', d.FirstName, NULLIF(d.MiddleName, ''), d.LastName)) AS ben_name, "
				+ "  CONCAT(TIMESTAMPDIFF(YEAR, d.DOB, bm.CreatedDate), ' years - ', "
				+ "         MOD(TIMESTAMPDIFF(MONTH, d.DOB, bm.CreatedDate), 12), ' months') AS ben_age, "
				+ "  d.DOB, d.Gender, d.GenderId, TIMESTAMPDIFF(YEAR, d.DOB, bm.CreatedDate) AS ben_age_val, "
				+ "  COALESCE(c.PreferredPhoneNum, c.PhoneNum1) AS phone, "
				+ "  COALESCE(d.FatherName, '') AS father, COALESCE(d.SpouseName, '') AS spouse, "
				+ "  1 AS nf, 0 AS df, 0 AS pf, 0 AS dsf, 0 AS baf, bm.CreatedBy, bm.CreatedDate AS cdate, "
				+ "  'downsync' AS mby, NOW() AS mdate, bm.CreatedDate AS rdate, "
				+ "  COALESCE(a.CurrDistrictId, a.PermDistrictId) AS districtID, "
				+ "  COALESCE(a.CurrDistrict, a.PermDistrict) AS district, "
				+ "  COALESCE(" + String.format(village, "villageid") + ", a.CurrVillageId, a.PermVillageId) AS villageID, "
				+ "  COALESCE(" + String.format(village, "village") + ", a.CurrVillage, a.PermVillage) AS village, "
				+ "  ? AS psm, bm.VanID, 'P' AS processed, 0 AS deleted "
				+ " FROM db_identity.i_beneficiarymapping bm "
				+ " JOIN db_identity.m_beneficiaryregidmapping rg ON rg.BenRegId = bm.BenRegId "
				+ " JOIN db_identity.i_beneficiarydetails d ON d.BeneficiaryDetailsId = bm.BenDetailsId "
				+ " LEFT JOIN db_identity.i_beneficiarycontacts c ON c.BenContactsID = bm.BenContactsId "
				+ " LEFT JOIN db_identity.i_beneficiaryaddress a ON a.BenAddressID = bm.BenAddressId "
				+ " WHERE bm.VanID = ? AND bm.Deleted = false "
				// the van can hold beneficiaries of other service lines; only its own get a row
				+ "   AND (SELECT r.ProviderServiceMapID FROM db_identity.i_beneficiarydetails_rmnch r "
				+ "        WHERE r.BeneficiaryRegID = bm.BenRegId LIMIT 1) = ? "
				+ "   AND NOT EXISTS (SELECT 1 FROM db_iemr.i_ben_flow_outreach f "
				+ "                   WHERE f.beneficiary_reg_id = bm.BenRegId) ) x "
				+ " WHERE x.villageID IS NOT NULL";

		int inserted = jdbcTemplate.update(insert, providerServiceMapID, vanID, providerServiceMapID);

		if (inserted > 0)
			// same convention as TM-API: VanSerialNo is the row's own id
			jdbcTemplate.update("UPDATE db_iemr.i_ben_flow_outreach SET VanSerialNo = ben_flow_id "
					+ " WHERE modified_by = 'downsync' AND VanSerialNo IS NULL");

		return inserted;
	}

	public int[] updateLatestMasterInLocal(String query, List<Object[]> syncDataList) {
		int[] i = null;
		// get JDBC template
		jdbcTemplate = getJdbcTemplate();
		// start batch insert/update
		i = jdbcTemplate.batchUpdate(query, syncDataList);

		return i;

	}

	// ---------------------------------- End of Download Repository

	// ---------------------------------- Down-Sync Repository (central -> local)

	public Map<String, String> getSyncGroupNamesByTable() {
		jdbcTemplate = getJdbcTemplate();

		String query = " SELECT LOWER(TRIM(s.TableName)) AS TableName, g.SyncTableGroupName "
				+ " FROM db_iemr.m_synctabledetail s "
				+ " JOIN db_iemr.m_synctablegroup g ON g.SyncTableGroupID = s.SyncTableGroupID "
				+ " WHERE IFNULL(s.Deleted, b'0') = b'0' AND IFNULL(g.Deleted, b'0') = b'0' "
				+ " AND s.TableName IS NOT NULL ";

		Map<String, String> groups = new LinkedHashMap<>();
		for (Map<String, Object> row : jdbcTemplate.queryForList(query)) {
			Object table = row.get("TableName");
			Object group = row.get("SyncTableGroupName");
			if (table != null && group != null)
				groups.putIfAbsent(String.valueOf(table), String.valueOf(group));
		}
		return groups;
	}

	public String resolveLastModColumn(String schema, String table) {
		jdbcTemplate = getJdbcTemplate();

		String query = " SELECT COLUMN_NAME FROM information_schema.COLUMNS "
				+ " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
				+ " AND COLUMN_NAME IN ('LastModDate', 'last_mod_date') "
				+ " ORDER BY FIELD(COLUMN_NAME, 'LastModDate', 'last_mod_date') ";

		List<String> found = jdbcTemplate.queryForList(query, String.class, schema, table);
		return (found == null || found.isEmpty()) ? null : found.get(0);
	}

	public Set<String> getTableColumns(String schema, String table) {
		jdbcTemplate = getJdbcTemplate();

		String query = " SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ";

		Set<String> columns = new HashSet<>();
		for (String c : jdbcTemplate.queryForList(query, String.class, schema, table))
			columns.add(c.toLowerCase());
		return columns;
	}

	public List<String> getDownSyncColumns(String schema, String table) {
		jdbcTemplate = getJdbcTemplate();

		String query = " SELECT COLUMN_NAME FROM information_schema.COLUMNS "
				+ " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
				+ " AND COLUMN_NAME NOT IN ('CentralID', 'DownSynced', 'DownSyncDate', 'DownSyncFailureReason', "
				+ " 'LastDownSyncDate', 'Processed', 'SyncFailureReason', 'SyncedBy', 'SyncedDate') "
				+ " ORDER BY COLUMN_NAME ";

		return jdbcTemplate.queryForList(query, String.class, schema, table);
	}

	public Map<String, Object> getLocalRecordForDownSync(String schema, String table, String autoIncColumnName,
			Object centralID, Object vanID, String lastModColumn, boolean preserveCentralPK) {
		if (centralID == null)
			return null;

		jdbcTemplate = getJdbcTemplate();
		// schema, table & column names cannot be bound as query parameters, so each of
		// them is validated before it is concatenated into the query
		String validSchema = SqlIdentifierValidator.validatedSchemaName(schema);
		String validTable = SqlIdentifierValidator.validatedTableName(table);
		String validPkColumn = SqlIdentifierValidator.validatedColumnName(autoIncColumnName);
		String validLastModColumn = SqlIdentifierValidator.validatedColumnName(lastModColumn);

		StringBuilder query = new StringBuilder(" SELECT ").append(validPkColumn).append(", CentralID, Processed, ")
				.append(validLastModColumn).append(" AS LastModDate, LastDownSyncDate FROM ").append(validSchema)
				.append(".").append(validTable);

		List<Object> params = new ArrayList<>();

		if (preserveCentralPK) {
			query.append(" WHERE ( CentralID = ? OR ").append(validPkColumn).append(" = ? ) ");
			params.add(centralID);
			params.add(centralID);
		} else {
			query.append(" WHERE CentralID = ? AND VanID = ? ");
			params.add(centralID);
			params.add(vanID);
		}

		List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query.toString(), params.toArray());
		if (resultSet == null || resultSet.isEmpty())
			return null;

		return resultSet.get(0);
	}

	/***
	 * Stamps the central key on a local row that was matched by its primary key, so
	 * the later runs of the down-sync find it on the CentralID match itself.
	 */
	public int stampCentralIDInLocal(String schema, String table, String autoIncColumnName, Object localID,
			Object centralID) {
		if (localID == null || centralID == null)
			return 0;

		jdbcTemplate = getJdbcTemplate();
		// schema, table & column names cannot be bound as query parameters, so each of
		// them is validated before it is concatenated into the query
		String validSchema = SqlIdentifierValidator.validatedSchemaName(schema);
		String validTable = SqlIdentifierValidator.validatedTableName(table);
		String validPkColumn = SqlIdentifierValidator.validatedColumnName(autoIncColumnName);

		String query = " UPDATE " + validSchema + "." + validTable + " SET CentralID = ? WHERE " + validPkColumn
				+ " = ? AND CentralID IS NULL ";

		return jdbcTemplate.update(query, centralID, localID);
	}

	public Long insertDownSyncRecordInLocal(final String query, final Object[] params) {
		jdbcTemplate = getJdbcTemplate();
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			return ps;
		}, keyHolder);

		Number key = keyHolder.getKey();
		return key != null ? key.longValue() : null;
	}

	public int updateDownSyncRecordInLocal(String query, Object[] params) {
		jdbcTemplate = getJdbcTemplate();
		return jdbcTemplate.update(query, params);
	}

	public int updateVanSerialNoInLocal(String schema, String table, String autoIncColumnName, Object localID) {
		jdbcTemplate = getJdbcTemplate();
		// schema, table & column names cannot be bound as query parameters, so each of
		// them is validated before it is concatenated into the query
		String validSchema = SqlIdentifierValidator.validatedSchemaName(schema);
		String validTable = SqlIdentifierValidator.validatedTableName(table);
		String validPkColumn = SqlIdentifierValidator.validatedColumnName(autoIncColumnName);

		String query = " UPDATE " + validSchema + "." + validTable + " SET VanSerialNo = ? WHERE "
				+ validPkColumn + " = ? ";
		return jdbcTemplate.update(query, localID, localID);
	}

	
	public Long resolveLocalIdForCentralValue(String schema, String table, String pkColumn, Object centralValue,
			Object vanID) {
		if (centralValue == null)
			return null;

		jdbcTemplate = getJdbcTemplate();
		// schema, table & column names cannot be bound as query parameters, so each of
		// them is validated before it is concatenated into the query
		String validSchema = SqlIdentifierValidator.validatedSchemaName(schema);
		String validTable = SqlIdentifierValidator.validatedTableName(table);
		String validPkColumn = SqlIdentifierValidator.validatedColumnName(pkColumn);
		String from = " FROM " + validSchema + "." + validTable + " WHERE ";

		Long resolved = queryForFirstLong(" SELECT " + validPkColumn + from + " CentralID = ? AND VanID = ? ",
				centralValue, vanID);
		if (resolved != null)
			return resolved;

		resolved = queryForFirstLong(" SELECT " + validPkColumn + from + " VanSerialNo = ? AND VanID = ? ",
				centralValue, vanID);
		if (resolved != null)
			return resolved;

		return queryForFirstLong(" SELECT " + validPkColumn + from + validPkColumn + " = ? AND VanID = ? ",
				centralValue, vanID);
	}

	public int countConflictsInLocal(String schema, String table, Object vanID) {
		jdbcTemplate = getJdbcTemplate();
		// schema & table cannot be bound as query parameters, so both are validated
		// before they are concatenated into the query
		String validSchema = SqlIdentifierValidator.validatedSchemaName(schema);
		String validTable = SqlIdentifierValidator.validatedTableName(table);

		String query = " SELECT COUNT(*) FROM " + validSchema + "." + validTable
				+ " WHERE VanID = ? AND Processed = 'F' AND SyncFailureReason = 'CONFLICT' ";
		Integer count = jdbcTemplate.queryForObject(query, Integer.class, vanID);
		return count == null ? 0 : count;
	}

	public List<Map<String, Object>> getForeignKeysOfTable(String schema, String table) {
		jdbcTemplate = getJdbcTemplate();
		String query = " SELECT k.CONSTRAINT_NAME, k.COLUMN_NAME AS CHILD_COLUMN,"
				+ " k.REFERENCED_TABLE_SCHEMA AS PARENT_SCHEMA, k.REFERENCED_TABLE_NAME AS PARENT_TABLE,"
				+ " k.REFERENCED_COLUMN_NAME AS PARENT_COLUMN "
				+ " FROM information_schema.KEY_COLUMN_USAGE k "
				+ " WHERE k.TABLE_SCHEMA = ? AND k.TABLE_NAME = ? AND k.REFERENCED_TABLE_NAME IS NOT NULL ";

		List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(query, schema, table);
		if (foreignKeys == null)
			return new ArrayList<>();

		Map<String, Integer> columnsPerConstraint = new LinkedHashMap<>();
		for (Map<String, Object> foreignKey : foreignKeys) {
			columnsPerConstraint.merge(String.valueOf(foreignKey.get("CONSTRAINT_NAME")), 1, Integer::sum);
		}

		List<Map<String, Object>> singleColumn = new ArrayList<>();
		for (Map<String, Object> foreignKey : foreignKeys) {
			if (columnsPerConstraint.get(String.valueOf(foreignKey.get("CONSTRAINT_NAME"))) == 1)
				singleColumn.add(foreignKey);
		}
		return singleColumn;
	}

	public boolean isColumnNullable(String schema, String table, String column) {
		jdbcTemplate = getJdbcTemplate();
		List<String> nullable = jdbcTemplate.queryForList(
				" SELECT IS_NULLABLE FROM information_schema.COLUMNS "
						+ " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ? ",
				String.class, schema, table, column);
		return !nullable.isEmpty() && "YES".equalsIgnoreCase(nullable.get(0));
	}

	private Long queryForFirstLong(String query, Object... params) {
		List<Long> found = jdbcTemplate.queryForList(query, Long.class, params);
		return (found == null || found.isEmpty()) ? null : found.get(0);
	}

	public int markDownSyncConflictInLocal(String schema, String table, String autoIncColumnName, Object localID) {
		jdbcTemplate = getJdbcTemplate();
		// schema, table & column names cannot be bound as query parameters, so each of
		// them is validated before it is concatenated into the query
		String validSchema = SqlIdentifierValidator.validatedSchemaName(schema);
		String validTable = SqlIdentifierValidator.validatedTableName(table);
		String validPkColumn = SqlIdentifierValidator.validatedColumnName(autoIncColumnName);

		String query = " UPDATE " + validSchema + "." + validTable
				+ " SET Processed = 'F', SyncFailureReason = 'CONFLICT',"
				+ " DownSynced = 'F', DownSyncFailureReason = 'CONFLICT' WHERE " + validPkColumn
				+ " = ? ";
		return jdbcTemplate.update(query, localID);
	}

	// End of Down-Sync Repository


}