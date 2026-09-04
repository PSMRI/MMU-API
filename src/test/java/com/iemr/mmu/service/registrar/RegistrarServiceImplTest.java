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
package com.iemr.mmu.service.registrar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.registrar.BeneficiaryData;
import com.iemr.mmu.data.registrar.BeneficiaryDemographicAdditional;
import com.iemr.mmu.data.registrar.BeneficiaryDemographicData;
import com.iemr.mmu.data.registrar.BeneficiaryImage;
import com.iemr.mmu.data.registrar.BeneficiaryPhoneMapping;
import com.iemr.mmu.data.registrar.V_BenAdvanceSearch;
import com.iemr.mmu.repo.registrar.BeneficiaryDemographicAdditionalRepo;
import com.iemr.mmu.repo.registrar.BeneficiaryImageRepo;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenData;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenDemoData;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenGovIdMapping;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenPhoneMapData;
import com.iemr.mmu.repo.registrar.RegistrarRepoBeneficiaryDetails;
import com.iemr.mmu.repo.registrar.ReistrarRepoBenSearch;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.utils.CookieUtil;

class RegistrarServiceImplTest {

	@Mock
	private RegistrarRepoBenData registrarRepoBenData;
	@Mock
	private RegistrarRepoBenDemoData registrarRepoBenDemoData;
	@Mock
	private RegistrarRepoBenPhoneMapData registrarRepoBenPhoneMapData;
	@Mock
	private RegistrarRepoBenGovIdMapping registrarRepoBenGovIdMapping;
	@Mock
	private ReistrarRepoBenSearch reistrarRepoBenSearch;
	@Mock
	private BeneficiaryDemographicAdditionalRepo beneficiaryDemographicAdditionalRepo;
	@Mock
	private RegistrarRepoBeneficiaryDetails registrarRepoBeneficiaryDetails;
	@Mock
	private BeneficiaryImageRepo beneficiaryImageRepo;
	@Mock
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	@Mock
	private CookieUtil cookieUtil;

	@InjectMocks
	private RegistrarServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(service, "registrationUrl", "http://registry/register");
		ReflectionTestUtils.setField(service, "registrarQuickSearchByIdUrl", "http://registry/searchById");
		ReflectionTestUtils.setField(service, "registrarQuickSearchByPhoneNoUrl", "http://registry/searchByPhone");
		ReflectionTestUtils.setField(service, "beneficiaryEditUrl", "http://registry/edit");
		ReflectionTestUtils.setField(service, "registrarAdvanceSearchUrl", "http://registry/advanceSearch");
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	/** A registration request with every optional field the mappers look for. */
	private static String fullBeneficiary() {
		return "{\"firstName\":\"Asha\",\"lastName\":\"Devi\",\"gender\":2,"
				+ "\"dob\":\"1990-05-04T00:00:00.000Z\",\"maritalStatus\":1,\"createdBy\":\"registrar\","
				+ "\"fatherName\":\"Ram\",\"husbandName\":\"Shyam\",\"aadharNo\":\"1234\","
				+ "\"beneficiaryRegID\":1,\"modifiedBy\":\"registrar\",\"countryID\":1,\"stateID\":2,"
				+ "\"districtID\":3,\"blockID\":4,\"servicePointID\":5,\"villageID\":6,\"community\":1,"
				+ "\"religion\":2,\"occupation\":3,\"educationQualification\":4,\"income\":5,"
				+ "\"benDemographicsID\":7,\"phoneNo\":\"9999999999\",\"benPhMapID\":8,"
				+ "\"literacyStatus\":\"Literate\",\"motherName\":\"Sita\",\"emailID\":\"a@b.c\","
				+ "\"bankName\":\"Bank\",\"branchName\":\"Branch\",\"IFSCCode\":\"IFSC1\","
				+ "\"accountNumber\":\"123\",\"ageAtMarriage\":20,\"age\":34,\"benDemoAdditionalID\":9,"
				+ "\"image\":\"base64\",\"benImageID\":10}";
	}

	@Nested
	@DisplayName("creating a beneficiary")
	class Create {

		@Test
		void createBeneficiary_storesTheMappedBeneficiary() {
			BeneficiaryData stored = new BeneficiaryData();
			when(registrarRepoBenData.save(any())).thenReturn(stored);
			assertEquals(stored, service.createBeneficiary(json(fullBeneficiary())));
		}

		@Test
		void createBeneficiaryDemographic_returnsTheStoredDemographicId() {
			BeneficiaryDemographicData stored = new BeneficiaryDemographicData();
			stored.setBenDemographicsID(5L);
			when(registrarRepoBenDemoData.save(any())).thenReturn(stored);
			assertEquals(5L, service.createBeneficiaryDemographic(json(fullBeneficiary()), 1L));

			when(registrarRepoBenDemoData.save(any())).thenReturn(null);
			assertNull(service.createBeneficiaryDemographic(json("{}"), 1L));
		}

		@Test
		void createBeneficiaryDemographicAdditional_returnsTheStoredId() {
			BeneficiaryDemographicAdditional stored = new BeneficiaryDemographicAdditional();
			stored.setBenDemoAdditionalID(6L);
			when(beneficiaryDemographicAdditionalRepo.save(any())).thenReturn(stored);
			assertEquals(6L, service.createBeneficiaryDemographicAdditional(json(fullBeneficiary()), 1L));

			when(beneficiaryDemographicAdditionalRepo.save(any())).thenReturn(null);
			assertNull(service.createBeneficiaryDemographicAdditional(json("{}"), 1L));
		}

		@Test
		void createBeneficiaryImage_returnsTheStoredBeneficiaryId() {
			BeneficiaryImage stored = new BeneficiaryImage();
			stored.setBeneficiaryRegID(1L);
			when(beneficiaryImageRepo.save(any())).thenReturn(stored);

			assertEquals(1L, service.createBeneficiaryImage(
					json("{\"image\":\"base64\",\"createdBy\":\"registrar\"}"), 1L));

			when(beneficiaryImageRepo.save(any())).thenReturn(null);
			assertNull(service.createBeneficiaryImage(json("{\"image\":null,\"createdBy\":null}"), 1L));
		}

		@Test
		void createBeneficiaryPhoneMapping_returnsTheStoredMappingId() {
			BeneficiaryPhoneMapping stored = new BeneficiaryPhoneMapping();
			stored.setBenPhMapID(7L);
			when(registrarRepoBenPhoneMapData.save(any())).thenReturn(stored);
			assertEquals(7L, service.createBeneficiaryPhoneMapping(json(fullBeneficiary()), 1L));

			when(registrarRepoBenPhoneMapData.save(any())).thenReturn(null);
			assertNull(service.createBeneficiaryPhoneMapping(json("{}"), 1L));
		}

		@Test
		void createBenGovIdMapping_reportsHowManyIdentitiesWereStored() {
			when(registrarRepoBenGovIdMapping.saveAll(any())).thenAnswer(invocation -> {
				List<?> saved = invocation.getArgument(0);
				return new ArrayList<>(saved);
			});

			String request = "{\"createdBy\":\"registrar\",\"govID\":[{\"type\":1,\"value\":\"1234\"},{}]}";
			assertEquals(2, service.createBenGovIdMapping(json(request), 1L));
		}
	}

	@Nested
	@DisplayName("mapping the request onto the stored beneficiary")
	class Mapping {

		@Test
		void getBenOBJ_readsEveryOptionalFieldOfTheRequest() {
			BeneficiaryData mapped = service.getBenOBJ(json(fullBeneficiary()));

			assertEquals("Asha", mapped.getFirstName());
			assertEquals("Devi", mapped.getLastName());
			assertEquals((short) 2, mapped.getGenderID());
			assertNotNull(mapped.getDob());
			assertEquals("Ram", mapped.getFatherName());
			assertEquals("Shyam", mapped.getSpouseName());
			assertEquals("1234", mapped.getAadharNo());
			assertEquals(1L, mapped.getBeneficiaryRegID());
			assertEquals("registrar", mapped.getModifiedBy());
		}

		@Test
		void getBenOBJ_leavesTheDateOfBirthUnsetWhenItCannotBeParsed() {
			assertNull(service.getBenOBJ(json("{\"dob\":\"not-a-date\"}")).getDob());
		}

		@Test
		void getBenOBJ_toleratesAnEmptyRequest() {
			assertNotNull(service.getBenOBJ(json("{}")));
			assertNull(service.getBenOBJ(json("{\"husbandName\":null,\"beneficiaryRegID\":null,"
					+ "\"modifiedBy\":null}")).getSpouseName());
		}

		@Test
		void getBenDemoOBJ_readsEveryOptionalFieldOfTheRequest() {
			BeneficiaryDemographicData mapped = service.getBenDemoOBJ(json(fullBeneficiary()), 1L);

			assertEquals(1, mapped.getCountryID());
			assertEquals(6, mapped.getDistrictBranchID());
			assertEquals((short) 5, mapped.getIncomeStatusID());
			assertEquals(7L, mapped.getBenDemographicsID());
		}

		@Test
		void getBenDemoOBJ_toleratesAnEmptyRequest() {
			assertEquals(1L, service.getBenDemoOBJ(json("{}"), 1L).getBeneficiaryRegID());
		}

		@Test
		void getBenPhoneOBJ_readsEveryOptionalFieldOfTheRequest() {
			BeneficiaryPhoneMapping mapped = service.getBenPhoneOBJ(json(fullBeneficiary()), 1L);

			assertEquals("9999999999", mapped.getPhoneNo());
			assertEquals(8L, mapped.getBenPhMapID());
			assertEquals("registrar", mapped.getModifiedBy());
		}

		@Test
		void getBenPhoneOBJ_toleratesAnEmptyRequest() {
			assertEquals(1L, service.getBenPhoneOBJ(json("{}"), 1L).getBenificiaryRegID());
		}
	}

	@Nested
	@DisplayName("searching and work lists")
	class Search {

		@Test
		void getRegWorkList_serialisesTheStoredWorklist() {
			when(registrarRepoBenData.getRegistrarWorkList(1)).thenReturn(new ArrayList<>());
			assertNotNull(service.getRegWorkList(1));
		}

		@Test
		void getQuickSearchBenData_serialisesTheMatchingBeneficiaries() {
			when(reistrarRepoBenSearch.getQuickSearch("BEN1")).thenReturn(new ArrayList<>());
			assertNotNull(service.getQuickSearchBenData("BEN1"));
		}

		@Test
		void getAdvanceSearchBenData_passesEveryProvidedCriterionToTheQuery() {
			V_BenAdvanceSearch criteria = new V_BenAdvanceSearch();
			criteria.setBeneficiaryID("BEN1");
			criteria.setFirstName("Asha");
			criteria.setLastName("Devi");
			criteria.setFatherName("Ram");
			criteria.setPhoneNo("9999999999");
			criteria.setAadharNo("1234");
			criteria.setGovtIdentityNo("GOV1");
			criteria.setStateID(2);
			criteria.setDistrictID(3);
			when(reistrarRepoBenSearch.getAdvanceBenSearchList("BEN1", "Asha", "Devi", "9999999999", "1234", "GOV1",
					"2", "3")).thenReturn(new ArrayList<>());

			assertNotNull(service.getAdvanceSearchBenData(criteria));
		}

		@Test
		void getAdvanceSearchBenData_fallsBackToWildcardsWhenNoCriterionWasGiven() {
			when(reistrarRepoBenSearch.getAdvanceBenSearchList("%%", "", "", "%%", "%%", "%%", "%%", "%%"))
					.thenReturn(new ArrayList<>());

			assertNotNull(service.getAdvanceSearchBenData(new V_BenAdvanceSearch()));
		}

		@Test
		void getAdvanceSearchBenData_returnsNothingWhenTheQueryFails() {
			when(reistrarRepoBenSearch.getAdvanceBenSearchList(anyString(), anyString(), anyString(), anyString(),
					anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException("db down"));

			assertEquals("", service.getAdvanceSearchBenData(new V_BenAdvanceSearch()));
		}
	}

	@Nested
	@DisplayName("reading a beneficiary")
	class Reads {

		/** One beneficiary-details row, wide enough for the details mapper. */
		private List<Object[]> detailRows(Boolean isGovType) {
			Object[] row = new Object[40];
			row[24] = Short.valueOf((short) 1);
			row[25] = "1234";
			row[26] = isGovType;
			List<Object[]> rows = new ArrayList<>();
			rows.add(row);
			return rows;
		}

		@Test
		void getBeneficiaryDetails_separatesGovernmentIdentitiesFromTheOthers() {
			when(registrarRepoBeneficiaryDetails.getBeneficiaryDetails(1L)).thenReturn(detailRows(Boolean.TRUE));
			when(beneficiaryImageRepo.getBenImage(1L)).thenReturn("base64");

			assertTrue(service.getBeneficiaryDetails(1L).contains("1234"));
		}

		@Test
		void getBeneficiaryDetails_recordsANonGovernmentIdentitySeparately() {
			when(registrarRepoBeneficiaryDetails.getBeneficiaryDetails(1L)).thenReturn(detailRows(Boolean.FALSE));

			assertNotNull(service.getBeneficiaryDetails(1L));
		}

		@Test
		void getBeneficiaryDetails_toleratesARowWithoutAnIdentityType() {
			when(registrarRepoBeneficiaryDetails.getBeneficiaryDetails(1L)).thenReturn(detailRows(null));

			assertNotNull(service.getBeneficiaryDetails(1L));
		}

		@Test
		void getBeneficiaryDetails_returnsNothingForAnUnknownBeneficiary() {
			when(registrarRepoBeneficiaryDetails.getBeneficiaryDetails(1L)).thenReturn(new ArrayList<>());
			assertNull(service.getBeneficiaryDetails(1L));
		}

		@Test
		void getBenImage_returnsTheStoredImage() {
			when(beneficiaryImageRepo.getBenImage(1L)).thenReturn("base64");
			assertTrue(service.getBenImage(1L).contains("base64"));

			when(beneficiaryImageRepo.getBenImage(2L)).thenReturn(null);
			assertEquals("{}", service.getBenImage(2L));
		}

		@Test
		void getBeneficiaryPersonalDetails_namesTheGenderAndAttachesTheServicePoint() {
			Object[] detailRow = new Object[30];
			detailRow[0] = 1L;
			List<Object[]> benRows = new ArrayList<>();
			benRows.add(detailRow);
			when(registrarRepoBenData.getBenDetailsByRegID(1L)).thenReturn(benRows);

			Object[] demoRow = new Object[10];
			demoRow[2] = "PHC Alpha";
			List<Object[]> demoRows = new ArrayList<>();
			demoRows.add(demoRow);
			when(registrarRepoBenDemoData.getBeneficiaryDemographicData(1L)).thenReturn(demoRows);

			BeneficiaryData details = service.getBeneficiaryPersonalDetails(1L);

			assertNotNull(details);
			assertEquals("PHC Alpha", details.getServicePointName());
		}

		@Test
		void getBeneficiaryPersonalDetails_returnsNothingForAnUnknownBeneficiary() {
			when(registrarRepoBenData.getBenDetailsByRegID(1L)).thenReturn(new ArrayList<>());
			when(registrarRepoBenDemoData.getBeneficiaryDemographicData(1L)).thenReturn(new ArrayList<>());

			assertNull(service.getBeneficiaryPersonalDetails(1L));
		}
	}

	@Nested
	@DisplayName("updating a beneficiary")
	class Updates {

		@Test
		void updateBeneficiary_updatesTheStoredBeneficiary() {
			when(registrarRepoBenData.updateBeneficiaryData(any(), any(), any(), any(), any(), any(), any(), any(),
					any(), any())).thenReturn(1);
			assertEquals(1, service.updateBeneficiary(json(fullBeneficiary())));
		}

		@Test
		void updateBeneficiaryDemographic_updatesTheStoredDemographics() {
			when(registrarRepoBenDemoData.updateBendemographicData(any(), any(), any(), any(), any(), any(), any(),
					any(), any(), any(), any(), any(), any())).thenReturn(1);
			assertEquals(1, service.updateBeneficiaryDemographic(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryPhoneMapping_updatesTheStoredPhoneNumber() {
			when(registrarRepoBenPhoneMapData.updateBenPhoneMap(any(), any(), any())).thenReturn(1);
			assertEquals(1, service.updateBeneficiaryPhoneMapping(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBenGovIdMapping_replacesTheStoredIdentities() {
			when(registrarRepoBenGovIdMapping.saveAll(any())).thenReturn(new ArrayList<>());

			assertEquals(0, service.updateBenGovIdMapping(json("{\"govID\":[]}"), 1L));
			verify(registrarRepoBenGovIdMapping).deletePreviousGovMapID(1L);
		}

		@Test
		void updateBeneficiaryDemographicAdditional_updatesTheStoredRowWhenOneExists() {
			when(beneficiaryDemographicAdditionalRepo.getBeneficiaryDemographicAdditional(1L))
					.thenReturn(new BeneficiaryDemographicAdditional());
			when(beneficiaryDemographicAdditionalRepo.updateBeneficiaryDemographicAdditional(any(), any(), any(),
					any(), any(), any(), any(), any(), any())).thenReturn(1);

			assertEquals(1, service.updateBeneficiaryDemographicAdditional(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryDemographicAdditional_insertsAFreshRowWhenNoneIsStoredYet() {
			BeneficiaryDemographicAdditional stored = new BeneficiaryDemographicAdditional();
			stored.setBenDemoAdditionalID(5L);
			when(beneficiaryDemographicAdditionalRepo.getBeneficiaryDemographicAdditional(1L)).thenReturn(null);
			when(beneficiaryDemographicAdditionalRepo.save(any())).thenReturn(stored);

			assertEquals(1, service.updateBeneficiaryDemographicAdditional(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryDemographicAdditional_reportsFailureWhenTheFreshRowWasNotStored() {
			BeneficiaryDemographicAdditional stored = new BeneficiaryDemographicAdditional();
			stored.setBenDemoAdditionalID(0L);
			when(beneficiaryDemographicAdditionalRepo.getBeneficiaryDemographicAdditional(1L)).thenReturn(null);
			when(beneficiaryDemographicAdditionalRepo.save(any())).thenReturn(stored);

			assertEquals(0, service.updateBeneficiaryDemographicAdditional(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryImage_updatesTheStoredImageWhenOneExists() {
			when(beneficiaryImageRepo.findBenImage(1L)).thenReturn(1L);
			when(beneficiaryImageRepo.updateBeneficiaryImage(anyString(), any(), anyLong())).thenReturn(1);

			assertEquals(1, service.updateBeneficiaryImage(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryImage_insertsAFreshImageWhenNoneIsStoredYet() {
			BeneficiaryImage stored = new BeneficiaryImage();
			stored.setBenImageID(5L);
			when(beneficiaryImageRepo.findBenImage(1L)).thenReturn(null);
			when(beneficiaryImageRepo.save(any())).thenReturn(stored);

			assertEquals(1, service.updateBeneficiaryImage(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryImage_reportsFailureWhenTheFreshImageWasNotStored() {
			BeneficiaryImage stored = new BeneficiaryImage();
			stored.setBenImageID(0L);
			when(beneficiaryImageRepo.findBenImage(1L)).thenReturn(null);
			when(beneficiaryImageRepo.save(any())).thenReturn(stored);

			assertEquals(0, service.updateBeneficiaryImage(json(fullBeneficiary()), 1L));
		}

		@Test
		void updateBeneficiaryImage_succeedsWithoutAnImageToStore() {
			assertEquals(1, service.updateBeneficiaryImage(json("{}"), 1L));
		}
	}

	@Nested
	@DisplayName("talking to the central identity service")
	class CentralIdentityService {

		@Test
		void registerBeneficiary_createsTheBeneficiaryFlowRecordForANewRegistration() throws Exception {
			String body = "{\"data\":{\"beneficiaryRegID\":1,\"beneficiaryID\":2}}";
			when(commonBenStatusFlowServiceImpl.createBenFlowRecord(anyString(), eq(1L), eq(2L))).thenReturn(1);

			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

				assertTrue(service.registerBeneficiary("{}", "auth", "token").contains("successfully registered"));
			}
		}

		@Test
		void registerBeneficiary_reportsAnErrorWhenTheFlowRecordCouldNotBeCreated() throws Exception {
			String body = "{\"data\":{\"beneficiaryRegID\":1,\"beneficiaryID\":2}}";
			when(commonBenStatusFlowServiceImpl.createBenFlowRecord(anyString(), eq(1L), eq(2L))).thenReturn(0);

			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

				assertTrue(service.registerBeneficiary("{}", "auth", "token").contains("contact administrator"));
			}
		}

		@Test
		void registerBeneficiary_reportsTheGenericFailureWhenTheRegistryRejectsTheRequest() throws Exception {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST)))) {

				assertTrue(service.registerBeneficiary("{}", "auth", "token").contains("FAILURE"));
			}
		}

		@Test
		void updateBeneficiary_passesTheBeneficiaryToTheNurseWhenTheRegistrarAskedForIt() throws Exception {
			when(commonBenStatusFlowServiceImpl.createBenFlowRecord(anyString(), eq(null), eq(null))).thenReturn(1);

			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>("", HttpStatus.OK)))) {

				assertEquals(1, service.updateBeneficiary("{\"passToNurse\":true}", "auth", "token"));
			}
		}

		@Test
		void updateBeneficiary_leavesTheBeneficiaryWithTheRegistrarByDefault() throws Exception {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>("", HttpStatus.OK)))) {

				assertEquals(1, service.updateBeneficiary("{\"passToNurse\":false}", "auth", "token"));
			}
		}

		@Test
		void updateBeneficiary_returnsNothingWhenTheRegistryRejectsTheRequest() throws Exception {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST)))) {

				assertNull(service.updateBeneficiary("{}", "auth", "token"));
			}
		}

		@Test
		void beneficiaryQuickSearch_searchesByBeneficiaryIdWhenOneWasGiven() {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>("byId", HttpStatus.OK)))) {

				assertEquals("byId", service.beneficiaryQuickSearch("{\"beneficiaryID\":\"BEN1\"}", "auth", "token"));
			}
		}

		@Test
		void beneficiaryQuickSearch_searchesByPhoneNumberWhenNoIdWasGiven() {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>("byPhone", HttpStatus.OK)))) {

				assertEquals("byPhone",
						service.beneficiaryQuickSearch("{\"phoneNo\":\"9999999999\"}", "auth", "token"));
			}
		}

		@Test
		void beneficiaryQuickSearch_returnsNothingWhenNoCriterionWasGiven() {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class)) {
				assertNull(service.beneficiaryQuickSearch("{}", "auth", "token"));
			}
		}

		@Test
		void beneficiaryAdvanceSearch_returnsTheRegistryResponse() {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>("matches", HttpStatus.OK)))) {

				assertEquals("matches", service.beneficiaryAdvanceSearch("{}", "auth", "token"));
			}
		}

		@Test
		void beneficiaryAdvanceSearch_returnsNothingWhenTheRegistryHasNoBody() {
			try (MockedConstruction<RestTemplate> rest = mockConstruction(RestTemplate.class,
					(mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
							.thenReturn(new ResponseEntity<>(HttpStatus.OK)))) {

				assertNull(service.beneficiaryAdvanceSearch("{}", "auth", "token"));
			}
		}

		@Test
		void searchAndSubmitBeneficiaryToNurse_createsTheBeneficiaryFlowRecord() throws Exception {
			when(commonBenStatusFlowServiceImpl.createBenFlowRecord("{}", null, null)).thenReturn(1);
			assertEquals(1, service.searchAndSubmitBeneficiaryToNurse("{}"));
		}
	}
}
