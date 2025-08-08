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
package com.iemr.mmu.service.common.master;

import com.iemr.mmu.data.doctor.ChiefComplaintMaster;
import com.iemr.mmu.data.labModule.ProcedureData;
import com.iemr.mmu.data.masterdata.anc.DiseaseType;
import com.iemr.mmu.data.masterdata.anc.PersonalHabitType;
import com.iemr.mmu.data.masterdata.nurse.FamilyMemberType;
import com.iemr.mmu.repo.doctor.ChiefComplaintMasterRepo;
import com.iemr.mmu.repo.doctor.LabTestMasterRepo;
import com.iemr.mmu.repo.labModule.ProcedureRepo;
import com.iemr.mmu.repo.masterrepo.anc.AllergicReactionTypesRepo;
import com.iemr.mmu.repo.masterrepo.anc.DiseaseTypeRepo;
import com.iemr.mmu.repo.masterrepo.anc.PersonalHabitTypeRepo;
import com.iemr.mmu.repo.masterrepo.ncdScreening.BPAndDiabeticStatusRepo;
import com.iemr.mmu.repo.masterrepo.ncdScreening.IDRS_ScreenQuestionsRepo;
import com.iemr.mmu.repo.masterrepo.ncdScreening.NCDScreeningConditionRepo;
import com.iemr.mmu.repo.masterrepo.ncdScreening.NCDScreeningReasonRepo;
import com.iemr.mmu.repo.masterrepo.ncdScreening.PhysicalActivityRepo;
import com.iemr.mmu.repo.masterrepo.nurse.FamilyMemberMasterRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NCDScreeningMasterServiceImplTest {
    private NCDScreeningMasterServiceImpl service;
    private NCDScreeningConditionRepo ncdScreeningConditionRepo;
    private NCDScreeningReasonRepo ncdScreeningReasonRepo;
    private BPAndDiabeticStatusRepo bpAndDiabeticStatusRepo;
    private LabTestMasterRepo labTestMasterRepo;
    private ChiefComplaintMasterRepo chiefComplaintMasterRepo;
    private ProcedureRepo procedureRepo;
    private IDRS_ScreenQuestionsRepo idrsScreenQuestionsRepo;
    private PhysicalActivityRepo physicalActivityRepo;
    private DiseaseTypeRepo diseaseTypeRepo;
    private FamilyMemberMasterRepo familyMemberMasterRepo;
    private PersonalHabitTypeRepo personalHabitTypeRepo;
    private AllergicReactionTypesRepo allergicReactionTypesRepo;

    @BeforeEach
    void setUp() {
        service = new NCDScreeningMasterServiceImpl();
        ncdScreeningConditionRepo = mock(NCDScreeningConditionRepo.class);
        ncdScreeningReasonRepo = mock(NCDScreeningReasonRepo.class);
        bpAndDiabeticStatusRepo = mock(BPAndDiabeticStatusRepo.class);
        labTestMasterRepo = mock(LabTestMasterRepo.class);
        chiefComplaintMasterRepo = mock(ChiefComplaintMasterRepo.class);
        procedureRepo = mock(ProcedureRepo.class);
        idrsScreenQuestionsRepo = mock(IDRS_ScreenQuestionsRepo.class);
        physicalActivityRepo = mock(PhysicalActivityRepo.class);
        diseaseTypeRepo = mock(DiseaseTypeRepo.class);
        familyMemberMasterRepo = mock(FamilyMemberMasterRepo.class);
        personalHabitTypeRepo = mock(PersonalHabitTypeRepo.class);
        allergicReactionTypesRepo = mock(AllergicReactionTypesRepo.class);
        setField(service, "ncdScreeningConditionRepo", ncdScreeningConditionRepo);
        setField(service, "ncdScreeningReasonRepo", ncdScreeningReasonRepo);
        setField(service, "bpAndDiabeticStatusRepo", bpAndDiabeticStatusRepo);
        setField(service, "labTestMasterRepo", labTestMasterRepo);
        setField(service, "chiefComplaintMasterRepo", chiefComplaintMasterRepo);
        setField(service, "procedureRepo", procedureRepo);
        setField(service, "iDRS_ScreenQuestionsRepo", idrsScreenQuestionsRepo);
        setField(service, "physicalActivityRepo", physicalActivityRepo);
        setField(service, "diseaseTypeRepo", diseaseTypeRepo);
        setField(service, "familyMemberMasterRepo", familyMemberMasterRepo);
        setField(service, "personalHabitTypeRepo", personalHabitTypeRepo);
        setField(service, "allergicReactionTypesRepo", allergicReactionTypesRepo);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetNCDScreeningConditions_successAndException() {
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{1, "cond"});
        when(ncdScreeningConditionRepo.getNCDScreeningConditions()).thenReturn(expected);
        assertEquals(expected, service.getNCDScreeningConditions());
        // Exception branch
        when(ncdScreeningConditionRepo.getNCDScreeningConditions()).thenThrow(new RuntimeException("fail"));
        assertNull(service.getNCDScreeningConditions());
    }

    @Test
    void testGetNCDScreeningReasons_successAndException() {
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{1, "reason"});
        when(ncdScreeningReasonRepo.getNCDScreeningReasons()).thenReturn(expected);
        assertEquals(expected, service.getNCDScreeningReasons());
        when(ncdScreeningReasonRepo.getNCDScreeningReasons()).thenThrow(new RuntimeException("fail"));
        assertNull(service.getNCDScreeningReasons());
    }

    @Test
    void testGetBPAndDiabeticStatus_successAndException() {
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{1, "bp"});
        when(bpAndDiabeticStatusRepo.getBPAndDiabeticStatus(true)).thenReturn(expected);
        assertEquals(expected, service.getBPAndDiabeticStatus(true));
        when(bpAndDiabeticStatusRepo.getBPAndDiabeticStatus(true)).thenThrow(new RuntimeException("fail"));
        assertNull(service.getBPAndDiabeticStatus(true));
    }

    @Test
    void testGetNCDTest_successAndException() {
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{1, "test"});
        when(labTestMasterRepo.getTestsBYVisitCategory("NCD Screening")).thenReturn(expected);
        assertEquals(expected, service.getNCDTest());
        when(labTestMasterRepo.getTestsBYVisitCategory("NCD Screening")).thenThrow(new RuntimeException("fail"));
        assertNull(service.getNCDTest());
    }

    @Test
    void testGetChiefComplaintMaster_successAndException() {
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{1, "cc"});
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(expected);
        assertEquals(expected, service.getChiefComplaintMaster());
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenThrow(new RuntimeException("fail"));
        assertNull(service.getChiefComplaintMaster());
    }

    /**
     * Only this test should use static mocking for ChiefComplaintMaster, DiseaseType, FamilyMemberType, ProcedureData, PersonalHabitType.
     * Do not use static mocking for these classes in other tests in this class.
     */
    @Test
    void testGetNCDScreeningMasterData() {
        // Setup all repo mocks to return non-null, simple data
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(new ArrayList<>());
        when(diseaseTypeRepo.getDiseaseTypes()).thenReturn(new ArrayList<>());
        when(familyMemberMasterRepo.getFamilyMemberTypeMaster()).thenReturn(new ArrayList<>());
        when(idrsScreenQuestionsRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(physicalActivityRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(procedureRepo.getProcedureMasterData(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(personalHabitTypeRepo.getPersonalHabitTypeMaster(anyString())).thenReturn(new ArrayList<>());
        when(allergicReactionTypesRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        org.mockito.MockedStatic<ChiefComplaintMaster> chiefComplaintMasterMocked = null;
        org.mockito.MockedStatic<DiseaseType> diseaseTypeMocked = null;
        org.mockito.MockedStatic<FamilyMemberType> familyMemberTypeMocked = null;
        org.mockito.MockedStatic<ProcedureData> procedureDataMocked = null;
        org.mockito.MockedStatic<PersonalHabitType> personalHabitTypeMocked = null;
        try {
            chiefComplaintMasterMocked = mockStatic(ChiefComplaintMaster.class);
            diseaseTypeMocked = mockStatic(DiseaseType.class);
            familyMemberTypeMocked = mockStatic(FamilyMemberType.class);
            procedureDataMocked = mockStatic(ProcedureData.class);
            personalHabitTypeMocked = mockStatic(PersonalHabitType.class);
            chiefComplaintMasterMocked.when(() -> ChiefComplaintMaster.getChiefComplaintMasters(any())).thenReturn(new ArrayList<>());
            diseaseTypeMocked.when(() -> DiseaseType.getDiseaseTypes(any())).thenReturn(new ArrayList<>());
            familyMemberTypeMocked.when(() -> FamilyMemberType.getFamilyMemberTypeMasterData(any())).thenReturn(new ArrayList<>());
            procedureDataMocked.when(() -> ProcedureData.getProcedures(any())).thenReturn(new ArrayList<>());
            personalHabitTypeMocked.when(() -> PersonalHabitType.getPersonalHabitTypeMasterData(any())).thenReturn(new ArrayList<>());
            String json = service.getNCDScreeningMasterData(1, 2, "F");
            assertNotNull(json);
            assertTrue(json.contains("chiefComplaintMaster"));
            assertTrue(json.contains("DiseaseTypes"));
            assertTrue(json.contains("familyMemberTypes"));
            assertTrue(json.contains("IDRSQuestions"));
            assertTrue(json.contains("physicalActivity"));
            assertTrue(json.contains("procedures"));
            assertTrue(json.contains("tobaccoUseStatus"));
            assertTrue(json.contains("typeOfTobaccoProducts"));
            assertTrue(json.contains("alcoholUseStatus"));
            assertTrue(json.contains("typeOfAlcoholProducts"));
            assertTrue(json.contains("frequencyOfAlcoholIntake"));
            assertTrue(json.contains("quantityOfAlcoholIntake"));
            assertTrue(json.contains("AllergicReactionTypes"));
        } finally {
            if (chiefComplaintMasterMocked != null) chiefComplaintMasterMocked.close();
            if (diseaseTypeMocked != null) diseaseTypeMocked.close();
            if (familyMemberTypeMocked != null) familyMemberTypeMocked.close();
            if (procedureDataMocked != null) procedureDataMocked.close();
            if (personalHabitTypeMocked != null) personalHabitTypeMocked.close();
        }
    }
}
