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
package com.iemr.mmu.controller.anc;

import com.iemr.mmu.service.anc.ANCService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class ANCControllerTest {
    @Test
    void updateANCCareNurse_success() throws Exception {
        when(ancService.updateBenANCDetails(any())).thenReturn(1);
        String request = "{\"careDetails\":{}}";
        mockMvc.perform(post("/ANC/update/ANCScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateANCCareNurse_unableToModify() throws Exception {
        when(ancService.updateBenANCDetails(any())).thenReturn(0);
        String request = "{\"careDetails\":{}}";
        mockMvc.perform(post("/ANC/update/ANCScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCCareNurse_exception() throws Exception {
        when(ancService.updateBenANCDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"careDetails\":{}}";
        mockMvc.perform(post("/ANC/update/ANCScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCHistoryNurse_success() throws Exception {
        when(ancService.updateBenANCHistoryDetails(any())).thenReturn(1);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/ANC/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateANCHistoryNurse_unableToModify() throws Exception {
        when(ancService.updateBenANCHistoryDetails(any())).thenReturn(0);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/ANC/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCHistoryNurse_exception() throws Exception {
        when(ancService.updateBenANCHistoryDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/ANC/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCVitalNurse_success() throws Exception {
        when(ancService.updateBenANCVitalDetails(any())).thenReturn(1);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/ANC/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateANCVitalNurse_unableToModify() throws Exception {
        when(ancService.updateBenANCVitalDetails(any())).thenReturn(0);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/ANC/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCVitalNurse_exception() throws Exception {
        when(ancService.updateBenANCVitalDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/ANC/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCExaminationNurse_success() throws Exception {
        when(ancService.updateBenANCExaminationDetails(any())).thenReturn(1);
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/ANC/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateANCExaminationNurse_unableToModify() throws Exception {
        when(ancService.updateBenANCExaminationDetails(any())).thenReturn(0);
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/ANC/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCExaminationNurse_exception() throws Exception {
        when(ancService.updateBenANCExaminationDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/ANC/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCDoctorData_success() throws Exception {
        when(ancService.updateANCDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/ANC/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateANCDoctorData_unableToModify() throws Exception {
        when(ancService.updateANCDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/ANC/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateANCDoctorData_exception() throws Exception {
        when(ancService.updateANCDoctorData(any(), anyString())).thenThrow(new RuntimeException("error"));
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/ANC/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }
    @Test
    void getBenExaminationDetailsANC_success() throws Exception {
        when(ancService.getANCExaminationDetailsData(anyLong(), anyLong())).thenReturn("exam details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenExaminationDetailsANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("exam details")));
    }

    @Test
    void getBenExaminationDetailsANC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getBenExaminationDetailsANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenExaminationDetailsANC_exception() throws Exception {
        when(ancService.getANCExaminationDetailsData(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenExaminationDetailsANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary examination data")));
    }

    @Test
    void getBenExaminationDetailsANC_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getBenExaminationDetailsANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary examination data")));
    }

    @Test
    void getBenCaseRecordFromDoctorANC_success() throws Exception {
        when(ancService.getBenCaseRecordFromDoctorANC(anyLong(), anyLong())).thenReturn("doctor details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenCaseRecordFromDoctorANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doctor details")));
    }

    @Test
    void getBenCaseRecordFromDoctorANC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getBenCaseRecordFromDoctorANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorANC_exception() throws Exception {
        when(ancService.getBenCaseRecordFromDoctorANC(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenCaseRecordFromDoctorANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void getBenCaseRecordFromDoctorANC_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getBenCaseRecordFromDoctorANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void getHRPStatus_success() throws Exception {
        when(ancService.getHRPStatus(anyLong(), anyLong())).thenReturn("hrp status");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getHRPStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hrp status")));
    }

    @Test
    void getHRPStatus_nullResponse() throws Exception {
        when(ancService.getHRPStatus(anyLong(), anyLong())).thenReturn(null);
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getHRPStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error in getting HRP status")));
    }

    @Test
    void getHRPStatus_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getHRPStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getHRPStatus_exception() throws Exception {
        when(ancService.getHRPStatus(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getHRPStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error in getting HRP status")));
    }

    @Test
    void getHRPStatus_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getHRPStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error in getting HRP status")));
    }
    @Test
    void getBenVisitDetailsFrmNurseANC_success() throws Exception {
        when(ancService.getBenVisitDetailsFrmNurseANC(anyLong(), anyLong())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenVisitDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenVisitDetailsFrmNurseANC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getBenVisitDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVisitDetailsFrmNurseANC_exception() throws Exception {
        when(ancService.getBenVisitDetailsFrmNurseANC(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenVisitDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenVisitDetailsFrmNurseANC_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getBenVisitDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenANCDetailsFrmNurseANC_success() throws Exception {
        when(ancService.getBenANCDetailsFrmNurseANC(anyLong(), anyLong())).thenReturn("anc details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenANCDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("anc details")));
    }

    @Test
    void getBenANCDetailsFrmNurseANC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getBenANCDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenANCDetailsFrmNurseANC_exception() throws Exception {
        when(ancService.getBenANCDetailsFrmNurseANC(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenANCDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary ANC care data")));
    }

    @Test
    void getBenANCDetailsFrmNurseANC_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getBenANCDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary ANC care data")));
    }

    @Test
    void getBenANCHistoryDetails_success() throws Exception {
        when(ancService.getBenANCHistoryDetails(anyLong(), anyLong())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenANCHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenANCHistoryDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getBenANCHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenANCHistoryDetails_exception() throws Exception {
        when(ancService.getBenANCHistoryDetails(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenANCHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenANCHistoryDetails_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getBenANCHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenANCVitalDetailsFrmNurseANC_success() throws Exception {
        when(ancService.getBeneficiaryVitalDetails(anyLong(), anyLong())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenANCVitalDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenANCVitalDetailsFrmNurseANC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/ANC/getBenANCVitalDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenANCVitalDetailsFrmNurseANC_exception() throws Exception {
        when(ancService.getBeneficiaryVitalDetails(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/ANC/getBenANCVitalDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenANCVitalDetailsFrmNurseANC_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/getBenANCVitalDetailsFrmNurseANC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }
    private MockMvc mockMvc;

    @Mock
    private ANCService ancService;

    @InjectMocks
    private ANCController ancController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ancController).build();
    }

    @Test
    void saveBenANCNurseData_success() throws Exception {
        when(ancService.saveANCNurseData(any())).thenReturn(1L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenANCNurseData_alreadySaved() throws Exception {
        when(ancService.saveANCNurseData(any())).thenReturn(0L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBenANCNurseData_unableToSave() throws Exception {
        when(ancService.saveANCNurseData(any())).thenReturn(null);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenANCNurseData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenANCNurseData_exception() throws Exception {
        when(ancService.saveANCNurseData(any())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenANCDoctorData_success() throws Exception {
        when(ancService.saveANCDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenANCDoctorData_unableToSave() throws Exception {
        when(ancService.saveANCDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenANCDoctorData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/ANC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenANCDoctorData_exception() throws Exception {
        when(ancService.saveANCDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/ANC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }
}
