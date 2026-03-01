package com.demo.workflow.services;

import java.io.InputStream;

/**
 * Antivirus Scanning Service
 *
 * Provides file scanning capabilities for malware and virus detection.
 * Supports multiple scanning backends (ClamAV, external APIs, cloud services).
 */
public interface AntivirusScanService {

    /**
     * Scan result containing status and details
     */
    class ScanResult {
        private final boolean clean;
        private final String threatName;
        private final String scanEngine;
        private final long scanDurationMs;
        private final String details;

        public ScanResult(boolean clean, String threatName, String scanEngine,
                          long scanDurationMs, String details) {
            this.clean = clean;
            this.threatName = threatName;
            this.scanEngine = scanEngine;
            this.scanDurationMs = scanDurationMs;
            this.details = details;
        }

        public boolean isClean() {
            return clean;
        }

        public String getThreatName() {
            return threatName;
        }

        public String getScanEngine() {
            return scanEngine;
        }

        public long getScanDurationMs() {
            return scanDurationMs;
        }

        public String getDetails() {
            return details;
        }

        public static ScanResult clean(String engine, long durationMs) {
            return new ScanResult(true, null, engine, durationMs, "No threats detected");
        }

        public static ScanResult infected(String threatName, String engine, long durationMs) {
            return new ScanResult(false, threatName, engine, durationMs,
                "Threat detected: " + threatName);
        }

        public static ScanResult error(String engine, String errorMessage) {
            return new ScanResult(false, null, engine, 0, "Scan error: " + errorMessage);
        }
    }

    /**
     * Scans a file for malware/viruses.
     *
     * @param inputStream the file content to scan
     * @param fileName the original filename (for logging and type detection)
     * @param fileSize the file size in bytes
     * @return ScanResult containing the scan outcome
     */
    ScanResult scanFile(InputStream inputStream, String fileName, long fileSize);

    /**
     * Scans a file at the given repository path.
     *
     * @param assetPath the JCR path to the asset
     * @return ScanResult containing the scan outcome
     */
    ScanResult scanAsset(String assetPath);

    /**
     * Checks if the antivirus service is available and configured.
     *
     * @return true if the service can perform scans
     */
    boolean isAvailable();

    /**
     * Gets the name of the active scan engine.
     *
     * @return the scan engine name (e.g., "ClamAV", "VirusTotal", "Mock")
     */
    String getScanEngineName();
}
