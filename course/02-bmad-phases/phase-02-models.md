# BMAD Phase 02: Model Definition

## Overview

Phase 02 (Models) translates business requirements from Phase 01 into domain models, content structures, and data flow diagrams. This phase defines the "what" before the "how" of Phase 03.

---

## Objectives

- Define domain entities and their relationships
- Design content models for AEM
- Create data flow diagrams
- Establish API contracts
- Map requirements to technical models

---

## Activities

### 2.1 Domain Entity Modeling

Identify and define core domain entities:

```java
// Domain Entity: Asset
@Entity
public class AssetEntity {
    private String path;
    private String name;
    private MimeType mimeType;
    private FileSignature signature;
    private AssetMetadata metadata;
    private WorkflowState workflowState;
    private Instant uploadedAt;
    private String uploadedBy;
}

// Domain Entity: Scan Result
@Entity
public class ScanResultEntity {
    private String scanId;
    private String assetPath;
    private ThreatLevel threatLevel;
    private ScanOutcome outcome;
    private List<SecurityFinding> findings;
    private Duration scanDuration;
    private Instant completedAt;
}

// Domain Entity: Security Finding
@ValueObject
public class SecurityFinding {
    private FindingCategory category;
    private Severity severity;
    private String description;
    private String location;
    private String pattern;
    private OwaspReference owasp;
    private CweReference cwe;
    private String remediation;
}
```

### 2.2 Content Model Design

Design AEM content models:

**Content Fragment Model: Asset Review**
```
Model: Asset Review
Fields:
├── title (Text)
├── assetReference (Content Reference)
├── scanStatus (Enumeration: PENDING, SCANNING, CLEAN, INFECTED, ERROR)
├── reviewer (User Reference)
├── reviewDecision (Enumeration: APPROVED, REJECTED, REVISION_REQUESTED)
├── comments (Text Area)
├── reviewedAt (Date)
└── workflowId (Reference)
```

**Content Fragment Model: Quarantine Record**
```
Model: Quarantine Record
Fields:
├── originalPath (Path)
├── quarantinePath (Path)
├── threatName (Text)
├── threatDetails (Text Area)
├── quarantinedBy (User Reference)
├── quarantinedAt (Date)
├── resolution (Enumeration: DELETED, RELEASED, EXPIRED)
└── resolutionNotes (Text Area)
```

### 2.3 Enumeration Definitions

Define key enumerations:

```java
public enum ThreatLevel {
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
    DOUBLE_EXTENSION("Double Extension");
}

public enum WorkflowState {
    UPLOADED("Asset uploaded, pending scan"),
    SCANNING("Security scan in progress"),
    SCAN_COMPLETE("Scan complete, pending review"),
    IN_REVIEW("Under manual review"),
    APPROVED("Approved for publication"),
    REJECTED("Rejected, not approved"),
    QUARANTINED("Quarantined due to security threat");

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == QUARANTINED;
    }
}
```

### 2.4 Data Flow Diagrams

Document how data flows through the system:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DATA FLOW: ASSET UPLOAD                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐    ┌──────────────┐    ┌───────────────┐    ┌───────────┐  │
│  │  DAM     │───▶│  Workflow    │───▶│  Antivirus    │───▶│  Security │  │
│  │  Upload  │    │  Launcher    │    │  Scan         │    │  Scan     │  │
│  └──────────┘    └──────────────┘    └───────────────┘    └───────────┘  │
│                                                                    │        │
│       │                    │                    │                  │        │
│       ▼                    ▼                    ▼                  ▼        │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                     METADATA UPDATE                                  │  │
│  │  • scanId         • scanStatus         • threatLevel              │  │
│  │  • scanStarted    • findings           • scanCompleted           │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                    │                                       │
│                                    ▼                                       │
│       ┌─────────────────┐    ┌──────────────┐    ┌──────────────────┐    │
│       │   CLEAN         │    │   INFECTED   │    │    ERROR         │    │
│       │   Route         │    │   Route      │    │    Route         │    │
│       └────────┬────────┘    └──────┬───────┘    └────────┬─────────┘    │
│                │                    │                     │               │
│                ▼                    ▼                     ▼               │
│       ┌───────────────┐    ┌──────────────┐    ┌──────────────────┐       │
│       │ Approval      │    │ Quarantine   │    │ Alert & Retry   │       │
│       │ Workflow      │    │ Process      │    │                  │       │
│       └───────────────┘    └──────────────┘    └──────────────────┘       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.5 API Contract Definitions

Define service interfaces:

```java
public interface AntivirusScanService {
    ScanResult scan(InputStream content, String fileName);
    boolean isServiceAvailable();
    ServiceStatus getStatus();
}

public interface SecurityScannerService {
    SecurityScanResult scanAsset(Asset asset);
    List<SecurityFinding> scanMetadata(Map<String, Object> metadata);
    FileTypeValidation validateFileType(InputStream content, String mimeType, String fileName);
}

public interface WorkflowInitiatorService {
    void initiateWorkflowForAsset(String assetPath);
    boolean canInitiate(String assetPath);
}
```

---

## Deliverables

### Domain Model Documentation

Complete domain model definitions:

| Entity | Type | Responsibility | Relationships |
|--------|------|----------------|---------------|
| Asset | Aggregate | Represents DAM asset | Has many Findings |
| ScanResult | Aggregate | Scan operation result | Belongs to Asset |
| SecurityFinding | Value Object | Single security issue | Referenced by ScanResult |
| WorkflowInstance | Entity | Active workflow | References Asset |

### Content Model Specifications

AEM content fragment models:

```
/conf/global/settings/dam/adminui-extension/model/
├── asset-review/
│   └── model.xml
├── quarantine-record/
│   └── model.xml
└── audit-entry/
    └── model.xml
```

### Data Flow Documentation

Complete data flow diagrams showing:
- Happy path
- Error paths
- Quarantine flow
- Notification flow

### API Contracts

Service interfaces with:
- Method signatures
- Parameter types
- Return types
- Exception specifications

---

## AI Augmentation

### AI-Generated Domain Models

Use AI to generate domain models from requirements:

**Prompt:**
```
Based on this PRD section, generate domain entities:

"System must scan uploaded assets for malware, detect XSS/SQL injection 
in metadata, validate file types, and route clean assets through 
multi-level approval."

Generate:
1. Core entities
2. Key attributes for each entity
3. Relationships between entities
4. Enumerations for status values
```

### AI Content Model Suggestions

**Prompt:**
```
For an AEM workflow that scans assets for malware, suggest:
1. Content Fragment models needed
2. Metadata schemas for assets
3. Workflow-specific content structures
```

---

## Phase Transition

### Exit Criteria

Before moving to Phase 03 (Architecture), ensure:

- [ ] Domain entities defined
- [ ] Content models designed
- [ ] Data flows documented
- [ ] API contracts specified
- [ ] Enumerations defined
- [ ] Models align with PRD requirements

### Artifacts to Pass to Phase 03

- Domain model documentation
- Content model specifications
- Data flow diagrams
- API contracts

---

## Common Pitfalls

### Avoiding

1. **Over-Engineering**
   - Don't model every possible future scenario
   - Focus on current requirements

2. **Missing Relationships**
   - Ensure all entity relationships are defined
   - Document cascade rules

3. **Incomplete Enumerations**
   - Include all possible states
   - Consider error states

---

## Next Phase

[Phase 03: Architecture Design](phase-03-architecture.md)

In Phase 03, we'll design the technical architecture and component specifications.
