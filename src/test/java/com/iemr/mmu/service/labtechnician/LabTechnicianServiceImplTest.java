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
package com.iemr.mmu.service.labtechnician;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.labModule.LabResultEntry;
import com.iemr.mmu.data.labModule.WrapperLabResultEntry;
import com.iemr.mmu.data.labtechnician.V_benLabTestOrderedDetails;
import com.iemr.mmu.repo.labModule.LabResultEntryRepo;
import com.iemr.mmu.repo.labtechnician.V_benLabTestOrderedDetailsRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabTechnicianServiceImpl Test Cases")
class LabTechnicianServiceImplTest {

    @Mock
    private V_benLabTestOrderedDetailsRepo v_benLabTestOrderedDetailsRepo;

    @Mock
    private LabResultEntryRepo labResultEntryRepo;

    @Mock
    private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;

    @InjectMocks
    private LabTechnicianServiceImpl labTechnicianService;

    private Long benRegID;
    private Long visitCode;
    private Long prescriptionID;
    private Integer procedureID;
    private Integer testComponentID;

    @BeforeEach
    void setUp() {
        benRegID = 12345L;
        visitCode = 67890L;
        prescriptionID = 111L;
        procedureID = 222;
        testComponentID = 333;
    }

    @Test
    @DisplayName("Test getBenePrescribedProcedureDetails with no previous results")
    void testGetBenePrescribedProcedureDetailsNoPreviousResults() throws Exception {
        // Arrange
        ArrayList<LabResultEntry> emptyProcedureResults = new ArrayList<>();
        ArrayList<V_benLabTestOrderedDetails> labTestList = createMockLabTestOrderedDetailsList();
        ArrayList<V_benLabTestOrderedDetails> radioTestList = createMockRadiologyTestOrderedDetailsList();

        when(labResultEntryRepo.findByBeneficiaryRegIDAndVisitCodeOrderByProcedureIDAsc(benRegID, visitCode))
                .thenReturn(emptyProcedureResults);
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Laboratory"), any()))
                .thenReturn(labTestList);
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Radiology"), any()))
                .thenReturn(radioTestList);

        // Act
        String result = labTechnicianService.getBenePrescribedProcedureDetails(benRegID, visitCode);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertTrue(jsonResult.has("radiologyList"));
        assertTrue(jsonResult.has("laboratoryList"));
        assertTrue(jsonResult.has("archive"));
    }

    @Test
    @DisplayName("Test getBenePrescribedProcedureDetails with existing results")
    void testGetBenePrescribedProcedureDetailsWithExistingResults() throws Exception {
        // Arrange
        ArrayList<LabResultEntry> existingResults = createMockLabResultEntries();
        ArrayList<V_benLabTestOrderedDetails> labTestList = createMockLabTestOrderedDetailsList();
        ArrayList<V_benLabTestOrderedDetails> radioTestList = createMockRadiologyTestOrderedDetailsList();

        when(labResultEntryRepo.findByBeneficiaryRegIDAndVisitCodeOrderByProcedureIDAsc(benRegID, visitCode))
                .thenReturn(existingResults);
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Laboratory"), any()))
                .thenReturn(labTestList);
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Radiology"), any()))
                .thenReturn(radioTestList);

        // Mock static method
        try (MockedStatic<LabResultEntry> mockedStatic = mockStatic(LabResultEntry.class)) {
            mockedStatic.when(() -> LabResultEntry.getLabResultEntry(existingResults)).thenReturn(existingResults);

            // Act
            String result = labTechnicianService.getBenePrescribedProcedureDetails(benRegID, visitCode);

            // Assert
            assertNotNull(result);
            JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
            assertTrue(jsonResult.has("radiologyList"));
            assertTrue(jsonResult.has("laboratoryList"));
            assertTrue(jsonResult.has("archive"));
        }
    }

    @Test
    @DisplayName("Test getLabResultDataForBen")
    void testGetLabResultDataForBen() throws Exception {
        // Arrange
        ArrayList<LabResultEntry> mockResults = createMockLabResultEntries();
        when(labResultEntryRepo.findByBeneficiaryRegIDAndVisitCodeOrderByProcedureIDAsc(benRegID, visitCode))
                .thenReturn(mockResults);

        // Mock static method
        try (MockedStatic<LabResultEntry> mockedStatic = mockStatic(LabResultEntry.class)) {
            mockedStatic.when(() -> LabResultEntry.getLabResultEntry(mockResults)).thenReturn(mockResults);

            // Act
            ArrayList<LabResultEntry> result = labTechnicianService.getLabResultDataForBen(benRegID, visitCode);

            // Assert
            assertNotNull(result);
            assertEquals(mockResults.size(), result.size());
        }
    }

    @Test
    @DisplayName("Test saveLabTestResult with JsonObject - success")
    void testSaveLabTestResultWithJsonObjectSuccess() throws Exception {
        // Arrange
        JsonObject requestOBJ = createMockJsonRequest();
        WrapperLabResultEntry mockWrapper = createMockWrapperLabResultEntry();
        
        when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());
        when(commonBenStatusFlowServiceImpl.updateFlowAfterLabResultEntry(
                anyLong(), anyLong(), anyLong(), anyShort(), anyShort(), anyShort())).thenReturn(1);
        
        // Mock static method for InputMapper
        try (MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class)) {
            InputMapper mockInputMapperInstance = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(any(JsonObject.class), eq(WrapperLabResultEntry.class)))
                    .thenReturn(mockWrapper);
        
            // Act
            Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test saveLabTestResult with JsonObject - null request")
    void testSaveLabTestResultWithJsonObjectNull() throws Exception {
        // Act
        Integer result = labTechnicianService.saveLabTestResult((JsonObject) null);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with JsonObject - no labTestResults")
    void testSaveLabTestResultWithJsonObjectNoLabTestResults() throws Exception {
        // Arrange
        JsonObject requestOBJ = new JsonObject();

        // Act
        Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with WrapperLabResultEntry - success")
    void testSaveLabTestResultWithWrapperSuccess() {
        // Arrange
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with WrapperLabResultEntry - empty lists")
    void testSaveLabTestResultWithWrapperEmptyLists() {
        // Arrange
        WrapperLabResultEntry wrapper = new WrapperLabResultEntry();
        wrapper.setLabTestResults(new ArrayList<>());
        wrapper.setRadiologyTestResults(new ArrayList<>());

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with WrapperLabResultEntry - null lists")
    void testSaveLabTestResultWithWrapperNullLists() {
        // Arrange
        WrapperLabResultEntry wrapper = new WrapperLabResultEntry();
        wrapper.setLabTestResults(null);
        wrapper.setRadiologyTestResults(null);

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with radiology results and file IDs")
    void testSaveLabTestResultWithRadiologyResults() {
        // Arrange
        WrapperLabResultEntry wrapper = createMockWrapperWithRadiologyResults();
        when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with ECG abnormalities")
    void testSaveLabTestResultWithEcgAbnormalities() {
        // Arrange
        WrapperLabResultEntry wrapper = createMockWrapperWithEcgAbnormalities();
        when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test saveLabTestResult with strips not available")
    void testSaveLabTestResultWithStripsNotAvailable() {
        // Arrange
        WrapperLabResultEntry wrapper = createMockWrapperWithStripsNotAvailable();
        when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowStatusFlagAfterLabResultEntry - specialist flow completed")
    void testUpdateBenFlowStatusFlagSpecialistCompleted() throws Exception {
        // Arrange
        JsonObject requestOBJ = createMockJsonRequestForSpecialist(true, (short) 2);
        WrapperLabResultEntry mockWrapper = createMockWrapperLabResultEntryForSpecialist(true, (short) 2);
        InputMapper mockInputMapper = mock(InputMapper.class);
        
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            mockedStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(WrapperLabResultEntry.class)))
                    .thenReturn(mockWrapper);
            when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());
            when(commonBenStatusFlowServiceImpl.updateFlowAfterLabResultEntryForTCSpecialist(
                    anyLong(), anyLong(), anyShort())).thenReturn(1);

            // Act
            Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

            // Assert
            assertEquals(1, result);
            verify(commonBenStatusFlowServiceImpl).updateFlowAfterLabResultEntryForTCSpecialist(
                    anyLong(), anyLong(), eq((short) 3));
        }
    }

    @Test
    @DisplayName("Test updateBenFlowStatusFlagAfterLabResultEntry - specialist flow not completed")
    void testUpdateBenFlowStatusFlagSpecialistNotCompleted() throws Exception {
        // Arrange
        JsonObject requestOBJ = createMockJsonRequestForSpecialist(false, (short) 2);
        WrapperLabResultEntry mockWrapper = createMockWrapperLabResultEntryForSpecialist(false, (short) 2);
        InputMapper mockInputMapper = mock(InputMapper.class);
        
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            mockedStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(WrapperLabResultEntry.class)))
                    .thenReturn(mockWrapper);
            when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());
            when(commonBenStatusFlowServiceImpl.updateFlowAfterLabResultEntryForTCSpecialist(
                    anyLong(), anyLong(), anyShort())).thenReturn(1);

            // Act
            Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

            // Assert
            assertEquals(1, result);
            verify(commonBenStatusFlowServiceImpl).updateFlowAfterLabResultEntryForTCSpecialist(
                    anyLong(), anyLong(), eq((short) 2));
        }
    }

    @Test
    @DisplayName("Test updateBenFlowStatusFlagAfterLabResultEntry - nurse flow completed")
    void testUpdateBenFlowStatusFlagNurseCompleted() throws Exception {
        // Arrange
        JsonObject requestOBJ = createMockJsonRequestForNurse(true, (short) 2, (short) 1);
        WrapperLabResultEntry mockWrapper = createMockWrapperLabResultEntryForNurse(true, (short) 2, (short) 1);
        InputMapper mockInputMapper = mock(InputMapper.class);
        
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            mockedStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(WrapperLabResultEntry.class)))
                    .thenReturn(mockWrapper);
            when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());
            when(commonBenStatusFlowServiceImpl.updateFlowAfterLabResultEntry(
                    anyLong(), anyLong(), anyLong(), anyShort(), anyShort(), anyShort())).thenReturn(1);

            // Act
            Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

            // Assert
            assertEquals(1, result);
            verify(commonBenStatusFlowServiceImpl).updateFlowAfterLabResultEntry(
                    anyLong(), anyLong(), anyLong(), eq((short) 3), eq((short) 1), eq((short) 1));
        }
    }

    @Test
    @DisplayName("Test updateBenFlowStatusFlagAfterLabResultEntry - doctor flow completed")
    void testUpdateBenFlowStatusFlagDoctorCompleted() throws Exception {
        // Arrange
        JsonObject requestOBJ = createMockJsonRequestForDoctor(true, (short) 3, (short) 2);
        WrapperLabResultEntry mockWrapper = createMockWrapperLabResultEntryForDoctor(true, (short) 3, (short) 2);
        InputMapper mockInputMapper = mock(InputMapper.class);
        
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            mockedStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(WrapperLabResultEntry.class)))
                    .thenReturn(mockWrapper);
            when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());
            when(commonBenStatusFlowServiceImpl.updateFlowAfterLabResultEntry(
                    anyLong(), anyLong(), anyLong(), anyShort(), anyShort(), anyShort())).thenReturn(1);

            // Act
            Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

            // Assert
            assertEquals(1, result);
            verify(commonBenStatusFlowServiceImpl).updateFlowAfterLabResultEntry(
                    anyLong(), anyLong(), anyLong(), eq((short) 3), eq((short) 3), eq((short) 1));
        }
    }

    @Test
    @DisplayName("Test updateBenFlowStatusFlagAfterLabResultEntry - not completed")
    void testUpdateBenFlowStatusFlagNotCompleted() throws Exception {
        // Arrange
        JsonObject requestOBJ = createMockJsonRequestForNurse(false, (short) 2, (short) 1);
        WrapperLabResultEntry mockWrapper = createMockWrapperLabResultEntryForNurse(false, (short) 2, (short) 1);
        InputMapper mockInputMapper = mock(InputMapper.class);
        
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            mockedStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(WrapperLabResultEntry.class)))
                    .thenReturn(mockWrapper);
            when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(createMockLabResultEntries());
            when(commonBenStatusFlowServiceImpl.updateFlowAfterLabResultEntry(
                    anyLong(), anyLong(), anyLong(), anyShort(), anyShort(), anyShort())).thenReturn(1);

            // Act
            Integer result = labTechnicianService.saveLabTestResult(requestOBJ);

            // Assert
            assertEquals(1, result);
            verify(commonBenStatusFlowServiceImpl).updateFlowAfterLabResultEntry(
                    anyLong(), anyLong(), anyLong(), eq((short) 2), eq((short) 1), eq((short) 1));
        }
    }

    @Test
    @DisplayName("Test getLast_3_ArchivedTestVisitList")
    void testGetLast3ArchivedTestVisitList() {
        // Arrange
        ArrayList<Object[]> mockVisitCodeList = createMockVisitCodeList();
        when(labResultEntryRepo.getLast_3_visitForLabTestDone(benRegID, visitCode))
                .thenReturn(mockVisitCodeList);

        // Mock static method
        try (MockedStatic<LabResultEntry> mockedStatic = mockStatic(LabResultEntry.class)) {
            mockedStatic.when(() -> LabResultEntry.getVisitCodeAndDate(mockVisitCodeList)).thenReturn(new ArrayList<>());

            // Act
            String result = labTechnicianService.getLast_3_ArchivedTestVisitList(benRegID, visitCode);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test getLabResultForVisitcode")
    void testGetLabResultForVisitcode() throws Exception {
        // Arrange
        ArrayList<LabResultEntry> mockResults = createMockLabResultEntries();
        when(labResultEntryRepo.findByBeneficiaryRegIDAndVisitCodeOrderByProcedureIDAsc(benRegID, visitCode))
                .thenReturn(mockResults);

        // Mock static method
        try (MockedStatic<LabResultEntry> mockedStatic = mockStatic(LabResultEntry.class)) {
            mockedStatic.when(() -> LabResultEntry.getLabResultEntry(mockResults)).thenReturn(mockResults);

            // Act
            String result = labTechnicianService.getLabResultForVisitcode(benRegID, visitCode);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test getPrescribedLabTestInJsonFormatlaboratory with multiple procedures and components")
    void testGetPrescribedLabTestInJsonFormatlaboratoryMultiple() throws Exception {
        // Arrange
        ArrayList<V_benLabTestOrderedDetails> complexLabTestList = createComplexMockLabTestOrderedDetailsList();
        when(labResultEntryRepo.findByBeneficiaryRegIDAndVisitCodeOrderByProcedureIDAsc(benRegID, visitCode))
                .thenReturn(new ArrayList<>());
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Laboratory"), any()))
                .thenReturn(complexLabTestList);
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Radiology"), any()))
                .thenReturn(new ArrayList<>());

        // Mock static method
        try (MockedStatic<LabResultEntry> mockedStatic = mockStatic(LabResultEntry.class)) {
            mockedStatic.when(() -> LabResultEntry.getLabResultEntry(any())).thenReturn(new ArrayList<>());

            // Act
            String result = labTechnicianService.getBenePrescribedProcedureDetails(benRegID, visitCode);

            // Assert
            assertNotNull(result);
            JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
            assertTrue(jsonResult.has("laboratoryList"));
        }
    }

    @Test
    @DisplayName("Test saveLabTestResult failure scenario")
    void testSaveLabTestResultFailure() {
        // Arrange
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        List<LabResultEntry> savedResults = new ArrayList<>(); // Empty list to simulate failure
        when(labResultEntryRepo.saveAll(anyIterable())).thenReturn(savedResults);

        // Act
        Integer result = labTechnicianService.saveLabTestResult(wrapper);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Test getPrescribedLabTestInJsonFormatlaboratory - multiple components same procedure edge case")
    void testGetPrescribedLabTestInJsonFormatlaboratoryMultipleComponentsSameProcedure() throws Exception {
        // Arrange - Create scenario where same procedure has multiple components with different result values
        ArrayList<V_benLabTestOrderedDetails> complexLabTestList = createLabTestListForComponentBranching();
        when(labResultEntryRepo.findByBeneficiaryRegIDAndVisitCodeOrderByProcedureIDAsc(benRegID, visitCode))
                .thenReturn(new ArrayList<>());
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Laboratory"), any()))
                .thenReturn(complexLabTestList);
        when(v_benLabTestOrderedDetailsRepo
                .findDistinctByBeneficiaryRegIDAndVisitCodeAndProcedureTypeAndProcedureIDNotInOrderByProcedureIDAscTestComponentIDAscResultValueAsc(
                        eq(benRegID), eq(visitCode), eq("Radiology"), any()))
                .thenReturn(new ArrayList<>());

        // Mock static method
        try (MockedStatic<LabResultEntry> mockedStatic = mockStatic(LabResultEntry.class)) {
            mockedStatic.when(() -> LabResultEntry.getLabResultEntry(any())).thenReturn(new ArrayList<>());

            // Act
            String result = labTechnicianService.getBenePrescribedProcedureDetails(benRegID, visitCode);

            // Assert
            assertNotNull(result);
            JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
            assertTrue(jsonResult.has("laboratoryList"));
            
            // Verify the structure contains the expected components
            assertTrue(jsonResult.getAsJsonArray("laboratoryList").size() > 0);
        }
    }

    // Helper methods for creating mock objects

    private ArrayList<LabResultEntry> createMockLabResultEntries() {
        ArrayList<LabResultEntry> list = new ArrayList<>();
        LabResultEntry entry = new LabResultEntry();
        entry.setID(BigInteger.valueOf(1));
        entry.setBeneficiaryRegID(benRegID);
        entry.setVisitCode(visitCode);
        entry.setProcedureID(procedureID);
        entry.setTestComponentID(testComponentID);
        entry.setTestResultValue("Normal");
        list.add(entry);
        return list;
    }

    private ArrayList<V_benLabTestOrderedDetails> createMockLabTestOrderedDetailsList() {
        ArrayList<V_benLabTestOrderedDetails> list = new ArrayList<>();
        V_benLabTestOrderedDetails details = new V_benLabTestOrderedDetails();
        details.setBeneficiaryRegID(benRegID);
        details.setVisitCode(visitCode);
        details.setProcedureID(procedureID);
        details.setProcedureName("Blood Test");
        details.setProcedureDesc("Complete Blood Count");
        details.setProcedureType("Laboratory");
        details.setPrescriptionID(prescriptionID);
        details.setTestComponentID(testComponentID);
        details.setTestComponentName("Hemoglobin");
        details.setTestComponentDesc("Hemoglobin Level");
        details.setInputType("Number");
        details.setMeasurementUnit("g/dL");
        details.setRange_min(10);
        details.setRange_max(20);
        details.setRange_normal_min(12);
        details.setRange_normal_max(16);
        details.setResultValue("Normal");
        details.setIsDecimal(true);
        details.setIotProcedureName("IOT Blood Test");
        details.setProcedureCode("BT001");
        details.setProcedureStartAPI("/start");
        details.setProcedureEndAPI("/end");
        details.setProcedureStatusAPI("/status");
        details.setIsLabProcedure(true);
        details.setDiscoveryCode("DC001");
        details.setIsMandatory(true);
        details.setCalibrationStartAPI("/cal-start");
        details.setCalibrationStatusAPI("/cal-status");
        details.setCalibrationEndAPI("/cal-end");
        details.setIOTComponentName("IOT Hemoglobin");
        details.setComponentCode("CC001");
        details.setIOTProcedureID("ITP001");
        details.setComponentUnit("g/dL");
        list.add(details);
        return list;
    }

    private ArrayList<V_benLabTestOrderedDetails> createComplexMockLabTestOrderedDetailsList() {
        ArrayList<V_benLabTestOrderedDetails> list = new ArrayList<>();
        
        // First procedure, first component
        V_benLabTestOrderedDetails details1 = new V_benLabTestOrderedDetails();
        details1.setBeneficiaryRegID(benRegID);
        details1.setVisitCode(visitCode);
        details1.setProcedureID(100);
        details1.setProcedureName("Blood Test");
        details1.setProcedureDesc("Complete Blood Count");
        details1.setProcedureType("Laboratory");
        details1.setPrescriptionID(prescriptionID);
        details1.setTestComponentID(300);
        details1.setTestComponentName("Hemoglobin");
        details1.setTestComponentDesc("Hemoglobin Level");
        details1.setInputType("Number");
        details1.setMeasurementUnit("g/dL");
        details1.setResultValue("Normal");
        details1.setIsDecimal(true);
        list.add(details1);
        
        // First procedure, second component
        V_benLabTestOrderedDetails details2 = new V_benLabTestOrderedDetails();
        details2.setBeneficiaryRegID(benRegID);
        details2.setVisitCode(visitCode);
        details2.setProcedureID(100);
        details2.setProcedureName("Blood Test");
        details2.setProcedureDesc("Complete Blood Count");
        details2.setProcedureType("Laboratory");
        details2.setPrescriptionID(prescriptionID);
        details2.setTestComponentID(301);
        details2.setTestComponentName("WBC Count");
        details2.setTestComponentDesc("White Blood Cell Count");
        details2.setInputType("Number");
        details2.setMeasurementUnit("cells/μL");
        details2.setResultValue("High");
        details2.setIsDecimal(false);
        list.add(details2);
        
        // Second procedure
        V_benLabTestOrderedDetails details3 = new V_benLabTestOrderedDetails();
        details3.setBeneficiaryRegID(benRegID);
        details3.setVisitCode(visitCode);
        details3.setProcedureID(200);
        details3.setProcedureName("Urine Test");
        details3.setProcedureDesc("Urine Analysis");
        details3.setProcedureType("Laboratory");
        details3.setPrescriptionID(prescriptionID);
        details3.setTestComponentID(400);
        details3.setTestComponentName("Protein");
        details3.setTestComponentDesc("Protein in Urine");
        details3.setInputType("Text");
        details3.setMeasurementUnit("mg/dL");
        details3.setResultValue("Trace");
        details3.setIsDecimal(true);
        list.add(details3);
        
        return list;
    }

    private ArrayList<V_benLabTestOrderedDetails> createLabTestListForComponentBranching() {
        ArrayList<V_benLabTestOrderedDetails> list = new ArrayList<>();
        
        // Same procedure (100), same component (300) - First entry will create initial compDetails
        V_benLabTestOrderedDetails details1 = new V_benLabTestOrderedDetails();
        details1.setBeneficiaryRegID(benRegID);
        details1.setVisitCode(visitCode);
        details1.setProcedureID(100);  // Same procedure ID
        details1.setProcedureName("Blood Test");
        details1.setProcedureDesc("Complete Blood Count");
        details1.setProcedureType("Laboratory");
        details1.setPrescriptionID(prescriptionID);
        details1.setTestComponentID(300);  // Same component ID
        details1.setTestComponentName("Hemoglobin");
        details1.setTestComponentDesc("Hemoglobin Level");
        details1.setInputType("Number");
        details1.setMeasurementUnit("g/dL");
        details1.setRange_min(10);
        details1.setRange_max(20);
        details1.setRange_normal_min(12);
        details1.setRange_normal_max(16);
        details1.setResultValue("Normal");  // First result value
        details1.setIsDecimal(true);
        details1.setIotProcedureName("IOT Blood Test");
        details1.setProcedureCode("BT001");
        details1.setProcedureStartAPI("/start");
        details1.setProcedureEndAPI("/end");
        details1.setProcedureStatusAPI("/status");
        details1.setIsLabProcedure(true);
        details1.setDiscoveryCode("DC001");
        details1.setIsMandatory(true);
        details1.setCalibrationStartAPI("/cal-start");
        details1.setCalibrationStatusAPI("/cal-status");
        details1.setCalibrationEndAPI("/cal-end");
        details1.setIOTComponentName("IOT Hemoglobin");
        details1.setComponentCode("CC001");
        details1.setIOTProcedureID("ITP001");
        details1.setComponentUnit("g/dL");
        list.add(details1);
        
        // Same procedure (100), same component (300) - This will trigger the else branch for adding to compOptionList
        V_benLabTestOrderedDetails details2 = new V_benLabTestOrderedDetails();
        details2.setBeneficiaryRegID(benRegID);
        details2.setVisitCode(visitCode);
        details2.setProcedureID(100);  // Same procedure ID
        details2.setProcedureName("Blood Test");
        details2.setProcedureDesc("Complete Blood Count");
        details2.setProcedureType("Laboratory");
        details2.setPrescriptionID(prescriptionID);
        details2.setTestComponentID(300);  // Same component ID - this triggers the else branch in the innermost condition
        details2.setTestComponentName("Hemoglobin");
        details2.setTestComponentDesc("Hemoglobin Level");
        details2.setInputType("Number");
        details2.setMeasurementUnit("g/dL");
        details2.setRange_min(10);
        details2.setRange_max(20);
        details2.setRange_normal_min(12);
        details2.setRange_normal_max(16);
        details2.setResultValue("High");  // Different result value - this will add to existing compOptionList
        details2.setIsDecimal(true);
        details2.setIotProcedureName("IOT Blood Test");
        details2.setProcedureCode("BT001");
        details2.setProcedureStartAPI("/start");
        details2.setProcedureEndAPI("/end");
        details2.setProcedureStatusAPI("/status");
        details2.setIsLabProcedure(true);
        details2.setDiscoveryCode("DC001");
        details2.setIsMandatory(true);
        details2.setCalibrationStartAPI("/cal-start");
        details2.setCalibrationStatusAPI("/cal-status");
        details2.setCalibrationEndAPI("/cal-end");
        details2.setIOTComponentName("IOT Hemoglobin");
        details2.setComponentCode("CC001");
        details2.setIOTProcedureID("ITP001");
        details2.setComponentUnit("g/dL");
        list.add(details2);
        
        // Same procedure (100), different component (301) - This will trigger the else branch for creating new compDetails
        V_benLabTestOrderedDetails details3 = new V_benLabTestOrderedDetails();
        details3.setBeneficiaryRegID(benRegID);
        details3.setVisitCode(visitCode);
        details3.setProcedureID(100);  // Same procedure ID - procedure exists
        details3.setProcedureName("Blood Test");
        details3.setProcedureDesc("Complete Blood Count");
        details3.setProcedureType("Laboratory");
        details3.setPrescriptionID(prescriptionID);
        details3.setTestComponentID(301);  // Different component ID - this triggers the target else branch
        details3.setTestComponentName("WBC Count");
        details3.setTestComponentDesc("White Blood Cell Count");
        details3.setInputType("Number");
        details3.setMeasurementUnit("cells/μL");
        details3.setRange_min(4000);
        details3.setRange_max(11000);
        details3.setRange_normal_min(4500);
        details3.setRange_normal_max(10500);
        details3.setResultValue("Normal");
        details3.setIsDecimal(false);
        details3.setIOTComponentName("IOT WBC");
        details3.setComponentCode("CC002");
        details3.setIOTProcedureID("ITP001");
        details3.setComponentUnit("cells/μL");
        list.add(details3);
        
        return list;
    }

    private ArrayList<V_benLabTestOrderedDetails> createMockRadiologyTestOrderedDetailsList() {
        ArrayList<V_benLabTestOrderedDetails> list = new ArrayList<>();
        V_benLabTestOrderedDetails details = new V_benLabTestOrderedDetails();
        details.setBeneficiaryRegID(benRegID);
        details.setVisitCode(visitCode);
        details.setProcedureID(procedureID + 1);
        details.setProcedureName("X-Ray Chest");
        details.setProcedureDesc("Chest X-Ray");
        details.setProcedureType("Radiology");
        details.setPrescriptionID(prescriptionID);
        details.setTestComponentID(testComponentID + 1);
        details.setTestComponentName("Chest X-Ray");
        details.setTestComponentDesc("Chest X-Ray Report");
        details.setInputType("File");
        list.add(details);
        return list;
    }

    private WrapperLabResultEntry createMockWrapperLabResultEntry() {
        WrapperLabResultEntry wrapper = new WrapperLabResultEntry();
        wrapper.setBeneficiaryRegID(benRegID);
        wrapper.setVisitID(123L);
        wrapper.setVisitCode(visitCode);
        wrapper.setProviderServiceMapID(1);
        wrapper.setCreatedBy("test-user");
        wrapper.setLabCompleted(true);
        wrapper.setBenFlowID(456L);
        wrapper.setNurseFlag((short) 2);
        wrapper.setDoctorFlag((short) 1);
        wrapper.setVanID(10);
        wrapper.setParkingPlaceID(20);

        List<LabResultEntry> labResults = new ArrayList<>();
        LabResultEntry labResult = new LabResultEntry();
        labResult.setPrescriptionID(prescriptionID);
        labResult.setProcedureID(procedureID);
        
        List<Map<String, Object>> compList = new ArrayList<>();
        Map<String, Object> comp = new HashMap<>();
        comp.put("testComponentID", testComponentID.doubleValue());
        comp.put("testResultValue", "Normal");
        comp.put("testResultUnit", "g/dL");
        comp.put("remarks", "All normal");
        compList.add(comp);
        
        labResult.setCompList(compList);
        labResults.add(labResult);
        wrapper.setLabTestResults(labResults);
        wrapper.setRadiologyTestResults(new ArrayList<>());

        return wrapper;
    }

    private WrapperLabResultEntry createMockWrapperWithRadiologyResults() {
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        
        // Ensure lab test results are empty or properly configured to avoid processing issues
        wrapper.setLabTestResults(new ArrayList<>());
        
        List<LabResultEntry> radiologyResults = new ArrayList<>();
        LabResultEntry radiologyResult = new LabResultEntry();
        radiologyResult.setPrescriptionID(prescriptionID);
        radiologyResult.setProcedureID(procedureID + 1);
        radiologyResult.setFileIDs(new String[]{"file1", "file2"});
        radiologyResults.add(radiologyResult);
        
        wrapper.setRadiologyTestResults(radiologyResults);
        return wrapper;
    }

    private WrapperLabResultEntry createMockWrapperWithEcgAbnormalities() {
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        
        List<LabResultEntry> labResults = wrapper.getLabTestResults();
        LabResultEntry labResult = labResults.get(0);
        
        List<Map<String, Object>> compList = labResult.getCompList();
        Map<String, Object> comp = compList.get(0);
        
        List<String> ecgAbnormalities = new ArrayList<>();
        ecgAbnormalities.add("Abnormality1");
        ecgAbnormalities.add("Abnormality2");
        comp.put("ecgAbnormalities", ecgAbnormalities);
        
        return wrapper;
    }

    private WrapperLabResultEntry createMockWrapperWithStripsNotAvailable() {
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        
        List<LabResultEntry> labResults = wrapper.getLabTestResults();
        LabResultEntry labResult = labResults.get(0);
        labResult.setStripsNotAvailable(true);
        
        List<Map<String, Object>> compList = labResult.getCompList();
        Map<String, Object> comp = compList.get(0);
        comp.put("stripsNotAvailable", "true");
        comp.remove("testResultValue"); // Remove to test strips not available scenario
        
        return wrapper;
    }

    private JsonObject createMockJsonRequest() {
        JsonObject request = new JsonObject();
        request.addProperty("beneficiaryRegID", benRegID);
        request.addProperty("visitID", 123L);
        request.addProperty("visitCode", visitCode);
        request.addProperty("providerServiceMapID", 1);
        request.addProperty("createdBy", "test-user");
        request.addProperty("labCompleted", true);
        request.addProperty("benFlowID", 456L);
        request.addProperty("nurseFlag", 2);
        request.addProperty("doctorFlag", 1);
        request.addProperty("vanID", 10);
        request.addProperty("parkingPlaceID", 20);
        
        String labTestResults = "[{\"prescriptionID\": " + prescriptionID + ", \"procedureID\": " + procedureID + 
                ", \"compList\": [{\"testComponentID\": " + testComponentID + 
                ", \"testResultValue\": \"Normal\", \"testResultUnit\": \"g/dL\", \"remarks\": \"All normal\"}]}]";
        request.add("labTestResults", JsonParser.parseString(labTestResults));
        
        return request;
    }

    private JsonObject createMockJsonRequestForSpecialist(boolean labCompleted, short specialistFlag) {
        JsonObject request = createMockJsonRequest();
        request.addProperty("labCompleted", labCompleted);
        request.addProperty("specialist_flag", specialistFlag);
        return request;
    }

    private JsonObject createMockJsonRequestForNurse(boolean labCompleted, short nurseFlag, short doctorFlag) {
        JsonObject request = createMockJsonRequest();
        request.addProperty("labCompleted", labCompleted);
        request.addProperty("nurseFlag", nurseFlag);
        request.addProperty("doctorFlag", doctorFlag);
        request.remove("specialist_flag");
        return request;
    }

    private JsonObject createMockJsonRequestForDoctor(boolean labCompleted, short nurseFlag, short doctorFlag) {
        JsonObject request = createMockJsonRequest();
        request.addProperty("labCompleted", labCompleted);
        request.addProperty("nurseFlag", nurseFlag);
        request.addProperty("doctorFlag", doctorFlag);
        request.remove("specialist_flag");
        return request;
    }

    private ArrayList<Object[]> createMockVisitCodeList() {
        ArrayList<Object[]> list = new ArrayList<>();
        Object[] visitData = new Object[]{visitCode, new java.sql.Date(System.currentTimeMillis())};
        list.add(visitData);
        return list;
    }

    private WrapperLabResultEntry createMockWrapperLabResultEntryForSpecialist(boolean labCompleted, short specialistFlag) {
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        wrapper.setLabCompleted(labCompleted);
        wrapper.setSpecialist_flag(specialistFlag);
        return wrapper;
    }

    private WrapperLabResultEntry createMockWrapperLabResultEntryForNurse(boolean labCompleted, short nurseFlag, short doctorFlag) {
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        wrapper.setLabCompleted(labCompleted);
        wrapper.setNurseFlag(nurseFlag);
        wrapper.setDoctorFlag(doctorFlag);
        wrapper.setSpecialist_flag(null);
        return wrapper;
    }

    private WrapperLabResultEntry createMockWrapperLabResultEntryForDoctor(boolean labCompleted, short nurseFlag, short doctorFlag) {
        WrapperLabResultEntry wrapper = createMockWrapperLabResultEntry();
        wrapper.setLabCompleted(labCompleted);
        wrapper.setNurseFlag(nurseFlag);
        wrapper.setDoctorFlag(doctorFlag);
        wrapper.setSpecialist_flag(null);
        return wrapper;
    }
}
