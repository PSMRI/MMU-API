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
package com.iemr.mmu.controller.common.main;

import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iemr.mmu.utils.JwtUtil;

import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonServiceImpl;
import com.iemr.mmu.utils.MediaTypeUtils;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;
import com.iemr.mmu.utils.CookieUtil;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/common", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class CommonController {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	private CommonNurseServiceImpl commonNurseServiceImpl;
	private CommonServiceImpl commonServiceImpl;
	private InputMapper inputMapper = new InputMapper();
	@Autowired
	private ServletContext servletContext;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AESEncryptionDecryption aESEncryptionDecryption;

	@Autowired
	public void setCommonServiceImpl(CommonServiceImpl commonServiceImpl) {
		this.commonServiceImpl = commonServiceImpl;
	}

	@Autowired
	public void setCommonDoctorServiceImpl(CommonDoctorServiceImpl commonDoctorServiceImpl) {
		this.commonDoctorServiceImpl = commonDoctorServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}

	private static final String BENEFICIARY_REG_ID = "beneficiaryRegID";

	@Operation(summary = "Provides doctor worklist")
	@GetMapping(value = { "/getDocWorklistNew/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getDocWorkListNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("serviceID") Integer serviceID, @PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			if (providerServiceMapID != null && serviceID != null) {
				String s = commonDoctorServiceImpl.getDocWorkListNew(providerServiceMapID, serviceID, vanID);
				if (s != null)
					response.setResponse(s);
			} else {
				logger.error("Invalid request, either ProviderServiceMapID or ServiceID is invalid. PSMID = "
						+ providerServiceMapID + " SID = " + serviceID);
				response.setError(5000, "Invalid request, either ProviderServiceMapID or ServiceID is invalid");
			}

		} catch (Exception e) {
			logger.error("Error in getDocWorkList:" + e);
			response.setError(5000, "Error while getting doctor worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Provides doctor worklist future scheduled for TM")
	@GetMapping(value = { "/getDocWorkListNewFutureScheduledForTM/{providerServiceMapID}/{serviceID}" })
	public String getDocWorkListNewFutureScheduledForTM(
			@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("serviceID") Integer serviceID) {
		OutputResponse response = new OutputResponse();
		try {
			if (providerServiceMapID != null && serviceID != null) {
				String s = commonDoctorServiceImpl.getDocWorkListNewFutureScheduledForTM(providerServiceMapID,
						serviceID);
				if (s != null)
					response.setResponse(s);
			} else {
				logger.error("Invalid request, either ProviderServiceMapID or ServiceID is invalid. PSMID = "
						+ providerServiceMapID + " SID = " + serviceID);
				response.setError(5000, "Invalid request, either ProviderServiceMapID or ServiceID is invalid");
			}

		} catch (Exception e) {
			logger.error("Error in getDocWorkListFutureScheduledbeneficiary:" + e);
			response.setError(5000, "Error while getting doctor worklist for future scheduled beneficiay");
		}
		return response.toString();
	}

	@Operation(summary = "Get nurse worklist new")
	@GetMapping(value = { "/getNurseWorklistNew/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getNurseWorkListNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			String s = commonNurseServiceImpl.getNurseWorkListNew(providerServiceMapID, vanID);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting nurse worklist");
		} catch (Exception e) {
			logger.error("Error in getNurseWorklist:" + e);
			response.setError(5000, "Error while getting nurse worklist");
		}
		return response.toString();
	}

	/**
	 * @ApiParam providerServiceMapID
	 * @ApiParam vanID
	 * @return
	 */
	@Operation(summary = "Get nurse worklist TM referred")
	@GetMapping(value = { "/getNurseWorklistTMreferred/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getNurseWorklistTMreferred(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			String s = commonNurseServiceImpl.getNurseWorkListTMReferred(providerServiceMapID, vanID);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting nurse worklist");
		} catch (Exception e) {
			logger.error("Error in getNurseWorklist:" + e);
			response.setError(5000, "Error while getting nurse worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Get doctor entered previous significant Ffindings")
	@PostMapping(value = { "/getDoctorPreviousSignificantFindings" })
	public String getDoctorPreviousSignificantFindings(
			@ApiParam(value = "{\"beneficiaryRegID\": \"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has(BENEFICIARY_REG_ID) && obj.get(BENEFICIARY_REG_ID) != null) {
				Long beneficiaryRegID = obj.getLong(BENEFICIARY_REG_ID);
				String s = commonDoctorServiceImpl.fetchBenPreviousSignificantFindings(beneficiaryRegID);
				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error while getting previous significant findings");
			} else {
				response.setError(5000, "Invalid data!");
			}
		} catch (Exception e) {
			logger.error("Error while fetching previous significant findings" + e);
			response.setError(5000, "Error while getting previous significant findings");
		}
		return response.toString();
	}

	@Operation(summary = "Get lab technician worklist new")
	@GetMapping(value = { "/getLabWorklistNew/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getLabWorkListNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			String s = commonNurseServiceImpl.getLabWorkListNew(providerServiceMapID, vanID);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting lab technician worklist");
		} catch (Exception e) {
			logger.error("Error in getLabWorklist:" + e);
			response.setError(5000, "Error while getting lab technician worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Get radiologist worklist new")
	@GetMapping(value = { "/getRadiologist-worklist-New/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getRadiologistWorklistNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			String s = commonNurseServiceImpl.getRadiologistWorkListNew(providerServiceMapID, vanID);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting radiologist worklist");
		} catch (Exception e) {
			logger.error("Error in getLabWorklist:" + e);
			response.setError(5000, "Error while getting radiologist worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Get oncologist worklist new")
	@GetMapping(value = { "/getOncologist-worklist-New/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getOncologistWorklistNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			String s = commonNurseServiceImpl.getOncologistWorkListNew(providerServiceMapID, vanID);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting oncologist worklist");
		} catch (Exception e) {
			logger.error("Error in getLabWorklist:" + e);
			response.setError(5000, "Error while getting oncologist worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Get pharma worklist new")
	@GetMapping(value = { "/getPharma-worklist-New/{providerServiceMapID}/{serviceID}/{vanID}" })
	public String getPharmaWorklistNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("vanID") Integer vanID) {
		OutputResponse response = new OutputResponse();
		try {
			String s = commonNurseServiceImpl.getPharmaWorkListNew(providerServiceMapID, vanID);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error while getting pharma worklist");
		} catch (Exception e) {
			logger.error("Error in getLabWorklist:" + e);
			response.setError(5000, "Error while getting pharma worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Get case-sheet print data for beneficiary.")
	@PostMapping(value = { "/get/Case-sheet/printData" })
	public String getCasesheetPrintData(@RequestBody String comingReq,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();
		try {
			if (comingReq != null) {
				BeneficiaryFlowStatus obj = InputMapper.gson().fromJson(comingReq, BeneficiaryFlowStatus.class);
				String casesheetData = commonServiceImpl.getCaseSheetPrintDataForBeneficiary(obj, authorization);
				response.setResponse(casesheetData);
			} else
				response.setError(5000, "Invalid request");
		} catch (Exception e) {
			logger.error("" + e);
		}

		return response.toString();
	}

	@Operation(summary = "Get beneficiary past history")
	@PostMapping(value = { "/getBenPastHistory" })
	public String getBenPastHistory(@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenPastHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenPastHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting illness and surgery history");
			logger.error("Error in getBenPastHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary tobacco history")
	@PostMapping(value = { "/getBenTobaccoHistory" })
	public String getBenTobaccoHistory(@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getPersonalTobaccoHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenTobaccoHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting tobacco history");
			logger.error("Error in getBenTobaccoHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary alcohol history")
	@PostMapping(value = { "/getBenAlcoholHistory" })
	public String getBenAlcoholHistory(@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getPersonalAlcoholHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenAlcoholHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting alcohol history");
			logger.error("Error in getBenAlcoholHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary allergy history")
	@PostMapping(value = { "/getBenAllergyHistory" })
	public String getBenANCAllergyHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getPersonalAllergyHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenAllergyHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting allergy history");
			logger.error("Error in getBenAllergyHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary medication history")
	@PostMapping(value = { "/getBenMedicationHistory" })
	public String getBenMedicationHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getMedicationHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenMedicationHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting medication history");
			logger.error("Error in getBenMedicationHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary family history")
	@PostMapping(value = { "/getBenFamilyHistory" })
	public String getBenFamilyHistory(@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getFamilyHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenFamilyHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting family history");
			logger.error("Error in getBenFamilyHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary menstrual history")
	@PostMapping(value = { "/getBenMenstrualHistory" })
	public String getBenMenstrualHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getMenstrualHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenMenstrualHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting menstrual history");
			logger.error("Error in getBenMenstrualHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary past obstetric history")
	@PostMapping(value = { "/getBenPastObstetricHistory" })
	public String getBenPastObstetricHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getObstetricHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenPastObstetricHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting obstetric history");
			logger.error("Error in getBenPastObstetricHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary comorbidity condition details")
	@PostMapping(value = { "/getBenComorbidityConditionHistory" })
	public String getBenANCComorbidityConditionHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getComorbidHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenComorbidityConditionHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting comodbidity history");
			logger.error("Error in getBenComorbidityConditionHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary optional vaccine details")
	@PostMapping(value = { "/getBenOptionalVaccineHistory" })
	public String getBenOptionalVaccineHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getChildVaccineHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenOptionalVaccineHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting optional vaccination history");
			logger.error("Error in getBenOptionalVaccineHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary child vaccine(Immunization) details")
	@PostMapping(value = { "/getBenChildVaccineHistory" })
	public String getBenImmunizationHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getImmunizationHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenImmunizationHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting child vaccine(immunization) history");
			logger.error("Error in getBenImmunizationHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary perinatal history details")
	@PostMapping(value = { "/getBenPerinatalHistory" })
	public String getBenPerinatalHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenPerinatalHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenPerinatalHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting perinatal history");
			logger.error("Error in getBenPerinatalHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary child feeding history details")
	@PostMapping(value = { "/getBenFeedingHistory" })
	public String getBenFeedingHistory(@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenFeedingHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenFeedingHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting child feeding history");
			logger.error("Error in getBenFeedingHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary child development history details")
	@PostMapping(value = { "/getBenDevelopmentHistory" })
	public String getBenDevelopmentHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenDevelopmentHistoryData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenDevelopmentHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting child development history");
			logger.error("Error in getBenDevelopmentHistory:" + e);
		}
		return response.toString();
	}

	/***
	 * fetch ben previous visit details for history case-record(Platform).
	 */
	@Operation(summary = "Get casesheet history of beneficiary")
	@PostMapping(value = { "/getBeneficiaryCaseSheetHistory" })
	public String getBeneficiaryCaseSheetHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {
			String responseData = commonServiceImpl.getBenPreviousVisitDataForCaseRecord(comingRequest);
			if (responseData != null)
				response.setResponse(responseData);
			else
				response.setError(5000, "Error while fetching beneficiary previous visit history details");
		} catch (Exception e) {
			response.setError(5000, "Error while fetching beneficiary previous visit history details");
			logger.error("Error while fetching beneficiary previous visit history :" + e);
		}
		return response.toString();
	}

	@Operation(summary = "TC specialist")
	@GetMapping(value = { "/getTCSpecialistWorklist/{providerServiceMapID}/{serviceID}/{userID}" })
	public String getTCSpecialistWorkListNew(@PathVariable("providerServiceMapID") Integer providerServiceMapID,
			@PathVariable("userID") Integer userID, @PathVariable("serviceID") Integer serviceID,HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {

		String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
		String userId = jwtUtil.getUserIdFromToken(jwtToken);
			if(jwtToken == null || userId == null) {
				response.setError(403, "Unauthorized access: Missing or invalid token");
			}
			if(!userId.equals(String.valueOf(userID))) {
				response.setError(403, "Unauthorized access: User ID does not match token");
			}
			if (providerServiceMapID != null && userID != null) {
				String s = commonDoctorServiceImpl.getTCSpecialistWorkListNewForTM(providerServiceMapID, userID,
						serviceID);
				if (s != null)
					response.setResponse(s);
			} else {
				logger.error("Invalid request, either ProviderServiceMapID or userID is invalid. PSMID = "
						+ providerServiceMapID + " SID = " + userID);
				response.setError(5000, "Invalid request, either ProviderServiceMapID or userID is invalid");
			}

		} catch (Exception e) {
			logger.error("Error in getTC_SpecialistWorkList:" + e);
			response.setError(5000, "Error while getting TC specialist worklist");
		}
		return response.toString();
	}

	@Operation(summary = "TC specialist future scheduled")
	@GetMapping(value = {
			"/getTCSpecialistWorklistFutureScheduled/{providerServiceMapID}/{serviceID}/{userID}" })
	public String getTCSpecialistWorklistFutureScheduled(
			@PathVariable("providerServiceMapID") Integer providerServiceMapID, @PathVariable("userID") Integer userID,
			@PathVariable("serviceID") Integer serviceID, HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			String jwtToken = CookieUtil.getJwtTokenFromCookie(request);
			String userId = jwtUtil.getUserIdFromToken(jwtToken);	
			if(!userId.equals(String.valueOf(userID))) {
				response.setError(403, "Unauthorized access: User ID does not match token");
			}
			if(jwtToken == null || userId == null) {
				response.setError(403, "Unauthorized access: Missing or invalid token");
			}
			if (providerServiceMapID != null && userID != null) {
				String s = commonDoctorServiceImpl.getTCSpecialistWorkListNewFutureScheduledForTM(providerServiceMapID,
						userID, serviceID);
				if (s != null)
					response.setResponse(s);
			} else {
				logger.error("Invalid request, either ProviderServiceMapID or userID is invalid. PSMID = "
						+ providerServiceMapID + " UserID = " + userID);
				response.setError(5000, "Invalid request, either ProviderServiceMapID or userID is invalid");
			}

		} catch (Exception e) {
			logger.error("Error in getTC_SpecialistWorkList future scheduled:" + e);
			response.setError(5000, "Error while getting TC specialist future scheduled worklist");
		}
		return response.toString();
	}

	@Operation(summary = "Download file from file system")
	@PostMapping(value = { "/downloadFile" })
	public ResponseEntity<InputStreamResource> downloadFile(@RequestBody String requestOBJ, HttpServletRequest request)
			throws Exception {
		JSONObject obj = new JSONObject(requestOBJ);
		try {
			if (obj.has("fileName") && obj.has("filePath") && obj.get("fileName") != null
					&& obj.get("filePath") != null) {

				MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext,
						obj.getString("fileName"));

				String decryptFilePath = aESEncryptionDecryption.decrypt(obj.getString("filePath"));

				File file = new File(decryptFilePath);
				InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + obj.getString("fileName"))
						.contentType(mediaType).contentLength(file.length()).body(resource);
			} else {
				throw new Exception("Invalid file name or file path");
			}
		} catch (Exception e) {
			logger.error("error while downloading file..." + obj.getString("fileName"));
			throw new Exception("Error while downloading file. Please contact administrator..");
		}

	}

	@Operation(summary = "Get beneficiary physical history")
	@PostMapping(value = { "/getBenPhysicalHistory" })
	public String getBenPhysicalHistory(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenPhysicalHistory(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("getBenPhysicalHistory response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting Physical history");
			logger.error("Error in getBenPhysicalHistory:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary symptomatic questionnaire answer details")
	@PostMapping(value = { "/getBenSymptomaticQuestionnaireDetails" })
	public String getBenSymptomaticQuestionnaireDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenSymptomaticQuestionnaireDetailsData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("Get Beneficiary Symptomatic questionnaire answer details response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting details");
			logger.error("Error in Get Beneficiary Symptomatic questionnaire answer details:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary previous diabetes history")
	@PostMapping(value = { "/getBenPreviousDiabetesHistoryDetails" })
	public String getBenPreviousDiabetesHistoryDetails(
			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenPreviousDiabetesData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("Get Beneficiary previous Diabetes history response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting details");
			logger.error("Error in Get Beneficiary previous Diabetes history:" + e);
		}
		return response.toString();
	}

	/***
	 * @ApiParam comingRequest
	 * @ApiParam Authorization
	 * @return
	 */
	@Operation(summary = "Get beneficiary TM case record")
	@PostMapping(value = { "/get/Case-sheet/TMReferredprintData" })
	public String getTMReferredPrintData(@RequestBody String comingRequest,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "ServerAuthorization") String serverAuthorization) {
		OutputResponse response = new OutputResponse();
		try {
			if (comingRequest != null) {
				BeneficiaryFlowStatus obj = InputMapper.gson().fromJson(comingRequest, BeneficiaryFlowStatus.class);
				String casesheetData = null;

				int caseSheetStatus = commonServiceImpl.checkIsCaseSheetDownloaded(obj.getBenVisitCode());

				if (caseSheetStatus == 1) {
					casesheetData = commonServiceImpl.getTmCaseSheetOffline(obj);
				} else if (caseSheetStatus == 0) {
					casesheetData = commonServiceImpl.getCaseSheetFromCentralServer(comingRequest, serverAuthorization);
				}
				if (casesheetData != null)
					response.setResponse(casesheetData);
				else
					response.setError(5000, "Beneficiary pending for Tele-Consultation");
			} else
				response.setError(5000, "Invalid request");
		} catch (IEMRException e) {
			logger.error("getTMReferredPrintData iemrexception : " + e);
			if (e.getErrorCode() != null && e.getErrorCode() == 5002)
				response.setError(5003, e.getMessage());
			else
				response.setError(5000, e.getMessage());
		} catch (Exception e) {
			logger.error("Error on getTMReferredPrintData Exception : " + e);
			response.setError(5000, "Error in getting case sheet - " + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary previous referral history")
	@PostMapping(value = { "/getBenPreviousReferralHistoryDetails" })
	public String getBenPreviousReferralHistoryDetails(

			@ApiParam(value = "{\"benRegID\":\"Long\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has("benRegID")) {
				Long benRegID = obj.getLong("benRegID");
				String s = commonServiceImpl.getBenPreviousReferralData(benRegID);
				response.setResponse(s);

			} else {
				logger.info("Invalid Request Data.");
				response.setError(5000, "Invalid request");
			}
			logger.info("Get Beneficiary previous Referral history response:" + response);
		} catch (Exception e) {
			response.setError(5000, "Error while getting details");
			logger.error("Error in Get Beneficiary previous Referral history:" + e);
		}
		return response.toString();
	}

	@Operation(summary = "Get beneficiary TM case record")
	@PostMapping(value = { "/get/Case-sheet/centralServerTMCaseSheet" })
	public String getTMCaseSheetFromCentralServer(@RequestBody String comingRequest,
			@RequestHeader(value = "Authorization") String authorization) {
		OutputResponse response = new OutputResponse();

		try {
			if (comingRequest != null) {

				String caseSheetFromCentralServer = commonServiceImpl.getCaseSheetOfTm(comingRequest, authorization);

				if (caseSheetFromCentralServer != null)
					response.setResponse(caseSheetFromCentralServer);
				else
					response.setError(5000, "Beneficiary pending for Tele-Consultation");

			} else
				response.setError(5000, "Invalid request");
		} catch (IEMRException e) {
			logger.error("getTMCaseSheetFromCentralServer IEMR exception - " + e);
			if (e.getErrorCode() != null && e.getErrorCode() == 5002)
				response.setError(5002, e.getMessage());
			else
				response.setError(5000, e.getMessage());
		} catch (Exception e) {
			logger.error("getTMCaseSheetFromCentralServer Exception - " + e);
			response.setError(5000, "Error in MMU central server case sheet" + e.getMessage());
		}

		return response.toString();

	}

	/**
	 * @ApiParam comingRequest
	 * @return ProviderSpecificMasterData
	 */
	@Operation(summary = "Calculate beneficiary BMI status")
	@PostMapping(value = { "/calculateBMIStatus" })
	public String calculateBMIStatus(
			@ApiParam(value = "{\"bmi\":\"double\",\"yearMonth\":\"String\",\"gender\":\"String\"}") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();

		try {
			String s = commonNurseServiceImpl.calculateBMIStatus(comingRequest);
			response.setResponse(s);
			logger.info("calculateBMIStatus response:" + response);
		} catch (Exception e) {
			response.setError(5000, e.getMessage());
			logger.error("Error in calculateBMIStatus:" + e.getMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Update beneficiary status flag")
	@PostMapping(value = { "/update/benDetailsAndSubmitToNurse" })
	public String saveBeneficiaryVisitDetail(
			@ApiParam(value = "{\"beneficiaryRegID\": \"Long\"}") @RequestBody String comingRequest) {

		OutputResponse response = new OutputResponse();
		inputMapper = new InputMapper();
		try {
			JSONObject obj = new JSONObject(comingRequest);
			if (obj.has(BENEFICIARY_REG_ID)) {
				if (obj.getLong(BENEFICIARY_REG_ID) > 0) {

					Integer i = commonNurseServiceImpl.updateBeneficiaryStatus('R', obj.getLong(BENEFICIARY_REG_ID));
					if (i != null && i > 0) {
						response.setResponse("Beneficiary Successfully Submitted to Nurse Work-List.");
					} else {
						response.setError(500, "Something went Wrong please try after Some Time !!!");
					}

				} else {
					response.setError(500, "Beneficiary Registration ID is Not valid !!!");
				}
			} else {
				response.setError(500, "Beneficiary Registration ID is Not valid !!!");
			}
			logger.info("benDetailsAndSubmitToNurse response:" + response);
		} catch (Exception e) {
			response.setError(e);
			logger.error("Error in benDetailsAndSubmitToNurse:" + e);
		}

		return response.toString();
	}

	@Operation(summary = "Extend redis session for 30 minutes")
	@PostMapping(value = { "/extend/redisSession" })
	public String extendRedisSession() {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse("Session extended for 30 mins");
		} catch (Exception e) {
			logger.error("Error while extending running session");
		}
		return response.toString();
	}

	@Operation(summary = "Soft delete prescribed medicine")
	@PostMapping(value = { "/doctor/delete/prescribedMedicine" })
	public String deletePrescribedMedicine(@RequestBody String requestOBJ) {
		OutputResponse response = new OutputResponse();
		try {
			if (requestOBJ != null) {
				JSONObject obj = new JSONObject(requestOBJ);
				String s = commonDoctorServiceImpl.deletePrescribedMedicine(obj);
				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "error while deleting record");
			}
		} catch (Exception e) {
			logger.error("Error while deleting prescribed medicine");
			response.setError(e);
		}
		return response.toString();
	}
}
