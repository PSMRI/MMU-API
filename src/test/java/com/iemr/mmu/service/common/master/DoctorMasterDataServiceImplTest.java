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

import com.google.gson.Gson;
import com.iemr.mmu.data.institution.Institute;
import com.iemr.mmu.data.masterdata.anc.ServiceMaster;
import com.iemr.mmu.data.masterdata.doctor.PreMalignantLesion;
import com.iemr.mmu.repo.masterrepo.anc.ServiceMasterRepo;
import com.iemr.mmu.repo.masterrepo.doctor.InstituteRepo;
import com.iemr.mmu.repo.masterrepo.doctor.PreMalignantLesionMasterRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorMasterDataServiceImplTest {
    private DoctorMasterDataServiceImpl service;
    private PreMalignantLesionMasterRepo preMalignantLesionMasterRepo;
    private InstituteRepo instituteRepo;
    private ServiceMasterRepo serviceMasterRepo;

    @BeforeEach
    void setUp() {
        service = new DoctorMasterDataServiceImpl();
        preMalignantLesionMasterRepo = mock(PreMalignantLesionMasterRepo.class);
        instituteRepo = mock(InstituteRepo.class);
        serviceMasterRepo = mock(ServiceMasterRepo.class);
        // Inject mocks
        setField(service, "preMalignantLesionMasterRepo", preMalignantLesionMasterRepo);
        service.setInstituteRepo(instituteRepo);
        service.setServiceMasterRepo(serviceMasterRepo);
    }

    @Test
    void testGetCancerScreeningMasterDataForDoctor_success() {
        ArrayList<Object[]> lesionTypesRaw = new ArrayList<>();
        lesionTypesRaw.add(new Object[]{1, "Lesion1"});
        ArrayList<Object[]> instituteDetailsRaw = new ArrayList<>();
        instituteDetailsRaw.add(new Object[]{1, "Inst1"});
        ArrayList<Object[]> additionalServicesRaw = new ArrayList<>();
        additionalServicesRaw.add(new Object[]{1, "Service1"});
        when(preMalignantLesionMasterRepo.getPreMalignantLesionMaster()).thenReturn(lesionTypesRaw);
        when(instituteRepo.getInstituteDetails(anyInt())).thenReturn(instituteDetailsRaw);
        when(serviceMasterRepo.getAdditionalServices()).thenReturn(additionalServicesRaw);

        // Mock static methods
    // Mock static methods with correct return types
    ArrayList<PreMalignantLesion> lesionList = new ArrayList<>();
    lesionList.add(mock(PreMalignantLesion.class));
    org.mockito.MockedStatic<PreMalignantLesion> mockedLesion = mockStatic(PreMalignantLesion.class);
    mockedLesion.when(() -> PreMalignantLesion.getPreMalignantLesionMasterData(lesionTypesRaw)).thenReturn(lesionList);

    ArrayList<ServiceMaster> serviceList = new ArrayList<>();
    serviceList.add(mock(ServiceMaster.class));
    org.mockito.MockedStatic<ServiceMaster> mockedService = mockStatic(ServiceMaster.class);
    mockedService.when(() -> ServiceMaster.getServiceMaster(additionalServicesRaw)).thenReturn(serviceList);

    ArrayList<Institute> instituteList = new ArrayList<>();
    instituteList.add(mock(Institute.class));
    org.mockito.MockedStatic<Institute> mockedInstitute = mockStatic(Institute.class);
    mockedInstitute.when(() -> Institute.getinstituteDetails(instituteDetailsRaw)).thenReturn(instituteList);

    String json = service.getCancerScreeningMasterDataForDoctor(123);
    assertNotNull(json);
    assertTrue(json.contains("preMalignantLesionTypes"));
    assertTrue(json.contains("higherHealthCare"));
    assertTrue(json.contains("additionalServices"));
    mockedLesion.close();
    mockedService.close();
    mockedInstitute.close();
    }

    // Helper to inject private fields
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to mock static method PreMalignantLesion.getPreMalignantLesionMasterData
    private void mockStaticPreMalignantLesion(ArrayList<Object[]> input, List<PreMalignantLesion> output) {
        try {
            org.mockito.MockedStatic<PreMalignantLesion> mocked = mockStatic(PreMalignantLesion.class);
            mocked.when(() -> PreMalignantLesion.getPreMalignantLesionMasterData(input)).thenReturn(output);
        } catch (Throwable ignored) {}
    }

    // Helper to mock static method ServiceMaster.getServiceMaster
    private void mockStaticServiceMaster(ArrayList<Object[]> input, List<ServiceMaster> output) {
        try {
            org.mockito.MockedStatic<ServiceMaster> mocked = mockStatic(ServiceMaster.class);
            mocked.when(() -> ServiceMaster.getServiceMaster(input)).thenReturn(output);
        } catch (Throwable ignored) {}
    }
}
