package com.demo.workflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.demo.workflow.services.AuditLogService.Action;
import com.demo.workflow.services.AuditLogService.AuditEntry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

@DisplayName("AuditLogService")
class AuditLogServiceSpec {

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogServiceImpl();
    }

    @Nested
    @DisplayName("LogFunctionality")
    class LogFunctionality {

        @Test
        @DisplayName("should log asset upload")
        void shouldLogAssetUpload() {
            auditLogService.log("admin", Action.ASSET_UPLOAD, "/content/dam/image.png", Map.of("size", 1024));
            List<AuditEntry> entries = auditLogService.getEntriesByUser("admin");
            assertEquals(1, entries.size());
            assertEquals(Action.ASSET_UPLOAD, entries.get(0).getAction());
        }

        @Test
        @DisplayName("should log quarantine action")
        void shouldLogQuarantineAction() {
            auditLogService.log("system", Action.QUARANTINE, "/content/dam/malware.exe", Map.of("threat", "EICAR"));
            List<AuditEntry> entries = auditLogService.getEntriesByAction(Action.QUARANTINE);
            assertEquals(1, entries.size());
        }

        @Test
        @DisplayName("should log workflow approval")
        void shouldLogWorkflowApproval() {
            auditLogService.log("approver", Action.WORKFLOW_APPROVE, "/workflows/approval/123", Map.of());
            List<AuditEntry> entries = auditLogService.getEntriesByResource("/workflows");
            assertEquals(1, entries.size());
        }
    }

    @Nested
    @DisplayName("QueryFunctionality")
    class QueryFunctionality {

        @Test
        @DisplayName("should query by user")
        void shouldQueryByUser() {
            auditLogService.log("user1", Action.ASSET_UPLOAD, "/path1", Map.of());
            auditLogService.log("user2", Action.ASSET_UPLOAD, "/path2", Map.of());
            auditLogService.log("user1", Action.ASSET_DELETE, "/path3", Map.of());
            
            List<AuditEntry> user1Entries = auditLogService.getEntriesByUser("user1");
            assertEquals(2, user1Entries.size());
        }

        @Test
        @DisplayName("should query by action")
        void shouldQueryByAction() {
            auditLogService.log("user", Action.SCAN_COMPLETED, "/asset1", Map.of());
            auditLogService.log("user", Action.SECURITY_SCAN, "/asset2", Map.of());
            
            List<AuditEntry> scanEntries = auditLogService.getEntriesByAction(Action.SCAN_COMPLETED);
            assertEquals(1, scanEntries.size());
        }

        @Test
        @DisplayName("should return recent entries")
        void shouldReturnRecentEntries() {
            for (int i = 0; i < 5; i++) {
                auditLogService.log("user", Action.ASSET_UPLOAD, "/path" + i, Map.of());
            }
            
            List<AuditEntry> recent = auditLogService.getRecentEntries(3);
            assertEquals(3, recent.size());
        }

        @Test
        @DisplayName("should return empty for unknown user")
        void shouldReturnEmptyForUnknownUser() {
            List<AuditEntry> entries = auditLogService.getEntriesByUser("unknown");
            assertTrue(entries.isEmpty());
        }
    }

    @Nested
    @DisplayName("ServiceAvailability")
    class ServiceAvailability {

        @Test
        @DisplayName("should report available")
        void shouldReportAvailable() {
            assertTrue(auditLogService.isAvailable());
        }
    }
}
