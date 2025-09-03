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
package com.iemr.mmu.service.nurse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.iemr.mmu.data.nurse.BenFamilyCancerHistory;
import com.iemr.mmu.data.nurse.BenObstetricCancerHistory;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("NurseServiceImpl Test Cases")
class NurseServiceImplTest {

    @Mock
    private BenVisitDetailRepo benVisitDetailRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NurseServiceImpl nurseService;

    @Test
    @DisplayName("Test savePatientVisitDetails - Always returns 'hii'")
    @org.junit.jupiter.api.Disabled("Disabled: cannot mock RestTemplate created inside method without refactor")
    void testSavePatientVisitDetails_ReturnsHii() {
        // Arrange
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("success"));

        // Act
        String result = nurseService.savePatientVisitDetails();

        // Assert
        assertEquals("hii", result);
        verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Test getBeneficiaryVisitHistory - Returns correct JSON for visit details")
    void testGetBeneficiaryVisitHistory_Success() {
        // Arrange
        Long benRegID = 123L;
        List<BeneficiaryVisitDetail> visitDetails = new ArrayList<>();
        BeneficiaryVisitDetail detail = new BeneficiaryVisitDetail(1L, benRegID, 2L, 3,
            new java.sql.Timestamp(System.currentTimeMillis()), (short)4, (short)5, "reason", 6, "category", "pregStatus", "rchid", "facilityType", "facilityLocation", "reportPath", false, "processed", "createdBy", new java.sql.Timestamp(System.currentTimeMillis()), "modifiedBy", new java.sql.Timestamp(System.currentTimeMillis()));
        visitDetails.add(detail);
        when(benVisitDetailRepo.getBeneficiaryVisitHistory(benRegID)).thenReturn(visitDetails);

        // Act
        String result = nurseService.getBeneficiaryVisitHistory(benRegID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("benVisitDetails"));
        assertTrue(result.contains("reason"));
        verify(benVisitDetailRepo).getBeneficiaryVisitHistory(benRegID);
    }

    @Test
    @DisplayName("Test getBeneficiaryVisitHistory - Empty visit details list")
    void testGetBeneficiaryVisitHistory_EmptyList() {
        // Arrange
        Long benRegID = 456L;
        when(benVisitDetailRepo.getBeneficiaryVisitHistory(benRegID)).thenReturn(new ArrayList<>());

        // Act
        String result = nurseService.getBeneficiaryVisitHistory(benRegID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("benVisitDetails"));
        verify(benVisitDetailRepo).getBeneficiaryVisitHistory(benRegID);
    }

    @Test
    @DisplayName("Test getBeneficiaryVisitHistory - Null benRegID")
    void testGetBeneficiaryVisitHistory_NullBenRegID() {
        // Arrange
        when(benVisitDetailRepo.getBeneficiaryVisitHistory(null)).thenReturn(new ArrayList<>());

        // Act
        String result = nurseService.getBeneficiaryVisitHistory(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("benVisitDetails"));
        verify(benVisitDetailRepo).getBeneficiaryVisitHistory(null);
    }

    @Test
    @DisplayName("Test getBeneficiaryVisitHistory - Multiple visit details")
    void testGetBeneficiaryVisitHistory_MultipleDetails() {
        // Arrange
        Long benRegID = 789L;
        List<BeneficiaryVisitDetail> visitDetails = new ArrayList<>();
        visitDetails.add(new BeneficiaryVisitDetail(1L, benRegID, 2L, 3,
            new java.sql.Timestamp(System.currentTimeMillis()), (short)4, (short)5, "reason1", 6, "category1", "pregStatus1", "rchid1", "facilityType1", "facilityLocation1", "reportPath1", false, "processed1", "createdBy1", new java.sql.Timestamp(System.currentTimeMillis()), "modifiedBy1", new java.sql.Timestamp(System.currentTimeMillis())));
        visitDetails.add(new BeneficiaryVisitDetail(2L, benRegID, 3L, 4,
            new java.sql.Timestamp(System.currentTimeMillis()), (short)5, (short)6, "reason2", 7, "category2", "pregStatus2", "rchid2", "facilityType2", "facilityLocation2", "reportPath2", false, "processed2", "createdBy2", new java.sql.Timestamp(System.currentTimeMillis()), "modifiedBy2", new java.sql.Timestamp(System.currentTimeMillis())));
        when(benVisitDetailRepo.getBeneficiaryVisitHistory(benRegID)).thenReturn(visitDetails);

        // Act
        String result = nurseService.getBeneficiaryVisitHistory(benRegID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("reason1"));
        assertTrue(result.contains("reason2"));
        verify(benVisitDetailRepo).getBeneficiaryVisitHistory(benRegID);
    }

    @Test
    @DisplayName("Test getBeneficiaryVisitHistory - Repository interaction verification")
    void testGetBeneficiaryVisitHistory_RepositoryInteraction() {
        // Arrange
        Long benRegID = 101L;
        when(benVisitDetailRepo.getBeneficiaryVisitHistory(benRegID)).thenReturn(new ArrayList<>());

        // Act
        nurseService.getBeneficiaryVisitHistory(benRegID);

        // Assert
        verify(benVisitDetailRepo, times(1)).getBeneficiaryVisitHistory(benRegID);
        verifyNoMoreInteractions(benVisitDetailRepo);
    }
}
