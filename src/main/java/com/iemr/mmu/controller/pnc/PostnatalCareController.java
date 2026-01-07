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
package com.iemr.mmu.controller.pnc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.service.pnc.PNCServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @Objective Saving PNC nurse and doctor data
 */
@RestController
@RequestMapping(value = "/PNC", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class PostnatalCareController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private PNCServiceImpl pncServiceImpl;

	@Autowired
	public void setPncServiceImpl(PNCServiceImpl pncServiceImpl) {
		this.pncServiceImpl = pncServiceImpl;
	}

	/**
	 * @Objective Saving PNC nurse data
	 * @ApiParam requestObj
	 * @return success or failure response
	 */
	@Operation(summary = "Save PNC nurse data")
	@PostMapping(value = { "/save/nurseData" })
	@PreAuthorize("hasRole('NURSE')")
	public String saveBenPNCNurseData(@RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Long ancRes = pncServiceImpl.savePNCNurseData(jsnOBJ);
				if (null != ancRes && ancRes > 0) {
					response.setResponse("Data saved successfully");
				} else if (null != ancRes && ancRes == 0) {
					response.setResponse("Data already saved");
				} else {
					response.setResponse("Unable to save data");
				}

			} else {
				response.setError(5000, "Invalid request");
			}

		} catch (Exception e) {
			logger.error("Error while saving nurse data :" + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	/**
	 * @Objective Saving PNC doctor data
	 * @ApiParam requestObj
	 * @return success or failure response
	 */
	@Operation(summary = "Save PNC doctor data")
	@PostMapping(value = { "/save/doctorData" })
	@PreAuthorize("hasRole('DOCTOR') ")
	public String saveBenPNCDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);
			if (jsnOBJ != null) {
				Long r = pncServiceImpl.savePNCDoctorData(jsnOBJ, authorization);
				if (r != null && r > 0) {
					// Extract drug IDs from JsonObject
					List<Long> prescribedDrugIDs = new ArrayList<>();
					if (jsnOBJ.has("savedDrugIDs") && !jsnOBJ.get("savedDrugIDs").isJsonNull()) {
						JsonArray drugIDsArray = jsnOBJ.getAsJsonArray("savedDrugIDs");
						for (int j = 0; j < drugIDsArray.size(); j++) {
							prescribedDrugIDs.add(drugIDsArray.get(j).getAsLong());
						}
					}

					// Create response with message and IDs
					Map<String, Object> responseData = new HashMap<>();
					responseData.put("message", "Data saved successfully");
					responseData.put("prescribedDrugIDs", prescribedDrugIDs);

					Gson gson = new Gson();
					String responseJson = gson.toJson(responseData);
					response.setResponse(responseJson);
				} else {
					response.setError(5000, "Unable to save data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}

		} catch (Exception e) {
			logger.error("Error while saving doctor data :" + e);
			response.setError(5000, "Unable to save data. " + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Get PNC beneficiary visit details from nurse")
	@PostMapping(value = { "/getBenVisitDetailsFrmNursePNC" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	@Transactional(rollbackFor = Exception.class)
	public String getBenVisitDetailsFrmNursePNC(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = pncServiceImpl.getBenVisitDetailsFrmNursePNC(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("PNC visit data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary visit data");
			logger.error("Error while getting beneficiary visit data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching Beneficiary PNC Care data entered by nurse
	 * @ApiParam comingRequest
	 * @return PNC Care data in JSON format
	 */
	@Operation(summary = "Get PNC beneficiary care details from nurse")
	@PostMapping(value = { "/getBenPNCDetailsFrmNursePNC" })
	@Transactional(rollbackFor = Exception.class)
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenPNCDetailsFrmNursePNC(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = pncServiceImpl.getBenPNCDetailsFrmNursePNC(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("PNC Care data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary PNC Care data");
			logger.error("Error while getting beneficiary PNC Care data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching Beneficiary history data entered by nurse
	 * @ApiParam comingRequest
	 * @return history data in JSON format
	 */
	@Operation(summary = "Get PNC beneficiary  history details from nurse to doctor ")
	@PostMapping(value = { "/getBenHistoryDetails" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenHistoryDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = pncServiceImpl.getBenHistoryDetails(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("History data fetch response :" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary history data");
			logger.error("Error while getting beneficiary history data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching Beneficiary vital data entered by nurse
	 * @ApiParam comingRequest
	 * @return vital data in JSON format
	 */
	@Operation(summary = "Get PNC beneficiary vital details from nurse")
	@PostMapping(value = { "/getBenVitalDetailsFrmNurse" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenVitalDetailsFrmNurse(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = pncServiceImpl.getBeneficiaryVitalDetails(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Vital data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary vital data");
			logger.error("Error while getting beneficiary vital data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching Beneficiary examination data entered by nurse
	 * @ApiParam comingRequest
	 * @return examination data in JSON format
	 */
	@Operation(summary = "Get PNC beneficiary examination details from nurse to doctor ")
	@PostMapping(value = { "/getBenExaminationDetailsPNC" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenExaminationDetailsPNC(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = pncServiceImpl.getPNCExaminationDetailsData(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("Examination data fetch response :" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary examination data");
			logger.error("Error while getting beneficiary examination data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching Beneficiary doctor data
	 * @ApiParam comingRequest
	 * @return doctor data in JSON format
	 */
	@Operation(summary = "Get PNC beneficiary case record")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorPNC" })
	@Transactional(rollbackFor = Exception.class)
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenCaseRecordFromDoctorPNC(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = pncServiceImpl.getBenCaseRecordFromDoctorPNC(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Doctor data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary doctor data");
			logger.error("Error while getting beneficiary doctor data:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Update PNC care data in doctor screen")
	@PostMapping(value = { "/update/PNCScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String updatePNCCareNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = pncServiceImpl.updateBenPNCDetails(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("PNC Care data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating PNC Care :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace PNC History Data entered by Nurse with the details entered
	 *            by Doctor
	 */
	@Operation(summary = "Update PNC beneficiary history in doctor screen")
	@PostMapping(value = { "/update/historyScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String updateHistoryNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = pncServiceImpl.updateBenHistoryDetails(jsnOBJ);
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
	 * @objective Replace PNC Vital Data entered by Nurse with the details entered
	 *            by Doctor
	 */
	@Operation(summary = "Update PNC beneficiary vitals in doctor screen")
	@PostMapping(value = { "/update/vitalScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String updateVitalNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = pncServiceImpl.updateBenVitalDetails(jsnOBJ);
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
	 * @objective Replace PNC Examination Data entered by Nurse with the details
	 *            entered by Doctor
	 */
	@Operation(summary = "Update PNC beneficiary examination data in doctor screen")
	@PostMapping(value = { "/update/examinationScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String updateGeneralOPDExaminationNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = pncServiceImpl.updateBenExaminationDetails(jsnOBJ);
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

	@Operation(summary = "Update PNC doctor data")
	@PostMapping(value = { "/update/doctorData" })
	@PreAuthorize("hasRole('DOCTOR') ")
	public String updatePNCDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			Long result = pncServiceImpl.updatePNCDoctorData(jsnOBJ, authorization);
			if (null != result && result > 0) {
				List<Long> prescribedDrugIDs = new ArrayList<>();
				if (jsnOBJ.has("savedDrugIDs") && !jsnOBJ.get("savedDrugIDs").isJsonNull()) {
					JsonArray drugIDsArray = jsnOBJ.getAsJsonArray("savedDrugIDs");
					for (int j = 0; j < drugIDsArray.size(); j++) {
						prescribedDrugIDs.add(drugIDsArray.get(j).getAsLong());
					}
				}

				// Create response with message and IDs
				Map<String, Object> responseData = new HashMap<>();
				responseData.put("message", "Data updated successfully");
				responseData.put("prescribedDrugIDs", prescribedDrugIDs);

				Gson gson = new Gson();
				String responseJson = gson.toJson(responseData);
				response.setResponse(responseJson);
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Doctor data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data. " + e.getMessage());
			logger.error("Error while updating doctor data :" + e);
		}

		return response.toString();
	}

	private JsonObject parseJsonRequest(String requestObj) {
		JsonElement jsonElement = JsonParser.parseString(requestObj);
		return jsonElement.getAsJsonObject();
	}
}
