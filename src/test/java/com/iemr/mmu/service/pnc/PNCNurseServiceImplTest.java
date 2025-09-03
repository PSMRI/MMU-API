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
package com.iemr.mmu.service.pnc;

import com.iemr.mmu.data.pnc.PNCCare;
import com.iemr.mmu.repo.nurse.pnc.PNCCareRepo;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PNCNurseServiceImplTest {
    @Mock
    private PNCCareRepo pncCareRepo;

    @InjectMocks
    private PNCNurseServiceImpl pncNurseServiceImpl;

    private PNCCare pncCare;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pncCare = mock(PNCCare.class);
    }

    @Test
    void testSaveBenPncCareDetails_success() throws Exception {
        when(pncCare.getID()).thenReturn(1L);
        when(pncCare.getdDate()).thenReturn("2025-08-08T00:00:00");
        when(pncCareRepo.save(any(PNCCare.class))).thenReturn(pncCare);
        Long result = pncNurseServiceImpl.saveBenPncCareDetails(pncCare);
        assertEquals(1L, result);
        verify(pncCareRepo, times(1)).save(any(PNCCare.class));
    }

    @Test
    void testSaveBenPncCareDetails_nullDate() throws Exception {
        when(pncCare.getID()).thenReturn(1L);
        when(pncCare.getdDate()).thenReturn(null);
        when(pncCareRepo.save(any(PNCCare.class))).thenReturn(pncCare);
        Long result = pncNurseServiceImpl.saveBenPncCareDetails(pncCare);
        assertEquals(1L, result);
    }

    @Test
    void testSaveBenPncCareDetails_emptyDate() throws Exception {
        when(pncCare.getID()).thenReturn(1L);
        when(pncCare.getdDate()).thenReturn("");
        when(pncCareRepo.save(any(PNCCare.class))).thenReturn(pncCare);
        Long result = pncNurseServiceImpl.saveBenPncCareDetails(pncCare);
        assertEquals(1L, result);
    }

    @Test
    void testSaveBenPncCareDetails_shortDate() throws Exception {
        when(pncCare.getID()).thenReturn(1L);
        when(pncCare.getdDate()).thenReturn("2025-08");
        when(pncCareRepo.save(any(PNCCare.class))).thenReturn(pncCare);
        Long result = pncNurseServiceImpl.saveBenPncCareDetails(pncCare);
        assertEquals(1L, result);
    }

    @Test
    void testSaveBenPncCareDetails_saveReturnsNull() throws Exception {
        when(pncCareRepo.save(any(PNCCare.class))).thenReturn(null);
        Long result = pncNurseServiceImpl.saveBenPncCareDetails(pncCare);
        assertNull(result);
    }

    @Test
    void testSaveBenPncCareDetails_saveReturnsZeroId() throws Exception {
        PNCCare zeroIdCare = mock(PNCCare.class);
        when(zeroIdCare.getID()).thenReturn(0L);
        when(pncCareRepo.save(any(PNCCare.class))).thenReturn(zeroIdCare);
        Long result = pncNurseServiceImpl.saveBenPncCareDetails(pncCare);
        assertNull(result);
    }

    @Test
    void testGetPNCCareDetails() {
        ArrayList<Object[]> resList = new ArrayList<>();
        PNCCare care = mock(PNCCare.class);
        try (var mocked = mockStatic(PNCCare.class)) {
            when(pncCareRepo.getPNCCareDetails(anyLong(), anyLong())).thenReturn(resList);
            when(PNCCare.getPNCCareDetails(resList)).thenReturn(care);
            String json = new Gson().toJson(care);
            String result = pncNurseServiceImpl.getPNCCareDetails(1L, 2L);
            assertEquals(json, result);
        }
    }

    @Test
    void testUpdateBenPNCCareDetails_updatePath() throws Exception {
        PNCCare care = mock(PNCCare.class);
        when(care.getBeneficiaryRegID()).thenReturn(1L);
        when(care.getVisitCode()).thenReturn(2L);
        when(pncCareRepo.getBenPNCCareDetailsStatus(anyLong(), anyLong())).thenReturn("U");
        PNCNurseServiceImpl spyService = spy(pncNurseServiceImpl);
        doReturn(1).when(spyService).updateBenPNCCare(any(PNCCare.class));
        int result = spyService.updateBenPNCCareDetails(care);
        assertEquals(1, result);
    }

    @Test
    void testUpdateBenPNCCareDetails_insertPath() throws Exception {
        PNCCare care = mock(PNCCare.class);
        when(care.getBeneficiaryRegID()).thenReturn(1L);
        when(care.getVisitCode()).thenReturn(2L);
        when(care.getModifiedBy()).thenReturn("3");
        when(pncCareRepo.getBenPNCCareDetailsStatus(anyLong(), anyLong())).thenReturn(null);
        PNCNurseServiceImpl spyService = spy(pncNurseServiceImpl);
        doReturn(1L).when(spyService).saveBenPncCareDetails(any(PNCCare.class));
        int result = spyService.updateBenPNCCareDetails(care);
        assertEquals(1, result);
    }

    @Test
    void testUpdateBenPNCCareDetails_insertPathFail() throws Exception {
        PNCCare care = mock(PNCCare.class);
        when(care.getBeneficiaryRegID()).thenReturn(1L);
        when(care.getVisitCode()).thenReturn(2L);
        when(care.getModifiedBy()).thenReturn("3");
        when(pncCareRepo.getBenPNCCareDetailsStatus(anyLong(), anyLong())).thenReturn(null);
        PNCNurseServiceImpl spyService = spy(pncNurseServiceImpl);
        doReturn(null).when(spyService).saveBenPncCareDetails(any(PNCCare.class));
        int result = spyService.updateBenPNCCareDetails(care);
        assertEquals(0, result);
    }

    @Test
    void testUpdateBenPNCCare() throws Exception {
        PNCCare care = mock(PNCCare.class);
        when(care.getdDate()).thenReturn("2025-08-08T00:00:00");
        when(pncCareRepo.updatePNCCareDetails(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any()
        )).thenReturn(1);
        int result = pncNurseServiceImpl.updateBenPNCCare(care);
        assertEquals(1, result);
    }
}
