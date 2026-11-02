package com.iemr.mmu.utils;

public final class NikshayConstants {

    private NikshayConstants() {
    }

    // Server that hosts the failedBeneficiaries API (it reads nikshay_beneficiary_phase_status)
    public static final String BASE_URL = "http://10.208.122.38:8080/mmu-api";

    public static final String NIKSHAY_PATH = "/nikshay";

    public static final String FAILED_BENEFICIARIES_PATH = "/syncStatus/failedBeneficiaries";

    public static final String FAILED_BENEFICIARIES_URL = BASE_URL + NIKSHAY_PATH + FAILED_BENEFICIARIES_PATH;
}
