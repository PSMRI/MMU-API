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
package com.iemr.mmu.controller.labtechnician;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


/***
 * @Objective Saving lab test results given by LabTechnician
 */

@RestController
@CrossOrigin
@RequestMapping(value = "/labTechnician", headers = "Authorization", consumes = "application/json", produces = "application/json")
public class LabTechnicianController {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private LabTechnicianServiceImpl labTechnicianServiceImpl;

	@Autowired
	public void setLabTechnicianServiceImpl(LabTechnicianServiceImpl labTechnicianServiceImpl) {
		this.labTechnicianServiceImpl = labTechnicianServiceImpl;
	}
	private static final String BENEFICIARY_REG_ID = "beneficiaryRegID";
	private static final String VISIT_CODE = "visitCode";
	/**
	 * @Objective Save lab test results given by LabTechnician
	 * @param JSON requestObj
	 * @return success or failure response
	 */
	@CrossOrigin
	@Operation(summary = "Save lab test result entered by lab technician")
	@PostMapping(value = { "/save/LabTestResult" })
	public String saveLabTestResult(@RequestBody String saveLabTestResult) {
		OutputResponse response = new OutputResponse();
		try {

			JsonElement jsnElmnt = JsonParser.parseString(saveLabTestResult);
			JsonObject saveLabTestResultJson = jsnElmnt.getAsJsonObject();

			if (saveLabTestResultJson != null) {
				Integer labResultSaveRes = labTechnicianServiceImpl.saveLabTestResult(saveLabTestResultJson);
				if (null != labResultSaveRes && labResultSaveRes > 0) {
					response.setResponse("Data saved successfully");
				} else {
					response.setResponse("Unable to save data");
				}

			} else {
				response.setResponse("Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error while saving lab test results  :" + e);
			response.setError(5000, "Unable to save data");
		}
		return response.toString();
	}

	@CrossOrigin
	@Operation(summary = "Get beneficiary prescribed procedure")
	@PostMapping(value = { "/get/prescribedProceduresList" })
	public String getBeneficiaryPrescribedProcedure(@RequestBody String requestOBJ) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("Request obj to fetch lab tests :" + requestOBJ);
			JsonElement jsnElmnt = JsonParser.parseString(requestOBJ);
			JsonObject prescribedProceduresListJson = jsnElmnt.getAsJsonObject();

			if (prescribedProceduresListJson != null && !prescribedProceduresListJson.isJsonNull() && prescribedProceduresListJson.has(BENEFICIARY_REG_ID) && prescribedProceduresListJson.has(VISIT_CODE)) {

				String s = labTechnicianServiceImpl.getBenePrescribedProcedureDetails(
						prescribedProceduresListJson.get(BENEFICIARY_REG_ID).getAsLong(), prescribedProceduresListJson.get(VISIT_CODE).getAsLong());
				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error in prescribed procedure details");
			} else {
				response.setError(5000, "Invalid request");
			}
		} catch (Exception e) {
			logger.error("Error while getting prescribed procedure data:" + e);
			response.setError(5000, "Error while getting prescribed procedure data");
		}
		return response.toString();
	}

	@CrossOrigin()
	@Operation(summary = "Get lab test result for a visitcode.")
	@PostMapping(value = { "/get/labResultForVisitcode" })
	public String getLabResultForVisitCode(@RequestBody String req) {
		OutputResponse response = new OutputResponse();
		try {
			JsonElement jsnElmnt = JsonParser.parseString(req);
			JsonObject jsnOBJ = jsnElmnt.getAsJsonObject();

			if (jsnOBJ != null && !jsnOBJ.isJsonNull() && jsnOBJ.has(BENEFICIARY_REG_ID) && jsnOBJ.has(VISIT_CODE)) {
				String s = labTechnicianServiceImpl.getLabResultForVisitcode(jsnOBJ.get(BENEFICIARY_REG_ID).getAsLong(),
						jsnOBJ.get(VISIT_CODE).getAsLong());

				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error while getting lab report");
			} else
				response.setError(5000, "Invalid request");
		} catch (Exception e) {
			logger.error("Error while getting lab result for requested data:" , req);
			response.setError(5000, "Error while getting lab report");
		}
		return response.toString();
	}

}
