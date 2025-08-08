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
package com.iemr.mmu.service.generalOPD;

import com.google.gson.Gson;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.data.snomedct.SCTDescription;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneralOPDDoctorServiceImplTest {
    @InjectMocks
    GeneralOPDDoctorServiceImpl service;
    @Mock
    PrescriptionDetailRepo prescriptionDetailRepo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getGeneralOPDDiagnosisDetails_emptyList_returnsEmptyPrescriptionDetail() {
        when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(anyLong(), anyLong()))
                .thenReturn(new ArrayList<>());
        String result = service.getGeneralOPDDiagnosisDetails(1L, 2L);
        PrescriptionDetail detail = new Gson().fromJson(result, PrescriptionDetail.class);
        assertNotNull(detail);
        assertNull(detail.getDiagnosisProvided());
    }

    @Test
    void getGeneralOPDDiagnosisDetails_nonEmptyList_nullDiagnosisFields() {
        PrescriptionDetail detail = new PrescriptionDetail();
        detail.setDiagnosisProvided_SCTCode(null);
        detail.setDiagnosisProvided(null);
        ArrayList<PrescriptionDetail> list = new ArrayList<>();
        list.add(detail);
        when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(anyLong(), anyLong())).thenReturn(list);
        String result = service.getGeneralOPDDiagnosisDetails(1L, 2L);
        PrescriptionDetail res = new Gson().fromJson(result, PrescriptionDetail.class);
        assertNotNull(res);
        assertNull(res.getDiagnosisProvided());
    }

    @Test
    void getGeneralOPDDiagnosisDetails_nonEmptyList_withDiagnosisFields() {
        PrescriptionDetail detail = new PrescriptionDetail();
        detail.setDiagnosisProvided_SCTCode("123||456");
        detail.setDiagnosisProvided("term1||term2");
        // Use the same delimiter as in the code: "  ||  "
        detail.setDiagnosisProvided_SCTCode("id1  ||  id2");
        detail.setDiagnosisProvided("term1  ||  term2");
        ArrayList<PrescriptionDetail> list = new ArrayList<>();
        list.add(detail);
        when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(anyLong(), anyLong())).thenReturn(list);
        String result = service.getGeneralOPDDiagnosisDetails(1L, 2L);
        PrescriptionDetail res = new Gson().fromJson(result, PrescriptionDetail.class);
        assertNotNull(res.getProvisionalDiagnosisList());
        assertEquals(2, res.getProvisionalDiagnosisList().size());
        assertEquals("id1", res.getProvisionalDiagnosisList().get(0).getConceptID());
        assertEquals("term1", res.getProvisionalDiagnosisList().get(0).getTerm());
        assertEquals("id2", res.getProvisionalDiagnosisList().get(1).getConceptID());
        assertEquals("term2", res.getProvisionalDiagnosisList().get(1).getTerm());
    }
}
