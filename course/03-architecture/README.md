# BMAD Phase 03: Architecture Design

## Overview

This phase defines the technical architecture for the Secure Asset Approval Workflow system. It covers component design, integration patterns, deployment topology, and non-functional requirements.

---

## System Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SYSTEM CONTEXT                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐                           ┌─────────────┐                │
│   │   Content   │──── uploads ────────────▶│    AEM      │                │
│   │   Authors   │                           │   Author    │                │
│   └─────────────┘                           └──────┬──────┘                │
│                                                    │                        │
│   ┌─────────────┐                           ┌──────▼──────┐                │
│   │  Security   │◀─── alerts ──────────────│  Workflow   │                │
│   │    Team     │                           │   Engine    │                │
│   └─────────────┘                           └──────┬──────┘                │
│                                                    │                        │
│   ┌─────────────┐                           ┌──────▼──────┐                │
│   │  External   │◀─── scan requests ───────│  Security   │                │
│   │  Antivirus  │                           │  Services   │                │
│   └─────────────┘                           └──────┬──────┘                │
│                                                    │                        │
│   ┌─────────────┐                           ┌──────▼──────┐                │
│   │   Audit     │◀─── log events ──────────│   Audit     │                │
│   │   System    │                           │   Service   │                │
│   └─────────────┘                           └─────────────┘                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        COMPONENT ARCHITECTURE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         WORKFLOW LAYER                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │   │
│  │  │ Antivirus   │  │ Security    │  │ Quarantine  │  │ Approver   │ │   │
│  │  │   Scan      │  │   Scan      │  │   Process   │  │  Assign    │ │   │
│  │  │  Process    │  │  Process    │  │             │  │  Process   │ │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────┬──────┘ │   │
│  └─────────┼────────────────┼────────────────┼───────────────┼────────┘   │
│            │                │                │               │             │
│  ┌─────────▼────────────────▼────────────────▼───────────────▼────────┐   │
│  │                         SERVICE LAYER                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │   │
│  │  │ Antivirus   │  │  Security   │  │   Audit     │  │ Notifica-  │ │   │
│  │  │   Scan      │  │  Scanner    │  │    Log      │  │   tion     │ │   │
│  │  │  Service    │  │  Service    │  │  Service    │  │  Service   │ │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────┬──────┘ │   │
│  │         │                │                │               │         │   │
│  │  ┌──────┴────────────────┴────────────────┴───────────────┴──────┐ │   │
│  │  │                    EXTENSION SERVICES                          │ │   │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │ │   │
│  │  │  │ AI Code     │  │ Performance │  │   Future    │           │ │   │
│  │  │  │  Review     │  │  Testing    │  │ Extensions  │           │ │   │
│  │  │  └─────────────┘  └─────────────┘  └─────────────┘           │ │   │
│  │  └──────────────────────────────────────────────────────────────┘ │   │
│  └───────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      INFRASTRUCTURE LAYER                            │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │   │
│  │  │    JCR      │  │    OSGi     │  │   Sling     │  │   Event    │ │   │
│  │  │ Repository  │  │  Container  │  │   Models    │  │   System   │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │   │
│  └───────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Service Interfaces

### Core Service Contracts

```java
/**
 * Contract for antivirus scanning service.
 */
public interface AntivirusScanService {
    ScanResult scan(InputStream content, String fileName);
    boolean isServiceAvailable();
    ServiceStatus getStatus();
}

/**
 * Contract for security content scanning.
 */
public interface SecurityScannerService {
    SecurityScanResult scanAsset(Asset asset);
    List<SecurityFinding> scanMetadata(Map<String, Object> metadata);
    FileTypeValidation validateFileType(InputStream content, String mimeType, String fileName);
    List<SecurityFinding> scanForEmbeddedScripts(InputStream content, String mimeType);
}

/**
 * Contract for audit logging.
 */
public interface AuditLogService {
    AuditEntry logSecurityEvent(String eventType, String assetPath, Map<String, Object> context);
    List<AuditEntry> findByEventType(String eventType);
    List<AuditEntry> findByPathPrefix(String pathPrefix);
    List<AuditEntry> findByTimeRange(Instant start, Instant end);
    int cleanupOldEntries();
}

/**
 * Contract for notification delivery.
 */
public interface NotificationService {
    void notifySecurityTeam(SecurityAlert alert);
    void notifyApprovers(ApprovalRequest request);
    void notifyContentAuthor(WorkflowUpdate update);
}
```

### Extension Service Contracts

```java
/**
 * Contract for AI-powered code review.
 */
public interface AICodeReviewService {
    CodeReviewResult reviewFile(String filePath, String content);
    BatchReviewResult reviewBatch(List<FileContent> files);
    List<ReviewRule> getAvailableRules();
    QualityScore calculateScore(BatchReviewResult results);
}

/**
 * Contract for performance testing.
 */
public interface PerformanceTestingService {
    PerformanceResult runTest(PerformanceTestConfig config);
    TimingResult measureExecution(Runnable operation, String operationName);
    ResourceSnapshot getResourceSnapshot();
    ComparisonResult compareToBaseline(PerformanceResult current, PerformanceResult baseline);
    SLACheckResult checkSLA(PerformanceResult result, SLADefinition sla);
}
```

---

## Workflow Process Design

### Process Sequence Diagram

```
┌────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌────────┐
│ Upload │     │ Antivirus│     │ Security │     │ Approver │     │ Publish│
│ Asset  │     │   Scan   │     │   Scan   │     │  Review  │     │  Asset │
└───┬────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘     └───┬────┘
    │               │                │                │               │
    │   upload      │                │                │               │
    │──────────────▶│                │                │               │
    │               │                │                │               │
    │               │  scan          │                │               │
    │               │───────────────▶│                │               │
    │               │                │                │               │
    │               │                │  [clean]       │               │
    │               │                │───────────────▶│               │
    │               │                │                │               │
    │               │                │                │  [approved]   │
    │               │                │                │──────────────▶│
    │               │                │                │               │
    │               │  [threat]      │                │               │
    │               │ ──────────────────────────────────────────────▶ │
    │               │                │                │    QUARANTINE │
    │               │                │                │               │
```

### Process State Machine

```
                    ┌─────────────┐
                    │   START     │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  ANTIVIRUS  │
                    │    SCAN     │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │  CLEAN   │ │  THREAT  │ │  ERROR   │
        └────┬─────┘ └────┬─────┘ └────┬─────┘
             │            │            │
             ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ SECURITY │ │QUARANTINE│ │  RETRY   │
        │   SCAN   │ └──────────┘ └──────────┘
        └────┬─────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
┌──────┐ ┌──────┐ ┌──────┐
│APPROVE│ │REVIEW│ │ BLOCK│
│ AUTO │ │MANUAL│ │      │
└──┬───┘ └──┬───┘ └──┬───┘
   │        │        │
   ▼        ▼        ▼
┌──────┐ ┌──────┐ ┌──────┐
│PUBLISH│ │DECIDE│ │REJECT│
└──────┘ └──┬───┘ └──────┘
            │
      ┌─────┴─────┐
      ▼           ▼
 ┌────────┐  ┌────────┐
 │APPROVED│  │REJECTED│
 └────────┘  └────────┘
```

---

## Integration Patterns

### External Antivirus Integration

```java
/**
 * Adapter pattern for external antivirus integration.
 */
public interface AntivirusAdapter {

    /**
     * Supported antivirus backends.
     */
    enum Backend {
        CLAMAV,      // Open source, network scanner
        METADEFENDER,// Cloud-based multi-scanner
        VIRUSTOTAL,  // API-based scanning
        MOCK         // For testing
    }

    ScanResult scan(InputStream content, String fileName);
    boolean isAvailable();
    String getVersion();
}

/**
 * ClamAV implementation via clamd network protocol.
 */
@Component(service = AntivirusAdapter.class)
@ServiceRanking(100)
public class ClamAVAdapter implements AntivirusAdapter {

    @Reference
    private HttpClientService httpClient;

    private static final String CLAMD_HOST = "localhost";
    private static final int CLAMD_PORT = 3310;

    @Override
    public ScanResult scan(InputStream content, String fileName) {
        // Connect to clamd daemon
        // Send INSTREAM command
        // Stream file content
        // Parse response
    }
}
```

### Event-Driven Architecture

```java
/**
 * Domain events published via OSGi Event Admin.
 */
@Component(service = SecurityEventPublisher.class)
public class SecurityEventPublisher {

    @Reference
    private EventAdmin eventAdmin;

    public void publishThreatDetected(Asset asset, SecurityFinding finding) {
        Map<String, Object> props = new HashMap<>();
        props.put("asset.path", asset.getPath());
        props.put("finding.category", finding.getCategory());
        props.put("finding.severity", finding.getSeverity().name());
        props.put("timestamp", Instant.now().toString());

        Event event = new Event("com/demo/security/THREAT_DETECTED", props);
        eventAdmin.postEvent(event);
    }
}

/**
 * Event handler for security notifications.
 */
@Component(
    service = EventHandler.class,
    property = {
        EventConstants.EVENT_TOPIC + "=com/demo/security/*"
    }
)
public class SecurityEventHandler implements EventHandler {

    @Reference
    private NotificationService notificationService;

    @Reference
    private AuditLogService auditLogService;

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();

        if (topic.endsWith("THREAT_DETECTED")) {
            handleThreatDetected(event);
        } else if (topic.endsWith("ASSET_QUARANTINED")) {
            handleAssetQuarantined(event);
        }
    }
}
```

---

## Deployment Architecture

### AEM Cloud Service Topology

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        AEM CLOUD SERVICE                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         AUTHOR TIER                                  │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │   Author    │  │   Author    │  │  Workflow   │                 │   │
│  │  │  Instance 1 │  │  Instance 2 │  │   Engine    │                 │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                 │   │
│  │         │                │                │                         │   │
│  │  ┌──────┴────────────────┴────────────────┴──────┐                 │   │
│  │  │              SHARED SERVICES                   │                 │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │                 │   │
│  │  │  │ Security │  │  Audit   │  │Notification│   │                 │   │
│  │  │  │ Services │  │ Service  │  │  Service  │    │                 │   │
│  │  │  └──────────┘  └──────────┘  └──────────┘    │                 │   │
│  │  └────────────────────────────────────────────────┘                 │   │
│  └───────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        PUBLISH TIER                                  │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │   Publish   │  │   Publish   │  │   Publish   │                 │   │
│  │  │  Instance 1 │  │  Instance 2 │  │  Instance N │                 │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                 │   │
│  └───────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      EXTERNAL SERVICES                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │  ClamAV     │  │   SMTP      │  │   SIEM      │                 │   │
│  │  │  Scanner    │  │   Server    │  │   System    │                 │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                 │   │
│  └───────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Non-Functional Requirements

### Performance Requirements

| Metric | Target | Warning | Critical |
|--------|--------|---------|----------|
| Antivirus scan (10MB) | < 5s | > 10s | > 30s |
| Security scan | < 500ms | > 1s | > 2s |
| Metadata scan | < 100ms | > 200ms | > 500ms |
| Workflow step | < 2s | > 5s | > 10s |

### Scalability Requirements

```yaml
scalability:
  concurrent_workflows: 100
  max_file_size: 100MB
  audit_log_retention: 90 days
  quarantine_capacity: 10GB

  horizontal_scaling:
    author_instances: 2-4
    publish_instances: 2-N
```

### Security Requirements

```yaml
security:
  authentication:
    - AEM user authentication required
    - Service users for system operations

  authorization:
    - Role-based access control
    - Separate permissions for quarantine access

  data_protection:
    - TLS 1.3 for external communications
    - Encrypted quarantine storage
    - PII handling compliance

  audit:
    - All security events logged
    - Tamper-proof audit trail
    - 90-day retention minimum
```

---

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Runtime | AEM as a Cloud Service | Latest |
| Framework | OSGi Declarative Services | R7 |
| Dependency Injection | OSGi SCR | 2.1+ |
| Testing | JUnit 5 + AEM Mock | 5.x |
| Build | Maven | 3.8+ |
| Java | OpenJDK | 11 |

---

## Next Phase

Phase 04: Implementation → [BEAD Tasks](../03-bead-tasks/README.md)
