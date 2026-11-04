package com.iemr.mmu.data.nikshay;

// One row of NikshayBeneficiarySyncStatusRepo.findBeneficiaryNames; column aliases match the getters
public interface BeneficiaryNameRow {

    Number getBenRegID();

    String getFirstName();

    String getLastName();
}
