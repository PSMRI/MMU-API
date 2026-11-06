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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.iemr.mmu.data.doctor.CancerAbdominalExamination;
import com.iemr.mmu.data.doctor.CancerBreastExamination;
import com.iemr.mmu.data.doctor.CancerExaminationImageAnnotation;
import com.iemr.mmu.data.doctor.CancerGynecologicalExamination;
import com.iemr.mmu.data.doctor.CancerLymphNodeDetails;
import com.iemr.mmu.data.doctor.CancerOralExamination;
import com.iemr.mmu.data.doctor.CancerSignAndSymptoms;
import com.iemr.mmu.data.doctor.WrapperCancerExamImgAnotasn;
import com.iemr.mmu.data.doctor.WrapperCancerSymptoms;
import com.iemr.mmu.data.nurse.BenCancerVitalDetail;
import com.iemr.mmu.data.nurse.BenFamilyCancerHistory;
import com.iemr.mmu.data.nurse.BenObstetricCancerHistory;
import com.iemr.mmu.data.nurse.BenPersonalCancerDietHistory;
import com.iemr.mmu.data.nurse.BenPersonalCancerHistory;
import com.iemr.mmu.repo.doctor.CancerAbdominalExaminationRepo;
import com.iemr.mmu.repo.doctor.CancerBreastExaminationRepo;
import com.iemr.mmu.repo.doctor.CancerExaminationImageAnnotationRepo;
import com.iemr.mmu.repo.doctor.CancerGynecologicalExaminationRepo;
import com.iemr.mmu.repo.doctor.CancerLymphNodeExaminationRepo;
import com.iemr.mmu.repo.doctor.CancerOralExaminationRepo;
import com.iemr.mmu.repo.doctor.CancerSignAndSymptomsRepo;
import com.iemr.mmu.repo.nurse.BenCancerVitalDetailRepo;
import com.iemr.mmu.repo.nurse.BenFamilyCancerHistoryRepo;
import com.iemr.mmu.repo.nurse.BenObstetricCancerHistoryRepo;
import com.iemr.mmu.repo.nurse.BenPersonalCancerDietHistoryRepo;
import com.iemr.mmu.repo.nurse.BenPersonalCancerHistoryRepo;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;

class CSNurseServiceImplTest {

	@Mock
	private AESEncryptionDecryption aESEncryptionDecryption;
	@Mock
	private BenFamilyCancerHistoryRepo benFamilyCancerHistoryRepo;
	@Mock
	private BenPersonalCancerHistoryRepo benPersonalCancerHistoryRepo;
	@Mock
	private BenPersonalCancerDietHistoryRepo benPersonalCancerDietHistoryRepo;
	@Mock
	private BenObstetricCancerHistoryRepo benObstetricCancerHistoryRepo;
	@Mock
	private BenCancerVitalDetailRepo benCancerVitalDetailRepo;
	@Mock
	private BenVisitDetailRepo benVisitDetailRepo;
	@Mock
	private CancerAbdominalExaminationRepo cancerAbdominalExaminationRepo;
	@Mock
	private CancerBreastExaminationRepo cancerBreastExaminationRepo;
	@Mock
	private CancerGynecologicalExaminationRepo cancerGynecologicalExaminationRepo;
	@Mock
	private CancerSignAndSymptomsRepo cancerSignAndSymptomsRepo;
	@Mock
	private CancerLymphNodeExaminationRepo cancerLymphNodeExaminationRepo;
	@Mock
	private CancerOralExaminationRepo cancerOralExaminationRepo;
	@Mock
	private CancerExaminationImageAnnotationRepo cancerExaminationImageAnnotationRepo;

	@InjectMocks
	private CSNurseServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	/** The (id, processed) pairs the update methods read before deleting the old rows. */
	private ArrayList<Object[]> statusRows(Object id, String processed) {
		ArrayList<Object[]> rows = new ArrayList<>();
		rows.add(new Object[] { id, processed });
		return rows;
	}

	/** A stored row wide enough for any of the cancer history mappers, with no values set. */
	private ArrayList<Object[]> oneEmptyRow() {
		ArrayList<Object[]> rows = new ArrayList<>();
		rows.add(new Object[20]);
		return rows;
	}

	@Nested
	@DisplayName("history saves")
	class HistorySaves {

		@Test
		void saveBenFamilyCancerHistory_flattensTheFamilyMembersOfEachDisease() {
			BenFamilyCancerHistory withMembers = new BenFamilyCancerHistory();
			withMembers.setFamilyMemberList(Arrays.asList("Mother", "Father"));
			BenFamilyCancerHistory withoutMembers = new BenFamilyCancerHistory();

			when(benFamilyCancerHistoryRepo.saveAll(any()))
					.thenReturn(Collections.singletonList(withMembers));

			assertEquals(1, service.saveBenFamilyCancerHistory(Arrays.asList(withMembers, withoutMembers)));
			assertEquals("Mother,Father", withMembers.getFamilyMember());
		}

		@Test
		void saveBenFamilyCancerHistory_reportsFailureWhenNotEveryDiseaseWasStored() {
			BenFamilyCancerHistory withMembers = new BenFamilyCancerHistory();
			withMembers.setFamilyMemberList(Collections.singletonList("Mother"));
			when(benFamilyCancerHistoryRepo.saveAll(any())).thenReturn(new ArrayList<>());

			assertEquals(0, service.saveBenFamilyCancerHistory(Collections.singletonList(withMembers)));
		}

		@Test
		void saveBenPersonalCancerHistory_flattensTheTobaccoProductsBeforeSaving() {
			BenPersonalCancerHistory history = new BenPersonalCancerHistory();
			history.setTypeOfTobaccoProductList(Arrays.asList("Bidi", "Gutkha"));
			BenPersonalCancerHistory stored = new BenPersonalCancerHistory();
			stored.setID(1L);
			when(benPersonalCancerHistoryRepo.save(history)).thenReturn(stored);

			assertEquals(1L, service.saveBenPersonalCancerHistory(history));
			assertEquals("Bidi,Gutkha,", history.getTypeOfTobaccoProduct());
		}

		@Test
		void saveBenPersonalCancerHistory_storesAnEmptyProductListAsAnEmptyString() {
			BenPersonalCancerHistory history = new BenPersonalCancerHistory();
			when(benPersonalCancerHistoryRepo.save(history)).thenReturn(null);

			assertNull(service.saveBenPersonalCancerHistory(history));
			assertEquals("", history.getTypeOfTobaccoProduct());
		}

		@Test
		void saveBenPersonalCancerDietHistory_flattensTheOilsConsumedBeforeSaving() {
			BenPersonalCancerDietHistory history = new BenPersonalCancerDietHistory();
			history.setTypeOfOilConsumedList(Arrays.asList("Mustard", "Sunflower"));
			BenPersonalCancerDietHistory stored = new BenPersonalCancerDietHistory();
			stored.setID(2L);
			when(benPersonalCancerDietHistoryRepo.save(history)).thenReturn(stored);

			assertEquals(2L, service.saveBenPersonalCancerDietHistory(history));
			assertEquals("Mustard,Sunflower,", history.getTypeOfOilConsumed());
		}

		@Test
		void saveBenPersonalCancerDietHistory_returnsNullWhenNothingWasStored() {
			BenPersonalCancerDietHistory history = new BenPersonalCancerDietHistory();
			when(benPersonalCancerDietHistoryRepo.save(history)).thenReturn(null);
			assertNull(service.saveBenPersonalCancerDietHistory(history));
		}

		@Test
		void saveBenObstetricCancerHistory_returnsTheStoredId() {
			BenObstetricCancerHistory history = new BenObstetricCancerHistory();
			BenObstetricCancerHistory stored = new BenObstetricCancerHistory();
			stored.setID(3L);
			when(benObstetricCancerHistoryRepo.save(history)).thenReturn(stored);
			assertEquals(3L, service.saveBenObstetricCancerHistory(history));

			when(benObstetricCancerHistoryRepo.save(history)).thenReturn(null);
			assertNull(service.saveBenObstetricCancerHistory(history));
		}

		@Test
		void saveBenVitalDetail_returnsTheStoredId() {
			BenCancerVitalDetail vital = new BenCancerVitalDetail();
			BenCancerVitalDetail stored = new BenCancerVitalDetail();
			stored.setID(4L);
			when(benCancerVitalDetailRepo.save(vital)).thenReturn(stored);
			assertEquals(4L, service.saveBenVitalDetail(vital));

			when(benCancerVitalDetailRepo.save(vital)).thenReturn(null);
			assertNull(service.saveBenVitalDetail(vital));
		}
	}

	@Nested
	@DisplayName("history updates")
	class HistoryUpdates {

		@Test
		void updateBeneficiaryFamilyCancerHistory_replacesTheStoredFamilyHistory() {
			BenFamilyCancerHistory history = new BenFamilyCancerHistory();
			history.setBeneficiaryRegID(1L);
			history.setVisitCode(2L);
			history.setFamilyMemberList(Collections.singletonList("Mother"));
			history.setModifiedBy("nurse");
			List<BenFamilyCancerHistory> input = Collections.singletonList(history);

			when(benFamilyCancerHistoryRepo.getFamilyCancerHistoryStatus(1L, 2L)).thenReturn(statusRows(5L, "P"));
			when(benFamilyCancerHistoryRepo.deleteExistingFamilyRecord(5L, "U")).thenReturn(1);
			when(benFamilyCancerHistoryRepo.saveAll(any())).thenReturn(new ArrayList<>(input));

			assertEquals(1, service.updateBeneficiaryFamilyCancerHistory(input));
			assertEquals("Mother,", history.getFamilyMember());
			assertEquals("nurse", history.getCreatedBy());
		}

		@Test
		void updateBeneficiaryFamilyCancerHistory_succeedsWhenNoFamilyMemberIsLeft() {
			BenFamilyCancerHistory history = new BenFamilyCancerHistory();
			List<BenFamilyCancerHistory> input = Collections.singletonList(history);
			when(benFamilyCancerHistoryRepo.getFamilyCancerHistoryStatus(any(), any())).thenReturn(new ArrayList<>());

			assertEquals(1, service.updateBeneficiaryFamilyCancerHistory(input));
		}

		@Test
		void updateBeneficiaryFamilyCancerHistory_reportsFailureWhenTheOldRowsCouldNotBeCleared() {
			BenFamilyCancerHistory history = new BenFamilyCancerHistory();
			List<BenFamilyCancerHistory> input = Collections.singletonList(history);
			when(benFamilyCancerHistoryRepo.getFamilyCancerHistoryStatus(any(), any()))
					.thenReturn(statusRows(5L, "N"));
			when(benFamilyCancerHistoryRepo.deleteExistingFamilyRecord(5L, "N")).thenReturn(0);

			assertEquals(0, service.updateBeneficiaryFamilyCancerHistory(input));
		}

		@Test
		void updateBeneficiaryFamilyCancerHistory_reportsFailureWhenTheHistoryIsEmpty() {
			assertEquals(0, service.updateBeneficiaryFamilyCancerHistory(new ArrayList<>()));
		}

		@Test
		void updateBenObstetricCancerHistory_updatesTheStoredRowWhenOneExists() {
			BenObstetricCancerHistory history = new BenObstetricCancerHistory();
			when(benObstetricCancerHistoryRepo.getObstetricCancerHistoryStatus(any(), any())).thenReturn("P");
			when(benObstetricCancerHistoryRepo.updateBenObstetricCancerHistory(any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
					any(), any(), eq("U"))).thenReturn(1);

			assertEquals(1, service.updateBenObstetricCancerHistory(history));
		}

		@Test
		void updateBenObstetricCancerHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			BenObstetricCancerHistory history = new BenObstetricCancerHistory();
			history.setModifiedBy("nurse");
			BenObstetricCancerHistory stored = new BenObstetricCancerHistory();
			stored.setID(6L);
			when(benObstetricCancerHistoryRepo.getObstetricCancerHistoryStatus(any(), any())).thenReturn(null);
			when(benObstetricCancerHistoryRepo.save(history)).thenReturn(stored);

			assertEquals(1, service.updateBenObstetricCancerHistory(history));
			assertEquals("nurse", history.getCreatedBy());
			assertEquals("N", history.getProcessed());
		}

		@Test
		void updateBenObstetricCancerHistory_reportsFailureWhenTheFreshRowWasNotStored() {
			BenObstetricCancerHistory history = new BenObstetricCancerHistory();
			when(benObstetricCancerHistoryRepo.getObstetricCancerHistoryStatus(any(), any())).thenReturn(null);
			when(benObstetricCancerHistoryRepo.save(history)).thenReturn(null);

			assertEquals(0, service.updateBenObstetricCancerHistory(history));
		}

		@Test
		void updateBenPersonalCancerHistory_updatesTheStoredRowWhenOneExists() {
			BenPersonalCancerHistory history = new BenPersonalCancerHistory();
			when(benPersonalCancerHistoryRepo.getPersonalCancerHistoryStatus(any(), any())).thenReturn("P");
			when(benPersonalCancerHistoryRepo.updateBenPersonalCancerHistory(any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq("U"))).thenReturn(1);

			assertEquals(1, service.updateBenPersonalCancerHistory(history));
		}

		@Test
		void updateBenPersonalCancerHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			BenPersonalCancerHistory history = new BenPersonalCancerHistory();
			BenPersonalCancerHistory stored = new BenPersonalCancerHistory();
			stored.setID(7L);
			when(benPersonalCancerHistoryRepo.getPersonalCancerHistoryStatus(any(), any())).thenReturn(null);
			when(benPersonalCancerHistoryRepo.save(history)).thenReturn(stored);

			assertEquals(1, service.updateBenPersonalCancerHistory(history));
		}

		@Test
		void updateBenPersonalCancerDietHistory_updatesTheStoredRowWhenOneExists() {
			BenPersonalCancerDietHistory history = new BenPersonalCancerDietHistory();
			when(benPersonalCancerDietHistoryRepo.getPersonalCancerDietHistoryStatus(any(), any())).thenReturn("P");
			when(benPersonalCancerDietHistoryRepo.updateBenPersonalCancerDietHistory(any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), eq("U"))).thenReturn(1);

			assertEquals(1, service.updateBenPersonalCancerDietHistory(history));
		}

		@Test
		void updateBenPersonalCancerDietHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			BenPersonalCancerDietHistory history = new BenPersonalCancerDietHistory();
			BenPersonalCancerDietHistory stored = new BenPersonalCancerDietHistory();
			stored.setID(8L);
			when(benPersonalCancerDietHistoryRepo.getPersonalCancerDietHistoryStatus(any(), any())).thenReturn(null);
			when(benPersonalCancerDietHistoryRepo.save(history)).thenReturn(stored);

			assertEquals(1, service.updateBenPersonalCancerDietHistory(history));
		}

		@Test
		void updateBenVitalDetail_updatesTheStoredRowWhenOneExists() {
			BenCancerVitalDetail vital = new BenCancerVitalDetail();
			when(benCancerVitalDetailRepo.getCancerVitalStatus(any(), any())).thenReturn("P");
			when(benCancerVitalDetailRepo.updateBenCancerVitalDetail(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq("U"), any(), any(),
					any())).thenReturn(1);

			assertEquals(1, service.updateBenVitalDetail(vital));
		}

		@Test
		void updateBenVitalDetail_insertsAFreshRowWhenNoneIsStoredYet() {
			BenCancerVitalDetail vital = new BenCancerVitalDetail();
			vital.setModifiedBy("nurse");
			when(benCancerVitalDetailRepo.getCancerVitalStatus(any(), any())).thenReturn(null);
			when(benCancerVitalDetailRepo.save(vital)).thenReturn(vital);

			assertEquals(1, service.updateBenVitalDetail(vital));
			assertEquals("nurse", vital.getCreatedBy());

			when(benCancerVitalDetailRepo.save(vital)).thenReturn(null);
			assertEquals(0, service.updateBenVitalDetail(vital));
		}
	}

	@Nested
	@DisplayName("case sheet reads")
	class CaseSheetReads {

		@Test
		void getBenFamilyHisData_splitsTheStoredFamilyMembersBackIntoAList() {
			BenFamilyCancerHistory withMembers = new BenFamilyCancerHistory();
			withMembers.setFamilyMember("Mother,Father");
			BenFamilyCancerHistory withoutMembers = new BenFamilyCancerHistory();
			when(benFamilyCancerHistoryRepo.getBenFamilyHistory(1L, 2L))
					.thenReturn(Arrays.asList(withMembers, withoutMembers));

			List<BenFamilyCancerHistory> result = service.getBenFamilyHisData(1L, 2L);

			assertEquals(Arrays.asList("Mother", "Father"), result.get(0).getFamilyMemberList());
			assertTrue(result.get(1).getFamilyMemberList().isEmpty());
		}

		@Test
		void getBenFamilyHisData_returnsAnEmptyListWhenNoHistoryWasRecorded() {
			when(benFamilyCancerHistoryRepo.getBenFamilyHistory(1L, 2L)).thenReturn(new ArrayList<>());
			assertTrue(service.getBenFamilyHisData(1L, 2L).isEmpty());
		}

		@Test
		void getBenPersonalCancerHistoryData_splitsTheStoredTobaccoProductsBackIntoAList() {
			BenPersonalCancerHistory stored = new BenPersonalCancerHistory();
			stored.setTypeOfTobaccoProduct("Bidi,Gutkha");
			when(benPersonalCancerHistoryRepo.getBenPersonalHistory(1L, 2L)).thenReturn(stored);

			assertEquals(Arrays.asList("Bidi", "Gutkha"),
					service.getBenPersonalCancerHistoryData(1L, 2L).getTypeOfTobaccoProductList());
		}

		@Test
		void getBenPersonalCancerHistoryData_returnsNullWhenNoHistoryWasRecorded() {
			when(benPersonalCancerHistoryRepo.getBenPersonalHistory(1L, 2L)).thenReturn(null);
			assertNull(service.getBenPersonalCancerHistoryData(1L, 2L));
		}

		@Test
		void getBenPersonalCancerDietHistoryData_splitsTheStoredOilsBackIntoAList() {
			BenPersonalCancerDietHistory stored = new BenPersonalCancerDietHistory();
			stored.setTypeOfOilConsumed("Mustard,Sunflower");
			when(benPersonalCancerDietHistoryRepo.getBenPersonaDietHistory(1L, 2L)).thenReturn(stored);

			assertEquals(Arrays.asList("Mustard", "Sunflower"),
					service.getBenPersonalCancerDietHistoryData(1L, 2L).getTypeOfOilConsumedList());

			when(benPersonalCancerDietHistoryRepo.getBenPersonaDietHistory(3L, 4L)).thenReturn(null);
			assertNull(service.getBenPersonalCancerDietHistoryData(3L, 4L));
		}

		@Test
		void getBenCancerGynecologicalExaminationData_decryptsEachAttachedFilePath() throws Exception {
			CancerGynecologicalExamination stored = new CancerGynecologicalExamination();
			stored.setFilePath("enc1,,enc2");
			when(cancerGynecologicalExaminationRepo.getBenCancerGynecologicalExaminationDetails(1L, 2L))
					.thenReturn(stored);
			when(aESEncryptionDecryption.decrypt("enc1")).thenReturn("/reports/first.png");
			when(aESEncryptionDecryption.decrypt("enc2")).thenThrow(new RuntimeException("bad key"));

			CancerGynecologicalExamination result = service.getBenCancerGynecologicalExaminationData(1L, 2L);

			assertEquals(1, result.getFiles().size());
			assertEquals("first.png", result.getFiles().get(0).get("fileName"));
		}

		@Test
		void getBenCancerGynecologicalExaminationData_leavesTheFileListUnsetWhenNothingIsAttached() {
			CancerGynecologicalExamination stored = new CancerGynecologicalExamination();
			stored.setFilePath("   ");
			when(cancerGynecologicalExaminationRepo.getBenCancerGynecologicalExaminationDetails(1L, 2L))
					.thenReturn(stored);
			assertNull(service.getBenCancerGynecologicalExaminationData(1L, 2L).getFiles());

			when(cancerGynecologicalExaminationRepo.getBenCancerGynecologicalExaminationDetails(3L, 4L))
					.thenReturn(null);
			assertNull(service.getBenCancerGynecologicalExaminationData(3L, 4L));
		}

		@Test
		void getCancerExaminationImageAnnotationCasesheet_groupsTheMarkersByImage() {
			CancerExaminationImageAnnotation firstMarker = annotation(1, 10, 20, 1);
			CancerExaminationImageAnnotation secondMarkerSameImage = annotation(1, 30, 40, 2);
			CancerExaminationImageAnnotation markerOnOtherImage = annotation(2, 50, 60, 1);
			when(cancerExaminationImageAnnotationRepo.getCancerExaminationImageAnnotationList(1L, 2L))
					.thenReturn(Arrays.asList(firstMarker, secondMarkerSameImage, markerOnOtherImage));

			ArrayList<WrapperCancerExamImgAnotasn> result = service
					.getCancerExaminationImageAnnotationCasesheet(1L, 2L);

			assertEquals(2, result.size());
			assertEquals(2, result.get(0).getMarkers().size());
			assertEquals(1, result.get(1).getMarkers().size());
		}

		@Test
		void getCancerExaminationImageAnnotationCasesheet_returnsNothingWhenNoImageWasAnnotated() {
			when(cancerExaminationImageAnnotationRepo.getCancerExaminationImageAnnotationList(1L, 2L))
					.thenReturn(new ArrayList<>());
			assertTrue(service.getCancerExaminationImageAnnotationCasesheet(1L, 2L).isEmpty());
		}

		private CancerExaminationImageAnnotation annotation(int imageId, int x, int y, int point) {
			CancerExaminationImageAnnotation annotation = new CancerExaminationImageAnnotation();
			annotation.setCancerImageID(imageId);
			annotation.setxCoordinate(x);
			annotation.setyCoordinate(y);
			annotation.setPoint(point);
			annotation.setPointDesc("marker");
			return annotation;
		}

		@Test
		void getBeneficiaryVisitDetails_mapsTheStoredVisitRow() {
			when(benVisitDetailRepo.getBeneficiaryVisitDetails(1L, 2L)).thenReturn(oneEmptyRow());
			assertNotNull(service.getBeneficiaryVisitDetails(1L, 2L));

			when(benVisitDetailRepo.getBeneficiaryVisitDetails(3L, 4L)).thenReturn(null);
			assertNull(service.getBeneficiaryVisitDetails(3L, 4L));
		}

		@Test
		void getBenNurseDataForCaseSheet_gathersEverySectionOfTheNurseCaseSheet() {
			when(benVisitDetailRepo.getBeneficiaryVisitDetails(1L, 2L)).thenReturn(new ArrayList<>());
			when(benFamilyCancerHistoryRepo.getBenFamilyHistory(1L, 2L)).thenReturn(new ArrayList<>());
			when(cancerLymphNodeExaminationRepo.getBenCancerLymphNodeDetails(1L, 2L)).thenReturn(new ArrayList<>());

			Map<String, Object> caseSheet = service.getBenNurseDataForCaseSheet(1L, 2L);

			assertEquals(12, caseSheet.size());
			assertTrue(caseSheet.containsKey("oralExamination"));
		}

		@Test
		void theRemainingExaminationReadsDelegateToTheirRepositories() {
			BenObstetricCancerHistory obstetric = new BenObstetricCancerHistory();
			when(benObstetricCancerHistoryRepo.getBenObstetricCancerHistory(1L, 2L)).thenReturn(obstetric);
			assertEquals(obstetric, service.getBenObstetricDetailsData(1L, 2L));

			BenCancerVitalDetail vital = new BenCancerVitalDetail();
			when(benCancerVitalDetailRepo.getBenCancerVitalDetail(1L, 2L)).thenReturn(vital);
			assertEquals(vital, service.getBenCancerVitalDetailData(1L, 2L));

			CancerAbdominalExamination abdominal = new CancerAbdominalExamination();
			when(cancerAbdominalExaminationRepo.getBenCancerAbdominalExaminationDetails(1L, 2L)).thenReturn(abdominal);
			assertEquals(abdominal, service.getBenCancerAbdominalExaminationData(1L, 2L));

			CancerBreastExamination breast = new CancerBreastExamination();
			when(cancerBreastExaminationRepo.getBenCancerBreastExaminationDetails(1L, 2L)).thenReturn(breast);
			assertEquals(breast, service.getBenCancerBreastExaminationData(1L, 2L));

			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			when(cancerSignAndSymptomsRepo.getBenCancerSignAndSymptomsDetails(1L, 2L)).thenReturn(symptoms);
			assertEquals(symptoms, service.getBenCancerSignAndSymptomsData(1L, 2L));

			List<CancerLymphNodeDetails> lymphNodes = new ArrayList<>();
			when(cancerLymphNodeExaminationRepo.getBenCancerLymphNodeDetails(1L, 2L)).thenReturn(lymphNodes);
			assertEquals(lymphNodes, service.getBenCancerLymphNodeDetailsData(1L, 2L));

			CancerOralExamination oral = new CancerOralExamination();
			when(cancerOralExaminationRepo.getBenCancerOralExaminationDetails(1L, 2L)).thenReturn(oral);
			assertEquals(oral, service.getBenCancerOralExaminationData(1L, 2L));
		}
	}

	@Nested
	@DisplayName("past visit history tables")
	class HistoryTables {

		@Test
		void getBenCancerFamilyHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benFamilyCancerHistoryRepo.getBenCancerFamilyHistory(1L)).thenReturn(oneEmptyRow());
			assertTrue(service.getBenCancerFamilyHistory(1L).contains("\"data\":[{"));

			when(benFamilyCancerHistoryRepo.getBenCancerFamilyHistory(2L)).thenReturn(null);
			assertTrue(service.getBenCancerFamilyHistory(2L).contains("\"data\":[]"));
		}

		@Test
		void getBenCancerPersonalHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benPersonalCancerHistoryRepo.getBenPersonalHistory(1L)).thenReturn(oneEmptyRow());
			assertTrue(service.getBenCancerPersonalHistory(1L).contains("\"data\":[{"));

			when(benPersonalCancerHistoryRepo.getBenPersonalHistory(2L)).thenReturn(null);
			assertTrue(service.getBenCancerPersonalHistory(2L).contains("\"data\":[]"));
		}

		@Test
		void getBenCancerPersonalDietHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benPersonalCancerDietHistoryRepo.getBenPersonaDietHistory(1L)).thenReturn(oneEmptyRow());
			assertTrue(service.getBenCancerPersonalDietHistory(1L).contains("\"data\":[{"));

			when(benPersonalCancerDietHistoryRepo.getBenPersonaDietHistory(2L)).thenReturn(null);
			assertTrue(service.getBenCancerPersonalDietHistory(2L).contains("\"data\":[]"));
		}

		@Test
		void getBenCancerObstetricHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benObstetricCancerHistoryRepo.getBenObstetricCancerHistoryData(1L)).thenReturn(oneEmptyRow());
			assertTrue(service.getBenCancerObstetricHistory(1L).contains("\"data\":[{"));

			when(benObstetricCancerHistoryRepo.getBenObstetricCancerHistoryData(2L)).thenReturn(null);
			assertTrue(service.getBenCancerObstetricHistory(2L).contains("\"data\":[]"));
		}
	}

	@Nested
	@DisplayName("examination saves")
	class ExaminationSaves {

		@Test
		void saveLymphNodeDetails_stampsTheVisitOntoEveryNodeAndReturnsTheLastId() {
			CancerLymphNodeDetails node = new CancerLymphNodeDetails();
			CancerLymphNodeDetails stored = new CancerLymphNodeDetails();
			stored.setID(9L);
			List<CancerLymphNodeDetails> nodes = Collections.singletonList(node);
			when(cancerLymphNodeExaminationRepo.saveAll(nodes)).thenReturn(Collections.singletonList(stored));

			assertEquals(9L, service.saveLymphNodeDetails(nodes, 1L, 2L));
			assertEquals(1L, node.getBenVisitID());
			assertEquals(2L, node.getVisitCode());
		}

		@Test
		void saveLymphNodeDetails_returnsNullWhenNoNodeWasStored() {
			List<CancerLymphNodeDetails> nodes = Collections.singletonList(new CancerLymphNodeDetails());
			when(cancerLymphNodeExaminationRepo.saveAll(nodes)).thenReturn(new ArrayList<>());
			assertNull(service.saveLymphNodeDetails(nodes, 1L, 2L));
		}

		@Test
		void saveCancerSignAndSymptomsData_stampsTheVisitBeforeSaving() {
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			CancerSignAndSymptoms stored = new CancerSignAndSymptoms();
			stored.setID(10L);
			when(cancerSignAndSymptomsRepo.save(symptoms)).thenReturn(stored);

			assertEquals(10L, service.saveCancerSignAndSymptomsData(symptoms, 1L, 2L));
			assertEquals(1L, symptoms.getBenVisitID());
			assertEquals(2L, symptoms.getVisitCode());

			when(cancerSignAndSymptomsRepo.save(symptoms)).thenReturn(null);
			assertNull(service.saveCancerSignAndSymptomsData(symptoms));
		}

		@Test
		void saveCancerOralExaminationData_flattensThePreMalignantLesionTypes() {
			CancerOralExamination oral = new CancerOralExamination();
			oral.setPreMalignantLesionTypeList(Arrays.asList("Leukoplakia", "Erythroplakia"));
			CancerOralExamination stored = new CancerOralExamination();
			stored.setID(11L);
			when(cancerOralExaminationRepo.save(oral)).thenReturn(stored);

			assertEquals(11L, service.saveCancerOralExaminationData(oral));
			assertEquals("Leukoplakia,Erythroplakia,", oral.getPreMalignantLesionType());

			when(cancerOralExaminationRepo.save(oral)).thenReturn(null);
			assertNull(service.saveCancerOralExaminationData(oral));
		}

		@Test
		void saveCancerBreastExaminationData_returnsTheStoredId() {
			CancerBreastExamination breast = new CancerBreastExamination();
			CancerBreastExamination stored = new CancerBreastExamination();
			stored.setID(12L);
			when(cancerBreastExaminationRepo.save(breast)).thenReturn(stored);
			assertEquals(12L, service.saveCancerBreastExaminationData(breast));

			when(cancerBreastExaminationRepo.save(breast)).thenReturn(null);
			assertNull(service.saveCancerBreastExaminationData(breast));
		}

		@Test
		void saveCancerAbdominalExaminationData_returnsTheStoredId() {
			CancerAbdominalExamination abdominal = new CancerAbdominalExamination();
			CancerAbdominalExamination stored = new CancerAbdominalExamination();
			stored.setID(13L);
			when(cancerAbdominalExaminationRepo.save(abdominal)).thenReturn(stored);
			assertEquals(13L, service.saveCancerAbdominalExaminationData(abdominal));

			when(cancerAbdominalExaminationRepo.save(abdominal)).thenReturn(null);
			assertNull(service.saveCancerAbdominalExaminationData(abdominal));
		}

		@Test
		void saveCancerGynecologicalExaminationData_flattensTheLesionTypes() {
			CancerGynecologicalExamination gynecological = new CancerGynecologicalExamination();
			gynecological.setTypeOfLesionList(Arrays.asList("Polyp", "Ulcer"));
			CancerGynecologicalExamination stored = new CancerGynecologicalExamination();
			stored.setID(14L);
			when(cancerGynecologicalExaminationRepo.save(gynecological)).thenReturn(stored);

			assertEquals(14L, service.saveCancerGynecologicalExaminationData(gynecological));
			assertEquals("Polyp,Ulcer,", gynecological.getTypeOfLesion());

			when(cancerGynecologicalExaminationRepo.save(gynecological)).thenReturn(null);
			assertNull(service.saveCancerGynecologicalExaminationData(gynecological));
		}

		@Test
		void saveDocExaminationImageAnnotation_expandsEveryMarkerIntoItsOwnRow() {
			WrapperCancerExamImgAnotasn wrapper = new WrapperCancerExamImgAnotasn();
			wrapper.setBeneficiaryRegID(1L);
			wrapper.setImageID(2);
			ArrayList<Map<String, Object>> markers = new ArrayList<>();
			Map<String, Object> marker = new HashMap<>();
			marker.put("xCord", 10d);
			marker.put("yCord", 20d);
			marker.put("point", 1d);
			marker.put("description", "lesion");
			markers.add(marker);
			wrapper.setMarkers(markers);

			when(cancerExaminationImageAnnotationRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1L, service.saveDocExaminationImageAnnotation(Collections.singletonList(wrapper), 3L, 4L));
			assertEquals(3L, wrapper.getVisitID());
		}

		@Test
		void saveDocExaminationImageAnnotation_returnsNullWhenNoMarkerWasDrawn() {
			WrapperCancerExamImgAnotasn wrapper = new WrapperCancerExamImgAnotasn();
			when(cancerExaminationImageAnnotationRepo.saveAll(any())).thenReturn(new ArrayList<>());
			assertNull(service.saveDocExaminationImageAnnotation(Collections.singletonList(wrapper), 3L, 4L));
		}

		@Test
		void getCancerExaminationImageAnnotationList_ignoresEmptyWrappers() {
			assertTrue(service.getCancerExaminationImageAnnotationList(new ArrayList<>(), 1L).isEmpty());
			assertTrue(service
					.getCancerExaminationImageAnnotationList(Collections.singletonList(null), 1L).isEmpty());
		}
	}

	@Nested
	@DisplayName("examination updates")
	class ExaminationUpdates {

		@Test
		void updateSignAndSymptomsExaminationDetails_updatesTheStoredRowWhenOneExists() {
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			when(cancerSignAndSymptomsRepo.getCancerSignAndSymptomsStatus(any(), any())).thenReturn("P");
			when(cancerSignAndSymptomsRepo.updateCancerSignAndSymptoms(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
					eq("U"))).thenReturn(1);

			assertEquals(1, service.updateSignAndSymptomsExaminationDetails(symptoms));
		}

		@Test
		void updateSignAndSymptomsExaminationDetails_insertsAFreshRowWhenNoneIsStoredYet() {
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			CancerSignAndSymptoms stored = new CancerSignAndSymptoms();
			stored.setID(15L);
			when(cancerSignAndSymptomsRepo.getCancerSignAndSymptomsStatus(any(), any())).thenReturn(null);
			when(cancerSignAndSymptomsRepo.save(symptoms)).thenReturn(stored);

			assertEquals(1, service.updateSignAndSymptomsExaminationDetails(symptoms));
		}

		@Test
		void updateLymphNodeExaminationDetails_clearsEveryNodeWhenTheEnlargementFlagIsTurnedOff() {
			WrapperCancerSymptoms wrapper = new WrapperCancerSymptoms();
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			symptoms.setLymphNode_Enlarged(false);
			symptoms.setBeneficiaryRegID(1L);
			symptoms.setVisitCode(2L);
			wrapper.setCancerSignAndSymptoms(symptoms);
			wrapper.setCancerLymphNodeDetails(new ArrayList<>());

			when(cancerLymphNodeExaminationRepo.getCancerLymphNodeDetailsStatus(1L, 2L))
					.thenReturn(statusRows(16L, "P"));
			when(cancerLymphNodeExaminationRepo.deleteExistingLymphNodeDetails(16L, "U")).thenReturn(1);

			assertEquals(1, service.updateLymphNodeExaminationDetails(wrapper));
		}

		@Test
		void updateLymphNodeExaminationDetails_replacesOnlyTheReportedNodesWhenTheFlagIsOn() {
			WrapperCancerSymptoms wrapper = new WrapperCancerSymptoms();
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			symptoms.setLymphNode_Enlarged(true);
			symptoms.setBeneficiaryRegID(1L);
			symptoms.setVisitCode(2L);
			wrapper.setCancerSignAndSymptoms(symptoms);

			CancerLymphNodeDetails measured = new CancerLymphNodeDetails();
			measured.setLymphNodeName("Cervical");
			measured.setSize_Left("2cm");
			CancerLymphNodeDetails unmeasured = new CancerLymphNodeDetails();
			unmeasured.setLymphNodeName("Cervical");
			wrapper.setCancerLymphNodeDetails(Arrays.asList(measured, unmeasured));

			when(cancerLymphNodeExaminationRepo.getCancerLymphNodeDetailsStatusForLymphnodeNameList(eq(1L), eq(2L),
					any())).thenReturn(statusRows(17L, "N"));
			when(cancerLymphNodeExaminationRepo.deleteExistingLymphNodeDetails(17L, "N")).thenReturn(1);
			when(cancerLymphNodeExaminationRepo.saveAll(any()))
					.thenReturn(new ArrayList<>(Collections.singletonList(measured)));

			assertEquals(1, service.updateLymphNodeExaminationDetails(wrapper));
		}

		@Test
		void updateLymphNodeExaminationDetails_succeedsWhenNoNodeWasMeasured() {
			WrapperCancerSymptoms wrapper = new WrapperCancerSymptoms();
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			symptoms.setLymphNode_Enlarged(true);
			wrapper.setCancerSignAndSymptoms(symptoms);
			wrapper.setCancerLymphNodeDetails(new ArrayList<>());

			assertEquals(1, service.updateLymphNodeExaminationDetails(wrapper));
		}

		@Test
		void updateLymphNodeExaminationDetails_reportsFailureWhenTheOldNodesCouldNotBeCleared() {
			WrapperCancerSymptoms wrapper = new WrapperCancerSymptoms();
			CancerSignAndSymptoms symptoms = new CancerSignAndSymptoms();
			symptoms.setLymphNode_Enlarged(false);
			wrapper.setCancerSignAndSymptoms(symptoms);
			wrapper.setCancerLymphNodeDetails(new ArrayList<>());

			when(cancerLymphNodeExaminationRepo.getCancerLymphNodeDetailsStatus(any(), any()))
					.thenReturn(statusRows(18L, "N"));
			when(cancerLymphNodeExaminationRepo.deleteExistingLymphNodeDetails(18L, "N")).thenReturn(0);

			assertEquals(0, service.updateLymphNodeExaminationDetails(wrapper));
		}

		@Test
		void updateCancerOralDetails_updatesTheStoredRowWhenOneExists() {
			CancerOralExamination oral = new CancerOralExamination();
			when(cancerOralExaminationRepo.getCancerOralExaminationStatus(any(), any())).thenReturn("P");
			when(cancerOralExaminationRepo.updateCancerOralExaminationDetails(any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), eq("U"))).thenReturn(1);

			assertEquals(1, service.updateCancerOralDetails(oral));
		}

		@Test
		void updateCancerOralDetails_insertsAFreshRowWhenNoneIsStoredYet() {
			CancerOralExamination oral = new CancerOralExamination();
			CancerOralExamination stored = new CancerOralExamination();
			stored.setID(19L);
			when(cancerOralExaminationRepo.getCancerOralExaminationStatus(any(), any())).thenReturn(null);
			when(cancerOralExaminationRepo.save(oral)).thenReturn(stored);

			assertEquals(1, service.updateCancerOralDetails(oral));
		}

		@Test
		void updateCancerBreastDetails_updatesTheStoredRowWhenOneExists() {
			CancerBreastExamination breast = new CancerBreastExamination();
			when(cancerBreastExaminationRepo.getCancerBreastExaminationStatus(any(), any())).thenReturn("P");
			when(cancerBreastExaminationRepo.updateCancerBreastExaminatio(any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq("U")))
							.thenReturn(1);

			assertEquals(1, service.updateCancerBreastDetails(breast));
		}

		@Test
		void updateCancerBreastDetails_insertsAFreshRowWhenNoneIsStoredYet() {
			CancerBreastExamination breast = new CancerBreastExamination();
			CancerBreastExamination stored = new CancerBreastExamination();
			stored.setID(20L);
			when(cancerBreastExaminationRepo.getCancerBreastExaminationStatus(any(), any())).thenReturn(null);
			when(cancerBreastExaminationRepo.save(breast)).thenReturn(stored);

			assertEquals(1, service.updateCancerBreastDetails(breast));
		}

		@Test
		void updateCancerAbdominalExaminationDetails_updatesTheStoredRowWhenOneExists() {
			CancerAbdominalExamination abdominal = new CancerAbdominalExamination();
			when(cancerAbdominalExaminationRepo.getCancerAbdominalExaminationStatus(any(), any())).thenReturn("P");
			when(cancerAbdominalExaminationRepo.updateCancerAbdominalExamination(any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq("U")))
							.thenReturn(1);

			assertEquals(1, service.updateCancerAbdominalExaminationDetails(abdominal));
		}

		@Test
		void updateCancerAbdominalExaminationDetails_insertsAFreshRowWhenNoneIsStoredYet() {
			CancerAbdominalExamination abdominal = new CancerAbdominalExamination();
			CancerAbdominalExamination stored = new CancerAbdominalExamination();
			stored.setID(21L);
			when(cancerAbdominalExaminationRepo.getCancerAbdominalExaminationStatus(any(), any())).thenReturn(null);
			when(cancerAbdominalExaminationRepo.save(abdominal)).thenReturn(stored);

			assertEquals(1, service.updateCancerAbdominalExaminationDetails(abdominal));
		}

		@Test
		void updateCancerGynecologicalExaminationDetails_updatesTheStoredRowWhenOneExists() {
			CancerGynecologicalExamination gynecological = new CancerGynecologicalExamination();
			when(cancerGynecologicalExaminationRepo.getCancerGynecologicalExaminationStatus(any(), any()))
					.thenReturn("P");
			when(cancerGynecologicalExaminationRepo.updateCancerGynecologicalExamination(any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq("U"))).thenReturn(1);

			assertEquals(1, service.updateCancerGynecologicalExaminationDetails(gynecological));
		}

		@Test
		void updateCancerGynecologicalExaminationDetails_insertsAFreshRowWhenNoneIsStoredYet() {
			CancerGynecologicalExamination gynecological = new CancerGynecologicalExamination();
			CancerGynecologicalExamination stored = new CancerGynecologicalExamination();
			stored.setID(22L);
			when(cancerGynecologicalExaminationRepo.getCancerGynecologicalExaminationStatus(any(), any()))
					.thenReturn(null);
			when(cancerGynecologicalExaminationRepo.save(gynecological)).thenReturn(stored);

			assertEquals(1, service.updateCancerGynecologicalExaminationDetails(gynecological));
		}

		@Test
		void updateCancerExamImgAnotasnDetails_replacesTheAnnotationsOfEveryTouchedImage() {
			CancerExaminationImageAnnotation complete = new CancerExaminationImageAnnotation();
			complete.setCancerImageID(1);
			complete.setBeneficiaryRegID(1L);
			complete.setVisitCode(2L);
			complete.setxCoordinate(10);
			complete.setyCoordinate(20);
			complete.setCreatedBy("nurse");
			CancerExaminationImageAnnotation incomplete = new CancerExaminationImageAnnotation();
			incomplete.setCancerImageID(1);
			List<CancerExaminationImageAnnotation> annotations = Arrays.asList(complete, incomplete);

			when(cancerExaminationImageAnnotationRepo.getCancerExaminationImageAnnotationDetailsStatus(eq(1L), eq(2L),
					any())).thenReturn(statusRows(23L, "P"));
			when(cancerExaminationImageAnnotationRepo.deleteExistingImageAnnotationDetails(23L, "U")).thenReturn(1);
			when(cancerExaminationImageAnnotationRepo.saveAll(any()))
					.thenReturn(new ArrayList<>(Collections.singletonList(complete)));

			assertEquals(1, service.updateCancerExamImgAnotasnDetails(annotations));
			assertEquals("nurse", complete.getModifiedBy());
		}

		@Test
		void updateCancerExamImgAnotasnDetails_succeedsWhenNoImageWasAnnotated() {
			assertEquals(1, service.updateCancerExamImgAnotasnDetails(new ArrayList<>()));
		}

		@Test
		void updateCancerExamImgAnotasnDetails_reportsFailureWhenTheOldAnnotationsCouldNotBeCleared() {
			CancerExaminationImageAnnotation annotation = new CancerExaminationImageAnnotation();
			annotation.setCancerImageID(1);
			List<CancerExaminationImageAnnotation> annotations = Collections.singletonList(annotation);

			when(cancerExaminationImageAnnotationRepo.getCancerExaminationImageAnnotationDetailsStatus(any(), any(),
					any())).thenReturn(statusRows(24L, "N"));
			when(cancerExaminationImageAnnotationRepo.deleteExistingImageAnnotationDetails(24L, "N")).thenReturn(0);

			assertEquals(0, service.updateCancerExamImgAnotasnDetails(annotations));
		}
	}
}
