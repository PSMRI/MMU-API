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
package com.iemr.mmu.controller.ncdscreening;

import com.iemr.mmu.service.ncdscreening.NCDScreeningServiceImpl;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class NCDControllerTest {
    private MockMvc mockMvc;

    @Mock
    private NCDScreeningServiceImpl ncdScreeningServiceImpl;
    @Mock
    private com.iemr.mmu.service.ncdscreening.NCDScreeningService ncdScreeningService;
    @Mock
    private com.iemr.mmu.service.ncdscreening.NCDSCreeningDoctorService ncdSCreeningDoctorService;

    @InjectMocks
    private NCDController ncdController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ncdController).build();
    }

    @Test
    void saveBeneficiaryNCDScreeningDetails_success() throws Exception {
        when(ncdScreeningServiceImpl.saveNCDScreeningNurseData(any(), anyString())).thenReturn(1L);
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBeneficiaryNCDScreeningDetails_alreadySaved() throws Exception {
        when(ncdScreeningServiceImpl.saveNCDScreeningNurseData(any(), anyString())).thenReturn(0L);
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBeneficiaryNCDScreeningDetails_unableToSave() throws Exception {
        when(ncdScreeningServiceImpl.saveNCDScreeningNurseData(any(), anyString())).thenReturn(null);
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBeneficiaryNCDScreeningDetails_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/NCD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBeneficiaryNCDScreeningDetails_exception() throws Exception {
        when(ncdScreeningServiceImpl.saveNCDScreeningNurseData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDScreeningDoctorData_success() throws Exception {
        when(ncdScreeningServiceImpl.saveDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenNCDScreeningDoctorData_unableToSave() throws Exception {
        when(ncdScreeningServiceImpl.saveDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDScreeningDoctorData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/NCD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenNCDScreeningDoctorData_exception() throws Exception {
        when(ncdScreeningServiceImpl.saveDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void getNCDScreenigDetails_success() throws Exception {
        when(ncdScreeningServiceImpl.getNCDScreeningDetails(anyLong(), anyLong())).thenReturn("screening details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/get/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("screening details")));
    }

    @Test
    void getNCDScreenigDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCD/get/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getNCDScreenigDetails_exception() throws Exception {
        when(ncdScreeningServiceImpl.getNCDScreeningDetails(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/get/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting NCD Screening data")));
    }

    @Test
    void getNcdScreeningVisitCount_success() throws Exception {
        when(ncdScreeningServiceImpl.getNcdScreeningVisitCnt(anyLong())).thenReturn("2");
        mockMvc.perform(get("/NCD/getNcdScreeningVisitCount/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2")));
    }

    @Test
    void getNcdScreeningVisitCount_nullId() throws Exception {
        mockMvc.perform(get("/NCD/getNcdScreeningVisitCount/")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getNcdScreeningVisitCount_error() throws Exception {
        when(ncdScreeningServiceImpl.getNcdScreeningVisitCnt(anyLong())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/NCD/getNcdScreeningVisitCount/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting NCD screening Visit Count")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_success() throws Exception {
        when(ncdScreeningServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCD/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVitalDetailsFrmNurse_exception() throws Exception {
        when(ncdScreeningServiceImpl.getBeneficiaryVitalDetails(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenVitalDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenIdrsDetailsFrmNurse_success() throws Exception {
        when(ncdScreeningServiceImpl.getBenIdrsDetailsFrmNurse(any(), any())).thenReturn("idrs details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenIdrsDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("idrs details")));
    }

    @Test
    void getBenIdrsDetailsFrmNurse_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCD/getBenIdrsDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenIdrsDetailsFrmNurse_exception() throws Exception {
        when(ncdScreeningServiceImpl.getBenIdrsDetailsFrmNurse(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenIdrsDetailsFrmNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary Idrs data")));
    }

    @Test
    void getBenCaseRecordFromDoctorNCDCare_success() throws Exception {
        when(ncdScreeningServiceImpl.getBenCaseRecordFromDoctorNCDScreening(any(), any())).thenReturn("doctor details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenCaseRecordFromDoctorNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doctor details")));
    }

    @Test
    void getBenCaseRecordFromDoctorNCDCare_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCD/getBenCaseRecordFromDoctorNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorNCDCare_exception() throws Exception {
        when(ncdScreeningServiceImpl.getBenCaseRecordFromDoctorNCDScreening(any(), any())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenCaseRecordFromDoctorNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void updateBeneficiaryNCDScreeningDetails_success() throws Exception {
        when(ncdScreeningServiceImpl.updateNurseNCDScreeningDetails(any())).thenReturn(1);
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/update/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateBeneficiaryNCDScreeningDetails_unableToModify() throws Exception {
        when(ncdScreeningServiceImpl.updateNurseNCDScreeningDetails(any())).thenReturn(0);
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/update/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateBeneficiaryNCDScreeningDetails_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/NCD/update/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateBeneficiaryNCDScreeningDetails_exception() throws Exception {
        when(ncdScreeningServiceImpl.updateNurseNCDScreeningDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"screeningDetails\":{}}";
        mockMvc.perform(post("/NCD/update/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_success() throws Exception {
        when(ncdScreeningService.UpdateNCDScreeningHistory(any())).thenReturn(1);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/NCD/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateHistoryNurse_unableToModify() throws Exception {
        when(ncdScreeningService.UpdateNCDScreeningHistory(any())).thenReturn(0);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/NCD/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateHistoryNurse_exception() throws Exception {
        when(ncdScreeningService.UpdateNCDScreeningHistory(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/NCD/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_success() throws Exception {
        when(ncdScreeningServiceImpl.updateBenVitalDetails(any())).thenReturn(1);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/NCD/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateVitalNurse_unableToModify() throws Exception {
        when(ncdScreeningServiceImpl.updateBenVitalDetails(any())).thenReturn(0);
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/NCD/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateVitalNurse_exception() throws Exception {
        when(ncdScreeningServiceImpl.updateBenVitalDetails(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"vitalDetails\":{}}";
        mockMvc.perform(post("/NCD/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateIDRSScreen_success() throws Exception {
        when(ncdScreeningService.UpdateIDRSScreen(any())).thenReturn(1L);
        String request = "{\"idrsDetails\":{}}";
        mockMvc.perform(post("/NCD/update/idrsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateIDRSScreen_unableToModify() throws Exception {
        when(ncdScreeningService.UpdateIDRSScreen(any())).thenReturn(0L);
        String request = "{\"idrsDetails\":{}}";
        mockMvc.perform(post("/NCD/update/idrsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateIDRSScreen_exception() throws Exception {
        when(ncdScreeningService.UpdateIDRSScreen(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"idrsDetails\":{}}";
        mockMvc.perform(post("/NCD/update/idrsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateIDRSScreen_nullResult() throws Exception {
        when(ncdScreeningService.UpdateIDRSScreen(any())).thenReturn(null);
        String request = "{\"idrsDetails\":{}}";
        mockMvc.perform(post("/NCD/update/idrsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateDoctorData_success() throws Exception {
        when(ncdSCreeningDoctorService.updateDoctorData(any())).thenReturn(1);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateDoctorData_unableToModify() throws Exception {
        when(ncdSCreeningDoctorService.updateDoctorData(any())).thenReturn(0);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error in data update")));
    }

    @Test
    void updateDoctorData_exception() throws Exception {
        when(ncdSCreeningDoctorService.updateDoctorData(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateDoctorData_negativeResult() throws Exception {
        when(ncdSCreeningDoctorService.updateDoctorData(any())).thenReturn(-1);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error in data update")));
    }

    @Test
    void updateDoctorData_zeroResult() throws Exception {
        when(ncdSCreeningDoctorService.updateDoctorData(any())).thenReturn(0);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error in data update")));
    }

    @Test
    void updateDoctorData_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_success() throws Exception {
        when(ncdScreeningServiceImpl.getBenVisitDetailsFrmNurseNCDScreening(anyLong(), anyLong())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenVisitDetailsFrmNurseNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCD/getBenVisitDetailsFrmNurseNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_exception() throws Exception {
        when(ncdScreeningServiceImpl.getBenVisitDetailsFrmNurseNCDScreening(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenVisitDetailsFrmNurseNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenVisitDetailsFrmNurseGOPD_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/NCD/getBenVisitDetailsFrmNurseNCDScreening")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenHistoryDetails_success() throws Exception {
        when(ncdScreeningServiceImpl.getBenHistoryDetails(anyLong(), anyLong())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenHistoryDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/NCD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenHistoryDetails_exception() throws Exception {
        when(ncdScreeningServiceImpl.getBenHistoryDetails(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/NCD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenHistoryDetails_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/NCD/getBenHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }
    void updateDoctorData_largePositiveResult() throws Exception {
        when(ncdSCreeningDoctorService.updateDoctorData(any())).thenReturn(100);
        String request = "{\"doctorDetails\":{}}";
        mockMvc.perform(post("/NCD/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }
}
