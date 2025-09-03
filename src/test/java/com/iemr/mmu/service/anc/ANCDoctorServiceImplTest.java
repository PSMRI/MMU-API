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
package com.iemr.mmu.service.anc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.anc.ANCDiagnosis;
import com.iemr.mmu.repo.nurse.anc.ANCDiagnosisRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ANCDoctorServiceImplTest {
    @Test
    void testUpdateBenANCDiagnosis_withNullAndMissingComplicationType() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setBeneficiaryRegID(1L);
        ancDiagnosis.setVisitCode(2L);
        ancDiagnosis.setPrescriptionID(3L);
        ArrayList<Map<String, String>> compList = new ArrayList<>();
        Map<String, String> comp1 = new HashMap<>();
        comp1.put("pregComplicationType", null); // null value
        Map<String, String> comp2 = new HashMap<>();
        // missing key
        Map<String, String> comp3 = new HashMap<>();
        comp3.put("pregComplicationType", "Type3"); // valid
        compList.add(comp1);
        compList.add(comp2);
        compList.add(comp3);
        ancDiagnosis.setComplicationOfCurrentPregnancyList(compList);
        when(ancDiagnosisRepo.getANCDiagnosisStatus(anyLong(), anyLong(), anyLong())).thenReturn("Y");
        when(ancDiagnosisRepo.updateANCDiagnosis(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        int res = service.updateBenANCDiagnosis(ancDiagnosis);
        assertEquals(1, res);
    }
    @Test
    void testSaveBenANCDiagnosis_withNullAndMissingComplicationType() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ArrayList<Map<String, String>> compList = new ArrayList<>();
        Map<String, String> comp1 = new HashMap<>();
        comp1.put("pregComplicationType", null); // null value
        Map<String, String> comp2 = new HashMap<>();
        // missing key
        Map<String, String> comp3 = new HashMap<>();
        comp3.put("pregComplicationType", "Type3"); // valid
        compList.add(comp1);
        compList.add(comp2);
        compList.add(comp3);
        ancDiagnosis.setComplicationOfCurrentPregnancyList(compList);
        ancDiagnosis.setPrescriptionID(123L);
        JsonObject obj = new Gson().toJsonTree(ancDiagnosis).getAsJsonObject();
        ANCDiagnosis saved = new ANCDiagnosis();
        saved.setID(101L);
        when(ancDiagnosisRepo.save(any())).thenReturn(saved);
        Long id = service.saveBenANCDiagnosis(obj, 123L);
        assertEquals(101L, id);
    }
    @Mock
    private ANCDiagnosisRepo ancDiagnosisRepo;
    @Mock
    private PrescriptionDetailRepo prescriptionDetailRepo;
    @InjectMocks
    private ANCDoctorServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ANCDoctorServiceImpl();
        service.setAncDiagnosisRepo(ancDiagnosisRepo);
        service.setPrescriptionDetailRepo(prescriptionDetailRepo);
    }

    @Test
    void testSaveBenANCDiagnosis_withComplications() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ArrayList<Map<String, String>> compList = new ArrayList<>();
        Map<String, String> comp1 = new HashMap<>();
        comp1.put("pregComplicationType", "Type1");
        Map<String, String> comp2 = new HashMap<>();
        comp2.put("pregComplicationType", "Type2");
        compList.add(comp1);
        compList.add(comp2);
        ancDiagnosis.setComplicationOfCurrentPregnancyList(compList);
        ancDiagnosis.setPrescriptionID(123L);
        JsonObject obj = new Gson().toJsonTree(ancDiagnosis).getAsJsonObject();
        ANCDiagnosis saved = new ANCDiagnosis();
        saved.setID(99L);
        when(ancDiagnosisRepo.save(any())).thenReturn(saved);
        Long id = service.saveBenANCDiagnosis(obj, 123L);
        assertEquals(99L, id);
    }

    @Test
    void testSaveBenANCDiagnosis_noComplications() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setPrescriptionID(123L);
        JsonObject obj = new Gson().toJsonTree(ancDiagnosis).getAsJsonObject();
        ANCDiagnosis saved = new ANCDiagnosis();
        saved.setID(99L);
        when(ancDiagnosisRepo.save(any())).thenReturn(saved);
        Long id = service.saveBenANCDiagnosis(obj, 123L);
        assertEquals(99L, id);
    }

    @Test
    void testSaveBenANCDiagnosis_nullReturn() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setPrescriptionID(123L);
        JsonObject obj = new Gson().toJsonTree(ancDiagnosis).getAsJsonObject();
        when(ancDiagnosisRepo.save(any())).thenReturn(null);
        Long id = service.saveBenANCDiagnosis(obj, 123L);
        assertNull(id);
    }

    @Test
    void testGetANCDiagnosisDetails_withComplicationsAndExternalInvestigation() {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setComplicationOfCurrentPregnancy("Type1 , Type2");
        ancDiagnosis.setOtherCurrPregComplication("Other");
        ArrayList<ANCDiagnosis> list = new ArrayList<>();
        list.add(ancDiagnosis);
        when(ancDiagnosisRepo.findByBeneficiaryRegIDAndVisitCode(anyLong(), anyLong())).thenReturn(list);
        when(prescriptionDetailRepo.getExternalinvestigationForVisitCode(anyLong(), anyLong())).thenReturn("ExtInv");
        String json = service.getANCDiagnosisDetails(1L, 2L);
        assertTrue(json.contains("Type1"));
        assertTrue(json.contains("Other-complications"));
        assertTrue(json.contains("ExtInv"));
    }

    @Test
    void testGetANCDiagnosisDetails_noComplications() {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ArrayList<ANCDiagnosis> list = new ArrayList<>();
        list.add(ancDiagnosis);
        when(ancDiagnosisRepo.findByBeneficiaryRegIDAndVisitCode(anyLong(), anyLong())).thenReturn(list);
        when(prescriptionDetailRepo.getExternalinvestigationForVisitCode(anyLong(), anyLong())).thenReturn(null);
        String json = service.getANCDiagnosisDetails(1L, 2L);
        assertTrue(json.contains("complicationOfCurrentPregnancyList"));
    }

    @Test
    void testGetANCDiagnosisDetails_emptyList() {
        when(ancDiagnosisRepo.findByBeneficiaryRegIDAndVisitCode(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        String json = service.getANCDiagnosisDetails(1L, 2L);
        assertNotNull(json);
    }

    @Test
    void testUpdateBenANCDiagnosis_updatePath() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setBeneficiaryRegID(1L);
        ancDiagnosis.setVisitCode(2L);
        ancDiagnosis.setPrescriptionID(3L);
        ancDiagnosis.setComplicationOfCurrentPregnancyList(new ArrayList<>());
        when(ancDiagnosisRepo.getANCDiagnosisStatus(anyLong(), anyLong(), anyLong())).thenReturn("Y");
        when(ancDiagnosisRepo.updateANCDiagnosis(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        int res = service.updateBenANCDiagnosis(ancDiagnosis);
        assertEquals(1, res);
    }

    @Test
    void testUpdateBenANCDiagnosis_savePath() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setBeneficiaryRegID(1L);
        ancDiagnosis.setVisitCode(2L);
        ancDiagnosis.setPrescriptionID(3L);
        ancDiagnosis.setComplicationOfCurrentPregnancyList(new ArrayList<>());
        when(ancDiagnosisRepo.getANCDiagnosisStatus(anyLong(), anyLong(), anyLong())).thenReturn(null);
        ANCDiagnosis saved = new ANCDiagnosis();
        saved.setID(99L);
        when(ancDiagnosisRepo.save(any())).thenReturn(saved);
        int res = service.updateBenANCDiagnosis(ancDiagnosis);
        assertEquals(1, res);
    }

    @Test
    void testUpdateBenANCDiagnosis_savePathNull() throws IEMRException {
        ANCDiagnosis ancDiagnosis = new ANCDiagnosis();
        ancDiagnosis.setBeneficiaryRegID(1L);
        ancDiagnosis.setVisitCode(2L);
        ancDiagnosis.setPrescriptionID(3L);
        ancDiagnosis.setComplicationOfCurrentPregnancyList(new ArrayList<>());
        when(ancDiagnosisRepo.getANCDiagnosisStatus(anyLong(), anyLong(), anyLong())).thenReturn(null);
        when(ancDiagnosisRepo.save(any())).thenReturn(null);
        int res = service.updateBenANCDiagnosis(ancDiagnosis);
        assertEquals(0, res);
    }
}
