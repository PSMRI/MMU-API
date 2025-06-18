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
package com.iemr.mmu.controller.common.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.mmu.service.common.master.CommonMasterServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/master", headers = "Authorization")
/** Objective: provides master data based on given visitCategory */
public class CommonMasterController {

	private Logger logger = LoggerFactory.getLogger(CommonMasterController.class);

	private CommonMasterServiceImpl commonMasterServiceImpl;

	@Autowired
	public void setCommonMasterServiceImpl(CommonMasterServiceImpl commonMasterServiceImpl) {
		this.commonMasterServiceImpl = commonMasterServiceImpl;
	}

	/**
	 * @Objective provides list of visit reasons and visit categories
	 * @return list of visit reasons and visit categories
	 */
	@Operation(summary = "Master data for visit reasons & categories")
	@GetMapping(value = "/get/visitReasonAndCategories", consumes = "application/json", produces = "application/json")
	public String getVisitReasonAndCategories() {
		logger.info("getVisitReasonAndCategories ...");
		OutputResponse response = new OutputResponse();
		response.setResponse(commonMasterServiceImpl.getVisitReasonAndCategories());
		logger.info("visitReasonAndCategories" + response.toString());
		return response.toString();
	}

	/**
	 * 
	 * @param visitCategoryID
	 * @return nurse master data for the provided visitCategoryID
	 */
	@Operation(summary = "Master data API for nurse")
	@GetMapping(value = "/nurse/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}", consumes = "application/json", produces = "application/json")
	public String nurseMasterData(@PathVariable("visitCategoryID") Integer visitCategoryID,
			@PathVariable("providerServiceMapID") Integer providerServiceMapID, @PathVariable("gender") String gender) {
		logger.info("Nurse master Data for categoryID:" + visitCategoryID + " and providerServiceMapID:"
				+ providerServiceMapID);
		;
		OutputResponse response = new OutputResponse();
		response.setResponse(
				commonMasterServiceImpl.getMasterDataForNurse(visitCategoryID, providerServiceMapID, gender));
		logger.info("Nurse master Data for categoryID:" + response.toString());
		return response.toString();
	}

	/**
	 * 
	 * @param visitCategoryID
	 * @return doctor master data for the provided visitCategoryID
	 */
	@Operation(summary = "Master data API for doctor")
	@GetMapping(value = "/doctor/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}/{facilityID}/{vanID}", produces = "application/json")
	public String doctorMasterData(@PathVariable("visitCategoryID") Integer visitCategoryID,
			@PathVariable("providerServiceMapID") Integer providerServiceMapID, @PathVariable("gender") String gender,
			@PathVariable("facilityID") Integer facilityID, @PathVariable("vanID") Integer vanID) {
		logger.info("Doctor master Data for categoryID:" + visitCategoryID + " and providerServiceMapID:"
				+ providerServiceMapID);
		OutputResponse response = new OutputResponse();
		response.setResponse(commonMasterServiceImpl.getMasterDataForDoctor(visitCategoryID, providerServiceMapID,
				gender, facilityID, vanID));
		logger.info("Doctor master Data for categoryID:" + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get ECG abnormalities")
	@GetMapping(value = "/ecgAbnormalities", consumes = "application/json", produces = "application/json")
	public String getECGAbnormalities() {

		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonMasterServiceImpl.getECGAbnormalities());
		} catch (Exception e) {
			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();

	}

}
