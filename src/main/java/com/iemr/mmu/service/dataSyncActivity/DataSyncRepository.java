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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.iemr.mmu.repo.syncActivity_syncLayer.SyncUtilityClassRepo;

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
					+ " SET created_date = ? , processed = ?, SyncedDate = ?, Syncedby = ? , SyncFailureReason = ? "
					+ "WHERE " + autoIncreamentColumn + " IN (" + String.join(",", vanSerialNos) + ")";
		} else {
			query = "UPDATE " + schemaName + "." + tableName
					+ " SET CreatedDate = ? , processed = ?, SyncedDate = ?, Syncedby = ? , SyncFailureReason = ? "
					+ "WHERE " + autoIncreamentColumn + " IN (" + String.join(",", vanSerialNos) + ")";
		}

		Timestamp syncedDate = new Timestamp(System.currentTimeMillis());
		int updatedRows = jdbcTemplate.update(query, syncedDate, status, syncedDate, user, reason);
		return updatedRows;

	}

	// ---------------------------------- End of Upload repository

	// ---------------------------------- Download Repository
	public int[] updateLatestMasterInLocal(String query, List<Object[]> syncDataList) {
		int[] i = null;
		// get JDBC template
		jdbcTemplate = getJdbcTemplate();
		// start batch insert/update
		i = jdbcTemplate.batchUpdate(query, syncDataList);

		return i;

	}

	// ---------------------------------- End of Download Repository

}