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
package com.iemr.mmu.service.quickConsultation;

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
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.quickConsultation.BenClinicalObservations;
import com.iemr.mmu.data.quickConsultation.ExternalLabTestOrder;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.BenPhysicalVitalRepo;
import com.iemr.mmu.repo.quickConsultation.BenChiefComplaintRepo;
import com.iemr.mmu.repo.quickConsultation.BenClinicalObservationsRepo;
import com.iemr.mmu.repo.quickConsultation.ExternalTestOrderRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.generalOPD.GeneralOPDDoctorServiceImpl;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;

/**
 * Covers the quick-consultation flows end to end - the nurse visit, the
 * doctor's first save and the doctor's later update - alongside the narrower
 * per-method checks in {@link QuickConsultationServiceImplTest}.
 */
class QuickConsultationFlowTest {

	@Mock
	private BenChiefComplaintRepo benChiefComplaintRepo;
	@Mock
	private BenClinicalObservationsRepo benClinicalObservationsRepo;
	@Mock
	private PrescriptionDetailRepo prescriptionDetailRepo;
	@Mock
	private ExternalTestOrderRepo externalTestOrderRepo;
	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private LabTechnicianServiceImpl labTechnicianServiceImpl;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;
	@Mock
	private GeneralOPDDoctorServiceImpl generalOPDDoctorServiceImpl;
	@Mock
	private TeleConsultationServiceImpl teleConsultationServiceImpl;
	@Mock
	private BenPhysicalVitalRepo benPhysicalVitalRepo;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	@InjectMocks
	private QuickConsultationServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	private static String nurseRequest() {
		return "{\"benFlowID\":3,\"vanID\":1,\"sessionID\":1,"
				+ "\"visitDetails\":{\"beneficiaryRegID\":1,\"visitReason\":\"New Chief Complaint\","
				+ "\"visitCategory\":\"General OPD (QC)\"},\"vitalsDetails\":{}}";
	}

	@Nested
	@DisplayName("the nurse visit")
	class NurseVisit {

		@Test
		void quickConsultNurseDataInsert_savesTheVisitAndVitalsAndAdvancesTheFlow() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(5L);
			when(commonNurseServiceImpl.generateVisitCode(anyLong(), any(), any())).thenReturn(6L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(1L);

			assertEquals(1, service.quickConsultNurseDataInsert(json(nurseRequest())));
			verify(commonBenStatusFlowServiceImpl).updateBenFlowNurseAfterNurseActivity(any(), anyLong(), anyLong(),
					anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any());
		}

		@Test
		void quickConsultNurseDataInsert_reportsAVisitThatWasAlreadyCreatedRecently() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(1);

			assertEquals(3, service.quickConsultNurseDataInsert(json(nurseRequest())));
		}

		@Test
		void quickConsultNurseDataInsert_skipsTheVisitWhenTheNurseAlreadySavedThisFlow() throws Exception {
			when(beneficiaryFlowStatusRepo.checkExistData(any(), any())).thenReturn(new BeneficiaryFlowStatus());

			assertEquals(0, service.quickConsultNurseDataInsert(json(nurseRequest())));
			verify(commonNurseServiceImpl, never()).saveBeneficiaryVisitDetails(any());
		}

		@Test
		void quickConsultNurseDataInsert_leavesTheFlowUntouchedWhenTheVitalsFailToSave() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(5L);
			when(commonNurseServiceImpl.generateVisitCode(anyLong(), any(), any())).thenReturn(6L);
			when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(null);

			assertEquals(0, service.quickConsultNurseDataInsert(json(nurseRequest())));
			verify(commonBenStatusFlowServiceImpl, never()).updateBenFlowNurseAfterNurseActivity(any(), anyLong(),
					anyLong(), anyString(), anyString(), any(), any(), any(), any(), any(), anyLong(), any());
		}

		@Test
		void quickConsultNurseDataInsert_leavesTheFlowUntouchedWhenTheVisitWasNotCreated() throws Exception {
			when(commonNurseServiceImpl.getMaxCurrentdate(any(), any(), any())).thenReturn(0);
			when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(0L);

			assertEquals(0, service.quickConsultNurseDataInsert(json(nurseRequest())));
		}

		@Test
		void quickConsultNurseDataInsert_ignoresARequestWithoutVisitDetails() throws Exception {
			assertEquals(0, service.quickConsultNurseDataInsert(json("{}")));
			assertEquals(0, service.quickConsultNurseDataInsert(null));
		}
	}

	@Nested
	@DisplayName("the individual saves")
	class Saves {

		@Test
		void saveBeneficiaryChiefComplaint_storesEveryComplaintAndStampsItsVanSerial() {
			BenChiefComplaint stored = new BenChiefComplaint();
			stored.setBenChiefComplaintID(9L);
			when(benChiefComplaintRepo.saveAll(any())).thenReturn(Collections.singletonList(stored));

			String request = "{\"chiefComplaintList\":[{\"chiefComplaintID\":1,\"chiefComplaint\":\"Fever\"}]}";

			assertEquals(1L, service.saveBeneficiaryChiefComplaint(json(request)));
			verify(benChiefComplaintRepo).updateVanSerialNo(9L);
		}

		@Test
		void saveBeneficiaryChiefComplaint_succeedsWhenNoComplaintWasEntered() {
			assertEquals(1L, service.saveBeneficiaryChiefComplaint(json("{}")));
			verify(benChiefComplaintRepo, never()).saveAll(any());
		}

		@Test
		void saveBeneficiaryChiefComplaint_reportsFailureWhenNotEveryComplaintWasStored() {
			when(benChiefComplaintRepo.saveAll(any())).thenReturn(new ArrayList<>());

			String request = "{\"chiefComplaintList\":[{\"chiefComplaintID\":1,\"chiefComplaint\":\"Fever\"}]}";

			assertNull(service.saveBeneficiaryChiefComplaint(json(request)));
		}

		@Test
		void saveBeneficiaryClinicalObservations_attachesTheSnomedCodesOfTheSymptoms() throws Exception {
			when(commonDoctorServiceImpl.getSnomedCTcode("Fever"))
					.thenReturn(new String[] { "111", "Fever" });
			BenClinicalObservations stored = new BenClinicalObservations();
			stored.setClinicalObservationID(4L);
			when(benClinicalObservationsRepo.save(any())).thenReturn(stored);

			assertEquals(4L, service.saveBeneficiaryClinicalObservations(json("{\"otherSymptoms\":\"Fever\"}")));
		}

		@Test
		void saveBeneficiaryClinicalObservations_reportsNothingWhenTheRowWasNotStored() throws Exception {
			BenClinicalObservations stored = new BenClinicalObservations();
			stored.setClinicalObservationID(0L);
			when(benClinicalObservationsRepo.save(any())).thenReturn(stored);

			assertNull(service.saveBeneficiaryClinicalObservations(json("{}")));
		}

		@Test
		void saveBenPrescriptionForANC_returnsTheStoredPrescriptionId() {
			PrescriptionDetail prescription = new PrescriptionDetail();
			PrescriptionDetail stored = new PrescriptionDetail();
			stored.setPrescriptionID(7L);
			when(prescriptionDetailRepo.save(prescription)).thenReturn(stored);
			assertEquals(7L, service.saveBenPrescriptionForANC(prescription));

			stored.setPrescriptionID(0L);
			assertNull(service.saveBenPrescriptionForANC(prescription));
		}

		@Test
		void saveBeneficiaryExternalLabTestOrderDetails_returnsTheStoredOrderId() {
			ExternalLabTestOrder stored = new ExternalLabTestOrder();
			// The id is assigned by the database, so it is set through the field.
			org.springframework.test.util.ReflectionTestUtils.setField(stored, "externalTestOrderID", 8L);
			when(externalTestOrderRepo.save(any())).thenReturn(stored);
			assertEquals(8L, service.saveBeneficiaryExternalLabTestOrderDetails(json("{}")));

			org.springframework.test.util.ReflectionTestUtils.setField(stored, "externalTestOrderID", 0L);
			assertNull(service.saveBeneficiaryExternalLabTestOrderDetails(json("{}")));
		}

		@Test
		void updateBeneficiaryClinicalObservations_delegatesToTheDoctorService() throws Exception {
			when(commonDoctorServiceImpl.getSnomedCTcode(any())).thenReturn(new String[] { "111", "Fever" });
			when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(1);

			assertEquals(1, service.updateBeneficiaryClinicalObservations(json("{\"otherSymptoms\":\"Fever\"}")));
		}
	}

	@Nested
	@DisplayName("the doctor's save and update")
	class DoctorData {

		private String doctorRequest(String extra) {
			return "{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"providerServiceMapID\":4,"
					+ "\"createdBy\":\"doctor\",\"prescriptionID\":7,"
					+ "\"chiefComplaintList\":[{\"chiefComplaintID\":1,\"chiefComplaint\":\"Fever\"}],"
					+ "\"labTestOrders\":[{\"testID\":1}],\"prescription\":[{\"drugID\":1}],"
					+ "\"rbsTestResult\":\"90\",\"refer\":{}" + extra + "}";
		}

		private String teleconsultationBlock() {
			return ",\"serviceID\":4,\"tcRequest\":{\"userID\":5,\"allocationDate\":\"2024-01-01\","
					+ "\"fromTime\":\"10:00:00\",\"toTime\":\"10:30:00\"}";
		}

		private Map<String, Object> drugResult() {
			Map<String, Object> result = new HashMap<>();
			result.put("count", 1);
			result.put("prescribedDrugIDs", Collections.singletonList(9L));
			return result;
		}

		@BeforeEach
		void stubTheCommonSaves() throws Exception {
			BenChiefComplaint storedComplaint = new BenChiefComplaint();
			storedComplaint.setBenChiefComplaintID(9L);
			when(benChiefComplaintRepo.saveAll(any())).thenReturn(Collections.singletonList(storedComplaint));
			when(commonDoctorServiceImpl.getSnomedCTcode(any())).thenReturn(new String[] { "111", "Fever" });
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult());
			when(commonNurseServiceImpl.saveBeneficiaryLabTestOrderDetails(any(), any())).thenReturn(1L);
			when(benPhysicalVitalRepo.updatePhysicalVitalDetailsQCDoctor(any(), any(), any(), any())).thenReturn(1);
			when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
		}

		@Test
		void quickConsultDoctorDataInsert_savesEverySectionAndAdvancesTheFlow() throws Exception {
			BenClinicalObservations storedObservation = new BenClinicalObservations();
			storedObservation.setClinicalObservationID(4L);
			when(benClinicalObservationsRepo.save(any())).thenReturn(storedObservation);
			when(commonNurseServiceImpl.saveBeneficiaryPrescription(any())).thenReturn(7L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1, service.quickConsultDoctorDataInsert(json(doctorRequest("")), "auth"));
		}

		@Test
		void quickConsultDoctorDataInsert_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			BenClinicalObservations storedObservation = new BenClinicalObservations();
			storedObservation.setClinicalObservationID(4L);
			when(benClinicalObservationsRepo.save(any())).thenReturn(storedObservation);
			when(commonNurseServiceImpl.saveBeneficiaryPrescription(any())).thenReturn(7L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class,
					() -> service.quickConsultDoctorDataInsert(json(doctorRequest("")), "auth"));
		}

		@Test
		void quickConsultDoctorDataInsert_failsWhenASectionCouldNotBeSaved() throws Exception {
			BenClinicalObservations storedObservation = new BenClinicalObservations();
			storedObservation.setClinicalObservationID(0L);
			when(benClinicalObservationsRepo.save(any())).thenReturn(storedObservation);
			when(commonNurseServiceImpl.saveBeneficiaryPrescription(any())).thenReturn(7L);

			assertThrows(RuntimeException.class,
					() -> service.quickConsultDoctorDataInsert(json(doctorRequest("")), "auth"));
		}

		@Test
		void quickConsultDoctorDataInsert_booksTheSpecialistSlotBeforeRaisingATeleconsultationRequest()
				throws Exception {
			BenClinicalObservations storedObservation = new BenClinicalObservations();
			storedObservation.setClinicalObservationID(4L);
			when(benClinicalObservationsRepo.save(any())).thenReturn(storedObservation);
			when(commonNurseServiceImpl.saveBeneficiaryPrescription(any())).thenReturn(7L);
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
			when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1,
					service.quickConsultDoctorDataInsert(json(doctorRequest(teleconsultationBlock())), "auth"));
			verify(teleConsultationServiceImpl).createTCRequest(any());
		}

		@Test
		void quickConsultDoctorDataInsert_failsWhenTheSpecialistSlotCouldNotBeBooked() {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(0);

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> service.quickConsultDoctorDataInsert(json(doctorRequest(teleconsultationBlock())), "auth"));
			assertEquals("Error while booking slot.", thrown.getMessage());
		}

		@Test
		void updateGeneralOPDQCDoctorData_updatesEverySectionAndAdvancesTheFlow() throws Exception {
			when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L, service.updateGeneralOPDQCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void updateGeneralOPDQCDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class,
					() -> service.updateGeneralOPDQCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void updateGeneralOPDQCDoctorData_failsWhenASectionCouldNotBeUpdated() throws Exception {
			when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(0);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);

			assertThrows(RuntimeException.class,
					() -> service.updateGeneralOPDQCDoctorData(json(doctorRequest("")), "auth"));
		}

		@Test
		void updateGeneralOPDQCDoctorData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			String minimal = "{\"beneficiaryRegID\":1,\"prescriptionID\":7,"
					+ "\"chiefComplaintList\":[{\"chiefComplaintID\":1,\"chiefComplaint\":\"Fever\"}]}";

			assertEquals(1L, service.updateGeneralOPDQCDoctorData(json(minimal), "auth"));
		}

		@Test
		void updateGeneralOPDQCDoctorData_booksTheSpecialistSlotBeforeRaisingATeleconsultationRequest()
				throws Exception {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
			when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1L,
					service.updateGeneralOPDQCDoctorData(json(doctorRequest(teleconsultationBlock())), "auth"));
		}

		@Test
		void updateGeneralOPDQCDoctorData_failsWhenTheSpecialistSlotCouldNotBeBooked() {
			when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(0);

			assertThrows(RuntimeException.class, () -> service
					.updateGeneralOPDQCDoctorData(json(doctorRequest(teleconsultationBlock())), "auth"));
		}
	}

	@Nested
	@DisplayName("the case sheet reads")
	class Reads {

		@Test
		void getBenQuickConsultNurseData_carriesTheVitals() {
			when(commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(1L, 2L)).thenReturn("a");
			when(commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(1L, 2L)).thenReturn("v");

			assertTrue(service.getBenQuickConsultNurseData(1L, 2L).contains("vitals"));
		}
	}
}
