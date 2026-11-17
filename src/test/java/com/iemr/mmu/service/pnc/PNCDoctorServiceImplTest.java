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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.pnc.PNCDiagnosis;
import com.iemr.mmu.repo.nurse.pnc.PNCDiagnosisRepo;
import com.iemr.mmu.repo.quickConsultation.PrescriptionDetailRepo;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;

class PNCDoctorServiceImplTest {

	@Mock
	private PNCDiagnosisRepo pncDiagnosisRepo;
	@Mock
	private PrescriptionDetailRepo prescriptionDetailRepo;
	@Mock
	private CommonDoctorServiceImpl commonDoctorServiceImpl;

	@InjectMocks
	private PNCDoctorServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static JsonObject json(String raw) {
		return JsonParser.parseString(raw).getAsJsonObject();
	}

	/** A diagnosis with two provisional and two confirmatory terms, one of them un-coded. */
	private static String diagnosisRequest() {
		return "{\"beneficiaryRegID\":1,\"visitCode\":2,\"createdBy\":\"doctor\","
				+ "\"provisionalDiagnosisList\":[{\"term\":\"Anaemia\",\"conceptID\":\"111\"},"
				+ "{\"term\":\"Fever\"},{\"conceptID\":\"333\"}],"
				+ "\"confirmatoryDiagnosisList\":[{\"term\":\"Anaemia\"},"
				+ "{\"term\":\"Fever\",\"conceptID\":\"222\"}]}";
	}

	@Test
	@DisplayName("a saved diagnosis joins every named term and its concept id")
	void saveBenPNCDiagnosis_joinsEveryNamedTerm() throws Exception {
		PNCDiagnosis stored = new PNCDiagnosis();
		stored.setID(5L);
		when(pncDiagnosisRepo.save(any())).thenReturn(stored);

		assertEquals(5L, service.saveBenPNCDiagnosis(json(diagnosisRequest()), 7L));

		ArgumentCaptor<PNCDiagnosis> saved = ArgumentCaptor.forClass(PNCDiagnosis.class);
		org.mockito.Mockito.verify(pncDiagnosisRepo).save(saved.capture());
		assertEquals(7L, saved.getValue().getPrescriptionID());
		// The third entry carries no term, so the separator the second one appended is
		// left dangling.
		assertEquals("Anaemia  ||  Fever  ||  ", saved.getValue().getProvisionalDiagnosis());
		assertEquals("111  ||  N/A  ||  ", saved.getValue().getProvisionalDiagnosisSCTCode());
		assertEquals("Anaemia  ||  Fever", saved.getValue().getConfirmatoryDiagnosis());
		assertEquals("N/A  ||  222", saved.getValue().getConfirmatoryDiagnosisSCTCode());
	}

	@Test
	@DisplayName("a diagnosis with no terms is stored with empty diagnosis fields")
	void saveBenPNCDiagnosis_storesEmptyDiagnosisFieldsWhenNoTermWasGiven() throws Exception {
		PNCDiagnosis stored = new PNCDiagnosis();
		stored.setID(5L);
		when(pncDiagnosisRepo.save(any())).thenReturn(stored);

		assertEquals(5L, service.saveBenPNCDiagnosis(json("{\"beneficiaryRegID\":1}"), 7L));

		ArgumentCaptor<PNCDiagnosis> saved = ArgumentCaptor.forClass(PNCDiagnosis.class);
		org.mockito.Mockito.verify(pncDiagnosisRepo).save(saved.capture());
		assertEquals("", saved.getValue().getProvisionalDiagnosis());
	}

	@Test
	@DisplayName("a diagnosis that was not persisted reports no id")
	void saveBenPNCDiagnosis_reportsNoIdWhenNothingWasStored() throws Exception {
		PNCDiagnosis stored = new PNCDiagnosis();
		stored.setID(0L);
		when(pncDiagnosisRepo.save(any())).thenReturn(stored);

		assertNull(service.saveBenPNCDiagnosis(json(diagnosisRequest()), 7L));
	}

	@Test
	@DisplayName("a stored diagnosis is read back with its terms split into lists")
	void getPNCDiagnosisDetails_splitsTheStoredTermsBackIntoLists() {
		PNCDiagnosis stored = new PNCDiagnosis();
		stored.setProvisionalDiagnosis("Anaemia  ||  Fever");
		stored.setProvisionalDiagnosisSCTCode("111  ||  N/A");
		stored.setConfirmatoryDiagnosis("Anaemia");
		stored.setConfirmatoryDiagnosisSCTCode("N/A");
		when(pncDiagnosisRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L))
				.thenReturn(new ArrayList<>(Arrays.asList(stored)));
		when(prescriptionDetailRepo.getExternalinvestigationForVisitCode(1L, 2L)).thenReturn("X-ray");

		String result = service.getPNCDiagnosisDetails(1L, 2L);

		assertTrue(result.contains("X-ray"), result);
		assertEquals(2, stored.getProvisionalDiagnosisList().size());
		assertEquals(1, stored.getConfirmatoryDiagnosisList().size());
	}

	@Test
	@DisplayName("a beneficiary with no diagnosis reads back as an empty diagnosis")
	void getPNCDiagnosisDetails_returnsAnEmptyDiagnosisWhenNoneWasRecorded() {
		when(pncDiagnosisRepo.findByBeneficiaryRegIDAndVisitCode(1L, 2L)).thenReturn(new ArrayList<>());

		assertEquals("{}", service.getPNCDiagnosisDetails(1L, 2L));
	}

	@Test
	@DisplayName("an already-stored diagnosis is updated in place")
	void updateBenPNCDiagnosis_updatesTheStoredRow() throws Exception {
		PNCDiagnosis diagnosis = new PNCDiagnosis();
		diagnosis.setBeneficiaryRegID(1L);
		diagnosis.setVisitCode(2L);
		diagnosis.setPrescriptionID(7L);
		when(pncDiagnosisRepo.getPNCDiagnosisStatus(1L, 2L, 7L)).thenReturn("P");
		when(pncDiagnosisRepo.updatePNCDiagnosis(anyString(), anyString(), any(), any(), any(), any(), any(),
				eq("U"), anyLong(), anyLong(), anyString(), any(), anyString(), any(), anyLong())).thenReturn(1);

		assertEquals(1, service.updateBenPNCDiagnosis(diagnosis));
	}

	@Test
	@DisplayName("a diagnosis that was never stored is inserted instead")
	void updateBenPNCDiagnosis_insertsAFreshRowWhenNoneIsStoredYet() throws Exception {
		PNCDiagnosis diagnosis = new PNCDiagnosis();
		PNCDiagnosis stored = new PNCDiagnosis();
		stored.setID(5L);
		when(pncDiagnosisRepo.getPNCDiagnosisStatus(any(), any(), any())).thenReturn(null);
		when(pncDiagnosisRepo.save(diagnosis)).thenReturn(stored);

		assertEquals(1, service.updateBenPNCDiagnosis(diagnosis));

		stored.setID(0L);
		assertEquals(0, service.updateBenPNCDiagnosis(diagnosis));
	}

	@Test
	@DisplayName("an update joins every named term and its concept id")
	void updateBenPNCDiagnosis_joinsEveryNamedTerm() throws Exception {
		PNCDiagnosis diagnosis = com.iemr.mmu.utils.mapper.InputMapper.gson().fromJson(diagnosisRequest(),
				PNCDiagnosis.class);
		when(pncDiagnosisRepo.getPNCDiagnosisStatus(any(), any(), any())).thenReturn("N");
		when(pncDiagnosisRepo.updatePNCDiagnosis(anyString(), anyString(), any(), any(), any(), any(), any(),
				eq("N"), anyLong(), anyLong(), anyString(), any(), anyString(), any(), any())).thenReturn(1);

		assertEquals(1, service.updateBenPNCDiagnosis(diagnosis));
		assertEquals("Anaemia  ||  Fever  ||  ", diagnosis.getProvisionalDiagnosis());
		assertEquals("111  ||  N/A  ||  ", diagnosis.getProvisionalDiagnosisSCTCode());
	}
}
