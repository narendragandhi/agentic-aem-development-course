# Lab 3: Architecture Design with AI
# From Requirements to Technical Specifications

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 75 minutes |
| Difficulty | Intermediate |
| Prerequisites | Labs 1-2 completed |
| Outcome | Complete architecture specification |

## Learning Objectives

By the end of this lab, you will:
- [ ] Generate component specifications using AI
- [ ] Create architecture diagrams collaboratively with AI
- [ ] Define integration contracts
- [ ] Produce a deployment architecture

---

## Exercise 3.1: Component Specification Generation (25 min)

### Step 1: Gather Context

Before generating specifications, provide AI with context:

```
I'm designing the architecture for a Secure Asset Approval Workflow in AEM.

Project Context:
- AEM Cloud Service
- OSGi-based services
- Workflow processes using Granite Workflow API
- Integration with ClamAV for virus scanning

Requirements Summary:
- Scan all uploaded assets for malware
- Route clean assets through multi-level approval
- Quarantine infected files
- Dynamic reviewer assignment based on asset type
- Email notifications at each stage

Generate a high-level component diagram showing all services and their relationships.
```

### Step 2: Generate Service Specifications

For each major component, request a detailed specification:

```
Generate a detailed component specification for AntivirusScanService.

Include:
1. Interface Definition
   - All public methods with parameters and return types
   - Inner classes/DTOs
   - Exceptions thrown

2. Implementation Notes
   - OSGi service properties
   - Configuration schema (all config options)
   - Dependencies (other services needed)

3. Behavior Specification
   - For each method: preconditions, postconditions, invariants
   - Error handling strategy
   - Logging requirements

4. Integration Points
   - External systems (ClamAV)
   - Protocol details
   - Timeout/retry behavior

Format as a technical specification document.
```

### Step 3: Create Specification Document

Create the architecture document:

```bash
touch course/02-bmad-phases/component-specifications.md
```

Add specifications for:
- AntivirusScanService
- QuarantineService
- NotificationService
- AuditService

### Step 4: Review and Refine

Ask AI to review for completeness:

```
Review this component specification for gaps:

[Paste your specification]

Check for:
1. Missing error scenarios
2. Undefined behavior at boundaries
3. Missing configuration options
4. Security considerations
5. Performance implications

Suggest specific additions.
```

#### Checkpoint 3.1
- [ ] 4+ component specifications created
- [ ] All interfaces fully defined
- [ ] Configuration schemas documented
- [ ] Integration points specified

---

## Exercise 3.2: Architecture Diagrams (20 min)

### Step 1: System Context Diagram

```
Create an ASCII art system context diagram for the Secure Asset Approval Workflow.

Show:
- AEM Author instance (central)
- External actors: Content Author, Reviewers, Admins
- External systems: ClamAV, Email Server, AEM Publish
- Data flows between components

Use box drawing characters for clarity.
```

### Step 2: Component Diagram

```
Create a detailed component diagram showing:

Internal Components:
- AntivirusScanService
- AntivirusScanProcess
- QuarantineProcess
- AssetApprovalParticipantChooser
- NotificationProcess
- ReplicationProcess

Show:
- Service dependencies (@Reference)
- Workflow process relationships
- Configuration dependencies

Format as ASCII art that can be embedded in code comments.
```

### Step 3: Sequence Diagram

```
Create an ASCII sequence diagram for the "Clean File Approval" flow:

Actors/Components:
1. Content Author
2. DAM Upload Listener
3. Workflow Launcher
4. AntivirusScanProcess
5. AntivirusScanService
6. ClamAV
7. Level1ReviewerInbox
8. Reviewer
9. ReplicationProcess
10. AEM Publish

Show the complete message flow from upload to publication.
```

### Step 4: Save Diagrams

Add diagrams to your architecture document with explanations.

#### Checkpoint 3.2
- [ ] System context diagram created
- [ ] Component diagram created
- [ ] Sequence diagram created
- [ ] All diagrams documented

---

## Exercise 3.3: Integration Contracts (15 min)

### Step 1: ClamAV Integration Contract

```
Define the integration contract for ClamAV communication.

Include:
1. Protocol Specification
   - Connection details (TCP socket)
   - Command format (INSTREAM)
   - Request/Response format
   - Byte-level protocol details

2. Error Handling
   - Connection failures
   - Timeout scenarios
   - Invalid responses
   - Recovery procedures

3. Configuration
   - Required settings
   - Optional settings
   - Default values

4. Testing Strategy
   - How to test without ClamAV
   - Mock responses
   - EICAR test file usage

Format as an API contract document.
```

### Step 2: Notification Contract

```
Define the notification service contract.

Include:
1. Notification Types
   - Approval request
   - Rejection notice
   - Quarantine alert
   - Publication success

2. For each type:
   - Trigger conditions
   - Recipients (how determined)
   - Message template variables
   - Delivery channels (email, Slack)

3. Configuration
   - Template locations
   - Channel settings
   - Retry policy

Format as a contract specification.
```

### Step 3: Document Contracts

Create integration documentation:

```bash
touch course/02-bmad-phases/integration-contracts.md
```

#### Checkpoint 3.3
- [ ] ClamAV contract defined
- [ ] Notification contract defined
- [ ] Error handling documented
- [ ] Testing approach specified

---

## Exercise 3.4: Deployment Architecture (15 min)

### Step 1: Environment Specification

```
Design the deployment architecture for the Secure Asset Approval Workflow.

Environments:
1. Local Development
   - AEM SDK
   - Docker ClamAV
   - Mock email

2. Development (Cloud)
   - AEM as Cloud Service - Dev
   - Managed ClamAV service
   - Development SMTP

3. Stage
   - AEM as Cloud Service - Stage
   - Production-like ClamAV
   - Stage SMTP

4. Production
   - AEM as Cloud Service - Prod
   - High-availability ClamAV cluster
   - Production email service

For each environment, specify:
- Infrastructure components
- Configuration differences
- Security requirements
- Monitoring needs
```

### Step 2: Configuration Matrix

```
Create a configuration matrix showing how settings differ by environment:

| Setting | Local | Dev | Stage | Prod |
|---------|-------|-----|-------|------|
| scanEngine | MOCK | CLAMAV | CLAMAV | CLAMAV |
| clamavHost | localhost | clamav-dev.internal | ... | ... |
| enabled | true | true | true | true |
| ... | ... | ... | ... | ... |

Include all configurable settings for all components.
```

### Step 3: CI/CD Pipeline Design

```
Design the Cloud Manager pipeline for this workflow:

Pipeline Stages:
1. Build
   - Maven build
   - Unit tests
   - Code quality checks

2. Deploy to Dev
   - Package installation
   - Smoke tests

3. Integration Tests
   - Workflow end-to-end tests
   - ClamAV integration tests

4. Deploy to Stage
   - UAT preparation
   - Performance baseline

5. Production Deployment
   - Blue-green deployment
   - Health checks
   - Rollback triggers

Show the pipeline YAML structure.
```

#### Checkpoint 3.4
- [ ] Environment specifications complete
- [ ] Configuration matrix created
- [ ] CI/CD pipeline designed
- [ ] All documentation saved

---

## Lab Deliverables

Submit the following:

1. **component-specifications.md** - All service specifications
2. **architecture-diagrams.md** - All ASCII diagrams with explanations
3. **integration-contracts.md** - ClamAV and notification contracts
4. **deployment-architecture.md** - Environment specs and pipeline design

---

## Evaluation Criteria

| Criteria | Points | Description |
|----------|--------|-------------|
| Component Specs | 25 | Complete, implementable |
| Diagrams | 25 | Clear, accurate, informative |
| Integration Contracts | 25 | Protocol details, error handling |
| Deployment Design | 25 | Environment matrix, pipeline |

---

## Lab Completion Checklist

- [ ] All component specifications created
- [ ] System context diagram
- [ ] Component diagram
- [ ] Sequence diagram
- [ ] Integration contracts
- [ ] Deployment architecture
- [ ] CI/CD pipeline design

---

## Next Lab

Proceed to [Lab 4: AI-Assisted Development](../lab-04-development/README.md)
