package com.demo.workflow.services.impl;

import com.demo.workflow.services.AuditEntry;
import com.demo.workflow.services.AuditLogService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SPECIFICATION TESTS for AuditLogService
 *
 * These tests define the expected behavior. They are written BEFORE
 * any implementation exists. The AI agent will implement the service
 * to satisfy these tests.
 *
 * DO NOT MODIFY THESE TESTS DURING IMPLEMENTATION.
 *
 * Test Count: 12
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Specification")
class AuditLogServiceSpec {

    private AuditLogService service;

    @BeforeEach
    void setUp() {
        service = new AuditLogServiceImpl();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 1: Basic Logging (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When logging a security event")
    class SecurityEventLogging {

        @Test
        @DisplayName("should record event with timestamp")
        void shouldRecordEventWithTimestamp() {
            // Given
            Instant before = Instant.now();

            // When
            AuditEntry entry = service.logSecurityEvent(
                "MALWARE_DETECTED",
                "/content/dam/uploads/virus.exe",
                Map.of("threatName", "Trojan.Test")
            );

            // Then
            assertNotNull(entry);
            assertNotNull(entry.getTimestamp());
            assertTrue(entry.getTimestamp().isAfter(before) ||
                       entry.getTimestamp().equals(before));
        }

        @Test
        @DisplayName("should include event type and asset path")
        void shouldIncludeEventTypeAndPath() {
            AuditEntry entry = service.logSecurityEvent(
                "FILE_QUARANTINED",
                "/content/dam/quarantine/malware.exe",
                Map.of()
            );

            assertEquals("FILE_QUARANTINED", entry.getEventType());
            assertEquals("/content/dam/quarantine/malware.exe", entry.getAssetPath());
        }

        @Test
        @DisplayName("should store additional context data")
        void shouldStoreContextData() {
            Map<String, Object> context = new HashMap<>();
            context.put("threatName", "Trojan.Malware");
            context.put("scanDuration", 1500);
            context.put("quarantined", true);

            AuditEntry entry = service.logSecurityEvent(
                "SCAN_COMPLETE",
                "/content/dam/test.pdf",
                context
            );

            assertEquals("Trojan.Malware", entry.getContext().get("threatName"));
            assertEquals(1500, entry.getContext().get("scanDuration"));
            assertEquals(true, entry.getContext().get("quarantined"));
        }

        @Test
        @DisplayName("should generate unique entry ID")
        void shouldGenerateUniqueId() {
            AuditEntry entry1 = service.logSecurityEvent("EVENT_A", "/path/a", Map.of());
            AuditEntry entry2 = service.logSecurityEvent("EVENT_B", "/path/b", Map.of());

            assertNotNull(entry1.getId());
            assertNotNull(entry2.getId());
            assertNotEquals(entry1.getId(), entry2.getId());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 2: Query Capabilities (3 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When querying audit logs")
    class AuditLogQueries {

        @Test
        @DisplayName("should find entries by event type")
        void shouldFindByEventType() {
            // Create test entries
            service.logSecurityEvent("MALWARE_DETECTED", "/path/1", Map.of());
            service.logSecurityEvent("FILE_QUARANTINED", "/path/2", Map.of());
            service.logSecurityEvent("MALWARE_DETECTED", "/path/3", Map.of());

            // Query
            List<AuditEntry> results = service.findByEventType("MALWARE_DETECTED");

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(e ->
                "MALWARE_DETECTED".equals(e.getEventType())));
        }

        @Test
        @DisplayName("should find entries by asset path prefix")
        void shouldFindByPathPrefix() {
            service.logSecurityEvent("EVENT", "/content/dam/uploads/file1.pdf", Map.of());
            service.logSecurityEvent("EVENT", "/content/dam/uploads/file2.pdf", Map.of());
            service.logSecurityEvent("EVENT", "/content/dam/approved/file3.pdf", Map.of());

            List<AuditEntry> results = service.findByPathPrefix("/content/dam/uploads");

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("should find entries within time range")
        void shouldFindByTimeRange() {
            Instant start = Instant.now();

            service.logSecurityEvent("EVENT_1", "/path/1", Map.of());
            service.logSecurityEvent("EVENT_2", "/path/2", Map.of());

            Instant end = Instant.now().plusSeconds(1);

            List<AuditEntry> results = service.findByTimeRange(start, end);

            assertEquals(2, results.size());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 3: Retention & Cleanup (2 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When managing log retention")
    class LogRetention {

        @Test
        @DisplayName("should not throw when cleaning up entries")
        void shouldNotThrowOnCleanup() {
            // Add some entries
            service.logSecurityEvent("EVENT", "/path", Map.of());

            // Should not throw
            assertDoesNotThrow(() -> service.cleanupOldEntries());
        }

        @Test
        @DisplayName("should return count of deleted entries")
        void shouldReturnDeleteCount() {
            int deleted = service.cleanupOldEntries();

            assertTrue(deleted >= 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 4: Error Handling (3 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When handling errors")
    class ErrorHandling {

        @Test
        @DisplayName("should reject null event type")
        void shouldRejectNullEventType() {
            assertThrows(IllegalArgumentException.class, () ->
                service.logSecurityEvent(null, "/path", Map.of())
            );
        }

        @Test
        @DisplayName("should reject empty asset path")
        void shouldRejectEmptyPath() {
            assertThrows(IllegalArgumentException.class, () ->
                service.logSecurityEvent("EVENT", "", Map.of())
            );
        }

        @Test
        @DisplayName("should handle null context gracefully")
        void shouldHandleNullContext() {
            AuditEntry entry = service.logSecurityEvent("EVENT", "/path", null);

            assertNotNull(entry);
            assertNotNull(entry.getContext());
            assertTrue(entry.getContext().isEmpty());
        }
    }
}
