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
package com.iemr.mmu.data.labModule;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;
import com.iemr.mmu.utils.AESEncryption.AESEncryptionDecryption;

@Entity
@Table(name = "t_lab_testresult")
public class LabResultEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	@Expose
	private Long ID;

	@Column(name = "BeneficiaryRegID")
	@Expose
	private Long beneficiaryRegID;

	@Column(name = "BenVisitID")
	@Expose
	private Long benVisitID;

	@Column(name = "ProviderServiceMapID")
	@Expose
	private Integer providerServiceMapID;

	@Column(name = "VisitCode")
	@Expose
	private Long visitCode;

	@Column(name = "PrescriptionID")
	@Expose
	private Long prescriptionID;

	@Column(name = "ProcedureID")
	@Expose
	private Integer procedureID;

	@Column(name = "TestComponentID")
	@Expose
	private Integer testComponentID;

	@Column(name = "TestResultValue")
	@Expose
	private String testResultValue;

	@Column(name = "TestResultUnit")
	@Expose
	private String testResultUnit;

	@Column(name = "TestReportFilePath")
	@Expose
	private String testReportFilePath;

	@Column(name = "Remarks")
	@Expose
	private String remarks;

	@Transient
	private List<Map<String, Object>> compList;

	@Expose
	@Column(name = "Deleted", insertable = false)
	private Boolean deleted;

	@Expose
	@Column(name = "Processed", insertable = false)
	private String processed;

	@Expose
	@Column(name = "CreatedBy")
	private String createdBy;

	@Expose
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;

	@Expose
	@Column(name = "ModifiedBy")
	private String modifiedBy;

	@Expose
	@Column(name = "LastModDate", insertable = false)
	private Timestamp lastModDate;

	@Expose
	@Column(name = "VanSerialNo")
	private Long vanSerialNo;

	@Expose
	@Column(name = "VehicalNo")
	private String vehicalNo;

	@Expose
	@Column(name = "vanID")
	private Integer vanID;

	@Expose
	@Column(name = "ParkingPlaceID")
	private Integer parkingPlaceID;

	@Expose
	@Column(name = "SyncedBy")
	private String syncedBy;

	@Expose
	@Column(name = "SyncedDate")
	private Timestamp syncedDate;

	@Expose
	@Column(name = "ReservedForChange")
	private String reservedForChange;

	@Transient
	private Boolean labCompleted;

	@Expose
	@ManyToOne
	@JoinColumn(name = "testComponentID", insertable = false)
	private TestComponentMaster testComponentMaster;

	@Expose
	@ManyToOne
	@JoinColumn(name = "procedureID", insertable = false)
	private ProcedureData procedureData;

	@Expose
	@Transient
	private String procedureName;

	@Expose
	@Transient
	private String procedureType;

	@Expose
	@Transient
	private ArrayList<Map<String, Object>> componentList;

	@Expose
	@Transient
	private String[] fileIDs;

	@Expose
	@Transient
	private Date date;

	@Expose
	@Column(name = "stripsNotAvailable")
	private Boolean stripsNotAvailable;

	@Expose
	@Column(name = "Ecgabnormalities")
	private String ecgAbnormalitiesDB;

	@Expose
	@Transient
	private String[] ecgAbnormalities;

	public String getEcgAbnormalitiesDB() {
		return ecgAbnormalitiesDB;
	}

	public void setEcgAbnormalitiesDB(String ecgAbnormalitiesDB) {
		this.ecgAbnormalitiesDB = ecgAbnormalitiesDB;
	}

	public String[] getEcgAbnormalities() {
		return ecgAbnormalities;
	}

	public void setEcgAbnormalities(String[] ecgAbnormalities) {
		this.ecgAbnormalities = ecgAbnormalities;
	}

	public Boolean getStripsNotAvailable() {
		return stripsNotAvailable;
	}

	public void setStripsNotAvailable(Boolean stripsNotAvailable) {
		this.stripsNotAvailable = stripsNotAvailable;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public LabResultEntry(Long visitCode, Date createdDate) {
		super();
		this.visitCode = visitCode;
		this.date = createdDate;
	}

	public static ArrayList<LabResultEntry> getVisitCodeAndDate(ArrayList<Object[]> resultSetArr) {
		LabResultEntry obj;
		ArrayList<LabResultEntry> returnArr = new ArrayList<>();
		if (resultSetArr.size() > 0) {
			for (Object[] arr : resultSetArr) {
				BigInteger vCode = (BigInteger) arr[0];
				obj = new LabResultEntry(vCode.longValue(), (Date) arr[1]);
				returnArr.add(obj);
			}
		}
		return returnArr;
	}

	public ArrayList<Map<String, Object>> getComponentList() {
		return componentList;
	}

	public void setComponentList(ArrayList<Map<String, Object>> componentList) {
		this.componentList = componentList;
	}

	public String getProcedureType() {
		return procedureType;
	}

	public void setProcedureType(String procedureType) {
		this.procedureType = procedureType;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public static ArrayList<LabResultEntry> getLabResultEntry(ArrayList<LabResultEntry> comingList) throws Exception {
		ArrayList<LabResultEntry> returnList = new ArrayList<>();
		Integer procedureId = null;
		Map<String, Object> compDetails = null;
		ArrayList<Map<String, Object>> componentList = null;
		AESEncryptionDecryption aESEncryptionDecryption = new AESEncryptionDecryption();

		LabResultEntry tmpOBJ;

		if (comingList != null && comingList.size() > 0) {
			for (LabResultEntry obj : comingList) {
				if (procedureId == null || obj.getProcedureID().intValue() != procedureId) {

					procedureId = obj.getProcedureID();

					tmpOBJ = new LabResultEntry();
					tmpOBJ.setPrescriptionID(obj.getPrescriptionID());
					tmpOBJ.setProcedureID(obj.getProcedureID());
					tmpOBJ.setProcedureName(obj.getProcedureData().getProcedureName());
					tmpOBJ.setProcedureType(obj.getProcedureData().getProcedureType());
					tmpOBJ.setCreatedDate(obj.getCreatedDate());

					compDetails = new HashMap<String, Object>();
					// compDetails.put("resultEntryDate", obj.getCreatedDate());
					compDetails.put("testComponentID", obj.getTestComponentID());
					compDetails.put("componentName", obj.getTestComponentMaster().getTestComponentName());
					// Shubham Shekhar,16-11-2020,lionic code added to casesheet
					compDetails.put("loincName", obj.getTestComponentMaster().getLionicNum());
					compDetails.put("loincTerm", obj.getTestComponentMaster().getLionicTerm());

					compDetails.put("testResultValue", obj.getTestResultValue());
					compDetails.put("testResultUnit", obj.getTestResultUnit());
					compDetails.put("testReportFilePath", obj.getTestReportFilePath());
					compDetails.put("stripsNotAvailable", obj.getStripsNotAvailable());

					if (obj.getEcgAbnormalitiesDB() != null) {
						compDetails.put("ecgAbnormalitiesDB", obj.getEcgAbnormalitiesDB());
						compDetails.put("ecgAbnormalities", obj.getEcgAbnormalitiesDB().split("\\|\\|"));
					}

					// file id array from string
					// String fileIds[];
					Map<String, String> fileMap;
					ArrayList<Map<String, String>> fileList = new ArrayList<>();
					if (obj.getTestReportFilePath() != null && obj.getTestReportFilePath().trim().length() > 0) {
						String fileIdsTemp[] = obj.getTestReportFilePath().split(",");
						// fileIds = new String[fileIdsTemp.length];
						for (String str : fileIdsTemp) {
							if (str != null && str.trim().length() > 0) {

								// DE40034072,20-04-2022, decrypting internal file path
								String decryptedFilePath = null;
								decryptedFilePath = aESEncryptionDecryption.decrypt(str);
								String[] tempArr = decryptedFilePath.split("\\/");
								fileMap = new HashMap<>();
								fileMap.put("filePath", str);
								fileMap.put("fileName", tempArr[tempArr.length - 1]);

								fileList.add(fileMap);
							}
						}
					}

					compDetails.put("fileIDs", fileList);

					compDetails.put("remarks", obj.getRemarks());

					componentList = new ArrayList<>();
					componentList.add(compDetails);
					tmpOBJ.setComponentList(componentList);

					returnList.add(tmpOBJ);

				} else {
					compDetails = new HashMap<String, Object>();
					// compDetails.put("resultEntryDate", obj.getCreatedDate());
					compDetails.put("testComponentID", obj.getTestComponentID());
					compDetails.put("componentName", obj.getTestComponentMaster().getTestComponentName());
					// Shubham Shekhar,16-11-2020,lionic code added to casesheet
					compDetails.put("loincName", obj.getTestComponentMaster().getLionicNum());
					compDetails.put("loincTerm", obj.getTestComponentMaster().getLionicTerm());

					compDetails.put("testResultValue", obj.getTestResultValue());
					compDetails.put("testResultUnit", obj.getTestResultUnit());
					compDetails.put("testReportFilePath", obj.getTestReportFilePath());
					compDetails.put("stripsNotAvailable", obj.getStripsNotAvailable());
					
					if (obj.getEcgAbnormalitiesDB() != null) {
						compDetails.put("ecgAbnormalitiesDB", obj.getEcgAbnormalitiesDB());
						compDetails.put("ecgAbnormalities", obj.getEcgAbnormalitiesDB().split("\\|\\|"));
					}

					// file id array from string
					// String fileIds[];
					Map<String, String> fileMap;
					ArrayList<Map<String, String>> fileList = new ArrayList<>();
					if (obj.getTestReportFilePath() != null && obj.getTestReportFilePath().trim().length() > 0) {
						String fileIdsTemp[] = obj.getTestReportFilePath().split(",");
						// fileIds = new String[fileIdsTemp.length];
						for (String str : fileIdsTemp) {
							if (str != null && str.trim().length() > 0) {
								// DE40034072,20-04-2022, decrypting internal file path
								String decryptedFilePath = null;
								decryptedFilePath = aESEncryptionDecryption.decrypt(str);
								String[] tempArr = decryptedFilePath.split("\\/");
								fileMap = new HashMap<>();
								fileMap.put("filePath", str);
								fileMap.put("fileName", tempArr[tempArr.length - 1]);

								fileList.add(fileMap);
							}
						}
					}

					compDetails.put("fileIDs", fileList);

					compDetails.put("remarks", obj.getRemarks());
					componentList.add(compDetails);
				}

				// tmpOBJ = new LabResultEntry();
				// tmpOBJ.setProcedureID(obj.getProcedureID());
				// tmpOBJ.setProcedureName(obj.getProcedureData().getProcedureName());
				// tmpOBJ.setProcedureType(obj.getProcedureData().getProcedureType());
				// tmpOBJ.setTestComponentID(obj.getTestComponentID());
				// tmpOBJ.setComponentName(obj.getTestComponentMaster().getTestComponentName());
				// tmpOBJ.setPrescriptionID(obj.getPrescriptionID());
				// tmpOBJ.setTestResultValue(obj.getTestResultValue());
				// tmpOBJ.setTestResultUnit(obj.getTestResultUnit());
				// tmpOBJ.setTestReportFilePath(obj.getTestReportFilePath());

				// returnList.add(tmpOBJ);

			}
		}
		return returnList;

	}

	public LabResultEntry() {

	}

	public TestComponentMaster getTestComponentMaster() {
		return testComponentMaster;
	}

	public void setTestComponentMaster(TestComponentMaster testComponentMaster) {
		this.testComponentMaster = testComponentMaster;
	}

	public ProcedureData getProcedureData() {
		return procedureData;
	}

	public void setProcedureData(ProcedureData procedureData) {
		this.procedureData = procedureData;
	}

	public Boolean getLabCompleted() {
		return labCompleted;
	}

	public void setLabCompleted(Boolean labCompleted) {
		this.labCompleted = labCompleted;
	}

	public Long getID() {
		return ID;
	}

	public void setID(Long iD) {
		ID = iD;
	}

	public Long getBeneficiaryRegID() {
		return beneficiaryRegID;
	}

	public void setBeneficiaryRegID(Long beneficiaryRegID) {
		this.beneficiaryRegID = beneficiaryRegID;
	}

	public Long getBenVisitID() {
		return benVisitID;
	}

	public void setBenVisitID(Long benVisitID) {
		this.benVisitID = benVisitID;
	}

	public Integer getProviderServiceMapID() {
		return providerServiceMapID;
	}

	public void setProviderServiceMapID(Integer providerServiceMapID) {
		this.providerServiceMapID = providerServiceMapID;
	}

	public Long getVisitCode() {
		return visitCode;
	}

	public void setVisitCode(Long visitCode) {
		this.visitCode = visitCode;
	}

	public Long getPrescriptionID() {
		return prescriptionID;
	}

	public void setPrescriptionID(Long prescriptionID) {
		this.prescriptionID = prescriptionID;
	}

	public Integer getProcedureID() {
		return procedureID;
	}

	public void setProcedureID(Integer procedureID) {
		this.procedureID = procedureID;
	}

	public Integer getTestComponentID() {
		return testComponentID;
	}

	public void setTestComponentID(Integer testComponentID) {
		this.testComponentID = testComponentID;
	}

	public String getTestResultValue() {
		return testResultValue;
	}

	public void setTestResultValue(String testResultValue) {
		this.testResultValue = testResultValue;
	}

	public String getTestResultUnit() {
		return testResultUnit;
	}

	public void setTestResultUnit(String testResultUnit) {
		this.testResultUnit = testResultUnit;
	}

	public String getTestReportFilePath() {
		return testReportFilePath;
	}

	public void setTestReportFilePath(String testReportFilePath) {
		this.testReportFilePath = testReportFilePath;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getProcessed() {
		return processed;
	}

	public void setProcessed(String processed) {
		this.processed = processed;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Timestamp getLastModDate() {
		return lastModDate;
	}

	public void setLastModDate(Timestamp lastModDate) {
		this.lastModDate = lastModDate;
	}

	public Long getVanSerialNo() {
		return vanSerialNo;
	}

	public void setVanSerialNo(Long vanSerialNo) {
		this.vanSerialNo = vanSerialNo;
	}

	public String getVehicalNo() {
		return vehicalNo;
	}

	public void setVehicalNo(String vehicalNo) {
		this.vehicalNo = vehicalNo;
	}

	public Integer getParkingPlaceID() {
		return parkingPlaceID;
	}

	public void setParkingPlaceID(Integer parkingPlaceID) {
		this.parkingPlaceID = parkingPlaceID;
	}

	public String getSyncedBy() {
		return syncedBy;
	}

	public void setSyncedBy(String syncedBy) {
		this.syncedBy = syncedBy;
	}

	public Timestamp getSyncedDate() {
		return syncedDate;
	}

	public void setSyncedDate(Timestamp syncedDate) {
		this.syncedDate = syncedDate;
	}

	public String getReservedForChange() {
		return reservedForChange;
	}

	public void setReservedForChange(String reservedForChange) {
		this.reservedForChange = reservedForChange;
	}

	public List<Map<String, Object>> getCompList() {
		return compList;
	}

	public void setCompList(List<Map<String, Object>> compList) {
		this.compList = compList;
	}

	public Integer getVanID() {
		return vanID;
	}

	public void setVanID(Integer vanID) {
		this.vanID = vanID;
	}

	public String[] getFileIDs() {
		return fileIDs;
	}

	public void setFileIDs(String[] fileIDs) {
		this.fileIDs = fileIDs;
	}

}
