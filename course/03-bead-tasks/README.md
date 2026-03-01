# BEAD Task Definitions
# Secure Asset Approval Workflow

## What is BEAD?

BEAD (Build, Execute, Analyze, Document) is a Git-backed issue tracking system designed specifically for AI agents. It provides:

- **Persistent Memory**: Context that survives across AI sessions
- **Dependency Tracking**: Task hierarchies and relationships
- **Progress Monitoring**: Real-time status updates
- **AI-Optimized Format**: Structured data for agent consumption

---

## Task Hierarchy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        BEAD TASK HIERARCHY                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   EPIC: Secure Asset Approval Workflow                                     │
│   └── FEATURE: Antivirus Integration                                       │
│       ├── TASK: Implement AntivirusScanService interface                   │
│       ├── TASK: Implement AntivirusScanServiceImpl                         │
│       │   ├── SUBTASK: ClamAV TCP integration                              │
│       │   ├── SUBTASK: REST API fallback                                   │
│       │   └── SUBTASK: Mock mode                                           │
│       ├── TASK: Write unit tests                                           │
│       └── TASK: Integration testing                                        │
│   └── FEATURE: Workflow Processes                                          │
│       ├── TASK: Implement AntivirusScanProcess                             │
│       ├── TASK: Implement QuarantineProcess                                │
│       └── TASK: Implement ParticipantChooser                               │
│   └── FEATURE: Workflow Model                                              │
│       ├── TASK: Create workflow model XML                                  │
│       ├── TASK: Create workflow launcher                                   │
│       └── TASK: OSGi configurations                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Task Files

Tasks are stored in `.issues/` directory, organized by hash prefix:

```
.issues/
├── a1/
│   └── a1b2c3d4.yaml    # Epic: Secure Asset Workflow
├── b2/
│   └── b2c3d4e5.yaml    # Feature: Antivirus Integration
├── c3/
│   ├── c3d4e5f6.yaml    # Task: AntivirusScanService interface
│   └── c3e5f6g7.yaml    # Task: AntivirusScanServiceImpl
└── index.yaml            # Task index for quick lookup
```

---

## Task Format

Each task follows a structured YAML format optimized for AI agent processing:

```yaml
id: "c3d4e5f6"
type: "task"
title: "Implement AntivirusScanService interface"
status: "in_progress"  # pending, in_progress, blocked, completed
priority: "high"       # critical, high, medium, low
assignee: "aem-coder-agent"

parent: "b2c3d4e5"     # Parent feature ID
dependencies:
  - id: "setup-project"
    status: "completed"

description: |
  Create the AntivirusScanService interface that defines
  the contract for antivirus scanning operations.

acceptance_criteria:
  - Interface defines scanFile() method
  - Interface defines scanAsset() method
  - Interface defines isAvailable() method
  - ScanResult inner class defined
  - JavaDoc documentation complete

artifacts:
  - type: "java-interface"
    path: "core/src/main/java/com/demo/workflow/services/AntivirusScanService.java"

context:
  prd_reference: "FR-1.1, FR-1.2"
  architecture_doc: "course/02-bmad-phases/phase-03-architecture.md#cs-01"
  related_tasks:
    - "c3e5f6g7"  # Implementation task

progress:
  started_at: "2024-02-28T10:00:00Z"
  updated_at: "2024-02-28T10:30:00Z"
  estimated_hours: 1
  actual_hours: 0.5

notes:
  - timestamp: "2024-02-28T10:30:00Z"
    agent: "aem-coder-agent"
    content: "Interface created with all methods. Ready for review."
```

---

## TDD-Enhanced BEAD Tasks (TDAD)

When using Test-Driven Agentic Development, BEAD tasks include test specifications that guide AI implementation:

```yaml
id: "c3d4e5f6"
type: "task"
title: "Implement AntivirusScanProcess"
methodology: "TDAD"  # Test-Driven Agentic Development
status: "pending"
priority: "high"
assignee: "aem-coder-agent"

# ═══════════════════════════════════════════════════════════════════
# TDD SPECIFICATION SECTION
# ═══════════════════════════════════════════════════════════════════

tdd:
  specification_test:
    file: "core/src/test/java/com/demo/workflow/process/AntivirusScanProcessSpec.java"
    test_count: 6
    test_sections:
      - name: "AssetProcessing"
        tests: ["shouldScanAssetBinary", "shouldStoreScanResultInMetadata"]
      - name: "MalwareDetection"
        tests: ["shouldMarkAsInfected", "shouldRouteToQuarantine"]
      - name: "ServiceUnavailable"
        tests: ["shouldFailWithClearError", "shouldSetErrorStatus"]

  test_status:
    total: 6
    passing: 0
    failing: 6
    last_run: null

  workflow:
    phase: "RED"  # RED, GREEN, REFACTOR
    iterations: 0

# ═══════════════════════════════════════════════════════════════════
# IMPLEMENTATION TARGET
# ═══════════════════════════════════════════════════════════════════

implementation:
  file: "core/src/main/java/com/demo/workflow/process/AntivirusScanProcess.java"
  interface: "com.adobe.granite.workflow.exec.WorkflowProcess"
  osgi_component: true

# ═══════════════════════════════════════════════════════════════════
# AI INSTRUCTIONS
# ═══════════════════════════════════════════════════════════════════

ai_instructions: |
  This task uses Test-Driven Development. Follow this workflow:

  1. RED PHASE: Read all specification tests
     - Understand what each test expects
     - Do NOT modify test files

  2. GREEN PHASE: Implement minimum code to pass tests
     - Run: mvn test -Dtest=AntivirusScanProcessSpec
     - Implement one test section at a time
     - Report progress after each section

  3. REFACTOR PHASE: Improve code quality
     - All tests must continue to pass
     - Extract helpers, add logging, improve names

acceptance_criteria:
  - command: "mvn test -Dtest=AntivirusScanProcessSpec"
    expected: "Tests run: 6, Failures: 0, Errors: 0"
  - All tests passing (GREEN)
  - Code review approved
  - No new warnings

# Standard BEAD fields continue...
progress:
  started_at: null
  tdd_iterations:
    - phase: "RED"
      timestamp: null
      tests_passing: 0
    - phase: "GREEN"
      timestamp: null
      tests_passing: 6
    - phase: "REFACTOR"
      timestamp: null
      tests_passing: 6
```

### TDD Task Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BEAD TASK WITH TDD LIFECYCLE                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Status: pending              Status: in_progress         Status: complete │
│   Phase: -                     Phase: RED→GREEN→REFACTOR   Phase: DONE      │
│                                                                             │
│   ┌──────────────┐            ┌──────────────────────┐    ┌──────────────┐ │
│   │ Task Created │───────────▶│ Tests Written First  │───▶│ All Tests    │ │
│   │              │            │ (Spec file exists)   │    │ Passing      │ │
│   └──────────────┘            └──────────────────────┘    └──────────────┘ │
│                                         │                                   │
│                                         ▼                                   │
│                               ┌──────────────────────┐                     │
│                               │   RED: 0/6 passing   │                     │
│                               │   AI reads specs     │                     │
│                               └──────────┬───────────┘                     │
│                                          │                                  │
│                                          ▼                                  │
│                               ┌──────────────────────┐                     │
│                               │  GREEN: Implement    │                     │
│                               │  Run tests after     │                     │
│                               │  each change         │                     │
│                               └──────────┬───────────┘                     │
│                                          │                                  │
│                                          ▼                                  │
│                               ┌──────────────────────┐                     │
│                               │ REFACTOR: Improve    │                     │
│                               │ Tests still pass     │                     │
│                               └──────────────────────┘                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Using BEAD in This Course

### Lab 5.1: Creating BEAD Tasks

1. **Initialize BEAD for your project**:
   ```bash
   bd init
   ```

2. **Create the epic**:
   ```bash
   bd create epic "Secure Asset Approval Workflow" \
     --description "Enterprise workflow with AV scanning and multi-level approval"
   ```

3. **Add features**:
   ```bash
   bd create feature "Antivirus Integration" --parent <epic-id>
   bd create feature "Workflow Processes" --parent <epic-id>
   bd create feature "Workflow Model" --parent <epic-id>
   ```

4. **Add tasks**:
   ```bash
   bd create task "Implement AntivirusScanService interface" \
     --parent <feature-id> \
     --priority high \
     --assignee aem-coder-agent
   ```

### Lab 5.2: Tracking Progress

1. **Start working on a task**:
   ```bash
   bd start <task-id>
   ```

2. **Update progress**:
   ```bash
   bd update <task-id> --note "ClamAV integration complete"
   ```

3. **Mark dependencies**:
   ```bash
   bd depends <task-id> --on <other-task-id>
   ```

4. **Complete a task**:
   ```bash
   bd complete <task-id> --artifact "path/to/file.java"
   ```

### Lab 5.3: AI Agent Context Loading

When an AI agent starts a session, it loads BEAD context:

```python
# Pseudo-code for AI agent session start
def load_bead_context():
    tasks = bd.list(status=["in_progress", "pending"])
    for task in tasks:
        context.add(task.description)
        context.add(task.acceptance_criteria)
        context.add(task.related_artifacts)
    return context
```

---

## Pre-Defined Tasks for This Course

See the `.issues/` directory for complete task definitions:

| ID | Title | Type | Status |
|----|-------|------|--------|
| SAW-001 | Secure Asset Approval Workflow | Epic | In Progress |
| SAW-010 | Antivirus Integration | Feature | In Progress |
| SAW-011 | Implement AntivirusScanService | Task | Completed |
| SAW-012 | Implement AntivirusScanServiceImpl | Task | Completed |
| SAW-013 | ClamAV TCP Integration | Subtask | Completed |
| SAW-014 | REST API Fallback | Subtask | Completed |
| SAW-015 | Mock Mode | Subtask | Completed |
| SAW-016 | Unit Tests | Task | Pending |
| SAW-020 | Workflow Processes | Feature | Pending |
| SAW-021 | AntivirusScanProcess | Task | Pending |
| SAW-022 | QuarantineProcess | Task | Pending |
| SAW-023 | ParticipantChooser | Task | Pending |
| SAW-030 | Workflow Model | Feature | Pending |
| SAW-031 | Create Workflow XML | Task | Pending |
| SAW-032 | Create Launcher | Task | Pending |
| SAW-033 | OSGi Configs | Task | Pending |

---

---

## BEAD Commands for TDD Tasks

### Creating a TDD Task

```bash
# Create task with TDD methodology
bd create task "Implement QuarantineProcess" \
  --methodology TDAD \
  --spec-test "core/src/test/java/.../QuarantineProcessSpec.java" \
  --test-count 7 \
  --parent SAW-020

# Initialize test status
bd tdd init SAW-022 --tests 7 --phase RED
```

### Updating TDD Progress

```bash
# Update test status after running tests
bd tdd update SAW-022 --passing 3 --failing 4

# Move to next phase
bd tdd phase SAW-022 --phase GREEN

# Record iteration
bd tdd iterate SAW-022 --note "AssetProcessing tests now passing"
```

### TDD Status Commands

```bash
# View TDD status for a task
bd tdd status SAW-022

# Output:
# Task: SAW-022 - Implement QuarantineProcess
# Methodology: TDAD
# Phase: GREEN
# Tests: 5/7 passing
# Iterations: 3
# Last Run: 2024-02-28T14:30:00Z

# List all TDD tasks
bd list --methodology TDAD

# Show tasks in RED phase (need implementation)
bd list --tdd-phase RED
```

---

## Integration with CI/CD

BEAD TDD tasks integrate with CI/CD pipelines:

```yaml
# .github/workflows/tdd-validation.yml
name: BEAD TDD Validation

on: [push, pull_request]

jobs:
  validate-tdd:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Run Specification Tests
        run: mvn test -Dtest="*Spec"

      - name: Update BEAD Task Status
        run: |
          # Parse test results and update BEAD
          bd tdd sync --from-surefire target/surefire-reports/

      - name: Fail if TDD Tasks Incomplete
        run: |
          # Ensure all TDD tasks are GREEN
          bd tdd verify --fail-on-red
```

---

## Next Steps

1. Review the task files in `.issues/`
2. Complete Lab 5.1 to create your own tasks
3. Practice updating task status
4. **NEW**: Create TDD tasks with specification tests
5. Move to GasTown for multi-agent orchestration
