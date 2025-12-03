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
package com.iemr.mmu.service.reports;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.iemr.mmu.data.reports.Report_ANC;
import com.iemr.mmu.data.reports.Report_ANCHighRisk;
import com.iemr.mmu.data.reports.Report_ChildrenCases;
import com.iemr.mmu.data.reports.Report_LabTestsResult;
import com.iemr.mmu.data.reports.Report_ModifiedAnc;
import com.iemr.mmu.data.reports.Report_PatientAttended;
import com.iemr.mmu.data.reports.Report_PatientInfo;
import com.iemr.mmu.data.reports.Report_TestConducted;
import com.iemr.mmu.data.reports.ReportsRequestDigester;
import com.iemr.mmu.repo.reports.ReportMasterRepo;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.mapper.OutputMapper;

@Service
public class ReportCheckPostImplNew implements ReportCheckPost {

	@Autowired
	private ReportMasterRepo reportMasterRepo;

	public String getReportMaster(Integer serviceID) throws Exception {
		return new Gson().toJson(reportMasterRepo.findByServiceIDAndDeletedOrderByReportNameAsc(serviceID, false));
	}

	public String reportHandler(String requestOBJ) throws Exception {
		String reportData = "";
		ReportsRequestDigester request = InputMapper.gson().fromJson(requestOBJ, ReportsRequestDigester.class);
		if (request != null && request.getReportID() != null) {
			switch (request.getReportID()) {
			case 1:
				reportData = report_PatientAttended(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 2:
				reportData = report_TestConducted(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 5:
				reportData = report_ANC(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 6:
				reportData = report_Patientinfo(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 7:
				reportData = report_ChildrenCases(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 8:
				reportData = report_LabTestResult(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 9:
				reportData = report_ANCHighRisk(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			case 11:
				reportData = report_PatientVisitInfo(request.getFromDate(), request.getToDate(), request.getVanID(),
						request.getProviderServiceMapID());
				break;

			default:// default statement
			}
		} else
			throw new Exception("Invalid/NULL report ID");
		return reportData;
	}

	private String report_PatientAttended(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_PatientAttended> report_PA = new ArrayList<>();

		// Gson gson = new GsonBuilder().serializeNulls().create();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_PatientAttended(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_PA = Report_PatientAttended.getPatientAttendedReport(RS);
		}
		return OutputMapper.gson().toJson(report_PA);
	}

	private String report_TestConducted(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_TestConducted> report_TC = new ArrayList<>();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_TestConducted(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_TC = Report_TestConducted.getTestConductedReport(RS);
		}
		return OutputMapper.gson().toJson(report_TC);
	}

	private String report_LabTestResult(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_LabTestsResult> report_LTR = new ArrayList<>();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_LabTestResult(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_LTR = Report_LabTestsResult.getLabTestResultReport(RS);
		}
		return OutputMapper.gson().toJson(report_LTR);
	}

	private String report_Patientinfo(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_PatientInfo> report_PI = new ArrayList<>();

		// Gson gson = new GsonBuilder().serializeNulls().create();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_PatientInfo(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_PI = Report_PatientInfo.getReport_PatientInfoReport(RS);
		}
		return OutputMapper.gson().toJson(report_PI);
	}

	private String report_ChildrenCases(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_ChildrenCases> report_PI = new ArrayList<>();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_SP_ChildrenCases(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_PI = Report_ChildrenCases.getReport_ChildrenCasesReport(RS);
		}
		return OutputMapper.gson().toJson(report_PI);
	}

	private String report_ANC(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID) throws Exception {
		ArrayList<Report_ANC> report_ANC = new ArrayList<>();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_SP_ANC(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_ANC = Report_ANC.getReport_ANC(RS);
		}
		return OutputMapper.gson().toJson(report_ANC);
	}

	private String report_ANCHighRisk(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_ANCHighRisk> report_ANCHighRisk = new ArrayList<>();

		// Gson gson = new GsonBuilder().serializeNulls().create();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_SP_ANCHighRisk(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0)
				report_ANCHighRisk = Report_ANCHighRisk.getReport_ANChighRisk(RS);
		}
		return OutputMapper.gson().toJson(report_ANCHighRisk);
	}

	private String report_PatientVisitInfo(Timestamp fromDate, Timestamp toDate, Integer vanID, Integer psmID)
			throws Exception {
		ArrayList<Report_ModifiedAnc> report_PatientVisitInfo = new ArrayList<>();
		ArrayList<Report_ModifiedAnc> temp_reportRS = new ArrayList<>();
		ArrayList<Report_ModifiedAnc> temp_reportRS1 = new ArrayList<>();
		ArrayList<Object[]> FinalRS = new ArrayList<>();

		if (fromDate == null || toDate == null || vanID == null || psmID == null)
			throw new Exception("Some parameter/parameters is/are missing.");
		else {
			if (vanID == 0)
				vanID = null;
			int sno = 1;
			ArrayList<Object[]> RS = reportMasterRepo.get_report_SP_PatientVisitInfo(fromDate, toDate, psmID, vanID);
			if (RS != null && RS.size() > 0) {
				report_PatientVisitInfo = Report_ModifiedAnc.getReport_modifiedAnc(RS);
				for (Report_ModifiedAnc rmANC : report_PatientVisitInfo) {
					rmANC.setSno(sno);
					sno++;
				}

			}

			ArrayList<Object[]> RS1 = reportMasterRepo.get_report_SP_PhyVitals(fromDate, toDate, psmID, vanID);

			if (RS1 != null && RS1.size() > 0) {
				if (RS.size() > 0) {
					for (Object[] rs1 : RS1) {
						for (Report_ModifiedAnc rmANC : report_PatientVisitInfo) {
							if (rs1[3].equals(rmANC.getVisitCode()) && rs1[0].equals(rmANC.getBeneficiaryRegId())) {
								rmANC.setTemperature((BigDecimal) rs1[4]);
								rmANC.setPulseRate((Short) rs1[5]);
								rmANC.setRespiratoryRate((Short) rs1[6]);
								rmANC.setSystolicBP_1stReading((Short) rs1[7]);
								rmANC.setDiastolicBP_1stReading((Short) rs1[8]);
								rmANC.setAverageSystolicBP((Short) rs1[9]);
								rmANC.setAverageDiastolicBP((Short) rs1[10]);
								rmANC.setBloodGlucose_Random((Short) rs1[14]);
								rmANC.setWeight_Kg((BigDecimal) rs1[17]);
								rmANC.setHeight_cm((BigDecimal) rs1[18]);
								rmANC.setBmi((BigDecimal) rs1[19]);
								rmANC.setClinicalObservation((String) rs1[20]);
								rmANC.setOtherSymptoms((String) rs1[21]);
								rmANC.setTobaccoUseStatus((String) rs1[22]);
								rmANC.setAlcoholIntakeStatus((String) rs1[23]);
								rmANC.setChiefComplaint((String) rs1[24]);
								break;
							}
						}
					}
				} else {
					report_PatientVisitInfo.addAll(Report_ModifiedAnc.getReport_modifiedAnc3(RS1));
				}

			}

			ArrayList<Object[]> RS2 = reportMasterRepo.get_report_SP_LabTestresult(fromDate, toDate, psmID, vanID);

			if (RS2 != null && RS2.size() > 0) {
//				if (RS1.size() > 0) {
				for (Report_ModifiedAnc rmANC : report_PatientVisitInfo) {
					for (Object[] rs2 : RS2) {
						if (rs2[2].equals(rmANC.getVisitCode()) && rs2[0].equals(rmANC.getBeneficiaryRegId())) {

							if (rs2[5].equals("Urobilinogen")) {
								rmANC.setUrobilinogen((String) rs2[6]);
							}
							if (rs2[5].equals("Bilirubin")) {
								rmANC.setBilirubin((String) rs2[6]);
							}
							if (rs2[5].equals("Ketone bodies")) {
								rmANC.setKetoneBodies((String) rs2[6]);
							}
							if (rs2[5].equals("Glucose")) {
								rmANC.setGlucose((String) rs2[6]);
							}
							if (rs2[5].equals("Creatinine")) {
								rmANC.setCreatinine((String) rs2[6]);
							}
							if (rs2[5].equals("Albumin")) {
								rmANC.setAlbumin((String) rs2[6]);
							}
							if (rs2[5].equals("Calcium")) {
								rmANC.setCalcium((String) rs2[6]);
							}
							if (rs2[5].equals("Protein")) {
								rmANC.setProtein((String) rs2[6]);
							}
							if (rs2[5].equals("Leukocyte")) {
								rmANC.setLeukocyte((String) rs2[6]);
							}
							if (rs2[5].equals("Malaria") || rs2[5].equals("Dengue")) {
								rmANC.setRDTforMalaria_Dengue((String) rs2[6]);
							}
							if (rs2[5].equals("Random blood glucose")) {
								rmANC.setRBS((String) rs2[6]);
							}
							if (rs2[5].equals("Haemoglobin")) {
								rmANC.setHB((String) rs2[6]);
							}
							if (rs2[5].equals("Hemoglobin A1c")) {
								rmANC.setHba1c((String) rs2[6]);
							}
							if (rs2[5].equals("Urine Albumin")) {
								rmANC.setUrineAlbumin((String) rs2[6]);
							}
							if (rs2[5].equals("Urine sugar analysis set")) {
								rmANC.setUrineSugar((String) rs2[6]);
							}
							if (rs2[5].equals("Urine pregnancy test")) {
								rmANC.setUrinePregnancyTest((String) rs2[6]);
							}

							break;
						}
					}
				}

			}

			ArrayList<Object[]> RS3 = reportMasterRepo.get_report_SP_PrescribedDrug(fromDate, toDate, psmID, vanID);

			if (RS3 != null && RS3.size() > 0) {
				   for (Report_ModifiedAnc rmANC : report_PatientVisitInfo) {
					   int count = 1;
					   for (Object[] rs3 : RS3) {
						   if (rs3[3].equals(rmANC.getVisitCode()) && rs3[0].equals(rmANC.getBeneficiaryRegId()) && count < 11) {
							   rmANC.setReferredToInstituteID((Integer) rs3[10]);
							   rmANC.setReferredToInstitute((String) rs3[11]);
							   rmANC.setDiagnosisProvided((String) rs3[9]);
							   switch (count) {
								   case 1:
									   rmANC.setDrug_1((String) rs3[4]);
									   rmANC.setDrug_prescribed1((Integer) rs3[6]);
									   break;
								   case 2:
									   rmANC.setDrug_2((String) rs3[4]);
									   rmANC.setDrug_prescribed2((Integer) rs3[6]);
									   break;
								   case 3:
									   rmANC.setDrug_3((String) rs3[4]);
									   rmANC.setDrug_prescribed3((Integer) rs3[6]);
									   break;
								   case 4:
									   rmANC.setDrug_4((String) rs3[4]);
									   rmANC.setDrug_prescribed4((Integer) rs3[6]);
									   break;
								   case 5:
									   rmANC.setDrug_5((String) rs3[4]);
									   rmANC.setDrug_prescribed5((Integer) rs3[6]);
									   break;
								   case 6:
									   rmANC.setDrug_6((String) rs3[4]);
									   rmANC.setDrug_prescribed6((Integer) rs3[6]);
									   break;
								   case 7:
									   rmANC.setDrug_7((String) rs3[4]);
									   rmANC.setDrug_prescribed7((Integer) rs3[6]);
									   break;
								   case 8:
									   rmANC.setDrug_8((String) rs3[4]);
									   rmANC.setDrug_prescribed8((Integer) rs3[6]);
									   break;
								   case 9:
									   rmANC.setDrug_9((String) rs3[4]);
									   rmANC.setDrug_prescribed9((Integer) rs3[6]);
									   break;
								   case 10:
									   rmANC.setDrug_10((String) rs3[4]);
									   rmANC.setDrug_prescribed10((Integer) rs3[6]);
									   break;
								   default:
									   // default case intentionally left blank
							   }
							   count++;
						   }
					   }
				   }
			}

			return OutputMapper.gson().toJson(report_PatientVisitInfo);
		}
	}
}