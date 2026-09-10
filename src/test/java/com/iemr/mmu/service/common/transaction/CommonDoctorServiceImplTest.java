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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.anc.WrapperAncFindings;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.login.Users;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.quickConsultation.BenClinicalObservations;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.doctor.BenReferDetailsRepo;
import com.iemr.mmu.repo.doctor.DocWorkListRepo;
import com.iemr.mmu.repo.login.UserLoginRepo;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;
import com.iemr.mmu.repo.quickConsultation.BenChiefComplaintRepo;
import com.iemr.mmu.repo.quickConsultation.BenClinicalObservationsRepo;
import com.iemr.mmu.repo.quickConsultation.LabTestOrderDetailRepo;
import com.iemr.mmu.repo.quickConsultation.PrescribedDrugDetailRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.snomedct.SnomedServiceImpl;
import com.iemr.mmu.utils.CookieUtil;

class CommonDoctorServiceImplTest {

	@Mock
	private BenClinicalObservationsRepo benClinicalObservationsRepo;
	@Mock
	private BenChiefComplaintRepo benChiefComplaintRepo;
	@Mock
	private DocWorkListRepo docWorkListRepo;
	@Mock
	private BenReferDetailsRepo benReferDetailsRepo;
	@Mock
	private LabTestOrderDetailRepo labTestOrderDetailRepo;
	@Mock
	private PrescribedDrugDetailRepo prescribedDrugDetailRepo;
	@Mock
	private PrescriptionDetailRepo prescriptionDetailRepo;
	@Mock
	private SnomedServiceImpl snomedServiceImpl;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Mock
	private CookieUtil cookieUtil;
	@Mock
	private BenVisitDetailRepo benVisitDetailRepo;
	@Mock
	private UserLoginRepo userLoginRepo;

	@InjectMocks
	private CommonDoctorServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(service, "tmReferCheckValue", "Tele-consultation");
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	private static WrapperAncFindings findings(BenChiefComplaint... complaints) {
		ArrayList<BenChiefComplaint> list = new ArrayList<>();
		Collections.addAll(list, complaints);
		return new WrapperAncFindings(1L, 2L, 4, "Alert", "Fever", "Anaemia", list, Boolean.FALSE, 3L);
	}

	private static BenChiefComplaint complaint(String name) {
		BenChiefComplaint complaint = new BenChiefComplaint();
		complaint.setChiefComplaint(name);
		complaint.setBenChiefComplaintID(9L);
		return complaint;
	}

	@Nested
	@DisplayName("findings")
	class Findings {

		@Test
		void saveFindings_reportsWhetherTheObservationWasStored() throws Exception {
			BenClinicalObservations stored = new BenClinicalObservations();
			when(benClinicalObservationsRepo.save(any())).thenReturn(stored);
			assertEquals(1, service.saveFindings(json("{}")));

			when(benClinicalObservationsRepo.save(any())).thenReturn(null);
			assertEquals(0, service.saveFindings(json("{}")));
		}

		@Test
		void saveDocFindings_storesTheObservationAndEveryNamedComplaint() {
			when(benClinicalObservationsRepo.save(any())).thenReturn(new BenClinicalObservations());
			when(benChiefComplaintRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			WrapperAncFindings wrapper = findings(complaint("Fever"), new BenChiefComplaint());

			assertEquals(1, service.saveDocFindings(wrapper));
			verify(benChiefComplaintRepo).updateVanSerialNo(9L);
		}

		@Test
		void saveDocFindings_succeedsWhenNoComplaintWasNamed() {
			when(benClinicalObservationsRepo.save(any())).thenReturn(new BenClinicalObservations());
			assertEquals(1, service.saveDocFindings(findings()));
			verify(benChiefComplaintRepo, never()).saveAll(any());
		}

		@Test
		void saveDocFindings_reportsFailureWhenTheObservationWasNotStored() {
			when(benClinicalObservationsRepo.save(any())).thenReturn(null);
			assertEquals(0, service.saveDocFindings(findings()));
		}

		@Test
		void saveDocFindings_reportsFailureWhenNotEveryComplaintWasStored() {
			when(benClinicalObservationsRepo.save(any())).thenReturn(new BenClinicalObservations());
			when(benChiefComplaintRepo.saveAll(any())).thenReturn(new ArrayList<>());

			assertEquals(0, service.saveDocFindings(findings(complaint("Fever"))));
		}

		@Test
		void getSnomedCTcode_looksUpEverySymptomAndFallsBackWhenOneIsUnknown() {
			SCTDescription known = new SCTDescription();
			known.setConceptID("111");
			known.setTerm("Fever");
			when(snomedServiceImpl.findSnomedCTRecordFromTerm("Fever")).thenReturn(known);
			when(snomedServiceImpl.findSnomedCTRecordFromTerm("Unknown")).thenReturn(null);

			assertArrayEquals(new String[] { "111,N/A", "Fever,N/A" },
					service.getSnomedCTcode("Fever, Unknown"));
		}

		@Test
		void getSnomedCTcode_returnsEmptyCodesForNoSymptoms() {
			assertArrayEquals(new String[] { "", "" }, service.getSnomedCTcode(null));
			assertArrayEquals(new String[] { "", "" }, service.getSnomedCTcode(""));
		}

		@Test
		void fetchBenPreviousSignificantFindings_mapsEveryStoredFinding() {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[] { new java.sql.Date(System.currentTimeMillis()), "Anaemia" });
			when(benClinicalObservationsRepo.getPreviousSignificantFindings(1L)).thenReturn(rows);

			assertTrue(service.fetchBenPreviousSignificantFindings(1L).contains("Anaemia"));
		}

		@Test
		void getFindingsDetails_combinesTheObservationsAndComplaints() {
			when(benClinicalObservationsRepo.getFindingsData(1L, 2L)).thenReturn(new ArrayList<>());
			when(benChiefComplaintRepo.getBenChiefComplaints(1L, 2L)).thenReturn(new ArrayList<>());

			assertNotNull(service.getFindingsDetails(1L, 2L));
		}

		@Test
		void updateDocFindings_updatesTheObservationAndEveryNamedComplaint() {
			when(benClinicalObservationsRepo.getBenClinicalObservationStatus(1L, 3L)).thenReturn("P");
			when(benClinicalObservationsRepo.updateBenClinicalObservations(any(), any(), any(), any(), any(), any(),
					any(), eq("U"), anyLong(), anyLong())).thenReturn(1);
			when(benChiefComplaintRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1, service.updateDocFindings(findings(complaint("Fever"))));
		}

		@Test
		void updateDocFindings_succeedsWhenNoComplaintWasNamed() {
			when(benClinicalObservationsRepo.getBenClinicalObservationStatus(any(), any())).thenReturn("N");
			when(benClinicalObservationsRepo.updateBenClinicalObservations(any(), any(), any(), any(), any(), any(),
					any(), eq("N"), anyLong(), anyLong())).thenReturn(1);

			assertEquals(1, service.updateDocFindings(findings()));
		}

		@Test
		void updateDocFindings_reportsFailureWhenTheObservationCouldNotBeUpdated() {
			when(benClinicalObservationsRepo.getBenClinicalObservationStatus(any(), any())).thenReturn("N");
			when(benClinicalObservationsRepo.updateBenClinicalObservations(any(), any(), any(), any(), any(), any(),
					any(), any(), anyLong(), anyLong())).thenReturn(0);

			assertEquals(0, service.updateDocFindings(findings()));
		}

		@Test
		void updateBenClinicalObservations_insertsAFreshRowWhenNoneIsStoredYet() {
			BenClinicalObservations observations = new BenClinicalObservations();
			BenClinicalObservations stored = new BenClinicalObservations();
			stored.setClinicalObservationID(5L);
			when(benClinicalObservationsRepo.getBenClinicalObservationStatus(any(), any())).thenReturn(null);
			when(benClinicalObservationsRepo.save(observations)).thenReturn(stored);

			assertEquals(1, service.updateBenClinicalObservations(observations));
		}

		@Test
		void updateBenClinicalObservations_reportsFailureWhenTheFreshRowWasNotStored() {
			BenClinicalObservations observations = new BenClinicalObservations();
			BenClinicalObservations stored = new BenClinicalObservations();
			stored.setClinicalObservationID(0L);
			when(benClinicalObservationsRepo.getBenClinicalObservationStatus(any(), any())).thenReturn(null);
			when(benClinicalObservationsRepo.save(observations)).thenReturn(stored);

			assertEquals(0, service.updateBenClinicalObservations(observations));
			assertEquals(0, service.updateBenClinicalObservations(null));
		}

		@Test
		void updateDoctorBenChiefComplaints_succeedsWhenThereIsNothingToUpdate() {
			assertEquals(1, service.updateDoctorBenChiefComplaints(null));
			assertEquals(1, service.updateDoctorBenChiefComplaints(new ArrayList<>()));
		}

		@Test
		void updateDoctorBenChiefComplaints_reportsNothingSavedWhenTheStoreDropsTheRows() {
			List<BenChiefComplaint> complaints = Collections.singletonList(complaint("Fever"));
			when(benChiefComplaintRepo.saveAll(complaints)).thenReturn(new ArrayList<>());

			assertEquals(0, service.updateDoctorBenChiefComplaints(complaints));
		}
	}

	@Nested
	@DisplayName("work lists")
	class WorkLists {

		@Test
		void getDocWorkList_serialisesTheStoredWorklist() {
			when(docWorkListRepo.getDocWorkList()).thenReturn(new ArrayList<>());
			assertNotNull(service.getDocWorkList());
		}

		@Test
		void getDocWorkListNew_readsTheMmuWorklistWithinTheConfiguredWindow() {
			ReflectionTestUtils.setField(service, "docWL", 10);
			when(beneficiaryFlowStatusRepo.getDocWorkListNew(any(), any(), any())).thenReturn(new ArrayList<>());

			assertEquals("[]", service.getDocWorkListNew(1, 2, 3));
		}

		@Test
		void getDocWorkListNew_readsTheTeleconsultationWorklist() {
			when(beneficiaryFlowStatusRepo.getDocWorkListNewTC(1)).thenReturn(new ArrayList<>());
			assertEquals("[]", service.getDocWorkListNew(1, 4, 3));
		}

		@Test
		void getDocWorkListNew_returnsNothingForAnUnknownService() {
			assertEquals("[]", service.getDocWorkListNew(1, 9, 3));
		}

		@Test
		void getDocWorkListNewFutureScheduledForTM_onlyAppliesToTeleconsultation() {
			when(beneficiaryFlowStatusRepo.getDocWorkListNewFutureScheduledTC(1)).thenReturn(new ArrayList<>());

			assertEquals("[]", service.getDocWorkListNewFutureScheduledForTM(1, 4));
			assertEquals("[]", service.getDocWorkListNewFutureScheduledForTM(1, 2));
		}

		@Test
		void getTCSpecialistWorkListNewForTM_onlyAppliesToTeleconsultation() {
			when(beneficiaryFlowStatusRepo.getTCSpecialistWorkListNew(1, 5)).thenReturn(new ArrayList<>());

			assertEquals("[]", service.getTCSpecialistWorkListNewForTM(1, 5, 4));
			assertEquals("[]", service.getTCSpecialistWorkListNewForTM(1, 5, 2));
		}

		@Test
		void getTCSpecialistWorkListNewFutureScheduledForTM_onlyAppliesToTeleconsultation() {
			when(beneficiaryFlowStatusRepo.getTCSpecialistWorkListNewFutureScheduled(1, 5))
					.thenReturn(new ArrayList<>());

			assertEquals("[]", service.getTCSpecialistWorkListNewFutureScheduledForTM(1, 5, 4));
			assertEquals("[]", service.getTCSpecialistWorkListNewFutureScheduledForTM(1, 5, 2));
		}
	}

	@Nested
	@DisplayName("referrals")
	class Referrals {

		private String referRequest(String extra) {
			return "{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"providerServiceMapID\":4,"
					+ "\"createdBy\":\"doctor\"" + extra + "}";
		}

		@Test
		void saveBenReferDetails_createsOneRowPerAdditionalService() throws Exception {
			when(benReferDetailsRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			String request = referRequest(",\"referredToInstituteID\":7,\"referredToInstituteName\":\"PHC\","
					+ "\"referralReason\":\"Fever\",\"revisitDate\":\"2024-01-01T00:00:00.000\","
					+ "\"refrredToAdditionalServiceList\":[{\"serviceID\":1,\"serviceName\":\"Tele-consultation\"},"
					+ "{\"serviceID\":2,\"serviceName\":\"Lab\"},{\"serviceID\":3}]");

			assertEquals(1L, service.saveBenReferDetails(json(request)));
		}

		@Test
		void saveBenReferDetails_storesTheReferralAsOneRowWhenNoServiceWasChosen() throws Exception {
			when(benReferDetailsRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1L, service.saveBenReferDetails(json(referRequest(
					",\"referredToInstituteName\":\"PHC\""))));
		}

		@Test
		void saveBenReferDetails_storesNothingWhenTheReferralIsEmpty() throws Exception {
			when(benReferDetailsRepo.saveAll(any())).thenReturn(new ArrayList<>());
			assertEquals(1L, service.saveBenReferDetails(json(referRequest(""))));
		}

		@Test
		void saveBenReferDetailsTMreferred_reusesTheExistingRowWhenTheInstituteWasUpdated() throws Exception {
			when(benReferDetailsRepo.updateReferredInstituteNameTMReferred(any(), any(), any(), eq("U")))
					.thenReturn(1);

			assertEquals(1L, service.saveBenReferDetailsTMreferred(json(referRequest(
					",\"referredToInstituteID\":7,\"referredToInstituteName\":\"PHC\""))));
		}

		@Test
		void saveBenReferDetailsTMreferred_createsOneRowPerAdditionalServiceWhenNoRowExisted() throws Exception {
			when(benReferDetailsRepo.updateReferredInstituteNameTMReferred(any(), any(), any(), any())).thenReturn(0);
			when(benReferDetailsRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			String request = referRequest(",\"referredToInstituteID\":7,\"referredToInstituteName\":\"PHC\","
					+ "\"revisitDate\":\"2024-01-01T00:00:00.000\","
					+ "\"refrredToAdditionalServiceList\":[{\"serviceID\":1,\"serviceName\":\"Lab\"},{}]");

			assertEquals(1L, service.saveBenReferDetailsTMreferred(json(request)));
		}

		@Test
		void saveBenReferDetailsTMreferred_storesTheReferralAsOneRowWhenNoServiceWasChosen() throws Exception {
			when(benReferDetailsRepo.updateReferredInstituteNameTMReferred(any(), any(), any(), any())).thenReturn(0);
			when(benReferDetailsRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1L, service.saveBenReferDetailsTMreferred(
					json(referRequest(",\"referredToInstituteName\":\"PHC\""))));
		}

		@Test
		void updateBenReferDetails_refreshesTheStoredRowsAndAddsTheNewServices() throws Exception {
			ArrayList<Object[]> statuses = new ArrayList<>();
			statuses.add(new Object[] { 5L, "P", "Lab" });
			when(benReferDetailsRepo.getBenReferDetailsStatus(1L, 3L)).thenReturn(statuses);
			when(benReferDetailsRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			String request = referRequest(",\"referredToInstituteID\":7,\"referredToInstituteName\":\"PHC\","
					+ "\"referralReason\":\"Fever\",\"revisitDate\":\"2024-01-01T00:00:00.000\","
					+ "\"refrredToAdditionalServiceList\":[{\"serviceID\":1,\"serviceName\":\"Lab\"},"
					+ "{\"serviceID\":2,\"serviceName\":\"Tele-consultation\"},{\"serviceID\":3,"
					+ "\"serviceName\":\"Radiology\"}]");

			assertEquals(1L, service.updateBenReferDetails(json(request)));
			verify(benReferDetailsRepo).updateReferredInstituteName(any(), any(), any(), any(), eq(5L), eq("U"));
		}

		@Test
		void updateBenReferDetails_keepsARowThatWasNeverSyncedMarkedAsNew() throws Exception {
			ArrayList<Object[]> statuses = new ArrayList<>();
			statuses.add(new Object[] { 5L, "N", "Lab" });
			when(benReferDetailsRepo.getBenReferDetailsStatus(any(), any())).thenReturn(statuses);
			when(benReferDetailsRepo.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			assertEquals(1L, service.updateBenReferDetails(
					json(referRequest(",\"referredToInstituteName\":\"PHC\""))));
			verify(benReferDetailsRepo).updateReferredInstituteName(any(), any(), any(), any(), eq(5L), eq("N"));
		}

		@Test
		void updateBenReferDetails_leavesTheStoredRowsAloneWhenNothingWasEntered() throws Exception {
			when(benReferDetailsRepo.getBenReferDetailsStatus(any(), any())).thenReturn(new ArrayList<>());
			when(benReferDetailsRepo.saveAll(any())).thenReturn(new ArrayList<>());

			assertEquals(1L, service.updateBenReferDetails(json(referRequest(""))));
		}

		@Test
		void getReferralDetails_mapsTheStoredReferral() {
			when(benReferDetailsRepo.getBenReferDetails(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getReferralDetails(1L, 2L));
		}
	}

	@Nested
	@DisplayName("investigations and prescriptions")
	class InvestigationsAndPrescriptions {

		@Test
		void getInvestigationDetails_mapsTheStoredOrders() {
			when(labTestOrderDetailRepo.getLabTestOrderDetails(1L, 2L)).thenReturn(new ArrayList<>());
			assertNotNull(service.getInvestigationDetails(1L, 2L));
		}

		@Test
		void getPrescribedDrugs_mapsTheStoredDrugs() {
			when(prescribedDrugDetailRepo.getBenPrescribedDrugDetails(1L, 2L)).thenReturn(new ArrayList<>());
			assertEquals("[]", service.getPrescribedDrugs(1L, 2L));
		}

		@Test
		void deletePrescribedMedicine_reportsWhetherTheRowWasRemoved() throws Exception {
			JSONObject request = new JSONObject();
			request.put("id", 5L);
			when(prescribedDrugDetailRepo.deletePrescribedmedicine(5L)).thenReturn(1);
			assertEquals("record deleted successfully", service.deletePrescribedMedicine(request));

			when(prescribedDrugDetailRepo.deletePrescribedmedicine(5L)).thenReturn(0);
			assertNull(service.deletePrescribedMedicine(request));
			assertNull(service.deletePrescribedMedicine(new JSONObject()));
			assertNull(service.deletePrescribedMedicine(null));
		}
	}

	@Nested
	@DisplayName("beneficiary flow after doctor data")
	class BeneficiaryFlow {

		private CommonUtilityClass utility(Boolean isSpecialist) {
			CommonUtilityClass utility = new CommonUtilityClass();
			utility.setBenFlowID(1L);
			utility.setBeneficiaryID(2L);
			utility.setBenVisitID(3L);
			utility.setBeneficiaryRegID(4L);
			utility.setVisitCode(5L);
			utility.setCreatedBy("doctor");
			utility.setIsSpecialist(isSpecialist);
			return utility;
		}

		private TeleconsultationRequestOBJ teleconsultationRequest() {
			TeleconsultationRequestOBJ request = new TeleconsultationRequestOBJ();
			request.setUserID(7);
			request.setAllocationDate(new Timestamp(System.currentTimeMillis()));
			return request;
		}

		@Test
		void updateBenFlowtableAfterDocDataSave_recordsTheDoctorAndSendsTheBeneficiaryToTheLabAndPharmacy()
				throws Exception {
			Users doctor = new Users();
			doctor.setUserID(11L);
			when(userLoginRepo.getUserByUsername("doctor")).thenReturn(doctor);
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataSave(utility(false), true, true,
					teleconsultationRequest(), true));
			verify(benVisitDetailRepo).updateDoctorID(11L, 5L);
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocData(eq(1L), eq(4L), eq(2L), eq(3L),
					eq((short) 2), eq((short) 1), eq((short) 0), eq((short) 1), eq(7), any(), eq(true));
		}

		@Test
		void updateBenFlowtableAfterDocDataSave_closesTheDoctorStepWhenNothingWasPrescribed() throws Exception {
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataSave(utility(false), false, false, null, false));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocData(eq(1L), eq(4L), eq(2L), eq(3L),
					eq((short) 9), eq((short) 0), eq((short) 0), eq((short) 0), eq(0), any(), eq(false));
		}

		@Test
		void updateBenFlowtableAfterDocDataSave_marksAnNcdScreeningTeleconsultationReferral() throws Exception {
			ReflectionTestUtils.setField(service, "TMReferred", 1);
			BeneficiaryFlowStatus stored = new BeneficiaryFlowStatus();
			stored.setVisitCategory("NCD screening");
			when(beneficiaryFlowStatusRepo.specialistFlagAndCategoryValue(5L)).thenReturn(stored);
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataSave(utility(false), false, false, null, false));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), eq((short) 100), anyInt(), any(), any());
		}

		@Test
		void updateBenFlowtableAfterDocDataSave_leavesTheDoctorUnresolvedWhenTheUsernameIsUnknown() throws Exception {
			CommonUtilityClass utility = utility(false);
			utility.setCreatedBy("  ");
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataSave(utility, false, false, null, false));
			verify(benVisitDetailRepo, never()).updateDoctorID(anyLong(), anyLong());
		}

		@Test
		void updateBenFlowtableAfterDocDataSave_leavesTheDoctorUnresolvedWhenTheVisitHasNoCode() throws Exception {
			CommonUtilityClass utility = utility(false);
			utility.setVisitCode(null);
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(any(), any(), any(), any(), anyShort(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataSave(utility, false, false, null, false));
			verify(benVisitDetailRepo, never()).updateDoctorID(anyLong(), anyLong());
		}

		@Test
		void updateBenFlowtableAfterDocDataUpdate_routesASpecialistUpdateThroughTheSpecialistFlow() throws Exception {
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdateTCSpecialist(any(), any(), any(),
					any(), anyShort(), anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataUpdate(utility(true), true, true, null, true));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocDataUpdateTCSpecialist(eq(1L), eq(4L), eq(2L),
					eq(3L), eq((short) 0), eq((short) 1), eq((short) 0), eq((short) 2), eq(0), any(), eq(true));
		}

		@Test
		void updateBenFlowtableAfterDocDataUpdate_closesTheSpecialistStepWhenNothingWasPrescribed() throws Exception {
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdateTCSpecialist(any(), any(), any(),
					any(), anyShort(), anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataUpdate(utility(true), false, false, null, false));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocDataUpdateTCSpecialist(any(), any(), any(),
					any(), anyShort(), eq((short) 0), anyShort(), eq((short) 9), anyInt(), any(), any());
		}

		@Test
		void updateBenFlowtableAfterDocDataUpdate_usesTheWalkInFlowForAnOrdinaryDoctorUpdate() throws Exception {
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdateWDF(any(), any(), any(), any(),
					anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataUpdate(utility(false), true, true,
					teleconsultationRequest(), true));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocDataUpdateWDF(eq(1L), eq(4L), eq(2L), eq(3L),
					eq((short) 2), eq((short) 1), eq((short) 0), eq(7), any(), eq(true));
		}

		@Test
		void updateBenFlowtableAfterDocDataUpdate_usesTheReferralFlowForAnNcdScreeningReferral() throws Exception {
			ReflectionTestUtils.setField(service, "TMReferred", 1);
			BeneficiaryFlowStatus stored = new BeneficiaryFlowStatus();
			stored.setVisitCategory("NCD screening");
			when(beneficiaryFlowStatusRepo.specialistFlagAndCategoryValue(5L)).thenReturn(stored);
			when(commonBenStatusFlowServiceImpl.updateBenFlowAfterDocDataUpdate(any(), any(), any(), any(),
					anyShort(), anyShort(), anyShort(), anyShort(), anyInt(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBenFlowtableAfterDocDataUpdate(utility(false), false, false, null, false));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowAfterDocDataUpdate(any(), any(), any(), any(),
					eq((short) 9), eq((short) 0), anyShort(), eq((short) 100), anyInt(), any(), any());
		}
	}
}
