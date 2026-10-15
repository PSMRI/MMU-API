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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.iemr.mmu.service.benid.BenIdAutoTopUpService;
import com.iemr.mmu.service.benid.CentralAuthService;

import jakarta.annotation.PreDestroy;

/**
 * StopTB laptops: runs Data Sync by itself, so central stays current and laptops get master changes without
 * anyone pressing Upload or Download Masters.
 *
 * - Upload (van to central) every sync.autoUpload.intervalMs (default 30 min).
 * - Master download (central to van) every sync.autoMasterDownload.intervalMs (default 6 h).
 *
 * Uses the central connection of the automatic BenID download (CentralAuthService): no password is stored, and
 * SyncedBy is the operator who connected the laptop. Active only where CentralAuthService is enabled
 * (stoptb.enforce.vanid=true and stoptb.van.id>0); central and other deployments are unaffected. The manual
 * buttons keep working as before.
 */
@Service
public class AutoSyncService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	/** Upload and master download never run at the same time from here. */
	private static final ReentrantLock SYNC_LOCK = new ReentrantLock();

	@Autowired
	private CentralAuthService centralAuthService;

	@Autowired
	private UploadDataToServerImpl uploadDataToServerImpl;

	@Autowired
	private DownloadDataFromServerImpl downloadDataFromServerImpl;

	@Autowired
	private DataSyncRepository dataSyncRepository;

	@Autowired
	private BenIdAutoTopUpService benIdAutoTopUpService;

	@Value("${sync.autoUpload.enabled:true}")
	private boolean uploadEnabled;

	@Value("${sync.autoMasterDownload.enabled:true}")
	private boolean masterDownloadEnabled;

	private volatile long lastUploadAt;
	private volatile String lastUploadResult;
	private volatile long lastMasterDownloadAt;
	private volatile String lastMasterDownloadResult;
	private volatile String lastError;

	// own thread: a long upload must not hold the shared scheduler thread
	private final ExecutorService background = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "auto-data-sync");
		t.setDaemon(true);
		return t;
	});

	@PreDestroy
	public void shutdown() {
		background.shutdownNow();
	}

	@Scheduled(initialDelayString = "${sync.autoUpload.initialDelayMs:600000}", fixedDelayString = "${sync.autoUpload.intervalMs:1800000}")
	public void scheduledUpload() {
		if (uploadEnabled && centralAuthService.isEnabled()) {
			background.submit(this::uploadOnce);
		}
	}

	@Scheduled(initialDelayString = "${sync.autoMasterDownload.initialDelayMs:900000}", fixedDelayString = "${sync.autoMasterDownload.intervalMs:21600000}")
	public void scheduledMasterDownload() {
		if (masterDownloadEnabled && centralAuthService.isEnabled()) {
			background.submit(this::masterDownloadOnce);
		}
	}

	void uploadOnce() {
		if (!SYNC_LOCK.tryLock()) {
			return;
		}
		try {
			String key = centralAuthService.getCentralSessionKey();
			if (key == null) {
				lastError = "No central connection - waiting for a user login";
				return;
			}
			int vanID = centralAuthService.getVanID();
			String user = centralAuthService.getConnectedUser();
			String result = uploadDataToServerImpl.getDataToSyncToServer(vanID,
					user != null && !user.isBlank() ? user : "auto-sync", key, null);
			lastUploadAt = System.currentTimeMillis();
			lastUploadResult = result;
			lastError = null;
			logger.info("Automatic upload for vanID {} finished: {}", vanID, result);
			// internet is up: top up the BenID pool if it is low (same as the manual upload)
			benIdAutoTopUpService.triggerNow();
		} catch (Exception e) {
			lastError = "Upload: " + e.getMessage();
			logger.warn("Automatic upload failed: {}", e.getMessage());
		} finally {
			SYNC_LOCK.unlock();
		}
	}

	void masterDownloadOnce() {
		if (!SYNC_LOCK.tryLock()) {
			return;
		}
		try {
			String key = centralAuthService.getCentralSessionKey();
			if (key == null) {
				lastError = "No central connection - waiting for a user login";
				return;
			}
			int vanID = centralAuthService.getVanID();
			Integer psm = dataSyncRepository.getVanProviderServiceMapID(vanID);
			if (psm == null) {
				lastError = "Master download: van " + vanID + " not found in m_van";
				return;
			}
			String result = downloadDataFromServerImpl.downloadMasterDataFromServer(key, null, vanID, psm);
			if ("inProgress".equalsIgnoreCase(result)) {
				logger.info("Master download already running (manual) - skipping the automatic one");
				return;
			}
			lastMasterDownloadAt = System.currentTimeMillis();
			lastMasterDownloadResult = result;
			lastError = null;
			logger.info("Automatic master download for vanID {} finished: {}", vanID, result);
		} catch (Exception e) {
			lastError = "Master download: " + e.getMessage();
			logger.warn("Automatic master download failed: {}", e.getMessage());
		} finally {
			SYNC_LOCK.unlock();
		}
	}

	/** For the Data Sync screen: last runs and the rows central has rejected. */
	public JSONObject status() {
		JSONObject s = new JSONObject();
		s.put("enabled", centralAuthService.isEnabled());
		s.put("uploadEnabled", uploadEnabled);
		s.put("masterDownloadEnabled", masterDownloadEnabled);
		s.put("lastUploadAt", lastUploadAt);
		s.put("lastUploadResult", lastUploadResult == null ? JSONObject.NULL : lastUploadResult);
		s.put("lastMasterDownloadAt", lastMasterDownloadAt);
		s.put("lastMasterDownloadResult",
				lastMasterDownloadResult == null ? JSONObject.NULL : lastMasterDownloadResult);
		s.put("lastError", lastError == null ? JSONObject.NULL : lastError);
		List<Map<String, Object>> failures = dataSyncRepository.getSyncFailures();
		long failedRows = failures.stream().mapToLong(f -> ((Number) f.get("failedRows")).longValue()).sum();
		s.put("failedRows", failedRows);
		s.put("failures", new JSONArray(failures));
		return s;
	}
}
