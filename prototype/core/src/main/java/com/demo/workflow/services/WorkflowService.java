package com.demo.workflow.services;

/**
 * Service for managing asset approval workflows.
 * 
 * BMAD Phase 04 - Development
 */
public interface WorkflowService {

    /**
     * Workflow status.
     */
    enum WorkflowStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Result of workflow operation.
     */
    class WorkflowResult {
        private final String workflowId;
        private final WorkflowStatus status;
        private final String message;

        public WorkflowResult(String workflowId, WorkflowStatus status, String message) {
            this.workflowId = workflowId;
            this.status = status;
            this.message = message;
        }

        public String getWorkflowId() { return workflowId; }
        public WorkflowStatus getStatus() { return status; }
        public String getMessage() { return message; }
    }

    /**
     * Start approval workflow for an asset.
     * @param assetPath Path to the asset
     * @return WorkflowResult with workflow ID
     */
    WorkflowResult startApprovalWorkflow(String assetPath);

    /**
     * Approve an asset in workflow.
     * @param workflowId Workflow ID
     * @param approver Approver name
     * @param comment Optional comment
     * @return WorkflowResult
     */
    WorkflowResult approve(String workflowId, String approver, String comment);

    /**
     * Reject an asset in workflow.
     * @param workflowId Workflow ID
     * @param rejecter Rejecter name
     * @param reason Rejection reason
     * @return WorkflowResult
     */
    WorkflowResult reject(String workflowId, String rejecter, String reason);

    /**
     * Get workflow status.
     * @param workflowId Workflow ID
     * @return WorkflowStatus
     */
    WorkflowStatus getStatus(String workflowId);
}
