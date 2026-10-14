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

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Keeps this laptop connected to the central server without storing any
 * central password.
 *
 * The operator's normal laptop login is passed here once; it is used for a
 * single central login as an API client, which returns a refresh token. Only
 * that refresh token is kept (AES-GCM encrypted with a key tied to this
 * machine). Each time a central session is needed the refresh token is
 * exchanged for a new JWT + refresh token, and the JWT for a central session
 * key.
 */
@Service
public class CentralAuthService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	public enum State {
		DISABLED, NOT_CONNECTED, CONNECTED, RECONNECT_NEEDED, PASSWORD_MISMATCH
	}

	// Central refresh tokens are only issued to API (mobile) clients.
	private static final String API_CLIENT_USER_AGENT = "okhttp/4.12.0";
	private static final String TOKEN_FILE_NAME = "central-refresh-token";
	// user name only (no password): a laptop password central rejected is not sent again,
	// also after a restart, so the central account cannot get locked by repeated attempts
	private static final String REJECTED_FILE_NAME = "central-login-rejected";

	@Value("${stoptb.enforce.vanid:false}")
	private boolean enforceVanID;

	@Value("${stoptb.van.id:0}")
	private int vanID;

	@Value("${centralLoginUrl:}")
	private String centralLoginUrl;

	@Value("${centralRefreshTokenUrl:}")
	private String centralRefreshTokenUrl;

	@Value("${centralSessionUrl:}")
	private String centralSessionUrl;

	@Value("${benid.token.dir:/opt/amrit/data}")
	private String tokenDir;

	@Value("${benid.token.secret:amrit-mmu-benid}")
	private String tokenSecret;

	@Value("${benid.http.connectTimeoutMs:15000}")
	private int connectTimeoutMs;

	@Value("${benid.http.readTimeoutMs:60000}")
	private int readTimeoutMs;

	private RestTemplate restTemplate;

	// in-memory copy; the file is only a durable backup of this value
	private volatile String refreshToken;
	private volatile State state = State.NOT_CONNECTED;
	private volatile String lastError;
	private volatile long lastRefreshAt;
	private volatile String connectedUser;
	private volatile String rejectedUser;

	@PostConstruct
	public void init() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeoutMs);
		factory.setReadTimeout(readTimeoutMs);
		restTemplate = new RestTemplate(factory);

		if (!isEnabled()) {
			state = State.DISABLED;
			logger.info("BenID auto top-up disabled (stoptb.enforce.vanid={}, stoptb.van.id={})", enforceVanID,
					vanID);
			return;
		}
		refreshToken = readTokenFile();
		rejectedUser = readRejectedUser();
		state = refreshToken != null ? State.CONNECTED : State.NOT_CONNECTED;
		logger.info("BenID auto top-up enabled for vanID {}; central token {}", vanID,
				refreshToken != null ? "loaded" : "not present yet");
	}

	public boolean isEnabled() {
		return enforceVanID && vanID > 0 && !centralLoginUrl.isEmpty();
	}

	public int getVanID() {
		return vanID;
	}

	public boolean hasToken() {
		return refreshToken != null;
	}

	public State getState() {
		return state;
	}

	public String getLastError() {
		return lastError;
	}

	public long getLastRefreshAt() {
		return lastRefreshAt;
	}

	public String getConnectedUser() {
		return connectedUser;
	}

	/**
	 * One central login with the operator's credentials (password already
	 * encrypted the way MMU-UI sends it to Common-API). Skipped when a working
	 * token already exists, so central is not hit on every laptop login. A
	 * rejected password is never retried, so the central account cannot get
	 * locked by this.
	 *
	 * @param force true when the user explicitly reconnects (e.g. Data Sync
	 *              login), even if a token exists
	 * @return true if a new token was saved
	 */
	public synchronized boolean capture(String userName, String encryptedPassword, boolean force) {
		if (!isEnabled() || userName == null || encryptedPassword == null) {
			return false;
		}
		if (!force && refreshToken != null && state == State.CONNECTED) {
			return false;
		}
		// a laptop password central already rejected is not sent again (account lock risk);
		// only a Data Sync login (force, real central password) tries again
		if (!force && userName.trim().equalsIgnoreCase(rejectedUser)) {
			logger.info("Central login skipped for user {}: password was rejected before", userName);
			return false;
		}
		try {
			JSONObject body = new JSONObject();
			body.put("userName", userName.trim());
			body.put("password", encryptedPassword);
			body.put("doLogout", false);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(HttpHeaders.USER_AGENT, API_CLIENT_USER_AGENT);

			ResponseEntity<String> response = restTemplate.exchange(centralLoginUrl, HttpMethod.POST,
					new HttpEntity<>(body.toString(), headers), String.class);
			JSONObject obj = new JSONObject(response.getBody());
			if (obj.optInt("statusCode") == 200 && obj.has("data")
					&& obj.getJSONObject("data").has("refreshToken")) {
				saveToken(obj.getJSONObject("data").getString("refreshToken"));
				connectedUser = userName.trim();
				rejectedUser = null;
				deleteRejectedUser();
				state = State.CONNECTED;
				lastError = null;
				logger.info("Central token captured for user {}", connectedUser);
				return true;
			}
			// wrong username/password or account problem: do not retry with these credentials
			lastError = obj.optString("errorMessage", "Central login rejected");
			rejectedUser = userName.trim();
			saveRejectedUser(rejectedUser);
			state = State.PASSWORD_MISMATCH;
			logger.warn("Central login rejected for user {}: {}", userName, lastError);
		} catch (Exception e) {
			// network problem: nothing saved, next login tries again
			lastError = "Central server not reachable: " + e.getMessage();
			logger.warn("Central login not possible right now: {}", e.getMessage());
		}
		return false;
	}

	/**
	 * Returns a fresh central session key (refreshToken -> JWT -> session key),
	 * or null if this laptop has no usable token. Throws on network errors so
	 * callers can retry later.
	 */
	public synchronized String getCentralSessionKey() throws Exception {
		if (!isEnabled() || refreshToken == null) {
			return null;
		}
		String jwt;
		long start = System.currentTimeMillis();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject body = new JSONObject();
			body.put("refreshToken", refreshToken);
			ResponseEntity<String> response = restTemplate.exchange(centralRefreshTokenUrl, HttpMethod.POST,
					new HttpEntity<>(body.toString(), headers), String.class);
			JSONObject obj = new JSONObject(response.getBody());
			jwt = obj.getString("jwtToken");
			if (obj.has("refreshToken")) {
				saveToken(obj.getString("refreshToken"));
			}
			lastRefreshAt = System.currentTimeMillis();
			logger.info("Central token refreshed in {} ms", lastRefreshAt - start);
		} catch (HttpStatusCodeException e) {
			if (e.getStatusCode().value() == 401) {
				// expired, revoked or user deactivated on central
				logger.warn("Central refresh token rejected ({}); a new login is needed",
						e.getResponseBodyAsString());
				deleteToken();
				state = State.RECONNECT_NEEDED;
				lastError = "Central connection expired, please log in again";
				return null;
			}
			throw e;
		}

		// JWT -> central session key; Common-API creates a new Redis session for it
		long sessionStart = System.currentTimeMillis();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Jwttoken", jwt);
		ResponseEntity<String> response = restTemplate.exchange(centralSessionUrl, HttpMethod.POST,
				new HttpEntity<>("{}", headers), String.class);
		JSONObject obj = new JSONObject(response.getBody());
		if (obj.optInt("statusCode") != 200 || !obj.has("data")) {
			throw new IllegalStateException("Central session not created: " + obj.optString("errorMessage"));
		}
		Object data = obj.get("data");
		JSONObject dataObj = data instanceof JSONObject ? (JSONObject) data : new JSONObject(data.toString());
		state = State.CONNECTED;
		lastError = null;
		logger.info("Central session created in {} ms", System.currentTimeMillis() - sessionStart);
		return dataObj.getString("key");
	}

	/** Keeps the 90-day token alive even when no IDs are needed. */
	public void keepAliveIfDue(long maxAgeMs) {
		if (refreshToken != null && System.currentTimeMillis() - lastRefreshAt > maxAgeMs) {
			try {
				getCentralSessionKey();
			} catch (Exception e) {
				logger.debug("Token keep-alive skipped: {}", e.getMessage());
			}
		}
	}

	// ---------------------------------------------------------------- token file

	private Path tokenPath() {
		return Paths.get(tokenDir, TOKEN_FILE_NAME);
	}

	private void saveToken(String token) {
		refreshToken = token;
		try {
			Path dir = Paths.get(tokenDir);
			Files.createDirectories(dir);
			setPermissions(dir, "rwx------");
			Path tmp = dir.resolve(TOKEN_FILE_NAME + ".tmp");
			Files.write(tmp, encrypt(token).getBytes(StandardCharsets.UTF_8));
			setPermissions(tmp, "rw-------");
			try {
				Files.move(tmp, tokenPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(tmp, tokenPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			// keep working from memory; the token is captured again after a restart
			logger.warn("Could not save central token to {} (kept in memory only): {}", tokenDir, e.getMessage());
		}
	}

	private String readTokenFile() {
		try {
			Path path = tokenPath();
			if (Files.exists(path)) {
				return decrypt(new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim());
			}
		} catch (Exception e) {
			logger.warn("Stored central token could not be read, a new login is needed: {}", e.getMessage());
		}
		return null;
	}

	private void deleteToken() {
		refreshToken = null;
		try {
			Files.deleteIfExists(tokenPath());
		} catch (Exception e) {
			logger.warn("Could not delete central token file: {}", e.getMessage());
		}
	}

	private String readRejectedUser() {
		try {
			Path path = Paths.get(tokenDir, REJECTED_FILE_NAME);
			if (Files.exists(path)) {
				String user = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
				return user.isEmpty() ? null : user;
			}
		} catch (Exception e) {
			logger.warn("Could not read rejected-login marker: {}", e.getMessage());
		}
		return null;
	}

	private void saveRejectedUser(String user) {
		try {
			Path dir = Paths.get(tokenDir);
			Files.createDirectories(dir);
			Path path = dir.resolve(REJECTED_FILE_NAME);
			Files.write(path, user.getBytes(StandardCharsets.UTF_8));
			setPermissions(path, "rw-------");
		} catch (Exception e) {
			logger.warn("Could not save rejected-login marker: {}", e.getMessage());
		}
	}

	private void deleteRejectedUser() {
		try {
			Files.deleteIfExists(Paths.get(tokenDir, REJECTED_FILE_NAME));
		} catch (Exception e) {
			logger.warn("Could not delete rejected-login marker: {}", e.getMessage());
		}
	}

	private static void setPermissions(Path path, String perms) {
		try {
			Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(perms));
		} catch (Exception e) {
			// non-POSIX file system; nothing more we can do
		}
	}

	private SecretKeySpec machineKey() throws Exception {
		String machineId;
		try {
			machineId = new String(Files.readAllBytes(Paths.get("/etc/machine-id")), StandardCharsets.UTF_8).trim();
		} catch (Exception e) {
			machineId = InetAddress.getLocalHost().getHostName();
		}
		byte[] key = MessageDigest.getInstance("SHA-256")
				.digest((machineId + ":" + tokenSecret).getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(key, "AES");
	}

	private String encrypt(String plain) throws Exception {
		byte[] iv = new byte[12];
		new SecureRandom().nextBytes(iv);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, machineKey(), new GCMParameterSpec(128, iv));
		byte[] enc = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
		byte[] out = new byte[iv.length + enc.length];
		System.arraycopy(iv, 0, out, 0, iv.length);
		System.arraycopy(enc, 0, out, iv.length, enc.length);
		return Base64.getEncoder().encodeToString(out);
	}

	private String decrypt(String encoded) throws Exception {
		byte[] in = Base64.getDecoder().decode(encoded);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, machineKey(), new GCMParameterSpec(128, in, 0, 12));
		return new String(cipher.doFinal(in, 12, in.length - 12), StandardCharsets.UTF_8);
	}
}
