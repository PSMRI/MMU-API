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
import com.iemr.mmu.data.masterdata.ncdcare.NCDCareType;
import com.iemr.mmu.data.masterdata.ncdscreening.NCDScreeningCondition;
import com.iemr.mmu.repo.masterrepo.ncdCare.NCDCareTypeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NCDCareMasterDataServiceImplTest {
    private NCDCareMasterDataServiceImpl service;
    private NCDScreeningMasterServiceImpl ncdScreeningMasterServiceImpl;
    private NCDCareTypeRepo ncdCareTypeRepo;

    @BeforeEach
    void setUp() {
        service = new NCDCareMasterDataServiceImpl();
        ncdScreeningMasterServiceImpl = mock(NCDScreeningMasterServiceImpl.class);
        ncdCareTypeRepo = mock(NCDCareTypeRepo.class);
        service.setNcdScreeningMasterServiceImpl(ncdScreeningMasterServiceImpl);
        service.setNcdCareTypeRepo(ncdCareTypeRepo);
    }

    @Test
    void testGetNCDCareMasterData_returnsExpectedJson() {
        // Mock data for screening conditions
        ArrayList<Object[]> screeningConditionsRaw = new ArrayList<>();
        screeningConditionsRaw.add(new Object[]{1, "Condition1"});
        when(ncdScreeningMasterServiceImpl.getNCDScreeningConditions()).thenReturn(screeningConditionsRaw);
    List<NCDScreeningCondition> screeningConditions = Collections.singletonList(new NCDScreeningCondition(1, "Condition1"));
        mockStaticNCDScreeningCondition(screeningConditionsRaw, screeningConditions);

        // Mock data for care types
        ArrayList<Object[]> careTypesRaw = new ArrayList<>();
        careTypesRaw.add(new Object[]{1, "CareType1"});
        when(ncdCareTypeRepo.getNCDCareTypes()).thenReturn(careTypesRaw);
    List<NCDCareType> careTypes = Collections.singletonList(new NCDCareType(1, "CareType1"));
        mockStaticNCDCareType(careTypesRaw, careTypes);

        String json = service.getNCDCareMasterData();
        assertNotNull(json);
        assertTrue(json.contains("ncdCareConditions"));
        assertTrue(json.contains("ncdCareTypes"));
    }

    // Helper to mock static method NCDScreeningCondition.getNCDScreeningCondition
    private void mockStaticNCDScreeningCondition(ArrayList<Object[]> input, List<NCDScreeningCondition> output) {
    // no-op
        // Use Mockito's inline mocking for static methods if available
        try {
            org.mockito.MockedStatic<NCDScreeningCondition> mocked = mockStatic(NCDScreeningCondition.class);
            mocked.when(() -> NCDScreeningCondition.getNCDScreeningCondition(input)).thenReturn(output);
        } catch (Throwable ignored) {}
    }

    // Helper to mock static method NCDCareType.getNCDCareTypes
    private void mockStaticNCDCareType(ArrayList<Object[]> input, List<NCDCareType> output) {
    // no-op
        try {
            org.mockito.MockedStatic<NCDCareType> mocked = mockStatic(NCDCareType.class);
            mocked.when(() -> NCDCareType.getNCDCareTypes(input)).thenReturn(output);
        } catch (Throwable ignored) {}
    }
}
