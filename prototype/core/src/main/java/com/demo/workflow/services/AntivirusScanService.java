package com.demo.workflow.services;

/**
 * Service for scanning assets for malware.
 * 
 * BMAD Phase 04 - Development with TDD
 * 
 * This is the service interface - the contract.
 */
public interface AntivirusScanService {

    /**
     * Result status after scanning.
     */
    enum ScanStatus {
        CLEAN,
        INFECTED,
        ERROR,
        PENDING
    }

    /**
     * Scan result containing status and details.
     */
    class ScanResult {
        private final ScanStatus status;
        private final String threatName;
        private final long scanDurationMs;

        public ScanResult(ScanStatus status, String threatName, long scanDurationMs) {
            this.status = status;
            this.threatName = threatName;
            this.scanDurationMs = scanDurationMs;
        }

        public ScanStatus getStatus() { return status; }
        public String getThreatName() { return threatName; }
        public long getScanDurationMs() { return scanDurationMs; }

        public static ScanResult clean(long durationMs) {
            return new ScanResult(ScanStatus.CLEAN, null, durationMs);
        }

        public static ScanResult infected(String threatName, long durationMs) {
            return new ScanResult(ScanStatus.INFECTED, threatName, durationMs);
        }

        public static ScanResult error(long durationMs) {
            return new ScanResult(ScanStatus.ERROR, null, durationMs);
        }

        public static ScanResult pending(long durationMs) {
            return new ScanResult(ScanStatus.PENDING, null, durationMs);
        }
    }

    /**
     * Scan content for malware.
     * @param content File content to scan
     * @param fileName File name for logging
     * @return ScanResult with status
     */
    ScanResult scan(java.io.InputStream content, String fileName);

    /**
     * Check if scanner is available.
     * @return true if available
     */
    boolean isAvailable();
}
