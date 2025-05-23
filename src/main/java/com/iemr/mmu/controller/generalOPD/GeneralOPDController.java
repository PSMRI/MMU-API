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
package com.iemr.mmu.controller.generalOPD;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.service.generalOPD.GeneralOPDServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;

/***
 * @Objective Saving General OPD data for Nurse and Doctor.
 */

@RestController

@RequestMapping(value = "/generalOPD", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class GeneralOPDController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private GeneralOPDServiceImpl generalOPDServiceImpl;

	@Autowired
	public void setGeneralOPDServiceImpl(GeneralOPDServiceImpl generalOPDServiceImpl) {
		this.generalOPDServiceImpl = generalOPDServiceImpl;
	}

	/**
	 * @Objective Save General OPD data for nurse.
	 * @ApiParam requestObj
	 * @return success or failure response
	 */

	@Operation(summary = "Save general OPD nurse data")
	@PostMapping(value = { "/save/nurseData" })
	public String saveBenGenOPDNurseData(@RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("Request object for GeneralOPD nurse data saving :" + requestObj);
			JsonObject jsonRequest = parseJsonRequest(requestObj);
			if (jsonRequest != null) {
				Long genOPDRes = generalOPDServiceImpl.saveNurseData(jsonRequest);
				if (null != genOPDRes && genOPDRes > 0) {
					response.setResponse("Data saved successfully");
				} else if (null != genOPDRes && genOPDRes == 0) {
					response.setResponse("Data already saved");
				} else {
					response.setResponse("Unable to save data");
				}

			} else {
				response.setResponse("Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error in nurse data saving :" + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	/**
	 * @Objective Save General OPD data for doctor.
	 * @ApiParam requestObj
	 * @return success or failure response
	 */

	@Operation(summary = "Save general OPD doctor data")
	@PostMapping(value = { "/save/doctorData" })
	public String saveBenGenOPDDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsonRequest = parseJsonRequest(requestObj);
			if (jsonRequest != null) {
				Long genOPDRes = generalOPDServiceImpl.saveDoctorData(jsonRequest, authorization);
				if (null != genOPDRes && genOPDRes > 0) {
					response.setResponse("Data saved successfully");
				} else {
					response.setResponse("Unable to save data");
				}

			} else {
				response.setResponse("Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error in doctor data saving :" + e);
			response.setError(5000, "Unable to save data. " + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary visit details from nurse general OPD")
	@PostMapping(value = { "/getBenVisitDetailsFrmNurseGOPD" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenVisitDetailsFrmNurseGOPD(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = generalOPDServiceImpl.getBenVisitDetailsFrmNurseGOPD(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenDataFrmNurseScrnToDocScrnVisitDetails response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary visit data");
			logger.error("Error in getBenDataFrmNurseScrnToDocScrnVisitDetails:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary history details enterted by nurse.
	 * @ApiParam comingRequest
	 * @return history details in JSON format
	 */

	@Operation(summary = "Get beneficiary general OPD history details from nurse to doctor ")
	@PostMapping(value = { "/getBenHistoryDetails" })

	public String getBenHistoryDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = generalOPDServiceImpl.getBenHistoryDetails(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenHistoryDetails response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary history data");
			logger.error("Error in getBenHistoryDetails:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary vital details enterted by nurse.
	 * @ApiParam comingRequest
	 * @return vital details in JSON format
	 */

	@Operation(summary = "Get beneficiary vital details from nurse general OPD")
	@PostMapping(value = { "/getBenVitalDetailsFrmNurse" })
	public String getBenVitalDetailsFrmNurse(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = generalOPDServiceImpl.getBeneficiaryVitalDetails(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenVitalDetailsFrmNurse response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary vital data");
			logger.error("Error in getBenVitalDetailsFrmNurse:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary examination details enterted by nurse.
	 * @ApiParam comingRequest
	 * @return examination details in JSON format
	 */

	@Operation(summary = "Get beneficiary general OPD examination details from nurse to doctor ")
	@PostMapping(value = { "/getBenExaminationDetails" })

	public String getBenExaminationDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = generalOPDServiceImpl.getExaminationDetailsData(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenExaminationDetails response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary examination data");
			logger.error("Error in getBenExaminationDetails:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary doctor details.
	 * @ApiParam comingRequest
	 * @return doctor details in JSON format
	 */

	@Operation(summary = "Get beneficiary doctor entered details")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorGeneralOPD" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenCaseRecordFromDoctorGeneralOPD(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = generalOPDServiceImpl.getBenCaseRecordFromDoctorGeneralOPD(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenCaseRecordFromDoctorGeneralOPD response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary doctor data");
			logger.error("Error in getBenCaseRecordFromDoctorGeneralOPD:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Update general OPD visit screen nurse data in doctor screen")
	@PostMapping(value = { "/update/visitDetailsScreen" })
	public String updateVisitNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsonRequest = parseJsonRequest(requestObj);

		try {
			int result = generalOPDServiceImpl.UpdateVisitDetails(jsonRequest);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Visit data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating visit data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace General OPD History Data entered by Nurse with the details
	 *            entered by Doctor
	 */

	@Operation(summary = "Update history data in doctor Screen")
	@PostMapping(value = { "/update/historyScreen" })
	public String updateHistoryNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();

		JsonObject jsonRequest = parseJsonRequest(requestObj);
		try {
			int result = generalOPDServiceImpl.updateBenHistoryDetails(jsonRequest);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("History data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating history data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace General OPD Vital Data entered by Nurse with the details
	 *            entered by Doctor
	 */

	@Operation(summary = "Update general OPD vital data in doctor screen")
	@PostMapping(value = { "/update/vitalScreen" })
	public String updateVitalNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsonRequest = parseJsonRequest(requestObj);

		try {
			int result = generalOPDServiceImpl.updateBenVitalDetails(jsonRequest);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Vital data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating vital data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace General OPD Examination Data entered by Nurse with the
	 *            details entered by Doctor
	 */

	@Operation(summary = "Update general OPD examination data in doctor screen")
	@PostMapping(value = { "/update/examinationScreen" })
	public String updateGeneralOPDExaminationNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsonRequest = parseJsonRequest(requestObj);

		try {
			int result = generalOPDServiceImpl.updateBenExaminationDetails(jsonRequest);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Examination data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating examination data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace General OPD doctor data for the doctor next visit
	 */

	@Operation(summary = "Update general OPD doctor data")
	@PostMapping(value = { "/update/doctorData" })
	public String updateGeneralOPDDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();
		JsonObject jsonRequest = parseJsonRequest(requestObj);

		try {
			Long result = generalOPDServiceImpl.updateGeneralOPDDoctorData(jsonRequest, authorization);
			if (null != result && result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Doctor data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data. " + e.getMessage());
			logger.error("Error while updating General OPD doctor data:" + e);
		}

		return response.toString();
	}

	private JsonObject parseJsonRequest(String requestObj) {
		JsonElement jsonElement = JsonParser.parseString(requestObj);
		return jsonElement.getAsJsonObject();
	}
}
