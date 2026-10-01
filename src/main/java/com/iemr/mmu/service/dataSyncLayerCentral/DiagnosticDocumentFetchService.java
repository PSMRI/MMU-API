package com.iemr.mmu.service.dataSyncLayerCentral;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/***
 * @purpose Hands back a short-lived presigned URL for the latest successfully-pushed
 *          diagnostic document matching a beneficiary+documentType, generated on demand from
 *          the object's S3 key (s3_path) - the bucket is private, so no permanent URL is
 *          ever persisted or handed out.
 */
@Service
public class DiagnosticDocumentFetchService {

	private static final Duration URL_VALIDITY = Duration.ofMinutes(15);

	@Value("${diagnostic.documents.s3.bucket}")
	private String bucket;

	@Autowired
	private DiagnosticDocumentRepository diagnosticDocumentRepository;

	@Autowired
	private S3Presigner s3Presigner;

	/***
	 * @return null if no successfully-pushed document matches, otherwise the download details
	 *         (documentType, orderType, externalOrderId, contentType, lastModDate, downloadUrl,
	 *         urlExpiresInSeconds)
	 */
	public Map<String, Object> getLatestDocumentDownload(Long beneficiaryId, String documentType) {
		Map<String, Object> row = diagnosticDocumentRepository.findLatestDocument(beneficiaryId, documentType);
		if (row == null) {
			return null;
		}

		String s3Key = (String) row.get("s3_path");
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(s3Key).build();
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().signatureDuration(URL_VALIDITY)
				.getObjectRequest(getObjectRequest).build();
		String downloadUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

		Map<String, Object> result = new java.util.HashMap<>();
		result.put("externalOrderId", row.get("external_order_id"));
		result.put("orderType", row.get("order_type"));
		result.put("documentType", row.get("document_type"));
		result.put("contentType", row.get("content_type"));
		result.put("originalFileName", row.get("original_file_name"));
		result.put("lastModDate", String.valueOf(row.get("last_mod_date")));
		result.put("downloadUrl", downloadUrl);
		result.put("urlExpiresInSeconds", URL_VALIDITY.getSeconds());
		return result;
	}
}