package com.iemr.mmu.repo.nikshay;


import com.iemr.mmu.data.nikshay.Beneficiarydetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Repository
public interface BeneficiaryRepo extends JpaRepository<Beneficiarydetail, Long> {

    @Query("SELECT d FROM Beneficiarydetail d WHERE d.BenRegId IN :benRegIDs")
    List<Beneficiarydetail> findDetailsByBenRegIDIn(@Param("benRegIDs") Collection<BigInteger> benRegIDs);

}
