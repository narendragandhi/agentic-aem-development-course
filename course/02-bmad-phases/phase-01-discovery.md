# BMAD Phase 01: Business Discovery

## Overview

Phase 01 (Discovery) is where we gather and analyze business requirements. This phase establishes the foundation for all subsequent work by creating a comprehensive understanding of what needs to be built and why.

---

## Objectives

- Gather comprehensive business requirements
- Identify stakeholders and their needs
- Define user personas
- Establish success metrics
- Create the Product Requirements Document (PRD)

---

## Activities

### 1.1 Stakeholder Identification

Identify all stakeholders who have interest in the project:

| Stakeholder Type | Questions to Ask |
|-----------------|------------------|
| Business Owners | What business problem are we solving? |
| Product Managers | What features are needed? |
| Developers | What technical constraints exist? |
| QA Team | What quality standards are required? |
| Operations | How will this be deployed and maintained? |
| Security | What security requirements apply? |
| Compliance | What regulatory requirements apply? |

### 1.2 Requirement Gathering

Gather requirements through various methods:

**Interviews**
- One-on-one sessions with key stakeholders
- Focus on pain points and needs
- Document current workflows

**Workshops**
- Cross-functional requirement workshops
- Use case mapping sessions
- Prioritization exercises

**Documentation Review**
- Existing system documentation
- Previous project documentation
- Industry best practices

### 1.3 User Persona Creation

Create detailed user personas:

```yaml
persona:
  id: "CONTENT_AUTHOR"
  name: "Content Author"
  role: "Marketing Team Member"
  
  responsibilities:
    - Upload new assets to DAM
    - Check approval status
    - Respond to revision requests
  
  pain_points:
    - Unclear approval status
    - Slow feedback cycles
    - Version confusion
  
  goals:
    - Quick asset publication
    - Clear feedback on submissions
    - Easy revision tracking
```

### 1.4 Success Metric Definition

Define measurable success metrics:

| Metric | Current State | Target | Measurement Method |
|--------|---------------|--------|-------------------|
| Approval cycle time | 5-7 days | 24-48 hours | Workflow timestamps |
| Malware detection | 0% | 100% | Automated scanning |
| False positive rate | N/A | <1% | Quarantine reviews |
| User satisfaction | 3.2/5 | 4.5/5 | Survey |

### 1.5 Risk Assessment

Identify and document risks:

```yaml
risks:
  - id: "R1"
    description: "ClamAV service unavailable"
    probability: "Medium"
    impact: "High"
    mitigation: "Fallback scanning, graceful degradation"
    
  - id: "R2"
    description: "Scanner false positives"
    probability: "Low"
    impact: "Medium"
    mitigation: "Quarantine review process, whitelist"
    
  - id: "R3"
    description: "Approval bottlenecks"
    probability: "Medium"
    impact: "Medium"
    mitigation: "SLA monitoring, escalation rules"
```

---

## Deliverables

### Product Requirements Document (PRD)

The primary deliverable is a comprehensive PRD:

```
PRD Structure:
├── 1. Executive Summary
│   ├── Problem Statement
│   ├── Proposed Solution
│   └── Success Metrics
├── 2. Stakeholders
│   ├── Primary Stakeholders
│   └── Secondary Stakeholders
├── 3. User Stories
│   ├── Epic: [Name]
│   │   └── User Story: [Name]
│   │       ├── Acceptance Criteria
│   │       └── Priority
├── 4. Functional Requirements
│   ├── Requirement ID
│   ├── Description
│   └── Priority (P0/P1/P2)
├── 5. Non-Functional Requirements
│   ├── Performance
│   ├── Security
│   ├── Scalability
│   └── Reliability
├── 6. Technical Architecture
│   ├── High-Level Diagram
│   └── Component Overview
├── 7. Integration Points
│   ├── External Systems
│   └── Internal Systems
├── 8. Configuration Requirements
│   ├── OSGi Configurations
│   └── Workflow Launcher
├── 9. Acceptance Criteria Summary
│   ├── MVP Features
│   └── Full Release Features
├── 10. Risks & Mitigations
├── 11. Timeline
└── 12. Appendices
```

### User Personas

Document all user personas identified:

```java
public enum UserPersona {
    CONTENT_AUTHOR("Content Author", "Uploads and manages assets"),
    CONTENT_REVIEWER("Content Reviewer", "Reviews and approves assets"),
    SECURITY_ADMIN("Security Administrator", "Manages security policies"),
    COMPLIANCE_OFFICER("Compliance Officer", "Audits compliance"),
    SYSTEM_ADMIN("System Administrator", "Manages system configuration");
}
```

### Requirements Traceability Matrix

Map requirements to user stories:

| Requirement ID | User Story | Priority | Phase |
|---------------|------------|----------|-------|
| FR-1.1 | US-1.1 | P0 | MVP |
| FR-1.2 | US-1.1 | P0 | MVP |
| FR-2.1 | US-2.1 | P1 | MVP |
| FR-2.2 | US-2.2 | P1 | Full |

---

## AI Augmentation

### AI-Assisted Requirements Writing

Use AI to help structure requirements:

**Prompt Template:**
```
I'm creating a PRD for an AEM workflow system.
The system needs to:
- Scan uploaded assets for malware
- Route assets through multi-level approval
- Provide audit trails for compliance

Generate user stories following this format:
As a [role], I want [capability], so that [benefit]

Include specific acceptance criteria that are:
- Measurable
- Testable
- Independent
```

### AI-Generated Acceptance Criteria

AI can generate acceptance criteria from user stories:

**Input:**
```
User Story: As a content author, I want my uploaded assets 
automatically scanned for malware so that infected files 
never reach reviewers.
```

**AI-Generated Acceptance Criteria:**
- [ ] AC-1: Scan triggers within 60 seconds of upload
- [ ] AC-2: Author receives notification of scan result
- [ ] AC-3: Clean assets proceed to approval queue
- [ ] AC-4: Infected assets move to quarantine
- [ ] AC-5: Scan results stored as asset metadata

### AI Gap Analysis

Use AI to identify requirements gaps:

**Prompt:**
```
Review these requirements and identify:
1. Missing acceptance criteria
2. Ambiguous requirements
3. Edge cases not covered
4. Security considerations
5. Performance requirements
```

---

## Phase Transition

### Exit Criteria

Before moving to Phase 02 (Models), ensure:

- [ ] PRD is complete and approved
- [ ] All stakeholders have reviewed
- [ ] Success metrics are defined
- [ ] Risks are documented
- [ ] User personas are created
- [ ] Requirements are traceable

### Artifacts to Pass to Phase 02

- Approved PRD document
- User personas
- Success metrics
- Risk register
- Requirements traceability matrix

---

## Common Pitfalls

### Avoiding

1. **Vague Requirements**
   - Bad: "System should be fast"
   - Good: "Scan completes within 60 seconds for files <100MB"

2. **Missing Acceptance Criteria**
   - Every user story needs measurable acceptance criteria

3. **Ignoring Non-Functional Requirements**
   - Performance, security, and scalability must be defined

4. **Stakeholder Exclusion**
   - Include all relevant stakeholders in discovery

---

## Next Phase

[Phase 02: Model Definition](phase-02-models.md)

In Phase 02, we'll translate requirements into domain models and content structures.
