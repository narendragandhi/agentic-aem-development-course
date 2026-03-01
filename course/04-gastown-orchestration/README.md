# GasTown Agent Orchestration
# Secure Asset Approval Workflow

## What is GasTown?

GasTown is an AI agent orchestration system that coordinates multiple specialized agents to complete complex development tasks. It acts as the "mayor" directing a team of AI workers.

---

## GasTown Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         GASTOWN ORCHESTRATION                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                           ┌─────────────────┐                               │
│                           │   MAYOR AI      │                               │
│                           │   Orchestrator  │                               │
│                           └────────┬────────┘                               │
│                                    │                                        │
│            ┌───────────────────────┼───────────────────────┐               │
│            │                       │                       │               │
│            ▼                       ▼                       ▼               │
│   ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐       │
│   │   AEM Coder     │    │   AEM Tester    │    │  AEM Reviewer   │       │
│   │   Agent         │    │   Agent         │    │  Agent          │       │
│   └────────┬────────┘    └────────┬────────┘    └────────┬────────┘       │
│            │                       │                       │               │
│            │                       │                       │               │
│            ▼                       ▼                       ▼               │
│   ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐       │
│   │     BEAD        │    │     BEAD        │    │     BEAD        │       │
│   │  Task Tracking  │    │  Task Tracking  │    │  Task Tracking  │       │
│   └─────────────────┘    └─────────────────┘    └─────────────────┘       │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                        SHARED CONTEXT                                │  │
│   │  • Git Repository         • BMAD Documents       • PRD Requirements │  │
│   │  • BEAD Task State        • Build Artifacts      • Test Results     │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Agent Definitions

### Mayor AI (Orchestrator)

The Mayor AI coordinates all specialized agents:

| Responsibility | Description |
|----------------|-------------|
| Task Distribution | Assigns BEAD tasks to appropriate agents |
| Dependency Management | Ensures tasks execute in correct order |
| Progress Monitoring | Tracks completion across all agents |
| Conflict Resolution | Handles blocking issues and escalations |
| Quality Gates | Enforces review before merge |

### Specialized Agents

| Agent | Specialization | Tools |
|-------|----------------|-------|
| **AEM Coder** | Java, Sling Models, OSGi, HTL | Write, Edit, Bash (mvn) |
| **AEM Tester** | JUnit, Integration Tests, UI Tests | Write, Bash (mvn test) |
| **AEM Reviewer** | Code Review, Security, Best Practices | Read, Grep, Analysis |
| **AEM Docs** | JavaDoc, Markdown, README | Write, Read |
| **AEM DevOps** | CI/CD, Cloud Manager, Docker | Bash, Config files |

---

## Workflow Definitions

### Workflow: Implement Feature

```yaml
workflow: implement-feature
trigger: bead.feature.assigned
agents:
  - aem-coder-agent
  - aem-tester-agent
  - aem-reviewer-agent

steps:
  - name: analyze-requirements
    agent: aem-coder-agent
    input:
      - bead.task.description
      - bead.task.acceptance_criteria
      - context.architecture_doc
    output:
      - implementation_plan.md

  - name: implement-code
    agent: aem-coder-agent
    depends_on: analyze-requirements
    input:
      - implementation_plan.md
      - context.existing_code
    output:
      - source_files[]
    bead_update:
      status: in_progress
      artifact: source_files

  - name: generate-tests
    agent: aem-tester-agent
    depends_on: implement-code
    parallel: false
    input:
      - source_files[]
      - bead.task.acceptance_criteria
    output:
      - test_files[]

  - name: run-tests
    agent: aem-tester-agent
    depends_on: generate-tests
    input:
      - test_files[]
    command: "mvn test -pl core"
    output:
      - test_results.xml
    gate:
      condition: "test_results.failures == 0"
      on_fail: "return to implement-code"

  - name: code-review
    agent: aem-reviewer-agent
    depends_on: run-tests
    input:
      - source_files[]
      - test_files[]
    output:
      - review_findings.md
    gate:
      condition: "review_findings.blockers == 0"
      on_fail: "return to implement-code"

  - name: complete
    agent: mayor
    depends_on: code-review
    action:
      - bead.task.status = completed
      - bead.task.artifacts = source_files + test_files
      - notify.stakeholders
```

### Workflow: Build and Deploy

```yaml
workflow: build-deploy
trigger: bead.feature.completed
agents:
  - aem-devops-agent

steps:
  - name: build-project
    agent: aem-devops-agent
    command: "mvn clean install"
    gate:
      condition: "build.exit_code == 0"
      on_fail: "alert.team"

  - name: deploy-local
    agent: aem-devops-agent
    depends_on: build-project
    command: "mvn -PautoInstallSinglePackage"

  - name: smoke-test
    agent: aem-tester-agent
    depends_on: deploy-local
    input:
      - smoke_test_suite
    output:
      - smoke_results.xml

  - name: update-status
    agent: mayor
    depends_on: smoke-test
    action:
      - bead.feature.status = deployed
      - notify.product_owner
```

---

## Lab 6.1: Configure GasTown

### Step 1: Create Agent Configuration

Create `gastown.yaml` in project root:

```yaml
# GasTown Configuration
# Secure Asset Approval Workflow

project:
  name: secure-asset-workflow
  repository: aem-workflow-demo
  bead_path: course/03-bead-tasks/.issues

mayor:
  model: claude-3-opus
  max_parallel_agents: 3
  session_timeout: 3600  # seconds
  checkpoint_interval: 300  # seconds

agents:
  aem-coder:
    model: claude-3-opus
    specialization: aem-java-development
    tools:
      - read
      - write
      - edit
      - bash
    context:
      - "core/src/main/java/**/*.java"
      - "course/02-bmad-phases/phase-03-architecture.md"
    max_tokens: 100000

  aem-tester:
    model: claude-3-sonnet
    specialization: aem-testing
    tools:
      - read
      - write
      - bash
    context:
      - "core/src/test/java/**/*.java"
      - "**/pom.xml"
    max_tokens: 50000

  aem-reviewer:
    model: claude-3-opus
    specialization: code-review
    tools:
      - read
      - grep
    context:
      - "course/02-bmad-phases/phase-04-development.md#code-review-guidelines"
    max_tokens: 50000

workflows:
  - name: implement-feature
    file: workflows/implement-feature.yaml
  - name: build-deploy
    file: workflows/build-deploy.yaml
  - name: full-cycle
    file: workflows/full-cycle.yaml

notifications:
  slack:
    webhook: "${SLACK_WEBHOOK_URL}"
    channel: "#aem-dev"
  email:
    smtp_host: "${SMTP_HOST}"
    from: "gastown@company.com"
    to:
      - "team@company.com"
```

### Step 2: Initialize GasTown

```bash
# Install GasTown CLI (hypothetical)
npm install -g @gastown/cli

# Initialize in project
gastown init

# Validate configuration
gastown validate

# List available agents
gastown agents list
```

### Step 3: Run a Workflow

```bash
# Start the implement-feature workflow for a BEAD task
gastown run implement-feature --task SAW-021

# Monitor progress
gastown status

# View agent logs
gastown logs aem-coder

# Pause workflow if needed
gastown pause
```

---

## Lab 6.2: Parallel Agent Development

### Scenario

Implement three workflow processes in parallel:
- AntivirusScanProcess
- QuarantineProcess
- AssetApprovalParticipantChooser

### Execution

```bash
# Create parallel tasks in BEAD
bd create task "Implement AntivirusScanProcess" --id SAW-021
bd create task "Implement QuarantineProcess" --id SAW-022
bd create task "Implement AssetApprovalParticipantChooser" --id SAW-023

# Run parallel implementation workflow
gastown run implement-feature \
  --tasks SAW-021,SAW-022,SAW-023 \
  --parallel

# Watch real-time progress
gastown watch

# Expected output:
# [10:00:01] Mayor: Starting parallel implementation for 3 tasks
# [10:00:02] AEM-Coder-1: Assigned SAW-021 (AntivirusScanProcess)
# [10:00:02] AEM-Coder-2: Assigned SAW-022 (QuarantineProcess)
# [10:00:02] AEM-Coder-3: Assigned SAW-023 (ParticipantChooser)
# [10:15:00] AEM-Coder-1: Completed SAW-021
# [10:15:01] AEM-Tester-1: Starting tests for SAW-021
# [10:18:00] AEM-Coder-2: Completed SAW-022
# ...
```

---

## Lab 6.3: Coordinating Review

### Multi-Agent Review Process

```bash
# After all implementations complete, run review
gastown run review-cycle \
  --tasks SAW-021,SAW-022,SAW-023

# Review workflow:
# 1. AEM-Reviewer analyzes each implementation
# 2. Findings aggregated by Mayor
# 3. Issues assigned back to AEM-Coder if needed
# 4. Re-review after fixes
# 5. Approval gate check
```

### Review Output

```yaml
review_summary:
  task: SAW-021
  agent: aem-reviewer
  timestamp: "2024-02-28T14:00:00Z"

  findings:
    critical: 0
    high: 0
    medium: 1
    low: 2
    info: 3

  details:
    - severity: medium
      file: AntivirusScanProcess.java
      line: 87
      message: "Consider adding timeout handling for workflow metadata write"
      suggestion: "Wrap metadata update in try-catch with timeout"

    - severity: low
      file: AntivirusScanProcess.java
      line: 45
      message: "Magic number 60000 should be a constant"
      suggestion: "Extract to SCAN_TIMEOUT_MS constant"

  recommendation: "approve_with_notes"
  approved: true
```

---

## Integration with BMAD and BEAD

### Complete Flow

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    BMAD-BEAD-GASTOWN INTEGRATION                           │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│   ┌─────────────┐                                                          │
│   │    PRD      │  Business Requirements                                   │
│   └──────┬──────┘                                                          │
│          │                                                                 │
│          ▼                                                                 │
│   ┌─────────────┐                                                          │
│   │    BMAD     │  Phase Planning & Architecture                           │
│   │   Phases    │                                                          │
│   └──────┬──────┘                                                          │
│          │                                                                 │
│          │  Generates tasks                                                │
│          ▼                                                                 │
│   ┌─────────────┐                                                          │
│   │    BEAD     │  Task Tracking & AI Memory                               │
│   │   Tasks     │                                                          │
│   └──────┬──────┘                                                          │
│          │                                                                 │
│          │  Tasks assigned to                                              │
│          ▼                                                                 │
│   ┌─────────────┐                                                          │
│   │  GASTOWN    │  Multi-Agent Orchestration                               │
│   │   Agents    │                                                          │
│   └──────┬──────┘                                                          │
│          │                                                                 │
│          │  Produces                                                       │
│          ▼                                                                 │
│   ┌─────────────┐                                                          │
│   │   CODE      │  Implemented, Tested, Reviewed                           │
│   │  ARTIFACTS  │                                                          │
│   └──────┬──────┘                                                          │
│          │                                                                 │
│          │  Updates                                                        │
│          ▼                                                                 │
│   ┌─────────────┐                                                          │
│   │    BEAD     │  Completion Status & Artifacts                           │
│   │   Status    │                                                          │
│   └─────────────┘                                                          │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## Next Steps

1. Create `gastown.yaml` configuration
2. Define agent prompts in `agents/` directory
3. Create workflow definitions in `workflows/` directory
4. Run the implementation workflow
5. Move to hands-on labs for practice
