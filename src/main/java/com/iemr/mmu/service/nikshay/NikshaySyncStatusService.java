package com.iemr.mmu.service.nikshay;

import com.iemr.mmu.data.nikshay.NikshayMissingBeneficiaryDTO;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface NikshaySyncStatusService {

    Map<String, Object> syncFailedPhaseStatuses(String serverAuthorization) throws Exception;

    Page<NikshayMissingBeneficiaryDTO> getMissingBeneficiaries(Integer providerServiceMapID, Integer villageID,
                                                               int pageNo, int pageSize);

    Map<String, Object> getFailedBeneficiaries();

    int updateIsUpdatedByBeneficiaryId(Long beneficiaryId, Boolean isUpdated);
}
