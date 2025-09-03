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
package com.iemr.mmu.data.reports;

import java.util.ArrayList;

public class DummyReportClasses {
    // Dummy classes for mocking static methods in tests
    public static class Report_PatientAttended {
        public static ArrayList<Report_PatientAttended> getPatientAttendedReport(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_TestConducted {
        public static ArrayList<Report_TestConducted> getTestConductedReport(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_LabTestsResult {
        public static ArrayList<Report_LabTestsResult> getLabTestResultReport(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_PatientInfo {
        public static ArrayList<Report_PatientInfo> getReport_PatientInfoReport(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_ChildrenCases {
        public static ArrayList<Report_ChildrenCases> getReport_ChildrenCasesReport(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_ANC {
        public static ArrayList<Report_ANC> getReport_ANC(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_ANCHighRisk {
        public static ArrayList<Report_ANCHighRisk> getReport_ANChighRisk(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
    public static class Report_ModifiedAnc {
        public static ArrayList<Report_ModifiedAnc> getReport_modifiedAnc(ArrayList<Object[]> rs) { return new ArrayList<>(); }
        public static ArrayList<Report_ModifiedAnc> getReport_modifiedAnc3(ArrayList<Object[]> rs) { return new ArrayList<>(); }
    }
}
