# Requirements Checklist
# PRD Quality Assurance

Use this checklist when reviewing or creating Product Requirements Documents.

---

## Executive Summary

- [ ] Clear problem statement (what business problem are we solving?)
- [ ] Proposed solution described
- [ ] Success metrics defined with targets
- [ ] Current vs target state documented

---

## Stakeholders

- [ ] Primary stakeholders identified (name, role, responsibility)
- [ ] Secondary stakeholders identified (interest, impact)
- [ ] Stakeholder contact information available

---

## User Stories

### Format
- [ ] Follows "As a [role], I want [capability], so that [benefit]"
- [ ] Role is specific (not just "user")
- [ ] Benefit is business-aligned

### Acceptance Criteria
- [ ] Each story has acceptance criteria
- [ ] Criteria are measurable
- [ ] Criteria are testable
- [ ] Criteria are independent (not dependent on other stories)
- [ ] Priority assigned (P0/P1/P2)

### Coverage
- [ ] All user roles have stories
- [ ] Happy path covered
- [ ] Error paths considered
- [ ] Edge cases addressed

---

## Functional Requirements

### Completeness
- [ ] All user story acceptance criteria converted to FRs
- [ ] Integration points defined
- [ ] Data handling requirements specified

### Clarity
- [ ] Each requirement is atomic (one thing)
- [ ] Clear description (no ambiguity)
- [ ] No implementation details (that's for later phases)

### Traceability
- [ ] Each FR links to user story
- [ ] Each FR has unique ID
- [ ] Requirement hierarchy clear (FR-1 → FR-1.1)

---

## Non-Functional Requirements

### Performance
- [ ] Response time targets specified
- [ ] Throughput requirements defined
- [ ] Load handling specified

### Security
- [ ] Authentication requirements
- [ ] Authorization requirements
- [ ] Data protection requirements
- [ ] Compliance requirements (if any)

### Scalability
- [ ] Growth expectations documented
- [ ] Scaling approach defined

### Reliability
- [ ] Availability targets specified
- [ ] Recovery requirements defined

---

## Technical Requirements

### AEM-Specific
- [ ] AEM version specified (Cloud Service, 6.5, etc.)
- [ ] Deployment target defined
- [ ] Run mode considerations (author/publish)

### Integration
- [ ] External systems identified
- [ ] Integration type specified (API, events, etc.)
- [ ] Authentication for integrations defined

### Constraints
- [ ] Budget constraints
- [ ] Timeline constraints
- [ ] Technology constraints
- [ ] Regulatory constraints

---

## Architecture

### High-Level Design
- [ ] System context diagram present
- [ ] Major components identified
- [ ] Data flow described
- [ ] Integration points documented

### Technical Decisions
- [ ] Key technical decisions documented
- [ ] Rationale provided for decisions
- [ ] Alternative approaches considered

---

## Acceptance Criteria Summary

### MVP (Minimum Viable Product)
- [ ] MVP features clearly identified
- [ ] MVP scope realistic
- [ ] MVP success criteria defined

### Full Release
- [ ] Full release features listed
- [ ] Phase 2+ requirements identified
- [ ] Dependencies between phases clear

---

## Risks and Mitigations

- [ ] Risks identified
- [ ] Probability and impact assessed
- [ ] Mitigation strategies defined
- [ ] Risk owner assigned

---

## Timeline

- [ ] Phases defined
- [ ] Duration estimated
- [ ] Dependencies between phases identified
- [ ] Milestones defined

---

## Review Checklist (For Reviewers)

### Before Approval
- [ ] All sections complete
- [ ] No placeholder text
- [ ] Consistent formatting
- [ ] Spelling/grammar checked
- [ ] Stakeholders have reviewed

### Quality Gates
- [ ] Requirements are unambiguous
- [ ] Requirements are achievable
- [ ] Requirements are testable
- [ ] Scope is realistic
- [ ] Dependencies are clear

---

## AI-Optimized Requirements Checklist

When writing for AI agents:

### Specificity
- [ ] Exact values used (not "fast", use "< 60 seconds")
- [ ] Clear boundaries defined
- [ ] No subjective terms

### Structure
- [ ] Hierarchical organization
- [ ] Numbered sections
- [ ] Consistent formatting
- [ ] Examples provided where helpful

### Completeness
- [ ] Preconditions specified
- [ ] Postconditions specified
- [ ] Error conditions covered
- [ ] Edge cases considered

---

## Example: Before and After

### Before (Not AI-Optimized)
```
The system should be fast and handle many users efficiently.
```

### After (AI-Optimized)
```
The system must:
- Handle 100 concurrent users without degradation
- Complete scan within 60 seconds for files up to 100MB
- Support 10,000 daily asset uploads
- Scale horizontally to handle peak loads (3x normal)
```

---

## Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | | | |
| Technical Lead | | | |
| Security Lead | | | |
| QA Lead | | | |
