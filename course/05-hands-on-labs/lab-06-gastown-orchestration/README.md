# Lab 6: GasTown Multi-Agent Orchestration
# Coordinating AI Agent Teams

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 60 minutes |
| Difficulty | Advanced |
| Prerequisites | Labs 1-5 completed |
| Outcome | Configured multi-agent workflow |

## Learning Objectives

By the end of this lab, you will:
- [ ] Configure GasTown orchestration
- [ ] Define specialized AI agents
- [ ] Create multi-agent workflows
- [ ] Run parallel agent execution

---

## Exercise 6.1: Configure GasTown (15 min)

### Step 1: Create GasTown Configuration

Create `gastown.yaml` in project root:

```yaml
# GasTown Configuration
# Multi-Agent Orchestration for AEM Workflow Development

version: "1.0"

project:
  name: secure-asset-workflow
  repository: aem-workflow-demo
  bead_path: .bead/.issues

# ============================================
# Mayor AI Configuration (Orchestrator)
# ============================================
mayor:
  model: claude-3-opus
  role: orchestrator
  description: |
    Coordinates all specialized agents, assigns tasks,
    monitors progress, and ensures quality gates are met.

  capabilities:
    - task_assignment
    - progress_monitoring
    - conflict_resolution
    - quality_gating
    - result_aggregation

  settings:
    max_parallel_agents: 4
    session_timeout: 3600
    checkpoint_interval: 300
    retry_failed_tasks: 2

# ============================================
# Specialized Agents
# ============================================
agents:
  aem-coder:
    model: claude-3-opus
    role: developer
    description: |
      Expert AEM Java developer specializing in OSGi services,
      workflow processes, and Sling Models.

    skills:
      - java
      - osgi
      - sling
      - workflow
      - htl

    tools:
      - read
      - write
      - edit
      - bash

    context_files:
      - "core/src/main/java/**/*.java"
      - "course/02-bmad-phases/phase-03-architecture.md"
      - "course/02-bmad-phases/phase-04-development.md"

    task_types:
      - task
      - subtask
      - bug

    max_tokens: 100000

  aem-tester:
    model: claude-3-sonnet
    role: tester
    description: |
      Creates and executes unit tests, integration tests,
      and end-to-end workflow tests.

    skills:
      - junit5
      - mockito
      - aem-testing
      - integration-testing

    tools:
      - read
      - write
      - bash

    context_files:
      - "core/src/test/java/**/*.java"
      - "**/pom.xml"

    task_types:
      - test-task

    max_tokens: 50000

  aem-reviewer:
    model: claude-3-opus
    role: reviewer
    description: |
      Reviews code for quality, security, AEM best practices,
      and performance issues.

    skills:
      - code-review
      - security-review
      - aem-best-practices

    tools:
      - read
      - grep

    context_files:
      - "course/02-bmad-phases/phase-04-development.md#code-review-guidelines"

    task_types:
      - review

    max_tokens: 50000

  aem-docs:
    model: claude-3-haiku
    role: documentation
    description: |
      Generates JavaDoc, README updates, and technical documentation.

    skills:
      - javadoc
      - markdown
      - technical-writing

    tools:
      - read
      - write

    task_types:
      - docs-task

    max_tokens: 30000

# ============================================
# Quality Gates
# ============================================
quality_gates:
  code_complete:
    conditions:
      - "all_acceptance_criteria_met"
      - "no_compilation_errors"
    actions:
      - trigger: "aem-tester"
        task: "generate-tests"

  tests_pass:
    conditions:
      - "unit_tests_pass"
      - "coverage >= 80%"
    actions:
      - trigger: "aem-reviewer"
        task: "code-review"

  review_approved:
    conditions:
      - "no_critical_findings"
      - "no_high_findings OR high_findings_addressed"
    actions:
      - update_bead: "status=completed"
      - notify: "stakeholders"

# ============================================
# Workflows
# ============================================
workflows:
  implement-feature:
    file: ".gastown/workflows/implement-feature.yaml"

  test-and-review:
    file: ".gastown/workflows/test-and-review.yaml"

  full-development-cycle:
    file: ".gastown/workflows/full-cycle.yaml"

# ============================================
# Notifications
# ============================================
notifications:
  channels:
    slack:
      enabled: true
      webhook: "${SLACK_WEBHOOK_URL}"
      events:
        - task_completed
        - task_blocked
        - review_required

    email:
      enabled: false
      smtp_host: "${SMTP_HOST}"
```

### Step 2: Create Workflows Directory

```bash
mkdir -p .gastown/workflows
```

#### Checkpoint 6.1
- [ ] gastown.yaml created
- [ ] Workflows directory created
- [ ] Configuration validated

---

## Exercise 6.2: Define Workflows (20 min)

### Step 1: Create Implementation Workflow

Create `.gastown/workflows/implement-feature.yaml`:

```yaml
# Implementation Workflow
# Orchestrates feature implementation from task to completion

name: implement-feature
version: "1.0"
description: |
  Complete implementation workflow for a BEAD feature/task.
  Includes coding, testing, and review phases.

trigger:
  type: manual
  params:
    - name: task_id
      type: string
      required: true
      description: "BEAD task ID to implement"

# ============================================
# Workflow Steps
# ============================================
steps:
  # Step 1: Load Task Context
  - id: load-context
    name: "Load Task Context"
    agent: mayor
    action: load_bead_task
    params:
      task_id: "${trigger.task_id}"
    outputs:
      - task_data
      - dependencies
      - context_files

  # Step 2: Verify Dependencies
  - id: check-dependencies
    name: "Check Dependencies"
    agent: mayor
    action: verify_dependencies
    depends_on: [load-context]
    params:
      dependencies: "${load-context.dependencies}"
    gate:
      condition: "all_dependencies_completed"
      on_fail:
        action: block_task
        message: "Dependencies not met"

  # Step 3: Assign to Coder
  - id: implement
    name: "Implement Code"
    agent: aem-coder
    depends_on: [check-dependencies]
    action: implement_task
    params:
      task: "${load-context.task_data}"
      context: "${load-context.context_files}"
    outputs:
      - source_files
      - implementation_notes
    bead_update:
      status: in_progress
      notes: "Implementation started by aem-coder"

  # Step 4: Verify Build
  - id: verify-build
    name: "Verify Build"
    agent: aem-coder
    depends_on: [implement]
    action: run_command
    params:
      command: "mvn compile -pl core -q"
    gate:
      condition: "exit_code == 0"
      on_fail:
        action: return_to
        step: implement
        message: "Build failed, fix errors"

  # Step 5: Generate Tests
  - id: generate-tests
    name: "Generate Unit Tests"
    agent: aem-tester
    depends_on: [verify-build]
    action: generate_tests
    params:
      source_files: "${implement.source_files}"
      acceptance_criteria: "${load-context.task_data.acceptance_criteria}"
    outputs:
      - test_files

  # Step 6: Run Tests
  - id: run-tests
    name: "Run Unit Tests"
    agent: aem-tester
    depends_on: [generate-tests]
    action: run_command
    params:
      command: "mvn test -pl core"
    outputs:
      - test_results
    gate:
      condition: "test_results.failures == 0"
      on_fail:
        action: return_to
        step: implement
        message: "Tests failing: ${test_results.failure_messages}"

  # Step 7: Code Review
  - id: code-review
    name: "Code Review"
    agent: aem-reviewer
    depends_on: [run-tests]
    action: review_code
    params:
      files: "${implement.source_files} + ${generate-tests.test_files}"
    outputs:
      - review_findings
    gate:
      condition: "review_findings.blockers == 0"
      on_fail:
        action: return_to
        step: implement
        message: "Review blockers: ${review_findings.blockers}"

  # Step 8: Complete Task
  - id: complete
    name: "Complete Task"
    agent: mayor
    depends_on: [code-review]
    action: complete_task
    params:
      task_id: "${trigger.task_id}"
      artifacts:
        - "${implement.source_files}"
        - "${generate-tests.test_files}"
      review: "${code-review.review_findings}"
    bead_update:
      status: completed
      artifacts: "${artifacts}"

# ============================================
# Error Handling
# ============================================
error_handling:
  max_retries: 3
  retry_delay: 60  # seconds

  on_failure:
    action: notify
    channels: [slack]
    message: "Workflow failed for ${trigger.task_id}: ${error.message}"

# ============================================
# Completion
# ============================================
on_complete:
  - action: notify
    channels: [slack]
    message: "✅ Task ${trigger.task_id} completed successfully"

  - action: trigger_workflow
    workflow: "deploy-to-dev"
    condition: "all_feature_tasks_complete"
```

### Step 2: Create Parallel Execution Workflow

Create `.gastown/workflows/parallel-implementation.yaml`:

```yaml
# Parallel Implementation Workflow
# Runs multiple implementations concurrently

name: parallel-implementation
version: "1.0"

trigger:
  type: manual
  params:
    - name: task_ids
      type: array
      required: true

steps:
  - id: fan-out
    name: "Start Parallel Implementations"
    agent: mayor
    action: parallel_execute
    params:
      workflow: implement-feature
      items: "${trigger.task_ids}"
      max_parallel: 3

  - id: fan-in
    name: "Aggregate Results"
    agent: mayor
    depends_on: [fan-out]
    action: aggregate_results
    params:
      results: "${fan-out.results}"
    outputs:
      - summary
      - any_failures

  - id: report
    name: "Generate Report"
    agent: mayor
    depends_on: [fan-in]
    action: generate_report
    params:
      results: "${fan-in.summary}"
```

#### Checkpoint 6.2
- [ ] implement-feature workflow created
- [ ] parallel-implementation workflow created
- [ ] All steps defined with dependencies

---

## Exercise 6.3: Run Multi-Agent Workflow (20 min)

### Step 1: Simulate Workflow Execution

Since GasTown is a conceptual framework, we'll simulate the execution:

```bash
# Create a simulation script
cat > simulate-workflow.sh << 'EOF'
#!/bin/bash
echo "=== GasTown Workflow Simulation ==="
echo ""
echo "[$(date +%H:%M:%S)] Mayor: Starting implement-feature for SAW-021"
sleep 1
echo "[$(date +%H:%M:%S)] Mayor: Loading BEAD task context..."
sleep 1
echo "[$(date +%H:%M:%S)] Mayor: Dependencies verified (SAW-012 completed)"
sleep 1
echo "[$(date +%H:%M:%S)] Mayor: Assigning to aem-coder agent"
sleep 2
echo "[$(date +%H:%M:%S)] AEM-Coder: Received task SAW-021"
echo "[$(date +%H:%M:%S)] AEM-Coder: Reading architecture specification..."
sleep 2
echo "[$(date +%H:%M:%S)] AEM-Coder: Generating implementation..."
sleep 3
echo "[$(date +%H:%M:%S)] AEM-Coder: Created AntivirusScanProcess.java (145 lines)"
sleep 1
echo "[$(date +%H:%M:%S)] AEM-Coder: Running mvn compile..."
sleep 2
echo "[$(date +%H:%M:%S)] AEM-Coder: Build successful"
echo "[$(date +%H:%M:%S)] AEM-Coder: Implementation complete, handing off to tester"
sleep 1
echo "[$(date +%H:%M:%S)] Mayor: Triggering aem-tester for test generation"
sleep 1
echo "[$(date +%H:%M:%S)] AEM-Tester: Generating unit tests..."
sleep 3
echo "[$(date +%H:%M:%S)] AEM-Tester: Created AntivirusScanProcessTest.java (8 tests)"
echo "[$(date +%H:%M:%S)] AEM-Tester: Running tests..."
sleep 2
echo "[$(date +%H:%M:%S)] AEM-Tester: All 8 tests passing"
sleep 1
echo "[$(date +%H:%M:%S)] Mayor: Triggering aem-reviewer for code review"
sleep 1
echo "[$(date +%H:%M:%S)] AEM-Reviewer: Reviewing code..."
sleep 3
echo "[$(date +%H:%M:%S)] AEM-Reviewer: Review complete"
echo "[$(date +%H:%M:%S)] AEM-Reviewer: Findings: 0 critical, 0 high, 2 medium, 3 info"
echo "[$(date +%H:%M:%S)] AEM-Reviewer: Recommendation: APPROVED"
sleep 1
echo "[$(date +%H:%M:%S)] Mayor: All quality gates passed"
echo "[$(date +%H:%M:%S)] Mayor: Updating BEAD task SAW-021 to completed"
echo ""
echo "=== Workflow Complete ==="
EOF
chmod +x simulate-workflow.sh
./simulate-workflow.sh
```

### Step 2: Trace Agent Interactions

Document the agent interaction flow:

```markdown
## Workflow Trace: SAW-021

### Timeline

| Time | Agent | Action | Output |
|------|-------|--------|--------|
| 10:00 | Mayor | Load task | Context loaded |
| 10:01 | Mayor | Check deps | SAW-012 ✓ |
| 10:02 | AEM-Coder | Start impl | - |
| 10:15 | AEM-Coder | Complete impl | 145 lines |
| 10:16 | AEM-Coder | Build | Success |
| 10:17 | AEM-Tester | Gen tests | 8 tests |
| 10:20 | AEM-Tester | Run tests | 8/8 pass |
| 10:21 | AEM-Reviewer | Review | Approved |
| 10:25 | Mayor | Complete | BEAD updated |

### Agent Handoffs

```
Mayor
  │
  ├── assigns to ──▶ AEM-Coder
  │                      │
  │                      ├── produces code
  │                      │
  │                      └── triggers ──▶ AEM-Tester
  │                                           │
  │                                           ├── produces tests
  │                                           │
  │                                           └── triggers ──▶ AEM-Reviewer
  │                                                                │
  │                                                                └── approves
  │
  └── receives results ◀────────────────────────────────────────────┘
```
```

### Step 3: Review Agent Outputs

Each agent produces structured output:

```yaml
# AEM-Coder Output
agent: aem-coder
task: SAW-021
status: completed
duration_minutes: 13

artifacts:
  - path: core/src/main/java/.../AntivirusScanProcess.java
    lines: 145
    type: java-class

acceptance_criteria:
  - "Inject service" : met
  - "Get payload" : met
  - "Scan rendition" : met
  - "Set metadata" : met
  - "Throw exception" : met

notes:
  - "Used existing WatermarkProcess as pattern"
  - "Added comprehensive logging"
  - "Ready for testing"

handoff_to: aem-tester
```

#### Checkpoint 6.3
- [ ] Workflow simulation run
- [ ] Agent interactions traced
- [ ] Outputs documented

---

## Exercise 6.4: Parallel Execution (5 min)

### Simulate Parallel Implementation

```bash
# Parallel simulation
cat > simulate-parallel.sh << 'EOF'
#!/bin/bash
echo "=== Parallel Implementation ==="
echo ""
echo "[00:00] Mayor: Starting parallel workflow for 3 tasks"
echo "[00:01] Mayor: Spawning AEM-Coder-1 for SAW-021"
echo "[00:01] Mayor: Spawning AEM-Coder-2 for SAW-022"
echo "[00:01] Mayor: Spawning AEM-Coder-3 for SAW-023"
echo ""
echo "--- Running in parallel ---"
echo ""
echo "[00:15] AEM-Coder-1: SAW-021 complete"
echo "[00:18] AEM-Coder-2: SAW-022 complete"
echo "[00:12] AEM-Coder-3: SAW-023 complete"
echo ""
echo "[00:20] Mayor: All implementations complete"
echo "[00:21] Mayor: Spawning testers..."
echo ""
echo "=== Parallel Complete (20 min vs 45 min sequential) ==="
EOF
chmod +x simulate-parallel.sh
./simulate-parallel.sh
```

---

## Lab Deliverables

1. **gastown.yaml** - Complete orchestration config
2. **workflows/** - Implementation workflow definitions
3. **workflow-trace.md** - Documented execution trace

---

## Lab Completion Checklist

- [ ] GasTown configuration created
- [ ] 4 agents defined
- [ ] Implementation workflow created
- [ ] Parallel workflow created
- [ ] Simulation executed
- [ ] Agent interactions documented

---

## Next Lab

Proceed to [Lab 7: Testing](../lab-07-testing/README.md)
