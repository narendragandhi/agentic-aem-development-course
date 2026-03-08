package com.demo.workflow.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    enum Action {
        ASSET_UPLOAD, ASSET_DELETE, ASSET_MOVE,
        SCAN_COMPLETED, QUARANTINE, RELEASE,
        WORKFLOW_START, WORKFLOW_APPROVE, WORKFLOW_REJECT,
        SECURITY_SCAN, NOTIFICATION_SENT
    }

    class AuditEntry {
        private final String entryId;
        private final String userId;
        private final Action action;
        private final String resourcePath;
        private final Map<String, Object> metadata;
        private final Instant timestamp;
        private final String ipAddress;

        public AuditEntry(String entryId, String userId, Action action, 
                String resourcePath, Map<String, Object> metadata, 
                Instant timestamp, String ipAddress) {
            this.entryId = entryId;
            this.userId = userId;
            this.action = action;
            this.resourcePath = resourcePath;
            this.metadata = metadata;
            this.timestamp = timestamp;
            this.ipAddress = ipAddress;
        }

        public String getEntryId() { return entryId; }
        public String getUserId() { return userId; }
        public Action getAction() { return action; }
        public String getResourcePath() { return resourcePath; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getTimestamp() { return timestamp; }
        public String getIpAddress() { return ipAddress; }
    }

    void log(String userId, Action action, String resourcePath, Map<String, Object> metadata);

    List<AuditEntry> getEntriesByUser(String userId);

    List<AuditEntry> getEntriesByResource(String resourcePath);

    List<AuditEntry> getEntriesByAction(Action action);

    List<AuditEntry> getRecentEntries(int limit);

    boolean isAvailable();
}
