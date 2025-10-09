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
package com.iemr.mmu.controller.covid19;

import com.iemr.mmu.service.covid19.Covid19Service;
import com.iemr.mmu.service.covid19.Covid19ServiceImpl;
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

class CovidControllerTest {
    private MockMvc mockMvc;

    @Mock
    private Covid19Service covid19Service;
    @Mock
    private Covid19ServiceImpl covid19ServiceImpl;

    @InjectMocks
    private CovidController covidController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(covidController).build();
    }

    @Test
    void saveBenCovid19NurseData_success() throws Exception {
        when(covid19Service.saveCovid19NurseData(any(), anyString())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenCovid19NurseData_alreadySaved() throws Exception {
        when(covid19Service.saveCovid19NurseData(any(), anyString())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBenCovid19NurseData_unableToSave() throws Exception {
        when(covid19Service.saveCovid19NurseData(any(), anyString())).thenReturn(null);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCovid19NurseData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/pandemic/covid/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCovid19NurseData_exception() throws Exception {
        when(covid19Service.saveCovid19NurseData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCovidDoctorData_success() throws Exception {
        when(covid19Service.saveDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenCovidDoctorData_unableToSave() throws Exception {
        when(covid19Service.saveDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCovidDoctorData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/pandemic/covid/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCovidDoctorData_exception() throws Exception {
        when(covid19Service.saveDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void getBenVisitDetailsFrmNurseCovid19_success() throws Exception {
        when(covid19ServiceImpl.getBenVisitDetailsFrmNurseCovid19(any(), any())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenVisitDetailsFrmNurseCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenVisitDetailsFrmNurseCovid19_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/pandemic/covid/getBenVisitDetailsFrmNurseCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVisitDetailsFrmNurseCovid19_exception() throws Exception {
        when(covid19ServiceImpl.getBenVisitDetailsFrmNurseCovid19(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenVisitDetailsFrmNurseCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenCovid19HistoryDetails_success() throws Exception {
        when(covid19ServiceImpl.getBenCovid19HistoryDetails(any(), any())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenCovid19HistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenCovid19HistoryDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/pandemic/covid/getBenCovid19HistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCovid19HistoryDetails_exception() throws Exception {
        when(covid19ServiceImpl.getBenCovid19HistoryDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenCovid19HistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenVitalDetailsFrmNurseNCDCare_success() throws Exception {
        when(covid19ServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenVitalDetailsFrmNurseCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenVitalDetailsFrmNurseNCDCare_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/pandemic/covid/getBenVitalDetailsFrmNurseCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVitalDetailsFrmNurseNCDCare_exception() throws Exception {
        when(covid19ServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenVitalDetailsFrmNurseCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void updateHistoryNurse_success() throws Exception {
        when(covid19ServiceImpl.updateBenHistoryDetails(any())).thenReturn(1);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateHistoryNurse_unableToModify() throws Exception {
        when(covid19ServiceImpl.updateBenHistoryDetails(any())).thenReturn(0);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_exception() throws Exception {
        when(covid19ServiceImpl.updateBenHistoryDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_success() throws Exception {
        when(covid19ServiceImpl.updateBenVitalDetails(any())).thenReturn(1);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateVitalNurse_unableToModify() throws Exception {
        when(covid19ServiceImpl.updateBenVitalDetails(any())).thenReturn(0);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_exception() throws Exception {
        when(covid19ServiceImpl.updateBenVitalDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCovid19DoctorData_success() throws Exception {
        when(covid19ServiceImpl.updateCovid19DoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateCovid19DoctorData_unableToModify() throws Exception {
        when(covid19ServiceImpl.updateCovid19DoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCovid19DoctorData_exception() throws Exception {
        when(covid19ServiceImpl.updateCovid19DoctorData(any(), anyString())).thenThrow(new RuntimeException("error"));
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/pandemic/covid/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void getBenCaseRecordFromDoctorCovid19_success() throws Exception {
        when(covid19ServiceImpl.getBenCaseRecordFromDoctorCovid19(any(), any())).thenReturn("doctor details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenCaseRecordFromDoctorCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doctor details")));
    }

    @Test
    void getBenCaseRecordFromDoctorCovid19_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/pandemic/covid/getBenCaseRecordFromDoctorCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorCovid19_exception() throws Exception {
        when(covid19ServiceImpl.getBenCaseRecordFromDoctorCovid19(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/pandemic/covid/getBenCaseRecordFromDoctorCovid")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }
}
