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
package com.iemr.mmu.controller.dataSyncActivity;

import com.iemr.mmu.service.dataSyncActivity.DownloadDataFromServerImpl;
import com.iemr.mmu.service.dataSyncActivity.DownloadDataFromServerTransactionalImpl;
import com.iemr.mmu.service.dataSyncActivity.UploadDataToServerImpl;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.response.OutputResponse;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.util.Map;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class StartSyncActivityTest {
    private MockMvc mockMvc;

    @Mock
    private UploadDataToServerImpl uploadDataToServerImpl;
    @Mock
    private DownloadDataFromServerImpl downloadDataFromServerImpl;
    @Mock
    private DownloadDataFromServerTransactionalImpl downloadDataFromServerTransactionalImpl;
    @Mock
    private Logger logger;

    @InjectMocks
    private StartSyncActivity startSyncActivity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(startSyncActivity).build();
    }

    @Test
    void testStartSyncActivity_success() throws Exception {
        String requestJson = "{\"vanID\":1,\"user\":\"testUser\",\"groupID\":2}";
            try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(uploadDataToServerImpl.getDataToSyncToServer(eq(1), eq("testUser"), anyString(), anyString())).thenReturn("success");
        mockMvc.perform(post("/dataSyncActivity/van-to-server")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("success")));
    }
}

    @Test
    void testStartSyncActivity_missingFields() throws Exception {
        String requestJson = "{\"vanID\":1}";
    mockMvc.perform(post("/dataSyncActivity/van-to-server")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth")
        .header("ServerAuthorization", "serverAuth")
        .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testStartSyncActivity_exception() throws Exception {
        String requestJson = "{\"vanID\":1,\"user\":\"testUser\",\"groupID\":2}";
            try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenThrow(new RuntimeException("fail"));
    mockMvc.perform(post("/dataSyncActivity/van-to-server")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth")
        .header("ServerAuthorization", "serverAuth")
        .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }
}

    @Test
    void testGetSyncGroupDetails_success() throws Exception {
        when(uploadDataToServerImpl.getDataSyncGroupDetails()).thenReturn("groupDetails");
        mockMvc.perform(get("/dataSyncActivity/getSyncGroupDetails")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.response").value("groupDetails"));
    }

    @Test
    void testGetSyncGroupDetails_null() throws Exception {
        when(uploadDataToServerImpl.getDataSyncGroupDetails()).thenReturn(null);
        mockMvc.perform(get("/dataSyncActivity/getSyncGroupDetails")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Error in getting data sync group details")));
    }

    @Test
    void testGetSyncGroupDetails_exception() throws Exception {
        when(uploadDataToServerImpl.getDataSyncGroupDetails()).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/dataSyncActivity/getSyncGroupDetails")
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testStartMasterDownload_success() throws Exception {
        String requestJson = "{\"vanID\":1,\"providerServiceMapID\":2}";
            try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerImpl.downloadMasterDataFromServer(anyString(), anyString(), eq(1), eq(2))).thenReturn("masterData");
            mockMvc.perform(post("/dataSyncActivity/startMasterDownload")
                .contentType("application/json")
                .header("Authorization", "auth")
                .header("ServerAuthorization", "serverAuth")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("masterData")));
    }
}

    @Test
    void testStartMasterDownload_inProgress() throws Exception {
    String requestJson = "{\"vanID\":1,\"providerServiceMapID\":2}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerImpl.downloadMasterDataFromServer(anyString(), anyString(), eq(1), eq(2))).thenReturn("inProgress");
        mockMvc.perform(post("/dataSyncActivity/startMasterDownload")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Download is already in progress")));
    }
}

    @Test
    void testStartMasterDownload_null() throws Exception {
        String requestJson = "{\"vanID\":1,\"providerServiceMapID\":2}";
        try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
            cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
            when(downloadDataFromServerImpl.downloadMasterDataFromServer(anyString(), anyString(), eq(1), eq(2))).thenReturn(null);
            mockMvc.perform(post("/dataSyncActivity/startMasterDownload")
                    .contentType("application/json")
                    .accept("application/json")
                    .header("Authorization", "auth")
                    .header("ServerAuthorization", "serverAuth")
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Test
    void testStartMasterDownload_missingFields() throws Exception {
    String requestJson = "{\"vanID\":1}";
    mockMvc.perform(post("/dataSyncActivity/startMasterDownload")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth")
        .header("ServerAuthorization", "serverAuth")
        .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("vanID / providerServiceMapID or both are missing")));
    }

    @Test
    void testStartMasterDownload_exception() throws Exception {
    String requestJson = "{\"vanID\":1,\"providerServiceMapID\":2}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/dataSyncActivity/startMasterDownload")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("fail")));
    }
}

    @Test
    void testCheckMastersDownloadProgress_success() throws Exception {
    Map<String, Object> statusMap = new java.util.HashMap<>();
    statusMap.put("status", "status");
    when(downloadDataFromServerImpl.getDownloadStatus()).thenReturn(statusMap);
    mockMvc.perform(get("/dataSyncActivity/checkMastersDownloadProgress")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("status"));
    }

    @Test
    void testCheckMastersDownloadProgress_exception() throws Exception {
    when(downloadDataFromServerImpl.getDownloadStatus()).thenThrow(new RuntimeException("fail"));
    mockMvc.perform(get("/dataSyncActivity/checkMastersDownloadProgress")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testGetVanDetailsForMasterDownload_success() throws Exception {
    when(downloadDataFromServerImpl.getVanDetailsForMasterDownload()).thenReturn("vanDetails");
    mockMvc.perform(get("/dataSyncActivity/getVanDetailsForMasterDownload")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.response").value("vanDetails"));
    }

    @Test
    void testGetVanDetailsForMasterDownload_null() throws Exception {
    when(downloadDataFromServerImpl.getVanDetailsForMasterDownload()).thenReturn(null);
    mockMvc.perform(get("/dataSyncActivity/getVanDetailsForMasterDownload")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Error while getting van details")));
    }

    @Test
    void testGetVanDetailsForMasterDownload_exception() throws Exception {
    when(downloadDataFromServerImpl.getVanDetailsForMasterDownload()).thenThrow(new RuntimeException("fail"));
    mockMvc.perform(get("/dataSyncActivity/getVanDetailsForMasterDownload")
        .contentType("application/json")
        .accept("application/json")
        .header("Authorization", "auth"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testCallCentralAPIToGenerateBenIDAndimportToLocal_error0() throws Exception {
    String requestJson = "{}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerImpl.callCentralAPIToGenerateBenIDAndimportToLocal(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        mockMvc.perform(post("/dataSyncActivity/callCentralAPIToGenerateBenIDAndimportToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Error while generating UNIQUE_ID at central server")));
    }
}

    @Test
    void testCallCentralAPIToGenerateBenIDAndimportToLocal_error1() throws Exception {
    String requestJson = "{}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerImpl.callCentralAPIToGenerateBenIDAndimportToLocal(anyString(), anyString(), anyString(), anyString())).thenReturn(1);
        mockMvc.perform(post("/dataSyncActivity/callCentralAPIToGenerateBenIDAndimportToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("UNIQUE_ID generated successfully, but error while importing to local")));
    }
}

    @Test
    void testCallCentralAPIToGenerateBenIDAndimportToLocal_success() throws Exception {
    String requestJson = "{}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerImpl.callCentralAPIToGenerateBenIDAndimportToLocal(anyString(), anyString(), anyString(), anyString())).thenReturn(2);
        mockMvc.perform(post("/dataSyncActivity/callCentralAPIToGenerateBenIDAndimportToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.response").value("UNIQUE_ID generated and imported successfully"));
    }
}

    @Test
    void testCallCentralAPIToGenerateBenIDAndimportToLocal_exception() throws Exception {
    String requestJson = "{}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/dataSyncActivity/callCentralAPIToGenerateBenIDAndimportToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "auth")
            .header("ServerAuthorization", "serverAuth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("fail")));
    }
}

    @Test
    void testDownloadTransactionToLocal_success() throws Exception {
    String requestJson = "{\"vanID\":1}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerTransactionalImpl.downloadTransactionalData(eq(1), anyString(), anyString())).thenReturn(1);
        mockMvc.perform(post("/dataSyncActivity/downloadTransactionToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("ServerAuthorization", "serverAuth")
            .header("Authorization", "auth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.response").value("Success"));
    }
}

    @Test
    void testDownloadTransactionToLocal_failure() throws Exception {
    String requestJson = "{\"vanID\":1}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenReturn("jwtToken");
        when(downloadDataFromServerTransactionalImpl.downloadTransactionalData(eq(1), anyString(), anyString())).thenReturn(0);
        mockMvc.perform(post("/dataSyncActivity/downloadTransactionToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("ServerAuthorization", "serverAuth")
            .header("Authorization", "auth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Issue in download")));
    }
}

    @Test
    void testDownloadTransactionToLocal_exception() throws Exception {
    String requestJson = "{\"vanID\":1}";
    try (MockedStatic<CookieUtil> cookieUtilMockedStatic = org.mockito.Mockito.mockStatic(CookieUtil.class)) {
        cookieUtilMockedStatic.when(() -> CookieUtil.getJwtTokenFromCookie(any())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(post("/dataSyncActivity/downloadTransactionToLocal")
            .contentType("application/json")
            .accept("application/json")
            .header("ServerAuthorization", "serverAuth")
            .header("Authorization", "auth")
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("fail")));
    }
}

}

