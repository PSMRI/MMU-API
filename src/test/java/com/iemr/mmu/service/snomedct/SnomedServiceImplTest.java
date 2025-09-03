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
package com.iemr.mmu.service.snomedct;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.repo.snomedct.SnomedRepository;
import com.iemr.mmu.utils.mapper.OutputMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SnomedServiceImpl Test Cases")
class SnomedServiceImplTest {

    @Mock
    private SnomedRepository snomedRepository;

    @InjectMocks
    private SnomedServiceImpl snomedService;

    private SCTDescription sampleSCTDescription;
    private List<Object[]> sampleRecords;
    private Page<SCTDescription> samplePage;

    @BeforeEach
    void setUp() {
        // Set the snomedCTPageSize property
        ReflectionTestUtils.setField(snomedService, "snomedCTPageSize", 10);
        
        // Initialize sample data
        sampleSCTDescription = new SCTDescription();
        sampleSCTDescription.setTerm("headache");
        sampleSCTDescription.setPageNo(0);
        sampleSCTDescription.setConceptID("25064002");
        
        // Create sample records for findSnomedCTRecordFromTerm
        sampleRecords = new ArrayList<>();
        Object[] record1 = {"25064002", "headache", "900000000000448009"};
        Object[] record2 = {"25064003", "severe headache", "900000000000013009"};
        sampleRecords.add(record1);
        sampleRecords.add(record2);
        
        // Create sample page for findSnomedCTRecordList
        List<SCTDescription> content = new ArrayList<>();
        content.add(sampleSCTDescription);
        samplePage = new PageImpl<>(content, PageRequest.of(0, 10), 1);
    }

    @Test
    @DisplayName("Test findSnomedCTRecordFromTerm - Success with valid term")
    void testFindSnomedCTRecordFromTerm_Success() {
        // Arrange
        String term = "headache";
        when(snomedRepository.findSnomedCTRecordFromTerm(term)).thenReturn(sampleRecords);
        
        try (MockedStatic<SCTDescription> mockedSCTDescription = mockStatic(SCTDescription.class)) {
            SCTDescription expectedResult = new SCTDescription("25064002", "headache");
            mockedSCTDescription.when(() -> SCTDescription.getSnomedCTOBJ(sampleRecords))
                              .thenReturn(expectedResult);
            
            // Act
            SCTDescription result = snomedService.findSnomedCTRecordFromTerm(term);
            
            // Assert
            assertNotNull(result);
            assertEquals("25064002", result.getConceptID());
            assertEquals("headache", result.getTerm());
            verify(snomedRepository).findSnomedCTRecordFromTerm(term);
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordFromTerm - Empty records")
    void testFindSnomedCTRecordFromTerm_EmptyRecords() {
        // Arrange
        String term = "nonexistent";
        List<Object[]> emptyRecords = new ArrayList<>();
        when(snomedRepository.findSnomedCTRecordFromTerm(term)).thenReturn(emptyRecords);
        
        try (MockedStatic<SCTDescription> mockedSCTDescription = mockStatic(SCTDescription.class)) {
            mockedSCTDescription.when(() -> SCTDescription.getSnomedCTOBJ(emptyRecords))
                              .thenReturn(null);
            
            // Act
            SCTDescription result = snomedService.findSnomedCTRecordFromTerm(term);
            
            // Assert
            assertNull(result);
            verify(snomedRepository).findSnomedCTRecordFromTerm(term);
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordFromTerm - Null term")
    void testFindSnomedCTRecordFromTerm_NullTerm() {
        // Arrange
        String term = null;
        when(snomedRepository.findSnomedCTRecordFromTerm(term)).thenReturn(new ArrayList<>());
        
        try (MockedStatic<SCTDescription> mockedSCTDescription = mockStatic(SCTDescription.class)) {
            mockedSCTDescription.when(() -> SCTDescription.getSnomedCTOBJ(any()))
                              .thenReturn(null);
            
            // Act
            SCTDescription result = snomedService.findSnomedCTRecordFromTerm(term);
            
            // Assert
            assertNull(result);
            verify(snomedRepository).findSnomedCTRecordFromTerm(term);
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Success with valid input")
    void testFindSnomedCTRecordList_Success() throws Exception {
        // Arrange
        when(snomedRepository.findSnomedCTRecordList(eq("headache"), any(PageRequest.class)))
            .thenReturn(samplePage);
        
        try (MockedStatic<OutputMapper> mockedOutputMapper = mockStatic(OutputMapper.class)) {
            Gson mockGson = mock(Gson.class);
            String expectedJson = "{\"sctMaster\":[],\"pageCount\":1}";
            
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(anyMap())).thenReturn(expectedJson);
            
            // Act
            String result = snomedService.findSnomedCTRecordList(sampleSCTDescription);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(snomedRepository).findSnomedCTRecordList(eq("headache"), any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Success with different page number")
    void testFindSnomedCTRecordList_DifferentPageNumber() throws Exception {
        // Arrange
        sampleSCTDescription.setPageNo(1);
        
        List<SCTDescription> emptyContent = new ArrayList<>();
        Page<SCTDescription> emptyPage = new PageImpl<>(emptyContent, PageRequest.of(1, 10), 1);
        
        when(snomedRepository.findSnomedCTRecordList(eq("headache"), any(PageRequest.class)))
            .thenReturn(emptyPage);
        
        try (MockedStatic<OutputMapper> mockedOutputMapper = mockStatic(OutputMapper.class)) {
            Gson mockGson = mock(Gson.class);
            String expectedJson = "{\"sctMaster\":[],\"pageCount\":1}";
            
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(anyMap())).thenReturn(expectedJson);
            
            // Act
            String result = snomedService.findSnomedCTRecordList(sampleSCTDescription);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(snomedRepository).findSnomedCTRecordList(eq("headache"), any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Null SCTDescription throws exception")
    void testFindSnomedCTRecordList_NullSCTDescription() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            snomedService.findSnomedCTRecordList(null);
        });
        
        assertEquals("invalid request", exception.getMessage());
        verifyNoInteractions(snomedRepository);
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Null term throws exception")
    void testFindSnomedCTRecordList_NullTerm() {
        // Arrange
        sampleSCTDescription.setTerm(null);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            snomedService.findSnomedCTRecordList(sampleSCTDescription);
        });
        
        assertEquals("invalid request", exception.getMessage());
        verifyNoInteractions(snomedRepository);
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Null pageNo throws exception")
    void testFindSnomedCTRecordList_NullPageNo() {
        // Arrange
        sampleSCTDescription.setPageNo(null);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            snomedService.findSnomedCTRecordList(sampleSCTDescription);
        });
        
        assertEquals("invalid request", exception.getMessage());
        verifyNoInteractions(snomedRepository);
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Empty term processes normally")
    void testFindSnomedCTRecordList_EmptyTerm() throws Exception {
        // Arrange
        sampleSCTDescription.setTerm("");
        
        List<SCTDescription> emptyContent = new ArrayList<>();
        Page<SCTDescription> emptyPage = new PageImpl<>(emptyContent, PageRequest.of(0, 10), 0);
        
        when(snomedRepository.findSnomedCTRecordList(eq(""), any(PageRequest.class)))
            .thenReturn(emptyPage);
        
        try (MockedStatic<OutputMapper> mockedOutputMapper = mockStatic(OutputMapper.class)) {
            Gson mockGson = mock(Gson.class);
            String expectedJson = "{\"sctMaster\":[],\"pageCount\":0}";
            
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(anyMap())).thenReturn(expectedJson);
            
            // Act
            String result = snomedService.findSnomedCTRecordList(sampleSCTDescription);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(snomedRepository).findSnomedCTRecordList(eq(""), any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Repository returns empty page")
    void testFindSnomedCTRecordList_EmptyPage() throws Exception {
        // Arrange
        List<SCTDescription> emptyContent = new ArrayList<>();
        Page<SCTDescription> emptyPage = new PageImpl<>(emptyContent, PageRequest.of(0, 10), 0);
        
        when(snomedRepository.findSnomedCTRecordList(eq("headache"), any(PageRequest.class)))
            .thenReturn(emptyPage);
        
        try (MockedStatic<OutputMapper> mockedOutputMapper = mockStatic(OutputMapper.class)) {
            Gson mockGson = mock(Gson.class);
            String expectedJson = "{\"sctMaster\":[],\"pageCount\":0}";
            
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(anyMap())).thenReturn(expectedJson);
            
            // Act
            String result = snomedService.findSnomedCTRecordList(sampleSCTDescription);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(snomedRepository).findSnomedCTRecordList(eq("headache"), any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Large page number")
    void testFindSnomedCTRecordList_LargePageNumber() throws Exception {
        // Arrange
        sampleSCTDescription.setPageNo(999);
        
        List<SCTDescription> emptyContent = new ArrayList<>();
        Page<SCTDescription> emptyPage = new PageImpl<>(emptyContent, PageRequest.of(999, 10), 1);
        
        when(snomedRepository.findSnomedCTRecordList(eq("headache"), any(PageRequest.class)))
            .thenReturn(emptyPage);
        
        try (MockedStatic<OutputMapper> mockedOutputMapper = mockStatic(OutputMapper.class)) {
            Gson mockGson = mock(Gson.class);
            String expectedJson = "{\"sctMaster\":[],\"pageCount\":1}";
            
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(anyMap())).thenReturn(expectedJson);
            
            // Act
            String result = snomedService.findSnomedCTRecordList(sampleSCTDescription);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(snomedRepository).findSnomedCTRecordList(eq("headache"), any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Test findSnomedCTRecordList - Verify PageRequest creation")
    void testFindSnomedCTRecordList_VerifyPageRequest() throws Exception {
        // Arrange
        sampleSCTDescription.setPageNo(2);
        
        when(snomedRepository.findSnomedCTRecordList(eq("headache"), any(PageRequest.class)))
            .thenReturn(samplePage);
        
        try (MockedStatic<OutputMapper> mockedOutputMapper = mockStatic(OutputMapper.class)) {
            Gson mockGson = mock(Gson.class);
            String expectedJson = "{\"sctMaster\":[{\"term\":\"headache\",\"conceptID\":123}],\"pageCount\":1}";
            
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(anyMap())).thenReturn(expectedJson);
            
            // Act
            String result = snomedService.findSnomedCTRecordList(sampleSCTDescription);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(snomedRepository).findSnomedCTRecordList(eq("headache"), eq(PageRequest.of(2, 10)));
        }
    }

    @Test
    @DisplayName("Test snomedCTPageSize property injection")
    void testSnomedCTPageSizeProperty() {
        // Arrange
        Integer expectedPageSize = 20;
        ReflectionTestUtils.setField(snomedService, "snomedCTPageSize", expectedPageSize);
        
        // Act
        Integer actualPageSize = (Integer) ReflectionTestUtils.getField(snomedService, "snomedCTPageSize");
        
        // Assert
        assertEquals(expectedPageSize, actualPageSize);
    }

    @Test
    @DisplayName("Test repository injection")
    void testRepositoryInjection() {
        // Assert
        assertNotNull(ReflectionTestUtils.getField(snomedService, "snomedRepository"));
    }
}
