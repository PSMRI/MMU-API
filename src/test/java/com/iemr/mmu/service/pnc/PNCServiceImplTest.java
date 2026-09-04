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
package com.iemr.mmu.service.pnc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;

class PNCServiceImplTest {

	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Mock
	private PNCNurseServiceImpl pncNurseServiceImpl;
	@Mock
	private PNCDoctorServiceImpl pncDoctorServiceImpl;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	@Mock
	private TeleConsultationServiceImpl teleConsultationServiceImpl;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	@InjectMocks
	private PNCServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	/** The visit block every nurse save starts from. */
	private static String visitDetailsBlock() {
		return "\"visitDetails\":{\"visitDetails\":{\"beneficiaryRegID\":1,\"visitReason\":\"New Chief Complaint\","
				+ "\"visitCategory\":\"PNC\"},\"chiefComplaints\":[{\"chiefComplaintID\":1}]}";
	}

	@Nested
	@DisplayName("saving nurse data")
	class NurseSave {

		@Test
		void savePNCNurseData_ignoresARequestWithoutVisitDetails() throws Exception {
			assertNull(service.savePNCNurseData(null));
			assertNull(service.savePNCNurseData(json("{}")));
			assertNull(service.savePNCNurseData(json("{\"visitDetails\":null}")));
		}

		@Test
		void savePNCNurseData_skipsTheVisitWhenTheNurseAlreadySavedThisFlow() throws Exception {
			when(beneficiaryFlowStatusRepo.checkExistData(any(), any())).thenReturn(new BeneficiaryFlowStatus());

			assertEquals(0L, service.savePNCNurseData(json("{" + visitDetailsBlock() + "}")));
			verify(commonNurseServiceImpl, never()).saveBeneficiaryVisitDetails(any());
		}

		@Test
		void savePNCNurseData_returnsZeroWhenTheVisitWasNotCreated() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(1);

			assertEquals(0L, service.savePNCNurseData(json("{" + visitDetailsBlock() + "}")));
		}

		@Test
		void savePNCNurseData_savesEverySectionAndAdvancesTheBeneficiaryFlow() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(1L);
			when(pncNurseServiceImpl.saveBenPncCareDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.savePhyGeneralExamination(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(1);

			String request = "{" + visitDetailsBlock() + ",\"historyDetails\":{\"pastHistory\":{}},"
					+ "\"pNCDeatils\":{},\"vitalDetails\":{},"
					+ "\"examinationDetails\":{\"generalExamination\":{}}}";

			assertEquals(1L, service.savePNCNurseData(json(request)));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any());
		}

		@Test
		void savePNCNurseData_leavesTheFlowUntouchedWhenASectionFailsToSave() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(0L);

			String request = "{" + visitDetailsBlock() + ",\"historyDetails\":{\"pastHistory\":{}}}";

			assertNull(service.savePNCNurseData(json(request)));
			verify(commonBenStatusFlowServiceImpl, never()).updateBenFlowNurseAfterNurseActivity(any(), anyLong(),
					anyLong(), anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any());
		}

		private void stubVisitCreation() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(5L);
			when(commonNurseServiceImpl.generateVisitCode(anyLong(), any(), any())).thenReturn(6L);
		}

		@Test
		void saveBenVisitDetails_stampsTheVisitOntoEveryChiefComplaint() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(5L);
			when(commonNurseServiceImpl.generateVisitCode(anyLong(), any(), any())).thenReturn(6L);

			Map<String, Long> result = service.saveBenVisitDetails(
					json("{" + visitDetailsBlock() + "}").getAsJsonObject("visitDetails"),
					new com.iemr.mmu.data.nurse.CommonUtilityClass());

			assertEquals(5L, result.get("visitID"));
			assertEquals(6L, result.get("visitCode"));
			verify(commonNurseServiceImpl).saveBenChiefComplaints(any());
		}

		@Test
		void saveBenVisitDetails_returnsNothingWhenTheVisitBlockIsMissing() throws Exception {
			assertTrue(service.saveBenVisitDetails(json("{}"), new com.iemr.mmu.data.nurse.CommonUtilityClass())
					.isEmpty());
			assertTrue(service.saveBenVisitDetails(null, new com.iemr.mmu.data.nurse.CommonUtilityClass()).isEmpty());
		}
	}

	@Nested
	@DisplayName("saving the individual nurse sections")
	class NurseSections {

		@Test
		void saveBenPNCHistoryDetails_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenPNCHistoryDetails(json("{}"), 1L, 2L));
		}

		@Test
		void saveBenPNCHistoryDetails_savesEverySectionThatWasSent() throws Exception {
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenComorbidConditions(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenMedicationHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.savePersonalHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveAllergyHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenFamilyHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenMenstrualHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveFemaleObstetricHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveImmunizationHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveChildOptionalVaccineDetail(any())).thenReturn(1L);

			String history = "{\"pastHistory\":{},\"comorbidConditions\":{},"
					+ "\"medicationHistory\":{\"medicationHistoryList\":[{\"currentMedication\":\"x\"}]},\"personalHistory\":{},"
					+ "\"familyHistory\":{},\"menstrualHistory\":{},\"femaleObstetricHistory\":{},"
					+ "\"immunizationHistory\":{},\"childVaccineDetails\":{}}";

			assertEquals(1L, service.saveBenPNCHistoryDetails(json(history), 1L, 2L));
		}

		@Test
		void saveBenPNCHistoryDetails_treatsAnEmptyMedicationListAsAlreadyDone() throws Exception {
			String history = "{\"medicationHistory\":{\"medicationHistoryList\":[]}}";
			assertEquals(1L, service.saveBenPNCHistoryDetails(json(history), 1L, 2L));
		}

		@Test
		void saveBenPNCHistoryDetails_reportsFailureWhenASectionCouldNotBeSaved() throws Exception {
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(0L);
			assertNull(service.saveBenPNCHistoryDetails(json("{\"pastHistory\":{}}"), 1L, 2L));
		}

		@Test
		void saveBenPNCDetails_savesTheCareBlockWhenOneWasSent() throws Exception {
			when(pncNurseServiceImpl.saveBenPncCareDetails(any())).thenReturn(3L);
			assertEquals(3L, service.saveBenPNCDetails(json("{\"pNCDeatils\":{}}"), 1L, 2L));
		}

		@Test
		void saveBenPNCDetails_treatsAnAbsentCareBlockAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenPNCDetails(json("{}"), 1L, 2L));
		}

		@Test
		void saveBenPNCVitalDetails_savesAnthropometryAndVitalsTogether() throws Exception {
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(4L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(5L);

			assertEquals(4L, service.saveBenPNCVitalDetails(json("{}"), 1L, 2L));
		}

		@Test
		void saveBenPNCVitalDetails_reportsFailureWhenTheVitalsCouldNotBeSaved() throws Exception {
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(4L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(null);

			assertNull(service.saveBenPNCVitalDetails(json("{}"), 1L, 2L));
			assertNull(service.saveBenPNCVitalDetails(null, 1L, 2L));
		}

		@Test
		void saveBenExaminationDetails_savesEverySystemThatWasExamined() throws Exception {
			when(commonNurseServiceImpl.savePhyGeneralExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.savePhyHeadToToeExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveSysGastrointestinalExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveSysCardiovascularExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveSysRespiratoryExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveSysCentralNervousExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveSysMusculoskeletalSystemExamination(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveSysGenitourinarySystemExamination(any())).thenReturn(1L);

			String examination = "{\"generalExamination\":{},\"headToToeExamination\":{},"
					+ "\"gastroIntestinalExamination\":{},\"cardioVascularExamination\":{},"
					+ "\"respiratorySystemExamination\":{},\"centralNervousSystemExamination\":{},"
					+ "\"musculoskeletalSystemExamination\":{},\"genitoUrinarySystemExamination\":{}}";

			assertEquals(1L, service.saveBenExaminationDetails(json(examination), 1L, 2L));
		}

		@Test
		void saveBenExaminationDetails_treatsEveryAbsentSystemAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenExaminationDetails(json("{}"), 1L, 2L));
		}

		@Test
		void saveBenExaminationDetails_reportsFailureWhenASystemCouldNotBeSaved() throws Exception {
			when(commonNurseServiceImpl.savePhyGeneralExamination(any())).thenReturn(0L);
			assertNull(service.saveBenExaminationDetails(json("{\"generalExamination\":{}}"), 1L, 2L));
		}
	}

	@Nested
	@DisplayName("reading the nurse and doctor case sheets")
	class Reads {

		@Test
		void getBenVisitDetailsFrmNursePNC_gathersTheVisitAndItsComplaints() throws Exception {
			when(commonNurseServiceImpl.getCSVisitDetails(1L, 2L)).thenReturn(null);
			when(commonNurseServiceImpl.getBenChiefComplaints(1L, 2L)).thenReturn("[]");

			String result = service.getBenVisitDetailsFrmNursePNC(1L, 2L);

			assertTrue(result.contains("PNCNurseVisitDetail"));
			assertTrue(result.contains("BenChiefComplaints"));
		}

		@Test
		void getBenPNCDetailsFrmNursePNC_readsTheCareBlock() {
			when(pncNurseServiceImpl.getPNCCareDetails(1L, 2L)).thenReturn("care");
			assertTrue(service.getBenPNCDetailsFrmNursePNC(1L, 2L).contains("care"));
		}

		@Test
		void getBenHistoryDetails_gathersEveryHistorySection() {
			when(commonNurseServiceImpl.getPastHistoryData(1L, 2L))
					.thenReturn(new com.iemr.mmu.data.anc.BenMedHistory());

			assertTrue(service.getBenHistoryDetails(1L, 2L).contains("PastHistory"));
			verify(commonNurseServiceImpl).getFemaleObstetricHistory(1L, 2L);
			verify(commonNurseServiceImpl).getFeedingHistory(1L, 2L);
		}

		@Test
		void getBeneficiaryVitalDetails_gathersAnthropometryAndVitals() {
			when(commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(1L, 2L)).thenReturn("a");
			when(commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(1L, 2L)).thenReturn("v");

			String result = service.getBeneficiaryVitalDetails(1L, 2L);
			assertTrue(result.contains("benAnthropometryDetail"));
			assertTrue(result.contains("benPhysicalVitalDetail"));
		}

		@Test
		void getPNCExaminationDetailsData_gathersEveryExaminedSystem() {
			when(commonNurseServiceImpl.getGeneralExaminationData(1L, 2L))
					.thenReturn(new com.iemr.mmu.data.anc.PhyGeneralExamination());

			assertTrue(service.getPNCExaminationDetailsData(1L, 2L).contains("generalExamination"));
			verify(commonNurseServiceImpl).getGenitourinaryExamination(1L, 2L);
		}

		@Test
		void getBenPNCNurseData_combinesTheFourNurseSections() {
			String result = service.getBenPNCNurseData(1L, 2L);

			assertTrue(result.contains("pnc"));
			assertTrue(result.contains("history"));
			assertTrue(result.contains("vitals"));
			assertTrue(result.contains("examination"));
		}

		@Test
		void getBenCaseRecordFromDoctorPNC_gathersEveryDoctorSection() throws Exception {
			when(commonDoctorServiceImpl.getFindingsDetails(1L, 2L)).thenReturn("findings");
			when(pncDoctorServiceImpl.getPNCDiagnosisDetails(1L, 2L)).thenReturn("diagnosis");
			when(commonDoctorServiceImpl.getInvestigationDetails(1L, 2L)).thenReturn("investigation");
			when(commonDoctorServiceImpl.getPrescribedDrugs(1L, 2L)).thenReturn("prescription");
			when(commonDoctorServiceImpl.getReferralDetails(1L, 2L)).thenReturn("refer");
			when(labTechnicianServiceImpl.getLabResultDataForBen(1L, 2L)).thenReturn(new ArrayList<>());
			when(commonNurseServiceImpl.getGraphicalTrendData(1L, "pnc")).thenReturn(new HashMap<>());
			when(labTechnicianServiceImpl.getLast_3_ArchivedTestVisitList(1L, 2L)).thenReturn("[]");

			String result = service.getBenCaseRecordFromDoctorPNC(1L, 2L);

			assertTrue(result.contains("findings"));
			assertTrue(result.contains("LabReport"));
			assertTrue(result.contains("GraphData"));
		}
	}

	@Nested
	@DisplayName("updating nurse data")
	class NurseUpdates {

		@Test
		void updateBenHistoryDetails_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1, service.updateBenHistoryDetails(json("{}")));
		}

		@Test
		void updateBenHistoryDetails_updatesEverySectionThatWasSent() throws Exception {
			when(commonNurseServiceImpl.updateBenPastHistoryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateBenComorbidConditions(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateBenMedicationHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateBenPersonalHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateBenAllergicHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateBenFamilyHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateMenstrualHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePastObstetricHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateChildImmunizationDetail(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateChildOptionalVaccineDetail(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateChildFeedingHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePerinatalHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateChildDevelopmentHistory(any())).thenReturn(1);

			String history = "{\"pastHistory\":{},\"comorbidConditions\":{},\"medicationHistory\":{},"
					+ "\"personalHistory\":{},\"familyHistory\":{},\"menstrualHistory\":{},"
					+ "\"femaleObstetricHistory\":{},\"immunizationHistory\":{\"benChildVaccineDetails\":[{}]},"
					+ "\"childVaccineDetails\":{},\"feedingHistory\":{},\"perinatalHistroy\":{},"
					+ "\"developmentHistory\":{}}";

			assertEquals(1, service.updateBenHistoryDetails(json(history)));
		}

		@Test
		void updateBenHistoryDetails_reportsFailureWhenASectionCouldNotBeUpdated() throws Exception {
			when(commonNurseServiceImpl.updateBenPastHistoryDetails(any())).thenReturn(0);
			assertEquals(0, service.updateBenHistoryDetails(json("{\"pastHistory\":{}}")));
		}

		@Test
		void updateBenVitalDetails_updatesAnthropometryAndVitalsTogether() throws Exception {
			when(commonNurseServiceImpl.updateANCAnthropometryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(1);

			assertEquals(1, service.updateBenVitalDetails(json("{}")));
		}

		@Test
		void updateBenVitalDetails_reportsFailureWhenTheVitalsCouldNotBeUpdated() throws Exception {
			when(commonNurseServiceImpl.updateANCAnthropometryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(0);

			assertEquals(0, service.updateBenVitalDetails(json("{}")));
			assertEquals(1, service.updateBenVitalDetails(null));
		}

		@Test
		void updateBenExaminationDetails_updatesEverySystemThatWasSent() throws Exception {
			when(commonNurseServiceImpl.updatePhyGeneralExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePhyHeadToToeExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateSysGastrointestinalExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateSysCardiovascularExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateSysRespiratoryExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateSysCentralNervousExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateSysMusculoskeletalSystemExamination(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateSysGenitourinarySystemExamination(any())).thenReturn(1);

			String examination = "{\"generalExamination\":{},\"headToToeExamination\":{},"
					+ "\"gastroIntestinalExamination\":{},\"cardioVascularExamination\":{},"
					+ "\"respiratorySystemExamination\":{},\"centralNervousSystemExamination\":{},"
					+ "\"musculoskeletalSystemExamination\":{},\"genitoUrinarySystemExamination\":{}}";

			assertEquals(1, service.updateBenExaminationDetails(json(examination)));
		}

		@Test
		void updateBenExaminationDetails_treatsEveryAbsentSystemAsAlreadyDone() throws Exception {
			assertEquals(1, service.updateBenExaminationDetails(json("{}")));
		}

		@Test
		void updateBenExaminationDetails_reportsFailureWhenASystemCouldNotBeUpdated() throws Exception {
			when(commonNurseServiceImpl.updatePhyGeneralExamination(any())).thenReturn(0);
			assertEquals(0, service.updateBenExaminationDetails(json("{\"generalExamination\":{}}")));
		}

		@Test
		void updateBenPNCDetails_updatesTheCareBlockWhenOneWasSent() throws Exception {
			when(pncNurseServiceImpl.updateBenPNCCareDetails(any())).thenReturn(1);
			assertEquals(1, service.updateBenPNCDetails(json("{\"PNCDetails\":{}}")));
			assertEquals(1, service.updateBenPNCDetails(json("{}")));
		}
	}

	@Nested
	@DisplayName("saving and updating doctor data")
	class DoctorData {

		private String doctorRequest(String extra) {
			return "{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"providerServiceMapID\":4,"
					+ "\"createdBy\":\"doctor\",\"findings\":{},\"investigation\":{\"laboratoryList\":[{}]},"
					+ "\"diagnosis\":{},\"prescription\":[{\"drugID\":1}],\"refer\":{}" + extra + "}";
		}

		private void stubSuccessfulDoctorSave() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.savePrescriptionDetailsAndGetPrescriptionID(any(), any(), any(), any(), any(),
					any(), any(), any(), any())).thenReturn(7L);
			when(pncDoctorServiceImpl.saveBenPNCDiagnosis(any(), any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			Map<String, Object> drugResult = new HashMap<>();
			drugResult.put("count", 1);
			drugResult.put("prescribedDrugIDs", Collections.singletonList(9L));
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult);
			when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
		}

		@Test
		void savePNCDoctorData_savesEverySectionAndAdvancesTheFlow() throws Exception {
			stubSuccessfulDoctorSave();
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.savePNCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void savePNCDoctorData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.savePrescriptionDetailsAndGetPrescriptionID(any(), any(), any(), any(), any(),
					any(), any(), any(), any())).thenReturn(7L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.savePNCDoctorData(json("{\"findings\":{},\"investigation\":{}}"), "auth"));
		}

		@Test
		void savePNCDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			stubSuccessfulDoctorSave();
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class, () -> service.savePNCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void savePNCDoctorData_failsWhenASectionCouldNotBeSaved() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(0);
			when(commonNurseServiceImpl.savePrescriptionDetailsAndGetPrescriptionID(any(), any(), any(), any(), any(),
					any(), any(), any(), any())).thenReturn(7L);

			assertThrows(RuntimeException.class, () -> service.savePNCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void savePNCDoctorData_booksTheSpecialistSlotBeforeRaisingATeleconsultationRequest() throws Exception {
			stubSuccessfulDoctorSave();
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
			when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.savePNCDoctorData(json(doctorRequest(teleconsultationBlock())), "auth"));
			verify(teleConsultationServiceImpl).createTCRequest(any());
		}

		@Test
		void savePNCDoctorData_failsWhenTheSpecialistSlotCouldNotBeBooked() throws Exception {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(0);

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.savePNCDoctorData(json(doctorRequest(teleconsultationBlock())), "auth"));
			assertEquals("Error while booking slot.", thrown.getMessage());
		}

		private String teleconsultationBlock() {
			return ",\"serviceID\":4,\"tcRequest\":{\"userID\":5,\"allocationDate\":\"2024-01-01\","
					+ "\"fromTime\":\"10:00:00\",\"toTime\":\"10:30:00\"}";
		}

		@Test
		void updatePNCDoctorData_updatesEverySectionAndAdvancesTheFlow() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(pncDoctorServiceImpl.updateBenPNCDiagnosis(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			Map<String, Object> drugResult = new HashMap<>();
			drugResult.put("count", 1);
			drugResult.put("prescribedDrugIDs", Collections.singletonList(9L));
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult);
			when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.updatePNCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void updatePNCDoctorData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.updatePNCDoctorData(json("{\"investigation\":{\"laboratoryList\":[{}]}}"),
					"auth"));
		}

		@Test
		void updatePNCDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(pncDoctorServiceImpl.updateBenPNCDiagnosis(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			Map<String, Object> drugResult = new HashMap<>();
			drugResult.put("count", 1);
			drugResult.put("prescribedDrugIDs", new ArrayList<Long>());
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult);
			when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class, () -> service.updatePNCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void updatePNCDoctorData_failsWhenASectionCouldNotBeUpdated() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(0);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);

			assertThrows(RuntimeException.class, () -> service.updatePNCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void updatePNCDoctorData_booksTheSpecialistSlotBeforeRaisingATeleconsultationRequest() throws Exception {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
			when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(pncDoctorServiceImpl.updateBenPNCDiagnosis(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			Map<String, Object> drugResult = new HashMap<>();
			drugResult.put("count", 1);
			drugResult.put("prescribedDrugIDs", Collections.singletonList(9L));
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult);
			when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.updatePNCDoctorData(json(doctorRequest(teleconsultationBlock())), "auth"));
		}

		@Test
		void updatePNCDoctorData_failsWhenTheSpecialistSlotCouldNotBeBooked() throws Exception {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(0);

			assertThrows(RuntimeException.class,
					() -> service.updatePNCDoctorData(json(doctorRequest(teleconsultationBlock())), "auth"));
		}
	}
}
