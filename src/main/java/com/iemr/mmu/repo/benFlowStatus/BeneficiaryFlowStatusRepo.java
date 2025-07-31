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
package com.iemr.mmu.repo.benFlowStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;

/***
 * 
 * @author NE298657
 *
 */
@Repository
public interface BeneficiaryFlowStatusRepo extends CrudRepository<BeneficiaryFlowStatus, Long> {

	@Query("SELECT  t from BeneficiaryFlowStatus t WHERE (t.nurseFlag = 1 OR t.nurseFlag = 100) AND t.deleted = false "
			+ " AND Date(t.visitDate)  >= Date(:fromDate) AND t.providerServiceMapId = :providerServiceMapId "
			+ " AND t.vanID = :vanID ORDER BY t.visitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getNurseWorklistNew(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("vanID") Integer vanID,@Param("fromDate") Timestamp fromDate);
	
	

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.benVisitID = :benVisitID, t.VisitReason = :visitReason, "
			+ " t.VisitCategory = :visitCategory, t.nurseFlag = :nurseFlag, t.doctorFlag = :docFlag, "
			+ " t.labIteration = :labIteration, t.lab_technician_flag = 0, t.radiologist_flag = :radiologistFlag, "
			+ " t.oncologist_flag = :oncologistFlag, t.benVisitDate = now(), "
			+ " t.visitCode = :benVisitCode, t.processed = 'U', t.vanID =:vanID "
			+ "  WHERE t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID " + " AND nurseFlag = 1  ")
	public int updateBenFlowStatusAfterNurseActivity(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("benVisitID") Long benVisitID,
			@Param("visitReason") String visitReason, @Param("visitCategory") String visitCategory,
			@Param("nurseFlag") Short nurseFlag, @Param("docFlag") Short docFlag,
			@Param("labIteration") Short labIteration, @Param("radiologistFlag") Short radiologistFlag,
			@Param("oncologistFlag") Short oncologistFlag, @Param("benVisitCode") Long benVisitCode,
			@Param("vanID") Integer vanID);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.specialist_flag = :specialistFlag , t.pharmacist_flag = :pharmacistflag "
			+ "  WHERE t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID  ")
	public int updateBenFlowStatusTMReferred(@Param("benFlowID") Long benFlowID, @Param("benRegID") Long benRegID,
			@Param("specialistFlag") Short specialistFlag, @Param("pharmacistflag") Short pharmacistflag);

	// @Query("SELECT  t.benFlowID, t.beneficiaryRegID, t.visitDate, t.benName, t.age, t.ben_age_val, t.genderID, t.genderName, "
	// 		+ " t.villageName, t.districtName, t.beneficiaryID, t.servicePointName, t.VisitReason, t.VisitCategory, t.benVisitID,  "
	// 		+ " t.registrationDate, t.benVisitDate, t.visitCode, t.consultationDate, t.fatherName, t.preferredPhoneNum FROM BeneficiaryFlowStatus t "
	// 		+ " Where t.beneficiaryRegID = :benRegID AND t.benFlowID = :benFlowID ")
	// public ArrayList<Object[]> getBenDetailsForLeftSidePanel(@Param("benRegID") Long benRegID,
	// 		@Param("benFlowID") Long benFlowID);
	
	//for testing
	@Query("SELECT t FROM BeneficiaryFlowStatus t Where t.benFlowID = :benFlowID ")
	public BeneficiaryFlowStatus getBenDetailsForLeftSidePanel(@Param("benFlowID") Long benFlowID);

	// MMU doc work-list
	@Query("SELECT t from BeneficiaryFlowStatus t WHERE (t.doctorFlag = 1 OR t.doctorFlag = 2 OR "
			+ " t.doctorFlag = 3 OR t.nurseFlag = 2 OR t.doctorFlag = 9) AND t.vanID = :vanID "
			+ " AND t.benVisitDate >= Date(:fromDate) AND t.deleted = false "
			+ " AND t.providerServiceMapId = :providerServiceMapId " + " ORDER BY benVisitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getDocWorkListNew(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("fromDate") Timestamp fromDate,
			@Param("vanID") Integer vanID);

	// TC doc work-list, 04-12-2018
	@Query("SELECT t from BeneficiaryFlowStatus t WHERE (t.doctorFlag = 1 OR t.doctorFlag = 2 OR "
			+ " t.doctorFlag = 3 OR t.nurseFlag = 2 OR t.doctorFlag = 9 ) "
			+ " AND (t.tCRequestDate is null OR DATE(t.tCRequestDate) <= curdate() ) "
			+ " AND t.deleted = false AND t.providerServiceMapId = :providerServiceMapId "
			+ " ORDER BY t.benVisitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getDocWorkListNewTC(
			@Param("providerServiceMapId") Integer providerServiceMapId);

	// TC doc work-list, future scheduled 13-12-2018
	@Query("SELECT t from BeneficiaryFlowStatus t "
			+ " WHERE t.specialist_flag = 1 AND t.tCRequestDate is not null AND t.tCSpecialistUserID is not null "
			+ " AND DATE(t.tCRequestDate) > curdate() "
			+ " AND t.deleted = false AND t.providerServiceMapId = :providerServiceMapId "
			+ " ORDER BY benVisitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getDocWorkListNewFutureScheduledTC(
			@Param("providerServiceMapId") Integer providerServiceMapId);

	// TC Specialist work-list, 13-12-2018
	@Query("SELECT t from BeneficiaryFlowStatus t "
			+ " WHERE t.specialist_flag NOT IN (0,4) AND t.tCRequestDate is not null AND t.tCSpecialistUserID is not null "
			+ " AND t.tCSpecialistUserID =:tCSpecialistUserID AND DATE(t.tCRequestDate) <= curdate() "
			+ " AND t.deleted = false AND t.providerServiceMapId = :providerServiceMapId "
			+ " ORDER BY t.benVisitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getTCSpecialistWorkListNew(
			@Param("providerServiceMapId") Integer providerServiceMapId,
			@Param("tCSpecialistUserID") Integer tCSpecialistUserID);

	// TC Specialist work-list, future scheduled, 13-12-2018
	@Query("SELECT t from BeneficiaryFlowStatus t "
			+ " WHERE t.specialist_flag NOT IN (0,4) AND t.tCRequestDate is not null AND t.tCSpecialistUserID is not null "
			+ " AND t.tCSpecialistUserID =:tCSpecialistUserID AND DATE(t.tCRequestDate) > curdate() "
			+ " AND t.deleted = false AND t.providerServiceMapId = :providerServiceMapId "
			+ " ORDER BY t.benVisitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getTCSpecialistWorkListNewFutureScheduled(
			@Param("providerServiceMapId") Integer providerServiceMapId,
			@Param("tCSpecialistUserID") Integer tCSpecialistUserID);

//	@Query("SELECT  t.benFlowID from BeneficiaryFlowStatus t WHERE t.beneficiaryRegID = :benRegID "
//			+ "AND t.providerServiceMapId = :provoderSerMapID AND t.vanID = :vanID AND "
//			+ " (t.nurseFlag = 1 OR t.nurseFlag = 100) AND Date(t.visitDate)  = curdate() AND t.deleted = false")
//	public ArrayList<Long> checkBenAlreadyInNurseWorkList(@Param("benRegID") Long benRegID,
//			@Param("provoderSerMapID") Integer provoderSerMapID, @Param("vanID") Integer vanID);
	
	@Query("SELECT  t.benFlowID from BeneficiaryFlowStatus t WHERE t.beneficiaryRegID = :benRegID "
			+ "AND t.providerServiceMapId = :provoderSerMapID AND t.vanID = :vanID AND "
			+ " (t.nurseFlag = 1 OR t.nurseFlag = 100) AND Date(t.visitDate) >= Date(:fromDate) AND t.deleted = false")
	public ArrayList<Long> checkBenAlreadyInNurseWorkList(@Param("benRegID") Long benRegID,
			@Param("provoderSerMapID") Integer provoderSerMapID, @Param("vanID") Integer vanID,@Param("fromDate") Timestamp fromDate);


	@Query("SELECT t from BeneficiaryFlowStatus t WHERE (t.nurseFlag = 2 OR t.doctorFlag = 2 OR t.specialist_flag = 2) "
			+ " AND t.benVisitDate >= Date(:fromDate) AND t.vanID = :vanID AND t.deleted = false "
			+ " AND t.providerServiceMapId = :providerServiceMapId ORDER BY consultationDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getLabWorklistNew(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("fromDate") Timestamp fromDate,
			@Param("vanID") Integer vanID);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.doctorFlag = :docFlag , t.pharmacist_flag = :pharmaFlag, "
			+ " t.oncologist_flag = :oncologistFlag, t.consultationDate = now(), t.processed = 'U', "
			+ " t.specialist_flag = :tcSpecialistFlag, t.tCSpecialistUserID = :tcSpecialistUserID, t.tCRequestDate = :tcDate "
			+ " WHERE t.benFlowID = :benFlowID AND " + " t.beneficiaryRegID = :benRegID AND t.beneficiaryID = :benID ")
	public int updateBenFlowStatusAfterDoctorActivity(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("benID") Long benID, @Param("docFlag") Short docFlag,
			@Param("pharmaFlag") Short pharmaFlag, @Param("oncologistFlag") Short oncologistFlag,
			@Param("tcSpecialistFlag") Short tcSpecialistFlag, @Param("tcSpecialistUserID") int tcSpecialistUserID,
			@Param("tcDate") Timestamp tcDate);
	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.doctorFlag = :docFlag , t.pharmacist_flag = :pharmaFlag, "
			+ " t.oncologist_flag = :oncologistFlag, t.consultationDate = now(), t.processed = 'U', "
			+ " t.tCSpecialistUserID = :tcSpecialistUserID, t.tCRequestDate = :tcDate "
			+ " WHERE t.benFlowID = :benFlowID AND " + " t.beneficiaryRegID = :benRegID AND t.beneficiaryID = :benID ")
	public int updateBenFlowStatusAfterDoctorActivityWDF(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("benID") Long benID, @Param("docFlag") Short docFlag,
			@Param("pharmaFlag") Short pharmaFlag, @Param("oncologistFlag") Short oncologistFlag,
			 @Param("tcSpecialistUserID") int tcSpecialistUserID,
			@Param("tcDate") Timestamp tcDate);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.pharmacist_flag = :pharmaFlag, "
			+ " t.oncologist_flag = :oncologistFlag, t.processed = 'U', t.specialist_flag = :tcSpecialistFlag "
			+ " WHERE t.benFlowID = :benFlowID AND  t.beneficiaryRegID = :benRegID AND t.beneficiaryID = :benID ")
	public int updateBenFlowStatusAfterDoctorActivityTCSpecialist(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("benID") Long benID, @Param("pharmaFlag") Short pharmaFlag,
			@Param("oncologistFlag") Short oncologistFlag, @Param("tcSpecialistFlag") Short tcSpecialistFlag);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.doctorFlag = :docFlag , t.pharmacist_flag = :pharmaFlag, "
			+ " t.oncologist_flag = :oncologistFlag , t.processed = 'U' " + " WHERE t.benFlowID = :benFlowID AND "
			+ " t.beneficiaryRegID = :benRegID AND t.beneficiaryID = :benID ")
	public int updateBenFlowStatusAfterDoctorActivityUpdate(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("benID") Long benID, @Param("docFlag") Short docFlag,
			@Param("pharmaFlag") Short pharmaFlag, @Param("oncologistFlag") Short oncologistFlag);

	@Query("SELECT t from BeneficiaryFlowStatus t WHERE t.radiologist_flag = 1 "
			+ " AND t.benVisitDate >= Date(:fromDate) AND t.vanID = :vanID "
			+ " AND t.providerServiceMapId= :providerServiceMapId ORDER BY t.benVisitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getRadiologistWorkListNew(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("fromDate") Timestamp fromDate,
			@Param("vanID") Integer vanID);

	@Query("SELECT t from BeneficiaryFlowStatus t WHERE t.oncologist_flag = 1 "
			+ " AND t.benVisitDate >= Date(:fromDate) AND t.vanID = :vanID "
			+ " AND t.providerServiceMapId= :providerServiceMapId")
	public ArrayList<BeneficiaryFlowStatus> getOncologistWorkListNew(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("fromDate") Timestamp fromDate,
			@Param("vanID") Integer vanID);

	@Query("SELECT t from BeneficiaryFlowStatus t WHERE t.pharmacist_flag = 1 "
			+ " AND t.benVisitDate >= Date(:fromDate) AND t.vanID = :vanID "
			+ "  AND t.providerServiceMapId= :providerServiceMapId AND (doctorFlag = 9 OR specialist_flag = 9) "
			+ "  ORDER BY consultationDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getPharmaWorkListNew(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("fromDate") Timestamp fromDate,
			@Param("vanID") Integer vanID);

	@Query("SELECT t.pharmacist_flag from BeneficiaryFlowStatus t WHERE t.benFlowID = :benFlowID")
	public Short getPharmaFlag(@Param("benFlowID") Long benFlowID);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.nurseFlag = :nurseFlag, t.processed = 'U' "
			+ " where t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID ")
	public int updateBenFlowStatusAfterNurseDataUpdateNCD_Screening(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("nurseFlag") Short nurseFlag);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set t.nurseFlag = :nurseFlag, t.doctorFlag = :doctorFlag, "
			+ " t.lab_technician_flag = :labFlag, t.processed = 'U' "
			+ " WHERE t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID ")
	public int updateBenFlowStatusAfterLabResultEntry(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("nurseFlag") Short nurseFlag,
			@Param("doctorFlag") Short doctorFlag, @Param("labFlag") Short labFlag);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t set  t.processed = 'U', t.specialist_flag = :specialist_flag "
			+ " WHERE t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID ")
	public int updateBenFlowStatusAfterLabResultEntryForSpecialist(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("specialist_flag") Short specialist_flag);

	// beneficiary previous visit history
	@Query("SELECT benFlowID, beneficiaryRegID, visitCode, "
			+ " benVisitDate, benVisitNo, VisitReason, VisitCategory  from BeneficiaryFlowStatus "
			+ "  WHERE beneficiaryRegID = :beneficiaryRegID AND ((doctorFlag = 9) "
			+ " OR (nurseFlag = 9 AND doctorFlag = 0)) AND providerServiceMapId IN :psmIDList  ORDER BY benVisitDate DESC ")
	public ArrayList<Object[]> getBenPreviousHistory(@Param("beneficiaryRegID") Long beneficiaryRegID,
			@Param("psmIDList") List<Integer> psmIDList);

	@Query(" SELECT COUNT(benFlowID) FROM BeneficiaryFlowStatus "
			+ " WHERE beneficiaryRegID = :beneficiaryRegID AND VisitCategory = 'NCD screening' ")
	public Long getNcdScreeningVisitCount(@Param("beneficiaryRegID") Long beneficiaryRegID);

	@Transactional
	@Modifying
	@Query(" UPDATE BeneficiaryFlowStatus t SET t.benArrivedFlag = :arrivalFlag, "
			+ " t.modified_by = :modifiedBy WHERE t.benFlowID = :benFlowID AND t.tCSpecialistUserID = :userID "
			+ " AND t.beneficiaryRegID = :benRegID AND t.visitCode = :visitCode ")
	public int updateBeneficiaryArrivalStatus(@Param("benFlowID") Long benFlowID, @Param("benRegID") Long benRegID,
			@Param("visitCode") Long visitCode, @Param("arrivalFlag") Boolean arrivalFlag,
			@Param("modifiedBy") String modifiedBy, @Param("userID") Integer userID);

	@Transactional
	@Modifying
	@Query(" UPDATE BeneficiaryFlowStatus t SET t.benArrivedFlag = false, t.modified_by = :modifiedBy, "
			+ " t.tCSpecialistUserID = null, t.tCRequestDate = null, t.specialist_flag = 4  "
			+ " WHERE t.benFlowID = :benFlowID AND t.tCSpecialistUserID = :userID "
			+ " AND t.beneficiaryRegID = :benRegID AND t.visitCode = :visitCode ")
	public int updateBeneficiaryStatusToCancelRequest(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("visitCode") Long visitCode,
			@Param("modifiedBy") String modifiedBy, @Param("userID") Integer userID);

	@Query(" SELECT t FROM BeneficiaryFlowStatus t "
			+ " WHERE t.benFlowID = :benFlowID AND t.tCSpecialistUserID = :userID "
			+ " AND t.beneficiaryRegID = :benRegID AND t.visitCode = :visitCode ")
	public ArrayList<BeneficiaryFlowStatus> checkBeneficiaryArrivalStatus(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("visitCode") Long visitCode, @Param("userID") Integer userID);

	@Transactional
	@Modifying
	@Query(" UPDATE BeneficiaryFlowStatus t SET t.specialist_flag = 1, t.tCSpecialistUserID = :userID, "
			+ " t.tCRequestDate = :reqDate, t.processed = 'U' "
			+ " WHERE t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID AND t.visitCode = :visitCode ")
	public int updateFlagAfterTcRequestCreatedFromWorklist(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("visitCode") Long visitCode, @Param("userID") Integer userID,
			@Param("reqDate") Timestamp reqDate);

	// TC request list
	@Query("SELECT t from BeneficiaryFlowStatus t "
			+ " WHERE t.tCSpecialistUserID = :tCSpecialistUserID AND t.providerServiceMapId = :providerServiceMapId "
			+ " AND DATE(t.tCRequestDate) = :reqDate ")
	public ArrayList<BeneficiaryFlowStatus> getTCRequestList(
			@Param("providerServiceMapId") Integer providerServiceMapId,
			@Param("tCSpecialistUserID") Integer tCSpecialistUserID, @Param("reqDate") Timestamp reqDate);

	@Transactional
	@Modifying
	@Query(" UPDATE BeneficiaryFlowStatus t SET t.specialist_flag = 9 "
			+ " WHERE t.benFlowID = :benFlowID AND t.beneficiaryRegID = :benRegID AND t.visitCode = :visitCode ")
	public int updateBenFlowAfterTCSpcialistDoneForCanceScreening(@Param("benFlowID") Long benFlowID,
			@Param("benRegID") Long benRegID, @Param("visitCode") Long visitCode);

	@Query(" SELECT benName, Date(dOB), genderID FROM BeneficiaryFlowStatus WHERE benFlowID = :benFlowID ")
	public ArrayList<Object[]> getBenDataForCareStream(@Param("benFlowID") Long benFlowID);

	@Query("SELECT  t from BeneficiaryFlowStatus t WHERE (t.specialist_flag = 100 OR t.specialist_flag = 200) AND t.deleted = false "
			+ " AND t.providerServiceMapId = :providerServiceMapId AND t.benVisitDate >= Date(:fromDate) "
			+ " AND t.vanID = :vanID AND t.referredVisitCode is null ORDER BY t.visitDate DESC ")
	public ArrayList<BeneficiaryFlowStatus> getNurseWorklistTMreferred(
			@Param("providerServiceMapId") Integer providerServiceMapId, @Param("vanID") Integer vanID,
			@Param("fromDate") Timestamp fromDate);
	// Query to check TM visit is done for referred case....Shubham Shekhar
//	@Query(value="SELECT count(ben_flow_id) FROM db_iemr.i_ben_flow_outreach i where i.referred_visitcode = :visitCode and specialist_flag=9"
//			,nativeQuery=true)
//	public Integer isTMvisitDone(@Param("visitCode") Long visitCode);

	@Query("SELECT t.isCaseSheetdownloaded from BeneficiaryFlowStatus t where t.visitCode = :mmuVisitCode")
	public Boolean checkIsCaseSheetDownloaded(@Param("mmuVisitCode") Long mmuVisitCode);

	@Query("SELECT t from BeneficiaryFlowStatus t where t.referredVisitCode = :mmuVisitCode")
	public BeneficiaryFlowStatus getTMVisitDetails(@Param("mmuVisitCode") Long mmuVisitCode);

	@Transactional
	@Modifying
	@Query("UPDATE BeneficiaryFlowStatus t SET t.isCaseSheetdownloaded = true WHERE t.visitCode = :mmuVisitCode")
	public int updateDownloadFlag(@Param("mmuVisitCode") Long mmuVisitCode);

	@Query("SELECT t from BeneficiaryFlowStatus t where t.visitCode = :mmuVisitCode")
	public BeneficiaryFlowStatus specialistFlagAndCategoryValue(@Param("mmuVisitCode") Long mmuVisitCode);

	// get ben age, HRP
	@Query(nativeQuery = true, value = " SELECT ben_dob FROM i_ben_flow_outreach WHERE beneficiary_reg_id = :benRegID "
			+ " AND ben_gender_val = 2 AND ben_dob is not null order by ben_flow_id DESC LIMIT 1 ")
	public Timestamp getBenAgeVal(@Param("benRegID") Long benRegID);
	
	@Query("SELECT t from BeneficiaryFlowStatus t where t.benFlowID = :benFlowID And t.nurseFlag = :nurseFlag")
	BeneficiaryFlowStatus checkExistData(@Param("benFlowID") Long benFlowID, @Param("nurseFlag") Short nurseFlag);
	
	
}
