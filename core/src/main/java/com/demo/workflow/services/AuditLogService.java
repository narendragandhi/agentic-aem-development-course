package com.demo.workflow.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service for recording and querying security audit events.
 *
 * This service provides:
 * - Logging of security events with context
 * - Query capabilities by event type, path, and time range
 * - Automatic cleanup of old entries based on retention policy
 */
public interface AuditLogService {

    /**
     * Log a security event.
     *
     * @param eventType Type of event (e.g., MALWARE_DETECTED, FILE_QUARANTINED)
     * @param assetPath Path to the affected asset
     * @param context Additional context data (can be null)
     * @return The created audit entry
     * @throws IllegalArgumentException if eventType is null or assetPath is empty
     */
    AuditEntry logSecurityEvent(String eventType, String assetPath, Map<String, Object> context);

    /**
     * Find entries by event type.
     *
     * @param eventType The event type to search for
     * @return List of matching entries
     */
    List<AuditEntry> findByEventType(String eventType);

    /**
     * Find entries by asset path prefix.
     *
     * @param pathPrefix The path prefix to match
     * @return List of matching entries
     */
    List<AuditEntry> findByPathPrefix(String pathPrefix);

    /**
     * Find entries within a time range.
     *
     * @param start Start of time range (inclusive)
     * @param end End of time range (inclusive)
     * @return List of matching entries
     */
    List<AuditEntry> findByTimeRange(Instant start, Instant end);

    /**
     * Clean up entries older than retention period.
     *
     * @return Number of entries deleted
     */
    int cleanupOldEntries();
}
