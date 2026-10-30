package com.iemr.mmu.repo.nikshay;

import com.iemr.mmu.data.nikshay.NikshayBeneficiaryPhaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NikshayBeneficiaryPhaseStatusRepo extends JpaRepository<NikshayBeneficiaryPhaseStatus, Long> {

    Optional<NikshayBeneficiaryPhaseStatus> findByBenRegIDAndPhase(Long benRegID, String phase);

    List<NikshayBeneficiaryPhaseStatus> findByBenRegID(Long benRegID);

    List<NikshayBeneficiaryPhaseStatus> findByStatus(String status);

    List<NikshayBeneficiaryPhaseStatus> findByPhaseAndStatus(String phase, String status);

    List<NikshayBeneficiaryPhaseStatus> findByStatusAndFailureReason(String status, String failureReason);

    List<NikshayBeneficiaryPhaseStatus> findByIndexBenRegID(Long indexBenRegID);

    List<NikshayBeneficiaryPhaseStatus> findByBenRegIDIn(Collection<Long> benRegIDs);

    // One entry per beneficiary that has at least one phase in the given status
    @Query("SELECT DISTINCT p.benRegID FROM NikshayBeneficiaryPhaseStatus p WHERE p.status = :status " +
            "ORDER BY p.benRegID DESC")
    List<Long> findDistinctBenRegIDsByStatus(@Param("status") String status);
}
