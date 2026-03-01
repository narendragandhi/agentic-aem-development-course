# Lab 2: PRD Creation with AI
# Writing AI-Optimized Requirements

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 60 minutes |
| Difficulty | Beginner-Intermediate |
| Prerequisites | Lab 1 completed |
| Outcome | PRD for a new workflow feature |

## Learning Objectives

By the end of this lab, you will:
- [ ] Understand the structure of AI-optimized PRDs
- [ ] Write user stories that AI agents can decompose
- [ ] Create testable acceptance criteria
- [ ] Use AI to validate and improve requirements

---

## Exercise 2.1: Analyze the Sample PRD (15 min)

### Step 1: Open the Sample PRD

```bash
# Open the PRD in your editor
code course/01-prd/secure-asset-workflow-prd.md

# Or view in terminal
cat course/01-prd/secure-asset-workflow-prd.md | head -100
```

### Step 2: Identify Key Sections

Review and annotate these sections:

| Section | Purpose | AI Usage |
|---------|---------|----------|
| Executive Summary | Problem/Solution overview | Context setting |
| User Stories | Functional requirements | Task decomposition |
| Acceptance Criteria | Testable conditions | Validation rules |
| Non-Functional Requirements | Quality attributes | Architecture decisions |
| Technical Architecture | System design | Implementation guidance |

### Step 3: Evaluate AI-Friendliness

Ask your AI assistant to evaluate:

```
Review this PRD section for AI agent consumption:

[Paste a user story from the PRD]

Evaluate:
1. Is the acceptance criteria specific enough for automated testing?
2. Can this be decomposed into implementable tasks?
3. What ambiguities need clarification?
4. Suggest improvements for AI processing.
```

#### Checkpoint 2.1
- [ ] Reviewed all PRD sections
- [ ] Identified 3+ areas for improvement
- [ ] Understand the structure

---

## Exercise 2.2: Write a New Feature PRD (25 min)

### Scenario

Your team needs to add a **Metadata Extraction** feature to the workflow:
- Extract EXIF data from images
- Extract document properties from PDFs
- Auto-tag assets based on content
- Store metadata for search and filtering

### Step 1: Create the Epic

Using AI assistance, create the epic:

```
I need to write a PRD epic for "Automated Metadata Extraction" in AEM DAM.

Context:
- Part of the Secure Asset Approval Workflow
- Runs after antivirus scan, before approval
- Should extract EXIF from images, properties from PDFs
- Should suggest tags based on AI analysis

Generate:
1. Problem statement
2. Proposed solution (2-3 sentences)
3. Success metrics (3-4 measurable goals)
4. Scope (in scope / out of scope)
```

### Step 2: Write User Stories

Create 3-4 user stories following the template:

```
Help me write user stories for metadata extraction.

User Story Template:
**As a** [role]
**I want** [capability]
**So that** [benefit]

Roles to consider:
- Content author (uploads assets)
- Content reviewer (approves assets)
- Marketing analyst (searches for assets)
- System administrator (configures extraction)

Generate 4 user stories with this exact format.
```

### Step 3: Define Acceptance Criteria

For each user story, create acceptance criteria:

```
For this user story:
"As a content author, I want image EXIF data automatically extracted
so that I don't have to manually enter camera settings and dates."

Generate acceptance criteria that are:
1. Specific and measurable
2. Testable by automation
3. Written in Given/When/Then format where appropriate

Include both happy path and error scenarios.
```

**Expected Output:**

```markdown
#### AC-1: EXIF Data Extraction
- [ ] AC-1.1: When a JPEG/PNG/TIFF is uploaded, EXIF data is extracted within 30 seconds
- [ ] AC-1.2: Extracted fields include: Camera Make, Model, Date Taken, GPS coordinates
- [ ] AC-1.3: Extracted metadata is stored in dam:exif/* properties
- [ ] AC-1.4: If EXIF extraction fails, workflow continues with warning logged
- [ ] AC-1.5: Original EXIF data is preserved (not modified)
```

### Step 4: Create Your PRD Document

Create a new file:

```bash
touch course/01-prd/metadata-extraction-prd.md
```

Structure your PRD:

```markdown
# Feature PRD: Automated Metadata Extraction

## Executive Summary
[Your problem statement and solution]

## User Stories

### US-1: EXIF Data Extraction
[Your user story and acceptance criteria]

### US-2: PDF Property Extraction
[Your user story and acceptance criteria]

### US-3: AI-Based Auto-Tagging
[Your user story and acceptance criteria]

## Non-Functional Requirements
[Performance, reliability, etc.]

## Technical Considerations
[Integration points, dependencies]
```

#### Checkpoint 2.2
- [ ] Created epic with clear scope
- [ ] Wrote 3-4 user stories
- [ ] Each story has 4+ acceptance criteria
- [ ] Saved PRD document

---

## Exercise 2.3: AI-Assisted PRD Validation (15 min)

### Step 1: Completeness Check

Ask AI to validate your PRD:

```
Review this PRD for completeness:

[Paste your PRD]

Check for:
1. Missing user roles or perspectives
2. Gaps in acceptance criteria
3. Undefined edge cases
4. Missing non-functional requirements
5. Unclear technical dependencies

Provide specific suggestions for each gap found.
```

### Step 2: Testability Review

```
Review the acceptance criteria in this PRD for testability:

[Paste your acceptance criteria]

For each criterion, answer:
1. Can this be tested automatically?
2. Is the expected result clearly defined?
3. Are there measurable thresholds?
4. What test data would be needed?

Suggest improvements where needed.
```

### Step 3: Decomposition Preview

```
Given this user story and acceptance criteria, show how it would
decompose into implementation tasks:

[Paste one user story]

Generate a task breakdown showing:
1. Backend tasks (Java/OSGi)
2. Configuration tasks
3. Testing tasks
4. Documentation tasks

Include estimated complexity for each task.
```

### Step 4: Refine and Finalize

Based on AI feedback:
1. Fill in any gaps identified
2. Improve unclear acceptance criteria
3. Add missing edge cases
4. Update technical considerations

#### Checkpoint 2.3
- [ ] PRD validated by AI
- [ ] Gaps addressed
- [ ] All acceptance criteria testable
- [ ] Ready for implementation

---

## Exercise 2.4: PRD-to-BEAD Translation (5 min)

### Preview: Creating BEAD Tasks

```
Convert this user story into BEAD task format:

[Paste one user story]

Generate YAML for:
1. Feature-level task
2. Implementation tasks (2-3)
3. Testing task
4. Documentation task

Use this format:
---
id: "MDE-001"
type: "feature"
title: "..."
parent: "SAW-001"
status: "pending"
description: |
  ...
acceptance_criteria:
  - "..."
subtasks:
  - id: "MDE-001-1"
    title: "..."
---
```

This preview shows how PRD requirements flow into BEAD tasks.

---

## Lab Deliverables

Submit the following:

1. **metadata-extraction-prd.md** - Your completed PRD
2. **prd-review-notes.md** - AI feedback and your responses
3. **task-preview.yaml** - BEAD task breakdown for one user story

---

## Evaluation Criteria

| Criteria | Points | Description |
|----------|--------|-------------|
| User Story Format | 20 | Follows As a/I want/So that |
| Acceptance Criteria | 30 | Specific, testable, complete |
| AI Validation | 20 | Addressed AI feedback |
| Technical Clarity | 15 | Clear dependencies and constraints |
| BEAD Preview | 15 | Correct task decomposition |

---

## Lab Completion Checklist

- [ ] Analyzed sample PRD structure
- [ ] Created new feature PRD with 3+ user stories
- [ ] Each story has testable acceptance criteria
- [ ] AI validation completed and addressed
- [ ] Task decomposition preview created

---

## Next Lab

Proceed to [Lab 3: Architecture Design](../lab-03-architecture/README.md)
