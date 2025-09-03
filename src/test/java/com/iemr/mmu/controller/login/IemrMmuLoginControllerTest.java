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
package com.iemr.mmu.controller.login;

import com.iemr.mmu.service.login.IemrMmuLoginServiceImpl;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class IemrMmuLoginControllerTest {
    private MockMvc mockMvc;

    @Mock
    private IemrMmuLoginServiceImpl iemrMmuLoginServiceImpl;

    @InjectMocks
    private IemrMmuLoginController iemrMmuLoginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(iemrMmuLoginController).build();
    }

    @Test
    void testGetUserServicePointVanDetails_success() throws Exception {
        when(iemrMmuLoginServiceImpl.getUserServicePointVanDetails(anyInt())).thenReturn("van-details");
        mockMvc.perform(MockMvcRequestBuilders.get("/user/getUserServicePointVanDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"userID\":1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("van-details")));
    }

    @Test
    void testGetUserServicePointVanDetails_exception() throws Exception {
        when(iemrMmuLoginServiceImpl.getUserServicePointVanDetails(anyInt())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.get("/user/getUserServicePointVanDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"userID\":1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting service points and van data")));
    }

    @Test
    void testGetServicepointVillages_success() throws Exception {
        when(iemrMmuLoginServiceImpl.getServicepointVillages(anyInt())).thenReturn("village-details");
        mockMvc.perform(MockMvcRequestBuilders.post("/user/getServicepointVillages")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"servicePointID\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("village-details")));
    }

    @Test
    void testGetServicepointVillages_exception() throws Exception {
        when(iemrMmuLoginServiceImpl.getServicepointVillages(anyInt())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/user/getServicepointVillages")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"servicePointID\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting service points and villages")));
    }

    @Test
    void testGetUserVanSpDetails_success() throws Exception {
        when(iemrMmuLoginServiceImpl.getUserVanSpDetails(anyInt(), anyInt())).thenReturn("van-sp-details");
        mockMvc.perform(MockMvcRequestBuilders.post("/user/getUserVanSpDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"userID\":1,\"providerServiceMapID\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("van-sp-details")));
    }

    @Test
    void testGetUserVanSpDetails_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/getUserVanSpDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"userID\":1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetUserVanSpDetails_exception() throws Exception {
        when(iemrMmuLoginServiceImpl.getUserVanSpDetails(anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.post("/user/getUserVanSpDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content("{\"userID\":1,\"providerServiceMapID\":2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting van and service points data")));
    }

    @Test
    void testGetVanMaster_success() throws Exception {
        when(iemrMmuLoginServiceImpl.getVanMaster(anyInt())).thenReturn("van-master");
        mockMvc.perform(MockMvcRequestBuilders.get("/user/getVanMaster/5")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("van-master")));
    }

    @Test
    void testGetVanMaster_invalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/getVanMaster/")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()); // PathVariable missing
    }

    @Test
    void testGetVanMaster_exception() throws Exception {
        when(iemrMmuLoginServiceImpl.getVanMaster(anyInt())).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(MockMvcRequestBuilders.get("/user/getVanMaster/5")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error occurred while fetching van master")));
    }
}
