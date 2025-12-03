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

import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.masterdata.registrar.*;
import com.iemr.mmu.data.registrar.BeneficiaryData;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.masterrepo.*;
import com.iemr.mmu.repo.nurse.anc.ANCCareRepo;
import com.iemr.mmu.repo.registrar.BeneficiaryImageRepo;
import com.iemr.mmu.repo.registrar.ReistrarRepoBenSearch;
import com.iemr.mmu.utils.RestTemplateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrarServiceMasterDataImplTest {
    @Test
    void testSettersCoverage() {
        // These are simple setter coverage tests
        ANCCareRepo ancCareRepo2 = mock(ANCCareRepo.class);
        service.setaNCCareRepo(ancCareRepo2);
        assertSame(ancCareRepo2, getField(service, "aNCCareRepo"));

        BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo2 = mock(BeneficiaryFlowStatusRepo.class);
        service.setBeneficiaryFlowStatusRepo(beneficiaryFlowStatusRepo2);
        assertSame(beneficiaryFlowStatusRepo2, getField(service, "beneficiaryFlowStatusRepo"));

        BeneficiaryImageRepo beneficiaryImageRepo2 = mock(BeneficiaryImageRepo.class);
        service.setBeneficiaryImageRepo(beneficiaryImageRepo2);
        assertSame(beneficiaryImageRepo2, getField(service, "beneficiaryImageRepo"));

        CommunityMasterRepo communityMasterRepo2 = mock(CommunityMasterRepo.class);
        service.setCommunityMasterRepo(communityMasterRepo2);
        assertSame(communityMasterRepo2, getField(service, "communityMasterRepo"));

        GenderMasterRepo genderMasterRepo2 = mock(GenderMasterRepo.class);
        service.setGenderMasterRepo(genderMasterRepo2);
        assertSame(genderMasterRepo2, getField(service, "genderMasterRepo"));

        GovIdEntityTypeRepo govIdEntityTypeRepo2 = mock(GovIdEntityTypeRepo.class);
        service.setGovIdEntityTypeRepo(govIdEntityTypeRepo2);
        assertSame(govIdEntityTypeRepo2, getField(service, "govIdEntityTypeRepo"));

        IncomeStatusMasterRepo incomeStatusMasterRepo2 = mock(IncomeStatusMasterRepo.class);
        service.setIncomeStatusMasterRepo(incomeStatusMasterRepo2);
        assertSame(incomeStatusMasterRepo2, getField(service, "incomeStatusMasterRepo"));

        MaritalStatusMasterRepo maritalStatusMasterRepo2 = mock(MaritalStatusMasterRepo.class);
        service.setMaritalStatusMasterRepo(maritalStatusMasterRepo2);
        assertSame(maritalStatusMasterRepo2, getField(service, "maritalStatusMasterRepo"));

        OccupationMasterRepo occupationMasterRepo2 = mock(OccupationMasterRepo.class);
        service.setOccupationMasterRepo(occupationMasterRepo2);
        assertSame(occupationMasterRepo2, getField(service, "occupationMasterRepo"));

        QualificationMasterRepo qualificationMasterRepo2 = mock(QualificationMasterRepo.class);
        service.setQualificationMasterRepo(qualificationMasterRepo2);
        assertSame(qualificationMasterRepo2, getField(service, "qualificationMasterRepo"));

        ReligionMasterRepo religionMasterRepo2 = mock(ReligionMasterRepo.class);
        service.setReligionMasterRepo(religionMasterRepo2);
        assertSame(religionMasterRepo2, getField(service, "religionMasterRepo"));

        ReistrarRepoBenSearch reistrarRepoBenSearch2 = mock(ReistrarRepoBenSearch.class);
        service.setReistrarRepoAdvanceBenSearch(reistrarRepoBenSearch2);
        assertSame(reistrarRepoBenSearch2, getField(service, "reistrarRepoBenSearch"));
    }

    // Helper to get private field value
    private Object getField(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private RegistrarServiceMasterDataImpl service;
    private CommunityMasterRepo communityMasterRepo;
    private GenderMasterRepo genderMasterRepo;
    private GovIdEntityTypeRepo govIdEntityTypeRepo;
    private IncomeStatusMasterRepo incomeStatusMasterRepo;
    private MaritalStatusMasterRepo maritalStatusMasterRepo;
    private OccupationMasterRepo occupationMasterRepo;
    private QualificationMasterRepo qualificationMasterRepo;
    private ReligionMasterRepo religionMasterRepo;
    private BeneficiaryImageRepo beneficiaryImageRepo;
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
    private ANCCareRepo ancCareRepo;
    private ReistrarRepoBenSearch reistrarRepoBenSearch;

    @BeforeEach
    void setUp() {
        service = new RegistrarServiceMasterDataImpl();
        communityMasterRepo = mock(CommunityMasterRepo.class);
        genderMasterRepo = mock(GenderMasterRepo.class);
        govIdEntityTypeRepo = mock(GovIdEntityTypeRepo.class);
        incomeStatusMasterRepo = mock(IncomeStatusMasterRepo.class);
        maritalStatusMasterRepo = mock(MaritalStatusMasterRepo.class);
        occupationMasterRepo = mock(OccupationMasterRepo.class);
        qualificationMasterRepo = mock(QualificationMasterRepo.class);
        religionMasterRepo = mock(ReligionMasterRepo.class);
        beneficiaryImageRepo = mock(BeneficiaryImageRepo.class);
        beneficiaryFlowStatusRepo = mock(BeneficiaryFlowStatusRepo.class);
        ancCareRepo = mock(ANCCareRepo.class);
        reistrarRepoBenSearch = mock(ReistrarRepoBenSearch.class);
        setField(service, "communityMasterRepo", communityMasterRepo);
        setField(service, "genderMasterRepo", genderMasterRepo);
        setField(service, "govIdEntityTypeRepo", govIdEntityTypeRepo);
        setField(service, "incomeStatusMasterRepo", incomeStatusMasterRepo);
        setField(service, "maritalStatusMasterRepo", maritalStatusMasterRepo);
        setField(service, "occupationMasterRepo", occupationMasterRepo);
        setField(service, "qualificationMasterRepo", qualificationMasterRepo);
        setField(service, "religionMasterRepo", religionMasterRepo);
        setField(service, "beneficiaryImageRepo", beneficiaryImageRepo);
        setField(service, "beneficiaryFlowStatusRepo", beneficiaryFlowStatusRepo);
        setField(service, "aNCCareRepo", ancCareRepo);
        setField(service, "reistrarRepoBenSearch", reistrarRepoBenSearch);
        setField(service, "getBenImageFromIdentity", "http://mocked-url");
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
    void testGetRegMasterData_success() {
        when(communityMasterRepo.getCommunityMaster()).thenReturn(new ArrayList<>());
        when(genderMasterRepo.getGenderMaster()).thenReturn(new ArrayList<>());
        when(govIdEntityTypeRepo.getGovIdEntityMaster()).thenReturn(new ArrayList<>());
        when(govIdEntityTypeRepo.getOtherGovIdEntityMaster()).thenReturn(new ArrayList<>());
        when(incomeStatusMasterRepo.getIncomeStatusMaster()).thenReturn(new ArrayList<>());
        when(maritalStatusMasterRepo.getMaritalStatusMaster()).thenReturn(new ArrayList<>());
        when(occupationMasterRepo.getOccupationMaster()).thenReturn(new ArrayList<>());
        when(qualificationMasterRepo.getQualificationMaster()).thenReturn(new ArrayList<>());
        when(religionMasterRepo.getReligionMaster()).thenReturn(new ArrayList<>());
        try (
            org.mockito.MockedStatic<CommunityMaster> communityMasterMocked = mockStatic(CommunityMaster.class);
            org.mockito.MockedStatic<GenderMaster> genderMasterMocked = mockStatic(GenderMaster.class);
            org.mockito.MockedStatic<GovIdEntityType> govIdEntityTypeMocked = mockStatic(GovIdEntityType.class);
            org.mockito.MockedStatic<IncomeStatusMaster> incomeStatusMasterMocked = mockStatic(IncomeStatusMaster.class);
            org.mockito.MockedStatic<MaritalStatusMaster> maritalStatusMasterMocked = mockStatic(MaritalStatusMaster.class);
            org.mockito.MockedStatic<OccupationMaster> occupationMasterMocked = mockStatic(OccupationMaster.class);
            org.mockito.MockedStatic<QualificationMaster> qualificationMasterMocked = mockStatic(QualificationMaster.class);
            org.mockito.MockedStatic<ReligionMaster> religionMasterMocked = mockStatic(ReligionMaster.class)
        ) {
            communityMasterMocked.when(() -> CommunityMaster.getCommunityMasterData(any())).thenReturn(new ArrayList<>());
            genderMasterMocked.when(() -> GenderMaster.getGenderMasterData(any())).thenReturn(new ArrayList<>());
            govIdEntityTypeMocked.when(() -> GovIdEntityType.getGovIdEntityTypeData(any())).thenReturn(new ArrayList<>());
            incomeStatusMasterMocked.when(() -> IncomeStatusMaster.getIncomeStatusMasterData(any())).thenReturn(new ArrayList<>());
            maritalStatusMasterMocked.when(() -> MaritalStatusMaster.getMaritalStatusMasterData(any())).thenReturn(new ArrayList<>());
            occupationMasterMocked.when(() -> OccupationMaster.getOccupationMasterData(any())).thenReturn(new ArrayList<>());
            qualificationMasterMocked.when(() -> QualificationMaster.getQualificationMasterData(any())).thenReturn(new ArrayList<>());
            religionMasterMocked.when(() -> ReligionMaster.getReligionMasterData(any())).thenReturn(new ArrayList<>());
            String json = service.getRegMasterData();
            assertNotNull(json);
            assertTrue(json.contains("communityMaster"));
            assertTrue(json.contains("genderMaster"));
            assertTrue(json.contains("govIdEntityMaster"));
            assertTrue(json.contains("otherGovIdEntityMaster"));
            assertTrue(json.contains("incomeMaster"));
            assertTrue(json.contains("maritalStatusMaster"));
            assertTrue(json.contains("occupationMaster"));
            assertTrue(json.contains("qualificationMaster"));
            assertTrue(json.contains("religionMaster"));
        }
    }

    @Test
    void testGetBenDetailsByRegID_genderBranches() {
        ArrayList<Object[]> benDetailsList = new ArrayList<>();
        BeneficiaryData benData = new BeneficiaryData();
        when(reistrarRepoBenSearch.getBenDetails(1L)).thenReturn(benDetailsList);
        List<BeneficiaryData> benDataList = new ArrayList<>();
        benDataList.add(benData);
        try (org.mockito.MockedStatic<BeneficiaryData> beneficiaryDataMocked = mockStatic(BeneficiaryData.class)) {
            beneficiaryDataMocked.when(() -> BeneficiaryData.getBeneficiaryData(benDetailsList)).thenReturn(benDataList);
            when(beneficiaryImageRepo.getBenImage(1L)).thenReturn("img");
            benData.setGenderID((short)1);
            String json = service.getBenDetailsByRegID(1L);
            assertTrue(json.contains("Male"));
            benData.setGenderID((short)2);
            json = service.getBenDetailsByRegID(1L);
            assertTrue(json.contains("Female"));
            benData.setGenderID((short)3);
            json = service.getBenDetailsByRegID(1L);
            assertTrue(json.contains("Transgender"));
            benData.setGenderID(null);
            json = service.getBenDetailsByRegID(1L);
            assertNotNull(json);
        }
    }

    @Test
    void testGetBenDetailsForLeftSideByRegIDNew_success() {
        ArrayList<Object[]> benFlowOBJ = new ArrayList<>();
        when(beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(1L, 2L)).thenReturn(benFlowOBJ);
        BeneficiaryFlowStatus benFlowStatus = new BeneficiaryFlowStatus();
        try (org.mockito.MockedStatic<BeneficiaryFlowStatus> benFlowStatusMocked = mockStatic(BeneficiaryFlowStatus.class)) {
            benFlowStatusMocked.when(() -> BeneficiaryFlowStatus.getBeneficiaryFlowStatusForLeftPanel(benFlowOBJ)).thenReturn(benFlowStatus);
            when(ancCareRepo.getBenANCCareDetailsStatus(1L)).thenReturn("B+");
            // set blood group directly
            benFlowStatus.setBloodGroup("B+");
            String json = service.getBenDetailsForLeftSideByRegIDNew(1L, 2L, "auth", "req");
            assertTrue(json.contains("B+"));
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testGetBenImageFromIdentityAPI_success() throws Exception {
        HttpEntity request = mock(HttpEntity.class);
        ResponseEntity response = mock(ResponseEntity.class);
        when(response.getBody()).thenReturn("imgdata");
        try (
            org.mockito.MockedStatic<RestTemplateUtil> restTemplateUtilMocked = mockStatic(RestTemplateUtil.class);
            org.mockito.MockedConstruction<RestTemplate> restTemplateMocked = mockConstruction(RestTemplate.class, (mock, context) -> {
                when(mock.exchange(anyString(), eq(HttpMethod.POST), eq(request), eq(String.class))).thenReturn(response);
            })
        ) {
            restTemplateUtilMocked.when(() -> RestTemplateUtil.createRequestEntity(anyString(), anyString(), anyString())).thenReturn(request);
            setField(service, "getBenImageFromIdentity", "http://mocked-url");
            String result = null;
            try {
                result = service.getBenImageFromIdentityAPI("auth", "req", "token");
            } catch (Exception e) {
                // ignore
            }
            assertNotNull(result);
        }
    }
}
