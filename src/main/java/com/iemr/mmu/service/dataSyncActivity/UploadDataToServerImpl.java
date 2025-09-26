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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iemr.mmu.data.syncActivity_syncLayer.DataSyncGroups;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUtilityClass;
import com.iemr.mmu.repo.login.MasterVanRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.DataSyncGroupsRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.SyncUtilityClassRepo;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.RestTemplateUtil;

import jakarta.servlet.http.HttpServletRequest;

/***
 * 
 * @author NE298657
 * @date 16-08-2018
 * @purpose "This service is user for data sync activity from van side. Means
 *          taking unprocessed data from van and sync to server and based on
 *          success or failure update local tables processed flag"
 *
 */
@Service
@PropertySource("classpath:application.properties")
public class UploadDataToServerImpl implements UploadDataToServer {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	// rest URLs from server to consume local van data and to sync to DB server
	@Value("${dataSyncUploadUrl}")
	private String dataSyncUploadUrl;

	@Value("${BATCH_SIZE}")
	private int BATCH_SIZE;

	@Autowired
	private DataSyncRepository dataSyncRepository;
	@Autowired
	private DataSyncGroupsRepo dataSyncGroupsRepo;
	@Autowired
	private MasterVanRepo masterVanRepo;

	@Autowired
	private SyncUtilityClassRepo syncutilityClassRepo;
	@Autowired
	private CookieUtil cookieUtil;
boolean criticalTableFailure = false; // Add this flag

	/**
	 * 
	 * @param groupName
	 * @param Authorization
	 * @return
	 */
	public String getDataToSyncToServer(int vanID, String user, String Authorization, String token) throws Exception {
		String syncData = null;
		syncData = syncIntercepter(vanID, user, Authorization, token);
		return syncData;
	}

	/**
	 * 
	 * @param Authorization
	 * @return
	 */
	public String syncIntercepter(int vanID, String user, String Authorization, String token) throws Exception {
		// sync activity trigger
		String serverAcknowledgement = startDataSync(vanID, user, Authorization, token);
		return serverAcknowledgement;
	}

	/**
	 * Enhanced startDataSync method with table-level and group-level tracking
	 * 
	 * @param syncTableDetailsIDs
	 * @param Authorization
	 * @return
	 */
	private String startDataSync(int vanID, String user, String Authorization, String token) throws Exception {
		String serverAcknowledgement = null;
		List<Map<String, Object>> responseStatus = new ArrayList<>();
		boolean hasSyncFailed = false;
		ObjectMapper objectMapper = new ObjectMapper();

		// fetch group masters
		List<DataSyncGroups> dataSyncGroupList = dataSyncGroupsRepo.findByDeleted(false);
		logger.debug("Fetched DataSyncGroups: {}",
				objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataSyncGroupList));

		for (DataSyncGroups dataSyncGroups : dataSyncGroupList) {
			int groupId = dataSyncGroups.getSyncTableGroupID();
			String groupName = dataSyncGroups.getSyncTableGroupName(); // Get group name if available

			List<SyncUtilityClass> syncUtilityClassList = getVanAndServerColumns(groupId);
			logger.debug("Fetched SyncUtilityClass for groupId {}: {}", groupId,
					objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(syncUtilityClassList));

			// Track table-level results for this group
			List<Map<String, Object>> tableDetailsList = new ArrayList<>();
			boolean groupHasFailures = false;

	for (SyncUtilityClass obj : syncUtilityClassList) {
    String tableKey = obj.getSchemaName() + "." + obj.getTableName();
    boolean tableHasError = false;

    // get data from DB to sync to server
    List<Map<String, Object>> syncData = getDataToSync(obj.getSchemaName(), obj.getTableName(),
            obj.getVanColumnName());
    logger.debug("Fetched syncData for schema {} and table {}: {}", obj.getSchemaName(), obj.getTableName(),
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(syncData));

    if (syncData != null && syncData.size() > 0) {
        int dataSize = syncData.size();
        int startIndex = 0;
        int fullBatchCount = dataSize / BATCH_SIZE;
        int remainder = dataSize % BATCH_SIZE;

        // Track table-level success/failure counts
        int totalRecords = dataSize;
        int successfulRecords = 0;
        int failedRecords = 0;
        List<String> tableFailureReasons = new ArrayList<>(); 

        logger.info("Starting batch sync for schema: {}, table: {} with {} full batches and {} remainder",
                obj.getSchemaName(), obj.getTableName(), fullBatchCount, remainder);

        // Process full batches
        for (int i = 0; i < fullBatchCount && !tableHasError; i++) {
            List<Map<String, Object>> syncDataBatch = getBatchOfAskedSizeDataToSync(syncData, startIndex,
                    BATCH_SIZE);
            
            // Updated to handle Map<String, Object> return type
            Map<String, Object> syncResult = syncDataToServer(vanID, obj.getSchemaName(), obj.getTableName(),
                    obj.getVanAutoIncColumnName(), obj.getServerColumnName(), syncDataBatch, user,
                    Authorization, token);
            
            if (syncResult == null) {
                logger.error("Sync failed for batch {} in schema: {}, table: {}", i, obj.getSchemaName(),
                        obj.getTableName());
                tableHasError = true;
                failedRecords += syncDataBatch.size();
                groupHasFailures = true;
                break;
            }
            
            String status = (String) syncResult.get("status");
            int batchSuccessCount = (Integer) syncResult.get("successCount");
            int batchFailCount = (Integer) syncResult.get("failCount");
            @SuppressWarnings("unchecked")
            List<String> batchFailureReasons = (List<String>) syncResult.get("failureReasons");
            
            successfulRecords += batchSuccessCount;
            failedRecords += batchFailCount;
            
            if (batchFailureReasons != null && !batchFailureReasons.isEmpty()) {
                tableFailureReasons.addAll(batchFailureReasons);
                groupHasFailures = true;
            }
            
            if (status.equals("Sync failed")) {
                tableHasError = true;
                break;
            }

            startIndex += BATCH_SIZE;
        }

        if (!tableHasError && remainder > 0) {
            List<Map<String, Object>> syncDataBatch = getBatchOfAskedSizeDataToSync(syncData, startIndex,
                    remainder);
            
            Map<String, Object> syncResult = syncDataToServer(vanID, obj.getSchemaName(), obj.getTableName(),
                    obj.getVanAutoIncColumnName(), obj.getServerColumnName(), syncDataBatch, user,
                    Authorization, token);

            if (syncResult == null) {
                logger.error("Sync failed for remaining data in schema: {}, table: {}", obj.getSchemaName(),
                        obj.getTableName());
                failedRecords += syncDataBatch.size();
                groupHasFailures = true;
            } else {
                String status = (String) syncResult.get("status");
                int batchSuccessCount = (Integer) syncResult.get("successCount");
                int batchFailCount = (Integer) syncResult.get("failCount");
                @SuppressWarnings("unchecked")
                List<String> batchFailureReasons = (List<String>) syncResult.get("failureReasons");
                
                successfulRecords += batchSuccessCount;
                failedRecords += batchFailCount;
                
                if (batchFailureReasons != null && !batchFailureReasons.isEmpty()) {
                    tableFailureReasons.addAll(batchFailureReasons);
                    groupHasFailures = true;
                }
                
                if (status.equals("Sync failed")) {
                    groupHasFailures = true;
                }
            }
        }

        // Determine table status based on success/failure counts
        // String tableStatus;
        // if (successfulRecords == totalRecords && failedRecords == 0) {
        //     tableStatus = "success";
        // } else if (failedRecords == totalRecords && successfulRecords == 0) {
        //     tableStatus = "failed";
        //     groupHasFailures = true;
        // } else if (successfulRecords > 0 && failedRecords > 0) {
        //     tableStatus = "partial";
        // } else {
        //     tableStatus = "failed"; // Default to failed if unclear
        //     groupHasFailures = true;
        // }

		String tableStatus;

if (successfulRecords == totalRecords && failedRecords == 0) {
    tableStatus = "success";
} else if (failedRecords == totalRecords && successfulRecords == 0) {
    tableStatus = "failed";
    criticalTableFailure = true; // Only critical failures stop sync
    groupHasFailures = true;
} else if (successfulRecords > 0 && failedRecords > 0) {
    tableStatus = "partial";
    groupHasFailures = true; // Group has issues but don't stop sync
} else {
    tableStatus = "failed";
    criticalTableFailure = true; // Complete failure is critical
    groupHasFailures = true;
}

        // Create detailed table info with failure reasons
        Map<String, Object> tableDetails = new HashMap<>();
        tableDetails.put("tableName", obj.getTableName());
        tableDetails.put("schemaName", obj.getSchemaName());
        tableDetails.put("status", tableStatus);
        tableDetails.put("totalRecords", totalRecords);
        tableDetails.put("successfulRecords", successfulRecords);
        tableDetails.put("failedRecords", failedRecords);
        
        // Add failure reasons only if there are any failures
        if (!tableFailureReasons.isEmpty()) {
            tableDetails.put("failureReasons", tableFailureReasons);
        }
        
        tableDetailsList.add(tableDetails);

        logger.info("Table sync summary - {}: {} (Success: {}, Failed: {}, Total: {}, Failure Reasons: {})",
                tableKey, tableStatus, successfulRecords, failedRecords, totalRecords, 
                tableFailureReasons.isEmpty() ? "None" : tableFailureReasons);

    } else {
        logger.info("No data to sync for schema {} and table {}", obj.getSchemaName(), obj.getTableName());

        Map<String, Object> tableDetails = new HashMap<>();
        tableDetails.put("tableName", obj.getTableName());
        tableDetails.put("schemaName", obj.getSchemaName());
        tableDetails.put("status", "no_data");
        tableDetails.put("totalRecords", 0);
        tableDetails.put("successfulRecords", 0);
        tableDetails.put("failedRecords", 0);
        tableDetailsList.add(tableDetails);
    }

    // If this table had critical failures, stop processing this group
    // if (tableHasError) {
    //     hasSyncFailed = true;
    //     break;
    // }
	if (criticalTableFailure) {
    hasSyncFailed = true;
    break;
}
}
			// Determine overall group status
			String groupStatus;
			long successTables = tableDetailsList.stream()
					.filter(table -> "success".equals(table.get("status")) || "no_data".equals(table.get("status")))
					.count();
			long partialTables = tableDetailsList.stream()
					.filter(table -> "partial".equals(table.get("status")))
					.count();
			long failedTables = tableDetailsList.stream()
					.filter(table -> "failed".equals(table.get("status")))
					.count();

			if (failedTables == 0 && partialTables == 0) {
				groupStatus = "completed";
			} else if (failedTables > 0 && successTables == 0 && partialTables == 0) {
				groupStatus = "failed";
			} else {
				groupStatus = "partial";
			}

			// Create group response
			Map<String, Object> groupResponse = new HashMap<>();
			groupResponse.put("syncTableGroupID", groupId);
			groupResponse.put("syncTableGroupName", groupName != null ? groupName : "Group " + groupId);
			groupResponse.put("status", groupStatus);
			groupResponse.put("tables", tableDetailsList);
			groupResponse.put("summary", Map.of(
					"totalTables", tableDetailsList.size(),
					"successfulTables", successTables,
					"partialTables", partialTables,
					"failedTables", failedTables));

			responseStatus.add(groupResponse);

			if (hasSyncFailed) {
				// Mark all subsequent groups as "pending"
				for (int j = dataSyncGroupList.indexOf(dataSyncGroups) + 1; j < dataSyncGroupList.size(); j++) {
					DataSyncGroups remainingGroup = dataSyncGroupList.get(j);
					Map<String, Object> pendingGroupResponse = new HashMap<>();
					pendingGroupResponse.put("syncTableGroupID", remainingGroup.getSyncTableGroupID());
					pendingGroupResponse.put("syncTableGroupName",
							remainingGroup.getSyncTableGroupName() != null ? remainingGroup.getSyncTableGroupName()
									: "Group " + remainingGroup.getSyncTableGroupID());
					pendingGroupResponse.put("status", "pending");
					pendingGroupResponse.put("tables", new ArrayList<>());
					pendingGroupResponse.put("summary", Map.of(
							"totalTables", 0,
							"successfulTables", 0L,
							"partialTables", 0L,
							"failedTables", 0L));
					responseStatus.add(pendingGroupResponse);
				}
				break;
			}
		}

		// Create final response
		Map<String, Object> finalResponse = new HashMap<>();
		if (hasSyncFailed) {
			finalResponse.put("response", "Data sync failed");
			finalResponse.put("groupsProgress", responseStatus);
			return objectMapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(finalResponse);
		} else {
			// Check if there was any data to sync
			boolean hasData = responseStatus.stream()
					.anyMatch(group -> {
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> tables = (List<Map<String, Object>>) ((Map<String, Object>) group)
								.get("tables");
						return tables.stream().anyMatch(table -> !("no_data".equals(table.get("status"))));
					});

			if (!hasData) {
				return "No data to sync";
			} else {
				finalResponse.put("response", "Data sync completed");
				finalResponse.put("groupsProgress", responseStatus);
				return objectMapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(finalResponse);
			}
		}
	}

	private List<SyncUtilityClass> getVanAndServerColumns(Integer groupID) throws Exception {
		List<SyncUtilityClass> syncUtilityClassList = getVanAndServerColumnList(groupID);
		logger.debug("Fetched SyncUtilityClass list for groupID {}: {}", groupID, syncUtilityClassList);
		return syncUtilityClassList;
	}

	public List<SyncUtilityClass> getVanAndServerColumnList(Integer groupID) throws Exception {
		List<SyncUtilityClass> syncUtilityClassList = syncutilityClassRepo
				.findBySyncTableGroupIDAndDeletedOrderBySyncTableDetailID(groupID, false);
		logger.debug("Fetched SyncUtilityClass list from repository for groupID {}: {}", groupID, syncUtilityClassList);
		return syncUtilityClassList;
	}

	private List<Map<String, Object>> getDataToSync(String schemaName, String tableName, String columnNames)
			throws Exception {
		logger.info("Fetching data to sync for schema: {}, table: {}, columns: {}", schemaName, tableName, columnNames);
		List<Map<String, Object>> resultSetList = dataSyncRepository.getDataForGivenSchemaAndTable(schemaName,
				tableName, columnNames);
		if (resultSetList != null) {
			logger.debug("Fetched {} records for schema '{}', table '{}'", resultSetList.size(), schemaName, tableName);
			if (!resultSetList.isEmpty()) {
				logger.debug("Sample record: {}", resultSetList.get(0));
			}
		} else {
			logger.debug("No records found for schema '{}', table '{}'", schemaName, tableName);
		}
		return resultSetList;
	}

	private List<Map<String, Object>> getBatchOfAskedSizeDataToSync(List<Map<String, Object>> syncData, int startIndex,
			int size) throws Exception {
		List<Map<String, Object>> syncDataOfBatchSize = syncData.subList(startIndex, (startIndex + size));
		return syncDataOfBatchSize;
	}

	public Map<String, Object> syncDataToServer(int vanID, String schemaName, String tableName, String vanAutoIncColumnName,
		String serverColumns, List<Map<String, Object>> dataToBesync, String user, String Authorization,
		String token) throws Exception {

	RestTemplate restTemplate = new RestTemplate();
	Integer facilityID = masterVanRepo.getFacilityID(vanID);

	// serialize null
	GsonBuilder gsonBuilder = new GsonBuilder();
	gsonBuilder.serializeNulls();
	Gson gson = gsonBuilder.create();

	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put("schemaName", schemaName);
	dataMap.put("tableName", tableName);
	dataMap.put("vanAutoIncColumnName", vanAutoIncColumnName);
	dataMap.put("serverColumns", serverColumns);
	dataMap.put("syncData", dataToBesync);
	dataMap.put("syncedBy", user);
	if (facilityID != null)
		dataMap.put("facilityID", facilityID);

	String requestOBJ = gson.toJson(dataMap);
	HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(requestOBJ, Authorization, "datasync");
	logger.info("Request to sync data: " + requestOBJ);
	ResponseEntity<String> response = restTemplate.exchange(dataSyncUploadUrl, HttpMethod.POST, request,
			String.class);

	logger.info("Response from the server=" + response);

	int successCount = 0;
	int failCount = 0;
	List<String> successVanSerialNos = new ArrayList<>();
	List<String> failedVanSerialNos = new ArrayList<>();
	List<String> failureReasons = new ArrayList<>(); 

	if (response != null && response.hasBody()) {
		JSONObject obj = new JSONObject(response.getBody());
		if (obj.has("data")) {
			JSONObject dataObj = obj.getJSONObject("data");
			if (dataObj.has("records")) {
				JSONArray recordsArr = dataObj.getJSONArray("records");
				for (int i = 0; i < recordsArr.length(); i++) {
					JSONObject record = recordsArr.getJSONObject(i);
					String vanSerialNo = record.getString("vanSerialNo");
					boolean success = record.getBoolean("success");
					if (success) {
						successVanSerialNos.add(vanSerialNo);
						successCount++;
					} else {
						failedVanSerialNos.add(vanSerialNo);
						failCount++;
						// Capture the failure reason
						String reason = record.optString("reason", "Unknown error");
						failureReasons.add(reason);
					}
				}
			} else if (tableName.equalsIgnoreCase("m_beneficiaryregidmapping")) {
				// Handle summary response for m_beneficiaryregidmapping
				String respMsg = dataObj.optString("response", "");
				int statusCode = obj.optInt("statusCode", 0);
				if (respMsg.toLowerCase().contains("success") && statusCode == 200) {
					// All records are successful
					for (Map<String, Object> map : dataToBesync) {
						successVanSerialNos.add(String.valueOf(map.get(vanAutoIncColumnName)));
					}
					successCount = successVanSerialNos.size();
				} else {
					// All records failed
					for (Map<String, Object> map : dataToBesync) {
						failedVanSerialNos.add(String.valueOf(map.get(vanAutoIncColumnName)));
					}
					failCount = failedVanSerialNos.size();
					// Add a generic reason for all failed records
					for (int i = 0; i < failCount; i++) {
						failureReasons.add(respMsg.isEmpty() ? "Sync failed" : respMsg);
					}
				}
			}
		}
	}

	logger.info("Success Van Serial No=" + successVanSerialNos.toString());
	logger.info("Failed Van Serial No=" + failedVanSerialNos.toString());
	
	// Update processed flag for success and failed vanSerialNos
	if (!successVanSerialNos.isEmpty()) {
		dataSyncRepository.updateProcessedFlagInVan(schemaName, tableName, successVanSerialNos,
				vanAutoIncColumnName, user, "P","Null");
	}
	if (!failedVanSerialNos.isEmpty()) {
		dataSyncRepository.updateProcessedFlagInVan(schemaName, tableName, failedVanSerialNos,
				vanAutoIncColumnName, user, "F",failureReasons.get(0));
	}

	// Return detailed result object instead of just a string
	Map<String, Object> result = new HashMap<>();
	if (successCount > 0 && failCount == 0) {
		result.put("status", "Data successfully synced");
	} else if (successCount > 0 && failCount > 0) {
		result.put("status", "Partial success: " + successCount + " records synced, " + failCount + " failed");
	} else {
		result.put("status", "Sync failed");
	}
	
	result.put("successCount", successCount);
	result.put("failCount", failCount);
	
	return result;
}

	public StringBuilder getVanSerialNoListForSyncedData(String vanAutoIncColumnName,
			List<Map<String, Object>> dataToBesync) throws Exception {
		// comma separated van serial no
		StringBuilder vanSerialNos = new StringBuilder();

		int pointer1 = 0;
		for (Map<String, Object> map : dataToBesync) {
			if (pointer1 == dataToBesync.size() - 1)
				vanSerialNos.append(map.get(vanAutoIncColumnName.trim()));
			else
				vanSerialNos.append(map.get(vanAutoIncColumnName.trim()) + ",");

			pointer1++;
		}
		return vanSerialNos;
	}

	public String getDataSyncGroupDetails() {
		List<DataSyncGroups> dataSyncGroupList = dataSyncGroupsRepo.findByDeleted(false);
		if (dataSyncGroupList != null)
			return new Gson().toJson(dataSyncGroupList);
		else
			return null;
	}
}