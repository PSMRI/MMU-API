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
package com.iemr.mmu.service.anc;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.anc.ANCCareDetails;
import com.iemr.mmu.data.anc.ANCDiagnosis;
import com.iemr.mmu.data.anc.BenAdherence;
import com.iemr.mmu.data.anc.BenAllergyHistory;
import com.iemr.mmu.data.anc.BenFamilyHistory;
import com.iemr.mmu.data.anc.BenMedHistory;
import com.iemr.mmu.data.anc.BenMenstrualDetails;
import com.iemr.mmu.data.anc.BenPersonalHabit;
import com.iemr.mmu.data.anc.FemaleObstetricHistory;
import com.iemr.mmu.data.anc.PhyGeneralExamination;
import com.iemr.mmu.data.anc.PhyHeadToToeExamination;
import com.iemr.mmu.data.anc.SysCardiovascularExamination;
import com.iemr.mmu.data.anc.SysCentralNervousExamination;
import com.iemr.mmu.data.anc.SysGenitourinarySystemExamination;
import com.iemr.mmu.data.anc.SysMusculoskeletalSystemExamination;
import com.iemr.mmu.data.anc.SysObstetricExamination;
import com.iemr.mmu.data.anc.SysRespiratoryExamination;
import com.iemr.mmu.data.anc.WrapperAncFindings;
import com.iemr.mmu.data.anc.WrapperAncImmunization;
import com.iemr.mmu.data.anc.WrapperBenInvestigationANC;
import com.iemr.mmu.data.anc.WrapperChildOptionalVaccineDetail;
import com.iemr.mmu.data.anc.WrapperComorbidCondDetails;
import com.iemr.mmu.data.anc.WrapperFemaleObstetricHistory;
import com.iemr.mmu.data.anc.WrapperImmunizationHistory;
import com.iemr.mmu.data.anc.WrapperMedicationHistory;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.nurse.BenAnthropometryDetail;
import com.iemr.mmu.data.nurse.BenPhysicalVitalDetail;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.data.tele_consultation.TCRequestModel;
import com.iemr.mmu.data.tele_consultation.TcSpecialistSlotBookingRequestOBJ;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.BenAnthropometryRepo;
import com.iemr.mmu.repo.nurse.anc.ANCCareRepo;
import com.iemr.mmu.repo.nurse.anc.ANCDiagnosisRepo;
import com.iemr.mmu.repo.nurse.anc.BenMedHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.BencomrbidityCondRepo;
import com.iemr.mmu.repo.nurse.anc.FemaleObstetricHistoryRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

@Service
public class ANCServiceImpl implements ANCService {
	@Autowired
	private ANCNurseServiceImpl ancNurseServiceImpl;
	private ANCDoctorServiceImpl ancDoctorServiceImpl;
	private CommonNurseServiceImpl commonNurseServiceImpl;
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Autowired
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Autowired
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	@Autowired
	private TeleConsultationServiceImpl teleConsultationServiceImpl;

	@Autowired
	private ANCCareRepo ancCareRepo;
	@Autowired
	private FemaleObstetricHistoryRepo femaleObstetricHistoryRepo;
	@Autowired
	private ANCDiagnosisRepo aNCDiagnosisRepo;

/*	@Autowired
	public void setLabTechnicianServiceImpl(LabTechnicianServiceImpl labTechnicianServiceImpl) {
		this.labTechnicianServiceImpl = labTechnicianServiceImpl;
	}

	@Autowired
	public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
		this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
	}
*/
	@Autowired
	public void setTeleConsultationServiceImpl(TeleConsultationServiceImpl teleConsultationServiceImpl) {
		this.teleConsultationServiceImpl = teleConsultationServiceImpl;
	}
	@Autowired
	public void setANCDoctorServiceImpl(ANCDoctorServiceImpl ancDoctorServiceImpl) {
		this.ancDoctorServiceImpl = ancDoctorServiceImpl;
	}

	@Autowired
	public void setCommonDoctorServiceImpl(CommonDoctorServiceImpl commonDoctorServiceImpl) {
		this.commonDoctorServiceImpl = commonDoctorServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}
	@Autowired
	public void setAncNurseServiceImpl(ANCNurseServiceImpl ancNurseServiceImpl) {
		this.ancNurseServiceImpl = ancNurseServiceImpl;
	}

	// @Autowired
	// public void setNurseServiceImpl(NurseServiceImpl nurseServiceImpl) {
	// this.nurseServiceImpl = nurseServiceImpl;
	// }
	static final String VISITDETAILS="visitDetails";  
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long saveANCNurseData(JsonObject requestOBJ) throws Exception {
		// String vr = "";
		// String vc = "";
		Long saveSuccessFlag = null;
		Map<String, Long> visitIdAndCodeMap = null;
		// check if visit details data is not null
		if (requestOBJ != null && requestOBJ.has(VISITDETAILS) && !requestOBJ.get(VISITDETAILS).isJsonNull() && !requestOBJ.getAsJsonObject(VISITDETAILS).isEmpty()) {
			CommonUtilityClass nurseUtilityClass = InputMapper.gson().fromJson(requestOBJ, CommonUtilityClass.class);
			Short nurseFlag = 9;
			BeneficiaryFlowStatus data = beneficiaryFlowStatusRepo.checkExistData(nurseUtilityClass.getBenFlowID(), nurseFlag);
			if(data == null) {
			// Call method to save visit details data
			visitIdAndCodeMap = saveBenVisitDetails(requestOBJ.getAsJsonObject(VISITDETAILS),
					nurseUtilityClass);
			}

			Long benVisitID = null;
			Long benVisitCode = null;

			if (visitIdAndCodeMap != null && visitIdAndCodeMap.size() > 0 && visitIdAndCodeMap.containsKey("visitID")
					&& visitIdAndCodeMap.containsKey("visitCode")) {
				benVisitID = visitIdAndCodeMap.get("visitID");
				benVisitCode = visitIdAndCodeMap.get("visitCode");
			}else {
				return new Long(0);
			}

			Long ancSaveSuccessFlag = null;
			Long historySaveSuccessFlag = null;
			Long vitalSaveSuccessFlag = null;
			Long examtnSaveSuccessFlag = null;
			Integer i = null;

			// code moved from inner code
			JsonObject tmpOBJ = requestOBJ.getAsJsonObject(VISITDETAILS).getAsJsonObject(VISITDETAILS);

			// Getting benflowID for ben status update
			Long benFlowID = null;
			
			benFlowID = nurseUtilityClass.getBenFlowID();

			if (benVisitID != null && benVisitID > 0) {

				// call method to save ANC data
				ancSaveSuccessFlag = saveBenANCDetails(requestOBJ.getAsJsonObject("ancDetails"), benVisitID,
						benVisitCode);
				// call method to save History data
				historySaveSuccessFlag = saveBenANCHistoryDetails(requestOBJ.getAsJsonObject("historyDetails"),
						benVisitID, benVisitCode);
				// call method to save Vital data
				vitalSaveSuccessFlag = saveBenANCVitalDetails(requestOBJ.getAsJsonObject("vitalDetails"), benVisitID,
						benVisitCode);
				// call method to save Examination data
				examtnSaveSuccessFlag = saveBenANCExaminationDetails(requestOBJ.getAsJsonObject("examinationDetails"),
						benVisitID, benVisitCode);

			} else {
				// Error in visit details saving or it is null
			}
			if ((null != ancSaveSuccessFlag && ancSaveSuccessFlag > 0)
					&& (null != historySaveSuccessFlag && historySaveSuccessFlag > 0)
					&& (null != vitalSaveSuccessFlag && vitalSaveSuccessFlag > 0)
					&& (null != examtnSaveSuccessFlag && examtnSaveSuccessFlag > 0)) {
				// removed on 07-06-2018 not using this logic anymore
				// && (i != null)

				saveSuccessFlag = ancSaveSuccessFlag;

				/**
				 * We have to write new code to update ben status flow new logic
				 */

				int J = updateBenFlowNurseAfterNurseActivityANC(
						requestOBJ.getAsJsonObject(VISITDETAILS).getAsJsonObject("investigation"), tmpOBJ, benVisitID,
						benFlowID, benVisitCode, nurseUtilityClass.getVanID());

				// End of update ben status flow new logic

			}
		} else {
			// Can't create BenVisitID
		}
		return saveSuccessFlag;
	}

	private int updateBenFlowNurseAfterNurseActivityANC(JsonObject investigationDataCheck, JsonObject tmpOBJ,
			Long benVisitID, Long benFlowID, Long visitCode, Integer vanID) {
		short nurseFlag;
		short docFlag;
		short labIteration;

		if (!investigationDataCheck.isJsonNull() && !investigationDataCheck.get("laboratoryList").isJsonNull()
				&& investigationDataCheck.getAsJsonArray("laboratoryList").size() > 0) {

			// ben will transfer to lab and doc both
			nurseFlag = (short) 2;
			docFlag = (short) 0;
			labIteration = (short) 1;
		} else {
			// ben will transfer doc only
			nurseFlag = (short) 9;
			docFlag = (short) 1;
			labIteration = (short) 0;
		}

		int rs = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(benFlowID,
				tmpOBJ.get("beneficiaryRegID").getAsLong(), benVisitID, tmpOBJ.get("visitReason").getAsString(),
				tmpOBJ.get("visitCategory").getAsString(), nurseFlag, docFlag, labIteration, (short) 0, (short) 0,
				visitCode, vanID);

		return rs;
	}

	@Transactional(rollbackFor = Exception.class)
	public Long saveANCDoctorData(JsonObject requestOBJ, String Authorization) throws Exception {
		Long saveSuccessFlag = null;
		Long prescriptionID = null;
		Long investigationSuccessFlag = null;
		Integer findingSuccessFlag = null;
		Long diagnosisSuccessFlag = null;
		Integer prescriptionSuccessFlag = null;
		Long referSaveSuccessFlag = null;
		Integer tcRequestStatusFlag = null;

		if (requestOBJ != null) {
			TeleconsultationRequestOBJ tcRequestOBJ = null;
			TcSpecialistSlotBookingRequestOBJ tcSpecialistSlotBookingRequestOBJ = null;
			CommonUtilityClass commonUtilityClass = InputMapper.gson().fromJson(requestOBJ, CommonUtilityClass.class);

			if (commonUtilityClass != null && commonUtilityClass.getServiceID() != null
					&& commonUtilityClass.getServiceID() == 4 && requestOBJ != null && requestOBJ.has("tcRequest")
					&& requestOBJ.get("tcRequest") != null) {
				tcRequestOBJ = InputMapper.gson().fromJson(requestOBJ.get("tcRequest"),
						TeleconsultationRequestOBJ.class);

				// create TC request
				if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
						&& tcRequestOBJ.getAllocationDate() != null) {

					tcRequestOBJ.setAllocationDate(Utility.combineDateAndTimeToDateTime(
							tcRequestOBJ.getAllocationDate().toString(), tcRequestOBJ.getFromTime()));

					// tc request model
					TCRequestModel tRequestModel = InputMapper.gson().fromJson(requestOBJ, TCRequestModel.class);
					tRequestModel.setUserID(tcRequestOBJ.getUserID());
					tRequestModel.setRequestDate(tcRequestOBJ.getAllocationDate());
					tRequestModel
							.setDuration_minute(Utility.timeDiff(tcRequestOBJ.getFromTime(), tcRequestOBJ.getToTime()));

					// tc speciaist slot booking model
					tcSpecialistSlotBookingRequestOBJ = new TcSpecialistSlotBookingRequestOBJ();
					tcSpecialistSlotBookingRequestOBJ.setUserID(tRequestModel.getUserID());
					tcSpecialistSlotBookingRequestOBJ.setDate(tRequestModel.getRequestDate());
					tcSpecialistSlotBookingRequestOBJ.setFromTime(tcRequestOBJ.getFromTime());
					tcSpecialistSlotBookingRequestOBJ.setToTime(tcRequestOBJ.getToTime());
					tcSpecialistSlotBookingRequestOBJ.setCreatedBy(commonUtilityClass.getCreatedBy());
					tcSpecialistSlotBookingRequestOBJ.setModifiedBy(commonUtilityClass.getCreatedBy());

					int j = commonDoctorServiceImpl.callTmForSpecialistSlotBook(tcSpecialistSlotBookingRequestOBJ,
							Authorization);
					if (j > 0)
						tcRequestStatusFlag = teleConsultationServiceImpl.createTCRequest(tRequestModel);
					else
						throw new RuntimeException("Error while booking slot.");

				}
			}

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

			// save findings
			if (requestOBJ.has("findings") && !requestOBJ.get("findings").isJsonNull()) {

				WrapperAncFindings wrapperAncFindings = InputMapper.gson().fromJson(requestOBJ.get("findings"),
						WrapperAncFindings.class);
				findingSuccessFlag = commonDoctorServiceImpl.saveDocFindings(wrapperAncFindings);
			} else {
				findingSuccessFlag = 1;
			}

			WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson()
					.fromJson(requestOBJ.get("investigation"), WrapperBenInvestigationANC.class);

			prescriptionID = commonNurseServiceImpl.savePrescriptionDetailsAndGetPrescriptionID(
					wrapperBenInvestigationANC.getBeneficiaryRegID(), wrapperBenInvestigationANC.getBenVisitID(),
					wrapperBenInvestigationANC.getProviderServiceMapID(), wrapperBenInvestigationANC.getCreatedBy(),
					wrapperBenInvestigationANC.getExternalInvestigations(), wrapperBenInvestigationANC.getVisitCode(),
					wrapperBenInvestigationANC.getVanID(), wrapperBenInvestigationANC.getParkingPlaceID(), null);

			// save prescribed lab test
			if (isTestPrescribed) {
				wrapperBenInvestigationANC.setPrescriptionID(prescriptionID);
				investigationSuccessFlag = commonNurseServiceImpl.saveBenInvestigation(wrapperBenInvestigationANC);
			} else {
				investigationSuccessFlag = new Long(1);
			}

			// save prescribed medicine
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
				Integer r = commonNurseServiceImpl.saveBenPrescribedDrugsList(prescribedDrugDetailList);
				if (r > 0 && r != null) {
					prescriptionSuccessFlag = r;
				}
			} else {
				prescriptionSuccessFlag = 1;
			}

			// save diagnosis
			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
				diagnosisSuccessFlag = ancDoctorServiceImpl
						.saveBenANCDiagnosis(requestOBJ.get("diagnosis").getAsJsonObject(), prescriptionID);
			} else {
				diagnosisSuccessFlag = new Long(1);
			}

			// save referral details
			if (requestOBJ.has("refer") && !requestOBJ.get("refer").isJsonNull()) {
				referSaveSuccessFlag = commonDoctorServiceImpl
						.saveBenReferDetails(requestOBJ.get("refer").getAsJsonObject());
			} else {
				referSaveSuccessFlag = new Long(1);
			}

			// check if all requested data saved properly
			if ((findingSuccessFlag != null && findingSuccessFlag > 0)
					&& (diagnosisSuccessFlag != null && diagnosisSuccessFlag > 0)
					&& (investigationSuccessFlag != null && investigationSuccessFlag > 0)
					&& (prescriptionSuccessFlag != null && prescriptionSuccessFlag > 0)
					&& (referSaveSuccessFlag != null && referSaveSuccessFlag > 0)) {

				// call method to update beneficiary flow table
				int i = commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(commonUtilityClass, isTestPrescribed,
						isMedicinePrescribed, tcRequestOBJ);

				if (i > 0) {
					saveSuccessFlag = diagnosisSuccessFlag;
				} else {
					throw new RuntimeException();
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// request OBJ is null.
		}
		return saveSuccessFlag;
	}

	/// ------------------- END of saving doctor data ------------------------

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */

	public Map<String, Long> saveBenVisitDetails(JsonObject visitDetailsOBJ, CommonUtilityClass nurseUtilityClass)
			throws Exception {
		Map<String, Long> visitIdAndCodeMap = new HashMap<>();
		Long benVisitID = null;
		int investigationSuccessFlag = 0;
		int adherenceSuccessFlag = 0;
		int chiefComplaintsSuccessFlag = 0;
		if (visitDetailsOBJ != null && visitDetailsOBJ.has(VISITDETAILS)
				&& !visitDetailsOBJ.get(VISITDETAILS).isJsonNull()) {

			CommonUtilityClass commonUtilityClass = InputMapper.gson().fromJson(visitDetailsOBJ,
					CommonUtilityClass.class);

			BeneficiaryVisitDetail benVisitDetailsOBJ = InputMapper.gson().fromJson(visitDetailsOBJ.get(VISITDETAILS),
					BeneficiaryVisitDetail.class);

			// benVisitDetailsOBJ.setVanID(commonUtilityClass.getVanID());
			// benVisitDetailsOBJ.setParkingPlaceID(commonUtilityClass.getParkingPlaceID());
			int i=commonNurseServiceImpl.getMaxCurrentdate(benVisitDetailsOBJ.getBeneficiaryRegID(),benVisitDetailsOBJ.getVisitReason(),benVisitDetailsOBJ.getVisitCategory());
			if(i<1) {
			benVisitID = commonNurseServiceImpl.saveBeneficiaryVisitDetails(benVisitDetailsOBJ);

			// 07-06-2018 visit code
			Long benVisitCode = commonNurseServiceImpl.generateVisitCode(benVisitID, nurseUtilityClass.getVanID(),
					nurseUtilityClass.getSessionID());

			if (benVisitID != null && benVisitID > 0 && benVisitCode != null && benVisitCode > 0) {
				if (visitDetailsOBJ.has("chiefComplaints") && !visitDetailsOBJ.get("chiefComplaints").isJsonNull()) {
					// Save Ben Chief Complaints
					BenChiefComplaint[] benChiefComplaintArray = InputMapper.gson()
							.fromJson(visitDetailsOBJ.get("chiefComplaints"), BenChiefComplaint[].class);

					List<BenChiefComplaint> benChiefComplaintList = Arrays.asList(benChiefComplaintArray);
					if (null != benChiefComplaintList && benChiefComplaintList.size() > 0) {
						for (BenChiefComplaint benChiefComplaint : benChiefComplaintList) {
							benChiefComplaint.setBenVisitID(benVisitID);
							benChiefComplaint.setVisitCode(benVisitCode);
						}
					}
					chiefComplaintsSuccessFlag = commonNurseServiceImpl.saveBenChiefComplaints(benChiefComplaintList);
				}
				if (visitDetailsOBJ.has("adherence") && !visitDetailsOBJ.get("adherence").isJsonNull()) {
					// Save Ben Adherence
					BenAdherence benAdherence = InputMapper.gson().fromJson(visitDetailsOBJ.get("adherence"),
							BenAdherence.class);
					benAdherence.setBenVisitID(benVisitID);
					benAdherence.setVisitCode(benVisitCode);
					adherenceSuccessFlag = commonNurseServiceImpl.saveBenAdherenceDetails(benAdherence);
				}
				if (visitDetailsOBJ.has("investigation") && !visitDetailsOBJ.get("investigation").isJsonNull()
						&& visitDetailsOBJ.getAsJsonObject("investigation").get("laboratoryList") != null
						&& !visitDetailsOBJ.getAsJsonObject("investigation").get("laboratoryList").isJsonNull()
						&& visitDetailsOBJ.getAsJsonObject("investigation").getAsJsonArray("laboratoryList")
								.size() > 0) {
					// Save Ben Investigations
					WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson()
							.fromJson(visitDetailsOBJ.get("investigation"), WrapperBenInvestigationANC.class);

					if (wrapperBenInvestigationANC != null) {
						wrapperBenInvestigationANC.setBenVisitID(benVisitID);
						wrapperBenInvestigationANC.setVisitCode(benVisitCode);
						investigationSuccessFlag = commonNurseServiceImpl
								.saveBenInvestigationDetails(wrapperBenInvestigationANC);

					} else {
						// Invalid Data..
					}

				} else {
					investigationSuccessFlag = 1;
				}
				if (adherenceSuccessFlag > 0 && chiefComplaintsSuccessFlag > 0 && investigationSuccessFlag > 0) {
					// Adherence, ChiefComplaints and Investigation Details
					// stored successfully.
				}

				// TODO Save Ben Upload Files
			}

			visitIdAndCodeMap.put("visitID", benVisitID);
			visitIdAndCodeMap.put("visitCode", benVisitCode);
		}
		}
		return visitIdAndCodeMap;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 * @throws ParseException
	 */
	public Long saveBenANCDetails(JsonObject ancDetailsOBJ, Long benVisitID, Long benVisitCode) throws Exception {
		Long ancSuccessFlag = null;
		Long ancCareSuccessFlag = null;
		Long ancImunizationSuccessFlag = null;
		if (ancDetailsOBJ != null && ancDetailsOBJ.has("ancObstetricDetails")
				&& !ancDetailsOBJ.get("ancObstetricDetails").isJsonNull()) {
			// Save Ben ANC Care Details
			ANCCareDetails ancCareDetailsOBJ = InputMapper.gson().fromJson(ancDetailsOBJ.get("ancObstetricDetails"),
					ANCCareDetails.class);
			if (null != ancCareDetailsOBJ) {
				ancCareDetailsOBJ.setBenVisitID(benVisitID);
				ancCareDetailsOBJ.setVisitCode(benVisitCode);
				ancCareSuccessFlag = ancNurseServiceImpl.saveBenAncCareDetails(ancCareDetailsOBJ);
			}
		}
		if (ancDetailsOBJ != null && ancDetailsOBJ.has("ancImmunization")
				&& !ancDetailsOBJ.get("ancImmunization").isJsonNull()) {
			WrapperAncImmunization wrapperAncImmunizationOBJ = InputMapper.gson()
					.fromJson(ancDetailsOBJ.get("ancImmunization"), WrapperAncImmunization.class);
			if (null != wrapperAncImmunizationOBJ) {
				wrapperAncImmunizationOBJ.setBenVisitID(benVisitID);
				wrapperAncImmunizationOBJ.setVisitCode(benVisitCode);
				ancImunizationSuccessFlag = ancNurseServiceImpl.saveAncImmunizationDetails(wrapperAncImmunizationOBJ);
			}

		}
		if ((null != ancCareSuccessFlag && ancCareSuccessFlag > 0)
				&& (null != ancImunizationSuccessFlag && ancImunizationSuccessFlag > 0)) {
			ancSuccessFlag = ancCareSuccessFlag;
		}

		return ancSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for history data saving
	 */
	public Long saveBenANCHistoryDetails(JsonObject ancHistoryOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long pastHistorySuccessFlag = null;
		Long comrbidSuccessFlag = null;
		Long medicationSuccessFlag = null;
		int personalHistorySuccessFlag = 0;
		Long allergyHistorySuccessFlag = null;
		Long familyHistorySuccessFlag = null;
		int menstrualHistorySuccessFlag = 0;
		Long obstetricSuccessFlag = null;
		Long immunizationSuccessFlag = null;
		Long childVaccineSuccessFlag = null;

		// Save Past History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("pastHistory")
				&& !ancHistoryOBJ.get("pastHistory").isJsonNull()) {
			BenMedHistory benMedHistory = InputMapper.gson().fromJson(ancHistoryOBJ.get("pastHistory"),
					BenMedHistory.class);
			if (null != benMedHistory) {
				benMedHistory.setBenVisitID(benVisitID);
				benMedHistory.setVisitCode(benVisitCode);
				pastHistorySuccessFlag = commonNurseServiceImpl.saveBenPastHistory(benMedHistory);
			}

		}

		// Save Comorbidity/concurrent Conditions
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("comorbidConditions")
				&& !ancHistoryOBJ.get("comorbidConditions").isJsonNull()) {
			WrapperComorbidCondDetails wrapperComorbidCondDetails = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("comorbidConditions"), WrapperComorbidCondDetails.class);
			if (null != wrapperComorbidCondDetails) {
				wrapperComorbidCondDetails.setBenVisitID(benVisitID);
				wrapperComorbidCondDetails.setVisitCode(benVisitCode);
				comrbidSuccessFlag = commonNurseServiceImpl.saveBenComorbidConditions(wrapperComorbidCondDetails);
			}
		}

		// Save Medication History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("medicationHistory")
				&& !ancHistoryOBJ.get("medicationHistory").isJsonNull()) {
			WrapperMedicationHistory wrapperMedicationHistory = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("medicationHistory"), WrapperMedicationHistory.class);
			if (null != wrapperMedicationHistory) {
				wrapperMedicationHistory.setBenVisitID(benVisitID);
				wrapperMedicationHistory.setVisitCode(benVisitCode);
				medicationSuccessFlag = commonNurseServiceImpl.saveBenMedicationHistory(wrapperMedicationHistory);

				// medicationSuccessFlag =
				// ancNurseServiceImpl.saveBenANCMedicationHistory(wrapperMedicationHistory);
			}

		}
		// Save Personal History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("personalHistory")
				&& !ancHistoryOBJ.get("personalHistory").isJsonNull()) {
			// Save Ben Personal Habits..
			BenPersonalHabit personalHabit = InputMapper.gson().fromJson(ancHistoryOBJ.get("personalHistory"),
					BenPersonalHabit.class);
			if (null != personalHabit) {
				personalHabit.setBenVisitID(benVisitID);
				personalHabit.setVisitCode(benVisitCode);

				personalHistorySuccessFlag = commonNurseServiceImpl.savePersonalHistory(personalHabit);
				// personalHistorySuccessFlag =
				// ancNurseServiceImpl.saveANCPersonalHistory(personalHabit);
			}

			BenAllergyHistory benAllergyHistory = InputMapper.gson().fromJson(ancHistoryOBJ.get("personalHistory"),
					BenAllergyHistory.class);
			if (null != benAllergyHistory) {
				benAllergyHistory.setBenVisitID(benVisitID);
				benAllergyHistory.setVisitCode(benVisitCode);

				allergyHistorySuccessFlag = commonNurseServiceImpl.saveAllergyHistory(benAllergyHistory);
				// allergyHistorySuccessFlag =
				// ancNurseServiceImpl.saveANCAllergyHistory(benAllergyHistory);
			}

		}

		// Save Family History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("familyHistory")
				&& !ancHistoryOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(ancHistoryOBJ.get("familyHistory"),
					BenFamilyHistory.class);
			if (null != benFamilyHistory) {
				benFamilyHistory.setBenVisitID(benVisitID);
				benFamilyHistory.setVisitCode(benVisitCode);
				familyHistorySuccessFlag = commonNurseServiceImpl.saveBenFamilyHistory(benFamilyHistory);
				// familyHistorySuccessFlag =
				// ancNurseServiceImpl.saveANCBenFamilyHistory(benFamilyHistory);
			}
		}

		// Save Menstrual History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("menstrualHistory")
				&& !ancHistoryOBJ.get("menstrualHistory").isJsonNull()) {
			BenMenstrualDetails menstrualDetails = InputMapper.gson().fromJson(ancHistoryOBJ.get("menstrualHistory"),
					BenMenstrualDetails.class);
			if (null != menstrualDetails) {
				menstrualDetails.setBenVisitID(benVisitID);
				menstrualDetails.setVisitCode(benVisitCode);
				menstrualHistorySuccessFlag = commonNurseServiceImpl.saveBenMenstrualHistory(menstrualDetails);
				// menstrualHistorySuccessFlag =
				// ancNurseServiceImpl.saveBenANCMenstrualHistory(menstrualDetails);
			}

		}

		// Save Past Obstetric History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("femaleObstetricHistory")
				&& !ancHistoryOBJ.get("femaleObstetricHistory").isJsonNull()) {
			WrapperFemaleObstetricHistory wrapperFemaleObstetricHistory = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("femaleObstetricHistory"), WrapperFemaleObstetricHistory.class);

			if (wrapperFemaleObstetricHistory != null) {
				wrapperFemaleObstetricHistory.setBenVisitID(benVisitID);
				wrapperFemaleObstetricHistory.setVisitCode(benVisitCode);
				obstetricSuccessFlag = commonNurseServiceImpl.saveFemaleObstetricHistory(wrapperFemaleObstetricHistory);
				// obstetricSuccessFlag =
				// ancNurseServiceImpl.saveFemaleObstetricHistory(wrapperFemaleObstetricHistory);
			} else {
				// Female Obstetric Details not provided.
			}

		} else {
			obstetricSuccessFlag = new Long(1);
		}

		/** For Female above 12 and below 16 years.. **/
		// Save Immunization History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("immunizationHistory")
				&& !ancHistoryOBJ.get("immunizationHistory").isJsonNull()) {
			WrapperImmunizationHistory wrapperImmunizationHistory = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("immunizationHistory"), WrapperImmunizationHistory.class);
			if (null != wrapperImmunizationHistory) {
				wrapperImmunizationHistory.setBenVisitID(benVisitID);
				wrapperImmunizationHistory.setVisitCode(benVisitCode);
				immunizationSuccessFlag = commonNurseServiceImpl.saveImmunizationHistory(wrapperImmunizationHistory);
				// immunizationSuccessFlag =
				// ancNurseServiceImpl.saveANCImmunizationHistory(wrapperImmunizationHistory);
			} else {

				// ImmunizationList Data not Available
			}

		} else {
			immunizationSuccessFlag = new Long(1);
		}

		// Save Other/Optional Vaccines History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("childVaccineDetails")
				&& !ancHistoryOBJ.get("childVaccineDetails").isJsonNull()) {
			WrapperChildOptionalVaccineDetail wrapperChildVaccineDetail = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("childVaccineDetails"), WrapperChildOptionalVaccineDetail.class);
			if (null != wrapperChildVaccineDetail) {
				wrapperChildVaccineDetail.setBenVisitID(benVisitID);
				wrapperChildVaccineDetail.setVisitCode(benVisitCode);
				childVaccineSuccessFlag = commonNurseServiceImpl
						.saveChildOptionalVaccineDetail(wrapperChildVaccineDetail);
				// childVaccineSuccessFlag =
				// ancNurseServiceImpl.saveChildOptionalVaccineDetail(wrapperChildVaccineDetail);
			} else {
				// Child Optional Vaccine Detail not provided.
			}

		} else {
			childVaccineSuccessFlag = new Long(1);
		}

		Long historySuccessFlag = null;

		if ((null != pastHistorySuccessFlag && pastHistorySuccessFlag > 0)
				&& (null != comrbidSuccessFlag && comrbidSuccessFlag > 0)
				&& (null != medicationSuccessFlag && medicationSuccessFlag > 0)
				&& (null != allergyHistorySuccessFlag && allergyHistorySuccessFlag > 0)
				&& (null != familyHistorySuccessFlag && familyHistorySuccessFlag > 0)
				&& (null != obstetricSuccessFlag && obstetricSuccessFlag > 0)
				&& (null != immunizationSuccessFlag && immunizationSuccessFlag > 0)
				&& (null != childVaccineSuccessFlag && childVaccineSuccessFlag > 0) && personalHistorySuccessFlag > 0
				&& menstrualHistorySuccessFlag > 0) {
			historySuccessFlag = pastHistorySuccessFlag;
		}
		return historySuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */
	public Long saveBenANCVitalDetails(JsonObject vitalDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long vitalSuccessFlag = null;
		Long anthropometrySuccessFlag = null;
		Long phyVitalSuccessFlag = null;
		// Save Physical Anthropometry && Physical Vital Details
		if (vitalDetailsOBJ != null) {
			BenAnthropometryDetail benAnthropometryDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenAnthropometryDetail.class);
			BenPhysicalVitalDetail benPhysicalVitalDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenPhysicalVitalDetail.class);

			if (null != benAnthropometryDetail) {
				benAnthropometryDetail.setBenVisitID(benVisitID);
				benAnthropometryDetail.setVisitCode(benVisitCode);
				anthropometrySuccessFlag = commonNurseServiceImpl
						.saveBeneficiaryPhysicalAnthropometryDetails(benAnthropometryDetail);
			}
			if (null != benPhysicalVitalDetail) {
				benPhysicalVitalDetail.setBenVisitID(benVisitID);
				benPhysicalVitalDetail.setVisitCode(benVisitCode);
				phyVitalSuccessFlag = commonNurseServiceImpl
						.saveBeneficiaryPhysicalVitalDetails(benPhysicalVitalDetail);
			}

			if (anthropometrySuccessFlag != null && anthropometrySuccessFlag > 0 && phyVitalSuccessFlag != null
					&& phyVitalSuccessFlag > 0) {
				vitalSuccessFlag = anthropometrySuccessFlag;
			}
		}

		return vitalSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */
	public Long saveBenANCExaminationDetails(JsonObject examinationDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {

		Long exmnSuccessFlag = null;

		Long genExmnSuccessFlag = null;
		Long headToToeExmnSuccessFlag = null;
		// Long gastroIntsExmnSuccessFlag = null;
		Long cardiExmnSuccessFlag = null;
		Long respiratoryExmnSuccessFlag = null;
		Long centralNrvsExmnSuccessFlag = null;
		Long muskelstlExmnSuccessFlag = null;
		Long genitorinaryExmnSuccessFlag = null;
		Long obstetricExmnSuccessFlag = null;

		// Save General Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("generalExamination")
				&& !examinationDetailsOBJ.get("generalExamination").isJsonNull()) {
			PhyGeneralExamination generalExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("generalExamination"), PhyGeneralExamination.class);
			if (null != generalExamination) {
				generalExamination.setBenVisitID(benVisitID);
				generalExamination.setVisitCode(benVisitCode);
				genExmnSuccessFlag = commonNurseServiceImpl.savePhyGeneralExamination(generalExamination);
				// genExmnSuccessFlag =
				// ancNurseServiceImpl.savePhyGeneralExamination(generalExamination);
			}

		}

		// Save Head to toe Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("headToToeExamination")
				&& !examinationDetailsOBJ.get("headToToeExamination").isJsonNull()) {
			PhyHeadToToeExamination headToToeExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("headToToeExamination"), PhyHeadToToeExamination.class);
			if (null != headToToeExamination) {
				headToToeExamination.setBenVisitID(benVisitID);
				headToToeExamination.setVisitCode(benVisitCode);
				headToToeExmnSuccessFlag = commonNurseServiceImpl.savePhyHeadToToeExamination(headToToeExamination);
				// headToToeExmnSuccessFlag =
				// ancNurseServiceImpl.savePhyHeadToToeExamination(headToToeExamination);
			}

		}
		// Save Gastro Intestinal Examination Details
		/**
		 * Removed from anc. Only applicable for generalOPD. date: 07-02-2018
		 */

		// Save Cardio Vascular Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("cardioVascularExamination")
				&& !examinationDetailsOBJ.get("cardioVascularExamination").isJsonNull()) {
			SysCardiovascularExamination cardiovascularExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("cardioVascularExamination"), SysCardiovascularExamination.class);
			if (null != cardiovascularExamination) {
				cardiovascularExamination.setBenVisitID(benVisitID);
				cardiovascularExamination.setVisitCode(benVisitCode);
				cardiExmnSuccessFlag = commonNurseServiceImpl
						.saveSysCardiovascularExamination(cardiovascularExamination);
				// cardiExmnSuccessFlag =
				// ancNurseServiceImpl.saveSysCardiovascularExamination(cardiovascularExamination);

			}
		}

		// Save Respiratory Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("respiratorySystemExamination")
				&& !examinationDetailsOBJ.get("respiratorySystemExamination").isJsonNull()) {
			SysRespiratoryExamination sysRespiratoryExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("respiratorySystemExamination"), SysRespiratoryExamination.class);
			if (null != sysRespiratoryExamination) {
				sysRespiratoryExamination.setBenVisitID(benVisitID);
				sysRespiratoryExamination.setVisitCode(benVisitCode);
				respiratoryExmnSuccessFlag = commonNurseServiceImpl
						.saveSysRespiratoryExamination(sysRespiratoryExamination);
				// respiratoryExmnSuccessFlag = ancNurseServiceImpl
				// .saveSysRespiratoryExamination(sysRespiratoryExamination);
			}
		}

		// Save Central Nervous Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("centralNervousSystemExamination")
				&& !examinationDetailsOBJ.get("centralNervousSystemExamination").isJsonNull()) {
			SysCentralNervousExamination sysCentralNervousExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("centralNervousSystemExamination"), SysCentralNervousExamination.class);
			if (null != sysCentralNervousExamination) {
				sysCentralNervousExamination.setBenVisitID(benVisitID);
				sysCentralNervousExamination.setVisitCode(benVisitCode);
				centralNrvsExmnSuccessFlag = commonNurseServiceImpl
						.saveSysCentralNervousExamination(sysCentralNervousExamination);
				// centralNrvsExmnSuccessFlag = ancNurseServiceImpl
				// .saveSysCentralNervousExamination(sysCentralNervousExamination);
			}
		}

		// Save Muskeloskeletal Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("musculoskeletalSystemExamination")
				&& !examinationDetailsOBJ.get("musculoskeletalSystemExamination").isJsonNull()) {
			SysMusculoskeletalSystemExamination sysMusculoskeletalSystemExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("musculoskeletalSystemExamination"),
					SysMusculoskeletalSystemExamination.class);
			if (null != sysMusculoskeletalSystemExamination) {
				sysMusculoskeletalSystemExamination.setBenVisitID(benVisitID);
				sysMusculoskeletalSystemExamination.setVisitCode(benVisitCode);
				muskelstlExmnSuccessFlag = commonNurseServiceImpl
						.saveSysMusculoskeletalSystemExamination(sysMusculoskeletalSystemExamination);
				// muskelstlExmnSuccessFlag = ancNurseServiceImpl
				// .saveSysMusculoskeletalSystemExamination(sysMusculoskeletalSystemExamination);

			}
		}

		// Save Genito Urinary Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("genitoUrinarySystemExamination")
				&& !examinationDetailsOBJ.get("genitoUrinarySystemExamination").isJsonNull()) {
			SysGenitourinarySystemExamination sysGenitourinarySystemExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("genitoUrinarySystemExamination"),
					SysGenitourinarySystemExamination.class);
			if (null != sysGenitourinarySystemExamination) {
				sysGenitourinarySystemExamination.setBenVisitID(benVisitID);
				sysGenitourinarySystemExamination.setVisitCode(benVisitCode);
				genitorinaryExmnSuccessFlag = commonNurseServiceImpl
						.saveSysGenitourinarySystemExamination(sysGenitourinarySystemExamination);
				// genitorinaryExmnSuccessFlag = ancNurseServiceImpl
				// .saveSysGenitourinarySystemExamination(sysGenitourinarySystemExamination);

			}
		}

		// Save Obstetric Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("obstetricExamination")
				&& !examinationDetailsOBJ.get("obstetricExamination").isJsonNull()) {
			SysObstetricExamination sysObstetricExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("obstetricExamination"), SysObstetricExamination.class);
			if (null != sysObstetricExamination) {
				sysObstetricExamination.setBenVisitID(benVisitID);
				sysObstetricExamination.setVisitCode(benVisitCode);
				obstetricExmnSuccessFlag = ancNurseServiceImpl.saveSysObstetricExamination(sysObstetricExamination);

			}
		}

		if ((null != genExmnSuccessFlag && genExmnSuccessFlag > 0)
				&& (null != headToToeExmnSuccessFlag && headToToeExmnSuccessFlag > 0)
				// && (null != gastroIntsExmnSuccessFlag &&
				// gastroIntsExmnSuccessFlag > 0)
				&& (null != cardiExmnSuccessFlag && cardiExmnSuccessFlag > 0)
				&& (null != respiratoryExmnSuccessFlag && respiratoryExmnSuccessFlag > 0)
				&& (null != centralNrvsExmnSuccessFlag && centralNrvsExmnSuccessFlag > 0)
				&& (null != muskelstlExmnSuccessFlag && muskelstlExmnSuccessFlag > 0)
				&& (null != genitorinaryExmnSuccessFlag && genitorinaryExmnSuccessFlag > 0)
				&& (null != obstetricExmnSuccessFlag && obstetricExmnSuccessFlag > 0)) {
			exmnSuccessFlag = genExmnSuccessFlag;
		}
		return exmnSuccessFlag;
	}

	// ----------Fetch ANC (Nurse) --------------------------------------

	@Override
	public String getBenVisitDetailsFrmNurseANC(Long benRegID, Long visitCode) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		BeneficiaryVisitDetail visitDetail = commonNurseServiceImpl.getCSVisitDetails(benRegID, visitCode);

		resMap.put("ANCNurseVisitDetail", new Gson().toJson(visitDetail));

		resMap.put("BenAdherence", commonNurseServiceImpl.getBenAdherence(benRegID, visitCode));

		resMap.put("BenChiefComplaints", commonNurseServiceImpl.getBenChiefComplaints(benRegID, visitCode));
		// resMap.put("BenChiefComplaints",
		// ancNurseServiceImpl.getBenChiefComplaints(benRegID, benVisitID));

		resMap.put("Investigation", commonNurseServiceImpl.getLabTestOrders(benRegID, visitCode));

		return resMap.toString();
	}

	@Override
	public String getBenANCDetailsFrmNurseANC(Long benRegID, Long visitCode) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("ANCCareDetail", ancNurseServiceImpl.getANCCareDetails(benRegID, visitCode));

		resMap.put("ANCWomenVaccineDetails", ancNurseServiceImpl.getANCWomenVaccineDetails(benRegID, visitCode));

		return resMap.toString();
	}

	@Override
	public String getBenANCHistoryDetails(Long benRegID, Long visitCode) {
		Map<String, Object> HistoryDetailsMap = new HashMap<String, Object>();

		HistoryDetailsMap.put("PastHistory", commonNurseServiceImpl.getPastHistoryData(benRegID, visitCode));
		HistoryDetailsMap.put("ComorbidityConditions",
				commonNurseServiceImpl.getComorbidityConditionsHistory(benRegID, visitCode));
		HistoryDetailsMap.put("MedicationHistory", commonNurseServiceImpl.getMedicationHistory(benRegID, visitCode));
		HistoryDetailsMap.put("PersonalHistory", commonNurseServiceImpl.getPersonalHistory(benRegID, visitCode));
		HistoryDetailsMap.put("FamilyHistory", commonNurseServiceImpl.getFamilyHistory(benRegID, visitCode));
		HistoryDetailsMap.put("MenstrualHistory", commonNurseServiceImpl.getMenstrualHistory(benRegID, visitCode));
		HistoryDetailsMap.put("FemaleObstetricHistory",
				commonNurseServiceImpl.getFemaleObstetricHistory(benRegID, visitCode));
		HistoryDetailsMap.put("ImmunizationHistory",
				commonNurseServiceImpl.getImmunizationHistory(benRegID, visitCode));
		HistoryDetailsMap.put("childOptionalVaccineHistory",
				commonNurseServiceImpl.getChildOptionalVaccineHistory(benRegID, visitCode));

		return new Gson().toJson(HistoryDetailsMap);
	}

	@Override
	public String getBeneficiaryVitalDetails(Long beneficiaryRegID, Long visitCode) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("benAnthropometryDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(beneficiaryRegID, visitCode));
		resMap.put("benPhysicalVitalDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(beneficiaryRegID, visitCode));

		return resMap.toString();
	}

	@Override
	public String getANCExaminationDetailsData(Long benRegID, Long visitCode) {
		Map<String, Object> examinationDetailsMap = new HashMap<String, Object>();

		examinationDetailsMap.put("generalExamination",
				commonNurseServiceImpl.getGeneralExaminationData(benRegID, visitCode));
		examinationDetailsMap.put("headToToeExamination",
				commonNurseServiceImpl.getHeadToToeExaminationData(benRegID, visitCode));

		/* Only for General OPD, Can remove from here */
		/*
		 * examinationDetailsMap.put("gastrointestinalExamination",
		 * ancNurseServiceImpl.getSysGastrointestinalExamination(benRegID, benVisitID));
		 */
		examinationDetailsMap.put("cardiovascularExamination",
				commonNurseServiceImpl.getCardiovascularExamination(benRegID, visitCode));
		examinationDetailsMap.put("respiratoryExamination",
				commonNurseServiceImpl.getRespiratoryExamination(benRegID, visitCode));
		examinationDetailsMap.put("centralNervousExamination",
				commonNurseServiceImpl.getSysCentralNervousExamination(benRegID, visitCode));
		examinationDetailsMap.put("musculoskeletalExamination",
				commonNurseServiceImpl.getMusculoskeletalExamination(benRegID, visitCode));
		examinationDetailsMap.put("genitourinaryExamination",
				commonNurseServiceImpl.getGenitourinaryExamination(benRegID, visitCode));
		examinationDetailsMap.put("obstetricExamination",
				ancNurseServiceImpl.getSysObstetricExamination(benRegID, visitCode));

		return new Gson().toJson(examinationDetailsMap);
	}

	// ---------- ENd of Fetch ANC (Nurse)--------------------------------------

	/*
	 * // ------- Fetch beneficiary all past history data ------------------ public
	 * String getANCPastHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPastMedicalHistory(beneficiaryRegID); } ///
	 * ------- End of Fetch beneficiary all past history data ----------
	 * 
	 * // ------- Fetch beneficiary all Comorbid conditions history data //
	 * ------------------ public String getANCComorbidHistoryData(Long
	 * beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenComorbidityHistory(beneficiaryRegID); } ///
	 * ------- End of Fetch beneficiary all Comorbid conditions history data ///
	 * --------
	 * 
	 * // ------- Fetch beneficiary all Medication history data ----------- public
	 * String getANCMedicationHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPersonalMedicationHistory(beneficiaryRegID); }
	 * /// ------- End of Fetch beneficiary all Medication history data --
	 * 
	 * // ------- Fetch beneficiary all Personal Tobacco history data //
	 * --------------- public String getANCPersonalTobaccoHistoryData(Long
	 * beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPersonalTobaccoHistory(beneficiaryRegID); }
	 * /// ------- End of Fetch beneficiary all Personal Tobacco history data------
	 * 
	 * // ------- Fetch beneficiary all Personal Alcohol history data //
	 * --------------- public String getANCPersonalAlcoholHistoryData(Long
	 * beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPersonalAlcoholHistory(beneficiaryRegID); }
	 * /// ------- End of Fetch beneficiary all Personal Alcohol history data ///
	 * ------
	 * 
	 * // ------- Fetch beneficiary all Personal Allergy history data //
	 * --------------- public String getANCPersonalAllergyHistoryData(Long
	 * beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPersonalAllergyHistory(beneficiaryRegID); }
	 * /// ------- End of Fetch beneficiary all Personal Allergy history data------
	 * 
	 * // ------- Fetch beneficiary all Family history data --------------- public
	 * String getANCFamilyHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPersonalFamilyHistory(beneficiaryRegID); } ///
	 * ------- End of Fetch beneficiary all Family history data ------
	 * 
	 * // ------- Fetch beneficiary all Menstrual history data ----------- public
	 * String getANCMenstrualHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenMenstrualHistory(beneficiaryRegID); } ///
	 * ------- End of Fetch beneficiary all Menstrual history data --
	 * 
	 * // ------- Fetch beneficiary all past obstetric history data ---------------
	 * public String getANCObstetricHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenPastObstetricHistory(beneficiaryRegID); } ///
	 * ------- End of Fetch beneficiary all past obstetric history data ------
	 * 
	 * // ------- Fetch beneficiary all Immunization history data ---------------
	 * public String getANCImmunizationHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenImmunizationHistory(beneficiaryRegID); } ///
	 * ------- End of Fetch beneficiary all Immunization history data ------
	 * 
	 * // ------- Fetch beneficiary all Child Vaccine history data ---------------
	 * public String getANCChildVaccineHistoryData(Long beneficiaryRegID) { return
	 * commonNurseServiceImpl.fetchBenOptionalVaccineHistory(beneficiaryRegID); }
	 * /// ------- End of Fetch beneficiary all Child Vaccine history data ------
	 */
	// -------Update (Nurse data from Doctor screen)----------------------

	/***
	 * 
	 * @param ancDetailsOBJ
	 * @param benVisitID
	 * @return success or failure flag for ANC update
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateBenANCDetails(JsonObject ancDetailsOBJ) throws Exception {
		int ancSuccessFlag = 0;
		int ancCareSuccessFlag = 0;
		int ancImunizationSuccessFlag = 0;
		if (ancDetailsOBJ != null && ancDetailsOBJ.has("ancObstetricDetails")
				&& !ancDetailsOBJ.get("ancObstetricDetails").isJsonNull()) {
			// Update Ben ANC Care Details
			ANCCareDetails ancCareDetailsOBJ = InputMapper.gson().fromJson(ancDetailsOBJ.get("ancObstetricDetails"),
					ANCCareDetails.class);
			ancCareSuccessFlag = ancNurseServiceImpl.updateBenAncCareDetails(ancCareDetailsOBJ);
		}
		if (ancDetailsOBJ != null && ancDetailsOBJ.has("ancImmunization")
				&& !ancDetailsOBJ.get("ancImmunization").isJsonNull()) {
			WrapperAncImmunization wrapperAncImmunizationOBJ = InputMapper.gson()
					.fromJson(ancDetailsOBJ.get("ancImmunization"), WrapperAncImmunization.class);
			ancImunizationSuccessFlag = ancNurseServiceImpl.updateBenAncImmunizationDetails(wrapperAncImmunizationOBJ);

		}
		if (ancCareSuccessFlag > 0 && ancImunizationSuccessFlag > 0) {
			ancSuccessFlag = ancImunizationSuccessFlag;
		}

		return ancSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for ANC History updating by Doctor
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateBenANCHistoryDetails(JsonObject ancHistoryOBJ) throws Exception {
		int pastHistorySuccessFlag = 0;
		int comrbidSuccessFlag = 0;
		int medicationSuccessFlag = 0;
		int personalHistorySuccessFlag = 0;
		int allergyHistorySuccessFlag = 0;
		int familyHistorySuccessFlag = 0;
		int menstrualHistorySuccessFlag = 0;
		int obstetricSuccessFlag = 0;
		int immunizationSuccessFlag = 0;
		int childVaccineSuccessFlag = 0;

		// Update Past History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("pastHistory")
				&& !ancHistoryOBJ.get("pastHistory").isJsonNull()) {
			BenMedHistory benMedHistory = InputMapper.gson().fromJson(ancHistoryOBJ.get("pastHistory"),
					BenMedHistory.class);
			pastHistorySuccessFlag = commonNurseServiceImpl.updateBenPastHistoryDetails(benMedHistory);

		} else {
			pastHistorySuccessFlag = 1;
		}

		// Update Comorbidity/concurrent Conditions
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("comorbidConditions")
				&& !ancHistoryOBJ.get("comorbidConditions").isJsonNull()) {
			WrapperComorbidCondDetails wrapperComorbidCondDetails = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("comorbidConditions"), WrapperComorbidCondDetails.class);
			comrbidSuccessFlag = commonNurseServiceImpl.updateBenComorbidConditions(wrapperComorbidCondDetails);
		} else {
			comrbidSuccessFlag = 1;
		}

		// Update Medication History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("medicationHistory")
				&& !ancHistoryOBJ.get("medicationHistory").isJsonNull()) {
			WrapperMedicationHistory wrapperMedicationHistory = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("medicationHistory"), WrapperMedicationHistory.class);
			medicationSuccessFlag = commonNurseServiceImpl.updateBenMedicationHistory(wrapperMedicationHistory);
		} else {
			medicationSuccessFlag = 1;
		}
		// Update Personal History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("personalHistory")
				&& !ancHistoryOBJ.get("personalHistory").isJsonNull()) {
			// Update Ben Personal Habits..
			BenPersonalHabit personalHabit = InputMapper.gson().fromJson(ancHistoryOBJ.get("personalHistory"),
					BenPersonalHabit.class);

			personalHistorySuccessFlag = commonNurseServiceImpl.updateBenPersonalHistory(personalHabit);

			// Update Ben Allergy History..
			BenAllergyHistory benAllergyHistory = InputMapper.gson().fromJson(ancHistoryOBJ.get("personalHistory"),
					BenAllergyHistory.class);
			allergyHistorySuccessFlag = commonNurseServiceImpl.updateBenAllergicHistory(benAllergyHistory);

		} else {
			personalHistorySuccessFlag = 1;
			allergyHistorySuccessFlag = 1;
		}

		// Update Family History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("familyHistory")
				&& !ancHistoryOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(ancHistoryOBJ.get("familyHistory"),
					BenFamilyHistory.class);
			familyHistorySuccessFlag = commonNurseServiceImpl.updateBenFamilyHistory(benFamilyHistory);
		} else {
			familyHistorySuccessFlag = 1;
		}

		// Update Menstrual History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("menstrualHistory")
				&& !ancHistoryOBJ.get("menstrualHistory").isJsonNull()) {
			BenMenstrualDetails menstrualDetails = InputMapper.gson().fromJson(ancHistoryOBJ.get("menstrualHistory"),
					BenMenstrualDetails.class);
			menstrualHistorySuccessFlag = commonNurseServiceImpl.updateMenstrualHistory(menstrualDetails);
		} else {
			menstrualHistorySuccessFlag = 1;
		}

		// Update Past Obstetric History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("femaleObstetricHistory")
				&& !ancHistoryOBJ.get("femaleObstetricHistory").isJsonNull()) {
			WrapperFemaleObstetricHistory wrapperFemaleObstetricHistory = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("femaleObstetricHistory"), WrapperFemaleObstetricHistory.class);

			obstetricSuccessFlag = commonNurseServiceImpl.updatePastObstetricHistory(wrapperFemaleObstetricHistory);
		} else {
			obstetricSuccessFlag = 1;
		}

		/** For Female above 12 yrs old and below 16 years old.. **/
		// Update Immunization History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("immunizationHistory")
				&& !ancHistoryOBJ.get("immunizationHistory").isJsonNull()) {

			JsonObject immunizationHistory = ancHistoryOBJ.getAsJsonObject("immunizationHistory");
			if (immunizationHistory.get("immunizationList") != null
					&& immunizationHistory.getAsJsonArray("immunizationList").size() > 0) {
				WrapperImmunizationHistory wrapperImmunizationHistory = InputMapper.gson()
						.fromJson(ancHistoryOBJ.get("immunizationHistory"), WrapperImmunizationHistory.class);
				immunizationSuccessFlag = commonNurseServiceImpl
						.updateChildImmunizationDetail(wrapperImmunizationHistory);
			} else {
				immunizationSuccessFlag = 1;
			}
		} else {
			immunizationSuccessFlag = 1;
		}

		// Update Other/Optional Vaccines History
		if (ancHistoryOBJ != null && ancHistoryOBJ.has("childVaccineDetails")
				&& !ancHistoryOBJ.get("childVaccineDetails").isJsonNull()) {
			WrapperChildOptionalVaccineDetail wrapperChildVaccineDetail = InputMapper.gson()
					.fromJson(ancHistoryOBJ.get("childVaccineDetails"), WrapperChildOptionalVaccineDetail.class);
			childVaccineSuccessFlag = commonNurseServiceImpl
					.updateChildOptionalVaccineDetail(wrapperChildVaccineDetail);
		} else {
			childVaccineSuccessFlag = 1;
		}

		int historyUpdateSuccessFlag = 0;

		if (pastHistorySuccessFlag > 0 && comrbidSuccessFlag > 0 && medicationSuccessFlag > 0
				&& allergyHistorySuccessFlag > 0 && familyHistorySuccessFlag > 0 && obstetricSuccessFlag > 0
				&& immunizationSuccessFlag > 0 && childVaccineSuccessFlag > 0 && personalHistorySuccessFlag > 0
				&& menstrualHistorySuccessFlag > 0) {

			historyUpdateSuccessFlag = pastHistorySuccessFlag;
		}
		return historyUpdateSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for vitals data updating
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateBenANCVitalDetails(JsonObject vitalDetailsOBJ) throws Exception {
		int vitalSuccessFlag = 0;
		int anthropometrySuccessFlag = 0;
		int phyVitalSuccessFlag = 0;
		// Save Physical Anthropometry && Physical Vital Details
		if (vitalDetailsOBJ != null) {
			BenAnthropometryDetail benAnthropometryDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenAnthropometryDetail.class);
			BenPhysicalVitalDetail benPhysicalVitalDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenPhysicalVitalDetail.class);

			anthropometrySuccessFlag = commonNurseServiceImpl.updateANCAnthropometryDetails(benAnthropometryDetail);
			phyVitalSuccessFlag = commonNurseServiceImpl.updateANCPhysicalVitalDetails(benPhysicalVitalDetail);

			if (anthropometrySuccessFlag > 0 && phyVitalSuccessFlag > 0) {
				vitalSuccessFlag = anthropometrySuccessFlag;
			}
		}

		return vitalSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for Examinationm data updating
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateBenANCExaminationDetails(JsonObject examinationDetailsOBJ) throws Exception {

		int exmnSuccessFlag = 0;

		int genExmnSuccessFlag = 0;
		int headToToeExmnSuccessFlag = 0;
		// int gastroIntsExmnSuccessFlag = 0;
		int cardiExmnSuccessFlag = 0;
		int respiratoryExmnSuccessFlag = 0;
		int centralNrvsExmnSuccessFlag = 0;
		int muskelstlExmnSuccessFlag = 0;
		int genitorinaryExmnSuccessFlag = 0;
		int obstetricExmnSuccessFlag = 0;

		// Save General Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("generalExamination")
				&& !examinationDetailsOBJ.get("generalExamination").isJsonNull()) {
			PhyGeneralExamination generalExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("generalExamination"), PhyGeneralExamination.class);
			genExmnSuccessFlag = commonNurseServiceImpl.updatePhyGeneralExamination(generalExamination);
		}

		// Save Head to toe Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("headToToeExamination")
				&& !examinationDetailsOBJ.get("headToToeExamination").isJsonNull()) {
			PhyHeadToToeExamination headToToeExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("headToToeExamination"), PhyHeadToToeExamination.class);
			headToToeExmnSuccessFlag = commonNurseServiceImpl.updatePhyHeadToToeExamination(headToToeExamination);
		}

		// Save Cardio Vascular Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("cardioVascularExamination")
				&& !examinationDetailsOBJ.get("cardioVascularExamination").isJsonNull()) {
			SysCardiovascularExamination cardiovascularExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("cardioVascularExamination"), SysCardiovascularExamination.class);
			cardiExmnSuccessFlag = commonNurseServiceImpl.updateSysCardiovascularExamination(cardiovascularExamination);
		}

		// Save Respiratory Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("respiratorySystemExamination")
				&& !examinationDetailsOBJ.get("respiratorySystemExamination").isJsonNull()) {
			SysRespiratoryExamination sysRespiratoryExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("respiratorySystemExamination"), SysRespiratoryExamination.class);
			respiratoryExmnSuccessFlag = commonNurseServiceImpl
					.updateSysRespiratoryExamination(sysRespiratoryExamination);
		}

		// Save Central Nervous Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("centralNervousSystemExamination")
				&& !examinationDetailsOBJ.get("centralNervousSystemExamination").isJsonNull()) {
			SysCentralNervousExamination sysCentralNervousExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("centralNervousSystemExamination"), SysCentralNervousExamination.class);
			centralNrvsExmnSuccessFlag = commonNurseServiceImpl
					.updateSysCentralNervousExamination(sysCentralNervousExamination);
		}

		// Save Muskeloskeletal Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("musculoskeletalSystemExamination")
				&& !examinationDetailsOBJ.get("musculoskeletalSystemExamination").isJsonNull()) {
			SysMusculoskeletalSystemExamination sysMusculoskeletalSystemExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("musculoskeletalSystemExamination"),
					SysMusculoskeletalSystemExamination.class);
			muskelstlExmnSuccessFlag = commonNurseServiceImpl
					.updateSysMusculoskeletalSystemExamination(sysMusculoskeletalSystemExamination);
		}

		// Save Genito Urinary Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("genitoUrinarySystemExamination")
				&& !examinationDetailsOBJ.get("genitoUrinarySystemExamination").isJsonNull()) {
			SysGenitourinarySystemExamination sysGenitourinarySystemExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("genitoUrinarySystemExamination"),
					SysGenitourinarySystemExamination.class);
			genitorinaryExmnSuccessFlag = commonNurseServiceImpl
					.updateSysGenitourinarySystemExamination(sysGenitourinarySystemExamination);
		}

		// Save Obstetric Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("obstetricExamination")
				&& !examinationDetailsOBJ.get("obstetricExamination").isJsonNull()) {
			SysObstetricExamination sysObstetricExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("obstetricExamination"), SysObstetricExamination.class);
			obstetricExmnSuccessFlag = ancNurseServiceImpl.updateSysObstetricExamination(sysObstetricExamination);
		}

		if (genExmnSuccessFlag > 0 && headToToeExmnSuccessFlag > 0 && cardiExmnSuccessFlag > 0
				&& respiratoryExmnSuccessFlag > 0 && centralNrvsExmnSuccessFlag > 0 && muskelstlExmnSuccessFlag > 0
				&& genitorinaryExmnSuccessFlag > 0 && obstetricExmnSuccessFlag > 0) {
			exmnSuccessFlag = genExmnSuccessFlag;
		}
		return exmnSuccessFlag;
	}

	public String getBenANCNurseData(Long benRegID, Long visitCode) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("anc", getBenANCDetailsFrmNurseANC(benRegID, visitCode));

		resMap.put("history", getBenANCHistoryDetails(benRegID, visitCode));

		resMap.put("vitals", getBeneficiaryVitalDetails(benRegID, visitCode));

		resMap.put("examination", getANCExaminationDetailsData(benRegID, visitCode));

		return resMap.toString();
	}

	// get ANC doctor data for update
	public String getBenCaseRecordFromDoctorANC(Long benRegID, Long visitCode) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("findings", commonDoctorServiceImpl.getFindingsDetails(benRegID, visitCode));

		resMap.put("diagnosis", ancDoctorServiceImpl.getANCDiagnosisDetails(benRegID, visitCode));

		resMap.put("investigation", commonDoctorServiceImpl.getInvestigationDetails(benRegID, visitCode));

		resMap.put("prescription", commonDoctorServiceImpl.getPrescribedDrugs(benRegID, visitCode));

		resMap.put("Refer", commonDoctorServiceImpl.getReferralDetails(benRegID, visitCode));
		Gson gson = new GsonBuilder()
		        .excludeFieldsWithoutExposeAnnotation()
		        .create();
		resMap.put("LabReport",
				gson.toJson(labTechnicianServiceImpl.getLabResultDataForBen(benRegID, visitCode)));
		Gson gson2 = new GsonBuilder()
		        .excludeFieldsWithoutExposeAnnotation()
		        .create();
		resMap.put("GraphData", gson2.toJson(commonNurseServiceImpl.getGraphicalTrendData(benRegID, "anc")));

		resMap.put("ArchivedVisitcodeForLabResult",
				labTechnicianServiceImpl.getLast_3_ArchivedTestVisitList(benRegID, visitCode));

		return resMap.toString();
	}

	@Transactional(rollbackFor = Exception.class)
	public Long updateANCDoctorData(JsonObject requestOBJ, String Authorization) throws Exception {
		Long updateSuccessFlag = null;
		Long prescriptionID = null;
		Long investigationSuccessFlag = null;
		Integer findingSuccessFlag = null;
		Integer diagnosisSuccessFlag = null;
		Integer prescriptionSuccessFlag = null;
		Long referSaveSuccessFlag = null;
		Integer tcRequestStatusFlag = null;

		if (requestOBJ != null) {
			TeleconsultationRequestOBJ tcRequestOBJ = null;
			TcSpecialistSlotBookingRequestOBJ tcSpecialistSlotBookingRequestOBJ = null;
			CommonUtilityClass commonUtilityClass = InputMapper.gson().fromJson(requestOBJ, CommonUtilityClass.class);

			if (commonUtilityClass != null && commonUtilityClass.getServiceID() != null
					&& commonUtilityClass.getServiceID() == 4 && requestOBJ != null && requestOBJ.has("tcRequest")
					&& requestOBJ.get("tcRequest") != null) {
				tcRequestOBJ = InputMapper.gson().fromJson(requestOBJ.get("tcRequest"),
						TeleconsultationRequestOBJ.class);

				// create TC request
				if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
						&& tcRequestOBJ.getAllocationDate() != null) {

					tcRequestOBJ.setAllocationDate(Utility.combineDateAndTimeToDateTime(
							tcRequestOBJ.getAllocationDate().toString(), tcRequestOBJ.getFromTime()));

					// tc request model
					TCRequestModel tRequestModel = InputMapper.gson().fromJson(requestOBJ, TCRequestModel.class);
					tRequestModel.setUserID(tcRequestOBJ.getUserID());
					tRequestModel.setRequestDate(tcRequestOBJ.getAllocationDate());
					tRequestModel
							.setDuration_minute(Utility.timeDiff(tcRequestOBJ.getFromTime(), tcRequestOBJ.getToTime()));

					// tc speciaist slot booking model
					tcSpecialistSlotBookingRequestOBJ = new TcSpecialistSlotBookingRequestOBJ();
					tcSpecialistSlotBookingRequestOBJ.setUserID(tRequestModel.getUserID());
					tcSpecialistSlotBookingRequestOBJ.setDate(tRequestModel.getRequestDate());
					tcSpecialistSlotBookingRequestOBJ.setFromTime(tcRequestOBJ.getFromTime());
					tcSpecialistSlotBookingRequestOBJ.setToTime(tcRequestOBJ.getToTime());
					tcSpecialistSlotBookingRequestOBJ.setCreatedBy(commonUtilityClass.getCreatedBy());
					tcSpecialistSlotBookingRequestOBJ.setModifiedBy(commonUtilityClass.getCreatedBy());

					int j = commonDoctorServiceImpl.callTmForSpecialistSlotBook(tcSpecialistSlotBookingRequestOBJ,
							Authorization);
					if (j > 0)
						tcRequestStatusFlag = teleConsultationServiceImpl.createTCRequest(tRequestModel);
					else
						throw new RuntimeException("Error while booking slot.");

				}
			}

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

			// update findings
			if (requestOBJ.has("findings") && !requestOBJ.get("findings").isJsonNull()) {
				WrapperAncFindings wrapperAncFindings = InputMapper.gson().fromJson(requestOBJ.get("findings"),
						WrapperAncFindings.class);
				findingSuccessFlag = commonDoctorServiceImpl.updateDocFindings(wrapperAncFindings);
			} else {
				findingSuccessFlag = 1;
			}

			// Generate ANC wrapper class OBJ
			WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson()
					.fromJson(requestOBJ.get("investigation"), WrapperBenInvestigationANC.class);

			// generate prescription OBJ & diagnosis OBJ
			ANCDiagnosis ancDiagnosis = null;
			PrescriptionDetail prescriptionDetail = null;
			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
				// diagnosis OBJ
				ancDiagnosis = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"), ANCDiagnosis.class);

				// prescription OBJ
				prescriptionDetail = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"), PrescriptionDetail.class);
				prescriptionDetail.setExternalInvestigation(wrapperBenInvestigationANC.getExternalInvestigations());
			}

			if (prescriptionDetail != null) {
				prescriptionID = prescriptionDetail.getPrescriptionID();
				commonNurseServiceImpl.updatePrescription(prescriptionDetail);
			}

			// update diagnosis
			if (ancDiagnosis != null) {
				diagnosisSuccessFlag = ancDoctorServiceImpl.updateBenANCDiagnosis(ancDiagnosis);
			} else {
				diagnosisSuccessFlag = 1;
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
				Integer r = commonNurseServiceImpl.saveBenPrescribedDrugsList(prescribedDrugDetailList);
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
					&& (diagnosisSuccessFlag != null && diagnosisSuccessFlag > 0)
					&& (investigationSuccessFlag != null && investigationSuccessFlag > 0)
					&& (prescriptionSuccessFlag != null && prescriptionSuccessFlag > 0)
					&& (referSaveSuccessFlag != null && referSaveSuccessFlag > 0)) {

				// call method to update beneficiary flow table
				int i = commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(commonUtilityClass,
						isTestPrescribed, isMedicinePrescribed, tcRequestOBJ);

				if (i > 0) {
					updateSuccessFlag = investigationSuccessFlag;
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

	@Autowired
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Autowired
	private BenAnthropometryRepo benAnthropometryRepo;
	@Autowired
	private BenMedHistoryRepo benMedHistoryRepo;
	@Autowired
	private BencomrbidityCondRepo bencomrbidityCondRepo;

	// check for HRP identification, all visits will be considered
	@Override
	public String getHRPStatus(Long benRegID, Long visitCode) throws Exception {
		Boolean isHRP = false;
		Map<String, Boolean> responseMap = new HashMap<>();

		// check with beneficiary latest captured age/DOB
		Timestamp dob = beneficiaryFlowStatusRepo.getBenAgeVal(benRegID);
		if (dob != null) {
			Date date = new Date(dob.getTime());

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);

			java.time.LocalDate todayDate = java.time.LocalDate.now();
			java.time.LocalDate birthdate = java.time.LocalDate.of(year, month, day);
			Period p = Period.between(birthdate, todayDate);

			int y = p.getYears();
			if (y < 20 || y > 35) {
				isHRP = true;
				responseMap.put("isHRP", isHRP);
				return new Gson().toJson(responseMap);
			}
		}

		// check with latest height
		Double d = benAnthropometryRepo.getBenLatestHeight(benRegID);
		if (d != null && d < 145) {
			isHRP = true;
			responseMap.put("isHRP", isHRP);
			return new Gson().toJson(responseMap);
		}

		// check with ANC care details screen-table, if result-set > 0 => confirm HRP
		ArrayList<ANCCareDetails> ancCareList = ancCareRepo.getANCCareDataForHRP(benRegID);
		if (ancCareList != null && ancCareList.size() > 0) {
			isHRP = true;
			responseMap.put("isHRP", isHRP);
			return new Gson().toJson(responseMap);
		}

		// check with past illness/med history
		List<Long> resultSet = benMedHistoryRepo.getHRPStatus(benRegID);
		if (resultSet != null && resultSet.size() > 0) {
			isHRP = true;
			responseMap.put("isHRP", isHRP);
			return new Gson().toJson(responseMap);
		}

		// check with comorbidity
		List<Long> resultList = bencomrbidityCondRepo.getHRPStatus(benRegID);
		if (resultList != null && resultList.size() > 0) {
			isHRP = true;
			responseMap.put("isHRP", isHRP);
			return new Gson().toJson(responseMap);
		}

		// check with Obestetric history data
		List<Short> ids = new ArrayList<>();
		ids.add((short) 2);
		ids.add((short) 3);

		ArrayList<FemaleObstetricHistory> obestetricList = femaleObstetricHistoryRepo.getPastObestetricDataForHRP(
				benRegID, "Hypothyroidism", "Obstructed Labor", "Severe anemia", "Pih-preeclampsia", "Eclampsia",
				"Syphilis in pregnancy", "HIV Complicating Pregnancy", "Gestational diabetes", "Multiple pregnancy",
				"Breech presentation", "Transverse lie", "APH-placenta previa", "Prolonged Labor", "Precipitate Labor",
				ids);
		if (obestetricList != null && obestetricList.size() > 0) {
			isHRP = true;
			responseMap.put("isHRP", isHRP);
			return new Gson().toJson(responseMap);
		}

		// check with diagnosis data for HRP
		ArrayList<Long> diagnosisPregCompList = aNCDiagnosisRepo.getANCDiagnosisDataForHRP(benRegID, "Hypothyroidism",
				"Obstructed Labor", "Severe anemia", "Pih-preeclampsia", "Eclampsia", "Syphilis in pregnancy",
				"HIV Complicating Pregnancy", "Gestational diabetes", "Multiple pregnancy", "Breech presentation",
				"Transverse lie", "APH-placenta previa");
		if (diagnosisPregCompList != null && diagnosisPregCompList.size() > 0) {
			isHRP = true;
			responseMap.put("isHRP", isHRP);
			return new Gson().toJson(responseMap);
		}

		responseMap.put("isHRP", isHRP);
		return new Gson().toJson(responseMap);
	}

}
