package com.iemr.mmu.repo.nikshay;

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

    // Shared parts of findMissingBeneficiaries and its count query (annotation values must be constants)
    String MISSING_BENEFICIARIES_FROM =
            "FROM (SELECT DISTINCT ben_reg_id FROM db_iemr.nikshay_beneficiary_sync_status) s " +
            "JOIN db_identity.i_beneficiarymapping bm ON bm.BenRegId = s.ben_reg_id AND bm.Deleted = 0 " +
            "LEFT JOIN db_identity.m_beneficiaryregidmapping rg ON rg.BenRegId = bm.BenRegId " +
            "LEFT JOIN db_identity.i_beneficiarydetails d ON d.BeneficiaryDetailsId = bm.BenDetailsId " +
            "AND (d.Deleted IS NULL OR d.Deleted = 0) " +
            "LEFT JOIN db_identity.i_beneficiaryaddress a ON a.BenAddressID = bm.BenAddressId " +
            "WHERE (:psmId IS NULL " +
            "OR EXISTS (SELECT 1 FROM db_identity.i_beneficiarydetails_rmnch r " +
            "WHERE r.BeneficiaryRegID = bm.BenRegId AND r.ProviderServiceMapID = :psmId " +
            "AND (r.Deleted IS NULL OR r.Deleted = 0)) " +
            "OR EXISTS (SELECT 1 FROM db_identity.i_beneficiaryservicemapping sm " +
            "WHERE sm.BenMapID = bm.BenMapId AND sm.ProviderServiceMapId = :psmId AND sm.Deleted = 0))";

    // the RMNCH household's village, else the beneficiary's own address
    String MISSING_BENEFICIARIES_VILLAGE_ID =
            "COALESCE((SELECT h.villageid FROM db_identity.i_beneficiarydetails_rmnch r " +
            "JOIN db_identity.i_householddetails h ON h.houseoldId = r.houseoldId " +
            "AND (h.Deleted IS NULL OR h.Deleted = 0) " +
            "WHERE r.BeneficiaryRegID = bm.BenRegId AND (r.Deleted IS NULL OR r.Deleted = 0) LIMIT 1)" +
            ", a.CurrVillageId, a.PermVillageId)";

    String MISSING_BENEFICIARIES_VILLAGE_NAME =
            "COALESCE((SELECT h.village FROM db_identity.i_beneficiarydetails_rmnch r " +
            "JOIN db_identity.i_householddetails h ON h.houseoldId = r.houseoldId " +
            "AND (h.Deleted IS NULL OR h.Deleted = 0) " +
            "WHERE r.BeneficiaryRegID = bm.BenRegId AND (r.Deleted IS NULL OR r.Deleted = 0) LIMIT 1)" +
            ", a.CurrVillage, a.PermVillage)";

    // Beneficiaries that have a row in nikshay_beneficiary_sync_status, filtered by provider service map
    // and/or village (a null filter is ignored), one entry per beneficiary even with a row per phase.
    // Same AMRIT identity rules as DataSyncRepository.createMissingWorklistRows:
    //  - beneficiary: i_beneficiarymapping (BenRegId) -> i_beneficiarydetails (name),
    //    m_beneficiaryregidmapping (BeneficiaryID)
    //  - village: the RMNCH household's village, else the beneficiary's own address (Curr, then Perm)
    //  - provider service map: the RMNCH record's, or any i_beneficiaryservicemapping row
    //    (registrar-flow beneficiaries have no RMNCH/household record)
    // Driven from the sync-status table, which is far smaller than the identity tables.
    // Native SQL because the identity tables live in db_identity, outside this persistence unit.
    @Query(nativeQuery = true,
            value = "SELECT x.benRegID, x.beneficiaryID, x.name, x.villageID, x.villageName FROM (" +
                    "SELECT bm.BenRegId AS benRegID, rg.BeneficiaryID AS beneficiaryID, " +
                    "NULLIF(TRIM(CONCAT_WS(' ', d.FirstName, d.LastName)), '') AS name, " +
                    MISSING_BENEFICIARIES_VILLAGE_ID + " AS villageID, " +
                    MISSING_BENEFICIARIES_VILLAGE_NAME + " AS villageName " +
                    MISSING_BENEFICIARIES_FROM + ") x " +
                    "WHERE (:villageId IS NULL OR x.villageID = :villageId) " +
                    "ORDER BY x.benRegID DESC",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT " + MISSING_BENEFICIARIES_VILLAGE_ID + " AS villageID " +
                    MISSING_BENEFICIARIES_FROM + ") x " +
                    "WHERE (:villageId IS NULL OR x.villageID = :villageId)")
    Page<NikshayMissingBeneficiaryRow> findMissingBeneficiaries(@Param("psmId") Integer psmId,
                                                                @Param("villageId") Integer villageId,
                                                                Pageable pageable);
}
