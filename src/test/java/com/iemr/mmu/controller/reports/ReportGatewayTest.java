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
package com.iemr.mmu.controller.reports;

import com.iemr.mmu.service.reports.ReportCheckPostImpl;
import com.iemr.mmu.service.reports.ReportCheckPostImplNew;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ReportGatewayTest {
    @Test
    void testGetReportByReportID_nullBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/report/getReport")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetReportByReportID1_nullBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/report/getReportNew")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }
    private MockMvc mockMvc;

    @Mock
    private ReportCheckPostImpl reportCheckPostImpl;
    @Mock
    private ReportCheckPostImplNew reportCheckPostImplNew;

    @InjectMocks
    private ReportGateway reportGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reportGateway).build();
    }

    @Test
    void testGetReportByReportID_success() throws Exception {
        when(reportCheckPostImpl.reportHandler(anyString())).thenReturn("report-data");
        mockMvc.perform(MockMvcRequestBuilders.post("/report/getReport")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"key\":\"value\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("report-data")));
    }

    @Test
    void testGetReportByReportID_invalidRequest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/report/getReport")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("null"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error occurred while fetching report is")));
    }

    @Test
    void testGetReportByReportID_exception() throws Exception {
        when(reportCheckPostImpl.reportHandler(anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/report/getReport")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"key\":\"value\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error occurred while fetching report is")));
    }

    @Test
    void testGetReportByReportID1_success() throws Exception {
        when(reportCheckPostImplNew.reportHandler(anyString())).thenReturn("report-data-new");
        mockMvc.perform(MockMvcRequestBuilders.post("/report/getReportNew")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"key\":\"value\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("report-data-new")));
    }

    @Test
    void testGetReportByReportID1_invalidRequest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/report/getReportNew")
        .header("Authorization", "Bearer token")
        .contentType("application/json")
        .content("null"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error occurred while fetching report is")));
    }

    @Test
    void testGetReportByReportID1_exception() throws Exception {
        when(reportCheckPostImplNew.reportHandler(anyString())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/report/getReportNew")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"key\":\"value\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error occurred while fetching report is")));
    }

    @Test
    void testGetReportMaster_success() throws Exception {
        when(reportCheckPostImpl.getReportMaster(anyInt())).thenReturn("master-data");
        mockMvc.perform(MockMvcRequestBuilders.get("/report/getReportMaster/1")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("master-data")));
    }

    @Test
    void testGetReportMaster_null() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/report/getReportMaster/")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    void testGetReportMaster_error() throws Exception {
        when(reportCheckPostImpl.getReportMaster(anyInt())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/report/getReportMaster/1")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while fetching report master data")));
    }

    @Test
    void testGetReportMaster_exception() throws Exception {
        when(reportCheckPostImpl.getReportMaster(anyInt())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.get("/report/getReportMaster/1")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while fetching report master data is")));
    }
}
