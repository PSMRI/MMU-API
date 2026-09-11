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

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Automatically keeps this device's local BenID pool topped up, replacing the
 * manual "Check BenID" / "Generate BenID" click on the Data Sync screen.
 *
 * Only runs on a van/laptop deployment (stoptb.enforce.vanid=true). MMU-API
 * is also deployed on the central server for the receiving side of data sync
 * (com.iemr.mmu.controller.dataSyncLayerCentral) — that deployment sets
 * stoptb.enforce.vanid=false, and every method here exits immediately in
 * that case. Central's own BenID pool is topped up separately by
 * BeneficiaryID-Generation-API's own scheduled job — unrelated, untouched.
 *
 * Triggers:
 * - On startup: bootstrap fetch if the local pool is empty (first-time setup).
 * - Hourly, but ONLY within a configurable overnight window (default 6 PM -
 *   6 AM, via benid.pool.retryWindow.startHour/endHour): top up if the pool
 *   has dropped below the low-watermark. Confined to overnight on purpose -
 *   retrying during active camp hours would add doomed network attempts on
 *   an already-strained connection right when the app is actually in use.
 *
 * Safety rules:
 * - Never two downloads running at once (inProgress guard).
 * - A dropped/slow connection times out rather than hanging the job thread.
 * - A partial/garbled response is never trusted as success.
 * - Every attempt requests a fresh batch, so a failed/lost attempt only
 *   wastes that batch — it can never produce a duplicate BenID.
 */
@Component
public class BenIDPoolTopUpScheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static final int CONNECT_TIMEOUT_MS = 15_000;
	private static final int READ_TIMEOUT_MS = 30_000;

	@Autowired
	private DownloadDataFromServerImpl downloadDataFromServerImpl;

	// Same stoptb.enforce.vanid / stoptb.van.id properties already used by
	// Identity-API/FLW-API/TM-API on this same laptop - not a separate name
	// for the same concept. No inline default on the enforce flag: every
	// properties file must set it explicitly, so a forgotten config fails
	// loudly at startup instead of silently running fail-open.
	@Value("${stoptb.enforce.vanid}")
	private boolean enforceVanID;

	@Value("${stoptb.van.id}")
	private int vanID;

	@Value("${benid.pool.lowerLimit:2000}")
	private long lowerLimit;

	@Value("${benid.pool.batchSize:1000}")
	private long batchSize;

	@Value("${benid.pool.bootstrapSize:10000}")
	private long bootstrapSize;

	@Value("${benCheckUrlLocal}")
	private String benCheckUrlLocal;

	@Value("${benGenSchedulerCredential:mmu-scheduler}")
	private String schedulerCredential;

	// Retry window - confines every retry attempt (after the one-time startup
	// bootstrap) to overnight hours, configurable per deployment. Default
	// 18 (6 PM) to 6 (6 AM); startHour > endHour is treated as wrapping past
	// midnight, which is the normal case.
	@Value("${benid.pool.retryWindow.startHour:18}")
	private int retryWindowStartHour;

	@Value("${benid.pool.retryWindow.endHour:6}")
	private int retryWindowEndHour;

	private final AtomicBoolean inProgress = new AtomicBoolean(false);
	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
		factory.setReadTimeout(READ_TIMEOUT_MS);
		this.restTemplate = new RestTemplate(factory);

		// Bootstrap: brand-new laptop, pool is empty - fetch immediately if
		// internet happens to be available right now at setup/startup.
		attemptTopUp(bootstrapSize, true);
	}

	/**
	 * Runs every hour, on the hour, every day - but only actually attempts a
	 * top-up when the current time falls inside the configured overnight
	 * window. Outside that window it's a no-op, by design: retrying during
	 * active camp hours would add doomed network attempts on an
	 * already-strained connection right when the app is in use.
	 */
	@Scheduled(cron = "0 0 * * * *")
	public void hourlyCheckWithinRetryWindow() {
		if (!isWithinRetryWindow()) {
			return;
		}
		attemptTopUp(batchSize, false);
	}

	private boolean isWithinRetryWindow() {
		int hour = LocalTime.now().getHour();
		if (retryWindowStartHour <= retryWindowEndHour) {
			// Same-day window, e.g. startHour=9, endHour=17.
			return hour >= retryWindowStartHour && hour < retryWindowEndHour;
		}
		// Wraps past midnight, e.g. startHour=18, endHour=6 (the default).
		return hour >= retryWindowStartHour || hour < retryWindowEndHour;
	}

	private void attemptTopUp(long amount, boolean isBootstrap) {
		if (!enforceVanID) {
			// Central deployment of MMU-API (or a van with the feature turned
			// off) - this whole mechanism is van/laptop-only. Central tops up
			// its own pool via BeneficiaryID-Generation-API's own scheduler.
			return;
		}
		if (vanID <= 0) {
			logger.warn("stoptb.enforce.vanid=true but stoptb.van.id is 0 - skipping BenID auto top-up");
			return;
		}
		if (!inProgress.compareAndSet(false, true)) {
			logger.info("BenID auto top-up already in progress - skipping this trigger");
			return;
		}
		try {
			long available = checkLocalAvailability();
			boolean needsTopUp = isBootstrap ? available <= 0 : available < lowerLimit;
			if (!needsTopUp) {
				logger.info("BenID pool healthy ({} available for vanID {}) - no top-up needed", available, vanID);
				return;
			}

			JSONObject requestObj = new JSONObject();
			requestObj.put("vanID", vanID);
			requestObj.put("benIDRequired", amount);

			// Static server-to-server credential in place of a live user JWT -
			// there is no logged-in session in a scheduled job. Passed as both
			// Authorization and ServerAuthorization, with token="datasync" so
			// the central-facing leg uses the existing sentinel-token path.
			int result = downloadDataFromServerImpl.callCentralAPIToGenerateBenIDAndimportToLocal(
					requestObj.toString(), schedulerCredential, schedulerCredential, "datasync");

			if (result == 2) {
				logger.info("BenID auto top-up succeeded: requested {} IDs for vanID {}", amount, vanID);
			} else {
				logger.warn(
						"BenID auto top-up for vanID {} did not fully complete (result code {}) - will retry later",
						vanID, result);
			}
		} catch (Exception e) {
			// No user-facing alert by design - poor-network camps would
			// otherwise get spammed. Just log and let the next scheduled
			// attempt retry.
			logger.warn("BenID auto top-up failed for vanID {} - will retry on next scheduled attempt: {}", vanID,
					e.getMessage());
		} finally {
			inProgress.set(false);
		}
	}

	/**
	 * Checks how many BenIDs are genuinely claimable right now on this
	 * device. Rejects anything short of a clean, fully-parsed success -
	 * a truncated/garbled response from a dying connection must never be
	 * trusted.
	 */
	private long checkLocalAvailability() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", schedulerCredential);
		ResponseEntity<String> response = restTemplate.exchange(benCheckUrlLocal, HttpMethod.GET,
				new HttpEntity<Void>(headers), String.class);

		if (response == null || !response.hasBody()) {
			throw new IllegalStateException("Empty response checking local BenID availability");
		}
		JSONObject obj = new JSONObject(response.getBody());
		if (!obj.has("data") || !obj.has("statusCode") || obj.getInt("statusCode") != 200) {
			throw new IllegalStateException("Unexpected response checking local BenID availability: "
					+ response.getBody());
		}
		return Long.parseLong(obj.getJSONObject("data").getString("response"));
	}
}
