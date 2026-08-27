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

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

/***
 * The outcome of down-syncing one table, reported to the screen.
 *
 * <p>
 * A table can fail in two quite different ways and the difference matters when
 * reading the result:
 *
 * <ul>
 * <li>the table itself failed - central could not even return the rows (a schema
 * mismatch, say), so nothing was delivered and no record was flagged;</li>
 * <li>the table was delivered but individual records failed, in which case the
 * table shows a record failure count and each distinct reason with a tally.</li>
 * </ul>
 */
public class DownSyncTableResult {

	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAILED = "FAILED";
	public static final String STATUS_PARTIAL = "PARTIAL";

	@Expose
	private String groupName;
	@Expose
	private String schemaName;
	@Expose
	private String tableName;
	@Expose
	private String status;
	@Expose
	private int inserted;
	@Expose
	private int updated;
	@Expose
	private int skipped;
	@Expose
	private int conflicts;
	@Expose
	private int failedRecords;
	/** why the table as a whole failed; null when the table itself was fine */
	@Expose
	private String failureReason;
	/** distinct record-level failure reasons, each with the number of records */
	@Expose
	private Map<String, Integer> recordFailureReasons = new LinkedHashMap<>();

	public DownSyncTableResult(String groupName, String schemaName, String tableName) {
		this.groupName = groupName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.status = STATUS_SUCCESS;
	}

	/** the table could not be processed at all - nothing was delivered */
	public void tableFailed(String reason) {
		this.status = STATUS_FAILED;
		this.failureReason = reason;
	}

	/** one record failed; the table itself is fine unless every record failed */
	public void recordFailed(String reason) {
		this.failedRecords++;
		String key = (reason == null || reason.trim().isEmpty()) ? "Unknown error" : reason.trim();
		recordFailureReasons.merge(key, 1, Integer::sum);
		if (STATUS_SUCCESS.equals(this.status))
			this.status = STATUS_PARTIAL;
	}

	public int getRecordsProcessed() {
		return inserted + updated + skipped + conflicts + failedRecords;
	}

	public void addInserted() {
		inserted++;
	}

	public void addUpdated() {
		updated++;
	}

	public void addSkipped() {
		skipped++;
	}

	public void addConflict() {
		conflicts++;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getStatus() {
		return status;
	}

	public int getInserted() {
		return inserted;
	}

	public int getUpdated() {
		return updated;
	}

	public int getSkipped() {
		return skipped;
	}

	public int getConflicts() {
		return conflicts;
	}

	public int getFailedRecords() {
		return failedRecords;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Map<String, Integer> getRecordFailureReasons() {
		return recordFailureReasons;
	}
}
