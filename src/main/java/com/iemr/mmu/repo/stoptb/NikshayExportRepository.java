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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
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
 * The Nikshay ID itself lives on db_identity.i_beneficiarydetails.nikshayId —
 * a beneficiary-level identifier, not a clinical fact, so it's stored
 * alongside the beneficiary's identity (same table already holds rchid for
 * RMNCH), not on db_iemr.tb_suspected. Storing it on tb_suspected would mean
 * fabricating a bare "this beneficiary is a TB suspect" row for anyone who
 * never actually had a genuine suspected-case row — wrong, since every
 * Stop TB beneficiary needs a Nikshay ID regardless of suspect status.
 * Not tb_stoptb_diagnostics either, which also has its own nikshay_id column
 * but is a separate, manually-entered field this feature never touches.
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
			// PhoneNum1 is usually empty on real data — PreferredPhoneNum is the
			// one actually populated at registration; fall back through the rest.
			+ "  COALESCE(NULLIF(c.PreferredPhoneNum,''), NULLIF(c.PhoneNum1,''), NULLIF(c.PhoneNum2,'')) AS phone, "
			// CurrAddressValue is usually empty on real data too — build from the
			// actual line fields instead, same as CurrAddrLine1 etc. being populated.
			+ "  COALESCE(NULLIF(d.address,''), NULLIF(a.CurrAddressValue,''), "
			// CONCAT_WS skips NULLs but not empty strings, so each part needs its
			// own NULLIF first or blank line fields leave stray ", ," artifacts.
			+ "     NULLIF(TRIM(CONCAT_WS(', ', NULLIF(a.CurrAddrLine1,''), NULLIF(a.CurrAddrLine2,''), "
			+ "        NULLIF(a.CurrAddrLine3,''), NULLIF(a.CurrHabitation,''))),'')) AS address, "
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
			// hiv_status used to live on tb_stoptb_general_examination too, but that copy was
			// dropped (AMRIT-DB V109) once tb_screening became the single source of truth.
			+ "  (SELECT s.hiv_status FROM tb_screening s WHERE s.ben_reg_id = m.BenRegId "
			+ "     AND (s.deleted = 0 OR s.deleted IS NULL) ORDER BY s.id DESC LIMIT 1) AS hivStatus, "
			+ "  d.IsHIVPositive AS isHivPos, "
			+ "  d.nikshayId AS existingNikshayId "
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
		// Same column-order fix as the export's phone selection: PhoneNum1 is
		// usually empty on real data, PreferredPhoneNum is what's actually
		// populated (confirmed: two real beneficiaries with PhoneNum1 IS NULL
		// but PreferredPhoneNum populated failed to match here before this fix).
		String sql = "SELECT DISTINCT m.BenRegId FROM db_identity.i_beneficiarymapping m "
				+ "JOIN db_identity.i_beneficiarydetails d ON d.BeneficiaryDetailsId = m.BenDetailsId AND d.Deleted = 0 "
				+ "JOIN db_identity.i_beneficiarycontacts c ON c.BenContactsId = m.BenContactsId "
				+ "WHERE m.Deleted = 0 "
				+ "  AND ? IN (c.PreferredPhoneNum, c.PhoneNum1, c.PhoneNum2) "
				+ "  AND LOWER(TRIM(d.FirstName)) = LOWER(TRIM(?))";
		return getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getLong("BenRegId"), phoneDigits, firstName);
	}

	/** Writes the Nikshay ID onto the beneficiary's own identity record
	 * (db_identity.i_beneficiarydetails.nikshayId) instead of tb_suspected —
	 * see class Javadoc. Always an UPDATE, never an INSERT: every beneficiary
	 * this method is called for already has an i_beneficiarydetails row (the
	 * export only ever surfaces/matches beneficiaries whose identity has
	 * synced — see streamPendingBeneficiaries/findMatchingBeneficiaryIds,
	 * both of which require d.FirstName to be non-null), so there's no
	 * "beneficiary has no row yet" case to fabricate a row for.
	 *
	 * Resolved via i_beneficiarymapping.BenRegId -> BenDetailsId, not by
	 * trusting i_beneficiarydetails.BeneficiaryRegID directly — that column
	 * is a nullable denormalized copy, not the authoritative link, same as
	 * every other query in this class.
	 *
	 * Returns the number of rows updated. Callers should treat 0 as an error
	 * — it means no synced i_beneficiarydetails row was found for this
	 * benRegId, which shouldn't happen given the guarantee above, but is
	 * worth surfacing rather than silently dropping the ID on the floor. */
	public int writeNikshayId(Long benRegId, String nikshayId, boolean createdByAmrit, String modifiedBy) {
		String sql = "UPDATE db_identity.i_beneficiarydetails d "
				+ "JOIN db_identity.i_beneficiarymapping m ON m.BenDetailsId = d.BeneficiaryDetailsId "
				+ "SET d.nikshayId = ?, d.nikshayCreatedByAmrit = ?, d.ModifiedBy = ?, d.LastModDate = CURRENT_TIMESTAMP "
				+ "WHERE m.BenRegId = ? AND m.Deleted = 0 AND d.Deleted = 0";
		return getJdbcTemplate().update(sql, nikshayId, createdByAmrit, modifiedBy, benRegId);
	}
}
