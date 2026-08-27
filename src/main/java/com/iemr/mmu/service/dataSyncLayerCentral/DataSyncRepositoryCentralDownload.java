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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncRecordAck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/***
 * 
 * @author NE298657
 *
 */

@Service
public class DataSyncRepositoryCentralDownload {
	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);

	}
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static final String DOWN_SYNC_TRANSACTIONAL = "TRANSACTIONAL";

	// Data Upload Repository
	public int checkRecordIsAlreadyPresentOrNot(String schemaName, String tableName, String vanSerialNo, String vanID,
			String vanAutoIncColumnName, int syncFacilityID) {
		jdbcTemplate = getJdbcTemplate();

		List<Object> params = new ArrayList<>();

		StringBuilder queryBuilder = new StringBuilder("SELECT ");
		queryBuilder.append(vanAutoIncColumnName);
		queryBuilder.append(" FROM ");
		queryBuilder.append(schemaName+"."+tableName);

		//params.add(vanAutoIncColumnName);
		//params.add(schemaName);
		//params.add(tableName);

		StringBuilder whereClause = new StringBuilder();
		whereClause.append(" WHERE ");
		whereClause.append("VanSerialNo = ?");
		params.add(vanSerialNo);

		if ((tableName.equalsIgnoreCase("t_patientissue") || tableName.equalsIgnoreCase("t_physicalstockentry")
				|| tableName.equalsIgnoreCase("t_stockadjustment") || tableName.equalsIgnoreCase("t_saitemmapping")
				|| tableName.equalsIgnoreCase("t_stocktransfer") || tableName.equalsIgnoreCase("t_patientreturn")
				|| tableName.equalsIgnoreCase("t_facilityconsumption") || tableName.equalsIgnoreCase("t_indent")
				|| tableName.equalsIgnoreCase("t_indentorder") || tableName.equalsIgnoreCase("t_indentissue")
				|| tableName.equalsIgnoreCase("t_itemstockentry") || tableName.equalsIgnoreCase("t_itemstockexit"))
				&& syncFacilityID > 0) {

			whereClause.append(" AND ");
			whereClause.append("SyncFacilityID = ?");
			params.add(syncFacilityID);

		}

		else {

			whereClause.append(" AND ");
			whereClause.append("VanID = ?");
			params.add(vanID);

		}

		queryBuilder.append(whereClause);
		String query = queryBuilder.toString();
		Object[] queryParams = params.toArray();
		List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query, queryParams);
		if (resultSet != null && resultSet.size() > 0)
			return 1;
		else
			return 0;
	}

	// Method for synchronization of data to central DB
	public int[] syncDataToCentralDB(String schema, String tableName, String serverColumns, String query,
			List<Object[]> syncDataList) {
		jdbcTemplate = getJdbcTemplate();
		if (query.startsWith("INSERT")) {
			for (int i = 0; i < syncDataList.size(); i++) {
				Object[] array = syncDataList.get(i);// Arrey 1

				if (query.startsWith("INSERT")) {
//					array = new Object[] {serverColumns, array };
					syncDataList.set(i, array);
				}
			}
		} else {
			for (int i = 0; i < syncDataList.size(); i++) {

				Object[] array = syncDataList.get(i);// Arrey 1
				String[] columnsArray = null;
				if(null != serverColumns)
				columnsArray = serverColumns.split(","); // arrey 2

				List<Object> Newarray = new ArrayList<>();

				int arrayIndex = 0;
				int columnsArrayIndex = 0;
				//Newarray.add(schema);
				//Newarray.add(tableName);
				//while (columnsArrayIndex < columnsArray.length || arrayIndex < array.length) {
					if (null != columnsArray && columnsArrayIndex < columnsArray.length) {
						Newarray.add(columnsArray[columnsArrayIndex]);
						columnsArrayIndex++;
					}

					/*
					 * if (arrayIndex < array.length) { Newarray.add(array); arrayIndex++; }
					 */
				//}

				// Convert Newarray back to an array
				//Object[] resultArray = Newarray.toArray(new Object[0]);
				syncDataList.set(i, array);

			}
		}
		// start batch insert/update
		int[] i = jdbcTemplate.batchUpdate(query, syncDataList);
		return i;

	}

	// End of Data Upload Repository

	public List<Map<String, Object>> getMasterDataFromTable(String schema, String table, String columnNames,
			String masterType, Timestamp lastDownloadDate, Integer vanID, Integer psmID) throws Exception {
		jdbcTemplate = getJdbcTemplate();
		List<Map<String, Object>> resultSetList =new ArrayList<>();
        String baseQuery = "";
		if (masterType != null) {
			if (lastDownloadDate != null) {
				if (masterType.equalsIgnoreCase("A")) {
					baseQuery += " SELECT " + columnNames + " FROM " + schema + "." + table
							+ " WHERE LastModDate >= ? ";
					resultSetList = jdbcTemplate.queryForList(baseQuery,lastDownloadDate);
					
				}
				else if (masterType.equalsIgnoreCase("V")) {
					baseQuery += " SELECT " + columnNames + " FROM " + schema + "." + table
							+ " WHERE LastModDate >= ? AND VanID = ? ";
					resultSetList = jdbcTemplate.queryForList(baseQuery,lastDownloadDate,vanID);
				}
				else if (masterType.equalsIgnoreCase("P")) {
					baseQuery += " SELECT " + columnNames + " FROM " + schema + "." + table
							+ " WHERE LastModDate >= ? AND ProviderServiceMapID = ? ";
					resultSetList = jdbcTemplate.queryForList(baseQuery,lastDownloadDate,psmID);
				}
			} else {
				if (masterType.equalsIgnoreCase("A")) {
					baseQuery += " SELECT " + columnNames + " FROM " + schema + "." + table;
					resultSetList = jdbcTemplate.queryForList(baseQuery);
				}
				else if (masterType.equalsIgnoreCase("V")) {
					baseQuery += " SELECT " + columnNames + " FROM " + schema + "." + table + " WHERE VanID = ? ";
					resultSetList = jdbcTemplate.queryForList(baseQuery,vanID);
				}
				else if (masterType.equalsIgnoreCase("P")) {
					baseQuery += " SELECT " + columnNames + " FROM " + schema + "." + table
							+ " WHERE ProviderServiceMapID = ? ";
					resultSetList = jdbcTemplate.queryForList(baseQuery,psmID);
				}
			}
		}
		logger.info("Select query central: " + baseQuery);
		logger.info("Last Downloaded Date " + lastDownloadDate);
		logger.info("Result set Details: " + resultSetList);
		return resultSetList;
	}

	// End of Data Download Repository

	// ---------------------------------- Down-Sync Repository (central -> local)

	public String resolveLastModColumn(String schema, String table) {
		jdbcTemplate = getJdbcTemplate();

		String query = " SELECT COLUMN_NAME FROM information_schema.COLUMNS "
				+ " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
				+ " AND COLUMN_NAME IN ('LastModDate', 'last_mod_date') "
				+ " ORDER BY FIELD(COLUMN_NAME, 'LastModDate', 'last_mod_date') ";

		List<String> found = jdbcTemplate.queryForList(query, String.class, schema, table);
		return (found == null || found.isEmpty()) ? null : found.get(0);
	}

	 public List<Map<String, Object>> getDownSyncDataFromTable(String schema, String table, String columnNames,
			String tableType, Integer vanID, String lastModColumn) throws Exception {
		jdbcTemplate = getJdbcTemplate();

		if (schema == null || table == null)
			throw new Exception("Invalid down-sync request. Schema/table info is missing");

		String columns = (columnNames == null || columnNames.trim().isEmpty()) ? " * " : columnNames;
		List<Map<String, Object>> resultSetList;

		if (DOWN_SYNC_TRANSACTIONAL.equalsIgnoreCase(tableType)) {
			if (vanID == null)
				throw new Exception("Invalid down-sync request. VanID is mandatory for transactional table " + table);

			String query = " SELECT " + columns + " FROM " + schema + "." + table
					+ " WHERE VanID = ? AND ( DownSynced IS NULL OR DownSynced IN ('N', 'U') "
					+ " OR ( DownSynced = 'P' AND DownSyncDate IS NOT NULL AND " + lastModColumn
					+ " > DownSyncDate ) ) ";
			logger.info("Down-sync select query for {}.{} : {}", schema, table, query);
			resultSetList = jdbcTemplate.queryForList(query, vanID);
		} else {
			String query = " SELECT " + columns + " FROM " + schema + "." + table;
			logger.info("Down-sync select query for {}.{} : {}", schema, table, query);
			resultSetList = jdbcTemplate.queryForList(query);
		}

		logger.info("Down-sync record count for {}.{} : {}", schema, table, resultSetList.size());
		return resultSetList;
	}

	public int updateDownSyncFlagPostDownload(String schema, String table, String pkColumnName,
			List<DownSyncRecordAck> records) throws Exception {
		jdbcTemplate = getJdbcTemplate();

		if (schema == null || table == null || pkColumnName == null)
			throw new Exception("Invalid request. Schema/table/primary key info is missing");
		if (records == null || records.isEmpty())
			return 0;

		List<Object[]> successWithSerialNo = new ArrayList<>();
		List<Object[]> successWithoutSerialNo = new ArrayList<>();
		List<Object[]> failed = new ArrayList<>();

		for (DownSyncRecordAck record : records) {
			if (record == null || record.getCentralID() == null)
				continue;

			if (record.isSuccess()) {
				if (record.getVanSerialNo() != null)
					successWithSerialNo.add(new Object[] { record.getVanSerialNo(), record.getCentralID() });
				else
					successWithoutSerialNo.add(new Object[] { record.getCentralID() });
			} else {
				failed.add(new Object[] { record.getFailureReason(), record.getCentralID() });
			}
		}

		int updatedRows = 0;

		if (!successWithSerialNo.isEmpty()) {
			String query = " UPDATE " + schema + "." + table
					+ " SET DownSynced = 'P', DownSyncDate = now(), DownSyncFailureReason = NULL, VanSerialNo = ? "
					+ " WHERE " + pkColumnName + " = ? ";
			updatedRows += countUpdatedRows(jdbcTemplate.batchUpdate(query, successWithSerialNo));
		}

		if (!successWithoutSerialNo.isEmpty()) {
			String query = " UPDATE " + schema + "." + table
					+ " SET DownSynced = 'P', DownSyncDate = now(), DownSyncFailureReason = NULL " + " WHERE "
					+ pkColumnName + " = ? ";
			updatedRows += countUpdatedRows(jdbcTemplate.batchUpdate(query, successWithoutSerialNo));
		}

		if (!failed.isEmpty()) {
			String query = " UPDATE " + schema + "." + table
					+ " SET DownSynced = 'F', DownSyncFailureReason = ? " + " WHERE " + pkColumnName + " = ? ";
			updatedRows += countUpdatedRows(jdbcTemplate.batchUpdate(query, failed));
		}

		logger.info("Down-sync flag updated for {}.{}. Records : {}, rows : {}", schema, table, records.size(),
				updatedRows);
		return updatedRows;
	}

	public int markDownSyncedPostUpSync(String schema, String table, List<Object[]> vanSerialNoAndVanID) {
		if (schema == null || table == null || vanSerialNoAndVanID == null || vanSerialNoAndVanID.isEmpty())
			return 0;

		jdbcTemplate = getJdbcTemplate();
		String query = " UPDATE " + schema + "." + table
				+ " SET DownSynced = 'P', DownSyncDate = now(), DownSyncFailureReason = NULL "
				+ " WHERE VanSerialNo = ? AND VanID = ? ";
		return countUpdatedRows(jdbcTemplate.batchUpdate(query, vanSerialNoAndVanID));
	}

	private int countUpdatedRows(int[] batchResult) {
		int count = 0;
		if (batchResult != null) {
			for (int rows : batchResult) {
				if (rows > 0)
					count += rows;
			}
		}
		return count;
	}

	// End of Down-Sync Repository
}
