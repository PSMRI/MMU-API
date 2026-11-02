package com.iemr.mmu.repo.nikshay;

import com.iemr.mmu.data.nikshay.NikshayBeneficiaryPhaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NikshayBeneficiaryPhaseStatusRepo extends JpaRepository<NikshayBeneficiaryPhaseStatus, Long> {

    List<NikshayBeneficiaryPhaseStatus> findByBenRegIDIn(Collection<Long> benRegIDs);

    // Every phase row (any status) of every beneficiary that has at least one phase in the given status,
    // newest beneficiary first
    @Query("SELECT p FROM NikshayBeneficiaryPhaseStatus p WHERE p.benRegID IN " +
            "(SELECT f.benRegID FROM NikshayBeneficiaryPhaseStatus f WHERE f.status = :status) " +
            "ORDER BY p.benRegID DESC")
    List<NikshayBeneficiaryPhaseStatus> findAllPhasesOfBeneficiariesWithStatus(@Param("status") String status);
}
