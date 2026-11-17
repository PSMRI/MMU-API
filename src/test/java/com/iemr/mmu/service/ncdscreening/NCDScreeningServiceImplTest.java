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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.ncdScreening.IDRSData;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;
import com.iemr.mmu.repo.nurse.ncdscreening.IDRSDataRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;

class NCDScreeningServiceImplTest {

	@Mock
	private NCDScreeningNurseServiceImpl ncdScreeningNurseServiceImpl;
	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Mock
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	@Mock
	private CommonServiceImpl commonServiceImpl;
	@Mock
	private TeleConsultationServiceImpl teleConsultationServiceImpl;
	@Mock
	private NCDSCreeningDoctorServiceImpl ncdSCreeningDoctorServiceImpl;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Mock
	private BenVisitDetailRepo benVisitDetailRepo;
	@Mock
	private IDRSDataRepo iDrsDataRepo;
	@Mock
	private PrescriptionDetailRepo prescriptionDetailRepo;

	@InjectMocks
	private NCDScreeningServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	private static String visitDetailsBlock() {
		return "\"visitDetails\":{\"visitDetails\":{\"beneficiaryRegID\":1,\"visitReason\":\"Screening\","
				+ "\"visitCategory\":\"NCD screening\"},\"chiefComplaints\":[{\"chiefComplaintID\":1}]}";
	}

	@Nested
	@DisplayName("saving nurse data")
	class NurseSave {

		@Test
		void saveNCDScreeningNurseData_rejectsARequestWithoutVisitDetails() {
			assertThrows(Exception.class, () -> service.saveNCDScreeningNurseData(json("{}"), "auth"));
			assertThrows(Exception.class, () -> service.saveNCDScreeningNurseData(null, "auth"));
		}

		@Test
		void saveNCDScreeningNurseData_skipsTheVisitWhenTheNurseAlreadySavedThisFlow() throws Exception {
			when(beneficiaryFlowStatusRepo.checkExistData(any(), any())).thenReturn(new BeneficiaryFlowStatus());

			assertEquals(0L, service.saveNCDScreeningNurseData(json("{" + visitDetailsBlock() + "}"), "auth"));
			verify(commonNurseServiceImpl, never()).saveBeneficiaryVisitDetails(any());
		}

		@Test
		void saveNCDScreeningNurseData_returnsZeroWhenTheVisitWasNotCreated() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(1);

			assertEquals(0L, service.saveNCDScreeningNurseData(json("{" + visitDetailsBlock() + "}"), "auth"));
		}

		@Test
		void saveNCDScreeningNurseData_savesEverySectionAndAdvancesTheBeneficiaryFlow() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveIDRS(any())).thenReturn(1L);
			when(commonNurseServiceImpl.savePhysicalActivity(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(1);

			String request = "{" + visitDetailsBlock()
					+ ",\"historyDetails\":{\"physicalActivityHistory\":{\"activityType\":\"Walking\"}},"
					+ "\"vitalDetails\":{},\"idrsDetails\":{}}";

			assertEquals(1L, service.saveNCDScreeningNurseData(json(request), "auth"));
		}

		@Test
		void saveNCDScreeningNurseData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(0);

			String request = "{" + visitDetailsBlock() + ",\"historyDetails\":{},\"vitalDetails\":{},"
					+ "\"idrsDetails\":{}}";

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.saveNCDScreeningNurseData(json(request), "auth"));
			assertTrue(thrown.getMessage().contains("Beneficiary status update failed"));
		}

		@Test
		void saveNCDScreeningNurseData_failsWhenASectionCouldNotBeSaved() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(null);

			String request = "{" + visitDetailsBlock() + ",\"historyDetails\":{},\"vitalDetails\":{},"
					+ "\"idrsDetails\":{}}";

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.saveNCDScreeningNurseData(json(request), "auth"));
			assertEquals("Error occurred while saving data", thrown.getMessage());
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
					json("{" + visitDetailsBlock() + "}").getAsJsonObject("visitDetails"), new CommonUtilityClass());

			assertEquals(5L, result.get("visitID"));
			verify(commonNurseServiceImpl).saveBenChiefComplaints(any());
		}

		@Test
		void saveBenVisitDetails_returnsNothingWhenTheVisitBlockIsMissing() throws Exception {
			assertTrue(service.saveBenVisitDetails(json("{}"), new CommonUtilityClass()).isEmpty());
			assertTrue(service.saveBenVisitDetails(null, new CommonUtilityClass()).isEmpty());
		}
	}

	@Nested
	@DisplayName("saving a teleconsultation referral")
	class TeleconsultationReferral {

		private String tmReferredRequest(String prescription) {
			return "{\"isTMCDone\":true,\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,"
					+ "\"providerServiceMapID\":4,\"prescription\":" + prescription + ",\"refer\":{}}";
		}

		@Test
		void saveNCDScreeningNurseData_storesTheReferralAndAdvancesTheFlow() throws Exception {
			when(commonDoctorServiceImpl.saveBenReferDetailsTMreferred(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowtableAfterNurseSaveForTMReferred(any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.saveNCDScreeningNurseData(json(tmReferredRequest("[]")), "auth"));
		}

		@Test
		void saveNCDScreeningNurseData_storesEveryPrescribedDrugAgainstTheReferralPrescription() throws Exception {
			when(prescriptionDetailRepo.getPrescriptionID(3L)).thenReturn(7L);
			Map<String, Object> drugResult = new HashMap<>();
			drugResult.put("count", 1);
			drugResult.put("prescribedDrugIDs", Collections.singletonList(9L));
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult);
			when(commonDoctorServiceImpl.saveBenReferDetailsTMreferred(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowtableAfterNurseSaveForTMReferred(any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L,
					service.saveNCDScreeningNurseData(json(tmReferredRequest("[{\"drugID\":1}]")), "auth"));
		}

		@Test
		void saveNCDScreeningNurseData_failsWhenTheReferralFlowCouldNotBeAdvanced() throws Exception {
			when(commonDoctorServiceImpl.saveBenReferDetailsTMreferred(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowtableAfterNurseSaveForTMReferred(any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class,
					() -> service.saveNCDScreeningNurseData(json(tmReferredRequest("[]")), "auth"));
		}
	}

	@Nested
	@DisplayName("saving the individual nurse sections")
	class NurseSections {

		@Test
		void saveBenNCDCareHistoryDetails_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenNCDCareHistoryDetails(json("{}"), 1L, 2L));
			assertEquals(1L, service.saveBenNCDCareHistoryDetails(null, 1L, 2L));
		}

		@Test
		void saveBenNCDCareHistoryDetails_savesEverySectionThatWasSent() throws Exception {
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenComorbidConditions(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenMedicationHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveFemaleObstetricHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenMenstrualHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenFamilyHistoryNCDScreening(any())).thenReturn(1L);
			when(commonNurseServiceImpl.savePersonalHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveAllergyHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveChildOptionalVaccineDetail(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveImmunizationHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveChildDevelopmentHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveChildFeedingHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.savePerinatalHistory(any())).thenReturn(1L);

			String history = "{\"pastHistory\":{},\"comorbidConditions\":{},"
					+ "\"medicationHistory\":{\"medicationHistoryList\":[{\"currentMedication\":\"x\"}]},"
					+ "\"femaleObstetricHistory\":{},\"menstrualHistory\":{},\"familyHistory\":{},"
					+ "\"personalHistory\":{},\"childVaccineDetails\":{},\"immunizationHistory\":{},"
					+ "\"developmentHistory\":{},\"feedingHistory\":{},\"perinatalHistroy\":{}}";

			assertEquals(1L, service.saveBenNCDCareHistoryDetails(json(history), 1L, 2L));
		}

		@Test
		void saveBenNCDCareHistoryDetails_reportsFailureWhenASectionCouldNotBeSaved() throws Exception {
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(0L);
			assertNull(service.saveBenNCDCareHistoryDetails(json("{\"pastHistory\":{}}"), 1L, 2L));
		}

		@Test
		void saveidrsDetails_joinsEveryAnsweredQuestionOntoOneRow() throws Exception {
			when(commonNurseServiceImpl.saveIDRS(any())).thenReturn(1L);

			String idrs = "{\"questionArray\":[{\"idrsQuestionID\":1,\"question\":\"Q1\",\"answer\":\"Yes\","
					+ "\"diseaseQuestionType\":\"Diabetes\"},{\"idrsQuestionID\":2,\"question\":\"Q2\","
					+ "\"answer\":\"No\",\"diseaseQuestionType\":\"Hypertension\"}],"
					+ "\"suspectArray\":[\"Diabetes\",\"Hypertension\"],\"confirmArray\":[\"Diabetes\"]}";

			assertEquals(1L, service.saveidrsDetails(json(idrs), 1L, 2L));

			ArgumentCaptor<IDRSData> saved = ArgumentCaptor.forClass(IDRSData.class);
			verify(commonNurseServiceImpl).saveIDRS(saved.capture());
			assertEquals("1||2", saved.getValue().getQuestionIds());
			assertEquals("Q1||Q2", saved.getValue().getQuestion());
			assertEquals("Yes||No", saved.getValue().getAnswer());
			assertEquals("Diabetes||Hypertension", saved.getValue().getDiseaseQuestionType());
			assertEquals("Diabetes,Hypertension", saved.getValue().getSuspectedDisease());
			assertEquals("Diabetes", saved.getValue().getConfirmedDisease());
		}

		@Test
		void saveidrsDetails_storesTheScreeningEvenWhenNoQuestionWasAnswered() throws Exception {
			when(commonNurseServiceImpl.saveIDRS(any())).thenReturn(1L);

			String idrs = "{\"suspectArray\":[\"Diabetes\"],\"confirmArray\":[\"Diabetes\"]}";

			assertEquals(1L, service.saveidrsDetails(json(idrs), 1L, 2L));
			assertNull(service.saveidrsDetails(null, 1L, 2L));
		}

		@Test
		void savePhysicalActivityDetails_storesTheActivityWhenOneWasReported() throws Exception {
			when(commonNurseServiceImpl.savePhysicalActivity(any())).thenReturn(1L);

			assertEquals(1L, service.savePhysicalActivityDetails(json("{\"activityType\":\"Walking\"}"), 1L, 2L));
			assertNull(service.savePhysicalActivityDetails(json("{}"), 1L, 2L));
			assertNull(service.savePhysicalActivityDetails(null, 1L, 2L));
		}

		@Test
		void saveBenNCDCareVitalDetails_savesAnthropometryAndVitalsTogether() throws Exception {
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(4L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(5L);

			assertEquals(4L, service.saveBenNCDCareVitalDetails(json("{}"), 1L, 2L));

			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(null);
			assertNull(service.saveBenNCDCareVitalDetails(json("{}"), 1L, 2L));
			assertNull(service.saveBenNCDCareVitalDetails(null, 1L, 2L));
		}

		@Test
		void saveNCDScreeningVitalDetails_readsTheVitalsOutOfTheScreeningBlock() throws Exception {
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(4L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(5L);

			assertEquals(4L, service.saveNCDScreeningVitalDetails(json("{\"ncdScreeningDetails\":{}}"), 1L, 2L));

			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(null);
			assertNull(service.saveNCDScreeningVitalDetails(json("{\"ncdScreeningDetails\":{}}"), 1L, 2L));
		}
	}

	@Nested
	@DisplayName("updating nurse data")
	class NurseUpdates {

		@Test
		void updateNurseNCDScreeningDetails_storesTheAttachedFilesAndAdvancesTheFlow() throws Exception {
			when(ncdScreeningNurseServiceImpl.updateNCDScreeningDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCAnthropometryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(1);

			String request = "{\"beneficiaryRegID\":1,\"visitCode\":2,\"benFlowID\":3,"
					+ "\"nextScreeningDate\":\"2024-01-01T10:00:00Z\",\"isScreeningComplete\":true,"
					+ "\"fileIDs\":[\"a\",\"b\"]}";

			assertEquals(1, service.updateNurseNCDScreeningDetails(json(request)));
			verify(benVisitDetailRepo).updateFileID("a,b,", 1L, 2L);
			verify(commonBenStatusFlowServiceImpl).updateBenFlowNurseAfterNurseUpdateNCD_Screening(3L, 1L, (short) 9);
		}

		@Test
		void updateNurseNCDScreeningDetails_marksAnIncompleteScreeningAsStillWithTheNurse() throws Exception {
			when(ncdScreeningNurseServiceImpl.updateNCDScreeningDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCAnthropometryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(1);

			assertEquals(1, service.updateNurseNCDScreeningDetails(
					json("{\"beneficiaryRegID\":1,\"benFlowID\":3,\"isScreeningComplete\":false}")));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowNurseAfterNurseUpdateNCD_Screening(3L, 1L,
					(short) 100);
		}

		@Test
		void updateNurseNCDScreeningDetails_reportsNoResultWhenAnUpdateCouldNotBeApplied() throws Exception {
			when(ncdScreeningNurseServiceImpl.updateNCDScreeningDetails(any())).thenReturn(null);
			when(commonNurseServiceImpl.updateANCAnthropometryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(1);

			assertNull(service.updateNurseNCDScreeningDetails(json("{\"beneficiaryRegID\":1}")));
		}

		@Test
		void updateBenVitalDetails_updatesAnthropometryAndVitalsTogether() throws Exception {
			when(commonNurseServiceImpl.updateANCAnthropometryDetails(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(1);

			assertEquals(1, service.updateBenVitalDetails(json("{}")));

			when(commonNurseServiceImpl.updateANCPhysicalVitalDetails(any())).thenReturn(0);
			assertEquals(0, service.updateBenVitalDetails(json("{}")));
			assertEquals(1, service.updateBenVitalDetails(null));
		}

		@Test
		void UpdateNCDScreeningHistory_updatesTheFamilyAndActivityHistoryTogether() throws Exception {
			when(commonNurseServiceImpl.updateBenFamilyHistoryNCDScreening(any())).thenReturn(1);
			when(commonNurseServiceImpl.updateBenPhysicalActivityHistoryNCDScreening(any())).thenReturn(1);

			String history = "{\"familyHistory\":{},\"physicalActivityHistory\":{},\"personalHistory\":{}}";

			assertEquals(1, service.UpdateNCDScreeningHistory(json(history)));
			verify(commonNurseServiceImpl).updateBenPersonalHistory(any());
			verify(commonNurseServiceImpl).updateBenAllergicHistory(any());
		}

		@Test
		void UpdateNCDScreeningHistory_reportsNoUpdateWhenNoHistoryWasSent() throws Exception {
			assertEquals(0, service.UpdateNCDScreeningHistory(json("{}")));
			assertEquals(0, service.UpdateNCDScreeningHistory(null));
		}

		@Test
		void UpdateIDRSScreen_joinsEveryAnsweredQuestionOntoOneRow() throws Exception {
			when(commonNurseServiceImpl.saveIDRS(any())).thenReturn(1L);

			String idrs = "{\"idrsDetails\":{\"questionArray\":[{\"id\":5,\"idrsQuestionID\":1,\"question\":\"Q1\","
					+ "\"answer\":\"Yes\",\"diseaseQuestionType\":\"Diabetes\"}],"
					+ "\"suspectArray\":[\"Diabetes\"],\"confirmArray\":[\"Diabetes\"]}}";

			assertEquals(1L, service.UpdateIDRSScreen(json(idrs)));

			ArgumentCaptor<IDRSData> saved = ArgumentCaptor.forClass(IDRSData.class);
			verify(commonNurseServiceImpl).saveIDRS(saved.capture());
			assertEquals(5L, saved.getValue().getId());
			assertEquals("1", saved.getValue().getQuestionIds());
		}

		@Test
		void UpdateIDRSScreen_updatesTheSuspectedDiseasesAndScoreWhenNoQuestionWasReanswered() throws Exception {
			when(iDrsDataRepo.updateSuspectedDiseases(1L, 2L, "Diabetes")).thenReturn(1);
			when(iDrsDataRepo.updateIdrsScore(1L, 2L, 30)).thenReturn(1);

			String idrs = "{\"idrsDetails\":{\"beneficiaryRegID\":1,\"visitCode\":2,"
					+ "\"suspectArray\":[\"Diabetes\"],\"idrsScore\":30}}";

			assertEquals(1L, service.UpdateIDRSScreen(json(idrs)));
		}

		@Test
		void UpdateIDRSScreen_doesNothingWithoutAScreeningBlock() throws Exception {
			assertNull(service.UpdateIDRSScreen(json("{}")));
			assertNull(service.UpdateIDRSScreen(null));
		}
	}

	@Nested
	@DisplayName("reading the nurse and doctor case sheets")
	class Reads {

		@Test
		void getNCDScreeningDetails_gathersTheScreeningAnthropometryAndVitals() {
			when(ncdScreeningNurseServiceImpl.getNCDScreeningDetails(1L, 2L)).thenReturn("screening");
			when(commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(1L, 2L)).thenReturn("anthro");
			when(commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(1L, 2L)).thenReturn("vitals");

			assertTrue(service.getNCDScreeningDetails(1L, 2L).contains("ncdScreeningDetails"));
		}

		@Test
		void getNCDScreeningDetails_returnsNothingWhenASectionIsMissing() {
			when(ncdScreeningNurseServiceImpl.getNCDScreeningDetails(1L, 2L)).thenReturn(null);
			assertEquals("{}", service.getNCDScreeningDetails(1L, 2L));
		}

		@Test
		void getNcdScreeningVisitCnt_reportsTheNextVisitNumber() {
			when(beneficiaryFlowStatusRepo.getNcdScreeningVisitCount(1L)).thenReturn(3L);
			assertTrue(service.getNcdScreeningVisitCnt(1L).contains("4"));
		}

		@Test
		void getBenVisitDetailsFrmNurseNCDScreening_gathersTheVisitAndItsComplaints() throws Exception {
			when(commonNurseServiceImpl.getCSVisitDetails(1L, 2L)).thenReturn(null);
			when(commonNurseServiceImpl.getBenChiefComplaints(1L, 2L)).thenReturn("[]");

			assertTrue(service.getBenVisitDetailsFrmNurseNCDScreening(1L, 2L)
					.contains("NCDScreeningNurseVisitDetail"));
		}

		@Test
		void getBenHistoryDetails_gathersTheFamilyActivityAndPersonalHistory() {
			when(commonNurseServiceImpl.getFamilyHistoryDetail(1L, 2L))
					.thenReturn(new com.iemr.mmu.data.anc.BenFamilyHistory());

			assertTrue(service.getBenHistoryDetails(1L, 2L).contains("FamilyHistory"));
			verify(commonNurseServiceImpl).getPhysicalActivityType(1L, 2L);
			verify(commonNurseServiceImpl).getPersonalHistory(1L, 2L);
		}

		@Test
		void getBenIdrsDetailsFrmNurse_readsTheStoredScreening() {
			when(commonNurseServiceImpl.getBeneficiaryIdrsDetails(1L, 2L)).thenReturn(new IDRSData());
			assertTrue(service.getBenIdrsDetailsFrmNurse(1L, 2L).contains("IDRSDetail"));
		}

		@Test
		void getBeneficiaryVitalDetails_gathersAnthropometryAndVitals() {
			when(commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(1L, 2L)).thenReturn("a");
			when(commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(1L, 2L)).thenReturn("v");

			assertTrue(service.getBeneficiaryVitalDetails(1L, 2L).contains("benAnthropometryDetail"));
		}

		@Test
		void getBenNCDScreeningNurseData_combinesTheVitalsHistoryAndScreening() {
			String result = service.getBenNCDScreeningNurseData(1L, 2L);

			assertTrue(result.contains("vitals"));
			assertTrue(result.contains("history"));
			assertTrue(result.contains("idrs"));
		}

		@Test
		void getBenCaseRecordFromDoctorNCDScreening_gathersEveryDoctorSection() throws Exception {
			when(commonDoctorServiceImpl.getFindingsDetails(1L, 2L)).thenReturn("findings");
			when(ncdSCreeningDoctorServiceImpl.getNCDDiagnosisData(1L, 2L)).thenReturn("diagnosis");
			when(commonDoctorServiceImpl.getInvestigationDetails(1L, 2L)).thenReturn("investigation");
			when(commonDoctorServiceImpl.getPrescribedDrugs(1L, 2L)).thenReturn("prescription");
			when(commonDoctorServiceImpl.getReferralDetails(1L, 2L)).thenReturn("refer");
			when(labTechnicianServiceImpl.getLabResultDataForBen(1L, 2L)).thenReturn(new ArrayList<>());
			when(commonNurseServiceImpl.getGraphicalTrendData(1L, "ncdCare")).thenReturn(new HashMap<>());
			when(labTechnicianServiceImpl.getLast_3_ArchivedTestVisitList(1L, 2L)).thenReturn("[]");

			assertTrue(service.getBenCaseRecordFromDoctorNCDScreening(1L, 2L).contains("findings"));
		}
	}

	@Nested
	@DisplayName("saving doctor data")
	class DoctorData {

		private String doctorRequest(String extra) {
			return "{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"providerServiceMapID\":4,"
					+ "\"createdBy\":\"doctor\",\"findings\":{},\"investigation\":{\"laboratoryList\":[{}]},"
					+ "\"diagnosis\":{},\"prescription\":[{\"drugID\":1}],\"refer\":{}" + extra + "}";
		}

		private Map<String, Object> drugResult() {
			Map<String, Object> result = new HashMap<>();
			result.put("count", 1);
			result.put("prescribedDrugIDs", Collections.singletonList(9L));
			return result;
		}

		private void stubSuccessfulDoctorSave() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenPrescription(any())).thenReturn(7L);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult());
			when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);
		}

		@Test
		void saveDoctorData_savesEverySectionAndAdvancesTheFlow() throws Exception {
			stubSuccessfulDoctorSave();

			assertEquals(1L, service.saveDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void saveDoctorData_storesTheFilesAttachedToTheVisit() throws Exception {
			stubSuccessfulDoctorSave();

			String request = "{\"beneficiaryRegID\":1,\"visitCode\":3,\"findings\":{},"
					+ "\"investigation\":{\"laboratoryList\":[{}]},\"diagnosis\":{},"
					+ "\"prescription\":[{\"drugID\":1}],\"refer\":{},"
					+ "\"visitDetails\":{\"visitDetails\":{\"fileIDs\":[\"a\",\"b\"]}}}";

			assertEquals(1L, service.saveDoctorData(json(request), "auth"));
			verify(benVisitDetailRepo).updateFileID(anyString(), any(), any());
		}

		@Test
		void saveDoctorData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			when(commonNurseServiceImpl.saveBenPrescription(any())).thenReturn(7L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.saveDoctorData(json("{\"investigation\":{}}"), "auth"));
		}

		@Test
		void saveDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			stubSuccessfulDoctorSave();
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class, () -> service.saveDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void saveDoctorData_failsWhenASectionCouldNotBeSaved() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBenPrescription(any())).thenReturn(7L);

			assertThrows(RuntimeException.class, () -> service.saveDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void saveDoctorData_booksTheSpecialistSlotBeforeRaisingATeleconsultationRequest() throws Exception {
			stubSuccessfulDoctorSave();
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
			when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);

			String extra = ",\"serviceID\":4,\"tcRequest\":{\"userID\":5,\"allocationDate\":\"2024-01-01\","
					+ "\"fromTime\":\"10:00:00\",\"toTime\":\"10:30:00\"}";

			assertEquals(1L, service.saveDoctorData(json(doctorRequest(extra)), "auth"));
			verify(teleConsultationServiceImpl).createTCRequest(any());
		}

		@Test
		void saveDoctorData_failsWhenTheSpecialistSlotCouldNotBeBooked() throws Exception {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(0);

			String extra = ",\"serviceID\":4,\"tcRequest\":{\"userID\":5,\"allocationDate\":\"2024-01-01\","
					+ "\"fromTime\":\"10:00:00\",\"toTime\":\"10:30:00\"}";

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.saveDoctorData(json(doctorRequest(extra)), "auth"));
			assertEquals("Error while booking slot.", thrown.getMessage());
		}
	}
}
