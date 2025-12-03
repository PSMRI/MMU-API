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
package com.iemr.mmu.controller.quickconsult;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.quickConsultation.WrapperQuickConsultation;
import com.iemr.mmu.service.quickConsultation.QuickConsultationServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;

import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * @Objective Saving general OPD quick consult data for Nurse and Doctor both.
 */
@RestController
@RequestMapping(value = "/genOPD-QC-quickConsult", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class QuickConsultController {
	private Logger logger = LoggerFactory.getLogger(QuickConsultController.class);

	private QuickConsultationServiceImpl quickConsultationServiceImpl;

	@Autowired
	public void setQuickConsultationServiceImpl(QuickConsultationServiceImpl quickConsultationServiceImpl) {
		this.quickConsultationServiceImpl = quickConsultationServiceImpl;
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective first data will be pushed to BenVisitDetails Table and benVisitID
	 *            will be generated and then this benVisitID will be patched with
	 *            Beneficiary Vital and Anthropometry Detail Object and pushed to
	 *            Database table
	 */
	@Operation(summary = "Save quick consult nurse data")
	@PostMapping(value = { "/save/nurseData" })
	@PreAuthorize("hasRole('NURSE') ")
	public String saveBenQuickConsultDataNurse(@RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Integer r = quickConsultationServiceImpl.quickConsultNurseDataInsert(jsnOBJ);
				if (r == 1) {
					response.setResponse("Data saved successfully");
				} else if (r == 3) {
					response.setResponse("Data already saved");
				} else {
					response.setError(500, "Unable to save data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error while saving quick consult nurse data: " + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Save beneficiary data for doctor quick consult - QC.
	 */
	@Operation(summary = "Save quick consultation detail for doctor")
	@PostMapping(value = { "/save/doctorData" })
	@PreAuthorize("hasRole('DOCTOR') ")
	public String saveQuickConsultationDetail(
			@ApiParam(value = "{\"quickConsultation\":{\"beneficiaryRegID\":\"Long\",\"providerServiceMapID\": \"Integer\", \"benVisitID\":\"Long\", \"benChiefComplaint\":[{\"chiefComplaintID\":\"Integer\", "
					+ "\"chiefComplaint\":\"String\", \"duration\":\"Integer\", \"unitOfDuration\":\"String\"}], \"description\":\"String\""
					+ "\"clinicalObservation\":\"String\", \"externalInvestigation\":\"String\", \"diagnosisProvided\":\"String\", \"instruction\":\"String\", \"remarks\":\"String\","
					+ "\"prescribedDrugs\":[{\"drugForm\":\"String\", \"drugTradeOrBrandName\":\"String\", \"genericDrugName\":\"String\", \"drugStrength\":\"String\", "
					+ "\"dose\":\"String\", \"route\":\"String\", \"frequency\":\"String\", \"drugDuration\":\"String\", \"relationToFood\":\"String\", "
					+ "\"specialInstruction\":\"String\"}], \"labTestOrders\":[{\"testID\":\"String\", \"testName\":\"String\", \"testingRequirements\":\"String\","
					+ " \"isRadiologyImaging\":\"String\", \"createdBy\":\"String\"}, {\"testID\":\"Integer\", \"testName\":\"String\", "
					+ "\"testingRequirements\":\"String\", \"isRadiologyImaging\":\"Boolean\"}],"
					+ "\"createdBy\":\"String\"}}") @RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();

		try {
			WrapperQuickConsultation wrapperQuickConsultation = InputMapper.gson().fromJson(requestObj,
					WrapperQuickConsultation.class);

			JsonObject quickConsultDoctorOBJ = wrapperQuickConsultation.getQuickConsultation();
			Integer i = quickConsultationServiceImpl.quickConsultDoctorDataInsert(quickConsultDoctorOBJ, authorization);

			if (i != null && i > 0) {
				// Check if drug IDs were saved and added to the JsonObject
				List<Long> prescribedDrugIDs = new ArrayList<>();
				if (quickConsultDoctorOBJ.has("savedDrugIDs")
						&& !quickConsultDoctorOBJ.get("savedDrugIDs").isJsonNull()) {
					JsonArray drugIDsArray = quickConsultDoctorOBJ.getAsJsonArray("savedDrugIDs");
					for (int j = 0; j < drugIDsArray.size(); j++) {
						prescribedDrugIDs.add(drugIDsArray.get(j).getAsLong());
					}
				}

				// Create response data with message and drug IDs
				Map<String, Object> responseData = new HashMap<>();
				responseData.put("message", "Data saved successfully");
				responseData.put("prescribedDrugIDs", prescribedDrugIDs);

				// Convert to JSON string and set response
				Gson gson = new Gson();
				String responseJson = gson.toJson(responseData);
				response.setResponse(responseJson);
			} else {
				response.setError(5000, "Unable to save data");
			}
			logger.info("Quick consult doctor data save response:" + response);
		} catch (Exception e) {
			logger.error("Error while saving quick consult doctor data:" + e);
			response.setError(5000, "Unable to save data. " + e.getMessage());
		}

		return response.toString();
	}

	@Operation(summary = "Get quick consult beneficiary visit details")
	@PostMapping(value = { "/getBenDataFrmNurseToDocVisitDetailsScreen" })
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenDataFrmNurseScrnToDocScrnVisitDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = quickConsultationServiceImpl.getBenDataFrmNurseToDocVisitDetailsScreen(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("Quick consult visit data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting visit data");
			logger.error("Error while fetching quick consult visit data:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary vital data filled by Nurse for Doctor
	 *            screen...
	 * @ApiParam benRegID and benVisitID
	 * @return visit details in JSON format
	 */
	@Operation(summary = "Get quick consult beneficiary vital details")
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

				String res = quickConsultationServiceImpl.getBeneficiaryVitalDetails(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Quick consult vital data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting vital data");
			logger.error("Error while fetching quick consult vital data:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary doctor data
	 * @ApiParam benRegID and benVisitID
	 * @return visit details in JSON format
	 */
	@Operation(summary = "Get quick consult beneficiary case record")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorQuickConsult" })
	@Transactional(rollbackFor = Exception.class)
	@PreAuthorize("hasRole('NURSE') || hasRole('DOCTOR') ")
	public String getBenCaseRecordFromDoctorQuickConsult(
			@ApiParam(value = "{\"benRegID\":\"Long\",\"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String res = quickConsultationServiceImpl.getBenCaseRecordFromDoctorQuickConsult(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid request");
				response.setError(5000, "Invalid request");
			}
			logger.info("Quick consult doctor data fetch response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting doctor data");
			logger.error("Error while fetching quick consult doctor data:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Update quick consult doctor data")
	@PostMapping(value = { "/update/doctorData" })
	@PreAuthorize("hasRole('DOCTOR') ")
	public String updateGeneralOPDQCDoctorData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {

		OutputResponse response = new OutputResponse();

		try {
			WrapperQuickConsultation wrapperQuickConsultation = InputMapper.gson().fromJson(requestObj,
					WrapperQuickConsultation.class);

			JsonObject quickConsultDoctorOBJ = wrapperQuickConsultation.getQuickConsultation();

			Long result = quickConsultationServiceImpl.updateGeneralOPDQCDoctorData(quickConsultDoctorOBJ,
					authorization);
			if (null != result && result > 0) {
				// Check if drug IDs were saved and added to the JsonObject
				List<Long> prescribedDrugIDs = new ArrayList<>();
				if (quickConsultDoctorOBJ.has("savedDrugIDs")
						&& !quickConsultDoctorOBJ.get("savedDrugIDs").isJsonNull()) {
					JsonArray drugIDsArray = quickConsultDoctorOBJ.getAsJsonArray("savedDrugIDs");
					for (int j = 0; j < drugIDsArray.size(); j++) {
						prescribedDrugIDs.add(drugIDsArray.get(j).getAsLong());
					}
				}

				// Create response data with message and drug IDs
				Map<String, Object> responseData = new HashMap<>();
				responseData.put("message", "Data updated successfully");
				responseData.put("prescribedDrugIDs", prescribedDrugIDs);

				// Convert to JSON string and set response
				Gson gson = new Gson();
				String responseJson = gson.toJson(responseData);
				response.setResponse(responseJson);
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("Quick consult doctor data update response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating quick consult doctor data :" + e);
		}

		return response.toString();
	}

	private JsonObject parseJsonRequest(String requestObj) {
		JsonElement jsonElement = JsonParser.parseString(requestObj);
		return jsonElement.getAsJsonObject();
	}
}
