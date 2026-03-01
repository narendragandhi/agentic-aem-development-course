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
| **AEM Spec Writer** | TDD Specification Tests | Write, Read (for TDAD) |

---

## Workflow Definitions

### Workflow: Implement Feature (Traditional)

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

### Workflow: TDD Implementation (TDAD)

This workflow follows Test-Driven Agentic Development - tests are written FIRST, then AI implements to pass them:

```yaml
workflow: implement-feature-tdd
trigger: bead.task.methodology == "TDAD"
agents:
  - aem-spec-writer-agent   # Writes specification tests
  - aem-coder-agent         # Implements to pass tests
  - aem-reviewer-agent      # Reviews implementation

steps:
  # ═══════════════════════════════════════════════════════════════════
  # PHASE 1: RED - Write Failing Tests First
  # ═══════════════════════════════════════════════════════════════════

  - name: write-specification-tests
    agent: aem-spec-writer-agent
    input:
      - bead.task.description
      - bead.task.acceptance_criteria
      - context.user_story
    output:
      - spec_test_file.java
    bead_update:
      tdd.phase: RED
      tdd.spec_file: spec_test_file.java

  - name: verify-tests-fail
    agent: aem-tester-agent
    depends_on: write-specification-tests
    command: "mvn test -Dtest=*Spec"
    gate:
      # Tests MUST fail initially (RED phase)
      condition: "test_results.failures > 0"
      on_fail: "error: Tests should fail in RED phase"
    output:
      - initial_test_count
    bead_update:
      tdd.test_status.total: initial_test_count
      tdd.test_status.passing: 0

  # ═══════════════════════════════════════════════════════════════════
  # PHASE 2: GREEN - Implement to Pass Tests
  # ═══════════════════════════════════════════════════════════════════

  - name: implement-to-pass-tests
    agent: aem-coder-agent
    depends_on: verify-tests-fail
    input:
      - spec_test_file.java        # Tests define requirements
      - context.existing_patterns   # Follow project patterns
    instructions: |
      Read the specification tests carefully.
      Implement MINIMUM code to make each test pass.
      Do NOT modify the test file.
      Run tests after each significant change.
    output:
      - implementation_file.java
    bead_update:
      tdd.phase: GREEN
      status: in_progress

  - name: run-tests-iteratively
    agent: aem-tester-agent
    depends_on: implement-to-pass-tests
    command: "mvn test -Dtest=*Spec"
    loop:
      max_iterations: 10
      until: "test_results.failures == 0"
      on_fail: "return to implement-to-pass-tests"
    output:
      - test_results.xml
    bead_update:
      tdd.test_status.passing: test_results.passed
      tdd.iterations: loop.count

  - name: verify-all-green
    agent: mayor
    depends_on: run-tests-iteratively
    gate:
      condition: "test_results.failures == 0"
      on_fail: "escalate: Unable to pass all tests"
    bead_update:
      tdd.phase: GREEN_COMPLETE

  # ═══════════════════════════════════════════════════════════════════
  # PHASE 3: REFACTOR - Improve Code Quality
  # ═══════════════════════════════════════════════════════════════════

  - name: refactor-implementation
    agent: aem-coder-agent
    depends_on: verify-all-green
    input:
      - implementation_file.java
      - spec_test_file.java
    instructions: |
      All tests are passing. Now improve code quality:
      - Extract helper methods
      - Add logging
      - Improve variable names
      - Add JavaDoc
      CONSTRAINT: All tests must still pass after refactoring.
    output:
      - refactored_file.java
    bead_update:
      tdd.phase: REFACTOR

  - name: verify-tests-still-pass
    agent: aem-tester-agent
    depends_on: refactor-implementation
    command: "mvn test -Dtest=*Spec"
    gate:
      condition: "test_results.failures == 0"
      on_fail: "return to refactor-implementation with: Refactoring broke tests"

  # ═══════════════════════════════════════════════════════════════════
  # COMPLETION
  # ═══════════════════════════════════════════════════════════════════

  - name: code-review
    agent: aem-reviewer-agent
    depends_on: verify-tests-still-pass
    input:
      - refactored_file.java
      - spec_test_file.java
    output:
      - review_findings.md

  - name: complete-tdd-task
    agent: mayor
    depends_on: code-review
    action:
      - bead.task.status = completed
      - bead.task.tdd.phase = DONE
      - bead.task.artifacts = [spec_test_file, refactored_file]
      - notify.stakeholders
```

### TDD vs Traditional Workflow Comparison

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WORKFLOW COMPARISON                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   TRADITIONAL                          TDD (TDAD)                           │
│   ──────────────                       ──────────────                       │
│                                                                             │
│   ┌─────────────┐                      ┌─────────────┐                     │
│   │ Analyze     │                      │ Write Tests │ ◀── Tests First!    │
│   │ Requirements│                      │ (Spec)      │                     │
│   └──────┬──────┘                      └──────┬──────┘                     │
│          │                                    │                             │
│          ▼                                    ▼                             │
│   ┌─────────────┐                      ┌─────────────┐                     │
│   │ Implement   │                      │ Verify RED  │ ◀── Tests fail      │
│   │ Code        │                      │ (All fail)  │                     │
│   └──────┬──────┘                      └──────┬──────┘                     │
│          │                                    │                             │
│          ▼                                    ▼                             │
│   ┌─────────────┐                      ┌─────────────┐                     │
│   │ Generate    │ ◀── Tests After      │ Implement   │ ◀── To pass tests   │
│   │ Tests       │                      │ Code        │                     │
│   └──────┬──────┘                      └──────┬──────┘                     │
│          │                                    │                             │
│          ▼                                    ▼                             │
│   ┌─────────────┐                      ┌─────────────┐                     │
│   │ Run Tests   │                      │ Verify GREEN│ ◀── All pass        │
│   │             │                      │             │                     │
│   └──────┬──────┘                      └──────┬──────┘                     │
│          │                                    │                             │
│          ▼                                    ▼                             │
│   ┌─────────────┐                      ┌─────────────┐                     │
│   │ Review      │                      │ Refactor    │ ◀── Improve quality │
│   │             │                      │             │                     │
│   └─────────────┘                      └──────┬──────┘                     │
│                                               │                             │
│                                               ▼                             │
│                                        ┌─────────────┐                     │
│                                        │ Review      │                     │
│                                        │             │                     │
│                                        └─────────────┘                     │
│                                                                             │
│   Advantage: Faster initial          Advantage: Better quality,            │
│   development                         tests as documentation               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
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

### Execution (Traditional)

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

### Execution (TDD Mode)

```bash
# Create TDD tasks with specification tests
bd create task "Implement AntivirusScanProcess" \
  --id SAW-021 \
  --methodology TDAD \
  --spec-test "core/src/test/java/.../AntivirusScanProcessSpec.java"

bd create task "Implement QuarantineProcess" \
  --id SAW-022 \
  --methodology TDAD \
  --spec-test "core/src/test/java/.../QuarantineProcessSpec.java"

bd create task "Implement DynamicApproverAssigner" \
  --id SAW-023 \
  --methodology TDAD \
  --spec-test "core/src/test/java/.../DynamicApproverAssignerSpec.java"

# Run parallel TDD workflow
gastown run implement-feature-tdd \
  --tasks SAW-021,SAW-022,SAW-023 \
  --parallel

# Watch TDD progress
gastown watch --show-tdd-phase

# Expected output:
# [10:00:01] Mayor: Starting parallel TDD implementation for 3 tasks
# [10:00:02] AEM-Spec-1: Writing tests for SAW-021 (AntivirusScanProcess)
# [10:00:02] AEM-Spec-2: Writing tests for SAW-022 (QuarantineProcess)
# [10:00:02] AEM-Spec-3: Writing tests for SAW-023 (DynamicApproverAssigner)
# [10:05:00] SAW-021: Phase RED - 6 tests written, 0 passing
# [10:05:01] SAW-022: Phase RED - 7 tests written, 0 passing
# [10:05:02] SAW-023: Phase RED - 5 tests written, 0 passing
# [10:05:03] AEM-Coder-1: Implementing SAW-021 to pass tests
# [10:10:00] SAW-021: Phase GREEN - 3/6 passing
# [10:15:00] SAW-021: Phase GREEN - 6/6 passing ✓
# [10:15:01] AEM-Coder-1: Refactoring SAW-021
# [10:18:00] SAW-021: Phase REFACTOR complete, all tests pass ✓
# [10:20:00] SAW-022: Phase GREEN - 7/7 passing ✓
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

---

## TDD Agent Configuration

Add the spec writer agent to `gastown.yaml`:

```yaml
agents:
  # ... existing agents ...

  aem-spec-writer:
    model: claude-3-opus
    specialization: tdd-specification-tests
    tools:
      - read
      - write
    context:
      - "core/src/test/java/**/*Spec.java"
      - "course/10-tdd-integration/*.md"
    instructions: |
      You write JUnit 5 specification tests that define expected behavior.
      Tests are written BEFORE implementation exists.
      Use @DisplayName for clear test descriptions.
      Use @Nested for logical grouping.
      Follow Given-When-Then pattern in test methods.
      Tests should be comprehensive but focused.
    max_tokens: 50000

workflows:
  - name: implement-feature
    file: workflows/implement-feature.yaml
  - name: implement-feature-tdd       # NEW: TDD workflow
    file: workflows/implement-feature-tdd.yaml
  - name: build-deploy
    file: workflows/build-deploy.yaml
```

---

## Benefits of TDD in GasTown

| Benefit | Description |
|---------|-------------|
| **Deterministic Validation** | Tests provide clear pass/fail criteria for AI |
| **Parallel Safety** | Multiple agents can implement independently with test isolation |
| **Progress Tracking** | Test counts show exact progress (3/7 passing) |
| **Quality Gates** | GREEN phase must complete before REFACTOR |
| **Reduced Review Burden** | Tests validate before human review |

---

## Next Steps

1. Create `gastown.yaml` configuration
2. Define agent prompts in `agents/` directory
3. Create workflow definitions in `workflows/` directory
4. **NEW**: Add TDD workflow for test-driven tasks
5. Run the implementation workflow
6. Move to hands-on labs for practice
