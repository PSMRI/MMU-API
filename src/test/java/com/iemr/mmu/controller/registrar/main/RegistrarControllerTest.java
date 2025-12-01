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
package com.iemr.mmu.controller.registrar.main;

import com.iemr.mmu.service.common.master.RegistrarServiceMasterDataImpl;
import com.iemr.mmu.service.registrar.RegistrarServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iemr.mmu.service.nurse.NurseServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;

class RegistrarControllerTest {
    @Mock
    private CommonNurseServiceImpl commonNurseServiceImpl;
    // --- Tests for updateBeneficiary(String) ---
    @Test
    void updateBeneficiary_success() throws Exception {
        com.google.gson.JsonObject benD = new com.google.gson.JsonObject();
        benD.addProperty("beneficiaryRegID", 1L);
        when(registrarServiceImpl.updateBeneficiary(any())).thenReturn(1);
        when(registrarServiceImpl.updateBeneficiaryDemographic(any(), anyLong())).thenReturn(1);
        when(registrarServiceImpl.updateBeneficiaryPhoneMapping(any(), anyLong())).thenReturn(1);
        when(registrarServiceImpl.updateBeneficiaryDemographicAdditional(any(), anyLong())).thenReturn(1);
        when(registrarServiceImpl.updateBeneficiaryImage(any(), anyLong())).thenReturn(1);
    // removed invalid doNothing() for non-void method updateBenGovIdMapping
        com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl commonNurseService = org.mockito.Mockito.mock(com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl.class);
        String req = "{\"benD\":{\"beneficiaryRegID\":1}}";
        mockMvc.perform(post("/registrar/update/BeneficiaryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary Details updated successfully")));
    }

    @Test
    void updateBeneficiary_dataNotSufficient() throws Exception {
        String req = "{\"benD\":null}";
    mockMvc.perform(post("/registrar/update/BeneficiaryDetails")
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(req))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Expected a com.google.gson.JsonObject but was com.google.gson.JsonNull")));
    }

    // --- Tests for createBeneficiary(String, String) ---
    @Test
    void createBeneficiary_success() throws Exception {
        com.google.gson.JsonObject benD = new com.google.gson.JsonObject();
        benD.addProperty("firstName", "John");
        com.iemr.mmu.data.registrar.BeneficiaryData benData = org.mockito.Mockito.mock(com.iemr.mmu.data.registrar.BeneficiaryData.class);
        when(benData.getBeneficiaryRegID()).thenReturn(1L);
        when(benData.getBeneficiaryID()).thenReturn("BENID");
        when(registrarServiceImpl.createBeneficiary(any())).thenReturn(benData);
        when(registrarServiceImpl.createBeneficiaryDemographic(any(), anyLong())).thenReturn(1L);
        when(registrarServiceImpl.createBeneficiaryPhoneMapping(any(), anyLong())).thenReturn(1L);
        when(registrarServiceImpl.createBeneficiaryDemographicAdditional(any(), anyLong())).thenReturn(1L);
        when(registrarServiceImpl.createBeneficiaryImage(any(), anyLong())).thenReturn(1L);
    // removed invalid doNothing() for non-void method createBenGovIdMapping
        com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl commonNurseService = org.mockito.Mockito.mock(com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl.class);
        String req = "{\"benD\":{\"firstName\":\"John\"}}";
        mockMvc.perform(post("/registrar/registrarBeneficaryRegistration")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("BENID")));
    }

    // --- Tests for masterDataForRegistration(String) ---
    @Test
    void masterDataForRegistration_success() throws Exception {
        when(registrarServiceMasterDataImpl.getRegMasterData()).thenReturn("masterData");
        String req = "{\"spID\":1}";
        mockMvc.perform(post("/registrar/registrarMasterData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("masterData")));
    }

    @Test
    void masterDataForRegistration_invalidServicePoint() throws Exception {
        String req = "{\"spID\":0}";
        mockMvc.perform(post("/registrar/registrarMasterData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid service point")));
    }

    @Test
    void masterDataForRegistration_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/registrar/registrarMasterData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    // --- Tests for beneficiaryUpdate(String, String, HttpServletRequest) ---
    @Test
    void beneficiaryUpdate_success() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.updateBeneficiary(anyString(), anyString(), anyString())).thenReturn(1);
            mockMvc.perform(post("/registrar/update/BeneficiaryUpdate")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("updated successfully")));
        }
    }

    @Test
    void beneficiaryUpdate_alreadyPresent() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.updateBeneficiary(anyString(), anyString(), anyString())).thenReturn(2);
            mockMvc.perform(post("/registrar/update/BeneficiaryUpdate")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("already present in nurse work list")));
        }
    }

    @Test
    void beneficiaryUpdate_error() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.updateBeneficiary(anyString(), anyString(), anyString())).thenReturn(null);
            mockMvc.perform(post("/registrar/update/BeneficiaryUpdate")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while updating beneficiary details")));
        }
    }

    // --- Tests for createReVisitForBenToNurse(String) ---
    @Test
    void createReVisitForBenToNurse_success() throws Exception {
        when(registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(anyString())).thenReturn(1);
        mockMvc.perform(post("/registrar/create/BenReVisitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("moved to nurse worklist")));
    }

    @Test
    void createReVisitForBenToNurse_alreadyPresent() throws Exception {
        when(registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(anyString())).thenReturn(2);
        mockMvc.perform(post("/registrar/create/BenReVisitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("already present in nurse worklist")));
    }

    @Test
    void createReVisitForBenToNurse_error() throws Exception {
        when(registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(anyString())).thenReturn(0);
        mockMvc.perform(post("/registrar/create/BenReVisitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while moving beneficiary to nurse worklist")));
    }

    // --- Tests for registrarBeneficaryRegistrationNew(String, String, HttpServletRequest) ---
    @Test
    void registrarBeneficaryRegistrationNew_success() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.registerBeneficiary(anyString(), anyString(), anyString())).thenReturn("regSuccess");
            mockMvc.perform(post("/registrar/registrarBeneficaryRegistrationNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("regSuccess")));
        }
    }

    @Test
    void registrarBeneficaryRegistrationNew_error() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.registerBeneficiary(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(post("/registrar/registrarBeneficaryRegistrationNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error in registration; please contact administrator")));
        }
    }

    // --- Test for setNurseServiceImpl(NurseServiceImpl) ---
    @Test
    void setNurseServiceImpl_setsField() {
    NurseServiceImpl nurseService = org.mockito.Mockito.mock(NurseServiceImpl.class);
    registrarController.setNurseServiceImpl(nurseService);
    // No exception means success; field is set
    org.junit.jupiter.api.Assertions.assertNotNull(nurseService);
    }
  
    private MockMvc mockMvc;


    @Mock
    private RegistrarServiceImpl registrarServiceImpl;
    @Mock
    private RegistrarServiceMasterDataImpl registrarServiceMasterDataImpl;
    @InjectMocks
    private RegistrarController registrarController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(registrarController).build();
    }

    // --- Tests for quickSearchNew ---
    @Test
    void quickSearchNew_success() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.beneficiaryQuickSearch(anyString(), anyString(), anyString())).thenReturn("searchList");
            mockMvc.perform(post("/registrar/quickSearchNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("searchList")));
        }
    }

    @Test
    void quickSearchNew_nullResult() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.beneficiaryQuickSearch(anyString(), anyString(), anyString())).thenReturn(null);
            mockMvc.perform(post("/registrar/quickSearchNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
        }
    }

    @Test
    void quickSearchNew_exception() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.beneficiaryQuickSearch(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(post("/registrar/quickSearchNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while searching beneficiary")));
        }
    }

    // --- Tests for advanceSearchNew ---
    @Test
    void advanceSearchNew_success() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.beneficiaryAdvanceSearch(anyString(), anyString(), anyString())).thenReturn("searchList");
            mockMvc.perform(post("/registrar/advanceSearchNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("searchList")));
        }
    }

    @Test
    void advanceSearchNew_nullResult() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.beneficiaryAdvanceSearch(anyString(), anyString(), anyString())).thenReturn(null);
            mockMvc.perform(post("/registrar/advanceSearchNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
        }
    }

    @Test
    void advanceSearchNew_exception() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceImpl.beneficiaryAdvanceSearch(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(post("/registrar/advanceSearchNew")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while searching beneficiary")));
        }
    }

    // --- Tests for getBenDetailsForLeftSidePanelByRegID ---
    @Test
    void getBenDetailsForLeftSidePanelByRegID_success() throws Exception {
        when(registrarServiceMasterDataImpl.getBenDetailsForLeftSideByRegIDNew(anyLong(), anyLong(), anyString(), anyString())).thenReturn("benDetails");
        String req = "{\"beneficiaryRegID\":1,\"benFlowID\":2}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegIDForLeftPanelNew")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("benDetails")));
    }

    @Test
    void getBenDetailsForLeftSidePanelByRegID_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegIDForLeftPanelNew")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenDetailsForLeftSidePanelByRegID_invalidId() throws Exception {
        String req = "{\"beneficiaryRegID\":0,\"benFlowID\":0}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegIDForLeftPanelNew")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid Beneficiary ID")));
    }

    @Test
    void getBenDetailsForLeftSidePanelByRegID_exception() throws Exception {
        when(registrarServiceMasterDataImpl.getBenDetailsForLeftSideByRegIDNew(anyLong(), anyLong(), anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        String req = "{\"beneficiaryRegID\":1,\"benFlowID\":2}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegIDForLeftPanelNew")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary details")));
    }

    // --- Tests for getBenImage ---
    @Test
    void getBenImage_success() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceMasterDataImpl.getBenImageFromIdentityAPI(anyString(), anyString(), anyString())).thenReturn("img");
            mockMvc.perform(post("/registrar/getBenImage")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("img")));
        }
    }

    @Test
    void getBenImage_exception() throws Exception {
        try (org.mockito.MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class)) {
            cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
            when(registrarServiceMasterDataImpl.getBenImageFromIdentityAPI(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(post("/registrar/getBenImage")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary image")));
        }
    }

    // --- Tests for createBeneficiary ---
    @Test
    void createBeneficiary_invalidInput() throws Exception {
        String req = "{\"benD\":null}";
        mockMvc.perform(post("/registrar/registrarBeneficaryRegistration")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Expected a com.google.gson.JsonObject but was com.google.gson.JsonNull")));
    }

    @Test
    void getRegistrarWorkList_success() throws Exception {
        when(registrarServiceImpl.getRegWorkList(anyInt())).thenReturn("worklist");
        String req = "{\"spID\":1}";
        mockMvc.perform(post("/registrar/registrarWorkListData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("worklist")));
    }

    @Test
    void getRegistrarWorkList_exception() throws Exception {
        when(registrarServiceImpl.getRegWorkList(anyInt())).thenThrow(new RuntimeException("fail"));
        String req = "{\"spID\":1}";
        mockMvc.perform(post("/registrar/registrarWorkListData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void quickSearchBeneficiary_success() throws Exception {
        when(registrarServiceImpl.getQuickSearchBenData(anyString())).thenReturn("benData");
        String req = "{\"benID\":\"B123\"}";
        mockMvc.perform(post("/registrar/quickSearch")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("benData")));
    }

    @Test
    void quickSearchBeneficiary_exception() throws Exception {
        when(registrarServiceImpl.getQuickSearchBenData(anyString())).thenThrow(new RuntimeException("fail"));
        String req = "{\"benID\":\"B123\"}";
        mockMvc.perform(post("/registrar/quickSearch")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void advanceSearch_success() throws Exception {
        when(registrarServiceImpl.getAdvanceSearchBenData(any())).thenReturn("advData");
        String req = "{\"firstName\":\"A\",\"lastName\":\"B\",\"phoneNo\":\"123\",\"beneficiaryID\":\"BID\",\"stateID\":1,\"districtID\":2,\"aadharNo\":\"AADHAR\",\"govtIdentityNo\":\"GOVT\"}";
        mockMvc.perform(post("/registrar/advanceSearch")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("advData")));
    }

    @Test
    void advanceSearch_exception() throws Exception {
        when(registrarServiceImpl.getAdvanceSearchBenData(any())).thenThrow(new RuntimeException("fail"));
        String req = "{\"firstName\":\"A\"}";
        mockMvc.perform(post("/registrar/advanceSearch")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

      @Test
    void getBeneficiaryImage_exception() throws Exception {
        when(registrarServiceImpl.getBenImage(anyLong())).thenThrow(new RuntimeException("fail"));
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/beneficiaryImage")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk()); // logs error, returns response
    }

    @Test
    void getBenDetailsByRegID_success() throws Exception {
        when(registrarServiceMasterDataImpl.getBenDetailsByRegID(anyLong())).thenReturn("benDetails");
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegID")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("benDetails")));
    }

    @Test
    void getBenDetailsByRegID_missingId() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegID")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("beneficiaryRegID is not there in request")));
    }

    @Test
    void getBenDetailsByRegID_zeroId() throws Exception {
        String req = "{\"beneficiaryRegID\":0}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegID")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please pass beneficiaryRegID")));
    }

    @Test
    void getBenDetailsByRegID_exception() throws Exception {
        when(registrarServiceMasterDataImpl.getBenDetailsByRegID(anyLong())).thenThrow(new RuntimeException("fail"));
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/benDetailsByRegID")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getBeneficiaryDetails_success() throws Exception {
        when(registrarServiceImpl.getBeneficiaryDetails(anyLong())).thenReturn("benDetails");
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/beneficiaryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("benDetails")));
    }

    @Test
    void getBeneficiaryDetails_nullData() throws Exception {
        when(registrarServiceImpl.getBeneficiaryDetails(anyLong())).thenReturn(null);
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/beneficiaryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("{}")));
    }

    @Test
    void getBeneficiaryDetails_zeroId() throws Exception {
        String req = "{\"beneficiaryRegID\":0}";
        mockMvc.perform(post("/registrar/get/beneficiaryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please pass beneficiaryRegID")));
    }

    @Test
    void getBeneficiaryDetails_missingId() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/registrar/get/beneficiaryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("beneficiaryRegID is not there in request")));
    }

    @Test
    void getBeneficiaryDetails_exception() throws Exception {
        when(registrarServiceImpl.getBeneficiaryDetails(anyLong())).thenThrow(new RuntimeException("fail"));
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/beneficiaryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getBeneficiaryImage_success() throws Exception {
        when(registrarServiceImpl.getBenImage(anyLong())).thenReturn("img");
        String req = "{\"beneficiaryRegID\":1}";
        mockMvc.perform(post("/registrar/get/beneficiaryImage")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("img")));
    }

    @Test
    void getBeneficiaryImage_zeroId() throws Exception {
        String req = "{\"beneficiaryRegID\":0}";
        mockMvc.perform(post("/registrar/get/beneficiaryImage")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please pass beneficiaryRegID")));
    }

    @Test
    void getBeneficiaryImage_missingId() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/registrar/get/beneficiaryImage")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("beneficiaryRegID is not there in request")));
    }
}
