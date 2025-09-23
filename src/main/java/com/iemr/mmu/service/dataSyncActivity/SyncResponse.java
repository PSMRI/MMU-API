package com.iemr.mmu.service.dataSyncActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SyncResponse {
    private boolean overallSuccess;
    private String message;
    private List<String> successfulVanSerialNos;
    private List<String> failedVanSerialNos;
    private Map<String, String> recordDetails; // VanSerialNo -> error message
    
public SyncResponse() {
        this.successfulVanSerialNos = new ArrayList<>();
        this.failedVanSerialNos = new ArrayList<>();
        this.recordDetails = new HashMap<>();
    }
    
    // Getters and setters...
    public boolean isOverallSuccess() { return overallSuccess; }
    public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public List<String> getSuccessfulVanSerialNos() { return successfulVanSerialNos; }
    public void setSuccessfulVanSerialNos(List<String> successfulVanSerialNos) { this.successfulVanSerialNos = successfulVanSerialNos; }
    
    public List<String> getFailedVanSerialNos() { return failedVanSerialNos; }
    public void setFailedVanSerialNos(List<String> failedVanSerialNos) { this.failedVanSerialNos = failedVanSerialNos; }
    
    public Map<String, String> getRecordDetails() { return recordDetails; }
    public void setRecordDetails(Map<String, String> recordDetails) { this.recordDetails = recordDetails; }
}
