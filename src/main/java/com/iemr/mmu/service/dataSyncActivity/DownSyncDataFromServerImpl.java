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
package com.iemr.mmu.service.dataSyncActivity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncDataDigester;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncRecordAck;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncTableDetail;
import com.iemr.mmu.data.syncActivity_syncLayer.DownSyncTableResult;
import com.iemr.mmu.repo.syncActivity_syncLayer.DownSyncTableDetailRepo;
import com.iemr.mmu.utils.RestTemplateUtil;


@Service
@PropertySource("classpath:application.properties")
public class DownSyncDataFromServerImpl implements DownSyncDataFromServer {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static final String VAN_SERIAL_NO = "VanSerialNo";
	private static final String VAN_ID = "VanID";
	private static final String LAST_MOD_DATE = "LastModDate";
	private static final String DATA_SYNC_CALL = "datasync";
	private static final String LAST_DOWN_SYNC_DATE = "LastDownSyncDate";
	private static final String DOWN_SYNCED = "DownSynced";
	private static final String DOWN_SYNC_DATE = "DownSyncDate";
	private static final String DOWN_SYNC_FAILURE_REASON = "DownSyncFailureReason";
	private static final String PROCESSED = "Processed";
	private static final String SYNC_FAILURE_REASON = "SyncFailureReason";
	private static final String CENTRAL_ID = "CentralID";

	@Value("${downSyncDataUrl}")
	private String downSyncDataUrl;

	@Value("${downSyncFlagUpdateUrl}")
	private String downSyncFlagUpdateUrl;

	@Autowired
	private DownSyncTableDetailRepo downSyncTableDetailRepo;

	@Autowired
	private DataSyncRepository dataSyncRepository;

	private static final AtomicBoolean IN_PROGRESS = new AtomicBoolean(false);
	private static int totalCounter = 0;
	private static int progressCounter = 0;
	private static int insertedCounter = 0;
	private static int updatedCounter = 0;
	private static int conflictCounter = 0;
	private static int skippedCounter = 0;
	private static int failedTableCounter = 0;
	private static int failedRecordCounter = 0;
	private static StringBuilder failedTables = new StringBuilder();
	private static final List<DownSyncTableResult> tableResults =
			Collections.synchronizedList(new ArrayList<>());
	private static DownSyncTableResult currentResult;
	private static String currentTable = "";

	public String startDownSync(String serverAuthorization, String jwtToken, Integer vanID,
			Integer providerServiceMapID) throws Exception {

		if (vanID == null)
			throw new Exception("vanID is mandatory for down-sync. Kindly contact the administrator.");

		final ArrayList<DownSyncTableDetail> downSyncTables = downSyncTableDetailRepo.getActiveDownSyncTables();
		if (downSyncTables == null || downSyncTables.isEmpty())
			throw new Exception("No table is configured for down-sync in m_downsynctabledetail.");

		if (!IN_PROGRESS.compareAndSet(false, true))
			return "inProgress";

		totalCounter = downSyncTables.size();
		progressCounter = 0;
		insertedCounter = 0;
		updatedCounter = 0;
		conflictCounter = 0;
		skippedCounter = 0;
		failedTableCounter = 0;
		failedRecordCounter = 0;
		failedTables = new StringBuilder();
		tableResults.clear();
		currentResult = null;
		currentTable = "";

		final Map<String, String> syncGroups = dataSyncRepository.getSyncGroupNamesByTable();

		try {
			for (DownSyncTableDetail tableDetail : downSyncTables) {
				currentTable = tableDetail.getSchemaName() + "." + tableDetail.getTableName();

				String groupName = syncGroups.getOrDefault(tableDetail.getTableName().toLowerCase(),
						tableDetail.isMasterTable() ? "Masters" : "Other");
				currentResult = new DownSyncTableResult(groupName, tableDetail.getSchemaName(),
						tableDetail.getTableName());
				tableResults.add(currentResult);

				try {
					downSyncTable(tableDetail, vanID, providerServiceMapID, serverAuthorization, jwtToken);
				} catch (Exception e) {
					failedTableCounter++;
					failedTables.append(tableDetail.getTableName()).append(" | ");
					currentResult.tableFailed(shorten(e.getMessage()));
					logger.error("Down-sync failed for " + currentTable + ". Exception : " + e.getMessage(), e);
				}
				progressCounter++;
			}
		} finally {
			IN_PROGRESS.set(false);
			currentTable = "";
		}

		logger.info("Down-sync finished : {} of {} tables succeeded, {} inserted, {} updated, {} skipped, "
				+ "{} conflicts, {} failed records", progressCounter - failedTableCounter, totalCounter,
				insertedCounter, updatedCounter, skippedCounter, conflictCounter, failedRecordCounter);

		return "Down-sync completed";
	}

	public Map<String, Object> getDownSyncStatus() {
		Map<String, Object> resultMap = new LinkedHashMap<>();
		resultMap.put("percentage", totalCounter == 0 ? 0 : Math.floor((progressCounter * 100) / totalCounter));
		resultMap.put("inProgress", IN_PROGRESS.get());
		resultMap.put("currentTable", currentTable);
		resultMap.put("totalTables", totalCounter);
		resultMap.put("completedTables", progressCounter);
		resultMap.put("successTableCount", progressCounter - failedTableCounter);
		resultMap.put("failedTableCount", failedTableCounter);
		resultMap.put("failedRecordCount", failedRecordCounter);
		resultMap.put("failedTables", failedTables.toString());
		synchronized (tableResults) {
			resultMap.put("tableResults", new ArrayList<>(tableResults));
		}
		resultMap.put("recordsInserted", insertedCounter);
		resultMap.put("recordsUpdated", updatedCounter);
		resultMap.put("recordsSkipped", skippedCounter);
		resultMap.put("conflicts", conflictCounter);
		return resultMap;
	}

	/***
	 * Down-syncs one configured table.
	 */
	private void downSyncTable(DownSyncTableDetail tableDetail, Integer vanID, Integer providerServiceMapID,
			String serverAuthorization, String jwtToken) throws Exception {

		List<String> serverColumns = splitColumns(tableDetail.getServerColumnName());
		List<String> vanColumns = splitColumns(tableDetail.getVanColumnName());

		if (serverColumns.isEmpty() && vanColumns.isEmpty()) {
			List<String> resolved = dataSyncRepository.getDownSyncColumns(tableDetail.getSchemaName(),
					tableDetail.getTableName());
			if (resolved.isEmpty())
				throw new Exception("No column found locally for " + tableDetail.getSchemaName() + "."
						+ tableDetail.getTableName()
						+ ". The table is configured for down-sync but does not exist in the local DB.");

			serverColumns = resolved;
			vanColumns = resolved;
		}

		if (serverColumns.isEmpty() || vanColumns.isEmpty())
			throw new Exception("Only one of ServerColumnName / VanColumnName is configured for "
					+ tableDetail.getTableName() + ". Configure both, with the same columns in the same order, "
					+ "or leave both empty to have them resolved from the schema.");
		if (serverColumns.size() != vanColumns.size())
			throw new Exception("ServerColumnName & VanColumnName column count does not match for "
					+ tableDetail.getTableName() + " (" + serverColumns.size() + " vs " + vanColumns.size()
					+ "). Central and local must expose the same number of columns, in the same order.");

		List<Map<String, Object>> dataFromCentral = fetchDataFromCentral(tableDetail, String.join(",", serverColumns),
				vanID, providerServiceMapID, serverAuthorization, jwtToken);

		if (dataFromCentral.isEmpty()) {
			logger.info("Nothing to down-sync for {}.{}", tableDetail.getSchemaName(), tableDetail.getTableName());
			return;
		}

		if (tableDetail.isMasterTable()) {
			upsertMasterData(tableDetail, serverColumns, vanColumns, dataFromCentral);
		} else {
			List<DownSyncRecordAck> acks = saveTransactionalData(tableDetail, serverColumns, vanColumns,
					dataFromCentral, vanID);
			acknowledgeToCentral(tableDetail, vanID, acks, serverAuthorization, jwtToken);
		}
	}

	private List<Map<String, Object>> fetchDataFromCentral(DownSyncTableDetail tableDetail, String serverColumnName,
			Integer vanID, Integer providerServiceMapID, String serverAuthorization, String jwtToken)
			throws Exception {

		DownSyncDataDigester digester = DownSyncDataDigester.forDownload(tableDetail, serverColumnName, vanID,
				providerServiceMapID);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(digester, serverAuthorization,
				DATA_SYNC_CALL);
		ResponseEntity<String> response = restTemplate.exchange(downSyncDataUrl, HttpMethod.POST, request, String.class);

		if (response == null || !response.hasBody())
			throw new Exception("Empty response from central for " + tableDetail.getTableName());

		JSONObject responseObj = new JSONObject(response.getBody());
		if (!responseObj.has("data") || !responseObj.has("statusCode") || responseObj.getInt("statusCode") != 200)
			throw new Exception("Down-sync API failed for " + tableDetail.getTableName() + " : "
					+ responseObj.optString("errorMessage"));

		Gson gson = new GsonBuilder().serializeNulls().create();
		List<Map<String, Object>> data = gson.fromJson(responseObj.get("data").toString(),
				new TypeToken<List<Map<String, Object>>>() {
				}.getType());

		return data != null ? data : new ArrayList<>();
	}

	/***
	 * MASTER tables : central authoritative, upserted in one batch.
	 */
	private void upsertMasterData(DownSyncTableDetail tableDetail, List<String> serverColumns, List<String> vanColumns,
			List<Map<String, Object>> dataFromCentral) {

		StringBuilder placeHolders = new StringBuilder();
		StringBuilder onDuplicate = new StringBuilder();
		for (int i = 0; i < vanColumns.size(); i++) {
			if (i > 0) {
				placeHolders.append(", ");
				onDuplicate.append(", ");
			}
			placeHolders.append("?");
			onDuplicate.append(vanColumns.get(i)).append(" = VALUES(").append(vanColumns.get(i)).append(")");
		}

		String query = " INSERT INTO " + tableDetail.getSchemaName() + "." + tableDetail.getTableName() + " ( "
				+ String.join(", ", vanColumns) + " ) VALUES ( " + placeHolders + " ) ON DUPLICATE KEY UPDATE "
				+ onDuplicate;

		List<Object[]> batch = new ArrayList<>();
		for (Map<String, Object> record : dataFromCentral) {
			Object[] values = new Object[serverColumns.size()];
			for (int i = 0; i < serverColumns.size(); i++) {
				values[i] = normalize(record.get(serverColumns.get(i)));
			}
			batch.add(values);
		}

		int[] result = dataSyncRepository.updateLatestMasterInLocal(query, batch);
		updatedCounter += result != null ? result.length : 0;
		logger.info("Down-synced {} master records into {}.{}", batch.size(), tableDetail.getSchemaName(),
				tableDetail.getTableName());
	}

	/***
	 * TRANSACTIONAL tables : record by record, so that a conflict on one record
	 * does not block the delivery of the rest.
	 */
	private List<DownSyncRecordAck> saveTransactionalData(DownSyncTableDetail tableDetail, List<String> serverColumns,
			List<String> vanColumns, List<Map<String, Object>> dataFromCentral, Integer vanID) throws Exception {

		String pkColumn = tableDetail.getVanAutoIncColumnName();
		if (pkColumn == null || pkColumn.trim().isEmpty())
			throw new Exception(
					"VanAutoIncColumnName is not configured for transactional table " + tableDetail.getTableName());
		pkColumn = pkColumn.trim();

		requireIdentifier(pkColumn, "VanAutoIncColumnName", tableDetail);

		String lastModColumn = dataSyncRepository.resolveLastModColumn(tableDetail.getSchemaName(),
				tableDetail.getTableName());
		if (lastModColumn == null)
			throw new Exception(tableDetail.getSchemaName() + "." + tableDetail.getTableName()
					+ " has neither LastModDate nor last_mod_date, so a change cannot be dated");
		requireIdentifier(lastModColumn, "modification-time column", tableDetail);

		List<DownSyncRecordAck> acks = new ArrayList<>();

		for (Map<String, Object> record : dataFromCentral) {
			Long centralID = toLong(record.get(pkColumn));

			try {
				Map<String, Object> localRecord = dataSyncRepository.getLocalRecordForDownSync(
						tableDetail.getSchemaName(), tableDetail.getTableName(), pkColumn, centralID, vanID,
						lastModColumn);

				if (localRecord == null) {
					Long localID = insertRecord(tableDetail, serverColumns, vanColumns, pkColumn, record, centralID);
					insertedCounter++;
					if (currentResult != null)
						currentResult.addInserted();
					acks.add(DownSyncRecordAck.success(centralID, localID));
					continue;
				}

				Long localID = toLong(localRecord.get(pkColumn));

				if (!isCentralCopyNewer(record, localRecord, lastModColumn)) {
					skippedCounter++;
					if (currentResult != null)
						currentResult.addSkipped();
					acks.add(DownSyncRecordAck.success(centralID, localID));
					continue;
				}

				if (hasUnsentLocalChanges(localRecord)) {
					dataSyncRepository.markDownSyncConflictInLocal(tableDetail.getSchemaName(),
							tableDetail.getTableName(), pkColumn, localID);
					conflictCounter++;
					if (currentResult != null)
						currentResult.addConflict();
					acks.add(DownSyncRecordAck.failure(centralID, localID, DownSyncRecordAck.CONFLICT));
					logger.warn("Down-sync conflict for {}.{}, local id {}", tableDetail.getSchemaName(),
							tableDetail.getTableName(), localID);
					continue;
				}

				updateRecord(tableDetail, serverColumns, vanColumns, pkColumn, record, localID);
				updatedCounter++;
				if (currentResult != null)
					currentResult.addUpdated();
				acks.add(DownSyncRecordAck.success(centralID, localID));

			} catch (Exception e) {
				failedRecordCounter++;
				String reason = shorten(e.getMessage());
				if (currentResult != null)
					currentResult.recordFailed(reason);
				acks.add(DownSyncRecordAck.failure(centralID, null, reason));
				logger.error("Down-sync failed for record " + centralID + " of " + tableDetail.getTableName() + " : "
						+ e.getMessage(), e);
			}
		}

		return acks;
	}

	private boolean isCentralCopyNewer(Map<String, Object> centralRecord, Map<String, Object> localRecord,
			String lastModColumn) {
		Timestamp centralLastModDate = toTimestamp(centralRecord.get(lastModColumn));
		if (centralLastModDate == null)
			return false;

		Timestamp localLastModDate = toTimestamp(localRecord.get(LAST_MOD_DATE));
		if (localLastModDate != null)
			return centralLastModDate.after(localLastModDate);

		Timestamp localLastDownSyncDate = toTimestamp(localRecord.get(LAST_DOWN_SYNC_DATE));
		return localLastDownSyncDate == null || centralLastModDate.after(localLastDownSyncDate);
	}

	private boolean hasUnsentLocalChanges(Map<String, Object> localRecord) {
		String localProcessed = localRecord.get(PROCESSED) != null ? String.valueOf(localRecord.get(PROCESSED)) : null;
		return "N".equalsIgnoreCase(localProcessed);
	}

	private Long insertRecord(DownSyncTableDetail tableDetail, List<String> serverColumns, List<String> vanColumns,
			String pkColumn, Map<String, Object> record, Long centralID) {

		List<String> columns = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		for (int i = 0; i < vanColumns.size(); i++) {
			String vanColumn = vanColumns.get(i);
			// the local auto-increment PK is skipped so that local generates its own value
			if (vanColumn.equalsIgnoreCase(pkColumn) || isDownSyncManagedColumn(vanColumn))
				continue;

			columns.add(vanColumn);
			values.add(normalize(record.get(serverColumns.get(i))));
		}

		columns.add(CENTRAL_ID);
		values.add(centralID);
		columns.add(PROCESSED);
		values.add("P");
		columns.add(SYNC_FAILURE_REASON);
		values.add(null);
		columns.add(LAST_DOWN_SYNC_DATE);
		values.add(Timestamp.valueOf(LocalDateTime.now()));
		columns.add(DOWN_SYNCED);
		values.add(DownSyncRecordAck.STATUS_PROCESSED);
		columns.add(DOWN_SYNC_DATE);
		values.add(Timestamp.valueOf(LocalDateTime.now()));

		StringBuilder placeHolders = new StringBuilder();
		for (int i = 0; i < columns.size(); i++) {
			if (i > 0)
				placeHolders.append(", ");
			placeHolders.append("?");
		}

		String query = " INSERT INTO " + tableDetail.getSchemaName() + "." + tableDetail.getTableName() + " ( "
				+ String.join(", ", columns) + " ) VALUES ( " + placeHolders + " ) ";

		Long localID = dataSyncRepository.insertDownSyncRecordInLocal(query, values.toArray());

		if (localID != null)
			dataSyncRepository.updateVanSerialNoInLocal(tableDetail.getSchemaName(), tableDetail.getTableName(),
					pkColumn, localID);

		return localID;
	}

	private void updateRecord(DownSyncTableDetail tableDetail, List<String> serverColumns, List<String> vanColumns,
			String pkColumn, Map<String, Object> record, Long localID) {

		StringBuilder setClause = new StringBuilder();
		List<Object> values = new ArrayList<>();

		for (int i = 0; i < vanColumns.size(); i++) {
			String vanColumn = vanColumns.get(i);
			if (vanColumn.equalsIgnoreCase(pkColumn) || isDownSyncManagedColumn(vanColumn))
				continue;

			if (setClause.length() > 0)
				setClause.append(", ");
			setClause.append(vanColumn).append(" = ?");
			values.add(normalize(record.get(serverColumns.get(i))));
		}

		setClause.append(setClause.length() > 0 ? ", " : "").append(PROCESSED).append(" = 'P', ")
				.append(SYNC_FAILURE_REASON).append(" = NULL, ").append(LAST_DOWN_SYNC_DATE).append(" = ?, ")
				.append(DOWN_SYNCED).append(" = 'P', ").append(DOWN_SYNC_DATE).append(" = ? ");
		values.add(Timestamp.valueOf(LocalDateTime.now()));
		values.add(Timestamp.valueOf(LocalDateTime.now()));
		values.add(localID);

		String query = " UPDATE " + tableDetail.getSchemaName() + "." + tableDetail.getTableName() + " SET " + setClause
				+ " WHERE " + pkColumn + " = ? ";

		dataSyncRepository.updateDownSyncRecordInLocal(query, values.toArray());
	}

	private static String shorten(String message) {
		if (message == null || message.trim().isEmpty())
			return "Unknown error";
		String trimmed = message.trim();
		return trimmed.length() > 250 ? trimmed.substring(0, 250) : trimmed;
	}

	private void requireIdentifier(String value, String what, DownSyncTableDetail tableDetail) throws Exception {
		if (value == null || !value.matches("^[a-zA-Z_][a-zA-Z0-9_]*$"))
			throw new Exception("The down-sync configuration of " + tableDetail.getTableName() + " holds an invalid "
					+ what);
	}
	
	private boolean isDownSyncManagedColumn(String column) {
		return VAN_SERIAL_NO.equalsIgnoreCase(column) || CENTRAL_ID.equalsIgnoreCase(column)
				|| PROCESSED.equalsIgnoreCase(column) || SYNC_FAILURE_REASON.equalsIgnoreCase(column)
				|| LAST_DOWN_SYNC_DATE.equalsIgnoreCase(column) || DOWN_SYNCED.equalsIgnoreCase(column)
				|| DOWN_SYNC_DATE.equalsIgnoreCase(column) || DOWN_SYNC_FAILURE_REASON.equalsIgnoreCase(column);
	}

	private void acknowledgeToCentral(DownSyncTableDetail tableDetail, Integer vanID, List<DownSyncRecordAck> acks,
			String serverAuthorization, String jwtToken) throws Exception {

		if (acks == null || acks.isEmpty())
			return;

		DownSyncDataDigester digester = DownSyncDataDigester.forAck(tableDetail, vanID, acks);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(digester, serverAuthorization,
				DATA_SYNC_CALL);
		ResponseEntity<String> response = restTemplate.exchange(downSyncFlagUpdateUrl, HttpMethod.POST, request,
				String.class);

		if (response == null || !response.hasBody())
			throw new Exception("Empty response while updating the down-sync flag for " + tableDetail.getTableName());

		JSONObject responseObj = new JSONObject(response.getBody());
		if (!responseObj.has("statusCode") || responseObj.getInt("statusCode") != 200)
			throw new Exception("Could not update the down-sync flag at central for " + tableDetail.getTableName()
					+ " : " + responseObj.optString("errorMessage"));

		logger.info("Down-sync flag updated at central for {} records of {}", acks.size(), tableDetail.getTableName());
	}

	private List<String> splitColumns(String columns) {
		List<String> columnList = new ArrayList<>();
		if (columns == null)
			return columnList;

		for (String column : Arrays.asList(columns.split(","))) {
			if (column != null && !column.trim().isEmpty())
				columnList.add(column.trim());
		}
		return columnList;
	}

	private Object normalize(Object value) {
		if (value instanceof Double) {
			double doubleValue = (Double) value;
			if (!Double.isInfinite(doubleValue) && !Double.isNaN(doubleValue) && doubleValue == Math.floor(doubleValue)
					&& Math.abs(doubleValue) < 1e15)
				return (long) doubleValue;
		}
		return value;
	}

	private Long toLong(Object value) {
		Object normalized = normalize(value);
		if (normalized == null)
			return null;
		if (normalized instanceof Number)
			return ((Number) normalized).longValue();
		try {
			return Long.valueOf(String.valueOf(normalized).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Timestamp toTimestamp(Object value) {
		if (value == null)
			return null;
		if (value instanceof Timestamp)
			return (Timestamp) value;
		if (value instanceof java.util.Date)
			return new Timestamp(((java.util.Date) value).getTime());

		String stringValue = String.valueOf(value).trim();
		if (stringValue.isEmpty() || "null".equalsIgnoreCase(stringValue))
			return null;

		String candidate = stringValue.replace("T", " ").replace("Z", "");
		try {
			return Timestamp.valueOf(candidate);
		} catch (IllegalArgumentException e) {
			try {
				// gson's default rendering of a java.sql.Timestamp
				return new Timestamp(DateFormat.getDateTimeInstance().parse(stringValue).getTime());
			} catch (java.text.ParseException pe) {
				logger.warn("Could not parse the timestamp '{}' received from central", stringValue);
				return null;
			}
		}
	}
}
