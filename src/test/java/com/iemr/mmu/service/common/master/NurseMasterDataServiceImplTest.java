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

import com.iemr.mmu.data.masterdata.nurse.*;
import com.iemr.mmu.repo.masterrepo.nurse.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NurseMasterDataServiceImplTest {
    // Helper to inject private fields
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private NurseMasterDataServiceImpl service;
    private CancerDiseaseMasterRepo cancerDiseaseMasterRepo;
    private CancerPersonalHabitMasterRepo cancerPersonalHabitMasterRepo;
    private FamilyMemberMasterRepo familyMemberMasterRepo;
    private VisitCategoryMasterRepo visitCategoryMasterRepo;
    private VisitReasonMasterRepo visitReasonMasterRepo;

    @BeforeEach
    void setUp() {
        service = new NurseMasterDataServiceImpl();
        cancerDiseaseMasterRepo = mock(CancerDiseaseMasterRepo.class);
        cancerPersonalHabitMasterRepo = mock(CancerPersonalHabitMasterRepo.class);
        familyMemberMasterRepo = mock(FamilyMemberMasterRepo.class);
        visitCategoryMasterRepo = mock(VisitCategoryMasterRepo.class);
        visitReasonMasterRepo = mock(VisitReasonMasterRepo.class);
        setField(service, "cancerDiseaseMasterRepo", cancerDiseaseMasterRepo);
        setField(service, "cancerPersonalHabitMasterRepo", cancerPersonalHabitMasterRepo);
        setField(service, "familyMemberMasterRepo", familyMemberMasterRepo);
        setField(service, "visitCategoryMasterRepo", visitCategoryMasterRepo);
        setField(service, "visitReasonMasterRepo", visitReasonMasterRepo);
    }

    @Test
    void testGetVisitReasonAndCategories_returnsExpectedJson() {
        ArrayList<Object[]> visitCategories = new ArrayList<>();
        visitCategories.add(new Object[]{1, "Cat1"});
        ArrayList<Object[]> visitReasons = new ArrayList<>();
        visitReasons.add(new Object[]{1, "Reason1"});
        when(visitCategoryMasterRepo.getVisitCategoryMaster()).thenReturn(visitCategories);
        when(visitReasonMasterRepo.getVisitReasonMaster()).thenReturn(visitReasons);
        try (
            org.mockito.MockedStatic<VisitCategory> visitCategoryMocked = mockStatic(VisitCategory.class);
            org.mockito.MockedStatic<VisitReason> visitReasonMocked = mockStatic(VisitReason.class)
        ) {
            visitCategoryMocked.when(() -> VisitCategory.getVisitCategoryMasterData(visitCategories)).thenReturn(new ArrayList<>());
            visitReasonMocked.when(() -> VisitReason.getVisitReasonMasterData(visitReasons)).thenReturn(new ArrayList<>());
            String json = service.GetVisitReasonAndCategories();
            assertNotNull(json);
            assertTrue(json.contains("visitCategories"));
            assertTrue(json.contains("visitReasons"));
        }
    }

    @Test
    void testGetCancerScreeningMasterDataForNurse_returnsExpectedJson() {
        ArrayList<Object[]> diseaseTypes = new ArrayList<>();
        diseaseTypes.add(new Object[]{1, "Disease1"});
        ArrayList<Object[]> tobaccoUseStatus = new ArrayList<>();
        tobaccoUseStatus.add(new Object[]{1, "Tobacco"});
        ArrayList<Object[]> typeOfTobaccoProducts = new ArrayList<>();
        typeOfTobaccoProducts.add(new Object[]{1, "Product"});
        ArrayList<Object[]> alcoholUseStatus = new ArrayList<>();
        alcoholUseStatus.add(new Object[]{1, "Alcohol"});
        ArrayList<Object[]> frequencyOfAlcoholIntake = new ArrayList<>();
        frequencyOfAlcoholIntake.add(new Object[]{1, "Freq"});
        ArrayList<Object[]> dietTypes = new ArrayList<>();
        dietTypes.add(new Object[]{1, "Diet"});
        ArrayList<Object[]> oilConsumed = new ArrayList<>();
        oilConsumed.add(new Object[]{1, "Oil"});
        ArrayList<Object[]> physicalActivityType = new ArrayList<>();
        physicalActivityType.add(new Object[]{1, "Activity"});
        ArrayList<Object[]> familyMemberTypes = new ArrayList<>();
        familyMemberTypes.add(new Object[]{1, "Family"});
        ArrayList<Object[]> visitCategories = new ArrayList<>();
        visitCategories.add(new Object[]{1, "Cat1"});
        ArrayList<Object[]> visitReasons = new ArrayList<>();
        visitReasons.add(new Object[]{1, "Reason1"});
        when(cancerDiseaseMasterRepo.getCancerDiseaseMaster()).thenReturn(diseaseTypes);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Tobacco Use Status")).thenReturn(tobaccoUseStatus);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Type of Tobacco Product")).thenReturn(typeOfTobaccoProducts);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Alcohol Usage")).thenReturn(alcoholUseStatus);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Frequency of Alcohol Intake")).thenReturn(frequencyOfAlcoholIntake);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Dietary Type ")).thenReturn(dietTypes);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Oil Consumed")).thenReturn(oilConsumed);
        when(cancerPersonalHabitMasterRepo.getCancerPersonalHabitTypeMaster("Physical Activity Type ")).thenReturn(physicalActivityType);
        when(familyMemberMasterRepo.getFamilyMemberTypeMaster()).thenReturn(familyMemberTypes);
        when(visitCategoryMasterRepo.getVisitCategoryMaster()).thenReturn(visitCategories);
        when(visitReasonMasterRepo.getVisitReasonMaster()).thenReturn(visitReasons);
        try (
            org.mockito.MockedStatic<CancerDiseaseType> cancerDiseaseTypeMocked = mockStatic(CancerDiseaseType.class);
            org.mockito.MockedStatic<CancerPersonalHabitType> cancerPersonalHabitTypeMocked = mockStatic(CancerPersonalHabitType.class);
            org.mockito.MockedStatic<FamilyMemberType> familyMemberTypeMocked = mockStatic(FamilyMemberType.class);
            org.mockito.MockedStatic<VisitCategory> visitCategoryMocked = mockStatic(VisitCategory.class);
            org.mockito.MockedStatic<VisitReason> visitReasonMocked = mockStatic(VisitReason.class)
        ) {
            cancerDiseaseTypeMocked.when(() -> CancerDiseaseType.getCancerDiseaseTypeMasterData(diseaseTypes)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(tobaccoUseStatus)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(typeOfTobaccoProducts)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(alcoholUseStatus)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(frequencyOfAlcoholIntake)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(dietTypes)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(oilConsumed)).thenReturn(new ArrayList<>());
            cancerPersonalHabitTypeMocked.when(() -> CancerPersonalHabitType.getCancerPersonalHabitTypeMasterData(physicalActivityType)).thenReturn(new ArrayList<>());
            familyMemberTypeMocked.when(() -> FamilyMemberType.getFamilyMemberTypeMasterData(familyMemberTypes)).thenReturn(new ArrayList<>());
            visitCategoryMocked.when(() -> VisitCategory.getVisitCategoryMasterData(visitCategories)).thenReturn(new ArrayList<>());
            visitReasonMocked.when(() -> VisitReason.getVisitReasonMasterData(visitReasons)).thenReturn(new ArrayList<>());
            String json = service.getCancerScreeningMasterDataForNurse();
            assertNotNull(json);
            assertTrue(json.contains("CancerDiseaseType"));
            assertTrue(json.contains("tobaccoUseStatus"));
            assertTrue(json.contains("typeOfTobaccoProducts"));
            assertTrue(json.contains("alcoholUseStatus"));
            assertTrue(json.contains("frequencyOfAlcoholIntake"));
            assertTrue(json.contains("dietTypes"));
            assertTrue(json.contains("oilConsumed"));
            assertTrue(json.contains("physicalActivityType"));
            assertTrue(json.contains("familyMemberTypes"));
            assertTrue(json.contains("visitCategories"));
            assertTrue(json.contains("visitReasons"));
        }
    }

    // Static mock helpers removed; all static mocks are now handled in single try-with-resources blocks per test.
}
