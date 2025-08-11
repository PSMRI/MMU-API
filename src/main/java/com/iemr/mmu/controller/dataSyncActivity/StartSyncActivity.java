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
package com.iemr.mmu.controller.dataSyncActivity;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.iemr.mmu.service.dataSyncActivity.DownloadDataFromServerImpl;
import com.iemr.mmu.service.dataSyncActivity.DownloadDataFromServerTransactionalImpl;
import com.iemr.mmu.service.dataSyncActivity.UploadDataToServerImpl;
import com.iemr.mmu.utils.response.OutputResponse;
import com.iemr.mmu.utils.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

/***
 * @purpose Class used for data sync from van-to-server & server-to-van
 */
@RestController
@RequestMapping(value = "/dataSyncActivity", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class StartSyncActivity {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private UploadDataToServerImpl uploadDataToServerImpl;
	@Autowired
	private DownloadDataFromServerImpl downloadDataFromServerImpl;
	@Autowired
	private DownloadDataFromServerTransactionalImpl downloadDataFromServerTransactionalImpl;
	private static final String GROUP_ID = "groupID";
	private static final String PROVIDER_SERVICE_MAP_ID = "providerServiceMapID";

	@Operation(summary = "Start data sync from van to Server")
	@PostMapping(value = { "/van-to-server" })
	public String dataSyncToServer(@RequestBody String requestOBJ,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "ServerAuthorization") String serverAuthorization, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);

			JSONObject obj = new JSONObject(requestOBJ);
			if (obj.has("user") && obj.get("user") != null && obj.has("vanID") && obj.get("vanID") != null) {
				String s = uploadDataToServerImpl.getDataToSyncToServer(obj.getInt("vanID"), obj.getString("user"),
						serverAuthorization, jwtToken);
				// if (s != null)
				response.setResponse(s);
				// else
				// response.setError(5000, "Error in data sync");
			} else {
				response.setError(5000, "Invalid request, Either of groupID or user is invalid or null");
			}
		} catch (Exception e) {
			logger.error("Error in data sync : " + e);
			response.setError(e);
		}
		return response.toStringWithSerialization();
	}

	@Operation(summary = "Get data sync group details")
	@GetMapping(value = { "/getSyncGroupDetails" })
	public String getSyncGroupDetails() {
		OutputResponse response = new OutputResponse();
		try {
			String s = uploadDataToServerImpl.getDataSyncGroupDetails();
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error in getting data sync group details");
		} catch (Exception e) {
			logger.error("Error in getting data sync group details : " + e);
			response.setError(e);
		}
		return response.toString();
	}

	/**
	 * @return Masters download in van from central server
	 */
	@Operation(summary = "Data sync master download")
	@PostMapping(value = { "/startMasterDownload" })
	public String startMasterDownload(@RequestBody String requestOBJ,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader	(value = "ServerAuthorization") String serverAuthorization, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("From master download");
			JSONObject obj = new JSONObject(requestOBJ);
			if (obj.has("vanID") && obj.get("vanID") != null && obj.has(PROVIDER_SERVICE_MAP_ID)
					&& obj.get(PROVIDER_SERVICE_MAP_ID) != null) {
				String s = downloadDataFromServerImpl.downloadMasterDataFromServer(serverAuthorization,"datasync",
						obj.getInt("vanID"), obj.getInt(PROVIDER_SERVICE_MAP_ID));
				if (s != null) {
					if (s.equalsIgnoreCase("inProgress"))
						response.setError(5000,
								"Download is already in progress from different device, kindly wait to finish this");
					else
						response.setResponse(s);
				} else
					response.setError(5000, s);
			} else {
				response.setError(5000,
						"vanID / providerServiceMapID or both are missing," + " Kindly contact the administrator.");
			}

		} catch (Exception e) {
			logger.error("Error in Master data Download : " + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Data sync master download progress check")
	@GetMapping(value = { "/checkMastersDownloadProgress" })
	public String checkMastersDownloadProgress() {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(new Gson().toJson(downloadDataFromServerImpl.getDownloadStatus()));
		} catch (Exception e) {
			logger.error("Error in Master data Download progress check : " + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get van details for master download")
	@GetMapping(value = { "/getVanDetailsForMasterDownload" })
	public String getVanDetailsForMasterDownload() {
		OutputResponse response = new OutputResponse();
		try {
			String s = downloadDataFromServerImpl.getVanDetailsForMasterDownload();
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting van details.");
		} catch (Exception e) {
			logger.error("Error while getting van details : " + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Call central API to generate beneficiary id and import to local")
	@PostMapping(value = { "/callCentralAPIToGenerateBenIDAndimportToLocal" })
	public String callCentralAPIToGenerateBenIDAndimportToLocal(@RequestBody String requestOBJ,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "ServerAuthorization") String serverAuthorization, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			int i = downloadDataFromServerImpl.callCentralAPIToGenerateBenIDAndimportToLocal(requestOBJ, authorization,
					serverAuthorization, jwtToken);
			if (i == 0) {
				response.setError(5000, "Error while generating UNIQUE_ID at central server");
			} else {
				if (i == 1) {
					response.setError(5000, "UNIQUE_ID generated successfully, but error while importing to local");
				} else if (i == 2)
					response.setResponse("UNIQUE_ID generated and imported successfully");
			}
		} catch (Exception e) {
			logger.error("Error while getting van details : " + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Call central API to download transaction data to local")
	@PostMapping(value = { "/downloadTransactionToLocal" })
	public String downloadTransactionToLocal(@RequestBody String requestOBJ,
			@RequestHeader(value = "ServerAuthorization") String serverAuthorization, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			JSONObject obj = new JSONObject(requestOBJ);
			if (obj.has("vanID") && obj.get("vanID") != null) {
				int i = downloadDataFromServerTransactionalImpl.downloadTransactionalData(obj.getInt("vanID"),
						serverAuthorization, jwtToken);

				if (i > 0)
					response.setResponse("Success");
				else
					response.setError(5000, "Issue in download. Please contact administrator");
			}
		} catch (Exception e) {
			logger.error("Error while downloading inventory transaction data : " + e);
			response.setError(e);
		}
		return response.toString();
	}

}
