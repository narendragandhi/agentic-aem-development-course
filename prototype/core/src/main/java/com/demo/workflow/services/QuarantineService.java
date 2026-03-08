package com.demo.workflow.services;

import java.time.Instant;

public interface QuarantineService {

    enum QuarantineStatus {
        QUARANTINED,
        RELEASED,
        DELETED,
        NOT_FOUND,
        ERROR
    }

    class QuarantineRecord {
        private final String originalPath;
        private final String quarantinePath;
        private final String threatName;
        private final Instant quarantinedAt;
        private final String quarantinedBy;

        public QuarantineRecord(String originalPath, String quarantinePath, 
                String threatName, Instant quarantinedAt, String quarantinedBy) {
            this.originalPath = originalPath;
            this.quarantinePath = quarantinePath;
            this.threatName = threatName;
            this.quarantinedAt = quarantinedAt;
            this.quarantinedBy = quarantinedBy;
        }

        public String getOriginalPath() { return originalPath; }
        public String getQuarantinePath() { return quarantinePath; }
        public String getThreatName() { return threatName; }
        public Instant getQuarantinedAt() { return quarantinedAt; }
        public String getQuarantinedBy() { return quarantinedBy; }
    }

    QuarantineStatus quarantine(String assetPath, String threatName);

    QuarantineStatus release(String quarantineId);

    QuarantineStatus delete(String quarantineId);

    QuarantineRecord getRecord(String quarantineId);

    boolean isAvailable();
}
