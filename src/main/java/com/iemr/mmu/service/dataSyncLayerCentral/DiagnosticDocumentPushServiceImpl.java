package com.iemr.mmu.service.dataSyncLayerCentral;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.iemr.mmu.utils.CryptoUtil;
import com.iemr.mmu.utils.RestTemplateUtil;

import java.lang.reflect.Type;

/***
 * @purpose Reads MMU-API's own locally-pending diagnostic documents (docsProcessed='N',
 *          shared db_iemr.tb_diagnostic_document table), decrypts the file off the shared
 *          filesystem, and pushes each batch to the further central server's
 *          /dataSync/diagnostic-documents endpoint - the same relay shape as
 *          UploadDataToServerImpl's push to dataSyncUploadUrl, just for this table instead
 *          of the generic sync-group config. MMU-API's own /dataSync/diagnostic-documents
 *          ingest endpoint (DiagnosticDocumentCentralIngestService) is untouched by this -
 *          this service is a separate outbound relay, not a caller of it.
 */
@Service
public class DiagnosticDocumentPushServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = new HashMap<>();
	static {
		CONTENT_TYPE_EXTENSIONS.put("application/pdf", "pdf");
		CONTENT_TYPE_EXTENSIONS.put("image/jpeg", "jpg");
		CONTENT_TYPE_EXTENSIONS.put("image/png", "png");
	}

	@Value("${diagnostic.documents.storage-root}")
	private String storageRoot;

	@Value("${diagnosticDocumentUploadUrl}")
	private String diagnosticDocumentUploadUrl;

	@Value("${diagnosticDocument.push.batchSize:3}")
	private int batchSize;

	@Autowired
	private DiagnosticDocumentRepository diagnosticDocumentRepository;

	@Autowired
	private CryptoUtil cryptoUtil;

	public String pushPendingDocuments(String Authorization, Long villageId) throws Exception {
		List<Map<String, Object>> pendingRows = diagnosticDocumentRepository.findPendingDocuments();
		boolean anyRowsFound = !pendingRows.isEmpty();
		int totalAttempted = 0;
		int totalSucceeded = 0;

		for (int offset = 0; offset < pendingRows.size(); offset += batchSize) {
			List<Map<String, Object>> rows = pendingRows.subList(offset, Math.min(offset + batchSize, pendingRows.size()));

			List<Map<String, Object>> payloadItems = new ArrayList<>();
			Map<Long, Map<String, Object>> rowsById = new HashMap<>();
			for (Map<String, Object> row : rows) {
				Long rowId = asLong(row.get("id"));
				String base64Plaintext;
				try {
					String storedPath = (String) row.get("stored_path");
					Path filePath = Paths.get(storageRoot, storedPath);
					String encryptedPayload = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
					base64Plaintext = cryptoUtil.decrypt(encryptedPayload);
				} catch (Exception e) {
					logger.warn("Skipping diagnostic document push, could not read file off disk: id={}, error={}",
							rowId, e.getMessage());
					diagnosticDocumentRepository.markPushFailed(rowId,
							"Could not read file off shared filesystem: " + e.getMessage());
					continue;
				}
				if (base64Plaintext == null) {
					logger.warn("Skipping diagnostic document push, decrypt failed: id={}", rowId);
					diagnosticDocumentRepository.markPushFailed(rowId, "Decrypt failed");
					continue;
				}

				Long diagnosticOrderId = asLong(row.get("diagnostic_order_id"));
				String externalOrderId = (String) row.get("external_order_id");
				String documentType = (String) row.get("document_type");
				String contentType = (String) row.get("content_type");

				Map<String, Object> item = new HashMap<>();
				item.put("documentId", rowId);
				item.put("diagnosticOrderId", diagnosticOrderId);
				item.put("externalOrderId", externalOrderId);
				item.put("beneficiaryId", row.get("beneficiary_id"));
				item.put("orderType", row.get("order_type"));
				item.put("documentType", documentType);
				item.put("storedFileName", row.get("stored_file_name"));
				item.put("sha256Hash", row.get("sha256_hash"));
				item.put("contentType", contentType);
				item.put("fileExtension", extensionFor(contentType));
				item.put("originalFileName", row.get("original_file_name"));
				item.put("vanID", row.get("vanID"));
				item.put("parkingPlaceID", row.get("parkingPlaceID"));
				item.put("vanSerialNo", row.get("vanSerialNo"));
				item.put("villageId", villageId);
				item.put("fileContentBase64", base64Plaintext);
				payloadItems.add(item);
				rowsById.put(rowId, row);
			}

			if (payloadItems.isEmpty()) {
				continue;
			}

			totalAttempted += payloadItems.size();

			String requestOBJ = new Gson().toJson(payloadItems);
			List<Map<String, Object>> acks;
			try {
				HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(requestOBJ, Authorization, "datasync");
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> response = restTemplate.exchange(diagnosticDocumentUploadUrl, HttpMethod.POST,
						request, String.class);

				if (response == null || !response.hasBody()) {
					logger.warn("No response body from central server for diagnostic document push, marking batch failed");
					markBatchFailed(rowsById, "No response body from central server");
					continue;
				}

				// Central server wraps every response in the shared OutputResponse envelope
				// ({"data": [...], "statusCode":200, ...}) - the ack array lives under "data".
				JsonElement parsedBody = JsonParser.parseString(response.getBody());
				if (!parsedBody.isJsonObject()) {
					logger.warn("Unexpected response shape from central server for diagnostic document push, marking batch failed");
					markBatchFailed(rowsById, "Unexpected response shape from central server");
					continue;
				}
				JsonObject envelope = parsedBody.getAsJsonObject();
				if (!envelope.has("statusCode") || envelope.get("statusCode").getAsInt() != 200
						|| !envelope.has("data")) {
					logger.warn("Central server reported failure for diagnostic document push batch, marking batch failed: {}",
							response.getBody());
					markBatchFailed(rowsById, "Central server reported failure: " + response.getBody());
					continue;
				}

				Type ackListType = new TypeToken<List<Map<String, Object>>>() {
				}.getType();
				acks = new Gson().fromJson(envelope.get("data"), ackListType);
				if (acks == null) {
					markBatchFailed(rowsById, "Central server returned no acknowledgements");
					continue;
				}
			} catch (Exception e) {
				logger.error("Error calling central server for diagnostic document push, marking batch failed", e);
				markBatchFailed(rowsById, "Error calling central server: " + e.getMessage());
				continue;
			}

			int batchSuccessCount = 0;
			Map<Long, Map<String, Object>> unmatchedRowsById = new HashMap<>(rowsById);
			for (Map<String, Object> ack : acks) {
				Long rowId = ack.get("documentId") instanceof Number ? asLong(ack.get("documentId")) : null;
				if (rowId == null || unmatchedRowsById.remove(rowId) == null) {
					continue;
				}
				if ("SUCCESS".equalsIgnoreCase((String) ack.get("status"))) {
					diagnosticDocumentRepository.markPushedToCentral(rowId, (String) ack.get("s3Path"));
					batchSuccessCount++;
				} else {
					String reason = (String) ack.get("error");
					logger.warn("Central server rejected diagnostic document push: id={}, externalOrderId={}, documentType={}, error={}",
							rowId, ack.get("externalOrderId"), ack.get("documentType"), reason);
					diagnosticDocumentRepository.markPushFailed(rowId,
							reason != null ? reason : "Central server rejected the document");
				}
			}
			if (!unmatchedRowsById.isEmpty()) {
				// Central sent back fewer acks than documents we sent - whatever wasn't
				// accounted for must not be silently left at its previous status forever.
				logger.warn(
						"Diagnostic document push: {} row(s) in this batch got no matching ack back, marking failed",
						unmatchedRowsById.size());
				markBatchFailed(unmatchedRowsById, "No acknowledgement received from central server");
			}

			totalSucceeded += batchSuccessCount;
			logger.info("Diagnostic document push batch complete: attempted={}, succeeded={}", payloadItems.size(),
					batchSuccessCount);
		}

		if (!anyRowsFound) {
			return "No pending diagnostic documents to sync";
		}
		if (totalAttempted == 0) {
			return "No documents could be decrypted for push";
		}
		if (totalSucceeded == 0) {
			// Documents WERE decrypted and sent, but none were accepted (e.g. the central
			// server rejected every batch) - the specific reason for each row is recorded in
			// its own docSyncFailureReason, this is just the overall-outcome summary.
			return "Documents were sent but none were accepted by the central server";
		}

		logger.info("Diagnostic document push complete overall: attempted={}, succeeded={}", totalAttempted,
				totalSucceeded);
		return "Data successfully synced";
	}

	/***
	 * @purpose Called when the whole batch call to the central server fails (no/garbled
	 *          response, non-200, or a thrown exception) - marks every row that was in this
	 *          batch as failed, matching how /van-to-server marks a whole batch failed on a
	 *          connection error, rather than leaving them stuck at docsProcessed='N' forever.
	 */
	private void markBatchFailed(Map<Long, Map<String, Object>> rowsById, String reason) {
		for (Long rowId : rowsById.keySet()) {
			diagnosticDocumentRepository.markPushFailed(rowId, reason);
		}
	}

	private static Long asLong(Object value) {
		return value == null ? null : ((Number) value).longValue();
	}

	private static String extensionFor(String contentType) {
		if (contentType == null) {
			return "bin";
		}
		String extension = CONTENT_TYPE_EXTENSIONS.get(contentType.toLowerCase());
		if (extension != null) {
			return extension;
		}
		int slashIndex = contentType.indexOf('/');
		return slashIndex >= 0 && slashIndex < contentType.length() - 1 ? contentType.substring(slashIndex + 1) : "bin";
	}
}
