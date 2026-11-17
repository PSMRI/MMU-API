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
package com.iemr.mmu.service.covid19;

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

import java.math.BigInteger;
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
import com.iemr.mmu.data.covid19.Covid19BenFeedback;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.covid19.Covid19BenFeedbackRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;

class Covid19ServiceImplTest {

	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	@Mock
	private CommonServiceImpl commonServiceImpl;
	@Mock
	private Covid19BenFeedbackRepo covid19BenFeedbackRepo;
	@Mock
	private PrescriptionDetailRepo prescriptionDetailRepo;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	@InjectMocks
	private Covid19ServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	private static String visitDetailsBlock(String covidDetails) {
		return "\"visitDetails\":{\"visitDetails\":{\"beneficiaryRegID\":1,\"visitReason\":\"New Chief Complaint\","
				+ "\"visitCategory\":\"COVID-19 Screening\"},\"covidDetails\":" + covidDetails + "}";
	}

	private static Covid19BenFeedback storedFeedback() {
		Covid19BenFeedback stored = new Covid19BenFeedback();
		stored.setcOVID19ID(BigInteger.valueOf(3));
		return stored;
	}

	@Nested
	@DisplayName("saving nurse data")
	class NurseSave {

		@Test
		void saveCovid19NurseData_ignoresARequestWithoutVisitDetails() throws Exception {
			assertNull(service.saveCovid19NurseData(null, "auth"));
			assertNull(service.saveCovid19NurseData(json("{}"), "auth"));
		}

		@Test
		void saveCovid19NurseData_skipsTheVisitWhenTheNurseAlreadySavedThisFlow() throws Exception {
			when(beneficiaryFlowStatusRepo.checkExistData(any(), any())).thenReturn(new BeneficiaryFlowStatus());

			assertEquals(0L, service.saveCovid19NurseData(json("{" + visitDetailsBlock("null") + "}"), "auth"));
			verify(commonNurseServiceImpl, never()).saveBeneficiaryVisitDetails(any());
		}

		@Test
		void saveCovid19NurseData_returnsZeroWhenTheVisitWasNotCreated() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(1);

			assertEquals(0L, service.saveCovid19NurseData(json("{" + visitDetailsBlock("null") + "}"), "auth"));
		}

		@Test
		void saveCovid19NurseData_savesTheScreeningAndAdvancesTheBeneficiaryFlow() throws Exception {
			stubVisitCreation();
			when(covid19BenFeedbackRepo.save(any())).thenReturn(storedFeedback());
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(1);

			String request = "{" + visitDetailsBlock(covidFeedback()) + ",\"historyDetails\":{},"
					+ "\"vitalDetails\":{}}";

			assertEquals(1L, service.saveCovid19NurseData(json(request), "auth"));
		}

		@Test
		void saveCovid19NurseData_treatsAnAbsentScreeningBlockAsAlreadyDone() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(1);

			String request = "{" + visitDetailsBlock("null") + ",\"historyDetails\":{},\"vitalDetails\":{}}";

			assertEquals(1L, service.saveCovid19NurseData(json(request), "auth"));
			verify(covid19BenFeedbackRepo, never()).save(any());
		}

		@Test
		void saveCovid19NurseData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(0);

			String request = "{" + visitDetailsBlock("null") + ",\"historyDetails\":{},\"vitalDetails\":{}}";

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.saveCovid19NurseData(json(request), "auth"));
			assertTrue(thrown.getMessage().contains("Beneficiary status update failed"));
		}

		@Test
		void saveCovid19NurseData_failsWhenASectionCouldNotBeSaved() throws Exception {
			stubVisitCreation();
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(null);

			String request = "{" + visitDetailsBlock("null") + ",\"historyDetails\":{},\"vitalDetails\":{}}";

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.saveCovid19NurseData(json(request), "auth"));
			assertEquals("Error occurred while saving data", thrown.getMessage());
		}

		private void stubVisitCreation() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(5L);
			when(commonNurseServiceImpl.generateVisitCode(anyLong(), any(), any())).thenReturn(6L);
		}

		private String covidFeedback() {
			return "{\"symptom\":[\"Fever\",\"Cough\"],\"contactStatus\":[\"Household\"],"
					+ "\"travelList\":[\"Domestic\"],\"recommendation\":[[\"Isolate\",\"Test\"]],"
					+ "\"suspectedStatusUI\":\"YES\"}";
		}

		@Test
		void saveCovid19NurseData_joinsEveryMultiValuedScreeningAnswerBeforeSaving() throws Exception {
			stubVisitCreation();
			when(covid19BenFeedbackRepo.save(any())).thenReturn(storedFeedback());
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(1);

			String request = "{" + visitDetailsBlock(covidFeedback()) + ",\"historyDetails\":{},"
					+ "\"vitalDetails\":{}}";
			service.saveCovid19NurseData(json(request), "auth");

			ArgumentCaptor<Covid19BenFeedback> saved = ArgumentCaptor.forClass(Covid19BenFeedback.class);
			verify(covid19BenFeedbackRepo).save(saved.capture());
			assertEquals("Fever||Cough", saved.getValue().getSymptoms_db());
			assertEquals("Household", saved.getValue().getcOVID19_contact_history());
			assertEquals("Domestic", saved.getValue().getTravelType());
			assertEquals("Isolate||Test", saved.getValue().getRecommendation_db());
			assertEquals(Boolean.TRUE, saved.getValue().getSuspectedStatus());
		}

		@Test
		void saveCovid19NurseData_recordsANegativeScreeningResult() throws Exception {
			stubVisitCreation();
			when(covid19BenFeedbackRepo.save(any())).thenReturn(storedFeedback());
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);
			when(commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any())).thenReturn(1);

			String request = "{" + visitDetailsBlock("{\"suspectedStatusUI\":\"NO\"}")
					+ ",\"historyDetails\":{},\"vitalDetails\":{}}";
			service.saveCovid19NurseData(json(request), "auth");

			ArgumentCaptor<Covid19BenFeedback> saved = ArgumentCaptor.forClass(Covid19BenFeedback.class);
			verify(covid19BenFeedbackRepo).save(saved.capture());
			assertEquals(Boolean.FALSE, saved.getValue().getSuspectedStatus());
		}

		@Test
		void saveBenVisitDetails_returnsNothingWhenTheVisitBlockIsMissing() throws Exception {
			assertTrue(service.saveBenVisitDetails(json("{}"), new CommonUtilityClass()).isEmpty());
			assertTrue(service.saveBenVisitDetails(null, new CommonUtilityClass()).isEmpty());
		}
	}

	@Nested
	@DisplayName("saving the individual nurse sections")
	class NurseSections {

		@Test
		void saveBenCovid19HistoryDetails_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenCovid19HistoryDetails(json("{}"), 1L, 2L));
			assertEquals(1L, service.saveBenCovid19HistoryDetails(null, 1L, 2L));
		}

		@Test
		void saveBenCovid19HistoryDetails_savesEverySectionThatWasSent() throws Exception {
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenComorbidConditions(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenMedicationHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveFemaleObstetricHistory(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenMenstrualHistory(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenFamilyHistory(any())).thenReturn(1L);
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

			assertEquals(1L, service.saveBenCovid19HistoryDetails(json(history), 1L, 2L));
		}

		@Test
		void saveBenCovid19HistoryDetails_reportsFailureWhenASectionCouldNotBeSaved() throws Exception {
			when(commonNurseServiceImpl.saveBenPastHistory(any())).thenReturn(0L);
			assertNull(service.saveBenCovid19HistoryDetails(json("{\"pastHistory\":{}}"), 1L, 2L));
		}

		@Test
		void saveBenCovid19VitalDetails_savesAnthropometryAndVitalsTogether() throws Exception {
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(4L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(5L);

			assertEquals(4L, service.saveBenCovid19VitalDetails(json("{}"), 1L, 2L));
		}

		@Test
		void saveBenCovid19VitalDetails_reportsFailureWhenTheVitalsCouldNotBeSaved() throws Exception {
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(4L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(null);

			assertNull(service.saveBenCovid19VitalDetails(json("{}"), 1L, 2L));
			assertNull(service.saveBenCovid19VitalDetails(null, 1L, 2L));
		}
	}

	@Nested
	@DisplayName("reading the nurse and doctor case sheets")
	class Reads {

		@Test
		void getBenVisitDetailsFrmNurseCovid19_splitsTheStoredScreeningAnswersBackIntoLists() throws Exception {
			Covid19BenFeedback stored = new Covid19BenFeedback();
			stored.setSymptoms_db("Fever||Cough");
			stored.setTravelType("Domestic||International");
			stored.setcOVID19_contact_history("Household");
			stored.setRecommendation_db("Isolate||Test");
			stored.setSuspectedStatus(true);
			when(covid19BenFeedbackRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(stored);

			String result = service.getBenVisitDetailsFrmNurseCovid19(1L, 2L);

			assertTrue(result.contains("covid19NurseVisitDetail"));
			assertEquals(2, stored.getSymptoms().length);
			assertEquals(2, stored.getTravelList().length);
			assertEquals(1, stored.getContactStatus().length);
			assertEquals(1, stored.getRecommendation().size());
			assertEquals("YES", stored.getSuspectedStatusUI());
		}

		@Test
		void getBenVisitDetailsFrmNurseCovid19_marksANegativeScreeningForTheUi() throws Exception {
			Covid19BenFeedback stored = new Covid19BenFeedback();
			stored.setSuspectedStatus(false);
			when(covid19BenFeedbackRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(stored);

			service.getBenVisitDetailsFrmNurseCovid19(1L, 2L);

			assertEquals("NO", stored.getSuspectedStatusUI());
		}

		@Test
		void getBenVisitDetailsFrmNurseCovid19_tolratesABeneficiaryWithNoScreeningOnRecord() throws Exception {
			when(covid19BenFeedbackRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(null);
			assertTrue(service.getBenVisitDetailsFrmNurseCovid19(1L, 2L).contains("covidDetails"));
		}

		@Test
		void getBenCovidNurseData_combinesTheScreeningVitalsAndHistory() {
			String result = service.getBenCovidNurseData(1L, 2L);

			assertTrue(result.contains("covidDetails"));
			assertTrue(result.contains("vitals"));
			assertTrue(result.contains("history"));
		}

		@Test
		void getBenCovid19HistoryDetails_gathersEveryHistorySection() {
			when(commonNurseServiceImpl.getPastHistoryData(1L, 2L))
					.thenReturn(new com.iemr.mmu.data.anc.BenMedHistory());

			assertTrue(service.getBenCovid19HistoryDetails(1L, 2L).contains("PastHistory"));
			verify(commonNurseServiceImpl).getFeedingHistory(1L, 2L);
		}

		@Test
		void getBeneficiaryVitalDetails_gathersAnthropometryAndVitals() {
			when(commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(1L, 2L)).thenReturn("a");
			when(commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(1L, 2L)).thenReturn("v");

			assertTrue(service.getBeneficiaryVitalDetails(1L, 2L).contains("benAnthropometryDetail"));
		}

		@Test
		void getBenCaseRecordFromDoctorCovid19_readsTheStoredDiagnosisWhenOneExists() throws Exception {
			PrescriptionDetail prescription = new PrescriptionDetail();
			prescription.setDiagnosisProvided("Covid-19");
			prescription.setPrescriptionID(7L);
			ArrayList<PrescriptionDetail> prescriptions = new ArrayList<>(
					Collections.singletonList(prescription));
			when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(prescriptions);
			when(labTechnicianServiceImpl.getLabResultDataForBen(1L, 2L)).thenReturn(new ArrayList<>());
			when(commonNurseServiceImpl.getGraphicalTrendData(1L, "ncdCare")).thenReturn(new HashMap<>());

			String result = service.getBenCaseRecordFromDoctorCovid19(1L, 2L);

			assertTrue(result.contains("Covid-19"));
			assertTrue(result.contains("GraphData"));
		}

		@Test
		void getBenCaseRecordFromDoctorCovid19_returnsAnEmptyDiagnosisWhenNoneWasRecorded() throws Exception {
			when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(new ArrayList<>());
			when(labTechnicianServiceImpl.getLabResultDataForBen(1L, 2L)).thenReturn(new ArrayList<>());
			when(commonNurseServiceImpl.getGraphicalTrendData(1L, "ncdCare")).thenReturn(new HashMap<>());

			assertTrue(service.getBenCaseRecordFromDoctorCovid19(1L, 2L).contains("diagnosis"));
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
	}

	@Nested
	@DisplayName("saving and updating doctor data")
	class DoctorData {

		private String doctorRequest(boolean isSpecialist) {
			return "{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"providerServiceMapID\":4,"
					+ "\"createdBy\":\"doctor\",\"isSpecialist\":" + isSpecialist + ",\"findings\":{},"
					+ "\"investigation\":{\"laboratoryList\":[{}]},"
					+ "\"diagnosis\":{\"doctorDiagnosis\":\"Covid-19\",\"specialistDiagnosis\":\"Isolate\","
					+ "\"prescriptionID\":7},\"prescription\":[{\"drugID\":1}],\"refer\":{}}";
		}

		private Map<String, Object> drugResult() {
			Map<String, Object> result = new HashMap<>();
			result.put("count", 1);
			result.put("prescribedDrugIDs", Collections.singletonList(9L));
			return result;
		}

		@Test
		void saveDoctorData_savesEverySectionAndAdvancesTheFlow() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.savePrescriptionDetailsCovid19(any(), any(), any(), any(), any(), any(), any(),
					any(), anyString())).thenReturn(7L);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult());
			when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.saveDoctorData(json(doctorRequest(false)), "auth"));
		}

		@Test
		void saveDoctorData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			when(commonNurseServiceImpl.savePrescriptionDetailsCovid19(any(), any(), any(), any(), any(), any(), any(),
					any(), any())).thenReturn(7L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.saveDoctorData(json("{\"investigation\":{}}"), "auth"));
		}

		@Test
		void saveDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.savePrescriptionDetailsCovid19(any(), any(), any(), any(), any(), any(), any(),
					any(), anyString())).thenReturn(7L);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult());
			when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class, () -> service.saveDoctorData(json(doctorRequest(false)), "auth"));
		}

		@Test
		void saveDoctorData_failsWhenASectionCouldNotBeSaved() throws Exception {
			when(commonDoctorServiceImpl.saveDocFindings(any())).thenReturn(0);
			when(commonNurseServiceImpl.savePrescriptionDetailsCovid19(any(), any(), any(), any(), any(), any(), any(),
					any(), anyString())).thenReturn(7L);

			assertThrows(RuntimeException.class, () -> service.saveDoctorData(json(doctorRequest(false)), "auth"));
		}

		@Test
		void updateCovid19DoctorData_recordsTheDoctorDiagnosisForANonSpecialist() throws Exception {
			stubSuccessfulDoctorUpdate();

			assertEquals(1L, service.updateCovid19DoctorData(json(doctorRequest(false)), "auth"));

			ArgumentCaptor<PrescriptionDetail> updated = ArgumentCaptor.forClass(PrescriptionDetail.class);
			verify(commonNurseServiceImpl).updatePrescription(updated.capture());
			assertEquals("Covid-19", updated.getValue().getDiagnosisProvided());
		}

		@Test
		void updateCovid19DoctorData_recordsTheSpecialistInstructionForASpecialist() throws Exception {
			stubSuccessfulDoctorUpdate();

			assertEquals(1L, service.updateCovid19DoctorData(json(doctorRequest(true)), "auth"));

			ArgumentCaptor<PrescriptionDetail> updated = ArgumentCaptor.forClass(PrescriptionDetail.class);
			verify(commonNurseServiceImpl).updatePrescription(updated.capture());
			assertEquals("Isolate", updated.getValue().getInstruction());
		}

		@Test
		void updateCovid19DoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			stubSuccessfulDoctorUpdate();
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(0);

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.updateCovid19DoctorData(json(doctorRequest(false)), "auth"));
			assertTrue(thrown.getMessage().contains("Beneficiary status update failed"));
		}

		@Test
		void updateCovid19DoctorData_failsWhenASectionCouldNotBeUpdated() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(0);

			assertThrows(RuntimeException.class,
					() -> service.updateCovid19DoctorData(json(doctorRequest(false)), "auth"));
		}

		private void stubSuccessfulDoctorUpdate() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult());
			when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);
		}
	}
}
