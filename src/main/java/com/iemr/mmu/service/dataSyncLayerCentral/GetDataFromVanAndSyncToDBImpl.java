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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.utils.mapper.InputMapper;

/***
 * 
 * @author NE298657
 *
 */

@Service
public class GetDataFromVanAndSyncToDBImpl implements GetDataFromVanAndSyncToDB {

	private static final String ServerColumnsNotRequired = null;
	@Autowired
	private DataSyncRepositoryCentral dataSyncRepositoryCentral;

	public String syncDataToServer(String requestOBJ, String Authorization) throws Exception {

		// feed sync request
		ObjectMapper mapper = new ObjectMapper();
		SyncUploadDataDigester syncUploadDataDigester = mapper.readValue(requestOBJ, SyncUploadDataDigester.class);
		/*
		 * SyncUploadDataDigester syncUploadDataDigester =
		 * InputMapper.gson().fromJson(requestOBJ, SyncUploadDataDigester.class);
		 */
		String syncTableName = syncUploadDataDigester.getTableName();
		if (syncUploadDataDigester != null && syncTableName != null
				&& syncTableName.equalsIgnoreCase("m_beneficiaryregidmapping")) {
			String s = update_M_BeneficiaryRegIdMapping_for_provisioned_benID(syncUploadDataDigester);
			return s;
		} else {

			List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();

			Object[] objArr;

			// sync data 'list of object array'
			List<Object[]> syncDataListInsert = new ArrayList<>();
			List<Object[]> syncDataListUpdate = new ArrayList<>();

			int pointer;
			String vanSerialNo;
			String vanID;
			int recordCheck;
			int syncFacilityID = 0;

			for (Map<String, Object> map : dataToBesync) {
				pointer = 0;
				recordCheck = 0;
				vanSerialNo = "";
				vanID = "";

				vanSerialNo = String.valueOf(map.get(syncUploadDataDigester.getVanAutoIncColumnName()));
				vanID = String.valueOf(map.get("VanID"));

				map.replace("SyncedBy", syncUploadDataDigester.getSyncedBy());

				map.replace("date_format(SyncedDate,'%Y-%m-%d %H:%i:%s')", String.valueOf(LocalDateTime.now()));
			
				if (syncUploadDataDigester.getFacilityID() != null) {
					Double changeDoubleToIntegerID = 0.0;
					switch (syncTableName) {
						case "t_indent": {
							if (map.containsKey("FromFacilityID") && map.get("FromFacilityID") != null) {
								changeDoubleToIntegerID = (Double) map.get("FromFacilityID");
								if (changeDoubleToIntegerID.intValue() == syncUploadDataDigester.getFacilityID())
									map.replace("Processed", "P");
							}

						}
						case "t_indentorder": {
							if (map.containsKey("FromFacilityID") && map.get("FromFacilityID") != null)
								changeDoubleToIntegerID = (Double) map.get("FromFacilityID");
							if (changeDoubleToIntegerID.intValue() == syncUploadDataDigester.getFacilityID())
								map.replace("Processed", "P");
						}
						case "t_indentissue": {
							if (map.containsKey("ToFacilityID") && map.get("ToFacilityID") != null) {
								changeDoubleToIntegerID = (Double) map.get("ToFacilityID");
								if (changeDoubleToIntegerID.intValue() == syncUploadDataDigester.getFacilityID())
									map.replace("Processed", "P");
							}

						}
						// here a change in rule, will compare with toFacilityID
						case "t_stocktransfer": {
							if (map.containsKey("TransferToFacilityID") && map.get("TransferToFacilityID") != null) {
								changeDoubleToIntegerID = (Double) map.get("TransferToFacilityID");
								if (changeDoubleToIntegerID.intValue() == syncUploadDataDigester.getFacilityID())
									map.replace("Processed", "P");
							}

						}
						case "t_itemstockentry": {

							if (map.containsKey("FacilityID") && map.get("FacilityID") != null) {
								changeDoubleToIntegerID = (Double) map.get("FacilityID");
								if (changeDoubleToIntegerID.intValue() == syncUploadDataDigester.getFacilityID())
									map.replace("Processed", "P");
							}

						}
						default:

					}

				}

				if (map.containsKey("SyncFacilityID")) {
					//double syncFaciltyID = (double) map.get("SyncFacilityID");
					syncFacilityID = (int) map.get("SyncFacilityID");
				}

				recordCheck = dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(
						syncUploadDataDigester.getSchemaName(), syncUploadDataDigester.getTableName(), vanSerialNo,
						vanID, syncUploadDataDigester.getVanAutoIncColumnName(), syncFacilityID);

				if (recordCheck == 0) {
					objArr = new Object[map.size()];
				} else {
					objArr = new Object[map.size() + 2];
				}

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					if (entry.getValue() != null) {
						if (String.valueOf(entry.getValue()).equalsIgnoreCase("false")
								|| String.valueOf(entry.getValue()).equalsIgnoreCase("true"))
							objArr[pointer] = entry.getValue();
						else
							objArr[pointer] = String.valueOf(entry.getValue());
					} else
						objArr[pointer] = entry.getValue();

					pointer++;
				}

				if (recordCheck == 0) {
					syncDataListInsert.add(objArr);
				} else {
				

					objArr[pointer] = String.valueOf(vanSerialNo);

					if ((syncTableName.equalsIgnoreCase("t_patientissue")
							|| syncTableName.equalsIgnoreCase("t_physicalstockentry")
							|| syncTableName.equalsIgnoreCase("t_stockadjustment")
							|| syncTableName.equalsIgnoreCase("t_saitemmapping")
							|| syncTableName.equalsIgnoreCase("t_stocktransfer")
							|| syncTableName.equalsIgnoreCase("t_patientreturn")
							|| syncTableName.equalsIgnoreCase("t_facilityconsumption")
							|| syncTableName.equalsIgnoreCase("t_indent")
							|| syncTableName.equalsIgnoreCase("t_indentorder")
							|| syncTableName.equalsIgnoreCase("t_indentissue")
							|| syncTableName.equalsIgnoreCase("t_itemstockentry")
							|| syncTableName.equalsIgnoreCase("t_itemstockexit"))
							&& map.containsKey("SyncFacilityID")) {

						objArr[pointer + 1] = String.valueOf(map.get("SyncFacilityID"));
					} else
						objArr[pointer + 1] = String.valueOf(vanID);

					syncDataListUpdate.add(objArr);
				}
				
			}

			int[] i = null;
			if (syncDataListInsert != null && syncDataListInsert.size() > 0) {
				// schema name hard coded(Insert query builder)
				String queryInsert = getQueryToInsertDataToServerDB(syncUploadDataDigester.getSchemaName(),
						syncUploadDataDigester.getTableName(),syncUploadDataDigester.getServerColumns());

				// call repository to execute the query with given data list(Insert)
				i = dataSyncRepositoryCentral.syncDataToCentralDB(
						syncUploadDataDigester.getSchemaName(),
						syncUploadDataDigester.getTableName(), syncUploadDataDigester.getServerColumns(), queryInsert,
						syncDataListInsert);
			}

			int[] j = null;
			if (syncDataListUpdate != null && syncDataListUpdate.size() > 0) {
				// schema name hard coded(Update query builder)
				String queryUpdate = getQueryToUpdateDataToServerDB(syncUploadDataDigester.getSchemaName(), syncUploadDataDigester.getServerColumns(),
						syncUploadDataDigester.getTableName());

				// call repository to execute the query with given data list(Update)
				j = dataSyncRepositoryCentral.syncDataToCentralDB(syncUploadDataDigester.getSchemaName(),
						syncUploadDataDigester.getTableName(), ServerColumnsNotRequired, queryUpdate,
						syncDataListUpdate);
			}

			// validating if data sync successfully
			if ((i != null && syncDataListInsert.size() != i.length)
					|| (j != null && syncDataListUpdate.size() != j.length))
				return null;
			else
				return "data sync passed";

		}

	}

	public String update_M_BeneficiaryRegIdMapping_for_provisioned_benID(
			SyncUploadDataDigester syncUploadDataDigester) {
		String returnOBJ = null;
		List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();

		Object[] objArr;
		// sync data 'list of object array'
		List<Object[]> syncData = new ArrayList<>();

		String query = getqueryFor_M_BeneficiaryRegIdMapping(syncUploadDataDigester.getSchemaName(),
				syncUploadDataDigester.getTableName());

		for (Map<String, Object> map : dataToBesync) {
			if (map.get("BenRegId") != null && map.get("BeneficiaryID") != null && map.get("VanID") != null) {
				objArr = new Object[4];
				objArr[0] = String.valueOf(syncUploadDataDigester.getSyncedBy());
				objArr[1] = String.valueOf(map.get("BenRegId"));
				objArr[2] = String.valueOf(map.get("BeneficiaryID"));
				objArr[3] = String.valueOf(map.get("VanID"));

				syncData.add(objArr);
			}
		}
		int[] i = null;

		if (syncData != null && syncData.size() > 0) {
			i = dataSyncRepositoryCentral.syncDataToCentralDB(syncUploadDataDigester.getSchemaName(),
					syncUploadDataDigester.getTableName(), ServerColumnsNotRequired, query, syncData);

			if (i.length == syncData.size()) {
				returnOBJ = "data sync passed";
			}
		} else {
			returnOBJ = "data sync passed";
		}

		return returnOBJ;

	}

	private String getqueryFor_M_BeneficiaryRegIdMapping(String schemaName, String tableName) {

		StringBuilder queryBuilder = new StringBuilder(" UPDATE  ");
		queryBuilder.append(schemaName+"."+tableName);
		queryBuilder.append(" SET ");
		queryBuilder.append("Provisioned = true, SyncedDate = now(), syncedBy = ?");
		queryBuilder.append(" WHERE ");
		queryBuilder.append(" BenRegId = ? ");
		queryBuilder.append(" AND ");
		queryBuilder.append(" BeneficiaryID = ? ");
		queryBuilder.append(" AND ");
		queryBuilder.append(" VanID = ? ");
		String query = queryBuilder.toString();
		return query;
	}

	public String getQueryToInsertDataToServerDB(String schemaName, String tableName, String serverColumns) {
		String[] columnsArr = null;
		if (serverColumns != null)
			columnsArr = serverColumns.split(",");

		StringBuilder preparedStatementSetter = new StringBuilder();
		/// StringBuilder updateStatement = new StringBuilder();

		if (columnsArr != null && columnsArr.length > 0) {
			int index = 0;
			for (String column : columnsArr) {
				if (index == columnsArr.length - 1) {
					preparedStatementSetter.append(" ? ");
					
				} else {
					preparedStatementSetter.append(" ?, ");
					
				}
				index++;
			}
		}
		/*
		 * String query = "INSERT INTO " + schemaName + "." + tableName + "( " +
		 * serverColumns + ") VALUES ( " + preparedStatementSetter + " ) ";
		 */

		StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
		queryBuilder.append(schemaName + "." + tableName);
		queryBuilder.append("(");
//		queryBuilder.append("?");
		queryBuilder.append(serverColumns);
		queryBuilder.append(") VALUES (");
		queryBuilder.append(preparedStatementSetter);
		queryBuilder.append(") ");
		String query = queryBuilder.toString();
		 
		return query;
	}

	public String getQueryToUpdateDataToServerDB(String schemaName, String serverColumns, String tableName) {
		String[] columnsArr = null;
		if (serverColumns != null)
			columnsArr = serverColumns.split(",");

		StringBuilder preparedStatementSetter = new StringBuilder();

		if (columnsArr != null && columnsArr.length > 0) {
			int index = 0;
			for (String column : columnsArr) {
				if (index == columnsArr.length - 1) {
					preparedStatementSetter.append(column);
					preparedStatementSetter.append("= ?");
				} else {
					preparedStatementSetter.append(column);
					preparedStatementSetter.append("= ?, ");
				}
				index++;
			}
		}

		if (tableName.equalsIgnoreCase("t_patientissue") || tableName.equalsIgnoreCase("t_physicalstockentry")
				|| tableName.equalsIgnoreCase("t_stockadjustment") || tableName.equalsIgnoreCase("t_saitemmapping")
				|| tableName.equalsIgnoreCase("t_stocktransfer") || tableName.equalsIgnoreCase("t_patientreturn")
				|| tableName.equalsIgnoreCase("t_facilityconsumption") || tableName.equalsIgnoreCase("t_indent")
				|| tableName.equalsIgnoreCase("t_indentorder") || tableName.equalsIgnoreCase("t_indentissue")
				|| tableName.equalsIgnoreCase("t_itemstockentry") || tableName.equalsIgnoreCase("t_itemstockexit")) {

			StringBuilder queryBuilder = new StringBuilder(" UPDATE  ");
			queryBuilder.append(schemaName+"."+tableName);
			queryBuilder.append(" SET ");
			queryBuilder.append(preparedStatementSetter);
			queryBuilder.append(" WHERE ");
			queryBuilder.append(" VanSerialNo =? ");
			queryBuilder.append(" AND ");
			queryBuilder.append(" SyncFacilityID = ? ");
			String query = queryBuilder.toString();
			return query;
		} else {
			StringBuilder queryBuilder = new StringBuilder(" UPDATE  ");
			queryBuilder.append(schemaName+"."+tableName);
			queryBuilder.append(" SET ");
			queryBuilder.append(preparedStatementSetter);
			queryBuilder.append(" WHERE ");
			queryBuilder.append(" VanSerialNo =? ");
			queryBuilder.append(" AND ");
			queryBuilder.append(" VanID = ? ");
			String query = queryBuilder.toString();
			return query;
		}

	}
}