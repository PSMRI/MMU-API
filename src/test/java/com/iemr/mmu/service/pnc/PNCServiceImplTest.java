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
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;

import com.google.gson.JsonObject;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.pnc.PNCDoctorServiceImpl;
import com.iemr.mmu.service.pnc.PNCNurseServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.service.anc.Utility;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PNCServiceImplTest {
    @Mock
    private CommonNurseServiceImpl commonNurseServiceImpl;
    @Mock
    private CommonDoctorServiceImpl commonDoctorServiceImpl;
    @Mock
    private PNCNurseServiceImpl pncNurseServiceImpl;
    @Mock
    private PNCDoctorServiceImpl pncDoctorServiceImpl;
    @Mock
    private TeleConsultationServiceImpl teleConsultationServiceImpl;
    @Mock
    private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
    @InjectMocks
    private PNCServiceImpl pncServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Add test methods for each public method in PNCServiceImpl here
}
