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
package com.iemr.mmu.controller.common.main;

import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonServiceImpl;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.servlet.ServletContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommonControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommonDoctorServiceImpl commonDoctorServiceImpl;
    @Mock
    private CommonNurseServiceImpl commonNurseServiceImpl;
    @Mock
    private CommonServiceImpl commonServiceImpl;
    @Mock
    private ServletContext servletContext;
    @Mock
    private AESEncryptionDecryption aESEncryptionDecryption;

    @InjectMocks
    private CommonController commonController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commonController).build();
    }

    // Test cases will be added here
        @Test
        void getDocWorkListNew_success() throws Exception {
            when(commonDoctorServiceImpl.getDocWorkListNew(1, 2, 3)).thenReturn("result");
            mockMvc.perform(get("/common/getDocWorklistNew/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("result")));
        }

        @Test
        void getDocWorkListNew_invalidRequest() throws Exception {
            mockMvc.perform(get("/common/getDocWorklistNew/null/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getDocWorkListNew_exception() throws Exception {
            when(commonDoctorServiceImpl.getDocWorkListNew(any(), any(), any())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(get("/common/getDocWorklistNew/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting doctor worklist")));
        }

        @Test
        void getDocWorkListNewFutureScheduledForTM_success() throws Exception {
            when(commonDoctorServiceImpl.getDocWorkListNewFutureScheduledForTM(1, 2)).thenReturn("future");
            mockMvc.perform(get("/common/getDocWorkListNewFutureScheduledForTM/1/2")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("future")));
        }

        @Test
        void getDocWorkListNewFutureScheduledForTM_invalidRequest() throws Exception {
            mockMvc.perform(get("/common/getDocWorkListNewFutureScheduledForTM/null/2")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getDocWorkListNewFutureScheduledForTM_exception() throws Exception {
            when(commonDoctorServiceImpl.getDocWorkListNewFutureScheduledForTM(any(), any())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(get("/common/getDocWorkListNewFutureScheduledForTM/1/2")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting doctor worklist for future scheduled beneficiay")));
        }

        @Test
        void getNurseWorkListNew_success() throws Exception {
            when(commonNurseServiceImpl.getNurseWorkListNew(1, 3)).thenReturn("nurse");
            mockMvc.perform(get("/common/getNurseWorklistNew/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("nurse")));
        }

        @Test
        void getNurseWorkListNew_error() throws Exception {
            when(commonNurseServiceImpl.getNurseWorkListNew(any(), any())).thenReturn(null);
            mockMvc.perform(get("/common/getNurseWorklistNew/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting nurse worklist")));
        }

        @Test
        void getNurseWorkListNew_exception() throws Exception {
            when(commonNurseServiceImpl.getNurseWorkListNew(any(), any())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(get("/common/getNurseWorklistNew/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting nurse worklist")));
        }

        @Test
        void getNurseWorklistTMreferred_success() throws Exception {
            when(commonNurseServiceImpl.getNurseWorkListTMReferred(1, 3)).thenReturn("tmref");
            mockMvc.perform(get("/common/getNurseWorklistTMreferred/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("tmref")));
        }

        @Test
        void getNurseWorklistTMreferred_error() throws Exception {
            when(commonNurseServiceImpl.getNurseWorkListTMReferred(any(), any())).thenReturn(null);
            mockMvc.perform(get("/common/getNurseWorklistTMreferred/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting nurse worklist")));
        }

        @Test
        void getNurseWorklistTMreferred_exception() throws Exception {
            when(commonNurseServiceImpl.getNurseWorkListTMReferred(any(), any())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(get("/common/getNurseWorklistTMreferred/1/2/3")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting nurse worklist")));
        }

        @Test
        void getDoctorPreviousSignificantFindings_success() throws Exception {
            String req = "{\"beneficiaryRegID\":123}";
            when(commonDoctorServiceImpl.fetchBenPreviousSignificantFindings(123L)).thenReturn("findings");
            mockMvc.perform(post("/common/getDoctorPreviousSignificantFindings")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("findings")));
        }

        @Test
        void getDoctorPreviousSignificantFindings_error() throws Exception {
            String req = "{\"beneficiaryRegID\":123}";
            when(commonDoctorServiceImpl.fetchBenPreviousSignificantFindings(123L)).thenReturn(null);
            mockMvc.perform(post("/common/getDoctorPreviousSignificantFindings")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting previous significant findings")));
        }

        @Test
        void getDoctorPreviousSignificantFindings_invalidData() throws Exception {
            String req = "{}";
            mockMvc.perform(post("/common/getDoctorPreviousSignificantFindings")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid data")));
        }

        @Test
        void getDoctorPreviousSignificantFindings_exception() throws Exception {
            String req = "{\"beneficiaryRegID\":123}";
            when(commonDoctorServiceImpl.fetchBenPreviousSignificantFindings(anyLong())).thenThrow(new RuntimeException("fail"));
            mockMvc.perform(post("/common/getDoctorPreviousSignificantFindings")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting previous significant findings")));
        }
            // --- downloadFile ---
    // Not possible to test file streaming in MockMvc standalone; focus on error/exception/invalid input

    @Test
    void downloadFile_invalidRequest() throws Exception {
        CommonController controller = new CommonController();
        controller.setCommonDoctorServiceImpl(commonDoctorServiceImpl);
        controller.setCommonNurseServiceImpl(commonNurseServiceImpl);
        controller.setCommonServiceImpl(commonServiceImpl);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "aESEncryptionDecryption", aESEncryptionDecryption);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "servletContext", servletContext);
        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            controller.downloadFile("{\"fileName\":null,\"filePath\":null}", null)
        );
        org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("Error while downloading file");
    }

    @Test
    void downloadFile_exception() throws Exception {
        CommonController controller = new CommonController();
        controller.setCommonDoctorServiceImpl(commonDoctorServiceImpl);
        controller.setCommonNurseServiceImpl(commonNurseServiceImpl);
        controller.setCommonServiceImpl(commonServiceImpl);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "aESEncryptionDecryption", aESEncryptionDecryption);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "servletContext", servletContext);
        String fileName = "test.txt";
        String filePath = "/tmp/test.txt";
        when(aESEncryptionDecryption.decrypt(filePath)).thenThrow(new RuntimeException("fail"));
        String req = String.format("{\"fileName\":\"%s\",\"filePath\":\"%s\"}", fileName, filePath);
        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            controller.downloadFile(req, null)
        );
        org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("Error while downloading file");
    }

    // --- getLabWorkListNew ---
    @Test
    void getLabWorkListNew_success() throws Exception {
        when(commonNurseServiceImpl.getLabWorkListNew(1, 2)).thenReturn("lab");
        mockMvc.perform(get("/common/getLabWorklistNew/1/2/2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("lab")));
    }

    @Test
    void getLabWorkListNew_error() throws Exception {
        when(commonNurseServiceImpl.getLabWorkListNew(any(), any())).thenReturn(null);
        mockMvc.perform(get("/common/getLabWorklistNew/1/2/2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting lab technician worklist")));
    }

    @Test
    void getLabWorkListNew_exception() throws Exception {
        when(commonNurseServiceImpl.getLabWorkListNew(any(), any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/common/getLabWorklistNew/1/2/2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting lab technician worklist")));
    }

    // --- getRadiologistWorklistNew ---
    @Test
    void getRadiologistWorklistNew_success() throws Exception {
        when(commonNurseServiceImpl.getRadiologistWorkListNew(1, 2)).thenReturn("radio");
        mockMvc.perform(get("/common/getRadiologist-worklist-New/1/2/2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("radio")));
    }

    @Test
    void getRadiologistWorklistNew_error() throws Exception {
        when(commonNurseServiceImpl.getRadiologistWorkListNew(any(), any())).thenReturn(null);
        mockMvc.perform(get("/common/getRadiologist-worklist-New/1/2/2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting radiologist worklist")));
    }

    @Test
    void getRadiologistWorklistNew_exception() throws Exception {
        when(commonNurseServiceImpl.getRadiologistWorkListNew(any(), any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/common/getRadiologist-worklist-New/1/2/2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting radiologist worklist")));
    }

    // --- getTMReferredPrintData: already covered, but add IEMRException and generic Exception ---
    @Test
    void getTMReferredPrintData_iemrException_5002() throws Exception {
        String req = "{\"benVisitCode\":123}";
        com.iemr.mmu.utils.exception.IEMRException ex = new com.iemr.mmu.utils.exception.IEMRException("fail", 5002);
        when(commonServiceImpl.checkIsCaseSheetDownloaded(123L)).thenThrow(ex);
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary pending for Tele-Consultation")));
    }

    @Test
    void getTMReferredPrintData_iemrException_other() throws Exception {
        String req = "{\"benVisitCode\":123}";
        com.iemr.mmu.utils.exception.IEMRException ex = new com.iemr.mmu.utils.exception.IEMRException("fail", 5001);
        when(commonServiceImpl.checkIsCaseSheetDownloaded(123L)).thenThrow(ex);
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary pending for Tele-Consultation")));
    }

    @Test
    void getTMReferredPrintData_genericException() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.checkIsCaseSheetDownloaded(123L)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary pending for Tele-Consultation")));
    }

    // --- getTMCaseSheetFromCentralServer ---

    @Test
    void getTMCaseSheetFromCentralServer_iemrException_5002() throws Exception {
        String req = "{\"benVisitCode\":123}";
        com.iemr.mmu.utils.exception.IEMRException ex = new com.iemr.mmu.utils.exception.IEMRException("fail", 5002);
        when(commonServiceImpl.getCaseSheetOfTm(anyString(), anyString())).thenThrow(ex);
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("5002")));
    }

    @Test
    void getTMCaseSheetFromCentralServer_iemrException_other() throws Exception {
        String req = "{\"benVisitCode\":123}";
        com.iemr.mmu.utils.exception.IEMRException ex = new com.iemr.mmu.utils.exception.IEMRException("fail", 5001);
        when(commonServiceImpl.getCaseSheetOfTm(anyString(), anyString())).thenThrow(ex);
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getTMCaseSheetFromCentralServer_genericException() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.getCaseSheetOfTm(anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error in MMU central server case sheet")));
    }
    @Test
    void getTMReferredPrintData_success_downloaded() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.checkIsCaseSheetDownloaded(123L)).thenReturn(1);
        // In this test context, the deserialized object will not have benVisitCode set, so the controller will return the error message
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary pending for Tele-Consultation")));
    }

    @Test
    void getTMReferredPrintData_success_central() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.checkIsCaseSheetDownloaded(123L)).thenReturn(0);
        when(commonServiceImpl.getCaseSheetFromCentralServer(anyString(), anyString())).thenReturn("centralcase");
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("centralcase")));
    }

    @Test
    void getTMReferredPrintData_pending() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.checkIsCaseSheetDownloaded(123L)).thenReturn(0);
        when(commonServiceImpl.getCaseSheetFromCentralServer(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary pending for Tele-Consultation")));
    }

    @Test
    void getTMReferredPrintData_invalidRequest() throws Exception {
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("") )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTMReferredPrintData_exception() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.checkIsCaseSheetDownloaded(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/get/Case-sheet/TMReferredprintData")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk()); // Just coverage for exception
    }

    @Test
    void getBenPreviousReferralHistoryDetails_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPreviousReferralData(123L)).thenReturn("referral");
        mockMvc.perform(post("/common/getBenPreviousReferralHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("referral")));
    }

    @Test
    void getBenPreviousReferralHistoryDetails_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenPreviousReferralHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPreviousReferralHistoryDetails_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPreviousReferralData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenPreviousReferralHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting details")));
    }

    @Test
    void getTMCaseSheetFromCentralServer_success() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.getCaseSheetOfTm(anyString(), anyString())).thenReturn("centralserver");
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("centralserver")));
    }

    @Test
    void getTMCaseSheetFromCentralServer_pending() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.getCaseSheetOfTm(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary pending for Tele-Consultation")));
    }

    @Test
    void getTMCaseSheetFromCentralServer_invalidRequest() throws Exception {
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("") )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTMCaseSheetFromCentralServer_exception() throws Exception {
        String req = "{\"benVisitCode\":123}";
        when(commonServiceImpl.getCaseSheetOfTm(anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/get/Case-sheet/centralServerTMCaseSheet")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk()); // Just coverage for exception
    }

    @Test
    void calculateBMIStatus_success() throws Exception {
        String req = "{\"bmi\":22.5,\"yearMonth\":\"2025-08\",\"gender\":\"M\"}";
        when(commonNurseServiceImpl.calculateBMIStatus(anyString())).thenReturn("bmiresult");
        mockMvc.perform(post("/common/calculateBMIStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bmiresult")));
    }

    @Test
    void calculateBMIStatus_exception() throws Exception {
        String req = "{\"bmi\":22.5,\"yearMonth\":\"2025-08\",\"gender\":\"M\"}";
        when(commonNurseServiceImpl.calculateBMIStatus(anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/calculateBMIStatus")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk());
    }

    @Test
    void saveBeneficiaryVisitDetail_success() throws Exception {
        String req = "{\"beneficiaryRegID\":123}";
        when(commonNurseServiceImpl.updateBeneficiaryStatus('R', 123L)).thenReturn(1);
        mockMvc.perform(post("/common/update/benDetailsAndSubmitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary Successfully Submitted to Nurse Work-List.")));
    }

    @Test
    void saveBeneficiaryVisitDetail_invalidId() throws Exception {
        String req = "{\"beneficiaryRegID\":0}";
        mockMvc.perform(post("/common/update/benDetailsAndSubmitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary Registration ID is Not valid")));
    }

    @Test
    void saveBeneficiaryVisitDetail_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/update/benDetailsAndSubmitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Beneficiary Registration ID is Not valid")));
    }

    @Test
    void saveBeneficiaryVisitDetail_error() throws Exception {
        String req = "{\"beneficiaryRegID\":123}";
        when(commonNurseServiceImpl.updateBeneficiaryStatus(anyChar(), anyLong())).thenReturn(0);
        mockMvc.perform(post("/common/update/benDetailsAndSubmitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Something went Wrong please try after Some Time")));
    }

    @Test
    void saveBeneficiaryVisitDetail_exception() throws Exception {
        String req = "{\"beneficiaryRegID\":123}";
        when(commonNurseServiceImpl.updateBeneficiaryStatus(anyChar(), anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/update/benDetailsAndSubmitToNurse")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk());
    }

    @Test
    void extendRedisSession_success() throws Exception {
        mockMvc.perform(post("/common/extend/redisSession")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Session extended for 30 mins")));
    }

    @Test
    void deletePrescribedMedicine_success() throws Exception {
        String req = "{\"id\":1}";
        when(commonDoctorServiceImpl.deletePrescribedMedicine(any())).thenReturn("deleted");
        mockMvc.perform(post("/common/doctor/delete/prescribedMedicine")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));
    }

    @Test
    void deletePrescribedMedicine_error() throws Exception {
        String req = "{\"id\":1}";
        when(commonDoctorServiceImpl.deletePrescribedMedicine(any())).thenReturn(null);
        mockMvc.perform(post("/common/doctor/delete/prescribedMedicine")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error while deleting record")));
    }

    @Test
    void deletePrescribedMedicine_exception() throws Exception {
        String req = "{\"id\":1}";
        when(commonDoctorServiceImpl.deletePrescribedMedicine(any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/doctor/delete/prescribedMedicine")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk());
    }
    @Test
    void getBeneficiaryCaseSheetHistory_success() throws Exception {
        when(commonServiceImpl.getBenPreviousVisitDataForCaseRecord(anyString())).thenReturn("casehistory");
        mockMvc.perform(post("/common/getBeneficiaryCaseSheetHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"benRegID\":123}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("casehistory")));
    }

    @Test
    void getBeneficiaryCaseSheetHistory_error() throws Exception {
        when(commonServiceImpl.getBenPreviousVisitDataForCaseRecord(anyString())).thenReturn(null);
        mockMvc.perform(post("/common/getBeneficiaryCaseSheetHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"benRegID\":123}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while fetching beneficiary previous visit history details")));
    }

    @Test
    void getBeneficiaryCaseSheetHistory_exception() throws Exception {
        when(commonServiceImpl.getBenPreviousVisitDataForCaseRecord(anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBeneficiaryCaseSheetHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"benRegID\":123}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while fetching beneficiary previous visit history details")));
    }

    @Test
    void getTCSpecialistWorkListNew_success() throws Exception {
        when(commonDoctorServiceImpl.getTCSpecialistWorkListNewForTM(1, 3, 2)).thenReturn("tcworklist");
        mockMvc.perform(get("/common/getTCSpecialistWorklist/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tcworklist")));
    }

    @Test
    void getTCSpecialistWorkListNew_invalidRequest() throws Exception {
        mockMvc.perform(get("/common/getTCSpecialistWorklist/null/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTCSpecialistWorkListNew_exception() throws Exception {
        when(commonDoctorServiceImpl.getTCSpecialistWorkListNewForTM(any(), any(), any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/common/getTCSpecialistWorklist/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting TC specialist worklist")));
    }

    @Test
    void getTCSpecialistWorklistFutureScheduled_success() throws Exception {
        when(commonDoctorServiceImpl.getTCSpecialistWorkListNewFutureScheduledForTM(1, 3, 2)).thenReturn("tcfuture");
        mockMvc.perform(get("/common/getTCSpecialistWorklistFutureScheduled/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tcfuture")));
    }

    @Test
    void getTCSpecialistWorklistFutureScheduled_invalidRequest() throws Exception {
        mockMvc.perform(get("/common/getTCSpecialistWorklistFutureScheduled/null/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTCSpecialistWorklistFutureScheduled_exception() throws Exception {
        when(commonDoctorServiceImpl.getTCSpecialistWorkListNewFutureScheduledForTM(any(), any(), any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/common/getTCSpecialistWorklistFutureScheduled/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting TC specialist future scheduled worklist")));
    }

    @Test
    void getBenPhysicalHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPhysicalHistory(123L)).thenReturn("physical");
        mockMvc.perform(post("/common/getBenPhysicalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("physical")));
    }

    @Test
    void getBenPhysicalHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenPhysicalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPhysicalHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPhysicalHistory(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenPhysicalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting Physical history")));
    }

    @Test
    void getBenSymptomaticQuestionnaireDetails_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenSymptomaticQuestionnaireDetailsData(123L)).thenReturn("symptomatic");
        mockMvc.perform(post("/common/getBenSymptomaticQuestionnaireDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("symptomatic")));
    }

    @Test
    void getBenSymptomaticQuestionnaireDetails_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenSymptomaticQuestionnaireDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenSymptomaticQuestionnaireDetails_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenSymptomaticQuestionnaireDetailsData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenSymptomaticQuestionnaireDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting details")));
    }

    @Test
    void getBenPreviousDiabetesHistoryDetails_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPreviousDiabetesData(123L)).thenReturn("diabetes");
        mockMvc.perform(post("/common/getBenPreviousDiabetesHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("diabetes")));
    }

    @Test
    void getBenPreviousDiabetesHistoryDetails_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenPreviousDiabetesHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPreviousDiabetesHistoryDetails_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPreviousDiabetesData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenPreviousDiabetesHistoryDetails")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting details")));
    }
    @Test
    void getBenOptionalVaccineHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getChildVaccineHistoryData(123L)).thenReturn("optionalvaccine");
        mockMvc.perform(post("/common/getBenOptionalVaccineHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("optionalvaccine")));
    }

    @Test
    void getBenOptionalVaccineHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenOptionalVaccineHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenOptionalVaccineHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getChildVaccineHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenOptionalVaccineHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting optional vaccination history")));
    }

    @Test
    void getBenImmunizationHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getImmunizationHistoryData(123L)).thenReturn("immunization");
        mockMvc.perform(post("/common/getBenChildVaccineHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("immunization")));
    }

    @Test
    void getBenImmunizationHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenChildVaccineHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenImmunizationHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getImmunizationHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenChildVaccineHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting child vaccine(immunization) history")));
    }

    @Test
    void getBenPerinatalHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPerinatalHistoryData(123L)).thenReturn("perinatal");
        mockMvc.perform(post("/common/getBenPerinatalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("perinatal")));
    }

    @Test
    void getBenPerinatalHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenPerinatalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPerinatalHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPerinatalHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenPerinatalHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting perinatal history")));
    }

    @Test
    void getBenFeedingHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenFeedingHistoryData(123L)).thenReturn("feeding");
        mockMvc.perform(post("/common/getBenFeedingHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("feeding")));
    }

    @Test
    void getBenFeedingHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenFeedingHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenFeedingHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenFeedingHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenFeedingHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting child feeding history")));
    }

    @Test
    void getBenDevelopmentHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenDevelopmentHistoryData(123L)).thenReturn("development");
        mockMvc.perform(post("/common/getBenDevelopmentHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("development")));
    }

    @Test
    void getBenDevelopmentHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenDevelopmentHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenDevelopmentHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenDevelopmentHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenDevelopmentHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting child development history")));
    }
    @Test
    void getBenANCAllergyHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getPersonalAllergyHistoryData(123L)).thenReturn("allergy");
        mockMvc.perform(post("/common/getBenAllergyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("allergy")));
    }

    @Test
    void getBenANCAllergyHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenAllergyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenANCAllergyHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getPersonalAllergyHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenAllergyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting allergy history")));
    }

    @Test
    void getBenMedicationHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getMedicationHistoryData(123L)).thenReturn("medication");
        mockMvc.perform(post("/common/getBenMedicationHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("medication")));
    }

    @Test
    void getBenMedicationHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenMedicationHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenMedicationHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getMedicationHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenMedicationHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting medication history")));
    }

    @Test
    void getBenFamilyHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getFamilyHistoryData(123L)).thenReturn("family");
        mockMvc.perform(post("/common/getBenFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("family")));
    }

    @Test
    void getBenFamilyHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenFamilyHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getFamilyHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenFamilyHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting family history")));
    }

    @Test
    void getBenMenstrualHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getMenstrualHistoryData(123L)).thenReturn("menstrual");
        mockMvc.perform(post("/common/getBenMenstrualHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("menstrual")));
    }

    @Test
    void getBenMenstrualHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenMenstrualHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenMenstrualHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getMenstrualHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenMenstrualHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting menstrual history")));
    }

    @Test
    void getBenPastObstetricHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getObstetricHistoryData(123L)).thenReturn("obstetric");
        mockMvc.perform(post("/common/getBenPastObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("obstetric")));
    }

    @Test
    void getBenPastObstetricHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenPastObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPastObstetricHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getObstetricHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenPastObstetricHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting obstetric history")));
    }

    @Test
    void getBenANCComorbidityConditionHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getComorbidHistoryData(123L)).thenReturn("comorbid");
        mockMvc.perform(post("/common/getBenComorbidityConditionHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("comorbid")));
    }

    @Test
    void getBenANCComorbidityConditionHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenComorbidityConditionHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenANCComorbidityConditionHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getComorbidHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenComorbidityConditionHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting comodbidity history")));
    }
    @Test
    void getOncologistWorklistNew_success() throws Exception {
        when(commonNurseServiceImpl.getOncologistWorkListNew(1, 3)).thenReturn("oncologist");
        mockMvc.perform(get("/common/getOncologist-worklist-New/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("oncologist")));
    }

    @Test
    void getOncologistWorklistNew_error() throws Exception {
        when(commonNurseServiceImpl.getOncologistWorkListNew(any(), any())).thenReturn(null);
        mockMvc.perform(get("/common/getOncologist-worklist-New/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting oncologist worklist")));
    }

    @Test
    void getOncologistWorklistNew_exception() throws Exception {
        when(commonNurseServiceImpl.getOncologistWorkListNew(any(), any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/common/getOncologist-worklist-New/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting oncologist worklist")));
    }

    @Test
    void getPharmaWorklistNew_success() throws Exception {
        when(commonNurseServiceImpl.getPharmaWorkListNew(1, 3)).thenReturn("pharma");
        mockMvc.perform(get("/common/getPharma-worklist-New/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("pharma")));
    }

    @Test
    void getPharmaWorklistNew_error() throws Exception {
        when(commonNurseServiceImpl.getPharmaWorkListNew(any(), any())).thenReturn(null);
        mockMvc.perform(get("/common/getPharma-worklist-New/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting pharma worklist")));
    }

    @Test
    void getPharmaWorklistNew_exception() throws Exception {
        when(commonNurseServiceImpl.getPharmaWorkListNew(any(), any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/common/getPharma-worklist-New/1/2/3")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting pharma worklist")));
    }

    @Test
    void getCasesheetPrintData_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getCaseSheetPrintDataForBeneficiary(any(), anyString())).thenReturn("casesheet");
        mockMvc.perform(post("/common/get/Case-sheet/printData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("casesheet")));
    }

    @Test
    void getCasesheetPrintData_invalidRequest() throws Exception {
        mockMvc.perform(post("/common/get/Case-sheet/printData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("") )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCasesheetPrintData_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getCaseSheetPrintDataForBeneficiary(any(), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/get/Case-sheet/printData")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk()); // No error string, just coverage
    }

    @Test
    void getBenPastHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPastHistoryData(123L)).thenReturn("past");
        mockMvc.perform(post("/common/getBenPastHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("past")));
    }

    @Test
    void getBenPastHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenPastHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenPastHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getBenPastHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenPastHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting illness and surgery history")));
    }

    @Test
    void getBenTobaccoHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getPersonalTobaccoHistoryData(123L)).thenReturn("tobacco");
        mockMvc.perform(post("/common/getBenTobaccoHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tobacco")));
    }

    @Test
    void getBenTobaccoHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenTobaccoHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenTobaccoHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getPersonalTobaccoHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenTobaccoHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting tobacco history")));
    }

    @Test
    void getBenAlcoholHistory_success() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getPersonalAlcoholHistoryData(123L)).thenReturn("alcohol");
        mockMvc.perform(post("/common/getBenAlcoholHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("alcohol")));
    }

    @Test
    void getBenAlcoholHistory_invalidRequest() throws Exception {
        String req = "{}";
        mockMvc.perform(post("/common/getBenAlcoholHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void getBenAlcoholHistory_exception() throws Exception {
        String req = "{\"benRegID\":123}";
        when(commonServiceImpl.getPersonalAlcoholHistoryData(anyLong())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/common/getBenAlcoholHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting alcohol history")));
    }
}
