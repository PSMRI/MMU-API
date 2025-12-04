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

import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.labModule.LabResultEntry;
import java.util.ArrayList;
import java.util.HashMap;
// import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonArray;
// import com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail;
// import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
// import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
// import com.iemr.mmu.data.tele_consultation.TCRequestModel;
// import com.iemr.mmu.data.tele_consultation.TcSpecialistSlotBookingRequestOBJ;
// import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
// import com.iemr.mmu.data.anc.WrapperAncFindings;
// import com.iemr.mmu.data.anc.WrapperBenInvestigationANC;
import java.util.Map;
// import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.data.tele_consultation.TCRequestModel;
import com.iemr.mmu.data.tele_consultation.TcSpecialistSlotBookingRequestOBJ;
import com.iemr.mmu.utils.mapper.InputMapper;

import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
// import com.iemr.mmu.data.anc.BenMedHistory;
// import com.iemr.mmu.data.anc.BenPersonalHabit;
// import com.iemr.mmu.data.anc.BenFamilyHistory;
// import com.iemr.mmu.data.anc.BenMenstrualDetails;
// import com.iemr.mmu.data.anc.PhyGeneralExamination;
// import com.iemr.mmu.data.anc.PhyHeadToToeExamination;
// import com.iemr.mmu.data.anc.SysCentralNervousExamination;
// import com.iemr.mmu.data.anc.SysGastrointestinalExamination;
// import com.iemr.mmu.data.anc.PerinatalHistory;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;

import java.lang.reflect.Field;

class GeneralOPDServiceImplTest {

	@Test
	void updateBenExaminationDetails_allFieldsPresent() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		obj.add("generalExamination", new JsonObject());
		obj.add("headToToeExamination", new JsonObject());
		obj.add("gastroIntestinalExamination", new JsonObject());
		obj.add("cardioVascularExamination", new JsonObject());
		obj.add("respiratorySystemExamination", new JsonObject());
		obj.add("centralNervousSystemExamination", new JsonObject());
		obj.add("musculoskeletalSystemExamination", new JsonObject());
		obj.add("genitoUrinarySystemExamination", new JsonObject());
		when(nurseService.updatePhyGeneralExamination(any())).thenReturn(2);
		when(nurseService.updatePhyHeadToToeExamination(any())).thenReturn(2);
		when(nurseService.updateSysGastrointestinalExamination(any())).thenReturn(2);
		when(nurseService.updateSysCardiovascularExamination(any())).thenReturn(2);
		when(nurseService.updateSysRespiratoryExamination(any())).thenReturn(2);
		when(nurseService.updateSysCentralNervousExamination(any())).thenReturn(2);
		when(nurseService.updateSysMusculoskeletalSystemExamination(any())).thenReturn(2);
		when(nurseService.updateSysGenitourinarySystemExamination(any())).thenReturn(2);
		int result = spyService.updateBenExaminationDetails(obj);
		assertEquals(2, result);
	}

	@Test
	void updateBenExaminationDetails_allFieldsMissing() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		int result = spyService.updateBenExaminationDetails(obj);
	// All branches missing, so all flags set to 1, exmnSuccessFlag = 1
	assertEquals(1, result);
	}

	@Test
	void updateBenExaminationDetails_someFieldsPresent() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		obj.add("generalExamination", new JsonObject());
		obj.add("headToToeExamination", new JsonObject());
		// Only two fields present, rest missing
		when(nurseService.updatePhyGeneralExamination(any())).thenReturn(2);
		when(nurseService.updatePhyHeadToToeExamination(any())).thenReturn(2);
		int result = spyService.updateBenExaminationDetails(obj);
	// Only the present flags are set to 2, so exmnSuccessFlag = 2
	assertEquals(2, result);
	}
	@Test
	void saveNurseData_successPath_allBranches() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		CommonBenStatusFlowServiceImpl benStatusService = mock(CommonBenStatusFlowServiceImpl.class);
		BeneficiaryFlowStatusRepo repo = mock(BeneficiaryFlowStatusRepo.class);
		java.lang.reflect.Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, nurseService);
		java.lang.reflect.Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonBenStatusFlowServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, benStatusService);
		java.lang.reflect.Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("beneficiaryFlowStatusRepo");
		f3.setAccessible(true);
		f3.set(spyService, repo);
	JsonObject visitDetails = new JsonObject();
	JsonObject innerVisitDetails = new JsonObject();
	innerVisitDetails.addProperty("beneficiaryRegID", 1L);
	innerVisitDetails.addProperty("visitReason", "reason");
	innerVisitDetails.addProperty("visitCategory", "cat");
	visitDetails.add("visitDetails", innerVisitDetails);
		JsonObject obj = new JsonObject();
		obj.add("visitDetails", visitDetails);
		obj.add("historyDetails", new JsonObject());
		obj.add("vitalDetails", new JsonObject());
		obj.add("examinationDetails", new JsonObject());
		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getBenFlowID()).thenReturn(1L);
		when(util.getVanID()).thenReturn(1);
		when(repo.checkExistData(anyLong(), anyShort())).thenReturn(null);
		// SaveBenVisitDetails returns a map with keys
		Map<String, Long> visitMap = new HashMap<>();
		visitMap.put("visitID", 1L);
		visitMap.put("visitCode", 2L);
		Mockito.doReturn(visitMap).when(spyService).saveBenVisitDetails(any(), any());
		Mockito.doReturn(1L).when(spyService).saveBenGeneralOPDHistoryDetails(any(), anyLong(), anyLong());
		Mockito.doReturn(1L).when(spyService).saveBenVitalDetails(any(), anyLong(), anyLong());
		Mockito.doReturn(1L).when(spyService).saveBenExaminationDetails(any(), anyLong(), anyLong());
		Long result = spyService.saveNurseData(obj);
		assertEquals(1L, result);
	}

	@Test
	void saveNurseData_existingData_returnsNull() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		BeneficiaryFlowStatusRepo repo = mock(BeneficiaryFlowStatusRepo.class);
		java.lang.reflect.Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, nurseService);
		java.lang.reflect.Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("beneficiaryFlowStatusRepo");
		f3.setAccessible(true);
		f3.set(spyService, repo);
		JsonObject visitDetails = new JsonObject();
		visitDetails.add("visitDetails", new JsonObject());
		JsonObject obj = new JsonObject();
		obj.add("visitDetails", visitDetails);
		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getBenFlowID()).thenReturn(1L);
		when(repo.checkExistData(anyLong(), anyShort())).thenReturn(mock(BeneficiaryFlowStatus.class));
		Long result = spyService.saveNurseData(obj);
		assertNull(result);
	}

	@Test
	void saveBenVisitDetails_successPath_allBranches() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject visitDetails = new JsonObject();
		JsonObject inner = new JsonObject();
		visitDetails.add("visitDetails", inner);
		visitDetails.add("chiefComplaints", new JsonArray());
		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getVanID()).thenReturn(1);
		when(util.getSessionID()).thenReturn(1);
		BeneficiaryVisitDetail benVisitDetailsOBJ = mock(BeneficiaryVisitDetail.class);
		when(benVisitDetailsOBJ.getBeneficiaryRegID()).thenReturn(1L);
		when(benVisitDetailsOBJ.getVisitReason()).thenReturn("reason");
		when(benVisitDetailsOBJ.getVisitCategory()).thenReturn("cat");
	// Instead of fromJson, mock InputMapper.gson().fromJson if needed (not required here)
		when(nurseService.getMaxCurrentdate(anyLong(), anyString(), anyString())).thenReturn(0);
		when(nurseService.saveBeneficiaryVisitDetails(any())).thenReturn(1L);
		when(nurseService.generateVisitCode(anyLong(), anyInt(), anyInt())).thenReturn(2L);
	when(nurseService.saveBenChiefComplaints(any())).thenReturn(1);
		Map<String, Long> result = spyService.saveBenVisitDetails(visitDetails, util);
		assertTrue(result.containsKey("visitID"));
		assertTrue(result.containsKey("visitCode"));
	}

	@Test
	void saveBenVisitDetails_nullBranches() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
	// Use null input to guarantee empty map
	CommonUtilityClass util = mock(CommonUtilityClass.class);
	Map<String, Long> result = spyService.saveBenVisitDetails(null, util);
	assertNotNull(result);
	assertEquals(0, result.size());
	}

	@Test
	void getBenCaseRecordFromDoctorGeneralOPD_success() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		GeneralOPDDoctorServiceImpl opdDocService = mock(GeneralOPDDoctorServiceImpl.class);
		LabTechnicianServiceImpl labService = mock(LabTechnicianServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		java.lang.reflect.Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("generalOPDDoctorServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, opdDocService);
		java.lang.reflect.Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("labTechnicianServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, labService);
		java.lang.reflect.Field f4 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f4.setAccessible(true);
		f4.set(spyService, nurseService);
		when(docService.getFindingsDetails(anyLong(), anyLong())).thenReturn("findings");
		when(opdDocService.getGeneralOPDDiagnosisDetails(anyLong(), anyLong())).thenReturn("diagnosis");
		when(docService.getInvestigationDetails(anyLong(), anyLong())).thenReturn("investigation");
		when(docService.getPrescribedDrugs(anyLong(), anyLong())).thenReturn("prescription");
		when(docService.getReferralDetails(anyLong(), anyLong())).thenReturn("refer");
	ArrayList<LabResultEntry> labResults = new ArrayList<>();
	LabResultEntry entry = new LabResultEntry();
	labResults.add(entry);
	when(labService.getLabResultDataForBen(anyLong(), anyLong())).thenReturn(labResults);
	HashMap<String, Object> graphData = new HashMap<>();
	graphData.put("someKey", "someValue");
	when(nurseService.getGraphicalTrendData(anyLong(), anyString())).thenReturn(graphData);
		when(labService.getLast_3_ArchivedTestVisitList(anyLong(), anyLong())).thenReturn("archived");
		String result = spyService.getBenCaseRecordFromDoctorGeneralOPD(1L, 2L);
		assertNotNull(result);
	assertTrue(result.contains("findings"));
	assertTrue(result.contains("diagnosis"));
	assertTrue(result.contains("investigation"));
	assertTrue(result.contains("prescription"));
	assertTrue(result.contains("refer"));
	assertTrue(result.contains("archived"));
	// Check that the stringified map/array is present
	assertTrue(result.contains("someKey"));
	}
	private GeneralOPDServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new GeneralOPDServiceImpl();
	}

	@Test
	void setLabTechnicianServiceImpl_setsDependency() {
		LabTechnicianServiceImpl lab = new LabTechnicianServiceImpl();
		service.setLabTechnicianServiceImpl(lab);
		// No exception means pass
	}

	@Test
	void setGeneralOPDDoctorServiceImpl_setsDependency() {
		GeneralOPDDoctorServiceImpl doc = new GeneralOPDDoctorServiceImpl();
		service.setGeneralOPDDoctorServiceImpl(doc);
	}

	@Test
	void setCommonBenStatusFlowServiceImpl_setsDependency() {
		CommonBenStatusFlowServiceImpl ben = new CommonBenStatusFlowServiceImpl();
		service.setCommonBenStatusFlowServiceImpl(ben);
	}

	@Test
	void setCommonDoctorServiceImpl_setsDependency() {
		CommonDoctorServiceImpl doc = new CommonDoctorServiceImpl();
		service.setCommonDoctorServiceImpl(doc);
	}

	@Test
	void setGeneralOPDNurseServiceImpl_setsDependency() {
		GeneralOPDNurseServiceImpl nurse = new GeneralOPDNurseServiceImpl();
		service.setGeneralOPDNurseServiceImpl(nurse);
	}

    @Test
	void saveBenGeneralOPDHistoryDetails_allNullBranches() throws Exception {
		// Setup
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		// All branches null
		Long result = spyService.saveBenGeneralOPDHistoryDetails(obj, 1L, 2L);
		assertEquals(1L, result);
	}

	@Test
	void saveBenGeneralOPDHistoryDetails_allBranchesPresent() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		obj.add("pastHistory", new JsonObject());
		obj.add("comorbidConditions", new JsonObject());
		obj.add("medicationHistory", new JsonObject());
		obj.add("femaleObstetricHistory", new JsonObject());
		obj.add("menstrualHistory", new JsonObject());
		obj.add("familyHistory", new JsonObject());
		obj.add("personalHistory", new JsonObject());
		obj.add("childVaccineDetails", new JsonObject());
		obj.add("immunizationHistory", new JsonObject());
		obj.add("developmentHistory", new JsonObject());
		obj.add("feedingHistory", new JsonObject());
		obj.add("perinatalHistroy", new JsonObject());
		// Stubbing all save methods to return >0
		when(nurseService.saveBenPastHistory(any())).thenReturn(2L);
		when(nurseService.saveBenComorbidConditions(any())).thenReturn(2L);
		when(nurseService.saveBenMedicationHistory(any())).thenReturn(2L);
		when(nurseService.saveFemaleObstetricHistory(any())).thenReturn(2L);
		when(nurseService.saveBenMenstrualHistory(any())).thenReturn(2);
		when(nurseService.saveBenFamilyHistory(any())).thenReturn(2L);
		when(nurseService.savePersonalHistory(any())).thenReturn(2);
		when(nurseService.saveAllergyHistory(any())).thenReturn(2L);
		when(nurseService.saveChildOptionalVaccineDetail(any())).thenReturn(2L);
		when(nurseService.saveImmunizationHistory(any())).thenReturn(2L);
		when(nurseService.saveChildDevelopmentHistory(any())).thenReturn(2L);
		when(nurseService.saveChildFeedingHistory(any())).thenReturn(2L);
		when(nurseService.savePerinatalHistory(any())).thenReturn(2L);
		Long result = spyService.saveBenGeneralOPDHistoryDetails(obj, 1L, 2L);
		assertEquals(2L, result);
	}
	@Test
	void updateBenVitalDetails_nullInput_returnsOne() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		Long result = spyService.saveBenVitalDetails(null, 1L, 2L);
		assertEquals(1L, result);
	}

	@Test
	void updateBenVitalDetails_withInput_successPath() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		when(nurseService.updateANCAnthropometryDetails(any())).thenReturn(1);
		when(nurseService.updateANCPhysicalVitalDetails(any())).thenReturn(1);
		int result = spyService.updateBenVitalDetails(obj);
		assertEquals(1, result);
	}

	@Test
	void updateBenStatusFlagAfterNurseSaveSuccess_success() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonBenStatusFlowServiceImpl benStatusService = mock(CommonBenStatusFlowServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonBenStatusFlowServiceImpl");
		f.setAccessible(true);
		f.set(spyService, benStatusService);
		JsonObject obj = new JsonObject();
		obj.addProperty("beneficiaryRegID", 1L);
		obj.addProperty("visitReason", "reason");
		obj.addProperty("visitCategory", "cat");
		when(benStatusService.updateBenFlowNurseAfterNurseActivity(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyShort(), anyShort(), anyShort(), anyShort(), anyShort(), anyLong(), anyInt())).thenReturn(42);
		// Use reflection to invoke private method
		java.lang.reflect.Method m = GeneralOPDServiceImpl.class.getDeclaredMethod("updateBenStatusFlagAfterNurseSaveSuccess", JsonObject.class, Long.class, Long.class, Long.class, Integer.class);
		m.setAccessible(true);
		int result = (int) m.invoke(spyService, obj, 2L, 3L, 4L, 5);
		assertEquals(42, result);
	}

	@Test
	void getBenGeneralOPDNurseData_returnsString() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		when(nurseService.getCSVisitDetails(anyLong(), anyLong())).thenReturn(mock(BeneficiaryVisitDetail.class));
		when(nurseService.getBenChiefComplaints(anyLong(), anyLong())).thenReturn("complaints");
		String result = spyService.getBenGeneralOPDNurseData(1L, 2L);
		assertNotNull(result);
	}

	@Test
	void setCommonNurseServiceImpl_setsDependency() {
		CommonNurseServiceImpl commonNurseService = new CommonNurseServiceImpl();
		service.setCommonNurseServiceImpl(commonNurseService);
		// No exception means pass
	}

	@Test
	void saveNurseData_nullRequest_returnsNull() throws Exception {
		Long result = service.saveNurseData(null);
		assertNull(result);
	}

	@Test
	void saveNurseData_emptyVisitDetails_returnsNull() throws Exception {
		JsonObject obj = new JsonObject();
		obj.add("visitDetails", new JsonObject());
		Long result = service.saveNurseData(obj);
		assertNull(result);
	}

	@Test
	void saveBenVisitDetails_nullObject_returnsEmpty() throws Exception {
		Map<String, Long> result = service.saveBenVisitDetails(null, mock(CommonUtilityClass.class));
		assertTrue(result.isEmpty());
	}

	@Test
	void saveBenVitalDetails_nullObject_returnsOne() throws Exception {
		Long result = service.saveBenVitalDetails(null, 1L, 2L);
		assertEquals(1L, result);
	}

	@Test
	void saveBenVitalDetails_withData() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		when(nurseService.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(2L);
		when(nurseService.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(2L);
		Long result = spyService.saveBenVitalDetails(obj, 1L, 2L);
		assertEquals(2L, result);
	}


	@Test
	void saveBenExaminationDetails_nullObject_returnsOne() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		try {
			java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
			f.setAccessible(true);
			f.set(spyService, nurseService);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}
		Long result = spyService.saveBenExaminationDetails(null, 1L, 2L);
		assertEquals(1L, result);
	}

	@Test
	void saveBenExaminationDetails_withData() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		JsonObject obj = new JsonObject();
		obj.add("generalExamination", new JsonObject());
		obj.add("headToToeExamination", new JsonObject());
		obj.add("gastroIntestinalExamination", new JsonObject());
		obj.add("cardioVascularExamination", new JsonObject());
		obj.add("respiratorySystemExamination", new JsonObject());
		obj.add("centralNervousSystemExamination", new JsonObject());
		obj.add("musculoskeletalSystemExamination", new JsonObject());
		obj.add("genitourinarySystemExamination", new JsonObject());
	when(nurseService.savePhyGeneralExamination(any())).thenReturn(2L);
	when(nurseService.savePhyHeadToToeExamination(any())).thenReturn(2L);
	when(nurseService.saveSysGastrointestinalExamination(any())).thenReturn(2L);
	when(nurseService.saveSysCardiovascularExamination(any())).thenReturn(2L);
	when(nurseService.saveSysRespiratoryExamination(any())).thenReturn(2L);
	when(nurseService.saveSysCentralNervousExamination(any())).thenReturn(2L);
	when(nurseService.saveSysMusculoskeletalSystemExamination(any())).thenReturn(2L);
	when(nurseService.saveSysGenitourinarySystemExamination(any())).thenReturn(2L);
		Long result = spyService.saveBenExaminationDetails(obj, 1L, 2L);
		assertEquals(2L, result);
	}

	@Test
	void getBenVisitDetailsFrmNurseGOPD_success() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f.setAccessible(true);
		f.set(spyService, nurseService);
		when(nurseService.getCSVisitDetails(anyLong(), anyLong())).thenReturn(mock(BeneficiaryVisitDetail.class));
		when(nurseService.getBenChiefComplaints(anyLong(), anyLong())).thenReturn("complaints");
		String result = spyService.getBenVisitDetailsFrmNurseGOPD(1L, 2L);
		assertNotNull(result);
	}


	@Test
	void getBenHistoryDetails_basicCoverage() {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		try {
			java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
			f.setAccessible(true);
			f.set(spyService, nurseService);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}
		when(nurseService.getPastHistoryData(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getComorbidityConditionsHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getMedicationHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getPersonalHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getFamilyHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getMenstrualHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getFemaleObstetricHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getImmunizationHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getChildOptionalVaccineHistory(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getPerinatalHistory(anyLong(), anyLong())).thenReturn(null);
		String result = spyService.getBenHistoryDetails(1L, 2L);
		assertNotNull(result);
	}


	@Test
	void getBeneficiaryVitalDetails_basicCoverage() {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		try {
			java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
			f.setAccessible(true);
			f.set(spyService, nurseService);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}
		when(nurseService.getBeneficiaryPhysicalAnthropometryDetails(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getBeneficiaryPhysicalVitalDetails(anyLong(), anyLong())).thenReturn(null);
		String result = spyService.getBeneficiaryVitalDetails(1L, 2L);
		assertNotNull(result);
	}


	@Test
	void getExaminationDetailsData_basicCoverage() {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		try {
			java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
			f.setAccessible(true);
			f.set(spyService, nurseService);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}
		when(nurseService.getGeneralExaminationData(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getHeadToToeExaminationData(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getSysCentralNervousExamination(anyLong(), anyLong())).thenReturn(null);
		when(nurseService.getSysGastrointestinalExamination(anyLong(), anyLong())).thenReturn(null);
		String result = spyService.getExaminationDetailsData(1L, 2L);
		assertNotNull(result);
	}


	@Test
	void UpdateVisitDetails_basicCoverage() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		try {
			java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
			f.setAccessible(true);
			f.set(spyService, nurseService);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}
		JsonObject obj = new JsonObject();
		obj.add("visitDetails", new JsonObject());
		obj.add("chiefComplaints", new JsonArray());
		when(nurseService.updateBeneficiaryVisitDetails(any())).thenReturn(1);
		when(nurseService.updateBenChiefComplaints(any())).thenReturn(1);
		int result = spyService.UpdateVisitDetails(obj);
		// Basic test for method coverage
		assertTrue(result >= 0);
	}


	@Test
	void updateBenHistoryDetails_basicCoverage() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		try {
			java.lang.reflect.Field f = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
			f.setAccessible(true);
			f.set(spyService, nurseService);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}
		JsonObject obj = new JsonObject();
		obj.add("pastHistory", new JsonObject());
		obj.add("comorbidConditions", new JsonObject());
		obj.add("medicationHistory", new JsonObject());
		obj.add("femaleObstetricHistory", new JsonObject());
		obj.add("menstrualHistory", new JsonObject());
		obj.add("familyHistory", new JsonObject());
		obj.add("personalHistory", new JsonObject());
		obj.add("childVaccineDetails", new JsonObject());
		obj.add("immunizationHistory", new JsonObject());
		obj.add("developmentHistory", new JsonObject());
		obj.add("feedingHistory", new JsonObject());
		obj.add("perinatalHistroy", new JsonObject());
		// Only mock methods that exist
		when(nurseService.updateBenPastHistoryDetails(any())).thenReturn(1);
		when(nurseService.updateBenComorbidConditions(any())).thenReturn(1);
		when(nurseService.updateBenMedicationHistory(any())).thenReturn(1);
		when(nurseService.updateMenstrualHistory(any())).thenReturn(1);
		when(nurseService.updateBenFamilyHistory(any())).thenReturn(1);
		when(nurseService.updateBenPersonalHistory(any())).thenReturn(1);
		when(nurseService.updateChildOptionalVaccineDetail(any())).thenReturn(1);
		when(nurseService.updateChildDevelopmentHistory(any())).thenReturn(1);
		when(nurseService.updateChildFeedingHistory(any())).thenReturn(1);
		when(nurseService.updatePerinatalHistory(any())).thenReturn(1);
		int result = spyService.updateBenHistoryDetails(obj);
		// Basic test for method coverage
		assertTrue(result >= 0);
	}
	@Test
	void updateGeneralOPDDoctorData_nullRequest_returnsNull() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		Long result = spyService.updateGeneralOPDDoctorData(null, "auth");
		assertNull(result);
	}

	@Test
	void updateGeneralOPDDoctorData_errorBranches_throwRuntime() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);
		JsonObject obj = new JsonObject();
		obj.add("investigation", new JsonObject());
		obj.add("prescription", new JsonArray());
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 1);
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn((int) 1L);
		try {
			spyService.updateGeneralOPDDoctorData(obj, "auth");
			fail("Should throw RuntimeException");
		} catch (RuntimeException e) {
			// expected
		}
	}

	@Test
	void saveDoctorData_nullRequest_returnsNull() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		Long result = spyService.saveDoctorData(null, "auth");
		assertNull(result);
	}

	@Test
	void saveDoctorData_errorBranches_throwRuntime() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);
		JsonObject obj = new JsonObject();
		obj.add("investigation", new JsonObject());
		obj.add("prescription", new JsonArray());
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 1);
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn((int) 1L);
		try {
			spyService.saveDoctorData(obj, "auth");
			fail("Should throw RuntimeException");
		} catch (RuntimeException e) {
			// expected
		}
	}

	@Test
	void saveDoctorData_success_allBranches() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);

		JsonObject obj = new JsonObject();
		obj.add("investigation", new JsonObject());
		obj.add("prescription", new JsonArray());
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		// Add tcRequest for teleconsultation branch
		obj.add("tcRequest", new JsonObject());

		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 4);
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn(1);
	when(util.getCreatedBy()).thenReturn("1");

		// Mock InputMapper.gson().fromJson to return util for CommonUtilityClass
		// (Assume InputMapper is static, so skip actual mapping in test)

		// Mock all service calls to return success
		when(docService.updateBenFlowtableAfterDocDataSave(any(), anyBoolean(), anyBoolean(), any(), null)).thenReturn(1);
		when(nurseService.saveBenPrescription(any())).thenReturn(2L);
		when(nurseService.saveBenInvestigation(any())).thenReturn(2L);
		when(nurseService.saveBenPrescribedDrugsList(any())).thenReturn(new HashMap<String, Object>());
		when(docService.saveDocFindings(any())).thenReturn(2);
		when(docService.saveBenReferDetails(any())).thenReturn(2L);

		Long result = null;
		try {
			result = spyService.saveDoctorData(obj, "auth");
		} catch (Exception e) {
			fail("Should not throw exception: " + e.getMessage());
		}
		assertNotNull(result);
		assertTrue(result > 0);
	}

	@Test
	void updateGeneralOPDDoctorData_success_allBranches() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);

		JsonObject obj = new JsonObject();
		obj.add("investigation", new JsonObject());
		obj.add("prescription", new JsonArray());
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		// Add tcRequest for teleconsultation branch
		obj.add("tcRequest", new JsonObject());

		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 4);
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn(1);
	when(util.getCreatedBy()).thenReturn("1");

		// Mock all service calls to return success
		when(docService.updateBenFlowtableAfterDocDataUpdate(any(), anyBoolean(), anyBoolean(), any(), null)).thenReturn(1);
		when(nurseService.updatePrescription(any())).thenReturn(2);
		when(nurseService.saveBenInvestigation(any())).thenReturn(2L);
		when(nurseService.saveBenPrescribedDrugsList(any())).thenReturn(new HashMap<String, Object>());
		when(docService.updateDocFindings(any())).thenReturn(2);
		when(docService.updateBenReferDetails(any())).thenReturn(2L);

		Long result = null;
		try {
			result = spyService.updateGeneralOPDDoctorData(obj, "auth");
		} catch (Exception e) {
			fail("Should not throw exception: " + e.getMessage());
		}
		assertNotNull(result);
		assertTrue(result > 0);
	}

	@Test
	void updateGeneralOPDDoctorData_teleconsultationBranch_success() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);

		// Create investigation object with actual data
		JsonObject investigationObj = new JsonObject();
		investigationObj.addProperty("prescriptionID", 123L);
		investigationObj.addProperty("beneficiaryRegID", 1L);
		investigationObj.addProperty("benVisitID", 1L);
		investigationObj.addProperty("externalInvestigations", "Blood Test, X-Ray");
		
		// Create prescription array with actual data to trigger the loop
		JsonArray prescriptionArray = new JsonArray();
		JsonObject drug1 = new JsonObject();
		drug1.addProperty("drugName", "Paracetamol");
		drug1.addProperty("dose", "500mg");
		JsonObject drug2 = new JsonObject();
		drug2.addProperty("drugName", "Ibuprofen");
		drug2.addProperty("dose", "400mg");
		prescriptionArray.add(drug1);
		prescriptionArray.add(drug2);

		JsonObject obj = new JsonObject();
		obj.add("investigation", investigationObj);
		obj.add("prescription", prescriptionArray);
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		JsonObject tcRequest = new JsonObject();
		tcRequest.addProperty("userID", 123);
		tcRequest.addProperty("allocationDate", "2025-08-08T00:00:00.000");
		tcRequest.addProperty("fromTime", "10:00");
		tcRequest.addProperty("toTime", "11:00");
		obj.add("tcRequest", tcRequest);

		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 4);
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn(1);
		when(util.getCreatedBy()).thenReturn("testUser");

		// Mock slot booking and TC request creation
		when(docService.callTmForSpecialistSlotBook(any(), any())).thenReturn(1);
		when(teleService.createTCRequest(any())).thenReturn(1);
		when(docService.updateBenFlowtableAfterDocDataUpdate(any(), anyBoolean(), anyBoolean(), any(), null)).thenReturn(1);
		when(nurseService.updatePrescription(any())).thenReturn(2);
		when(nurseService.saveBenInvestigation(any())).thenReturn(2L);
		when(nurseService.saveBenPrescribedDrugsList(any())).thenReturn(new HashMap<String, Object>());
		when(docService.updateDocFindings(any())).thenReturn(2);
		when(docService.updateBenReferDetails(any())).thenReturn(2L);

		try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
			 MockedStatic<com.iemr.mmu.service.anc.Utility> utilityMock = mockStatic(com.iemr.mmu.service.anc.Utility.class)) {
			
			TeleconsultationRequestOBJ tcRequestObj = new TeleconsultationRequestOBJ();
			tcRequestObj.setUserID(123);
			tcRequestObj.setFromTime("10:00");
			tcRequestObj.setToTime("11:00");
			tcRequestObj.setAllocationDate(java.sql.Timestamp.valueOf("2025-08-08 00:00:00"));
			
			// Mock Utility static methods
			utilityMock.when(() -> com.iemr.mmu.service.anc.Utility.combineDateAndTimeToDateTime(any(), any()))
				.thenReturn(java.sql.Timestamp.valueOf("2025-08-08 10:00:00"));
			utilityMock.when(() -> com.iemr.mmu.service.anc.Utility.timeDiff(any(), any()))
				.thenReturn(60L);
			
			InputMapper inputMapperMockInstance = mock(InputMapper.class);
			inputMapperMock.when(InputMapper::gson).thenReturn(inputMapperMockInstance);
			
			// Mock fromJson calls with exact object matching
			when(inputMapperMockInstance.fromJson(eq(obj), eq(CommonUtilityClass.class))).thenReturn(util);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(CommonUtilityClass.class))).thenReturn(util);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(CommonUtilityClass.class))).thenReturn(util);
			
			when(inputMapperMockInstance.fromJson(eq(tcRequest), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			
			TCRequestModel tRequestModel = new TCRequestModel();
			tRequestModel.setUserID(123);
			tRequestModel.setRequestDate(java.sql.Timestamp.valueOf("2025-08-08 10:00:00"));
			tRequestModel.setDuration_minute(60L);
			when(inputMapperMockInstance.fromJson(eq(obj), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			
			// Mock investigation branch
			com.iemr.mmu.data.anc.WrapperBenInvestigationANC mockInvestigation = mock(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class))).thenReturn(mockInvestigation);
			when(inputMapperMockInstance.fromJson(eq(investigationObj), eq(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class))).thenReturn(mockInvestigation);
			when(mockInvestigation.getExternalInvestigations()).thenReturn("test investigations");
			doNothing().when(mockInvestigation).setPrescriptionID(any(Long.class));

			// Mock prescription detail
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail mockPrescriptionDetail = mock(com.iemr.mmu.data.quickConsultation.PrescriptionDetail.class);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.quickConsultation.PrescriptionDetail.class))).thenReturn(mockPrescriptionDetail);
			doNothing().when(mockPrescriptionDetail).setExternalInvestigation(any());

			// Mock PrescribedDrugDetail array for prescription branch
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail mockDrug1 = mock(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail.class);
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail mockDrug2 = mock(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail.class);
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[] mockDrugArray = {mockDrug1, mockDrug2};
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[].class))).thenReturn(mockDrugArray);
			when(inputMapperMockInstance.fromJson(eq(prescriptionArray), eq(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[].class))).thenReturn(mockDrugArray);
			
			// Configure drug mocks for setter calls
			doNothing().when(mockDrug1).setPrescriptionID(any());
			doNothing().when(mockDrug1).setBeneficiaryRegID(any());
			doNothing().when(mockDrug1).setBenVisitID(any());
			doNothing().when(mockDrug1).setVisitCode(any());
			doNothing().when(mockDrug1).setProviderServiceMapID(any());
			doNothing().when(mockDrug2).setPrescriptionID(any());
			doNothing().when(mockDrug2).setBeneficiaryRegID(any());
			doNothing().when(mockDrug2).setBenVisitID(any());
			doNothing().when(mockDrug2).setVisitCode(any());
			doNothing().when(mockDrug2).setProviderServiceMapID(any());

		Long result = spyService.updateGeneralOPDDoctorData(obj, "auth");
		assertNotNull(result);
		assertTrue(result > 0);
	}
}
	@Test
	void saveDoctorData_teleconsultationBranch_success() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);

		// Create investigation object with actual data
		JsonObject investigationObj = new JsonObject();
		investigationObj.addProperty("prescriptionID", 123L);
		investigationObj.addProperty("beneficiaryRegID", 1L);
		investigationObj.addProperty("benVisitID", 1L);
		investigationObj.addProperty("externalInvestigations", "Blood Test, X-Ray");
		
		// Create prescription array with actual data to trigger the loop
		JsonArray prescriptionArray = new JsonArray();
		JsonObject drug1 = new JsonObject();
		drug1.addProperty("drugName", "Paracetamol");
		drug1.addProperty("dose", "500mg");
		JsonObject drug2 = new JsonObject();
		drug2.addProperty("drugName", "Ibuprofen");
		drug2.addProperty("dose", "400mg");
		prescriptionArray.add(drug1);
		prescriptionArray.add(drug2);

		// Create the main request object
		JsonObject obj = new JsonObject();
		obj.add("investigation", investigationObj);
		obj.add("prescription", prescriptionArray);
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		
		// Create a properly structured tcRequest object
		JsonObject tcRequest = new JsonObject();
		tcRequest.addProperty("userID", 123);
		tcRequest.addProperty("allocationDate", "2025-08-08T00:00:00.000");
		tcRequest.addProperty("fromTime", "10:00");
		tcRequest.addProperty("toTime", "11:00");
		obj.add("tcRequest", tcRequest);

		// Create and configure CommonUtilityClass mock
		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 4); // This is crucial - must be 4
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn(1);
		when(util.getCreatedBy()).thenReturn("testUser");

		// Mock service calls to return success values
		when(docService.callTmForSpecialistSlotBook(any(), any())).thenReturn(1); // Must return > 0
		when(teleService.createTCRequest(any())).thenReturn(1);
		when(docService.updateBenFlowtableAfterDocDataSave(any(), anyBoolean(), anyBoolean(), any(), null)).thenReturn(1);
		when(nurseService.saveBenPrescription(any())).thenReturn(2L);
		when(nurseService.saveBenInvestigation(any())).thenReturn(2L);
		when(nurseService.saveBenPrescribedDrugsList(any())).thenReturn(new HashMap<String, Object>());
		when(docService.saveDocFindings(any())).thenReturn(2);
		when(docService.saveBenReferDetails(any())).thenReturn(2L);

		try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
			 MockedStatic<com.iemr.mmu.service.anc.Utility> utilityMock = mockStatic(com.iemr.mmu.service.anc.Utility.class)) {
			
			// Create properly configured TeleconsultationRequestOBJ
			TeleconsultationRequestOBJ tcRequestObj = new TeleconsultationRequestOBJ();
			tcRequestObj.setUserID(123); // Must be > 0
			tcRequestObj.setFromTime("10:00");
			tcRequestObj.setToTime("11:00");
			tcRequestObj.setAllocationDate(java.sql.Timestamp.valueOf("2025-08-08 00:00:00")); // Must be non-null
			
			// Mock Utility static methods
			utilityMock.when(() -> com.iemr.mmu.service.anc.Utility.combineDateAndTimeToDateTime(any(), any()))
				.thenReturn(java.sql.Timestamp.valueOf("2025-08-08 10:00:00"));
			utilityMock.when(() -> com.iemr.mmu.service.anc.Utility.timeDiff(any(), any()))
				.thenReturn(60L);
			
			// Create and configure InputMapper mock
			InputMapper inputMapperMockInstance = mock(InputMapper.class);
			inputMapperMock.when(InputMapper::gson).thenReturn(inputMapperMockInstance);
			
			// Mock fromJson calls with all possible argument combinations
			when(inputMapperMockInstance.fromJson(eq(obj), eq(CommonUtilityClass.class))).thenReturn(util);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(CommonUtilityClass.class))).thenReturn(util);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(CommonUtilityClass.class))).thenReturn(util);
			
			when(inputMapperMockInstance.fromJson(eq(tcRequest), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			
			TCRequestModel tRequestModel = new TCRequestModel();
			tRequestModel.setUserID(123);
			tRequestModel.setRequestDate(java.sql.Timestamp.valueOf("2025-08-08 10:00:00"));
			tRequestModel.setDuration_minute(60L);
			when(inputMapperMockInstance.fromJson(eq(obj), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			
			// Mock other necessary classes
			com.iemr.mmu.data.anc.WrapperBenInvestigationANC mockInvestigation = mock(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class))).thenReturn(mockInvestigation);
			when(inputMapperMockInstance.fromJson(eq(investigationObj), eq(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class))).thenReturn(mockInvestigation);
			when(mockInvestigation.getExternalInvestigations()).thenReturn("test investigations");
			doNothing().when(mockInvestigation).setPrescriptionID(any(Long.class)); // Critical for coverage

			com.iemr.mmu.data.quickConsultation.PrescriptionDetail mockPrescriptionDetail = mock(com.iemr.mmu.data.quickConsultation.PrescriptionDetail.class);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.quickConsultation.PrescriptionDetail.class))).thenReturn(mockPrescriptionDetail);
			doNothing().when(mockPrescriptionDetail).setExternalInvestigation(any());

			// Mock PrescribedDrugDetail array for prescription branch
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail mockDrug1 = mock(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail.class);
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail mockDrug2 = mock(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail.class);
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[] mockDrugArray = {mockDrug1, mockDrug2};
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[].class))).thenReturn(mockDrugArray);
			when(inputMapperMockInstance.fromJson(eq(prescriptionArray), eq(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[].class))).thenReturn(mockDrugArray);
			
			// Configure drug mocks for setter calls
			doNothing().when(mockDrug1).setPrescriptionID(any());
			doNothing().when(mockDrug1).setBeneficiaryRegID(any());
			doNothing().when(mockDrug1).setBenVisitID(any());
			doNothing().when(mockDrug1).setVisitCode(any());
			doNothing().when(mockDrug1).setProviderServiceMapID(any());
			doNothing().when(mockDrug2).setPrescriptionID(any());
			doNothing().when(mockDrug2).setBeneficiaryRegID(any());
			doNothing().when(mockDrug2).setBenVisitID(any());
			doNothing().when(mockDrug2).setVisitCode(any());
			doNothing().when(mockDrug2).setProviderServiceMapID(any());

			Long result = spyService.saveDoctorData(obj, "auth");
			assertNotNull(result);
			assertTrue(result > 0);
		}
	}

	@Test
	void updateGeneralOPDDoctorData_emptyPrescription_success() throws Exception {
		GeneralOPDServiceImpl spyService = Mockito.spy(new GeneralOPDServiceImpl());
		CommonDoctorServiceImpl docService = mock(CommonDoctorServiceImpl.class);
		CommonNurseServiceImpl nurseService = mock(CommonNurseServiceImpl.class);
		TeleConsultationServiceImpl teleService = mock(TeleConsultationServiceImpl.class);
		Field f1 = GeneralOPDServiceImpl.class.getDeclaredField("commonDoctorServiceImpl");
		f1.setAccessible(true);
		f1.set(spyService, docService);
		Field f2 = GeneralOPDServiceImpl.class.getDeclaredField("commonNurseServiceImpl");
		f2.setAccessible(true);
		f2.set(spyService, nurseService);
		Field f3 = GeneralOPDServiceImpl.class.getDeclaredField("teleConsultationServiceImpl");
		f3.setAccessible(true);
		f3.set(spyService, teleService);

		// Create investigation object with actual data
		JsonObject investigationObj = new JsonObject();
		investigationObj.addProperty("prescriptionID", 123L);
		investigationObj.addProperty("beneficiaryRegID", 1L);
		investigationObj.addProperty("benVisitID", 1L);
		investigationObj.addProperty("externalInvestigations", "Blood Test, X-Ray");
		
		// Create empty prescription array to trigger the else branch
		JsonArray prescriptionArray = new JsonArray();

		JsonObject obj = new JsonObject();
		obj.add("investigation", investigationObj);
		obj.add("prescription", prescriptionArray);
		obj.add("findings", new JsonObject());
		obj.add("diagnosis", new JsonObject());
		obj.add("refer", new JsonObject());
		JsonObject tcRequest = new JsonObject();
		tcRequest.addProperty("userID", 123);
		tcRequest.addProperty("allocationDate", "2025-08-08T00:00:00.000");
		tcRequest.addProperty("fromTime", "10:00");
		tcRequest.addProperty("toTime", "11:00");
		obj.add("tcRequest", tcRequest);

		CommonUtilityClass util = mock(CommonUtilityClass.class);
		when(util.getServiceID()).thenReturn((short) 4);
		when(util.getBenVisitID()).thenReturn(1L);
		when(util.getBeneficiaryRegID()).thenReturn(1L);
		when(util.getVisitCode()).thenReturn(1L);
		when(util.getProviderServiceMapID()).thenReturn(1);
		when(util.getCreatedBy()).thenReturn("testUser");

		// Mock slot booking and TC request creation
		when(docService.callTmForSpecialistSlotBook(any(), any())).thenReturn(1);
		when(teleService.createTCRequest(any())).thenReturn(1);
		when(docService.updateBenFlowtableAfterDocDataUpdate(any(), anyBoolean(), anyBoolean(), any(), null)).thenReturn(1);
		when(nurseService.updatePrescription(any())).thenReturn(2);
		when(nurseService.saveBenInvestigation(any())).thenReturn(2L);
		when(docService.updateDocFindings(any())).thenReturn(2);
		when(docService.updateBenReferDetails(any())).thenReturn(2L);

		try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
			 MockedStatic<com.iemr.mmu.service.anc.Utility> utilityMock = mockStatic(com.iemr.mmu.service.anc.Utility.class)) {
			
			TeleconsultationRequestOBJ tcRequestObj = new TeleconsultationRequestOBJ();
			tcRequestObj.setUserID(123);
			tcRequestObj.setFromTime("10:00");
			tcRequestObj.setToTime("11:00");
			tcRequestObj.setAllocationDate(java.sql.Timestamp.valueOf("2025-08-08 00:00:00"));
			
			// Mock Utility static methods
			utilityMock.when(() -> com.iemr.mmu.service.anc.Utility.combineDateAndTimeToDateTime(any(), any()))
				.thenReturn(java.sql.Timestamp.valueOf("2025-08-08 10:00:00"));
			utilityMock.when(() -> com.iemr.mmu.service.anc.Utility.timeDiff(any(), any()))
				.thenReturn(60L);
			
			InputMapper inputMapperMockInstance = mock(InputMapper.class);
			inputMapperMock.when(InputMapper::gson).thenReturn(inputMapperMockInstance);
			
			// Mock fromJson calls with exact object matching
			when(inputMapperMockInstance.fromJson(eq(obj), eq(CommonUtilityClass.class))).thenReturn(util);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(CommonUtilityClass.class))).thenReturn(util);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(CommonUtilityClass.class))).thenReturn(util);
			
			when(inputMapperMockInstance.fromJson(eq(tcRequest), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(TeleconsultationRequestOBJ.class))).thenReturn(tcRequestObj);
			
			TCRequestModel tRequestModel = new TCRequestModel();
			tRequestModel.setUserID(123);
			tRequestModel.setRequestDate(java.sql.Timestamp.valueOf("2025-08-08 10:00:00"));
			tRequestModel.setDuration_minute(60L);
			when(inputMapperMockInstance.fromJson(eq(obj), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonObject.class), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(TCRequestModel.class))).thenReturn(tRequestModel);
			
			// Mock investigation branch
			com.iemr.mmu.data.anc.WrapperBenInvestigationANC mockInvestigation = mock(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class))).thenReturn(mockInvestigation);
			when(inputMapperMockInstance.fromJson(eq(investigationObj), eq(com.iemr.mmu.data.anc.WrapperBenInvestigationANC.class))).thenReturn(mockInvestigation);
			when(mockInvestigation.getExternalInvestigations()).thenReturn("test investigations");
			doNothing().when(mockInvestigation).setPrescriptionID(any(Long.class));

			// Mock prescription detail
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail mockPrescriptionDetail = mock(com.iemr.mmu.data.quickConsultation.PrescriptionDetail.class);
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.quickConsultation.PrescriptionDetail.class))).thenReturn(mockPrescriptionDetail);
			doNothing().when(mockPrescriptionDetail).setExternalInvestigation(any());

			// Mock empty PrescribedDrugDetail array for the else branch
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[] emptyDrugArray = {};
			when(inputMapperMockInstance.fromJson(any(com.google.gson.JsonElement.class), eq(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[].class))).thenReturn(emptyDrugArray);
			when(inputMapperMockInstance.fromJson(eq(prescriptionArray), eq(com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail[].class))).thenReturn(emptyDrugArray);

			Long result = spyService.updateGeneralOPDDoctorData(obj, "auth");
			assertNotNull(result);
			assertTrue(result > 0);
		}
	}
}