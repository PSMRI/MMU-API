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

import com.iemr.mmu.service.common.transaction.CommonServiceImpl;
import com.iemr.mmu.data.common.DocFileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InsertCommonControllerTest {
    private MockMvc mockMvc;
    @Mock
    private CommonServiceImpl commonServiceImpl;
    @InjectMocks
    private InsertCommonController insertCommonController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(insertCommonController).build();
    }

    @Test
    void saveFiles_success() throws Exception {
        when(commonServiceImpl.saveFiles(anyList())).thenReturn("saved");
        String req = "[{\"fileName\":\"doc1.pdf\"}]";
        mockMvc.perform(post("/commonInsert/saveFiles")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("saved")));
    }

    @Test
    void saveFiles_nullResponse() throws Exception {
        when(commonServiceImpl.saveFiles(anyList())).thenReturn(null);
        String req = "[{\"fileName\":\"doc1.pdf\"}]";
        mockMvc.perform(post("/commonInsert/saveFiles")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("saved"))));
    }

    @Test
    void saveFiles_exception() throws Exception {
        when(commonServiceImpl.saveFiles(anyList())).thenThrow(new RuntimeException("fail"));
        String req = "[{\"fileName\":\"doc1.pdf\"}]";
        mockMvc.perform(post("/commonInsert/saveFiles")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while saving files")));
    }
}
