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
package com.iemr.mmu.controller.covid19;

import java.util.HashMap;
import java.util.Map;

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
import com.iemr.mmu.service.covid19.Covid19Service;
import com.iemr.mmu.service.covid19.Covid19ServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 
 * @author DU20091017
 * @Saving covidq9 Data for nurse and Doctor
 * @Date : 25/06/2020
 *
 */

@RestController
@RequestMapping(value = "pandemic/covid", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class CovidController {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	@Autowired
	private Covid19Service covid19Service;
	@Autowired
	private Covid19ServiceImpl covid19ServiceImpl;

	@Operation(summary = "Save covid nurse data")
	@PostMapping(value = { "/save/nurseData" })
	public String saveBenCovid19NurseData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse outputResponse = new OutputResponse();

		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);
			if (jsnOBJ != null) {

				Long covid19Res = covid19Service.saveCovid19NurseData(jsnOBJ, authorization);
				if (null != covid19Res && covid19Res > 0) {
					outputResponse.setResponse("Data saved successfully");
				} else if (null != covid19Res && covid19Res == 0) {
					outputResponse.setResponse("Data already saved");
				} else {
					outputResponse.setResponse("Unable to save data");
				}
			} else {
				outputResponse.setError(5000, "Invalid Request !!!");
			}
		} catch (Exception e) {
			logger.error("Error while saving Pandemic nurse data :" + e);
			outputResponse.setError(5000, "Unable to save data");
		}

		return outputResponse.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @ApiParam Authorization
	 * @return
	 */

	@Operation(summary = "Save covid doctor data")
	@PostMapping(value = { "/save/doctorData" })
	public String saveBenCovidDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Long ncdCareRes = covid19Service.saveDoctorData(jsnOBJ, authorization);
				if (null != ncdCareRes && ncdCareRes > 0) {
					response.setResponse("Data saved successfully");
				} else {
					response.setResponse("Unable to save data");
				}

			} else {
				response.setResponse("Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error while saving Covid doctor data :" + e);
			response.setError(5000, "Unable to save data. " + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary visit details from nurse covid 19")
	@PostMapping(value = { "/getBenVisitDetailsFrmNurseCovid" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenVisitDetailsFrmNurseCovid19(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = covid19ServiceImpl.getBenVisitDetailsFrmNurseCovid19(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Covid 19 visit data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary visit data");
			logger.error("Error while getting beneficiary visit data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary history details enterted by nurse.
	 * @ApiParam comingRequest
	 * @return visit details in JSON format
	 */

	@Operation(summary = "Get beneficiary covid 19 history details from nurse to doctor ")
	@PostMapping(value = { "/getBenCovid19HistoryDetails" })

	public String getBenCovid19HistoryDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = covid19ServiceImpl.getBenCovid19HistoryDetails(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("NCD Care history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary history data");
			logger.error("Error while getting beneficiary history data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary vital details enterted by nurse.
	 * @ApiParam comingRequest
	 * @return visit details in JSON format
	 */

	@Operation(summary = "Get beneficiary covid 19 vital details from nurse NCD care")
	@PostMapping(value = { "/getBenVitalDetailsFrmNurseCovid" })
	public String getBenVitalDetailsFrmNurseNCDCare(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = covid19ServiceImpl.getBeneficiaryVitalDetails(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Covid 19 vital data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary vital data");
			logger.error("Error while getting beneficiary vital data :" + e);
		}
		return response.toString();

	}

	@Operation(summary = "Get beneficiary doctor entered details")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorCovid" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenCaseRecordFromDoctorCovid19(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = covid19ServiceImpl.getBenCaseRecordFromDoctorCovid19(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Covid 19 doctor data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary doctor data");
			logger.error("Error while getting beneficiary doctor data :" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Update history data in doctor screen")
	@PostMapping(value = { "/update/historyScreen" })
	public String updateHistoryNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = covid19ServiceImpl.updateBenHistoryDetails(jsnOBJ);
			if (result > 0) {
				Map<String, Integer> resMap = new HashMap<>();
				resMap.put("result", result);
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("History data update Response:" + response);
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
	 * @objective Replace NCD Care Vital Data entered by Nurse with the details
	 *            entered by Doctor
	 */

	@Operation(summary = "Update covid vital data in doctor screen")
	@PostMapping(value = { "/update/vitalScreen" })
	public String updateVitalNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = covid19ServiceImpl.updateBenVitalDetails(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Vital data update Response:" + response);
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
	 * @objective Replace covid 19 doctor data for the doctor next visit
	 */

	@Operation(summary = "Update covid 19 doctor data")
	@PostMapping(value = { "/update/doctorData" })
	public String updateCovid19DoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			Long result = covid19ServiceImpl.updateCovid19DoctorData(jsnOBJ, authorization);
			if (null != result && result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Doctor data update Response:" + response);
		} catch (Exception e) {
			response.setError(500, "Unable to modify data. " + e.getMessage());
			logger.error("Error while updating doctor data :" + e);
		}

		return response.toString();
	}

	private JsonObject parseJsonRequest(String requestObj) {
		JsonElement jsonElement = JsonParser.parseString(requestObj);
		return jsonElement.getAsJsonObject();
	}
}
