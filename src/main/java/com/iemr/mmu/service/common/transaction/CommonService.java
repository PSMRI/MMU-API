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
package com.iemr.mmu.service.common.transaction;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;

import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.common.DocFileManager;
import com.iemr.mmu.utils.exception.IEMRException;

public interface CommonService {

	public Resource loadFileAsResource(String file, String path) throws IOException;

	public String getBenSymptomaticQuestionnaireDetailsData(Long beneficiaryRegID) throws Exception;

	public String getBenPreviousDiabetesData(Long beneficiaryRegID) throws Exception;

	public String getBenPreviousReferralData(Long beneficiaryRegID) throws Exception;

	String getCaseSheetFromCentralServer(String mmuBenFlowOBJ, String authCentralServer) throws Exception;

	String getCaseSheetOfTm(String mmuBenFlowReq, String authCentralServer) throws Exception;
}
