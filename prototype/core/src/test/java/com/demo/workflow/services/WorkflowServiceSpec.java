package com.demo.workflow.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Tests for WorkflowService.
 * 
 * BMAD Phase 05 - Testing
 */
@DisplayName("WorkflowService Specifications")
class WorkflowServiceSpec {

    private WorkflowService workflowService;
    private AntivirusScanService antivirusScanService;

    @BeforeEach
    void setUp() {
        antivirusScanService = new AntivirusScanServiceImpl();
        workflowService = new WorkflowServiceImpl(antivirusScanService);
    }

    @Nested
    @DisplayName("Start Workflow")
    class StartWorkflow {

        @Test
        @DisplayName("should start workflow for asset")
        void shouldStartWorkflow() {
            WorkflowService.WorkflowResult result = 
                workflowService.startApprovalWorkflow("/content/dam/test.pdf");

            assertNotNull(result.getWorkflowId());
            assertEquals(WorkflowService.WorkflowStatus.IN_PROGRESS, result.getStatus());
            assertTrue(result.getMessage().contains("started"));
        }

        @Test
        @DisplayName("should generate unique workflow IDs")
        void shouldGenerateUniqueIds() {
            WorkflowService.WorkflowResult result1 = 
                workflowService.startApprovalWorkflow("/content/dam/test1.pdf");
            WorkflowService.WorkflowResult result2 = 
                workflowService.startApprovalWorkflow("/content/dam/test2.pdf");

            assertNotEquals(result1.getWorkflowId(), result2.getWorkflowId());
        }
    }

    @Nested
    @DisplayName("Approve Workflow")
    class ApproveWorkflow {

        @Test
        @DisplayName("should approve workflow")
        void shouldApproveWorkflow() {
            WorkflowService.WorkflowResult start = 
                workflowService.startApprovalWorkflow("/content/dam/test.pdf");
            
            WorkflowService.WorkflowResult approve = 
                workflowService.approve(start.getWorkflowId(), "john", "Looks good");

            assertEquals(WorkflowService.WorkflowStatus.COMPLETED, approve.getStatus());
            assertTrue(approve.getMessage().contains("john"));
        }

        @Test
        @DisplayName("should fail to approve non-existent workflow")
        void shouldFailForNonExistent() {
            WorkflowService.WorkflowResult result = 
                workflowService.approve("WF-INVALID", "john", "comment");

            assertEquals(WorkflowService.WorkflowStatus.FAILED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Reject Workflow")
    class RejectWorkflow {

        @Test
        @DisplayName("should reject workflow")
        void shouldRejectWorkflow() {
            WorkflowService.WorkflowResult start = 
                workflowService.startApprovalWorkflow("/content/dam/test.pdf");
            
            WorkflowService.WorkflowResult reject = 
                workflowService.reject(start.getWorkflowId(), "jane", "Needs revision");

            assertEquals(WorkflowService.WorkflowStatus.CANCELLED, reject.getStatus());
            assertTrue(reject.getMessage().contains("jane"));
        }
    }

    @Nested
    @DisplayName("Get Status")
    class GetStatus {

        @Test
        @DisplayName("should get workflow status")
        void shouldGetStatus() {
            WorkflowService.WorkflowResult start = 
                workflowService.startApprovalWorkflow("/content/dam/test.pdf");
            
            WorkflowService.WorkflowStatus status = 
                workflowService.getStatus(start.getWorkflowId());

            assertEquals(WorkflowService.WorkflowStatus.IN_PROGRESS, status);
        }

        @Test
        @DisplayName("should return null for unknown workflow")
        void shouldReturnNullForUnknown() {
            WorkflowService.WorkflowStatus status = 
                workflowService.getStatus("WF-UNKNOWN");

            assertNull(status);
        }
    }
}
