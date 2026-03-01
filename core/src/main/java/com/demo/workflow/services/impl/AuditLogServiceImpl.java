package com.demo.workflow.services.impl;

import com.demo.workflow.services.AuditEntry;
import com.demo.workflow.services.AuditLogService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of AuditLogService for security event tracking.
 *
 * <p>Provides thread-safe in-memory storage for audit events with:</p>
 * <ul>
 *   <li>Unique ID generation via UUID</li>
 *   <li>Query by event type, path prefix, and time range</li>
 *   <li>Configurable retention-based cleanup</li>
 * </ul>
 *
 * <p>This service is designed for the Secure Asset Approval Workflow
 * to track security events like malware detection, quarantine actions,
 * and approval decisions.</p>
 *
 * @see AuditLogService
 * @see AuditEntry
 */
@Component(service = AuditLogService.class, immediate = true)
@Designate(ocd = AuditLogServiceImpl.Config.class)
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    /** Default retention period in days */
    private static final int DEFAULT_RETENTION_DAYS = 90;

    @ObjectClassDefinition(
        name = "Audit Log Service Configuration",
        description = "Configuration for the security audit log service"
    )
    public @interface Config {

        @AttributeDefinition(
            name = "Retention Days",
            description = "Number of days to retain audit entries before cleanup"
        )
        int retentionDays() default DEFAULT_RETENTION_DAYS;
    }

    /** Thread-safe storage for audit entries */
    private final Map<String, AuditEntry> entries = new ConcurrentHashMap<>();

    /** Configurable retention period */
    private int retentionDays = DEFAULT_RETENTION_DAYS;

    /**
     * Activates or updates the service configuration.
     *
     * @param config OSGi configuration
     */
    @Activate
    @Modified
    protected void activate(Config config) {
        if (config != null) {
            this.retentionDays = config.retentionDays();
        }
        LOG.info("AuditLogService activated - retention: {} days, entries: {}",
            retentionDays, entries.size());
    }

    @Override
    public AuditEntry logSecurityEvent(String eventType, String assetPath, Map<String, Object> context) {
        validateInputs(eventType, assetPath);

        AuditEntry entry = createEntry(eventType, assetPath, context);
        entries.put(entry.getId(), entry);

        LOG.info("Security event logged: type={}, path={}, id={}",
            eventType, assetPath, entry.getId());

        return entry;
    }

    /**
     * Validates input parameters for logging.
     */
    private void validateInputs(String eventType, String assetPath) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (assetPath == null || assetPath.isEmpty()) {
            throw new IllegalArgumentException("Asset path cannot be null or empty");
        }
    }

    /**
     * Creates a new audit entry with generated ID and timestamp.
     */
    private AuditEntry createEntry(String eventType, String assetPath, Map<String, Object> context) {
        AuditEntry entry = new AuditEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setEventType(eventType);
        entry.setAssetPath(assetPath);
        entry.setTimestamp(Instant.now());
        entry.setContext(context != null ? new HashMap<>(context) : new HashMap<>());
        return entry;
    }

    @Override
    public List<AuditEntry> findByEventType(String eventType) {
        LOG.debug("Querying entries by event type: {}", eventType);
        return entries.values().stream()
            .filter(e -> eventType.equals(e.getEventType()))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditEntry> findByPathPrefix(String pathPrefix) {
        LOG.debug("Querying entries by path prefix: {}", pathPrefix);
        return entries.values().stream()
            .filter(e -> hasPathPrefix(e, pathPrefix))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditEntry> findByTimeRange(Instant start, Instant end) {
        LOG.debug("Querying entries in time range: {} to {}", start, end);
        return entries.values().stream()
            .filter(e -> isWithinTimeRange(e, start, end))
            .collect(Collectors.toList());
    }

    @Override
    public int cleanupOldEntries() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        List<String> expiredIds = findExpiredEntryIds(cutoff);

        expiredIds.forEach(entries::remove);

        if (!expiredIds.isEmpty()) {
            LOG.info("Cleaned up {} expired audit entries (older than {} days)",
                expiredIds.size(), retentionDays);
        }

        return expiredIds.size();
    }

    /**
     * Checks if entry path starts with given prefix.
     */
    private boolean hasPathPrefix(AuditEntry entry, String pathPrefix) {
        return entry.getAssetPath() != null &&
               entry.getAssetPath().startsWith(pathPrefix);
    }

    /**
     * Checks if entry timestamp falls within the given range.
     */
    private boolean isWithinTimeRange(AuditEntry entry, Instant start, Instant end) {
        Instant ts = entry.getTimestamp();
        if (ts == null) {
            return false;
        }
        boolean afterStart = ts.equals(start) || ts.isAfter(start);
        boolean beforeEnd = ts.equals(end) || ts.isBefore(end);
        return afterStart && beforeEnd;
    }

    /**
     * Finds IDs of entries older than the cutoff date.
     */
    private List<String> findExpiredEntryIds(Instant cutoff) {
        return entries.entrySet().stream()
            .filter(e -> isExpired(e.getValue(), cutoff))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Checks if an entry is expired based on cutoff time.
     */
    private boolean isExpired(AuditEntry entry, Instant cutoff) {
        return entry.getTimestamp() != null &&
               entry.getTimestamp().isBefore(cutoff);
    }
}
