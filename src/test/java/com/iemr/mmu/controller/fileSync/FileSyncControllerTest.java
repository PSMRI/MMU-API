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
package com.iemr.mmu.controller.fileSync;

import com.iemr.mmu.service.fileSync.FileSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FileSyncControllerTest {
    private MockMvc mockMvc;

    @Mock
    private FileSyncService fileSyncService;

    @InjectMocks
    private FileSyncController fileSyncController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fileSyncController).build();
    }

    @Test
    void testGetServerCredential_success() throws Exception {
        when(fileSyncService.getServerCredential()).thenReturn("server-cred");
        mockMvc.perform(MockMvcRequestBuilders.get("/fileSyncController/getServerCredential")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("server-cred")));
    }

    @Test
    void testGetServerCredential_exception() throws Exception {
        when(fileSyncService.getServerCredential()).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.get("/fileSyncController/getServerCredential")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testSyncFiles_success() throws Exception {
        when(fileSyncService.syncFiles(anyString())).thenReturn("sync-success");
        mockMvc.perform(MockMvcRequestBuilders.get("/fileSyncController/syncFiles")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-auth")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("sync-success")));
    }

    @Test
    void testSyncFiles_exception() throws Exception {
        when(fileSyncService.syncFiles(anyString())).thenThrow(new RuntimeException("sync-fail"));
        mockMvc.perform(MockMvcRequestBuilders.get("/fileSyncController/syncFiles")
                .header("Authorization", "Bearer token")
                .header("ServerAuthorization", "server-auth")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("sync-fail")));
    }
}
