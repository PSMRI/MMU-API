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
package com.iemr.mmu.controller.dataSyncLayerCentral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncDownloadMaster;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.service.dataSyncLayerCentral.FetchDownloadDataImpl;
import com.iemr.mmu.service.dataSyncLayerCentral.GetDataFromVanAndSyncToDBImpl;
import com.iemr.mmu.service.dataSyncLayerCentral.GetMasterDataFromCentralForVanImpl;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

/***
 * @operation Class used for data sync from van-to-server & server-to-van
 */
@RestController
@RequestMapping(value = "/dataSync", headers = "Authorization")
public class MMUDataSyncVanToServer {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private GetDataFromVanAndSyncToDBImpl getDataFromVanAndSyncToDBImpl;
	@Autowired
	private GetMasterDataFromCentralForVanImpl getMasterDataFromCentralForVanImpl;
	@Autowired
	private FetchDownloadDataImpl fetchDownloadDataImpl;

	@Operation(summary = "Sync data from van-to-server")
	@PostMapping(value = { "/van-to-server" }, consumes = "application/json", produces = "application/json")
	public String dataSyncToServer(@RequestBody String requestOBJ,
			@RequestHeader(value = "Authorization") String Authorization,  HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		
		logger.info("test: vanto server auth="+Authorization);
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
		logger.info("test: vanto server token="+jwtToken);

			String s = getDataFromVanAndSyncToDBImpl.syncDataToServer(requestOBJ, Authorization, jwtToken);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "data dync failed");
		} catch (Exception e) {
			response.setError(e);
			logger.error("Upload SYNC Exception" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Download data from server-to-van")
	@PostMapping(value = { "/server-to-van" }, consumes = "application/json", produces = "application/json")
	public String dataDownloadFromServer(@RequestBody SyncDownloadMaster syncDownloadMaster,
			@RequestHeader(value = "Authorization") String Authorization) {
		OutputResponse response = new OutputResponse();
		try {
			if (syncDownloadMaster != null) {
				String s = getMasterDataFromCentralForVanImpl.getMasterDataForVan(syncDownloadMaster);
				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error in master download for table " + syncDownloadMaster.getSchemaName()
							+ "." + syncDownloadMaster.getTableName());
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			response.setError(e);
		}
		return response.toStringWithSerialization();
	}

	@Operation(summary = "Download data from server-to-van transactional")
	@PostMapping(value = {
			"/server-to-van-transactional" }, consumes = "application/json", produces = "application/json")
	public String dataDownloadFromServerTransactional(@RequestBody SyncUploadDataDigester syncUploadDataDigester,
			@RequestHeader(value = "Authorization") String Authorization) {
		OutputResponse response = new OutputResponse();
		try {
			if (syncUploadDataDigester != null) {
				String s = fetchDownloadDataImpl.getDownloadData(syncUploadDataDigester);
				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error in data download for table " + syncUploadDataDigester.getSchemaName()
							+ "." + syncUploadDataDigester.getTableName());
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			if (syncUploadDataDigester != null) {
				logger.error(e + " - Error in data download for table " + syncUploadDataDigester.getSchemaName() + "."
						+ syncUploadDataDigester.getTableName());
			}
			response.setError(e);
		}
		return response.toStringWithSerialization();
	}

	@Operation(summary = "Update processed flag at central post successfull download")
	@PostMapping(value = {
			"/updateProcessedFlagPostDownload" }, consumes = "application/json", produces = "application/json")
	public String updateProcessedFlagPostDownload(@RequestBody SyncUploadDataDigester syncUploadDataDigester,
			@RequestHeader(value = "Authorization") String Authorization) {
		OutputResponse response = new OutputResponse();
		try {
			if (syncUploadDataDigester != null) {
				int i = fetchDownloadDataImpl.updateProcessedFlagPostSuccessfullDownload(syncUploadDataDigester);
				if (i > 0)
					response.setResponse("success");
				else
					response.setError(5000,
							"Error while updating flag. Please contact administrator "
									+ syncUploadDataDigester.getSchemaName() + "."
									+ syncUploadDataDigester.getTableName() + "." + syncUploadDataDigester.getIds());
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			if (syncUploadDataDigester != null) {
				logger.error(e + " - Error while updating flag. Please contact administrator "
						+ syncUploadDataDigester.getSchemaName() + "." + syncUploadDataDigester.getTableName() + "."
						+ syncUploadDataDigester.getIds());
			}
			response.setError(e);
		}
		return response.toStringWithSerialization();
	}

}
