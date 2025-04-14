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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.mmu.data.common.DocFileManager;
import com.iemr.mmu.service.common.transaction.CommonServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping(value = "/commonInsert", headers = "Authorization")
public class InsertCommonController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	@Autowired
	private CommonServiceImpl commonServiceImpl;

	@Operation(summary = "Save beneficairy documents locally")
	@PostMapping(value = { "/saveFiles" }, consumes = "application/json", produces = "application/json")
	public String saveFiles(@RequestBody String request) {
		OutputResponse response = new OutputResponse();

		try {
			DocFileManager[] docFileManager = InputMapper.gson().fromJson(request, DocFileManager[].class);
			List<DocFileManager> docFileManagerList = Arrays.asList(docFileManager);
			String s = commonServiceImpl.saveFiles(docFileManagerList);
			if (s != null)
				response.setResponse(s);
		} catch (Exception e) {
			logger.error("Error while saving files : " + e);
			response.setError(5000, "Error while saving files : " + e);
		}

		return response.toString();
	}
}
