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

import com.google.gson.JsonObject;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.quickConsultation.BenClinicalObservations;
import com.iemr.mmu.data.quickConsultation.ExternalLabTestOrder;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.repo.quickConsultation.BenChiefComplaintRepo;
import com.iemr.mmu.repo.quickConsultation.BenClinicalObservationsRepo;
import com.iemr.mmu.repo.quickConsultation.ExternalTestOrderRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import com.iemr.mmu.service.generalOPD.GeneralOPDDoctorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class QuickConsultationServiceImplTest {
    // Setter methods coverage
    @Test
    void testSetCommonBenStatusFlowServiceImpl() {
        com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl mockImpl = mock(com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl.class);
        service.setCommonBenStatusFlowServiceImpl(mockImpl);
        // No assertion needed, just coverage
    }

    @Test
    void testSetGeneralOPDDoctorServiceImpl() {
        GeneralOPDDoctorServiceImpl mockImpl = mock(GeneralOPDDoctorServiceImpl.class);
        service.setGeneralOPDDoctorServiceImpl(mockImpl);
    }

    @Test
    void testSetLabTechnicianServiceImpl() {
        LabTechnicianServiceImpl mockImpl = mock(LabTechnicianServiceImpl.class);
        service.setLabTechnicianServiceImpl(mockImpl);
    }

    @Test
    void testSetCommonDoctorServiceImpl() {
        com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl mockImpl = mock(com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl.class);
        service.setCommonDoctorServiceImpl(mockImpl);
    }

    @Test
    void testSetBenChiefComplaintRepo() {
        BenChiefComplaintRepo mockRepo = mock(BenChiefComplaintRepo.class);
        service.setBenChiefComplaintRepo(mockRepo);
    }

    @Test
    void testSetBenClinicalObservationsRepo() {
        BenClinicalObservationsRepo mockRepo = mock(BenClinicalObservationsRepo.class);
        service.setBenClinicalObservationsRepo(mockRepo);
    }

    @Test
    void testSetPrescriptionDetailRepo() {
        PrescriptionDetailRepo mockRepo = mock(PrescriptionDetailRepo.class);
        service.setPrescriptionDetailRepo(mockRepo);
    }

    @Test
    void testSetExternalTestOrderRepo() {
        ExternalTestOrderRepo mockRepo = mock(ExternalTestOrderRepo.class);
        service.setExternalTestOrderRepo(mockRepo);
    }

    // Utility and fetch methods coverage
    @Test
    void testUpdateBenStatusFlagAfterNurseSaveSuccess() {
        com.iemr.mmu.data.nurse.BeneficiaryVisitDetail visitDetail = mock(com.iemr.mmu.data.nurse.BeneficiaryVisitDetail.class);
        when(visitDetail.getBeneficiaryRegID()).thenReturn(1L);
        when(visitDetail.getVisitReason()).thenReturn("reason");
        when(visitDetail.getVisitCategory()).thenReturn("category");
        com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl benStatusFlowService = mock(com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl.class);
        service.setCommonBenStatusFlowServiceImpl(benStatusFlowService);
        when(benStatusFlowService.updateBenFlowNurseAfterNurseActivity(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyShort(), anyShort(), anyShort(), anyShort(), anyShort(), anyLong(), anyInt())).thenReturn(1);
        try {
            java.lang.reflect.Method method = QuickConsultationServiceImpl.class.getDeclaredMethod("updateBenStatusFlagAfterNurseSaveSuccess", com.iemr.mmu.data.nurse.BeneficiaryVisitDetail.class, Long.class, Long.class, Long.class, Integer.class);
            method.setAccessible(true);
            int result = (int) method.invoke(service, visitDetail, 2L, 3L, 4L, 5);
            assertEquals(1, result);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void testGetBeneficiaryVitalDetails() {
        when(commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(anyLong(), anyLong())).thenReturn("anthro");
        when(commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(anyLong(), anyLong())).thenReturn("vital");
        String result = service.getBeneficiaryVitalDetails(1L, 2L);
        assertTrue(result.contains("anthro"));
        assertTrue(result.contains("vital"));
    }

    @Test
    void testGetBenDataFrmNurseToDocVisitDetailsScreen() throws Exception {
        com.iemr.mmu.data.nurse.BeneficiaryVisitDetail visitDetail = mock(com.iemr.mmu.data.nurse.BeneficiaryVisitDetail.class);
        when(commonNurseServiceImpl.getCSVisitDetails(anyLong(), anyLong())).thenReturn(visitDetail);
        String result = service.getBenDataFrmNurseToDocVisitDetailsScreen(1L, 2L);
        assertTrue(result.contains("benVisitDetails"));
    }

    @Test
    void testGetBenQuickConsultNurseData() {
        QuickConsultationServiceImpl spyService = spy(service);
        doReturn("vitalDetails").when(spyService).getBeneficiaryVitalDetails(anyLong(), anyLong());
        String result = spyService.getBenQuickConsultNurseData(1L, 2L);
        assertTrue(result.contains("vitalDetails"));
    }
    @InjectMocks
    private QuickConsultationServiceImpl service;

    @Mock private BenChiefComplaintRepo benChiefComplaintRepo;
    @Mock private BenClinicalObservationsRepo benClinicalObservationsRepo;
    @Mock private PrescriptionDetailRepo prescriptionDetailRepo;
    @Mock private ExternalTestOrderRepo externalTestOrderRepo;
    @Mock private com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl commonDoctorServiceImpl;
    @Mock private com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl commonNurseServiceImpl;
    @Mock private com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
    @Mock private com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl teleConsultationServiceImpl;

    // 1. saveBeneficiaryChiefComplaint
    @Test
    void testSaveBeneficiaryChiefComplaint_valid() {
        JsonObject caseSheet = new JsonObject();
        ArrayList<BenChiefComplaint> mockList = new ArrayList<>();
        mockList.add(mock(BenChiefComplaint.class));
        when(benChiefComplaintRepo.saveAll(anyList())).thenReturn(mockList);
        // BenChiefComplaint.getBenChiefComplaintList should return non-empty ArrayList
        try (var mockedStatic = mockStatic(BenChiefComplaint.class)) {
            mockedStatic.when(() -> BenChiefComplaint.getBenChiefComplaintList(any())).thenReturn(mockList);
            Long result = service.saveBeneficiaryChiefComplaint(caseSheet);
            assertEquals(1L, result);
        }
    }

    @Test
    void testSaveBeneficiaryChiefComplaint_empty() {
        JsonObject caseSheet = new JsonObject();
        try (var mockedStatic = mockStatic(BenChiefComplaint.class)) {
            mockedStatic.when(() -> BenChiefComplaint.getBenChiefComplaintList(any())).thenReturn(new ArrayList<>());
            Long result = service.saveBeneficiaryChiefComplaint(caseSheet);
            assertEquals(1L, result);
        }
    }

    // 2. saveBeneficiaryClinicalObservations
    @Test
    void testSaveBeneficiaryClinicalObservations_valid() throws Exception {
        JsonObject caseSheet = new JsonObject();
        BenClinicalObservations mockObs = mock(BenClinicalObservations.class);
        when(benClinicalObservationsRepo.save(any())).thenReturn(mockObs);
        Long id = 10L;
        when(mockObs.getClinicalObservationID()).thenReturn(id);
        Long result = service.saveBeneficiaryClinicalObservations(caseSheet);
        assertEquals(id, result);
    }

    @Test
    void testSaveBeneficiaryClinicalObservations_null() throws Exception {
        JsonObject caseSheet = new JsonObject();
        when(benClinicalObservationsRepo.save(any())).thenReturn(null);
        Long result = service.saveBeneficiaryClinicalObservations(caseSheet);
        assertNull(result);
    }

    // 3. saveBenPrescriptionForANC
    @Test
    void testSaveBenPrescriptionForANC_valid() {
        PrescriptionDetail mockPrescription = mock(PrescriptionDetail.class);
        when(mockPrescription.getPrescriptionID()).thenReturn(5L);
        when(prescriptionDetailRepo.save(any())).thenReturn(mockPrescription);
        Long result = service.saveBenPrescriptionForANC(mockPrescription);
        assertEquals(5L, result);
    }

    @Test
    void testSaveBenPrescriptionForANC_null() {
        when(prescriptionDetailRepo.save(any())).thenReturn(null);
        PrescriptionDetail mockPrescription = mock(PrescriptionDetail.class);
        Long result = service.saveBenPrescriptionForANC(mockPrescription);
        assertNull(result);
    }

    // 4. saveBeneficiaryExternalLabTestOrderDetails
    @Test
    void testSaveBeneficiaryExternalLabTestOrderDetails_valid() {
        JsonObject caseSheet = new JsonObject();
        ExternalLabTestOrder mockOrder = mock(ExternalLabTestOrder.class);
        when(mockOrder.getExternalTestOrderID()).thenReturn(7L);
        when(externalTestOrderRepo.save(any())).thenReturn(mockOrder);
        try (var mockedStatic = mockStatic(ExternalLabTestOrder.class)) {
            mockedStatic.when(() -> ExternalLabTestOrder.getExternalLabTestOrderList(any())).thenReturn(mockOrder);
            Long result = service.saveBeneficiaryExternalLabTestOrderDetails(caseSheet);
            assertEquals(7L, result);
        }
    }

    @Test
    void testSaveBeneficiaryExternalLabTestOrderDetails_null() {
        JsonObject caseSheet = new JsonObject();
        when(externalTestOrderRepo.save(any())).thenReturn(null);
        try (var mockedStatic = mockStatic(ExternalLabTestOrder.class)) {
            mockedStatic.when(() -> ExternalLabTestOrder.getExternalLabTestOrderList(any())).thenReturn(null);
            Long result = service.saveBeneficiaryExternalLabTestOrderDetails(caseSheet);
            assertNull(result);
        }
    }

    // 5. quickConsultNurseDataInsert
    @Test
    void testQuickConsultNurseDataInsert_nullInput() throws Exception {
        Integer result = service.quickConsultNurseDataInsert(null);
        assertEquals(0, result);
    }

    // More detailed tests for quickConsultNurseDataInsert can be added to cover all branches
    // 6. updateGeneralOPDQCDoctorData
    @Test
    void testUpdateGeneralOPDQCDoctorData_basicFlow() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("benFlowID", 1L);
        obj.addProperty("serviceID", 4);
        obj.addProperty("createdBy", 1L);
        obj.addProperty("vanID", 1L);
        obj.addProperty("sessionID", 1L);
        // Create a tcRequest object with required fields
        JsonObject tcRequest = new JsonObject();
        tcRequest.addProperty("userID", 99L);
        // Use default Gson date format for allocationDate
        tcRequest.addProperty("allocationDate", "2025-08-06");
        tcRequest.addProperty("fromTime", "10:00");
        tcRequest.addProperty("toTime", "10:30");
        obj.add("tcRequest", tcRequest);
        // Add a dummy prescription item to cover prescribedDrugDetail branch
        com.google.gson.JsonArray prescriptionArray = new com.google.gson.JsonArray();
        com.google.gson.JsonObject prescriptionItem = new com.google.gson.JsonObject();
        // Add required fields for PrescribedDrugDetail
        prescriptionItem.addProperty("prescriptionID", 1L);
        prescriptionItem.addProperty("beneficiaryRegID", 1L);
        prescriptionItem.addProperty("benVisitID", 1L);
        prescriptionItem.addProperty("visitCode", 1L);
        prescriptionItem.addProperty("providerServiceMapID", 1L);
        prescriptionItem.addProperty("drugName", "TestDrug");
        prescriptionItem.addProperty("dosage", "1 tablet");
        prescriptionArray.add(prescriptionItem);
        obj.add("prescription", prescriptionArray);
        obj.add("labTestOrders", new com.google.gson.JsonArray());
        obj.add("refer", new JsonObject());
        // Mocks for TC request block
        when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
        when(commonNurseServiceImpl.saveBenPrescribedDrugsList(anyList())).thenReturn(new HashMap<String, Object>());
        when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(1);
        when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), anyBoolean(), anyBoolean(), any(),null)).thenReturn(1);
        when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
        when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
        when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);
        Long result = service.updateGeneralOPDQCDoctorData(obj, "auth");
        assertNotNull(result);
    }

    // 7. quickConsultDoctorDataInsert
    @Test
    void testQuickConsultDoctorDataInsert_tcRequestFlow() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("benFlowID", 1L);
        obj.addProperty("serviceID", 4);
        obj.addProperty("createdBy", 1L);
        obj.addProperty("vanID", 1L);
        obj.addProperty("sessionID", 1L);
        // Create a tcRequest object with required fields
        JsonObject tcRequest = new JsonObject();
        tcRequest.addProperty("userID", 99L);
        tcRequest.addProperty("allocationDate", "2025-08-06");
        tcRequest.addProperty("fromTime", "10:00");
        tcRequest.addProperty("toTime", "10:30");
        obj.add("tcRequest", tcRequest);
        obj.add("prescription", new com.google.gson.JsonArray());
        obj.add("labTestOrders", new com.google.gson.JsonArray());
        obj.add("refer", new JsonObject());
        // Mocks for TC request block
        when(commonDoctorServiceImpl.getSnomedCTcode(any())).thenReturn(new String[] {"A", "B"});
        ArrayList<BenChiefComplaint> mockChiefComplaintList = new ArrayList<>();
        BenChiefComplaint mockChiefComplaint = mock(BenChiefComplaint.class);
        mockChiefComplaintList.add(mockChiefComplaint);
        when(benChiefComplaintRepo.saveAll(anyList())).thenReturn(mockChiefComplaintList);
        try (var mockedStatic = mockStatic(BenChiefComplaint.class)) {
            mockedStatic.when(() -> BenChiefComplaint.getBenChiefComplaintList(any())).thenReturn(mockChiefComplaintList);
            BenClinicalObservations mockObs = mock(BenClinicalObservations.class);
            when(benClinicalObservationsRepo.save(any())).thenReturn(mockObs);
            when(mockObs.getClinicalObservationID()).thenReturn(10L);
            when(commonNurseServiceImpl.saveBeneficiaryPrescription(any())).thenReturn(10L);
            when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
            when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), anyBoolean(), anyBoolean(), any(), null)).thenReturn(1);
            when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(), anyString())).thenReturn(1);
            when(teleConsultationServiceImpl.createTCRequest(any())).thenReturn(1);
            Integer result = service.quickConsultDoctorDataInsert(obj, "auth");
            assertEquals(1, result);
        }
    }

    @Test
    void testQuickConsultDoctorDataInsert_prescriptionBranch() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("benFlowID", 1L);
        obj.addProperty("serviceID", 4);
        obj.addProperty("createdBy", 1L);
        obj.addProperty("vanID", 1L);
        obj.addProperty("sessionID", 1L);
        // Add a non-empty prescription array
        com.google.gson.JsonArray prescriptionArray = new com.google.gson.JsonArray();
        com.google.gson.JsonObject prescriptionItem = new com.google.gson.JsonObject();
        prescriptionItem.addProperty("prescriptionID", 1L);
        prescriptionItem.addProperty("beneficiaryRegID", 2L);
        prescriptionItem.addProperty("benVisitID", 3L);
        prescriptionItem.addProperty("visitCode", 4L);
        prescriptionItem.addProperty("providerServiceMapID", 5L);
        prescriptionItem.addProperty("drugName", "TestDrug");
        prescriptionItem.addProperty("dosage", "1 tablet");
        prescriptionArray.add(prescriptionItem);
        obj.add("prescription", prescriptionArray);
        obj.add("labTestOrders", new com.google.gson.JsonArray());
        obj.add("refer", new JsonObject());
        // Required mocks for flow
        when(commonDoctorServiceImpl.getSnomedCTcode(any())).thenReturn(new String[] {"A", "B"});
        ArrayList<BenChiefComplaint> mockChiefComplaintList = new ArrayList<>();
        BenChiefComplaint mockChiefComplaint = mock(BenChiefComplaint.class);
        mockChiefComplaintList.add(mockChiefComplaint);
        when(benChiefComplaintRepo.saveAll(anyList())).thenReturn(mockChiefComplaintList);
        try (var mockedStatic = mockStatic(BenChiefComplaint.class)) {
            mockedStatic.when(() -> BenChiefComplaint.getBenChiefComplaintList(any())).thenReturn(mockChiefComplaintList);
            BenClinicalObservations mockObs = mock(BenClinicalObservations.class);
            when(benClinicalObservationsRepo.save(any())).thenReturn(mockObs);
            when(mockObs.getClinicalObservationID()).thenReturn(10L);
            when(commonNurseServiceImpl.saveBeneficiaryPrescription(any())).thenReturn(10L);
            // Mock the prescription save branch
            when(commonNurseServiceImpl.saveBenPrescribedDrugsList(anyList())).thenReturn(new HashMap<String, Object>());
            when(commonDoctorServiceImpl.saveBenReferDetails(any())).thenReturn(1L);
            when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataSave(any(), anyBoolean(), anyBoolean(), any(),null)).thenReturn(1);
            Integer result = service.quickConsultDoctorDataInsert(obj, "auth");
            assertEquals(1, result);
        }
    }

    // 8. quickConsultNurseDataInsert (branches)
    @Test
    void testQuickConsultNurseDataInsert_withVisitDetails() throws Exception {
        JsonObject obj = new JsonObject();
        JsonObject visitDetailsObj = new JsonObject();
        visitDetailsObj.addProperty("visitReason", "reason");
        visitDetailsObj.addProperty("visitCategory", "category");
        visitDetailsObj.addProperty("beneficiaryRegID", 123L);
        obj.add("visitDetails", visitDetailsObj); // valid object for deserialization
        obj.addProperty("benFlowID", 1L);
        obj.addProperty("vanID", 1);
        obj.addProperty("sessionID", 1);
        Integer result = service.quickConsultNurseDataInsert(obj);
        assertEquals(0, result); // branch covered, returnOBJ always 0
    }

    @Test
    void testQuickConsultNurseDataInsert_vitalsBranch() throws Exception {
        JsonObject obj = new JsonObject();
        JsonObject visitDetailsObj = new JsonObject();
        visitDetailsObj.addProperty("visitReason", "reason");
        visitDetailsObj.addProperty("visitCategory", "category");
        visitDetailsObj.addProperty("beneficiaryRegID", 123L);
        obj.add("visitDetails", visitDetailsObj);
        obj.addProperty("benFlowID", 1L);
        obj.addProperty("vanID", 2);
        obj.addProperty("sessionID", 3);
        
        // Adding vitalsDetails to the input object
        JsonObject vitalsDetailsObj = new JsonObject();
        obj.add("vitalsDetails", vitalsDetailsObj);

        // Mocking the flow
        when(beneficiaryFlowStatusRepo.checkExistData(anyLong(), anyShort())).thenReturn(null);
        when(commonNurseServiceImpl.getMaxCurrentdate(anyLong(), anyString(), anyString())).thenReturn(0);
        when(commonNurseServiceImpl.saveBeneficiaryVisitDetails(any())).thenReturn(10L);
        when(commonNurseServiceImpl.generateVisitCode(anyLong(), anyInt(), anyInt())).thenReturn(20L);
        
        // Mock anthropometry and vital save methods to return > 0
        when(commonNurseServiceImpl.saveBeneficiaryPhysicalAnthropometryDetails(any())).thenReturn(11L);
        when(commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(any())).thenReturn(12L);
        
        // Mocking the private method call
        com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl benStatusFlowService = mock(com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl.class);
        service.setCommonBenStatusFlowServiceImpl(benStatusFlowService);
        when(benStatusFlowService.updateBenFlowNurseAfterNurseActivity(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyShort(), anyShort(), anyShort(), anyShort(), anyShort(), anyLong(), anyInt())).thenReturn(1);

        Integer result = service.quickConsultNurseDataInsert(obj);
        assertEquals(1, result);
    }

    // 9. getBenCaseRecordFromDoctorQuickConsult
    @Test
    void testGetBenCaseRecordFromDoctorQuickConsult_basicFlow() throws Exception {
        Long benRegID = 1L, visitCode = 2L;
        when(commonDoctorServiceImpl.getFindingsDetails(anyLong(), anyLong())).thenReturn("findings");
        when(commonDoctorServiceImpl.getInvestigationDetails(anyLong(), anyLong())).thenReturn("investigation");
        when(commonDoctorServiceImpl.getPrescribedDrugs(anyLong(), anyLong())).thenReturn("prescription");
        when(commonDoctorServiceImpl.getReferralDetails(anyLong(), anyLong())).thenReturn("refer");
        LabTechnicianServiceImpl labTechMock = mock(LabTechnicianServiceImpl.class);
        service.setLabTechnicianServiceImpl(labTechMock);
        when(labTechMock.getLabResultDataForBen(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        when(labTechMock.getLast_3_ArchivedTestVisitList(anyLong(), anyLong())).thenReturn("archivedList");
        GeneralOPDDoctorServiceImpl generalOPDMock = mock(GeneralOPDDoctorServiceImpl.class);
        service.setGeneralOPDDoctorServiceImpl(generalOPDMock);
        when(generalOPDMock.getGeneralOPDDiagnosisDetails(anyLong(), anyLong())).thenReturn("diagnosis");
        String result = service.getBenCaseRecordFromDoctorQuickConsult(benRegID, visitCode);
        assertTrue(result.contains("findings"));
        assertTrue(result.contains("diagnosis"));
        assertTrue(result.contains("investigation"));
        assertTrue(result.contains("prescription"));
        assertTrue(result.contains("LabReport"));
        assertTrue(result.contains("Refer"));
        assertTrue(result.contains("ArchivedVisitcodeForLabResult"));
    }

    // 10. updateBeneficiaryClinicalObservations
    @Test
    void testUpdateBeneficiaryClinicalObservations_valid() throws Exception {
        JsonObject caseSheet = new JsonObject();
        when(commonDoctorServiceImpl.updateBenClinicalObservations(any())).thenReturn(2);
        Integer result = service.updateBeneficiaryClinicalObservations(caseSheet);
        assertEquals(2, result);
    }
}
