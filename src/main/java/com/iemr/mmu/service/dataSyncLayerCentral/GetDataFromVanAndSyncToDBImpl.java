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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.service.dataSyncActivity.SyncResult;

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
        List<SyncResult> syncResults = new ArrayList<>(); // <-- define here

        if (syncUploadDataDigester == null || syncUploadDataDigester.getTableName() == null) {
            logger.error("Invalid SyncUploadDataDigester object or tableName is null.");
            return "Error: Invalid sync request.";
        }

        String syncTableName = syncUploadDataDigester.getTableName();
        // Handle specific tables first, if their logic is distinct
        if ("m_beneficiaryregidmapping".equalsIgnoreCase(syncTableName)) {
            String result = update_M_BeneficiaryRegIdMapping_for_provisioned_benID(syncUploadDataDigester, syncResults);
            if ("data sync passed".equals(result)) {
                return "Sync successful for m_beneficiaryregidmapping.";
            } else {
                logger.error("Sync failed for m_beneficiaryregidmapping: {}", result);
                return "Sync failed for m_beneficiaryregidmapping.";
            }
        } else {
            boolean syncSuccess = true;
            String errorMessage = "";
            if (syncTableName != null && !syncTableName.isEmpty()) {
                boolean foundInGroup = false;

                for (Map<String, Object> map : dataToBesync) {
                    if (map.get("tableName") != null
                            && map.get("tableName").toString().equalsIgnoreCase(syncTableName)) {
                        syncSuccess = syncTablesInGroup(syncUploadDataDigester.getSchemaName(), syncTableName,
                                syncUploadDataDigester, syncResults);
                        foundInGroup = true;
                        break;
                    }
                }
                if (!foundInGroup) {
                    logger.warn("Table '{}' not found in any predefined groups. Proceeding with generic sync logic.",
                            syncTableName);
                    syncSuccess = performGenericTableSync(syncUploadDataDigester, syncResults);
                }
            } else {

                for (Map.Entry<Integer, List<String>> entry : TABLE_GROUPS.entrySet()) {
                    Integer groupId = entry.getKey();
                    List<String> tablesInGroup = entry.getValue();
                    for (String table : tablesInGroup) {
                        try {

                            boolean currentTableSyncResult = syncTablesInGroup(syncUploadDataDigester.getSchemaName(),
                                    table, syncUploadDataDigester, syncResults);
                            if (!currentTableSyncResult) {
                                syncSuccess = false;
                                errorMessage += "Failed to sync table: " + table + " in Group " + groupId + ". ";
                                logger.error("Sync failed for table '{}' in Group {}. Error: {}", table, groupId,
                                        errorMessage);

                            } else {
                                logger.info("Successfully synced table: {} in Group {}", table, groupId);
                            }
                        } catch (Exception e) {
                            syncSuccess = false;
                            errorMessage += "Exception during sync for table: " + table + " in Group " + groupId + ": "
                                    + e.getMessage() + ". ";
                            logger.error("Exception during sync for table '{}' in Group {}: {}", table, groupId,
                                    e.getMessage(), e);
                        }
                    }
                }
            }

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("statusCode", 200);
            responseMap.put("message", "Data sync completed");
            responseMap.put("records", syncResults);
            logger.info("Response = " + responseMap);
            logger.info("Sync Results = " + syncResults);
            return new ObjectMapper().writeValueAsString(responseMap);

        }
    }

    private boolean syncTablesInGroup(String schemaName, String currentTableName,
            SyncUploadDataDigester originalDigester, List<SyncResult> syncResults) {

        // Filter syncData for this specific table
        List<Map<String, Object>> filteredData = new ArrayList<>();
        for (Map<String, Object> map : originalDigester.getSyncData()) {
            if (map.get("tableName") != null &&
                    map.get("tableName").toString().equalsIgnoreCase(currentTableName)) {
                filteredData.add(map);
            }
        }

        logger.info("Filtered {} records for table {}", filteredData.size(), currentTableName);

        if (filteredData.isEmpty()) {
            logger.info("No data found for table: {}", currentTableName);
            return true; // No data to sync is considered success
        }

        SyncUploadDataDigester tableSpecificDigester = new SyncUploadDataDigester();
        tableSpecificDigester.setSchemaName(schemaName);
        tableSpecificDigester.setTableName(currentTableName);
        tableSpecificDigester.setSyncedBy(originalDigester.getSyncedBy());
        tableSpecificDigester.setFacilityID(originalDigester.getFacilityID());
        tableSpecificDigester.setVanAutoIncColumnName(originalDigester.getVanAutoIncColumnName());
        tableSpecificDigester.setServerColumns(originalDigester.getServerColumns());
        tableSpecificDigester.setSyncData(filteredData); // Use filtered data

        return performGenericTableSync(tableSpecificDigester, syncResults);
    }

    private String update_M_BeneficiaryRegIdMapping_for_provisioned_benID(
            SyncUploadDataDigester syncUploadDataDigester, List<SyncResult> syncResults) {

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

private boolean performGenericTableSync(SyncUploadDataDigester syncUploadDataDigester,
        List<SyncResult> syncResults) {
    List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
    List<Object[]> syncDataListInsert = new ArrayList<>();
    List<Object[]> syncDataListUpdate = new ArrayList<>();

    Map<Integer, Integer> insertIndexMap = new HashMap<>();
    Map<Integer, Integer> updateIndexMap = new HashMap<>();

    boolean overallSuccess = true;

    if (dataToBesync == null || dataToBesync.isEmpty()) {
        logger.info("No data to sync for table: {}", syncUploadDataDigester.getTableName());
        return true;
    }

    String syncTableName = syncUploadDataDigester.getTableName();
    String vanAutoIncColumnName = syncUploadDataDigester.getVanAutoIncColumnName();
    String schemaName = syncUploadDataDigester.getSchemaName();
    Integer facilityIDFromDigester = syncUploadDataDigester.getFacilityID();
    String serverColumns = syncUploadDataDigester.getServerColumns();

    int vanSerialIndex = Arrays.asList(serverColumns.split(",")).indexOf(vanAutoIncColumnName);
    List<String> serverColumnsList = Arrays.asList(serverColumns.split(","));

    for (Map<String, Object> map : dataToBesync) {
        Map<String, Object> cleanRecord = new HashMap<>();
        
        for (String key : map.keySet()) {
            String cleanKey = key;
            Object value = map.get(key);
            
            // Handle date_format fields
            if (key.startsWith("date_format(") && key.endsWith(")")) {
                int start = key.indexOf("(") + 1;
                int end = key.indexOf(",");
                if (end > start) {
                    cleanKey = key.substring(start, end).trim();
                } else {
                    cleanKey = key.substring(start, key.indexOf(")")).trim();
                }
            }
            
            // CRITICAL FIX: Keep null as null, don't convert to string
            cleanRecord.put(cleanKey.trim(), value);
        }
logger.info("Clean REcord="+cleanRecord);

        String vanSerialNo = String.valueOf(cleanRecord.get(vanAutoIncColumnName));
        String vanID = String.valueOf(cleanRecord.get("VanID"));
        int syncFacilityID = 0;

        cleanRecord.put("SyncedBy", syncUploadDataDigester.getSyncedBy());
        cleanRecord.put("SyncedDate", String.valueOf(LocalDateTime.now()));

        if (facilityIDFromDigester != null) {
            switch (syncTableName.toLowerCase()) {
                case "t_indent":
                case "t_indentorder": {
                    if (cleanRecord.containsKey("FromFacilityID")
                            && cleanRecord.get("FromFacilityID") instanceof Number) {
                        Number fromFacilityID = (Number) cleanRecord.get("FromFacilityID");
                        if (fromFacilityID.intValue() == facilityIDFromDigester) {
                            cleanRecord.put("Processed", "P");
                        }
                    }
                    break;
                }
                case "t_indentissue": {
                    if (cleanRecord.containsKey("ToFacilityID")
                            && cleanRecord.get("ToFacilityID") instanceof Number) {
                        Number toFacilityID = (Number) cleanRecord.get("ToFacilityID");
                        if (toFacilityID.intValue() == facilityIDFromDigester) {
                            cleanRecord.put("Processed", "P");
                        }
                    }
                    break;
                }
                case "t_stocktransfer": {
                    if (cleanRecord.containsKey("TransferToFacilityID")
                            && cleanRecord.get("TransferToFacilityID") instanceof Number) {
                        Number transferToFacilityID = (Number) cleanRecord.get("TransferToFacilityID");
                        if (transferToFacilityID.intValue() == facilityIDFromDigester) {
                            cleanRecord.put("Processed", "P");
                        }
                    }
                    break;
                }
                case "t_itemstockentry": {
                    logger.info("case:t_itemStockEntry"+cleanRecord);
                    if (cleanRecord.containsKey("FacilityID") && cleanRecord.get("FacilityID") instanceof Number) {
                        Number mapFacilityID = (Number) cleanRecord.get("FacilityID");
                        logger.info("Map Facility ID="+mapFacilityID);
                         logger.info("Facility ID From Digester="+facilityIDFromDigester);
                        if (mapFacilityID.intValue() == facilityIDFromDigester) {
                            cleanRecord.put("Processed", "P");
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }

        if (cleanRecord.containsKey("SyncFacilityID") && cleanRecord.get("SyncFacilityID") instanceof Number) {
            logger.info("Clean Record - syncFacilityID="+cleanRecord.get("SyncFacilityID"));
            syncFacilityID = ((Number) cleanRecord.get("SyncFacilityID")).intValue();
        }

        int recordCheck;
        try {
            recordCheck = dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(
                    schemaName, syncTableName, vanSerialNo, vanID, vanAutoIncColumnName, syncFacilityID);
            logger.info("Record check result: {}", recordCheck);
        } catch (Exception e) {
            logger.error("Error checking record existence for table {}: VanSerialNo={}, VanID={}. Error: {}",
                    syncTableName, vanSerialNo, vanID, e.getMessage(), e);

            String mainErrorReason = "Record check failed: " + extractMainErrorReason(e);
            syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo,
                    syncUploadDataDigester.getSyncedBy(), false, mainErrorReason));
            overallSuccess = false;
            continue;
        }

        List<Object> currentRecordValues = new ArrayList<>();
        for (String column : serverColumnsList) {
            logger.info("Column="+column);
            Object value = cleanRecord.get(column.trim());
            // CRITICAL FIX: Don't convert null to string
            if (value == null) {
                currentRecordValues.add(null);
            } else if (value instanceof Boolean) {
                currentRecordValues.add(value);
            } else {
                currentRecordValues.add(String.valueOf(value));
            }
        }

        Object[] objArr = currentRecordValues.toArray();

        int currentSyncResultIndex = syncResults.size();
        syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo,
                syncUploadDataDigester.getSyncedBy(), false, "Pending"));

        if (recordCheck == 0) {
            insertIndexMap.put(currentSyncResultIndex, syncDataListInsert.size());
            syncDataListInsert.add(objArr);
        } else {
            List<Object> updateParams = new ArrayList<>(Arrays.asList(objArr));
            updateParams.add(String.valueOf(vanSerialNo));

            if (Arrays.asList("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
                    "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                    "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit")
                    .contains(syncTableName.toLowerCase()) && cleanRecord.containsKey("SyncFacilityID")) {
                        logger.info("Adding SyncFacilityID to update params for table {}", syncTableName);
                updateParams.add(String.valueOf(cleanRecord.get("SyncFacilityID")));
            } else {
                logger.info("Adding VanID to update params for table {}", syncTableName);
                updateParams.add(String.valueOf(vanID));
            }

            updateIndexMap.put(currentSyncResultIndex, syncDataListUpdate.size());
            logger.info("Update Params=",updateParams.toArray());
            syncDataListUpdate.add(updateParams.toArray());
        }
    }

    // FIXED: Initialize as true, set to false only on failures
    boolean insertSuccess = true;
    boolean updateSuccess = true;

    // Process INSERT operations
    if (!syncDataListInsert.isEmpty()) {
        String queryInsert = getQueryToInsertDataToServerDB(schemaName, syncTableName, serverColumns);

        try {
            int[] insertResults = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
                    serverColumns, queryInsert, syncDataListInsert);

            for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int insertListIndex = entry.getValue();

                boolean success = insertListIndex < insertResults.length && insertResults[insertListIndex] > 0;

                if (!success) {
                    String failedVanSerialNo = getVanSerialNo(syncDataListInsert.get(insertListIndex),
                            vanSerialIndex, syncResults.get(syncResultIndex));
                    String conciseReason = "Insert failed (code: " +
                            (insertListIndex < insertResults.length ? insertResults[insertListIndex] : "unknown") + ")";
                    syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, failedVanSerialNo,
                            syncUploadDataDigester.getSyncedBy(), false, conciseReason));
                    insertSuccess = false;
                    overallSuccess = false;
                } else {
                    syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName,
                            syncResults.get(syncResultIndex).getVanSerialNo(),
                            syncUploadDataDigester.getSyncedBy(), true, null));
                }
            }

        } catch (Exception e) {
            String mainErrorReason = extractMainErrorReason(e);
            logger.info("Batch insert failed for table {}: {}", syncTableName, mainErrorReason, e);
            
            // Mark ALL inserts as failed
            for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int insertListIndex = entry.getValue();
                String vanSerialNo = getVanSerialNo(syncDataListInsert.get(insertListIndex), vanSerialIndex,
                        syncResults.get(syncResultIndex));
                syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
                        syncUploadDataDigester.getSyncedBy(), false, "INSERT: " + mainErrorReason));
            }
            insertSuccess = false;
            overallSuccess = false;
        }
    }

    // Process UPDATE operations
    if (!syncDataListUpdate.isEmpty()) {
        String queryUpdate = getQueryToUpdateDataToServerDB(schemaName, serverColumns, syncTableName);

        try {
            int[] updateResults = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
                    serverColumns, queryUpdate, syncDataListUpdate);
logger.info("Update Results="+updateResults);
            for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int updateListIndex = entry.getValue();

logger.info("Update Results Array: {}", Arrays.toString(updateResults));
                boolean success = updateListIndex < updateResults.length && updateResults[updateListIndex] > 0;
logger.info("Success="+success);
                if (!success) {
                        Object[] failedParams = syncDataListUpdate.get(updateListIndex);
 
    logger.info("No rows updated for {}. Query: {} | Params: {}",
            syncTableName, queryUpdate, Arrays.toString(failedParams));
 
    String failedVanSerialNo = getVanSerialNo(failedParams, vanSerialIndex,
            syncResults.get(syncResultIndex));
    String conciseReason = "No matching row (0 rows updated)";

    //                 String failedVanSerialNo = getVanSerialNo(syncDataListUpdate.get(updateListIndex),
    //                         vanSerialIndex, syncResults.get(syncResultIndex));
                    // String conciseReason = "Update failed (code: " +
                            // (updateListIndex < updateResults.length ? updateResults[updateListIndex] : "unknown") + ")";
                    syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, failedVanSerialNo,
                            syncUploadDataDigester.getSyncedBy(), false, conciseReason));
                    updateSuccess = false;
                    overallSuccess = false;
                } else {
                    syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName,
                            syncResults.get(syncResultIndex).getVanSerialNo(),
                            syncUploadDataDigester.getSyncedBy(), true, null));
                }
            }

        } catch (Exception e) {
            String mainErrorReason = extractMainErrorReason(e);
            logger.info("Batch update failed for table {}: {}", syncTableName, mainErrorReason, e);
            
            // Mark ALL updates as failed
            for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int updateListIndex = entry.getValue();
                String vanSerialNo = getVanSerialNo(syncDataListUpdate.get(updateListIndex), vanSerialIndex,
                        syncResults.get(syncResultIndex));
                syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
                        syncUploadDataDigester.getSyncedBy(), false, "UPDATE: " + mainErrorReason));
            }
            updateSuccess = false;
            overallSuccess = false;
        }
    }

    logger.info("Sync results for table {}: {}", syncTableName, syncResults);
    return overallSuccess;
}
    // private boolean performGenericTableSync(SyncUploadDataDigester syncUploadDataDigester,
//             List<SyncResult> syncResults) {
//         List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
//         List<Object[]> syncDataListInsert = new ArrayList<>();
//         List<Object[]> syncDataListUpdate = new ArrayList<>();

//         // Track indices for insert and update operations
//         Map<Integer, Integer> insertIndexMap = new HashMap<>(); // syncResults index -> insert list index
//         Map<Integer, Integer> updateIndexMap = new HashMap<>(); // syncResults index -> update list index

//         boolean overallSuccess = true;

//         if (dataToBesync == null || dataToBesync.isEmpty()) {
//             logger.info("No data to sync for table: {}", syncUploadDataDigester.getTableName());
//             return true;
//         }

//         String syncTableName = syncUploadDataDigester.getTableName();
//         String vanAutoIncColumnName = syncUploadDataDigester.getVanAutoIncColumnName();
//         String schemaName = syncUploadDataDigester.getSchemaName();
//         Integer facilityIDFromDigester = syncUploadDataDigester.getFacilityID();
//         String serverColumns = syncUploadDataDigester.getServerColumns();

//         int vanSerialIndex = Arrays.asList(serverColumns.split(",")).indexOf(vanAutoIncColumnName);
//         List<String> serverColumnsList = Arrays.asList(serverColumns.split(","));

//         for (Map<String, Object> map : dataToBesync) {
//             // Create a new map with clean column names as keys
//             Map<String, Object> cleanRecord = new HashMap<>();
//             for (String key : map.keySet()) {
//                 String cleanKey = key;
//                 // Handle keys with SQL functions like date_format
//                 if (key.startsWith("date_format(") && key.endsWith(")")) {
//                     int start = key.indexOf("(") + 1;
//                     int end = key.indexOf(",");
//                     if (end > start) {
//                         cleanKey = key.substring(start, end).trim();
//                     } else {
//                         cleanKey = key.substring(start, key.indexOf(")")).trim();
//                     }
//                 }
//                 cleanRecord.put(cleanKey.trim(), map.get(key));
//             }

//             String vanSerialNo = String.valueOf(cleanRecord.get(vanAutoIncColumnName));
//             String vanID = String.valueOf(cleanRecord.get("VanID"));
//             int syncFacilityID = 0;

//             // Update SyncedBy and SyncedDate in the cleanRecord
//             cleanRecord.put("SyncedBy", syncUploadDataDigester.getSyncedBy());
//             cleanRecord.put("SyncedDate", String.valueOf(LocalDateTime.now()));

//             if (facilityIDFromDigester != null) {
//                 // Determine the 'Processed' status based on facility ID for specific tables
//                 switch (syncTableName.toLowerCase()) {
//                     case "t_indent":
//                     case "t_indentorder": {
//                         if (cleanRecord.containsKey("FromFacilityID")
//                                 && cleanRecord.get("FromFacilityID") instanceof Number) {
//                             Number fromFacilityID = (Number) cleanRecord.get("FromFacilityID");
//                             if (fromFacilityID.intValue() == facilityIDFromDigester) {
//                                 cleanRecord.put("Processed", "P");
//                             }
//                         }
//                         break;
//                     }
//                     case "t_indentissue": {
//                         if (cleanRecord.containsKey("ToFacilityID")
//                                 && cleanRecord.get("ToFacilityID") instanceof Number) {
//                             Number toFacilityID = (Number) cleanRecord.get("ToFacilityID");
//                             if (toFacilityID.intValue() == facilityIDFromDigester) {
//                                 cleanRecord.put("Processed", "P");
//                             }
//                         }
//                         break;
//                     }
//                     case "t_stocktransfer": {
//                         if (cleanRecord.containsKey("TransferToFacilityID")
//                                 && cleanRecord.get("TransferToFacilityID") instanceof Number) {
//                             Number transferToFacilityID = (Number) cleanRecord.get("TransferToFacilityID");
//                             if (transferToFacilityID.intValue() == facilityIDFromDigester) {
//                                 cleanRecord.put("Processed", "P");
//                             }
//                         }
//                         break;
//                     }
//                     case "t_itemstockentry": {
//                         if (cleanRecord.containsKey("FacilityID") && cleanRecord.get("FacilityID") instanceof Number) {
//                             Number mapFacilityID = (Number) cleanRecord.get("FacilityID");
//                             if (mapFacilityID.intValue() == facilityIDFromDigester) {
//                                 cleanRecord.put("Processed", "P");
//                             }
//                         }
//                         break;
//                     }
//                     default:
//                         break;
//                 }
//             }

//             // Extract SyncFacilityID for checkRecordIsAlreadyPresentOrNot
//             if (cleanRecord.containsKey("SyncFacilityID") && cleanRecord.get("SyncFacilityID") instanceof Number) {
//                 syncFacilityID = ((Number) cleanRecord.get("SyncFacilityID")).intValue();
//             }

//             int recordCheck;
//             try {
//                 recordCheck = dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(
//                         schemaName, syncTableName, vanSerialNo, vanID, vanAutoIncColumnName, syncFacilityID);
//                 logger.info("Record check result: {}", recordCheck);
//             } catch (Exception e) {
//                 logger.error("Error checking record existence for table {}: VanSerialNo={}, VanID={}. Error: {}",
//                         syncTableName, vanSerialNo, vanID, e.getMessage(), e);

//                 // Store the main error reason from record check failure
//                 String mainErrorReason = "Record check failed: " + extractMainErrorReason(e);

//                 syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo,
//                         syncUploadDataDigester.getSyncedBy(), false, mainErrorReason));
//                 continue; // Skip to next record
//             }

//             // Prepare Object array for insert/update
//             List<Object> currentRecordValues = new ArrayList<>();
//             for (String column : serverColumnsList) {
//                 Object value = cleanRecord.get(column.trim());
//                 if (value instanceof Boolean) {
//                     currentRecordValues.add(value);
//                 } else if (value != null) {
//                     currentRecordValues.add(String.valueOf(value));
//                 } else {
//                     currentRecordValues.add(null);
//                 }
//             }

//             Object[] objArr = currentRecordValues.toArray();

//             // Add to syncResults first, then track the index
//             int currentSyncResultIndex = syncResults.size();
//             syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo,
//                     syncUploadDataDigester.getSyncedBy(), false, "Pending")); // Initially set as success

//             if (recordCheck == 0) {
//                 // Record doesn't exist - INSERT
//                 insertIndexMap.put(currentSyncResultIndex, syncDataListInsert.size());
//                 syncDataListInsert.add(objArr);
//             } else {
//                 // Record exists - UPDATE
//                 List<Object> updateParams = new ArrayList<>(Arrays.asList(objArr));
//                 updateParams.add(String.valueOf(vanSerialNo));

//                 if (Arrays.asList("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
//                         "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
//                         "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit")
//                         .contains(syncTableName.toLowerCase()) && cleanRecord.containsKey("SyncFacilityID")) {
//                     updateParams.add(String.valueOf(cleanRecord.get("SyncFacilityID")));
//                 } else {
//                     updateParams.add(String.valueOf(vanID));
//                 }

//                 updateIndexMap.put(currentSyncResultIndex, syncDataListUpdate.size());
//                 syncDataListUpdate.add(updateParams.toArray());
//             }
//         }

//         boolean insertSuccess = false;
//         boolean updateSuccess = false;

//         // Process INSERT operations
//         if (!syncDataListInsert.isEmpty()) {
//             String queryInsert = getQueryToInsertDataToServerDB(schemaName, syncTableName, serverColumns);

//             try {
//                 int[] insertResults = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
//                         serverColumns, queryInsert, syncDataListInsert);

//                 for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
//                     int syncResultIndex = entry.getKey();
//                     int insertListIndex = entry.getValue();

//                     boolean success = insertListIndex < insertResults.length && insertResults[insertListIndex] > 0;

//                     if (!success) {
//                         String failedVanSerialNo = getVanSerialNo(syncDataListInsert.get(insertListIndex),
//                                 vanSerialIndex,
//                                 syncResults.get(syncResultIndex));
//                         String conciseReason = "Insert failed (code: " +
//                                 (insertListIndex < insertResults.length ? insertResults[insertListIndex] : "unknown") +
//                                 ")";
//                         syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, failedVanSerialNo,
//                                 syncUploadDataDigester.getSyncedBy(), false, conciseReason));
//                         insertSuccess = false;
//                     }
//                     else {
//     // ADD THIS ELSE BLOCK
//     syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName,
//             syncResults.get(syncResultIndex).getVanSerialNo(),
//             syncUploadDataDigester.getSyncedBy(), true, null));
// }
//                 }

//             } catch (Exception e) {
//     String mainErrorReason = extractMainErrorReason(e);
    
//     // Check if we can get partial results even with an exception
//     try {
//         // Try to check which records actually made it to the database
//         for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
//             int syncResultIndex = entry.getKey();
//             int insertListIndex = entry.getValue();
            
//             String vanSerialNo = getVanSerialNo(syncDataListInsert.get(insertListIndex), vanSerialIndex,
//                     syncResults.get(syncResultIndex));
//             int vanIDIndex = serverColumnsList.indexOf("VanID");
//             String vanID = vanIDIndex >= 0 && vanIDIndex < syncDataListInsert.get(insertListIndex).length
//                 ? String.valueOf(syncDataListInsert.get(insertListIndex)[vanIDIndex])
//                 : null;
            
//             // Check if this specific record exists in DB now (it might have succeeded before the exception)
//             int recordExists = dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(
//                     schemaName, syncTableName, vanSerialNo, vanID, vanAutoIncColumnName, 0);
            
//             if (recordExists > 0) {
//                 // Record exists - it was actually inserted successfully
//                 syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
//                         syncUploadDataDigester.getSyncedBy(), true, null));
//             } else {
//                 // Record doesn't exist - it failed
//                 syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
//                         syncUploadDataDigester.getSyncedBy(), false, "INSERT: " + mainErrorReason));
//             }
//         }
//     } catch (Exception checkException) {
//         // If we can't check, mark all as failed
//         for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
//             int syncResultIndex = entry.getKey();
//             int insertListIndex = entry.getValue();
//             String vanSerialNo = getVanSerialNo(syncDataListInsert.get(insertListIndex), vanSerialIndex,
//                     syncResults.get(syncResultIndex));
//             syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
//                     syncUploadDataDigester.getSyncedBy(), false, "INSERT: " + mainErrorReason));
//         }
//     }
//     insertSuccess = false;
// }
//         }

//         if (!syncDataListUpdate.isEmpty()) {
//             String queryUpdate = getQueryToUpdateDataToServerDB(schemaName, serverColumns, syncTableName);

//             try {
//                 int[] updateResults = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
//                         serverColumns, queryUpdate, syncDataListUpdate);

//                 for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
//                     int syncResultIndex = entry.getKey();
//                     int updateListIndex = entry.getValue();

//                     boolean success = updateListIndex < updateResults.length && updateResults[updateListIndex] > 0;

//                     if (!success) {
//                         String failedVanSerialNo = getVanSerialNo(syncDataListUpdate.get(updateListIndex),
//                                 vanSerialIndex,
//                                 syncResults.get(syncResultIndex));
//                         String conciseReason = "Update failed (code: " +
//                                 (updateListIndex < updateResults.length ? updateResults[updateListIndex] : "unknown") +
//                                 ")";
//                         syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, failedVanSerialNo,
//                                 syncUploadDataDigester.getSyncedBy(), false, conciseReason));
//                         updateSuccess = false;
//                     }
//                     else {
//     // ADD THIS ELSE BLOCK  
//     syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName,
//             syncResults.get(syncResultIndex).getVanSerialNo(),
//             syncUploadDataDigester.getSyncedBy(), true, null));
// }
//                 }

//             } catch (Exception e) {
//     String mainErrorReason = extractMainErrorReason(e);
    
//     // Check if we can get partial results even with an exception
//     try {
//         // Try to check which records actually made it to the database
//         for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
//             int syncResultIndex = entry.getKey();
//             int updateListIndex = entry.getValue();
            
//             String vanSerialNo = getVanSerialNo(syncDataListUpdate.get(updateListIndex), vanSerialIndex,
//                     syncResults.get(syncResultIndex));
//             int vanIDIndex = serverColumnsList.indexOf("VanID");
//             String vanID = vanIDIndex >= 0 && vanIDIndex < syncDataListUpdate.get(updateListIndex).length
//                 ? String.valueOf(syncDataListUpdate.get(updateListIndex)[vanIDIndex])
//                 : null;
            
//             // Check if this specific record was actually updated in DB
//             int recordExists = dataSyncRepositoryCentral.checkRecordIsAlreadyPresentOrNot(
//                     schemaName, syncTableName, vanSerialNo, vanID, vanAutoIncColumnName, 0);
            
//             if (recordExists > 0) {
//                 // Record exists and was likely updated successfully
//                 syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
//                         syncUploadDataDigester.getSyncedBy(), true, null));
//             } else {
//                 // Record doesn't exist or update failed
//                 syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
//                         syncUploadDataDigester.getSyncedBy(), false, "UPDATE: " + mainErrorReason));
//             }
//         }
//     } catch (Exception checkException) {
//         // If we can't check, mark all as failed
//         for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
//             int syncResultIndex = entry.getKey();
//             int updateListIndex = entry.getValue();
//             String vanSerialNo = getVanSerialNo(syncDataListUpdate.get(updateListIndex), vanSerialIndex,
//                     syncResults.get(syncResultIndex));
//             syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
//                     syncUploadDataDigester.getSyncedBy(), false, "UPDATE: " + mainErrorReason));
//         }
//     }
//     updateSuccess = false;
// }
//         }

//         logger.info("Sync results for table {}: {}", syncTableName, syncResults);
//         return insertSuccess && updateSuccess;
//     }

    private String getVanSerialNo(Object[] record, int vanSerialIndex, SyncResult originalResult) {
        if (vanSerialIndex >= 0 && vanSerialIndex < record.length) {
            return String.valueOf(record[vanSerialIndex]);
        }
        return originalResult.getVanSerialNo() != null ? originalResult.getVanSerialNo() : "UNKNOWN";
    }

    // Helper method to extract concise but meaningful error message
    private String extractMainErrorReason(Exception e) {
        if (e == null) {
            return "Unknown error";
        }

        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return e.getClass().getSimpleName();
        }

        // Extract key information based on common error patterns
        message = message.trim();

        // Handle SQL constraint violations - extract the key constraint info
        if (message.contains("Duplicate entry") && message.contains("for key")) {
            // Extract: "Duplicate entry 'value' for key 'constraint_name'"
            int keyStart = message.indexOf("for key '") + 9;
            int keyEnd = message.indexOf("'", keyStart);
            if (keyStart > 8 && keyEnd > keyStart) {
                return "Duplicate key: " + message.substring(keyStart, keyEnd);
            }
            return "Duplicate entry error";
        }

        // Handle column cannot be null
        if (message.contains("cannot be null")) {
            int colStart = message.indexOf("Column '") + 8;
            int colEnd = message.indexOf("'", colStart);
            if (colStart > 7 && colEnd > colStart) {
                return "Required field: " + message.substring(colStart, colEnd);
            }
            return "Required field missing";
        }

        // Handle data too long errors
        if (message.contains("Data too long for column")) {
            int colStart = message.indexOf("column '") + 8;
            int colEnd = message.indexOf("'", colStart);
            if (colStart > 7 && colEnd > colStart) {
                return "Data too long: " + message.substring(colStart, colEnd);
            }
            return "Data length exceeded";
        }

        // Handle foreign key constraint violations
        if (message.contains("foreign key constraint")) {
            if (message.contains("CONSTRAINT `")) {
                int constStart = message.indexOf("CONSTRAINT `") + 12;
                int constEnd = message.indexOf("`", constStart);
                if (constStart > 11 && constEnd > constStart) {
                    return "FK violation: " + message.substring(constStart, constEnd);
                }
            }
            return "Foreign key constraint failed";
        }

        // Handle connection/timeout issues
        if (message.toLowerCase().contains("timeout")) {
            return "Database connection timeout";
        }

        if (message.toLowerCase().contains("connection")) {
            return "Database connection failed";
        }

        // Handle table/schema issues
        if (message.contains("doesn't exist")) {
            return "Table/schema not found";
        }

        // For other cases, try to get the first meaningful part of the message
        // Split by common delimiters and take the first substantial part
        String[] parts = message.split("[;:|]");
        for (String part : parts) {
            part = part.trim();
            if (part.length() > 10 && part.length() <= 100) { // Reasonable length
                return part;
            }
        }

        // If message is short enough, return it as is
        if (message.length() <= 150) {
            return message;
        }

        // Otherwise, truncate to first 150 characters
        return message.substring(0, 150) + "...";
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
                String column = columnsArr[i].trim();
                preparedStatementSetter.append(column).append(" = ?");
                if (i < columnsArr.length - 1) {
                    preparedStatementSetter.append(", ");
                }
            }
        }

        StringBuilder queryBuilder = new StringBuilder("UPDATE ");
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
        logger.info("Test Query Builder: {}", queryBuilder.toString());
        return queryBuilder.toString();
    }

    // Helper to get information about failed records (for logging purposes)
    private String getFailedRecords(int[] results, List<Object[]> data) {
        logger.info("Inside get Failed Records");
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