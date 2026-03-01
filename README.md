# Agentic AEM Development Course

A comprehensive course on **Agentic Development for AEM** using **BMAD**, **BEAD**, and **GasTown** methodologies. Features a production-ready **Secure Asset Approval Workflow** as the hands-on case study.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              AGENTIC AEM DEVELOPMENT COURSE                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   📚 Learn Three AI-First Development Methodologies:                       │
│                                                                             │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                    │
│   │    BMAD     │    │    BEAD     │    │   GasTown   │                    │
│   │  ─────────  │    │  ─────────  │    │  ─────────  │                    │
│   │  Business   │    │  AI-Ready   │    │   Multi-    │                    │
│   │  Driven     │    │   Task      │    │   Agent     │                    │
│   │  Phases     │    │  Tracking   │    │  Orchestr.  │                    │
│   └─────────────┘    └─────────────┘    └─────────────┘                    │
│                                                                             │
│   🔧 Build a Production-Ready Case Study:                                  │
│      Secure Asset Approval Workflow with Antivirus Scanning                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Course Structure

```
agentic-aem-development-course/
├── course/                              # 📚 Educational Materials
│   ├── 00-course-overview/              # Introduction & prerequisites
│   ├── 01-prd/                          # Product Requirements Document
│   ├── 02-bmad-phases/                  # BMAD methodology phases
│   ├── 03-bead-tasks/                   # BEAD task tracking system
│   ├── 04-gastown-orchestration/        # GasTown agent orchestration
│   ├── 05-hands-on-labs/                # 8 hands-on labs
│   ├── 06-instructor-guide/             # Facilitation guide
│   ├── 07-slides/                       # Presentation materials
│   ├── 08-assessments/                  # Knowledge quizzes
│   └── 09-video-scripts/                # Video content scripts
│
├── core/                                # 🔧 Working AEM Code
├── ui.apps/                             # Application package
├── ui.content/                          # Content & workflows
├── docs/                                # Production patterns
└── .cloudmanager/                       # CI/CD configuration
```

## What You'll Learn

| Methodology | Focus | Outcome |
|-------------|-------|---------|
| **BMAD** | Business-driven development phases | Structured project lifecycle |
| **BEAD** | Issue/task tracking with YAML | AI-friendly task management |
| **GasTown** | Multi-agent orchestration | Coordinated AI development |

## Case Study Features

| Feature | Description |
|---------|-------------|
| **Antivirus Scanning** | ClamAV integration with MOCK mode for development |
| **Circuit Breaker** | Resilient external service integration |
| **Health Monitoring** | REST endpoints for load balancer integration |
| **Multi-Channel Notifications** | Email and Slack alert support |
| **Environment Configs** | Separate configs for dev/stage/prod |
| **25 Unit Tests** | Comprehensive test coverage |

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        AEM WORKFLOW ARCHITECTURE                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                  │
│  │   Workflow   │───▶│   Workflow   │───▶│   Workflow   │                  │
│  │   Launcher   │    │    Model     │    │   Instance   │                  │
│  └──────────────┘    └──────────────┘    └──────────────┘                  │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                  │
│  │   Content    │    │   Process    │    │   Payload    │                  │
│  │   Events     │    │    Steps     │    │   Handler    │                  │
│  └──────────────┘    └──────────────┘    └──────────────┘                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Featured Workflows

### 1. Content Approval Workflow
Multi-stage approval process for content publishing.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     CONTENT APPROVAL WORKFLOW                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────┐    │
│   │  START  │────▶│   Author    │────▶│   Review    │────▶│ Approval │    │
│   │         │     │  Submits    │     │   Stage     │     │  Stage   │    │
│   └─────────┘     └─────────────┘     └──────┬──────┘     └────┬─────┘    │
│                                              │                  │          │
│                         ┌────────────────────┘                  │          │
│                         ▼                                       ▼          │
│                   ┌───────────┐                          ┌───────────┐    │
│                   │  REJECT   │                          │  APPROVE  │    │
│                   │  ──────   │                          │  ───────  │    │
│                   │ Send Back │                          │  Publish  │    │
│                   │ to Author │                          │  Content  │    │
│                   └───────────┘                          └───────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2. Asset Processing Workflow
Automated asset processing with metadata enrichment and rendition generation.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ASSET PROCESSING WORKFLOW                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────┐                                                               │
│   │  Asset  │                                                               │
│   │ Upload  │                                                               │
│   └────┬────┘                                                               │
│        │                                                                    │
│        ▼                                                                    │
│   ┌─────────────────────────────────────────────────────────────────┐      │
│   │                    PARALLEL PROCESSING                          │      │
│   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │      │
│   │  │  Extract    │  │  Generate   │  │   Apply     │             │      │
│   │  │  Metadata   │  │ Renditions  │  │ Watermark   │             │      │
│   │  └─────────────┘  └─────────────┘  └─────────────┘             │      │
│   └─────────────────────────────────────────────────────────────────┘      │
│        │                                                                    │
│        ▼                                                                    │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐              │
│   │   Smart Tag  │────▶│   Sync to    │────▶│   Notify     │              │
│   │   Analysis   │     │    XMP       │     │   Owners     │              │
│   └──────────────┘     └──────────────┘     └──────────────┘              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3. Multi-Level Approval Workflow
Enterprise approval workflow with escalation and delegation.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                   MULTI-LEVEL APPROVAL WORKFLOW                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                           ┌─────────────┐                                   │
│                           │    START    │                                   │
│                           └──────┬──────┘                                   │
│                                  │                                          │
│                                  ▼                                          │
│   ┌──────────────────────────────────────────────────────────────────┐     │
│   │                      LEVEL 1: TEAM LEAD                          │     │
│   │   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐     │     │
│   │   │ Assign  │───▶│ Review  │───▶│Decision │───▶│Escalate?│     │     │
│   │   │  Task   │    │ Content │    │         │    │         │     │     │
│   │   └─────────┘    └─────────┘    └────┬────┘    └────┬────┘     │     │
│   └──────────────────────────────────────┼──────────────┼──────────┘     │
│                         ┌────────────────┘              │                │
│                         ▼                               ▼                │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │                    LEVEL 2: MANAGER                              │   │
│   │   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐     │   │
│   │   │ Review  │───▶│Validate │───▶│ Approve │───▶│Delegate?│     │   │
│   │   │ Changes │    │ Policy  │    │         │    │         │     │   │
│   │   └─────────┘    └─────────┘    └────┬────┘    └────┬────┘     │   │
│   └──────────────────────────────────────┼──────────────┼──────────┘   │
│                         ┌────────────────┘              │              │
│                         ▼                               ▼              │
│   ┌──────────────────────────────────────────────────────────────────┐ │
│   │                  LEVEL 3: DIRECTOR                               │ │
│   │   ┌─────────┐    ┌─────────┐    ┌─────────┐                     │ │
│   │   │ Final   │───▶│ Sign-Off│───▶│Complete │                     │ │
│   │   │ Review  │    │         │    │         │                     │ │
│   │   └─────────┘    └─────────┘    └─────────┘                     │ │
│   └──────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Project Structure

```
aem-workflow-demo/
├── core/                              # Java OSGi bundle
│   └── src/main/java/com/demo/workflow/
│       ├── process/                   # Custom workflow process steps
│       ├── models/                    # Sling Models for workflow
│       ├── services/                  # OSGi service interfaces
│       ├── services/impl/             # OSGi service implementations
│       └── servlets/                  # Sling Servlets
├── ui.apps/                           # Application package (packageType=application)
│   └── src/main/content/jcr_root/
│       └── apps/demo-workflow/
│           ├── components/            # Workflow-related components
│           ├── clientlibs/            # Client libraries
│           ├── dialogs/               # Workflow dialogs
│           └── install/               # Embedded core bundle
├── ui.content/                        # Content package (packageType=content)
│   └── src/main/content/jcr_root/
│       ├── content/demo-workflow/     # Sample content
│       └── conf/global/settings/workflow/
│           ├── models/                # Workflow model definitions
│           └── launcher/config/       # Launcher configurations
├── all/                               # Container package (embeds all)
└── docs/
    └── diagrams/                      # Visual documentation
```

## Custom Workflow Processes

All workflow processes are located in `com.demo.workflow.process` package.

| Process | Description | Use Case |
|---------|-------------|----------|
| `ContentValidationProcess` | Validates content against business rules | Pre-approval validation |
| `MetadataEnrichmentProcess` | Enriches metadata from external sources | Asset ingestion |
| `NotificationProcess` | Sends notifications via email/Slack/Teams | Status updates |
| `BrandPortalSyncProcess` | Syncs approved assets to Brand Portal | Asset distribution |
| `WorkflowDelegationProcess` | Delegates tasks based on rules | Absence management |
| `WatermarkProcess` | Applies watermarks to renditions | Asset protection |
| `ReplicationProcess` | Replicates content with tracking | Content publishing |
| `DynamicApproverAssignerProcess` | Routes to approvers based on content category | Dynamic routing |
| `ExternalApiProcess` | Calls external REST APIs | System integration |
| `SummarizeContentProcess` | Summarizes content using LLM services | AI integration |

## OSGi Services

| Service | Description |
|---------|-------------|
| `NotificationService` | Email and notification delivery |
| `ExternalSystemIntegrationService` | HTTP-based external API calls |
| `LlmService` | LLM integration for content summarization |

## Workflow Step Types

### Participant Steps
Human intervention points in the workflow.

```
┌─────────────────────────────────────────────────┐
│              PARTICIPANT STEP                   │
├─────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────┐  │
│  │  • Assigned to User/Group                │  │
│  │  • Inbox Task Created                    │  │
│  │  • Email Notification Sent               │  │
│  │  • Actions: Approve/Reject/Delegate      │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### Process Steps
Automated steps executed by the system.

```
┌─────────────────────────────────────────────────┐
│               PROCESS STEP                      │
├─────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────┐  │
│  │  • Automatic Execution                   │  │
│  │  • Java/ECMA Script Implementation       │  │
│  │  • Timeout Configuration                 │  │
│  │  • Handler Advance Options               │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### OR Split / AND Split
Branching logic for conditional workflows.

```
┌─────────────────────────────────────────────────────────────────────┐
│                         OR SPLIT                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│                      ┌─────────────┐                                │
│                      │  Condition  │                                │
│                      │  Evaluated  │                                │
│                      └──────┬──────┘                                │
│              ┌──────────────┼──────────────┐                        │
│              ▼              ▼              ▼                        │
│        ┌─────────┐    ┌─────────┐    ┌─────────┐                   │
│        │ Branch  │    │ Branch  │    │ Branch  │                   │
│        │    A    │    │    B    │    │    C    │                   │
│        └─────────┘    └─────────┘    └─────────┘                   │
│                                                                     │
│  Note: Only ONE branch executes based on condition                  │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         AND SPLIT                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│                      ┌─────────────┐                                │
│                      │   Parallel  │                                │
│                      │  Execution  │                                │
│                      └──────┬──────┘                                │
│              ┌──────────────┼──────────────┐                        │
│              ▼              ▼              ▼                        │
│        ┌─────────┐    ┌─────────┐    ┌─────────┐                   │
│        │ Branch  │    │ Branch  │    │ Branch  │                   │
│        │    A    │    │    B    │    │    C    │                   │
│        └────┬────┘    └────┬────┘    └────┬────┘                   │
│             └──────────────┼──────────────┘                        │
│                            ▼                                        │
│                      ┌─────────────┐                                │
│                      │  AND JOIN   │                                │
│                      │ (Wait All)  │                                │
│                      └─────────────┘                                │
│                                                                     │
│  Note: ALL branches execute in parallel, join waits for all        │
└─────────────────────────────────────────────────────────────────────┘
```

## Workflow Launchers

Launchers automatically trigger workflows based on content events. Configurations are stored under `/conf/global/settings/workflow/launcher/config/`.

| Event | Path Pattern | Workflow | Node Type |
|-------|--------------|----------|-----------|
| `CREATED` | `/content/dam/demo-workflow/.*` | Asset Processing | `dam:Asset` |
| `MODIFIED` | `/content/demo-site/.*` | Content Approval | `cq:PageContent` |
| `CREATED,MODIFIED` | `/content/experience-fragments/demo-site/production/.*` | Multi-Level Approval | `cq:PageContent` |

## Requirements

- Java 11+
- Maven 3.6+
- AEM as a Cloud Service SDK (local instance on port 4502)

## Installation

### Build Only
```bash
mvn clean install
```

### Build and Deploy (All-in-One Package)
```bash
mvn clean install -PautoInstallSinglePackage
```

### Build and Deploy (Individual Packages)
```bash
mvn clean install -PautoInstallPackage
```

### Built Artifacts
| Module | Artifact | Description |
|--------|----------|-------------|
| all | `aem-workflow-demo.all-*.zip` | Container package (deploy this) |
| core | `aem-workflow-demo.core-*.jar` | OSGi bundle |
| ui.apps | `aem-workflow-demo.ui.apps-*.zip` | Application code |
| ui.content | `aem-workflow-demo.ui.content-*.zip` | Content and configurations |

### Verification URLs
| Resource | URL |
|----------|-----|
| Workflow Models | http://localhost:4502/libs/cq/workflow/admin/console/content/models.html |
| Workflow Instances | http://localhost:4502/libs/cq/workflow/admin/console/content/instances.html |
| Package Manager | http://localhost:4502/crx/packmgr/index.jsp |
| OSGi Bundles | http://localhost:4502/system/console/bundles |

## Testing the Workflows

### Content Approval Workflow
1. Create a page under `/content/demo-site/`
2. Set page status to "pending-approval"
3. Check workflow instances for triggered workflow

### Asset Processing Workflow
1. Upload an image to `/content/dam/demo-workflow/`
2. Workflow triggers automatically for image assets
3. Check asset metadata for enrichment

### Multi-Level Approval Workflow
1. Create an experience fragment under `/content/experience-fragments/demo-site/production/`
2. Workflow triggers for multi-level approval

## Secure Asset Approval Workflow

This project includes a **Secure Asset Approval Workflow** that demonstrates enterprise security patterns:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SECURE ASSET APPROVAL WORKFLOW                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌──────────┐     ┌──────────┐     ┌────────────────────┐             │
│   │  Asset   │────▶│   AV     │────▶│   Route Based      │             │
│   │  Upload  │     │   Scan   │     │   On Result        │             │
│   └──────────┘     └──────────┘     └─────────┬──────────┘             │
│                                               │                        │
│                         ┌─────────────────────┼───────────┐            │
│                         │                     │           │            │
│                   ┌─────▼─────┐        ┌──────▼──────┐    │            │
│                   │   CLEAN   │        │  INFECTED   │    │            │
│                   │           │        │             │    │            │
│                   │ Continue  │        │ Quarantine  │    │            │
│                   │ Approval  │        │ & Notify    │    │            │
│                   └───────────┘        └─────────────┘    │            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Running ClamAV (Docker)

```bash
docker run -d --name clamav -p 3310:3310 clamav/clamav:latest
```

### Health Check Endpoint

```bash
curl http://localhost:4502/bin/workflow/health/antivirus
# Response: {"status":"OK","available":true,"engine":"ClamAV"}
```

## Documentation

- [Production Patterns](docs/PRODUCTION_PATTERNS.md) - Enterprise patterns implemented
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md) - Common issues and solutions
- [Improvements Roadmap](docs/IMPROVEMENTS.md) - Future enhancements

## Cloud Manager Deployment

The project includes Cloud Manager pipeline configuration in `.cloudmanager/`:

```yaml
pipelines:
  full-stack:
    steps:
      - Build & Unit Tests
      - Code Scanning
      - Deploy to Dev
      - Deploy to Stage (manual approval)
      - Deploy to Production (manual approval)
```

## References

- [ACS AEM Commons Workflow Processes](https://adobe-consulting-services.github.io/acs-aem-commons/features/workflow-processes/index.html)
- [AEM Workflow Documentation](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/sites/administering/workflows-administering.html)
- [Developing Workflow Processes](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/implementing/developing/extending/workflow/extending-workflows.html)

## License

This project is intended for educational and demonstration purposes.
