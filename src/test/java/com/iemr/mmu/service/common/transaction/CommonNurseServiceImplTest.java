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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
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

import com.iemr.mmu.data.anc.BenAllergyHistory;
import com.iemr.mmu.data.anc.BenFamilyHistory;
import com.iemr.mmu.data.anc.BenMedHistory;
import com.iemr.mmu.data.anc.BenMedicationHistory;
import com.iemr.mmu.data.anc.BenMenstrualDetails;
import com.iemr.mmu.data.anc.BenPersonalHabit;
import com.iemr.mmu.data.anc.BencomrbidityCondDetails;
import com.iemr.mmu.data.anc.ChildOptionalVaccineDetail;
import com.iemr.mmu.data.anc.ChildVaccineDetail1;
import com.iemr.mmu.data.anc.FemaleObstetricHistory;
import com.iemr.mmu.data.anc.PhyGeneralExamination;
import com.iemr.mmu.data.anc.PhyHeadToToeExamination;
import com.iemr.mmu.data.anc.SysCardiovascularExamination;
import com.iemr.mmu.data.anc.SysCentralNervousExamination;
import com.iemr.mmu.data.anc.SysGastrointestinalExamination;
import com.iemr.mmu.data.anc.SysGenitourinarySystemExamination;
import com.iemr.mmu.data.anc.SysMusculoskeletalSystemExamination;
import com.iemr.mmu.data.anc.SysRespiratoryExamination;
import com.iemr.mmu.data.anc.WrapperChildOptionalVaccineDetail;
import com.iemr.mmu.data.anc.WrapperComorbidCondDetails;
import com.iemr.mmu.data.anc.WrapperFemaleObstetricHistory;
import com.iemr.mmu.data.anc.WrapperImmunizationHistory;
import com.iemr.mmu.data.anc.WrapperMedicationHistory;
import com.iemr.mmu.data.login.Users;
import com.iemr.mmu.data.ncdScreening.IDRSData;
import com.iemr.mmu.data.ncdScreening.PhysicalActivityType;
import com.iemr.mmu.data.nurse.BenAnthropometryDetail;
import com.iemr.mmu.data.nurse.BenPhysicalVitalDetail;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.bmiCalculation.BMICalculationRepo;
import com.iemr.mmu.repo.login.UserLoginRepo;
import com.iemr.mmu.repo.nurse.BenAnthropometryRepo;
import com.iemr.mmu.repo.nurse.BenCancerVitalDetailRepo;
import com.iemr.mmu.repo.nurse.BenPhysicalVitalRepo;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;
import com.iemr.mmu.repo.nurse.anc.BenAdherenceRepo;
import com.iemr.mmu.repo.nurse.anc.BenAllergyHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.BenChildDevelopmentHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.BenFamilyHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.BenMedHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.BenMedicationHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.BenMenstrualDetailsRepo;
import com.iemr.mmu.repo.nurse.anc.BenPersonalHabitRepo;
import com.iemr.mmu.repo.nurse.anc.BencomrbidityCondRepo;
import com.iemr.mmu.repo.nurse.anc.ChildFeedingDetailsRepo;
import com.iemr.mmu.repo.nurse.anc.ChildOptionalVaccineDetailRepo;
import com.iemr.mmu.repo.nurse.anc.ChildVaccineDetail1Repo;
import com.iemr.mmu.repo.nurse.anc.FemaleObstetricHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.PerinatalHistoryRepo;
import com.iemr.mmu.repo.nurse.anc.PhyGeneralExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.PhyHeadToToeExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.SysCardiovascularExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.SysCentralNervousExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.SysGastrointestinalExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.SysGenitourinarySystemExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.SysMusculoskeletalSystemExaminationRepo;
import com.iemr.mmu.repo.nurse.anc.SysRespiratoryExaminationRepo;
import com.iemr.mmu.repo.nurse.ncdscreening.IDRSDataRepo;
import com.iemr.mmu.repo.nurse.ncdscreening.PhysicalActivityTypeRepo;
import com.iemr.mmu.repo.quickConsultation.BenChiefComplaintRepo;
import com.iemr.mmu.repo.quickConsultation.LabTestOrderDetailRepo;
import com.iemr.mmu.repo.quickConsultation.PrescribedDrugDetailRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenData;
import com.iemr.mmu.repo.registrar.ReistrarRepoBenSearch;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;
import com.iemr.mmu.utils.exception.IEMRException;

class CommonNurseServiceImplTest {

	@Mock
	private BenVisitDetailRepo benVisitDetailRepo;
	@Mock
	private UserLoginRepo userLoginRepo;
	@Mock
	private BenChiefComplaintRepo benChiefComplaintRepo;
	@Mock
	private BenMedHistoryRepo benMedHistoryRepo;
	@Mock
	private BencomrbidityCondRepo bencomrbidityCondRepo;
	@Mock
	private BenMedicationHistoryRepo benMedicationHistoryRepo;
	@Mock
	private FemaleObstetricHistoryRepo femaleObstetricHistoryRepo;
	@Mock
	private BenMenstrualDetailsRepo benMenstrualDetailsRepo;
	@Mock
	private BenFamilyHistoryRepo benFamilyHistoryRepo;
	@Mock
	private BenPersonalHabitRepo benPersonalHabitRepo;
	@Mock
	private BenAllergyHistoryRepo benAllergyHistoryRepo;
	@Mock
	private ChildOptionalVaccineDetailRepo childOptionalVaccineDetailRepo;
	@Mock
	private ChildVaccineDetail1Repo childVaccineDetail1Repo;
	@Mock
	private BenAnthropometryRepo benAnthropometryRepo;
	@Mock
	private BenPhysicalVitalRepo benPhysicalVitalRepo;
	@Mock
	private PhyGeneralExaminationRepo phyGeneralExaminationRepo;
	@Mock
	private PhyHeadToToeExaminationRepo phyHeadToToeExaminationRepo;
	@Mock
	private SysGastrointestinalExaminationRepo sysGastrointestinalExaminationRepo;
	@Mock
	private SysCardiovascularExaminationRepo sysCardiovascularExaminationRepo;
	@Mock
	private SysRespiratoryExaminationRepo sysRespiratoryExaminationRepo;
	@Mock
	private SysCentralNervousExaminationRepo sysCentralNervousExaminationRepo;
	@Mock
	private SysMusculoskeletalSystemExaminationRepo sysMusculoskeletalSystemExaminationRepo;
	@Mock
	private SysGenitourinarySystemExaminationRepo sysGenitourinarySystemExaminationRepo;
	@Mock
	private RegistrarRepoBenData registrarRepoBenData;
	@Mock
	private PrescriptionDetailRepo prescriptionDetailRepo;
	@Mock
	private LabTestOrderDetailRepo labTestOrderDetailRepo;
	@Mock
	private PrescribedDrugDetailRepo prescribedDrugDetailRepo;
	@Mock
	private ReistrarRepoBenSearch reistrarRepoBenSearch;
	@Mock
	private BenAdherenceRepo benAdherenceRepo;
	@Mock
	private BenChildDevelopmentHistoryRepo benChildDevelopmentHistoryRepo;
	@Mock
	private ChildFeedingDetailsRepo childFeedingDetailsRepo;
	@Mock
	private PerinatalHistoryRepo perinatalHistoryRepo;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Mock
	private BenCancerVitalDetailRepo benCancerVitalDetailRepo;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Mock
	private PhysicalActivityTypeRepo physicalActivityTypeRepo;
	@Mock
	private AESEncryptionDecryption aESEncryptionDecryption;
	@Mock
	private IDRSDataRepo iDRSDataRepo;
	@Mock
	private BMICalculationRepo bmiCalculationRepo;

	@InjectMocks
	private CommonNurseServiceImpl service;

	private AutoCloseable mocks;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("beneficiary visit details")
	class VisitDetails {

		@Test
		void saveBeneficiaryVisitDetails_incrementsVisitCountAndResolvesNurse() {
			BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail();
			detail.setBeneficiaryRegID(11L);
			detail.setCreatedBy("nurse1");
			detail.setFileIDs(new String[] { "a", "b" });

			Users user = new Users();
			user.setUserID(77L);
			when(userLoginRepo.getUserByUsername("nurse1")).thenReturn(user);
			when(benVisitDetailRepo.getVisitCountForBeneficiary(11L)).thenReturn((short) 3);

			BeneficiaryVisitDetail saved = new BeneficiaryVisitDetail();
			saved.setBenVisitID(99L);
			when(benVisitDetailRepo.save(any(BeneficiaryVisitDetail.class))).thenReturn(saved);

			assertEquals(99L, service.saveBeneficiaryVisitDetails(detail));
			assertEquals((short) 4, detail.getVisitNo());
			assertEquals("a,b,", detail.getReportFilePath());
			assertEquals(77L, detail.getNurseID());
			verify(benVisitDetailRepo).updateVanSerialNo(99L);
		}

		@Test
		void saveBeneficiaryVisitDetails_startsAtVisitOneWhenBeneficiaryHasNoHistory() {
			BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail();
			detail.setBeneficiaryRegID(11L);
			when(benVisitDetailRepo.getVisitCountForBeneficiary(11L)).thenReturn(null);

			BeneficiaryVisitDetail saved = new BeneficiaryVisitDetail();
			saved.setBenVisitID(5L);
			when(benVisitDetailRepo.save(any(BeneficiaryVisitDetail.class))).thenReturn(saved);

			assertEquals(5L, service.saveBeneficiaryVisitDetails(detail));
			assertEquals((short) 1, detail.getVisitNo());
			assertEquals("", detail.getReportFilePath());
			assertNull(detail.getNurseID());
		}

		@Test
		void saveBeneficiaryVisitDetails_leavesNurseUnresolvedWhenUsernameIsUnknown() {
			BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail();
			detail.setCreatedBy("  ");

			BeneficiaryVisitDetail saved = new BeneficiaryVisitDetail();
			saved.setBenVisitID(1L);
			when(benVisitDetailRepo.save(any(BeneficiaryVisitDetail.class))).thenReturn(saved);

			service.saveBeneficiaryVisitDetails(detail);
			assertNull(detail.getNurseID());
			verify(userLoginRepo, never()).getUserByUsername(anyString());
		}

		@Test
		void saveBeneficiaryVisitDetails_leavesNurseUnresolvedWhenLookupFindsNoUser() {
			BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail();
			detail.setCreatedBy("ghost");
			when(userLoginRepo.getUserByUsername("ghost")).thenReturn(null);

			BeneficiaryVisitDetail saved = new BeneficiaryVisitDetail();
			saved.setBenVisitID(1L);
			when(benVisitDetailRepo.save(any(BeneficiaryVisitDetail.class))).thenReturn(saved);

			service.saveBeneficiaryVisitDetails(detail);
			assertNull(detail.getNurseID());
		}

		@Test
		void getBenVisitCount_returnsNextVisitNumber() {
			when(benVisitDetailRepo.getVisitCountForBeneficiary(1L)).thenReturn((short) 2);
			assertEquals((short) 3, service.getBenVisitCount(1L));

			when(benVisitDetailRepo.getVisitCountForBeneficiary(2L)).thenReturn(null);
			assertEquals((short) 1, service.getBenVisitCount(2L));
		}

		@Test
		void updateBeneficiaryStatus_delegatesToRegistrarRepo() {
			when(registrarRepoBenData.updateBenFlowStatus('N', 4L)).thenReturn(1);
			assertEquals(1, service.updateBeneficiaryStatus('N', 4L));
		}

		@Test
		void getMaxCurrentdate_returnsZeroWhenNoPreviousVisitExists() throws Exception {
			when(benVisitDetailRepo.getMaxCreatedDate(1L, "reason", "category")).thenReturn(null);
			assertEquals(0, service.getMaxCurrentdate(1L, "reason", "category"));
		}

		@Test
		void getMaxCurrentdate_returnsPositiveWhileThePreviousVisitIsStillWithinTenMinutes() throws Exception {
			String recent = new java.sql.Timestamp(System.currentTimeMillis()).toString();
			when(benVisitDetailRepo.getMaxCreatedDate(1L, "reason", "category")).thenReturn(recent);
			assertTrue(service.getMaxCurrentdate(1L, "reason", "category") > 0);
		}

		@Test
		void getMaxCurrentdate_returnsNegativeOnceThePreviousVisitIsOlderThanTenMinutes() throws Exception {
			String old = new java.sql.Timestamp(System.currentTimeMillis() - 3600_000L).toString();
			when(benVisitDetailRepo.getMaxCreatedDate(1L, "reason", "category")).thenReturn(old);
			assertTrue(service.getMaxCurrentdate(1L, "reason", "category") < 0);
		}

		@Test
		void getMaxCurrentdate_wrapsAnUnparseableStoredDate() {
			when(benVisitDetailRepo.getMaxCreatedDate(1L, "r", "c")).thenReturn("not-a-date.0");
			IEMRException thrown = assertThrows(IEMRException.class,
					() -> service.getMaxCurrentdate(1L, "r", "c"));
			assertTrue(thrown.getMessage().contains("Error while parseing created date"));
		}

		@Test
		void generateVisitCode_padsVanAndVisitIdsIntoAFourteenDigitCode() {
			when(benVisitDetailRepo.updateVisitCode(anyLong(), anyLong())).thenReturn(1);
			assertEquals(Long.valueOf("10000100000123"), service.generateVisitCode(123L, 1, 1));
		}

		@Test
		void generateVisitCode_returnsZeroWhenTheCodeCouldNotBeStored() {
			when(benVisitDetailRepo.updateVisitCode(anyLong(), anyLong())).thenReturn(0);
			assertEquals(0L, service.generateVisitCode(123L, 1, 1));
		}

		@Test
		void updateVisitCodeInVisitDetailsTable_delegatesToRepo() {
			when(benVisitDetailRepo.updateVisitCode(5L, 6L)).thenReturn(1);
			assertEquals(1, service.updateVisitCodeInVisitDetailsTable(5L, 6L));
		}

		@Test
		void updateBeneficiaryVisitDetails_returnsRepoResult() {
			BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail();
			when(benVisitDetailRepo.updateBeneficiaryVisitDetail(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any())).thenReturn(1);
			assertEquals(1, service.updateBeneficiaryVisitDetails(detail));
		}

		@Test
		void updateBeneficiaryVisitDetails_returnsZeroWhenTheRepoFails() {
			BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail();
			when(benVisitDetailRepo.updateBeneficiaryVisitDetail(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any())).thenThrow(new RuntimeException("db down"));
			assertEquals(0, service.updateBeneficiaryVisitDetails(detail));
		}

		@Test
		void getCSVisitDetails_returnsNullWhenTheVisitDoesNotExist() throws Exception {
			when(benVisitDetailRepo.getVisitDetails(1L, 2L)).thenReturn(null);
			assertNull(service.getCSVisitDetails(1L, 2L));
		}

		@Test
		void getCSVisitDetails_decryptsEachAttachedReportPath() throws Exception {
			BeneficiaryVisitDetail stored = new BeneficiaryVisitDetail();
			stored.setBenVisitID(1L);
			stored.setReportFilePath("enc1,,enc2");
			when(benVisitDetailRepo.getVisitDetails(1L, 2L)).thenReturn(stored);
			when(aESEncryptionDecryption.decrypt("enc1")).thenReturn("/tmp/reports/first.pdf");
			when(aESEncryptionDecryption.decrypt("enc2")).thenReturn("/tmp/reports/second.pdf");

			BeneficiaryVisitDetail result = service.getCSVisitDetails(1L, 2L);

			assertNotNull(result);
			assertEquals(2, result.getFiles().size());
			assertEquals("first.pdf", result.getFiles().get(0).get("fileName"));
			assertEquals("enc2", result.getFiles().get(1).get("filePath"));
		}

		@Test
		void getCSVisitDetails_returnsAnEmptyFileListWhenNoReportsAreAttached() throws Exception {
			BeneficiaryVisitDetail stored = new BeneficiaryVisitDetail();
			stored.setBenVisitID(1L);
			stored.setReportFilePath("   ");
			when(benVisitDetailRepo.getVisitDetails(1L, 2L)).thenReturn(stored);

			assertTrue(service.getCSVisitDetails(1L, 2L).getFiles().isEmpty());
		}
	}

	@Nested
	@DisplayName("history and examination saves")
	class HistorySaves {

		@Test
		void saveBenChiefComplaints_skipsEntriesWithoutAComplaintId() {
			BenChiefComplaint withId = new BenChiefComplaint();
			withId.setChiefComplaintID(1);
			withId.setBenChiefComplaintID(10L);
			BenChiefComplaint withoutId = new BenChiefComplaint();

			when(benChiefComplaintRepo.saveAll(any())).thenReturn(Collections.singletonList(withId));

			assertEquals(1, service.saveBenChiefComplaints(Arrays.asList(withId, withoutId)));
			verify(benChiefComplaintRepo).updateVanSerialNo(10L);
		}

		@Test
		void saveBenChiefComplaints_succeedsWhenThereIsNothingToSave() {
			assertEquals(1, service.saveBenChiefComplaints(new ArrayList<>()));
			verify(benChiefComplaintRepo, never()).saveAll(any());
		}

		@Test
		void saveBenChiefComplaints_reportsFailureWhenNotEveryComplaintWasSaved() {
			BenChiefComplaint withId = new BenChiefComplaint();
			withId.setChiefComplaintID(1);
			when(benChiefComplaintRepo.saveAll(any())).thenReturn(new ArrayList<>());
			assertEquals(0, service.saveBenChiefComplaints(Collections.singletonList(withId)));
		}

		@Test
		void saveBenPastHistory_savesEveryPastHistoryEntry() {
			BenMedHistory history = mock(BenMedHistory.class);
			ArrayList<BenMedHistory> entries = new ArrayList<>(Collections.singletonList(new BenMedHistory()));
			when(history.getBenPastHistory()).thenReturn(entries);
			when(benMedHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveBenPastHistory(history));
		}

		@Test
		void saveBenPastHistory_succeedsWhenThereIsNoPastHistory() {
			BenMedHistory history = mock(BenMedHistory.class);
			when(history.getBenPastHistory()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveBenPastHistory(history));
		}

		@Test
		void saveBenPastHistory_reportsFailureWhenNotEveryEntryWasSaved() {
			BenMedHistory history = mock(BenMedHistory.class);
			ArrayList<BenMedHistory> entries = new ArrayList<>(Collections.singletonList(new BenMedHistory()));
			when(history.getBenPastHistory()).thenReturn(entries);
			when(benMedHistoryRepo.saveAll(entries)).thenReturn(new ArrayList<BenMedHistory>());
			assertNull(service.saveBenPastHistory(history));
		}

		@Test
		void saveBenComorbidConditions_returnsTheIdOfTheFirstStoredCondition() {
			WrapperComorbidCondDetails wrapper = mock(WrapperComorbidCondDetails.class);
			BencomrbidityCondDetails stored = new BencomrbidityCondDetails();
			stored.setID(42L);
			ArrayList<BencomrbidityCondDetails> entries = new ArrayList<>(Collections.singletonList(stored));
			when(wrapper.getComrbidityConds()).thenReturn(entries);
			when(bencomrbidityCondRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(42L, service.saveBenComorbidConditions(wrapper));
		}

		@Test
		void saveBenComorbidConditions_succeedsWhenThereAreNoConditions() {
			WrapperComorbidCondDetails wrapper = mock(WrapperComorbidCondDetails.class);
			when(wrapper.getComrbidityConds()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveBenComorbidConditions(wrapper));
		}

		@Test
		void saveBenMedicationHistory_returnsTheIdOfTheFirstStoredEntry() {
			WrapperMedicationHistory wrapper = mock(WrapperMedicationHistory.class);
			BenMedicationHistory stored = new BenMedicationHistory();
			stored.setID(7L);
			ArrayList<BenMedicationHistory> entries = new ArrayList<>(Collections.singletonList(stored));
			when(wrapper.getBenMedicationHistoryDetails()).thenReturn(entries);
			when(benMedicationHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(7L, service.saveBenMedicationHistory(wrapper));
		}

		@Test
		void saveBenMedicationHistory_succeedsWhenThereIsNoMedicationHistory() {
			WrapperMedicationHistory wrapper = mock(WrapperMedicationHistory.class);
			when(wrapper.getBenMedicationHistoryDetails()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveBenMedicationHistory(wrapper));
		}

		@Test
		void saveFemaleObstetricHistory_flattensEveryComplicationListOntoTheStoredRow() {
			FemaleObstetricHistory entry = new FemaleObstetricHistory();
			entry.setPregComplicationList(complications("pregComplicationID", "pregComplicationType"));
			entry.setDeliveryComplicationList(complications("deliveryComplicationID", "deliveryComplicationType"));
			entry.setPostpartumComplicationList(
					complications("postpartumComplicationID", "postpartumComplicationType"));

			ArrayList<Map<String, Object>> postAbortion = new ArrayList<>();
			postAbortion.add(complication(1d, "first"));
			postAbortion.add(complication(2d, "second"));
			entry.setPostAbortionComplication(postAbortion);
			entry.setAbortionType(complication(3d, "abortion"));

			Map<String, Object> facility = new HashMap<>();
			facility.put("serviceFacilityID", 4d);
			facility.put("facilityName", "PHC");
			entry.setTypeofFacility(facility);

			WrapperFemaleObstetricHistory wrapper = mock(WrapperFemaleObstetricHistory.class);
			ArrayList<FemaleObstetricHistory> entries = new ArrayList<>(Collections.singletonList(entry));
			when(wrapper.getFemaleObstetricHistoryDetails()).thenReturn(entries);
			when(femaleObstetricHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveFemaleObstetricHistory(wrapper));
			assertEquals("11,12", entry.getPregComplicationID());
			assertEquals("name11,name12", entry.getPregComplicationType());
			assertEquals("11,12", entry.getDeliveryComplicationID());
			assertEquals("11,12", entry.getPostpartumComplicationID());
			assertEquals("1,2", entry.getPostAbortionComplication_db());
			assertEquals("first,second", entry.getPostAbortionComplicationsValues());
			assertEquals(3, entry.getAbortionTypeID());
			assertEquals("abortion", entry.getTypeOfAbortionValue());
			assertEquals(4, entry.getTypeofFacilityID());
			assertEquals("PHC", entry.getServiceFacilityValue());
		}

		@Test
		void saveFemaleObstetricHistory_leavesComplicationFieldsEmptyWhenNoneWereReported() {
			FemaleObstetricHistory entry = new FemaleObstetricHistory();
			WrapperFemaleObstetricHistory wrapper = mock(WrapperFemaleObstetricHistory.class);
			ArrayList<FemaleObstetricHistory> entries = new ArrayList<>(Collections.singletonList(entry));
			when(wrapper.getFemaleObstetricHistoryDetails()).thenReturn(entries);
			when(femaleObstetricHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveFemaleObstetricHistory(wrapper));
			assertEquals("", entry.getPregComplicationID());
			assertNull(entry.getPostAbortionComplication_db());
		}

		@Test
		void saveFemaleObstetricHistory_succeedsWhenThereIsNoObstetricHistory() {
			WrapperFemaleObstetricHistory wrapper = mock(WrapperFemaleObstetricHistory.class);
			when(wrapper.getFemaleObstetricHistoryDetails()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveFemaleObstetricHistory(wrapper));
		}

		private ArrayList<Map<String, Object>> complications(String idKey, String nameKey) {
			ArrayList<Map<String, Object>> list = new ArrayList<>();
			for (int i = 11; i <= 12; i++) {
				Map<String, Object> item = new HashMap<>();
				item.put(idKey, i);
				item.put(nameKey, "name" + i);
				list.add(item);
			}
			return list;
		}

		private Map<String, Object> complication(Double id, String value) {
			Map<String, Object> map = new HashMap<>();
			map.put("complicationID", id);
			map.put("complicationValue", value);
			return map;
		}

		@Test
		void saveBenMenstrualHistory_flattensTheReportedProblemsBeforeSaving() {
			BenMenstrualDetails details = new BenMenstrualDetails();
			ArrayList<Map<String, Object>> problems = new ArrayList<>();
			for (int i = 1; i <= 2; i++) {
				Map<String, Object> problem = new HashMap<>();
				problem.put("menstrualProblemID", i);
				problem.put("problemName", "problem" + i);
				problems.add(problem);
			}
			details.setMenstrualProblemList(problems);

			BenMenstrualDetails stored = new BenMenstrualDetails();
			stored.setBenMenstrualID(9);
			when(benMenstrualDetailsRepo.save(details)).thenReturn(stored);

			assertEquals(9, service.saveBenMenstrualHistory(details));
			assertEquals("1,2", details.getMenstrualProblemID());
			assertEquals("problem1,problem2", details.getProblemName());
		}

		@Test
		void saveBenMenstrualHistory_returnsNullWhenTheRowWasNotPersisted() {
			BenMenstrualDetails details = new BenMenstrualDetails();
			BenMenstrualDetails stored = new BenMenstrualDetails();
			stored.setBenMenstrualID(0);
			when(benMenstrualDetailsRepo.save(details)).thenReturn(stored);
			assertNull(service.saveBenMenstrualHistory(details));
		}

		@Test
		void saveBenFamilyHistory_savesEveryReportedFamilyDisease() {
			BenFamilyHistory input = mock(BenFamilyHistory.class);
			ArrayList<BenFamilyHistory> entries = new ArrayList<>(Collections.singletonList(new BenFamilyHistory()));
			when(input.getBenFamilyHistory()).thenReturn(entries);
			when(benFamilyHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveBenFamilyHistory(input));
		}

		@Test
		void saveBenFamilyHistory_succeedsWhenThereIsNoFamilyHistory() {
			BenFamilyHistory input = mock(BenFamilyHistory.class);
			when(input.getBenFamilyHistory()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveBenFamilyHistory(input));
		}

		@Test
		void saveBenFamilyHistoryNCDScreening_savesTheScreeningVariantOfTheList() {
			BenFamilyHistory input = mock(BenFamilyHistory.class);
			ArrayList<BenFamilyHistory> entries = new ArrayList<>(Collections.singletonList(new BenFamilyHistory()));
			when(input.getBenFamilyHist()).thenReturn(entries);
			when(benFamilyHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveBenFamilyHistoryNCDScreening(input));
		}

		@Test
		void saveBenFamilyHistoryNCDScreening_succeedsWhenNothingWasScreened() {
			BenFamilyHistory input = mock(BenFamilyHistory.class);
			when(input.getBenFamilyHist()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveBenFamilyHistoryNCDScreening(input));
		}

		@Test
		void savePersonalHistory_savesEveryReportedHabit() {
			BenPersonalHabit input = mock(BenPersonalHabit.class);
			ArrayList<BenPersonalHabit> entries = new ArrayList<>(Collections.singletonList(new BenPersonalHabit()));
			when(input.getPersonalHistory()).thenReturn(entries);
			when(benPersonalHabitRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.savePersonalHistory(input));
		}

		@Test
		void savePersonalHistory_succeedsWhenNoHabitsWereReported() {
			BenPersonalHabit input = mock(BenPersonalHabit.class);
			when(input.getPersonalHistory()).thenReturn(new ArrayList<>());
			assertEquals(1, service.savePersonalHistory(input));
		}

		@Test
		void saveAllergyHistory_savesEveryReportedAllergy() {
			BenAllergyHistory input = mock(BenAllergyHistory.class);
			ArrayList<BenAllergyHistory> entries = new ArrayList<>(Collections.singletonList(new BenAllergyHistory()));
			when(input.getBenAllergicHistory()).thenReturn(entries);
			when(benAllergyHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveAllergyHistory(input));
		}

		@Test
		void saveAllergyHistory_succeedsWhenNoAllergiesWereReported() {
			BenAllergyHistory input = mock(BenAllergyHistory.class);
			when(input.getBenAllergicHistory()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveAllergyHistory(input));
		}

		@Test
		void saveChildOptionalVaccineDetail_savesEveryOptionalVaccine() {
			WrapperChildOptionalVaccineDetail wrapper = mock(WrapperChildOptionalVaccineDetail.class);
			ArrayList<ChildOptionalVaccineDetail> entries = new ArrayList<>(
					Collections.singletonList(new ChildOptionalVaccineDetail()));
			when(wrapper.getChildOptionalVaccineDetails()).thenReturn(entries);
			when(childOptionalVaccineDetailRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1L, service.saveChildOptionalVaccineDetail(wrapper));
		}

		@Test
		void saveChildOptionalVaccineDetail_succeedsWhenNoOptionalVaccineWasGiven() {
			WrapperChildOptionalVaccineDetail wrapper = mock(WrapperChildOptionalVaccineDetail.class);
			when(wrapper.getChildOptionalVaccineDetails()).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveChildOptionalVaccineDetail(wrapper));
		}

		@Test
		void saveImmunizationHistory_returnsTheIdOfTheFirstStoredVaccine() {
			WrapperImmunizationHistory wrapper = mock(WrapperImmunizationHistory.class);
			ChildVaccineDetail1 stored = new ChildVaccineDetail1();
			stored.setID(3L);
			ArrayList<ChildVaccineDetail1> entries = new ArrayList<>(Collections.singletonList(stored));
			when(wrapper.getBenChildVaccineDetails()).thenReturn(entries);
			when(childVaccineDetail1Repo.saveAll(entries)).thenReturn(entries);

			assertEquals(3L, service.saveImmunizationHistory(wrapper));
		}

		@Test
		void saveImmunizationHistory_returnsNullWhenNoVaccineWasStored() {
			WrapperImmunizationHistory wrapper = mock(WrapperImmunizationHistory.class);
			ArrayList<ChildVaccineDetail1> entries = new ArrayList<>();
			when(wrapper.getBenChildVaccineDetails()).thenReturn(entries);
			when(childVaccineDetail1Repo.saveAll(entries)).thenReturn(entries);

			assertNull(service.saveImmunizationHistory(wrapper));
		}
	}

	@Nested
	@DisplayName("vitals and anthropometry")
	class Vitals {

		@Test
		void saveBeneficiaryPhysicalAnthropometryDetails_returnsTheStoredId() {
			BenAnthropometryDetail detail = new BenAnthropometryDetail();
			BenAnthropometryDetail stored = new BenAnthropometryDetail();
			stored.setID(5L);
			when(benAnthropometryRepo.save(detail)).thenReturn(stored);
			assertEquals(5L, service.saveBeneficiaryPhysicalAnthropometryDetails(detail));
		}

		@Test
		void saveBeneficiaryPhysicalVitalDetails_averagesEveryBloodPressureReadingTaken() {
			BenPhysicalVitalDetail detail = new BenPhysicalVitalDetail();
			detail.setSystolicBP_1stReading((short) 120);
			detail.setDiastolicBP_1stReading((short) 80);
			detail.setSystolicBP_2ndReading((short) 130);
			detail.setDiastolicBP_2ndReading((short) 90);
			detail.setSystolicBP_3rdReading((short) 140);
			detail.setDiastolicBP_3rdReading((short) 100);

			BenPhysicalVitalDetail stored = new BenPhysicalVitalDetail();
			stored.setID(8L);
			when(benPhysicalVitalRepo.save(detail)).thenReturn(stored);

			assertEquals(8L, service.saveBeneficiaryPhysicalVitalDetails(detail));
			assertEquals((short) 130, detail.getAverageSystolicBP());
			assertEquals((short) 90, detail.getAverageDiastolicBP());
		}

		@Test
		void saveBeneficiaryPhysicalVitalDetails_leavesTheAverageUnsetWhenNoReadingWasTaken() {
			BenPhysicalVitalDetail detail = new BenPhysicalVitalDetail();
			when(benPhysicalVitalRepo.save(detail)).thenReturn(null);

			assertNull(service.saveBeneficiaryPhysicalVitalDetails(detail));
			assertNull(detail.getAverageSystolicBP());
		}

		@Test
		void getBeneficiaryPhysicalAnthropometryDetails_serialisesTheStoredRow() {
			BenAnthropometryDetail stored = new BenAnthropometryDetail();
			stored.setID(1L);
			when(benAnthropometryRepo.getBenAnthropometryDetail(1L, 2L)).thenReturn(stored);
			assertTrue(service.getBeneficiaryPhysicalAnthropometryDetails(1L, 2L).contains("\"ID\":1"));
		}

		@Test
		void getBeneficiaryPhysicalVitalDetails_serialisesTheStoredRow() {
			BenPhysicalVitalDetail stored = new BenPhysicalVitalDetail();
			stored.setID(2L);
			when(benPhysicalVitalRepo.getBenPhysicalVitalDetail(1L, 2L)).thenReturn(stored);
			assertTrue(service.getBeneficiaryPhysicalVitalDetails(1L, 2L).contains("\"ID\":2"));
		}

		@Test
		void updateANCAnthropometryDetails_marksAnAlreadySyncedRowAsUpdated() {
			BenAnthropometryDetail detail = new BenAnthropometryDetail();
			detail.setBeneficiaryRegID(1L);
			detail.setVisitCode(2L);
			when(benAnthropometryRepo.getBenAnthropometryStatus(1L, 2L)).thenReturn("P");
			when(benAnthropometryRepo.updateANCCareDetails(any(), any(), any(), any(), any(), any(), any(), any(),
					any(), org.mockito.ArgumentMatchers.eq("U"), anyLong(), anyLong())).thenReturn(1);

			assertEquals(1, service.updateANCAnthropometryDetails(detail));
		}

		@Test
		void updateANCAnthropometryDetails_keepsARowThatWasNeverSyncedAsNew() {
			BenAnthropometryDetail detail = new BenAnthropometryDetail();
			detail.setBeneficiaryRegID(1L);
			detail.setVisitCode(2L);
			when(benAnthropometryRepo.getBenAnthropometryStatus(1L, 2L)).thenReturn("N");
			when(benAnthropometryRepo.updateANCCareDetails(any(), any(), any(), any(), any(), any(), any(), any(),
					any(), org.mockito.ArgumentMatchers.eq("N"), anyLong(), anyLong())).thenReturn(1);

			assertEquals(1, service.updateANCAnthropometryDetails(detail));
		}

		@Test
		void updateANCAnthropometryDetails_doesNothingWithoutARow() {
			assertEquals(0, service.updateANCAnthropometryDetails(null));
		}

		@Test
		void updateANCPhysicalVitalDetails_copiesTheFirstReadingIntoTheAverage() {
			BenPhysicalVitalDetail detail = new BenPhysicalVitalDetail();
			detail.setBeneficiaryRegID(1L);
			detail.setVisitCode(2L);
			detail.setSystolicBP_1stReading((short) 118);
			detail.setDiastolicBP_1stReading((short) 76);
			when(benPhysicalVitalRepo.getBenPhysicalVitalStatus(1L, 2L)).thenReturn("P");
			when(benPhysicalVitalRepo.updatePhysicalVitalDetails(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
					anyString(), any(), any(), any(), anyLong(), anyLong())).thenReturn(1);

			assertEquals(1, service.updateANCPhysicalVitalDetails(detail));
			assertEquals((short) 118, detail.getAverageSystolicBP());
			assertEquals((short) 76, detail.getAverageDiastolicBP());
		}

		@Test
		void updateANCPhysicalVitalDetails_doesNothingWithoutARow() {
			assertEquals(0, service.updateANCPhysicalVitalDetails(null));
		}

		@Test
		void saveIDRS_returnsTheStoredId() {
			IDRSData data = new IDRSData();
			IDRSData stored = new IDRSData();
			stored.setId(4L);
			when(iDRSDataRepo.save(data)).thenReturn(stored);
			assertEquals(4L, service.saveIDRS(data));
		}

		@Test
		void saveIDRS_returnsNullWhenNothingWasStored() {
			IDRSData data = new IDRSData();
			when(iDRSDataRepo.save(data)).thenReturn(null);
			assertNull(service.saveIDRS(data));
		}

		@Test
		void savePhysicalActivity_returnsTheStoredId() {
			PhysicalActivityType activity = new PhysicalActivityType();
			PhysicalActivityType stored = new PhysicalActivityType();
			stored.setpAID(6L);
			when(physicalActivityTypeRepo.save(activity)).thenReturn(stored);
			assertEquals(6L, service.savePhysicalActivity(activity));
		}

		@Test
		void savePhysicalActivity_returnsNullWhenNothingWasStored() {
			PhysicalActivityType activity = new PhysicalActivityType();
			when(physicalActivityTypeRepo.save(activity)).thenReturn(null);
			assertNull(service.savePhysicalActivity(activity));
		}
	}

	@Nested
	@DisplayName("examination saves")
	class ExaminationSaves {

		@Test
		void savePhyGeneralExamination_joinsTheReportedDangerSigns() {
			PhyGeneralExamination examination = new PhyGeneralExamination();
			examination.setTypeOfDangerSigns(new ArrayList<>(Arrays.asList("fever", "bleeding")));
			PhyGeneralExamination stored = new PhyGeneralExamination();
			stored.setID(1L);
			when(phyGeneralExaminationRepo.save(examination)).thenReturn(stored);

			assertEquals(1L, service.savePhyGeneralExamination(examination));
			assertEquals("fever,bleeding,", examination.getTypeOfDangerSign());
		}

		@Test
		void savePhyGeneralExamination_leavesDangerSignsUnsetWhenNoneWereReported() {
			PhyGeneralExamination examination = new PhyGeneralExamination();
			when(phyGeneralExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.savePhyGeneralExamination(examination));
			assertNull(examination.getTypeOfDangerSign());
		}

		@Test
		void savePhyHeadToToeExamination_returnsTheStoredId() {
			PhyHeadToToeExamination examination = new PhyHeadToToeExamination();
			PhyHeadToToeExamination stored = new PhyHeadToToeExamination();
			stored.setID(2L);
			when(phyHeadToToeExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(2L, service.savePhyHeadToToeExamination(examination));

			when(phyHeadToToeExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.savePhyHeadToToeExamination(examination));
		}

		@Test
		void saveSysGastrointestinalExamination_returnsTheStoredId() {
			SysGastrointestinalExamination examination = new SysGastrointestinalExamination();
			SysGastrointestinalExamination stored = new SysGastrointestinalExamination();
			stored.setID(3L);
			when(sysGastrointestinalExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(3L, service.saveSysGastrointestinalExamination(examination));

			when(sysGastrointestinalExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.saveSysGastrointestinalExamination(examination));
		}

		@Test
		void saveSysCardiovascularExamination_returnsTheStoredId() {
			SysCardiovascularExamination examination = new SysCardiovascularExamination();
			SysCardiovascularExamination stored = new SysCardiovascularExamination();
			stored.setID(4L);
			when(sysCardiovascularExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(4L, service.saveSysCardiovascularExamination(examination));

			when(sysCardiovascularExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.saveSysCardiovascularExamination(examination));
		}

		@Test
		void saveSysRespiratoryExamination_returnsTheStoredId() {
			SysRespiratoryExamination examination = new SysRespiratoryExamination();
			SysRespiratoryExamination stored = new SysRespiratoryExamination();
			stored.setID(5L);
			when(sysRespiratoryExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(5L, service.saveSysRespiratoryExamination(examination));

			when(sysRespiratoryExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.saveSysRespiratoryExamination(examination));
		}

		@Test
		void saveSysCentralNervousExamination_returnsTheStoredId() {
			SysCentralNervousExamination examination = new SysCentralNervousExamination();
			SysCentralNervousExamination stored = new SysCentralNervousExamination();
			stored.setID(6L);
			when(sysCentralNervousExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(6L, service.saveSysCentralNervousExamination(examination));

			when(sysCentralNervousExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.saveSysCentralNervousExamination(examination));
		}

		@Test
		void saveSysMusculoskeletalSystemExamination_returnsTheStoredId() {
			SysMusculoskeletalSystemExamination examination = new SysMusculoskeletalSystemExamination();
			SysMusculoskeletalSystemExamination stored = new SysMusculoskeletalSystemExamination();
			stored.setID(7L);
			when(sysMusculoskeletalSystemExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(7L, service.saveSysMusculoskeletalSystemExamination(examination));

			when(sysMusculoskeletalSystemExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.saveSysMusculoskeletalSystemExamination(examination));
		}

		@Test
		void saveSysGenitourinarySystemExamination_returnsTheStoredId() {
			SysGenitourinarySystemExamination examination = new SysGenitourinarySystemExamination();
			SysGenitourinarySystemExamination stored = new SysGenitourinarySystemExamination();
			stored.setID(8L);
			when(sysGenitourinarySystemExaminationRepo.save(examination)).thenReturn(stored);
			assertEquals(8L, service.saveSysGenitourinarySystemExamination(examination));

			when(sysGenitourinarySystemExaminationRepo.save(examination)).thenReturn(null);
			assertNull(service.saveSysGenitourinarySystemExamination(examination));
		}
	}

	@Nested
	@DisplayName("beneficiary history tables")
	class HistoryTables {

		/** A stored row wide enough for any of the history mappers, with no values set. */
		private ArrayList<Object[]> oneEmptyRow() {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[20]);
			return rows;
		}

		private void assertTableWithOneRow(String json) {
			assertTrue(json.contains("\"columns\""), "the table must describe its columns: " + json);
			assertTrue(json.contains("\"data\":[{"), "the stored row must be mapped into data: " + json);
		}

		private void assertEmptyTable(String json) {
			assertTrue(json.contains("\"columns\""), "the table must describe its columns even when empty: " + json);
			assertTrue(json.contains("\"data\":[]"), "an empty result must map to no data: " + json);
		}

		@Test
		void fetchBenPastMedicalHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benMedHistoryRepo.getBenPastHistory(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPastMedicalHistory(1L));

			when(benMedHistoryRepo.getBenPastHistory(2L)).thenReturn(new ArrayList<>());
			assertEmptyTable(service.fetchBenPastMedicalHistory(2L));

			when(benMedHistoryRepo.getBenPastHistory(3L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPastMedicalHistory(3L));
		}

		@Test
		void fetchBenPersonalTobaccoHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benPersonalHabitRepo.getBenPersonalTobaccoHabitDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPersonalTobaccoHistory(1L));

			when(benPersonalHabitRepo.getBenPersonalTobaccoHabitDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPersonalTobaccoHistory(2L));
		}

		@Test
		void fetchBenPersonalAlcoholHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benPersonalHabitRepo.getBenPersonalAlcoholHabitDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPersonalAlcoholHistory(1L));

			when(benPersonalHabitRepo.getBenPersonalAlcoholHabitDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPersonalAlcoholHistory(2L));
		}

		@Test
		void fetchBenPersonalAllergyHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benAllergyHistoryRepo.getBenPersonalAllergyDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPersonalAllergyHistory(1L));

			when(benAllergyHistoryRepo.getBenPersonalAllergyDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPersonalAllergyHistory(2L));
		}

		@Test
		void fetchBenPersonalMedicationHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benMedicationHistoryRepo.getBenMedicationHistoryDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPersonalMedicationHistory(1L));

			when(benMedicationHistoryRepo.getBenMedicationHistoryDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPersonalMedicationHistory(2L));
		}

		@Test
		void fetchBenPersonalFamilyHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benFamilyHistoryRepo.getBenFamilyHistoryDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPersonalFamilyHistory(1L));

			when(benFamilyHistoryRepo.getBenFamilyHistoryDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPersonalFamilyHistory(2L));
		}

		@Test
		void fetchBenPhysicalHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(physicalActivityTypeRepo.getBenPhysicalHistoryDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPhysicalHistory(1L));

			when(physicalActivityTypeRepo.getBenPhysicalHistoryDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPhysicalHistory(2L));
		}

		@Test
		void fetchBenMenstrualHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benMenstrualDetailsRepo.getBenMenstrualDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenMenstrualHistory(1L));

			when(benMenstrualDetailsRepo.getBenMenstrualDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenMenstrualHistory(2L));
		}

		@Test
		void fetchBenPastObstetricHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(femaleObstetricHistoryRepo.getBenFemaleObstetricHistoryDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPastObstetricHistory(1L));

			when(femaleObstetricHistoryRepo.getBenFemaleObstetricHistoryDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPastObstetricHistory(2L));
		}

		@Test
		void fetchBenComorbidityHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(bencomrbidityCondRepo.getBencomrbidityCondDetails(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenComorbidityHistory(1L));

			when(bencomrbidityCondRepo.getBencomrbidityCondDetails(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenComorbidityHistory(2L));
		}

		@Test
		void fetchBenImmunizationHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(childVaccineDetail1Repo.getBenChildVaccineDetails(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenImmunizationHistory(1L));

			when(childVaccineDetail1Repo.getBenChildVaccineDetails(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenImmunizationHistory(2L));
		}

		@Test
		void fetchBenOptionalVaccineHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(childOptionalVaccineDetailRepo.getBenOptionalVaccineDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenOptionalVaccineHistory(1L));

			when(childOptionalVaccineDetailRepo.getBenOptionalVaccineDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenOptionalVaccineHistory(2L));
		}

		@Test
		void fetchBenPerinatalHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(perinatalHistoryRepo.getBenPerinatalDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenPerinatalHistory(1L));

			when(perinatalHistoryRepo.getBenPerinatalDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenPerinatalHistory(2L));
		}

		@Test
		void fetchBenFeedingHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(childFeedingDetailsRepo.getBenFeedingHistoryDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenFeedingHistory(1L));

			when(childFeedingDetailsRepo.getBenFeedingHistoryDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenFeedingHistory(2L));
		}

		@Test
		void fetchBenDevelopmentHistory_mapsStoredRowsAndTolratesNoHistory() {
			when(benChildDevelopmentHistoryRepo.getBenDevelopmentHistoryDetail(1L)).thenReturn(oneEmptyRow());
			assertTableWithOneRow(service.fetchBenDevelopmentHistory(1L));

			when(benChildDevelopmentHistoryRepo.getBenDevelopmentHistoryDetail(2L)).thenReturn(null);
			assertEmptyTable(service.fetchBenDevelopmentHistory(2L));
		}
	}

	@Nested
	@DisplayName("history updates")
	class HistoryUpdates {

		/** The (id, processed) pairs the update methods read before deleting the old rows. */
		private ArrayList<Object[]> statusRows(Object id, String processed) {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[] { id, processed });
			return rows;
		}

		@Test
		void updateBenChiefComplaints_replacesTheExistingComplaints() {
			BenChiefComplaint complaint = new BenChiefComplaint();
			complaint.setBeneficiaryRegID(1L);
			complaint.setVisitCode(2L);
			complaint.setBenChiefComplaintID(3L);
			List<BenChiefComplaint> complaints = Collections.singletonList(complaint);
			when(benChiefComplaintRepo.saveAll(complaints)).thenReturn(complaints);

			assertEquals(1, service.updateBenChiefComplaints(complaints));
			verify(benChiefComplaintRepo).deleteExistingBenChiefComplaints(1L, 2L);
			verify(benChiefComplaintRepo).updateVanSerialNo(3L);
		}

		@Test
		void updateBenChiefComplaints_doesNothingWhenNoComplaintWasSent() {
			assertEquals(0, service.updateBenChiefComplaints(null));
			assertEquals(0, service.updateBenChiefComplaints(new ArrayList<>()));
		}

		@Test
		void updateBenChiefComplaints_reportsFailureWhenNothingWasStored() {
			BenChiefComplaint complaint = new BenChiefComplaint();
			List<BenChiefComplaint> complaints = Collections.singletonList(complaint);
			when(benChiefComplaintRepo.saveAll(complaints)).thenReturn(new ArrayList<>());
			assertEquals(0, service.updateBenChiefComplaints(complaints));
		}

		@Test
		void updateBenPastHistoryDetails_marksAlreadySyncedRowsAsUpdatedBeforeReplacingThem() throws Exception {
			BenMedHistory history = mock(BenMedHistory.class);
			when(history.getBeneficiaryRegID()).thenReturn(1L);
			when(history.getVisitCode()).thenReturn(2L);
			when(benMedHistoryRepo.getBenMedHistoryStatus(1L, 2L)).thenReturn(statusRows(5L, "P"));
			ArrayList<BenMedHistory> entries = new ArrayList<>(Collections.singletonList(new BenMedHistory()));
			when(history.getBenPastHistory()).thenReturn(entries);
			when(benMedHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateBenPastHistoryDetails(history));
			verify(benMedHistoryRepo).deleteExistingBenMedHistory(5L, "U");
		}

		@Test
		void updateBenPastHistoryDetails_keepsNeverSyncedRowsMarkedAsNew() throws Exception {
			BenMedHistory history = mock(BenMedHistory.class);
			when(benMedHistoryRepo.getBenMedHistoryStatus(any(), any())).thenReturn(statusRows(5L, "N"));
			when(history.getBenPastHistory()).thenReturn(new ArrayList<>());

			assertEquals(1, service.updateBenPastHistoryDetails(history));
			verify(benMedHistoryRepo).deleteExistingBenMedHistory(5L, "N");
		}

		@Test
		void updateBenPastHistoryDetails_doesNothingWithoutAHistory() throws Exception {
			assertEquals(0, service.updateBenPastHistoryDetails(null));
		}

		@Test
		void updateBenComorbidConditions_replacesTheStoredConditions() {
			WrapperComorbidCondDetails wrapper = mock(WrapperComorbidCondDetails.class);
			when(bencomrbidityCondRepo.getBenComrbidityCondHistoryStatus(any(), any()))
					.thenReturn(statusRows(6L, "P"));
			ArrayList<BencomrbidityCondDetails> entries = new ArrayList<>(
					Collections.singletonList(new BencomrbidityCondDetails()));
			when(wrapper.getComrbidityConds()).thenReturn(entries);
			when(bencomrbidityCondRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateBenComorbidConditions(wrapper));
			verify(bencomrbidityCondRepo).deleteExistingBenComrbidityCondDetails(6L, "U");
		}

		@Test
		void updateBenComorbidConditions_succeedsWhenEveryConditionWasCleared() {
			WrapperComorbidCondDetails wrapper = mock(WrapperComorbidCondDetails.class);
			when(bencomrbidityCondRepo.getBenComrbidityCondHistoryStatus(any(), any()))
					.thenReturn(statusRows(6L, null));
			when(wrapper.getComrbidityConds()).thenReturn(new ArrayList<>());

			assertEquals(1, service.updateBenComorbidConditions(wrapper));
			verify(bencomrbidityCondRepo).deleteExistingBenComrbidityCondDetails(6L, "N");
		}

		@Test
		void updateBenComorbidConditions_doesNothingWithoutConditions() {
			assertEquals(0, service.updateBenComorbidConditions(null));
		}

		@Test
		void updateBenMedicationHistory_replacesTheStoredMedication() {
			WrapperMedicationHistory wrapper = mock(WrapperMedicationHistory.class);
			when(benMedicationHistoryRepo.getBenMedicationHistoryStatus(any(), any()))
					.thenReturn(statusRows(7L, "P"));
			ArrayList<BenMedicationHistory> entries = new ArrayList<>(
					Collections.singletonList(new BenMedicationHistory()));
			when(wrapper.getBenMedicationHistoryDetails()).thenReturn(entries);
			when(benMedicationHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateBenMedicationHistory(wrapper));
			verify(benMedicationHistoryRepo).deleteExistingBenMedicationHistory(7L, "U");
		}

		@Test
		void updateBenMedicationHistory_succeedsWhenEveryMedicationWasCleared() {
			WrapperMedicationHistory wrapper = mock(WrapperMedicationHistory.class);
			when(benMedicationHistoryRepo.getBenMedicationHistoryStatus(any(), any()))
					.thenReturn(statusRows(7L, "N"));
			when(wrapper.getBenMedicationHistoryDetails()).thenReturn(new ArrayList<>());
			assertEquals(1, service.updateBenMedicationHistory(wrapper));
		}

		@Test
		void updateBenMedicationHistory_doesNothingWithoutMedication() {
			assertEquals(0, service.updateBenMedicationHistory(null));
		}

		@Test
		void updateBenPersonalHistory_replacesTheStoredHabits() {
			BenPersonalHabit habit = mock(BenPersonalHabit.class);
			when(benPersonalHabitRepo.getBenPersonalHistoryStatus(any(), any()))
					.thenReturn(statusRows(Integer.valueOf(8), "P"));
			ArrayList<BenPersonalHabit> entries = new ArrayList<>(Collections.singletonList(new BenPersonalHabit()));
			when(habit.getPersonalHistory()).thenReturn(entries);
			when(benPersonalHabitRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateBenPersonalHistory(habit));
			verify(benPersonalHabitRepo).deleteExistingBenPersonalHistory(8, "U");
		}

		@Test
		void updateBenPersonalHistory_succeedsWhenEveryHabitWasCleared() {
			BenPersonalHabit habit = mock(BenPersonalHabit.class);
			when(benPersonalHabitRepo.getBenPersonalHistoryStatus(any(), any()))
					.thenReturn(statusRows(Integer.valueOf(8), "N"));
			when(habit.getPersonalHistory()).thenReturn(new ArrayList<>());
			assertEquals(1, service.updateBenPersonalHistory(habit));
		}

		@Test
		void updateBenPersonalHistory_doesNothingWithoutHabits() {
			assertEquals(0, service.updateBenPersonalHistory(null));
		}

		@Test
		void updateBenAllergicHistory_replacesTheStoredAllergies() {
			BenAllergyHistory allergy = mock(BenAllergyHistory.class);
			when(benAllergyHistoryRepo.getBenAllergyHistoryStatus(any(), any())).thenReturn(statusRows(9L, "P"));
			ArrayList<BenAllergyHistory> entries = new ArrayList<>(Collections.singletonList(new BenAllergyHistory()));
			when(allergy.getBenAllergicHistory()).thenReturn(entries);
			when(benAllergyHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateBenAllergicHistory(allergy));
			verify(benAllergyHistoryRepo).deleteExistingBenAllergyHistory(9L, "U");
		}

		@Test
		void updateBenAllergicHistory_succeedsWhenEveryAllergyWasCleared() {
			BenAllergyHistory allergy = mock(BenAllergyHistory.class);
			when(benAllergyHistoryRepo.getBenAllergyHistoryStatus(any(), any())).thenReturn(statusRows(9L, "N"));
			when(allergy.getBenAllergicHistory()).thenReturn(new ArrayList<>());
			assertEquals(1, service.updateBenAllergicHistory(allergy));
		}

		@Test
		void updateBenAllergicHistory_doesNothingWithoutAllergies() {
			assertEquals(0, service.updateBenAllergicHistory(null));
		}

		@Test
		void updateBenFamilyHistory_replacesTheStoredFamilyDiseases() {
			BenFamilyHistory family = mock(BenFamilyHistory.class);
			when(benFamilyHistoryRepo.getBenFamilyHistoryStatus(any(), any())).thenReturn(statusRows(10L, "P"));
			ArrayList<BenFamilyHistory> entries = new ArrayList<>(Collections.singletonList(new BenFamilyHistory()));
			when(family.getBenFamilyHistory()).thenReturn(entries);
			when(benFamilyHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateBenFamilyHistory(family));
			verify(benFamilyHistoryRepo).deleteExistingBenFamilyHistory(10L, "U");
		}

		@Test
		void updateBenFamilyHistory_succeedsWhenEveryDiseaseWasCleared() {
			BenFamilyHistory family = mock(BenFamilyHistory.class);
			when(benFamilyHistoryRepo.getBenFamilyHistoryStatus(any(), any())).thenReturn(statusRows(10L, "N"));
			when(family.getBenFamilyHistory()).thenReturn(new ArrayList<>());
			assertEquals(1, service.updateBenFamilyHistory(family));
		}

		@Test
		void updateBenFamilyHistory_doesNothingWithoutFamilyHistory() {
			assertEquals(0, service.updateBenFamilyHistory(null));
		}

		@Test
		void updateMenstrualHistory_updatesTheExistingRowWhenOneIsAlreadyStored() {
			BenMenstrualDetails details = new BenMenstrualDetails();
			details.setBeneficiaryRegID(1L);
			details.setVisitCode(2L);
			ArrayList<Map<String, Object>> problems = new ArrayList<>();
			Map<String, Object> problem = new HashMap<>();
			problem.put("menstrualProblemID", 3);
			problem.put("problemName", "cramps");
			problems.add(problem);
			details.setMenstrualProblemList(problems);

			when(benMenstrualDetailsRepo.getBenMenstrualDetailStatus(1L, 2L)).thenReturn("P");
			when(benMenstrualDetailsRepo.updateMenstrualDetails(any(), any(), any(), any(), any(), any(), any(),
					anyString(), anyString(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), anyLong(), anyLong()))
							.thenReturn(1);

			assertEquals(1, service.updateMenstrualHistory(details));
			assertEquals("3", details.getMenstrualProblemID());
			assertEquals("cramps", details.getProblemName());
		}

		@Test
		void updateMenstrualHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			BenMenstrualDetails details = new BenMenstrualDetails();
			details.setModifiedBy("nurse");
			when(benMenstrualDetailsRepo.getBenMenstrualDetailStatus(any(), any())).thenReturn(null);

			BenMenstrualDetails stored = new BenMenstrualDetails();
			stored.setBenMenstrualID(4);
			when(benMenstrualDetailsRepo.save(details)).thenReturn(stored);

			assertEquals(1, service.updateMenstrualHistory(details));
			assertEquals("nurse", details.getCreatedBy());
		}

		@Test
		void updateMenstrualHistory_reportsFailureWhenTheFreshRowWasNotStored() {
			BenMenstrualDetails details = new BenMenstrualDetails();
			when(benMenstrualDetailsRepo.getBenMenstrualDetailStatus(any(), any())).thenReturn(null);
			BenMenstrualDetails stored = new BenMenstrualDetails();
			stored.setBenMenstrualID(0);
			when(benMenstrualDetailsRepo.save(details)).thenReturn(stored);

			assertEquals(0, service.updateMenstrualHistory(details));
		}

		@Test
		void updateMenstrualHistory_doesNothingWithoutMenstrualDetails() {
			assertEquals(0, service.updateMenstrualHistory(null));
		}

		@Test
		void updatePastObstetricHistory_replacesTheStoredPregnancies() {
			WrapperFemaleObstetricHistory wrapper = mock(WrapperFemaleObstetricHistory.class);
			when(femaleObstetricHistoryRepo.getBenObstetricHistoryStatus(any(), any()))
					.thenReturn(statusRows(11L, "P"));
			ArrayList<FemaleObstetricHistory> entries = new ArrayList<>(
					Collections.singletonList(new FemaleObstetricHistory()));
			when(wrapper.getFemaleObstetricHistoryDetails()).thenReturn(entries);
			when(femaleObstetricHistoryRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updatePastObstetricHistory(wrapper));
			verify(femaleObstetricHistoryRepo).deleteExistingObstetricHistory(11L, "U");
		}

		@Test
		void updatePastObstetricHistory_doesNothingWithoutObstetricHistory() {
			assertEquals(0, service.updatePastObstetricHistory(null));
		}

		@Test
		void updateChildOptionalVaccineDetail_replacesTheStoredOptionalVaccines() {
			WrapperChildOptionalVaccineDetail wrapper = mock(WrapperChildOptionalVaccineDetail.class);
			when(childOptionalVaccineDetailRepo.getBenChildOptionalVaccineHistoryStatus(any(), any()))
					.thenReturn(statusRows(12L, "P"));
			ArrayList<ChildOptionalVaccineDetail> entries = new ArrayList<>(
					Collections.singletonList(new ChildOptionalVaccineDetail()));
			when(wrapper.getChildOptionalVaccineDetails()).thenReturn(entries);
			when(childOptionalVaccineDetailRepo.saveAll(entries)).thenReturn(entries);

			assertEquals(1, service.updateChildOptionalVaccineDetail(wrapper));
			verify(childOptionalVaccineDetailRepo).deleteExistingChildOptionalVaccineDetail(12L, "U");
		}

		@Test
		void updateChildOptionalVaccineDetail_succeedsWhenEveryOptionalVaccineWasCleared() {
			WrapperChildOptionalVaccineDetail wrapper = mock(WrapperChildOptionalVaccineDetail.class);
			when(childOptionalVaccineDetailRepo.getBenChildOptionalVaccineHistoryStatus(any(), any()))
					.thenReturn(statusRows(12L, "N"));
			when(wrapper.getChildOptionalVaccineDetails()).thenReturn(new ArrayList<>());
			assertEquals(1, service.updateChildOptionalVaccineDetail(wrapper));
		}

		@Test
		void updateChildOptionalVaccineDetail_doesNothingWithoutOptionalVaccines() {
			assertEquals(0, service.updateChildOptionalVaccineDetail(null));
		}

		@Test
		void updateChildImmunizationDetail_marksAPreviouslySyncedVaccineAsUpdated() {
			ChildVaccineDetail1 vaccine = new ChildVaccineDetail1();
			vaccine.setBeneficiaryRegID(1L);
			vaccine.setVisitCode(2L);
			vaccine.setDefaultReceivingAge("6 weeks");
			vaccine.setVaccineName("BCG");

			WrapperImmunizationHistory wrapper = mock(WrapperImmunizationHistory.class);
			when(wrapper.getBenChildVaccineDetails())
					.thenReturn(new ArrayList<>(Collections.singletonList(vaccine)));

			ArrayList<Object[]> statuses = new ArrayList<>();
			statuses.add(new Object[] { "6 weeks", "BCG", "P" });
			when(childVaccineDetail1Repo.getBenChildVaccineDetailStatus(1L, 2L)).thenReturn(statuses);
			when(childVaccineDetail1Repo.updateChildANCImmunization(any(), any(), org.mockito.ArgumentMatchers.eq("U"),
					anyLong(), anyLong(), anyString(), anyString(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateChildImmunizationDetail(wrapper));
		}

		@Test
		void updateChildImmunizationDetail_treatsAVaccineWithNoStoredStatusAsNew() {
			ChildVaccineDetail1 vaccine = new ChildVaccineDetail1();
			vaccine.setBeneficiaryRegID(1L);
			vaccine.setVisitCode(2L);
			vaccine.setDefaultReceivingAge("6 weeks");
			vaccine.setVaccineName("BCG");

			WrapperImmunizationHistory wrapper = mock(WrapperImmunizationHistory.class);
			when(wrapper.getBenChildVaccineDetails())
					.thenReturn(new ArrayList<>(Collections.singletonList(vaccine)));
			when(childVaccineDetail1Repo.getBenChildVaccineDetailStatus(1L, 2L)).thenReturn(new ArrayList<>());
			when(childVaccineDetail1Repo.updateChildANCImmunization(any(), any(), org.mockito.ArgumentMatchers.eq("N"),
					anyLong(), anyLong(), anyString(), anyString(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateChildImmunizationDetail(wrapper));
		}
	}

	@Nested
	@DisplayName("examination updates")
	class ExaminationUpdates {

		@Test
		void updatePhyGeneralExamination_joinsDangerSignsAndMarksAnAlreadySyncedRow() {
			PhyGeneralExamination examination = new PhyGeneralExamination();
			examination.setBeneficiaryRegID(1L);
			examination.setVisitCode(2L);
			examination.setTypeOfDangerSigns(new ArrayList<>(Arrays.asList("fever")));
			when(phyGeneralExaminationRepo.getBenGeneralExaminationStatus(1L, 2L)).thenReturn("P");
			when(phyGeneralExaminationRepo.updatePhyGeneralExamination(any(), any(), any(), any(), any(), any(), any(),
					anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
					any(), org.mockito.ArgumentMatchers.eq("U"), anyLong(), anyLong())).thenReturn(1);

			assertEquals(1, service.updatePhyGeneralExamination(examination));
			assertEquals("fever,", examination.getTypeOfDangerSign());
		}

		@Test
		void updatePhyGeneralExamination_keepsANeverSyncedRowMarkedAsNew() {
			PhyGeneralExamination examination = new PhyGeneralExamination();
			when(phyGeneralExaminationRepo.getBenGeneralExaminationStatus(any(), any())).thenReturn("N");
			when(phyGeneralExaminationRepo.updatePhyGeneralExamination(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
					org.mockito.ArgumentMatchers.eq("N"), any(), any())).thenReturn(1);

			assertEquals(1, service.updatePhyGeneralExamination(examination));
		}

		@Test
		void updatePhyGeneralExamination_doesNothingWithoutAnExamination() {
			assertEquals(0, service.updatePhyGeneralExamination(null));
		}

		@Test
		void updatePhyHeadToToeExamination_marksAnAlreadySyncedRowAsUpdated() {
			PhyHeadToToeExamination examination = new PhyHeadToToeExamination();
			when(phyHeadToToeExaminationRepo.getBenHeadToToeExaminationStatus(any(), any())).thenReturn("P");
			when(phyHeadToToeExaminationRepo.updatePhyHeadToToeExamination(any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), any(), any(),
					org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updatePhyHeadToToeExamination(examination));
			assertEquals(0, service.updatePhyHeadToToeExamination(null));
		}

		@Test
		void updateSysCardiovascularExamination_marksAnAlreadySyncedRowAsUpdated() {
			SysCardiovascularExamination examination = new SysCardiovascularExamination();
			when(sysCardiovascularExaminationRepo.getBenCardiovascularExaminationStatus(any(), any()))
					.thenReturn("P");
			when(sysCardiovascularExaminationRepo.updateSysCardiovascularExamination(any(), any(), any(), any(), any(),
					any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updateSysCardiovascularExamination(examination));
			assertEquals(0, service.updateSysCardiovascularExamination(null));
		}

		@Test
		void updateSysRespiratoryExamination_marksAnAlreadySyncedRowAsUpdated() {
			SysRespiratoryExamination examination = new SysRespiratoryExamination();
			when(sysRespiratoryExaminationRepo.getBenRespiratoryExaminationStatus(any(), any())).thenReturn("P");
			when(sysRespiratoryExaminationRepo.updateSysRespiratoryExamination(any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(),
					any())).thenReturn(1);

			assertEquals(1, service.updateSysRespiratoryExamination(examination));
			assertEquals(0, service.updateSysRespiratoryExamination(null));
		}

		@Test
		void updateSysCentralNervousExamination_marksAnAlreadySyncedRowAsUpdated() {
			SysCentralNervousExamination examination = new SysCentralNervousExamination();
			when(sysCentralNervousExaminationRepo.getBenCentralNervousExaminationStatus(any(), any()))
					.thenReturn("P");
			when(sysCentralNervousExaminationRepo.updateSysCentralNervousExamination(any(), any(), any(), any(), any(),
					any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updateSysCentralNervousExamination(examination));
			assertEquals(0, service.updateSysCentralNervousExamination(null));
		}

		@Test
		void updateSysMusculoskeletalSystemExamination_marksAnAlreadySyncedRowAsUpdated() {
			SysMusculoskeletalSystemExamination examination = new SysMusculoskeletalSystemExamination();
			when(sysMusculoskeletalSystemExaminationRepo.getBenMusculoskeletalSystemExaminationStatus(any(), any()))
					.thenReturn("P");
			when(sysMusculoskeletalSystemExaminationRepo.updateSysMusculoskeletalSystemExamination(any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(),
					any())).thenReturn(1);

			assertEquals(1, service.updateSysMusculoskeletalSystemExamination(examination));
			assertEquals(0, service.updateSysMusculoskeletalSystemExamination(null));
		}

		@Test
		void updateSysGenitourinarySystemExamination_marksAnAlreadySyncedRowAsUpdated() {
			SysGenitourinarySystemExamination examination = new SysGenitourinarySystemExamination();
			when(sysGenitourinarySystemExaminationRepo.getBenGenitourinarySystemExaminationStatus(any(), any()))
					.thenReturn("P");
			when(sysGenitourinarySystemExaminationRepo.updateSysGenitourinarySystemExamination(any(), any(), any(),
					any(), org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updateSysGenitourinarySystemExamination(examination));
			assertEquals(0, service.updateSysGenitourinarySystemExamination(null));
		}

		@Test
		void updateSysGastrointestinalExamination_marksAnAlreadySyncedRowAsUpdated() {
			SysGastrointestinalExamination examination = new SysGastrointestinalExamination();
			when(sysGastrointestinalExaminationRepo.getBenGastrointestinalExaminationStatus(any(), any()))
					.thenReturn("P");
			when(sysGastrointestinalExaminationRepo.updateSysGastrointestinalExamination(any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(),
					any())).thenReturn(1);

			assertEquals(1, service.updateSysGastrointestinalExamination(examination));
			assertEquals(0, service.updateSysGastrointestinalExamination(null));
		}
	}

	@Nested
	@DisplayName("examination and history reads")
	class Reads {

		private ArrayList<Object[]> oneEmptyRow() {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[20]);
			return rows;
		}

		@Test
		void getBenChiefComplaints_serialisesTheStoredComplaints() {
			when(benChiefComplaintRepo.getBenChiefComplaints(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getBenChiefComplaints(1L, 2L));
		}

		@Test
		void getPastHistoryData_mapsTheStoredRows() {
			when(benMedHistoryRepo.getBenPastHistory(1L, 2L)).thenReturn(oneEmptyRow());
			assertNotNull(service.getPastHistoryData(1L, 2L));

			when(benMedHistoryRepo.getBenPastHistory(3L, 4L)).thenReturn(new ArrayList<>());
			assertNull(service.getPastHistoryData(3L, 4L));
		}

		@Test
		void getComorbidityConditionsHistory_mapsTheStoredRows() {
			when(bencomrbidityCondRepo.getBencomrbidityCondDetails(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getComorbidityConditionsHistory(1L, 2L));
		}

		@Test
		void getMedicationHistory_mapsTheStoredRows() {
			when(benMedicationHistoryRepo.getBenMedicationHistoryDetail(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getMedicationHistory(1L, 2L));
		}

		@Test
		void getPersonalHistory_returnsAnEmptyHabitWhenNothingWasRecorded() {
			when(benPersonalHabitRepo.getBenPersonalHabitDetail(1L, 2L)).thenReturn(new ArrayList<>());
			when(benAllergyHistoryRepo.getBenPersonalAllergyDetail(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getPersonalHistory(1L, 2L));
		}

		@Test
		void getPersonalHistory_copiesTheAllergyStatusOntoTheHabit() {
			when(benPersonalHabitRepo.getBenPersonalHabitDetail(1L, 2L)).thenReturn(new ArrayList<>());
			when(benAllergyHistoryRepo.getBenPersonalAllergyDetail(1L, 2L)).thenReturn(oneEmptyRow());

			BenPersonalHabit habit = service.getPersonalHistory(1L, 2L);
			assertNotNull(habit.getAllergicList());
		}

		@Test
		void getFamilyHistory_mapsTheStoredRows() {
			when(benFamilyHistoryRepo.getBenFamilyHistoryDetail(1L, 2L)).thenReturn(oneEmptyRow());
			assertNotNull(service.getFamilyHistory(1L, 2L));
		}

		@Test
		void getFamilyHistoryDetail_mapsTheScreeningVariantOfTheRows() {
			when(benFamilyHistoryRepo.getBenFamilyHisDetail(1L, 2L)).thenReturn(oneEmptyRow());
			assertNotNull(service.getFamilyHistoryDetail(1L, 2L));
		}

		@Test
		void getPhysicalActivityType_delegatesToRepo() {
			PhysicalActivityType stored = new PhysicalActivityType();
			when(physicalActivityTypeRepo.getBenPhysicalHistoryDetails(1L, 2L)).thenReturn(stored);
			assertEquals(stored, service.getPhysicalActivityType(1L, 2L));
		}

		@Test
		void getBeneficiaryIdrsDetails_mapsTheStoredRows() {
			when(iDRSDataRepo.getBenIdrsDetail(1L, 2L)).thenReturn(new ArrayList<>());
			service.getBeneficiaryIdrsDetails(1L, 2L);
		}

		@Test
		void getMenstrualHistory_splitsTheStoredProblemsBackIntoAList() {
			BenMenstrualDetails stored = new BenMenstrualDetails();
			stored.setMenstrualProblemID("1,2");
			stored.setProblemName("cramps,spotting");
			try (org.mockito.MockedStatic<BenMenstrualDetails> statics = org.mockito.Mockito
					.mockStatic(BenMenstrualDetails.class)) {
				statics.when(() -> BenMenstrualDetails.getBenMenstrualDetails(any())).thenReturn(stored);
				BenMenstrualDetails result = service.getMenstrualHistory(1L, 2L);
				assertEquals(2, result.getMenstrualProblemList().size());
				assertEquals("cramps", result.getMenstrualProblemList().get(0).get("problemName"));
			}
		}

		@Test
		void getMenstrualHistory_leavesTheProblemListUnsetWhenNoProblemWasRecorded() {
			BenMenstrualDetails stored = new BenMenstrualDetails();
			try (org.mockito.MockedStatic<BenMenstrualDetails> statics = org.mockito.Mockito
					.mockStatic(BenMenstrualDetails.class)) {
				statics.when(() -> BenMenstrualDetails.getBenMenstrualDetails(any())).thenReturn(stored);
				assertNull(service.getMenstrualHistory(1L, 2L).getMenstrualProblemList());
			}
		}

		@Test
		void getChildOptionalVaccineHistory_mapsTheStoredRows() {
			when(childOptionalVaccineDetailRepo.getBenOptionalVaccineDetail(1L, 2L)).thenReturn(new ArrayList<>());
			service.getChildOptionalVaccineHistory(1L, 2L);
		}

		@Test
		void getImmunizationHistory_mapsTheStoredRows() {
			when(childVaccineDetail1Repo.getBenChildVaccineDetails(1L, 2L)).thenReturn(new ArrayList<>());
			service.getImmunizationHistory(1L, 2L);
		}

		@Test
		void getGeneralExaminationData_splitsTheStoredDangerSignsBackIntoAList() {
			PhyGeneralExamination stored = new PhyGeneralExamination();
			stored.setTypeOfDangerSign("fever,bleeding");
			when(phyGeneralExaminationRepo.getPhyGeneralExaminationData(1L, 2L)).thenReturn(stored);

			assertEquals(Arrays.asList("fever", "bleeding"), service.getGeneralExaminationData(1L, 2L)
					.getTypeOfDangerSigns());
		}

		@Test
		void getGeneralExaminationData_returnsAnEmptyDangerSignListWhenNoneWereRecorded() {
			PhyGeneralExamination stored = new PhyGeneralExamination();
			when(phyGeneralExaminationRepo.getPhyGeneralExaminationData(1L, 2L)).thenReturn(stored);
			assertTrue(service.getGeneralExaminationData(1L, 2L).getTypeOfDangerSigns().isEmpty());
		}

		@Test
		void getGeneralExaminationData_returnsNullWhenNoExaminationWasRecorded() {
			when(phyGeneralExaminationRepo.getPhyGeneralExaminationData(1L, 2L)).thenReturn(null);
			assertNull(service.getGeneralExaminationData(1L, 2L));
		}

		@Test
		void theRemainingSystemExaminationReadsDelegateToTheirRepositories() {
			PhyHeadToToeExamination headToToe = new PhyHeadToToeExamination();
			when(phyHeadToToeExaminationRepo.getPhyHeadToToeExaminationData(1L, 2L)).thenReturn(headToToe);
			assertEquals(headToToe, service.getHeadToToeExaminationData(1L, 2L));

			SysGastrointestinalExamination gastro = new SysGastrointestinalExamination();
			when(sysGastrointestinalExaminationRepo.getSSysGastrointestinalExamination(1L, 2L)).thenReturn(gastro);
			assertEquals(gastro, service.getSysGastrointestinalExamination(1L, 2L));

			SysCardiovascularExamination cardio = new SysCardiovascularExamination();
			when(sysCardiovascularExaminationRepo.getSysCardiovascularExaminationData(1L, 2L)).thenReturn(cardio);
			assertEquals(cardio, service.getCardiovascularExamination(1L, 2L));

			SysRespiratoryExamination respiratory = new SysRespiratoryExamination();
			when(sysRespiratoryExaminationRepo.getSysRespiratoryExaminationData(1L, 2L)).thenReturn(respiratory);
			assertEquals(respiratory, service.getRespiratoryExamination(1L, 2L));

			SysCentralNervousExamination nervous = new SysCentralNervousExamination();
			when(sysCentralNervousExaminationRepo.getSysCentralNervousExaminationData(1L, 2L)).thenReturn(nervous);
			assertEquals(nervous, service.getSysCentralNervousExamination(1L, 2L));

			SysMusculoskeletalSystemExamination musculoskeletal = new SysMusculoskeletalSystemExamination();
			when(sysMusculoskeletalSystemExaminationRepo.getSysMusculoskeletalSystemExamination(1L, 2L))
					.thenReturn(musculoskeletal);
			assertEquals(musculoskeletal, service.getMusculoskeletalExamination(1L, 2L));

			SysGenitourinarySystemExamination genitourinary = new SysGenitourinarySystemExamination();
			when(sysGenitourinarySystemExaminationRepo.getSysGenitourinarySystemExaminationData(1L, 2L))
					.thenReturn(genitourinary);
			assertEquals(genitourinary, service.getGenitourinaryExamination(1L, 2L));
		}
	}

	@Nested
	@DisplayName("prescriptions and prescribed drugs")
	class Prescriptions {

		private com.iemr.mmu.data.snomedct.SCTDescription diagnosis(String term, String conceptId) {
			com.iemr.mmu.data.snomedct.SCTDescription description = new com.iemr.mmu.data.snomedct.SCTDescription();
			description.setTerm(term);
			description.setConceptID(conceptId);
			return description;
		}

		private com.iemr.mmu.data.quickConsultation.PrescriptionDetail storedPrescription(Long id) {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail stored =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			stored.setPrescriptionID(id);
			return stored;
		}

		@Test
		void saveBenPrescription_joinsEveryProvisionalDiagnosisTermAndConceptId() {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail prescription =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			ArrayList<com.iemr.mmu.data.snomedct.SCTDescription> diagnoses = new ArrayList<>();
			diagnoses.add(diagnosis("Fever", "111"));
			diagnoses.add(diagnosis("Cough", null));
			diagnoses.add(diagnosis(null, "333"));
			prescription.setProvisionalDiagnosisList(diagnoses);
			when(prescriptionDetailRepo.save(prescription)).thenReturn(storedPrescription(21L));

			assertEquals(21L, service.saveBenPrescription(prescription));
			assertEquals("Fever  ||  Cough", prescription.getDiagnosisProvided());
			assertEquals("111  ||  N/A", prescription.getDiagnosisProvided_SCTCode());
			verify(prescriptionDetailRepo).updateVanSerialNo(21L);
		}

		@Test
		void saveBenPrescription_leavesTheDiagnosisUnsetWhenNoneWasProvided() {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail prescription =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			when(prescriptionDetailRepo.save(prescription)).thenReturn(storedPrescription(22L));

			assertEquals(22L, service.saveBenPrescription(prescription));
			assertNull(prescription.getDiagnosisProvided());
		}

		@Test
		void saveBenPrescription_returnsNullWhenTheRowWasNotPersisted() {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail prescription =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			when(prescriptionDetailRepo.save(prescription)).thenReturn(storedPrescription(0L));
			assertNull(service.saveBenPrescription(prescription));
		}

		@Test
		void savePrescriptionDetailsAndGetPrescriptionID_buildsThePrescriptionFromTheVisitContext() {
			when(prescriptionDetailRepo.save(any())).thenReturn(storedPrescription(23L));
			ArrayList<com.iemr.mmu.data.snomedct.SCTDescription> diagnoses = new ArrayList<>();
			diagnoses.add(diagnosis("Fever", "111"));

			assertEquals(23L, service.savePrescriptionDetailsAndGetPrescriptionID(1L, 2L, 3, "doctor", "x-ray", 4L, 5,
					6, diagnoses));
			assertEquals(23L, service.savePrescriptionDetailsAndGetPrescriptionID(1L, 2L, 3, "doctor", "x-ray", 4L, 5,
					6, null));
		}

		@Test
		void savePrescriptionDetailsCovid19_recordsTheDoctorDiagnosisWhenOneWasGiven() {
			when(prescriptionDetailRepo.save(any())).thenReturn(storedPrescription(24L));

			assertEquals(24L,
					service.savePrescriptionDetailsCovid19(1L, 2L, 3, "doctor", "x-ray", 4L, 5, 6, "Covid"));
			assertEquals(24L, service.savePrescriptionDetailsCovid19(1L, 2L, 3, "doctor", "x-ray", 4L, 5, 6, null));
		}

		@Test
		void saveBeneficiaryPrescription_readsThePrescriptionOutOfTheCaseSheet() throws Exception {
			when(prescriptionDetailRepo.save(any())).thenReturn(storedPrescription(25L));
			com.google.gson.JsonObject caseSheet = new com.google.gson.JsonObject();
			caseSheet.addProperty("beneficiaryRegID", 1);
			assertEquals(25L, service.saveBeneficiaryPrescription(caseSheet));
		}

		@Test
		void updatePrescription_updatesTheStoredRowAndJoinsTheDiagnosisTerms() {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail prescription =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			prescription.setBeneficiaryRegID(1L);
			prescription.setVisitCode(2L);
			prescription.setPrescriptionID(3L);
			ArrayList<com.iemr.mmu.data.snomedct.SCTDescription> diagnoses = new ArrayList<>();
			diagnoses.add(diagnosis("Fever", null));
			diagnoses.add(diagnosis("Cough", "222"));
			prescription.setProvisionalDiagnosisList(diagnoses);

			when(prescriptionDetailRepo.getGeneralOPDDiagnosisStatus(1L, 2L, 3L)).thenReturn("P");
			when(prescriptionDetailRepo.updatePrescription(anyString(), any(), any(),
					org.mockito.ArgumentMatchers.eq("U"), anyLong(), anyLong(), anyLong(), any(), anyString(), any()))
							.thenReturn(1);

			assertEquals(1, service.updatePrescription(prescription));
			assertEquals("Fever  ||  Cough", prescription.getDiagnosisProvided());
			assertEquals("N/A  ||  222", prescription.getDiagnosisProvided_SCTCode());
		}

		@Test
		void updatePrescription_insertsAFreshRowWhenNoneIsStoredYet() {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail prescription =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			when(prescriptionDetailRepo.getGeneralOPDDiagnosisStatus(any(), any(), any())).thenReturn(null);
			when(prescriptionDetailRepo.save(prescription)).thenReturn(storedPrescription(26L));

			assertEquals(1, service.updatePrescription(prescription));
		}

		@Test
		void updatePrescription_reportsFailureWhenTheFreshRowWasNotStored() {
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail prescription =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			when(prescriptionDetailRepo.getGeneralOPDDiagnosisStatus(any(), any(), any())).thenReturn(null);
			when(prescriptionDetailRepo.save(prescription)).thenReturn(storedPrescription(0L));

			assertEquals(0, service.updatePrescription(prescription));
		}

		@Test
		void saveBeneficiaryLabTestOrderDetails_succeedsWhenTheCaseSheetOrdersNoTest() {
			assertEquals(1L, service.saveBeneficiaryLabTestOrderDetails(new com.google.gson.JsonObject(), 1L));
		}

		@Test
		void saveBeneficiaryLabTestOrderDetails_storesEveryOrderedTest() {
			com.google.gson.JsonObject caseSheet = new com.google.gson.JsonObject();
			com.google.gson.JsonArray orders = new com.google.gson.JsonArray();
			com.google.gson.JsonObject order = new com.google.gson.JsonObject();
			order.addProperty("testID", 1);
			orders.add(order);
			caseSheet.add("labTestOrders", orders);

			when(labTestOrderDetailRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1L, service.saveBeneficiaryLabTestOrderDetails(caseSheet, 1L));
		}

		@Test
		void saveBenPrescribedDrugsList_calculatesTheQuantityForTabletsAndCapsules() {
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail tablet = drug("Tablet", "One Tab",
					"Twice Daily(BD)", "5", "Day(s)");
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail syrup = drug("Syrup", "5 ml", "Once Daily(OD)",
					"5", "Day(s)");
			tablet.setId(1L);
			List<com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail> drugs = Arrays.asList(tablet, syrup);
			when(prescribedDrugDetailRepo.saveAll(drugs)).thenReturn(drugs);

			Map<String, Object> result = service.saveBenPrescribedDrugsList(drugs);

			assertEquals(2, result.get("count"));
			assertEquals(Collections.singletonList(1L), result.get("prescribedDrugIDs"));
			assertEquals(10, tablet.getQtyPrescribed());
			assertNull(syrup.getQtyPrescribed());
		}

		@Test
		void saveBenPrescribedDrugsList_succeedsWhenNoDrugWasPrescribed() {
			Map<String, Object> result = service.saveBenPrescribedDrugsList(new ArrayList<>());
			assertEquals(1, result.get("count"));
		}

		@Test
		void saveBenPrescribedDrugsList_reportsNothingSavedWhenTheStoreDropsARow() {
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail tablet = drug("Tablet", "One Tab",
					"Once Daily(OD)", "1", "Day(s)");
			List<com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail> drugs = Collections.singletonList(tablet);
			when(prescribedDrugDetailRepo.saveAll(drugs)).thenReturn(new ArrayList<>());

			assertEquals(0, service.saveBenPrescribedDrugsList(drugs).get("count"));
		}

		@org.junit.jupiter.params.ParameterizedTest(name = "{0} {1} {2} for {3} {4} is {5}")
		@org.junit.jupiter.params.provider.CsvSource({
				"Tablet, Half Tab, Once Daily(OD), 2, Day(s), 1",
				"Tablet, One Tab, Once Daily(OD) Before Food, 2, Day(s), 2",
				"Tablet, One & Half Tab, Once Daily(OD) After Food, 2, Day(s), 3",
				"Tablet, Two Tabs, Once Daily(OD) At Bedtime, 2, Day(s), 4",
				"Capsule, One Cap, Once Daily(OD), 3, Day(s), 3",
				"Tablet, Half Tab, Twice Daily(BD), 2, Day(s), 2",
				"Tablet, One & Half Tab, Twice Daily(BD) Before Food, 1, Day(s), 3",
				"Tablet, Two Tabs, Twice Daily(BD) After Food, 1, Day(s), 4",
				"Capsule, One Cap, Twice Daily(BD), 1, Day(s), 2",
				"Tablet, Half Tab, Thrice Daily (TID), 2, Day(s), 3",
				"Tablet, One Tab, Thrice Daily (TID) After Food, 1, Day(s), 3",
				"Tablet, One & Half Tab, Thrice Daily (TID) Before Food, 1, Day(s), 5",
				"Tablet, Two Tabs, Thrice Daily (TID), 1, Day(s), 6",
				"Capsule, One Cap, Thrice Daily (TID), 1, Day(s), 3",
				"Tablet, Half Tab, Four Times in a Day (QID), 1, Day(s), 2",
				"Tablet, One Tab, Four Times in a Day AF, 1, Day(s), 4",
				"Tablet, One & Half Tab, Four Times in a Day BF, 1, Day(s), 6",
				"Tablet, Two Tabs, Four Times in a Day (QID), 1, Day(s), 8",
				"Capsule, One Cap, Four Times in a Day (QID), 1, Day(s), 4",
				"Tablet, Half Tab, Single Dose, 5, Day(s), 1",
				"Tablet, One Tab, Stat Dose, 5, Day(s), 1",
				"Tablet, One & Half Tab, Single Dose Before  Food, 1, Day(s), 2",
				"Tablet, Two Tabs, Single Dose After Food, 1, Day(s), 2",
				"Capsule, One Cap, Single Dose, 1, Day(s), 1",
				"Tablet, Half Tab, Once in a Week, 4, Week(s), 2",
				"Tablet, One Tab, Once in a Week After Food, 4, Week(s), 4",
				"Tablet, One & Half Tab, Once in a Week Before Food, 4, Week(s), 6",
				"Tablet, Two Tabs, Once in a Week, 4, Week(s), 8",
				"Capsule, One Cap, Once in a Week, 4, Week(s), 5",
				"Tablet, Half Tab, SOS, 2, Day(s), 1",
				"Tablet, One Tab, SOS, 1, Month(s), 30",
				"Tablet, One & Half Tab, SOS, 1, Day(s), 2",
				"Tablet, Two Tabs, SOS, 1, Day(s), 2",
				"Capsule, One Cap, SOS, 1, Day(s), 1",
				"Tablet, Unknown Dose, Once Daily(OD), 1, Day(s), 0",
				"Tablet, One Tab, Unrecognised Frequency, 1, Day(s), 0",
				"Tablet, One Tab, Once Daily(OD), 1, Unknown Unit, 0" })
		void saveBenPrescribedDrugsList_derivesTheDispensedQuantityFromFormDoseAndFrequency(String form, String dose,
				String frequency, String duration, String unit, int expectedQuantity) {
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail drug = drug(form, dose, frequency, duration,
					unit);
			List<com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail> drugs = Collections.singletonList(drug);
			when(prescribedDrugDetailRepo.saveAll(drugs)).thenReturn(drugs);

			service.saveBenPrescribedDrugsList(drugs);

			assertEquals(expectedQuantity, drug.getQtyPrescribed());
		}

		@Test
		void saveBenPrescribedDrugsList_leavesTheQuantityAtZeroWhenTheOrderIsIncomplete() {
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail drug = drug("Tablet", null, "Once Daily(OD)",
					"1", "Day(s)");
			List<com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail> drugs = Collections.singletonList(drug);
			when(prescribedDrugDetailRepo.saveAll(drugs)).thenReturn(drugs);

			service.saveBenPrescribedDrugsList(drugs);

			assertEquals(0, drug.getQtyPrescribed());
		}

		private com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail drug(String form, String dose,
				String frequency, String duration, String unit) {
			com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail drug =
					new com.iemr.mmu.data.quickConsultation.PrescribedDrugDetail();
			drug.setFormName(form);
			drug.setDose(dose);
			drug.setFrequency(frequency);
			drug.setDuration(duration);
			drug.setUnit(unit);
			return drug;
		}
	}

	@Nested
	@DisplayName("investigations, worklists and status flags")
	class WorklistsAndInvestigations {

		@Test
		void saveBenInvestigationDetails_storesThePrescriptionAndItsInvestigations() {
			com.iemr.mmu.data.anc.WrapperBenInvestigationANC wrapper =
					new com.iemr.mmu.data.anc.WrapperBenInvestigationANC();
			wrapper.setBeneficiaryRegID(1L);
			com.iemr.mmu.data.quickConsultation.PrescriptionDetail stored =
					new com.iemr.mmu.data.quickConsultation.PrescriptionDetail();
			stored.setPrescriptionID(30L);
			when(prescriptionDetailRepo.save(any())).thenReturn(stored);

			assertEquals(1, service.saveBenInvestigationDetails(wrapper));
			assertEquals(30L, wrapper.getPrescriptionID());
		}

		@Test
		void saveBenInvestigationDetails_doesNothingWithoutInvestigationData() {
			assertEquals(0, service.saveBenInvestigationDetails(null));
		}

		@Test
		void saveBenInvestigation_copiesTheVisitContextOntoEveryOrderedTest() {
			com.iemr.mmu.data.anc.WrapperBenInvestigationANC wrapper =
					new com.iemr.mmu.data.anc.WrapperBenInvestigationANC();
			wrapper.setBeneficiaryRegID(1L);
			wrapper.setBenVisitID(2L);
			wrapper.setVisitCode(3L);
			wrapper.setPrescriptionID(4L);
			com.iemr.mmu.data.quickConsultation.LabTestOrderDetail order =
					new com.iemr.mmu.data.quickConsultation.LabTestOrderDetail();
			ArrayList<com.iemr.mmu.data.quickConsultation.LabTestOrderDetail> orders = new ArrayList<>(
					Collections.singletonList(order));
			wrapper.setLaboratoryList(orders);
			when(labTestOrderDetailRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1L, service.saveBenInvestigation(wrapper));
			assertEquals(4L, order.getPrescriptionID());
			assertEquals(1L, order.getBeneficiaryRegID());
			assertEquals(3L, order.getVisitCode());
		}

		@Test
		void saveBenInvestigation_succeedsWhenNoTestWasOrdered() {
			com.iemr.mmu.data.anc.WrapperBenInvestigationANC wrapper =
					new com.iemr.mmu.data.anc.WrapperBenInvestigationANC();
			assertEquals(1L, service.saveBenInvestigation(wrapper));
		}

		@Test
		void updateBenVisitStatusFlag_reportsSuccessWhenTheFlagWasStored() {
			when(benVisitDetailRepo.updateBenFlowStatus("N", 1L)).thenReturn(1);
			assertTrue(service.updateBenVisitStatusFlag(1L, "N").contains("Updated Successfully"));
		}

		@Test
		void updateBenStatus_returnsAnEmptyResultWhenNothingWasUpdated() {
			when(benVisitDetailRepo.updateBenFlowStatus("N", 1L)).thenReturn(0);
			assertEquals("{}", service.updateBenStatus(1L, "N"));
		}

		@Test
		void getNurseWorkList_serialisesTheRegistrarWorklist() {
			when(reistrarRepoBenSearch.getNurseWorkList()).thenReturn(new ArrayList<>());
			assertNotNull(service.getNurseWorkList());
		}

		@Test
		void theRoleWorklistsFallBackToASevenDayWindowWhenNoLimitIsConfigured() {
			when(beneficiaryFlowStatusRepo.getNurseWorklistNew(any(), any(), any())).thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getLabWorklistNew(any(), any(), any())).thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getRadiologistWorkListNew(any(), any(), any()))
					.thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getOncologistWorkListNew(any(), any(), any())).thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getPharmaWorkListNew(any(), any(), any())).thenReturn(new ArrayList<>());

			assertEquals("[]", service.getNurseWorkListNew(1, 2));
			assertEquals("[]", service.getLabWorkListNew(1, 2));
			assertEquals("[]", service.getRadiologistWorkListNew(1, 2));
			assertEquals("[]", service.getOncologistWorkListNew(1, 2));
			assertEquals("[]", service.getPharmaWorkListNew(1, 2));
		}

		@Test
		void theRoleWorklistsHonourTheConfiguredDayWindow() {
			org.springframework.test.util.ReflectionTestUtils.setField(service, "nurseWL", 10);
			org.springframework.test.util.ReflectionTestUtils.setField(service, "labWL", 10);
			org.springframework.test.util.ReflectionTestUtils.setField(service, "radioWL", 10);
			org.springframework.test.util.ReflectionTestUtils.setField(service, "oncoWL", 10);
			org.springframework.test.util.ReflectionTestUtils.setField(service, "pharmaWL", 10);
			org.springframework.test.util.ReflectionTestUtils.setField(service, "TMReferredWL", 10);

			when(beneficiaryFlowStatusRepo.getNurseWorklistNew(any(), any(), any())).thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getNurseWorklistTMreferred(any(), any(), any()))
					.thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getLabWorklistNew(any(), any(), any())).thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getRadiologistWorkListNew(any(), any(), any()))
					.thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getOncologistWorkListNew(any(), any(), any())).thenReturn(new ArrayList<>());
			when(beneficiaryFlowStatusRepo.getPharmaWorkListNew(any(), any(), any())).thenReturn(new ArrayList<>());

			assertEquals("[]", service.getNurseWorkListNew(1, 2));
			assertEquals("[]", service.getNurseWorkListTMReferred(1, 2));
			assertEquals("[]", service.getLabWorkListNew(1, 2));
			assertEquals("[]", service.getRadiologistWorkListNew(1, 2));
			assertEquals("[]", service.getOncologistWorkListNew(1, 2));
			assertEquals("[]", service.getPharmaWorkListNew(1, 2));
		}
	}

	@Nested
	@DisplayName("child history and screening")
	class ChildHistoryAndScreening {

		@Test
		void saveBenAdherenceDetails_reportsWhetherTheRowWasStored() {
			com.iemr.mmu.data.anc.BenAdherence adherence = new com.iemr.mmu.data.anc.BenAdherence();
			when(benAdherenceRepo.save(adherence)).thenReturn(adherence);
			assertEquals(1, service.saveBenAdherenceDetails(adherence));

			when(benAdherenceRepo.save(adherence)).thenReturn(null);
			assertEquals(0, service.saveBenAdherenceDetails(adherence));
		}

		@Test
		void saveChildDevelopmentHistory_returnsTheStoredId() {
			com.iemr.mmu.data.anc.BenChildDevelopmentHistory history =
					new com.iemr.mmu.data.anc.BenChildDevelopmentHistory();
			com.iemr.mmu.data.anc.BenChildDevelopmentHistory stored =
					new com.iemr.mmu.data.anc.BenChildDevelopmentHistory();
			stored.setID(31L);
			when(benChildDevelopmentHistoryRepo.save(any())).thenReturn(stored);
			assertEquals(31L, service.saveChildDevelopmentHistory(history));

			stored.setID(0L);
			assertNull(service.saveChildDevelopmentHistory(history));
		}

		@Test
		void saveChildFeedingHistory_returnsTheStoredId() {
			com.iemr.mmu.data.anc.ChildFeedingDetails details = new com.iemr.mmu.data.anc.ChildFeedingDetails();
			com.iemr.mmu.data.anc.ChildFeedingDetails stored = new com.iemr.mmu.data.anc.ChildFeedingDetails();
			stored.setID(32L);
			when(childFeedingDetailsRepo.save(details)).thenReturn(stored);
			assertEquals(32L, service.saveChildFeedingHistory(details));

			stored.setID(0L);
			assertNull(service.saveChildFeedingHistory(details));
		}

		@Test
		void savePerinatalHistory_returnsTheStoredId() {
			com.iemr.mmu.data.anc.PerinatalHistory history = new com.iemr.mmu.data.anc.PerinatalHistory();
			com.iemr.mmu.data.anc.PerinatalHistory stored = new com.iemr.mmu.data.anc.PerinatalHistory();
			stored.setID(33L);
			when(perinatalHistoryRepo.save(history)).thenReturn(stored);
			assertEquals(33L, service.savePerinatalHistory(history));

			stored.setID(0L);
			assertNull(service.savePerinatalHistory(history));
		}

		@Test
		void getBenAdherence_andGetLabTestOrders_serialiseTheStoredRows() {
			when(benAdherenceRepo.getBenAdherence(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getBenAdherence(1L, 2L));

			when(labTestOrderDetailRepo.getLabTestOrderDetails(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getLabTestOrders(1L, 2L));
		}

		@Test
		void theChildHistoryReadsMapTheStoredRows() {
			when(benChildDevelopmentHistoryRepo.getBenDevelopmentDetails(1L, 2L)).thenReturn(new ArrayList<>());
			service.getDevelopmentHistory(1L, 2L);

			when(perinatalHistoryRepo.getBenPerinatalDetails(1L, 2L)).thenReturn(new ArrayList<>());
			service.getPerinatalHistory(1L, 2L);

			when(childFeedingDetailsRepo.getBenFeedingDetails(1L, 2L)).thenReturn(new ArrayList<>());
			service.getFeedingHistory(1L, 2L);
		}

		@Test
		void updateChildFeedingHistory_updatesTheStoredRowWhenOneExists() {
			com.iemr.mmu.data.anc.ChildFeedingDetails details = new com.iemr.mmu.data.anc.ChildFeedingDetails();
			when(childFeedingDetailsRepo.getBenChildFeedingDetailStatus(any(), any())).thenReturn("P");
			when(childFeedingDetailsRepo.updateFeedingDetails(any(), any(), any(), any(), any(), any(), any(), any(),
					org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updateChildFeedingHistory(details));
		}

		@Test
		void updateChildFeedingHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			com.iemr.mmu.data.anc.ChildFeedingDetails details = new com.iemr.mmu.data.anc.ChildFeedingDetails();
			details.setModifiedBy("nurse");
			com.iemr.mmu.data.anc.ChildFeedingDetails stored = new com.iemr.mmu.data.anc.ChildFeedingDetails();
			stored.setID(34L);
			when(childFeedingDetailsRepo.getBenChildFeedingDetailStatus(any(), any())).thenReturn(null);
			when(childFeedingDetailsRepo.save(details)).thenReturn(stored);

			assertEquals(1, service.updateChildFeedingHistory(details));
			assertEquals("nurse", details.getCreatedBy());
			assertEquals(0, service.updateChildFeedingHistory(null));
		}

		@Test
		void updatePerinatalHistory_updatesTheStoredRowWhenOneExists() {
			com.iemr.mmu.data.anc.PerinatalHistory history = new com.iemr.mmu.data.anc.PerinatalHistory();
			when(perinatalHistoryRepo.getPerinatalHistoryStatus(any(), any())).thenReturn("P");
			when(perinatalHistoryRepo.updatePerinatalDetails(any(), any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updatePerinatalHistory(history));
		}

		@Test
		void updatePerinatalHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			com.iemr.mmu.data.anc.PerinatalHistory history = new com.iemr.mmu.data.anc.PerinatalHistory();
			com.iemr.mmu.data.anc.PerinatalHistory stored = new com.iemr.mmu.data.anc.PerinatalHistory();
			stored.setID(35L);
			when(perinatalHistoryRepo.getPerinatalHistoryStatus(any(), any())).thenReturn(null);
			when(perinatalHistoryRepo.save(history)).thenReturn(stored);

			assertEquals(1, service.updatePerinatalHistory(history));
			assertEquals(0, service.updatePerinatalHistory(null));
		}

		@Test
		void updateChildDevelopmentHistory_updatesTheStoredRowWhenOneExists() {
			com.iemr.mmu.data.anc.BenChildDevelopmentHistory history =
					new com.iemr.mmu.data.anc.BenChildDevelopmentHistory();
			when(benChildDevelopmentHistoryRepo.getDevelopmentHistoryStatus(any(), any())).thenReturn("P");
			when(benChildDevelopmentHistoryRepo.updatePerinatalDetails(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), org.mockito.ArgumentMatchers.eq("U"), any(), any())).thenReturn(1);

			assertEquals(1, service.updateChildDevelopmentHistory(history));
		}

		@Test
		void updateChildDevelopmentHistory_insertsAFreshRowWhenNoneIsStoredYet() {
			com.iemr.mmu.data.anc.BenChildDevelopmentHistory history =
					new com.iemr.mmu.data.anc.BenChildDevelopmentHistory();
			com.iemr.mmu.data.anc.BenChildDevelopmentHistory stored =
					new com.iemr.mmu.data.anc.BenChildDevelopmentHistory();
			stored.setID(36L);
			when(benChildDevelopmentHistoryRepo.getDevelopmentHistoryStatus(any(), any())).thenReturn(null);
			when(benChildDevelopmentHistoryRepo.save(any())).thenReturn(stored);

			assertEquals(1, service.updateChildDevelopmentHistory(history));
			assertEquals(0, service.updateChildDevelopmentHistory(null));
		}

		@Test
		void updateBenFamilyHistoryNCDScreening_reportsWhetherEveryDiseaseWasStored() {
			BenFamilyHistory input = mock(BenFamilyHistory.class);
			ArrayList<BenFamilyHistory> entries = new ArrayList<>(Collections.singletonList(new BenFamilyHistory()));
			when(input.getBenFamilyHist()).thenReturn(entries);
			when(benFamilyHistoryRepo.saveAll(entries)).thenReturn(entries);
			assertEquals(1, service.updateBenFamilyHistoryNCDScreening(input));

			when(input.getBenFamilyHist()).thenReturn(new ArrayList<>());
			assertEquals(0, service.updateBenFamilyHistoryNCDScreening(input));
		}

		@Test
		void updateBenPhysicalActivityHistoryNCDScreening_marksAnExistingRowAsUpdated() {
			PhysicalActivityType activity = new PhysicalActivityType();
			activity.setID(1L);
			when(physicalActivityTypeRepo.save(activity)).thenReturn(activity);

			assertEquals(1, service.updateBenPhysicalActivityHistoryNCDScreening(activity));
			assertEquals("U", activity.getProcessed());
			assertEquals(Boolean.FALSE, activity.getDeleted());
		}

		@Test
		void updateBenPhysicalActivityHistoryNCDScreening_marksAFreshRowAsNew() {
			PhysicalActivityType activity = new PhysicalActivityType();
			when(physicalActivityTypeRepo.save(activity)).thenReturn(null);

			assertEquals(0, service.updateBenPhysicalActivityHistoryNCDScreening(activity));
			assertEquals("N", activity.getProcessed());
		}
	}

	@Nested
	@DisplayName("graph trends and NCD summaries")
	class TrendsAndSummaries {

		@Test
		void getGraphicalTrendData_readsWeightAndVitalsFromTheNonCancerVisits() {
			ArrayList<Object[]> visits = new ArrayList<>();
			visits.add(new Object[] { 1L, "ANC", 100L });
			visits.add(new Object[] { 2L, "ANC", null });
			when(benVisitDetailRepo.getLastSixVisitDetailsForBeneficiary(1L)).thenReturn(visits);

			ArrayList<Object[]> anthro = new ArrayList<>();
			anthro.add(new Object[] { 55.5d, Date.valueOf("2024-01-01") });
			when(benAnthropometryRepo.getBenAnthropometryDetailForGraphtrends(any())).thenReturn(anthro);

			ArrayList<Object[]> vitals = new ArrayList<>();
			vitals.add(new Object[] { (short) 120, (short) 80, 90d, 140d, 150d, Date.valueOf("2024-01-01") });
			vitals.add(new Object[] { (short) 0, (short) 0, null, null, null, Date.valueOf("2024-01-02") });
			when(benPhysicalVitalRepo.getBenPhysicalVitalDetailForGraphTrends(any())).thenReturn(vitals);

			Map<String, Object> trends = service.getGraphicalTrendData(1L, "ANC");

			assertEquals(1, ((List<?>) trends.get("weightList")).size());
			assertEquals(1, ((List<?>) trends.get("bpList")).size());
			assertEquals(1, ((List<?>) trends.get("bgList")).size());
		}

		@Test
		void getGraphicalTrendData_averagesTheThreeReadingsTakenAtCancerScreeningVisits() {
			ArrayList<Object[]> visits = new ArrayList<>();
			visits.add(new Object[] { 1L, "Cancer Screening", 100L });
			when(benVisitDetailRepo.getLastSixVisitDetailsForBeneficiary(1L)).thenReturn(visits);

			com.iemr.mmu.data.nurse.BenCancerVitalDetail cancerVital =
					new com.iemr.mmu.data.nurse.BenCancerVitalDetail();
			cancerVital.setWeight_Kg(60d);
			cancerVital.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
			cancerVital.setSystolicBP_1stReading((short) 120);
			cancerVital.setSystolicBP_2ndReading((short) 130);
			cancerVital.setSystolicBP_3rdReading((short) 140);
			cancerVital.setDiastolicBP_1stReading((short) 80);
			cancerVital.setDiastolicBP_2ndReading((short) 90);
			cancerVital.setDiastolicBP_3rdReading((short) 100);
			cancerVital.setBloodGlucose_Fasting((short) 95);

			ArrayList<com.iemr.mmu.data.nurse.BenCancerVitalDetail> cancerVitals = new ArrayList<>(
					Collections.singletonList(cancerVital));
			when(benCancerVitalDetailRepo.getBenCancerVitalDetailForGraph(any())).thenReturn(cancerVitals);

			Map<String, Object> trends = service.getGraphicalTrendData(1L, "Cancer Screening");

			assertEquals(1, ((List<?>) trends.get("weightList")).size());
			List<?> bpList = (List<?>) trends.get("bpList");
			assertEquals(1, bpList.size());
			assertEquals(130, ((Map<?, ?>) bpList.get(0)).get("avgSysBP"));
			assertEquals(90, ((Map<?, ?>) bpList.get(0)).get("avgDysBP"));
			assertEquals(1, ((List<?>) trends.get("bgList")).size());
		}

		@Test
		void getGraphicalTrendData_returnsEmptyTrendsWhenTheBeneficiaryHasNoVisits() {
			when(benVisitDetailRepo.getLastSixVisitDetailsForBeneficiary(1L)).thenReturn(new ArrayList<>());
			Map<String, Object> trends = service.getGraphicalTrendData(1L, "ANC");
			assertTrue(((List<?>) trends.get("weightList")).isEmpty());
		}

		@Test
		void getBenSymptomaticData_collectsTheAnswersOfTheMostRecentVisit() throws Exception {
			IDRSData first = new IDRSData();
			first.setVisitCode(1L);
			first.setIdrsQuestionID(10);
			first.setAnswer("Yes");
			first.setSuspectedDisease("Diabetes");
			first.setConfirmedDisease("None");
			IDRSData sameVisit = new IDRSData();
			sameVisit.setVisitCode(1L);
			sameVisit.setIdrsQuestionID(11);
			IDRSData earlierVisit = new IDRSData();
			earlierVisit.setVisitCode(2L);

			when(iDRSDataRepo.getBenIdrsDetailsLast_3_Month(anyLong(), any()))
					.thenReturn(new ArrayList<>(Arrays.asList(first, sameVisit, earlierVisit)));
			when(iDRSDataRepo.isDiabeticCheck(1L)).thenReturn(1);
			when(iDRSDataRepo.isHypertensionCheck(1L)).thenReturn(1);
			when(iDRSDataRepo.isEpilepsyCheck(1L)).thenReturn(1);
			when(iDRSDataRepo.isDefectiveVisionCheck(1L)).thenReturn(1);

			String json = service.getBenSymptomaticData(1L);

			assertTrue(json.contains("\"isDiabetic\":true"), json);
			assertTrue(json.contains("\"isHypertension\":true"), json);
			assertTrue(json.contains("\"isEpilepsy\":true"), json);
			assertTrue(json.contains("\"isDefectiveVision\":true"), json);
			assertTrue(json.contains("\"suspectedDisease\":\"Diabetes\""), json);
			assertTrue(json.contains("\"confirmedDisease\":\"None\""), json);
		}

		@Test
		void getBenSymptomaticData_reportsNoConditionsWhenNothingWasScreened() throws Exception {
			when(iDRSDataRepo.getBenIdrsDetailsLast_3_Month(anyLong(), any())).thenReturn(new ArrayList<>());
			when(iDRSDataRepo.isDiabeticCheck(1L)).thenReturn(0);
			when(iDRSDataRepo.isHypertensionCheck(1L)).thenReturn(0);
			when(iDRSDataRepo.isEpilepsyCheck(1L)).thenReturn(null);
			when(iDRSDataRepo.isDefectiveVisionCheck(1L)).thenReturn(null);

			String json = service.getBenSymptomaticData(1L);

			assertTrue(json.contains("\"isDiabetic\":false"), json);
			assertTrue(json.contains("\"isEpilepsy\":false"), json);
			assertTrue(json.contains("\"questionariesData\":[]"), json);
		}

		@Test
		void getBenPreviousDiabetesData_keepsOnlyTheDiabetesQuestionsOfEachRow() throws Exception {
			IDRSData diabetesRow = new IDRSData();
			diabetesRow.setDiseaseQuestionType("Diabetes||Hypertension");
			diabetesRow.setAnswer("Yes||No");
			diabetesRow.setQuestion("Do you have diabetes?||Do you have hypertension?");
			diabetesRow.setQuestionIds("1||2");
			IDRSData otherRow = new IDRSData();
			otherRow.setDiseaseQuestionType("Hypertension");
			otherRow.setAnswer("No");
			otherRow.setQuestion("Do you have hypertension?");
			otherRow.setQuestionIds("2");

			when(iDRSDataRepo.getBenPreviousDiabetesDetails(1L))
					.thenReturn(new ArrayList<>(Arrays.asList(diabetesRow, otherRow)));

			String json = service.getBenPreviousDiabetesData(1L);

			assertTrue(json.contains("Do you have diabetes?"), json);
			assertTrue(json.contains("\"columns\""), json);
		}

		@Test
		void getBenPreviousReferralData_mapsEachStoredReferral() throws Exception {
			ArrayList<Object[]> referrals = new ArrayList<>();
			referrals.add(new Object[] { java.math.BigInteger.valueOf(5),
					new java.sql.Timestamp(System.currentTimeMillis()), "Diabetes" });
			when(iDRSDataRepo.getBenPreviousReferredDetails(1L)).thenReturn(referrals);

			String json = service.getBenPreviousReferralData(1L);
			assertTrue(json.contains("Diabetes"), json);

			when(iDRSDataRepo.getBenPreviousReferredDetails(2L)).thenReturn(null);
			assertTrue(service.getBenPreviousReferralData(2L).contains("\"data\":[]"));
		}

		@org.junit.jupiter.params.ParameterizedTest(name = "a BMI of {0} reads as {1}")
		@org.junit.jupiter.params.provider.CsvSource({ "0.5, Normal", "-1.5, Mild malnourished",
				"-2.5, Moderately Malnourished", "-4.0, Severely Malnourished", "1.5, Overweight", "2.5, Obese",
				"3.5, Severely Obese" })
		void calculateBMIStatus_classifiesTheBmiAgainstTheStoredStandardDeviations(double bmi, String expectedStatus)
				throws Exception {
			com.iemr.mmu.data.bmi.BmiCalculation standard = new com.iemr.mmu.data.bmi.BmiCalculation();
			standard.setN3SD(-3d);
			standard.setN2SD(-2d);
			standard.setN1SD(-1d);
			standard.setP1SD(1d);
			standard.setP2SD(2d);
			standard.setP3SD(3d);
			when(bmiCalculationRepo.getBMIDetails(27, "Male")).thenReturn(standard);

			String request = "{\"yearMonth\":\"2 Years and 3 Months\",\"gender\":\"Male\",\"bmi\":" + bmi + "}";
			assertTrue(service.calculateBMIStatus(request).contains(expectedStatus));
		}

		@Test
		void calculateBMIStatus_reportsNoStatusWhenTheRequestIsIncomplete() throws Exception {
			assertEquals("{\"bmiStatus\":\"\"}", service.calculateBMIStatus("{}"));
		}

		@Test
		void calculateBMIStatus_failsWhenNoStandardIsStoredForTheCategory() {
			when(bmiCalculationRepo.getBMIDetails(anyInt(), anyString())).thenReturn(null);
			String request = "{\"yearMonth\":\"2 Years and 3 Months\",\"gender\":\"Male\",\"bmi\":1.0}";
			IEMRException thrown = assertThrows(IEMRException.class, () -> service.calculateBMIStatus(request));
			assertTrue(thrown.getMessage().contains("No data found for this category"));
		}
	}
}
