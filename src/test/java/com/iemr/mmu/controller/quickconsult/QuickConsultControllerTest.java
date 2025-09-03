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
package com.iemr.mmu.controller.quickconsult;

import com.google.gson.JsonObject;
import com.iemr.mmu.service.quickConsultation.QuickConsultationServiceImpl;
import com.iemr.mmu.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class QuickConsultControllerTest {
    private MockMvc mockMvc;

    @Mock
    private QuickConsultationServiceImpl quickConsultationServiceImpl;
    @Mock
    private Logger logger;

    @InjectMocks
    private QuickConsultController quickConsultController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(quickConsultController).build();
    }

    @Test
    void testSaveBenQuickConsultDataNurse_success() throws Exception {
        String requestJson = "{" +
                "\"someField\":1" +
                "}";
        when(quickConsultationServiceImpl.quickConsultNurseDataInsert(any(JsonObject.class))).thenReturn(1);
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/nurseData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void testSaveBenQuickConsultDataNurse_alreadySaved() throws Exception {
        String requestJson = "{" +
                "\"someField\":1" +
                "}";
        when(quickConsultationServiceImpl.quickConsultNurseDataInsert(any(JsonObject.class))).thenReturn(3);
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/nurseData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void testSaveBenQuickConsultDataNurse_failure() throws Exception {
        String requestJson = "{" +
                "\"someField\":1" +
                "}";
        when(quickConsultationServiceImpl.quickConsultNurseDataInsert(any(JsonObject.class))).thenReturn(0);
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/nurseData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void testSaveBenQuickConsultDataNurse_invalidRequest() throws Exception {
        String requestJson = "{}";
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/nurseData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void testSaveBenQuickConsultDataNurse_exception() throws Exception {
        String requestJson = "{" +
                "\"someField\":1" +
                "}";
        when(quickConsultationServiceImpl.quickConsultNurseDataInsert(any(JsonObject.class))).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/nurseData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    // --- /save/doctorData ---
    @Test
    void testSaveQuickConsultationDetail_success() throws Exception {
        String requestJson = "{\"quickConsultation\":{}}";
        when(quickConsultationServiceImpl.quickConsultDoctorDataInsert(any(JsonObject.class), anyString())).thenReturn(1);
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/doctorData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void testSaveQuickConsultationDetail_failure() throws Exception {
        String requestJson = "{\"quickConsultation\":{}}";
        when(quickConsultationServiceImpl.quickConsultDoctorDataInsert(any(JsonObject.class), anyString())).thenReturn(0);
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/doctorData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void testSaveQuickConsultationDetail_exception() throws Exception {
        String requestJson = "{\"quickConsultation\":{}}";
        when(quickConsultationServiceImpl.quickConsultDoctorDataInsert(any(JsonObject.class), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/genOPD-QC-quickConsult/save/doctorData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    // --- /getBenDataFrmNurseToDocVisitDetailsScreen ---
    @Test
    void testGetBenDataFrmNurseScrnToDocScrnVisitDetails_success() throws Exception {
        String requestJson = "{\"benRegID\":1,\"visitCode\":2}";
        when(quickConsultationServiceImpl.getBenDataFrmNurseToDocVisitDetailsScreen(anyLong(), anyLong())).thenReturn("visitData");
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenDataFrmNurseToDocVisitDetailsScreen")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visitData")));
    }

    @Test
    void testGetBenDataFrmNurseScrnToDocScrnVisitDetails_invalidRequest() throws Exception {
        String requestJson = "{\"benRegID\":1}";
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenDataFrmNurseToDocVisitDetailsScreen")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetBenDataFrmNurseScrnToDocScrnVisitDetails_exception() throws Exception {
        String requestJson = "{\"benRegID\":1,\"visitCode\":2}";
        when(quickConsultationServiceImpl.getBenDataFrmNurseToDocVisitDetailsScreen(anyLong(), anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenDataFrmNurseToDocVisitDetailsScreen")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting visit data")));
    }

    // --- /getBenVitalDetailsFrmNurse ---
    @Test
    void testGetBenVitalDetailsFrmNurse_success() throws Exception {
        String requestJson = "{\"benRegID\":1,\"visitCode\":2}";
        when(quickConsultationServiceImpl.getBeneficiaryVitalDetails(anyLong(), anyLong())).thenReturn("vitalData");
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenVitalDetailsFrmNurse")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vitalData")));
    }

    @Test
    void testGetBenVitalDetailsFrmNurse_invalidRequest() throws Exception {
        String requestJson = "{\"benRegID\":1}";
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenVitalDetailsFrmNurse")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetBenVitalDetailsFrmNurse_exception() throws Exception {
        String requestJson = "{\"benRegID\":1,\"visitCode\":2}";
        when(quickConsultationServiceImpl.getBeneficiaryVitalDetails(anyLong(), anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenVitalDetailsFrmNurse")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting vital data")));
    }

    // --- /getBenCaseRecordFromDoctorQuickConsult ---
    @Test
    void testGetBenCaseRecordFromDoctorQuickConsult_success() throws Exception {
        String requestJson = "{\"benRegID\":1,\"visitCode\":2}";
        when(quickConsultationServiceImpl.getBenCaseRecordFromDoctorQuickConsult(anyLong(), anyLong())).thenReturn("caseRecord");
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenCaseRecordFromDoctorQuickConsult")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("caseRecord")));
    }

    @Test
    void testGetBenCaseRecordFromDoctorQuickConsult_invalidRequest() throws Exception {
        String requestJson = "{\"benRegID\":1}";
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenCaseRecordFromDoctorQuickConsult")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetBenCaseRecordFromDoctorQuickConsult_exception() throws Exception {
        String requestJson = "{\"benRegID\":1,\"visitCode\":2}";
        when(quickConsultationServiceImpl.getBenCaseRecordFromDoctorQuickConsult(anyLong(), anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/genOPD-QC-quickConsult/getBenCaseRecordFromDoctorQuickConsult")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting doctor data")));
    }

    // --- /update/doctorData ---
    @Test
    void testUpdateGeneralOPDQCDoctorData_success() throws Exception {
        String requestJson = "{\"quickConsultation\":{}}";
        when(quickConsultationServiceImpl.updateGeneralOPDQCDoctorData(any(JsonObject.class), anyString())).thenReturn(1L);
        mockMvc.perform(post("/genOPD-QC-quickConsult/update/doctorData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void testUpdateGeneralOPDQCDoctorData_failure() throws Exception {
        String requestJson = "{\"quickConsultation\":{}}";
        when(quickConsultationServiceImpl.updateGeneralOPDQCDoctorData(any(JsonObject.class), anyString())).thenReturn(0L);
        mockMvc.perform(post("/genOPD-QC-quickConsult/update/doctorData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void testUpdateGeneralOPDQCDoctorData_exception() throws Exception {
        String requestJson = "{\"quickConsultation\":{}}";
        when(quickConsultationServiceImpl.updateGeneralOPDQCDoctorData(any(JsonObject.class), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/genOPD-QC-quickConsult/update/doctorData")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }
}
