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
package com.iemr.mmu.service.generalOPD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.anc.BenAllergyHistory;
import com.iemr.mmu.data.anc.BenChildDevelopmentHistory;
import com.iemr.mmu.data.anc.BenFamilyHistory;
import com.iemr.mmu.data.anc.BenMedHistory;
import com.iemr.mmu.data.anc.BenMenstrualDetails;
import com.iemr.mmu.data.anc.BenPersonalHabit;
import com.iemr.mmu.data.anc.ChildFeedingDetails;
import com.iemr.mmu.data.anc.PerinatalHistory;
import com.iemr.mmu.data.anc.PhyGeneralExamination;
import com.iemr.mmu.data.anc.PhyHeadToToeExamination;
import com.iemr.mmu.data.anc.SysCardiovascularExamination;
import com.iemr.mmu.data.anc.SysCentralNervousExamination;
import com.iemr.mmu.data.anc.SysGastrointestinalExamination;
import com.iemr.mmu.data.anc.SysGenitourinarySystemExamination;
import com.iemr.mmu.data.anc.SysMusculoskeletalSystemExamination;
import com.iemr.mmu.data.anc.SysRespiratoryExamination;
import com.iemr.mmu.data.anc.WrapperAncFindings;
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
import com.iemr.mmu.service.anc.Utility;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

/***
 * 
 * @author NE298657
 *
 */
@Service
public class GeneralOPDServiceImpl implements GeneralOPDService {

	private CommonNurseServiceImpl commonNurseServiceImpl;
	private GeneralOPDNurseServiceImpl generalOPDNurseServiceImpl;
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	private GeneralOPDDoctorServiceImpl generalOPDDoctorServiceImpl;
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	@Autowired
	private TeleConsultationServiceImpl teleConsultationServiceImpl;

	@Autowired
	public void setLabTechnicianServiceImpl(LabTechnicianServiceImpl labTechnicianServiceImpl) {
		this.labTechnicianServiceImpl = labTechnicianServiceImpl;
	}

	@Autowired
	public void setGeneralOPDDoctorServiceImpl(GeneralOPDDoctorServiceImpl generalOPDDoctorServiceImpl) {
		this.generalOPDDoctorServiceImpl = generalOPDDoctorServiceImpl;
	}

	@Autowired
	public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
		this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
	}

	@Autowired
	public void setCommonDoctorServiceImpl(CommonDoctorServiceImpl commonDoctorServiceImpl) {
		this.commonDoctorServiceImpl = commonDoctorServiceImpl;
	}

	@Autowired
	public void setGeneralOPDNurseServiceImpl(GeneralOPDNurseServiceImpl generalOPDNurseServiceImpl) {
		this.generalOPDNurseServiceImpl = generalOPDNurseServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}
	@Autowired
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	/// --------------- start of saving nurse data ------------------------
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long saveNurseData(JsonObject requestOBJ) throws Exception {
		Long historySaveSuccessFlag = null;
		Long vitalSaveSuccessFlag = null;
		Long examtnSaveSuccessFlag = null;
		Long saveSuccessFlag = null;
		Map<String, Long> visitIdAndCodeMap = null;
		
		if (requestOBJ != null && requestOBJ.has("visitDetails") && !requestOBJ.get("visitDetails").isJsonNull() && !requestOBJ.getAsJsonObject("visitDetails").isEmpty()) {
			 
			CommonUtilityClass nurseUtilityClass = InputMapper.gson().fromJson(requestOBJ, CommonUtilityClass.class);
			
			Short nurseFlag = 9;
			BeneficiaryFlowStatus data = beneficiaryFlowStatusRepo.checkExistData(nurseUtilityClass.getBenFlowID(), nurseFlag);
			if(data == null) {
			// Call method to save visit details data
			visitIdAndCodeMap = saveBenVisitDetails(requestOBJ.getAsJsonObject("visitDetails"),
					nurseUtilityClass);
			}

			Long benVisitID = null;
			Long benVisitCode = null;

			if (visitIdAndCodeMap != null && visitIdAndCodeMap.size() > 0 && visitIdAndCodeMap.containsKey("visitID")
					&& visitIdAndCodeMap.containsKey("visitCode")) {
				benVisitID = visitIdAndCodeMap.get("visitID");
				benVisitCode = visitIdAndCodeMap.get("visitCode");
			}
			else {
				return new Long(0);
			}

			// temporary object for ben flow part. for getting visit reason and
			// category and ben reg id
			JsonObject tmpOBJ = requestOBJ.getAsJsonObject("visitDetails").getAsJsonObject("visitDetails");
			// Getting benflowID for ben status update
			Long benFlowID = null;
			// if (requestOBJ.has("benFlowID")) {
			// benFlowID = requestOBJ.get("benFlowID").getAsLong();
			// }

			// Above if block code replaced by below line
			benFlowID = nurseUtilityClass.getBenFlowID();

			if (benVisitID != null && benVisitID > 0) {
				// call method to save History data
				if (requestOBJ.has("historyDetails") && !requestOBJ.get("historyDetails").isJsonNull())
					historySaveSuccessFlag = saveBenGeneralOPDHistoryDetails(
							requestOBJ.getAsJsonObject("historyDetails"), benVisitID, benVisitCode);

				// call method to save vital data
				if (requestOBJ.has("vitalDetails") && !requestOBJ.get("vitalDetails").isJsonNull())
					vitalSaveSuccessFlag = saveBenVitalDetails(requestOBJ.getAsJsonObject("vitalDetails"), benVisitID,
							benVisitCode);

				// call method to save examination data
				if (requestOBJ.has("examinationDetails") && !requestOBJ.get("examinationDetails").isJsonNull())
					examtnSaveSuccessFlag = saveBenExaminationDetails(requestOBJ.getAsJsonObject("examinationDetails"),
							benVisitID, benVisitCode);

				// int i = commonNurseServiceImpl.updateBeneficiaryStatus('N',
				// tmpOBJ.get("beneficiaryRegID").getAsLong());
			} else {
				// Beneficiary Visit ID not generated.
			}

			if ((null != historySaveSuccessFlag && historySaveSuccessFlag > 0)
					&& (null != vitalSaveSuccessFlag && vitalSaveSuccessFlag > 0)
					&& (null != examtnSaveSuccessFlag && examtnSaveSuccessFlag > 0)) {

				saveSuccessFlag = historySaveSuccessFlag;

				/**
				 * We have to write new code to update ben status flow new logic
				 */

				int i = updateBenStatusFlagAfterNurseSaveSuccess(tmpOBJ, benVisitID, benFlowID, benVisitCode,
						nurseUtilityClass.getVanID());

			}
		} else {
			// Can't create benVisitID.
		}
		return saveSuccessFlag;
	}

	// method for updating ben flow status flag for nurse
	private int updateBenStatusFlagAfterNurseSaveSuccess(JsonObject tmpOBJ, Long benVisitID, Long benFlowID,
			Long benVisitCode, Integer vanID) {
		short nurseFlag = (short) 9;
		short docFlag = (short) 1;
		short labIteration = (short) 0;

		int i = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(benFlowID,
				tmpOBJ.get("beneficiaryRegID").getAsLong(), benVisitID, tmpOBJ.get("visitReason").getAsString(),
				tmpOBJ.get("visitCategory").getAsString(), nurseFlag, docFlag, labIteration, (short) 0, (short) 0,
				benVisitCode, vanID);

		return i;
	}

	@Override
	public Map<String, Long> saveBenVisitDetails(JsonObject visitDetailsOBJ, CommonUtilityClass nurseUtilityClass)
			throws Exception {
		Map<String, Long> visitIdAndCodeMap = new HashMap<>();
		Long benVisitID = null;
		if (visitDetailsOBJ != null && visitDetailsOBJ.has("visitDetails")
				&& !visitDetailsOBJ.get("visitDetails").isJsonNull()) {
			// Save Beneficiary visit details
			BeneficiaryVisitDetail benVisitDetailsOBJ = InputMapper.gson().fromJson(visitDetailsOBJ.get("visitDetails"),
					BeneficiaryVisitDetail.class);
			int i=commonNurseServiceImpl.getMaxCurrentdate(benVisitDetailsOBJ.getBeneficiaryRegID(),benVisitDetailsOBJ.getVisitReason(),benVisitDetailsOBJ.getVisitCategory());
			if(i<1) {
			benVisitID = commonNurseServiceImpl.saveBeneficiaryVisitDetails(benVisitDetailsOBJ);

			// 07-06-2018 visit code
			Long benVisitCode = commonNurseServiceImpl.generateVisitCode(benVisitID, nurseUtilityClass.getVanID(),
					nurseUtilityClass.getSessionID());

			if (benVisitID != null && benVisitID > 0 && benVisitCode != null && benVisitCode > 0) {
				if (visitDetailsOBJ.has("chiefComplaints") && !visitDetailsOBJ.get("chiefComplaints").isJsonNull()) {
					BenChiefComplaint[] benChiefComplaintArray = InputMapper.gson()
							.fromJson(visitDetailsOBJ.get("chiefComplaints"), BenChiefComplaint[].class);

					List<BenChiefComplaint> benChiefComplaintList = Arrays.asList(benChiefComplaintArray);
					if (null != benChiefComplaintList && benChiefComplaintList.size() > 0) {
						for (BenChiefComplaint benChiefComplaint : benChiefComplaintList) {
							benChiefComplaint.setBenVisitID(benVisitID);
							benChiefComplaint.setVisitCode(benVisitCode);
						}
					}
					// Save Beneficiary Chief Complaints
					commonNurseServiceImpl.saveBenChiefComplaints(benChiefComplaintList);
				}
			}
			visitIdAndCodeMap.put("visitID", benVisitID);
			visitIdAndCodeMap.put("visitCode", benVisitCode);
			}
		}
		return visitIdAndCodeMap;
	}

	@Override
	public Long saveBenGeneralOPDHistoryDetails(JsonObject generalOPDHistoryOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long pastHistorySuccessFlag = null;
		Long comrbidSuccessFlag = null;
		Long medicationSuccessFlag = null;
		Long obstetricSuccessFlag = null;
		Integer menstrualHistorySuccessFlag = null;
		Long familyHistorySuccessFlag = null;
		Integer personalHistorySuccessFlag = null;
		Long allergyHistorySuccessFlag = null;
		Long childVaccineSuccessFlag = null;
		Long immunizationSuccessFlag = null;
		Long developmentHistorySuccessFlag = null;
		Long childFeedingSuccessFlag = null;
		Long perinatalHistorySuccessFlag = null;

		// Save past History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("pastHistory")
				&& !generalOPDHistoryOBJ.get("pastHistory").isJsonNull()) {
			BenMedHistory benMedHistory = InputMapper.gson().fromJson(generalOPDHistoryOBJ.get("pastHistory"),
					BenMedHistory.class);
			if (null != benMedHistory) {
				benMedHistory.setBenVisitID(benVisitID);
				benMedHistory.setVisitCode(benVisitCode);
				pastHistorySuccessFlag = commonNurseServiceImpl.saveBenPastHistory(benMedHistory);
			}

		} else {
			pastHistorySuccessFlag = new Long(1);
		}

		// Save Comorbidity/concurrent Conditions
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("comorbidConditions")
				&& !generalOPDHistoryOBJ.get("comorbidConditions").isJsonNull()) {
			WrapperComorbidCondDetails wrapperComorbidCondDetails = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("comorbidConditions"), WrapperComorbidCondDetails.class);
			if (null != wrapperComorbidCondDetails) {
				wrapperComorbidCondDetails.setBenVisitID(benVisitID);
				wrapperComorbidCondDetails.setVisitCode(benVisitCode);
				comrbidSuccessFlag = commonNurseServiceImpl.saveBenComorbidConditions(wrapperComorbidCondDetails);
			}
		} else {
			comrbidSuccessFlag = new Long(1);
		}

		// Save Medication History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("medicationHistory")
				&& !generalOPDHistoryOBJ.get("medicationHistory").isJsonNull()) {
			WrapperMedicationHistory wrapperMedicationHistory = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("medicationHistory"), WrapperMedicationHistory.class);
			if (null != wrapperMedicationHistory) {
				wrapperMedicationHistory.setBenVisitID(benVisitID);
				wrapperMedicationHistory.setVisitCode(benVisitCode);
				medicationSuccessFlag = commonNurseServiceImpl.saveBenMedicationHistory(wrapperMedicationHistory);
			}

		} else {
			medicationSuccessFlag = new Long(1);
		}

		// Save Past Obstetric History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("femaleObstetricHistory")
				&& !generalOPDHistoryOBJ.get("femaleObstetricHistory").isJsonNull()) {
			WrapperFemaleObstetricHistory wrapperFemaleObstetricHistory = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("femaleObstetricHistory"), WrapperFemaleObstetricHistory.class);

			if (wrapperFemaleObstetricHistory != null) {
				wrapperFemaleObstetricHistory.setBenVisitID(benVisitID);
				wrapperFemaleObstetricHistory.setVisitCode(benVisitCode);
				obstetricSuccessFlag = commonNurseServiceImpl.saveFemaleObstetricHistory(wrapperFemaleObstetricHistory);
			} else {
				// Female Obstetric Details not provided.
			}

		} else {
			obstetricSuccessFlag = new Long(1);
		}

		// Save Menstrual History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("menstrualHistory")
				&& !generalOPDHistoryOBJ.get("menstrualHistory").isJsonNull()) {
			BenMenstrualDetails menstrualDetails = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("menstrualHistory"), BenMenstrualDetails.class);
			if (null != menstrualDetails) {
				menstrualDetails.setBenVisitID(benVisitID);
				menstrualDetails.setVisitCode(benVisitCode);
				menstrualHistorySuccessFlag = commonNurseServiceImpl.saveBenMenstrualHistory(menstrualDetails);
			}

		} else {
			menstrualHistorySuccessFlag = 1;
		}

		// Save Family History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("familyHistory")
				&& !generalOPDHistoryOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(generalOPDHistoryOBJ.get("familyHistory"),
					BenFamilyHistory.class);
			if (null != benFamilyHistory) {
				benFamilyHistory.setBenVisitID(benVisitID);
				benFamilyHistory.setVisitCode(benVisitCode);
				familyHistorySuccessFlag = commonNurseServiceImpl.saveBenFamilyHistory(benFamilyHistory);
			}
		} else {
			familyHistorySuccessFlag = new Long(1);
		}

		// Save Personal History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("personalHistory")
				&& !generalOPDHistoryOBJ.get("personalHistory").isJsonNull()) {
			// Save Ben Personal Habits..
			BenPersonalHabit personalHabit = InputMapper.gson().fromJson(generalOPDHistoryOBJ.get("personalHistory"),
					BenPersonalHabit.class);
			if (null != personalHabit) {
				personalHabit.setBenVisitID(benVisitID);
				personalHabit.setVisitCode(benVisitCode);
				personalHistorySuccessFlag = commonNurseServiceImpl.savePersonalHistory(personalHabit);
			}

			BenAllergyHistory benAllergyHistory = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("personalHistory"), BenAllergyHistory.class);
			if (null != benAllergyHistory) {
				benAllergyHistory.setBenVisitID(benVisitID);
				benAllergyHistory.setVisitCode(benVisitCode);
				allergyHistorySuccessFlag = commonNurseServiceImpl.saveAllergyHistory(benAllergyHistory);
			}

		} else {
			personalHistorySuccessFlag = 1;
			allergyHistorySuccessFlag = new Long(1);
		}

		// Save Other/Optional Vaccines History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("childVaccineDetails")
				&& !generalOPDHistoryOBJ.get("childVaccineDetails").isJsonNull()) {
			WrapperChildOptionalVaccineDetail wrapperChildVaccineDetail = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("childVaccineDetails"), WrapperChildOptionalVaccineDetail.class);
			if (null != wrapperChildVaccineDetail) {
				wrapperChildVaccineDetail.setBenVisitID(benVisitID);
				wrapperChildVaccineDetail.setVisitCode(benVisitCode);
				childVaccineSuccessFlag = commonNurseServiceImpl
						.saveChildOptionalVaccineDetail(wrapperChildVaccineDetail);
			} else {
				// Child Optional Vaccine Detail not provided.
			}

		} else {
			childVaccineSuccessFlag = new Long(1);
		}

		// Save Immunization History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("immunizationHistory")
				&& !generalOPDHistoryOBJ.get("immunizationHistory").isJsonNull()) {
			WrapperImmunizationHistory wrapperImmunizationHistory = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("immunizationHistory"), WrapperImmunizationHistory.class);
			if (null != wrapperImmunizationHistory) {
				wrapperImmunizationHistory.setBenVisitID(benVisitID);
				wrapperImmunizationHistory.setVisitCode(benVisitCode);
				immunizationSuccessFlag = commonNurseServiceImpl.saveImmunizationHistory(wrapperImmunizationHistory);
			} else {

				// ImmunizationList Data not Available
			}

		} else {
			immunizationSuccessFlag = new Long(1);
		}

		// Save Development History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("developmentHistory")
				&& !generalOPDHistoryOBJ.get("developmentHistory").isJsonNull()) {
			BenChildDevelopmentHistory benChildDevelopmentHistory = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("developmentHistory"), BenChildDevelopmentHistory.class);

			if (null != benChildDevelopmentHistory) {
				benChildDevelopmentHistory.setBenVisitID(benVisitID);
				benChildDevelopmentHistory.setVisitCode(benVisitCode);
				developmentHistorySuccessFlag = commonNurseServiceImpl
						.saveChildDevelopmentHistory(benChildDevelopmentHistory);
			}

		} else {
			developmentHistorySuccessFlag = new Long(1);
		}

		// Save Feeding History
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("feedingHistory")
				&& !generalOPDHistoryOBJ.get("feedingHistory").isJsonNull()) {
			ChildFeedingDetails childFeedingDetails = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("feedingHistory"), ChildFeedingDetails.class);

			if (null != childFeedingDetails) {
				childFeedingDetails.setBenVisitID(benVisitID);
				childFeedingDetails.setVisitCode(benVisitCode);
				childFeedingSuccessFlag = commonNurseServiceImpl.saveChildFeedingHistory(childFeedingDetails);
			}

		}
		{
			childFeedingSuccessFlag = new Long(1);
		}

		// Save Perinatal Histroy
		if (generalOPDHistoryOBJ != null && generalOPDHistoryOBJ.has("perinatalHistroy")
				&& !generalOPDHistoryOBJ.get("perinatalHistroy").isJsonNull()) {
			PerinatalHistory perinatalHistory = InputMapper.gson()
					.fromJson(generalOPDHistoryOBJ.get("perinatalHistroy"), PerinatalHistory.class);

			if (null != perinatalHistory) {
				perinatalHistory.setBenVisitID(benVisitID);
				perinatalHistory.setVisitCode(benVisitCode);
				perinatalHistorySuccessFlag = commonNurseServiceImpl.savePerinatalHistory(perinatalHistory);
			}

		}
		{
			perinatalHistorySuccessFlag = new Long(1);
		}

		Long historySaveSucccessFlag = null;

		if ((null != pastHistorySuccessFlag && pastHistorySuccessFlag > 0)
				&& (null != comrbidSuccessFlag && comrbidSuccessFlag > 0)
				&& (null != medicationSuccessFlag && medicationSuccessFlag > 0)
				&& (null != obstetricSuccessFlag && obstetricSuccessFlag > 0)
				&& (null != menstrualHistorySuccessFlag && menstrualHistorySuccessFlag > 0)
				&& (null != familyHistorySuccessFlag && familyHistorySuccessFlag > 0)
				&& (null != personalHistorySuccessFlag && personalHistorySuccessFlag > 0)
				&& (null != allergyHistorySuccessFlag && allergyHistorySuccessFlag > 0)
				&& (null != childVaccineSuccessFlag && childVaccineSuccessFlag > 0)
				&& (null != immunizationSuccessFlag && immunizationSuccessFlag > 0)
				&& (null != developmentHistorySuccessFlag && developmentHistorySuccessFlag > 0)
				&& (null != childFeedingSuccessFlag && childFeedingSuccessFlag > 0)
				&& (null != perinatalHistorySuccessFlag && perinatalHistorySuccessFlag > 0)) {

			historySaveSucccessFlag = pastHistorySuccessFlag;
		}
		return historySaveSucccessFlag;
	}

	@Override
	public Long saveBenVitalDetails(JsonObject vitalDetailsOBJ, Long benVisitID, Long benVisitCode) throws Exception {
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
		} else {
			vitalSuccessFlag = new Long(1);
		}

		return vitalSuccessFlag;
	}

	@Override
	public Long saveBenExaminationDetails(JsonObject examinationDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {

		Long genExmnSuccessFlag = null;
		Long headToToeExmnSuccessFlag = null;
		Long gastroIntsExmnSuccessFlag = null;
		Long cardiExmnSuccessFlag = null;
		Long respiratoryExmnSuccessFlag = null;
		Long centralNrvsExmnSuccessFlag = null;
		Long muskelstlExmnSuccessFlag = null;
		Long genitorinaryExmnSuccessFlag = null;

		// Save General Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("generalExamination")
				&& !examinationDetailsOBJ.get("generalExamination").isJsonNull()) {
			PhyGeneralExamination generalExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("generalExamination"), PhyGeneralExamination.class);
			if (null != generalExamination) {
				generalExamination.setBenVisitID(benVisitID);
				generalExamination.setVisitCode(benVisitCode);
				genExmnSuccessFlag = commonNurseServiceImpl.savePhyGeneralExamination(generalExamination);
			}

		} else {
			genExmnSuccessFlag = new Long(1);
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
			}

		} else {
			headToToeExmnSuccessFlag = new Long(1);
		}

		// Save Gastro Intestinal Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("gastroIntestinalExamination")
				&& !examinationDetailsOBJ.get("gastroIntestinalExamination").isJsonNull()) {
			SysGastrointestinalExamination gastrointestinalExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("gastroIntestinalExamination"), SysGastrointestinalExamination.class);
			if (null != gastrointestinalExamination) {
				gastrointestinalExamination.setBenVisitID(benVisitID);
				gastrointestinalExamination.setVisitCode(benVisitCode);
				gastroIntsExmnSuccessFlag = commonNurseServiceImpl
						.saveSysGastrointestinalExamination(gastrointestinalExamination);

			}
		} else {
			gastroIntsExmnSuccessFlag = new Long(1);
		}

		// Save cardioVascular Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("cardioVascularExamination")
				&& !examinationDetailsOBJ.get("cardioVascularExamination").isJsonNull()) {
			SysCardiovascularExamination cardiovascularExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("cardioVascularExamination"), SysCardiovascularExamination.class);
			if (null != cardiovascularExamination) {
				cardiovascularExamination.setBenVisitID(benVisitID);
				cardiovascularExamination.setVisitCode(benVisitCode);
				cardiExmnSuccessFlag = commonNurseServiceImpl
						.saveSysCardiovascularExamination(cardiovascularExamination);

			}
		} else {
			cardiExmnSuccessFlag = new Long(1);
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
			}
		} else {
			respiratoryExmnSuccessFlag = new Long(1);
		}

		// Save Central Nervous System Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("centralNervousSystemExamination")
				&& !examinationDetailsOBJ.get("centralNervousSystemExamination").isJsonNull()) {
			SysCentralNervousExamination sysCentralNervousExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("centralNervousSystemExamination"), SysCentralNervousExamination.class);
			if (null != sysCentralNervousExamination) {
				sysCentralNervousExamination.setBenVisitID(benVisitID);
				sysCentralNervousExamination.setVisitCode(benVisitCode);
				centralNrvsExmnSuccessFlag = commonNurseServiceImpl
						.saveSysCentralNervousExamination(sysCentralNervousExamination);
			}
		} else {
			centralNrvsExmnSuccessFlag = new Long(1);
		}

		// Save Musculoskeletal System Examination Details
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

			}
		} else {
			muskelstlExmnSuccessFlag = new Long(1);
		}

		// Save Genito Urinary System Examination Details
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

			}
		} else {
			genitorinaryExmnSuccessFlag = new Long(1);
		}

		Long exmnSuccessFlag = null;
		if ((null != genExmnSuccessFlag && genExmnSuccessFlag > 0)
				&& (null != headToToeExmnSuccessFlag && headToToeExmnSuccessFlag > 0)
				&& (null != gastroIntsExmnSuccessFlag && gastroIntsExmnSuccessFlag > 0)
				&& (null != cardiExmnSuccessFlag && cardiExmnSuccessFlag > 0)
				&& (null != respiratoryExmnSuccessFlag && respiratoryExmnSuccessFlag > 0)
				&& (null != centralNrvsExmnSuccessFlag && centralNrvsExmnSuccessFlag > 0)
				&& (null != muskelstlExmnSuccessFlag && muskelstlExmnSuccessFlag > 0)
				&& (null != genitorinaryExmnSuccessFlag && genitorinaryExmnSuccessFlag > 0)) {
			exmnSuccessFlag = genExmnSuccessFlag;
		}

		return exmnSuccessFlag;

	}/*
		 * /// --------------- End of saving nurse data ------------------------
		 * 
		 * // ------- Fetch beneficiary all past history data ------------------ public
		 * String getPastHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPastMedicalHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all past history data ----------
		 * 
		 * // ------- Fetch beneficiary all Personal Tobacco history data-----------
		 * public String getPersonalTobaccoHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPersonalTobaccoHistory(beneficiaryRegID); }
		 * /// ------- End of Fetch beneficiary all Personal Tobacco history data------
		 * 
		 * // ------- Fetch beneficiary all Personal Alcohol history data -----------
		 * public String getPersonalAlcoholHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPersonalAlcoholHistory(beneficiaryRegID); }
		 * /// ------- End of Fetch beneficiary all Personal Alcohol history data-----
		 * 
		 * // ------- Fetch beneficiary all Personal Allergy history data -----------
		 * public String getPersonalAllergyHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPersonalAllergyHistory(beneficiaryRegID); }
		 * /// ------- End of Fetch beneficiary all Personal Allergy history data------
		 * 
		 * // ------- Fetch beneficiary all Medication history data ----------- public
		 * String getMedicationHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPersonalMedicationHistory(beneficiaryRegID); }
		 * /// ------- End of Fetch beneficiary all Medication history data --
		 * 
		 * // ------- Fetch beneficiary all Family history data --------------- public
		 * String getFamilyHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPersonalFamilyHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all Family history data ------
		 * 
		 * // ------- Fetch beneficiary all Menstrual history data ----------- public
		 * String getMenstrualHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenMenstrualHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all Menstrual history data --
		 * 
		 * // ------- Fetch beneficiary all past obstetric history data ---------------
		 * public String getObstetricHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPastObstetricHistory(beneficiaryRegID); }
		 * 
		 * /// ------- End of Fetch beneficiary all past obstetric history data ------
		 * 
		 * // ------- Fetch beneficiary all Comorbid conditions history data----------
		 * public String getComorbidHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenComorbidityHistory(beneficiaryRegID); } ///
		 * -----End of Fetch beneficiary all Comorbid conditions history data ----
		 * 
		 * // ------- Fetch beneficiary all Child Vaccine history data ---------------
		 * public String getChildVaccineHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenOptionalVaccineHistory(beneficiaryRegID); }
		 * /// ------- End of Fetch beneficiary all Child Vaccine history data ------
		 * 
		 * // ------- Fetch beneficiary all Immunization history data ---------------
		 * public String getImmunizationHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenImmunizationHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all Immunization history data ------
		 * 
		 * // ------- Fetch beneficiary all Perinatal history data ---------------
		 * public String getBenPerinatalHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenPerinatalHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all Perinatal history data ------
		 * 
		 * // ------- Fetch beneficiary all Feeding history data --------------- public
		 * String getBenFeedingHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenFeedingHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all Feeding history data ------
		 * 
		 * // ------- Fetch beneficiary all Development history data ---------------
		 * public String getBenDevelopmentHistoryData(Long beneficiaryRegID) { return
		 * commonNurseServiceImpl.fetchBenDevelopmentHistory(beneficiaryRegID); } ///
		 * ------- End of Fetch beneficiary all Development history data ------
		 */

	/**
	 * @param JsonObject
	 * @return saveSuccessFlag
	 * 
	 */
	/// --------------- start of saving doctor data ------------------------
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long saveDoctorData(JsonObject requestOBJ, String Authorization) throws Exception {
		Boolean doctorSignatureFlag = false;
		if (requestOBJ.has("doctorSignatureFlag") && !requestOBJ.get("doctorSignatureFlag").isJsonNull()) {
			doctorSignatureFlag = requestOBJ.get("doctorSignatureFlag").getAsBoolean();
		}
		Long saveSuccessFlag = null;
		Long prescriptionID = null;
		Long investigationSuccessFlag = null;
		Integer findingSuccessFlag = null;
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

			// creating prescription object
			PrescriptionDetail prescriptionDetail = new PrescriptionDetail();

			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
				JsonObject diagnosisObj = requestOBJ.getAsJsonObject("diagnosis");

				prescriptionDetail = InputMapper.gson().fromJson(diagnosisObj, PrescriptionDetail.class);
			} else {
			}

			WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson()
					.fromJson(requestOBJ.get("investigation"), WrapperBenInvestigationANC.class);
			// Save Prescription
			prescriptionDetail.setExternalInvestigation(wrapperBenInvestigationANC.getExternalInvestigations());
			prescriptionID = commonNurseServiceImpl.saveBenPrescription(prescriptionDetail);

			// save prescribed lab test
			if (isTestPrescribed) {
				if (wrapperBenInvestigationANC != null) {
					wrapperBenInvestigationANC.setPrescriptionID(prescriptionID);
					investigationSuccessFlag = commonNurseServiceImpl.saveBenInvestigation(wrapperBenInvestigationANC);
				}
			} else {
				investigationSuccessFlag = new Long(1);
			}

			// save prescribed medicine
			if (isMedicinePrescribed) {
				PrescribedDrugDetail[] prescribedDrugDetail = InputMapper.gson()
						.fromJson(requestOBJ.get("prescription"), PrescribedDrugDetail[].class);

				List<PrescribedDrugDetail> prescribedDrugDetailList = Arrays.asList(prescribedDrugDetail);

				if (prescribedDrugDetailList.size() > 0) {
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
			} else {
				prescriptionSuccessFlag = 1;
			}

			// save referral details
			if (requestOBJ.has("refer") && !requestOBJ.get("refer").isJsonNull()) {
				referSaveSuccessFlag = commonDoctorServiceImpl
						.saveBenReferDetails(requestOBJ.get("refer").getAsJsonObject());
			} else {
				referSaveSuccessFlag = new Long(1);
			}

			// check if all requested data saved properly
			if ((findingSuccessFlag != null && findingSuccessFlag > 0) && (prescriptionID != null && prescriptionID > 0)
					&& (investigationSuccessFlag != null && investigationSuccessFlag > 0)
					&& (prescriptionSuccessFlag != null && prescriptionSuccessFlag > 0)
					&& (referSaveSuccessFlag != null && referSaveSuccessFlag > 0)) {

				// call method to update beneficiary flow table
				int i = commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(commonUtilityClass, isTestPrescribed,
						isMedicinePrescribed, tcRequestOBJ, doctorSignatureFlag);

				if (i > 0) {
					saveSuccessFlag = investigationSuccessFlag;
				} else
					throw new RuntimeException();
			} else {
				throw new RuntimeException();
			}
		} else {
			// request OBJ is null.
		}
		return saveSuccessFlag;
	}
	/// ------------------- END of saving doctor data ------------------------

	/// --------------- Start of Fetching GeneralOPD Nurse Data ----------------
	public String getBenVisitDetailsFrmNurseGOPD(Long benRegID, Long visitCode) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		BeneficiaryVisitDetail visitDetail = commonNurseServiceImpl.getCSVisitDetails(benRegID, visitCode);

		resMap.put("GOPDNurseVisitDetail", new Gson().toJson(visitDetail));

		resMap.put("BenChiefComplaints", commonNurseServiceImpl.getBenChiefComplaints(benRegID, visitCode));

		return resMap.toString();
	}

	public String getBenHistoryDetails(Long benRegID, Long visitCode) {
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

		HistoryDetailsMap.put("DevelopmentHistory", commonNurseServiceImpl.getDevelopmentHistory(benRegID, visitCode));
		HistoryDetailsMap.put("PerinatalHistory", commonNurseServiceImpl.getPerinatalHistory(benRegID, visitCode));
		HistoryDetailsMap.put("FeedingHistory", commonNurseServiceImpl.getFeedingHistory(benRegID, visitCode));

		return new Gson().toJson(HistoryDetailsMap);
	}

	public String getBeneficiaryVitalDetails(Long beneficiaryRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("benAnthropometryDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(beneficiaryRegID, benVisitID));
		resMap.put("benPhysicalVitalDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(beneficiaryRegID, benVisitID));

		return resMap.toString();
	}

	public String getExaminationDetailsData(Long benRegID, Long visitCode) {
		Map<String, Object> examinationDetailsMap = new HashMap<String, Object>();

		examinationDetailsMap.put("generalExamination",
				commonNurseServiceImpl.getGeneralExaminationData(benRegID, visitCode));
		examinationDetailsMap.put("headToToeExamination",
				commonNurseServiceImpl.getHeadToToeExaminationData(benRegID, visitCode));
		examinationDetailsMap.put("gastrointestinalExamination",
				commonNurseServiceImpl.getSysGastrointestinalExamination(benRegID, visitCode));
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

		return new Gson().toJson(examinationDetailsMap);
	}

	/// --------------- END of Fetching GeneralOPD Nurse Data ----------------

	/// --------------- start of updating GeneralOPD Nurse Data ----------------
	@Transactional(rollbackFor = Exception.class)
	public int UpdateVisitDetails(JsonObject jsnOBJ) throws Exception {

		int chiefCompltUpdateRes = 0;

		if (jsnOBJ != null && jsnOBJ.has("visitDetails") && !jsnOBJ.get("visitDetails").isJsonNull()) {

			if (jsnOBJ.has("chiefComplaints") && !jsnOBJ.get("chiefComplaints").isJsonNull()) {
				// Update Ben Chief Complaints
				BenChiefComplaint[] benChiefComplaintArray = InputMapper.gson().fromJson(jsnOBJ.get("chiefComplaints"),
						BenChiefComplaint[].class);

				List<BenChiefComplaint> benChiefComplaintList = Arrays.asList(benChiefComplaintArray);

				chiefCompltUpdateRes = commonNurseServiceImpl.updateBenChiefComplaints(benChiefComplaintList);
			}
		}
		return chiefCompltUpdateRes;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for General OPD History updating by Doctor
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateBenHistoryDetails(JsonObject historyOBJ) throws Exception {
		int pastHistorySuccessFlag = 0;
		int comrbidSuccessFlag = 0;
		int medicationSuccessFlag = 0;
		int personalHistorySuccessFlag = 0;
		int allergyHistorySuccessFlag = 0;
		int familyHistorySuccessFlag = 0;
		int menstrualHistorySuccessFlag = 0;
		int obstetricSuccessFlag = 0;
		int childVaccineSuccessFlag = 0;
		int childFeedingSuccessFlag = 0;
		int perinatalHistorySuccessFlag = 0;
		int developmentHistorySuccessFlag = 0;
		int immunizationSuccessFlag = 0;

		// Update Past History
		if (historyOBJ != null && historyOBJ.has("pastHistory") && !historyOBJ.get("pastHistory").isJsonNull()) {
			BenMedHistory benMedHistory = InputMapper.gson().fromJson(historyOBJ.get("pastHistory"),
					BenMedHistory.class);
			pastHistorySuccessFlag = commonNurseServiceImpl.updateBenPastHistoryDetails(benMedHistory);

		} else {
			pastHistorySuccessFlag = 1;
		}

		// Update Comorbidity/concurrent Conditions
		if (historyOBJ != null && historyOBJ.has("comorbidConditions")
				&& !historyOBJ.get("comorbidConditions").isJsonNull()) {
			WrapperComorbidCondDetails wrapperComorbidCondDetails = InputMapper.gson()
					.fromJson(historyOBJ.get("comorbidConditions"), WrapperComorbidCondDetails.class);
			comrbidSuccessFlag = commonNurseServiceImpl.updateBenComorbidConditions(wrapperComorbidCondDetails);
		} else {
			comrbidSuccessFlag = 1;
		}

		// Update Medication History
		if (historyOBJ != null && historyOBJ.has("medicationHistory")
				&& !historyOBJ.get("medicationHistory").isJsonNull()) {
			WrapperMedicationHistory wrapperMedicationHistory = InputMapper.gson()
					.fromJson(historyOBJ.get("medicationHistory"), WrapperMedicationHistory.class);
			medicationSuccessFlag = commonNurseServiceImpl.updateBenMedicationHistory(wrapperMedicationHistory);
		} else {
			medicationSuccessFlag = 1;
		}
		// Update Personal History
		if (historyOBJ != null && historyOBJ.has("personalHistory")
				&& !historyOBJ.get("personalHistory").isJsonNull()) {
			// Update Ben Personal Habits..
			BenPersonalHabit personalHabit = InputMapper.gson().fromJson(historyOBJ.get("personalHistory"),
					BenPersonalHabit.class);

			personalHistorySuccessFlag = commonNurseServiceImpl.updateBenPersonalHistory(personalHabit);

			// Update Ben Allergy History..
			BenAllergyHistory benAllergyHistory = InputMapper.gson().fromJson(historyOBJ.get("personalHistory"),
					BenAllergyHistory.class);
			allergyHistorySuccessFlag = commonNurseServiceImpl.updateBenAllergicHistory(benAllergyHistory);

		} else {
			allergyHistorySuccessFlag = 1;
			personalHistorySuccessFlag = 1;
		}

		// Update Family History
		if (historyOBJ != null && historyOBJ.has("familyHistory") && !historyOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(historyOBJ.get("familyHistory"),
					BenFamilyHistory.class);
			familyHistorySuccessFlag = commonNurseServiceImpl.updateBenFamilyHistory(benFamilyHistory);
		} else {
			familyHistorySuccessFlag = 1;
		}

		// Update Menstrual History
		if (historyOBJ != null && historyOBJ.has("menstrualHistory")
				&& !historyOBJ.get("menstrualHistory").isJsonNull()) {
			BenMenstrualDetails menstrualDetails = InputMapper.gson().fromJson(historyOBJ.get("menstrualHistory"),
					BenMenstrualDetails.class);
			menstrualHistorySuccessFlag = commonNurseServiceImpl.updateMenstrualHistory(menstrualDetails);
		} else {
			menstrualHistorySuccessFlag = 1;
		}

		// Update Past Obstetric History
		if (historyOBJ != null && historyOBJ.has("femaleObstetricHistory")
				&& !historyOBJ.get("femaleObstetricHistory").isJsonNull()) {
			WrapperFemaleObstetricHistory wrapperFemaleObstetricHistory = InputMapper.gson()
					.fromJson(historyOBJ.get("femaleObstetricHistory"), WrapperFemaleObstetricHistory.class);

			obstetricSuccessFlag = commonNurseServiceImpl.updatePastObstetricHistory(wrapperFemaleObstetricHistory);
		} else {
			obstetricSuccessFlag = 1;
		}

		//update immunization history
		if (historyOBJ != null && historyOBJ.has("immunizationHistory")
				&& !historyOBJ.get("immunizationHistory").isJsonNull()) {

			JsonObject immunizationHistory = historyOBJ.getAsJsonObject("immunizationHistory");
			if (immunizationHistory.get("immunizationList") != null
					&& immunizationHistory.getAsJsonArray("immunizationList").size() > 0) {
				WrapperImmunizationHistory wrapperImmunizationHistory = InputMapper.gson()
						.fromJson(historyOBJ.get("immunizationHistory"), WrapperImmunizationHistory.class);
				immunizationSuccessFlag = commonNurseServiceImpl
						.updateChildImmunizationDetail(wrapperImmunizationHistory);
			} else {
				immunizationSuccessFlag = 1;
			}
		} else {
			immunizationSuccessFlag = 1;
		}

		// Update Other/Optional Vaccines History
		if (historyOBJ != null && historyOBJ.has("childVaccineDetails")
				&& !historyOBJ.get("childVaccineDetails").isJsonNull()) {
			WrapperChildOptionalVaccineDetail wrapperChildVaccineDetail = InputMapper.gson()
					.fromJson(historyOBJ.get("childVaccineDetails"), WrapperChildOptionalVaccineDetail.class);
			childVaccineSuccessFlag = commonNurseServiceImpl
					.updateChildOptionalVaccineDetail(wrapperChildVaccineDetail);
		} else {
			childVaccineSuccessFlag = 1;
		}

		// Update ChildFeeding History
		if (historyOBJ != null && historyOBJ.has("feedingHistory") && !historyOBJ.get("feedingHistory").isJsonNull()) {
			ChildFeedingDetails childFeedingDetails = InputMapper.gson().fromJson(historyOBJ.get("feedingHistory"),
					ChildFeedingDetails.class);

			if (null != childFeedingDetails) {
				childFeedingSuccessFlag = commonNurseServiceImpl.updateChildFeedingHistory(childFeedingDetails);
			}

		} else {
			childFeedingSuccessFlag = 1;
		}

		// Update Perinatal History
		if (historyOBJ != null && historyOBJ.has("perinatalHistroy")
				&& !historyOBJ.get("perinatalHistroy").isJsonNull()) {
			PerinatalHistory perinatalHistory = InputMapper.gson().fromJson(historyOBJ.get("perinatalHistroy"),
					PerinatalHistory.class);

			if (null != perinatalHistory) {
				perinatalHistorySuccessFlag = commonNurseServiceImpl.updatePerinatalHistory(perinatalHistory);
			}

		} else {
			perinatalHistorySuccessFlag = 1;
		}

		// Update Development History
		if (historyOBJ != null && historyOBJ.has("developmentHistory")
				&& !historyOBJ.get("developmentHistory").isJsonNull()) {
			BenChildDevelopmentHistory benChildDevelopmentHistory = InputMapper.gson()
					.fromJson(historyOBJ.get("developmentHistory"), BenChildDevelopmentHistory.class);

			if (null != benChildDevelopmentHistory) {
				developmentHistorySuccessFlag = commonNurseServiceImpl
						.updateChildDevelopmentHistory(benChildDevelopmentHistory);
			}

		} else {
			developmentHistorySuccessFlag = 1;
		}

		int historyUpdateSuccessFlag = 0;

		if (pastHistorySuccessFlag > 0 && comrbidSuccessFlag > 0 && medicationSuccessFlag > 0
				&& allergyHistorySuccessFlag > 0 && familyHistorySuccessFlag > 0 && obstetricSuccessFlag > 0
				&& childVaccineSuccessFlag > 0 && personalHistorySuccessFlag > 0 && menstrualHistorySuccessFlag > 0
				&& immunizationSuccessFlag > 0 && childFeedingSuccessFlag > 0 && perinatalHistorySuccessFlag > 0
				&& developmentHistorySuccessFlag > 0) {

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
	public int updateBenVitalDetails(JsonObject vitalDetailsOBJ) throws Exception {
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
		} else {
			vitalSuccessFlag = 1;
		}

		return vitalSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for Examinationm data updating
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateBenExaminationDetails(JsonObject examinationDetailsOBJ) throws Exception {

		int exmnSuccessFlag = 0;

		int genExmnSuccessFlag = 0;
		int headToToeExmnSuccessFlag = 0;
		int gastroIntsExmnSuccessFlag = 0;
		int cardiExmnSuccessFlag = 0;
		int respiratoryExmnSuccessFlag = 0;
		int centralNrvsExmnSuccessFlag = 0;
		int muskelstlExmnSuccessFlag = 0;
		int genitorinaryExmnSuccessFlag = 0;

		// Save General Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("generalExamination")
				&& !examinationDetailsOBJ.get("generalExamination").isJsonNull()) {
			PhyGeneralExamination generalExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("generalExamination"), PhyGeneralExamination.class);
			genExmnSuccessFlag = commonNurseServiceImpl.updatePhyGeneralExamination(generalExamination);
		} else {
			genExmnSuccessFlag = 1;
		}

		// Save Head to toe Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("headToToeExamination")
				&& !examinationDetailsOBJ.get("headToToeExamination").isJsonNull()) {
			PhyHeadToToeExamination headToToeExamination = InputMapper.gson()
					.fromJson(examinationDetailsOBJ.get("headToToeExamination"), PhyHeadToToeExamination.class);
			headToToeExmnSuccessFlag = commonNurseServiceImpl.updatePhyHeadToToeExamination(headToToeExamination);
		} else {
			headToToeExmnSuccessFlag = 1;
		}
		// Save Gastro Intestinal Examination Details

		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("gastroIntestinalExamination")
				&& !examinationDetailsOBJ.get("gastroIntestinalExamination").isJsonNull()) {
			SysGastrointestinalExamination gastrointestinalExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("gastroIntestinalExamination"), SysGastrointestinalExamination.class);
			gastroIntsExmnSuccessFlag = commonNurseServiceImpl
					.updateSysGastrointestinalExamination(gastrointestinalExamination);
		} else {
			gastroIntsExmnSuccessFlag = 1;
		}
		// Save Cardio Vascular Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("cardioVascularExamination")
				&& !examinationDetailsOBJ.get("cardioVascularExamination").isJsonNull()) {
			SysCardiovascularExamination cardiovascularExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("cardioVascularExamination"), SysCardiovascularExamination.class);
			cardiExmnSuccessFlag = commonNurseServiceImpl.updateSysCardiovascularExamination(cardiovascularExamination);
		} else {
			cardiExmnSuccessFlag = 1;
		}

		// Save Respiratory Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("respiratorySystemExamination")
				&& !examinationDetailsOBJ.get("respiratorySystemExamination").isJsonNull()) {
			SysRespiratoryExamination sysRespiratoryExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("respiratorySystemExamination"), SysRespiratoryExamination.class);
			respiratoryExmnSuccessFlag = commonNurseServiceImpl
					.updateSysRespiratoryExamination(sysRespiratoryExamination);
		} else {
			respiratoryExmnSuccessFlag = 1;
		}

		// Save Central Nervous Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("centralNervousSystemExamination")
				&& !examinationDetailsOBJ.get("centralNervousSystemExamination").isJsonNull()) {
			SysCentralNervousExamination sysCentralNervousExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("centralNervousSystemExamination"), SysCentralNervousExamination.class);
			centralNrvsExmnSuccessFlag = commonNurseServiceImpl
					.updateSysCentralNervousExamination(sysCentralNervousExamination);
		} else {
			centralNrvsExmnSuccessFlag = 1;
		}

		// Save Muskeloskeletal Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("musculoskeletalSystemExamination")
				&& !examinationDetailsOBJ.get("musculoskeletalSystemExamination").isJsonNull()) {
			SysMusculoskeletalSystemExamination sysMusculoskeletalSystemExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("musculoskeletalSystemExamination"),
					SysMusculoskeletalSystemExamination.class);
			muskelstlExmnSuccessFlag = commonNurseServiceImpl
					.updateSysMusculoskeletalSystemExamination(sysMusculoskeletalSystemExamination);
		} else {
			muskelstlExmnSuccessFlag = 1;
		}

		// Save Genito Urinary Examination Details
		if (examinationDetailsOBJ != null && examinationDetailsOBJ.has("genitoUrinarySystemExamination")
				&& !examinationDetailsOBJ.get("genitoUrinarySystemExamination").isJsonNull()) {
			SysGenitourinarySystemExamination sysGenitourinarySystemExamination = InputMapper.gson().fromJson(
					examinationDetailsOBJ.get("genitoUrinarySystemExamination"),
					SysGenitourinarySystemExamination.class);
			genitorinaryExmnSuccessFlag = commonNurseServiceImpl
					.updateSysGenitourinarySystemExamination(sysGenitourinarySystemExamination);
		} else {
			genitorinaryExmnSuccessFlag = 1;
		}

		if (genExmnSuccessFlag > 0 && headToToeExmnSuccessFlag > 0 && cardiExmnSuccessFlag > 0
				&& respiratoryExmnSuccessFlag > 0 && centralNrvsExmnSuccessFlag > 0 && muskelstlExmnSuccessFlag > 0
				&& genitorinaryExmnSuccessFlag > 0 && gastroIntsExmnSuccessFlag > 0) {
			exmnSuccessFlag = genExmnSuccessFlag;
		}
		return exmnSuccessFlag;
	}

	public String getBenGeneralOPDNurseData(Long benRegID, Long visitCode) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("history", getBenHistoryDetails(benRegID, visitCode));

		resMap.put("vitals", getBeneficiaryVitalDetails(benRegID, visitCode));

		resMap.put("examination", getExaminationDetailsData(benRegID, visitCode));

		return resMap.toString();
	}

	public String getBenCaseRecordFromDoctorGeneralOPD(Long benRegID, Long visitCode) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("findings", commonDoctorServiceImpl.getFindingsDetails(benRegID, visitCode));

		resMap.put("diagnosis", generalOPDDoctorServiceImpl.getGeneralOPDDiagnosisDetails(benRegID, visitCode));

		resMap.put("investigation", commonDoctorServiceImpl.getInvestigationDetails(benRegID, visitCode));

		resMap.put("prescription", commonDoctorServiceImpl.getPrescribedDrugs(benRegID, visitCode));

		resMap.put("Refer", commonDoctorServiceImpl.getReferralDetails(benRegID, visitCode));

		resMap.put("LabReport",
				new Gson().toJson(labTechnicianServiceImpl.getLabResultDataForBen(benRegID, visitCode)));

		resMap.put("GraphData", new Gson().toJson(commonNurseServiceImpl.getGraphicalTrendData(benRegID, "genOPD")));

		resMap.put("ArchivedVisitcodeForLabResult",
				labTechnicianServiceImpl.getLast_3_ArchivedTestVisitList(benRegID, visitCode));

		return resMap.toString();
	}

	// update doctor data
	@Transactional(rollbackFor = Exception.class)
	public Long updateGeneralOPDDoctorData(JsonObject requestOBJ, String Authorization) throws Exception {
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

			// creating prescription OBJ
			PrescriptionDetail prescriptionDetail = null;
			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
				// JsonObject diagnosisObj = requestOBJ.getAsJsonObject("diagnosis");
				prescriptionDetail = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"), PrescriptionDetail.class);
			}

			// generating WrapperBenInvestigationANC OBJ
			WrapperBenInvestigationANC wrapperBenInvestigationANCTMP = InputMapper.gson()
					.fromJson(requestOBJ.get("investigation"), WrapperBenInvestigationANC.class);

			if (prescriptionDetail != null) {
				prescriptionDetail.setExternalInvestigation(wrapperBenInvestigationANCTMP.getExternalInvestigations());
				prescriptionID = prescriptionDetail.getPrescriptionID();
			}

			// update prescription
			int p = commonNurseServiceImpl.updatePrescription(prescriptionDetail);

			if (p > 0)
				diagnosisSuccessFlag = 1;

			// save prescribed lab test
			if (isTestPrescribed == true) {
				WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson()
						.fromJson(requestOBJ.get("investigation"), WrapperBenInvestigationANC.class);

				if (wrapperBenInvestigationANC != null) {
					wrapperBenInvestigationANC.setPrescriptionID(prescriptionID);
					investigationSuccessFlag = commonNurseServiceImpl.saveBenInvestigation(wrapperBenInvestigationANC);
				}
			} else {
				investigationSuccessFlag = new Long(1);
			}

			// save prescribed medicine
			if (isMedicinePrescribed == true) {
				PrescribedDrugDetail[] prescribedDrugDetail = InputMapper.gson()
						.fromJson(requestOBJ.get("prescription"), PrescribedDrugDetail[].class);
				List<PrescribedDrugDetail> prescribedDrugDetailList = Arrays.asList(prescribedDrugDetail);

				if (prescribedDrugDetailList.size() > 0) {
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

			// check if all requested data updated/saved properly
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

}
