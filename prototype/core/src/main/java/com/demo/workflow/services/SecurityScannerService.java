package com.demo.workflow.services;

import java.util.List;
import java.util.Map;

public interface SecurityScannerService {

    enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    enum SecurityStatus {
        SECURE, VULNERABLE, ERROR, SCANNING
    }

    class SecurityIssue {
        private final String issueId;
        private final String description;
        private final Severity severity;
        private final String location;
        private final String remediation;

        public SecurityIssue(String issueId, String description, Severity severity, 
                String location, String remediation) {
            this.issueId = issueId;
            this.description = description;
            this.severity = severity;
            this.location = location;
            this.remediation = remediation;
        }

        public String getIssueId() { return issueId; }
        public String getDescription() { return description; }
        public Severity getSeverity() { return severity; }
        public String getLocation() { return location; }
        public String getRemediation() { return remediation; }
    }

    class ScanReport {
        private final SecurityStatus status;
        private final List<SecurityIssue> issues;
        private final long scanDurationMs;
        private final String scannedBy;

        public ScanReport(SecurityStatus status, List<SecurityIssue> issues, 
                long scanDurationMs, String scannedBy) {
            this.status = status;
            this.issues = issues;
            this.scanDurationMs = scanDurationMs;
            this.scannedBy = scannedBy;
        }

        public SecurityStatus getStatus() { return status; }
        public List<SecurityIssue> getIssues() { return issues; }
        public long getScanDurationMs() { return scanDurationMs; }
        public String getScannedBy() { return scannedBy; }

        public int getCriticalCount() {
            return (int) issues.stream().filter(i -> i.getSeverity() == Severity.CRITICAL).count();
        }

        public int getHighCount() {
            return (int) issues.stream().filter(i -> i.getSeverity() == Severity.HIGH).count();
        }
    }

    ScanReport scan(String assetPath, Map<String, Object> options);

    SecurityStatus getStatus(String assetPath);

    boolean isAvailable();
}
