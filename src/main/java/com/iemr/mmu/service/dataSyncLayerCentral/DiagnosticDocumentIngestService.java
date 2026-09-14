package com.iemr.mmu.service.dataSyncLayerCentral;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

/***
 * @purpose Receives a batch of decrypted diagnostic documents pushed from a van and stores
 *          each in S3 - purely a storage relay, no database writes here. The pushing van
 *          persists its own record locally (DiagnosticDocumentPushServiceImpl.markPushedToCentral,
 *          keyed off the s3Path this returns in each ack) - the central server's own database is
 *          left untouched.
 */
@Service
public class DiagnosticDocumentIngestService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private static final Gson GSON = new Gson();

	@Value("${diagnostic.documents.s3.bucket}")
	private String bucket;

	@Autowired
	private S3Client s3Client;

	public String ingestDocuments(String requestOBJ) throws Exception {
		Type listType = new TypeToken<List<Map<String, Object>>>() {
		}.getType();
		List<Map<String, Object>> items = GSON.fromJson(requestOBJ, listType);
		if (items == null || items.isEmpty()) {
			return GSON.toJson(new ArrayList<>());
		}

		List<Map<String, Object>> acks = new ArrayList<>();
		for (Map<String, Object> item : items) {
			acks.add(ingestOne(item));
		}
		return GSON.toJson(acks);
	}

	private Map<String, Object> ingestOne(Map<String, Object> item) {
		Long diagnosticOrderId = asLong(item.get("diagnosticOrderId"));
		String externalOrderId = (String) item.get("externalOrderId");
		String documentType = (String) item.get("documentType");

		Map<String, Object> ack = new HashMap<>();
		ack.put("diagnosticOrderId", diagnosticOrderId);
		ack.put("externalOrderId", externalOrderId);
		ack.put("documentType", documentType);

		try {
			byte[] plaintext = Base64.getDecoder().decode((String) item.get("fileContentBase64"));

			String sha256Hash = (String) item.get("sha256Hash");
			if (sha256Hash != null && !sha256Hash.equalsIgnoreCase(sha256Hex(plaintext))) {
				ack.put("status", "FAILED");
				ack.put("error", "sha256 mismatch on receipt");
				return ack;
			}

			Long beneficiaryId = asLong(item.get("beneficiaryId"));
			Long villageId = asLong(item.get("villageId"));
			String orderType = (String) item.get("orderType");
			String storedFileName = (String) item.get("storedFileName");
			String s3Key = villageId + "/" + beneficiaryId + "/" + orderType + "/" + documentType + "/"
					+ storedFileName;
			String contentType = (String) item.get("contentType");

			s3Client.putObject(
					PutObjectRequest.builder().bucket(bucket).key(s3Key)
							.contentType(contentType != null ? contentType : "application/octet-stream")
							.serverSideEncryption(ServerSideEncryption.AES256).build(),
					RequestBody.fromBytes(plaintext));

			ack.put("status", "SUCCESS");
			ack.put("s3Path", s3Key);
		} catch (Exception e) {
			logger.error("Error ingesting diagnostic document: diagnosticOrderId=" + diagnosticOrderId
					+ ", documentType=" + documentType, e);
			ack.put("status", "FAILED");
			ack.put("error", e.getMessage());
		}
		return ack;
	}

	private static Long asLong(Object value) {
		return value == null ? null : ((Number) value).longValue();
	}

	private static String sha256Hex(byte[] data) throws Exception {
		byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
		StringBuilder hex = new StringBuilder(digest.length * 2);
		for (byte b : digest) {
			hex.append(String.format(Locale.ROOT, "%02x", b));
		}
		return hex.toString();
	}
}
