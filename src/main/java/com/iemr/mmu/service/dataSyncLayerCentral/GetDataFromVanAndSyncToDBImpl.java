package com.iemr.mmu.service.dataSyncLayerCentral;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


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
    private static final Set<String> VALID_SCHEMAS = new HashSet<>(Arrays.asList("public", "db_iemr_mmu_sync")); // Add your actual schema names
    private static final Set<String> VALID_TABLES = new HashSet<>(Arrays.asList(
        "m_beneficiaryregidmapping", "i_beneficiaryaccount","i_beneficiaryaddress","i_beneficiarycontacts","i_beneficiarydetails","i_beneficiaryfamilymapping","i_beneficiaryidentity","i_beneficiarymapping",
        "t_benvisitdetail","t_phy_anthropometry","t_phy_vitals","t_benadherence","t_anccare","t_pnccare","t_ncdscreening","t_ncdcare","i_ben_flow_outreach","t_covid19","t_idrsdetails","t_physicalactivity",
        "t_phy_generalexam","t_phy_headtotoe","t_sys_obstetric","t_sys_gastrointestinal","t_sys_cardiovascular","t_sys_respiratory","t_sys_centralnervous","t_sys_musculoskeletalsystem","t_sys_genitourinarysystem",
        "t_ancdiagnosis","t_ncddiagnosis","t_pncdiagnosis","t_benchefcomplaint","t_benclinicalobservation","t_prescription","t_prescribeddrug","t_lab_testorder","t_benreferdetails",
        "t_lab_testresult","t_physicalstockentry","t_patientissue","t_facilityconsumption","t_itemstockentry","t_itemstockexit",
        "t_benmedhistory","t_femaleobstetrichistory","t_benmenstrualdetails","t_benpersonalhabit","t_childvaccinedetail1","t_childvaccinedetail2","t_childoptionalvaccinedetail","t_ancwomenvaccinedetail","t_childfeedinghistory","t_benallergyhistory","t_bencomorbiditycondition","t_benmedicationhistory","t_benfamilyhistory","t_perinatalhistory","t_developmenthistory",
        "t_cancerfamilyhistory","t_cancerpersonalhistory","t_cancerdiethistory","t_cancerobstetrichistory","t_cancervitals","t_cancersignandsymptoms","t_cancerlymphnode","t_canceroralexamination","t_cancerbreastexamination","t_cancerabdominalexamination","t_cancergynecologicalexamination","t_cancerdiagnosis","t_cancerimageannotation",
        "i_beneficiaryimage",
        "t_stockadjustment","t_stocktransfer","t_patientreturn","t_indent","t_indentissue","t_indentorder","t_saitemmapping"
    ));

    static {
        
        TABLE_GROUPS.put(1, Arrays.asList("m_beneficiaryregidmapping", "i_beneficiaryaccount","i_beneficiaryaddress","i_beneficiarycontacts","i_beneficiarydetails","i_beneficiaryfamilymapping","i_beneficiaryidentity","i_beneficiarymapping"));

        TABLE_GROUPS.put(2, Arrays.asList("t_benvisitdetail","t_phy_anthropometry","t_phy_vitals","t_benadherence","t_anccare","t_pnccare","t_ncdscreening","t_ncdcare","i_ben_flow_outreach","t_covid19","t_idrsdetails","t_physicalactivity"));
        
        TABLE_GROUPS.put(3, Arrays.asList("t_phy_generalexam","t_phy_headtotoe","t_sys_obstetric","t_sys_gastrointestinal","t_sys_cardiovascular","t_sys_respiratory","t_sys_centralnervous","t_sys_musculoskeletalsystem","t_sys_genitourinarysystem"));
        
        TABLE_GROUPS.put(4, Arrays.asList("t_ancdiagnosis","t_ncddiagnosis","t_pncdiagnosis","t_benchefcomplaint","t_benclinicalobservation","t_prescription","t_prescribeddrug","t_lab_testorder","t_benreferdetails"));

        TABLE_GROUPS.put(5, Arrays.asList("t_lab_testresult","t_physicalstockentry","t_patientissue","t_facilityconsumption","t_itemstockentry","t_itemstockexit"));

        TABLE_GROUPS.put(6, Arrays.asList("t_benmedhistory","t_femaleobstetrichistory","t_benmenstrualdetails","t_benpersonalhabit","t_childvaccinedetail1","t_childvaccinedetail2","t_childoptionalvaccinedetail","t_ancwomenvaccinedetail","t_childfeedinghistory","t_benallergyhistory","t_bencomorbiditycondition","t_benmedicationhistory","t_benfamilyhistory","t_perinatalhistory","t_developmenthistory"));

        TABLE_GROUPS.put(7, Arrays.asList("t_cancerfamilyhistory","t_cancerpersonalhistory","t_cancerdiethistory","t_cancerobstetrichistory","t_cancervitals","t_cancersignandsymptoms","t_cancerlymphnode","t_canceroralexamination","t_cancerbreastexamination","t_cancerabdominalexamination","t_cancergynecologicalexamination","t_cancerdiagnosis","t_cancerimageannotation"));

        TABLE_GROUPS.put(8, Arrays.asList("i_beneficiaryimage"));
        
        TABLE_GROUPS.put(9, Arrays.asList("t_itemstockentry","t_itemstockexit","t_patientissue","t_physicalstockentry","t_stockadjustment","t_stocktransfer","t_patientreturn","t_facilityconsumption","t_indent","t_indentissue","t_indentorder","t_saitemmapping"));
      
    }

    public String syncDataToServer(String requestOBJ, String Authorization, String token) throws Exception {
        logger.info("Starting syncDataToServer. Token: {}", token);
        ObjectMapper mapper = new ObjectMapper();
        SyncUploadDataDigester syncUploadDataDigester = mapper.readValue(requestOBJ, SyncUploadDataDigester.class);

        if (syncUploadDataDigester == null || syncUploadDataDigester.getTableName() == null) {
            logger.error("Invalid SyncUploadDataDigester object or tableName is null.");
            return "Error: Invalid sync request.";
        }

        String syncTableName = syncUploadDataDigester.getTableName();
        String schemaName = syncUploadDataDigester.getSchemaName();

        if (!isValidSchemaName(schemaName) || !isValidTableName(syncTableName)) {
            logger.error("Invalid schema or table name provided: Schema='{}', Table='{}'.", schemaName, syncTableName);
            return "Error: Invalid schema or table name.";
        }


        // Handle specific tables first, if their logic is distinct
        if ("m_beneficiaryregidmapping".equalsIgnoreCase(syncTableName)) {
            String result = update_M_BeneficiaryRegIdMapping_for_provisioned_benID(syncUploadDataDigester);
            if ("data sync passed".equals(result)) {
                return "Sync successful for m_beneficiaryregidmapping.";
            } else {
                logger.error("Sync failed for m_beneficiaryregidmapping: {}", result);
                return "Sync failed for m_beneficiaryregidmapping.";
            }
        }  
        if ("i_beneficiarydetails".equalsIgnoreCase(syncTableName)) {
            String result = update_I_BeneficiaryDetails_for_processed_in_batches(syncUploadDataDigester);
            if ("data sync passed".equals(result)) {
                return "Sync successful for i_beneficiarydetails.";
            } else {
                logger.error("Sync failed for i_beneficiarydetails: {}", result);
                return "Sync failed for i_beneficiarydetails.";
            }
        } else {
            // Determine the group for the current table or iterate through all if no specific table is given
            boolean syncSuccess = true;
            String errorMessage = "";

            // If a specific table is provided in the request, try to find its group and sync only that table.
            // Otherwise, iterate through all defined groups.
            if (syncTableName != null && !syncTableName.isEmpty()) {
                boolean foundInGroup = false;
                for (Map.Entry<Integer, List<String>> entry : TABLE_GROUPS.entrySet()) {
                    if (entry.getValue().contains(syncTableName.toLowerCase())) {
                        logger.info("Attempting to sync table '{}' from Group {}", syncTableName, entry.getKey());
                        syncSuccess = syncTablesInGroup(syncUploadDataDigester.getSchemaName(), syncTableName, syncUploadDataDigester);
                        foundInGroup = true;
                        break;
                    }
                }
                if (!foundInGroup) {
                    logger.warn("Table '{}' not found in any predefined groups. Proceeding with generic sync logic.", syncTableName);
                    syncSuccess = performGenericTableSync(syncUploadDataDigester);
                }
            } else {
                // If no specific table is in the request (e.g., a general sync trigger), iterate through groups
                logger.info("No specific table provided. Attempting to sync all tables group by group.");
                for (Map.Entry<Integer, List<String>> entry : TABLE_GROUPS.entrySet()) {
                    Integer groupId = entry.getKey();
                    List<String> tablesInGroup = entry.getValue();
                    logger.info("Starting sync for Group {}", groupId);
                    for (String table : tablesInGroup) {
                        if (!isValidTableName(table)) {
                            logger.error("Invalid table name '{}' encountered in group {}. Skipping.", table, groupId);
                            syncSuccess = false;
                            errorMessage += "Invalid table name: " + table + " in Group " + groupId + ". ";
                            continue; // Skip this table
                        }

                        try {
                           
                            boolean currentTableSyncResult = syncTablesInGroup(syncUploadDataDigester.getSchemaName(), table, syncUploadDataDigester);
                            if (!currentTableSyncResult) {
                                syncSuccess = false;
                                errorMessage += "Failed to sync table: " + table + " in Group " + groupId + ". ";
                                logger.error("Sync failed for table '{}' in Group {}. Error: {}", table, groupId, errorMessage);
                            } else {
                                logger.info("Successfully synced table: {} in Group {}", table, groupId);
                            }
                        } catch (Exception e) {
                            syncSuccess = false;
                            errorMessage += "Exception during sync for table: " + table + " in Group " + groupId + ": " + e.getMessage() + ". ";
                            logger.error("Exception during sync for table '{}' in Group {}: {}", table, groupId, e.getMessage(), e);
                        }
                    }
                }
            }

            if (syncSuccess) {
                logger.info("Overall data sync passed.");
                return "Overall data sync passed.";
            } else {
                logger.info("Overall data sync failed. Details: " + errorMessage);
                return "Overall data sync failed. Details: " + errorMessage;
            }
        }
    }

   
    private boolean syncTablesInGroup(String schemaName, String currentTableName, SyncUploadDataDigester originalDigester) {
        logger.info("Attempting generic sync for table: {}", currentTableName);
        
        // Validate schemaName and currentTableName for safety before proceeding
        if (!isValidSchemaName(schemaName) || !isValidTableName(currentTableName)) {
            logger.error("Invalid schema or table name for group sync: Schema='{}', Table='{}'.", schemaName, currentTableName);
            return false; // Fail fast if identifiers are invalid
        }

        SyncUploadDataDigester tableSpecificDigester = new SyncUploadDataDigester();
        tableSpecificDigester.setSchemaName(schemaName);
        tableSpecificDigester.setTableName(currentTableName);
        tableSpecificDigester.setSyncedBy(originalDigester.getSyncedBy());
        tableSpecificDigester.setFacilityID(originalDigester.getFacilityID());
        tableSpecificDigester.setVanAutoIncColumnName(originalDigester.getVanAutoIncColumnName());
        tableSpecificDigester.setServerColumns(originalDigester.getServerColumns()); // Assuming serverColumns is generic or set per table

       tableSpecificDigester.setSyncData(originalDigester.getSyncData()); // Placeholder: Replace with actual data fetching

        return performGenericTableSync(tableSpecificDigester);
    }


    private String update_M_BeneficiaryRegIdMapping_for_provisioned_benID(SyncUploadDataDigester syncUploadDataDigester) {
        logger.info("Processing update_M_BeneficiaryRegIdMapping_for_provisioned_benID for table: {}", syncUploadDataDigester.getTableName());
        
        String schemaName = syncUploadDataDigester.getSchemaName();
        String tableName = syncUploadDataDigester.getTableName();

        if (!isValidSchemaName(schemaName) || !isValidTableName(tableName)) {
            logger.error("Invalid schema or table name provided for m_beneficiaryregidmapping update: Schema='{}', Table='{}'.", schemaName, tableName);
            return "Error: Invalid schema or table name.";
        }

        List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
        List<Object[]> syncData = new ArrayList<>();

        String query = String.format("UPDATE %s.%s SET Provisioned = true, SyncedDate = now(), SyncedBy = ? WHERE BenRegId = ? AND BeneficiaryID = ? AND VanID = ?", schemaName, tableName);

        for (Map<String, Object> map : dataToBesync) {
            if (map.get("BenRegId") != null && map.get("BeneficiaryID") != null && map.get("VanID") != null) {
                Object[] objArr = new Object[4];
                objArr[0] = syncUploadDataDigester.getSyncedBy(); // SyncedBy
                objArr[1] = String.valueOf(map.get("BenRegId"));
                objArr[2] = String.valueOf(map.get("BeneficiaryID"));
                objArr[3] = String.valueOf(map.get("VanID"));
                syncData.add(objArr);
            } else {
                logger.warn("Skipping record in m_beneficiaryregidmapping due to missing BenRegId, BeneficiaryID, or VanID: {}", map);
            }
        }

        if (!syncData.isEmpty()) {
            try {
                int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName,
                        tableName, SERVER_COLUMNS_NOT_REQUIRED, query, syncData);

                if (i.length == syncData.size()) {
                    logger.info("Successfully updated {} records for m_beneficiaryregidmapping.", i.length);
                    return "data sync passed";
                } else {
                    logger.error("Partial update for m_beneficiaryregidmapping. Expected {} updates, got {}. Failed records: {}", syncData.size(), i.length, getFailedRecords(i, syncData));
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

    
    public String update_I_BeneficiaryDetails_for_processed_in_batches(SyncUploadDataDigester syncUploadDataDigester) {
        logger.info("Processing update_I_BeneficiaryDetails_for_processed_in_batches for table: {}", syncUploadDataDigester.getTableName());
        String schemaName = syncUploadDataDigester.getSchemaName();
        String tableName = syncUploadDataDigester.getTableName();

        if (!isValidSchemaName(schemaName) || !isValidTableName(tableName)) {
            logger.error("Invalid schema or table name provided for i_beneficiarydetails update: Schema='{}', Table='{}'.", schemaName, tableName);
            return "Error: Invalid schema or table name.";
        }

        List<Object[]> syncData = new ArrayList<>(); // This list will hold data for batch updates to 'Processed'
    
        String updateQuery = getQueryFor_I_BeneficiaryDetails(schemaName, tableName);
    
        int limit = 1000;
        int offset = 0;
        int totalProcessed = 0;
    
        String whereClauseForBatchFetch = " WHERE Processed <> 'P' AND VanID IS NOT NULL "; // This is for fetching, not for update
    
        while (true) {
            List<Map<String, Object>> batchToFetch;
            try {
               batchToFetch = dataSyncRepositoryCentral.getBatchForBenDetails(
                        schemaName,
                        tableName,
                        syncUploadDataDigester.getServerColumns(), 
                        whereClauseForBatchFetch, 
                        limit,
                        offset);
            } catch (Exception e) {
                logger.error("Error fetching batch for i_beneficiarydetails: {}", e.getMessage(), e);
                return "Error fetching data for i_beneficiarydetails: " + e.getMessage();
            }
        
            if (batchToFetch.isEmpty()) {
                break; 
            }
        
            for (Map<String, Object> map : batchToFetch) {
                if (map.get("BeneficiaryDetailsId") != null && map.get("VanID") != null) {
                    Object[] params = new Object[3];
                    params[0] = syncUploadDataDigester.getSyncedBy();
                    params[1] = String.valueOf(map.get("BeneficiaryDetailsId"));
                    params[2] = String.valueOf(map.get("VanID"));
                    syncData.add(params);
                } else {
                    logger.warn("Skipping record in i_beneficiarydetails due to missing BeneficiaryDetailsId or VanID: {}", map);
                }
            }
        
            if (!syncData.isEmpty()) {
                try {
                    int[] batchUpdateResults = dataSyncRepositoryCentral.syncDataToCentralDB(
                            schemaName,
                            tableName,
                            SERVER_COLUMNS_NOT_REQUIRED,
                            updateQuery,
                            syncData);
        
                    int successfulUpdates = 0;
                    for (int result : batchUpdateResults) {
                        if (result >= 1) { 
                            successfulUpdates++;
                        }
                    }
                    totalProcessed += successfulUpdates;
                    logger.info("Batch update for i_beneficiarydetails: {} records processed, {} successfully updated.", syncData.size(), successfulUpdates);
        
                    syncData.clear();
                    offset += limit; 
        
                } catch (Exception e) {
                    logger.error("Exception during batch update for i_beneficiarydetails: {}", e.getMessage(), e);
                    return "Error during sync for i_beneficiarydetails: " + e.getMessage();
                }
            } else {
                logger.info("No valid records in the current batch for i_beneficiarydetails to update.");
                offset += limit;
            }
        }
    
        if (totalProcessed > 0) {
            logger.info("Finished processing i_beneficiarydetails. Total records processed: {}", totalProcessed);
            return "data sync passed";
        } else {
            logger.info("No records were processed for i_beneficiarydetails.");
            return "No data processed for i_beneficiarydetails.";
        }
    }

    private String getQueryFor_I_BeneficiaryDetails(String schemaName, String tableName) {
        if (!isValidSchemaName(schemaName) || !isValidTableName(tableName)) {
            logger.error("Invalid schema or table name for getQueryFor_I_BeneficiaryDetails: Schema='{}', Table='{}'.", schemaName, tableName);
            throw new IllegalArgumentException("Invalid schema or table name provided.");
        }
        return String.format("UPDATE %s.%s SET Processed = 'P', SyncedDate = now(), SyncedBy = ? WHERE BeneficiaryDetailsId = ? AND VanID = ?", schemaName, tableName);
    }


    
    private boolean performGenericTableSync(SyncUploadDataDigester syncUploadDataDigester) {
        logger.info("Performing generic sync for table: {}", syncUploadDataDigester.getTableName());
        
        String schemaName = syncUploadDataDigester.getSchemaName();
        String syncTableName = syncUploadDataDigester.getTableName();
        String vanAutoIncColumnName = syncUploadDataDigester.getVanAutoIncColumnName();
        String serverColumns = syncUploadDataDigester.getServerColumns();

        if (!isValidSchemaName(schemaName) || !isValidTableName(syncTableName)) {
            logger.error("Invalid schema or table name for generic sync: Schema='{}', Table='{}'.", schemaName, syncTableName);
            return false;
        }

        if (!isValidColumnNames(serverColumns)) {
             logger.error("Invalid server columns provided for generic sync: {}", serverColumns);
             return false;
        }


        List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
        List<Object[]> syncDataListInsert = new ArrayList<>();
        List<Object[]> syncDataListUpdate = new ArrayList<>();

        if (dataToBesync == null || dataToBesync.isEmpty()) {
            logger.info("No data to sync for table: {}", syncUploadDataDigester.getTableName());
            return true; // Nothing to sync, consider it a success
        }

        Integer facilityIDFromDigester = syncUploadDataDigester.getFacilityID();

        for (Map<String, Object> map : dataToBesync) {
            String vanSerialNo = String.valueOf(map.get(vanAutoIncColumnName));
            String vanID = String.valueOf(map.get("VanID"));
            int syncFacilityID = 0;

            map.put("SyncedBy", syncUploadDataDigester.getSyncedBy());
            map.put("SyncedDate", String.valueOf(LocalDateTime.now())); // Ensure column name matches DB

            if (facilityIDFromDigester != null) {
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
                        if (map.containsKey("TransferToFacilityID") && map.get("TransferToFacilityID") instanceof Double) {
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
                        // No specific facility ID logic for other tables, maintain existing 'Processed' status or default
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
                logger.error("Error checking record existence for table {}: VanSerialNo={}, VanID={}. Error: {}", syncTableName, vanSerialNo, vanID, e.getMessage(), e);
                return false; // Critical error, stop sync for this table
            }

            // Prepare Object array for insert/update
            Object[] objArr;
            List<String> serverColumnsList = Arrays.asList(serverColumns.split(","));
            List<Object> currentRecordValues = new ArrayList<>();

            for (String column : serverColumnsList) {
                Object value = map.get(column.trim());
                // Handle boolean conversion if necessary, though String.valueOf should generally work for prepared statements
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
            String queryInsert = getQueryToInsertDataToServerDB(schemaName, syncTableName, serverColumns);
            try {
                int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName, serverColumns, queryInsert, syncDataListInsert);
                if (i.length != syncDataListInsert.size()) {
                    insertSuccess = false;
                    logger.error("Partial insert for table {}. Expected {} inserts, got {}. Failed records: {}", syncTableName, syncDataListInsert.size(), i.length, getFailedRecords(i, syncDataListInsert));
                } else {
                    logger.info("Successfully inserted {} records into table {}.", i.length, syncTableName);
                }
            } catch (Exception e) {
                insertSuccess = false;
                logger.error("Exception during insert for table {}: {}", syncTableName, e.getMessage(), e);
            }
        }

        if (!syncDataListUpdate.isEmpty()) {
            String queryUpdate = getQueryToUpdateDataToServerDB(schemaName, serverColumns, syncTableName);
            try {
                int[] j = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName, SERVER_COLUMNS_NOT_REQUIRED, queryUpdate, syncDataListUpdate);
                if (j.length != syncDataListUpdate.size()) {
                    updateSuccess = false;
                    logger.error("Partial update for table {}. Expected {} updates, got {}. Failed records: {}", syncTableName, syncDataListUpdate.size(), j.length, getFailedRecords(j, syncDataListUpdate));
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
        if (!isValidSchemaName(schemaName) || !isValidTableName(tableName)) {
            logger.error("Invalid schema or table name for getQueryToInsertDataToServerDB: Schema='{}', Table='{}'.", schemaName, tableName);
            throw new IllegalArgumentException("Invalid schema or table name provided.");
        }
        if (!isValidColumnNames(serverColumns)) {
            logger.error("Invalid server columns provided for getQueryToInsertDataToServerDB: {}", serverColumns);
            throw new IllegalArgumentException("Invalid column names provided.");
        }


        String[] columnsArr = serverColumns.split(",");
        StringBuilder preparedStatementSetter = new StringBuilder();

        for (int i = 0; i < columnsArr.length; i++) {
            preparedStatementSetter.append("?");
            if (i < columnsArr.length - 1) {
                preparedStatementSetter.append(", ");
            }
        }

        return String.format("INSERT INTO %s.%s(%s) VALUES (%s)", schemaName, tableName, serverColumns, preparedStatementSetter.toString());
    }

    public String getQueryToUpdateDataToServerDB(String schemaName, String serverColumns, String tableName) {
        if (!isValidSchemaName(schemaName) || !isValidTableName(tableName)) {
            logger.error("Invalid schema or table name for getQueryToUpdateDataToServerDB: Schema='{}', Table='{}'.", schemaName, tableName);
            throw new IllegalArgumentException("Invalid schema or table name provided.");
        }
        if (!isValidColumnNames(serverColumns)) {
            logger.error("Invalid server columns provided for getQueryToUpdateDataToServerDB: {}", serverColumns);
            throw new IllegalArgumentException("Invalid column names provided.");
        }

        String[] columnsArr = serverColumns.split(",");
        StringBuilder preparedStatementSetter = new StringBuilder();

        for (int i = 0; i < columnsArr.length; i++) {
            String column = columnsArr[i].trim();
            if (!isValidColumnName(column)) {
                 logger.error("Invalid individual column name encountered: {}", column);
                 throw new IllegalArgumentException("Invalid individual column name provided: " + column);
            }

            preparedStatementSetter.append(column);
            preparedStatementSetter.append(" = ?");
            if (i < columnsArr.length - 1) {
                preparedStatementSetter.append(", ");
            }
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("UPDATE %s.%s SET %s WHERE VanSerialNo = ?", schemaName, tableName, preparedStatementSetter.toString()));


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

    private boolean isValidSchemaName(String schemaName) {
        return VALID_SCHEMAS.contains(schemaName.toLowerCase());
    }

    private boolean isValidTableName(String tableName) {
        return VALID_TABLES.contains(tableName.toLowerCase());
    }

    private boolean isValidColumnName(String columnName) {
        return columnName != null && columnName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    private boolean isValidColumnNames(String columnNames) {
        if (columnNames == null || columnNames.trim().isEmpty()) {
            return false; 
        }
        String[] cols = columnNames.split(",");
        for (String col : cols) {
            if (!isValidColumnName(col.trim())) {
                return false;
            }
        }
        return true;
    }


    private String getFailedRecords(int[] results, List<Object[]> data) {
        List<String> failedRecordsInfo = new ArrayList<>();
        for (int k = 0; k < results.length; k++) {
            if (results[k] < 1) { 
                String idInfo = "N/A";
                if (data.get(k) != null && data.get(k).length > 0) {
                  idInfo = "Record data size: " + data.get(k).length;
                }
                failedRecordsInfo.add("Record at index " + k + " (Info: " + idInfo + ")");
            }
        }
        return String.join("; ", failedRecordsInfo);
    }
}