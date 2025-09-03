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
package com.iemr.mmu.controller.labtechnician;

import com.google.gson.JsonObject;
import com.iemr.mmu.service.labtechnician.LabTechnicianServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class LabTechnicianControllerTest {
    private MockMvc mockMvc;

    @Mock
    private LabTechnicianServiceImpl labTechnicianServiceImpl;

    @InjectMocks
    private LabTechnicianController labTechnicianController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(labTechnicianController).build();
    }

    @Test
    void testSaveLabTestResult_success() throws Exception {
        when(labTechnicianServiceImpl.saveLabTestResult(any(JsonObject.class))).thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/save/LabTestResult")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"foo\":\"bar\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void testSaveLabTestResult_unableToSave() throws Exception {
        when(labTechnicianServiceImpl.saveLabTestResult(any(JsonObject.class))).thenReturn(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/save/LabTestResult")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"foo\":\"bar\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void testSaveLabTestResult_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/save/LabTestResult")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("null"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testSaveLabTestResult_exception() throws Exception {
        when(labTechnicianServiceImpl.saveLabTestResult(any(JsonObject.class))).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/save/LabTestResult")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"foo\":\"bar\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void testGetBeneficiaryPrescribedProcedure_success() throws Exception {
        when(labTechnicianServiceImpl.getBenePrescribedProcedureDetails(anyLong(), anyLong())).thenReturn("prescribed-data");
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/prescribedProceduresList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"beneficiaryRegID\":1,\"visitCode\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("prescribed-data")));
    }

    @Test
    void testGetBeneficiaryPrescribedProcedure_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/prescribedProceduresList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetBeneficiaryPrescribedProcedure_error() throws Exception {
        when(labTechnicianServiceImpl.getBenePrescribedProcedureDetails(anyLong(), anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/prescribedProceduresList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"beneficiaryRegID\":1,\"visitCode\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in prescribed procedure details")));
    }

    @Test
    void testGetBeneficiaryPrescribedProcedure_exception() throws Exception {
        when(labTechnicianServiceImpl.getBenePrescribedProcedureDetails(anyLong(), anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/prescribedProceduresList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"beneficiaryRegID\":1,\"visitCode\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting prescribed procedure data")));
    }

    @Test
    void testGetLabResultForVisitCode_success() throws Exception {
        when(labTechnicianServiceImpl.getLabResultForVisitcode(anyLong(), anyLong())).thenReturn("lab-result");
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/labResultForVisitcode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"beneficiaryRegID\":1,\"visitCode\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("lab-result")));
    }

    @Test
    void testGetLabResultForVisitCode_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/labResultForVisitcode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetLabResultForVisitCode_error() throws Exception {
        when(labTechnicianServiceImpl.getLabResultForVisitcode(anyLong(), anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/labResultForVisitcode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"beneficiaryRegID\":1,\"visitCode\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting lab report")));
    }

    @Test
    void testGetLabResultForVisitCode_exception() throws Exception {
        when(labTechnicianServiceImpl.getLabResultForVisitcode(anyLong(), anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/labTechnician/get/labResultForVisitcode")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"beneficiaryRegID\":1,\"visitCode\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting lab report")));
    }
}
