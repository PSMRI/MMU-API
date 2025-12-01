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
package com.iemr.mmu.controller.generalOPD;

import com.iemr.mmu.service.generalOPD.GeneralOPDServiceImpl;
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

class GeneralOPDControllerTest {
    private MockMvc mockMvc;

    @Mock
    private GeneralOPDServiceImpl generalOPDServiceImpl;

    @InjectMocks
    private GeneralOPDController generalOPDController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(generalOPDController).build();
    }

    @Test
    void saveBenGenOPDNurseData_success() throws Exception {
        when(generalOPDServiceImpl.saveNurseData(any())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenGenOPDNurseData_alreadySaved() throws Exception {
        when(generalOPDServiceImpl.saveNurseData(any())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBenGenOPDNurseData_unableToSave() throws Exception {
        when(generalOPDServiceImpl.saveNurseData(any())).thenReturn(null);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenGenOPDNurseData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/generalOPD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenGenOPDNurseData_exception() throws Exception {
        when(generalOPDServiceImpl.saveNurseData(any())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenGenOPDDoctorData_success() throws Exception {
        when(generalOPDServiceImpl.saveDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenGenOPDDoctorData_unableToSave() throws Exception {
        when(generalOPDServiceImpl.saveDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenGenOPDDoctorData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/generalOPD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenGenOPDDoctorData_exception() throws Exception {
        when(generalOPDServiceImpl.saveDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_success() throws Exception {
        when(generalOPDServiceImpl.getBenVisitDetailsFrmNurseGOPD(any(), any())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenVisitDetailsFrmNurseGOPD")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/generalOPD/getBenVisitDetailsFrmNurseGOPD")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_exception() throws Exception {
        when(generalOPDServiceImpl.getBenVisitDetailsFrmNurseGOPD(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenVisitDetailsFrmNurseGOPD")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenHistoryDetails_success() throws Exception {
        when(generalOPDServiceImpl.getBenHistoryDetails(any(), any())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenHistoryDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/generalOPD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenHistoryDetails_exception() throws Exception {
        when(generalOPDServiceImpl.getBenHistoryDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_success() throws Exception {
        when(generalOPDServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/generalOPD/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_exception() throws Exception {
        when(generalOPDServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenExaminationDetails_success() throws Exception {
        when(generalOPDServiceImpl.getExaminationDetailsData(any(), any())).thenReturn("exam details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenExaminationDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("exam details")));
    }

    @Test
    void getBenExaminationDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/generalOPD/getBenExaminationDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenExaminationDetails_exception() throws Exception {
        when(generalOPDServiceImpl.getExaminationDetailsData(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenExaminationDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary examination data")));
    }

    @Test
    void getBenCaseRecordFromDoctorGeneralOPD_success() throws Exception {
        when(generalOPDServiceImpl.getBenCaseRecordFromDoctorGeneralOPD(any(), any())).thenReturn("doctor details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenCaseRecordFromDoctorGeneralOPD")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doctor details")));
    }

    @Test
    void getBenCaseRecordFromDoctorGeneralOPD_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/generalOPD/getBenCaseRecordFromDoctorGeneralOPD")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorGeneralOPD_exception() throws Exception {
        when(generalOPDServiceImpl.getBenCaseRecordFromDoctorGeneralOPD(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/generalOPD/getBenCaseRecordFromDoctorGeneralOPD")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void updateVisitNurse_success() throws Exception {
        when(generalOPDServiceImpl.UpdateVisitDetails(any())).thenReturn(1);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/visitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateVisitNurse_unableToModify() throws Exception {
        when(generalOPDServiceImpl.UpdateVisitDetails(any())).thenReturn(0);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/visitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVisitNurse_exception() throws Exception {
        when(generalOPDServiceImpl.UpdateVisitDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/visitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_success() throws Exception {
        when(generalOPDServiceImpl.updateBenHistoryDetails(any())).thenReturn(1);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateHistoryNurse_unableToModify() throws Exception {
        when(generalOPDServiceImpl.updateBenHistoryDetails(any())).thenReturn(0);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_exception() throws Exception {
        when(generalOPDServiceImpl.updateBenHistoryDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_success() throws Exception {
        when(generalOPDServiceImpl.updateBenVitalDetails(any())).thenReturn(1);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateVitalNurse_unableToModify() throws Exception {
        when(generalOPDServiceImpl.updateBenVitalDetails(any())).thenReturn(0);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_exception() throws Exception {
        when(generalOPDServiceImpl.updateBenVitalDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateGeneralOPDExaminationNurse_success() throws Exception {
        when(generalOPDServiceImpl.updateBenExaminationDetails(any())).thenReturn(1);
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateGeneralOPDExaminationNurse_unableToModify() throws Exception {
        when(generalOPDServiceImpl.updateBenExaminationDetails(any())).thenReturn(0);
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateGeneralOPDExaminationNurse_exception() throws Exception {
        when(generalOPDServiceImpl.updateBenExaminationDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateGeneralOPDDoctorData_success() throws Exception {
        when(generalOPDServiceImpl.updateGeneralOPDDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateGeneralOPDDoctorData_unableToModify() throws Exception {
        when(generalOPDServiceImpl.updateGeneralOPDDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateGeneralOPDDoctorData_exception() throws Exception {
        when(generalOPDServiceImpl.updateGeneralOPDDoctorData(any(), anyString())).thenThrow(new RuntimeException("error"));
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/generalOPD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }
}
