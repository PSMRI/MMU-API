package com.iemr.mmu.repo.nikshay;


import com.iemr.mmu.data.nikshay.RMNCHBeneficiaryDetailsRmnch;
import com.iemr.mmu.data.nikshay.RMNCHMBeneficiarydetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepo extends JpaRepository<RMNCHBeneficiaryDetailsRmnch, Long> {

    Optional<RMNCHBeneficiaryDetailsRmnch> findById(Long benID);


    @Query("SELECT r FROM RMNCHBeneficiaryDetailsRmnch r WHERE r.BenRegId IN :benRegIDs")
    List<RMNCHBeneficiaryDetailsRmnch> findRmnchByBenRegIDIn(@Param("benRegIDs") Collection<Long> benRegIDs);

    @Query("SELECT d FROM RMNCHMBeneficiarydetail d WHERE d.BenRegId IN :benRegIDs")
    List<RMNCHMBeneficiarydetail> findDetailsByBenRegIDIn(@Param("benRegIDs") Collection<BigInteger> benRegIDs);

}
