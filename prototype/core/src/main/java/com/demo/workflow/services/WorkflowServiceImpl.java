package com.demo.workflow.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of WorkflowService.
 * 
 * Simulates workflow management for asset approval.
 */
public class WorkflowServiceImpl implements WorkflowService {

    private final Map<String, WorkflowRecord> workflows = new ConcurrentHashMap<>();
    private final AntivirusScanService antivirusScanService;

    public WorkflowServiceImpl(AntivirusScanService antivirusScanService) {
        this.antivirusScanService = antivirusScanService;
    }

    @Override
    public WorkflowResult startApprovalWorkflow(String assetPath) {
        String workflowId = generateWorkflowId();
        
        // Scan first
        AntivirusScanService.ScanResult scanResult;
        try {
            scanResult = antivirusScanService.scan(null, assetPath);
        } catch (Exception e) {
            return new WorkflowResult(null, WorkflowStatus.FAILED, "Scan failed: " + e.getMessage());
        }

        // Create workflow record
        WorkflowRecord record = new WorkflowRecord();
        record.workflowId = workflowId;
        record.assetPath = assetPath;
        record.scanStatus = scanResult.getStatus();
        record.status = WorkflowStatus.IN_PROGRESS;
        record.createdAt = System.currentTimeMillis();
        
        workflows.put(workflowId, record);
        
        return new WorkflowResult(workflowId, WorkflowStatus.IN_PROGRESS, 
            "Workflow started, scan: " + scanResult.getStatus());
    }

    @Override
    public WorkflowResult approve(String workflowId, String approver, String comment) {
        WorkflowRecord record = workflows.get(workflowId);
        if (record == null) {
            return new WorkflowResult(null, WorkflowStatus.FAILED, "Workflow not found");
        }
        
        if (record.status != WorkflowStatus.IN_PROGRESS) {
            return new WorkflowResult(null, WorkflowStatus.FAILED, "Workflow not in progress");
        }
        
        record.status = WorkflowStatus.COMPLETED;
        record.approver = approver;
        record.comment = comment;
        record.completedAt = System.currentTimeMillis();
        
        return new WorkflowResult(workflowId, WorkflowStatus.COMPLETED, "Approved by " + approver);
    }

    @Override
    public WorkflowResult reject(String workflowId, String rejecter, String reason) {
        WorkflowRecord record = workflows.get(workflowId);
        if (record == null) {
            return new WorkflowResult(null, WorkflowStatus.FAILED, "Workflow not found");
        }
        
        record.status = WorkflowStatus.CANCELLED;
        record.approver = rejecter;
        record.comment = reason;
        record.completedAt = System.currentTimeMillis();
        
        return new WorkflowResult(workflowId, WorkflowStatus.CANCELLED, "Rejected by " + rejecter);
    }

    @Override
    public WorkflowStatus getStatus(String workflowId) {
        WorkflowRecord record = workflows.get(workflowId);
        return record != null ? record.status : null;
    }

    private String generateWorkflowId() {
        return "WF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static class WorkflowRecord {
        String workflowId;
        String assetPath;
        AntivirusScanService.ScanStatus scanStatus;
        WorkflowStatus status;
        String approver;
        String comment;
        long createdAt;
        long completedAt;
    }
}
