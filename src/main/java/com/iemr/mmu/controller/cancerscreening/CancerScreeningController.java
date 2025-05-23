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
package com.iemr.mmu.controller.cancerscreening;

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
import com.iemr.mmu.data.doctor.CancerDiagnosis;
import com.iemr.mmu.data.nurse.BenCancerVitalDetail;
import com.iemr.mmu.service.cancerScreening.CSService;
import com.iemr.mmu.service.cancerScreening.CSServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @Objective Saving Cancer screening data for Nurse and Doctor both.
 */

@RestController
@RequestMapping(value = "/CS-cancerScreening", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class CancerScreeningController {
	private Logger logger = LoggerFactory.getLogger(CancerScreeningController.class);
	private CSService cSService;

	@Autowired
	public void setCancerScreeningServiceImpl(CSServiceImpl cSServiceImpl) {
		this.cSService = cSServiceImpl;
	}

	/**
	 * @Objective Save Cancer Screening data for nurse.
	 * @ApiParam JSON requestObj
	 * @return success or failure response
	 */

	@Operation(summary = "Save cancer screening nurse data")
	@PostMapping(value = { "/save/nurseData" })
	public String saveBenCancerScreeningNurseData(@RequestBody String requestObj,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Long nurseDataSaveSuccessFlag = cSService.saveCancerScreeningNurseData(jsnOBJ, authorization);
				if (nurseDataSaveSuccessFlag != null && nurseDataSaveSuccessFlag > 0) {
					if (nurseDataSaveSuccessFlag == 1)
						response.setResponse("Data saved successfully");
					else if (nurseDataSaveSuccessFlag == 2)
						response.setResponse("Data saved and MAMMOGRAM order created successfully");
					else
						response.setError(9999,
								"Data saved successfully but 'error in mammogram order creation';please contact administrator");
				} else if (nurseDataSaveSuccessFlag != null && nurseDataSaveSuccessFlag == 0) {
					response.setResponse("Data already saved");
				} else {
					response.setError(5000, "Unable to save data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}

		} catch (Exception e) {
			logger.error("Error while saving beneficiary nurse data :" + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	/**
	 * @Objective Save Cancer Screening data for doctor.
	 * @ApiParam JSON requestObj
	 * @return success or failure response
	 */

	@Operation(summary = "Save cancer screening doctor data")
	@PostMapping(value = { "/save/doctorData" })
	public String saveBenCancerScreeningDoctorData(@RequestBody String requestObj,
			@RequestHeader String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			JsonObject jsnOBJ = parseJsonRequest(requestObj);

			if (jsnOBJ != null) {
				Long csDocDataSaveSuccessFlag = cSService.saveCancerScreeningDoctorData(jsnOBJ, authorization);
				if (csDocDataSaveSuccessFlag != null && csDocDataSaveSuccessFlag > 0) {
					response.setResponse("Data saved successfully");
				} else {
					response.setError(5000, "Unable to save data");
				}
			} else {
				response.setError(5000, "Invalid request");
			}

		} catch (Exception e) {
			logger.error("Error while saving beneficiary doctor data :" + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary visit details from nurse screen")
	@PostMapping(value = { "/getBenDataFrmNurseToDocVisitDetailsScreen" })
	public String getBenDataFrmNurseScrnToDocScrnVisitDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");
				String s = cSService.getBenDataFrmNurseToDocVisitDetailsScreen(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("CS visit data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary visit data");
			logger.error("Error while getting beneficiary visit data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary history details enterted by nurse.
	 * @ApiParam benRegID and benVisitID
	 * @return history details in JSON format
	 */

	@Operation(summary = "Get beneficiary cancer history details from nurse screen")
	@PostMapping(value = { "/getBenDataFrmNurseToDocHistoryScreen" })
	public String getBenDataFrmNurseScrnToDocScrnHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");
				String s = cSService.getBenDataFrmNurseToDocHistoryScreen(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("CS history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary history data");
			logger.error("Error while getting beneficiary history data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary vital details entered by nurse.
	 * @ApiParam benRegID and benVisitID
	 * @return vital details in JSON format
	 */

	@Operation(summary = "Get beneficiary vital details from nurse screen")
	@PostMapping(value = { "/getBenDataFrmNurseToDocVitalScreen" })
	public String getBenDataFrmNurseScrnToDocScrnVital(
			@ApiParam(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");

				String s = cSService.getBenDataFrmNurseToDocVitalScreen(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("CS vital data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary vital data");
			logger.error("Error while getting beneficiary vital data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetching beneficiary examination details entered by nurse.
	 * @ApiParam benRegID and benVisitID
	 * @return examination details in JSON format
	 */

	@Operation(summary = "Get beneficiary examination details from nurse screen")
	@PostMapping(value = { "/getBenDataFrmNurseToDocExaminationScreen" })
	public String getBenDataFrmNurseScrnToDocScrnExamination(
			@ApiParam(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");
				String s = cSService.getBenDataFrmNurseToDocExaminationScreen(benRegID, visitCode);
				response.setResponse(s);
			} else {
				response.setError(5000, "Invalid request");
			}
			logger.info("CS examination data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary examination data");
			logger.error("Error while getting beneficiary examination data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetch beneficiary previous family history details enterted by
	 *            nurse.
	 * @ApiParam benRegID
	 * @return previous family history details in JSON format
	 */

	@Operation(summary = "Get beneficiary cancer family history")
	@PostMapping(value = { "/getBenCancerFamilyHistory" })
	public String getBenCancerFamilyHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = cSService.getBenFamilyHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("CS family history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary family history data");
			logger.error("Error while getting beneficiary family history data:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetch beneficiary previous personal history details enterted by
	 *            nurse.
	 * @ApiParam benRegID
	 * @return previous personal history details in JSON format
	 */

	@Operation(summary = "Get beneficiary cancer personal history")
	@PostMapping(value = { "/getBenCancerPersonalHistory" })
	public String getBenCancerPersonalHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = cSService.getBenPersonalHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("CS personal history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary personal history data");
			logger.error("Error while getting beneficiary personal history data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetch beneficiary previous personal diet history details enterted
	 *            by nurse.
	 * @ApiParam benRegID
	 * @return previous personal history details in JSON format
	 */

	@Operation(summary = "Get beneficiary cancer personal diet history")
	@PostMapping(value = { "/getBenCancerPersonalDietHistory" })
	public String getBenCancerPersonalDietHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = cSService.getBenPersonalDietHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("CS personal diet history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary personal diet history data");
			logger.error("Error while getting beneficiary personal diet history data :" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetch beneficiary previous obstetric history details enterted by
	 *            nurse.
	 * @ApiParam benRegID
	 * @return previous obstetric history details in JSON format
	 */

	@Operation(summary = "Get beneficiary cancer obstetric history")
	@PostMapping(value = { "/getBenCancerObstetricHistory" })
	public String getBenCancerObstetricHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = cSService.getBenObstetricHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("CS obstetric history data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary obstetric history data");
			logger.error("Error while getting beneficiary obstetric history data:" + e);
		}
		return response.toString();
	}

	/**
	 * @Objective Fetch beneficiary diagnosis details enterted by doctor.
	 * @ApiParam benRegID
	 * @return doctor details in JSON format
	 */

	@Operation(summary = "Get beneficiary doctor entered details")
	@PostMapping(value = { "/getBenCaseRecordFromDoctorCS" })
	@Transactional(rollbackFor = Exception.class)
	public String getBenCaseRecordFromDoctorCS(
			@ApiParam(value = "{\"benRegID\":\"Long\", \"visitCode\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.length() > 1 && obj.has("benRegID") && obj.has("visitCode")) {
				Long benRegID = obj.getLong("benRegID");
				Long visitCode = obj.getLong("visitCode");
				String res = cSService.getBenCaseRecordFromDoctorCS(benRegID, visitCode);
				response.setResponse(res);
			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("CS doctor data fetching Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting beneficiary doctor data");
			logger.error("Error while getting beneficiary doctor data :" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Update cancer screening history nurse data in doctor screen")
	@PostMapping(value = { "/update/historyScreen" })
	public String updateCSHistoryNurse(
			@ApiParam(value = "{\"historyDetails\": {\"familyHistory\":{\"diseases\": [{\"beneficiaryRegID\":\"Long\", \"benVisitID\":\"Long\", "
					+ "\"providerServiceMapID\":\"Integer\", \"cancerDiseaseType\":\"String\", \"otherDiseaseType\":\"String\", \"familyMemberList\":\"List\", "
					+ "\"createdBy\":\"String\"}]}, \"personalHistory\":{\"beneficiaryRegID\":\"Long\",\"benVisitID\":\"Long\", \"providerServiceMapID\":\"Integer\", "
					+ "\"tobaccoUse\":\"String\", \"startAge_year\":\"Integer\", \"endAge_year\":\"Integer\", \"typeOfTobaccoProductList\":\"List\", "
					+ "\"quantityPerDay\":\"Integer\", \"isFilteredCigaerette\":\"Boolean\", \"isCigaretteExposure\":\"Boolean\", \"isBetelNutChewing\":\"Boolean\", "
					+ "\"durationOfBetelQuid\":\"Integer\", \"alcoholUse\":\"String\", \"ssAlcoholUsed\":\"Boolean\", \"frequencyOfAlcoholUsed\":\"String\", "
					+ "\"dietType\":\"String\", \"intakeOfOutsidePreparedMeal\":\"Integer\", \"fruitConsumptionDays\":\"Integer\", \"fruitQuantityPerDay\":\"Integer\", "
					+ "\"vegetableConsumptionDays\":\"Integer\", \"vegetableQuantityPerDay\":\"Integer\", \"typeOfOilConsumedList\":\"List\","
					+ "\"physicalActivityType\":\"String\", \"ssRadiationExposure\":\"Boolean\", \"isThyroidDisorder\":\"Boolean\", \"createdBy\":\"String\"}"
					+ "\"pastObstetricHistory\":{\"beneficiaryRegID\":\"Long\", \"benVisitID\":\"Long\", \"providerServiceMapID\":\"Integer\", "
					+ "\"pregnancyStatus\":\"String\", \"isUrinePregTest\":\"Boolean\", \"pregnant_No\":\"String\",\"noOfLivingChild\":\"Integer\","
					+ "\"isAbortion\":\"Boolean\",\"isOralContraceptiveUsed\":\"Boolean\", \"isHormoneReplacementTherapy\":\"Boolean\",\"menarche_Age\":\"Integer\", "
					+ "\"isMenstrualCycleRegular\":\"Boolean\",\"menstrualCycleLength\":\"Integer\", \"menstrualFlowDuration\":\"Integer\", "
					+ "\"menstrualFlowType\":\"String\", \"isDysmenorrhea\":\"Boolean\", \"isInterMenstrualBleeding\":\"Boolean\", "
					+ "\"menopauseAge\":\"Integer\", \"isPostMenopauseBleeding\":\"Boolean\", \"createdBy\":\"String\"}}}") @RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);
		try {
			int result = cSService.UpdateCSHistoryNurseData(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("CS history data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary history data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace Cancer Screening Vital Details entered by Nurse with the
	 *            details entered by Doctor
	 */

	@Operation(summary = "Update beneficiary vital detail")
	@PostMapping(value = { "/update/vitalScreen" })
	public String upodateBenVitalDetail(
			@ApiParam(value = "{\"ID\": \"Long\", \"beneficiaryRegID\":\"Long\",\"benVisitID\":\"Long\","
					+ "\"weight_Kg\":\"Double\", \"height_cm\":\"Double\", \"waistCircumference_cm\":\"Double\", \"bloodGlucose_Fasting\":\"Short\","
					+ "\"bloodGlucose_Random\":\"Short\", \"bloodGlucose_2HrPostPrandial\":\"Short\", \"systolicBP_1stReading\":\"Short\", "
					+ "\"diastolicBP_1stReading\":\"Short\", \"systolicBP_2ndReading\":\"Short\", \"diastolicBP_2ndReading\":\"Short\", "
					+ "\"systolicBP_3rdReading\":\"Short\", \"diastolicBP_3rdReading\":\"Short\","
					+ " \"hbA1C\":\"Short\",\"hemoglobin\":\"Short\",\"modifiedBy\":\"String\"}") @RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		try {
			BenCancerVitalDetail benCancerVitalDetail = InputMapper.gson().fromJson(requestObj,
					BenCancerVitalDetail.class);
			int responseObj = cSService.updateBenVitalDetail(benCancerVitalDetail);
			if (responseObj > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("CS vital data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary vital data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace Cancer Screening examination Details entered by Nurse with
	 *            the details entered by Doctor
	 */

	@Operation(summary = "Update beneficiary examination detail")
	@PostMapping(value = { "/update/examinationScreen" })
	public String upodateBenExaminationDetail(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);
		try {
			int responseObj = cSService.updateBenExaminationDetail(jsnOBJ);
			if (responseObj > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("CS examination data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary examination data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective update Cancer Diagnosis Details By Oncologist
	 * 
	 */

	@Operation(summary = "Update cancer diagnosis details by oncologist")
	@PostMapping(value = { "/update/examinationScreen/diagnosis" })
	public String updateCancerDiagnosisDetailsByOncologist(
			@ApiParam(value = "{\"beneficiaryRegID\":\"Long\", \"benVisitID\":\"Long\", \"visitCode\":\"Long\", "
					+ "\"provisionalDiagnosisOncologist\":\"String\", \"modifiedBy\":\"string\"}") @RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		try {
			CancerDiagnosis cancerDiagnosis = InputMapper.gson().fromJson(requestObj, CancerDiagnosis.class);
			int result = cSService.updateCancerDiagnosisDetailsByOncologist(cancerDiagnosis);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(5000, "Unable to modify data");
			}
			logger.info("CS diagnosis data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Unable to modify data");
			logger.error("Error while updating beneficiary diagnosis data :" + e);
		}

		return response.toString();
	}

	/**
	 * 
	 * @ApiParam requestObj
	 * @return success or failure response
	 * @objective Replace doctor data for the doctor next visit
	 * 
	 */

	@Operation(summary = "Update cancer screening doctor data")
	@PostMapping(value = { "/update/doctorData" })
	public String updateCancerScreeningDoctorData(@RequestBody String requestObj) {

		OutputResponse response = new OutputResponse();
		JsonObject jsnOBJ = parseJsonRequest(requestObj);
		try {
			int result = cSService.updateCancerScreeningDoctorData(jsnOBJ);
			if (result > 0) {
				response.setResponse("Data updated successfully");
			} else {
				response.setError(500, "Unable to modify data");
			}
			logger.info("CS doctor data update Response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while updating beneficiary data. " + e);
			logger.error("Error while updating beneficiary doctor data :" + e);
		}

		return response.toString();
	}

	private JsonObject parseJsonRequest(String requestObj) {
		JsonElement jsonElement = JsonParser.parseString(requestObj);
		return jsonElement.getAsJsonObject();
	}
}
