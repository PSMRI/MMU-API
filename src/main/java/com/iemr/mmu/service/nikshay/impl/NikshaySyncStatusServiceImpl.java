package com.iemr.mmu.service.nikshay.impl;


import com.iemr.mmu.data.nikshay.NikshayBeneficiaryPhaseStatus;
import com.iemr.mmu.data.nikshay.NikshayBeneficiarySyncStatus;
import com.iemr.mmu.data.nikshay.NikshayMissingBeneficiaryDTO;
import com.iemr.mmu.data.nikshay.NikshayMissingBeneficiaryRow;
import com.iemr.mmu.data.nikshay.RMNCHMBeneficiarydetail;
import com.iemr.mmu.repo.nikshay.BeneficiaryRepo;
import com.iemr.mmu.repo.nikshay.NikshayBeneficiaryPhaseStatusRepo;
import com.iemr.mmu.repo.nikshay.NikshayBeneficiarySyncStatusRepo;
import com.iemr.mmu.service.nikshay.NikshaySyncStatusService;
import com.iemr.mmu.utils.NikshayConstants;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NikshaySyncStatusServiceImpl implements NikshaySyncStatusService {

    private static final String STATUS_FAILED = "FAILED";

    private static final String STATUS_NOT_STARTED = "NOT_STARTED";

    private static final String PHASE_ENROLMENT = "ENROLMENT";

    // Every beneficiary is reported with all phases, in this order
    private static final List<String> PHASES =
            List.of(PHASE_ENROLMENT, "NAAT_UPLOAD", "XRAY_UPLOAD", "CONTACTS", "TRANSFER_OUT");

    private final Logger logger = LoggerFactory.getLogger(NikshaySyncStatusServiceImpl.class);

    @Autowired
    private NikshayBeneficiaryPhaseStatusRepo phaseStatusRepo;

    @Autowired
    private NikshayBeneficiarySyncStatusRepo syncStatusRepo;

    @Autowired
    private BeneficiaryRepo beneficiaryRepo;

    @Value("${nikshay.http.connectTimeoutMs:15000}")
    private int connectTimeoutMs;

    // the failedBeneficiaries API is not paged, so allow it time on a large backlog
    @Value("${nikshay.http.readTimeoutMs:120000}")
    private int readTimeoutMs;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        restTemplate = new RestTemplate(factory);
    }

    // Fetches the FAILED phases from the failedBeneficiaries API (deployed on another server), then purges
    // rows already marked updated more than a month ago and stores one row per FAILED phase.
    // (ben_reg_id, phase) is unique, so a surviving row is refreshed in place and keeps its isUpdated
    // flag; a new row starts with isUpdated = false.
    @Override
    @Transactional
    public Map<String, Object> syncFailedPhaseStatuses(String serverAuthorization) throws Exception {
        // fetched before touching the table, so an unreachable server leaves it as it was
        List<NikshayBeneficiarySyncStatus> failed = fetchFailedPhases(serverAuthorization);

        Timestamp cutoff = Timestamp.valueOf(LocalDateTime.now().minusMonths(1));
        int deleted = syncStatusRepo.deleteUpdatedOlderThan(cutoff);
        logger.info("Nikshay sync: deleted {} rows with isUpdated = true last changed before {}", deleted, cutoff);

        int inserted = 0;
        int updated = 0;
        List<NikshayBeneficiarySyncStatus> toSave = new ArrayList<>();
        for (NikshayBeneficiarySyncStatus remote : failed) {
            NikshayBeneficiarySyncStatus sync = syncStatusRepo
                    .findByBenRegIDAndPhase(remote.getBenRegID(), remote.getPhase())
                    .orElse(null);
            if (sync == null) {
                sync = new NikshayBeneficiarySyncStatus();
                inserted++;
            } else {
                updated++;
            }
            sync.setBenRegID(remote.getBenRegID());
            sync.setBeneficiaryID(remote.getBeneficiaryID());
            sync.setBeneficiaryType(remote.getBeneficiaryType());
            sync.setIndexBenRegID(remote.getIndexBenRegID());
            sync.setPhase(remote.getPhase());
            sync.setStatus(remote.getStatus());
            sync.setFailureReason(remote.getFailureReason());
            sync.setErrorDetail(remote.getErrorDetail());
            sync.setNikshayId(remote.getNikshayId());
            sync.setEpisodeId(remote.getEpisodeId());
            sync.setLastAttemptAt(remote.getLastAttemptAt());
            toSave.add(sync);
        }
        syncStatusRepo.saveAll(toSave);

        logger.info("Nikshay sync: {} FAILED phase rows received, {} inserted, {} updated",
                failed.size(), inserted, updated);

        Map<String, Object> result = new HashMap<>();
        result.put("failedRecordsFound", failed.size());
        result.put("deleted", deleted);
        result.put("inserted", inserted);
        result.put("updated", updated);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NikshayMissingBeneficiaryDTO> getMissingBeneficiaries(Integer providerServiceMapID, Integer villageID,
                                                                      int pageNo, int pageSize) {
        // beneficiary, name and village come from the AMRIT identity tables (see the repo query)
        Page<NikshayMissingBeneficiaryRow> benPage = syncStatusRepo.findMissingBeneficiaries(
                providerServiceMapID, villageID, PageRequest.of(pageNo, pageSize));
        List<Long> benRegIDs = benPage.getContent().stream()
                .map(row -> row.getBenRegID().longValue())
                .collect(Collectors.toList());
        logger.info("Nikshay missing beneficiaries: psmId={}, villageId={}, page={}, size={} -> {} on page, {} total",
                providerServiceMapID, villageID, pageNo, pageSize, benRegIDs.size(), benPage.getTotalElements());
        if (benRegIDs.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), benPage.getPageable(), benPage.getTotalElements());
        }

        Map<Long, List<NikshayBeneficiarySyncStatus>> syncRowsByBen = new HashMap<>();
        for (NikshayBeneficiarySyncStatus row : syncStatusRepo.findByBenRegIDIn(benRegIDs)) {
            syncRowsByBen.computeIfAbsent(row.getBenRegID(), k -> new ArrayList<>()).add(row);
        }

        List<NikshayMissingBeneficiaryDTO> data = new ArrayList<>();
        for (NikshayMissingBeneficiaryRow ben : benPage.getContent()) {
            Long benRegID = ben.getBenRegID().longValue();
            List<NikshayBeneficiarySyncStatus> rows = syncRowsByBen.getOrDefault(benRegID, new ArrayList<>());

            Long beneficiaryID = null;
            boolean isUpdated = !rows.isEmpty();
            Set<String> reasons = new LinkedHashSet<>();
            for (NikshayBeneficiarySyncStatus row : rows) {
                if (beneficiaryID == null) beneficiaryID = row.getBeneficiaryID();
                // a beneficiary counts as updated only once every failed phase has been updated
                if (!Boolean.TRUE.equals(row.getIsUpdated())) isUpdated = false;
                reasons.add(row.getFailureReason() != null
                        ? row.getFailureReason()
                        : row.getPhase() + " sync failed");
            }
            if (beneficiaryID == null && ben.getBeneficiaryID() != null) {
                beneficiaryID = ben.getBeneficiaryID().longValue();
            }

            data.add(new NikshayMissingBeneficiaryDTO(beneficiaryID, isUpdated, new ArrayList<>(reasons),
                    ben.getName(),
                    ben.getVillageID() != null ? ben.getVillageID().intValue() : null,
                    ben.getVillageName()));
        }
        return new PageImpl<>(data, benPage.getPageable(), benPage.getTotalElements());
    }

    // Every beneficiary with at least one FAILED phase, with only its FAILED phases. Not paged.
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFailedBeneficiaries() {
        List<Long> benRegIDs = phaseStatusRepo.findDistinctBenRegIDsByStatus(STATUS_FAILED);
        logger.info("Nikshay failed beneficiaries: {} beneficiaries with a FAILED phase", benRegIDs.size());

        List<Map<String, Object>> beneficiaries = new ArrayList<>();
        if (!benRegIDs.isEmpty()) {
            Map<Long, Map<String, NikshayBeneficiaryPhaseStatus>> phasesByBen = new HashMap<>();
            Set<Long> nameBenRegIDs = new LinkedHashSet<>(benRegIDs);
            for (NikshayBeneficiaryPhaseStatus row : phaseStatusRepo.findByBenRegIDIn(benRegIDs)) {
                phasesByBen.computeIfAbsent(row.getBenRegID(), k -> new HashMap<>()).put(row.getPhase(), row);
                if (row.getIndexBenRegID() != null) nameBenRegIDs.add(row.getIndexBenRegID());
            }

            // Names of the beneficiaries and of their index patients, from i_beneficiarydetails
            List<BigInteger> nameBenRegIDsBig = nameBenRegIDs.stream()
                    .map(BigInteger::valueOf)
                    .collect(Collectors.toList());
            Map<Long, String> nameByBen = new HashMap<>();
            for (RMNCHMBeneficiarydetail detail : beneficiaryRepo.findDetailsByBenRegIDIn(nameBenRegIDsBig)) {
                if (detail.getBenRegId() == null || Boolean.TRUE.equals(detail.getDeleted())) continue;
                nameByBen.putIfAbsent(detail.getBenRegId().longValue(), buildName(detail));
            }

            for (Long benRegID : benRegIDs) {
                Map<String, NikshayBeneficiaryPhaseStatus> rows = phasesByBen.getOrDefault(benRegID, new HashMap<>());
                // beneficiary-level fields are the same on every phase row; prefer the ENROLMENT row
                NikshayBeneficiaryPhaseStatus first = rows.containsKey(PHASE_ENROLMENT)
                        ? rows.get(PHASE_ENROLMENT)
                        : rows.values().stream().findFirst().orElse(null);
                Long indexBenRegID = first != null ? first.getIndexBenRegID() : null;
                NikshayBeneficiaryPhaseStatus enrolment = rows.get(PHASE_ENROLMENT);

                Map<String, Object> phases = new LinkedHashMap<>();
                for (String phase : PHASES) {
                    NikshayBeneficiaryPhaseStatus row = rows.get(phase);
                    if (row != null && STATUS_FAILED.equals(row.getStatus())) {
                        phases.put(phase, toPhaseMap(row));
                    }
                }

                Map<String, Object> ben = new LinkedHashMap<>();
                ben.put("benRegId", benRegID);
                ben.put("beneficiaryId", first != null ? first.getBeneficiaryID() : null);
                ben.put("name", nameByBen.get(benRegID));
                ben.put("beneficiaryType", first != null ? first.getBeneficiaryType() : null);
                ben.put("indexBenRegId", indexBenRegID);
                ben.put("indexName", indexBenRegID != null ? nameByBen.get(indexBenRegID) : null);
                ben.put("nikshayId", enrolment != null ? enrolment.getNikshayId() : null);
                ben.put("phases", phases);
                beneficiaries.add(ben);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", beneficiaries.size());
        result.put("beneficiaries", beneficiaries);
        return result;
    }

    // Updates every phase row of the beneficiary. Rows synced before beneficiary_id could be resolved
    // hold it as null, so when nothing matches on beneficiary_id, fall back to the BenRegId.
    @Override
    public int updateIsUpdatedByBeneficiaryId(Long beneficiaryId, Boolean isUpdated) {
        int rows = syncStatusRepo.updateIsUpdatedByBeneficiaryID(beneficiaryId, isUpdated);
//        if (rows == 0) {
//            Long benRegID = beneficiaryRepo.getBenRegIdFromBenId(beneficiaryId);
//            if (benRegID != null) {
//                logger.info("Nikshay sync status: no rows on beneficiaryId={}, retrying with benRegID={}",
//                        beneficiaryId, benRegID);
//                rows = syncStatusRepo.updateIsUpdatedByBenRegID(benRegID, isUpdated);
//            }
//        }
        logger.info("Nikshay sync status: set isUpdated={} for beneficiaryId={} on {} rows",
                isUpdated, beneficiaryId, rows);
        return rows;
    }

    private Map<String, Object> toPhaseMap(NikshayBeneficiaryPhaseStatus row) {
        Map<String, Object> phase = new LinkedHashMap<>();
        phase.put("status", row != null ? row.getStatus() : STATUS_NOT_STARTED);
        phase.put("failureReason", row != null ? row.getFailureReason() : null);
        phase.put("errorDetail", row != null ? row.getErrorDetail() : null);
        phase.put("attempts", row != null && row.getAttempts() != null ? row.getAttempts() : 0);
        phase.put("nikshayId", row != null ? row.getNikshayId() : null);
        phase.put("episodeId", row != null ? row.getEpisodeId() : null);
        phase.put("lastAttemptAt", row != null ? row.getLastAttemptAt() : null);
        phase.put("completedAt", row != null ? row.getCompletedAt() : null);
        return phase;
    }

    // Calls the failedBeneficiaries API and flattens its response to one row per FAILED phase
    private List<NikshayBeneficiarySyncStatus> fetchFailedPhases(String serverAuthorization) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // the JWT filter on the other server lets a java/ client through on its Authorization header;
        // the controller itself requires ServerAuthorization
        headers.set(HttpHeaders.AUTHORIZATION, serverAuthorization);
        headers.set("ServerAuthorization", serverAuthorization);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(NikshayConstants.FAILED_BENEFICIARIES_URL, HttpMethod.GET, new HttpEntity<>(headers),
                    String.class);
        } catch (HttpStatusCodeException e) {
            throw new Exception("failedBeneficiaries API returned " + e.getStatusCode().value() + ": "
                    + e.getResponseBodyAsString());
        }
        if (!response.hasBody())
            throw new Exception("Empty response from failedBeneficiaries API");

        JSONObject body = new JSONObject(response.getBody());
        if (!body.has("beneficiaries"))
            // a rejected session comes back from HttpInterceptor as HTTP 200 with an OutputResponse body
            throw new Exception("failedBeneficiaries API failed: "
                    + body.optString("errorMessage", body.optString("message")));

        List<NikshayBeneficiarySyncStatus> rows = new ArrayList<>();
        JSONArray beneficiaries = body.getJSONArray("beneficiaries");
        for (int i = 0; i < beneficiaries.length(); i++) {
            JSONObject ben = beneficiaries.getJSONObject(i);
            JSONObject phases = ben.optJSONObject("phases");
            if (phases == null) continue;
            for (String phaseName : phases.keySet()) {
                JSONObject phase = phases.getJSONObject(phaseName);
                if (!STATUS_FAILED.equals(phase.optString("status"))) continue;

                NikshayBeneficiarySyncStatus row = new NikshayBeneficiarySyncStatus();
                row.setBenRegID(ben.getLong("benRegId"));
                row.setBeneficiaryID(optLong(ben, "beneficiaryId"));
                row.setBeneficiaryType(ben.optString("beneficiaryType", null));
                row.setIndexBenRegID(optLong(ben, "indexBenRegId"));
                row.setPhase(phaseName);
                row.setStatus(STATUS_FAILED);
                row.setFailureReason(optString(phase, "failureReason"));
                row.setErrorDetail(optString(phase, "errorDetail"));
                // the phase's own nikshayId, else the beneficiary's (from its ENROLMENT phase)
                String nikshayId = optString(phase, "nikshayId");
                row.setNikshayId(nikshayId != null ? nikshayId : optString(ben, "nikshayId"));
                row.setEpisodeId(optString(phase, "episodeId"));
                row.setLastAttemptAt(optTimestamp(phase, "lastAttemptAt"));
                rows.add(row);
            }
        }
        logger.info("Nikshay sync: failedBeneficiaries API returned {} beneficiaries, {} FAILED phases",
                beneficiaries.length(), rows.size());
        return rows;
    }

    private static String optString(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.optString(key, null);
    }

    private static Long optLong(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getLong(key);
    }

    // Spring serialises a Timestamp as an ISO string or as epoch millis, depending on its Jackson settings
    private static Timestamp optTimestamp(JSONObject obj, String key) {
        if (obj.isNull(key)) return null;
        Object value = obj.get(key);
        if (value instanceof Number) return new Timestamp(((Number) value).longValue());
        return Timestamp.from(OffsetDateTime.parse(value.toString()).toInstant());
    }

    private String buildName(RMNCHMBeneficiarydetail detail) {
        String name = ((detail.getFirstName() != null ? detail.getFirstName() : "") + " "
                + (detail.getLastName() != null ? detail.getLastName() : "")).trim();
        return name.isEmpty() ? null : name;
    }
}
