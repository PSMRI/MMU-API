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

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.anc.WrapperAncFindings;
import com.iemr.mmu.data.anc.WrapperBenInvestigationANC;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.doctor.BenReferDetails;
import com.iemr.mmu.data.masterdata.anc.ServiceMaster;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.quickConsultation.BenClinicalObservations;
import com.iemr.mmu.data.quickConsultation.LabTestOrderDetail;
import com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail;
import com.iemr.mmu.data.registrar.WrapperRegWorklist;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.data.tele_consultation.TcSpecialistSlotBookingRequestOBJ;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.doctor.BenReferDetailsRepo;
import com.iemr.mmu.repo.doctor.DocWorkListRepo;
import com.iemr.mmu.repo.quickConsultation.BenChiefComplaintRepo;
import com.iemr.mmu.repo.quickConsultation.BenClinicalObservationsRepo;
import com.iemr.mmu.repo.quickConsultation.LabTestOrderDetailRepo;
import com.iemr.mmu.repo.quickConsultation.PrescribedDrugDetailRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.snomedct.SnomedServiceImpl;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.RestTemplateUtil;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.mapper.OutputMapper;

import jakarta.servlet.http.HttpServletRequest;

/***
 * 
 * @author NE298657
 *
 */
@Service
@PropertySource("classpath:application.properties")
public class CommonDoctorServiceImpl {

	@Value("${tcSpecialistSlotBook}")
	private String tcSpecialistSlotBook;

	@Value("${docWL}")
	private Integer docWL;

	@Value("${tmReferCheckValue}")
	private String tmReferCheckValue;

	private int TMReferred = 0;

	private BenClinicalObservationsRepo benClinicalObservationsRepo;
	private BenChiefComplaintRepo benChiefComplaintRepo;
	private DocWorkListRepo docWorkListRepo;
	private BenReferDetailsRepo benReferDetailsRepo;
	private LabTestOrderDetailRepo labTestOrderDetailRepo;
	private PrescribedDrugDetailRepo prescribedDrugDetailRepo;
	private PrescriptionDetailRepo prescriptionDetailRepo;

	private SnomedServiceImpl snomedServiceImpl;

	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;

	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Autowired
	private CookieUtil cookieUtil;

	@Autowired
	public void setSnomedServiceImpl(SnomedServiceImpl snomedServiceImpl) {
		this.snomedServiceImpl = snomedServiceImpl;
	}

	@Autowired
	public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
		this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
	}

	@Autowired
	public void setBeneficiaryFlowStatusRepo(BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo) {
		this.beneficiaryFlowStatusRepo = beneficiaryFlowStatusRepo;
	}

	@Autowired
	public void setPrescriptionDetailRepo(PrescriptionDetailRepo prescriptionDetailRepo) {
		this.prescriptionDetailRepo = prescriptionDetailRepo;
	}

	@Autowired
	public void setPrescribedDrugDetailRepo(PrescribedDrugDetailRepo prescribedDrugDetailRepo) {
		this.prescribedDrugDetailRepo = prescribedDrugDetailRepo;
	}

	@Autowired
	public void setLabTestOrderDetailRepo(LabTestOrderDetailRepo labTestOrderDetailRepo) {
		this.labTestOrderDetailRepo = labTestOrderDetailRepo;
	}

	@Autowired
	public void setBenReferDetailsRepo(BenReferDetailsRepo benReferDetailsRepo) {
		this.benReferDetailsRepo = benReferDetailsRepo;
	}

	@Autowired
	public void setDocWorkListRepo(DocWorkListRepo docWorkListRepo) {
		this.docWorkListRepo = docWorkListRepo;
	}

	@Autowired
	public void setBenChiefComplaintRepo(BenChiefComplaintRepo benChiefComplaintRepo) {
		this.benChiefComplaintRepo = benChiefComplaintRepo;
	}

	@Autowired
	public void setBenClinicalObservationsRepo(BenClinicalObservationsRepo benClinicalObservationsRepo) {
		this.benClinicalObservationsRepo = benClinicalObservationsRepo;
	}

	public Integer saveFindings(JsonObject obj) throws Exception {
		int i = 0;
		BenClinicalObservations clinicalObservations = InputMapper.gson().fromJson(obj, BenClinicalObservations.class);
		BenClinicalObservations benClinicalObservationsRS = benClinicalObservationsRepo.save(clinicalObservations);
		if (benClinicalObservationsRS != null) {
			i = 1;
		}

		return i;

	}

	public Integer saveDocFindings(WrapperAncFindings wrapperAncFindings) {
		int i = 0;
		int clinicalObservationFlag = 0;
		int chiefComFlag = 0;

		// save clinical observation
		BenClinicalObservations benClinicalObservationsRS = benClinicalObservationsRepo
				.save(getBenClinicalObservations(wrapperAncFindings));
		if (benClinicalObservationsRS != null) {
			clinicalObservationFlag = 1;
		}

		// get chief complaints Object to save
		ArrayList<BenChiefComplaint> tmpBenCHiefComplaints = wrapperAncFindings.getComplaints();
		ArrayList<BenChiefComplaint> tmpBenCHiefComplaintsTMP = new ArrayList<>();
		// filter out valid chief complaints
		if (tmpBenCHiefComplaints.size() > 0) {
			for (BenChiefComplaint benChiefComplaint : tmpBenCHiefComplaints) {
				if (benChiefComplaint.getChiefComplaint() != null) {
					benChiefComplaint.setBeneficiaryRegID(wrapperAncFindings.getBeneficiaryRegID());
					benChiefComplaint.setBenVisitID(wrapperAncFindings.getBenVisitID());
					benChiefComplaint.setVisitCode(wrapperAncFindings.getVisitCode());
					benChiefComplaint.setProviderServiceMapID(wrapperAncFindings.getProviderServiceMapID());
					benChiefComplaint.setCreatedBy(wrapperAncFindings.getCreatedBy());
					benChiefComplaint.setVanID(wrapperAncFindings.getVanID());
					benChiefComplaint.setParkingPlaceID(wrapperAncFindings.getParkingPlaceID());

					tmpBenCHiefComplaintsTMP.add(benChiefComplaint);
				}
			}

		}
		// if valid chief complaints is present than save to DB
		if (tmpBenCHiefComplaintsTMP.size() > 0) {
			ArrayList<BenChiefComplaint> benChiefComplaintListRS = (ArrayList<BenChiefComplaint>) benChiefComplaintRepo
					.saveAll(tmpBenCHiefComplaintsTMP);
			if (tmpBenCHiefComplaintsTMP.size() == benChiefComplaintListRS.size()) {
				  for (BenChiefComplaint complaint : benChiefComplaintListRS) {
            if (complaint.getBenChiefComplaintID() != null) {
                benChiefComplaintRepo.updateVanSerialNo(complaint.getBenChiefComplaintID());
            }
        }
				chiefComFlag = 1;
			}
		} else {
			chiefComFlag = 1;
		}

		// check if both clinical observation & chief complaints both saved successfully
		if (clinicalObservationFlag > 0 && chiefComFlag > 0)
			i = 1;

		return i;
	}

	// get comma separated snomedCT code for give string comma seperated
	public String[] getSnomedCTcode(String requestString) {
		String[] returnARR = new String[2];
		String snomedCTidVal = "";
		String snomedCTtermVal = "";

		if (requestString != null && requestString.length() > 0) {
			String[] symptomArr = requestString.split(",");

			int pointer = 0;
			for (String s : symptomArr) {
				SCTDescription obj = snomedServiceImpl.findSnomedCTRecordFromTerm(s.trim());
				if (obj != null) {
					if (pointer == symptomArr.length - 1) {
						snomedCTidVal += obj.getConceptID();
						snomedCTtermVal += obj.getTerm();
					} else {
						snomedCTidVal += obj.getConceptID() + ",";
						snomedCTtermVal += obj.getTerm() + ",";
					}
				} else {
					if (pointer == symptomArr.length - 1) {
						snomedCTidVal += "N/A";
						snomedCTtermVal += "N/A";
					} else {
						snomedCTidVal += "N/A" + ",";
						snomedCTtermVal += "N/A" + ",";
					}

				}
				pointer++;
			}

		}
		returnARR[0] = snomedCTidVal;
		returnARR[1] = snomedCTtermVal;

		return returnARR;
	}

	private BenClinicalObservations getBenClinicalObservations(WrapperAncFindings wrapperAncFindings) {
		// snomedCT integration started on : 06-08-2018
		String symptoms = wrapperAncFindings.getOtherSymptoms();
		String[] responseString = getSnomedCTcode(symptoms);
		// end of snomedCT integration

		BenClinicalObservations benClinicalObservations = new BenClinicalObservations();
		benClinicalObservations.setBeneficiaryRegID(wrapperAncFindings.getBeneficiaryRegID());
		benClinicalObservations.setBenVisitID(wrapperAncFindings.getBenVisitID());
		benClinicalObservations.setVisitCode(wrapperAncFindings.getVisitCode());
		benClinicalObservations.setProviderServiceMapID(wrapperAncFindings.getProviderServiceMapID());
		benClinicalObservations.setVanID(wrapperAncFindings.getVanID());
		benClinicalObservations.setParkingPlaceID(wrapperAncFindings.getParkingPlaceID());
		benClinicalObservations.setCreatedBy(wrapperAncFindings.getCreatedBy());
		benClinicalObservations.setClinicalObservation(wrapperAncFindings.getClinicalObservation());
		benClinicalObservations.setOtherSymptoms(wrapperAncFindings.getOtherSymptoms());
		benClinicalObservations.setSignificantFindings(wrapperAncFindings.getSignificantFindings());
		benClinicalObservations.setIsForHistory(wrapperAncFindings.getIsForHistory());
		benClinicalObservations.setModifiedBy(wrapperAncFindings.getModifiedBy());
		if (responseString != null && responseString.length > 1) {
			benClinicalObservations.setOtherSymptomsSCTCode(responseString[0]);
			benClinicalObservations.setOtherSymptomsSCTTerm(responseString[1]);
		}
		return benClinicalObservations;
	}

	private ArrayList<BenChiefComplaint> getBenChiefComplaint(WrapperAncFindings wrapperAncFindings) {
		ArrayList<BenChiefComplaint> benChiefComplaintList = new ArrayList<>();
		BenChiefComplaint benChiefComplaint;
		if (wrapperAncFindings != null && wrapperAncFindings.getComplaints() != null
				&& wrapperAncFindings.getComplaints().size() > 0) {
			for (BenChiefComplaint complaintsDetails : wrapperAncFindings.getComplaints()) {
				benChiefComplaint = new BenChiefComplaint();
				benChiefComplaint.setBeneficiaryRegID(wrapperAncFindings.getBeneficiaryRegID());
				benChiefComplaint.setBenVisitID(wrapperAncFindings.getBenVisitID());
				benChiefComplaint.setProviderServiceMapID(wrapperAncFindings.getProviderServiceMapID());
				benChiefComplaint.setCreatedBy(wrapperAncFindings.getCreatedBy());

				if (null != complaintsDetails.getChiefComplaintID()) {
					/*
					 * Double d = (Double) complaintsDetails.getChiefComplaintID(); if (d == null)
					 * continue;
					 */
					benChiefComplaint.setChiefComplaintID(complaintsDetails.getChiefComplaintID());
				}
				if (null != complaintsDetails.getChiefComplaint())
					benChiefComplaint.setChiefComplaint(complaintsDetails.getChiefComplaint());
				if (null != complaintsDetails.getDuration())
					benChiefComplaint.setDuration(complaintsDetails.getDuration());
				if (null != complaintsDetails.getUnitOfDuration())
					benChiefComplaint.setUnitOfDuration(complaintsDetails.getUnitOfDuration());
				if (null != complaintsDetails.getDescription())
					benChiefComplaint.setDescription(complaintsDetails.getDescription());

				benChiefComplaintList.add(benChiefComplaint);
			}
		}
		return benChiefComplaintList;
	}

	public String getDocWorkList() {
		List<Object[]> docWorkListData = docWorkListRepo.getDocWorkList();
		return WrapperRegWorklist.getDocWorkListData(docWorkListData);
	}

	// New doc work-list service
	public String getDocWorkListNew(Integer providerServiceMapId, Integer serviceID, Integer vanID) {

		Calendar cal = Calendar.getInstance();
		if (docWL != null && docWL > 0 && docWL <= 30)
			cal.add(Calendar.DAY_OF_YEAR, -docWL);
		else
			cal.add(Calendar.DAY_OF_YEAR, -7);
		long sevenDaysAgo = cal.getTimeInMillis();

		ArrayList<BeneficiaryFlowStatus> docWorkList = new ArrayList<>();
		// MMU doc work-list
		if (serviceID != null && serviceID == 2) {
			docWorkList = beneficiaryFlowStatusRepo.getDocWorkListNew(providerServiceMapId, new Timestamp(sevenDaysAgo),
					vanID);
		}
		// TC doc work-list
		else if (serviceID != null && serviceID == 4) {
			docWorkList = beneficiaryFlowStatusRepo.getDocWorkListNewTC(providerServiceMapId);
		}

		return new Gson().toJson(docWorkList);
	}

	// New doc work-list service (Future scheduled beneficiary for TM)
	public String getDocWorkListNewFutureScheduledForTM(Integer providerServiceMapId, Integer serviceID) {

		ArrayList<BeneficiaryFlowStatus> docWorkListFutureScheduled = new ArrayList<>();
		if (serviceID != null && serviceID == 4) {
			docWorkListFutureScheduled = beneficiaryFlowStatusRepo
					.getDocWorkListNewFutureScheduledTC(providerServiceMapId);
		}
		return new Gson().toJson(docWorkListFutureScheduled);
	}

	// New TC specialist work-list service
	public String getTCSpecialistWorkListNewForTM(Integer providerServiceMapId, Integer userID, Integer serviceID) {

		ArrayList<BeneficiaryFlowStatus> tcSpecialistWorkList = new ArrayList<>();
		if (serviceID != null && serviceID == 4) {
			tcSpecialistWorkList = beneficiaryFlowStatusRepo.getTCSpecialistWorkListNew(providerServiceMapId, userID);
		}
		return new Gson().toJson(tcSpecialistWorkList);
	}

	// New TC specialist work-list service (Future scheduled beneficiary for TM)
	public String getTCSpecialistWorkListNewFutureScheduledForTM(Integer providerServiceMapId, Integer userID,
			Integer serviceID) {

		ArrayList<BeneficiaryFlowStatus> tcSpecialistWorkListFutureScheduled = new ArrayList<>();
		if (serviceID != null && serviceID == 4) {
			tcSpecialistWorkListFutureScheduled = beneficiaryFlowStatusRepo
					.getTCSpecialistWorkListNewFutureScheduled(providerServiceMapId, userID);
		}
		return new Gson().toJson(tcSpecialistWorkListFutureScheduled);
	}

	public String fetchBenPreviousSignificantFindings(Long beneficiaryRegID) {
		ArrayList<Object[]> previousSignificantFindings = (ArrayList<Object[]>) benClinicalObservationsRepo
				.getPreviousSignificantFindings(beneficiaryRegID);

		Map<String, Object> response = new HashMap<String, Object>();

		ArrayList<BenClinicalObservations> clinicalObservations = new ArrayList<BenClinicalObservations>();
		if (null != clinicalObservations) {
			for (Object[] obj : previousSignificantFindings) {
				BenClinicalObservations clinicalObservation = new BenClinicalObservations((String) obj[1],
						(Date) obj[0]);
				clinicalObservations.add(clinicalObservation);
			}
		}

		response.put("findings", clinicalObservations);
		return new Gson().toJson(response);

	}

	public Long saveBenReferDetails(JsonObject obj) throws IEMRException {
		Long ID = null;
		BenReferDetails referDetails = InputMapper.gson().fromJson(obj, BenReferDetails.class);
		List<BenReferDetails> referDetailsList = new ArrayList<BenReferDetails>();

		BenReferDetails referDetailsTemp = null;

		if (referDetails.getRefrredToAdditionalServiceList() != null
				&& referDetails.getRefrredToAdditionalServiceList().size() > 0) {
			for (ServiceMaster sm : referDetails.getRefrredToAdditionalServiceList()) {
				if (sm.getServiceName() != null) {
					referDetailsTemp = new BenReferDetails();
					referDetailsTemp.setBeneficiaryRegID(referDetails.getBeneficiaryRegID());
					referDetailsTemp.setBenVisitID(referDetails.getBenVisitID());
					referDetailsTemp.setVisitCode(referDetails.getVisitCode());
					referDetailsTemp.setProviderServiceMapID(referDetails.getProviderServiceMapID());
					referDetailsTemp.setVisitCode(referDetails.getVisitCode());
					referDetailsTemp.setCreatedBy(referDetails.getCreatedBy());
					referDetailsTemp.setVanID(referDetails.getVanID());
					referDetailsTemp.setParkingPlaceID(referDetails.getParkingPlaceID());

					referDetailsTemp.setServiceID(sm.getServiceID());
					referDetailsTemp.setServiceName(sm.getServiceName());

					if (sm.getServiceName().equalsIgnoreCase(tmReferCheckValue)) {
						TMReferred = 1;
					}
					else
					{
						TMReferred = 0;
					}

					if (referDetails.getReferredToInstituteID() != null
							&& referDetails.getReferredToInstituteName() != null) {
						referDetailsTemp.setReferredToInstituteID(referDetails.getReferredToInstituteID());
						referDetailsTemp.setReferredToInstituteName(referDetails.getReferredToInstituteName());
					}
					if (referDetails.getRevisitDate() != null)
						referDetailsTemp.setRevisitDate(referDetails.getRevisitDate());
					referDetailsList.add(referDetailsTemp);

					if (referDetails.getReferralReason() != null)
						referDetailsTemp.setReferralReason(referDetails.getReferralReason());
				}
			}
		} else {
			if (referDetails.getReferredToInstituteName() != null || referDetails.getRevisitDate() != null
					|| referDetails.getReferralReason() != null)
				referDetailsList.add(referDetails);
			TMReferred = 0;
		}

		ArrayList<BenReferDetails> res = (ArrayList<BenReferDetails>) benReferDetailsRepo.saveAll(referDetailsList);
		if (referDetailsList.size() == res.size()) {
			ID = new Long(1);
		}
		return ID;
	}
	public Long saveBenReferDetailsTMreferred(JsonObject obj1) throws IEMRException {
		Long ID = null;
		int delRes = 0;
		BenReferDetails referDetails = InputMapper.gson().fromJson(obj1, BenReferDetails.class);
		List<BenReferDetails> referDetailsList = new ArrayList<BenReferDetails>();

		BenReferDetails referDetailsTemp = null;
        int u=benReferDetailsRepo.updateReferredInstituteNameTMReferred(referDetails.getReferredToInstituteID(),
				referDetails.getReferredToInstituteName(), referDetails.getVisitCode(),
				"U");
//		ArrayList<Object[]> benReferDetailsStatuses = benReferDetailsRepo
//				.getBenReferDetailsStatus(referDetails.getBeneficiaryRegID(), referDetails.getVisitCode());
//
//		for (Object[] obj : benReferDetailsStatuses) {
//			String processed = (String) obj[1];
//			if (null != processed && !"N".equals(processed)) {
//				processed = "U";
//			} else {
//				processed = "N";
//			}
//			if (referDetails.getReferredToInstituteID() != null || referDetails.getReferredToInstituteName() != null
//					) {
//				benReferDetailsRepo.updateReferredInstituteNameTMReferred(referDetails.getReferredToInstituteID(),
//						referDetails.getReferredToInstituteName(), referDetails.getVisitCode(),
//						processed);
//			}
//		}

		if (referDetails.getRefrredToAdditionalServiceList() != null
				&& referDetails.getRefrredToAdditionalServiceList().size() > 0) {
			for (ServiceMaster sm : referDetails.getRefrredToAdditionalServiceList()) {
				if (sm.getServiceName() != null) {
					referDetailsTemp = new BenReferDetails();
					referDetailsTemp.setBeneficiaryRegID(referDetails.getBeneficiaryRegID());
					referDetailsTemp.setBenVisitID(referDetails.getBenVisitID());
					referDetailsTemp.setProviderServiceMapID(referDetails.getProviderServiceMapID());
					referDetailsTemp.setVisitCode(referDetails.getVisitCode());
					referDetailsTemp.setCreatedBy(referDetails.getCreatedBy());
					if (referDetails.getReferredToInstituteID() != null
							&& referDetails.getReferredToInstituteName() != null) {
						referDetailsTemp.setReferredToInstituteID(referDetails.getReferredToInstituteID());
						referDetailsTemp.setReferredToInstituteName(referDetails.getReferredToInstituteName());
					}
					referDetailsTemp.setServiceID(sm.getServiceID());
					referDetailsTemp.setServiceName(sm.getServiceName());

					if (referDetails.getRevisitDate() != null)
						referDetailsTemp.setRevisitDate(referDetails.getRevisitDate());

					referDetailsList.add(referDetailsTemp);
				}
			}
		} else {
			if (referDetails.getReferredToInstituteName() != null || referDetails.getRevisitDate() != null)
				referDetailsList.add(referDetails);
		}
        if(u>0)
        	ID=Long.valueOf(u);
        else
        {
		ArrayList<BenReferDetails> res = (ArrayList<BenReferDetails>) benReferDetailsRepo.saveAll(referDetailsList);
		if (referDetailsList.size() == res.size()) {
			ID = new Long(1);
		}
        }
		return ID;

	}
	public String getFindingsDetails(Long beneficiaryRegID, Long visitCode) {
		ArrayList<Object[]> clinicalObservationsList = benClinicalObservationsRepo.getFindingsData(beneficiaryRegID,
				visitCode);
		ArrayList<Object[]> chiefComplaintsList = benChiefComplaintRepo.getBenChiefComplaints(beneficiaryRegID,
				visitCode);

		WrapperAncFindings findings = WrapperAncFindings.getFindingsData(clinicalObservationsList, chiefComplaintsList);
		return new Gson().toJson(findings);
	}

	public String getInvestigationDetails(Long beneficiaryRegID, Long visitCode) {
		ArrayList<Object[]> labTestOrders = labTestOrderDetailRepo.getLabTestOrderDetails(beneficiaryRegID, visitCode);
		WrapperBenInvestigationANC labTestOrdersList = LabTestOrderDetail.getLabTestOrderDetails(labTestOrders);

		return new Gson().toJson(labTestOrdersList);
	}

	public String getPrescribedDrugs(Long beneficiaryRegID, Long visitCode) {
		// ArrayList<Object[]> prescriptions =
		// prescriptionDetailRepo.getBenPrescription(beneficiaryRegID, visitCode);
		//
		// PrescriptionDetail prescriptionData =
		// PrescriptionDetail.getPrescriptions(prescriptions);
		// if (null != prescriptionData) {
		ArrayList<Object[]> resList = prescribedDrugDetailRepo.getBenPrescribedDrugDetails(beneficiaryRegID, visitCode);

		ArrayList<PrescribedDrugDetail> prescribedDrugs = PrescribedDrugDetail.getprescribedDrugs(resList);
		// prescriptionData.setPrescribedDrugs(prescribedDrugs);
		// }

		return new Gson().toJson(prescribedDrugs);
	}

	/*
	 * public PrescriptionDetail getLatestPrescription(Long beneficiaryRegID, Long
	 * benVisitID) { ArrayList<Object[]> prescriptions =
	 * prescriptionDetailRepo.getBenPrescription(beneficiaryRegID, benVisitID);
	 * 
	 * PrescriptionDetail prescriptionData =
	 * PrescriptionDetail.getPrescriptions(prescriptions); return prescriptionData;
	 * }
	 */

	public String getReferralDetails(Long beneficiaryRegID, Long visitCode) {
		ArrayList<Object[]> resList = benReferDetailsRepo.getBenReferDetails(beneficiaryRegID, visitCode);

		BenReferDetails referDetails = BenReferDetails.getBenReferDetails(resList);

		return new Gson().toJson(referDetails);
	}

	public Integer updateDocFindings(WrapperAncFindings wrapperAncFindings) {
		int clinObsrvtnsRes = 0;
		int chiefCmpltsRes = 0;
		int updateFindingsRes = 0;

		BenClinicalObservations benClinicalObservations = getBenClinicalObservations(wrapperAncFindings);
		clinObsrvtnsRes = updateBenClinicalObservations(benClinicalObservations);

		ArrayList<BenChiefComplaint> tmpBenCHiefComplaints = wrapperAncFindings.getComplaints();
		ArrayList<BenChiefComplaint> tmpBenCHiefComplaintsTMP = new ArrayList<>();
		if (tmpBenCHiefComplaints.size() > 0) {
			for (BenChiefComplaint benChiefComplaint : tmpBenCHiefComplaints) {
				if (benChiefComplaint.getChiefComplaint() != null) {
					benChiefComplaint.setBeneficiaryRegID(wrapperAncFindings.getBeneficiaryRegID());
					benChiefComplaint.setBenVisitID(wrapperAncFindings.getBenVisitID());
					benChiefComplaint.setVisitCode(wrapperAncFindings.getVisitCode());
					benChiefComplaint.setProviderServiceMapID(wrapperAncFindings.getProviderServiceMapID());
					benChiefComplaint.setCreatedBy(wrapperAncFindings.getCreatedBy());

					tmpBenCHiefComplaintsTMP.add(benChiefComplaint);
				}
			}
			chiefCmpltsRes = updateDoctorBenChiefComplaints(tmpBenCHiefComplaintsTMP);

		} else {
			chiefCmpltsRes = 1;
		}
		if (clinObsrvtnsRes > 0 && chiefCmpltsRes > 0) {
			updateFindingsRes = 1;

		}
		return updateFindingsRes;
	}

	public int updateDoctorBenChiefComplaints(List<BenChiefComplaint> benChiefComplaintList) {
		int r = 0;
		if (null != benChiefComplaintList && benChiefComplaintList.size() > 0) {

			List<BenChiefComplaint> benChiefComplaintResultList = (List<BenChiefComplaint>) benChiefComplaintRepo
					.saveAll(benChiefComplaintList);

			if (!benChiefComplaintResultList.isEmpty()) {
				r = benChiefComplaintResultList.size();
			}
		} else {
			r = 1;
		}
		return r;
	}

	public int updateBenClinicalObservations(BenClinicalObservations benClinicalObservations) {
		Integer r = 0;
		int recordsAvailable = 0;
		if (null != benClinicalObservations) {
			String processed = benClinicalObservationsRepo.getBenClinicalObservationStatus(
					benClinicalObservations.getBeneficiaryRegID(), benClinicalObservations.getVisitCode());

			if (null != processed) {
				recordsAvailable = 1;
			}

			if (null != processed && !processed.equals("N")) {
				processed = "U";
			} else {
				processed = "N";
			}

			if (recordsAvailable > 0) {
				r = benClinicalObservationsRepo.updateBenClinicalObservations(
						benClinicalObservations.getClinicalObservation(), benClinicalObservations.getOtherSymptoms(),
						benClinicalObservations.getOtherSymptomsSCTCode(),
						benClinicalObservations.getOtherSymptomsSCTTerm(),
						benClinicalObservations.getSignificantFindings(), benClinicalObservations.getIsForHistory(),
						benClinicalObservations.getCreatedBy(), processed,
						benClinicalObservations.getBeneficiaryRegID(), benClinicalObservations.getVisitCode());
			} else {
				BenClinicalObservations observationsRes = benClinicalObservationsRepo.save(benClinicalObservations);
				if (null != observationsRes && observationsRes.getClinicalObservationID() > 0) {
					r = 1;
				}
			}
		}
		return r;
	}

	public Long updateBenReferDetails(JsonObject referObj) throws IEMRException {
		Long ID = null;
		int delRes = 0;
		BenReferDetails referDetails = InputMapper.gson().fromJson(referObj, BenReferDetails.class);
		List<BenReferDetails> referDetailsList = new ArrayList<BenReferDetails>();

		BenReferDetails referDetailsTemp = null;
		List<String> serviceNamesList = new ArrayList<>();

		ArrayList<Object[]> benReferDetailsStatuses = benReferDetailsRepo
				.getBenReferDetailsStatus(referDetails.getBeneficiaryRegID(), referDetails.getVisitCode());

		for (Object[] obj : benReferDetailsStatuses) {
			serviceNamesList.add((String) obj[2]);
			String processed = (String) obj[1];
			if (null != processed && !"N".equals(processed)) {
				processed = "U";
			} else {
				processed = "N";
			}
			if (referDetails.getReferredToInstituteID() != null || referDetails.getReferredToInstituteName() != null
					|| referDetails.getRevisitDate() != null || referDetails.getReferralReason() != null) {
				benReferDetailsRepo.updateReferredInstituteName(referDetails.getReferredToInstituteID(),
						referDetails.getReferredToInstituteName(), referDetails.getRevisitDate(), referDetails.getReferralReason(), (Long) obj[0],
						processed);
			}
		}

		if (referDetails.getRefrredToAdditionalServiceList() != null
				&& referDetails.getRefrredToAdditionalServiceList().size() > 0) {
			for (ServiceMaster sm : referDetails.getRefrredToAdditionalServiceList()) {
				if (sm.getServiceName() != null && !serviceNamesList.contains(sm.getServiceName())) {
					referDetailsTemp = new BenReferDetails();
					referDetailsTemp.setBeneficiaryRegID(referDetails.getBeneficiaryRegID());
					referDetailsTemp.setBenVisitID(referDetails.getBenVisitID());
					referDetailsTemp.setProviderServiceMapID(referDetails.getProviderServiceMapID());
					referDetailsTemp.setVisitCode(referDetails.getVisitCode());
					referDetailsTemp.setCreatedBy(referDetails.getCreatedBy());
					referDetailsTemp.setVanID(referDetails.getVanID());
					referDetailsTemp.setParkingPlaceID(referDetails.getParkingPlaceID());
					if (referDetails.getReferredToInstituteID() != null
							&& referDetails.getReferredToInstituteName() != null) {
						referDetailsTemp.setReferredToInstituteID(referDetails.getReferredToInstituteID());
						referDetailsTemp.setReferredToInstituteName(referDetails.getReferredToInstituteName());
					}
					referDetailsTemp.setServiceID(sm.getServiceID());
					referDetailsTemp.setServiceName(sm.getServiceName());

					if (sm.getServiceName().equalsIgnoreCase(tmReferCheckValue)) {
						TMReferred = 1;
					}
					else
					{
						TMReferred = 0;
					}
					
					if (referDetails.getRevisitDate() != null)
						referDetailsTemp.setRevisitDate(referDetails.getRevisitDate());
					
					if (referDetails.getReferralReason() != null)
						referDetailsTemp.setReferralReason(referDetails.getReferralReason());

					referDetailsList.add(referDetailsTemp);
				}
			}
		} /*
			 * else { if (referDetails.getReferredToInstituteName() != null ||
			 * referDetails.getRevisitDate() != null || referDetails.getReferralReason() !=
			 * null) referDetailsList.add(referDetails); TMReferred = 0; }
			 */

		ArrayList<BenReferDetails> res = (ArrayList<BenReferDetails>) benReferDetailsRepo.saveAll(referDetailsList);
		if (referDetailsList.size() == res.size()) {
			ID = new Long(1);
		}
		return ID;
	}

	/**
	 * 
	 * 
	 * @param commonUtilityClass
	 * @param testList
	 * @param drugList
	 * @return
	 */
	/// ------Start of beneficiary flow table after doctor data save-------------

	public int updateBenFlowtableAfterDocDataSave(CommonUtilityClass commonUtilityClass,
			Boolean isTestPrescribed, Boolean isMedicinePrescribed,
			TeleconsultationRequestOBJ tcRequestOBJ, Boolean doctorSignatureFlag) throws Exception {

		short pharmaFalg;
		short docFlag;
		short tcSpecialistFlag = (short) 0;
		int tcUserID = 0;
		Timestamp tcDate = null;

		Long tmpBenFlowID = commonUtilityClass.getBenFlowID();
		Long tmpBeneficiaryID = commonUtilityClass.getBeneficiaryID();
		Long tmpBenVisitID = commonUtilityClass.getBenVisitID();
		Long tmpbeneficiaryRegID = commonUtilityClass.getBeneficiaryRegID();

		// checking if test is prescribed
		if (isTestPrescribed) {
			docFlag = (short) 2;
		} else {
			docFlag = (short) 9;
		}
		// checking if medicine is prescribed
		if (isMedicinePrescribed) {
			pharmaFalg = (short) 1;
		} else {
			pharmaFalg = (short) 0;
		}

		if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
				&& tcRequestOBJ.getAllocationDate() != null) {
			tcSpecialistFlag = (short) 1;
			tcUserID = tcRequestOBJ.getUserID();
			tcDate = tcRequestOBJ.getAllocationDate();

		}

		if (TMReferred == 1) {
			
			BeneficiaryFlowStatus specialistValue = beneficiaryFlowStatusRepo.specialistFlagAndCategoryValue(commonUtilityClass.getVisitCode());
			
			if(specialistValue != null && specialistValue.getVisitCategory() != null && specialistValue.getVisitCategory().equalsIgnoreCase("NCD screening"))
				tcSpecialistFlag = (short) 100;
		}

		int i = commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(tmpBenFlowID, tmpbeneficiaryRegID,
				tmpBeneficiaryID, tmpBenVisitID, docFlag, pharmaFalg, (short) 0, tcSpecialistFlag,
				tcUserID, tcDate, doctorSignatureFlag);
		return i;
	}

	/// ------End of beneficiary flow table after doctor data save-------------

	/// ------Start of beneficiary flow table after doctor data update-------------
	/**
	 * 
	 * 
	 * @param commonUtilityClass
	 * @param isTestPrescribed
	 * @param isMedicinePrescribed
	 * @return
	 */
	public int updateBenFlowtableAfterDocDataUpdate(CommonUtilityClass commonUtilityClass, Boolean isTestPrescribed,
			Boolean isMedicinePrescribed, TeleconsultationRequestOBJ tcRequestOBJ) throws Exception {
		int i = 0;
		short pharmaFalg;
		short docFlag = (short) 0;
		short tcSpecialistFlag = (short) 0;
		int tcUserID = 0;
		Timestamp tcDate = null;

		Long tmpBenFlowID = commonUtilityClass.getBenFlowID();
		Long tmpBeneficiaryID = commonUtilityClass.getBeneficiaryID();
		Long tmpBenVisitID = commonUtilityClass.getBenVisitID();
		Long tmpbeneficiaryRegID = commonUtilityClass.getBeneficiaryRegID();

		if (commonUtilityClass.getIsSpecialist() != null && commonUtilityClass.getIsSpecialist() == true) {
			if (isTestPrescribed)
				tcSpecialistFlag = (short) 2;
			else
				tcSpecialistFlag = (short) 9;

			if (isMedicinePrescribed)
				pharmaFalg = (short) 1;
			else
				pharmaFalg = (short) 0;

			i = commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdateTCSpecialist(tmpBenFlowID,
					tmpbeneficiaryRegID, tmpBeneficiaryID, tmpBenVisitID, docFlag, pharmaFalg, (short) 0,
					tcSpecialistFlag, tcUserID, tcDate);
		} else {

			if (isTestPrescribed)
				docFlag = (short) 2;
			else
				docFlag = (short) 9;

			if (isMedicinePrescribed)
				pharmaFalg = (short) 1;
			else
				pharmaFalg = (short) 0;

			if (tcRequestOBJ != null && tcRequestOBJ.getUserID() != null && tcRequestOBJ.getUserID() > 0
					&& tcRequestOBJ.getAllocationDate() != null) {
				tcSpecialistFlag = (short) 1;
				tcUserID = tcRequestOBJ.getUserID();
				tcDate = tcRequestOBJ.getAllocationDate();
			}

			if (TMReferred == 1) {
				
				BeneficiaryFlowStatus specialistValue = beneficiaryFlowStatusRepo.specialistFlagAndCategoryValue(commonUtilityClass.getVisitCode());
				
				if(specialistValue != null && specialistValue.getVisitCategory() != null && specialistValue.getVisitCategory().equalsIgnoreCase("NCD screening"))
					tcSpecialistFlag = (short) 100;
				i = commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdate(tmpBenFlowID, tmpbeneficiaryRegID,
						tmpBeneficiaryID, tmpBenVisitID, docFlag, pharmaFalg, (short) 0, tcSpecialistFlag, tcUserID,
						tcDate);
			}
			
			else
				i = commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdateWDF(tmpBenFlowID, tmpbeneficiaryRegID,
						tmpBeneficiaryID, tmpBenVisitID, docFlag, pharmaFalg, (short) 0, tcUserID,
						tcDate);

		}

		return i;
	}

	/// ------End of beneficiary flow table after doctor data update-------------

	public String deletePrescribedMedicine(JSONObject obj) {
		int i = 0;
		if (obj != null && obj.has("id")) {
			i = prescribedDrugDetailRepo.deletePrescribedmedicine(obj.getLong("id"));
		} else {

		}
		if (i > 0)
			return "record deleted successfully";
		else
			return null;
	}

	public int callTmForSpecialistSlotBook(TcSpecialistSlotBookingRequestOBJ tcSpecialistSlotBookingRequestOBJ,
			String Authorization) {
		int successFlag = 0;
		// OutputMapper outputMapper = new OutputMapper();
		String requestOBJ = OutputMapper.gson().toJson(tcSpecialistSlotBookingRequestOBJ);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(requestOBJ, Authorization,"");
		ResponseEntity<String> response = restTemplate.exchange(tcSpecialistSlotBook, HttpMethod.POST, request,
				String.class);
		// System.out.println(response.getBody());

		if (response.getStatusCodeValue() == 200 && response.hasBody()) {
			JsonObject jsnOBJ = new JsonObject();
			JsonParser jsnParser = new JsonParser();
			JsonElement jsnElmnt = jsnParser.parse(response.getBody());
			jsnOBJ = jsnElmnt.getAsJsonObject();
			if (jsnOBJ.has("statusCode") && jsnOBJ.get("statusCode").getAsInt() == 200)
				successFlag = 1;
		}
		return successFlag;
	}

	

}
