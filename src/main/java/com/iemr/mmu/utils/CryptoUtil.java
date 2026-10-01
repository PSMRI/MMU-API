package com.iemr.mmu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decrypts diagnostic-document files written by FLW-API's own CryptoUtil
 * (same AES/ECB/PKCS5Padding scheme and key) so they can be read directly
 * off the shared filesystem before pushing to the further central server.
 */
@Service
public class CryptoUtil {

	private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
	private static final String ALGORITHM = "AES";
	private static final String SECRET_KEY = "dev-envro-secret";

	public String decrypt(String encryptedValue) {
		try {
			SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
			return removePadding(new String(decryptedBytes, StandardCharsets.UTF_8));
		} catch (Exception e) {
			logger.error("Exception while decrypting diagnostic document", e);
			return null;
		}
	}

	private String removePadding(String value) {
		int paddingLength = value.charAt(value.length() - 1);
		return value.substring(0, value.length() - paddingLength);
	}
}
