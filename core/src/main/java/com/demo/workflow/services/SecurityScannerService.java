package com.demo.workflow.services;

import com.day.cq.dam.api.Asset;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service for scanning content for security vulnerabilities.
 *
 * <p>Provides security analysis including:</p>
 * <ul>
 *   <li>Metadata injection detection (XSS, SQL, Command injection)</li>
 *   <li>File type validation (header vs extension mismatch)</li>
 *   <li>Embedded script detection in documents</li>
 * </ul>
 */
public interface SecurityScannerService {

    /**
     * Perform comprehensive security scan on an asset.
     *
     * @param asset The DAM asset to scan
     * @return Complete security scan result with all findings
     */
    SecurityScanResult scanAsset(Asset asset);

    /**
     * Scan metadata for injection patterns.
     *
     * @param metadata Map of metadata properties to scan
     * @return List of security findings
     */
    List<SecurityFinding> scanMetadata(Map<String, Object> metadata);

    /**
     * Validate that file content matches declared MIME type.
     *
     * @param content File content stream
     * @param declaredMimeType The declared MIME type
     * @param fileName The file name for extension checking
     * @return Validation result with any mismatches
     */
    FileTypeValidation validateFileType(InputStream content, String declaredMimeType, String fileName);

    /**
     * Check for embedded scripts in document content.
     *
     * @param content Document content stream
     * @param mimeType The document MIME type
     * @return List of findings for embedded scripts
     */
    List<SecurityFinding> scanForEmbeddedScripts(InputStream content, String mimeType);

    /**
     * Security scan result containing all findings.
     */
    class SecurityScanResult {
        private final List<SecurityFinding> findings;
        private final Severity overallSeverity;
        private final boolean blocked;
        private final long scanDurationMs;

        public SecurityScanResult(List<SecurityFinding> findings, Severity overallSeverity,
                                  boolean blocked, long scanDurationMs) {
            this.findings = findings;
            this.overallSeverity = overallSeverity;
            this.blocked = blocked;
            this.scanDurationMs = scanDurationMs;
        }

        public List<SecurityFinding> getFindings() { return findings; }
        public Severity getOverallSeverity() { return overallSeverity; }
        public boolean isBlocked() { return blocked; }
        public long getScanDurationMs() { return scanDurationMs; }

        public boolean hasCriticalFindings() {
            return findings.stream().anyMatch(f -> f.getSeverity() == Severity.CRITICAL);
        }

        public boolean hasHighFindings() {
            return findings.stream().anyMatch(f -> f.getSeverity() == Severity.HIGH);
        }
    }

    /**
     * Individual security finding.
     */
    class SecurityFinding {
        private final String id;
        private final Severity severity;
        private final String category;
        private final String description;
        private final String location;
        private final String pattern;
        private final String owaspReference;
        private final String cweReference;

        public SecurityFinding(String id, Severity severity, String category,
                               String description, String location, String pattern,
                               String owaspReference, String cweReference) {
            this.id = id;
            this.severity = severity;
            this.category = category;
            this.description = description;
            this.location = location;
            this.pattern = pattern;
            this.owaspReference = owaspReference;
            this.cweReference = cweReference;
        }

        public String getId() { return id; }
        public Severity getSeverity() { return severity; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getLocation() { return location; }
        public String getPattern() { return pattern; }
        public String getOwaspReference() { return owaspReference; }
        public String getCweReference() { return cweReference; }
    }

    /**
     * File type validation result.
     */
    class FileTypeValidation {
        private final boolean valid;
        private final String detectedMimeType;
        private final String declaredMimeType;
        private final String mismatchReason;
        private final Severity severity;

        public FileTypeValidation(boolean valid, String detectedMimeType,
                                  String declaredMimeType, String mismatchReason,
                                  Severity severity) {
            this.valid = valid;
            this.detectedMimeType = detectedMimeType;
            this.declaredMimeType = declaredMimeType;
            this.mismatchReason = mismatchReason;
            this.severity = severity;
        }

        public boolean isValid() { return valid; }
        public String getDetectedMimeType() { return detectedMimeType; }
        public String getDeclaredMimeType() { return declaredMimeType; }
        public String getMismatchReason() { return mismatchReason; }
        public Severity getSeverity() { return severity; }
    }

    /**
     * Security severity levels.
     */
    enum Severity {
        CRITICAL,  // Block + Quarantine immediately
        HIGH,      // Block + Require review
        MEDIUM,    // Flag for review
        LOW,       // Log only
        INFO       // Informational
    }
}
