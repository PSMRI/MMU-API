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
package com.iemr.mmu.service.common.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.syncActivity_syncLayer.EmployeeSignature;
import com.iemr.mmu.data.syncActivity_syncLayer.DownloadedCaseSheet;
import com.iemr.mmu.data.common.DocFileManager;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.EmployeeSignatureRepo;
import com.iemr.mmu.repo.nurse.ncdscreening.IDRSDataRepo;
import com.iemr.mmu.repo.provider.ProviderServiceMappingRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.DownloadedCaseSheetRepo;
import com.iemr.mmu.service.anc.ANCServiceImpl;
import com.iemr.mmu.service.cancerScreening.CSNurseServiceImpl;
import com.iemr.mmu.service.cancerScreening.CSServiceImpl;
import com.iemr.mmu.service.covid19.Covid19ServiceImpl;
import com.iemr.mmu.service.generalOPD.GeneralOPDServiceImpl;
import com.iemr.mmu.service.ncdCare.NCDCareServiceImpl;
import com.iemr.mmu.service.ncdscreening.NCDScreeningServiceImpl;
import com.iemr.mmu.service.pnc.PNCServiceImpl;
import com.iemr.mmu.service.quickConsultation.QuickConsultationServiceImpl;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.exception.IEMRException;

class CommonServiceImplTest {

	@Mock
	private Covid19ServiceImpl covid19ServiceImpl;
	@Mock
	private AESEncryptionDecryption aESEncryptionDecryption;
	@Mock
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	@Mock
	private ANCServiceImpl ancServiceImpl;
	@Mock
	private PNCServiceImpl pncServiceImpl;
	@Mock
	private GeneralOPDServiceImpl generalOPDServiceImpl;
	@Mock
	private NCDCareServiceImpl ncdCareServiceImpl;
	@Mock
	private QuickConsultationServiceImpl quickConsultationServiceImpl;
	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;
	@Mock
	private CSNurseServiceImpl cSNurseServiceImpl;
	@Mock
	private CSServiceImpl csServiceImpl;
	@Mock
	private NCDScreeningServiceImpl ncdScreeningServiceImpl;
	@Mock
	private ProviderServiceMappingRepo providerServiceMappingRepo;
	@Mock
	private DownloadedCaseSheetRepo downloadedCaseSheetRepo;
	@Mock
	private IDRSDataRepo iDRSDataRepo;
	@Mock
	private EmployeeSignatureRepo employeeSignatureRepo;
	@Mock
	private CookieUtil cookieUtil;

	@InjectMocks
	private CommonServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(service, "mmuCentralServer", "http://central/casesheet");
		ReflectionTestUtils.setField(service, "tmCentralServer", "http://central/tm");
		ReflectionTestUtils.setField(service, "specialistSign", "http://central/sign");
	}

	@AfterEach
	void clearRequestContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	private static BeneficiaryFlowStatus flow(String visitCategory) {
		BeneficiaryFlowStatus flow = new BeneficiaryFlowStatus();
		flow.setVisitCategory(visitCategory);
		flow.setBeneficiaryRegID(1L);
		flow.setBenVisitCode(2L);
		flow.setVisitCode(2L);
		flow.setBenFlowID(3L);
		flow.setBenVisitID(4L);
		return flow;
	}

	@Nested
	@DisplayName("case sheet print data")
	class PrintData {

		@BeforeEach
		void stubTheLeftPanel() {
			when(beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(1L, 3L)).thenReturn(new ArrayList<>());
		}

		@Test
		void getCaseSheetPrintDataForBeneficiary_routesEachVisitCategoryToItsOwnPrintData() throws Exception {
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("ANC"), "auth").contains("nurseData"));
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("PNC"), "auth").contains("nurseData"));
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("General OPD"), "auth")
					.contains("nurseData"));
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("NCD care"), "auth").contains("nurseData"));
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("General OPD (QC)"), "auth")
					.contains("nurseData"));
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("COVID-19 Screening"), "auth")
					.contains("nurseData"));
			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("NCD screening"), "auth")
					.contains("nurseData"));
		}

		@Test
		void getCaseSheetPrintDataForBeneficiary_addsTheAnnotatedImagesForCancerScreening() throws Exception {
			when(cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(1L, 2L))
					.thenReturn(new ArrayList<>());

			assertTrue(service.getCaseSheetPrintDataForBeneficiary(flow("Cancer Screening"), "auth")
					.contains("ImageAnnotatedData"));
		}

		@Test
		void getCaseSheetPrintDataForBeneficiary_rejectsAnUnknownVisitCategory() throws Exception {
			assertEquals("Invalid VisitCategory",
					service.getCaseSheetPrintDataForBeneficiary(flow("Unknown"), "auth"));
		}
	}

	@Nested
	@DisplayName("past history reads")
	class PastHistoryReads {

		@Test
		void everyPastHistoryReadDelegatesToTheNurseService() throws Exception {
			when(commonNurseServiceImpl.fetchBenPastMedicalHistory(1L)).thenReturn("past");
			assertEquals("past", service.getBenPastHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenComorbidityHistory(1L)).thenReturn("comorbid");
			assertEquals("comorbid", service.getComorbidHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPersonalMedicationHistory(1L)).thenReturn("medication");
			assertEquals("medication", service.getMedicationHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPersonalTobaccoHistory(1L)).thenReturn("tobacco");
			assertEquals("tobacco", service.getPersonalTobaccoHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPersonalAlcoholHistory(1L)).thenReturn("alcohol");
			assertEquals("alcohol", service.getPersonalAlcoholHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPersonalAllergyHistory(1L)).thenReturn("allergy");
			assertEquals("allergy", service.getPersonalAllergyHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPersonalFamilyHistory(1L)).thenReturn("family");
			assertEquals("family", service.getFamilyHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPhysicalHistory(1L)).thenReturn("physical");
			assertEquals("physical", service.getBenPhysicalHistory(1L));

			when(commonNurseServiceImpl.fetchBenMenstrualHistory(1L)).thenReturn("menstrual");
			assertEquals("menstrual", service.getMenstrualHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPastObstetricHistory(1L)).thenReturn("obstetric");
			assertEquals("obstetric", service.getObstetricHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenImmunizationHistory(1L)).thenReturn("immunization");
			assertEquals("immunization", service.getImmunizationHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenOptionalVaccineHistory(1L)).thenReturn("vaccine");
			assertEquals("vaccine", service.getChildVaccineHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenPerinatalHistory(1L)).thenReturn("perinatal");
			assertEquals("perinatal", service.getBenPerinatalHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenFeedingHistory(1L)).thenReturn("feeding");
			assertEquals("feeding", service.getBenFeedingHistoryData(1L));

			when(commonNurseServiceImpl.fetchBenDevelopmentHistory(1L)).thenReturn("development");
			assertEquals("development", service.getBenDevelopmentHistoryData(1L));

			when(commonNurseServiceImpl.getBenSymptomaticData(1L)).thenReturn("symptomatic");
			assertEquals("symptomatic", service.getBenSymptomaticQuestionnaireDetailsData(1L));

			when(commonNurseServiceImpl.getBenPreviousDiabetesData(1L)).thenReturn("diabetes");
			assertEquals("diabetes", service.getBenPreviousDiabetesData(1L));

			when(commonNurseServiceImpl.getBenPreviousReferralData(1L)).thenReturn("referral");
			assertEquals("referral", service.getBenPreviousReferralData(1L));
		}

		@Test
		void getBenPreviousVisitDataForCaseRecord_looksTheVisitsUpAgainstEveryMmuProvider() throws Exception {
			when(providerServiceMappingRepo.getProviderServiceMapIdForServiceID((short) 2))
					.thenReturn(new ArrayList<>(Collections.singletonList(5)));
			when(beneficiaryFlowStatusRepo.getBenPreviousHistory(eq(1L), any())).thenReturn(new ArrayList<>());

			assertNotNull(service.getBenPreviousVisitDataForCaseRecord("{\"beneficiaryRegID\":1}"));
		}
	}

	@Nested
	@DisplayName("uploaded files")
	class Files {

		@TempDir
		Path uploadRoot;

		@Test
		void saveFiles_writesEachAttachmentUnderTheVanAndDateFolderAndEncryptsItsPath() throws Exception {
			ReflectionTestUtils.setField(service, "fileBasePath", uploadRoot.toString() + "/");
			when(aESEncryptionDecryption.encrypt(anyString())).thenReturn("encrypted");

			DocFileManager attachment = new DocFileManager();
			attachment.setVanID(7);
			attachment.setFileName("re*port.pdf");
			attachment.setFileExtension(".pdf");
			attachment.setFileContent(Base64.getEncoder().encodeToString("content".getBytes()));

			String response = service.saveFiles(Collections.singletonList(attachment));

			assertTrue(response.contains("encrypted"));
			assertTrue(response.contains("report.pdf"));
		}

		@Test
		void saveFiles_reusesTheDateFolderOnASecondUpload() throws Exception {
			ReflectionTestUtils.setField(service, "fileBasePath", uploadRoot.toString() + "/");
			when(aESEncryptionDecryption.encrypt(anyString())).thenReturn("encrypted");

			DocFileManager attachment = new DocFileManager();
			attachment.setVanID(7);
			attachment.setFileName("report.pdf");
			attachment.setFileExtension(".pdf");
			attachment.setFileContent(Base64.getEncoder().encodeToString("content".getBytes()));

			service.saveFiles(Collections.singletonList(attachment));
			assertTrue(service.saveFiles(Collections.singletonList(attachment)).contains("encrypted"));
		}

		@Test
		void saveFiles_skipsAnAttachmentWithoutANameOrExtension() throws Exception {
			ReflectionTestUtils.setField(service, "fileBasePath", uploadRoot.toString() + "/");

			DocFileManager attachment = new DocFileManager();
			attachment.setVanID(7);

			assertEquals("[]", service.saveFiles(Collections.singletonList(attachment)));
		}

		@Test
		void saveFiles_returnsNothingWhenNoAttachmentWasSent() throws Exception {
			ReflectionTestUtils.setField(service, "fileBasePath", uploadRoot.toString() + "/");
			assertEquals("[]", service.saveFiles(new ArrayList<>()));
		}

		@Test
		void loadFileAsResource_returnsAnExistingFile() throws Exception {
			Path file = uploadRoot.resolve("report.pdf");
			java.nio.file.Files.write(file, "content".getBytes());

			assertNotNull(service.loadFileAsResource("report.pdf", file.toString()));
		}

		@Test
		void loadFileAsResource_failsForAMissingFile() {
			assertThrows(IOException.class,
					() -> service.loadFileAsResource("gone.pdf", uploadRoot.resolve("gone.pdf").toString()));
		}
	}

	@Nested
	@DisplayName("teleconsultation case sheets")
	class TeleconsultationCaseSheets {

		private CommonServiceImpl serviceSpy;

		@BeforeEach
		void useASpyForTheOutboundCalls() {
			serviceSpy = spy(service);
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		}

		private ResponseEntity<String> ok(String body) {
			return new ResponseEntity<>(body, HttpStatus.OK);
		}

		@Test
		void checkIsCaseSheetDownloaded_reportsWhetherTheSheetWasAlreadyPulled() throws Exception {
			when(beneficiaryFlowStatusRepo.checkIsCaseSheetDownloaded(1L)).thenReturn(true);
			assertEquals(1, service.checkIsCaseSheetDownloaded(1L));

			when(beneficiaryFlowStatusRepo.checkIsCaseSheetDownloaded(1L)).thenReturn(false);
			assertEquals(0, service.checkIsCaseSheetDownloaded(1L));

			when(beneficiaryFlowStatusRepo.checkIsCaseSheetDownloaded(1L)).thenReturn(null);
			assertEquals(0, service.checkIsCaseSheetDownloaded(1L));
		}

		@Test
		void getTmVisitCode_readsTheTeleconsultationVisitOfAnMmuVisit() throws Exception {
			BeneficiaryFlowStatus tmVisit = new BeneficiaryFlowStatus();
			when(beneficiaryFlowStatusRepo.getTMVisitDetails(1L)).thenReturn(tmVisit);
			assertEquals(tmVisit, service.getTmVisitCode(1L));
		}

		@Test
		void getTmCaseSheet_returnsTheCaseSheetAndTheSpecialistSignature() throws Exception {
			String caseSheet = "{\"statusCode\":200,\"data\":{\"BeneficiaryData\":{\"tCSpecialistUserID\":5}}}";
			String signature = "{\"statusCode\":200,\"data\":{\"userID\":5}}";
			doReturn(ok(caseSheet)).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(), any());
			doReturn(ok(signature)).when(serviceSpy).restTemplateGet(anyString(), any(), any());

			ArrayList<String> result = serviceSpy.getTmCaseSheet(flow("NCD screening"), flow("NCD screening"), "auth");

			assertEquals(2, result.size());
		}

		@Test
		void getTmCaseSheet_returnsOnlyTheCaseSheetWhenNoSpecialistSigned() throws Exception {
			String caseSheet = "{\"statusCode\":200,\"data\":{\"BeneficiaryData\":{}}}";
			doReturn(ok(caseSheet)).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(), any());

			assertEquals(1,
					serviceSpy.getTmCaseSheet(flow("NCD screening"), flow("NCD screening"), "auth").size());
		}

		@Test
		void getTmCaseSheet_reportsTheLoginFailureFromTheCentralServer() {
			String error = "{\"statusCode\":5002,\"errorMessage\":\"Session expired\"}";
			doReturn(ok(error)).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(), any());

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> serviceSpy.getTmCaseSheet(flow("NCD screening"), flow("NCD screening"), "auth"));
			assertEquals(5002, thrown.getErrorCode());
		}

		@Test
		void getTmCaseSheet_reportsAnyOtherFailureFromTheCentralServer() {
			String error = "{\"statusCode\":5000,\"errorMessage\":\"Boom\"}";
			doReturn(ok(error)).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(), any());

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> serviceSpy.getTmCaseSheet(flow("NCD screening"), flow("NCD screening"), "auth"));
			assertEquals("Boom", thrown.getMessage());
		}

		@Test
		void getTmCaseSheet_failsWhenTheCentralServerRejectsTheRequest() {
			doReturn(new ResponseEntity<String>(HttpStatus.BAD_REQUEST)).when(serviceSpy)
					.restTemplatePost(anyString(), any(), anyString(), any());

			assertThrows(IEMRException.class,
					() -> serviceSpy.getTmCaseSheet(flow("NCD screening"), flow("NCD screening"), "auth"));
		}

		@Test
		void getTmCaseSheetOffline_returnsThePreviouslyDownloadedSheet() throws Exception {
			DownloadedCaseSheet stored = new DownloadedCaseSheet();
			stored.setTmCaseSheetResponse("sheet");
			when(downloadedCaseSheetRepo.getTmCaseSheetFromOffline(2L)).thenReturn(stored);

			assertEquals("sheet", service.getTmCaseSheetOffline(flow("NCD screening")));
		}

		@Test
		void getTmCaseSheetOffline_failsWhenNothingWasDownloadedYet() {
			when(downloadedCaseSheetRepo.getTmCaseSheetFromOffline(2L)).thenReturn(null);

			assertThrows(IEMRException.class, () -> service.getTmCaseSheetOffline(flow("NCD screening")));
		}

		@Test
		void getCaseSheetOfTm_returnsTheCaseSheetOnceTeleconsultationIsComplete() throws Exception {
			BeneficiaryFlowStatus tmVisit = new BeneficiaryFlowStatus();
			tmVisit.setSpecialist_flag((short) 9);
			when(beneficiaryFlowStatusRepo.getTMVisitDetails(any())).thenReturn(tmVisit);
			doReturn(new ArrayList<>(Collections.singletonList("sheet"))).when(serviceSpy).getTmCaseSheet(any(),
					any(), anyString());

			assertTrue(serviceSpy.getCaseSheetOfTm("{\"benVisitCode\":2}", "auth").contains("sheet"));
		}

		@Test
		void getCaseSheetOfTm_failsWhileTeleconsultationIsStillPending() {
			BeneficiaryFlowStatus tmVisit = new BeneficiaryFlowStatus();
			tmVisit.setSpecialist_flag((short) 1);
			when(beneficiaryFlowStatusRepo.getTMVisitDetails(any())).thenReturn(tmVisit);

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> service.getCaseSheetOfTm("{\"benVisitCode\":2}", "auth"));
			assertEquals("Tele-Consultation is not completed", thrown.getMessage());
		}

		@Test
		void getCaseSheetOfTm_failsWhileTheBeneficiaryIsStillInTheTeleconsultationWorklist() {
			when(beneficiaryFlowStatusRepo.getTMVisitDetails(any())).thenReturn(null);

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> service.getCaseSheetOfTm("{\"benVisitCode\":2}", "auth"));
			assertEquals("Patient is waiting in Tele-Medicine worklist", thrown.getMessage());
		}

		/** The central-server payload for a downloaded case sheet, with an optional signature. */
		private String centralPayload(boolean withSignature) {
			String caseSheet = "{\"nurseData\":{\"history\":{\"PhysicalActivityHistory\":{\"visitCode\":9,"
					+ "\"createdBy\":\"specialist\"}},\"idrs\":{\"IDRSDetail\":{\"confirmedDisease\":\"Diabetes\","
					+ "\"suspectedDisease\":\"Hypertension\"}}}}";
			String signature = withSignature ? ",{\"userID\":5}" : "";
			return "{\"statusCode\":200,\"data\":[" + caseSheet + signature + "]}";
		}

		@Test
		void getCaseSheetFromCentralServer_storesTheSheetTheSignatureAndTheScreeningOutcome() throws Exception {
			doReturn(ok(centralPayload(true))).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(),
					any());
			when(employeeSignatureRepo.findOneByUserID(5L)).thenReturn(null);
			when(downloadedCaseSheetRepo.save(any())).thenReturn(new DownloadedCaseSheet());
			when(beneficiaryFlowStatusRepo.updateDownloadFlag(2L)).thenReturn(1);
			when(iDRSDataRepo.updateConfirmedAndSuspectedDisease("Diabetes", "Hypertension", 2L)).thenReturn(1);

			assertTrue(serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth")
					.contains("PhysicalActivityHistory"));
			verify(employeeSignatureRepo).save(any());
		}

		@Test
		void getCaseSheetFromCentralServer_keepsAnAlreadyStoredSpecialistSignature() throws Exception {
			doReturn(ok(centralPayload(true))).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(),
					any());
			when(employeeSignatureRepo.findOneByUserID(5L)).thenReturn(new EmployeeSignature());
			when(downloadedCaseSheetRepo.save(any())).thenReturn(new DownloadedCaseSheet());
			when(beneficiaryFlowStatusRepo.updateDownloadFlag(2L)).thenReturn(1);
			when(iDRSDataRepo.updateConfirmedAndSuspectedDisease(any(), any(), anyLong())).thenReturn(1);

			assertNotNull(serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth"));
			verify(employeeSignatureRepo, org.mockito.Mockito.never()).save(any());
		}

		@Test
		void getCaseSheetFromCentralServer_failsWhenTheScreeningOutcomeCouldNotBeStored() throws Exception {
			doReturn(ok(centralPayload(false))).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(),
					any());
			when(downloadedCaseSheetRepo.save(any())).thenReturn(new DownloadedCaseSheet());
			when(beneficiaryFlowStatusRepo.updateDownloadFlag(2L)).thenReturn(1);
			when(iDRSDataRepo.updateConfirmedAndSuspectedDisease(any(), any(), anyLong())).thenReturn(0);

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth"));
			assertTrue(thrown.getMessage().contains("confirmed and suspected disease"));
		}

		@Test
		void getCaseSheetFromCentralServer_failsWhenTheDownloadFlagCouldNotBeStored() throws Exception {
			doReturn(ok(centralPayload(false))).when(serviceSpy).restTemplatePost(anyString(), any(), anyString(),
					any());
			when(downloadedCaseSheetRepo.save(any())).thenReturn(new DownloadedCaseSheet());
			when(beneficiaryFlowStatusRepo.updateDownloadFlag(2L)).thenReturn(0);

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth"));
			assertTrue(thrown.getMessage().contains("download flag"));
		}

		@Test
		void getCaseSheetFromCentralServer_reportsTheLoginFailureFromTheCentralServer() {
			doReturn(ok("{\"statusCode\":5002,\"errorMessage\":\"Session expired\"}")).when(serviceSpy)
					.restTemplatePost(anyString(), any(), anyString(), any());

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth"));
			assertEquals(5002, thrown.getErrorCode());
		}

		@Test
		void getCaseSheetFromCentralServer_reportsAnyOtherFailureFromTheCentralServer() {
			doReturn(ok("{\"statusCode\":5000,\"errorMessage\":\"Boom\"}")).when(serviceSpy)
					.restTemplatePost(anyString(), any(), anyString(), any());

			IEMRException thrown = assertThrows(IEMRException.class,
					() -> serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth"));
			assertEquals("Boom", thrown.getMessage());
		}

		@Test
		void getCaseSheetFromCentralServer_failsWhenTheCentralServerRejectsTheRequest() {
			doReturn(new ResponseEntity<String>(HttpStatus.BAD_REQUEST)).when(serviceSpy)
					.restTemplatePost(anyString(), any(), anyString(), any());

			assertThrows(IEMRException.class,
					() -> serviceSpy.getCaseSheetFromCentralServer("{\"visitCode\":2}", "auth"));
		}

		@Test
		void updateConfirmedDisease_delegatesToTheScreeningRepository() {
			when(iDRSDataRepo.updateConfirmedAndSuspectedDisease("Diabetes", "Hypertension", 2L)).thenReturn(1);
			assertEquals(1, service.updateConfirmedDisease("Diabetes", "Hypertension", 2L));
		}

		@Test
		void getJsonObj_parsesTheResponseBody() {
			assertTrue(service.getJsonObj(ok("{\"statusCode\":200}")).has("statusCode"));
		}
	}
}
