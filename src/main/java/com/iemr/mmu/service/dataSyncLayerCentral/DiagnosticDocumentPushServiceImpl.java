package com.iemr.mmu.service.dataSyncLayerCentral;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

	public String pushPendingDocuments(String Authorization) throws Exception {
		List<Map<String, Object>> pendingRows = diagnosticDocumentRepository.findPendingDocuments();
		int totalSucceeded = 0;
		List<String> failureReasons = new ArrayList<>();

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
					markFailed(rowId, "Could not read file off shared filesystem: " + e.getMessage(), failureReasons);
					continue;
				}
				if (base64Plaintext == null) {
					logger.warn("Skipping diagnostic document push, decrypt failed: id={}", rowId);
					markFailed(rowId, "Decrypt failed", failureReasons);
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
				item.put("fileContentBase64", base64Plaintext);
				payloadItems.add(item);
				rowsById.put(rowId, row);
			}

			if (payloadItems.isEmpty()) {
				continue;
			}

			String requestOBJ = new Gson().toJson(payloadItems);
			List<Map<String, Object>> acks;
			try {
				HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(requestOBJ, Authorization, "datasync");
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> response = restTemplate.exchange(diagnosticDocumentUploadUrl, HttpMethod.POST,
						request, String.class);

				if (response == null || !response.hasBody()) {
					logger.warn("No response body from central server for diagnostic document push, marking batch failed");
					markBatchFailed(rowsById, "No response body from central server", failureReasons);
					continue;
				}

				// Central server wraps every response in the shared OutputResponse envelope
				// ({"data": [...], "statusCode":200, ...}) - the ack array lives under "data".
				JsonElement parsedBody = JsonParser.parseString(response.getBody());
				if (!parsedBody.isJsonObject()) {
					logger.warn("Unexpected response shape from central server for diagnostic document push, marking batch failed");
					markBatchFailed(rowsById, "Unexpected response shape from central server", failureReasons);
					continue;
				}
				JsonObject envelope = parsedBody.getAsJsonObject();
				if (!envelope.has("statusCode") || envelope.get("statusCode").getAsInt() != 200
						|| !envelope.has("data")) {
					logger.warn("Central server reported failure for diagnostic document push batch, marking batch failed: {}",
							response.getBody());
					markBatchFailed(rowsById, "Central server reported failure: " + response.getBody(), failureReasons);
					continue;
				}

				Type ackListType = new TypeToken<List<Map<String, Object>>>() {
				}.getType();
				acks = new Gson().fromJson(envelope.get("data"), ackListType);
				if (acks == null) {
					markBatchFailed(rowsById, "Central server returned no acknowledgements", failureReasons);
					continue;
				}
			} catch (Exception e) {
				logger.error("Error calling central server for diagnostic document push, marking batch failed", e);
				markBatchFailed(rowsById, "Error calling central server: " + e.getMessage(), failureReasons);
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
					markFailed(rowId, reason != null ? reason : "Central server rejected the document", failureReasons);
				}
			}
			if (!unmatchedRowsById.isEmpty()) {
				// Central sent back fewer acks than documents we sent - whatever wasn't
				// accounted for must not be silently left at its previous status forever.
				logger.warn(
						"Diagnostic document push: {} row(s) in this batch got no matching ack back, marking failed",
						unmatchedRowsById.size());
				markBatchFailed(unmatchedRowsById, "No acknowledgement received from central server", failureReasons);
			}

			totalSucceeded += batchSuccessCount;
			logger.info("Diagnostic document push batch complete: attempted={}, succeeded={}", payloadItems.size(),
					batchSuccessCount);
		}

		if (pendingRows.isEmpty()) {
			return "No data to sync";
		}

		// Same table-level summary shape as /van-to-server (UploadDataToServerImpl) so the
		// data sync screen can read both the same way.
		int totalRecords = pendingRows.size();
		int failedRecords = failureReasons.size();
		String status;
		if (failedRecords == 0) {
			status = "success";
		} else if (totalSucceeded == 0) {
			status = "failed";
		} else {
			status = "partial";
		}

		Map<String, Object> finalResponse = new LinkedHashMap<>();
		finalResponse.put("response",
				"success".equals(status) ? "Data sync completed successfully" : "Data sync completed with failures");
		finalResponse.put("schemaName", "db_iemr");
		finalResponse.put("tableName", "tb_diagnostic_document");
		finalResponse.put("status", status);
		finalResponse.put("totalRecords", totalRecords);
		finalResponse.put("successfulRecords", totalSucceeded);
		finalResponse.put("failedRecords", failedRecords);
		if (!failureReasons.isEmpty()) {
			finalResponse.put("failureReasons", failureReasons);
		}

		logger.info("Diagnostic document push complete: {} (Success: {}, Failed: {}, Total: {})", status,
				totalSucceeded, failedRecords, totalRecords);
		return new Gson().toJson(finalResponse);
	}

	/***
	 * @purpose Marks one row failed locally and records its reason for the run summary.
	 */
	private void markFailed(Long rowId, String reason, List<String> failureReasons) {
		diagnosticDocumentRepository.markPushFailed(rowId, reason);
		failureReasons.add("documentId " + rowId + ": " + reason);
	}

	/***
	 * @purpose Called when the whole batch call to the central server fails (no/garbled
	 *          response, non-200, or a thrown exception) - marks every row that was in this
	 *          batch as failed, matching how /van-to-server marks a whole batch failed on a
	 *          connection error, rather than leaving them stuck at docsProcessed='N' forever.
	 */
	private void markBatchFailed(Map<Long, Map<String, Object>> rowsById, String reason, List<String> failureReasons) {
		for (Long rowId : rowsById.keySet()) {
			markFailed(rowId, reason, failureReasons);
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
