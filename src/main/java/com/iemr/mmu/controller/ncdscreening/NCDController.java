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
package com.iemr.mmu.controller.ncdscreening;

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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.iemr.mmu.service.ncdscreening.NCDSCreeningDoctorService;
import com.iemr.mmu.service.ncdscreening.NCDScreeningService;
import com.iemr.mmu.service.ncdscreening.NCDScreeningServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @Objective Saving NCD Screening nurse data.
 */
@RestController
@RequestMapping(value = "/NCD", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class NCDController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private NCDScreeningServiceImpl ncdScreeningServiceImpl;

	@Autowired
	public void setNcdScreeningServiceImpl(NCDScreeningServiceImpl ncdScreeningServiceImpl) {
		this.ncdScreeningServiceImpl = ncdScreeningServiceImpl;
	}

	@Autowired
	private NCDScreeningService ncdScreeningService;

	@Autowired
	private NCDSCreeningDoctorService ncdSCreeningDoctorService;

	/**
	 * @Objective Save NCD Screening data for nurse.
	 * @ApiParam JSON requestObj
	 * @return success or failure response
	 */
	@Operation(summary = "Save beneficiary NCD screening details")
	@PostMapping(value = { "/save/nurseData" })
	@PreAuthorize("hasRole('NURSE')")
	public String saveBeneficiaryNCDScreeningDetails(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();

		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Long r = ncdScreeningServiceImpl.saveNCDScreeningNurseData(jsnOBJ, authorization);
				if (r != null && r > 0) {
					response.setResponse("Data saved successfully");
				} else if (r != null && r == 0) {
					response.setResponse("Data already saved");
				} else {
					response.setError(5000, "Unable to save data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			response.setError(5000, "Unable to save data");
			logger.error("Error while storing NCD Screening nurse data: " + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Save NCD Screening data for doctor.
	 * @ApiParam JSON requestObj
	 * @return success or failure response
	 */
	@Operation(summary = "Save NCD screening doctor data")
	@PostMapping(value = { "/save/doctorData" })
	@PreAuthorize("hasRole('DOCTOR')")
	public String saveBenNCDScreeningDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("Request object for NCD Screening doctor data saving :" + requestObj);

			JsonObject jsnOBJ = parseJsonRequest(requestObj);
			if (jsnOBJ != null) {
				Long ncdCareRes = ncdScreeningServiceImpl.saveDoctorData(jsnOBJ, authorization);
				if (null != ncdCareRes && ncdCareRes > 0) {
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
					response.setResponse("Unable to save data");
				}

			} else {
				response.setResponse("Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error while saving NCD Screening doctor data :" + e);
			response.setError(5000, "Unable to save data. " + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Get NCD screening visit details")
	@PostMapping(value = { "/get/nurseData" })
	@PreAuthorize("hasRole('NURSE')")
	public String getNCDScreenigDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {

		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String screeningDetails = ncdScreeningServiceImpl.getNCDScreeningDetails(benRegID, visitCode);
				response.setResponse(screeningDetails);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("NCD Screening nurse data fetch response :" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting NCD Screening data");
			logger.error("Error while getting NCD Screening data :" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get NCD screening visit count for beneficiary registration id")
	@GetMapping(value = { "/getNcdScreeningVisitCount/{beneficiaryRegID}" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String getNcdScreeningVisitCount(@PathVariable("beneficiaryRegID") Long beneficiaryRegID) {
		OutputResponse response = new OutputResponse();
		try {
			if (beneficiaryRegID != null) {
				String s = ncdScreeningServiceImpl.getNcdScreeningVisitCnt(beneficiaryRegID);
				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error while getting NCD screening Visit Count");

			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			response.setError(5000, "Error while getting NCD screening Visit Count");
			logger.error("Error while getting NCD screening Visit Count" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary visit details from nurse NCD screening")
	@PostMapping(value = { "/getBenVisitDetailsFrmNurseNCDScreening" })
	@Transactional(rollbackFor = Exception.class)
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String getBenVisitDetailsFrmNurseGOPD(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ncdScreeningServiceImpl.getBenVisitDetailsFrmNurseNCDScreening(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenVisitDetailsFrmNurseNCDScreening response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary visit data");
			logger.error("Error in getBenVisitDetailsFrmNurseNCDScreening:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary general OPD history details from nurse to doctor ")
	@PostMapping(value = { "/getBenHistoryDetails" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String getBenHistoryDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = ncdScreeningServiceImpl.getBenHistoryDetails(benRegID, visitCode);
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

	@Operation(summary = "Get beneficiary vital details from nurse general OPD")
	@PostMapping(value = { "/getBenVitalDetailsFrmNurse" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String getBenVitalDetailsFrmNurse(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ncdScreeningServiceImpl.getBeneficiaryVitalDetails(benRegID, visitCode);
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

	@Operation(summary = "Get beneficiary vital details from nurse general OPD")
	@PostMapping(value = { "/getBenIdrsDetailsFrmNurse" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String getBenIdrsDetailsFrmNurse(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ncdScreeningServiceImpl.getBenIdrsDetailsFrmNurse(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenIdrsDetailsFrmNurse response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary Idrs data");
			logger.error("Error in getBenIdrsDetailsFrmNurse:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary doctor details.
	 * @ApiParam comingRequest
	 * @return visit details in JSON format
	 */
	@Operation(summary = "Get beneficiary doctor entered details")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorNCDScreening" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	@Transactional(rollbackFor = Exception.class)
	public String getBenCaseRecordFromDoctorNCDCare(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = ncdScreeningServiceImpl.getBenCaseRecordFromDoctorNCDScreening(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("NCD Screening doctor data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary doctor data");
			logger.error("Error while getting beneficiary doctor data :" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Update beneficiary NCD screening details")
	@PostMapping(value = { "/update/nurseData" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String updateBeneficiaryNCDScreeningDetails(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();

		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Integer r = ncdScreeningServiceImpl.updateNurseNCDScreeningDetails(jsnOBJ);
				if (r != null && r == 1) {
					response.setResponse("Data updated successfully");
				} else {
					response.setError(5000, "Unable to modify data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error in updating beneficiary NCD screening data: " + e);
		}
		logger.info("Update NCDScreening Details response:" + response);
		return response.toString();
	}

	@Operation(summary = "Update history data in doctor screen")
	@PostMapping(value = { "/update/historyScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String updateHistoryNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		logger.info("Request object for history data updating :" + requestObj);
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = ncdScreeningService.UpdateNCDScreeningHistory(jsnOBJ);
			if (result > 0) {
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

	@Operation(summary = "Update NCD screening vital data in doctor screen")
	@PostMapping(value = { "/update/vitalScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String updateVitalNurse(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			int result = ncdScreeningServiceImpl.updateBenVitalDetails(jsnOBJ);
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

	@Operation(summary = "Update history data in doctor screen")
	@PostMapping(value = { "/update/idrsScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR')")
	public String updateIDRSScreen(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);

		try {
			Long result = ncdScreeningService.UpdateIDRSScreen(jsnOBJ);
			if (result != null && result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("IDRS data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating history data :" + e);
		}

		return response.toString();
	}

	@Operation(summary = "Update doctor data")
	@PostMapping(value = { "/update/doctorData" })
	@PreAuthorize("hasRole('DOCTOR')")
	public String updateDoctorData(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		logger.info("Request object for doctor data update :" + requestObj);

		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);
			int i = ncdSCreeningDoctorService.updateDoctorData(jsnOBJ);
			if (i > 0) {
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
				responseData.put("message", "Data updated successfully");
				responseData.put("prescribedDrugIDs", prescribedDrugIDs);

				Gson gson = new Gson();
				String responseJson = gson.toJson(responseData);
				response.setResponse(responseJson);
			} else {
				response.setError(5000, "Error in data update");
			}
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating doctor data :" + e);
		}

		return response.toString();
	}

	private JsonObject parseJsonRequest(String requestObj) {
		JsonElement jsonElement = JsonParser.parseString(requestObj);
		return jsonElement.getAsJsonObject();
	}
}
