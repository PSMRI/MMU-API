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

    private static final Map<Integer, List<String>> TABLE_GROUPS = new HashMap<>();
    static {
        TABLE_GROUPS.put(1,
                Arrays.asList("m_beneficiaryregidmapping", "i_beneficiaryaccount", "i_beneficiaryaddress",
                        "i_beneficiarycontacts", "i_beneficiarydetails", "i_beneficiaryfamilymapping",
                        "i_beneficiaryidentity", "i_beneficiarymapping"));

        TABLE_GROUPS.put(2,
        Arrays.asList("t_benvisitdetail", "t_phy_anthropometry", "t_phy_vitals",
        "t_benadherence", "t_anccare",
        "t_pnccare", "t_ncdscreening", "t_ncdcare", "i_ben_flow_outreach",
        "t_covid19", "t_idrsdetails",
        "t_physicalactivity"));
        // TABLE_GROUPS.put(2,
        //         Arrays.asList(
        //                 "t_physicalactivity",
        //                 "t_idrsdetails",
        //                 "t_covid19",
        //                 "i_ben_flow_outreach",
        //                 "t_ncdcare",
        //                 "t_ncdscreening",
        //                 "t_pnccare",
        //                 "t_anccare",
        //                 "t_benadherence",
        //                 "t_phy_vitals",
        //                 "t_phy_anthropometry",
        //                 "t_benvisitdetail"));

        TABLE_GROUPS.put(3,
                Arrays.asList("t_phy_generalexam", "t_phy_headtotoe", "t_sys_obstetric", "t_sys_gastrointestinal",
                        "t_sys_cardiovascular", "t_sys_respiratory", "t_sys_centralnervous",
                        "t_sys_musculoskeletalsystem", "t_sys_genitourinarysystem"));

        TABLE_GROUPS.put(4,
                Arrays.asList("t_ancdiagnosis", "t_ncddiagnosis", "t_pncdiagnosis", "t_benchefcomplaint",
                        "t_benclinicalobservation", "t_prescription", "t_prescribeddrug", "t_lab_testorder",
                        "t_benreferdetails"));

        TABLE_GROUPS.put(5, Arrays.asList("t_lab_testresult", "t_physicalstockentry", "t_patientissue",
                "t_facilityconsumption", "t_itemstockentry", "t_itemstockexit"));

        TABLE_GROUPS.put(6, Arrays.asList("t_benmedhistory", "t_femaleobstetrichistory", "t_benmenstrualdetails",
                "t_benpersonalhabit", "t_childvaccinedetail1", "t_childvaccinedetail2", "t_childoptionalvaccinedetail",
                "t_ancwomenvaccinedetail", "t_childfeedinghistory", "t_benallergyhistory", "t_bencomorbiditycondition",
                "t_benmedicationhistory", "t_benfamilyhistory", "t_perinatalhistory", "t_developmenthistory"));

        TABLE_GROUPS.put(7,
                Arrays.asList("t_cancerfamilyhistory", "t_cancerpersonalhistory", "t_cancerdiethistory",
                        "t_cancerobstetrichistory", "t_cancervitals", "t_cancersignandsymptoms", "t_cancerlymphnode",
                        "t_canceroralexamination", "t_cancerbreastexamination", "t_cancerabdominalexamination",
                        "t_cancergynecologicalexamination", "t_cancerdiagnosis", "t_cancerimageannotation"));

        TABLE_GROUPS.put(8, Arrays.asList("i_beneficiaryimage"));

        TABLE_GROUPS.put(9,
                Arrays.asList("t_itemstockentry", "t_itemstockexit", "t_patientissue", "t_physicalstockentry",
                        "t_stockadjustment", "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                        "t_indentissue", "t_indentorder", "t_saitemmapping"));

    }

    public String syncDataToServer(String requestOBJ, String Authorization) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        SyncUploadDataDigester syncUploadDataDigester = mapper.readValue(requestOBJ, SyncUploadDataDigester.class);

        if (syncUploadDataDigester == null || syncUploadDataDigester.getTableName() == null) {
            logger.error("Invalid SyncUploadDataDigester object or tableName is null.");
            return "Error: Invalid sync request.";
        }

        String syncTableName = syncUploadDataDigester.getTableName();

        // Handle specific tables first, if their logic is distinct
        if ("m_beneficiaryregidmapping".equalsIgnoreCase(syncTableName)) {
            String result = update_M_BeneficiaryRegIdMapping_for_provisioned_benID(syncUploadDataDigester);
            if ("data sync passed".equals(result)) {
                return "Sync successful for m_beneficiaryregidmapping.";
            } else {
                logger.error("Sync failed for m_beneficiaryregidmapping: {}", result);
                return "Sync failed for m_beneficiaryregidmapping.";
            }
        } else if ("i_beneficiarydetails".equalsIgnoreCase(syncTableName)) {
            String result = update_I_BeneficiaryDetails_for_processed_in_batches(syncUploadDataDigester);
            if ("data sync passed".equals(result)) {
                return "Sync successful for i_beneficiarydetails.";
            } else {
                logger.error("Sync failed for i_beneficiarydetails: {}", result);
                return "Sync failed for i_beneficiarydetails.";
            }
        } else {
            // Determine the group for the current table or iterate through all if no
            // specific table is given
            boolean syncSuccess = true;
            String errorMessage = "";

            // If a specific table is provided in the request, try to find its group and
            // sync only that table.
            // Otherwise, iterate through all defined groups.
            if (syncTableName != null && !syncTableName.isEmpty()) {
                boolean foundInGroup = false;
                for (Map.Entry<Integer, List<String>> entry : TABLE_GROUPS.entrySet()) {
                    if (entry.getValue().contains(syncTableName.toLowerCase())) {
                        logger.info("Attempting to sync table '{}' from Group {}", syncTableName, entry.getKey());
                        syncSuccess = syncTablesInGroup(syncUploadDataDigester.getSchemaName(), syncTableName,
                                syncUploadDataDigester);
                        foundInGroup = true;
                        break;
                    }
                }
                if (!foundInGroup) {
                    logger.warn("Table '{}' not found in any predefined groups. Proceeding with generic sync logic.",
                            syncTableName);
                    syncSuccess = performGenericTableSync(syncUploadDataDigester);
                }
            } else {
                // If no specific table is in the request (e.g., a general sync trigger),
                // iterate through groups
                logger.info("No specific table provided. Attempting to sync all tables group by group.");
                for (Map.Entry<Integer, List<String>> entry : TABLE_GROUPS.entrySet()) {
                    Integer groupId = entry.getKey();
                    List<String> tablesInGroup = entry.getValue();
                    logger.info("Starting sync for Group {}", groupId);
                    for (String table : tablesInGroup) {
                        try {
                            // Create a new digester for each table within the group,
                            // or adapt if the original digester contains data for multiple tables.
                            // For simplicity, assuming syncDataDigester needs to be tailored per table or
                            // group.
                            // If your requestOBJ contains data for only one table at a time, this loop
                            // might need adjustment
                            // to fetch data for each table in the group.
                            // For now, it will use the syncData from the original requestOBJ, which implies
                            // the original requestOBJ should represent data for a single table.
                            // A more robust solution would involve fetching data for each table
                            // dynamically.
                            boolean currentTableSyncResult = syncTablesInGroup(syncUploadDataDigester.getSchemaName(),
                                    table, syncUploadDataDigester);
                            if (!currentTableSyncResult) {
                                syncSuccess = false;
                                errorMessage += "Failed to sync table: " + table + " in Group " + groupId + ". ";
                                logger.error("Sync failed for table '{}' in Group {}. Error: {}", table, groupId,
                                        errorMessage);
                                // Optionally, you can choose to break here or continue to sync other tables in
                                // the group/next group
                                // For now, let's continue to attempt other tables within the group.
                            } else {
                                logger.info("Successfully synced table: {} in Group {}", table, groupId);
                            }
                        } catch (Exception e) {
                            syncSuccess = false;
                            errorMessage += "Exception during sync for table: " + table + " in Group " + groupId + ": "
                                    + e.getMessage() + ". ";
                            logger.error("Exception during sync for table '{}' in Group {}: {}", table, groupId,
                                    e.getMessage(), e);
                            // Continue to attempt other tables
                        }
                    }
                }
            }

            if (syncSuccess) {
                return "Overall data sync passed.";
            } else {
                return "Overall data sync failed. Details: " + errorMessage;
            }
        }
    }

    /**
     * Helper method to sync tables belonging to a specific group.
     * This method assumes that the `syncUploadDataDigester` will be populated
     * with relevant data for the `currentTableName` before calling this.
     * In a real-world scenario, you might fetch data for each table here.
     */
    private boolean syncTablesInGroup(String schemaName, String currentTableName,
            SyncUploadDataDigester originalDigester) {
        logger.info("Attempting generic sync for table: {}", currentTableName);
        // This is a simplification. In a production system, you would likely need
        // to retrieve the actual data for 'currentTableName' from the local DB
        // based on the group sync approach. For this example, we'll assume the
        // originalDigester's syncData is relevant or needs to be re-populated.

        // Create a new digester instance or modify the existing one for the current
        // table
        SyncUploadDataDigester tableSpecificDigester = new SyncUploadDataDigester();
        tableSpecificDigester.setSchemaName(schemaName);
        tableSpecificDigester.setTableName(currentTableName);
        tableSpecificDigester.setSyncedBy(originalDigester.getSyncedBy());
        tableSpecificDigester.setFacilityID(originalDigester.getFacilityID());
        tableSpecificDigester.setVanAutoIncColumnName(originalDigester.getVanAutoIncColumnName());
        tableSpecificDigester.setServerColumns(originalDigester.getServerColumns()); // Assuming serverColumns is
                                                                                     // generic or set per table

        // !!! IMPORTANT: You'll need to fetch the data for 'currentTableName' from your local DB here.
        // The `originalDigester.getSyncData()` might not be correct for all tables in a group.
        // For demonstration, I'm just using the original digester's data, which is likely incorrect
        tableSpecificDigester.setSyncData(originalDigester.getSyncData());
        logger.info("vanitha: sync tables in group" + tableSpecificDigester.getSyncData());
        return performGenericTableSync(tableSpecificDigester);
    }

    private String update_M_BeneficiaryRegIdMapping_for_provisioned_benID(
            SyncUploadDataDigester syncUploadDataDigester) {
        logger.info("Processing update_M_BeneficiaryRegIdMapping_for_provisioned_benID for table: {}",
                syncUploadDataDigester.getTableName());
        List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
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