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
package com.iemr.mmu.controller.nurse.vitals;

import com.iemr.mmu.service.nurse.vitals.AnthropometryVitalsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnthropometryVitalsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnthropometryVitalsService anthropometryVitalsService;

    @InjectMocks
    private AnthropometryVitalsController anthropometryVitalsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(anthropometryVitalsController).build();
    }

    @Test
    void testGetBenHeightDetailsFrmNurse_Success() throws Exception {
        Mockito.when(anthropometryVitalsService.getBeneficiaryHeightDetails(anyLong()))
                .thenReturn("height details");

        String requestJson = "{\"benRegID\":123}";

        mockMvc.perform(post("/anthropometryVitals/getBenHeightDetailsFrmNurse")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer testtoken")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("height details")));
    }

    @Test
    void testGetBenHeightDetailsFrmNurse_InvalidRequest() throws Exception {
        String requestJson = "{\"invalidKey\":123}";

        mockMvc.perform(post("/anthropometryVitals/getBenHeightDetailsFrmNurse")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer testtoken")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }

    @Test
    void testGetBenHeightDetailsFrmNurse_Exception() throws Exception {
        Mockito.when(anthropometryVitalsService.getBeneficiaryHeightDetails(anyLong()))
                .thenThrow(new RuntimeException("Service error"));

        String requestJson = "{\"benRegID\":123}";

        mockMvc.perform(post("/anthropometryVitals/getBenHeightDetailsFrmNurse")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer testtoken")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error while getting beneficiary height data")));
    }
}
