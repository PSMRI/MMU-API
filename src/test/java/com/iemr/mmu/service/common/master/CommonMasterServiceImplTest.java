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

import com.iemr.mmu.data.labtechnician.M_ECGabnormalities;
import com.iemr.mmu.repo.labtechnician.M_ECGabnormalitiesRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommonMasterServiceImplTest {
    private CommonMasterServiceImpl service;
    private NurseMasterDataServiceImpl nurseMasterDataServiceImpl;
    private ANCMasterDataServiceImpl ancMasterDataServiceImpl;
    private DoctorMasterDataServiceImpl doctorMasterDataServiceImpl;
    private RegistrarServiceMasterDataImpl registrarServiceMasterDataImpl;
    private NCDScreeningMasterServiceImpl ncdScreeningServiceImpl;
    private QCMasterDataServiceImpl qcMasterDataServiceImpl;
    private NCDCareMasterDataServiceImpl ncdCareMasterDataServiceImpl;
    private M_ECGabnormalitiesRepo m_ECGabnormalitiesRepo;

    @BeforeEach
    void setUp() {
        service = new CommonMasterServiceImpl();
        nurseMasterDataServiceImpl = mock(NurseMasterDataServiceImpl.class);
        ancMasterDataServiceImpl = mock(ANCMasterDataServiceImpl.class);
        doctorMasterDataServiceImpl = mock(DoctorMasterDataServiceImpl.class);
        registrarServiceMasterDataImpl = mock(RegistrarServiceMasterDataImpl.class);
        ncdScreeningServiceImpl = mock(NCDScreeningMasterServiceImpl.class);
        qcMasterDataServiceImpl = mock(QCMasterDataServiceImpl.class);
        ncdCareMasterDataServiceImpl = mock(NCDCareMasterDataServiceImpl.class);
        m_ECGabnormalitiesRepo = mock(M_ECGabnormalitiesRepo.class);
        // Inject dependencies
        service.setNurseMasterDataServiceImpl(nurseMasterDataServiceImpl);
        service.setAncMasterDataServiceImpl(ancMasterDataServiceImpl);
        service.setDoctorMasterDataServiceImpl(doctorMasterDataServiceImpl);
        service.setRegistrarServiceMasterDataImpl(registrarServiceMasterDataImpl);
        service.setNcdScreeningServiceImpl(ncdScreeningServiceImpl);
        service.setqCMasterDataServiceImpl(qcMasterDataServiceImpl);
        service.setNcdCareMasterDataServiceImpl(ncdCareMasterDataServiceImpl);
        setField(service, "m_ECGabnormalitiesRepo", m_ECGabnormalitiesRepo);
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

    @Test
    void testGetVisitReasonAndCategories_delegatesToNurseService() {
        when(nurseMasterDataServiceImpl.GetVisitReasonAndCategories()).thenReturn("{\"visitCategories\":[],\"visitReasons\":[]}");
        String result = service.getVisitReasonAndCategories();
        assertEquals("{\"visitCategories\":[],\"visitReasons\":[]}", result);
        verify(nurseMasterDataServiceImpl).GetVisitReasonAndCategories();
    }

    @Test
    void testGetMasterDataForNurse_cancerScreening() {
        when(nurseMasterDataServiceImpl.getCancerScreeningMasterDataForNurse()).thenReturn("cancer");
        String result = service.getMasterDataForNurse(1, 2, "F");
        assertEquals("cancer", result);
    }

    @Test
    void testGetMasterDataForNurse_ncdScreening() {
        when(ncdScreeningServiceImpl.getNCDScreeningMasterData(2, 3, "M")).thenReturn("ncd");
        String result = service.getMasterDataForNurse(2, 3, "M");
        assertEquals("ncd", result);
    }

    @Test
    void testGetMasterDataForNurse_anc_ncdcare_pnc_genopd() {
        when(ancMasterDataServiceImpl.getCommonNurseMasterDataForGenopdAncNcdcarePnc(anyInt(), anyInt(), anyString())).thenReturn("anc");
        assertEquals("anc", service.getMasterDataForNurse(3, 4, "F"));
        assertEquals("anc", service.getMasterDataForNurse(4, 4, "F"));
        assertEquals("anc", service.getMasterDataForNurse(5, 4, "F"));
        assertEquals("anc", service.getMasterDataForNurse(6, 4, "F"));
        assertEquals("anc", service.getMasterDataForNurse(8, 4, "F"));
        assertEquals("anc", service.getMasterDataForNurse(10, 4, "F"));
    }

    @Test
    void testGetMasterDataForNurse_qc() {
        String result = service.getMasterDataForNurse(7, 4, "F");
        assertEquals("No Master Data found for QuickConsultation", result);
    }

    @Test
    void testGetMasterDataForNurse_invalidOrNull() {
        assertEquals("Invalid VisitCategoryID", service.getMasterDataForNurse(99, 1, "F"));
        assertEquals("Invalid VisitCategoryID", service.getMasterDataForNurse(null, 1, "F"));
    }

    @Test
    void testGetMasterDataForDoctor_cancerScreening() {
        when(doctorMasterDataServiceImpl.getCancerScreeningMasterDataForDoctor(2)).thenReturn("cancer");
        String result = service.getMasterDataForDoctor(1, 2, "F", 3, 4);
        assertEquals("cancer", result);
    }

    @Test
    void testGetMasterDataForDoctor_anc_ncdcare_pnc_genopd() {
        when(ancMasterDataServiceImpl.getCommonDoctorMasterDataForGenopdAncNcdcarePnc(anyInt(), anyInt(), anyString(), anyInt(), anyInt())).thenReturn("anc");
        assertEquals("anc", service.getMasterDataForDoctor(2, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(3, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(4, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(5, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(6, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(7, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(8, 2, "F", 3, 4));
        assertEquals("anc", service.getMasterDataForDoctor(10, 2, "F", 3, 4));
    }

    @Test
    void testGetMasterDataForDoctor_invalidOrNull() {
        assertEquals("Invalid VisitCategoryID", service.getMasterDataForDoctor(99, 1, "F", 2, 3));
        assertEquals("Invalid VisitCategoryID", service.getMasterDataForDoctor(null, 1, "F", 2, 3));
    }

    @Test
    void testGetECGAbnormalities_returnsJson() throws Exception {
        List<M_ECGabnormalities> list = new ArrayList<>();
        M_ECGabnormalities abn = mock(M_ECGabnormalities.class);
        list.add(abn);
        when(m_ECGabnormalitiesRepo.findByDeleted(false)).thenReturn(list);
        String json = service.getECGAbnormalities();
        assertTrue(json.contains("[") && json.contains("]"));
    }

    @Test
    void testGetECGAbnormalities_exception() {
        try {
            when(m_ECGabnormalitiesRepo.findByDeleted(false)).thenThrow(new RuntimeException("fail"));
            service.getECGAbnormalities();
            fail("Should throw exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }
}
