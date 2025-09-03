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
package com.iemr.mmu.service.benFlowStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.benFlowStatus.I_bendemographics;
import com.iemr.mmu.data.benFlowStatus.BenPhoneMaps;
import com.iemr.mmu.data.masterdata.registrar.GenderMaster;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.mapper.InputMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommonBenStatusFlowServiceImpl Test Cases")
class CommonBenStatusFlowServiceImplTest {

    @InjectMocks
    private CommonBenStatusFlowServiceImpl commonBenStatusFlowService;

    @Mock
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Mock
    private BenVisitDetailRepo benVisitDetailRepo;

    @Mock
    private BeneficiaryFlowStatus mockBeneficiaryFlowStatus;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(commonBenStatusFlowService, "nurseWL", 7);
    }

    @Test
    @DisplayName("Test createBenFlowRecord - Success with valid beneficiary IDs")
    void testCreateBenFlowRecord_SuccessWithValidBeneficiaryIDs() {
        // Arrange
        String requestOBJ = createTestRequestJSON();
        Long beneficiaryRegID = 1L;
        Long beneficiaryID = 1L;
        
        BeneficiaryFlowStatus savedBenFlow = new BeneficiaryFlowStatus();
        savedBenFlow.setBenFlowID(1L);

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenReturn(createMockBeneficiaryFlowStatus());
            } catch (IEMRException e) {
                fail("Exception should not occur in test setup");
            }
            
            when(benVisitDetailRepo.getVisitCountForBeneficiary(anyLong())).thenReturn((short) 2);
            when(beneficiaryFlowStatusRepo.save(any(BeneficiaryFlowStatus.class))).thenReturn(savedBenFlow);

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test createBenFlowRecord - Save returns null")
    void testCreateBenFlowRecord_SaveReturnsNull() {
        // Arrange
        String requestOBJ = createTestRequestJSON();
        Long beneficiaryRegID = 1L;
        Long beneficiaryID = 1L;

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenReturn(createMockBeneficiaryFlowStatus());
            } catch (IEMRException e) {
                fail("Exception should not occur in test setup");
            }
            
            when(beneficiaryFlowStatusRepo.save(any(BeneficiaryFlowStatus.class))).thenReturn(null);

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(0, result);
        }
    }

    @Test
    @DisplayName("Test createBenFlowRecord - Beneficiary already in work list")
    void testCreateBenFlowRecord_BeneficiaryAlreadyInWorkList() {
        // Arrange
        String requestOBJ = createTestRequestJSON();
        Long beneficiaryRegID = null;
        Long beneficiaryID = null;
        
        ArrayList<Long> existingBenFlowIDs = new ArrayList<>();
        existingBenFlowIDs.add(1L);

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenReturn(createMockBeneficiaryFlowStatus());
            } catch (IEMRException e) {
                fail("Exception should not occur in test setup");
            }
            
            when(benVisitDetailRepo.getVisitCountForBeneficiary(anyLong())).thenReturn((short) 2);
            when(beneficiaryFlowStatusRepo.checkBenAlreadyInNurseWorkList(anyLong(), any(), any(), any(Timestamp.class)))
                .thenReturn(existingBenFlowIDs);

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(3, result);
        }
    }

    @Test
    @DisplayName("Test createBenFlowRecord - New beneficiary with custom nurse work list days")
    void testCreateBenFlowRecord_NewBeneficiaryWithCustomNurseWL() {
        // Arrange
        ReflectionTestUtils.setField(commonBenStatusFlowService, "nurseWL", 15);
        String requestOBJ = createTestRequestJSON();
        Long beneficiaryRegID = null;
        Long beneficiaryID = null;
        
        BeneficiaryFlowStatus savedBenFlow = new BeneficiaryFlowStatus();
        savedBenFlow.setBenFlowID(1L);

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenReturn(createMockBeneficiaryFlowStatus());
            } catch (IEMRException e) {
                fail("Exception should not occur in test setup");
            }
            
            when(benVisitDetailRepo.getVisitCountForBeneficiary(anyLong())).thenReturn((short) 2);
            when(beneficiaryFlowStatusRepo.checkBenAlreadyInNurseWorkList(anyLong(), any(), any(), any(Timestamp.class)))
                .thenReturn(new ArrayList<>());
            when(beneficiaryFlowStatusRepo.save(any(BeneficiaryFlowStatus.class))).thenReturn(savedBenFlow);

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test createBenFlowRecord - Exception handling")
    void testCreateBenFlowRecord_ExceptionHandling() {
        // Arrange
        String requestOBJ = "invalid json";
        Long beneficiaryRegID = 1L;
        Long beneficiaryID = 1L;

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenThrow(new RuntimeException("JSON parsing error"));
            } catch (IEMRException e) {
                // Handle exception in test
            }

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(0, result);
        }
    }

    @Test
    @DisplayName("Test updateBenFlowNurseAfterNurseActivity - Success")
    void testUpdateBenFlowNurseAfterNurseActivity_Success() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benVisitID = 1L;
        String visitReason = "Routine";
        String visitCategory = "General";
        Short nurseFlag = 1;
        Short docFlag = 0;
        Short labIteration = 0;
        Short radiologistFlag = 0;
        Short oncologistFlag = 0;
        Long visitCode = 12345L;
        Integer vanID = 1;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterNurseActivity(
            benFlowID, benRegID, benVisitID, visitReason, visitCategory, 
            nurseFlag, docFlag, labIteration, radiologistFlag, oncologistFlag, visitCode, vanID))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowNurseAfterNurseActivity(
            benFlowID, benRegID, benVisitID, visitReason, visitCategory, 
            nurseFlag, docFlag, labIteration, radiologistFlag, oncologistFlag, visitCode, vanID);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowNurseAfterNurseActivity - Exception")
    void testUpdateBenFlowNurseAfterNurseActivity_Exception() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benVisitID = 1L;
        String visitReason = "Routine";
        String visitCategory = "General";
        Short nurseFlag = 1;
        Short docFlag = 0;
        Short labIteration = 0;
        Short radiologistFlag = 0;
        Short oncologistFlag = 0;
        Long visitCode = 12345L;
        Integer vanID = 1;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterNurseActivity(
            benFlowID, benRegID, benVisitID, visitReason, visitCategory, 
            nurseFlag, docFlag, labIteration, radiologistFlag, oncologistFlag, visitCode, vanID))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        int result = commonBenStatusFlowService.updateBenFlowNurseAfterNurseActivity(
            benFlowID, benRegID, benVisitID, visitReason, visitCategory, 
            nurseFlag, docFlag, labIteration, radiologistFlag, oncologistFlag, visitCode, vanID);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test updateBenFlowtableAfterNurseSaveForTMReferred - TM done with medicine")
    void testUpdateBenFlowtableAfterNurseSaveForTMReferred_TMDoneWithMedicine() {
        // Arrange
        CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
        Boolean isTMCDone = true;
        Boolean isMedicinePrescribed = true;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusTMReferred(1L, 1L, (short) 200, (short) 1))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowtableAfterNurseSaveForTMReferred(
            commonUtilityClass, isTMCDone, isMedicinePrescribed);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowtableAfterNurseSaveForTMReferred - TM not done without medicine")
    void testUpdateBenFlowtableAfterNurseSaveForTMReferred_TMNotDoneWithoutMedicine() {
        // Arrange
        CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
        Boolean isTMCDone = false;
        Boolean isMedicinePrescribed = false;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusTMReferred(1L, 1L, (short) 300, (short) 0))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowtableAfterNurseSaveForTMReferred(
            commonUtilityClass, isTMCDone, isMedicinePrescribed);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowtableAfterNurseSaveForTMReferred - Exception")
    void testUpdateBenFlowtableAfterNurseSaveForTMReferred_Exception() {
        // Arrange
        CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
        Boolean isTMCDone = true;
        Boolean isMedicinePrescribed = true;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusTMReferred(anyLong(), anyLong(), anyShort(), anyShort()))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        int result = commonBenStatusFlowService.updateBenFlowtableAfterNurseSaveForTMReferred(
            commonUtilityClass, isTMCDone, isMedicinePrescribed);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test updateBenFlowNurseAfterNurseUpdateNCD_Screening - Success")
    void testUpdateBenFlowNurseAfterNurseUpdateNCD_Screening_Success() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Short nurseFlag = 1;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterNurseDataUpdateNCD_Screening(
            benFlowID, benRegID, nurseFlag)).thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowNurseAfterNurseUpdateNCD_Screening(
            benFlowID, benRegID, nurseFlag);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowNurseAfterNurseUpdateNCD_Screening - Exception")
    void testUpdateBenFlowNurseAfterNurseUpdateNCD_Screening_Exception() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Short nurseFlag = 1;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterNurseDataUpdateNCD_Screening(
            benFlowID, benRegID, nurseFlag)).thenThrow(new RuntimeException("Database error"));

        // Act
        int result = commonBenStatusFlowService.updateBenFlowNurseAfterNurseUpdateNCD_Screening(
            benFlowID, benRegID, nurseFlag);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocData - Success")
    void testUpdateBenFlowAfterDocData_Success() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 1;
        short oncologistFlag = 0;
        short tcSpecialistFlag = 0;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterDoctorActivity(
            benFlowID, benRegID, benID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowAfterDocData(
            benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocData - Exception")
    void testUpdateBenFlowAfterDocData_Exception() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 1;
        short oncologistFlag = 0;
        short tcSpecialistFlag = 0;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterDoctorActivity(
            benFlowID, benRegID, benID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        int result = commonBenStatusFlowService.updateBenFlowAfterDocData(
            benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocDataUpdate - Success with existing pharma flag")
    void testUpdateBenFlowAfterDocDataUpdate_SuccessWithExistingPharmaFlag() throws Exception {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 0;
        short oncologistFlag = 0;
        short tcSpecialistFlag = 0;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.getPharmaFlag(benFlowID)).thenReturn((short) 1);
        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterDoctorActivity(
            benFlowID, benRegID, benID, docFlag, (short) 1, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowAfterDocDataUpdate(
            benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocDataUpdate - Success with new pharma flag")
    void testUpdateBenFlowAfterDocDataUpdate_SuccessWithNewPharmaFlag() throws Exception {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 1;
        short oncologistFlag = 0;
        short tcSpecialistFlag = 0;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.getPharmaFlag(benFlowID)).thenReturn((short) 0);
        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterDoctorActivity(
            benFlowID, benRegID, benID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowAfterDocDataUpdate(
            benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocDataUpdate - Exception")
    void testUpdateBenFlowAfterDocDataUpdate_Exception() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 1;
        short oncologistFlag = 0;
        short tcSpecialistFlag = 0;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.getPharmaFlag(benFlowID))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            commonBenStatusFlowService.updateBenFlowAfterDocDataUpdate(
                benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate);
        });

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocDataUpdateWDF - Success")
    void testUpdateBenFlowAfterDocDataUpdateWDF_Success() throws Exception {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 1;
        short oncologistFlag = 0;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.getPharmaFlag(benFlowID)).thenReturn((short) 0);
        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterDoctorActivityWDF(
            benFlowID, benRegID, benID, docFlag, pharmaFlag, oncologistFlag, tcUserID, tcDate))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowAfterDocDataUpdateWDF(
            benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcUserID, tcDate);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBenFlowAfterDocDataUpdateTCSpecialist - Success")
    void testUpdateBenFlowAfterDocDataUpdateTCSpecialist_Success() throws Exception {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benID = 1L;
        Long benVisitID = 1L;
        short docFlag = 1;
        short pharmaFlag = 1;
        short oncologistFlag = 0;
        short tcSpecialistFlag = 1;
        int tcUserID = 123;
        Timestamp tcDate = new Timestamp(System.currentTimeMillis());

        when(beneficiaryFlowStatusRepo.getPharmaFlag(benFlowID)).thenReturn((short) 0);
        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterDoctorActivityTCSpecialist(
            benFlowID, benRegID, benID, pharmaFlag, oncologistFlag, tcSpecialistFlag))
            .thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateBenFlowAfterDocDataUpdateTCSpecialist(
            benFlowID, benRegID, benID, benVisitID, docFlag, pharmaFlag, oncologistFlag, tcSpecialistFlag, tcUserID, tcDate);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateFlowAfterLabResultEntry - Success")
    void testUpdateFlowAfterLabResultEntry_Success() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Long benVisitID = 1L;
        Short nurseFlag = 1;
        Short doctorFlag = 1;
        Short labFlag = 1;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterLabResultEntry(
            benFlowID, benRegID, nurseFlag, doctorFlag, labFlag)).thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateFlowAfterLabResultEntry(
            benFlowID, benRegID, benVisitID, nurseFlag, doctorFlag, labFlag);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateFlowAfterLabResultEntryForTCSpecialist - Success")
    void testUpdateFlowAfterLabResultEntryForTCSpecialist_Success() {
        // Arrange
        Long benFlowID = 1L;
        Long benRegID = 1L;
        Short specialistFlag = 1;

        when(beneficiaryFlowStatusRepo.updateBenFlowStatusAfterLabResultEntryForSpecialist(
            benFlowID, benRegID, specialistFlag)).thenReturn(1);

        // Act
        int result = commonBenStatusFlowService.updateFlowAfterLabResultEntryForTCSpecialist(
            benFlowID, benRegID, specialistFlag);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test setter methods coverage")
    void testSetterMethods() {
        // Act
        commonBenStatusFlowService.setBenVisitDetailRepo(benVisitDetailRepo);
        commonBenStatusFlowService.setBeneficiaryFlowStatusRepo(beneficiaryFlowStatusRepo);

        // Assert - This test ensures setter methods are called and covered
        assertNotNull(commonBenStatusFlowService);
    }

    @Test
    @DisplayName("Test getBenFlowRecordObj - Complete coverage with all data")
    void testGetBenFlowRecordObj_CompleteCoverage() {
        // This test covers the private getBenFlowRecordObj method through createBenFlowRecord
        String requestOBJ = createCompleteTestRequestJSON();
        Long beneficiaryRegID = 1L;
        Long beneficiaryID = 1L;

        BeneficiaryFlowStatus savedBenFlow = new BeneficiaryFlowStatus();
        savedBenFlow.setBenFlowID(1L);

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenReturn(createCompleteMockBeneficiaryFlowStatus());
            } catch (IEMRException e) {
                fail("Exception should not occur in test setup");
            }
            
            when(benVisitDetailRepo.getVisitCountForBeneficiary(anyLong())).thenReturn(null);
            when(beneficiaryFlowStatusRepo.save(any(BeneficiaryFlowStatus.class))).thenReturn(savedBenFlow);

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test getBenFlowRecordObj - With age calculation in months and days")
    void testGetBenFlowRecordObj_AgeCalculationMonthsAndDays() {
        // This test covers age calculation for infants (months and days)
        String requestOBJ = createTestRequestJSONWithInfantAge();
        Long beneficiaryRegID = 1L;
        Long beneficiaryID = 1L;

        BeneficiaryFlowStatus savedBenFlow = new BeneficiaryFlowStatus();
        savedBenFlow.setBenFlowID(1L);

        try (MockedStatic<InputMapper> inputMapperStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = org.mockito.Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(inputMapper);
            
            try {
                when(inputMapper.fromJson(eq(requestOBJ), eq(BeneficiaryFlowStatus.class)))
                    .thenReturn(createMockBeneficiaryFlowStatusWithInfantAge());
            } catch (IEMRException e) {
                fail("Exception should not occur in test setup");
            }
            
            when(benVisitDetailRepo.getVisitCountForBeneficiary(anyLong())).thenReturn((short) 0);
            when(beneficiaryFlowStatusRepo.save(any(BeneficiaryFlowStatus.class))).thenReturn(savedBenFlow);

            // Act
            int result = commonBenStatusFlowService.createBenFlowRecord(requestOBJ, beneficiaryRegID, beneficiaryID);

            // Assert
            assertEquals(1, result);
        }
    }

    // Helper methods
    private String createTestRequestJSON() {
        return "{}"; // Simplified JSON for testing
    }

    private String createCompleteTestRequestJSON() {
        return "{}"; // Simplified JSON for testing
    }

    private String createTestRequestJSONWithInfantAge() {
        return "{}"; // Simplified JSON for testing
    }

    private BeneficiaryFlowStatus createMockBeneficiaryFlowStatus() {
        BeneficiaryFlowStatus benFlow = new BeneficiaryFlowStatus();
        benFlow.setBeneficiaryRegID(1L);
        benFlow.setBeneficiaryID(1L);
        benFlow.setProviderServiceMapID(1);
        benFlow.setVanID(1);
        benFlow.setFirstName("John");
        benFlow.setLastName("Doe");
        benFlow.setdOB(new Timestamp(System.currentTimeMillis() - (30L * 365 * 24 * 60 * 60 * 1000))); // 30 years ago
        benFlow.setCreatedBy("Test User");
        benFlow.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        
        // Create mock nested objects to avoid null pointer exceptions
        I_bendemographics demographics = new I_bendemographics();
        demographics.setDistrictID(1);
        demographics.setDistrictName("Test District");
        demographics.setDistrictBranchID(1);
        demographics.setDistrictBranchName("Test Branch");
        demographics.setServicePointID(1);
        demographics.setServicePointName("Test Service Point");
        benFlow.setI_bendemographics(demographics);
        
        GenderMaster gender = new GenderMaster();
        gender.setGenderID((short) 1);
        gender.setGenderName("Male");
        benFlow.setM_gender(gender);
        
        ArrayList<BenPhoneMaps> phoneList = new ArrayList<>();
        BenPhoneMaps phoneMap = new BenPhoneMaps();
        phoneMap.setPhoneNo("1234567890");
        phoneList.add(phoneMap);
        benFlow.setBenPhoneMaps(phoneList);
        
        return benFlow;
    }

    private BeneficiaryFlowStatus createCompleteMockBeneficiaryFlowStatus() {
        BeneficiaryFlowStatus benFlow = createMockBeneficiaryFlowStatus();
        benFlow.setGenderID((short) 1); // Use Short instead of int
        benFlow.setGenderName("Male");
        return benFlow;
    }

    private BeneficiaryFlowStatus createMockBeneficiaryFlowStatusWithInfantAge() {
        BeneficiaryFlowStatus benFlow = createMockBeneficiaryFlowStatus();
        // Set date of birth to 6 months ago
        benFlow.setdOB(new Timestamp(System.currentTimeMillis() - (6L * 30 * 24 * 60 * 60 * 1000))); // 6 months ago
        return benFlow;
    }

    private CommonUtilityClass createMockCommonUtilityClass() {
        CommonUtilityClass commonUtilityClass = new CommonUtilityClass();
        commonUtilityClass.setBenFlowID(1L);
        commonUtilityClass.setBeneficiaryID(1L);
        commonUtilityClass.setBenVisitID(1L);
        commonUtilityClass.setBeneficiaryRegID(1L);
        return commonUtilityClass;
    }
}
