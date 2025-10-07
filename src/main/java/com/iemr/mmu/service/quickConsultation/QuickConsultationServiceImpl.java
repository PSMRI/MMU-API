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
package com.iemr.mmu.service.quickConsultation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.nurse.BenAnthropometryDetail;
import com.iemr.mmu.data.nurse.BenPhysicalVitalDetail;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.quickConsultation.BenClinicalObservations;
import com.iemr.mmu.data.quickConsultation.ExternalLabTestOrder;
import com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.data.tele_consultation.TCRequestModel;
import com.iemr.mmu.data.tele_consultation.TcSpecialistSlotBookingRequestOBJ;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.BenPhysicalVitalRepo;
import com.iemr.mmu.repo.quickConsultation.BenChiefComplaintRepo;
import com.iemr.mmu.repo.quickConsultation.BenClinicalObservationsRepo;
import com.iemr.mmu.repo.quickConsultation.ExternalTestOrderRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.anc.Utility;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.generalOPD.GeneralOPDDoctorServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

@Service
public class QuickConsultationServiceImpl implements QuickConsultationService {
	private BenChiefComplaintRepo benChiefComplaintRepo;
	private BenClinicalObservationsRepo benClinicalObservationsRepo;
	private PrescriptionDetailRepo prescriptionDetailRepo;
	// private PrescribedDrugDetailRepo prescribedDrugDetailRepo;
	// private LabTestOrderDetailRepo labTestOrderDetailRepo;
	private ExternalTestOrderRepo externalTestOrderRepo;
	// private NurseServiceImpl nurseServiceImpl;
	// private BenVisitDetailRepo benVisitDetailRepo;
	private CommonNurseServiceImpl commonNurseServiceImpl;
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	// private GeneralOPDDoctorServiceImpl generalOPDDoctorServiceImpl;

	private GeneralOPDDoctorServiceImpl generalOPDDoctorServiceImpl;

	@Autowired
	private TeleConsultationServiceImpl teleConsultationServiceImpl;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	public void setGeneralOPDDoctorServiceImpl(GeneralOPDDoctorServiceImpl generalOPDDoctorServiceImpl) {
		this.generalOPDDoctorServiceImpl = generalOPDDoctorServiceImpl;
	}

	@Autowired
	public void setLabTechnicianServiceImpl(LabTechnicianServiceImpl labTechnicianServiceImpl) {
		this.labTechnicianServiceImpl = labTechnicianServiceImpl;
	}

	@Autowired
	public void setCommonDoctorServiceImpl(CommonDoctorServiceImpl commonDoctorServiceImpl) {
		this.commonDoctorServiceImpl = commonDoctorServiceImpl;
	}

	// @Autowired
	// public void setGeneralOPDDoctorServiceImpl(GeneralOPDDoctorServiceImpl
	// generalOPDDoctorServiceImpl) {
	// this.generalOPDDoctorServiceImpl = generalOPDDoctorServiceImpl;
	// }

	@Autowired
	public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
		this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}

	@Autowired
	private BenPhysicalVitalRepo benPhysicalVitalRepo;

	// @Autowired
	// public void setBeneficiaryVisitDetail(BenVisitDetailRepo benVisitDetailRepo)
	// {
	// this.benVisitDetailRepo = benVisitDetailRepo;
	// }
	//
	// @Autowired
	// public void setNurseServiceImpl(NurseServiceImpl nurseServiceImpl) {
	// this.nurseServiceImpl = nurseServiceImpl;
	// }

	@Autowired
	public void setBenChiefComplaintRepo(BenChiefComplaintRepo benChiefComplaintRepo) {
		this.benChiefComplaintRepo = benChiefComplaintRepo;
	}

	@Autowired
	public void setBenClinicalObservationsRepo(BenClinicalObservationsRepo benClinicalObservationsRepo) {
		this.benClinicalObservationsRepo = benClinicalObservationsRepo;
	}

	@Autowired
	public void setPrescriptionDetailRepo(PrescriptionDetailRepo prescriptionDetailRepo) {
		this.prescriptionDetailRepo = prescriptionDetailRepo;
	}

	// @Autowired
	// public void setPrescribedDrugDetailRepo(PrescribedDrugDetailRepo
	// prescribedDrugDetailRepo) {
	// this.prescribedDrugDetailRepo = prescribedDrugDetailRepo;
	// }
	//
	// @Autowired
	// public void setLabTestOrderDetailRepo(LabTestOrderDetailRepo
	// labTestOrderDetailRepo) {
	// this.labTestOrderDetailRepo = labTestOrderDetailRepo;
	// }

	@Autowired
	public void setExternalTestOrderRepo(ExternalTestOrderRepo externalTestOrderRepo) {
		this.externalTestOrderRepo = externalTestOrderRepo;
	}
	@Autowired
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	@Override
	public Long saveBeneficiaryChiefComplaint(JsonObject caseSheet) {
		ArrayList<BenChiefComplaint> benChiefComplaints = BenChiefComplaint.getBenChiefComplaintList(caseSheet);
		Long returnOBJ = null;
		if (benChiefComplaints != null && benChiefComplaints.size() > 0) {
			List<BenChiefComplaint> chiefComplaints = (List<BenChiefComplaint>) benChiefComplaintRepo
					.saveAll(benChiefComplaints);
logger.info("in save beneficiary chief complaint"+ benChiefComplaints.size()+"---"+chiefComplaints.size());
        if (benChiefComplaints.size() == chiefComplaints.size()) {
            // Update vanSerialNo for each saved record
			logger.info("inside if");
            for (BenChiefComplaint complaint : chiefComplaints) {
				logger.info("inside for"+complaint.getBenChiefComplaintID());
                if (complaint.getBenChiefComplaintID() != null) {
					logger.info("inside for if"+complaint.getBenChiefComplaintID());
                    benChiefComplaintRepo.updateVanSerialNo(complaint.getBenChiefComplaintID());
                }
            }
            returnOBJ = new Long(1);
        }
			// if (benChiefComplaints.size() == chiefComplaints.size()) {
			// 	returnOBJ = new Long(1);
			// }
		} else {
			returnOBJ = new Long(1);
		}

		return returnOBJ;
	}

	@Override
	public Long saveBeneficiaryClinicalObservations(JsonObject caseSheet) throws Exception {

		BenClinicalObservations benClinicalObservations = InputMapper.gson().fromJson(caseSheet,
				BenClinicalObservations.class);

		// get snomedCT code for symptoms
		if (benClinicalObservations != null) {
			String[] snomedCTcodeArr = commonDoctorServiceImpl
					.getSnomedCTcode(benClinicalObservations.getOtherSymptoms());
			if (snomedCTcodeArr != null && snomedCTcodeArr.length > 1) {
				benClinicalObservations.setOtherSymptomsSCTCode(snomedCTcodeArr[0]);
				benClinicalObservations.setOtherSymptomsSCTTerm(snomedCTcodeArr[1]);
			}
		}
		BenClinicalObservations benClinicalObservation = benClinicalObservationsRepo.save(benClinicalObservations);
		if (null != benClinicalObservation && benClinicalObservation.getClinicalObservationID() > 0) {
			return benClinicalObservation.getClinicalObservationID();
		}
		return null;
	}

	// Prescription for ANC...
	/* @Deprecated */
	public Long saveBenPrescriptionForANC(PrescriptionDetail prescription) {
		Long r = null;
		PrescriptionDetail prescriptionRS = prescriptionDetailRepo.save(prescription);
		if (prescriptionRS != null && prescriptionRS.getPrescriptionID() > 0) {
			r = prescriptionRS.getPrescriptionID();
		}
		return r;
	}

	// now not required
	// @Deprecated
	// @Override
	// public Long saveBeneficiaryPrescribedDrugDetail(JsonObject caseSheet, Long
	// prescriptionID,
	// CommonUtilityClass commonUtilityClass) {
	// Long prescribedDrugSuccessFlag = null;
	// ArrayList<PrescribedDrugDetail> prescriptionDetails = PrescribedDrugDetail
	// .getBenPrescribedDrugDetailList(caseSheet, prescriptionID,
	// commonUtilityClass);
	//
	// /*
	// * List<PrescribedDrugDetail> prescribedDrugs = (List<PrescribedDrugDetail>)
	// * prescribedDrugDetailRepo .save(prescriptionDetails);
	// *
	// * if (null != prescribedDrugs && prescribedDrugs.size() > 0) { for
	// * (PrescribedDrugDetail prescribedDrug : prescribedDrugs) { return
	// * prescribedDrug.getPrescribedDrugID(); } }
	// */
	//
	// Integer r =
	// commonNurseServiceImpl.saveBenPrescribedDrugsList(prescriptionDetails);
	// if (r > 0 && r != null) {
	// prescribedDrugSuccessFlag = new Long(r);
	// }
	//
	// return prescribedDrugSuccessFlag;
	// }

	@Override
	public Long saveBeneficiaryExternalLabTestOrderDetails(JsonObject caseSheet) {

		ExternalLabTestOrder externalLabTestOrder = ExternalLabTestOrder.getExternalLabTestOrderList(caseSheet);
		ExternalLabTestOrder externalTestOrder = externalTestOrderRepo.save(externalLabTestOrder);

		if (null != externalTestOrder && externalTestOrder.getExternalTestOrderID() > 0) {
			return externalTestOrder.getExternalTestOrderID();
		}
		return null;
	}

	@Override
	public Integer quickConsultNurseDataInsert(JsonObject jsnOBJ) throws Exception {
		Integer returnOBJ = 0;
		if (jsnOBJ != null && jsnOBJ.has("visitDetails") && !jsnOBJ.get("visitDetails").isJsonNull()) {

			CommonUtilityClass nurseUtilityClass = InputMapper.gson().fromJson(jsnOBJ, CommonUtilityClass.class);

			BeneficiaryVisitDetail benVisitDetailsOBJ = InputMapper.gson().fromJson(jsnOBJ.get("visitDetails"),
					BeneficiaryVisitDetail.class);
			Short nurseFlag = 9;
			BeneficiaryFlowStatus data = beneficiaryFlowStatusRepo.checkExistData(nurseUtilityClass.getBenFlowID(),
					nurseFlag);
			if (data == null) {
				int i = commonNurseServiceImpl.getMaxCurrentdate(benVisitDetailsOBJ.getBeneficiaryRegID(),
						benVisitDetailsOBJ.getVisitReason(), benVisitDetailsOBJ.getVisitCategory());
				if (i < 1) {
					Long benVisitID = commonNurseServiceImpl.saveBeneficiaryVisitDetails(benVisitDetailsOBJ);

					// 11-06-2018 visit code
					Long benVisitCode = commonNurseServiceImpl.generateVisitCode(benVisitID,
							nurseUtilityClass.getVanID(), nurseUtilityClass.getSessionID());

					// Getting benflowID for ben status update
					Long benFlowID = null;
					// if (jsnOBJ.has("benFlowID")) {
					// benFlowID = jsnOBJ.get("benFlowID").getAsLong();
					// }

					// Above if block code replaced by below line
					benFlowID = nurseUtilityClass.getBenFlowID();

					if (benVisitID != null && benVisitID > 0) {
						BenAnthropometryDetail benAnthropometryDetail = InputMapper.gson()
								.fromJson(jsnOBJ.get("vitalsDetails"), BenAnthropometryDetail.class);
						benAnthropometryDetail.setBenVisitID(benVisitID);
						benAnthropometryDetail.setVisitCode(benVisitCode);
						Long benAnthropometryID = commonNurseServiceImpl
								.saveBeneficiaryPhysicalAnthropometryDetails(benAnthropometryDetail);
						BenPhysicalVitalDetail benPhysicalVitalDetail = InputMapper.gson()
								.fromJson(jsnOBJ.get("vitalsDetails"), BenPhysicalVitalDetail.class);
						benPhysicalVitalDetail.setBenVisitID(benVisitID);
						benPhysicalVitalDetail.setVisitCode(benVisitCode);
						Long benPhysicalVitalID = commonNurseServiceImpl
								.saveBeneficiaryPhysicalVitalDetails(benPhysicalVitalDetail);
						if (benAnthropometryID != null && benAnthropometryID > 0 && benPhysicalVitalID != null
								&& benPhysicalVitalID > 0) {
							// Integer i = commonNurseServiceImpl.updateBeneficiaryStatus('N',
							// benVisitDetailsOBJ.getBeneficiaryRegID());

							returnOBJ = 1;

							/**
							 * We have to write new code to update ben status flow new logic
							 */

							int j = updateBenStatusFlagAfterNurseSaveSuccess(benVisitDetailsOBJ, benVisitID, benFlowID,
									benVisitCode, nurseUtilityClass.getVanID());

						} else {

						}
					} else {
						// Error in beneficiary visit creation ...
					}
				} else {
					return new Integer(3);
				}
			}
		}
		return returnOBJ;
	}

	// method for updating ben flow status flag for nurse
	private int updateBenStatusFlagAfterNurseSaveSuccess(BeneficiaryVisitDetail benVisitDetailsOBJ, Long benVisitID,
			Long benFlowID, Long benVisitCode, Integer vanID) {
		short nurseFlag = (short) 9;
		short docFlag = (short) 1;
		short labIteration = (short) 0;

		int i = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(benFlowID,
				benVisitDetailsOBJ.getBeneficiaryRegID(), benVisitID, benVisitDetailsOBJ.getVisitReason(),
				benVisitDetailsOBJ.getVisitCategory(), nurseFlag, docFlag, labIteration, (short) 0, (short) 0,
				benVisitCode, vanID);

		return i;
	}

	@Override
	public Integer quickConsultDoctorDataInsert(JsonObject quickConsultDoctorOBJ, String Authorization)
			throws Exception {
		Integer returnOBJ = 0;
		Integer prescriptionSuccessFlag = null;
		Integer investigationSuccessFlag = null;
		Integer tcRequestStatusFlag = null;
		Integer vitalsRBSTestFlag = null;
		Long referSaveSuccessFlag = null;
		TeleconsultationRequestOBJ tcRequestOBJ = null;
		TcSpecialistSlotBookingRequestOBJ tcSpecialistSlotBookingRequestOBJ = null;
		CommonUtilityClass commonUtilityClass = InputMapper.gson().fromJson(quickConsultDoctorOBJ,
				CommonUtilityClass.class);

		if (commonUtilityClass != null && commonUtilityClass.getServiceID() != null
				&& commonUtilityClass.getServiceID() == 4 && quickConsultDoctorOBJ != null
				&& quickConsultDoctorOBJ.has("tcRequest") && quickConsultDoctorOBJ.get("tcRequest") != null) {

			tcRequestOBJ = InputMapper.gson().fromJson(quickConsultDoctorOBJ.get("tcRequest"),
					TeleconsultationRequestOBJ.class);

			// create TC request
			if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
					&& tcRequestOBJ.getAllocationDate() != null) {

				tcRequestOBJ.setAllocationDate(Utility.combineDateAndTimeToDateTime(
						tcRequestOBJ.getAllocationDate().toString(), tcRequestOBJ.getFromTime()));

				// tc request model
				TCRequestModel tRequestModel = InputMapper.gson().fromJson(quickConsultDoctorOBJ, TCRequestModel.class);
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

		Long benChiefComplaintID = saveBeneficiaryChiefComplaint(quickConsultDoctorOBJ);
		Long clinicalObservationID = saveBeneficiaryClinicalObservations(quickConsultDoctorOBJ);
		// generate prescription
		Long prescriptionID = commonNurseServiceImpl.saveBeneficiaryPrescription(quickConsultDoctorOBJ);

		Boolean isTestPrescribed = false;
		Boolean isMedicinePrescribed = false;

		JsonArray testList = quickConsultDoctorOBJ.getAsJsonArray("labTestOrders");
		if (testList != null && !testList.isJsonNull() && testList.size() > 0)
			isTestPrescribed = true;

		JsonArray drugList = quickConsultDoctorOBJ.getAsJsonArray("prescription");
		if (drugList != null && !drugList.isJsonNull() && drugList.size() > 0)
			isMedicinePrescribed = true;

		// save prescribed medicine
		if (isMedicinePrescribed) {

			PrescribedDrugDetail[] prescribedDrugDetail = InputMapper.gson()
					.fromJson(quickConsultDoctorOBJ.get("prescription"), PrescribedDrugDetail[].class);
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

		// save prescribed lab test
		if (isTestPrescribed) {
			Long i = commonNurseServiceImpl.saveBeneficiaryLabTestOrderDetails(quickConsultDoctorOBJ, prescriptionID);
			if (i != null && i > 0)
				investigationSuccessFlag = 1;
		} else {
			investigationSuccessFlag = 1;
		}

		// Updating Vitals RBS Test Result
		BenPhysicalVitalDetail physicalVitalDet = InputMapper.gson().fromJson(quickConsultDoctorOBJ,
				BenPhysicalVitalDetail.class);
		int r = 0;
		if (quickConsultDoctorOBJ.has("rbsTestResult") || quickConsultDoctorOBJ.has("rbsTestRemarks")) {

			r = benPhysicalVitalRepo.updatePhysicalVitalDetailsQCDoctor(physicalVitalDet.getRbsTestResult(),
					physicalVitalDet.getRbsTestRemarks(), physicalVitalDet.getBeneficiaryRegID(),
					physicalVitalDet.getVisitCode());
			if (r > 0) {
				vitalsRBSTestFlag = 1;
			}
		} else {
			vitalsRBSTestFlag = 1;
		}
		// save referral details
		if (quickConsultDoctorOBJ.has("refer") && !quickConsultDoctorOBJ.get("refer").isJsonNull()) {
			referSaveSuccessFlag = commonDoctorServiceImpl
					.saveBenReferDetails(quickConsultDoctorOBJ.get("refer").getAsJsonObject());
		} else {
			referSaveSuccessFlag = new Long(1);
		}
		// check if all data updated successfully
		if ((null != benChiefComplaintID && benChiefComplaintID > 0)
				&& (null != clinicalObservationID && clinicalObservationID > 0)
				&& (prescriptionID != null && prescriptionID > 0)
				&& (prescriptionSuccessFlag != null && prescriptionSuccessFlag > 0)
				&& (investigationSuccessFlag != null && investigationSuccessFlag > 0)
				&& (vitalsRBSTestFlag != null && vitalsRBSTestFlag > 0)
				&& (referSaveSuccessFlag != null && referSaveSuccessFlag > 0)) {

			// call method to update beneficiary flow table
			int i = commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(commonUtilityClass, isTestPrescribed,
					isMedicinePrescribed, tcRequestOBJ);

			if (i > 0) {
				returnOBJ = 1;
			} else
				throw new RuntimeException();
		} else {
			throw new RuntimeException();
		}
		return returnOBJ;
	}

	// ------- Start Fetch (Nurse data to Doctor screen) ----------------
	public String getBenDataFrmNurseToDocVisitDetailsScreen(Long benRegID, Long visitCode) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		BeneficiaryVisitDetail benVisitDetailsOBJ = commonNurseServiceImpl.getCSVisitDetails(benRegID, visitCode);

		if (null != benVisitDetailsOBJ) {

			resMap.put("benVisitDetails", benVisitDetailsOBJ);
		}

		return new Gson().toJson(resMap);
	}

	public String getBeneficiaryVitalDetails(Long beneficiaryRegID, Long visitCode) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("benAnthropometryDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(beneficiaryRegID, visitCode));
		resMap.put("benPhysicalVitalDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(beneficiaryRegID, visitCode));

		return resMap.toString();
	}

	public String getBenQuickConsultNurseData(Long benRegID, Long visitCode) {

		Map<String, Object> resMap = new HashMap<>();

		resMap.put("vitals", getBeneficiaryVitalDetails(benRegID, visitCode));

		return resMap.toString();
	}
	// ------- END of Fetch (Nurse data to Doctor screen) ----------------

	public String getBenCaseRecordFromDoctorQuickConsult(Long benRegID, Long visitCode) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("findings", commonDoctorServiceImpl.getFindingsDetails(benRegID, visitCode));
		resMap.put("diagnosis", generalOPDDoctorServiceImpl.getGeneralOPDDiagnosisDetails(benRegID, visitCode));
		resMap.put("investigation", commonDoctorServiceImpl.getInvestigationDetails(benRegID, visitCode));
		resMap.put("prescription", commonDoctorServiceImpl.getPrescribedDrugs(benRegID, visitCode));
		resMap.put("LabReport",
				new Gson().toJson(labTechnicianServiceImpl.getLabResultDataForBen(benRegID, visitCode)));
		resMap.put("Refer", commonDoctorServiceImpl.getReferralDetails(benRegID, visitCode));
		resMap.put("ArchivedVisitcodeForLabResult",
				labTechnicianServiceImpl.getLast_3_ArchivedTestVisitList(benRegID, visitCode));

		// resList.add(commonDoctorServiceImpl.getFindingsDetails(benRegID, visitCode));
		// resList.add(commonDoctorServiceImpl.getInvestigationDetails(benRegID,
		// visitCode));
		// resList.add(commonDoctorServiceImpl.getPrescribedDrugs(benRegID, visitCode));
		// resList.add(new
		// Gson().toJson(labTechnicianServiceImpl.getLabResultDataForBen(benRegID,
		// visitCode)));

		return resMap.toString();
	}

	@Transactional(rollbackFor = Exception.class)
	public Long updateGeneralOPDQCDoctorData(JsonObject quickConsultDoctorOBJ, String Authorization) throws Exception {
		Long updateSuccessFlag = null;
		Long prescriptionID = null;
		Long prescribedDrugSuccessFlag = null;
		Long labTestOrderSuccessFlag = null;
		Long vitalsRBSTestFlag = null;
		Long referupdateSuccessFlag = null;
		Integer tcRequestStatusFlag = null;

		TeleconsultationRequestOBJ tcRequestOBJ = null;
		TcSpecialistSlotBookingRequestOBJ tcSpecialistSlotBookingRequestOBJ = null;
		CommonUtilityClass commonUtilityClass = InputMapper.gson().fromJson(quickConsultDoctorOBJ,
				CommonUtilityClass.class);

		if (commonUtilityClass != null && commonUtilityClass.getServiceID() != null
				&& commonUtilityClass.getServiceID() == 4 && quickConsultDoctorOBJ != null
				&& quickConsultDoctorOBJ.has("tcRequest") && quickConsultDoctorOBJ.get("tcRequest") != null) {
			tcRequestOBJ = InputMapper.gson().fromJson(quickConsultDoctorOBJ.get("tcRequest"),
					TeleconsultationRequestOBJ.class);

			// create TC request
			if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
					&& tcRequestOBJ.getAllocationDate() != null) {

				tcRequestOBJ.setAllocationDate(Utility.combineDateAndTimeToDateTime(
						tcRequestOBJ.getAllocationDate().toString(), tcRequestOBJ.getFromTime()));

				// tc request model
				TCRequestModel tRequestModel = InputMapper.gson().fromJson(quickConsultDoctorOBJ, TCRequestModel.class);
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
		Long benChiefComplaintID = saveBeneficiaryChiefComplaint(quickConsultDoctorOBJ);
		Integer clinicalObservationID = updateBeneficiaryClinicalObservations(quickConsultDoctorOBJ);

		// generate prescription OBJ
		PrescriptionDetail prescriptionDetail = InputMapper.gson().fromJson(quickConsultDoctorOBJ,
				PrescriptionDetail.class);

		if (prescriptionDetail != null) {
			int p = commonNurseServiceImpl.updatePrescription(prescriptionDetail);
			prescriptionID = prescriptionDetail.getPrescriptionID();
		}

		JsonArray testList = null;
		JsonArray drugList = null;

		Boolean isTestPrescribed = false;
		Boolean isMedicinePrescribed = false;

		// checking if test is prescribed
		if (quickConsultDoctorOBJ.has("labTestOrders")) {
			testList = quickConsultDoctorOBJ.getAsJsonArray("labTestOrders");
			if (testList != null && !testList.isJsonNull() && testList.size() > 0)
				isTestPrescribed = true;
		}
		// checking if medicine is prescribed
		if (quickConsultDoctorOBJ.has("prescription") && !quickConsultDoctorOBJ.get("prescription").isJsonNull()
				&& quickConsultDoctorOBJ.get("prescription") != null) {
			drugList = quickConsultDoctorOBJ.getAsJsonArray("prescription");
			if (drugList != null && !drugList.isJsonNull() && drugList.size() > 0) {
				isMedicinePrescribed = true;
			}
		}

		// update prescribed medicine
		if (isMedicinePrescribed == true) {
			PrescribedDrugDetail[] prescribedDrugDetail = InputMapper.gson()
					.fromJson(quickConsultDoctorOBJ.get("prescription"), PrescribedDrugDetail[].class);
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
				prescribedDrugSuccessFlag = new Long(1);
			}

		} else {
			prescribedDrugSuccessFlag = new Long(1);
		}

		// update prescribed lab test
		if (isTestPrescribed == true) {
			labTestOrderSuccessFlag = commonNurseServiceImpl.saveBeneficiaryLabTestOrderDetails(quickConsultDoctorOBJ,
					prescriptionID);
		} else {
			labTestOrderSuccessFlag = new Long(1);
		}

		// Updating Vitals RBS Test Result
		BenPhysicalVitalDetail physicalVitalDet = InputMapper.gson().fromJson(quickConsultDoctorOBJ,
				BenPhysicalVitalDetail.class);
		int r = 0;
		if (quickConsultDoctorOBJ.has("rbsTestResult") || quickConsultDoctorOBJ.has("rbsTestRemarks")) {

			r = benPhysicalVitalRepo.updatePhysicalVitalDetailsQCDoctor(physicalVitalDet.getRbsTestResult(),
					physicalVitalDet.getRbsTestRemarks(), physicalVitalDet.getBeneficiaryRegID(),
					physicalVitalDet.getVisitCode());
			if (r > 0) {
				vitalsRBSTestFlag = new Long(1);
			}
		} else {
			vitalsRBSTestFlag = new Long(1);
		}
		// update referral
		if (quickConsultDoctorOBJ.has("refer") && !quickConsultDoctorOBJ.get("refer").isJsonNull()) {
			referupdateSuccessFlag = commonDoctorServiceImpl
					.updateBenReferDetails(quickConsultDoctorOBJ.get("refer").getAsJsonObject());
		} else {
			referupdateSuccessFlag = new Long(1);
		}

		if ((null != benChiefComplaintID && benChiefComplaintID > 0)
				&& (null != clinicalObservationID && clinicalObservationID > 0)
				&& (null != prescribedDrugSuccessFlag && prescribedDrugSuccessFlag > 0)
				&& (null != labTestOrderSuccessFlag && labTestOrderSuccessFlag > 0)
				&& (null != vitalsRBSTestFlag && vitalsRBSTestFlag > 0)
				&& (referupdateSuccessFlag != null && referupdateSuccessFlag > 0)) {

			// call method to update beneficiary flow table
			int i = commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(commonUtilityClass, isTestPrescribed,
					isMedicinePrescribed, tcRequestOBJ);

			if (i > 0) {
				updateSuccessFlag = benChiefComplaintID;
			} else
				throw new RuntimeException();
		} else {
			throw new RuntimeException();
		}

		return updateSuccessFlag;
	}

	@Override
	public Integer updateBeneficiaryClinicalObservations(JsonObject caseSheet) throws Exception {
		Integer r = 0;
		BenClinicalObservations benClinicalObservations = InputMapper.gson().fromJson(caseSheet,
				BenClinicalObservations.class);
		// get snomedCT code for symptoms
		if (benClinicalObservations != null) {
			String[] snomedCTcodeArr = commonDoctorServiceImpl
					.getSnomedCTcode(benClinicalObservations.getOtherSymptoms());
			if (snomedCTcodeArr != null && snomedCTcodeArr.length > 1) {
				benClinicalObservations.setOtherSymptomsSCTCode(snomedCTcodeArr[0]);
				benClinicalObservations.setOtherSymptomsSCTTerm(snomedCTcodeArr[1]);
			}
		}

		r = commonDoctorServiceImpl.updateBenClinicalObservations(benClinicalObservations);
		return r;
	}

}
