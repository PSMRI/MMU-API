package com.iemr.mmu.controller.nikshay;

import com.google.gson.Gson;
import com.iemr.mmu.data.nikshay.NikshayMissingBeneficiaryDTO;
import com.iemr.mmu.data.nikshay.NikshaySyncStatusUpdateDTO;
import com.iemr.mmu.service.nikshay.NikshaySyncStatusService;
import com.iemr.mmu.utils.NikshayConstants;
import com.iemr.mmu.utils.response.OutputResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(NikshayConstants.NIKSHAY_PATH)
public class NikshaySyncStatusController {

    private static final int MAX_PAGE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(NikshaySyncStatusController.class);

    private final Gson gson = new Gson();

    @Autowired
    private NikshaySyncStatusService nikshaySyncStatusService;

    @PostMapping("/syncStatus/syncFailed")
    @Operation(summary = "Copy all FAILED rows from nikshay_beneficiary_phase_status into nikshay_beneficiary_sync_status")
    public String syncFailedPhaseStatuses(@RequestHeader(value = "Authorization") String authorization,
                                          @RequestHeader(value = "ServerAuthorization") String serverAuthorization) {
        OutputResponse response = new OutputResponse();
        try {
            Map<String, Object> result = nikshaySyncStatusService.syncFailedPhaseStatuses(serverAuthorization);
            response.setResponse(gson.toJson(result));
        } catch (Exception e) {
            logger.error("Error in syncFailedPhaseStatuses: " + e);
            response.setError(5000, "Error syncing failed Nikshay phase statuses: " + e.getMessage());
        }
        return response.toString();
    }

    @GetMapping("/syncStatus/missingBeneficiaries")
    @Operation(summary = "Get beneficiaries whose Ni-kshay sync failed, filtered by provider service map, village or both (paged)")
    public ResponseEntity<Map<String, Object>> getMissingBeneficiaries(
            @RequestParam(value = "providerServiceMapID", required = false) Integer providerServiceMapID,
            @RequestParam(value = "villageID", required = false) Integer villageID,
            @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "30") Integer pageSize) {
        logger.info("getMissingBeneficiaries request: providerServiceMapID={}, villageID={}, pageNo={}, pageSize={}",
                providerServiceMapID, villageID, pageNo, pageSize);
        try {
            if (providerServiceMapID == null && villageID == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "providerServiceMapID or villageID is required");
            }
            if (pageNo < 0) {
                return errorResponse(HttpStatus.BAD_REQUEST, "pageNo must be 0 or greater");
            }
            if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
                return errorResponse(HttpStatus.BAD_REQUEST, "pageSize must be between 1 and " + MAX_PAGE_SIZE);
            }

            Page<NikshayMissingBeneficiaryDTO> page = nikshaySyncStatusService.getMissingBeneficiaries(
                    providerServiceMapID, villageID, pageNo, pageSize);

            Map<String, Object> pagination = new LinkedHashMap<>();
            pagination.put("page", page.getNumber());
            pagination.put("size", page.getSize());
            pagination.put("totalElements", page.getTotalElements());
            pagination.put("totalPages", page.getTotalPages());
            pagination.put("hasNext", page.hasNext());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "SUCCESS");
            body.put("message", page.isEmpty()
                    ? "No missing beneficiary data found"
                    : "Missing beneficiary data fetched successfully");
            body.put("data", page.getContent());
            body.put("pagination", pagination);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            logger.error("Error in getMissingBeneficiaries for providerServiceMapID={}, villageID={}",
                    providerServiceMapID, villageID, e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching missing beneficiary data: " + e.getMessage());
        }
    }

    @GetMapping(NikshayConstants.FAILED_BENEFICIARIES_PATH)
    @Operation(summary = "Get every beneficiary with a FAILED phase in nikshay_beneficiary_phase_status, with all its phases (not paged)")
    public ResponseEntity<Map<String, Object>> getFailedBeneficiaries(@RequestHeader(value = "ServerAuthorization") String serverAuthorization) {
        try {
            return ResponseEntity.ok(nikshaySyncStatusService.getFailedBeneficiaries());
        } catch (Exception e) {
            logger.error("Error in getFailedBeneficiaries", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching failed Nikshay beneficiaries: " + e.getMessage());
        }
    }

    @PostMapping("/syncStatus/update")
    @Operation(summary = "Set isUpdated on all nikshay_beneficiary_sync_status rows of a beneficiary")
    public ResponseEntity<Map<String, Object>> updateSyncStatus(@RequestBody NikshaySyncStatusUpdateDTO request) {
        logger.info("updateSyncStatus request: beneficiaryId={}, isUpdated={}",
                request != null ? request.getBeneficiaryId() : null,
                request != null ? request.getIsUpdated() : null);
        try {
            if (request == null || request.getBeneficiaryId() == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "beneficiaryId is required");
            }
            if (request.getIsUpdated() == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "isUpdated is required");
            }

            int rows = nikshaySyncStatusService.updateIsUpdatedByBeneficiaryId(
                    request.getBeneficiaryId(), request.getIsUpdated());
            if (rows == 0) {
                return errorResponse(HttpStatus.NOT_FOUND,
                        "No sync status record found for beneficiaryId " + request.getBeneficiaryId());
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("beneficiaryId", request.getBeneficiaryId());
            data.put("isUpdated", request.getIsUpdated());
            data.put("recordsUpdated", rows);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "SUCCESS");
            body.put("message", "Update done for beneficiaryId " + request.getBeneficiaryId());
            body.put("data", data);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            logger.error("Error in updateSyncStatus for beneficiaryId={}",
                    request != null ? request.getBeneficiaryId() : null, e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating Nikshay sync status: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus httpStatus, String message) {
        if (httpStatus.is4xxClientError()) {
            logger.warn("Nikshay sync status request rejected ({}): {}", httpStatus.value(), message);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "FAILED");
        body.put("message", message);
        body.put("data", new ArrayList<>());
        return ResponseEntity.status(httpStatus).body(body);
    }
}
