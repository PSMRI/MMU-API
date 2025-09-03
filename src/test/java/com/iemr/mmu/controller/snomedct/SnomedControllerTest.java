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
package com.iemr.mmu.controller.snomedct;

import com.google.gson.Gson;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.service.snomedct.SnomedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnomedControllerTest {
    private MockMvc mockMvc;

    @Mock
    private SnomedService snomedService;

    @InjectMocks
    private SnomedController snomedController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(snomedController).build();
    }

    @Test
    void testGetSnomedCTRecord_found() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        SCTDescription resp = new SCTDescription();
        resp.setTerm("test-term");
        resp.setConceptID("123");
        when(snomedService.findSnomedCTRecordFromTerm(anyString())).thenReturn(resp);
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecord")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("123")));
    }

    @Test
    void testGetSnomedCTRecord_notFound() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        when(snomedService.findSnomedCTRecordFromTerm(anyString())).thenReturn(null);
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecord")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("No Records Found")));
    }

    @Test
    void testGetSnomedCTRecord_nullConceptId() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        SCTDescription resp = new SCTDescription();
        resp.setTerm("test-term");
        resp.setConceptID(null);
        when(snomedService.findSnomedCTRecordFromTerm(anyString())).thenReturn(resp);
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecord")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("No Records Found")));
    }

    @Test
    void testGetSnomedCTRecord_exception() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        when(snomedService.findSnomedCTRecordFromTerm(anyString())).thenThrow(new RuntimeException("fail"));
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecord")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void testGetSnomedCTRecordList_found() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        when(snomedService.findSnomedCTRecordList(any(SCTDescription.class))).thenReturn("list-data");
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecordList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("list-data")));
    }

    @Test
    void testGetSnomedCTRecordList_notFound() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        when(snomedService.findSnomedCTRecordList(any(SCTDescription.class))).thenReturn(null);
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecordList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("No Records Found")));
    }

    @Test
    void testGetSnomedCTRecordList_exception() throws Exception {
        SCTDescription req = new SCTDescription();
        req.setTerm("test-term");
        when(snomedService.findSnomedCTRecordList(any(SCTDescription.class))).thenThrow(new RuntimeException("fail-list"));
        String json = new Gson().toJson(req);
        mockMvc.perform(MockMvcRequestBuilders.post("/snomed/getSnomedCTRecordList")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail-list")));
    }
}
