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
package com.iemr.mmu.service.ncdscreening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.anc.WrapperAncFindings;
import com.iemr.mmu.data.anc.WrapperBenInvestigationANC;
import com.iemr.mmu.data.ncdcare.NCDCareDiagnosis;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

@Service
public class NCDSCreeningDoctorServiceImpl implements NCDSCreeningDoctorService {

	private PrescriptionDetailRepo prescriptionDetailRepo;

	@Autowired
	public void setPrescriptionDetailRepo(PrescriptionDetailRepo prescriptionDetailRepo) {
		this.prescriptionDetailRepo = prescriptionDetailRepo;
	}

	@Autowired
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Autowired
	private TeleConsultationServiceImpl teleConsultationServiceImpl;
	@Autowired
	private CommonNurseServiceImpl commonNurseServiceImpl;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public int updateDoctorData(JsonObject requestOBJ) throws Exception {
		int updateSuccessFlag = 0;
		Long prescriptionID = null;
		Long investigationSuccessFlag = null;
		Integer findingSuccessFlag = null;
		// Integer diagnosisSuccessFlag = null;
		Integer prescriptionSuccessFlag = null;
		Long referSaveSuccessFlag = null;
		// Integer tcRequestStatusFlag = null;

		Boolean doctorSignatureFlag = false;
		if (requestOBJ.has("doctorSignatureFlag") && !requestOBJ.get("doctorSignatureFlag").isJsonNull()) {
			doctorSignatureFlag = requestOBJ.get("doctorSignatureFlag").getAsBoolean();
		}

		if (requestOBJ != null) {
			TeleconsultationRequestOBJ tcRequestOBJ = null;
//			TcSpecialistSlotBookingRequestOBJ tcSpecialistSlotBookingRequestOBJ = null;
			CommonUtilityClass commonUtilityClass = InputMapper.gson().fromJson(requestOBJ, CommonUtilityClass.class);

//			if (commonUtilityClass != null && commonUtilityClass.getServiceID() != null
//					&& commonUtilityClass.getServiceID() == 4 && requestOBJ != null && requestOBJ.has("tcRequest")
//					&& requestOBJ.get("tcRequest") != null) {
//				tcRequestOBJ = InputMapper.gson().fromJson(requestOBJ.get("tcRequest"),
//						TeleconsultationRequestOBJ.class);
//
//				// create TC request
//				if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
//						&& tcRequestOBJ.getAllocationDate() != null) {
//
//					tcRequestOBJ.setAllocationDate(Utility.combineDateAndTimeToDateTime(
//							tcRequestOBJ.getAllocationDate().toString(), tcRequestOBJ.getFromTime()));
//
//					// tc request model
//					TCRequestModel tRequestModel = InputMapper.gson().fromJson(requestOBJ, TCRequestModel.class);
//					tRequestModel.setUserID(tcRequestOBJ.getUserID());
//					tRequestModel.setRequestDate(tcRequestOBJ.getAllocationDate());
//					tRequestModel
//							.setDuration_minute(Utility.timeDiff(tcRequestOBJ.getFromTime(), tcRequestOBJ.getToTime()));
//
//					// tc speciaist slot booking model
//					tcSpecialistSlotBookingRequestOBJ = new TcSpecialistSlotBookingRequestOBJ();
//					tcSpecialistSlotBookingRequestOBJ.setUserID(tRequestModel.getUserID());
//					tcSpecialistSlotBookingRequestOBJ.setDate(tRequestModel.getRequestDate());
//					tcSpecialistSlotBookingRequestOBJ.setFromTime(tcRequestOBJ.getFromTime());
//					tcSpecialistSlotBookingRequestOBJ.setToTime(tcRequestOBJ.getToTime());
//					tcSpecialistSlotBookingRequestOBJ.setCreatedBy(commonUtilityClass.getCreatedBy());
//					tcSpecialistSlotBookingRequestOBJ.setModifiedBy(commonUtilityClass.getCreatedBy());
//
//					int j = commonDoctorServiceImpl.callTmForSpecialistSlotBook(tcSpecialistSlotBookingRequestOBJ,
//							Authorization);
//					if (j > 0)
//						tcRequestStatusFlag = teleConsultationServiceImpl.createTCRequest(tRequestModel);
//					else
//						throw new RuntimeException("Error while booking slot.");
//
//				}
//			}

			JsonArray testList = null;
			JsonArray drugList = null;

			Boolean isTestPrescribed = false;
			Boolean isMedicinePrescribed = false;

			// checking if test is prescribed
			if (requestOBJ.has("investigation") && !requestOBJ.get("investigation").isJsonNull()
					&& requestOBJ.get("investigation") != null) {
				testList = requestOBJ.getAsJsonObject("investigation").getAsJsonArray("laboratoryList");
				if (testList != null && !testList.isJsonNull() && testList.size() > 0)
					isTestPrescribed = true;
			}

			// checking if medicine is prescribed
			if (requestOBJ.has("prescription") && !requestOBJ.get("prescription").isJsonNull()
					&& requestOBJ.get("prescription") != null) {
				drugList = requestOBJ.getAsJsonArray("prescription");
				if (drugList != null && !drugList.isJsonNull() && drugList.size() > 0) {
					isMedicinePrescribed = true;
				}
			}

			if (requestOBJ.has("findings") && !requestOBJ.get("findings").isJsonNull()) {

				WrapperAncFindings wrapperAncFindings = InputMapper.gson().fromJson(requestOBJ.get("findings"),
						WrapperAncFindings.class);
				findingSuccessFlag = commonDoctorServiceImpl.updateDocFindings(wrapperAncFindings);

			} else {
				findingSuccessFlag = 1;
			}

			// creating prescription OBJ
			PrescriptionDetail prescriptionDetail = null;
			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
				// JsonObject diagnosisObj = requestOBJ.getAsJsonObject("diagnosis");
				prescriptionDetail = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"), PrescriptionDetail.class);
			}

			// generate WrapperBenInvestigationANC OBJ
			WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson()
					.fromJson(requestOBJ.get("investigation"), WrapperBenInvestigationANC.class);

//			// save doctor diagnosis
//			String doctorDiagnosis = null;
//			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()
//					&& requestOBJ.get("diagnosis").getAsJsonObject().has("doctorDiagnosis")
//					&& !requestOBJ.get("diagnosis").getAsJsonObject().get("doctorDiagnosis").isJsonNull()) {
//				doctorDiagnosis = requestOBJ.get("diagnosis").getAsJsonObject().get("doctorDiagnosis").getAsString();
//			}
//
//			// generate prescription OBJ & diagnosis OBJ
//			PrescriptionDetail prescriptionDetail = null;
//			NCDCareDiagnosis ncdCareDiagnosis = null;
//			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
//				prescriptionDetail = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"), PrescriptionDetail.class);
//				prescriptionDetail.setExternalInvestigation(wrapperBenInvestigationANC.getExternalInvestigations());
//				prescriptionID = prescriptionDetail.getPrescriptionID();
//				if (doctorDiagnosis != null)
//					prescriptionDetail.setDiagnosisProvided(doctorDiagnosis);
//				// ncdCareDiagnosis = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"),
//				// NCDCareDiagnosis.class);
//			}

			if (prescriptionDetail != null) {
				prescriptionDetail.setExternalInvestigation(wrapperBenInvestigationANC.getExternalInvestigations());
				prescriptionID = prescriptionDetail.getPrescriptionID();
			}

			// update prescription
			if (prescriptionDetail != null) {
				int p = commonNurseServiceImpl.updatePrescription(prescriptionDetail);
			}

			// update prescribed lab test
			if (isTestPrescribed) {
				wrapperBenInvestigationANC.setPrescriptionID(prescriptionID);
				investigationSuccessFlag = commonNurseServiceImpl.saveBenInvestigation(wrapperBenInvestigationANC);
			} else {
				investigationSuccessFlag = new Long(1);
			}

			// update prescribed medicine
			if (isMedicinePrescribed) {
				PrescribedDrugDetail[] prescribedDrugDetail = InputMapper.gson()
						.fromJson(requestOBJ.get("prescription"), PrescribedDrugDetail[].class);
				List<PrescribedDrugDetail> prescribedDrugDetailList = Arrays.asList(prescribedDrugDetail);

				for (PrescribedDrugDetail tmpObj : prescribedDrugDetailList) {
					tmpObj.setPrescriptionID(prescriptionID);
					tmpObj.setBeneficiaryRegID(commonUtilityClass.getBeneficiaryRegID());
					tmpObj.setBenVisitID(commonUtilityClass.getBenVisitID());
					tmpObj.setVisitCode(commonUtilityClass.getVisitCode());
					tmpObj.setProviderServiceMapID(commonUtilityClass.getProviderServiceMapID());
				}
				Map<String, Object> drugSaveResult = commonNurseServiceImpl
						.saveBenPrescribedDrugsList(prescribedDrugDetailList);
				Integer r = (Integer) drugSaveResult.get("count");
				List<Long> prescribedDrugIDs = (List<Long>) drugSaveResult.get("prescribedDrugIDs");

				// Store IDs in JsonObject
				if (prescribedDrugIDs != null && !prescribedDrugIDs.isEmpty()) {
					Gson gson = new Gson();
					requestOBJ.add("savedDrugIDs", gson.toJsonTree(prescribedDrugIDs));
				}
				if (r > 0 && r != null) {
					prescriptionSuccessFlag = r;
				}
			} else {
				prescriptionSuccessFlag = 1;
			}

			// update referral
			if (requestOBJ.has("refer") && !requestOBJ.get("refer").isJsonNull()) {
				referSaveSuccessFlag = commonDoctorServiceImpl
						.updateBenReferDetails(requestOBJ.get("refer").getAsJsonObject());
			} else {
				referSaveSuccessFlag = new Long(1);
			}

			// check if all data updated successfully
			if ((findingSuccessFlag != null && findingSuccessFlag > 0)
					&& (investigationSuccessFlag != null && investigationSuccessFlag > 0)
					&& (prescriptionSuccessFlag != null && prescriptionSuccessFlag > 0)
					&& (referSaveSuccessFlag != null && referSaveSuccessFlag > 0)) {

				// call method to update beneficiary flow table
				int i = commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(commonUtilityClass,
						isTestPrescribed, isMedicinePrescribed, tcRequestOBJ, doctorSignatureFlag);

				if (i > 0) {
					updateSuccessFlag = 1;
				} else
					throw new RuntimeException();

			} else {
				throw new RuntimeException();
			}
		} else {
			// request OBJ is null.
		}
		return updateSuccessFlag;
	}

	public String getNCDDiagnosisData(Long beneficiaryRegID, Long visitCode) {
		PrescriptionDetail obj;
		SCTDescription sctOBJ;
		ArrayList<SCTDescription> sctOBJList = new ArrayList<>();
		// ArrayList<Object[]> diagnosisDetails =
		// prescriptionDetailRepo.getBenPrescription(beneficiaryRegID, visitCode);
		// PrescriptionDetail diagnosisList =
		// PrescriptionDetail.getPrescriptions(diagnosisDetails);

		ArrayList<PrescriptionDetail> prescriptionDetailRS = prescriptionDetailRepo
				.findByBeneficiaryRegIDAndVisitCode(beneficiaryRegID, visitCode);

		if (prescriptionDetailRS != null && prescriptionDetailRS.size() > 0) {
			obj = prescriptionDetailRS.get(0);
			if (obj.getDiagnosisProvided_SCTCode() != null && obj.getDiagnosisProvided() != null) {
				String[] conceptIDArr = obj.getDiagnosisProvided_SCTCode().split(Pattern.quote("  ||  "));
				String[] termArr = obj.getDiagnosisProvided().split(Pattern.quote("  ||  "));

				// StringBuilder pd = new StringBuilder();
				int pointer = 0;
				for (String s : termArr) {
					sctOBJ = new SCTDescription();
					sctOBJ.setConceptID(conceptIDArr[pointer]);
					sctOBJ.setTerm(s);
					sctOBJList.add(sctOBJ);

					pointer++;
				}

				obj.setProvisionalDiagnosisList(sctOBJList);
				// obj.setDiagnosisProvided(pd.toString());
			}
		} else {
			obj = new PrescriptionDetail();
		}

		return new Gson().toJson(obj);
	}
}
