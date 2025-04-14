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
package com.iemr.mmu.controller.anc;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.service.anc.ANCService;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


/**
 * @Objective Saving ANC data for Nurse and Doctor.
 */


@RestController
@RequestMapping(value = "/ANC", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class ANCController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	@Autowired
	private ANCService ancService;

	

	/**
	 * @Objective Save ANC data for nurse.
	 * @param JSON requestObj
	 * @return success or failure response
	 */
	@Operation(summary = "Save ANC nurse data")
	@PostMapping(value = { "/save/nurseData" })
	public String saveBenANCNurseData(@RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);
			if (jsnOBJ != null) {
				Long ancRes = ancService.saveANCNurseData(jsnOBJ);
				if (null != ancRes && ancRes > 0) {
					response.setResponse("Data saved successfully");
				} else if (null != ancRes && ancRes == 0) {
					response.setResponse("Data already saved");
				} else {
					response.setError(5000, "Unable to save data");
				}

			} else {
				response.setError(5000, "Invalid request");
			}

		} catch (Exception e) {
			logger.error("Error while saving nurse data:" + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	/**
	 * @Objective Save ANC data for doctor.
	 * @param JSON requestObj
	 * @return success or failure response
	 */
	@Operation(summary = "Save ANC doctor data")
	@PostMapping(value = { "/save/doctorData" })
	public String saveBenANCDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);
			if (jsnOBJ != null) {
				Long r = ancService.saveANCDoctorData(jsnOBJ, authorization);
				if (r != null && r > 0) {
					response.setResponse("Data saved successfully");
				} else {
					response.setError(5000, "Unable to save data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}

		} catch (Exception e) {
			logger.error("Error while saving doctor data:" + e);
			response.setError(5000, "Unable to save data. " + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary visit details from nurse ANC")
	@PostMapping(value = { "/getBenVisitDetailsFrmNurseANC" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenVisitDetailsFrmNurseANC(
			@Param(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");
				String res = ancService.getBenVisitDetailsFrmNurseANC(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("ANC visit data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary visit data");
			logger.error("Error while getting beneficiary visit data:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary anc care details entered by nurse.
	 * @param benRegID and benVisitID
	 * @return anc care details in JSON format
	 */
	@Operation(summary = "Get beneficiary ANC care details from nurse ANC")
	@PostMapping(value = { "/getBenANCDetailsFrmNurseANC" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenANCDetailsFrmNurseANC(
			@Param(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ancService.getBenANCDetailsFrmNurseANC(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("ANC Care data fetching response :" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary ANC care data");
			logger.error("Error while getting beneficiary ANC care data:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary history details entered by nurse.
	 * @param benRegID and benVisitID
	 * @return history details in JSON format
	 */
	@Operation(summary = "Get beneficiary ANC history details from nurse to doctor ")
	@PostMapping(value = { "/getBenANCHistoryDetails" })
	public String getBenANCHistoryDetails(
			@Param(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = ancService.getBenANCHistoryDetails(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("ANC history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary history data");
			logger.error("Error while getting beneficiary history data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary vital details entered by nurse.
	 * @param benRegID and benVisitID
	 * @return vital details in JSON format
	 */
	@Operation(summary = "Get beneficiary ANC vital details from nurse ANC")
	@PostMapping(value = { "/getBenANCVitalDetailsFrmNurseANC" })
	public String getBenANCVitalDetailsFrmNurseANC(
			@Param(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ancService.getBeneficiaryVitalDetails(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("ANC vital data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary vital data");
			logger.error("Error while getting beneficiary vital data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary examination details entered by nurse.
	 * @param benRegID and benVisitID
	 * @return examination details in JSON format
	 */
	@Operation(summary = "Get beneficiary ANC examination details from nurse to doctor ")
	@PostMapping(value = { "/getBenExaminationDetailsANC" })

	public String getBenExaminationDetailsANC(
			@Param(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = ancService.getANCExaminationDetailsData(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("ANC examination data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary examination data");
			logger.error("Error while getting beneficiary examination data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary details entered by doctor.
	 * @param benRegID and benVisitID
	 * @return doctor entered details in JSON format
	 */

	@Operation(summary = "Get beneficiary doctor entered details")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorANC" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenCaseRecordFromDoctorANC(
			@Param(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ancService.getBenCaseRecordFromDoctorANC(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("ANC doctor data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary doctor data");
			logger.error("Error while getting beneficiary doctor data :" + e);
		}
		return response.toString();
	}


	@Operation(summary = "Check high risk pregnancy status for ANC beneficiary")
	@PostMapping(value = { "/getHRPStatus" })
	@Transactional(rollbackFor = Exception.class)
	public String getHRPStatus(
			@Param(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ancService.getHRPStatus(benRegID, visitCode);
				if (res != null)
					response.setResponse(res);
				else
					response.setError(5000, "error in getting HRP status");
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			response.setError(5000, "error in getting HRP status");
			logger.error("error in getting HRP status : " + e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Update ANC care data in doctor screen")
	@PostMapping(value = { "/update/ANCScreen" })
	public String updateANCCareNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();

		JsonObject jsnOBJ = parseJsonRequest(requestObj);
		try {
			int result = ancService.updateBenANCDetails(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("ANC Care data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary ANC care data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @param requestObj
	 * @return success or failure response
	 * @objective Replace ANC History Data entered by Nurse with the details entered
	 *            by Doctor
	 */

	
	@Operation(summary = "Update ANC history data in doctor screen")
	@PostMapping(value = { "/update/historyScreen" })
	public String updateANCHistoryNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);
		try {
			int result = ancService.updateBenANCHistoryDetails(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("ANC history data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary history data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @param requestObj
	 * @return success or failure response
	 * @objective Replace ANC Vital Data entered by Nurse with the details entered
	 *            by Doctor
	 */

	
	@Operation(summary = "Update ANC vital data in doctor screen")
	@PostMapping(value = { "/update/vitalScreen" })
	public String updateANCVitalNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();

		JsonObject jsnOBJ = parseJsonRequest(requestObj);
		try {
			int result = ancService.updateBenANCVitalDetails(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("ANC vital data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary vital data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @param requestObj
	 * @return success or failure response
	 * @objective Replace ANC History Data entered by Nurse with the details entered
	 *            by Doctor
	 */

	
	@Operation(summary = "Update ANC examination data in doctor screen")
	@PostMapping(value = { "/update/examinationScreen" })
	public String updateANCExaminationNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = ancService.updateBenANCExaminationDetails(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("ANC examination data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary examination data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @param requestObj
	 * @return success or failure response
	 * @objective Replace ANC doctor data for the doctor next visit
	 * 
	 */
	
	@Operation(summary = "Update ANC doctor data")
	@PostMapping(value = { "/update/doctorData" })
	public String updateANCDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			Long result = ancService.updateANCDoctorData(jsnOBJ, authorization);
			if (null != result && result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("ANC doctor data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data. " + e.getMessage());
			logger.error("Error while updating beneficiary doctor data :" + e);
		}

		return response.toString();
	}
	private JsonObject parseJsonRequest(String requestObj) {
        JsonElement jsonElement = JsonParser.parseString(requestObj);
        return jsonElement.getAsJsonObject();
    }
}
