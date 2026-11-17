package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiagnosticDocumentPushServiceImplTest {

	@Test
	void extensionFor_mapsDicomToDcm() {
		assertEquals("dcm", DiagnosticDocumentPushServiceImpl.extensionFor("application/dicom"));
		assertEquals("dcm", DiagnosticDocumentPushServiceImpl.extensionFor("APPLICATION/DICOM"));
	}

	@Test
	void extensionFor_keepsExistingMappingsAndFallbacks() {
		assertEquals("pdf", DiagnosticDocumentPushServiceImpl.extensionFor("application/pdf"));
		assertEquals("jpg", DiagnosticDocumentPushServiceImpl.extensionFor("image/jpeg"));
		assertEquals("webp", DiagnosticDocumentPushServiceImpl.extensionFor("image/webp"));
		assertEquals("bin", DiagnosticDocumentPushServiceImpl.extensionFor(null));
	}
}
