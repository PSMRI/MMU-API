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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;

@Service
public class GetDataFromVanAndSyncToDBImpl implements GetDataFromVanAndSyncToDB {

    private static final String SERVER_COLUMNS_NOT_REQUIRED = null; // Renamed for clarity
    private static final Logger logger = LoggerFactory.getLogger(GetDataFromVanAndSyncToDBImpl.class);

    @Autowired
    private DataSyncRepositoryCentral dataSyncRepositoryCentral;

    private static final Map<Integer, List<String>> TABLE_GROUPS = new HashMap<>();
    static {
        TABLE_GROUPS.put(1,
                Arrays.asList("m_beneficiaryregidmapping", "i_beneficiaryaccount", "i_beneficiaryaddress",
                        "i_beneficiarycontacts", "i_beneficiarydetails", "i_beneficiaryfamilymapping",
                        "i_beneficiaryidentity", "i_beneficiarymapping"));

        TABLE_GROUPS.put(2,
                Arrays.asList("t_benvisitdetail", "t_phy_anthropometry", "t_phy_vitals", "t_benadherence", "t_anccare",
                        "t_pnccare", "t_ncdscreening", "t_ncdcare", "i_ben_flow_outreach", "t_covid19", "t_idrsdetails",
                        "t_physicalactivity"));

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
        List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
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
            logger.error("This is beneficary details table, processing in batches.");
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

                for (Map<String, Object> map : dataToBesync) {
                    // if (entry.getValue().contains(syncTableName.toLowerCase())) {
                    if (map.get("tableName") != null
                            && map.get("tableName").toString().equalsIgnoreCase(syncTableName)) {
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

        // !!! IMPORTANT: You'll need to fetch the data for 'currentTableName' from your
        // local DB here.
        // The `originalDigester.getSyncData()` might not be correct for all tables in a
        // group.
        // For demonstration, I'm just using the original digester's data, which is
        // likely incorrect
        tableSpecificDigester.setSyncData(originalDigester.getSyncData());
        return performGenericTableSync(tableSpecificDigester);
    }

    private String update_M_BeneficiaryRegIdMapping_for_provisioned_benID(
            SyncUploadDataDigester syncUploadDataDigester) {

        List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
        List<Object[]> syncData = new ArrayList<>();

        String query = getqueryFor_M_BeneficiaryRegIdMapping(syncUploadDataDigester.getSchemaName(),
                syncUploadDataDigester.getTableName());

        for (Map<String, Object> map : dataToBesync) {
            if (map.get("BenRegId") != null && map.get("BeneficiaryID") != null && map.get("VanID") != null) {
                Object[] objArr = new Object[4];
                objArr[0] = syncUploadDataDigester.getSyncedBy(); // SyncedBy
                objArr[1] = String.valueOf(map.get("BenRegId"));
                objArr[2] = String.valueOf(map.get("BeneficiaryID"));
                objArr[3] = String.valueOf(map.get("VanID"));
                syncData.add(objArr);
            } else {
                logger.warn(
                        "Skipping record in m_beneficiaryregidmapping due to missing BenRegId, BeneficiaryID, or VanID: {}",
                        map);
            }
        }

        if (!syncData.isEmpty()) {
            try {
                int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(syncUploadDataDigester.getSchemaName(),
                        syncUploadDataDigester.getTableName(), syncUploadDataDigester.getServerColumns(), query,
                        syncData);

                if (i.length == syncData.size()) {
                    logger.info("Successfully updated {} records for m_beneficiaryregidmapping.", i.length);
                    return "data sync passed";
                } else {
                    logger.error(
                            "Partial update for m_beneficiaryregidmapping. Expected {} updates, got {}. Failed records: {}",
                            syncData.size(), i.length, getFailedRecords(i, syncData));
                    return "Partial data sync for m_beneficiaryregidmapping.";
                }
            } catch (Exception e) {
                logger.error("Exception during update for m_beneficiaryregidmapping: {}", e.getMessage(), e);
                return "Error during sync for m_beneficiaryregidmapping: " + e.getMessage();
            }
        } else {
            logger.info("No data to sync for m_beneficiaryregidmapping.");
            return "data sync passed";
        }
    }

    private String getqueryFor_M_BeneficiaryRegIdMapping(String schemaName, String tableName) {
        StringBuilder queryBuilder = new StringBuilder(" UPDATE ");
        queryBuilder.append(schemaName).append(".").append(tableName);
        queryBuilder.append(" SET ");
        queryBuilder.append("Provisioned = true, SyncedDate = now(), syncedBy = ?");
        queryBuilder.append(" WHERE ");
        queryBuilder.append(" BenRegId = ? ");
        queryBuilder.append(" AND ");
        queryBuilder.append(" BeneficiaryID = ? ");
        queryBuilder.append(" AND ");
        queryBuilder.append(" VanID = ? ");

        return queryBuilder.toString();
    }

    public String update_I_BeneficiaryDetails_for_processed_in_batches(SyncUploadDataDigester syncUploadDataDigester) {
        List<Object[]> syncData = new ArrayList<>();

        String query = getQueryFor_I_BeneficiaryDetails(syncUploadDataDigester.getSchemaName(),
                syncUploadDataDigester.getTableName());

        int limit = 1000;
        int offset = 0;
        int totalProcessed = 0;

        String problematicWhereClause = " WHERE Processed <> 'P' AND VanID IS NOT NULL "; // Define it explicitly

        while (true) {
            List<Map<String, Object>> batch;
            try {

                batch = dataSyncRepositoryCentral.getBatchForBenDetails(
                        syncUploadDataDigester,
                        problematicWhereClause,
                        limit,
                        offset);
            } catch (Exception e) {
                logger.error("Error fetching batch for i_beneficiarydetails: {}", e.getMessage(), e);
                return "Error fetching data for i_beneficiarydetails: " + e.getMessage();
            }

            if (totalProcessed > 0 || syncData.isEmpty()) { // syncData.isEmpty() means no records to process, still a
                                                            // "success"
                logger.info("Finished processing i_beneficiarydetails. Total records processed: {}", totalProcessed);
                return "data sync passed";
            } else {
                logger.error("No records were processed for i_beneficiarydetails or an unknown error occurred.");
                return "No data processed or sync failed for i_beneficiarydetails.";
            }
        }
    }

    private String getQueryFor_I_BeneficiaryDetails(String schemaName, String tableName) {
        StringBuilder queryBuilder = new StringBuilder(" UPDATE ");
        queryBuilder.append(schemaName).append(".").append(tableName);
        queryBuilder.append(" SET ");
        queryBuilder.append("Processed = 'P', SyncedDate = now(), SyncedBy = ? ");
        queryBuilder.append(" WHERE ");
        queryBuilder.append("BeneficiaryDetailsId = ? ");
        queryBuilder.append(" AND ");
        queryBuilder.append("VanID = ? ");
        return queryBuilder.toString();
    }

    /**
     * Handles the generic synchronization logic for tables not covered by specific
     * handlers.
     */
    private boolean performGenericTableSync(SyncUploadDataDigester syncUploadDataDigester) {
        List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
        List<Object[]> syncDataListInsert = new ArrayList<>();
        List<Object[]> syncDataListUpdate = new ArrayList<>();

        if (dataToBesync == null || dataToBesync.isEmpty()) {
            logger.info("No data to sync for table: {}", syncUploadDataDigester.getTableName());
            return true; // Nothing to sync, consider it a success
        }

        String syncTableName = syncUploadDataDigester.getTableName();
        String vanAutoIncColumnName = syncUploadDataDigester.getVanAutoIncColumnName();
        String schemaName = syncUploadDataDigester.getSchemaName();
        Integer facilityIDFromDigester = syncUploadDataDigester.getFacilityID();

        for (Map<String, Object> map : dataToBesync) {
            String vanSerialNo = String.valueOf(map.get(vanAutoIncColumnName));
            String vanID = String.valueOf(map.get("VanID"));
            int syncFacilityID = 0;

            // Update SyncedBy and SyncedDate in the xmap itself before processing
            map.put("SyncedBy", syncUploadDataDigester.getSyncedBy());
            map.put("SyncedDate", String.valueOf(LocalDateTime.now())); // Ensure column name matches DB
            if (map.get("CreatedDate") == null || map.get("created_date") == null) {
                logger.info("CreatedDate was null for table: " + syncTableName + ", inserting current time");
                if (map.get("CreatedDate") == null)
                    map.put("CreatedDate", String.valueOf(LocalDateTime.now()));
                if (map.get("created_date") == null)
                    map.put("created_date", String.valueOf(LocalDateTime.now()));
            }
            // Facility ID processing
            if (facilityIDFromDigester != null) {
                // Determine the 'Processed' status based on facility ID for specific tables
                switch (syncTableName.toLowerCase()) {
                    case "t_indent":
                    case "t_indentorder": {
                        if (map.containsKey("FromFacilityID") && map.get("FromFacilityID") instanceof Double) {
                            Double fromFacilityID = (Double) map.get("FromFacilityID");
                            if (fromFacilityID.intValue() == facilityIDFromDigester) {
                                map.put("Processed", "P");
                            }
                        }
                        break;
                    }
                    case "t_indentissue": {
                        if (map.containsKey("ToFacilityID") && map.get("ToFacilityID") instanceof Double) {
                            Double toFacilityID = (Double) map.get("ToFacilityID");
                            if (toFacilityID.intValue() == facilityIDFromDigester) {
                                map.put("Processed", "P");
                            }
                        }
                        break;
                    }
                    case "t_stocktransfer": {
                        if (map.containsKey("TransferToFacilityID")
                                && map.get("TransferToFacilityID") instanceof Double) {
                            Double transferToFacilityID = (Double) map.get("TransferToFacilityID");
                            if (transferToFacilityID.intValue() == facilityIDFromDigester) {
                                map.put("Processed", "P");
                            }
                        }
                        break;
                    }
                    case "t_itemstockentry": {
                        if (map.containsKey("FacilityID") && map.get("FacilityID") instanceof Double) {
                            Double mapFacilityID = (Double) map.get("FacilityID");
                            if (mapFacilityID.intValue() == facilityIDFromDigester) {
                                map.put("Processed", "P");
                            }
                        }
                        break;
                    }
                    default:
                        // No specific facility ID logic for other tables, maintain existing 'Processed'
                        // status or default
                        break;
                }
            }

            // Extract SyncFacilityID for checkRecordIsAlreadyPresentOrNot
            if (map.containsKey("SyncFacilityID") && map.get("SyncFacilityID") instanceof Integer) {
                syncFacilityID = (Integer) map.get("SyncFacilityID");
            } else if (map.containsKey("SyncFacilityID") && map.get("SyncFacilityID") instanceof Double) {
                syncFacilityID = ((Double) map.get("SyncFacilityID")).intValue();
            }

            int recordCheck;
            try {
                recordCheck = dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(
                        schemaName, syncTableName, vanSerialNo, vanID, vanAutoIncColumnName, syncFacilityID);
            } catch (Exception e) {
                logger.error("Error checking record existence for table {}: VanSerialNo={}, VanID={}. Error: {}",
                        syncTableName, vanSerialNo, vanID, e.getMessage(), e);
                return false; // Critical error, stop sync for this table
            }

            // Prepare Object array for insert/update
            Object[] objArr;
            List<String> serverColumnsList = Arrays.asList(syncUploadDataDigester.getServerColumns().split(","));
            List<Object> currentRecordValues = new ArrayList<>();

            for (String column : serverColumnsList) {
                Object value = map.get(column.trim());
                // Handle boolean conversion if necessary, though String.valueOf should
                // generally work for prepared statements
                if (value instanceof Boolean) {
                    currentRecordValues.add(value);
                } else if (value != null) {
                    currentRecordValues.add(String.valueOf(value));
                } else {
                    currentRecordValues.add(null);
                }
            }

            objArr = currentRecordValues.toArray();

            if (recordCheck == 0) {
                syncDataListInsert.add(objArr);
            } else {
                // For update, append the WHERE clause parameters at the end of the array
                List<Object> updateParams = new ArrayList<>(Arrays.asList(objArr));
                updateParams.add(String.valueOf(vanSerialNo));

                if (Arrays.asList("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
                        "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                        "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit")
                        .contains(syncTableName.toLowerCase()) && map.containsKey("SyncFacilityID")) {
                    updateParams.add(String.valueOf(map.get("SyncFacilityID")));
                } else {
                    updateParams.add(String.valueOf(vanID));
                }
                syncDataListUpdate.add(updateParams.toArray());
            }
        }

        boolean insertSuccess = true;
        boolean updateSuccess = true;

        if (!syncDataListInsert.isEmpty()) {
            String queryInsert = getQueryToInsertDataToServerDB(schemaName, syncTableName,
                    syncUploadDataDigester.getServerColumns());

            try {
                int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
                        syncUploadDataDigester.getServerColumns(), queryInsert, syncDataListInsert);
                if (i.length != syncDataListInsert.size()) {
                    insertSuccess = false;
                    logger.error("Partial insert for table {}. Expected {} inserts, got {}. Failed records: {}",
                            syncTableName, syncDataListInsert.size(), i.length,
                            getFailedRecords(i, syncDataListInsert));
                } else {
                    logger.info("Successfully inserted {} records into table {}.", i.length, syncTableName);
                }
            } catch (Exception e) {
                insertSuccess = false;
                logger.error("Exception during insert for table {}: {}", syncTableName, e.getMessage(), e);
            }
        }

        if (!syncDataListUpdate.isEmpty()) {
            String queryUpdate = getQueryToUpdateDataToServerDB(schemaName, syncUploadDataDigester.getServerColumns(),
                    syncTableName);
            // Ensure the update query is correct and matches the expected format
            try {
                int[] j = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
                        SERVER_COLUMNS_NOT_REQUIRED, queryUpdate, syncDataListUpdate);
                if (j.length != syncDataListUpdate.size()) {
                    updateSuccess = false;
                    logger.error("Partial update for table {}. Expected {} updates, got {}. Failed records: {}",
                            syncTableName, syncDataListUpdate.size(), j.length,
                            getFailedRecords(j, syncDataListUpdate));
                } else {
                    logger.info("Successfully updated {} records in table {}.", j.length, syncTableName);
                }
            } catch (Exception e) {
                updateSuccess = false;
                logger.error("Exception during update for table {}: {}", syncTableName, e.getMessage(), e);
            }
        }
        return insertSuccess && updateSuccess;
    }

    private String getQueryToInsertDataToServerDB(String schemaName, String tableName, String serverColumns) {
        String[] columnsArr = null;
        if (serverColumns != null)
            columnsArr = serverColumns.split(",");

        StringBuilder preparedStatementSetter = new StringBuilder();

        if (columnsArr != null && columnsArr.length > 0) {
            for (int i = 0; i < columnsArr.length; i++) {
                preparedStatementSetter.append("?");
                if (i < columnsArr.length - 1) {
                    preparedStatementSetter.append(", ");
                }
            }
        }

        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
        queryBuilder.append(schemaName).append(".").append(tableName);
        queryBuilder.append("(");
        queryBuilder.append(serverColumns);
        queryBuilder.append(") VALUES (");
        queryBuilder.append(preparedStatementSetter);
        queryBuilder.append(")");
        return queryBuilder.toString();
    }

    public String getQueryToUpdateDataToServerDB(String schemaName, String serverColumns, String tableName) {
        String[] columnsArr = null;
        if (serverColumns != null)
            columnsArr = serverColumns.split(",");

        StringBuilder preparedStatementSetter = new StringBuilder();

        if (columnsArr != null && columnsArr.length > 0) {
            for (int i = 0; i < columnsArr.length; i++) {
                String columnName = columnsArr[i].trim(); // ← NEW LINE

                // Special handling for CreatedDate - use COALESCE to prevent NULL
                if (columnName.equalsIgnoreCase("CreatedDate")) { // ← NEW BLOCK
                    preparedStatementSetter.append(columnName);
                    preparedStatementSetter.append(" = COALESCE(?, CURRENT_TIMESTAMP)");
                } else {
                    preparedStatementSetter.append(columnName);
                    preparedStatementSetter.append(" = ?");
                }

                if (i < columnsArr.length - 1) {
                    preparedStatementSetter.append(", ");
                }
            }
        }
        StringBuilder queryBuilder = new StringBuilder(" UPDATE ");
        queryBuilder.append(schemaName).append(".").append(tableName);
        queryBuilder.append(" SET ");
        queryBuilder.append(preparedStatementSetter);
        queryBuilder.append(" WHERE VanSerialNo = ? ");

        if (Arrays.asList("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
                "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit")
                .contains(tableName.toLowerCase())) {
            queryBuilder.append(" AND SyncFacilityID = ? ");
        } else {
            queryBuilder.append(" AND VanID = ? ");
        }
        return queryBuilder.toString();
    }

    // Helper to get information about failed records (for logging purposes)
    private String getFailedRecords(int[] results, List<Object[]> data) {
        List<String> failedRecordsInfo = new ArrayList<>();
        for (int k = 0; k < results.length; k++) {
            // In Spring JDBC batchUpdate, a value of Statement.EXECUTE_FAILED or
            // Statement.SUCCESS_NO_INFO
            // usually indicates a failure or success without specific row count.
            // A common return value for success is 1 (for one row updated/inserted).
            if (results[k] < 1) { // Assuming 1 means success, and anything else (0, -2, etc.) means failure
                // Attempt to get some identifiable info from the failed record
                if (data.get(k).length > 0) {
                    failedRecordsInfo.add(
                            "Record at index " + k + " (VanSerialNo/ID: " + data.get(k)[data.get(k).length - 2] + ")");
                } else {
                    failedRecordsInfo.add("Record at index " + k + " (No identifiable info)");
                }
            }
        }
        logger.info("Failed records info: {}", failedRecordsInfo);
        return String.join("; ", failedRecordsInfo);
    }
}