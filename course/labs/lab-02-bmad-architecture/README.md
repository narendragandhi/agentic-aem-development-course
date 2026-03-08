# Lab 2: BMAD Architecture & Domain Models (3 hours)

## Objective
Apply BMAD methodology to design the architecture and domain models for the Secure Asset Approval Workflow.

---

## Part 1: BMAD Overview (30 min)

### 1.1 The 7 BMAD Phases

```
Phase 00: Initialization     → Project setup, team alignment
Phase 01: Discovery          → User research, requirements
Phase 02: Domain Models      → Entities, value objects, events
Phase 03: Architecture       → System design, contracts
Phase 04: Development        → Implementation sprints
Phase 05: Testing            → Validation, QA
Phase 06: Deployment         → Release, operations
```

### 1.2 This Lab Covers

- **Phase 02**: Domain Models
- **Phase 03**: Architecture Design

---

## Part 2: Domain Models (1 hour)

### 2.1 Identify Core Entities

From the PRD, extract domain concepts:

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN MODEL DIAGRAM                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐       ┌─────────────┐       ┌─────────────┐   │
│  │   Asset     │───────│  ScanResult │───────│   Finding   │   │
│  └─────────────┘       └─────────────┘       └─────────────┘   │
│        │                     │                     │            │
│        ▼                     ▼                     ▼            │
│  ┌─────────────┐       ┌─────────────┐       ┌─────────────┐   │
│  │  Metadata   │       │ ThreatLevel │       │  Severity   │   │
│  └─────────────┘       └─────────────┘       └─────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Define Value Objects

Create `course/02-domain-models/entities.md`:

```java
// Asset Entity - The item being scanned
@ValueObject
public class AssetEntity {
    private final String path;
    private final String name;
    private final MimeType mimeType;
    private final AssetMetadata metadata;
    private final WorkflowState state;

    public boolean requiresSecurityReview() {
        return mimeType.isExecutable() ||
               mimeType.isScript() ||
               metadata.containsSuspiciousPatterns();
    }
}

// Security Finding - Immutable scan result item
@ValueObject
@Immutable
public class SecurityFinding {
    private final String id;
    private final Severity severity;
    private final String category;
    private final String description;
    private final String location;
    private final String owaspReference;
    private final String cweReference;
}

// Scan Result Aggregate - Collection of findings
@AggregateRoot
public class ScanResultAggregate {
    private final String scanId;
    private final AssetEntity asset;
    private final List<SecurityFinding> findings;
    private final ThreatLevel threatLevel;

    public boolean shouldBlock() {
        return threatLevel == ThreatLevel.CRITICAL ||
               threatLevel == ThreatLevel.HIGH;
    }

    public WorkflowAction determineNextAction() {
        if (shouldBlock()) return WorkflowAction.QUARANTINE;
        if (threatLevel == ThreatLevel.MEDIUM) return WorkflowAction.REVIEW;
        return WorkflowAction.APPROVE;
    }
}
```

### 2.3 Define Enumerations

```java
public enum Severity {
    CRITICAL(100, "Immediate action required"),
    HIGH(80, "Action within 24 hours"),
    MEDIUM(50, "Action within 1 week"),
    LOW(20, "Address when convenient"),
    INFO(0, "Informational only");

    public boolean blocksWorkflow() {
        return this == CRITICAL || this == HIGH;
    }
}

public enum WorkflowState {
    UPLOADED, SCANNING, SCAN_COMPLETE,
    IN_REVIEW, APPROVED, REJECTED, QUARANTINED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == QUARANTINED;
    }
}

public enum FindingCategory {
    XSS, SQL_INJECTION, COMMAND_INJECTION,
    MALWARE, FILE_TYPE_MISMATCH, EMBEDDED_SCRIPT,
    PATH_TRAVERSAL, SENSITIVE_DATA;
}
```

### 2.4 Define Domain Events

```java
public sealed interface SecurityDomainEvent {
    Instant occurredAt();
    String assetPath();
}

public record AssetUploadedEvent(
    Instant occurredAt, String assetPath,
    String uploadedBy, MimeType mimeType
) implements SecurityDomainEvent {}

public record ThreatDetectedEvent(
    Instant occurredAt, String assetPath,
    SecurityFinding finding, ThreatLevel level
) implements SecurityDomainEvent {}

public record AssetQuarantinedEvent(
    Instant occurredAt, String assetPath,
    String quarantinePath, List<SecurityFinding> findings
) implements SecurityDomainEvent {}
```

---

## Part 3: Architecture Design (1.5 hours)

### 3.1 System Context

```
┌─────────────────────────────────────────────────────────────────┐
│                      SYSTEM CONTEXT                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐                              ┌──────────────┐     │
│  │  Author  │──────upload────────────────▶│   AEM DAM    │     │
│  └──────────┘                              └──────┬───────┘     │
│                                                   │              │
│  ┌──────────┐     ┌──────────────┐              │              │
│  │  Admin   │◀────│  Workflow    │◀─────────────┘              │
│  └──────────┘     │  Engine      │                              │
│                   └──────┬───────┘                              │
│                          │                                       │
│         ┌────────────────┼────────────────┐                     │
│         ▼                ▼                ▼                     │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐                │
│  │  Security  │  │  Antivirus │  │   Audit    │                │
│  │  Scanner   │  │  (ClamAV)  │  │   Logger   │                │
│  └────────────┘  └────────────┘  └────────────┘                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    COMPONENT ARCHITECTURE                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  WORKFLOW LAYER                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  SecurityScanProcess → QuarantineProcess → NotifyProcess   │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│  SERVICE LAYER               ▼                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │SecurityScanner│  │AntivirusScan │  │  AuditLog    │          │
│  │   Service    │  │   Service    │  │   Service    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│  ┌──────┴──────────────────┴──────────────────┴──────┐         │
│  │              DocumentSecurityScanner               │         │
│  │              OwaspSecurityPatterns                 │         │
│  └───────────────────────────────────────────────────┘         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 Service Contracts

```java
// Core scanning interface
public interface SecurityScannerService {
    SecurityScanResult scanAsset(Asset asset);
    List<SecurityFinding> scanMetadata(Map<String, Object> metadata);
    FileTypeValidation validateFileType(InputStream content,
                                         String mimeType, String fileName);
    List<SecurityFinding> scanForEmbeddedScripts(InputStream content,
                                                   String mimeType);
}

// Document-specific scanning
public interface DocumentSecurityScanner {
    List<SecurityFinding> scanPdf(InputStream content);
    List<SecurityFinding> scanOfficeDocument(InputStream content, String mimeType);
    boolean isScannableDocument(String mimeType);
}

// Audit logging
public interface AuditLogService {
    void logSecurityEvent(SecurityDomainEvent event);
    List<AuditEntry> queryLogs(AuditQuery query);
}
```

### 3.4 Workflow State Machine

```
                    ┌──────────────┐
                    │   UPLOADED   │
                    └──────┬───────┘
                           │ trigger scan
                           ▼
                    ┌──────────────┐
                    │   SCANNING   │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
       ┌──────────┐ ┌──────────┐ ┌──────────┐
       │QUARANTINE│ │ IN_REVIEW│ │ APPROVED │
       └──────────┘ └────┬─────┘ └──────────┘
                         │
                    ┌────┴────┐
                    ▼         ▼
              ┌──────────┐ ┌──────────┐
              │ APPROVED │ │ REJECTED │
              └──────────┘ └──────────┘
```

---

## Part 4: Create Architecture Document

Create `course/03-architecture/architecture.md` with:

1. System context diagram
2. Component diagram
3. Service interfaces
4. Data flow diagrams
5. Deployment architecture
6. Non-functional requirements

---

## Verification Checklist

- [ ] Domain entities defined (Asset, Finding, ScanResult)
- [ ] Enumerations defined (Severity, State, Category)
- [ ] Domain events defined
- [ ] System context diagram created
- [ ] Component architecture defined
- [ ] Service contracts specified
- [ ] Workflow state machine documented

---

## Key Takeaways

1. **Domain models drive code** - Entities become classes
2. **Events enable audit** - Every state change is traceable
3. **Contracts enable TDD** - Interfaces before implementation

---

## Next Lab
[Lab 3: TDD Development](../lab-03-tdd-development/README.md)
