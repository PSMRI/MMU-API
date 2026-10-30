package com.iemr.mmu.data.nikshay;

// One row of NikshayBeneficiarySyncStatusRepo.findMissingBeneficiaries; column aliases match the getters
public interface NikshayMissingBeneficiaryRow {

    Number getBenRegID();

    Number getBeneficiaryID();

    String getName();

    Number getVillageID();

    String getVillageName();
}
