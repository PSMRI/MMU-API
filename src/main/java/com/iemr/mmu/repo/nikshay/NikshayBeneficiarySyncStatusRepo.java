package com.iemr.mmu.repo.nikshay;

import com.iemr.mmu.data.nikshay.BeneficiaryNameRow;
import com.iemr.mmu.data.nikshay.NikshayBeneficiarySyncStatus;
import com.iemr.mmu.data.nikshay.NikshayMissingBeneficiaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NikshayBeneficiarySyncStatusRepo extends JpaRepository<NikshayBeneficiarySyncStatus, Long> {

    Optional<NikshayBeneficiarySyncStatus> findByBenRegIDAndPhase(Long benRegID, String phase);


    List<NikshayBeneficiarySyncStatus> findByBenRegIDIn(Collection<Long> benRegIDs);

    // updated_at is refreshed by the column's ON UPDATE CURRENT_TIMESTAMP
    @Transactional
    @Modifying
    @Query("UPDATE NikshayBeneficiarySyncStatus s SET s.isUpdated = :isUpdated WHERE s.beneficiaryID = :beneficiaryID")
    int updateIsUpdatedByBeneficiaryID(@Param("beneficiaryID") Long beneficiaryID, @Param("isUpdated") Boolean isUpdated);


    // Purges rows already marked updated whose last change is older than the cutoff
    @Transactional
    @Modifying
    @Query("DELETE FROM NikshayBeneficiarySyncStatus s WHERE s.isUpdated = true " +
            "AND COALESCE(s.updatedAt, s.createdAt) < :cutoff")
    int deleteUpdatedOlderThan(@Param("cutoff") Timestamp cutoff);

    // Shared FROM/WHERE of findMissingBeneficiaries and its count query (annotation values must be constants).
    // Identity tables are LEFT JOINed so that, with no filter, every beneficiary in the sync-status table
    // is returned even when its identity data is missing on this server.
    //  - villageId: matched on the beneficiary's current address village (i_beneficiaryaddress.CurrVillageId)
    //  - psmId: matched on i_beneficiarydetails_rmnch.ProviderServiceMapID
    // When both are given, both must match.
    String MISSING_BENEFICIARIES_FROM =
            "FROM (SELECT DISTINCT ben_reg_id FROM db_iemr.nikshay_beneficiary_sync_status) s " +
            "LEFT JOIN db_identity.i_beneficiarymapping bm ON bm.BenRegId = s.ben_reg_id AND bm.Deleted = 0 " +
            "LEFT JOIN db_identity.i_beneficiarydetails d ON d.BeneficiaryDetailsId = bm.BenDetailsId " +
            "AND (d.Deleted IS NULL OR d.Deleted = 0) " +
            "LEFT JOIN db_identity.i_beneficiaryaddress a ON a.BenAddressID = bm.BenAddressId ";

    String MISSING_BENEFICIARIES_WHERE =
            "WHERE (:villageId IS NULL OR (d.BeneficiaryDetailsId IS NOT NULL " +
            "AND a.CurrVillageId = :villageId)) " +
            "AND (:psmId IS NULL OR EXISTS (SELECT 1 FROM db_identity.i_beneficiarydetails_rmnch r " +
            "WHERE r.BeneficiaryRegID = s.ben_reg_id AND r.ProviderServiceMapID = :psmId " +
            "AND (r.Deleted IS NULL OR r.Deleted = 0)))";

    // Beneficiaries that have a row in nikshay_beneficiary_sync_status, one entry per beneficiary even
    // with a row per phase. No filter returns all of them; a filter keeps only the matching ones.
    // Native SQL because the identity tables live in db_identity, outside this persistence unit.
    @Query(nativeQuery = true,
            value = "SELECT s.ben_reg_id AS benRegID, rg.BeneficiaryID AS beneficiaryID, " +
                    "NULLIF(TRIM(CONCAT_WS(' ', d.FirstName, d.LastName)), '') AS name, " +
                    "a.CurrVillageId AS villageID, " +
                    "a.CurrVillage AS villageName " +
                    MISSING_BENEFICIARIES_FROM +
                    "LEFT JOIN db_identity.m_beneficiaryregidmapping rg ON rg.BenRegId = s.ben_reg_id " +
                    MISSING_BENEFICIARIES_WHERE +
                    " ORDER BY s.ben_reg_id DESC",
            countQuery = "SELECT COUNT(*) " + MISSING_BENEFICIARIES_FROM + MISSING_BENEFICIARIES_WHERE)
    Page<NikshayMissingBeneficiaryRow> findMissingBeneficiaries(@Param("psmId") Integer psmId,
                                                                @Param("villageId") Integer villageId,
                                                                Pageable pageable);

    // Names of the given beneficiaries from i_beneficiarydetails, deleted records excluded.
    // Native projection of only the needed columns: i_beneficiarydetails is not mapped as an entity,
    // so a column added, renamed or retyped elsewhere in that table cannot break this lookup.
    @Query(nativeQuery = true,
            value = "SELECT d.BeneficiaryRegID AS benRegID, d.FirstName AS firstName, d.LastName AS lastName " +
                    "FROM db_identity.i_beneficiarydetails d " +
                    "WHERE d.BeneficiaryRegID IN (:benRegIDs) AND (d.Deleted IS NULL OR d.Deleted = 0)")
    List<BeneficiaryNameRow> findBeneficiaryNames(@Param("benRegIDs") Collection<Long> benRegIDs);
}
