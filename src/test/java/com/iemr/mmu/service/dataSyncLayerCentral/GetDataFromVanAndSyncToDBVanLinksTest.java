package com.iemr.mmu.service.dataSyncLayerCentral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GetDataFromVanAndSyncToDBVanLinksTest {

	private GetDataFromVanAndSyncToDBImpl service;
	private DataSyncRepositoryCentral repo;

	@BeforeEach
	void setUp() {
		service = new GetDataFromVanAndSyncToDBImpl();
		repo = mock(DataSyncRepositoryCentral.class);
		ReflectionTestUtils.setField(service, "dataSyncRepositoryCentral", repo);
	}

	private static Map<String, Object> row(Object... kv) {
		Map<String, Object> m = new HashMap<>();
		for (int i = 0; i < kv.length; i += 2)
			m.put((String) kv[i], kv[i + 1]);
		return m;
	}

	@Test
	void prescriptionGetsTheCentralIdOfItsVisit() {
		// laptop visit 1261 of van 428 is central visit 5003 (central's own 1261 is another van's visit)
		when(repo.findCentralIdOfVanRow("db_iemr", "t_benvisitdetail", "BenVisitID", 1261, "428")).thenReturn(5003L);
		Map<String, Object> rx = row("PrescriptionID", 378, "BenVisitID", 1261, "VanID", 428);

		String error = service.translateVanLinks("db_iemr", "t_prescription", rx,
				List.of("BenVisitID", "VanSerialNo", "VanID"), "428");

		assertNull(error);
		assertEquals(5003L, rx.get("BenVisitID"));
		assertEquals(378, rx.get("PrescriptionID")); // own ID of t_prescription is not a link
	}

	@Test
	void prescribedDrugGetsCentralVisitAndPrescription() {
		when(repo.findCentralIdOfVanRow("db_iemr", "t_benvisitdetail", "BenVisitID", 1261, "428")).thenReturn(5003L);
		when(repo.findCentralIdOfVanRow("db_iemr", "t_prescription", "PrescriptionID", 378, "428")).thenReturn(9001L);
		Map<String, Object> drug = row("PrescribedDrugID", 5, "BenVisitID", 1261, "PrescriptionID", 378);

		assertNull(service.translateVanLinks("db_iemr", "t_prescribeddrug", drug,
				Arrays.asList("BenVisitID", "PrescriptionID", "VanSerialNo"), "428"));

		assertEquals(5003L, drug.get("BenVisitID"));
		assertEquals(9001L, drug.get("PrescriptionID"));
	}

	@Test
	void parentNotOnCentralYetIsNotSaved() {
		when(repo.findCentralIdOfVanRow(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(null);
		Map<String, Object> rx = row("BenVisitID", 1991);

		String error = service.translateVanLinks("db_iemr", "t_prescription", rx, List.of("BenVisitID"), "428");

		assertNotNull(error);
		assertEquals(1991, rx.get("BenVisitID")); // left as it was, row not saved
	}

	@Test
	void visitItselfAndRowsWithoutLinksAreUnchanged() {
		Map<String, Object> visit = row("BenVisitID", 1261, "VisitCode", 10042800001261L);
		assertNull(service.translateVanLinks("db_iemr", "t_benvisitdetail", visit, List.of("BenVisitID"), "428"));
		assertEquals(1261, visit.get("BenVisitID"));

		Map<String, Object> batch = row("ItemStockEntryID", 41, "FacilityID", 428);
		assertNull(service.translateVanLinks("db_iemr", "t_itemstockentry", batch, List.of("FacilityID"), "428"));

		Map<String, Object> noVan = row("BenVisitID", 1261);
		assertNull(service.translateVanLinks("db_iemr", "t_prescription", noVan, List.of("BenVisitID"), null));
		verify(repo, never()).findCentralIdOfVanRow(anyString(), anyString(), anyString(), any(), anyString());
	}

	@Test
	void linkColumnNotUploadedIsLeftAlone() {
		Map<String, Object> issue = row("PrescriptionID", 378);

		assertNull(service.translateVanLinks("db_iemr", "t_patientissue", issue, List.of("VisitCode"), "428"));

		verify(repo, never()).findCentralIdOfVanRow(anyString(), anyString(), anyString(), any(), anyString());
	}
}
