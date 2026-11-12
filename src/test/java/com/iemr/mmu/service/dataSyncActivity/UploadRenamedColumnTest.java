package com.iemr.mmu.service.dataSyncActivity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

class UploadRenamedColumnTest {

	private static final String VAN = "status,error_message,reason_for_refusal,push_response_json,"
			+ "date_format(last_mod_date,'%Y-%m-%d %H:%i:%s'),vanSerialNo";
	private static final String SERVER = "status,error_message,reason_for_refusal,push_response_json,last_mod_date,vanSerialNo";

	@Test
	void oldNameInSyncListUsesTheTableName() {
		Set<String> table = Set.of("status", "error_message", "reason_to_close", "push_response_json", "last_mod_date",
				"vanserialno");

		String[] cols = UploadDataToServerImpl.useRenamedColumns("tb_diagnostic_order", VAN, SERVER, table);

		assertArrayEquals(new String[] { VAN.replace("reason_for_refusal", "reason_to_close"),
				SERVER.replace("reason_for_refusal", "reason_to_close") }, cols);
	}

	@Test
	void tableStillWithOldNameIsUnchanged() {
		Set<String> table = Set.of("status", "error_message", "reason_for_refusal", "push_response_json");

		assertArrayEquals(new String[] { VAN, SERVER },
				UploadDataToServerImpl.useRenamedColumns("tb_diagnostic_order", VAN, SERVER, table));
	}

	@Test
	void newNameInSyncListOnOlderTable() {
		String van = "status,reason_to_close", server = "status,reason_to_close";

		assertArrayEquals(new String[] { "status,reason_for_refusal", "status,reason_for_refusal" },
				UploadDataToServerImpl.useRenamedColumns("tb_diagnostic_order", van, server,
						Set.of("status", "reason_for_refusal")));
	}

	@Test
	void otherTablesAreUnchanged() {
		assertArrayEquals(new String[] { VAN, SERVER }, UploadDataToServerImpl.useRenamedColumns("t_tbsuspected", VAN,
				SERVER, Set.of("reason_to_close")));
	}
}
