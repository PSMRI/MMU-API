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

            logger.info("Data to be synced: {}", dataToBesync);
            if (syncUploadDataDigester == null || syncUploadDataDigester.getTableName() == null) {
                logger.error("Invalid SyncUploadDataDigester object or tableName is null.");
                return "Error: Invalid sync request.";
            }

            String syncTableName = syncUploadDataDigester.getTableName();
            logger.info("Syncing data for table: {}", syncTableName);
            // Handle specific tables first, if their logic is distinct
            if ("m_beneficiaryregidmapping".equalsIgnoreCase(syncTableName)) {
                String result = update_M_BeneficiaryRegIdMapping_for_provisioned_benID(syncUploadDataDigester, syncResults);
                if ("data sync passed".equals(result)) {
                    return "Sync successful for m_beneficiaryregidmapping.";
                } else {
                    logger.error("Sync failed for m_beneficiaryregidmapping: {}", result);
                    return "Sync failed for m_beneficiaryregidmapping.";
                }
            }
            else {
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

                // if (syncSuccess) {
                //     return "Overall data sync passed.";
                // } else {
                //     return "Overall data sync failed. Details: " + errorMessage;
                // }
                Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("statusCode", 200);
    responseMap.put("message", "Data sync completed");
    responseMap.put("records", syncResults);
    logger.info("Response = "+responseMap);
    logger.info("Sync Results = "+syncResults);
    return new ObjectMapper().writeValueAsString(responseMap);

            }
        }

        private boolean syncTablesInGroup(String schemaName, String currentTableName,
                SyncUploadDataDigester originalDigester, List<SyncResult> syncResults) {
            SyncUploadDataDigester tableSpecificDigester = new SyncUploadDataDigester();
            tableSpecificDigester.setSchemaName(schemaName);
            tableSpecificDigester.setTableName(currentTableName);
            tableSpecificDigester.setSyncedBy(originalDigester.getSyncedBy());
            tableSpecificDigester.setFacilityID(originalDigester.getFacilityID());
            tableSpecificDigester.setVanAutoIncColumnName(originalDigester.getVanAutoIncColumnName());
            tableSpecificDigester.setServerColumns(originalDigester.getServerColumns());
            tableSpecificDigester.setSyncData(originalDigester.getSyncData());
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

        /**
         * Handles the generic synchronization logic for tables not covered by specific
         * handlers.
         */

//         private boolean performGenericTableSync(SyncUploadDataDigester syncUploadDataDigester, List<SyncResult> syncResults) {
//         List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
//         List<Object[]> syncDataListInsert = new ArrayList<>();
//         List<Object[]> syncDataListUpdate = new ArrayList<>();
//         // List<SyncResult> syncResults = new ArrayList<>();
        
//         boolean overallSuccess = true;

//         if (dataToBesync == null || dataToBesync.isEmpty()) {
//             logger.info("No data to sync for table: {}", syncUploadDataDigester.getTableName());
//             return true; // Nothing to sync, consider it a success
//         }

//         String syncTableName = syncUploadDataDigester.getTableName();
//         String vanAutoIncColumnName = syncUploadDataDigester.getVanAutoIncColumnName();
//         String schemaName = syncUploadDataDigester.getSchemaName();
//         Integer facilityIDFromDigester = syncUploadDataDigester.getFacilityID();
//         String serverColumns = syncUploadDataDigester.getServerColumns();

//     int vanSerialIndex = Arrays.asList(serverColumns.split(",")).indexOf(vanAutoIncColumnName);

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
//                         // Fallback if format is unexpected
//                         cleanKey = key.substring(start, key.indexOf(")")).trim();
//                     }
//                 }
//                 cleanRecord.put(cleanKey.trim(), map.get(key));
//             }

//             String vanSerialNo = String.valueOf(cleanRecord.get(vanAutoIncColumnName));
//             String vanID = String.valueOf(cleanRecord.get("VanID"));
//             int syncFacilityID = 0;

//             // Update SyncedBy and SyncedDate in the xmap itself before processing
//             cleanRecord.put("SyncedBy", syncUploadDataDigester.getSyncedBy());
//             cleanRecord.put("SyncedDate", String.valueOf(LocalDateTime.now()));

//             if (facilityIDFromDigester != null) {
//                 // Determine the 'Processed' status based on facility ID for specific tables
//                 switch (syncTableName.toLowerCase()) {
//                     case "t_indent":
//                     case "t_indentorder": {
//                         if (cleanRecord.containsKey("FromFacilityID") && cleanRecord.get("FromFacilityID") instanceof Number) {
//                             Number fromFacilityID = (Number) cleanRecord.get("FromFacilityID");
//                             if (fromFacilityID.intValue() == facilityIDFromDigester) {
//                                 cleanRecord.put("Processed", "P");
//                             }
//                         }
//                         break;
//                     }
//                     case "t_indentissue": {
//                         if (cleanRecord.containsKey("ToFacilityID") && cleanRecord.get("ToFacilityID") instanceof Number) {
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
//                         // No specific facility ID logic for other tables
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
//                         logger.info("Test Record check="+recordCheck);
//                         logger.info("Test all the data="+ schemaName +":: Tble="+ syncTableName+":: vanSerialNo="+vanSerialNo+":: vanID="+vanID+":: vanAutoIncColumnName="+vanAutoIncColumnName+":: syncFacilityID="+syncFacilityID);
//             } catch (Exception e) {
//                 logger.error("Error checking record existence for table {}: VanSerialNo={}, VanID={}. Error: {}",
//                         syncTableName, vanSerialNo, vanID, e.getMessage(), e);
//                 return false; // Critical error, stop sync for this table
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
//             logger.info("Test Obj Arr {}",objArr);
//             if (recordCheck == 0) {
//                 syncDataListInsert.add(objArr);
//                 syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo, syncUploadDataDigester.getSyncedBy(), true, null));
//             } else {
//                 // For update, append the WHERE clause parameters at the end of the array
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
//                 syncDataListUpdate.add(updateParams.toArray());
//                 syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo, syncUploadDataDigester.getSyncedBy(), true, null));

//             }
//         }

//         boolean insertSuccess = true;
//         boolean updateSuccess = true;

//         if (!syncDataListInsert.isEmpty()) {
//             String queryInsert = getQueryToInsertDataToServerDB(schemaName, syncTableName, serverColumns);
//     logger.info("Query Insert="+queryInsert);
//     try {
//         int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName, serverColumns, queryInsert, syncDataListInsert);
//         for (int k = 0; k < i.length; k++) {
//             if (i[k] < 1) {
//                 syncResults.set(k, new SyncResult(schemaName, syncTableName,
//                     String.valueOf(syncDataListInsert.get(k)[vanSerialIndex]), // VanSerialNo position
//                     syncUploadDataDigester.getSyncedBy(), false, "Insert failed"));
//             }
//         }
//     } 
//     catch (Exception e) {
//     String shortReason;
//     if (e.getMessage() != null) {
//         if (e.getMessage().toLowerCase().contains("duplicate")) {
//             shortReason = "Duplicate entry";
//         } else if (e.getMessage().toLowerCase().contains("constraint")) {
//             shortReason = "Constraint violation";
//         } else if (e.getMessage().toLowerCase().contains("timeout")) {
//             shortReason = "DB timeout";
//         } else {
//             shortReason = "Insert/Update failed";
//         }
//     } else {
//         shortReason = "Unknown DB error";
//     }
//     // Always add, never set, to avoid index errors
//     for (int k = 0; k < syncDataListInsert.size(); k++) {
//         syncResults.add(new SyncResult(schemaName, syncTableName,
//             String.valueOf(syncDataListInsert.get(k)[vanSerialIndex]),
//             syncUploadDataDigester.getSyncedBy(), false, shortReason));
//     }
// }

//             // try {
//             //     int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
//             //             serverColumns, queryInsert, syncDataListInsert);
//             //             logger.info("Insert result array length: {}", i.length);
//             //             logger.info("Expected insert size: {}", syncDataListInsert.size());
//             //     if (i.length != syncDataListInsert.size()) {
//             //         insertSuccess = false;
//             //         logger.error("Partial insert for table {}. Expected {} inserts, got {}. Failed records: {}",
//             //                 syncTableName, syncDataListInsert.size(), i.length,
//             //                 getFailedRecords(i, syncDataListInsert));
//             //     } else {
//             //         logger.info("Successfully inserted {} records into table {}.", i.length, syncTableName);
//             //     }
//             // } catch (Exception e) {
//             //     insertSuccess = false;
//             //     logger.error("Get failed records="+getFailedRecords(new int[] {}, syncDataListInsert));
//             //     logger.error("Exception during insert for table {}: {}", syncTableName, e.getMessage(), e);
//             // }
//         }

//         if (!syncDataListUpdate.isEmpty()) {
//             String queryUpdate = getQueryToUpdateDataToServerDB(schemaName, serverColumns, syncTableName);
//             logger.info("Query Update="+queryUpdate);
//             try {
//         int[] i = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName, serverColumns, queryUpdate, syncDataListUpdate);
//         logger.info("Test Update result array length: {}", i.length);
//         logger.info("Test Expected update size: {}", syncDataListUpdate.size());
//         for (int k = 0; k < i.length; k++) {
//             if (i[k] < 1) {
//                 syncResults.set(k, new SyncResult(schemaName, syncTableName,
//                     String.valueOf(syncDataListUpdate.get(k)[vanSerialIndex]), // VanSerialNo position
//                     syncUploadDataDigester.getSyncedBy(), false, "Update failed"));
//             }
//         }
//     } 
//     catch (Exception e) {
//     String shortReason;
//     if (e.getMessage() != null) {
//         if (e.getMessage().toLowerCase().contains("duplicate")) {
//             shortReason = "Duplicate entry";
//         } else if (e.getMessage().toLowerCase().contains("constraint")) {
//             shortReason = "Constraint violation";
//         } else if (e.getMessage().toLowerCase().contains("timeout")) {
//             shortReason = "DB timeout";
//         } else {
//             shortReason = "Insert/Update failed";
//         }
//     } else {
//         shortReason = "Unknown DB error";
//     }
//     for (int k = 0; k < syncDataListUpdate.size(); k++) {
//         syncResults.add(new SyncResult(schemaName, syncTableName,
//             String.valueOf(syncDataListUpdate.get(k)[vanSerialIndex]),
//             syncUploadDataDigester.getSyncedBy(), false, shortReason));
//     }
// }

//             // try {
//             //     int[] j = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName,
//             //             SERVER_COLUMNS_NOT_REQUIRED, queryUpdate, syncDataListUpdate);
//             //             logger.info("Update result array length: {}", j.length);
//             //             logger.info("Expected update size: {}", syncDataListUpdate.size());
//             //     if (j.length != syncDataListUpdate.size()) {
//             //         updateSuccess = false;
//             //         logger.error("Partial update for table {}. Expected {} updates, got {}. Failed records: {}",
//             //                 syncTableName, syncDataListUpdate.size(), j.length,
//             //                 getFailedRecords(j, syncDataListUpdate));
//             //     } else {
//             //         logger.info("Successfully updated {} records in table {}.", j.length, syncTableName);
//             //     }
//             // } catch (Exception e) {
//             //     updateSuccess = false;
//             //     logger.error("Get failed records="+getFailedRecords(new int[] {}, syncDataListUpdate));
//             //     logger.error("Exception during update for table {}: {}", syncTableName, e.getMessage(), e);
//             // }
//         }
//         logger.info("Sync results for table {}: {}", syncTableName, syncResults);
//         return insertSuccess && updateSuccess;
//     }

private boolean performGenericTableSync(SyncUploadDataDigester syncUploadDataDigester, List<SyncResult> syncResults) {
    List<Map<String, Object>> dataToBesync = syncUploadDataDigester.getSyncData();
    List<Object[]> syncDataListInsert = new ArrayList<>();
    List<Object[]> syncDataListUpdate = new ArrayList<>();
    
    // Track indices for insert and update operations
    Map<Integer, Integer> insertIndexMap = new HashMap<>(); // syncResults index -> insert list index
    Map<Integer, Integer> updateIndexMap = new HashMap<>(); // syncResults index -> update list index
    
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
        // Create a new map with clean column names as keys
        Map<String, Object> cleanRecord = new HashMap<>();
        for (String key : map.keySet()) {
            String cleanKey = key;
            // Handle keys with SQL functions like date_format
            if (key.startsWith("date_format(") && key.endsWith(")")) {
                int start = key.indexOf("(") + 1;
                int end = key.indexOf(",");
                if (end > start) {
                    cleanKey = key.substring(start, end).trim();
                } else {
                    cleanKey = key.substring(start, key.indexOf(")")).trim();
                }
            }
            cleanRecord.put(cleanKey.trim(), map.get(key));
        }

        String vanSerialNo = String.valueOf(cleanRecord.get(vanAutoIncColumnName));
        String vanID = String.valueOf(cleanRecord.get("VanID"));
        int syncFacilityID = 0;

        // Update SyncedBy and SyncedDate in the cleanRecord
        cleanRecord.put("SyncedBy", syncUploadDataDigester.getSyncedBy());
        cleanRecord.put("SyncedDate", String.valueOf(LocalDateTime.now()));

        if (facilityIDFromDigester != null) {
            // Determine the 'Processed' status based on facility ID for specific tables
            switch (syncTableName.toLowerCase()) {
                case "t_indent":
                case "t_indentorder": {
                    if (cleanRecord.containsKey("FromFacilityID") && cleanRecord.get("FromFacilityID") instanceof Number) {
                        Number fromFacilityID = (Number) cleanRecord.get("FromFacilityID");
                        if (fromFacilityID.intValue() == facilityIDFromDigester) {
                            cleanRecord.put("Processed", "P");
                        }
                    }
                    break;
                }
                case "t_indentissue": {
                    if (cleanRecord.containsKey("ToFacilityID") && cleanRecord.get("ToFacilityID") instanceof Number) {
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
                    if (cleanRecord.containsKey("FacilityID") && cleanRecord.get("FacilityID") instanceof Number) {
                        Number mapFacilityID = (Number) cleanRecord.get("FacilityID");
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

        // Extract SyncFacilityID for checkRecordIsAlreadyPresentOrNot
        if (cleanRecord.containsKey("SyncFacilityID") && cleanRecord.get("SyncFacilityID") instanceof Number) {
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
            
            // Add failed result for this record
            syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo, 
                    syncUploadDataDigester.getSyncedBy(), false, "Record check failed"));
            continue; // Skip to next record
        }

        // Prepare Object array for insert/update
        List<Object> currentRecordValues = new ArrayList<>();
        for (String column : serverColumnsList) {
            Object value = cleanRecord.get(column.trim());
            if (value instanceof Boolean) {
                currentRecordValues.add(value);
            } else if (value != null) {
                currentRecordValues.add(String.valueOf(value));
            } else {
                currentRecordValues.add(null);
            }
        }

        Object[] objArr = currentRecordValues.toArray();
        
        // Add to syncResults first, then track the index
        int currentSyncResultIndex = syncResults.size();
        syncResults.add(new SyncResult(schemaName, syncTableName, vanSerialNo, 
                syncUploadDataDigester.getSyncedBy(), true, null)); // Initially set as success
        
        if (recordCheck == 0) {
            // Record doesn't exist - INSERT
            insertIndexMap.put(currentSyncResultIndex, syncDataListInsert.size());
            syncDataListInsert.add(objArr);
        } else {
            // Record exists - UPDATE
            List<Object> updateParams = new ArrayList<>(Arrays.asList(objArr));
            updateParams.add(String.valueOf(vanSerialNo));

            if (Arrays.asList("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
                    "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                    "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit")
                    .contains(syncTableName.toLowerCase()) && cleanRecord.containsKey("SyncFacilityID")) {
                updateParams.add(String.valueOf(cleanRecord.get("SyncFacilityID")));
            } else {
                updateParams.add(String.valueOf(vanID));
            }
            
            updateIndexMap.put(currentSyncResultIndex, syncDataListUpdate.size());
            syncDataListUpdate.add(updateParams.toArray());
        }
    }

    boolean insertSuccess = true;
    boolean updateSuccess = true;

    // Process INSERT operations
    if (!syncDataListInsert.isEmpty()) {
        String queryInsert = getQueryToInsertDataToServerDB(schemaName, syncTableName, serverColumns);
        logger.info("Insert Query: {}", queryInsert);
        
        try {
            int[] insertResults = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName, 
                    serverColumns, queryInsert, syncDataListInsert);
            
            // Update syncResults based on insert results
            for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int insertListIndex = entry.getValue();
                
                if (insertListIndex < insertResults.length && insertResults[insertListIndex] > 0) {
                    // Success - keep the existing success entry
                    logger.info("Successfully inserted record at index {}", insertListIndex);
                } else {
                    // Failed - update the syncResults entry
                    String vanSerialNo = String.valueOf(syncDataListInsert.get(insertListIndex)[vanSerialIndex]);
                    syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
                            syncUploadDataDigester.getSyncedBy(), false, "Insert failed"));
                    insertSuccess = false;
                }
            }
            
        } catch (Exception e) {
            insertSuccess = false;
            logger.error("Exception during insert for table {}: {}", syncTableName, e.getMessage(), e);
            
            String shortReason = getShortErrorReason(e);
            
            // Update all insert-related syncResults to failed
            for (Map.Entry<Integer, Integer> entry : insertIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int insertListIndex = entry.getValue();
                String vanSerialNo = String.valueOf(syncDataListInsert.get(insertListIndex)[vanSerialIndex]);
                
                syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
                        syncUploadDataDigester.getSyncedBy(), false, shortReason));
            }
        }
    }

    // Process UPDATE operations
    if (!syncDataListUpdate.isEmpty()) {
        String queryUpdate = getQueryToUpdateDataToServerDB(schemaName, serverColumns, syncTableName);
        logger.info("Update Query: {}", queryUpdate);
        
        try {
            int[] updateResults = dataSyncRepositoryCentral.syncDataToCentralDB(schemaName, syncTableName, 
                    serverColumns, queryUpdate, syncDataListUpdate);
            
            // Update syncResults based on update results
            for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int updateListIndex = entry.getValue();
                
                if (updateListIndex < updateResults.length && updateResults[updateListIndex] > 0) {
                    // Success - keep the existing success entry
                    logger.info("Successfully updated record at index {}", updateListIndex);
                } else {
                    // Failed - update the syncResults entry
                    String vanSerialNo = String.valueOf(syncDataListUpdate.get(updateListIndex)[vanSerialIndex]);
                    syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
                            syncUploadDataDigester.getSyncedBy(), false, "Update failed"));
                    updateSuccess = false;
                }
            }
            
        } catch (Exception e) {
            updateSuccess = false;
            logger.error("Exception during update for table {}: {}", syncTableName, e.getMessage(), e);
            
            String shortReason = getShortErrorReason(e);
            
            // Update all update-related syncResults to failed
            for (Map.Entry<Integer, Integer> entry : updateIndexMap.entrySet()) {
                int syncResultIndex = entry.getKey();
                int updateListIndex = entry.getValue();
                String vanSerialNo = String.valueOf(syncDataListUpdate.get(updateListIndex)[vanSerialIndex]);
                
                syncResults.set(syncResultIndex, new SyncResult(schemaName, syncTableName, vanSerialNo,
                        syncUploadDataDigester.getSyncedBy(), false, shortReason));
            }
        }
    }

    logger.info("Sync results for table {}: {}", syncTableName, syncResults);
    return insertSuccess && updateSuccess;
}

// Helper method to get short error reason
private String getShortErrorReason(Exception e) {
    if (e.getMessage() != null) {
        String message = e.getMessage().toLowerCase();
        if (message.contains("duplicate")) {
            return "Duplicate entry";
        } else if (message.contains("constraint")) {
            return "Constraint violation";
        } else if (message.contains("timeout")) {
            return "DB timeout";
        } else {
            return "Database error";
        }
    } else {
        return "Unknown error";
    }
}
        private String getQueryToInsertDataToServerDB(String schemaName, String
        tableName, String serverColumns) {
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
        logger.info("Test Query Builder: {}", queryBuilder.toString());
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