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
package com.iemr.mmu.controller.pnc;

import com.iemr.mmu.service.pnc.PNCServiceImpl;
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

class PostnatalCareControllerTest {
    private MockMvc mockMvc;

    @Mock
    private PNCServiceImpl pncServiceImpl;

    @InjectMocks
    private PostnatalCareController postnatalCareController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(postnatalCareController).build();
    }

    @Test
    void saveBenPNCNurseData_success() throws Exception {
        when(pncServiceImpl.savePNCNurseData(any())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenPNCNurseData_alreadySaved() throws Exception {
        when(pncServiceImpl.savePNCNurseData(any())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBenPNCNurseData_unableToSave() throws Exception {
        when(pncServiceImpl.savePNCNurseData(any())).thenReturn(null);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenPNCNurseData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/PNC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenPNCNurseData_exception() throws Exception {
        when(pncServiceImpl.savePNCNurseData(any())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenPNCDoctorData_success() throws Exception {
        when(pncServiceImpl.savePNCDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenPNCDoctorData_unableToSave() throws Exception {
        when(pncServiceImpl.savePNCDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenPNCDoctorData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/PNC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenPNCDoctorData_exception() throws Exception {
        when(pncServiceImpl.savePNCDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"visitDetails\":{}}";
        mockMvc.perform(post("/PNC/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void getBenVisitDetailsFrmNursePNC_success() throws Exception {
        when(pncServiceImpl.getBenVisitDetailsFrmNursePNC(any(), any())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenVisitDetailsFrmNursePNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenVisitDetailsFrmNursePNC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/PNC/getBenVisitDetailsFrmNursePNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVisitDetailsFrmNursePNC_exception() throws Exception {
        when(pncServiceImpl.getBenVisitDetailsFrmNursePNC(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenVisitDetailsFrmNursePNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenPNCDetailsFrmNursePNC_success() throws Exception {
        when(pncServiceImpl.getBenPNCDetailsFrmNursePNC(any(), any())).thenReturn("pnc details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenPNCDetailsFrmNursePNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("pnc details")));
    }

    @Test
    void getBenPNCDetailsFrmNursePNC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/PNC/getBenPNCDetailsFrmNursePNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPNCDetailsFrmNursePNC_exception() throws Exception {
        when(pncServiceImpl.getBenPNCDetailsFrmNursePNC(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenPNCDetailsFrmNursePNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary PNC Care data")));
    }

    @Test
    void getBenHistoryDetails_success() throws Exception {
        when(pncServiceImpl.getBenHistoryDetails(any(), any())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenHistoryDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/PNC/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenHistoryDetails_exception() throws Exception {
        when(pncServiceImpl.getBenHistoryDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_success() throws Exception {
        when(pncServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/PNC/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_exception() throws Exception {
        when(pncServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenExaminationDetailsPNC_success() throws Exception {
        when(pncServiceImpl.getPNCExaminationDetailsData(any(), any())).thenReturn("exam details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenExaminationDetailsPNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("exam details")));
    }

    @Test
    void getBenExaminationDetailsPNC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/PNC/getBenExaminationDetailsPNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenExaminationDetailsPNC_exception() throws Exception {
        when(pncServiceImpl.getPNCExaminationDetailsData(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenExaminationDetailsPNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary examination data")));
    }

    @Test
    void getBenCaseRecordFromDoctorPNC_success() throws Exception {
        when(pncServiceImpl.getBenCaseRecordFromDoctorPNC(any(), any())).thenReturn("doctor details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenCaseRecordFromDoctorPNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doctor details")));
    }

    @Test
    void getBenCaseRecordFromDoctorPNC_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/PNC/getBenCaseRecordFromDoctorPNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorPNC_exception() throws Exception {
        when(pncServiceImpl.getBenCaseRecordFromDoctorPNC(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/PNC/getBenCaseRecordFromDoctorPNC")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void updatePNCCareNurse_success() throws Exception {
        when(pncServiceImpl.updateBenPNCDetails(any())).thenReturn(1);
        String request = "{\"pncDetails\":{}}";
        mockMvc.perform(post("/PNC/update/PNCScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updatePNCCareNurse_unableToModify() throws Exception {
        when(pncServiceImpl.updateBenPNCDetails(any())).thenReturn(0);
        String request = "{\"pncDetails\":{}}";
        mockMvc.perform(post("/PNC/update/PNCScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updatePNCCareNurse_exception() throws Exception {
        when(pncServiceImpl.updateBenPNCDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"pncDetails\":{}}";
        mockMvc.perform(post("/PNC/update/PNCScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_success() throws Exception {
        when(pncServiceImpl.updateBenHistoryDetails(any())).thenReturn(1);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/PNC/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateHistoryNurse_unableToModify() throws Exception {
        when(pncServiceImpl.updateBenHistoryDetails(any())).thenReturn(0);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/PNC/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_exception() throws Exception {
        when(pncServiceImpl.updateBenHistoryDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/PNC/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_success() throws Exception {
        when(pncServiceImpl.updateBenVitalDetails(any())).thenReturn(1);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/PNC/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateVitalNurse_unableToModify() throws Exception {
        when(pncServiceImpl.updateBenVitalDetails(any())).thenReturn(0);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/PNC/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_exception() throws Exception {
        when(pncServiceImpl.updateBenVitalDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/PNC/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateGeneralOPDExaminationNurse_success() throws Exception {
        when(pncServiceImpl.updateBenExaminationDetails(any())).thenReturn(1);
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/PNC/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateGeneralOPDExaminationNurse_unableToModify() throws Exception {
        when(pncServiceImpl.updateBenExaminationDetails(any())).thenReturn(0);
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/PNC/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateGeneralOPDExaminationNurse_exception() throws Exception {
        when(pncServiceImpl.updateBenExaminationDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"examDetails\":{}}";
        mockMvc.perform(post("/PNC/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updatePNCDoctorData_success() throws Exception {
        when(pncServiceImpl.updatePNCDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/PNC/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updatePNCDoctorData_unableToModify() throws Exception {
        when(pncServiceImpl.updatePNCDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/PNC/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updatePNCDoctorData_exception() throws Exception {
        when(pncServiceImpl.updatePNCDoctorData(any(), anyString())).thenThrow(new RuntimeException("error"));
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/PNC/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }
}
