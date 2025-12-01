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
package com.iemr.mmu.controller.teleconsultation;

import com.google.gson.JsonObject;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

class TeleConsultationControllerTest {
    static MockedStatic<com.iemr.mmu.utils.CookieUtil> cookieUtilMock;
    @BeforeAll
    static void setUpAll() {
        cookieUtilMock = org.mockito.Mockito.mockStatic(com.iemr.mmu.utils.CookieUtil.class);
        cookieUtilMock.when(() -> com.iemr.mmu.utils.CookieUtil.getJwtTokenFromCookie(any(HttpServletRequest.class)))
            .thenReturn("dummy-jwt");
    }

    @AfterAll
    static void tearDownAll() {
        if (cookieUtilMock != null) {
            cookieUtilMock.close();
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(teleConsultationController).build();
    }
    private MockMvc mockMvc;

    @Mock
    private TeleConsultationServiceImpl teleConsultationServiceImpl;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TeleConsultationController teleConsultationController;


    @Test
    void testBenArrivalStatusUpdater_success() throws Exception {
        when(teleConsultationServiceImpl.updateBeneficiaryArrivalStatus(anyString())).thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/update/benArrivalStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Beneficiary arrival status updated successfully.")));
    }

    @Test
    void testBenArrivalStatusUpdater_error() throws Exception {
        when(teleConsultationServiceImpl.updateBeneficiaryArrivalStatus(anyString())).thenReturn(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/update/benArrivalStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in updating beneficiary arrival status.")));
    }

    @Test
    void testBenArrivalStatusUpdater_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/update/benArrivalStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("null"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in updating beneficiary arrival status.")));
    }

    @Test
    void testBenArrivalStatusUpdater_exception() throws Exception {
        when(teleConsultationServiceImpl.updateBeneficiaryArrivalStatus(anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/update/benArrivalStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while updating beneficiary arrival status.")));
    }

    @Test
    void testUpdateBeneficiaryStatusToCancelTCRequest_success() throws Exception {
        when(teleConsultationServiceImpl.updateBeneficiaryStatusToCancelTCRequest(anyString(), anyString(), anyString())).thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/cancel/benTCRequest")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Beneficiary TC request cancelled successfully.")));
    }

    @Test
    void testUpdateBeneficiaryStatusToCancelTCRequest_error() throws Exception {
        when(teleConsultationServiceImpl.updateBeneficiaryStatusToCancelTCRequest(anyString(), anyString(), anyString())).thenReturn(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/cancel/benTCRequest")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Teleconsultation cancel request failed.")));
    }

    @Test
    void testUpdateBeneficiaryStatusToCancelTCRequest_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/cancel/benTCRequest")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("null"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Teleconsultation cancel request failed.")));
    }

    @Test
    void testUpdateBeneficiaryStatusToCancelTCRequest_exception() throws Exception {
        when(teleConsultationServiceImpl.updateBeneficiaryStatusToCancelTCRequest(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/cancel/benTCRequest")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while updating beneficiary status for Teleconsultation cancel request")));
    }

    @Test
    void testCheckBeneficiaryStatusToProceedWithSpecialist_success() throws Exception {
        when(teleConsultationServiceImpl.checkBeneficiaryStatusForSpecialistTransaction(anyString())).thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/check/benTCRequestStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Specialist can proceed with beneficiary TM session.")));
    }

    @Test
    void testCheckBeneficiaryStatusToProceedWithSpecialist_error() throws Exception {
        when(teleConsultationServiceImpl.checkBeneficiaryStatusForSpecialistTransaction(anyString())).thenReturn(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/check/benTCRequestStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Issue while fetching beneficiary status.")));
    }

    @Test
    void testCheckBeneficiaryStatusToProceedWithSpecialist_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/check/benTCRequestStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("null"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Issue while fetching beneficiary status.")));
    }

    @Test
    void testCheckBeneficiaryStatusToProceedWithSpecialist_exception() throws Exception {
        when(teleConsultationServiceImpl.checkBeneficiaryStatusForSpecialistTransaction(anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/check/benTCRequestStatus")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Issue while fetching beneficiary status")));
    }

    @Test
    void testCreateTCRequestForBeneficiary_success() throws Exception {
        when(teleConsultationServiceImpl.createTCRequestFromWorkList(any(JsonObject.class), anyString())).thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/create/benTCRequestWithVisitCode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"psmID\":1,\"userID\":2,\"date\":\"2025-08-12\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Teleconsultation request created successfully.")));
    }

    @Test
    void testCreateTCRequestForBeneficiary_error() throws Exception {
        when(teleConsultationServiceImpl.createTCRequestFromWorkList(any(JsonObject.class), anyString())).thenReturn(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/create/benTCRequestWithVisitCode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"psmID\":1,\"userID\":2,\"date\":\"2025-08-12\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Issue while creating Teleconsultation request.")));
    }

    @Test
    void testCreateTCRequestForBeneficiary_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/create/benTCRequestWithVisitCode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("null"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Issue while creating Teleconsultation request")));
    }

    @Test
    void testCreateTCRequestForBeneficiary_exception() throws Exception {
        when(teleConsultationServiceImpl.createTCRequestFromWorkList(any(JsonObject.class), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/create/benTCRequestWithVisitCode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"psmID\":1,\"userID\":2,\"date\":\"2025-08-12\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Issue while creating Teleconsultation request")));
    }

    @Test
    void testGetTCSpecialistWorkListNew_success() throws Exception {
        when(teleConsultationServiceImpl.getTCRequestListBySpecialistIdAndDate(anyInt(), anyInt(), anyString())).thenReturn("worklist");
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/getTCRequestList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"psmID\":1,\"userID\":2,\"date\":\"2025-08-12\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("worklist")));
    }

    @Test
    void testGetTCSpecialistWorkListNew_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/getTCRequestList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("null"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting TC requestList")));
    }

    @Test
    void testGetTCSpecialistWorkListNew_exception() throws Exception {
        when(teleConsultationServiceImpl.getTCRequestListBySpecialistIdAndDate(anyInt(), anyInt(), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/tc/getTCRequestList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"psmID\":1,\"userID\":2,\"date\":\"2025-08-12\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting TC requestList")));
    }
}
