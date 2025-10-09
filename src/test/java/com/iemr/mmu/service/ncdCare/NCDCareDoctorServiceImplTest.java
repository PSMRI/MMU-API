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
package com.iemr.mmu.service.ncdCare;

import com.iemr.mmu.data.ncdcare.NCDCareDiagnosis;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.repo.nurse.ncdcare.NCDCareDiagnosisRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NCDCareDoctorServiceImplTest {
    @Mock
    private NCDCareDiagnosisRepo ncdCareDiagnosisRepo;
    @Mock
    private PrescriptionDetailRepo prescriptionDetailRepo;
    @InjectMocks
    private NCDCareDoctorServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveNCDDiagnosisData_withScreeningConditionArray() {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{"A", "B"});
        NCDCareDiagnosis saved = mock(NCDCareDiagnosis.class);
        when(ncdCareDiagnosisRepo.save(any())).thenReturn(saved);
        when(saved.getID()).thenReturn(123L);
        long result = service.saveNCDDiagnosisData(diagnosis);
        assertEquals(123L, result);
        verify(diagnosis).setNcdScreeningCondition("A||B");
    }

    @Test
    void testSaveNCDDiagnosisData_withNullScreeningConditionArray() {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(null);
        NCDCareDiagnosis saved = mock(NCDCareDiagnosis.class);
        when(ncdCareDiagnosisRepo.save(any())).thenReturn(saved);
        when(saved.getID()).thenReturn(456L);
        long result = service.saveNCDDiagnosisData(diagnosis);
        assertEquals(456L, result);
        verify(diagnosis).setNcdScreeningCondition(null);
    }

    @Test
    void testSaveNCDDiagnosisData_withEmptyScreeningConditionArray() {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{});
        NCDCareDiagnosis saved = mock(NCDCareDiagnosis.class);
        when(ncdCareDiagnosisRepo.save(any())).thenReturn(saved);
        when(saved.getID()).thenReturn(789L);
        long result = service.saveNCDDiagnosisData(diagnosis);
        assertEquals(789L, result);
        verify(diagnosis).setNcdScreeningCondition(null);
    }

    @Test
    void testSaveNCDDiagnosisData_repoReturnsNull() {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{"A"});
        when(ncdCareDiagnosisRepo.save(any())).thenReturn(null);
        long result = service.saveNCDDiagnosisData(diagnosis);
        assertEquals(0L, result);
    }

    @Test
    void testGetNCDCareDiagnosisDetails_allBranches() {
        Long benId = 1L, visitCode = 2L;
        NCDCareDiagnosis details = mock(NCDCareDiagnosis.class);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisDetails(benId, visitCode)).thenReturn(new ArrayList<>());
        when(prescriptionDetailRepo.getExternalinvestigationForVisitCode(benId, visitCode)).thenReturn("extInv");
        when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCodeOrderByPrescriptionIDDesc(benId, visitCode)).thenReturn(new ArrayList<>());
        try (org.mockito.MockedStatic<NCDCareDiagnosis> mockedStatic = mockStatic(NCDCareDiagnosis.class)) {
            mockedStatic.when(() -> NCDCareDiagnosis.getNCDCareDiagnosisDetails(any())).thenReturn(details);
            when(details.getNcdScreeningCondition()).thenReturn("cond1||cond2");
            when(details.getNcdScreeningConditionArray()).thenReturn(null);
            doNothing().when(details).setNcdScreeningConditionArray(any());
            doNothing().when(details).setExternalInvestigation(any());
            String json = service.getNCDCareDiagnosisDetails(benId, visitCode);
            assertNotNull(json);
            verify(details).setNcdScreeningConditionArray(any());
            verify(details).setExternalInvestigation("extInv");
        }
    }

    @Test
    void testGetNCDCareDiagnosisDetails_nullDiagnosisDetails() {
        Long benId = 1L, visitCode = 2L;
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisDetails(benId, visitCode)).thenReturn(new ArrayList<>());
        when(prescriptionDetailRepo.getExternalinvestigationForVisitCode(benId, visitCode)).thenReturn(null);
        try (org.mockito.MockedStatic<NCDCareDiagnosis> mockedStatic = mockStatic(NCDCareDiagnosis.class)) {
            mockedStatic.when(() -> NCDCareDiagnosis.getNCDCareDiagnosisDetails(any())).thenReturn(null);
            String json = service.getNCDCareDiagnosisDetails(benId, visitCode);
            assertNotNull(json);
        }
    }

    @Test
    void testGetNCDCareDiagnosisDetails_withPrescriptionDetails() {
        Long benId = 1L, visitCode = 2L;
        NCDCareDiagnosis details = mock(NCDCareDiagnosis.class);
        PrescriptionDetail pd = mock(PrescriptionDetail.class);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisDetails(benId, visitCode)).thenReturn(new ArrayList<>());
        when(prescriptionDetailRepo.getExternalinvestigationForVisitCode(benId, visitCode)).thenReturn("extInv");
        ArrayList<PrescriptionDetail> pdList = new ArrayList<>();
        pdList.add(pd);
        when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCodeOrderByPrescriptionIDDesc(benId, visitCode)).thenReturn(pdList);
        try (org.mockito.MockedStatic<NCDCareDiagnosis> mockedStatic = mockStatic(NCDCareDiagnosis.class)) {
            mockedStatic.when(() -> NCDCareDiagnosis.getNCDCareDiagnosisDetails(any())).thenReturn(details);
            when(details.getNcdScreeningCondition()).thenReturn("cond1||cond2");
            doNothing().when(details).setNcdScreeningConditionArray(any());
            doNothing().when(details).setExternalInvestigation(any());
            when(pd.getDiagnosisProvided_SCTCode()).thenReturn("code1  ||  code2");
            when(pd.getDiagnosisProvided()).thenReturn("term1  ||  term2");
            doNothing().when(pd).setProvisionalDiagnosisList(any());
            doNothing().when(details).setDiagnosisProvided(any());
            doNothing().when(details).setDiagnosisProvided_SCTCode(any());
            doNothing().when(details).setProvisionalDiagnosisList(any());
            String json = service.getNCDCareDiagnosisDetails(benId, visitCode);
            assertNotNull(json);
            verify(pd).setProvisionalDiagnosisList(any());
            verify(details).setDiagnosisProvided(any());
            verify(details).setDiagnosisProvided_SCTCode(any());
            verify(details).setProvisionalDiagnosisList(any());
        }
    }

    @Test
    void testUpdateBenNCDCareDiagnosis_updateBranch() throws IEMRException {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getBeneficiaryRegID()).thenReturn(1L);
        when(diagnosis.getVisitCode()).thenReturn(2L);
        when(diagnosis.getPrescriptionID()).thenReturn(3L);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisStatus(1L, 2L, 3L)).thenReturn("Y");
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{"A", "B"});
        when(ncdCareDiagnosisRepo.updateNCDCareDiagnosis(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(5);
        int result = service.updateBenNCDCareDiagnosis(diagnosis);
        assertEquals(5, result);
        verify(diagnosis).setProcessed("U");
        verify(diagnosis).setNcdScreeningCondition("A||B");
    }

    @Test
    void testUpdateBenNCDCareDiagnosis_saveBranch() throws IEMRException {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getBeneficiaryRegID()).thenReturn(1L);
        when(diagnosis.getVisitCode()).thenReturn(2L);
        when(diagnosis.getPrescriptionID()).thenReturn(3L);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisStatus(1L, 2L, 3L)).thenReturn(null);
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{"A", "B"});
        NCDCareDiagnosis saved = mock(NCDCareDiagnosis.class);
        when(ncdCareDiagnosisRepo.save(any())).thenReturn(saved);
        when(saved.getID()).thenReturn(10L);
        int result = service.updateBenNCDCareDiagnosis(diagnosis);
        assertEquals(1, result);
        verify(diagnosis).setNcdScreeningCondition("A||B");
    }

    @Test
    void testUpdateBenNCDCareDiagnosis_saveBranchNullID() throws IEMRException {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getBeneficiaryRegID()).thenReturn(1L);
        when(diagnosis.getVisitCode()).thenReturn(2L);
        when(diagnosis.getPrescriptionID()).thenReturn(3L);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisStatus(1L, 2L, 3L)).thenReturn(null);
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{"A", "B"});
        NCDCareDiagnosis saved = mock(NCDCareDiagnosis.class);
        when(ncdCareDiagnosisRepo.save(any())).thenReturn(saved);
        when(saved.getID()).thenReturn(0L);
        int result = service.updateBenNCDCareDiagnosis(diagnosis);
        assertEquals(0, result);
    }

    @Test
    void testUpdateBenNCDCareDiagnosis_screeningConditionNull() throws IEMRException {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getBeneficiaryRegID()).thenReturn(1L);
        when(diagnosis.getVisitCode()).thenReturn(2L);
        when(diagnosis.getPrescriptionID()).thenReturn(3L);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisStatus(1L, 2L, 3L)).thenReturn("N");
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(null);
        when(ncdCareDiagnosisRepo.updateNCDCareDiagnosis(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(7);
        int result = service.updateBenNCDCareDiagnosis(diagnosis);
        assertEquals(7, result);
        verify(diagnosis).setNcdScreeningCondition(null);
    }

    @Test
    void testUpdateBenNCDCareDiagnosis_screeningConditionEmpty() throws IEMRException {
        NCDCareDiagnosis diagnosis = mock(NCDCareDiagnosis.class);
        when(diagnosis.getBeneficiaryRegID()).thenReturn(1L);
        when(diagnosis.getVisitCode()).thenReturn(2L);
        when(diagnosis.getPrescriptionID()).thenReturn(3L);
        when(ncdCareDiagnosisRepo.getNCDCareDiagnosisStatus(1L, 2L, 3L)).thenReturn("N");
        when(diagnosis.getNcdScreeningConditionArray()).thenReturn(new String[]{});
        when(ncdCareDiagnosisRepo.updateNCDCareDiagnosis(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(8);
        int result = service.updateBenNCDCareDiagnosis(diagnosis);
        assertEquals(8, result);
        verify(diagnosis).setNcdScreeningCondition(null);
    }
}
