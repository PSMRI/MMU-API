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
package com.iemr.mmu.controller.cancerscreening;

import com.iemr.mmu.service.cancerScreening.CSService;
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

class CancerScreeningControllerTest {
    @Test
    void getBenCaseRecordFromDoctorCS_success() throws Exception {
        when(cSService.getBenCaseRecordFromDoctorCS(anyLong(), anyLong())).thenReturn("doctor record");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCaseRecordFromDoctorCS")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doctor record")));
    }

    @Test
    void getBenCaseRecordFromDoctorCS_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCaseRecordFromDoctorCS")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCaseRecordFromDoctorCS_exception() throws Exception {
        when(cSService.getBenCaseRecordFromDoctorCS(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCaseRecordFromDoctorCS")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary doctor data")));
    }

    @Test
    void upodateBenVitalDetail_success() throws Exception {
        when(cSService.updateBenVitalDetail(any())).thenReturn(1);
        String request = "{\"ID\":1,\"beneficiaryRegID\":1,\"benVisitID\":1,\"weight_Kg\":70.0,\"height_cm\":170.0,\"waistCircumference_cm\":80.0,\"bloodGlucose_Fasting\":90,\"bloodGlucose_Random\":100,\"bloodGlucose_2HrPostPrandial\":110,\"systolicBP_1stReading\":120,\"diastolicBP_1stReading\":80,\"systolicBP_2ndReading\":121,\"diastolicBP_2ndReading\":81,\"systolicBP_3rdReading\":122,\"diastolicBP_3rdReading\":82,\"hbA1C\":5,\"hemoglobin\":13,\"modifiedBy\":\"user\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void upodateBenVitalDetail_failure() throws Exception {
        when(cSService.updateBenVitalDetail(any())).thenReturn(0);
        String request = "{\"ID\":1,\"beneficiaryRegID\":1,\"benVisitID\":1,\"weight_Kg\":70.0,\"height_cm\":170.0,\"waistCircumference_cm\":80.0,\"bloodGlucose_Fasting\":90,\"bloodGlucose_Random\":100,\"bloodGlucose_2HrPostPrandial\":110,\"systolicBP_1stReading\":120,\"diastolicBP_1stReading\":80,\"systolicBP_2ndReading\":121,\"diastolicBP_2ndReading\":81,\"systolicBP_3rdReading\":122,\"diastolicBP_3rdReading\":82,\"hbA1C\":5,\"hemoglobin\":13,\"modifiedBy\":\"user\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void upodateBenVitalDetail_exception() throws Exception {
        when(cSService.updateBenVitalDetail(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"ID\":1,\"beneficiaryRegID\":1,\"benVisitID\":1,\"weight_Kg\":70.0,\"height_cm\":170.0,\"waistCircumference_cm\":80.0,\"bloodGlucose_Fasting\":90,\"bloodGlucose_Random\":100,\"bloodGlucose_2HrPostPrandial\":110,\"systolicBP_1stReading\":120,\"diastolicBP_1stReading\":80,\"systolicBP_2ndReading\":121,\"diastolicBP_2ndReading\":81,\"systolicBP_3rdReading\":122,\"diastolicBP_3rdReading\":82,\"hbA1C\":5,\"hemoglobin\":13,\"modifiedBy\":\"user\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/vitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCancerDiagnosisDetailsByOncologist_success() throws Exception {
        when(cSService.updateCancerDiagnosisDetailsByOncologist(any())).thenReturn(1);
        String request = "{\"beneficiaryRegID\":1,\"benVisitID\":1,\"visitCode\":2,\"provisionalDiagnosisOncologist\":\"diag\",\"modifiedBy\":\"user\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/examinationScreen/diagnosis")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateCancerDiagnosisDetailsByOncologist_failure() throws Exception {
        when(cSService.updateCancerDiagnosisDetailsByOncologist(any())).thenReturn(0);
        String request = "{\"beneficiaryRegID\":1,\"benVisitID\":1,\"visitCode\":2,\"provisionalDiagnosisOncologist\":\"diag\",\"modifiedBy\":\"user\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/examinationScreen/diagnosis")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCancerDiagnosisDetailsByOncologist_exception() throws Exception {
        when(cSService.updateCancerDiagnosisDetailsByOncologist(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"beneficiaryRegID\":1,\"benVisitID\":1,\"visitCode\":2,\"provisionalDiagnosisOncologist\":\"diag\",\"modifiedBy\":\"user\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/examinationScreen/diagnosis")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCancerScreeningDoctorData_success() throws Exception {
        when(cSService.updateCancerScreeningDoctorData(any())).thenReturn(1);
        String request = "{\"field\":\"value\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateCancerScreeningDoctorData_failure() throws Exception {
        when(cSService.updateCancerScreeningDoctorData(any())).thenReturn(0);
        String request = "{\"field\":\"value\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCancerScreeningDoctorData_exception() throws Exception {
        when(cSService.updateCancerScreeningDoctorData(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"field\":\"value\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while updating beneficiary data")));
    }

    @Test
    void updateCSHistoryNurse_success() throws Exception {
        when(cSService.UpdateCSHistoryNurseData(any())).thenReturn(1);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void updateCSHistoryNurse_failure() throws Exception {
        when(cSService.UpdateCSHistoryNurseData(any())).thenReturn(0);
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void updateCSHistoryNurse_exception() throws Exception {
        when(cSService.UpdateCSHistoryNurseData(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"historyDetails\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/update/historyScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void upodateBenExaminationDetail_success() throws Exception {
        when(cSService.updateBenExaminationDetail(any())).thenReturn(1);
        String request = "{\"field\":\"value\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data updated successfully")));
    }

    @Test
    void upodateBenExaminationDetail_failure() throws Exception {
        when(cSService.updateBenExaminationDetail(any())).thenReturn(0);
        String request = "{\"field\":\"value\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }

    @Test
    void upodateBenExaminationDetail_exception() throws Exception {
        when(cSService.updateBenExaminationDetail(any())).thenThrow(new RuntimeException("error"));
        String request = "{\"field\":\"value\"}";
        mockMvc.perform(post("/CS-cancerScreening/update/examinationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to modify data")));
    }
    @Test
    void getBenCancerFamilyHistory_success() throws Exception {
        when(cSService.getBenFamilyHistoryData(anyLong())).thenReturn("family history");
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("family history")));
    }

    @Test
    void getBenCancerFamilyHistory_invalidRequest() throws Exception {
        String request = "{}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCancerFamilyHistory_exception() throws Exception {
        when(cSService.getBenFamilyHistoryData(anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary family history data")));
    }

    @Test
    void getBenCancerFamilyHistory_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary family history data")));
    }

    @Test
    void getBenCancerPersonalHistory_success() throws Exception {
        when(cSService.getBenPersonalHistoryData(anyLong())).thenReturn("personal history");
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("personal history")));
    }

    @Test
    void getBenCancerPersonalHistory_invalidRequest() throws Exception {
        String request = "{}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCancerPersonalHistory_exception() throws Exception {
        when(cSService.getBenPersonalHistoryData(anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary personal history data")));
    }

    @Test
    void getBenCancerPersonalHistory_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary personal history data")));
    }

    @Test
    void getBenCancerPersonalDietHistory_success() throws Exception {
        when(cSService.getBenPersonalDietHistoryData(anyLong())).thenReturn("diet history");
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalDietHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("diet history")));
    }

    @Test
    void getBenCancerPersonalDietHistory_invalidRequest() throws Exception {
        String request = "{}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalDietHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCancerPersonalDietHistory_exception() throws Exception {
        when(cSService.getBenPersonalDietHistoryData(anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalDietHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary personal diet history data")));
    }

    @Test
    void getBenCancerPersonalDietHistory_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerPersonalDietHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary personal diet history data")));
    }

    @Test
    void getBenCancerObstetricHistory_success() throws Exception {
        when(cSService.getBenObstetricHistoryData(anyLong())).thenReturn("obstetric history");
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("obstetric history")));
    }

    @Test
    void getBenCancerObstetricHistory_invalidRequest() throws Exception {
        String request = "{}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenCancerObstetricHistory_exception() throws Exception {
        when(cSService.getBenObstetricHistoryData(anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary obstetric history data")));
    }

    @Test
    void getBenCancerObstetricHistory_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenCancerObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary obstetric history data")));
    }
    @Test
    void getBenDataFrmNurseScrnToDocScrnVisitDetails_success() throws Exception {
        when(cSService.getBenDataFrmNurseToDocVisitDetailsScreen(anyLong(), anyLong())).thenReturn("visit details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVisitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("visit details")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVisitDetails_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVisitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVisitDetails_exception() throws Exception {
        when(cSService.getBenDataFrmNurseToDocVisitDetailsScreen(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVisitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVisitDetails_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVisitDetailsScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary visit data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnHistory_success() throws Exception {
        when(cSService.getBenDataFrmNurseToDocHistoryScreen(anyLong(), anyLong())).thenReturn("history details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocHistoryScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("history details")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnHistory_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocHistoryScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnHistory_exception() throws Exception {
        when(cSService.getBenDataFrmNurseToDocHistoryScreen(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocHistoryScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnHistory_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocHistoryScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary history data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVital_success() throws Exception {
        when(cSService.getBenDataFrmNurseToDocVitalScreen(anyLong(), anyLong())).thenReturn("vital details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vital details")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVital_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVital_exception() throws Exception {
        when(cSService.getBenDataFrmNurseToDocVitalScreen(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnVital_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocVitalScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary vital data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnExamination_success() throws Exception {
        when(cSService.getBenDataFrmNurseToDocExaminationScreen(anyLong(), anyLong())).thenReturn("exam details");
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocExaminationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("exam details")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnExamination_invalidRequest() throws Exception {
        String request = "{\"benRegID\":1}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocExaminationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnExamination_exception() throws Exception {
        when(cSService.getBenDataFrmNurseToDocExaminationScreen(anyLong(), anyLong())).thenThrow(new RuntimeException("error"));
        String request = "{\"benRegID\":1,\"visitCode\":2}";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocExaminationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary examination data")));
    }

    @Test
    void getBenDataFrmNurseScrnToDocScrnExamination_invalidJson() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/getBenDataFrmNurseToDocExaminationScreen")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary examination data")));
    }
    private MockMvc mockMvc;

    @Mock
    private CSService cSService;

    @InjectMocks
    private CancerScreeningController cancerScreeningController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cancerScreeningController).build();
    }

    @Test
    void saveBenCancerScreeningNurseData_success() throws Exception {
        when(cSService.saveCancerScreeningNurseData(any(), anyString())).thenReturn(1L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenCancerScreeningNurseData_mammogramOrder() throws Exception {
        when(cSService.saveCancerScreeningNurseData(any(), anyString())).thenReturn(2L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MAMMOGRAM order created successfully")));
    }

    @Test
    void saveBenCancerScreeningNurseData_mammogramOrderError() throws Exception {
        when(cSService.saveCancerScreeningNurseData(any(), anyString())).thenReturn(3L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error in mammogram order creation")));
    }

    @Test
    void saveBenCancerScreeningNurseData_alreadySaved() throws Exception {
        when(cSService.saveCancerScreeningNurseData(any(), anyString())).thenReturn(0L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data already saved")));
    }

    @Test
    void saveBenCancerScreeningNurseData_unableToSave() throws Exception {
        when(cSService.saveCancerScreeningNurseData(any(), anyString())).thenReturn(null);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCancerScreeningNurseData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCancerScreeningNurseData_exception() throws Exception {
        when(cSService.saveCancerScreeningNurseData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/nurseData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCancerScreeningDoctorData_success() throws Exception {
        when(cSService.saveCancerScreeningDoctorData(any(), anyString())).thenReturn(1L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data saved successfully")));
    }

    @Test
    void saveBenCancerScreeningDoctorData_unableToSave() throws Exception {
        when(cSService.saveCancerScreeningDoctorData(any(), anyString())).thenReturn(0L);
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCancerScreeningDoctorData_invalidRequest() throws Exception {
        String request = "invalid json";
        mockMvc.perform(post("/CS-cancerScreening/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }

    @Test
    void saveBenCancerScreeningDoctorData_exception() throws Exception {
        when(cSService.saveCancerScreeningDoctorData(any(), anyString())).thenThrow(new RuntimeException("DB error"));
        String request = "{\"someKey\":{}}";
        mockMvc.perform(post("/CS-cancerScreening/save/doctorData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unable to save data")));
    }
}
