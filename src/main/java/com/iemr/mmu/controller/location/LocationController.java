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
package com.iemr.mmu.controller.location;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import com.iemr.mmu.utils.JwtUtil;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import com.iemr.mmu.utils.CookieUtil;

import com.iemr.mmu.controller.common.master.CommonMasterController;
import com.iemr.mmu.service.location.LocationServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/location", headers = "Authorization")
@PreAuthorize("hasRole('NURSE') || hasRole('PHARMACIST') || hasRole('LABTECHNICIAN') || hasRole('REGISTRAR') || hasRole('DATASYNC') || hasRole('DATA_SYNC') || hasRole('DOCTOR') || hasRole('LAB_TECHNICIAN')  || hasRole('TC_SPECIALIST') || hasRole('ONCOLOGIST') || hasRole('RADIOLOGIST')")
public class LocationController {
	private OutputResponse response;
	private Logger logger = LoggerFactory.getLogger(CommonMasterController.class);
	private LocationServiceImpl locationServiceImpl;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	public void setLocationServiceImpl(LocationServiceImpl locationServiceImpl) {
		this.locationServiceImpl = locationServiceImpl;
	}

	@Operation(summary = "State master for beneficiary")
	@GetMapping(value = "/get/stateMaster", consumes = "application/json", produces = "application/json")
	public String getStateMaster() {
		logger.info("get state master ...");
		response = new OutputResponse();
		String s = locationServiceImpl.getStateList();
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting states");
		logger.info("stateMaster" + response.toString());
		return response.toString();
	}

	@Operation(summary = "Country master for beneficiary")
	@GetMapping(value = "/get/countryMaster", consumes = "application/json", produces = "application/json")
	public String getCountryMaster() {
		logger.info("get country master ...");
		response = new OutputResponse();
		String s = locationServiceImpl.getCountryList();
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting country");
		return response.toString();
	}

	@Operation(summary = "Country city master for beneficiary")
	@GetMapping(value = "/get/countryCityMaster/{countryID}/", consumes = "application/json", produces = "application/json")
	public String getCountryCityMaster(@PathVariable("countryID") Integer countryID) {
		logger.info("get country citymaster ...");
		response = new OutputResponse();
		String s = locationServiceImpl.getCountryCityList(countryID);
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting country city");
		return response.toString();
	}

	@Operation(summary = "District master from state id")
	@GetMapping(value = "/get/districtMaster/{stateID}", consumes = "application/json", produces = "application/json")
	public String getDistrictMaster(@PathVariable("stateID") Integer stateID) {
		logger.info("get District master ...");
		response = new OutputResponse();
		String s = locationServiceImpl.getDistrictList(stateID);
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting districts");
		logger.info("districtMaster" + response.toString());
		return response.toString();
	}

	@Operation(summary = "District block master")
	@GetMapping(value = "/get/districtBlockMaster/{districtID}", consumes = "application/json", produces = "application/json")
	public String getDistrictBlockMaster(@PathVariable("districtID") Integer districtID) {
		logger.info("get District Block master districtID ..." + districtID);
		response = new OutputResponse();
		String s = locationServiceImpl.getDistrictBlockList(districtID);
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting district blocks");
		logger.info("districtBlockMaster" + response.toString());
		return response.toString();
	}

	@Operation(summary = "Village master from block id")
	@GetMapping(value = "/get/villageMasterFromBlockID/{blockID}", consumes = "application/json", produces = "application/json")
	public String getVillageMaster(@PathVariable("blockID") Integer blockID) {
		logger.info("get District Block master districtID ..." + blockID);
		response = new OutputResponse();
		String s = locationServiceImpl.getVillageMasterFromBlockID(blockID);
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting villages");
		logger.info("village master" + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get location details based on SP id and PSM id")
	@PostMapping(value = "/getLocDetailsBasedOnSpIDAndPsmID", consumes = "application/json", produces = "application/json")
	public String getLocDetailsBasedOnSpIDAndPsmIDNew(@RequestBody String comingRequest, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			String userId = jwtUtil.getUserIdFromToken(jwtToken);
			if(userId != null) {
				int userID = Integer.parseInt(userId);
			if (obj != null && obj.has("spID") && obj.has("spPSMID") && obj.get("spID") != null
					&& obj.get("spPSMID") != null) {
			
				String s = locationServiceImpl.getLocDetailsNew(obj.getInt("spID"), obj.getInt("spPSMID"), userID);

				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			} else {
				response.setError(403, "Unauthorized access");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(5000, "Error while getting location data");
		}
		return response.toString();
	}

	@Operation(summary = "Get district taluk master")
	@GetMapping(value = "/get/DistrictTalukMaster/{districtBranchID}", consumes = "application/json", produces = "application/json")
	public String getDistrictTalukMaster(@PathVariable("districtBranchID") Integer districtBranchID) {

		response = new OutputResponse();
		String s = locationServiceImpl.getDistrictTalukList(districtBranchID);
		if (s != null)
			response.setResponse(s);
		else
			response.setError(5000, "Error while getting district blocks");

		return response.toString();
	}
}
