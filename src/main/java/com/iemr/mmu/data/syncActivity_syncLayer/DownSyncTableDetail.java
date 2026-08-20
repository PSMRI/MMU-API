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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/***
 * Down-sync table configuration (central -> local). Analogous to
 * {@link SyncUtilityClass} (m_synctabledetail) which drives the up-sync.
 */
@Entity
@Table(name = "m_downsynctabledetail")
public class DownSyncTableDetail {

	public static final String TABLE_TYPE_MASTER = "MASTER";
	public static final String TABLE_TYPE_TRANSACTIONAL = "TRANSACTIONAL";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Expose
	@Column(name = "DownSyncTableDetailID", updatable = false)
	private Integer downSyncTableDetailID;
	@Expose
	@Column(name = "SchemaName")
	private String schemaName;
	@Expose
	@Column(name = "TableName")
	private String tableName;
	/** columns to SELECT from central */
	@Expose
	@Column(name = "ServerColumnName")
	private String serverColumnName;
	/** columns in local, positionally mapped with {@link #serverColumnName} */
	@Expose
	@Column(name = "VanColumnName")
	private String vanColumnName;
	/** local auto-increment PK column - skipped on INSERT */
	@Expose
	@Column(name = "VanAutoIncColumnName")
	private String vanAutoIncColumnName;
	/** MASTER = full pull, no VanID filter / TRANSACTIONAL = VanID + DownSynced filter */
	@Expose
	@Column(name = "TableType")
	private String tableType;
	/** lower number syncs first - enforces the FK dependency chain */
	@Expose
	@Column(name = "SyncOrder")
	private Integer syncOrder;
	@Expose
	@Column(name = "IsActive")
	private Boolean isActive;

	@Transient
	@Expose
	private Integer vanID;
	@Transient
	@Expose
	private Integer providerServiceMapID;

	public DownSyncTableDetail() {
	}

	public boolean isMasterTable() {
		return tableType == null || TABLE_TYPE_MASTER.equalsIgnoreCase(tableType);
	}

	public boolean isTransactionalTable() {
		return TABLE_TYPE_TRANSACTIONAL.equalsIgnoreCase(tableType);
	}

	public Integer getDownSyncTableDetailID() {
		return downSyncTableDetailID;
	}

	public void setDownSyncTableDetailID(Integer downSyncTableDetailID) {
		this.downSyncTableDetailID = downSyncTableDetailID;
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

	public String getVanColumnName() {
		return vanColumnName;
	}

	public void setVanColumnName(String vanColumnName) {
		this.vanColumnName = vanColumnName;
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

	public Integer getSyncOrder() {
		return syncOrder;
	}

	public void setSyncOrder(Integer syncOrder) {
		this.syncOrder = syncOrder;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
}
