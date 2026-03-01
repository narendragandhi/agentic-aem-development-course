# BMAD Phase 03: Architecture Design
# Secure Asset Approval Workflow

## Phase Overview

This phase defines the technical architecture for the Secure Asset Approval Workflow, including component design, integration patterns, and deployment architecture.

---

## AI Agent Prompts for Phase 03

### Prompt 1: Service Architecture

```
You are an AEM Technical Architect. Design the service layer architecture
for the Secure Asset Approval Workflow.

Based on the PRD requirements:
1. Design the AntivirusScanService interface and implementation
2. Define the integration pattern for ClamAV (TCP socket vs REST)
3. Specify the fallback strategy when scanner is unavailable
4. Create the service configuration schema

Output: Interface definitions, class diagrams, and configuration specs.
```

### Prompt 2: Workflow Process Design

```
You are an AEM Workflow Specialist. Design the workflow process steps
for the Secure Asset Approval Workflow.

Requirements:
1. Define each WorkflowProcess implementation
2. Specify the ParticipantStepChooser for dynamic routing
3. Design the OR_SPLIT conditions for decision points
4. Create the workflow model node structure

Output: Process specifications with metadata configurations.
```

### Prompt 3: Integration Architecture

```
You are an Integration Architect. Design the external integrations
for the Secure Asset Approval Workflow.

Integrations needed:
1. ClamAV daemon (TCP/3310)
2. Email notification service
3. Slack/Teams webhooks (optional)
4. Audit logging system

Output: Integration diagrams, error handling, and retry strategies.
```

---

## Architecture Diagrams

### System Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SYSTEM CONTEXT                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────┐                              ┌──────────────────┐       │
│   │   Content    │                              │    Reviewer /    │       │
│   │   Author     │                              │    Approver      │       │
│   └──────┬───────┘                              └────────┬─────────┘       │
│          │ Upload Asset                                  │ Review Task     │
│          ▼                                               ▼                 │
│   ┌─────────────────────────────────────────────────────────────────┐     │
│   │                                                                  │     │
│   │                    AEM AUTHOR INSTANCE                          │     │
│   │                                                                  │     │
│   │   ┌─────────────────────────────────────────────────────────┐   │     │
│   │   │           SECURE ASSET APPROVAL WORKFLOW                │   │     │
│   │   │                                                         │   │     │
│   │   │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐   │   │     │
│   │   │  │AV Scan  │─▶│Approval │─▶│Approval │─▶│ Publish │   │   │     │
│   │   │  │         │  │Level 1  │  │Level 2  │  │         │   │   │     │
│   │   │  └────┬────┘  └─────────┘  └─────────┘  └─────────┘   │   │     │
│   │   │       │                                                │   │     │
│   │   │  ┌────▼────┐                                          │   │     │
│   │   │  │Quarantin│                                          │   │     │
│   │   │  └─────────┘                                          │   │     │
│   │   └─────────────────────────────────────────────────────────┘   │     │
│   │                                                                  │     │
│   └─────────────────────────────────────────────────────────────────┘     │
│          │              │              │               │                   │
│          │TCP           │SMTP          │HTTP           │HTTP               │
│          ▼              ▼              ▼               ▼                   │
│   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────────┐          │
│   │  ClamAV  │   │  Mail    │   │  Slack/  │   │ AEM Publish  │          │
│   │  Daemon  │   │  Server  │   │  Teams   │   │  Instance    │          │
│   └──────────┘   └──────────┘   └──────────┘   └──────────────┘          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         COMPONENT ARCHITECTURE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        OSGi SERVICES LAYER                           │   │
│  │                                                                      │   │
│  │   ┌───────────────────────┐    ┌───────────────────────┐           │   │
│  │   │ AntivirusScanService  │    │  NotificationService  │           │   │
│  │   │ <<interface>>         │    │  <<interface>>        │           │   │
│  │   ├───────────────────────┤    ├───────────────────────┤           │   │
│  │   │ +scanFile()           │    │ +sendEmail()          │           │   │
│  │   │ +scanAsset()          │    │ +sendSlackAlert()     │           │   │
│  │   │ +isAvailable()        │    │ +sendTeamsAlert()     │           │   │
│  │   └───────────┬───────────┘    └───────────────────────┘           │   │
│  │               │                                                     │   │
│  │   ┌───────────▼───────────┐                                        │   │
│  │   │AntivirusScanServiceImpl│                                       │   │
│  │   ├───────────────────────┤                                        │   │
│  │   │ -config: Config       │                                        │   │
│  │   │ -scanWithClamAV()     │                                        │   │
│  │   │ -scanWithRestApi()    │                                        │   │
│  │   │ -scanWithMock()       │                                        │   │
│  │   └───────────────────────┘                                        │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     WORKFLOW PROCESSES LAYER                         │   │
│  │                                                                      │   │
│  │   ┌───────────────────┐  ┌───────────────────┐  ┌────────────────┐ │   │
│  │   │AntivirusScanProcess│  │ QuarantineProcess │  │NotificationProc│ │   │
│  │   │<<WorkflowProcess>> │  │<<WorkflowProcess>>│  │<<WorkflowProc>>│ │   │
│  │   ├───────────────────┤  ├───────────────────┤  ├────────────────┤ │   │
│  │   │+execute()         │  │+execute()         │  │+execute()      │ │   │
│  │   │-setWorkflowMeta() │  │-moveToQuarantine()│  │-sendNotify()   │ │   │
│  │   │-storeAssetMeta()  │  │-restrictAccess()  │  │-getTemplate()  │ │   │
│  │   └───────────────────┘  └───────────────────┘  └────────────────┘ │   │
│  │                                                                      │   │
│  │   ┌─────────────────────────────┐  ┌─────────────────────────────┐ │   │
│  │   │AssetApprovalParticipantChooser│ │   ReplicationProcess       │ │   │
│  │   │<<ParticipantStepChooser>>   │  │   <<WorkflowProcess>>       │ │   │
│  │   ├─────────────────────────────┤  ├─────────────────────────────┤ │   │
│  │   │+getParticipant()            │  │+execute()                   │ │   │
│  │   │-determineByMimeType()       │  │-activateAsset()             │ │   │
│  │   │-determineByFolder()         │  │-handleFailure()             │ │   │
│  │   │-determineByMetadata()       │  │                             │ │   │
│  │   └─────────────────────────────┘  └─────────────────────────────┘ │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Workflow Model Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     WORKFLOW MODEL ARCHITECTURE                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Node Type Legend:                                                         │
│   ○ = START/END    □ = PROCESS    ◇ = OR_SPLIT    ◆ = PARTICIPANT         │
│                                                                             │
│                           ○ START                                           │
│                              │                                              │
│                              ▼                                              │
│                     □ AntivirusScanProcess                                  │
│                              │                                              │
│                              ▼                                              │
│                     ◇ Scan Result Check                                     │
│                      /              \                                       │
│                CLEAN/                \INFECTED                              │
│                    /                  \                                     │
│                   ▼                    ▼                                    │
│      ◆ Level 1 Review          □ QuarantineProcess                         │
│               │                        │                                    │
│               ▼                        ▼                                    │
│      ◇ L1 Decision             □ SecurityNotification                      │
│      /     |      \                    │                                    │
│   APP    REJ    REVISE                 ▼                                    │
│    /       |        \              ○ QUARANTINED                            │
│   ▼        ▼         ▼                                                      │
│  ◆L2    □Notify   □Notify                                                   │
│   │       │          │                                                      │
│   ▼       ▼          ▼                                                      │
│  ◇L2   ○REJ      ○REJ                                                       │
│  /  |  \                                                                    │
│APP REJ ESC                                                                  │
│ /    |    \                                                                 │
│▼     ▼     ▼                                                                │
│□Pub □Not  ◆L3                                                               │
│ │     │    │                                                                │
│ ▼     ▼    ▼                                                                │
│□Not ○REJ  ◇L3                                                               │
│ │         / \                                                               │
│ ▼       APP REJ                                                             │
│□Aud     /     \                                                             │
│ │      ▼       ▼                                                            │
│ ▼    □Pub   □Not                                                            │
│○END    │       │                                                            │
│        ▼       ▼                                                            │
│      □Not   ○REJ                                                            │
│        │                                                                    │
│        ▼                                                                    │
│      □Aud                                                                   │
│        │                                                                    │
│        ▼                                                                    │
│      ○END                                                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### CS-01: AntivirusScanService

**Purpose**: Provide antivirus scanning capabilities for uploaded assets.

**Interface Definition**:

```java
public interface AntivirusScanService {

    class ScanResult {
        boolean clean;
        String threatName;
        String scanEngine;
        long scanDurationMs;
        String details;
    }

    ScanResult scanFile(InputStream inputStream, String fileName, long fileSize);

    ScanResult scanAsset(String assetPath);

    boolean isAvailable();

    String getScanEngineName();
}
```

**Implementation Strategy**:

| Engine | Priority | Use Case |
|--------|----------|----------|
| ClamAV | Primary | Production environments |
| REST API | Fallback | Cloud-hosted scanning |
| Mock | Development | Local testing only |

**Configuration Schema**:

```yaml
AntivirusScanServiceImpl:
  scanEngine:
    type: String
    options: [CLAMAV, REST_API, MOCK]
    default: CLAMAV
  clamavHost:
    type: String
    default: localhost
  clamavPort:
    type: Integer
    default: 3310
  connectionTimeout:
    type: Integer
    default: 5000
    unit: milliseconds
  readTimeout:
    type: Integer
    default: 60000
    unit: milliseconds
  maxFileSize:
    type: Long
    default: 104857600
    unit: bytes
```

### CS-02: AntivirusScanProcess

**Purpose**: Workflow process step that orchestrates asset scanning.

**Workflow Metadata Set**:

| Key | Type | Description |
|-----|------|-------------|
| av.scanStatus | String | CLEAN, INFECTED, ERROR, SKIPPED |
| av.scanEngine | String | Engine used for scan |
| av.scanTime | Long | Timestamp of scan completion |
| av.threatName | String | Detected threat (if infected) |
| av.scanDuration | Long | Scan duration in ms |

**Error Handling**:

```
Scanner Unavailable:
  1. Log warning
  2. Set status = SKIPPED
  3. Continue workflow (configurable)

Scan Error:
  1. Log error with details
  2. Set status = ERROR
  3. Throw WorkflowException

Infected File:
  1. Set status = INFECTED
  2. Store threat details
  3. Throw WorkflowException (routes to quarantine)
```

### CS-03: QuarantineProcess

**Purpose**: Isolate infected files in secure quarantine location.

**Quarantine Structure**:

```
/content/dam/quarantine/
├── 2024/
│   ├── 02/
│   │   ├── 28/
│   │   │   ├── infected-file-1.pdf
│   │   │   │   └── jcr:content/metadata
│   │   │   │       ├── dam:quarantineDate
│   │   │   │       ├── dam:originalPath
│   │   │   │       ├── dam:threatName
│   │   │   │       ├── dam:quarantineReason
│   │   │   │       └── dam:quarantineExpiryDate
│   │   │   └── infected-file-2.exe
```

**Actions**:
1. Create date-based folder structure
2. Move asset to quarantine
3. Set quarantine metadata
4. Restrict ACL to admin-only
5. Send security notification

### CS-04: AssetApprovalParticipantChooser

**Purpose**: Dynamically assign approvers based on asset characteristics.

**Routing Logic**:

```
Level 1 Routing:
├── image/* → image-reviewers
├── video/* → video-reviewers
├── application/pdf → document-reviewers
├── application/ms* → document-reviewers
└── default → content-reviewers

Level 2 Routing:
├── /brand/* path → brand-managers
├── /legal/* path → legal-reviewers
├── dam:sensitivityLevel=high → senior-approvers
├── size > 50MB → senior-approvers
└── default → senior-approvers

Level 3 Routing:
└── all → content-directors
```

---

## Integration Specifications

### INT-01: ClamAV Integration

**Protocol**: TCP Socket (clamd protocol)

**Commands Used**:
- `PING` - Connection health check
- `INSTREAM` - Stream-based file scanning

**Message Format**:

```
Request (INSTREAM):
  1. Send: "zINSTREAM\0"
  2. For each chunk:
     - Send: 4-byte length (big-endian)
     - Send: chunk data
  3. Send: 4 zero bytes (end marker)

Response:
  - Clean: "stream: OK"
  - Infected: "stream: <virus_name> FOUND"
  - Error: "stream: <error> ERROR"
```

**Error Handling**:

| Error | Retry | Action |
|-------|-------|--------|
| Connection refused | 3x | Fallback to REST API |
| Timeout | 2x | Increase timeout, retry |
| Protocol error | 0x | Log error, fail scan |

### INT-02: Email Notification

**Service**: AEM Mail Service (Day CQ Mail Service)

**Templates**:

| Event | Template | Recipients |
|-------|----------|------------|
| Approval Request | approval-request.html | Assigned reviewer |
| Rejection | rejection-notice.html | Asset owner |
| Quarantine Alert | security-alert.html | security-admins |
| Publication | publish-success.html | Asset owner, stakeholders |

---

## Deployment Architecture

### AEM Cloud Service

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        CLOUD SERVICE DEPLOYMENT                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                    CLOUD MANAGER PIPELINE                            │  │
│   │                                                                      │  │
│   │   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐        │  │
│   │   │  Build  │───▶│  Test   │───▶│  Stage  │───▶│  Prod   │        │  │
│   │   └─────────┘    └─────────┘    └─────────┘    └─────────┘        │  │
│   │                                                                      │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                         AEM AUTHOR                                   │  │
│   │                                                                      │  │
│   │   ┌─────────────────┐    ┌─────────────────────────────────────┐   │  │
│   │   │ Secure Asset    │    │         External Services           │   │  │
│   │   │ Approval        │◄──▶│  ┌─────────┐  ┌─────────────────┐  │   │  │
│   │   │ Workflow        │    │  │ ClamAV  │  │ Notification    │  │   │  │
│   │   │ (OSGi Bundle)   │    │  │ (Docker)│  │ Service         │  │   │  │
│   │   └─────────────────┘    │  └─────────┘  └─────────────────┘  │   │  │
│   │                          └─────────────────────────────────────┘   │  │
│   │                                                                      │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                         │                                   │
│                                         │ Replication                       │
│                                         ▼                                   │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                         AEM PUBLISH                                  │  │
│   │                                                                      │  │
│   │   ┌─────────────────┐    ┌─────────────────────────────────────┐   │  │
│   │   │ Approved Assets │    │           CDN / Dispatcher          │   │  │
│   │   │ /content/dam    │───▶│                                     │   │  │
│   │   └─────────────────┘    └─────────────────────────────────────┘   │  │
│   │                                                                      │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Hands-On Lab: Architecture Design

### Lab 3.1: Generate Component Specifications with AI

**Objective**: Use AI to generate detailed component specifications.

**Steps**:

1. **Open your AI coding assistant** (Claude Code, Copilot, etc.)

2. **Provide the context**:
   ```
   I'm designing an AEM workflow for secure asset approval.

   Here's the PRD excerpt for the AntivirusScanService:
   [Paste relevant PRD section]

   Generate a detailed component specification including:
   - Interface definition
   - Implementation strategy
   - Configuration options
   - Error handling matrix
   ```

3. **Review and refine** the generated specification

4. **Validate against requirements**:
   - [ ] Covers all functional requirements
   - [ ] Addresses error scenarios
   - [ ] Follows AEM best practices

### Lab 3.2: Create Workflow Model Diagram

**Objective**: Design the workflow model structure.

**Steps**:

1. **List all workflow steps** from the PRD
2. **Identify decision points** (OR_SPLIT nodes)
3. **Map transitions** between nodes
4. **Document metadata** for each step

**Template**:

```yaml
workflow_model:
  name: secure-asset-approval
  title: Secure Asset Approval Workflow
  nodes:
    - id: node0
      type: START
      next: node1
    - id: node1
      type: PROCESS
      process: com.demo.workflow.process.AntivirusScanProcess
      auto_advance: true
      next: node2
    # ... continue for all nodes
```

### Lab 3.3: Design Integration Architecture

**Objective**: Define external system integrations.

**Steps**:

1. **Create sequence diagram** for ClamAV integration
2. **Define retry policies** for each integration
3. **Document fallback strategies**
4. **Specify monitoring/alerting requirements**

---

## AI Agent Task Completion

When Phase 03 is complete, the AI agent should report:

```yaml
phase: "03-architecture"
status: "complete"
deliverables:
  - name: "component-specifications"
    status: "documented"
    components:
      - AntivirusScanService
      - AntivirusScanProcess
      - QuarantineProcess
      - AssetApprovalParticipantChooser
  - name: "workflow-model-design"
    status: "documented"
    nodes: 21
    transitions: 24
  - name: "integration-specifications"
    status: "documented"
    integrations:
      - ClamAV
      - Email
      - Slack (optional)
  - name: "deployment-architecture"
    status: "documented"
    environments: [local, dev, stage, prod]
blockers: []
next_phase: "04-development"
```

---

## Transition to Phase 04

Prerequisites for Phase 04:
- [ ] All component specifications reviewed
- [ ] Workflow model approved
- [ ] Integration contracts defined
- [ ] Development environment ready
