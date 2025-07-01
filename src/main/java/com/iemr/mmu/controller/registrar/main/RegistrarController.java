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
package com.iemr.mmu.controller.registrar.main;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.registrar.BeneficiaryData;
import com.iemr.mmu.data.registrar.V_BenAdvanceSearch;
import com.iemr.mmu.data.registrar.WrapperBeneficiaryRegistration;
import com.iemr.mmu.service.common.master.RegistrarServiceMasterDataImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.nurse.NurseServiceImpl;
import com.iemr.mmu.service.registrar.RegistrarServiceImpl;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/registrar", headers = "Authorization", consumes = "application/json", produces = "application/json")
/**
 * Objective: Performs QuickSearch, AdvancedSearch and fetching Beneficiary
 * Details
 */
public class RegistrarController {
	private Logger logger = LoggerFactory.getLogger(RegistrarController.class);
	private InputMapper inputMapper = new InputMapper();
	private RegistrarServiceImpl registrarServiceImpl;
	private RegistrarServiceMasterDataImpl registrarServiceMasterDataImpl;
	private NurseServiceImpl nurseServiceImpl;
	private CommonNurseServiceImpl commonNurseServiceImpl;

	@Autowired
	public void setRegistrarServiceImpl(RegistrarServiceImpl registrarServiceImpl) {
		this.registrarServiceImpl = registrarServiceImpl;
	}

	@Autowired
	public void setRegistrarServiceMasterDataImpl(RegistrarServiceMasterDataImpl registrarServiceMasterDataImpl) {
		this.registrarServiceMasterDataImpl = registrarServiceMasterDataImpl;
	}

	@Autowired
	public void setNurseServiceImpl(NurseServiceImpl nurseServiceImpl) {
		this.nurseServiceImpl = nurseServiceImpl;
	}

	@Operation(summary = "Get registrar worklist data")
	@PostMapping(value = { "/registrarWorkListData" })
	public String getRegistrarWorkList(@ApiParam(value = "{\"spID\": \"Integer\"}") @RequestBody String comingRequest)
			throws JSONException {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);

			response.setResponse(this.registrarServiceImpl.getRegWorkList(obj.getInt("spID")));
			logger.info("getRegistrarWorkList response:" + response);
		} catch (Exception e) {
			response.setError(e);
			logger.error("Error in getRegistrarWorkList:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Search for the beneficiary by beneficiary id")
	@PostMapping(value = { "/quickSearch" })
	public String quickSearchBeneficiary(
			@ApiParam(value = "{\"benID\": \"String\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);

			response.setResponse(registrarServiceImpl.getQuickSearchBenData(obj.getString("benID")));
			logger.info("quickSearchBeneficiary response:" + response);
		} catch (Exception e) {
			logger.error("Error in quickSearchBeneficiary :" + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Search for the beneficiary based on provided data")
	@PostMapping(value = { "/advanceSearch" })
	public String advanceSearch(
			@ApiParam(value = "{\"firstName\": \"String\", \"lastName\": \"String\", \"phoneNo\": \"String\","
					+ "\"beneficiaryID\": \"String\", \"stateID\": \"Integer\", \"districtID\": \"Integer\", \"aadharNo\": \"String\"},"
					+ " \"govtIdentityNo\": \"String\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			V_BenAdvanceSearch v_BenAdvanceSearch = inputMapper.gson().fromJson(comingRequest,
					V_BenAdvanceSearch.class);
			response.setResponse(registrarServiceImpl.getAdvanceSearchBenData(v_BenAdvanceSearch));
			logger.info("advanceSearch response:" + response);
		} catch (Exception e) {
			logger.error("Error in advanceSearch :" + e);
			response.setError(e);
		}

		return response.toString();
	}

	@Operation(summary = "Get beneficiary details of given beneficiary registration id")
	@PostMapping(value = { "/get/benDetailsByRegID" })
	public String getBenDetailsByRegID(
			@ApiParam(value = "{\"beneficiaryRegID\": \"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("beneficiaryRegID")) {
				if (obj.getLong("beneficiaryRegID") > 0) {

					String beneficiaryData = registrarServiceMasterDataImpl
							.getBenDetailsByRegID(obj.getLong("beneficiaryRegID"));

					response.setResponse(beneficiaryData);
				} else {
					response.setError(500, "Please pass beneficiaryRegID");
				}
			} else {
				response.setError(500, "Bad Request... beneficiaryRegID is not there in request");
			}
			logger.info("getBenDetailsByRegID response :" + response);
		} catch (Exception e) {
			logger.error("Error in getBenDetailsByRegID :" + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary details")
	@PostMapping(value = { "/get/beneficiaryDetails" })
	public String getBeneficiaryDetails(
			@ApiParam(value = "{\"beneficiaryRegID\": \"Long\"}") @RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(requestObj);
			if (obj.has("beneficiaryRegID")) {
				if (obj.getLong("beneficiaryRegID") > 0) {

					String beneficiaryData = registrarServiceImpl
							.getBeneficiaryDetails(obj.getLong("beneficiaryRegID"));
					if (beneficiaryData != null) {
						response.setResponse(beneficiaryData);
					} else {
						Map<String, String> noDataMap = new HashMap<>();
						response.setResponse(new Gson().toJson(noDataMap));
					}

				} else {
					response.setError(500, "Please pass beneficiaryRegID");
				}
			} else {
				response.setError(500, "Bad Request... beneficiaryRegID is not there in request");
			}
			logger.info("getBeneficiaryDetails response :" + response);
		} catch (Exception e) {
			logger.error("Error in getBeneficiaryDetails :" + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary image")
	@PostMapping(value = { "/get/beneficiaryImage" })
	public String getBeneficiaryImage(
			@ApiParam(value = "{\"beneficiaryRegID\": \"Long\"}") @RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(requestObj);
			if (obj.has("beneficiaryRegID")) {
				if (obj.getLong("beneficiaryRegID") > 0) {
					String beneficiaryData = registrarServiceImpl.getBenImage(obj.getLong("beneficiaryRegID"));

					response.setResponse(beneficiaryData);
				} else {
					response.setError(500, "Please pass beneficiaryRegID");
				}
			} else {
				response.setError(500, "Bad Request... beneficiaryRegID is not there in request");
			}
			logger.info("getBeneficiaryDetails response :" + response);
		} catch (Exception e) {
			logger.error("Error caused by {} ", e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Search beneficiary for beneficiary id or beneficiary phone no")
	@PostMapping(value = { "/quickSearchNew" })
	public String quickSearchNew(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest request) {
		String searchList = null;
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			searchList = registrarServiceImpl.beneficiaryQuickSearch(requestObj, authorization, jwtToken);
			if (searchList == null) {
				response.setError(5000, "Invalid request");
				return response.toString();
			} else {
				return searchList;
			}
		} catch (Exception e) {
			logger.error("Error in Quick Search" + e);
			response.setError(5000, "Error while searching beneficiary");
			return response.toString();
		}

	}

	@Operation(summary = "Search beneficiary advance search new")
	@PostMapping(value = { "/advanceSearchNew" })
	public String advanceSearchNew(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest request) {
		String searchList = null;
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			searchList = registrarServiceImpl.beneficiaryAdvanceSearch(requestObj, authorization, jwtToken);
			if (searchList == null) {
				response.setError(5000, "Invalid request");
				return response.toString();
			} else {
				return searchList;
			}
		} catch (Exception e) {
			logger.error("Error in Quick Search" + e);
			response.setError(5000, "Error while searching beneficiary");
			return response.toString();
		}

	}

	@Operation(summary = "Get beneficiary details for left side panel of given beneficiary registration id")
	@PostMapping(value = { "/get/benDetailsByRegIDForLeftPanelNew" })
	public String getBenDetailsForLeftSidePanelByRegID(
			@ApiParam(value = "{\"beneficiaryRegID\": \"Long\"}") @RequestBody String comingRequest,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("beneficiaryRegID") && obj.has("benFlowID")) {
				if (obj.getLong("beneficiaryRegID") > 0 && obj.getLong("benFlowID") > 0) {

					String beneficiaryData = registrarServiceMasterDataImpl.getBenDetailsForLeftSideByRegIDNew(
							obj.getLong("beneficiaryRegID"), obj.getLong("benFlowID"), authorization, comingRequest);

					response.setResponse(beneficiaryData);
				} else {
					response.setError(500, "Invalid Beneficiary ID");
				}
			} else {
				response.setError(500, "Invalid request");
			}
			logger.info("getBenDetailsByRegID response :" + response);
		} catch (Exception e) {
			logger.error("Error in getBenDetailsByRegID :" + e);
			response.setError(5000, "Error while getting beneficiary details");
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary image")
	@PostMapping(value = { "/getBenImage" })
	public String getBenImage(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			
			return registrarServiceMasterDataImpl.getBenImageFromIdentityAPI(authorization, requestObj, jwtToken);
		} catch (Exception e) {
			logger.error("Error ben image fetch" + e);
			response.setError(5000, "Error while getting beneficiary image");
			return response.toString();
		}

	}

	@Operation(summary = "Register a new beneficiary")
	@PostMapping(value = { "/registrarBeneficaryRegistration" })
	public String createBeneficiary(
			@ApiParam(value = "{\"benD\":{\"firstName\": \"String\", \"lastName\": \"String\", \"gender\": \"Short\","
					+ "\"dob\": \"Timestamp\", \"maritalStatus\": \"Short\", \"fatherName\": \"String\", \"motherName\": \"String\","
					+ "\"husbandName\": \"String\", \"image\": \"String\", \"aadharNo\": \"String\", \"income\": \"Short\", "
					+ "\"literacyStatus\": \"String\", \"educationQualification\": \"Short\", \"occupation\": \"Short\", \"phoneNo\": \"String\","
					+ "\"emailID\": \"Integer\", \"bankName\": \"String\", \"branchName\": \"String\", \"IFSCCode\": \"String\", \"accountNumber\": \"String\","
					+ "\"community\": \"Short\", \"religion\": \"Short\", \"blockID\": \"Integer\", \"blockName\": \"String\","
					+ "\"habitation\": \"String\", \"villageID\": \"Integer\", \"villageName\": \"String\", \"districtID\": \"Integer\","
					+ "\"districtName\": \"String\", \"stateID\": \"Integer\", \"stateName\": \"String\", \"countryID\": \"Integer\","
					+ "\"govID\": [{\"type\": \"String\",\"value\": \"String\"}], \"ageAtMarriage\": \"Integer\", \"createdBy\": \"String\", "
					+ "\"servicePointID\": \"Integer\"}}") @RequestBody String comingRequest,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();
		try {

			WrapperBeneficiaryRegistration wrapperBeneficiaryRegistrationOBJ = InputMapper.gson()
					.fromJson(comingRequest, WrapperBeneficiaryRegistration.class);
			logger.info("createBeneficiary request:" + comingRequest);
			JsonObject benD = wrapperBeneficiaryRegistrationOBJ.getBenD();

			if (benD == null || benD.isJsonNull()) {
				response.setError(0, "Invalid input data");
			} else {
				BeneficiaryData benData = registrarServiceImpl.createBeneficiary(benD);

				if (benData != null) {
					Long benRegID = benData.getBeneficiaryRegID();
					Long benDemoID = registrarServiceImpl.createBeneficiaryDemographic(benD, benRegID);
					Long benPhonMapID = registrarServiceImpl.createBeneficiaryPhoneMapping(benD, benRegID);

					registrarServiceImpl.createBenGovIdMapping(benD, benRegID);

					Long benbenDemoOtherID = registrarServiceImpl.createBeneficiaryDemographicAdditional(benD,
							benRegID);

					Long benImageID = registrarServiceImpl.createBeneficiaryImage(benD, benRegID);

					if (benRegID > 0 && benDemoID > 0 && benPhonMapID > 0 && benbenDemoOtherID > 0 && benImageID > 0) {
						commonNurseServiceImpl.updateBeneficiaryStatus('R', benRegID);
						if (benData.getBeneficiaryID() != null) {
							response.setResponse(benData.getBeneficiaryID());
						} else {
							response.setResponse("Registration Done. Beneficiary ID is : " + benRegID);
						}
					} else {
						response.setError(500, "Something Went-Wrong");
					}
				} else {
					response.setError(500, "Something Went-Wrong");
				}
			}
			logger.info("createBeneficiary response:" + response);
		} catch (Exception e) {
			logger.error("Error in createBeneficiary :" + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Register a new beneficiary API")
	@PostMapping(value = { "/registrarBeneficaryRegistrationNew" })
	public String registrarBeneficaryRegistrationNew(@RequestBody String comingReq,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest request) {
		String s;
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			s = registrarServiceImpl.registerBeneficiary(comingReq, authorization, jwtToken);
			return s;
		} catch (Exception e) {
			logger.error("Error in registration" + e);
			response.setError(5000, "Error in registration; please contact administrator");
			return response.toString();
		}

	}

	@Operation(summary = "Update registered beneficiary data")
	@PostMapping(value = { "/update/BeneficiaryDetails" })
	public String updateBeneficiary(
			@ApiParam(value = "{\"benD\": {\"beneficiaryRegID\": \"Long\", \"firstName\": \"String\", \"lastName\": \"String\", \"gender\": \"Short\","
					+ "\"dob\": \"Timestamp\", \"maritalStatus\": \"Short\", \"fatherName\": \"String\", \"motherName\": \"String\","
					+ "\"husbandName\": \"String\", \"image\": \"String\", \"aadharNo\": \"String\", \"income\": \"Short\", "
					+ "\"literacyStatus\": \"String\", \"educationQualification\": \"Short\", \"occupation\": \"Short\", \"phoneNo\": \"String\","
					+ "\"emailID\": \"Integer\", \"bankName\": \"String\", \"branchName\": \"String\", \"IFSCCode\": \"String\", \"accountNumber\": \"String\","
					+ "\"community\": \"Short\", \"religion\": \"Short\", \"blockID\": \"Integer\", \"blockName\": \"String\", \"habitation\": \"String\", "
					+ "\"villageID\": \"Integer\", \"villageName\": \"String\", \"districtID\": \"Integer\", \"districtName\": \"String\", \"stateID\": \"Integer\", "
					+ "\"stateName\": \"String\", \"govID\": [{\"benGovMapID\": \"Long\", \"type\": \"String\",\"value\": \"String\"},"
					+ "{\"type\": \"String\",\"value\": \"String\"}], \"ageAtMarriage\": \"Integer\", \"createdBy\": \"String\", "
					+ "\"servicePointID\": \"Integer\", \"govtIdentityNo\": \"Integer\", \"govtIdentityTypeID\": \"Integer\", \"modifiedBy\": \"String\"}}") @RequestBody String comingRequest) {

		OutputResponse response = new OutputResponse();
		try {

			WrapperBeneficiaryRegistration wrapperBeneficiaryRegistrationOBJ = InputMapper.gson()
					.fromJson(comingRequest, WrapperBeneficiaryRegistration.class);
			logger.info("updateBeneficiary request:" + comingRequest);
			JsonObject benD = wrapperBeneficiaryRegistrationOBJ.getBenD();

			if (benD == null || benD.isJsonNull() || !benD.has("beneficiaryRegID")) {
				response.setError(0, "Data Not Sufficient...");
			} else {
				int benData = registrarServiceImpl.updateBeneficiary(benD);
				if (benData != 0 && !benD.get("beneficiaryRegID").isJsonNull()) {
					Long benRegID = benD.get("beneficiaryRegID").getAsLong();
					int benDemoUpdateRes = registrarServiceImpl.updateBeneficiaryDemographic(benD, benRegID);
					int benPhonMapUpdateRes = registrarServiceImpl.updateBeneficiaryPhoneMapping(benD, benRegID);

					registrarServiceImpl.updateBenGovIdMapping(benD, benRegID);

					int benbenDemoOtherUpdateRes = registrarServiceImpl.updateBeneficiaryDemographicAdditional(benD,
							benRegID);

					int benImageUpdateRes = registrarServiceImpl.updateBeneficiaryImage(benD, benRegID);

					if (benRegID >= 0 && benDemoUpdateRes >= 0 && benPhonMapUpdateRes >= 0
							&& benbenDemoOtherUpdateRes >= 0 && benImageUpdateRes >= 0) {
						commonNurseServiceImpl.updateBeneficiaryStatus('R', benRegID);
						response.setResponse("Beneficiary Details updated successfully!!!");

					} else {
						response.setError(500, "Something Went-Wrong");
					}
				} else {
					response.setError(500, "Something Went-Wrong");
				}
			}
			logger.info("updateBeneficiary response:" + response);
		} catch (Exception e) {
			logger.error("Error in updateBeneficiary :" + e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Registrar will submit a beneficiary to nurse for revisit")
	@PostMapping(value = { "/create/BenReVisitToNurse" })
	public String createReVisitForBenToNurse(@RequestBody String requestOBJ) {
		OutputResponse response = new OutputResponse();
		try {
			int i = registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(requestOBJ);
			if (i > 0) {
				if (i == 1)
					response.setResponse("Beneficiary moved to nurse worklist");
				else
					response.setError(5000, "Beneficiary already present in nurse worklist");
			} else {
				response.setError(5000, "Error while moving beneficiary to nurse worklist");
			}
		} catch (Exception e) {
			logger.error("Error while creating re-visit " + e);
			response.setError(5000, "Error while moving beneficiary to nurse worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Beneficiary edit, save or submit")
	@PostMapping(value = { "/update/BeneficiaryUpdate" })
	public String beneficiaryUpdate(@RequestBody String requestOBJ,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		Integer s = null;
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			s = registrarServiceImpl.updateBeneficiary(requestOBJ, authorization, jwtToken);
			if (s != null) {
				if (s == 1)
					response.setResponse("Beneficiary details updated successfully");
				else
					response.setResponse(
							"Beneficiary details updated successfully but already present in nurse work list");
			} else {
				response.setError(5000, "Error while updating beneficiary details");
			}
		} catch (Exception e) {
			response.setError(5000, "Error in beneficiary details update");
		}
		return response.toString();
	}

	@Operation(summary = "Get master data for registrar")
	@PostMapping(value = { "/registrarMasterData" })
	public String masterDataForRegistration(
			@ApiParam(value = "{\"spID\": \"Integer\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("spID")) {
				if (obj.getInt("spID") > 0) {
					response.setResponse(registrarServiceMasterDataImpl.getRegMasterData());
				} else {
					response.setError(5000, "Invalid service point");
				}
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("masterDataForRegistration response :" + response);

		} catch (Exception e) {
			logger.error("Error in masterDataForRegistration :" + e);
			response.setError(5000, "Error while getting master data for registration");
		}
		return response.toString();
	}
}
