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

import java.util.List;

import com.google.gson.annotations.Expose;

public class DownSyncDataDigester {

	@Expose
	private String schemaName;
	@Expose
	private String tableName;
	/** columns to SELECT from central */
	@Expose
	private String serverColumnName;
	/** primary key column of the table (same name in central & local) */
	@Expose
	private String vanAutoIncColumnName;
	/** MASTER / TRANSACTIONAL */
	@Expose
	private String tableType;
	@Expose
	private Integer vanID;
	@Expose
	private Integer providerServiceMapID;
	/** populated only on the acknowledgement call */
	@Expose
	private List<DownSyncRecordAck> records;
	private Long lastFetchedID;
	private Integer batchSize;

	public DownSyncDataDigester() {
	}

	public static DownSyncDataDigester forDownload(DownSyncTableDetail tableDetail, String serverColumnName,
			Integer vanID, Integer providerServiceMapID, Long lastFetchedID, Integer batchSize) {
		DownSyncDataDigester digester = new DownSyncDataDigester();
		digester.schemaName = tableDetail.getSchemaName();
		digester.tableName = tableDetail.getTableName();
		digester.serverColumnName = serverColumnName;
		digester.vanAutoIncColumnName = tableDetail.getVanAutoIncColumnName();
		digester.tableType = tableDetail.getTableType();
		digester.vanID = vanID;
		digester.providerServiceMapID = providerServiceMapID;
		digester.lastFetchedID = lastFetchedID;
		digester.batchSize = batchSize;
		return digester;
	}

	public static DownSyncDataDigester forAck(DownSyncTableDetail tableDetail, Integer vanID,
			List<DownSyncRecordAck> records) {
		DownSyncDataDigester digester = new DownSyncDataDigester();
		digester.schemaName = tableDetail.getSchemaName();
		digester.tableName = tableDetail.getTableName();
		digester.vanAutoIncColumnName = tableDetail.getVanAutoIncColumnName();
		digester.tableType = tableDetail.getTableType();
		digester.vanID = vanID;
		digester.records = records;
		return digester;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getServerColumnName() {
		return serverColumnName;
	}

	public void setServerColumnName(String serverColumnName) {
		this.serverColumnName = serverColumnName;
	}

	public String getVanAutoIncColumnName() {
		return vanAutoIncColumnName;
	}

	public void setVanAutoIncColumnName(String vanAutoIncColumnName) {
		this.vanAutoIncColumnName = vanAutoIncColumnName;
	}

	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public Integer getVanID() {
		return vanID;
	}

	public void setVanID(Integer vanID) {
		this.vanID = vanID;
	}

	public Integer getProviderServiceMapID() {
		return providerServiceMapID;
	}

	public void setProviderServiceMapID(Integer providerServiceMapID) {
		this.providerServiceMapID = providerServiceMapID;
	}

	public Long getLastFetchedID() {
		return lastFetchedID;
	}

	public void setLastFetchedID(Long lastFetchedID) {
		this.lastFetchedID = lastFetchedID;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public List<DownSyncRecordAck> getRecords() {
		return records;
	}

	public void setRecords(List<DownSyncRecordAck> records) {
		this.records = records;
	}

	public boolean isMasterTable() {
		return tableType == null || DownSyncTableDetail.TABLE_TYPE_MASTER.equalsIgnoreCase(tableType);
	}
}
