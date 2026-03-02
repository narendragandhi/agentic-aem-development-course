# BMAD Phase 02: Domain Models

## Overview

This phase defines the core domain models for the Secure Asset Approval Workflow. These models represent the business entities and their relationships, independent of implementation details.

---

## Domain Model Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SECURE ASSET APPROVAL DOMAIN                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐       ┌─────────────┐       ┌─────────────┐              │
│  │   Asset     │───────│  ScanResult │───────│  Finding    │              │
│  └─────────────┘       └─────────────┘       └─────────────┘              │
│        │                     │                     │                       │
│        │                     │                     │                       │
│        ▼                     ▼                     ▼                       │
│  ┌─────────────┐       ┌─────────────┐       ┌─────────────┐              │
│  │  Metadata   │       │   Threat    │       │  Severity   │              │
│  └─────────────┘       └─────────────┘       └─────────────┘              │
│                                                                             │
│  ┌─────────────┐       ┌─────────────┐       ┌─────────────┐              │
│  │  Workflow   │───────│   Action    │───────│ AuditEntry  │              │
│  │   State     │       │   Result    │       │             │              │
│  └─────────────┘       └─────────────┘       └─────────────┘              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Core Domain Entities

### 1. Asset Entity

Represents a DAM asset undergoing security review.

```java
/**
 * Domain entity representing a DAM asset in the security workflow.
 */
@ValueObject
public class AssetEntity {

    // Identity
    private final String path;
    private final String name;

    // Classification
    private final MimeType mimeType;
    private final FileSignature signature;

    // Metadata
    private final AssetMetadata metadata;

    // State
    private final WorkflowState workflowState;
    private final Instant uploadedAt;
    private final String uploadedBy;

    // Validation
    public boolean isValid() {
        return signature.matchesMimeType(mimeType);
    }

    public boolean requiresSecurityReview() {
        return mimeType.isExecutable() ||
               mimeType.isScript() ||
               metadata.containsSuspiciousPatterns();
    }
}
```

### 2. Scan Result Aggregate

Aggregates all findings from a security scan.

```java
/**
 * Aggregate root for security scan results.
 */
@AggregateRoot
public class ScanResultAggregate {

    // Identity
    private final String scanId;
    private final AssetEntity asset;

    // Findings
    private final List<SecurityFinding> findings;

    // Classification
    private final ThreatLevel threatLevel;
    private final ScanOutcome outcome;

    // Metrics
    private final Duration scanDuration;
    private final Instant completedAt;

    // Business Rules
    public boolean shouldBlock() {
        return threatLevel == ThreatLevel.CRITICAL ||
               threatLevel == ThreatLevel.HIGH;
    }

    public boolean requiresManualReview() {
        return threatLevel == ThreatLevel.MEDIUM;
    }

    public WorkflowAction determineNextAction() {
        if (shouldBlock()) return WorkflowAction.QUARANTINE;
        if (requiresManualReview()) return WorkflowAction.REVIEW;
        return WorkflowAction.APPROVE;
    }
}
```

### 3. Security Finding Value Object

Immutable representation of a single security finding.

```java
/**
 * Value object representing a security finding.
 */
@ValueObject
@Immutable
public class SecurityFinding {

    // Classification
    private final FindingCategory category;
    private final Severity severity;

    // Details
    private final String description;
    private final String location;
    private final String pattern;

    // References
    private final OwaspReference owasp;
    private final CweReference cwe;

    // Recommendations
    private final String remediation;

    // Factory Methods
    public static SecurityFinding xss(String location, String pattern) {
        return new SecurityFinding(
            FindingCategory.XSS,
            Severity.CRITICAL,
            "Cross-Site Scripting vulnerability detected",
            location,
            pattern,
            OwaspReference.A7_2017,
            CweReference.CWE_79,
            "Sanitize or encode user input before rendering"
        );
    }

    public static SecurityFinding sqlInjection(String location, String pattern) {
        return new SecurityFinding(
            FindingCategory.SQL_INJECTION,
            Severity.CRITICAL,
            "SQL Injection vulnerability detected",
            location,
            pattern,
            OwaspReference.A1_2017,
            CweReference.CWE_89,
            "Use parameterized queries or prepared statements"
        );
    }
}
```

---

## Enumerations

### Severity Levels

```java
public enum Severity {
    CRITICAL(100, "Immediate action required"),
    HIGH(80, "Action required within 24 hours"),
    MEDIUM(50, "Action required within 1 week"),
    LOW(20, "Address when convenient"),
    INFO(0, "Informational only");

    private final int score;
    private final String sla;

    public boolean blocksWorkflow() {
        return this == CRITICAL || this == HIGH;
    }
}
```

### Finding Categories

```java
public enum FindingCategory {
    // Injection
    XSS("Cross-Site Scripting"),
    SQL_INJECTION("SQL Injection"),
    COMMAND_INJECTION("Command Injection"),
    LDAP_INJECTION("LDAP Injection"),

    // File-based
    MALWARE("Malware Detected"),
    FILE_TYPE_MISMATCH("File Type Mismatch"),
    EMBEDDED_SCRIPT("Embedded Script"),
    POLYGLOT("Polyglot File"),

    // Path-based
    PATH_TRAVERSAL("Path Traversal"),
    NULL_BYTE("Null Byte Injection"),
    DOUBLE_EXTENSION("Double Extension"),

    // Data
    SENSITIVE_DATA("Sensitive Data Exposure"),
    PII("Personally Identifiable Information");

    private final String displayName;
}
```

### Workflow States

```java
public enum WorkflowState {
    UPLOADED("Asset uploaded, pending scan"),
    SCANNING("Security scan in progress"),
    SCAN_COMPLETE("Scan complete, pending review"),
    IN_REVIEW("Under manual review"),
    APPROVED("Approved for publication"),
    REJECTED("Rejected, not approved"),
    QUARANTINED("Quarantined due to security threat");

    private final String description;

    public boolean isTerminal() {
        return this == APPROVED ||
               this == REJECTED ||
               this == QUARANTINED;
    }

    public Set<WorkflowState> allowedTransitions() {
        switch (this) {
            case UPLOADED: return Set.of(SCANNING);
            case SCANNING: return Set.of(SCAN_COMPLETE, QUARANTINED);
            case SCAN_COMPLETE: return Set.of(IN_REVIEW, APPROVED, QUARANTINED);
            case IN_REVIEW: return Set.of(APPROVED, REJECTED);
            default: return Set.of();
        }
    }
}
```

---

## OWASP Reference Model

```java
public enum OwaspReference {
    A1_2017("Injection", "https://owasp.org/Top10/A03_2021-Injection/"),
    A2_2017("Broken Authentication", "https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/"),
    A3_2017("Sensitive Data Exposure", "https://owasp.org/Top10/A02_2021-Cryptographic_Failures/"),
    A4_2017("XML External Entities", "https://owasp.org/Top10/A05_2021-Security_Misconfiguration/"),
    A5_2017("Broken Access Control", "https://owasp.org/Top10/A01_2021-Broken_Access_Control/"),
    A6_2017("Security Misconfiguration", "https://owasp.org/Top10/A05_2021-Security_Misconfiguration/"),
    A7_2017("Cross-Site Scripting", "https://owasp.org/Top10/A03_2021-Injection/"),
    A8_2017("Insecure Deserialization", "https://owasp.org/Top10/A08_2021-Software_and_Data_Integrity_Failures/"),
    A9_2017("Using Components with Known Vulnerabilities", "https://owasp.org/Top10/A06_2021-Vulnerable_and_Outdated_Components/"),
    A10_2017("Insufficient Logging", "https://owasp.org/Top10/A09_2021-Security_Logging_and_Monitoring_Failures/");

    private final String name;
    private final String url;
}
```

---

## CWE Reference Model

```java
public enum CweReference {
    CWE_79("Improper Neutralization of Input During Web Page Generation (XSS)"),
    CWE_89("Improper Neutralization of Special Elements used in SQL Command"),
    CWE_94("Improper Control of Generation of Code (Code Injection)"),
    CWE_434("Unrestricted Upload of File with Dangerous Type"),
    CWE_22("Improper Limitation of a Pathname to a Restricted Directory"),
    CWE_77("Improper Neutralization of Special Elements used in a Command"),
    CWE_90("Improper Neutralization of Special Elements used in LDAP Query"),
    CWE_611("Improper Restriction of XML External Entity Reference"),
    CWE_502("Deserialization of Untrusted Data");

    private final String description;

    public String getUrl() {
        return "https://cwe.mitre.org/data/definitions/" +
               name().replace("CWE_", "") + ".html";
    }
}
```

---

## Domain Events

```java
/**
 * Domain events emitted during the security workflow.
 */
public sealed interface SecurityDomainEvent {
    Instant occurredAt();
    String assetPath();
}

public record AssetUploadedEvent(
    Instant occurredAt,
    String assetPath,
    String uploadedBy,
    MimeType mimeType
) implements SecurityDomainEvent {}

public record ScanStartedEvent(
    Instant occurredAt,
    String assetPath,
    String scanId
) implements SecurityDomainEvent {}

public record ThreatDetectedEvent(
    Instant occurredAt,
    String assetPath,
    SecurityFinding finding,
    ThreatLevel threatLevel
) implements SecurityDomainEvent {}

public record AssetQuarantinedEvent(
    Instant occurredAt,
    String assetPath,
    String quarantinePath,
    List<SecurityFinding> findings
) implements SecurityDomainEvent {}

public record AssetApprovedEvent(
    Instant occurredAt,
    String assetPath,
    String approvedBy,
    String approvalNotes
) implements SecurityDomainEvent {}
```

---

## Business Rules

### BR-1: Scan Blocking Rules

```yaml
blocking_rules:
  - condition: "severity == CRITICAL"
    action: "QUARANTINE"
    immediate: true

  - condition: "severity == HIGH"
    action: "BLOCK_AND_REVIEW"
    immediate: true

  - condition: "category == MALWARE"
    action: "QUARANTINE"
    immediate: true
    notify: ["security-team@company.com"]

  - condition: "file_type_mismatch && extension_is_executable"
    action: "QUARANTINE"
    immediate: true
```

### BR-2: Approval Rules

```yaml
approval_rules:
  - condition: "no_findings"
    action: "AUTO_APPROVE"

  - condition: "only_low_findings && count < 3"
    action: "AUTO_APPROVE_WITH_NOTES"

  - condition: "medium_findings"
    action: "REQUIRE_REVIEW"
    reviewers: ["content-reviewers"]

  - condition: "high_findings"
    action: "REQUIRE_REVIEW"
    reviewers: ["security-team", "content-lead"]
```

---

## File Signature Database

```java
public class FileSignatureDatabase {

    private static final Map<String, FileSignature> SIGNATURES = Map.of(
        // Images
        "image/png", FileSignature.of(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47}),
        "image/jpeg", FileSignature.of(new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}),
        "image/gif", FileSignature.of(new byte[]{0x47, 0x49, 0x46, 0x38}),
        "image/webp", FileSignature.of(new byte[]{0x52, 0x49, 0x46, 0x46}),

        // Documents
        "application/pdf", FileSignature.of(new byte[]{0x25, 0x50, 0x44, 0x46}),
        "application/zip", FileSignature.of(new byte[]{0x50, 0x4B, 0x03, 0x04}),

        // Office (OOXML)
        "application/vnd.openxmlformats-officedocument",
            FileSignature.of(new byte[]{0x50, 0x4B, 0x03, 0x04}), // ZIP-based

        // Executables (DANGEROUS)
        "application/x-msdownload",
            FileSignature.of(new byte[]{0x4D, 0x5A}), // MZ header
        "application/x-executable",
            FileSignature.of(new byte[]{0x7F, 0x45, 0x4C, 0x46}) // ELF header
    );

    public Optional<String> detectMimeType(byte[] header) {
        return SIGNATURES.entrySet().stream()
            .filter(e -> e.getValue().matches(header))
            .map(Map.Entry::getKey)
            .findFirst();
    }
}
```

---

## Next Phase

Phase 03: Architecture Design → [Architecture Documentation](../03-architecture/README.md)
