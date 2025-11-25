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
package com.iemr.mmu.controller.fileSync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.mmu.service.fileSync.FileSyncService;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RequestMapping("/fileSyncController")
@RestController
@PreAuthorize("hasRole('DATASYNC') || hasRole('DATA_SYNC') ")
public class FileSyncController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	FileSyncService fileSyncService;

	@Operation(summary = "Get server credential")
	@GetMapping(value = "/getServerCredential", headers = "Authorization", consumes = "application/json", produces = "application/json")
	public String getServerCredential() {
		logger.info("getServerCredential request ");
		OutputResponse response = new OutputResponse();
		try {

			String data = fileSyncService.getServerCredential();
			response.setResponse(data);
			logger.info("getServerCredential response " + response.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(e);

		}

		return response.toString();
	}

	@Operation(summary = "Sync files")
	@GetMapping(value = "/syncFiles", headers = "Authorization", consumes = "application/json", produces = "application/json")
	public String syncFiles(@RequestHeader(value = "ServerAuthorization") String ServerAuthorization) {
		logger.info("syncFiles request ");
		OutputResponse response = new OutputResponse();
		try {

			String data = fileSyncService.syncFiles(ServerAuthorization);
			response.setResponse(data);
			logger.info("syncFiles response " + response.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(e);

		}

		return response.toString();
	}
}
