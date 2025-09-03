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
package com.iemr.mmu.controller.dataSyncLayerCentral;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncDownloadMaster;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.service.dataSyncLayerCentral.FetchDownloadDataImpl;
import com.iemr.mmu.service.dataSyncLayerCentral.GetDataFromVanAndSyncToDBImpl;
import com.iemr.mmu.service.dataSyncLayerCentral.GetMasterDataFromCentralForVanImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MMUDataSyncVanToServerTest {
    private MockMvc mockMvc;

    @Mock
    private GetDataFromVanAndSyncToDBImpl getDataFromVanAndSyncToDBImpl;
    @Mock
    private GetMasterDataFromCentralForVanImpl getMasterDataFromCentralForVanImpl;
    @Mock
    private FetchDownloadDataImpl fetchDownloadDataImpl;

    @InjectMocks
    private MMUDataSyncVanToServer controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testDataSyncToServer_success() throws Exception {
        when(getDataFromVanAndSyncToDBImpl.syncDataToServer(anyString(), anyString())).thenReturn("ok");
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/van-to-server")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("ok")));
    }

    @Test
    void testDataSyncToServer_null() throws Exception {
        when(getDataFromVanAndSyncToDBImpl.syncDataToServer(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/van-to-server")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("data dync failed")));
    }

    @Test
    void testDataSyncToServer_exception() throws Exception {
        when(getDataFromVanAndSyncToDBImpl.syncDataToServer(anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/van-to-server")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testDataDownloadFromServer_success() throws Exception {
        when(getMasterDataFromCentralForVanImpl.getMasterDataForVan(any(SyncDownloadMaster.class))).thenReturn("download");
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"schemaName\":\"s\",\"tableName\":\"t\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("download")));
    }

    @Test
    void testDataDownloadFromServer_null() throws Exception {
        when(getMasterDataFromCentralForVanImpl.getMasterDataForVan(any(SyncDownloadMaster.class))).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"schemaName\":\"s\",\"tableName\":\"t\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in master download")));
    }

    @Test
    void testDataDownloadFromServer_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in master download for table null.null")));
    }

    @Test
    void testDataDownloadFromServer_exception() throws Exception {
        when(getMasterDataFromCentralForVanImpl.getMasterDataForVan(any(SyncDownloadMaster.class))).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"schemaName\":\"s\",\"tableName\":\"t\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testDataDownloadFromServerTransactional_success() throws Exception {
        when(fetchDownloadDataImpl.getDownloadData(any(SyncUploadDataDigester.class))).thenReturn("trans-download");
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van-transactional")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"schemaName\":\"s\",\"tableName\":\"t\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("trans-download")));
    }

    @Test
    void testDataDownloadFromServerTransactional_null() throws Exception {
        when(fetchDownloadDataImpl.getDownloadData(any(SyncUploadDataDigester.class))).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van-transactional")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"schemaName\":\"s\",\"tableName\":\"t\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in data download")));
    }

    @Test
    void testDataDownloadFromServerTransactional_invalidRequest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van-transactional")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("{}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error in data download for table null.null")));
    }

    @Test
    void testDataDownloadFromServerTransactional_exception() throws Exception {
        when(fetchDownloadDataImpl.getDownloadData(any(SyncUploadDataDigester.class))).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/server-to-van-transactional")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"schemaName\":\"s\",\"tableName\":\"t\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testUpdateProcessedFlagPostDownload_success() throws Exception {
    when(fetchDownloadDataImpl.updateProcessedFlagPostSuccessfullDownload(any(SyncUploadDataDigester.class))).thenReturn(1);
    mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/updateProcessedFlagPostDownload")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("{\"schemaName\":\"s\",\"tableName\":\"t\",\"vanAutoIncColumnName\":\"v\",\"serverColumns\":\"c\",\"syncedBy\":\"user\",\"facilityID\":1,\"syncData\":[],\"ids\":[1,2]}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("success")));
    }

    @Test
    void testUpdateProcessedFlagPostDownload_error() throws Exception {
    when(fetchDownloadDataImpl.updateProcessedFlagPostSuccessfullDownload(any(SyncUploadDataDigester.class))).thenReturn(0);
    mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/updateProcessedFlagPostDownload")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("{\"schemaName\":\"s\",\"tableName\":\"t\",\"vanAutoIncColumnName\":\"v\",\"serverColumns\":\"c\",\"syncedBy\":\"user\",\"facilityID\":1,\"syncData\":[],\"ids\":[1,2]}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while updating flag. Please contact administrator s.t.[1, 2]")));
    }

    @Test
    void testUpdateProcessedFlagPostDownload_invalidRequest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/updateProcessedFlagPostDownload")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("{}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while updating flag. Please contact administrator null.null.null")));
    }

    @Test
    void testUpdateProcessedFlagPostDownload_exception() throws Exception {
    when(fetchDownloadDataImpl.updateProcessedFlagPostSuccessfullDownload(any(SyncUploadDataDigester.class))).thenThrow(new RuntimeException("fail"));
    mockMvc.perform(MockMvcRequestBuilders.post("/dataSync/updateProcessedFlagPostDownload")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("{\"schemaName\":\"s\",\"tableName\":\"t\",\"vanAutoIncColumnName\":\"v\",\"serverColumns\":\"c\",\"syncedBy\":\"user\",\"facilityID\":1,\"syncData\":[],\"ids\":[1,2]}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail")));
    }
}
