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
package com.iemr.mmu.service.ncdscreening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.ncdScreening.NCDScreening;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.repo.nurse.ncdscreening.NCDScreeningRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.tele_consultation.TeleConsultationServiceImpl;

class NCDScreeningNurseAndDoctorServiceTest {

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	@Nested
	@DisplayName("NCDScreeningNurseServiceImpl")
	class NurseService {

		@Mock
		private NCDScreeningRepo ncdScreeningRepo;

		@InjectMocks
		private NCDScreeningNurseServiceImpl service;

		@BeforeEach
		void setUp() {
			MockitoAnnotations.openMocks(this);
		}

		private NCDScreening screeningWithConditions() {
			NCDScreening screening = new NCDScreening();
			ArrayList<Map<String, Object>> conditions = new ArrayList<>();
			for (int i = 1; i <= 2; i++) {
				Map<String, Object> condition = new HashMap<>();
				condition.put("ncdScreeningConditionID", i);
				condition.put("screeningCondition", "condition" + i);
				conditions.add(condition);
			}
			screening.setNcdScreeningConditionList(conditions);
			return screening;
		}

		@Test
		void saveNCDScreeningDetails_flattensTheScreenedConditionsBeforeSaving() {
			NCDScreening screening = screeningWithConditions();
			NCDScreening stored = new NCDScreening();
			stored.setID(5L);
			when(ncdScreeningRepo.save(screening)).thenReturn(stored);

			assertEquals(5L, service.saveNCDScreeningDetails(screening));
			assertEquals("1,2", screening.getNcdScreeningConditionID());
			assertEquals("condition1,condition2", screening.getScreeningCondition());
		}

		@Test
		void saveNCDScreeningDetails_returnsNothingWhenTheRowWasNotStored() {
			NCDScreening screening = new NCDScreening();
			when(ncdScreeningRepo.save(screening)).thenReturn(null);

			assertNull(service.saveNCDScreeningDetails(screening));
		}

		@Test
		void getNCDScreeningDetails_splitsTheStoredConditionsBackIntoAList() {
			NCDScreening stored = new NCDScreening();
			stored.setNcdScreeningConditionID("1,2");
			stored.setScreeningCondition("condition1,condition2");
			stored.setNextScreeningDateDB(Timestamp.valueOf("2024-06-01 00:00:00"));
			when(ncdScreeningRepo.getNCDScreeningDetails(1L, 2L)).thenReturn(stored);

			String result = service.getNCDScreeningDetails(1L, 2L);

			assertEquals(2, stored.getNcdScreeningConditionList().size());
			assertTrue(result.contains("2024-06-01"), result);
		}

		@Test
		void getNCDScreeningDetails_leavesTheConditionListUnsetWhenNothingWasScreened() {
			NCDScreening stored = new NCDScreening();
			when(ncdScreeningRepo.getNCDScreeningDetails(1L, 2L)).thenReturn(stored);

			service.getNCDScreeningDetails(1L, 2L);

			assertNull(stored.getNcdScreeningConditionList());
		}

		@Test
		void getNCDScreeningDetails_ignoresConditionIdsAndNamesThatDoNotLineUp() {
			NCDScreening stored = new NCDScreening();
			stored.setNcdScreeningConditionID("1,2");
			stored.setScreeningCondition("condition1");
			when(ncdScreeningRepo.getNCDScreeningDetails(1L, 2L)).thenReturn(stored);

			service.getNCDScreeningDetails(1L, 2L);

			assertTrue(stored.getNcdScreeningConditionList().isEmpty());
		}

		@Test
		void updateNCDScreeningDetails_flattensTheConditionsBeforeUpdating() {
			NCDScreening screening = screeningWithConditions();
			when(ncdScreeningRepo.updateNCDScreeningDetails(anyString(), anyString(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateNCDScreeningDetails(screening));
			assertEquals("1,2", screening.getNcdScreeningConditionID());
		}
	}

	@Nested
	@DisplayName("NCDSCreeningDoctorServiceImpl")
	class DoctorService {

		@Mock
		private PrescriptionDetailRepo prescriptionDetailRepo;
		@Mock
		private CommonDoctorServiceImpl commonDoctorServiceImpl;
		@Mock
		private TeleConsultationServiceImpl teleConsultationServiceImpl;
		@Mock
		private CommonNurseServiceImpl commonNurseServiceImpl;

		@InjectMocks
		private NCDSCreeningDoctorServiceImpl service;

		@BeforeEach
		void setUp() {
			MockitoAnnotations.openMocks(this);
		}

		private String doctorRequest() {
			return "{\"beneficiaryRegID\":1,\"benVisitID\":2,\"visitCode\":3,\"providerServiceMapID\":4,"
					+ "\"createdBy\":\"doctor\",\"findings\":{},\"investigation\":{\"laboratoryList\":[{}],"
					+ "\"externalInvestigations\":\"MRI\"},\"diagnosis\":{\"prescriptionID\":7},"
					+ "\"prescription\":[{\"drugID\":1}],\"refer\":{}}";
		}

		private Map<String, Object> drugResult() {
			Map<String, Object> result = new HashMap<>();
			result.put("count", 1);
			result.put("prescribedDrugIDs", Collections.singletonList(9L));
			return result;
		}

		private void stubSuccessfulUpdate() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(1);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);
			when(commonNurseServiceImpl.saveBenInvestigation(any())).thenReturn(1L);
			when(commonNurseServiceImpl.saveBenPrescribedDrugsList(any())).thenReturn(drugResult());
			when(commonDoctorServiceImpl.updateBenReferDetails(any())).thenReturn(1L);
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);
		}

		@Test
		void updateDoctorData_updatesEverySectionAndAdvancesTheFlow() throws Exception {
			stubSuccessfulUpdate();

			assertEquals(1, service.updateDoctorData(json(doctorRequest())));
		}

		@Test
		void updateDoctorData_treatsEveryAbsentSectionAsAlreadyDone() throws Exception {
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(1);

			assertEquals(1, service.updateDoctorData(json("{\"investigation\":{}}")));
		}

		@Test
		void updateDoctorData_failsWhenTheBeneficiaryFlowCouldNotBeAdvanced() throws Exception {
			stubSuccessfulUpdate();
			when(commonDoctorServiceImpl.updateBenFlowtableAfterDocDataUpdate(any(), any(), any(), any(), any()))
					.thenReturn(0);

			assertThrows(RuntimeException.class, () -> service.updateDoctorData(json(doctorRequest())));
		}

		@Test
		void updateDoctorData_failsWhenASectionCouldNotBeUpdated() throws Exception {
			when(commonDoctorServiceImpl.updateDocFindings(any())).thenReturn(0);
			when(commonNurseServiceImpl.updatePrescription(any())).thenReturn(1);

			assertThrows(RuntimeException.class, () -> service.updateDoctorData(json(doctorRequest())));
		}

		@Test
		void getNCDDiagnosisData_splitsTheStoredDiagnosisBackIntoAList() {
			PrescriptionDetail stored = new PrescriptionDetail();
			stored.setDiagnosisProvided("Diabetes  ||  Hypertension");
			stored.setDiagnosisProvided_SCTCode("111  ||  222");
			when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L))
					.thenReturn(new ArrayList<>(Arrays.asList(stored)));

			String result = service.getNCDDiagnosisData(1L, 2L);

			assertEquals(2, stored.getProvisionalDiagnosisList().size());
			assertTrue(result.contains("Diabetes"), result);
		}

		@Test
		void getNCDDiagnosisData_returnsAnEmptyDiagnosisWhenNoneWasRecorded() {
			when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(new ArrayList<>());

			assertEquals("{}", service.getNCDDiagnosisData(1L, 2L));
		}

		@Test
		void getNCDDiagnosisData_leavesTheDiagnosisListUnsetWhenNoTermWasCoded() {
			PrescriptionDetail stored = new PrescriptionDetail();
			when(prescriptionDetailRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L))
					.thenReturn(new ArrayList<>(Arrays.asList(stored)));

			service.getNCDDiagnosisData(1L, 2L);

			assertNull(stored.getProvisionalDiagnosisList());
		}
	}
}
