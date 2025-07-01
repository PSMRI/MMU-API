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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.common.DocFileManager;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.syncActivity_syncLayer.DownloadedCaseSheet;
import com.iemr.mmu.data.syncActivity_syncLayer.EmployeeSignature;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.ncdscreening.IDRSDataRepo;
import com.iemr.mmu.repo.provider.ProviderServiceMappingRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.DownloadedCaseSheetRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.EmployeeSignatureRepo;
import com.iemr.mmu.service.anc.ANCServiceImpl;
import com.iemr.mmu.service.cancerScreening.CSNurseServiceImpl;
import com.iemr.mmu.service.cancerScreening.CSServiceImpl;
import com.iemr.mmu.service.covid19.Covid19ServiceImpl;
import com.iemr.mmu.service.generalOPD.GeneralOPDServiceImpl;
import com.iemr.mmu.service.ncdCare.NCDCareServiceImpl;
import com.iemr.mmu.service.ncdscreening.NCDScreeningServiceImpl;
import com.iemr.mmu.service.pnc.PNCServiceImpl;
import com.iemr.mmu.service.quickConsultation.QuickConsultationServiceImpl;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.RestTemplateUtil;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.mapper.InputMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
@PropertySource("classpath:application.properties")
public class CommonServiceImpl implements CommonService {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	@Value("${fileBasePath}")
	private String fileBasePath;

	@Value("${mmucentralserver}")
	private String mmuCentralServer;

	@Value("${tmCentralServer}")
	private String tmCentralServer;

	@Value("${specialistSign}")
	private String specialistSign;

	@Autowired
	private Covid19ServiceImpl covid19ServiceImpl;
	
	@Autowired
	private AESEncryptionDecryption aESEncryptionDecryption;

	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;
	private ANCServiceImpl ancServiceImpl;
	private PNCServiceImpl pncServiceImpl;
	private GeneralOPDServiceImpl generalOPDServiceImpl;
	private NCDCareServiceImpl ncdCareServiceImpl;
	private QuickConsultationServiceImpl quickConsultationServiceImpl;
	private CommonNurseServiceImpl commonNurseServiceImpl;
	private CSNurseServiceImpl cSNurseServiceImpl;
	private CSServiceImpl csServiceImpl;
	private NCDScreeningServiceImpl ncdScreeningServiceImpl;
	@Autowired
	private ProviderServiceMappingRepo providerServiceMappingRepo;

	@Autowired
	private DownloadedCaseSheetRepo downloadedCaseSheetRepo;

	@Autowired
	private IDRSDataRepo iDRSDataRepo;

	@Autowired
	private EmployeeSignatureRepo employeeSignatureRepo;
	@Autowired
	private CookieUtil cookieUtil;

	@Autowired
	public void setNcdScreeningServiceImpl(NCDScreeningServiceImpl ncdScreeningServiceImpl) {
		this.ncdScreeningServiceImpl = ncdScreeningServiceImpl;
	}

	@Autowired
	public void setCsServiceImpl(CSServiceImpl csServiceImpl) {
		this.csServiceImpl = csServiceImpl;
	}

	@Autowired
	public void setcSNurseServiceImpl(CSNurseServiceImpl cSNurseServiceImpl) {
		this.cSNurseServiceImpl = cSNurseServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}

	@Autowired
	public void setQuickConsultationServiceImpl(QuickConsultationServiceImpl quickConsultationServiceImpl) {
		this.quickConsultationServiceImpl = quickConsultationServiceImpl;
	}

	@Autowired
	public void setNcdCareServiceImpl(NCDCareServiceImpl ncdCareServiceImpl) {
		this.ncdCareServiceImpl = ncdCareServiceImpl;
	}

	@Autowired
	public void setGeneralOPDServiceImpl(GeneralOPDServiceImpl generalOPDServiceImpl) {
		this.generalOPDServiceImpl = generalOPDServiceImpl;
	}

	@Autowired
	public void setPncServiceImpl(PNCServiceImpl pncServiceImpl) {
		this.pncServiceImpl = pncServiceImpl;
	}

	@Autowired
	public void setAncServiceImpl(ANCServiceImpl ancServiceImpl) {
		this.ancServiceImpl = ancServiceImpl;
	}

	@Autowired
	public void setBeneficiaryFlowStatusRepo(BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo) {
		this.beneficiaryFlowStatusRepo = beneficiaryFlowStatusRepo;
	}

	public String getCaseSheetPrintDataForBeneficiary(BeneficiaryFlowStatus benFlowOBJ, String Authorization) throws Exception {
		String visitCategory = benFlowOBJ.getVisitCategory();
		String caseSheetData = null;

		if (null != visitCategory && visitCategory.length() > 0) {
			switch (visitCategory) {
			case "ANC": {
				caseSheetData = getANC_PrintData(benFlowOBJ);
			}
				break;
			case "PNC": {
				caseSheetData = getPNC_PrintData(benFlowOBJ);
			}
				break;
			case "General OPD": {
				caseSheetData = getGenOPD_PrintData(benFlowOBJ);
			}
				break;
			case "NCD care": {
				caseSheetData = getNCDcare_PrintData(benFlowOBJ);
			}
				break;
			case "General OPD (QC)": {
				caseSheetData = getQC_PrintData(benFlowOBJ);
			}
				break;
			case "Cancer Screening": {
				caseSheetData = getCancerScreening_PrintData(benFlowOBJ);
			}
				break;
			case "COVID-19 Screening": {
				caseSheetData = getCovid19_PrintData(benFlowOBJ);
			}
				break;

			case "NCD screening": {
				caseSheetData = getNCDScreening_PrintData(benFlowOBJ);
			}
				break;
			default: {
				caseSheetData = "Invalid VisitCategory";
			}
			}
		}
		return caseSheetData;
	}

	private String getCovid19_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData", covid19ServiceImpl.getBenCovidNurseData(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", covid19ServiceImpl
				.getBenCaseRecordFromDoctorCovid19(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 4);

		return caseSheetData.toString();
	}

	private String getANC_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData",
				ancServiceImpl.getBenANCNurseData(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", ancServiceImpl.getBenCaseRecordFromDoctorANC(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getCancerScreening_PrintData(BeneficiaryFlowStatus benFlowOBJ) {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData", csServiceImpl.getBenNurseDataForCaseSheet(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));
		caseSheetData.put("doctorData", csServiceImpl.getBenCaseRecordFromDoctorCS(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("ImageAnnotatedData",
				new Gson().toJson(cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(
						benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode())));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getGenOPD_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData", generalOPDServiceImpl.getBenGeneralOPDNurseData(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", generalOPDServiceImpl
				.getBenCaseRecordFromDoctorGeneralOPD(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getNCDcare_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData", ncdCareServiceImpl.getBenNCDCareNurseData(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", ncdCareServiceImpl
				.getBenCaseRecordFromDoctorNCDCare(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getPNC_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData",
				pncServiceImpl.getBenPNCNurseData(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", pncServiceImpl.getBenCaseRecordFromDoctorPNC(benFlowOBJ.getBeneficiaryRegID(),
				benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getQC_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData", quickConsultationServiceImpl
				.getBenQuickConsultNurseData(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", quickConsultationServiceImpl.getBenCaseRecordFromDoctorQuickConsult(
				benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getNCDScreening_PrintData(BeneficiaryFlowStatus benFlowOBJ) throws Exception {
		Map<String, Object> caseSheetData = new HashMap<>();

		caseSheetData.put("nurseData", ncdScreeningServiceImpl
				.getBenNCDScreeningNurseData(benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("doctorData", ncdScreeningServiceImpl.getBenCaseRecordFromDoctorNCDScreening(
				benFlowOBJ.getBeneficiaryRegID(), benFlowOBJ.getBenVisitCode()));

		caseSheetData.put("BeneficiaryData",
				getBenDetails(benFlowOBJ.getBenFlowID(), benFlowOBJ.getBeneficiaryRegID()));

		caseSheetData.put("serviceID", 2);

		return caseSheetData.toString();
	}

	private String getBenDetails(Long benFlowID, Long benRegID) {
		ArrayList<Object[]> tmpOBJ = beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(benRegID, benFlowID);
		BeneficiaryFlowStatus obj = BeneficiaryFlowStatus.getBeneficiaryFlowStatusForLeftPanel(tmpOBJ);
		return new Gson().toJson(obj);
	}

	// ------- Fetch beneficiary all past history data ------------------
	public String getBenPastHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPastMedicalHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all past history data ----------

	// ------- Fetch beneficiary all Comorbid conditions history data
	// ------------------
	public String getComorbidHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenComorbidityHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Comorbid conditions history data
	/// --------

	// ------- Fetch beneficiary all Medication history data -----------
	public String getMedicationHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPersonalMedicationHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Medication history data --

	// ------- Fetch beneficiary all Personal Tobacco history data
	// ---------------
	public String getPersonalTobaccoHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPersonalTobaccoHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Personal Tobacco history data------

	// ------- Fetch beneficiary all Personal Alcohol history data
	// ---------------
	public String getPersonalAlcoholHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPersonalAlcoholHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Personal Alcohol history data
	/// ------

	// ------- Fetch beneficiary all Personal Allergy history data
	// ---------------
	public String getPersonalAllergyHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPersonalAllergyHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Personal Allergy history data------

	// ------- Fetch beneficiary all Family history data ---------------
	public String getFamilyHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPersonalFamilyHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Family history data ------

	// ------- Fetch beneficiary all Physical history data ---------------
	public String getBenPhysicalHistory(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPhysicalHistory(beneficiaryRegID);
	}
/// ------- End of Fetch beneficiary all Physical history data ------

	// ------- Fetch beneficiary all Menstrual history data -----------
	public String getMenstrualHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenMenstrualHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Menstrual history data --

	// ------- Fetch beneficiary all past obstetric history data ---------------
	public String getObstetricHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPastObstetricHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all past obstetric history data ------

	// ------- Fetch beneficiary all Immunization history data ---------------
	public String getImmunizationHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenImmunizationHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Immunization history data ------

	// ------- Fetch beneficiary all Child Vaccine history data ---------------
	public String getChildVaccineHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenOptionalVaccineHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Child Vaccine history data ------

	// ------- Fetch beneficiary all Perinatal history data ---------------
	public String getBenPerinatalHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenPerinatalHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Perinatal history data ------

	// ------- Fetch beneficiary all Feeding history data ---------------
	public String getBenFeedingHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenFeedingHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all Feeding history data ------

	// ------- Fetch beneficiary all Development history data ---------------
	public String getBenDevelopmentHistoryData(Long beneficiaryRegID) {
		return commonNurseServiceImpl.fetchBenDevelopmentHistory(beneficiaryRegID);
	}

	// -- Fetch beneficiary previous visit details for case-record ---
	/**
	 * date : 08-09-2018
	 * 
	 * @throws IEMRException
	 */
	public String getBenPreviousVisitDataForCaseRecord(String comingRequest) throws IEMRException {
		CommonUtilityClass obj = InputMapper.gson().fromJson(comingRequest, CommonUtilityClass.class);
		// List<Short> flagList = new ArrayList<>();
		// flagList.add((short) 9);
		// flagList.add((short) 3);

		List<Integer> psmIDList = providerServiceMappingRepo.getProviderServiceMapIdForServiceID((short) 2);
		psmIDList.add(0);
		ArrayList<Object[]> resultList = beneficiaryFlowStatusRepo.getBenPreviousHistory(obj.getBeneficiaryRegID(),
				psmIDList);

		return new Gson().toJson(BeneficiaryFlowStatus.getBeneficiaryPastVisitHistory(resultList));
	}

	// end of Fetch beneficiary previous visit details for case-record

	// files upload/save start
	@Override
	public String saveFiles(List<DocFileManager> docFileManagerList) throws Exception {
		ArrayList<Map<String, String>> responseList = new ArrayList<>();
		// this will come from property file
		// String basePath = "C:/apps/Neeraj/mmuDoc";
		String basePath = fileBasePath;

		String currDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		if (docFileManagerList.size() > 0) {
			basePath += docFileManagerList.get(0).getVanID();
			File file = new File(basePath + "/" + currDate);

			if (file.isDirectory()) {
				responseList = createFile(docFileManagerList, basePath, currDate);
			} else {
				// create a new directory
				Files.createDirectories(Paths.get(basePath + "/" + currDate));
				responseList = createFile(docFileManagerList, basePath, currDate);
			}
		}
/*
 *
 *
 AN40085822 - Internal path disclosure -encryption
 *
 *
 */
		for (Map<String, String> obj : responseList) {
			String encryptedFilePath = aESEncryptionDecryption.encrypt(obj.get("filePath"));
			obj.put("filePath",encryptedFilePath);
		}
		return new Gson().toJson(responseList);
	}

	private ArrayList<Map<String, String>> createFile(List<DocFileManager> docFileManagerList, String basePath,
			String currDate) throws IOException {
		ArrayList<Map<String, String>> responseList = new ArrayList<>();
		Map<String, String> responseMap;
		FileOutputStream FOS = null;
		try
		{
		
		
		for (DocFileManager dFM : docFileManagerList) {
			if (dFM.getFileName() != null && dFM.getFileExtension() != null) {
				responseMap = new HashMap<>();
				dFM.setFileName(dFM.getFileName().replace("`", "").replace("'", "").replace("$", "").replace("\\", "")
						.replace("/", "").replace("~", "").replace("`", "").replace("!", "").replace("@", "")
						.replace("#", "").replace("$", "").replace("%", "").replace("^", "").replace("&", "")
						.replace("*", "").replace("(", "").replace(")", "").replace("{", "").replace("}", "")
						.replace("[", "").replace("]", "").replace("|", "").replace("\\", "").replace(":", "")
						.replace(";", "").replace("-", "").replace("_", "").replace("+", "").replace("=", "")
						.replace("\"", "").replace("'", ""));

				Long currTimestamp = System.currentTimeMillis();
				FOS = new FileOutputStream(
						basePath + "/" + currDate + "/" + currTimestamp + dFM.getFileName());

				FOS.write(Base64.getDecoder().decode(dFM.getFileContent()));

				responseMap.put("fileName", dFM.getFileName());
				responseMap.put("filePath", basePath + "/" + currDate + "/" + currTimestamp + dFM.getFileName());
//						+ System.currentTimeMillis() + dFM.getFileExtension());
//				returnOBJ[i] = basePath + "/" + currDate + "/" + dFM.getFileName() + System.currentTimeMillis()
//						+ dFM.getFileExtension();

				FOS.flush();
				
				
				responseList.add(responseMap);
			}
			
			if(FOS!=null)
    			FOS.close();
		}
		}
		catch(Exception e)
    	{
    		logger.error(e.getMessage());
    	}
    	finally
    	{
    		if(FOS!=null)
    			FOS.close();
    	}
		return responseList;
		
	}

	// load file as resource
	@Override
	public Resource loadFileAsResource(String file, String path) throws IOException {
		try {
			Path filePath = Paths.get(path).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists())
				return resource;
			else
				throw new FileNotFoundException();
		} catch (Exception e) {
			throw new FileNotFoundException();
		}
	}

	// End files upload/save start

	// get

	@Override
	public String getBenSymptomaticQuestionnaireDetailsData(Long beneficiaryRegID) throws Exception {
		return commonNurseServiceImpl.getBenSymptomaticData(beneficiaryRegID);

	}

	@Override
	public String getBenPreviousDiabetesData(Long beneficiaryRegID) throws Exception {
		return commonNurseServiceImpl.getBenPreviousDiabetesData(beneficiaryRegID);

	}

	public int checkIsCaseSheetDownloaded(Long mmuVisitCode) throws IEMRException {
		Boolean check = beneficiaryFlowStatusRepo.checkIsCaseSheetDownloaded(mmuVisitCode);
		if (check != null && check == true)
			return 1;
		else
			return 0;
	}

	public BeneficiaryFlowStatus getTmVisitCode(Long mmuVisitCode) throws IEMRException {
		BeneficiaryFlowStatus tmVisitCode = beneficiaryFlowStatusRepo.getTMVisitDetails(mmuVisitCode);
		return tmVisitCode;
//		return commonNurseServiceImpl.getBenPreviousDiabetesData(beneficiaryRegID);

	}

	public ArrayList<String> getTmCaseSheet(BeneficiaryFlowStatus TmBenFlowOBJ, BeneficiaryFlowStatus mmuBenFlowOBJ,
			String Authorization) throws IEMRException, Exception {

		ArrayList<String> caseSheetAndSign = new ArrayList<String>();

		HashMap<String, String> tmReqObj = new HashMap<String, String>();
		tmReqObj.put("visitCode", String.valueOf(TmBenFlowOBJ.getVisitCode()));
		tmReqObj.put("VisitCategory", String.valueOf(TmBenFlowOBJ.getVisitCategory()));
		tmReqObj.put("benFlowID", String.valueOf(TmBenFlowOBJ.getBenFlowID()));
		tmReqObj.put("benVisitID", String.valueOf(TmBenFlowOBJ.getBenVisitID()));
		tmReqObj.put("beneficiaryRegID", String.valueOf(TmBenFlowOBJ.getBeneficiaryRegID()));

		logger.info("TM print data request obj - " + new Gson().toJson(tmReqObj));
		HttpServletRequest requestHeader = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtTokenFromCookie = cookieUtil.getJwtTokenFromCookie(requestHeader);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

		headers.add("Cookie", "Jwttoken=" + jwtTokenFromCookie);

		// get TM case sheet by passing TM details
		ResponseEntity<String> response = restTemplatePost(tmCentralServer, Authorization, new Gson().toJson(tmReqObj), jwtTokenFromCookie);

		if (response.getStatusCodeValue() == 200 & response.hasBody()) {
			JsonObject jsnOBJ = getJsonObj(response);
			if (jsnOBJ.get("statusCode").getAsLong() == 5002) {
				throw new IEMRException(jsnOBJ.get("errorMessage").getAsString(), 5002);
			} else if (jsnOBJ.get("statusCode").getAsLong() != 200) {
				throw new IEMRException(jsnOBJ.get("errorMessage").getAsString());
			} else {
				// adding casesheet to array
				caseSheetAndSign.add(jsnOBJ.getAsJsonObject("data").toString());
				if (jsnOBJ.getAsJsonObject("data").getAsJsonObject("BeneficiaryData")
						.get("tCSpecialistUserID") != null) {
					int specialistUserID = jsnOBJ.getAsJsonObject("data").getAsJsonObject("BeneficiaryData")
							.get("tCSpecialistUserID").getAsInt();
					ResponseEntity<String> responseSign = restTemplateGet(specialistSign + "/" + specialistUserID,
							Authorization, jwtTokenFromCookie);
					JsonObject signJsonResponse = getJsonObj(responseSign);
					if (signJsonResponse.get("statusCode").getAsLong() == 200) {
						// adding sign response to array
						caseSheetAndSign.add(signJsonResponse.getAsJsonObject("data").toString());
					}

				}

			}

		} else
			throw new IEMRException("Error in getting the response from TM print API" + response.getBody());

		return caseSheetAndSign;
	}

	public String getTmCaseSheetOffline(BeneficiaryFlowStatus mmuBenFlowOBJ) throws IEMRException {

		String response = null;
		DownloadedCaseSheet caseSheetResponse = downloadedCaseSheetRepo
				.getTmCaseSheetFromOffline(mmuBenFlowOBJ.getVisitCode());

		if (caseSheetResponse != null)
			response = caseSheetResponse.getTmCaseSheetResponse();
		else
			throw new IEMRException("Error in fetching case Sheet in offline mode");

		return response;
	}

	@Override
	public String getBenPreviousReferralData(Long beneficiaryRegID) throws Exception {
		return commonNurseServiceImpl.getBenPreviousReferralData(beneficiaryRegID);

	}

	@Override
	public String getCaseSheetFromCentralServer(String mmuBenFlowReq, String authCentralServer) throws IEMRException {

		String tmCaseSheet = null;
		Long tmVisitCode = null;
		String createdBy = null;
		String confirmedDisease = null;
		String suspectedDisease = null;
		int updated = 0;
		int allDataUpdated = 0;
		HttpServletRequest requestHeader = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtTokenFromCookie = cookieUtil.getJwtTokenFromCookie(requestHeader);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Cookie", "Jwttoken=" + jwtTokenFromCookie);

		ResponseEntity<String> response = restTemplatePost(mmuCentralServer, authCentralServer, mmuBenFlowReq, jwtTokenFromCookie);

		if (response.getStatusCodeValue() == 200 & response.hasBody()) {

			JsonObject jsnOBJ = getJsonObj(response);
			if (jsnOBJ.get("statusCode").getAsLong() == 5002) {
				throw new IEMRException(jsnOBJ.get("errorMessage").getAsString(), 5002);
			} else if (jsnOBJ.get("statusCode").getAsLong() != 200) {
				throw new IEMRException(jsnOBJ.get("errorMessage").getAsString());
			} else {
				int sizeOfArray = jsnOBJ.get("data").getAsJsonArray().size();

				tmCaseSheet = jsnOBJ.get("data").getAsJsonArray().get(0).toString();
				JsonObject tmCaseSheetJsonObj = jsnOBJ.get("data").getAsJsonArray().get(0).getAsJsonObject();
				tmVisitCode = tmCaseSheetJsonObj.getAsJsonObject("nurseData").getAsJsonObject("history")
						.getAsJsonObject("PhysicalActivityHistory").get("visitCode").getAsLong();
				createdBy = tmCaseSheetJsonObj.getAsJsonObject("nurseData").getAsJsonObject("history")
						.getAsJsonObject("PhysicalActivityHistory").get("createdBy").getAsString();
				if (tmCaseSheetJsonObj.getAsJsonObject("nurseData").getAsJsonObject("idrs")
						.getAsJsonObject("IDRSDetail").get("confirmedDisease") != null) {
					confirmedDisease = tmCaseSheetJsonObj.getAsJsonObject("nurseData").getAsJsonObject("idrs")
							.getAsJsonObject("IDRSDetail").get("confirmedDisease").getAsString();
				}
				if (tmCaseSheetJsonObj.getAsJsonObject("nurseData").getAsJsonObject("idrs")
						.getAsJsonObject("IDRSDetail").get("suspectedDisease") != null) {
					suspectedDisease = tmCaseSheetJsonObj.getAsJsonObject("nurseData").getAsJsonObject("idrs")
							.getAsJsonObject("IDRSDetail").get("suspectedDisease").getAsString();
				}

				// checks whether signature is present or not.
				if (sizeOfArray == 2) {
					String signatureRes = jsnOBJ.get("data").getAsJsonArray().get(1).toString();
					EmployeeSignature specialistSign = InputMapper.gson().fromJson(signatureRes,
							EmployeeSignature.class);

					JsonObject signJsonObj = jsnOBJ.get("data").getAsJsonArray().get(1).getAsJsonObject();
					EmployeeSignature userSign = employeeSignatureRepo.findOneByUserID(specialistSign.getUserID());
					if (userSign == null) {
						employeeSignatureRepo.save(specialistSign);
					}
				}

			}
			if (tmCaseSheet != null) {
				BeneficiaryFlowStatus objMMU = InputMapper.gson().fromJson(mmuBenFlowReq, BeneficiaryFlowStatus.class);
				DownloadedCaseSheet saveTmCaseSheetRes = new DownloadedCaseSheet();
				saveTmCaseSheetRes.setTmVisitCode(tmVisitCode);
				saveTmCaseSheetRes.setMmuVisitCode(objMMU.getVisitCode());
				saveTmCaseSheetRes.setTmCaseSheetResponse(tmCaseSheet);
				saveTmCaseSheetRes.setCreatedBy(createdBy);

				DownloadedCaseSheet responseDownloaded = downloadedCaseSheetRepo.save(saveTmCaseSheetRes);

				if (responseDownloaded != null) {
					updated = beneficiaryFlowStatusRepo.updateDownloadFlag(objMMU.getVisitCode());
					if (updated > 0) {
						int diseaseUpdated = updateConfirmedDisease(confirmedDisease, suspectedDisease,
								saveTmCaseSheetRes.getMmuVisitCode());
						if (diseaseUpdated > 0) {
							allDataUpdated = 1;
						} else if (diseaseUpdated == 0)
							throw new IEMRException("Error in updating the confirmed and suspected disease");
					} else if (updated == 0)
						throw new IEMRException("Error in updating the download flag for mmu visitcode");
				}

			}
		} else
			throw new IEMRException("Error in getting data from central server - " + response.getBody());
		if (allDataUpdated > 0)
			return tmCaseSheet;
		else
			throw new IEMRException("Error in showing the caseSheet");

	}

	public int updateConfirmedDisease(String confirmedDiasese, String suspectedDisease, Long MMUVisitCode) {
		int diseaseUpdated = iDRSDataRepo.updateConfirmedAndSuspectedDisease(confirmedDiasese, suspectedDisease,
				MMUVisitCode);
		return diseaseUpdated;
	}

	@Override
	public String getCaseSheetOfTm(String mmuBenFlowReq, String authCentralServer) throws Exception {

		ArrayList<String> casesheetData = null;
		BeneficiaryFlowStatus objMMU = InputMapper.gson().fromJson(mmuBenFlowReq, BeneficiaryFlowStatus.class);

		// gets TM visit Code
		BeneficiaryFlowStatus tmVisitCodeObj = getTmVisitCode(objMMU.getBenVisitCode());

		if (tmVisitCodeObj != null) {
			if (tmVisitCodeObj.getSpecialist_flag() == 9) {
				casesheetData = getTmCaseSheet(tmVisitCodeObj, objMMU, authCentralServer);
				return casesheetData.toString();
			} else
				throw new IEMRException("Tele-Consultation is not completed");

		} else
			throw new IEMRException("Patient is waiting in Tele-Medicine worklist");

	}

	public ResponseEntity<String> restTemplatePost(String URL, String authorization, String reqObj, String token) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(reqObj, authorization, token);
		ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, request, String.class);
		return response;
	}

	public ResponseEntity<String> restTemplateGet(String URL, String authorization, String token) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity("", authorization, token);
		ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, request, String.class);
		return response;
	}

	public JsonObject getJsonObj(ResponseEntity<String> convertToJsonObj) {
		String jsonToConvert = convertToJsonObj.getBody();
		JsonObject jsnOBJ = new JsonObject();
		JsonParser jsnParser = new JsonParser();
		JsonElement jsnElmnt = jsnParser.parse(jsonToConvert);
		jsnOBJ = jsnElmnt.getAsJsonObject();
		return jsnOBJ;
	}
}
