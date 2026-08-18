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
 * Reads Stop TB beneficiaries for the Nikshay ID Generator CSV export.
 *
 * All table/column names here are verified against real data on a live
 * server (2026-08-18), not assumed from convention — an earlier version of
 * this class used i_beneficiary/I_bendemographics/tb_stoptb_visit, which
 * turned out not to exist at all. The real picture:
 *
 * - Visit filter: db_iemr.t_benvisitdetail (VisitCategory = 'Stop TB'),
 *   NOT tb_stoptb_visit — confirmed 511 real rows on the reference server,
 *   vs. zero for the table this used to query.
 * - Beneficiary identity: MMU's single datasource only connects to db_iemr,
 *   but the actual beneficiary/demographic tables live in db_identity, on
 *   the same physical MySQL server — reached here via fully-qualified
 *   cross-schema table names. The chain is
 *   db_identity.i_beneficiarymapping (BenRegId, unique) -> BenDetailsId ->
 *   db_identity.i_beneficiarydetails (name/DOB/gender/caste/occupation/
 *   income/HIV — already denormalized to readable strings, no separate
 *   master-table joins needed) and BenAddressId ->
 *   db_identity.i_beneficiaryaddress (address/village/pincode), plus
 *   BenContactsId -> db_identity.i_beneficiarycontacts (phone).
 * - Confirmed real gap, not a bug: many beneficiaries registered through
 *   the van-local registrar flow (i_beneficiarymapping.CreatedBy =
 *   'reglocal') have a mapping row but their BenDetailsId/BenAddressId
 *   never synced to db_identity — sometimes for many days. Rows with no
 *   synced identity are silently left out (see streamPendingBeneficiaries)
 *   the same way an unresolved location is — there's nothing to put in a
 *   CSV row for someone whose name/address was never actually synced.
 *
 * Location columns (village/healthFacility/tu/district/state) are resolved
 * against Nikshay's own, isolated location hierarchy — m_nikshay_village →
 * m_nikshay_facility → m_nikshay_tu → m_nikshay_district → m_nikshay_state.
 * This walk currently starts from the beneficiary's own personal
 * i_beneficiaryaddress.CurrVillageId — confirmed on real data that this is
 * AMRIT's own village ID, not Nikshay's (same numeric ID resolves to a
 * different real place in each hierarchy), so this is a best-effort,
 * unconfirmed mapping: it only produces a row when that AMRIT village ID
 * happens to also be a valid Nikshay village ID, which will under-match.
 * Whether Nikshay location should instead come from the camp/facility
 * (via the logged-in worker's own assigned NikshayTUID/NikshayFacilityID
 * on m_userservicerolemapping) rather than the beneficiary's personal
 * address is still an open question — not yet resolved.
 *
 * Beneficiaries are silently left out of the streamed rows (see
 * streamPendingBeneficiaries) in three cases — already having a Nikshay ID
 * recorded, their identity never having synced to db_identity, or their
 * village not resolving all the way up to a state. All three are counted so
 * callers can report totals, but there is deliberately no separate report
 * of *who* was skipped or why.
 *
 * The Nikshay ID itself lives on db_iemr.tb_suspected.nikshay_id — not
 * tb_stoptb_diagnostics, which also has a nikshay_id column but is not the
 * table this feature writes to.
 */
@Repository
public class NikshayExportRepository {

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}

	/** One beneficiary's raw, unmapped source data — Nikshay-vocabulary
	 * mapping/validation happens in the service layer, not here. benRegId is
	 * carried into the CSV itself (as a pass-through column the Nikshay ID
	 * Generator app never touches) so results can be matched back to a
	 * beneficiary on import without needing any AMRIT-side row tracking. */
	public record NikshayRawRow(Long benRegId, String firstName, String middleLastName, Integer age, String gender,
			String phone, String address, String stateName, String districtName, String tu, String healthFacility,
			String village, String pincode, String maritalStatus, String caste, String occupation,
			String socioeconomicStatus, String chiefComplaint, String hivStatus, Boolean isHivPos) {
	}

	// Placeholders in order: [1] fromDate (inclusive), [2] toDate-exclusive-upper-bound.
	private static final String BASE_SELECT = "SELECT "
			+ "  m.BenRegId AS benRegId, "
			+ "  d.FirstName AS firstName, "
			+ "  TRIM(CONCAT(COALESCE(d.MiddleName,''),' ',COALESCE(d.LastName,''))) AS middleLastName, "
			+ "  TIMESTAMPDIFF(YEAR, d.DOB, CURDATE()) AS age, "
			+ "  d.Gender AS gender, "
			+ "  c.PhoneNum1 AS phone, "
			+ "  COALESCE(d.address, a.CurrAddressValue) AS address, "
			+ "  ns.StateName AS stateName, "
			+ "  nd.DistrictName AS districtName, "
			+ "  ntu.TUName AS tu, "
			+ "  nf.FacilityName AS healthFacility, "
			+ "  nv.VillageName AS village, "
			+ "  a.CurrPinCode AS pincode, "
			+ "  d.MaritalStatus AS maritalStatus, "
			+ "  d.community AS caste, "
			+ "  d.occupation AS occupation, "
			+ "  d.incomeStatus AS socioeconomicStatus, "
			+ "  (SELECT o.chief_complaint FROM tb_stoptb_general_opd o WHERE o.ben_reg_id = m.BenRegId "
			+ "     AND o.deleted = 0 ORDER BY o.id DESC LIMIT 1) AS chiefComplaint, "
			+ "  (SELECT ge.hiv_status FROM tb_stoptb_general_examination ge WHERE ge.beneficiary_reg_id = m.BenRegId "
			+ "     AND ge.deleted = 0 ORDER BY ge.id DESC LIMIT 1) AS hivStatus, "
			+ "  d.IsHIVPositive AS isHivPos, "
			+ "  (SELECT s.nikshay_id FROM tb_suspected s WHERE s.benRegID = m.BenRegId "
			+ "     AND s.nikshay_id IS NOT NULL ORDER BY s.id DESC LIMIT 1) AS existingNikshayId "
			+ "FROM db_identity.i_beneficiarymapping m "
			+ "LEFT JOIN db_identity.i_beneficiarydetails d ON d.BeneficiaryDetailsId = m.BenDetailsId AND d.Deleted = 0 "
			+ "LEFT JOIN db_identity.i_beneficiaryaddress a ON a.BenAddressID = m.BenAddressId "
			+ "LEFT JOIN db_identity.i_beneficiarycontacts c ON c.BenContactsId = m.BenContactsId "
			// a.CurrVillageId is the beneficiary's own AMRIT village ID, not confirmed
			// to be a Nikshay Village ID — see class Javadoc "still an open question".
			+ "LEFT JOIN m_nikshay_village nv ON nv.NikshayVillageID = a.CurrVillageId AND nv.Deleted = 0 "
			+ "LEFT JOIN m_nikshay_facility nf ON nf.NikshayFacilityID = nv.NikshayFacilityID AND nf.Deleted = 0 "
			+ "LEFT JOIN m_nikshay_tu ntu ON ntu.NikshayTUID = nf.NikshayTUID AND ntu.Deleted = 0 "
			+ "LEFT JOIN m_nikshay_district nd ON nd.NikshayDistrictID = ntu.NikshayDistrictID AND nd.Deleted = 0 "
			+ "LEFT JOIN m_nikshay_state ns ON ns.NikshayStateID = nd.NikshayStateID AND ns.Deleted = 0 "
			+ "WHERE m.Deleted = 0 "
			+ "  AND m.BenRegId IN ( "
			+ "    SELECT DISTINCT v.BeneficiaryRegID FROM t_benvisitdetail v "
			+ "    WHERE v.VisitCategory = 'Stop TB' AND v.Deleted = 0 "
			+ "      AND v.VisitDateTime >= ? AND v.VisitDateTime < ? "
			+ "  )";

	public int countAlreadyGenerated(LocalDate fromDate, LocalDate toDate) {
		String sql = "SELECT COUNT(*) FROM (" + BASE_SELECT + ") t WHERE t.existingNikshayId IS NOT NULL";
		Integer count = getJdbcTemplate().query(sql, pss(fromDate, toDate), rs -> rs.next() ? rs.getInt(1) : 0);
		return count == null ? 0 : count;
	}

	/** Counts beneficiaries skipped because their identity never synced to
	 * db_identity (no FirstName resolved at all) OR their location doesn't
	 * resolve through the Nikshay hierarchy — reported as one combined "not
	 * ready to export" count, since both are data-completeness gaps rather
	 * than a beneficiary genuinely not needing a Nikshay ID. */
	public int countNotReadyToExport(LocalDate fromDate, LocalDate toDate) {
		String sql = "SELECT COUNT(*) FROM (" + BASE_SELECT + ") t WHERE t.existingNikshayId IS NULL "
				+ "AND (t.firstName IS NULL "
				+ "OR t.village IS NULL OR t.healthFacility IS NULL OR t.tu IS NULL "
				+ "OR t.districtName IS NULL OR t.stateName IS NULL)";
		Integer count = getJdbcTemplate().query(sql, pss(fromDate, toDate), rs -> rs.next() ? rs.getInt(1) : 0);
		return count == null ? 0 : count;
	}

	/** Streams every not-yet-Nikshay-ID'd beneficiary in the date range to
	 * {@code rowConsumer} one row at a time, without materializing the full
	 * result set in memory — safe for large date ranges. Silently skips any
	 * beneficiary whose identity never synced or whose Nikshay village
	 * doesn't resolve all the way up to a state (see class Javadoc). */
	public void streamPendingBeneficiaries(LocalDate fromDate, LocalDate toDate, Consumer<NikshayRawRow> rowConsumer) {
		String sql = "SELECT * FROM (" + BASE_SELECT + ") t WHERE t.existingNikshayId IS NULL "
				+ "AND t.firstName IS NOT NULL "
				+ "AND t.village IS NOT NULL AND t.healthFacility IS NOT NULL AND t.tu IS NOT NULL "
				+ "AND t.districtName IS NOT NULL AND t.stateName IS NOT NULL";
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		// MySQL Connector/J-specific: Integer.MIN_VALUE forces true row-by-row
		// network streaming instead of buffering the whole result set client-side.
		jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
		jdbcTemplate.query(sql, pss(fromDate, toDate), (ResultSet rs) -> rowConsumer.accept(mapRow(rs)));
	}

	private PreparedStatementSetter pss(LocalDate fromDate, LocalDate toDate) {
		return (PreparedStatement ps) -> {
			ps.setTimestamp(1, Timestamp.valueOf(fromDate.atStartOfDay()));
			ps.setTimestamp(2, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
		};
	}

	private NikshayRawRow mapRow(ResultSet rs) throws SQLException {
		return new NikshayRawRow(
				rs.getObject("benRegId", Long.class),
				rs.getString("firstName"),
				rs.getString("middleLastName"),
				rs.getObject("age", Integer.class),
				rs.getString("gender"),
				rs.getString("phone"),
				rs.getString("address"),
				rs.getString("stateName"),
				rs.getString("districtName"),
				rs.getString("tu"),
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

	/** Finds beneficiaries matching a results-file row by content, since the
	 * real Nikshay ID Generator app's results CSV carries no beneficiary ID of
	 * any kind back — only phone/name/age survive the round trip. Matches on
	 * normalized 10-digit phone plus a case-insensitive first-name match;
	 * callers must treat anything other than exactly one result as ambiguous
	 * (e.g. a shared family phone number) rather than guessing. */
	public List<Long> findMatchingBeneficiaryIds(String phoneDigits, String firstName) {
		String sql = "SELECT DISTINCT m.BenRegId FROM db_identity.i_beneficiarymapping m "
				+ "JOIN db_identity.i_beneficiarydetails d ON d.BeneficiaryDetailsId = m.BenDetailsId AND d.Deleted = 0 "
				+ "JOIN db_identity.i_beneficiarycontacts c ON c.BenContactsId = m.BenContactsId "
				+ "WHERE m.Deleted = 0 AND c.PhoneNum1 = ? AND LOWER(TRIM(d.FirstName)) = LOWER(TRIM(?))";
		return getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getLong("BenRegId"), phoneDigits, firstName);
	}

	/** The most recent tb_suspected row for this beneficiary, if any — looked
	 * up live at import time (no export-time snapshot needed, since the
	 * beneficiary is identified directly from the results CSV's own benRegId
	 * column). Null if none exists yet. Note: tb_suspected has no `deleted`
	 * column, unlike most other AMRIT tables. */
	public Long findLatestSuspectedId(Long benRegId) {
		String sql = "SELECT id FROM tb_suspected WHERE benRegID = ? ORDER BY id DESC LIMIT 1";
		return getJdbcTemplate().query(sql, (ResultSet rs) -> rs.next() ? rs.getLong("id") : null, benRegId);
	}

	public void updateNikshayId(Long suspectedId, String nikshayId, String modifiedBy) {
		String sql = "UPDATE tb_suspected SET nikshay_id = ?, modified_by = ?, "
				+ "last_mod_date = CURRENT_TIMESTAMP WHERE id = ?";
		getJdbcTemplate().update(sql, nikshayId, modifiedBy, suspectedId);
	}

	/** Called when a beneficiary had no tb_suspected row yet — creates one to
	 * hold the Nikshay ID the portal generated. created_date is set explicitly
	 * because, unlike created_date on most other AMRIT tables, tb_suspected's
	 * has no DB-side default. */
	public Long insertSuspectedWithNikshayId(Long benRegId, LocalDate visitDate, String nikshayId,
			String createdBy) {
		String sql = "INSERT INTO tb_suspected (benRegID, visit_date, nikshay_id, created_by, created_date) "
				+ "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, benRegId);
			ps.setTimestamp(2, Timestamp.valueOf(visitDate.atStartOfDay()));
			ps.setString(3, nikshayId);
			ps.setString(4, createdBy);
			return ps;
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}
}
