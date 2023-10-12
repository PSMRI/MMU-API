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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/***
 * 
 * @author NE298657
 *
 */

@Service
public class DataSyncRepositoryCentral {
	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);

	}

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
		List<Map<String, Object>> resultSetList;

		StringBuilder queryBuilder = new StringBuilder("SELECT ");

		List<Object> params = new ArrayList<>();

		StringBuilder whereClause = new StringBuilder();

		if (masterType != null) {

			if (masterType.equalsIgnoreCase("A")) {
				queryBuilder.append("?");
				queryBuilder.append(" FROM ");
				queryBuilder.append("?.?");
				params.add(columnNames);
				params.add(schema);
				params.add(table);
				if (lastDownloadDate != null) {
					whereClause.append(" WHERE ");
					whereClause.append("Date(LastModDate) >= ?");
					params.add(lastDownloadDate);
				}
			} else if (masterType.equalsIgnoreCase("V")) {
				queryBuilder.append("?");
				queryBuilder.append(" FROM ");
				queryBuilder.append("?.?");
				params.add(columnNames);
				params.add(schema);
				params.add(table);
				whereClause.append(" WHERE ");
				if (lastDownloadDate != null) {
					whereClause.append("Date(LastModDate) >= ?");
					params.add(lastDownloadDate);
					whereClause.append(" AND ");
				}
				whereClause.append("VanID = ?");
				params.add(vanID);
			} else if (masterType.equalsIgnoreCase("P")) {
				queryBuilder.append("?");
				queryBuilder.append(" FROM ");
				queryBuilder.append("?.?");
				params.add(columnNames);
				params.add(schema);
				params.add(table);
				whereClause.append(" WHERE ");
				if (lastDownloadDate != null) {
					whereClause.append(" AND ");
				}
				whereClause.append("ProviderServiceMapID = ?");
				params.add(psmID);
			}
		}
		queryBuilder.append(whereClause);

		// Use PreparedStatement to avoid SQL injection and improve performance
		String query = queryBuilder.toString();
		Object[] queryParams = params.toArray();

		// Use PreparedStatement for the entire query
		resultSetList = jdbcTemplate.queryForList(query, queryParams);
		return resultSetList;
	}

	// End of Data Download Repository
}
