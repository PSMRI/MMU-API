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
package com.iemr.mmu.controller.common.master;

import com.iemr.mmu.service.common.master.CommonMasterServiceImpl;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CommonMasterControllerTest {
    private MockMvc mockMvc;

    @Mock
    private CommonMasterServiceImpl commonMasterServiceImpl;

    @InjectMocks
    private CommonMasterController commonMasterController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commonMasterController).build();
    }

    @Test
    void testGetVisitReasonAndCategories() throws Exception {
        when(commonMasterServiceImpl.getVisitReasonAndCategories()).thenReturn("reasons-categories");
        mockMvc.perform(MockMvcRequestBuilders.get("/master/get/visitReasonAndCategories")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("reasons-categories")));
    }

    @Test
    void testNurseMasterData() throws Exception {
        when(commonMasterServiceImpl.getMasterDataForNurse(anyInt(), anyInt(), anyString())).thenReturn("nurse-master");
        mockMvc.perform(MockMvcRequestBuilders.get("/master/nurse/masterData/1/2/M")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("nurse-master")));
    }

    @Test
    void testDoctorMasterData() throws Exception {
        when(commonMasterServiceImpl.getMasterDataForDoctor(anyInt(), anyInt(), anyString(), anyInt(), anyInt())).thenReturn("doctor-master");
        mockMvc.perform(MockMvcRequestBuilders.get("/master/doctor/masterData/1/2/M/3/4")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("doctor-master")));
    }

    @Test
    void testGetECGAbnormalities_success() throws Exception {
        when(commonMasterServiceImpl.getECGAbnormalities()).thenReturn("ecg-data");
        mockMvc.perform(MockMvcRequestBuilders.get("/master/ecgAbnormalities")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("ecg-data")));
    }

    @Test
    void testGetECGAbnormalities_exception() throws Exception {
        when(commonMasterServiceImpl.getECGAbnormalities()).thenThrow(new RuntimeException("fail-ecg"));
        mockMvc.perform(MockMvcRequestBuilders.get("/master/ecgAbnormalities")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("fail-ecg")));
    }
}
