package com.iemr.mmu.controller.doctor.main.anc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.iemr.mmu.controller.doctor.main.cancerScreening.InsertDoctorCSController;
import com.iemr.mmu.data.anc.WrapperAncFindings;
import com.iemr.mmu.data.anc.WrapperBenInvestigationANC;
import com.iemr.mmu.data.quickConsultation.PrescriptionDetail;
import com.iemr.mmu.service.anc.ANCServiceImpl;
import com.iemr.utils.mapper.InputMapper;
import com.iemr.utils.response.OutputResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
@RequestMapping({ "/doctor" })
public class InsertDoctorAncController {
	private OutputResponse response;
	private Logger logger = LoggerFactory.getLogger(InsertDoctorCSController.class);
	private ANCServiceImpl ancServiceImpl;

	@Autowired
	public void setAncServiceImpl(ANCServiceImpl ancServiceImpl) {
		this.ancServiceImpl = ancServiceImpl;
	}

	@CrossOrigin
	@ApiOperation(value = "Save doc anc Findings details", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = { "/save/anc/caseRecord/findings" }, method = { RequestMethod.POST })
	public String caseRecordFindings(@ApiParam(value = "") @RequestBody String requestObj) {
		response = new OutputResponse();
		try {
			if (requestObj != null) {
				WrapperAncFindings wrapperAncFindings = InputMapper.gson().fromJson(requestObj,
						WrapperAncFindings.class);

				Integer rd = ancServiceImpl.saveAncDocFindings(wrapperAncFindings);
			} else {
				response.setError(5000, "Data is not sufficient !!!");
			}
		} catch (Exception e) {
			logger.error("error in Save doc anc Findings details" + e);
			response.setError(e);
		}
		return null;
	}

	@CrossOrigin
	@ApiOperation(value = "save Beneficiary ANC Diagnosis", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = { "/save/anc/caseRecord/diagnosis" }, method = { RequestMethod.POST })
	public String saveBenANCDiagnosis(@ApiParam(value = "{\"beneficiaryRegID\":\"Long\",\"benVisitID\": \"Long\","
			+ "\"providerServiceMapID\":\"Integer\", \"diagnosisProvided\":\"String\", \"instruction\":\"String\", "
			+ "\"confirmatoryDiagnosis\":\"String\", \"remarks\":\"String\", \"createdBy\":\"String\"}") @RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();

		logger.info("saveBenANCDiagnosis request:" + requestObj);
		try {
			if (requestObj != null) {
				PrescriptionDetail prescriptionDetail = InputMapper.gson().fromJson(requestObj,
						PrescriptionDetail.class);
				Long r = ancServiceImpl.saveBenANCDiagnosis(prescriptionDetail);
				if (r > 0) {
					Map<String, Long> prescriptionMap = new HashMap<String, Long>();
					prescriptionMap.put("prescriptionID", r);
					String s = new Gson().toJson(prescriptionMap);
					response.setResponse(s);
				} else {
					response.setError(5000, "Something went wrong !!!");
				}
			} else {
				response.setError(5000, "Invalid request Data");
			}
		} catch (Exception e) {
		}
		return response.toString();
	}

	@CrossOrigin
	@ApiOperation(value = "save Beneficiary ANC Investigation", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = { "/save/doctorCasesheet/diagnosis" }, method = { RequestMethod.POST })
	public String saveBenAncInvestigation(@ApiParam(value = "") @RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();

		logger.info("save Beneficiary ANC Investigation request:" + requestObj);
		try {
			if (requestObj != null) {
				WrapperBenInvestigationANC wrapperBenInvestigationANC = InputMapper.gson().fromJson(requestObj,
						WrapperBenInvestigationANC.class);

				if (wrapperBenInvestigationANC != null) {
					
				}
			} else {
				response.setError(5000, "Invalid request Data");
			}
		} catch (Exception e) {
		}
		return response.toString();
	}

}