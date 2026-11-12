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
package com.iemr.mmu.service.cancerScreening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
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
import com.iemr.mmu.data.doctor.CancerDiagnosis;
import com.iemr.mmu.data.nurse.BenCancerVitalDetail;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenData;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;

class CSServiceImplTest {

	@Mock
	private CSNurseServiceImpl cSNurseServiceImpl;
	@Mock
	private CSDoctorServiceImpl cSDoctorServiceImpl;
	@Mock
	private CSOncologistServiceImpl csOncologistServiceImpl;
	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;
	@Mock
	private CSCarestreamServiceImpl cSCarestreamServiceImpl;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private TeleConsultationServiceImpl teleConsultationServiceImpl;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Mock
	private RegistrarRepoBenData registrarRepoBenData;

	@InjectMocks
	private CSServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	private static String visitDetailsBlock() {
		return "\"visitDetails\":{\"beneficiaryRegID\":1,\"visitReason\":\"Screening\","
				+ "\"visitCategory\":\"Cancer Screening\"}";
	}

	@Nested
	@DisplayName("saving nurse data")
	class NurseSave {

		@Test
		void saveCancerScreeningNurseData_ignoresARequestWithoutVisitDetails() throws Exception {
			assertNull(service.saveCancerScreeningNurseData(null, "auth"));
			assertNull(service.saveCancerScreeningNurseData(json("{}"), "auth"));
		}

		@Test
		void saveCancerScreeningNurseData_skipsTheVisitWhenTheNurseAlreadySavedThisFlow() throws Exception {
			when(beneficiaryFlowStatusRepo.checkExistData(any(), any())).thenReturn(new BeneficiaryFlowStatus());

			assertEquals(0L, service.saveCancerScreeningNurseData(json("{" + visitDetailsBlock() + "}"), "auth"));
			verify(commonNurseServiceImpl, never()).saveBeneficiaryVisitDetails(any());
		}

		@Test
		void saveCancerScreeningNurseData_returnsZeroWhenTheVisitWasNotCreated() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(1);

			assertEquals(0L, service.saveCancerScreeningNurseData(json("{" + visitDetailsBlock() + "}"), "auth"));
		}

		@Test
		void saveCancerScreeningNurseData_sendsTheBeneficiaryToTheOncologistByDefault() throws Exception {
			stubVisitCreation();
			when(cSNurseServiceImpl.saveBenVitalDetail(any())).thenReturn(1L);

			String request = "{" + visitDetailsBlock() + ",\"vitalsDetails\":{}}";

			assertEquals(1L, service.saveCancerScreeningNurseData(json(request), "auth"));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), org.mockito.ArgumentMatchers.eq((short) 9),
					org.mockito.ArgumentMatchers.eq((short) 0), org.mockito.ArgumentMatchers.eq((short) 0),
					org.mockito.ArgumentMatchers.eq((short) 0), org.mockito.ArgumentMatchers.eq((short) 1), anyLong(),
					any());
		}

		@Test
		void saveCancerScreeningNurseData_sendsTheBeneficiaryToTheDoctorAndRadiologistWhenBothAreNeeded()
				throws Exception {
			stubVisitCreation();
			when(cSNurseServiceImpl.saveBenVitalDetail(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveCancerBreastExaminationData(any())).thenReturn(1L);
			when(beneficiaryFlowStatusRepo.getBenDataForCareStream(any())).thenReturn(new ArrayList<>());
			when(cSCarestreamServiceImpl.createMamographyRequest(any(), anyLong(), anyLong(), anyString()))
					.thenReturn(1);

			String request = "{" + visitDetailsBlock() + ",\"vitalsDetails\":{},\"sendToDoctorWorklist\":true,"
					+ "\"examinationDetails\":{\"breastDetails\":{\"beneficiaryRegID\":1,\"referredToMammogram\":true}}}";

			assertEquals(2L, service.saveCancerScreeningNurseData(json(request), "auth"));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), org.mockito.ArgumentMatchers.eq((short) 9),
					org.mockito.ArgumentMatchers.eq((short) 1), org.mockito.ArgumentMatchers.eq((short) 0),
					org.mockito.ArgumentMatchers.eq((short) 1), org.mockito.ArgumentMatchers.eq((short) 0), anyLong(),
					any());
		}

		@Test
		void saveCancerScreeningNurseData_leavesTheFlowUntouchedWhenASectionFailsToSave() throws Exception {
			stubVisitCreation();
			when(cSNurseServiceImpl.saveBenVitalDetail(any())).thenReturn(null);

			String request = "{" + visitDetailsBlock() + ",\"vitalsDetails\":{}}";

			assertNull(service.saveCancerScreeningNurseData(json(request), "auth"));
			verify(commonBenStatusFlowServiceImpl, never()).updateBenFlowNurseAfterNurseActivity(any(), anyLong(),
					anyLong(), anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any());
		}

		private void stubVisitCreation() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(5L);
			when(commonNurseServiceImpl.generateVisitCode(anyLong(), any(), any())).thenReturn(6L);
		}

		@Test
		void saveBenVisitDetails_returnsNothingWhenAVisitWasAlreadyCreatedRecently() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(1);
			assertTrue(service.saveBenVisitDetails(new BeneficiaryVisitDetail(), new CommonUtilityClass()).isEmpty());
		}
	}

	@Nested
	@DisplayName("saving the individual nurse sections")
	class NurseSections {

		@Test
		void saveBenHistoryDetails_treatsAnAbsentHistoryBlockAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenHistoryDetails(json("{}"), 1L, 2L));
			assertEquals(1L, service.saveBenHistoryDetails(null, 1L, 2L));
		}

		@Test
		void saveBenHistoryDetails_savesEverySectionThatWasSent() throws Exception {
			when(cSNurseServiceImpl.saveBenFamilyCancerHistory(any())).thenReturn(1);
			when(cSNurseServiceImpl.saveBenPersonalCancerHistory(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveBenPersonalCancerDietHistory(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveBenObstetricCancerHistory(any())).thenReturn(1L);

			String request = "{\"historyDetails\":{\"familyHistory\":{\"diseases\":[{\"diseaseType\":\"Breast\"}]},"
					+ "\"personalHistory\":{},\"pastObstetricHistory\":{}}}";

			assertEquals(1L, service.saveBenHistoryDetails(json(request), 1L, 2L));
		}

		@Test
		void saveBenHistoryDetails_treatsEachAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenHistoryDetails(json("{\"historyDetails\":{}}"), 1L, 2L));
		}

		@Test
		void saveBenHistoryDetails_failsWhenTheFamilyHistoryCarriesNoDisease() {
			// An empty disease array leaves the family-history flag unset, which the
			// success check then dereferences.
			String request = "{\"historyDetails\":{\"familyHistory\":{\"diseases\":[]}}}";
			assertThrows(NullPointerException.class, () -> service.saveBenHistoryDetails(json(request), 1L, 2L));
		}

		@Test
		void saveBenHistoryDetails_reportsFailureWhenASectionCouldNotBeSaved() throws Exception {
			when(cSNurseServiceImpl.saveBenPersonalCancerHistory(any())).thenReturn(0L);

			String request = "{\"historyDetails\":{\"personalHistory\":{}}}";
			assertNull(service.saveBenHistoryDetails(json(request), 1L, 2L));
		}

		@Test
		void saveBenVitalsDetails_savesTheVitalsWhenTheyWereSent() throws Exception {
			when(cSNurseServiceImpl.saveBenVitalDetail(any())).thenReturn(3L);

			assertEquals(3L, service.saveBenVitalsDetails(json("{\"vitalsDetails\":{}}"), 1L, 2L));
			assertEquals(1L, service.saveBenVitalsDetails(json("{}"), 1L, 2L));
			assertEquals(1L, service.saveBenVitalsDetails(null, 1L, 2L));
		}

		@Test
		void saveBenFamilyHistoryDetails_isNotWiredUpYet() {
			assertNull(service.saveBenFamilyHistoryDetails());
		}

		@Test
		void saveBenExaminationDetails_treatsAnAbsentExaminationBlockAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenExaminationDetails(json("{}"), 1L, "auth", 2L, 3L));
			assertEquals(1L, service.saveBenExaminationDetails(null, 1L, "auth", 2L, 3L));
		}

		@Test
		void saveBenExaminationDetails_treatsEachAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1L, service.saveBenExaminationDetails(json("{\"examinationDetails\":{}}"), 1L, "auth", 2L,
					3L));
		}

		@Test
		void saveBenExaminationDetails_savesEverySectionThatWasSent() throws Exception {
			when(cSNurseServiceImpl.saveCancerSignAndSymptomsData(any(), anyLong(), anyLong())).thenReturn(1L);
			when(cSNurseServiceImpl.saveLymphNodeDetails(any(), anyLong(), anyLong())).thenReturn(1L);
			when(cSNurseServiceImpl.saveCancerOralExaminationData(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveCancerBreastExaminationData(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveCancerAbdominalExaminationData(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveCancerGynecologicalExaminationData(any())).thenReturn(1L);
			when(cSNurseServiceImpl.saveDocExaminationImageAnnotation(any(), anyLong(), anyLong())).thenReturn(1L);

			String request = "{\"examinationDetails\":{\"signsDetails\":{\"cancerSignAndSymptoms\":{},"
					+ "\"cancerLymphNodeDetails\":[{}]},\"oralDetails\":{},\"breastDetails\":{},"
					+ "\"abdominalDetails\":{},\"gynecologicalDetails\":{},\"imageCoordinates\":[{}]}}";

			assertEquals(1L, service.saveBenExaminationDetails(json(request), 1L, "auth", 2L, 3L));
		}

		@Test
		void saveBenExaminationDetails_raisesAMammogramOrderWhenTheBeneficiaryWasReferred() throws Exception {
			when(cSNurseServiceImpl.saveCancerBreastExaminationData(any())).thenReturn(1L);
			when(beneficiaryFlowStatusRepo.getBenDataForCareStream(3L)).thenReturn(new ArrayList<>());
			when(cSCarestreamServiceImpl.createMamographyRequest(any(), anyLong(), anyLong(), anyString()))
					.thenReturn(1);

			String request = "{\"examinationDetails\":{\"breastDetails\":{\"beneficiaryRegID\":1,"
					+ "\"referredToMammogram\":true}}}";

			assertEquals(2L, service.saveBenExaminationDetails(json(request), 1L, "auth", 2L, 3L));
		}

		@Test
		void saveBenExaminationDetails_reportsAFailedMammogramOrder() throws Exception {
			when(cSNurseServiceImpl.saveCancerBreastExaminationData(any())).thenReturn(1L);
			when(beneficiaryFlowStatusRepo.getBenDataForCareStream(3L)).thenReturn(new ArrayList<>());
			when(cSCarestreamServiceImpl.createMamographyRequest(any(), anyLong(), anyLong(), anyString()))
					.thenReturn(0);

			String request = "{\"examinationDetails\":{\"breastDetails\":{\"beneficiaryRegID\":1,"
					+ "\"referredToMammogram\":true}}}";

			assertEquals(3L, service.saveBenExaminationDetails(json(request), 1L, "auth", 2L, 3L));
		}

		@Test
		void saveBenExaminationDetails_reportsFailureWhenASectionCouldNotBeSaved() throws Exception {
			when(cSNurseServiceImpl.saveCancerOralExaminationData(any())).thenReturn(0L);

			String request = "{\"examinationDetails\":{\"oralDetails\":{}}}";
			assertNull(service.saveBenExaminationDetails(json(request), 1L, "auth", 2L, 3L));
		}
	}

	@Nested
	@DisplayName("updating nurse data from the doctor screen")
	class NurseUpdates {

		@Test
		void UpdateCSHistoryNurseData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1, service.UpdateCSHistoryNurseData(json("{}")));
			assertEquals(1, service.UpdateCSHistoryNurseData(null));
		}

		@Test
		void UpdateCSHistoryNurseData_updatesEverySectionThatWasSent() throws Exception {
			when(cSNurseServiceImpl.updateBeneficiaryFamilyCancerHistory(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateBenObstetricCancerHistory(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateBenPersonalCancerHistory(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateBenPersonalCancerDietHistory(any())).thenReturn(1);

			String request = "{\"familyHistory\":[{}],\"pastObstetricHistory\":{},\"personalHistory\":{}}";

			assertEquals(1, service.UpdateCSHistoryNurseData(json(request)));
		}

		@Test
		void UpdateCSHistoryNurseData_treatsAnEmptyFamilyHistoryAsAlreadyDone() throws Exception {
			assertEquals(1, service.UpdateCSHistoryNurseData(json("{\"familyHistory\":[]}")));
		}

		@Test
		void UpdateCSHistoryNurseData_reportsFailureWhenASectionCouldNotBeUpdated() throws Exception {
			when(cSNurseServiceImpl.updateBenObstetricCancerHistory(any())).thenReturn(0);
			assertEquals(0, service.UpdateCSHistoryNurseData(json("{\"pastObstetricHistory\":{}}")));
		}

		@Test
		void updateBenExaminationDetail_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			assertEquals(1, service.updateBenExaminationDetail(json("{}")));
			assertEquals(1, service.updateBenExaminationDetail(null));
		}

		@Test
		void updateBenExaminationDetail_updatesEverySectionThatWasSent() throws Exception {
			when(cSNurseServiceImpl.updateSignAndSymptomsExaminationDetails(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateLymphNodeExaminationDetails(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateCancerOralDetails(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateCancerBreastDetails(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateCancerAbdominalExaminationDetails(any())).thenReturn(1);
			when(cSNurseServiceImpl.updateCancerGynecologicalExaminationDetails(any())).thenReturn(1);
			when(cSNurseServiceImpl.getCancerExaminationImageAnnotationList(any(), any()))
					.thenReturn(new ArrayList<>());
			when(cSNurseServiceImpl.updateCancerExamImgAnotasnDetails(any())).thenReturn(1);

			String request = "{\"visitCode\":3,\"signsDetails\":{\"cancerSignAndSymptoms\":{},"
					+ "\"cancerLymphNodeDetails\":[{}]},\"oralDetails\":{},\"breastDetails\":{},"
					+ "\"abdominalDetails\":{},\"gynecologicalDetails\":{\"fileIDs\":[\"a\",\"b\"]},"
					+ "\"imageCoordinates\":[{}]}";

			assertEquals(1, service.updateBenExaminationDetail(json(request)));
		}

		@Test
		void updateBenExaminationDetail_treatsAnEmptySignsBlockAsAlreadyDone() throws Exception {
			assertEquals(1, service.updateBenExaminationDetail(json("{\"signsDetails\":{}}")));
		}

		@Test
		void updateBenExaminationDetail_reportsFailureWhenASectionCouldNotBeUpdated() throws Exception {
			when(cSNurseServiceImpl.updateCancerOralDetails(any())).thenReturn(0);
			assertEquals(0, service.updateBenExaminationDetail(json("{\"oralDetails\":{}}")));
		}

		@Test
		void updateBenVitalDetail_delegatesToTheNurseService() {
			BenCancerVitalDetail vital = new BenCancerVitalDetail();
			when(cSNurseServiceImpl.updateBenVitalDetail(vital)).thenReturn(1);
			assertEquals(1, service.updateBenVitalDetail(vital));
		}
	}

	@Nested
	@DisplayName("reading the nurse and doctor screens")
	class Reads {

		/** One left-panel row, wide enough for the beneficiary summary mapper. */
		private ArrayList<Object[]> oneBenDetailRow() {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[40]);
			return rows;
		}

		@Test
		void getBenDataFrmNurseToDocVisitDetailsScreen_returnsTheStoredVisit() throws Exception {
			BeneficiaryVisitDetail visit = new BeneficiaryVisitDetail();
			visit.setBenVisitID(1L);
			when(commonNurseServiceImpl.getCSVisitDetails(1L, 2L)).thenReturn(visit);

			assertTrue(service.getBenDataFrmNurseToDocVisitDetailsScreen(1L, 2L).contains("benVisitDetails"));

			when(commonNurseServiceImpl.getCSVisitDetails(3L, 4L)).thenReturn(null);
			assertEquals("{}", service.getBenDataFrmNurseToDocVisitDetailsScreen(3L, 4L));
		}

		@Test
		void getBenDataFrmNurseToDocHistoryScreen_gathersEveryHistorySection() {
			assertTrue(service.getBenDataFrmNurseToDocHistoryScreen(1L, 2L).contains("benFamilyHistory"));
			verify(cSNurseServiceImpl).getBenPersonalCancerDietHistoryData(1L, 2L);
		}

		@Test
		void getBenDataFrmNurseToDocVitalScreen_gathersTheVitalsAndTheirTrend() {
			when(commonNurseServiceImpl.getGraphicalTrendData(1L, "cancer screening")).thenReturn(new HashMap<>());
			assertTrue(service.getBenDataFrmNurseToDocVitalScreen(1L, 2L).contains("GraphData"));
		}

		@Test
		void getBenDataFrmNurseToDocExaminationScreen_gathersEveryExaminedSystem() {
			when(cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(1L, 2L))
					.thenReturn(new ArrayList<>());

			assertTrue(service.getBenDataFrmNurseToDocExaminationScreen(1L, 2L).contains("imageCoordinates"));
			verify(cSNurseServiceImpl).getBenCancerOralExaminationData(1L, 2L);
		}

		@Test
		void getBenNurseDataForCaseSheet_gathersEverySectionOfTheNurseCaseSheet() {
			when(cSNurseServiceImpl.getBeneficiaryVisitDetails(1L, 2L)).thenReturn(new BeneficiaryVisitDetail());

			assertTrue(service.getBenNurseDataForCaseSheet(1L, 2L).contains("benVisitDetail"));
			verify(cSNurseServiceImpl).getBenCancerLymphNodeDetailsData(1L, 2L);
		}

		@Test
		void getBenDataForCaseSheet_combinesTheNurseAndDoctorSections() throws Exception {
			when(cSNurseServiceImpl.getBenNurseDataForCaseSheet(1L, 2L)).thenReturn(new HashMap<>());
			when(cSDoctorServiceImpl.getBenDoctorEnteredDataForCaseSheet(1L, 2L)).thenReturn(new HashMap<>());
			when(beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(1L, 3L)).thenReturn(oneBenDetailRow());
			when(cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(1L, 2L))
					.thenReturn(new ArrayList<>());

			assertTrue(service.getBenDataForCaseSheet(3L, 1L, 2L, "auth").contains("ImageAnnotatedData"));
		}

		@Test
		void getCancerCasesheetData_readsTheBeneficiaryKeysOutOfTheRequest() throws Exception {
			when(cSNurseServiceImpl.getBenNurseDataForCaseSheet(1L, 2L)).thenReturn(new HashMap<>());
			when(cSDoctorServiceImpl.getBenDoctorEnteredDataForCaseSheet(1L, 2L)).thenReturn(new HashMap<>());
			when(beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(1L, 3L)).thenReturn(oneBenDetailRow());
			when(cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(1L, 2L))
					.thenReturn(new ArrayList<>());

			JSONObject request = new JSONObject();
			request.put("benRegID", 1L);
			request.put("benVisitID", 4L);
			request.put("benFlowID", 3L);
			request.put("visitCode", 2L);

			assertTrue(service.getCancerCasesheetData(request, "auth").contains("BeneficiaryData"));
		}

		@Test
		void getCancerCasesheetData_returnsNothingForAnEmptyRequest() throws Exception {
			assertNull(service.getCancerCasesheetData(new JSONObject(), "auth"));
		}

		@Test
		void getCancerCasesheetData_tolratesARequestMissingTheBeneficiaryKeys() throws Exception {
			when(cSNurseServiceImpl.getBenNurseDataForCaseSheet(null, null)).thenReturn(new HashMap<>());
			when(cSDoctorServiceImpl.getBenDoctorEnteredDataForCaseSheet(null, null)).thenReturn(new HashMap<>());
			when(beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(null, null)).thenReturn(oneBenDetailRow());
			when(cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(null, null))
					.thenReturn(new ArrayList<>());

			JSONObject request = new JSONObject();
			request.put("unrelated", 1);
			request.put("alsoUnrelated", 2);

			assertTrue(service.getCancerCasesheetData(request, "auth").contains("BeneficiaryData"));
		}

		@Test
		void thePastHistoryReadsDelegateToTheNurseService() {
			when(cSNurseServiceImpl.getBenCancerFamilyHistory(1L)).thenReturn("family");
			assertEquals("family", service.getBenFamilyHistoryData(1L));

			when(cSNurseServiceImpl.getBenCancerPersonalHistory(1L)).thenReturn("personal");
			assertEquals("personal", service.getBenPersonalHistoryData(1L));

			when(cSNurseServiceImpl.getBenCancerPersonalDietHistory(1L)).thenReturn("diet");
			assertEquals("diet", service.getBenPersonalDietHistoryData(1L));

			when(cSNurseServiceImpl.getBenCancerObstetricHistory(1L)).thenReturn("obstetric");
			assertEquals("obstetric", service.getBenObstetricHistoryData(1L));
		}

		@Test
		void theDoctorDiagnosisReadsDelegateToTheDoctorService() {
			when(cSDoctorServiceImpl.getBenCancerDiagnosisData(1L, 2L)).thenReturn(new CancerDiagnosis());

			assertTrue(service.getBenDoctorDiagnosisData(1L, 2L).contains("benDiagnosisDetails"));
			assertTrue(service.getBenCaseRecordFromDoctorCS(1L, 2L).contains("diagnosis"));
		}
	}

	@Nested
	@DisplayName("saving and updating doctor data")
	class DoctorData {

		private String doctorRequest(String extra) {
			return "{\"diagnosis\":{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"benFlowID\":4,"
					+ "\"createdBy\":\"doctor\"" + extra + "}}";
		}

		@Test
		void saveCancerScreeningDoctorData_savesTheDiagnosisAndAdvancesTheFlow() throws Exception {
			when(cSDoctorServiceImpl.saveCancerDiagnosisData(any())).thenReturn(1L);

			assertEquals(1L, service.saveCancerScreeningDoctorData(json(doctorRequest("")), "auth"));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any());
		}

		@Test
		void saveCancerScreeningDoctorData_ignoresARequestWithoutADiagnosis() throws Exception {
			assertNull(service.saveCancerScreeningDoctorData(json("{}"), "auth"));
		}

		@Test
		void saveCancerScreeningDoctorData_leavesTheFlowUntouchedWhenTheDiagnosisWasNotStored() throws Exception {
			when(cSDoctorServiceImpl.saveCancerDiagnosisData(any())).thenReturn(0L);

			assertNull(service.saveCancerScreeningDoctorData(json(doctorRequest("")), "auth"));
			verify(commonBenStatusFlowServiceImpl, never()).updateBenFlowAfterDocData(any(), any(), any(), any(),
					anyShort(), anyShort(), anyShort(), anyShort(), anyInt(), any(), any());
		}

		@Test
		void saveCancerScreeningDoctorData_booksTheSpecialistSlotBeforeRaisingATeleconsultationRequest()
				throws Exception {
			when(cSDoctorServiceImpl.saveCancerDiagnosisData(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
			when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);

			String request = "{\"diagnosis\":{\"beneficiaryRegID\":1,\"serviceID\":4,\"createdBy\":\"doctor\"},"
					+ "\"tcRequest\":{\"userID\":5,\"allocationDate\":\"2024-01-01\",\"fromTime\":\"10:00:00\","
					+ "\"toTime\":\"10:30:00\"}}";

			assertEquals(1L, service.saveCancerScreeningDoctorData(json(request), "auth"));
			verify(teleConsultationServiceImpl).createTCRequest(any());
		}

		@Test
		void saveCancerScreeningDoctorData_failsWhenTheSpecialistSlotCouldNotBeBooked() throws Exception {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(0);

			String request = "{\"diagnosis\":{\"serviceID\":4,\"createdBy\":\"doctor\"},"
					+ "\"tcRequest\":{\"userID\":5,\"allocationDate\":\"2024-01-01\",\"fromTime\":\"10:00:00\","
					+ "\"toTime\":\"10:30:00\"}}";

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.saveCancerScreeningDoctorData(json(request), "auth"));
			assertEquals("Error while booking slot.", thrown.getMessage());
		}

		@Test
		void saveBenDiagnosisDetails_storesTheDiagnosisWhenOneWasSent() throws Exception {
			when(cSDoctorServiceImpl.saveCancerDiagnosisData(any())).thenReturn(4L);

			assertEquals(4L, service.saveBenDiagnosisDetails(json(doctorRequest(""))));
			assertEquals(1L, service.saveBenDiagnosisDetails(json("{}")));
			assertEquals(1L, service.saveBenDiagnosisDetails(null));
		}

		@Test
		void saveBenDiagnosisDetails_reportsFailureWhenTheDiagnosisWasNotStored() throws Exception {
			when(cSDoctorServiceImpl.saveCancerDiagnosisData(any())).thenReturn(0L);
			assertNull(service.saveBenDiagnosisDetails(json(doctorRequest(""))));
		}

		@Test
		void updateCancerScreeningDoctorData_updatesTheDiagnosisAndAdvancesTheFlow() throws Exception {
			when(cSDoctorServiceImpl.updateCancerDiagnosisDetailsByDoctor(any())).thenReturn(1);
			when(beneficiaryFlowStatusRepo.updateBenFlowAfterTCSpcialistDoneForCanceScreening(4L, 1L, 3L))
					.thenReturn(1);

			assertEquals(1, service.updateCancerScreeningDoctorData(json(doctorRequest(""))));
		}

		@Test
		void updateCancerScreeningDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			when(cSDoctorServiceImpl.updateCancerDiagnosisDetailsByDoctor(any())).thenReturn(1);
			when(beneficiaryFlowStatusRepo.updateBenFlowAfterTCSpcialistDoneForCanceScreening(any(), any(), any()))
					.thenReturn(0);

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.updateCancerScreeningDoctorData(json(doctorRequest(""))));
			assertTrue(thrown.getMessage().contains("beneficiary flow status"));
		}

		@Test
		void updateCancerScreeningDoctorData_failsWhenTheDiagnosisCouldNotBeUpdated() throws Exception {
			when(cSDoctorServiceImpl.updateCancerDiagnosisDetailsByDoctor(any())).thenReturn(0);

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.updateCancerScreeningDoctorData(json(doctorRequest(""))));
			assertEquals("Error while saving data.", thrown.getMessage());
		}

		@Test
		void updateCancerScreeningDoctorData_rejectsARequestWithoutADiagnosis() {
			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.updateCancerScreeningDoctorData(json("{}")));
			assertEquals("Invalid request.", thrown.getMessage());

			RuntimeException nullRequest = assertThrows(RuntimeException.class,
					() -> service.updateCancerScreeningDoctorData(null));
			assertEquals("Invalid request as it is null.", nullRequest.getMessage());
		}

		@Test
		void updateCancerDiagnosisDetailsByOncologist_delegatesToTheOncologistService() {
			CancerDiagnosis diagnosis = new CancerDiagnosis();
			when(csOncologistServiceImpl.updateCancerDiagnosisDetailsByOncologist(diagnosis)).thenReturn(1);
			assertEquals(1, service.updateCancerDiagnosisDetailsByOncologist(diagnosis));
		}
	}
}
