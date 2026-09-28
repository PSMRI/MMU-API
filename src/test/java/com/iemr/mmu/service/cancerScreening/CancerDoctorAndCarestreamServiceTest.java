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
package com.iemr.mmu.service.cancerScreening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.iemr.mmu.data.doctor.CancerDiagnosis;
import com.iemr.mmu.repo.doctor.CancerDiagnosisRepo;
import com.iemr.mmu.utils.CookieUtil;

class CancerDoctorAndCarestreamServiceTest {

	@Nested
	@DisplayName("CSDoctorServiceImpl")
	class DoctorService {

		@Mock
		private CancerDiagnosisRepo cancerDiagnosisRepo;

		@InjectMocks
		private CSDoctorServiceImpl service;

		@BeforeEach
		void setUp() {
			MockitoAnnotations.openMocks(this);
		}

		private CancerDiagnosis diagnosisWithServices(String... services) {
			CancerDiagnosis diagnosis = new CancerDiagnosis();
			diagnosis.setBeneficiaryRegID(1L);
			diagnosis.setVisitCode(2L);
			diagnosis.setRefrredToAdditionalServiceList(Arrays.asList(services));
			return diagnosis;
		}

		@Test
		void saveCancerDiagnosisData_flattensTheAdditionalServicesBeforeSaving() {
			CancerDiagnosis diagnosis = diagnosisWithServices("Radiology", "Oncology");
			CancerDiagnosis stored = new CancerDiagnosis();
			stored.setID(5L);
			when(cancerDiagnosisRepo.save(diagnosis)).thenReturn(stored);

			assertEquals(5L, service.saveCancerDiagnosisData(diagnosis));
			assertEquals("Radiology,Oncology", diagnosis.getRefrredToAdditionalService());
			assertEquals(5L, stored.getVanSerialNo());
		}

		@Test
		void saveCancerDiagnosisData_storesAnEmptyServiceListAsAnEmptyString() {
			CancerDiagnosis diagnosis = new CancerDiagnosis();
			when(cancerDiagnosisRepo.save(diagnosis)).thenReturn(null);

			assertNull(service.saveCancerDiagnosisData(diagnosis));
			assertEquals("", diagnosis.getRefrredToAdditionalService());
		}

		@Test
		void getBenCancerDiagnosisData_splitsTheStoredServicesBackIntoAList() {
			CancerDiagnosis stored = new CancerDiagnosis();
			stored.setRefrredToAdditionalService("Radiology,Oncology");
			when(cancerDiagnosisRepo.getBenCancerDiagnosisDetails(1L, 2L)).thenReturn(stored);

			assertEquals(List.of("Radiology", "Oncology"),
					service.getBenCancerDiagnosisData(1L, 2L).getRefrredToAdditionalServiceList());
		}

		@Test
		void getBenCancerDiagnosisData_namesTheReferredInstituteWhenOneIsLinked() {
			CancerDiagnosis stored = new CancerDiagnosis();
			com.iemr.mmu.data.institution.Institute institute = new com.iemr.mmu.data.institution.Institute();
			institute.setInstitutionName("District Hospital");
			stored.setInstitute(institute);
			when(cancerDiagnosisRepo.getBenCancerDiagnosisDetails(1L, 2L)).thenReturn(stored);

			assertEquals("District Hospital", service.getBenCancerDiagnosisData(1L, 2L).getReferredToInstituteName());
		}

		@Test
		void getBenCancerDiagnosisData_returnsNothingForABeneficiaryWithNoDiagnosis() {
			when(cancerDiagnosisRepo.getBenCancerDiagnosisDetails(1L, 2L)).thenReturn(null);

			assertNull(service.getBenCancerDiagnosisData(1L, 2L));
		}

		@Test
		void getBenDoctorEnteredDataForCaseSheet_carriesTheDiagnosis() {
			CancerDiagnosis stored = new CancerDiagnosis();
			when(cancerDiagnosisRepo.getBenCancerDiagnosisDetails(1L, 2L)).thenReturn(stored);

			assertEquals(stored, service.getBenDoctorEnteredDataForCaseSheet(1L, 2L).get("diagnosis"));
		}

		@Test
		void updateCancerDiagnosisDetailsByDoctor_updatesTheStoredRowWhenOneExists() {
			CancerDiagnosis diagnosis = diagnosisWithServices("Radiology");
			when(cancerDiagnosisRepo.getCancerDiagnosisStatuses(1L, 2L)).thenReturn("P");
			when(cancerDiagnosisRepo.updateCancerDiagnosisDetailsByDoctor(any(), any(), any(), anyString(), any(),
					any(), any(), eq("U"), anyLong(), anyLong())).thenReturn(1);

			assertEquals(1, service.updateCancerDiagnosisDetailsByDoctor(diagnosis));
		}

		@Test
		void updateCancerDiagnosisDetailsByDoctor_insertsAFreshRowWhenNoneIsStoredYet() {
			CancerDiagnosis diagnosis = diagnosisWithServices("Radiology");
			CancerDiagnosis stored = new CancerDiagnosis();
			stored.setID(5L);
			when(cancerDiagnosisRepo.getCancerDiagnosisStatuses(1L, 2L)).thenReturn(null);
			when(cancerDiagnosisRepo.save(diagnosis)).thenReturn(stored);

			assertEquals(1, service.updateCancerDiagnosisDetailsByDoctor(diagnosis));

			stored.setID(0L);
			assertEquals(0, service.updateCancerDiagnosisDetailsByDoctor(diagnosis));
		}
	}

	@Nested
	@DisplayName("CSCarestreamServiceImpl")
	class CarestreamService {

		@Mock
		private CookieUtil cookieUtil;

		@InjectMocks
		private CSCarestreamServiceImpl service;

		@BeforeEach
		void setUp() {
			MockitoAnnotations.openMocks(this);
			ReflectionTestUtils.setField(service, "carestreamOrderCreateURL", "http://carestream/order");
		}

		/** One beneficiary row as the flow-status query returns it. */
		private ArrayList<Object[]> beneficiary(String name, short genderID) {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[] { name, "Devi", Date.valueOf("1990-05-04"), genderID });
			return rows;
		}

		private MockedConstruction<RestTemplate> carestreamAnswering(String body) {
			return mockConstruction(RestTemplate.class,
					(mock, context) -> when(
							mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
									.thenReturn(new ResponseEntity<>(body, HttpStatus.OK)));
		}

		@Test
		void createMamographyRequest_reportsAnAcceptedOrder() {
			try (MockedConstruction<RestTemplate> rest = carestreamAnswering("{\"statusCode\":200}")) {
				assertEquals(1, service.createMamographyRequest(beneficiary("Asha Devi", (short) 2), 1L, 2L, "auth"));
			}
		}

		@Test
		void createMamographyRequest_reportsARejectedOrder() {
			try (MockedConstruction<RestTemplate> rest = carestreamAnswering("{\"statusCode\":500}")) {
				assertEquals(0, service.createMamographyRequest(beneficiary("Asha Devi", (short) 2), 1L, 2L, "auth"));
			}
		}

		@Test
		void createMamographyRequest_mapsEveryGenderCodeCarestreamExpects() {
			try (MockedConstruction<RestTemplate> rest = carestreamAnswering("{\"statusCode\":200}")) {
				assertEquals(1, service.createMamographyRequest(beneficiary("Ram", (short) 1), 1L, 2L, "auth"));
				assertEquals(1, service.createMamographyRequest(beneficiary("Asha Devi", (short) 2), 1L, 2L, "auth"));
				assertEquals(1, service.createMamographyRequest(beneficiary("Kiran", (short) 3), 1L, 2L, "auth"));
			}
		}

		@Test
		void createMamographyRequest_sendsAnEmptyOrderForABeneficiaryWithNoDetails() {
			try (MockedConstruction<RestTemplate> rest = carestreamAnswering("{\"statusCode\":200}")) {
				assertEquals(1, service.createMamographyRequest(new ArrayList<>(), 1L, 2L, "auth"));
			}
		}

		@Test
		void createMamographyRequest_reportsAnOrderThatCouldNotBeSent() {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(
							mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
									.thenThrow(new RuntimeException("carestream unreachable")))) {

				assertEquals(0, service.createMamographyRequest(beneficiary("Asha Devi", (short) 2), 1L, 2L, "auth"));
			}
		}
	}
}
