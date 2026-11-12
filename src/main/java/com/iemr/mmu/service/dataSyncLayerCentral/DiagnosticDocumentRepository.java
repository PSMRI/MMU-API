package com.iemr.mmu.service.dataSyncLayerCentral;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/***
 * @purpose Local (van) side persistence for the diagnostic-document push pipeline - the central
 *          server is a pure S3 storage relay (DiagnosticDocumentCentralIngestService) and writes
 *          nothing to its own database, so this repository only ever operates on the pushing
 *          van's own local db_iemr.tb_diagnostic_document rows.
 */
@Service
public class DiagnosticDocumentRepository {

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (this.jdbcTemplate == null) {
			this.jdbcTemplate = new JdbcTemplate(dataSource);
		}
		return this.jdbcTemplate;
	}

	/***
	 * @purpose Latest successfully-pushed document for a beneficiary+documentType, used to hand
	 *          back a fresh presigned URL on demand instead of persisting a permanent (and, for a
	 *          private bucket, non-functional) URL. s3_path holds the S3 key for a
	 *          docsProcessed='P' row (markPushedToCentral() below writes it there).
	 */
	public Map<String, Object> findLatestDocument(Long beneficiaryId, String documentType) {
		String query = "SELECT id, external_order_id, order_type, document_type, s3_path, content_type, "
				+ "original_file_name, last_mod_date FROM db_iemr.tb_diagnostic_document "
				+ "WHERE beneficiary_id = ? AND document_type = ? AND docsProcessed = 'P' "
				+ "ORDER BY last_mod_date DESC, id DESC LIMIT 1";
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(query, beneficiaryId, documentType);
		return rows.isEmpty() ? null : rows.get(0);
	}

	/***
	 * @purpose Snapshot of every locally-pending row's metadata (docsProcessed='N' or 'F') at
	 *          the start of a push run, taken in one query so a row this same run marks 'F'
	 *          partway through (e.g. an auth failure) is never re-picked-up later in the same
	 *          run - it only becomes eligible again on the next trigger, alongside whatever's
	 *          newly 'N' by then. Only metadata is loaded here (id, hashes, filenames, etc.) -
	 *          each row's actual file content is still read off disk and decrypted one batch at
	 *          a time by the caller, so the whole backlog's decrypted content is never held in
	 *          memory at once.
	 */
	public List<Map<String, Object>> findPendingDocuments() {
		String query = "SELECT id, diagnostic_order_id, external_order_id, beneficiary_id, order_type, document_type, "
				+ "stored_file_name, stored_path, sha256_hash, content_type, original_file_name, "
				+ "vanID, parkingPlaceID, vanSerialNo FROM db_iemr.tb_diagnostic_document "
				+ "WHERE docsProcessed = 'N' OR docsProcessed = 'F' ORDER BY id ASC";
		return getJdbcTemplate().queryForList(query);
	}

	/***
	 * @purpose Marks a document as successfully pushed on the LOCAL (van) row - the central
	 *          server itself writes nothing to its own database. s3Path is the S3 key the
	 *          central server's ack reported back, persisted here so the van's own local DB
	 *          knows where the document ended up, not just that it did.
	 */
	public void markPushedToCentral(Long id, String s3Path) {
		String update = "UPDATE db_iemr.tb_diagnostic_document SET processed = 'N', docsProcessed = 'P', "
				+ "s3_path = ?, docSyncedDate = NOW(), docSyncFailureReason = NULL, last_mod_date = NOW() WHERE id = ?";
		getJdbcTemplate().update(update, s3Path, id);
	}

	/***
	 * @purpose Marks a document push to the further central server as failed - docsProcessed='F',
	 *          matching the P/F status convention used by the generic sync pipeline's
	 *          DataSyncRepository.updateProcessedFlagInVan for /van-to-server. Persists why in
	 *          docSyncFailureReason (mirroring the generic pipeline's own SyncFailureReason, kept
	 *          separate since this pipeline's docsProcessed/docSyncedDate are their own dedicated
	 *          columns) so a failure isn't only visible in the application log. Also clears
	 *          s3_path - otherwise a row that succeeded once, then got re-attempted and failed,
	 *          would keep showing a stale S3 key while docsProcessed says 'F'. docSyncedDate is
	 *          left untouched - it records the last time this row was actually confirmed synced,
	 *          if ever.
	 */
	public void markPushFailed(Long id, String reason) {
		String update = "UPDATE db_iemr.tb_diagnostic_document SET processed = 'N', docsProcessed = 'F', "
				+ "s3_path = NULL, docSyncFailureReason = ?, last_mod_date = NOW() WHERE id = ?";
		getJdbcTemplate().update(update, reason, id);
	}
}
