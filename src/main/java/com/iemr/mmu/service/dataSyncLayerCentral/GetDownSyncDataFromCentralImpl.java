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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncDataDigester;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncTableDetail;
import com.iemr.mmu.repo.syncActivity_syncLayer.DownSyncTableDetailRepo;


@Service
public class GetDownSyncDataFromCentralImpl implements GetDownSyncDataFromCentral {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private DataSyncRepositoryCentralDownload dataSyncRepositoryCentralDownload;

	@Autowired
	private DownSyncTableDetailRepo downSyncTableDetailRepo;

	public String getDownSyncDataForVan(DownSyncDataDigester downSyncDataDigester) throws Exception {
		if (downSyncDataDigester == null || downSyncDataDigester.getSchemaName() == null
				|| downSyncDataDigester.getTableName() == null)
			throw new Exception("Invalid down-sync request. Either schema or table info is missing/wrong");

		if (!downSyncDataDigester.isMasterTable() && downSyncDataDigester.getVanID() == null)
			throw new Exception("Invalid down-sync request. VanID is mandatory for a transactional table");

		List<Map<String, Object>> resultSetList = dataSyncRepositoryCentralDownload.getDownSyncDataFromTable(
				downSyncDataDigester.getSchemaName(), downSyncDataDigester.getTableName(),
				downSyncDataDigester.getServerColumnName(), downSyncDataDigester.getTableType(),
				downSyncDataDigester.getVanID(),
				resolveLastModColumn(downSyncDataDigester.getSchemaName(), downSyncDataDigester.getTableName()),
				downSyncDataDigester.getVanAutoIncColumnName(), downSyncDataDigester.getLastFetchedID(),
				downSyncDataDigester.getBatchSize());

		return downSyncGson().toJson(resultSetList);
	}

	private String resolveLastModColumn(String schemaName, String tableName) throws Exception {
		String lastModColumn = dataSyncRepositoryCentralDownload.resolveLastModColumn(schemaName, tableName);

		if (lastModColumn == null)
			throw new Exception(schemaName + "." + tableName
					+ " has neither LastModDate nor last_mod_date, so the down-sync cannot tell when a record was"
					+ " last changed");

		if (!lastModColumn.matches("^[a-zA-Z_][a-zA-Z0-9_]*$"))
			throw new Exception(schemaName + "." + tableName + " holds an invalid modification-time column name");

		return lastModColumn;
	}

	private Gson downSyncGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.serializeNulls();
		gsonBuilder.registerTypeAdapter(Timestamp.class,
				(JsonSerializer<Timestamp>) (src, type, context) -> new JsonPrimitive(src.toString()));
		gsonBuilder.registerTypeAdapter(java.sql.Date.class,
				(JsonSerializer<java.sql.Date>) (src, type, context) -> new JsonPrimitive(src.toString()));
		gsonBuilder.registerTypeAdapter(java.sql.Time.class,
				(JsonSerializer<java.sql.Time>) (src, type, context) -> new JsonPrimitive(src.toString()));
		gsonBuilder.registerTypeAdapter(LocalDateTime.class,
				(JsonSerializer<LocalDateTime>) (src, type, context) -> new JsonPrimitive(
						Timestamp.valueOf(src).toString()));
		return gsonBuilder.create();
	}

	public int updateDownSyncFlagPostDownload(DownSyncDataDigester downSyncDataDigester) throws Exception {
		if (downSyncDataDigester == null || downSyncDataDigester.getSchemaName() == null
				|| downSyncDataDigester.getTableName() == null || downSyncDataDigester.getRecords() == null)
			throw new Exception("Invalid request. Schema, table or record info is missing/wrong");

		String pkColumnName = downSyncDataDigester.getVanAutoIncColumnName();
		if (pkColumnName == null || pkColumnName.trim().isEmpty())
			throw new Exception("Invalid request. Primary key column of " + downSyncDataDigester.getTableName()
					+ " is missing in the request");

		return dataSyncRepositoryCentralDownload.updateDownSyncFlagPostDownload(downSyncDataDigester.getSchemaName(),
				downSyncDataDigester.getTableName(), pkColumnName.trim(), downSyncDataDigester.getRecords());
	}

	@Override
	public int markDownSyncedPostUpSync(String schemaName, String tableName, List<Object[]> vanSerialNoAndVanID) {
		if (schemaName == null || tableName == null || vanSerialNoAndVanID == null || vanSerialNoAndVanID.isEmpty())
			return 0;

		try {
			ArrayList<DownSyncTableDetail> tableDetails = downSyncTableDetailRepo
					.getActiveDownSyncTableByName(tableName);
			boolean downSyncEnabled = false;
			for (DownSyncTableDetail tableDetail : tableDetails) {
				if (tableDetail.isTransactionalTable())
					downSyncEnabled = true;
			}
			if (!downSyncEnabled)
				return 0;

			return dataSyncRepositoryCentralDownload.markDownSyncedPostUpSync(schemaName, tableName,
					vanSerialNoAndVanID);
		} catch (Exception e) {
			logger.warn("Could not mark the DownSynced flag for {}.{} post up-sync : {}", schemaName, tableName,
					e.getMessage());
			return 0;
		}
	}
}
