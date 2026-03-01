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

## Next Steps

1. Review the task files in `.issues/`
2. Complete Lab 5.1 to create your own tasks
3. Practice updating task status
4. Move to GasTown for multi-agent orchestration
