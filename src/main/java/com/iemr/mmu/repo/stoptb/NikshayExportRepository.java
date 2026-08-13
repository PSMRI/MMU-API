/*
* AMRIT – Accessible Medical Records via Integrated Technology
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.repo.stoptb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Reads Stop TB camp beneficiaries for the Nikshay ID Generator CSV export.
 *
 * Joins MMU's own beneficiary/demographic tables with the Stop TB dual-write
 * tables (tb_stoptb_*) and the relevant master tables, resolving one row per
 * beneficiary who has a visit in the requested van/service-point/date range.
 * Beneficiaries that already have a Nikshay ID recorded in
 * tb_stoptb_diagnostics are excluded from the streamed rows (they don't need
 * a new one) but are counted separately so callers can report how many were
 * skipped as already-generated.
 */
@Repository
public class NikshayExportRepository {

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}

	/** One camp beneficiary's raw, unmapped source data — Nikshay-vocabulary
	 * mapping/validation happens in the service layer, not here. benRegId/diagnosticsId
	 * are AMRIT-internal bookkeeping (for export-batch row tracking), not CSV output. */
	public record NikshayRawRow(Long benRegId, Long diagnosticsId, String firstName, String middleLastName,
			Integer age, String gender, String phone, String address, String stateName, String districtName,
			String healthFacility, String village, String pincode, String maritalStatus, String caste,
			String occupation, String socioeconomicStatus, String chiefComplaint, String hivStatus,
			Boolean isHivPos) {
	}

	// Placeholders in order: [1] vanID, [2] parkingPlaceID (both for the diagnosticsId
	// lookup), [3] parkingPlaceID (facility-name join), [4] vanID, [5] parkingPlaceID
	// (visit filter), [6] fromDate (inclusive), [7] toDate-exclusive-upper-bound.
	private static final String BASE_SELECT = "SELECT "
			+ "  b.BeneficiaryRegID AS benRegId, "
			+ "  (SELECT diag2.id FROM tb_stoptb_diagnostics diag2 WHERE diag2.ben_reg_id = b.BeneficiaryRegID "
			+ "     AND diag2.vanID = ? AND diag2.parkingPlaceID = ? AND diag2.deleted = 0 "
			+ "     ORDER BY diag2.id DESC LIMIT 1) AS diagnosticsId, "
			+ "  b.FirstName AS firstName, "
			+ "  TRIM(CONCAT(COALESCE(b.MiddleName,''),' ',COALESCE(b.LastName,''))) AS middleLastName, "
			+ "  TIMESTAMPDIFF(YEAR, b.DOB, CURDATE()) AS age, "
			+ "  g.GenderName AS gender, "
			+ "  (SELECT p.PhoneNo FROM i_benphonemap p WHERE p.BenificiaryRegID = b.BeneficiaryRegID "
			+ "     AND p.Deleted = 0 ORDER BY p.BenPhMapID ASC LIMIT 1) AS phone, "
			+ "  TRIM(CONCAT_WS(', ', d.AddressLine1, d.AddressLine2, d.AddressLine3, d.AddressLine4, d.AddressLine5)) AS address, "
			+ "  st.StateName AS stateName, "
			+ "  dist.DistrictName AS districtName, "
			+ "  pp.ParkingPlaceName AS healthFacility, "
			+ "  vill.VillageName AS village, "
			+ "  COALESCE(d.PinCode, vill.PinCode) AS pincode, "
			+ "  ms.Status AS maritalStatus, "
			+ "  c.CommunityType AS caste, "
			+ "  occ.OccupationType AS occupation, "
			+ "  inc.IncomeStatus AS socioeconomicStatus, "
			+ "  (SELECT o.chief_complaint FROM tb_stoptb_general_opd o WHERE o.ben_reg_id = b.BeneficiaryRegID "
			+ "     AND o.deleted = 0 ORDER BY o.id DESC LIMIT 1) AS chiefComplaint, "
			+ "  (SELECT ge.hiv_status FROM tb_stoptb_general_examination ge WHERE ge.beneficiary_reg_id = b.BeneficiaryRegID "
			+ "     AND ge.deleted = 0 ORDER BY ge.id DESC LIMIT 1) AS hivStatus, "
			+ "  b.IsHIVPos AS isHivPos, "
			+ "  (SELECT diag.nikshay_id FROM tb_stoptb_diagnostics diag WHERE diag.ben_reg_id = b.BeneficiaryRegID "
			+ "     AND diag.nikshay_id IS NOT NULL AND diag.deleted = 0 ORDER BY diag.id DESC LIMIT 1) AS existingNikshayId "
			+ "FROM i_beneficiary b "
			+ "LEFT JOIN I_bendemographics d ON d.BeneficiaryRegID = b.BeneficiaryRegID "
			+ "LEFT JOIN m_gender g ON g.GenderID = b.GenderID "
			+ "LEFT JOIN m_maritalstatus ms ON ms.MaritalStatusID = b.MaritalStatusID "
			+ "LEFT JOIN m_community c ON c.CommunityID = d.CommunityID "
			+ "LEFT JOIN m_beneficiaryincomestatus inc ON inc.IncomeStatusID = d.IncomeStatusID "
			+ "LEFT JOIN m_beneficiaryoccupation occ ON occ.OccupationID = d.OccupationID "
			+ "LEFT JOIN m_DistrictBranchMapping vill ON vill.DistrictBranchID = d.DistrictBranchID "
			+ "LEFT JOIN m_parkingplace pp ON pp.ParkingPlaceID = ? "
			+ "LEFT JOIN m_state st ON st.StateID = pp.StateID "
			+ "LEFT JOIN m_district dist ON dist.DistrictID = pp.DistrictID "
			+ "WHERE b.Deleted = 0 "
			+ "  AND b.BeneficiaryRegID IN ( "
			+ "    SELECT DISTINCT v.beneficiary_reg_id FROM tb_stoptb_visit v "
			+ "    WHERE v.vanID = ? AND v.parkingPlaceID = ? AND v.visit_date >= ? AND v.visit_date < ? "
			+ "  )";

	public int countAlreadyGenerated(Integer vanID, Integer servicePointID, LocalDate fromDate, LocalDate toDate) {
		String sql = "SELECT COUNT(*) FROM (" + BASE_SELECT + ") t WHERE t.existingNikshayId IS NOT NULL";
		Integer count = getJdbcTemplate().query(sql, pss(vanID, servicePointID, fromDate, toDate),
				rs -> rs.next() ? rs.getInt(1) : 0);
		return count == null ? 0 : count;
	}

	/** Streams every not-yet-Nikshay-ID'd beneficiary for the camp/date range to
	 * {@code rowConsumer} one row at a time, without materializing the full
	 * result set in memory — safe for large date ranges. */
	public void streamPendingBeneficiaries(Integer vanID, Integer servicePointID, LocalDate fromDate,
			LocalDate toDate, Consumer<NikshayRawRow> rowConsumer) {
		String sql = "SELECT * FROM (" + BASE_SELECT + ") t WHERE t.existingNikshayId IS NULL";
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		// MySQL Connector/J-specific: Integer.MIN_VALUE forces true row-by-row
		// network streaming instead of buffering the whole result set client-side.
		jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
		jdbcTemplate.query(sql, pss(vanID, servicePointID, fromDate, toDate),
				(ResultSet rs) -> rowConsumer.accept(mapRow(rs)));
	}

	private PreparedStatementSetter pss(Integer vanID, Integer servicePointID, LocalDate fromDate,
			LocalDate toDate) {
		return (PreparedStatement ps) -> {
			ps.setInt(1, vanID);
			ps.setInt(2, servicePointID);
			ps.setInt(3, servicePointID);
			ps.setInt(4, vanID);
			ps.setInt(5, servicePointID);
			ps.setTimestamp(6, Timestamp.valueOf(fromDate.atStartOfDay()));
			ps.setTimestamp(7, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
		};
	}

	private NikshayRawRow mapRow(ResultSet rs) throws SQLException {
		return new NikshayRawRow(
				rs.getObject("benRegId", Long.class),
				rs.getObject("diagnosticsId", Long.class),
				rs.getString("firstName"),
				rs.getString("middleLastName"),
				rs.getObject("age", Integer.class),
				rs.getString("gender"),
				rs.getString("phone"),
				rs.getString("address"),
				rs.getString("stateName"),
				rs.getString("districtName"),
				rs.getString("healthFacility"),
				rs.getString("village"),
				rs.getString("pincode"),
				rs.getString("maritalStatus"),
				rs.getString("caste"),
				rs.getString("occupation"),
				rs.getString("socioeconomicStatus"),
				rs.getString("chiefComplaint"),
				rs.getString("hivStatus"),
				rs.getObject("isHivPos", Boolean.class));
	}

	/** Camp/date-range parameters an export batch was created for — needed on the
	 * import side to create a diagnostics row for a beneficiary that didn't have one yet. */
	public record ExportBatch(Integer vanID, Integer servicePointID, LocalDate fromDate, LocalDate toDate) {
	}

	/** One tracked CSV row: which beneficiary/diagnostics row it corresponds to. */
	public record ExportBatchRow(int rowIndex, Long benRegId, Long diagnosticsId) {
	}

	/** Creates the batch header row up front (before streaming starts) so its ID can
	 * go out as a response header immediately, ahead of the streamed CSV body. */
	public Long createExportBatch(Integer vanID, Integer servicePointID, LocalDate fromDate, LocalDate toDate,
			String createdBy) {
		String sql = "INSERT INTO tb_stoptb_nikshay_export_batch "
				+ "(vanID, parkingPlaceID, from_date, to_date, created_by) VALUES (?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, vanID);
			ps.setInt(2, servicePointID);
			ps.setDate(3, java.sql.Date.valueOf(fromDate));
			ps.setDate(4, java.sql.Date.valueOf(toDate));
			ps.setString(5, createdBy);
			return ps;
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}

	/** Records one CSV row's beneficiary/diagnostics identity against the batch,
	 * called as each row is streamed out. */
	public void addBatchRow(Long batchId, int rowIndex, Long benRegId, Long diagnosticsId) {
		String sql = "INSERT INTO tb_stoptb_nikshay_export_batch_row "
				+ "(batch_id, row_index, ben_reg_id, diagnostics_id) VALUES (?, ?, ?, ?)";
		getJdbcTemplate().update(sql, batchId, rowIndex, benRegId, diagnosticsId);
	}

	public void finalizeBatchRowCount(Long batchId, int rowCount) {
		getJdbcTemplate().update("UPDATE tb_stoptb_nikshay_export_batch SET row_count = ? WHERE id = ?", rowCount,
				batchId);
	}

	public ExportBatch getBatch(Long batchId) {
		String sql = "SELECT vanID, parkingPlaceID, from_date, to_date FROM tb_stoptb_nikshay_export_batch WHERE id = ?";
		return getJdbcTemplate().query(sql, (ResultSet rs) -> rs.next()
				? new ExportBatch(rs.getInt("vanID"), rs.getInt("parkingPlaceID"), rs.getDate("from_date").toLocalDate(),
						rs.getDate("to_date").toLocalDate())
				: null, batchId);
	}

	/** All tracked rows for a batch, in the same order they were streamed into the CSV. */
	public List<ExportBatchRow> getBatchRows(Long batchId) {
		String sql = "SELECT row_index, ben_reg_id, diagnostics_id FROM tb_stoptb_nikshay_export_batch_row "
				+ "WHERE batch_id = ? ORDER BY row_index ASC";
		return getJdbcTemplate().query(sql,
				(rs, rowNum) -> new ExportBatchRow(rs.getInt("row_index"), rs.getLong("ben_reg_id"),
						rs.getObject("diagnostics_id", Long.class)),
				batchId);
	}

	public void updateNikshayId(Long diagnosticsId, String nikshayId, String modifiedBy) {
		String sql = "UPDATE tb_stoptb_diagnostics SET nikshay_id = ?, modified_by = ?, "
				+ "last_mod_date = CURRENT_TIMESTAMP WHERE id = ?";
		getJdbcTemplate().update(sql, nikshayId, modifiedBy, diagnosticsId);
	}

	/** Called when a beneficiary had no tb_stoptb_diagnostics row for this camp visit yet
	 * at export time — creates one to hold the Nikshay ID the portal generated. */
	public Long insertDiagnosticsWithNikshayId(Long benRegId, Integer vanID, Integer servicePointID,
			LocalDate visitDate, String nikshayId, String createdBy) {
		String sql = "INSERT INTO tb_stoptb_diagnostics "
				+ "(ben_reg_id, vanID, parkingPlaceID, visit_date, nikshay_id, created_by) VALUES (?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, benRegId);
			ps.setInt(2, vanID);
			ps.setInt(3, servicePointID);
			ps.setTimestamp(4, Timestamp.valueOf(visitDate.atStartOfDay()));
			ps.setString(5, nikshayId);
			ps.setString(6, createdBy);
			return ps;
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}
}
