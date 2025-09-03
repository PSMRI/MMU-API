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
package com.iemr.mmu.service.common.master;

import com.iemr.mmu.data.doctor.*;
import com.iemr.mmu.data.labModule.ProcedureData;
import com.iemr.mmu.repo.doctor.*;
import com.iemr.mmu.repo.labModule.ProcedureRepo;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QCMasterDataServiceImplTest {
    private QCMasterDataServiceImpl service;
    private ChiefComplaintMasterRepo chiefComplaintMasterRepo;
    private DrugDoseMasterRepo drugDoseMasterRepo;
    private DrugDurationUnitMasterRepo drugDurationUnitMasterRepo;
    private DrugFormMasterRepo drugFormMasterRepo;
    private DrugFrequencyMasterRepo drugFrequencyMasterRepo;
    private LabTestMasterRepo labTestMasterRepo;
    private TempMasterDrugRepo tempMasterDrugRepo;
    private ProcedureRepo procedureRepo;

    @BeforeEach
    void setUp() {
        service = new QCMasterDataServiceImpl();
        chiefComplaintMasterRepo = mock(ChiefComplaintMasterRepo.class);
        drugDoseMasterRepo = mock(DrugDoseMasterRepo.class);
        drugDurationUnitMasterRepo = mock(DrugDurationUnitMasterRepo.class);
        drugFormMasterRepo = mock(DrugFormMasterRepo.class);
        drugFrequencyMasterRepo = mock(DrugFrequencyMasterRepo.class);
        labTestMasterRepo = mock(LabTestMasterRepo.class);
        tempMasterDrugRepo = mock(TempMasterDrugRepo.class);
        procedureRepo = mock(ProcedureRepo.class);
        service.setChiefComplaintMasterRepo(chiefComplaintMasterRepo);
        service.setDrugDoseMasterRepo(drugDoseMasterRepo);
        service.setDrugDurationUnitMasterRepo(drugDurationUnitMasterRepo);
        service.setDrugFormMasterRepo(drugFormMasterRepo);
        service.setDrugFrequencyMasterRepo(drugFrequencyMasterRepo);
        service.setLabTestMasterRepo(labTestMasterRepo);
        service.setTempMasterDrugRepo(tempMasterDrugRepo);
        service.setProcedureRepo(procedureRepo);
    }

    @Test
    void testGetQuickConsultMasterData_returnsExpectedJson() {
        ArrayList<Object[]> ccList = new ArrayList<>();
        ccList.add(new Object[]{1, "CC1"});
        ArrayList<Object[]> ddmList = new ArrayList<>();
        ddmList.add(new Object[]{1, "Dose1"});
        ArrayList<Object[]> ddumList = new ArrayList<>();
        ddumList.add(new Object[]{1, "Unit1"});
        ArrayList<Object[]> dfmList = new ArrayList<>();
        dfmList.add(new Object[]{1, "Form1"});
        ArrayList<Object[]> dfrmList = new ArrayList<>();
        dfrmList.add(new Object[]{1, "Freq1"});
        ArrayList<Object[]> ltmList = new ArrayList<>();
        ltmList.add(new Object[]{1, "Lab1"});
        ArrayList<Object[]> procedures = new ArrayList<>();
        procedures.add(new Object[]{1, "Proc1"});
        ArrayList<TempMasterDrug> tempMasterDrugList = new ArrayList<>();
        tempMasterDrugList.add(mock(TempMasterDrug.class));
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(ccList);
        when(drugDoseMasterRepo.getDrugDoseMaster()).thenReturn(ddmList);
        when(drugDurationUnitMasterRepo.getDrugDurationUnitMaster()).thenReturn(ddumList);
        when(drugFormMasterRepo.getDrugFormMaster()).thenReturn(dfmList);
        when(drugFrequencyMasterRepo.getDrugFrequencyMaster()).thenReturn(dfrmList);
        when(labTestMasterRepo.getLabTestMaster()).thenReturn(ltmList);
        when(procedureRepo.getProcedureMasterData(anyInt(), anyString())).thenReturn(procedures);
        when(tempMasterDrugRepo.findByDeletedFalseOrderByDrugDisplayNameAsc()).thenReturn(tempMasterDrugList);

        try (
            org.mockito.MockedStatic<ChiefComplaintMaster> chiefComplaintMasterMocked = mockStatic(ChiefComplaintMaster.class);
            org.mockito.MockedStatic<DrugDoseMaster> drugDoseMasterMocked = mockStatic(DrugDoseMaster.class);
            org.mockito.MockedStatic<DrugDurationUnitMaster> drugDurationUnitMasterMocked = mockStatic(DrugDurationUnitMaster.class);
            org.mockito.MockedStatic<DrugFormMaster> drugFormMasterMocked = mockStatic(DrugFormMaster.class);
            org.mockito.MockedStatic<DrugFrequencyMaster> drugFrequencyMasterMocked = mockStatic(DrugFrequencyMaster.class);
            org.mockito.MockedStatic<LabTestMaster> labTestMasterMocked = mockStatic(LabTestMaster.class);
            org.mockito.MockedStatic<TempMasterDrug> tempMasterDrugMocked = mockStatic(TempMasterDrug.class);
            org.mockito.MockedStatic<ProcedureData> procedureDataMocked = mockStatic(ProcedureData.class)
        ) {
            chiefComplaintMasterMocked.when(() -> ChiefComplaintMaster.getChiefComplaintMasters(ccList)).thenReturn(new ArrayList<>());
            drugDoseMasterMocked.when(() -> DrugDoseMaster.getDrugDoseMasters(ddmList)).thenReturn(new ArrayList<>());
            drugDurationUnitMasterMocked.when(() -> DrugDurationUnitMaster.getDrugDurationUnitMaster(ddumList)).thenReturn(new ArrayList<>());
            drugFormMasterMocked.when(() -> DrugFormMaster.getDrugFormMaster(dfmList)).thenReturn(new ArrayList<>());
            drugFrequencyMasterMocked.when(() -> DrugFrequencyMaster.getDrugFrequencyMaster(dfrmList)).thenReturn(new ArrayList<>());
            labTestMasterMocked.when(() -> LabTestMaster.getLabTestMasters(ltmList)).thenReturn(new ArrayList<>());
            tempMasterDrugMocked.when(() -> TempMasterDrug.getTempDrugMasterList(tempMasterDrugList)).thenReturn(new ArrayList<>());
            procedureDataMocked.when(() -> ProcedureData.getProcedures(procedures)).thenReturn(new ArrayList<>());

            String json = service.getQuickConsultMasterData(1, "M");
            assertNotNull(json);
            assertTrue(json.contains("chiefComplaintMaster"));
            assertTrue(json.contains("drugDoseMaster"));
            assertTrue(json.contains("drugDurationUnitMaster"));
            assertTrue(json.contains("drugFormMaster"));
            assertTrue(json.contains("drugFrequencyMaster"));
            assertTrue(json.contains("labTestMaster"));
            assertTrue(json.contains("tempDrugMaster"));
            assertTrue(json.contains("procedures"));
        }
    }

    // Static mock helpers removed; all static mocks are now handled in a single try-with-resources block per test.
}
