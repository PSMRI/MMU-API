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
package com.iemr.mmu.controller.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.mmu.controller.registrar.main.RegistrarController;
import com.iemr.mmu.service.reports.ReportCheckPostImpl;
import com.iemr.mmu.service.reports.ReportCheckPostImplNew;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/report", headers = "Authorization")
public class ReportGateway {
	private Logger logger = LoggerFactory.getLogger(RegistrarController.class);

	@Autowired
	private ReportCheckPostImpl reportCheckPostImpl;

	@Operation(summary = "Get report")
	@PostMapping(value = { "/getReport" }, consumes = "application/json", produces = "application/json")
	public String getReportByReportID(@RequestBody(required = false) String requestOBJ) {
		OutputResponse response = new OutputResponse();

		try {
			if (requestOBJ != null)
				response.setResponse(reportCheckPostImpl.reportHandler(requestOBJ));
			else
				response.setError(5000, "Invalid request");

		} catch (Exception e) {
			logger.error("Error occurred while fetching report : " + e);
			response.setError(5000, "Error occurred while fetching report is : " + e);
		}
		return response.toStringWithSerialization();
	}

	@Autowired
	private ReportCheckPostImplNew reportCheckPostImplNew;

	@Operation(summary = "Get report by report id")
	@PostMapping(value = { "/getReportNew" }, consumes = "application/json", produces = "application/json")
	public String getReportByReportID1(@RequestBody(required = false) String requestOBJ) {
		OutputResponse response = new OutputResponse();

		try {
			if (requestOBJ != null)
				response.setResponse(reportCheckPostImplNew.reportHandler(requestOBJ));
			else
				response.setError(5000, "Invalid request");

		} catch (Exception e) {
			logger.error("Error occurred while fetching report : " + e);
			response.setError(5000, "Error occurred while fetching report is : " + e);
		}
		return response.toStringWithSerialization();
	}

	@Operation(summary = "Get report master")
	@GetMapping(value = {
			"/getReportMaster/{serviceID}" }, consumes = "application/json", produces = "application/json")
	public String getReportMaster(@PathVariable("serviceID") Integer serviceID) {
		OutputResponse response = new OutputResponse();
		try {
			if (serviceID != null) {
				String s = reportCheckPostImpl.getReportMaster(serviceID);

				if (s != null)
					response.setResponse(s);
				else
					response.setError(5000, "Error while fetching report master data");
			} else
				response.setError(5000, "invalid request");

		} catch (Exception e) {
			response.setError(5000, "Error while fetching report master data is : " + e);
		}
		return response.toString();
	}

}
