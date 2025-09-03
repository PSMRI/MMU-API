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
package com.iemr.mmu.controller.location;

import com.iemr.mmu.service.location.LocationServiceImpl;
import org.json.JSONObject;
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

class LocationControllerTest {
    private MockMvc mockMvc;

    @Mock
    private LocationServiceImpl locationServiceImpl;

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    @Test
    void testGetStateMaster_success() throws Exception {
        when(locationServiceImpl.getStateList()).thenReturn("state-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/stateMaster")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("state-list")));
    }

    @Test
    void testGetStateMaster_error() throws Exception {
        when(locationServiceImpl.getStateList()).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/stateMaster")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting states")));
    }

    @Test
    void testGetCountryMaster_success() throws Exception {
        when(locationServiceImpl.getCountryList()).thenReturn("country-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/countryMaster")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("country-list")));
    }

    @Test
    void testGetCountryMaster_error() throws Exception {
        when(locationServiceImpl.getCountryList()).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/countryMaster")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting country")));
    }

    @Test
    void testGetCountryCityMaster_success() throws Exception {
        when(locationServiceImpl.getCountryCityList(anyInt())).thenReturn("city-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/countryCityMaster/1/")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("city-list")));
    }

    @Test
    void testGetCountryCityMaster_error() throws Exception {
        when(locationServiceImpl.getCountryCityList(anyInt())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/countryCityMaster/1/")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting country city")));
    }

    @Test
    void testGetDistrictMaster_success() throws Exception {
        when(locationServiceImpl.getDistrictList(anyInt())).thenReturn("district-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/districtMaster/2")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("district-list")));
    }

    @Test
    void testGetDistrictMaster_error() throws Exception {
        when(locationServiceImpl.getDistrictList(anyInt())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/districtMaster/2")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting districts")));
    }

    @Test
    void testGetDistrictBlockMaster_success() throws Exception {
        when(locationServiceImpl.getDistrictBlockList(anyInt())).thenReturn("block-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/districtBlockMaster/3")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("block-list")));
    }

    @Test
    void testGetDistrictBlockMaster_error() throws Exception {
        when(locationServiceImpl.getDistrictBlockList(anyInt())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/districtBlockMaster/3")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting district blocks")));
    }

    @Test
    void testGetVillageMaster_success() throws Exception {
        when(locationServiceImpl.getVillageMasterFromBlockID(anyInt())).thenReturn("village-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/villageMasterFromBlockID/4")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("village-list")));
    }

    @Test
    void testGetVillageMaster_error() throws Exception {
        when(locationServiceImpl.getVillageMasterFromBlockID(anyInt())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/villageMasterFromBlockID/4")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting villages")));
    }

    @Test
    void testGetLocDetailsBasedOnSpIDAndPsmIDNew_success() throws Exception {
    when(locationServiceImpl.getLocDetailsNew(anyInt(), anyInt(), org.mockito.ArgumentMatchers.any())).thenReturn("loc-details");
        String body = "{\"spID\":1,\"spPSMID\":2,\"userId\":3}";
        mockMvc.perform(MockMvcRequestBuilders.post("/location/getLocDetailsBasedOnSpIDAndPsmID")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("loc-details")));
    }

    @Test
    void testGetLocDetailsBasedOnSpIDAndPsmIDNew_invalidRequest() throws Exception {
        String body = "{\"foo\":1}";
        mockMvc.perform(MockMvcRequestBuilders.post("/location/getLocDetailsBasedOnSpIDAndPsmID")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetLocDetailsBasedOnSpIDAndPsmIDNew_exception() throws Exception {
        String body = "not a json";
        mockMvc.perform(MockMvcRequestBuilders.post("/location/getLocDetailsBasedOnSpIDAndPsmID")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting location data")));
    }

    @Test
    void testGetDistrictTalukMaster_success() throws Exception {
        when(locationServiceImpl.getDistrictTalukList(anyInt())).thenReturn("taluk-list");
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/DistrictTalukMaster/5")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("taluk-list")));
    }

    @Test
    void testGetDistrictTalukMaster_error() throws Exception {
        when(locationServiceImpl.getDistrictTalukList(anyInt())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/get/DistrictTalukMaster/5")
                .header("Authorization", "Bearer token")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Error while getting district blocks")));
    }
}
