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
package com.iemr.mmu.controller.benid;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.mmu.service.benid.BenIdAutoTopUpService;
import com.iemr.mmu.service.benid.CentralAuthService;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/centralAuth", headers = "Authorization", produces = "application/json")
public class CentralAuthController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private CentralAuthService centralAuthService;

	@Autowired
	private BenIdAutoTopUpService benIdAutoTopUpService;

	@Operation(summary = "Automatic BenID top-up and central connection status")
	@GetMapping(value = "/status")
	public String status() {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(benIdAutoTopUpService.status().toString());
		} catch (Exception e) {
			logger.error("Error reading BenID auto top-up status: " + e.getMessage());
			response.setError(e);
		}
		return response.toString();
	}

	/**
	 * Called by MMU-UI right after a successful login. Returns immediately; the
	 * central login and any top-up happen in the background.
	 */
	@Operation(summary = "Connect this laptop to central with the user's login (one time)")
	@PostMapping(value = "/capture", consumes = "application/json")
	public String capture(@RequestBody String requestOBJ) {
		OutputResponse response = new OutputResponse();
		try {
			if (!centralAuthService.isEnabled()) {
				response.setResponse("disabled");
				return response.toString();
			}
			JSONObject obj = new JSONObject(requestOBJ);
			String userName = obj.optString("userName", null);
			String password = obj.optString("password", null);
			boolean force = obj.optBoolean("reconnect", false);
			CompletableFuture.runAsync(() -> {
				if (centralAuthService.capture(userName, password, force)) {
					benIdAutoTopUpService.triggerNow();
				}
			});
			response.setResponse("accepted");
		} catch (Exception e) {
			logger.error("Error in central capture: " + e.getMessage());
			response.setError(e);
		}
		return response.toString();
	}
}
