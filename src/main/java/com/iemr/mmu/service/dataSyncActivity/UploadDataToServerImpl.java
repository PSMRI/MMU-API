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

	// batch size for data upload
	// private static final int BATCH_SIZE = 30;

	/**
	 * 
	 * @param groupName
	 * @param Authorization
	 * @return
	 */
	// @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {
	// Exception.class })
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
	 * 
	 * @param syncTableDetailsIDs
	 * @param Authorization
	 * @return
	 */

	private String startDataSync(int vanID, String user, String Authorization, String token) throws Exception {
		String serverAcknowledgement = null;
		List<Map<String, String>> responseStatus = new ArrayList<>();
		boolean isProgress = false;
		boolean hasSyncFailed = false;
		ObjectMapper objectMapper = new ObjectMapper();
		// fetch group masters
		List<DataSyncGroups> dataSyncGroupList = dataSyncGroupsRepo.findByDeleted(false);
		logger.debug("Fetched DataSyncGroups: {}",
				objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataSyncGroupList));
		for (DataSyncGroups dataSyncGroups : dataSyncGroupList) {
			int groupId = dataSyncGroups.getSyncTableGroupID();
			List<SyncUtilityClass> syncUtilityClassList = getVanAndServerColumns(groupId);
			logger.debug("Fetched SyncUtilityClass for groupId {}: {}", groupId,
					objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(syncUtilityClassList));
			List<Map<String, Object>> syncData;
			List<Map<String, Object>> syncDataBatch;
			Map<String, String> groupIdStatus = new HashMap<>();
			for (SyncUtilityClass obj : syncUtilityClassList) {
				// if (!isProgress) {
				// get data from DB to sync to server
				syncData = getDataToSync(obj.getSchemaName(), obj.getTableName(), obj.getVanColumnName());
				logger.debug("Fetched syncData for schema {} and table {}: {}", obj.getSchemaName(), obj.getTableName(),
						objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(syncData));
				// System.out.println(new Gson().toJson(syncData));
				if (syncData != null && syncData.size() > 0) {
					int dataSize = syncData.size();
					int startIndex = 0;
					int fullBatchCount = dataSize / BATCH_SIZE;
					int remainder = dataSize % BATCH_SIZE;

					logger.info("Starting batch sync for schema: {}, table: {} with {} full batches and {} remainder",
							obj.getSchemaName(), obj.getTableName(), fullBatchCount, remainder);


					for (int i = 0; i < fullBatchCount; i++) {
						syncDataBatch = getBatchOfAskedSizeDataToSync(syncData, startIndex,
								BATCH_SIZE);
						serverAcknowledgement = syncDataToServer(vanID, obj.getSchemaName(), obj.getTableName(),
								obj.getVanAutoIncColumnName(), obj.getServerColumnName(), syncDataBatch, user,
								Authorization, token);
						logger.debug("Server acknowledgement for batch {}: {}", i, serverAcknowledgement);

						if (serverAcknowledgement == null || !serverAcknowledgement.contains("success")) {
							logger.error("Sync failed for batch {} in schema: {}, table: {}", i, obj.getSchemaName(),
									obj.getTableName());
							hasSyncFailed = true;
							setResponseStatus(groupIdStatus, groupId, "failed", responseStatus);
							break;
						}

						startIndex += BATCH_SIZE;
					}

					if (!hasSyncFailed && remainder > 0) {
						syncDataBatch = getBatchOfAskedSizeDataToSync(syncData, startIndex,
								remainder);
						serverAcknowledgement = syncDataToServer(vanID, obj.getSchemaName(), obj.getTableName(),
								obj.getVanAutoIncColumnName(), obj.getServerColumnName(), syncDataBatch, user,
								Authorization, token);

						if (serverAcknowledgement == null || !serverAcknowledgement.contains("success")) {
							logger.error("Sync failed for remaining data in schema: {}, table: {}", obj.getSchemaName(),
									obj.getTableName());
							hasSyncFailed = true;
							setResponseStatus(groupIdStatus, groupId, "failed", responseStatus);
							break;
						}
					}

					if (!hasSyncFailed) {
						logger.info("Data sync completed for schema: {}, table: {}", obj.getSchemaName(),
								obj.getTableName());
						setResponseStatus(groupIdStatus, groupId, "completed", responseStatus);
					}
				} else {
					logger.info("No data to sync for schema {} and table {}", obj.getSchemaName(), obj.getTableName());
					setResponseStatus(groupIdStatus, groupId, "completed", responseStatus);
				}

				if (hasSyncFailed) {
					// Mark all subsequent groups as "pending"
					for (DataSyncGroups remainingGroup : dataSyncGroupList
							.subList(dataSyncGroupList.indexOf(dataSyncGroups) + 1, dataSyncGroupList.size())) {
						Map<String, String> pendingGroupIdStatus = new HashMap<>();
						pendingGroupIdStatus.put("groupId", String.valueOf(remainingGroup.getSyncTableGroupID()));
						pendingGroupIdStatus.put("status", "pending");
						responseStatus.add(pendingGroupIdStatus);
					}
					break;
				}
			}
		}

		if (hasSyncFailed) {
			Map<String, Object> response = new HashMap<>();
			response.put("response", "Data sync failed");
			response.put("groupsProgress", responseStatus);
			objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
			return objectMapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(Collections.singletonMap("data", response));
		} else {
			if ("No data to sync".equals(serverAcknowledgement)) {
				return serverAcknowledgement;
			} else {
				return "Data successfully synced";
			}
		}
	}

	private void setResponseStatus(Map<String, String> groupIdStatus, int groupId, String serverAcknowledgement,
			List<Map<String, String>> responseStatus) {
		groupIdStatus.put("groupId", String.valueOf(groupId));
		groupIdStatus.put("status", serverAcknowledgement);
		responseStatus.add(groupIdStatus);
	}

	/**
	 * 
	 * @param syncTableDetailsIDs
	 * @return
	 */

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

	/**
	 * 
	 * @param schemaName
	 * @param tableName
	 * @param columnNames
	 * @return
	 */

	private List<Map<String, Object>> getDataToSync(String schemaName, String tableName, String columnNames)
			throws Exception {
				logger.info("Fetching data to sync for schema: {}, table: {}, columns: {}", schemaName, tableName, columnNames);
		List<Map<String, Object>> resultSetList = dataSyncRepository.getDataForGivenSchemaAndTable(schemaName,
				tableName, columnNames);
		if (resultSetList != null) {
			logger.debug("Fetched {} records for schema '{}', table '{}'", resultSetList.size(), schemaName, tableName);
			// Optionally log a sample of the resultSetList for verification (be careful
			// with large datasets)
			if (!resultSetList.isEmpty()) {
				logger.debug("Sample record: {}", resultSetList.get(0));
			}
		} else {
			logger.debug("No records found for schema '{}', table '{}'", schemaName, tableName);
		}
		return resultSetList;
	}

	/**
	 * 
	 * @param syncData
	 * @param startIndex
	 * @param size
	 * @return
	 */

	private List<Map<String, Object>> getBatchOfAskedSizeDataToSync(List<Map<String, Object>> syncData, int startIndex,
			int size) throws Exception {
		List<Map<String, Object>> syncDataOfBatchSize = syncData.subList(startIndex, (startIndex + size));
		return syncDataOfBatchSize;
	}

	/**
	 * 
	 * @param schemaName
	 * @param tableName
	 * @param vanAutoIncColumnName
	 * @param serverColumns
	 * @param dataToBesync
	 * @param Authorization
	 * @return
	 */

	public String syncDataToServer(int vanID, String schemaName, String tableName, String vanAutoIncColumnName,
			String serverColumns, List<Map<String, Object>> dataToBesync, String user, String Authorization,
			String token)
			throws Exception {

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
		logger.info("Request obj="+requestOBJ);
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(requestOBJ, Authorization, "datasync");
		ResponseEntity<String> response = restTemplate.exchange(dataSyncUploadUrl, HttpMethod.POST, request,
				String.class);
logger.info("Response for thes erver="+response);
logger.info("Response body="+response.getBody());
 int successCount = 0;
        int failCount = 0;
        List<String> successVanSerialNos = new ArrayList<>();
        List<String> failedVanSerialNos = new ArrayList<>();

        // if (response != null && response.hasBody()) {
        //     JSONObject obj = new JSONObject(response.getBody());
        //     if (obj.has("data")) {
        //         JSONObject dataObj = obj.getJSONObject("data");
        //         if (dataObj.has("records")) {
        //             JSONArray recordsArr = dataObj.getJSONArray("records");
        //             for (int i = 0; i < recordsArr.length(); i++) {
        //                 JSONObject record = recordsArr.getJSONObject(i);
        //                 String vanSerialNo = record.getString("vanSerialNo");
        //                 boolean success = record.getBoolean("success");
        //                 if (success) {
        //                     successVanSerialNos.add(vanSerialNo);
        //                     successCount++;
        //                 } else {
        //                     failedVanSerialNos.add(vanSerialNo);
        //                     failCount++;
        //                 }
        //             }
        //         }
        //     }
        // }

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
            }
        }
    }
}

logger.info("Success Van Serial No="+successVanSerialNos.toString());
logger.info("Failed Van Serial No="+failedVanSerialNos.toString());
        // Update processed flag for success and failed vanSerialNos
        if (!successVanSerialNos.isEmpty()) {
            dataSyncRepository.updateProcessedFlagInVan(schemaName, tableName, successVanSerialNos,
                    vanAutoIncColumnName, user, "P");
        }
        if (!failedVanSerialNos.isEmpty()) {
            dataSyncRepository.updateProcessedFlagInVan(schemaName, tableName, failedVanSerialNos,
                    vanAutoIncColumnName, user, "F");
        }

        if (successCount > 0 && failCount == 0)
            return "Data successfully synced";
        else if (successCount > 0 && failCount > 0)
            return "Partial success: " + successCount + " records synced, " + failCount + " failed";
        else
            return "Sync failed";
    }
	// 	int i = 0;
	// 	if (response != null && response.hasBody()) {
	// 		JSONObject obj = new JSONObject(response.getBody());
	// 		if (obj != null && obj.has("statusCode") && obj.getInt("statusCode") == 200) {
	// 			StringBuilder vanSerialNos = getVanSerialNoListForSyncedData(vanAutoIncColumnName, dataToBesync);
				
	// 			i = dataSyncRepository.updateProcessedFlagInVan(schemaName, tableName, vanSerialNos,
	// 					vanAutoIncColumnName, user);
	// 		}
	// 	}
	// 	if (i > 0)
	// 		return "Data successfully synced";
	// 	else
	// 		return null;
	// }

	/**
	 * 
	 * @param vanAutoIncColumnName
	 * @param dataToBesync
	 * @return
	 */

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
