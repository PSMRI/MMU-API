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
package com.iemr.mmu.controller.ncdCare;

import com.iemr.mmu.service.ncdCare.NCDCareServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class NCDCareControllerTest {
    private MockMvc mockMvc;

    @Mock
    private NCDCareServiceImpl ncdCareServiceImpl;

    @InjectMocks
    private NCDCareController ncdCareController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ncdCareController).build();
    }

    @Test
    void saveBenNCDCareNurseData_success() throws Exception {
        when(ncdCareServiceImpl.saveNCDCareNurseData(any())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenNCDCareNurseData_alreadySaved() throws Exception {
        when(ncdCareServiceImpl.saveNCDCareNurseData(any())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBenNCDCareNurseData_unableToSave() throws Exception {
        when(ncdCareServiceImpl.saveNCDCareNurseData(any())).thenReturn(null);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDCareNurseData_invalidRequest() throws Exception {
    String request = "invalid json";
    mockMvc.perform(post("/NCDCare/save/nurseData")
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDCareNurseData_exception() throws Exception {
        when(ncdCareServiceImpl.saveNCDCareNurseData(any())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDCareDoctorData_success() throws Exception {
        when(ncdCareServiceImpl.saveDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenNCDCareDoctorData_unableToSave() throws Exception {
        when(ncdCareServiceImpl.saveDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDCareDoctorData_invalidRequest() throws Exception {
    String request = "invalid json";
    mockMvc.perform(post("/NCDCare/save/doctorData")
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDCareDoctorData_exception() throws Exception {
        when(ncdCareServiceImpl.saveDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/NCDCare/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void getBenVisitDetailsFrmNurseNCDCare_success() throws Exception {
        when(ncdCareServiceImpl.getBenVisitDetailsFrmNurseNCDCare(any(), any())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenVisitDetailsFrmNurseNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenVisitDetailsFrmNurseNCDCare_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCDCare/getBenVisitDetailsFrmNurseNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVisitDetailsFrmNurseNCDCare_exception() throws Exception {
        when(ncdCareServiceImpl.getBenVisitDetailsFrmNurseNCDCare(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenVisitDetailsFrmNurseNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenNCDCareHistoryDetails_success() throws Exception {
        when(ncdCareServiceImpl.getBenNCDCareHistoryDetails(any(), any())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenNCDCareHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenNCDCareHistoryDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCDCare/getBenNCDCareHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenNCDCareHistoryDetails_exception() throws Exception {
        when(ncdCareServiceImpl.getBenNCDCareHistoryDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenNCDCareHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenVitalDetailsFrmNurseNCDCare_success() throws Exception {
        when(ncdCareServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenVitalDetailsFrmNurseNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenVitalDetailsFrmNurseNCDCare_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCDCare/getBenVitalDetailsFrmNurseNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVitalDetailsFrmNurseNCDCare_exception() throws Exception {
        when(ncdCareServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenVitalDetailsFrmNurseNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenCaseRecordFromDoctorNCDCare_success() throws Exception {
        when(ncdCareServiceImpl.getBenCaseRecordFromDoctorNCDCare(any(), any())).thenReturn("case record");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenCaseRecordFromDoctorNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("case record")));
    }

    @Test
    void getBenCaseRecordFromDoctorNCDCare_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCDCare/getBenCaseRecordFromDoctorNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorNCDCare_exception() throws Exception {
        when(ncdCareServiceImpl.getBenCaseRecordFromDoctorNCDCare(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCDCare/getBenCaseRecordFromDoctorNCDCare")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void updateHistoryNurse_success() throws Exception {
        when(ncdCareServiceImpl.updateBenHistoryDetails(any())).thenReturn(1);
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateHistoryNurse_unableToModify() throws Exception {
        when(ncdCareServiceImpl.updateBenHistoryDetails(any())).thenReturn(0);
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_exception() throws Exception {
        when(ncdCareServiceImpl.updateBenHistoryDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_success() throws Exception {
        when(ncdCareServiceImpl.updateBenVitalDetails(any())).thenReturn(1);
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateVitalNurse_unableToModify() throws Exception {
        when(ncdCareServiceImpl.updateBenVitalDetails(any())).thenReturn(0);
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_exception() throws Exception {
        when(ncdCareServiceImpl.updateBenVitalDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateNCDCareDoctorData_success() throws Exception {
        when(ncdCareServiceImpl.updateNCDCareDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateNCDCareDoctorData_unableToModify() throws Exception {
        when(ncdCareServiceImpl.updateNCDCareDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateNCDCareDoctorData_exception() throws Exception {
        when(ncdCareServiceImpl.updateNCDCareDoctorData(any(), anyString())).thenThrow(new RuntimeException("error"));
        String request = "{\"someKey\":\"someValue\"}";
        mockMvc.perform(post("/NCDCare/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }
}
