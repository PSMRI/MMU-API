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
package com.iemr.mmu.data.syncActivity_syncLayer;

import com.google.gson.annotations.Expose;

public class DownSyncRecordAck {

	public static final String STATUS_PROCESSED = "P";
	public static final String STATUS_FAILED = "F";
	public static final String STATUS_RETRY = "U";
	public static final String CONFLICT = "CONFLICT";

	@Expose
	private Long centralID;
	@Expose
	private Long vanSerialNo;
	@Expose
	private String status;
	@Expose
	private String failureReason;

	public DownSyncRecordAck() {
	}

	public DownSyncRecordAck(Long centralID, Long vanSerialNo, String status, String failureReason) {
		this.centralID = centralID;
		this.vanSerialNo = vanSerialNo;
		this.status = status;
		this.failureReason = failureReason;
	}

	public static DownSyncRecordAck success(Long centralID, Long vanSerialNo) {
		return new DownSyncRecordAck(centralID, vanSerialNo, STATUS_PROCESSED, null);
	}

	public static DownSyncRecordAck failure(Long centralID, Long vanSerialNo, String failureReason) {
		return new DownSyncRecordAck(centralID, vanSerialNo, STATUS_FAILED, failureReason);
	}

	
	public static DownSyncRecordAck retryable(Long centralID, Long vanSerialNo, String failureReason) {
		return new DownSyncRecordAck(centralID, vanSerialNo, STATUS_RETRY, failureReason);
	}

	public boolean isRetryable() {
		return STATUS_RETRY.equalsIgnoreCase(status);
	}

	public boolean isSuccess() {
		return STATUS_PROCESSED.equalsIgnoreCase(status);
	}

	public Long getCentralID() {
		return centralID;
	}

	public void setCentralID(Long centralID) {
		this.centralID = centralID;
	}

	public Long getVanSerialNo() {
		return vanSerialNo;
	}

	public void setVanSerialNo(Long vanSerialNo) {
		this.vanSerialNo = vanSerialNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
}
