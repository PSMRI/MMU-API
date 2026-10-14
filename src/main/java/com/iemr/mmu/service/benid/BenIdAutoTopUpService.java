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
package com.iemr.mmu.service.benid;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.iemr.mmu.utils.RestTemplateUtil;

import jakarta.annotation.PostConstruct;

/**
 * Keeps the local BenID pool filled without anyone clicking "Generate BenID".
 *
 * Runs only on StopTB laptops (stoptb.enforce.vanid=true and stoptb.van.id>0).
 * Pool empty -> bootstrap size; pool below lower limit -> batch size. IDs
 * received from central are written to a pending file before the local import,
 * so a failed import is retried with the same IDs instead of reserving new ones.
 */
@Service
public class BenIdAutoTopUpService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	/** Shared with the manual "Generate BenID" button so both never run together. */
	public static final ReentrantLock GENERATION_LOCK = new ReentrantLock();

	private static final String PENDING_FILE_NAME = "benid-pending-import.json";
	private static final long TOKEN_KEEP_ALIVE_MS = 24 * 60 * 60_000L;

	@Autowired
	private CentralAuthService centralAuthService;

	@Value("${benGenUrlCentral:}")
	private String benGenUrlCentral;

	@Value("${benImportUrlLocal:}")
	private String benImportUrlLocal;

	@Value("${benCheckUrlLocal:}")
	private String benCheckUrlLocal;

	@Value("${benid.pool.lowerLimit:2000}")
	private long lowerLimit;

	@Value("${benid.pool.batchSize:1000}")
	private long batchSize;

	@Value("${benid.pool.bootstrapSize:10000}")
	private long bootstrapSize;

	@Value("${benid.token.dir:/opt/amrit/data}")
	private String dataDir;

	// wait after the 1st, 2nd, 3rd and later failures (5, 15, 30, 60 minutes)
	@Value("${benid.autoTopUp.retryDelaysMs:300000,900000,1800000,3600000}")
	private long[] retryDelaysMs;

	@Value("${benid.http.connectTimeoutMs:15000}")
	private int connectTimeoutMs;

	@Value("${benid.http.readTimeoutMs:60000}")
	private int readTimeoutMs;

	private RestTemplate restTemplate;

	private volatile int failureCount;
	private volatile long nextAttemptAt;
	private volatile long lastPoolCount = -1;
	private volatile long lastTopUpAt;
	private volatile long lastTopUpCount;
	private volatile String lastError;

	@PostConstruct
	public void init() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeoutMs);
		factory.setReadTimeout(readTimeoutMs);
		restTemplate = new RestTemplate(factory);
	}

	/** Regular check; does nothing on central or non-StopTB deployments. */
	@Scheduled(initialDelayString = "${benid.autoTopUp.initialDelayMs:120000}", fixedDelayString = "${benid.autoTopUp.checkIntervalMs:300000}")
	public void scheduledCheck() {
		runOnce(false);
	}

	/** Run straight away (after a login or a Data Sync), in the background. */
	public void triggerNow() {
		if (centralAuthService.isEnabled()) {
			CompletableFuture.runAsync(() -> runOnce(true));
		}
	}

	private void runOnce(boolean ignoreBackoff) {
		if (!centralAuthService.isEnabled()) {
			return;
		}
		if (!ignoreBackoff && System.currentTimeMillis() < nextAttemptAt) {
			return;
		}
		if (!GENERATION_LOCK.tryLock()) {
			logger.info("BenID generation already running - skipping this check");
			return;
		}
		try {
			// IDs already received but not saved locally are imported first
			if (importPendingIfAny()) {
				onSuccess();
			}

			long available = countLocalPool();
			lastPoolCount = available;
			long needed = available <= 0 ? bootstrapSize : (available < lowerLimit ? batchSize : 0);
			if (needed == 0) {
				centralAuthService.keepAliveIfDue(TOKEN_KEEP_ALIVE_MS);
				onSuccess();
				return;
			}

			String key = centralAuthService.getCentralSessionKey();
			if (key == null) {
				lastError = "No central connection - waiting for a user login";
				logger.warn("BenID pool low ({} for vanID {}) but no central token - waiting for a user login",
						available, centralAuthService.getVanID());
				return;
			}

			int imported = generateAndImport(centralAuthService.getVanID(), needed, key, key, "datasync");
			lastPoolCount = countLocalPool();
			lastTopUpAt = System.currentTimeMillis();
			lastTopUpCount = imported;
			logger.info("BenID auto top-up: vanID {} +{} IDs, pool now {}", centralAuthService.getVanID(), imported,
					lastPoolCount);
			onSuccess();
		} catch (Exception e) {
			onFailure(e);
		} finally {
			GENERATION_LOCK.unlock();
		}
	}

	/**
	 * Asks central for IDs for the van and imports them locally. The central
	 * response is written to the pending file first, so a failed local import
	 * does not lose IDs that central has already reserved for this van.
	 *
	 * @return number of IDs imported
	 */
	public int generateAndImport(int vanID, long count, String centralAuthorization, String localAuthorization,
			String localToken) throws Exception {
		JSONObject request = new JSONObject();
		request.put("vanID", vanID);
		request.put("benIDRequired", count);

		HttpEntity<Object> centralRequest = RestTemplateUtil.createRequestEntity(request.toString(),
				centralAuthorization, "datasync");
		logger.info("Requesting {} BenIDs from central for vanID {}", count, vanID);
		long start = System.currentTimeMillis();
		ResponseEntity<String> response = restTemplate.exchange(benGenUrlCentral, HttpMethod.POST, centralRequest,
				String.class);
		logger.info("Central BenID generation answered in {} ms", System.currentTimeMillis() - start);
		JSONObject obj = new JSONObject(response.getBody());
		if (obj.optInt("statusCode") != 200 || !obj.has("data")) {
			throw new IllegalStateException("Central BenID generation failed: " + obj.optString("errorMessage"));
		}
		JSONArray ids = obj.getJSONArray("data");
		for (int k = 0; k < ids.length(); k++) {
			ids.getJSONObject(k).put("vanID", vanID);
		}
		writePending(ids.toString());
		long importStart = System.currentTimeMillis();
		importLocally(ids.toString(), localAuthorization, localToken);
		deletePending();
		logger.info("Imported {} BenIDs locally in {} ms", ids.length(), System.currentTimeMillis() - importStart);
		return ids.length();
	}

	private boolean importPendingIfAny() throws Exception {
		Path pending = pendingPath();
		if (!Files.exists(pending)) {
			return false;
		}
		String payload = new String(Files.readAllBytes(pending), StandardCharsets.UTF_8);
		logger.info("Importing BenIDs left from an earlier run");
		try {
			importLocally(payload, "datasync", "datasync");
		} catch (Exception e) {
			// the earlier run already saved these IDs; nothing left to import
			if (e.getMessage() == null || !e.getMessage().toLowerCase().contains("duplicate")) {
				throw e;
			}
			logger.info("Pending BenIDs were already imported earlier");
		}
		deletePending();
		return true;
	}

	private void importLocally(String payload, String authorization, String token) {
		HttpEntity<Object> localRequest = RestTemplateUtil.createRequestEntity(payload, authorization, token);
		ResponseEntity<String> response = restTemplate.exchange(benImportUrlLocal, HttpMethod.POST, localRequest,
				String.class);
		JSONObject obj = new JSONObject(response.getBody());
		if (obj.optInt("statusCode") != 200) {
			throw new IllegalStateException("Local BenID import failed: " + obj.optString("errorMessage"));
		}
	}

	public long countLocalPool() {
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(null, "datasync", "datasync");
		ResponseEntity<String> response = restTemplate.exchange(benCheckUrlLocal, HttpMethod.GET, request,
				String.class);
		JSONObject obj = new JSONObject(response.getBody());
		if (obj.optInt("statusCode") != 200) {
			throw new IllegalStateException("Local BenID count failed: " + obj.optString("errorMessage"));
		}
		Object data = obj.get("data");
		JSONObject dataObj = data instanceof JSONObject ? (JSONObject) data : new JSONObject(data.toString());
		return Long.parseLong(dataObj.get("response").toString());
	}

	private void onSuccess() {
		failureCount = 0;
		nextAttemptAt = 0;
		lastError = null;
	}

	private void onFailure(Exception e) {
		long delay = retryDelaysMs[Math.min(failureCount, retryDelaysMs.length - 1)];
		failureCount++;
		nextAttemptAt = System.currentTimeMillis() + delay;
		lastError = e.getMessage();
		logger.warn("BenID auto top-up failed (attempt {}), next try in {} s: {}", failureCount, delay / 1000,
				e.getMessage());
	}

	private Path pendingPath() {
		return Paths.get(dataDir, PENDING_FILE_NAME);
	}

	private void writePending(String payload) {
		try {
			Files.createDirectories(Paths.get(dataDir));
			Files.write(pendingPath(), payload.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			logger.warn("Could not write pending BenID file: {}", e.getMessage());
		}
	}

	private void deletePending() {
		try {
			Files.deleteIfExists(pendingPath());
		} catch (Exception e) {
			logger.warn("Could not delete pending BenID file: {}", e.getMessage());
		}
	}

	// ---------------------------------------------------------------- status

	public JSONObject status() {
		JSONObject s = new JSONObject();
		s.put("enabled", centralAuthService.isEnabled());
		if (!centralAuthService.isEnabled()) {
			return s;
		}
		s.put("vanID", centralAuthService.getVanID());
		s.put("centralConnection", centralAuthService.getState().name());
		s.put("connectedUser", centralAuthService.getConnectedUser());
		try {
			lastPoolCount = countLocalPool();
		} catch (Exception e) {
			logger.debug("Live BenID count not available: {}", e.getMessage());
		}
		s.put("poolCount", lastPoolCount);
		s.put("lowerLimit", lowerLimit);
		s.put("lastTopUpAt", lastTopUpAt);
		s.put("lastTopUpCount", lastTopUpCount);
		s.put("pendingImport", Files.exists(pendingPath()));
		String error = lastError != null ? lastError : centralAuthService.getLastError();
		if (error != null) {
			s.put("lastError", error);
		}
		return s;
	}
}
