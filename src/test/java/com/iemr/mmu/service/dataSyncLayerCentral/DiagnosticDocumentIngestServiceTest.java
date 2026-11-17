package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class DiagnosticDocumentIngestServiceTest {

	private static final Gson GSON = new Gson();

	@InjectMocks
	private DiagnosticDocumentIngestService service;

	@Mock
	private S3Client s3Client;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(service, "bucket", "test-bucket");
	}

	@Test
	void objectFileName_stripsEncSuffix() {
		assertEquals("CAD.pdf", DiagnosticDocumentIngestService.objectFileName("CAD.enc", "pdf"));
	}

	@Test
	void objectFileName_stripsGzEncSuffix() {
		assertEquals("XRAY.dcm", DiagnosticDocumentIngestService.objectFileName("XRAY.gz.enc", "dcm"));
	}

	@Test
	void objectFileName_keepsNameWithoutEncSuffix() {
		assertEquals("report.pdf", DiagnosticDocumentIngestService.objectFileName("report", "pdf"));
		assertEquals("report", DiagnosticDocumentIngestService.objectFileName("report", null));
	}

	@Test
	void gunzip_roundTripsContent() throws IOException {
		byte[] original = "DICM fake dicom payload".getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(original, DiagnosticDocumentIngestService.gunzip(gzip(original)));
	}

	@Test
	void ingest_gzipItem_uploadsDecompressedBytesUnderCleanKey() throws Exception {
		byte[] original = "DICM fake dicom payload".getBytes(StandardCharsets.UTF_8);
		Map<String, Object> item = baseItem(original, "XRAY.gz.enc", "application/dicom", "dcm");
		item.put("fileContentBase64", Base64.getEncoder().encodeToString(gzip(original)));
		item.put("contentEncoding", "gzip");

		Map<String, Object> ack = ingestSingle(item);

		assertEquals("SUCCESS", ack.get("status"));
		assertEquals("10/20/RADIOLOGY/XRAY/XRAY.dcm", ack.get("s3Path"));
		ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
		ArgumentCaptor<RequestBody> body = ArgumentCaptor.forClass(RequestBody.class);
		verify(s3Client).putObject(request.capture(), body.capture());
		assertEquals("application/dicom", request.getValue().contentType());
		assertArrayEquals(original, body.getValue().contentStreamProvider().newStream().readAllBytes());
	}

	@Test
	void ingest_plainItem_isUploadedUnchanged() throws Exception {
		byte[] original = "%PDF-1.4 fake".getBytes(StandardCharsets.UTF_8);
		Map<String, Object> item = baseItem(original, "CAD.enc", "application/pdf", "pdf");

		Map<String, Object> ack = ingestSingle(item);

		assertEquals("SUCCESS", ack.get("status"));
		assertEquals("10/20/RADIOLOGY/XRAY/CAD.pdf", ack.get("s3Path"));
	}

	@Test
	void ingest_gzipItemWithMismatchedHash_fails() throws Exception {
		byte[] original = "DICM fake dicom payload".getBytes(StandardCharsets.UTF_8);
		Map<String, Object> item = baseItem(original, "XRAY.gz.enc", "application/dicom", "dcm");
		item.put("fileContentBase64", Base64.getEncoder().encodeToString(gzip(original)));
		item.put("contentEncoding", "gzip");
		item.put("sha256Hash", "deadbeef");

		Map<String, Object> ack = ingestSingle(item);

		assertEquals("FAILED", ack.get("status"));
		verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
	}

	private Map<String, Object> ingestSingle(Map<String, Object> item) throws Exception {
		List<Map<String, Object>> items = new ArrayList<>();
		items.add(item);
		List<Map<String, Object>> acks = GSON.fromJson(service.ingestDocuments(GSON.toJson(items)),
				new TypeToken<List<Map<String, Object>>>() {
				}.getType());
		return acks.get(0);
	}

	private static Map<String, Object> baseItem(byte[] plaintext, String storedFileName, String contentType,
			String fileExtension) throws Exception {
		Map<String, Object> item = new HashMap<>();
		item.put("documentId", 1);
		item.put("diagnosticOrderId", 2);
		item.put("beneficiaryId", 20);
		item.put("vanID", 10);
		item.put("orderType", "RADIOLOGY");
		item.put("documentType", "XRAY");
		item.put("storedFileName", storedFileName);
		item.put("contentType", contentType);
		item.put("fileExtension", fileExtension);
		item.put("sha256Hash", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(plaintext)));
		item.put("fileContentBase64", Base64.getEncoder().encodeToString(plaintext));
		return item;
	}

	private static byte[] gzip(byte[] data) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
			gz.write(data);
		}
		return out.toByteArray();
	}
}
