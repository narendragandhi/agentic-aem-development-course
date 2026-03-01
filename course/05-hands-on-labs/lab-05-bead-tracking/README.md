# Lab 5: BEAD Task Management
# Persistent Memory for AI Agents

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 45 minutes |
| Difficulty | Intermediate |
| Prerequisites | Labs 1-4 completed |
| Outcome | BEAD-tracked development workflow |

## Learning Objectives

By the end of this lab, you will:
- [ ] Initialize BEAD for your project
- [ ] Create task hierarchies (Epic → Feature → Task)
- [ ] Track progress across AI sessions
- [ ] Manage task dependencies

---

## Exercise 5.1: Initialize BEAD (10 min)

### Step 1: Create BEAD Directory Structure

```bash
# Navigate to project root
cd ~/aem-workflow-demo

# Create BEAD directory structure
mkdir -p .bead/.issues
touch .bead/config.yaml
touch .bead/.issues/index.yaml
```

### Step 2: Configure BEAD

Create `.bead/config.yaml`:

```yaml
# BEAD Configuration
# Secure Asset Approval Workflow Project

project:
  name: secure-asset-workflow
  version: 1.0.0
  repository: aem-workflow-demo

settings:
  # ID generation
  id_prefix: "SAW"
  id_format: "{prefix}-{sequence:03d}"

  # Status values
  statuses:
    - pending
    - in_progress
    - blocked
    - review
    - completed
    - cancelled

  # Priority levels
  priorities:
    - critical
    - high
    - medium
    - low

  # Task types
  types:
    - epic
    - feature
    - task
    - subtask
    - bug
    - spike

agents:
  # Agent assignments
  aem-coder:
    types: [task, subtask]
    skills: [java, osgi, sling]
  aem-tester:
    types: [task]
    skills: [junit, testing]
  aem-reviewer:
    types: [review]
    skills: [code-review]

index:
  # Auto-update index on changes
  auto_update: true
  # Include completed tasks in index
  include_completed: false
```

### Step 3: Create Index File

Create `.bead/.issues/index.yaml`:

```yaml
# BEAD Task Index
# Auto-generated, manual edits may be overwritten

last_updated: "2024-02-28T00:00:00Z"
total_tasks: 0
by_status:
  pending: 0
  in_progress: 0
  completed: 0
by_type:
  epic: 0
  feature: 0
  task: 0

tasks: []
```

#### Checkpoint 5.1
- [ ] BEAD directory created
- [ ] Config file created
- [ ] Index initialized

---

## Exercise 5.2: Create Task Hierarchy (15 min)

### Step 1: Create the Epic

Create `.bead/.issues/saw-001.yaml`:

```yaml
id: "SAW-001"
type: epic
title: "Secure Asset Approval Workflow"
status: in_progress
priority: critical
created: "2024-02-28T09:00:00Z"
updated: "2024-02-28T09:00:00Z"

description: |
  Implement a comprehensive workflow for secure asset management
  including antivirus scanning and multi-level approval.

business_value: |
  - Prevent malware in DAM
  - Streamline approval process
  - Ensure compliance

success_metrics:
  - name: "Malware Detection"
    target: "100%"
  - name: "Approval Time"
    target: "<48 hours"

features:
  - id: "SAW-010"
    title: "Antivirus Integration"
  - id: "SAW-020"
    title: "Workflow Processes"
  - id: "SAW-030"
    title: "Workflow Model"
```

### Step 2: Create Features

Create `.bead/.issues/saw-010.yaml`:

```yaml
id: "SAW-010"
type: feature
title: "Antivirus Integration"
status: completed
priority: high
parent: "SAW-001"
created: "2024-02-28T09:00:00Z"
updated: "2024-02-28T14:00:00Z"

description: |
  Integrate antivirus scanning capability into the asset
  upload workflow.

acceptance_criteria:
  - "ClamAV integration via TCP"
  - "REST API fallback"
  - "Mock mode for testing"
  - "Configurable via OSGi"

tasks:
  - id: "SAW-011"
    title: "AntivirusScanService interface"
    status: completed
  - id: "SAW-012"
    title: "AntivirusScanServiceImpl"
    status: completed
  - id: "SAW-013"
    title: "Unit tests"
    status: completed
```

### Step 3: Create Tasks

Create `.bead/.issues/saw-021.yaml`:

```yaml
id: "SAW-021"
type: task
title: "Implement AntivirusScanProcess"
status: pending
priority: high
parent: "SAW-020"
assignee: aem-coder
created: "2024-02-28T10:00:00Z"
updated: "2024-02-28T10:00:00Z"

dependencies:
  - id: "SAW-012"
    type: requires
    status: completed

description: |
  Create a workflow process step that scans assets using
  the AntivirusScanService.

acceptance_criteria:
  - criteria: "Inject AntivirusScanService"
    status: pending
  - criteria: "Get asset from workflow payload"
    status: pending
  - criteria: "Scan original rendition"
    status: pending
  - criteria: "Set workflow metadata with results"
    status: pending
  - criteria: "Throw exception for infected files"
    status: pending

context:
  prd_ref: "US-1.1"
  arch_ref: "CS-02"
  files:
    - "core/src/main/java/com/demo/workflow/process/AntivirusScanProcess.java"

estimated_hours: 2
```

### Step 4: Update Index

Update `.bead/.issues/index.yaml`:

```yaml
last_updated: "2024-02-28T10:00:00Z"
total_tasks: 8
by_status:
  pending: 3
  in_progress: 0
  completed: 5
by_type:
  epic: 1
  feature: 3
  task: 4

tasks:
  - id: "SAW-001"
    title: "Secure Asset Approval Workflow"
    type: epic
    status: in_progress
  - id: "SAW-010"
    title: "Antivirus Integration"
    type: feature
    status: completed
    parent: "SAW-001"
  - id: "SAW-020"
    title: "Workflow Processes"
    type: feature
    status: in_progress
    parent: "SAW-001"
  - id: "SAW-021"
    title: "Implement AntivirusScanProcess"
    type: task
    status: pending
    parent: "SAW-020"
```

#### Checkpoint 5.2
- [ ] Epic created
- [ ] 2+ features created
- [ ] 3+ tasks created
- [ ] Index updated

---

## Exercise 5.3: Track Work Progress (15 min)

### Step 1: Start a Task

Simulate starting work on SAW-021:

```yaml
# Update saw-021.yaml

id: "SAW-021"
status: in_progress  # Changed from pending
updated: "2024-02-28T10:30:00Z"

progress:
  started_at: "2024-02-28T10:30:00Z"

session_log:
  - timestamp: "2024-02-28T10:30:00Z"
    agent: aem-coder
    action: started
    note: "Beginning implementation based on CS-02 specification"
```

### Step 2: Record Progress Updates

Add progress notes as work continues:

```yaml
session_log:
  - timestamp: "2024-02-28T10:30:00Z"
    agent: aem-coder
    action: started
    note: "Beginning implementation based on CS-02 specification"

  - timestamp: "2024-02-28T10:45:00Z"
    agent: aem-coder
    action: progress
    note: "Class structure created, @Component annotation added"

  - timestamp: "2024-02-28T11:00:00Z"
    agent: aem-coder
    action: progress
    note: "Implemented execute() method, scan logic complete"
    artifacts:
      - path: "core/src/main/java/.../AntivirusScanProcess.java"
        lines: 145

  - timestamp: "2024-02-28T11:15:00Z"
    agent: aem-coder
    action: progress
    note: "Added metadata handling and error cases"
```

### Step 3: Mark Acceptance Criteria

Update criteria as they're met:

```yaml
acceptance_criteria:
  - criteria: "Inject AntivirusScanService"
    status: met
    evidence: "@Reference annotation on line 42"
  - criteria: "Get asset from workflow payload"
    status: met
    evidence: "getPayload().toString() on line 58"
  - criteria: "Scan original rendition"
    status: met
    evidence: "asset.getOriginal() on line 72"
  - criteria: "Set workflow metadata with results"
    status: met
    evidence: "setWorkflowMetadata() lines 95-110"
  - criteria: "Throw exception for infected files"
    status: met
    evidence: "throw new WorkflowException line 88"
```

### Step 4: Complete the Task

```yaml
id: "SAW-021"
status: completed
updated: "2024-02-28T11:30:00Z"

progress:
  started_at: "2024-02-28T10:30:00Z"
  completed_at: "2024-02-28T11:30:00Z"
  estimated_hours: 2
  actual_hours: 1

artifacts:
  - path: "core/src/main/java/com/demo/workflow/process/AntivirusScanProcess.java"
    type: java-class
    lines: 145
    checksum: "sha256:abc123..."

review:
  status: pending
  assigned_to: aem-reviewer
```

#### Checkpoint 5.3
- [ ] Task started and tracked
- [ ] Progress notes added
- [ ] Acceptance criteria updated
- [ ] Task completed with artifacts

---

## Exercise 5.4: AI Agent Context Loading (5 min)

### Understanding Context Loading

When an AI agent starts a new session, it loads BEAD context:

```python
# Pseudo-code for agent session initialization

def load_bead_context():
    """Load relevant BEAD tasks for AI agent context."""

    # 1. Read index to find active tasks
    index = read_yaml(".bead/.issues/index.yaml")

    # 2. Filter to relevant tasks
    my_tasks = [t for t in index.tasks
                if t.status in ['pending', 'in_progress']
                and t.assignee == current_agent]

    # 3. Load full task details
    context = []
    for task in my_tasks:
        task_data = read_yaml(f".bead/.issues/{task.id}.yaml")
        context.append({
            'task': task_data,
            'parent': load_parent(task_data.parent),
            'dependencies': [load_task(d.id) for d in task_data.dependencies]
        })

    # 4. Add to agent context
    return format_for_agent(context)
```

### Verify Context Format

The AI agent receives context like:

```markdown
## Current Task: SAW-021

**Title:** Implement AntivirusScanProcess
**Status:** in_progress
**Priority:** high

### Description
Create a workflow process step that scans assets using
the AntivirusScanService.

### Acceptance Criteria
- [ ] Inject AntivirusScanService
- [ ] Get asset from workflow payload
- [ ] Scan original rendition
- [ ] Set workflow metadata with results
- [ ] Throw exception for infected files

### Dependencies
- SAW-012: AntivirusScanServiceImpl (completed) ✓

### Context Files
- Specification: course/02-bmad-phases/phase-03-architecture.md#cs-02
- Related: core/src/main/java/.../AntivirusScanService.java

### Previous Progress
- 10:30 - Started implementation
- 10:45 - Class structure created
```

---

## Lab Deliverables

Submit the following:

1. **`.bead/config.yaml`** - Your BEAD configuration
2. **`.bead/.issues/`** - Complete task hierarchy
3. **Task completion example** - One fully tracked task

---

## Lab Completion Checklist

- [ ] BEAD initialized with config
- [ ] Epic → Feature → Task hierarchy created
- [ ] Task progress tracked with session logs
- [ ] Acceptance criteria updated
- [ ] Artifacts recorded

---

## Next Lab

Proceed to [Lab 6: GasTown Orchestration](../lab-06-gastown-orchestration/README.md)
