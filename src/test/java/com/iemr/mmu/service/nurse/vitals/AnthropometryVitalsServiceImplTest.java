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
package com.iemr.mmu.service.nurse.vitals;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;
import com.iemr.mmu.repo.nurse.BenAnthropometryRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnthropometryVitalsServiceImpl Test Cases")
class AnthropometryVitalsServiceImplTest {

    @Mock
    private BenAnthropometryRepo benAnthropometryRepo;

    @InjectMocks
    private AnthropometryVitalsServiceImpl anthropometryVitalsService;

    private Long testBenRegID;
    private Long testVisitCode;
    private Double testHeight;

    @BeforeEach
    void setUp() {
        testBenRegID = 123L;
        testVisitCode = 456L;
        testHeight = 170.5;
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Success with valid height data")
    void testGetBeneficiaryHeightDetails_Success() {
        // Arrange
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(testHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(testHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Visit code is null")
    void testGetBeneficiaryHeightDetails_VisitCodeNull() {
        // Arrange
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(null);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals("Visit code is not found", result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo, never()).getBenLatestHeightDetails(any());
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Height data is null")
    void testGetBeneficiaryHeightDetails_HeightNull() {
        // Arrange
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(null);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals("No data found", result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Zero height value")
    void testGetBeneficiaryHeightDetails_ZeroHeight() {
        // Arrange
        Double zeroHeight = 0.0;
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(zeroHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(zeroHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Negative height value")
    void testGetBeneficiaryHeightDetails_NegativeHeight() {
        // Arrange
        Double negativeHeight = -5.0;
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(negativeHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(negativeHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Very large height value")
    void testGetBeneficiaryHeightDetails_LargeHeight() {
        // Arrange
        Double largeHeight = 999.99;
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(largeHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(largeHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Null benRegID")
    void testGetBeneficiaryHeightDetails_NullBenRegID() {
        // Arrange
        when(benAnthropometryRepo.getBenLatestVisitCode(null)).thenReturn(null);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(null);

        // Assert
        assertNotNull(result);
        assertEquals("Visit code is not found", result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(null);
        verify(benAnthropometryRepo, never()).getBenLatestHeightDetails(any());
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Zero benRegID")
    void testGetBeneficiaryHeightDetails_ZeroBenRegID() {
        // Arrange
        Long zeroBenRegID = 0L;
        when(benAnthropometryRepo.getBenLatestVisitCode(zeroBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(testHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(zeroBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(testHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(zeroBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Negative benRegID")
    void testGetBeneficiaryHeightDetails_NegativeBenRegID() {
        // Arrange
        Long negativeBenRegID = -123L;
        when(benAnthropometryRepo.getBenLatestVisitCode(negativeBenRegID)).thenReturn(null);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(negativeBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals("Visit code is not found", result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(negativeBenRegID);
        verify(benAnthropometryRepo, never()).getBenLatestHeightDetails(any());
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Zero visit code")
    void testGetBeneficiaryHeightDetails_ZeroVisitCode() {
        // Arrange
        Long zeroVisitCode = 0L;
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(zeroVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(zeroVisitCode)).thenReturn(testHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(testHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(zeroVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Decimal height precision")
    void testGetBeneficiaryHeightDetails_DecimalPrecision() {
        // Arrange
        Double preciseHeight = 175.123456789;
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(preciseHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertEquals(new Gson().toJson(preciseHeight), result);
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Repository interaction verification")
    void testGetBeneficiaryHeightDetails_RepositoryInteraction() {
        // Arrange
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(testHeight);

        // Act
        anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        verify(benAnthropometryRepo, times(1)).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo, times(1)).getBenLatestHeightDetails(testVisitCode);
        verifyNoMoreInteractions(benAnthropometryRepo);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - Multiple consecutive calls")
    void testGetBeneficiaryHeightDetails_MultipleCalls() {
        // Arrange
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(testHeight);

        // Act
        String result1 = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);
        String result2 = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertEquals(result1, result2);
        verify(benAnthropometryRepo, times(2)).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo, times(2)).getBenLatestHeightDetails(testVisitCode);
    }

    @Test
    @DisplayName("Test getBeneficiaryHeightDetails - JSON serialization validation")
    void testGetBeneficiaryHeightDetails_JsonSerialization() {
        // Arrange
        Double specialHeight = Double.MAX_VALUE;
        when(benAnthropometryRepo.getBenLatestVisitCode(testBenRegID)).thenReturn(testVisitCode);
        when(benAnthropometryRepo.getBenLatestHeightDetails(testVisitCode)).thenReturn(specialHeight);

        // Act
        String result = anthropometryVitalsService.getBeneficiaryHeightDetails(testBenRegID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(String.valueOf(specialHeight)));
        verify(benAnthropometryRepo).getBenLatestVisitCode(testBenRegID);
        verify(benAnthropometryRepo).getBenLatestHeightDetails(testVisitCode);
    }
}
