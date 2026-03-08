# BMAD Methodology Reference
# Breakthrough Method for Agile Development

## Overview

BMAD (Breakthrough Method for Agile Development) is an AI-augmented methodology for the software development lifecycle. It provides structure for teams using AI agents to develop enterprise AEM solutions.

---

## The 7 BMAD Phases

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          BMAD PHASES OVERVIEW                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌───────────┐     ┌───────────┐     ┌───────────┐     ┌───────────┐     │
│   │   Phase   │     │   Phase   │     │   Phase   │     │   Phase   │     │
│   │    00     │────▶│    01     │────▶│    02     │────▶│    03     │     │
│   │   Init    │     │ Discovery │     │  Models   │     │  Arch    │     │
│   └───────────┘     └───────────┘     └───────────┘     └───────────┘     │
│        │                  │                  │                  │            │
│        ▼                  ▼                  ▼                  ▼            │
│   ┌───────────┐     ┌───────────┐     ┌───────────┐     ┌───────────┐     │
│   │   Phase   │     │   Phase   │     │   Phase   │     │   Phase   │     │
│   │    06     │◀────│    05     │◀────│    04     │◀────│    03     │     │
│   │  Ops      │     │ Testing   │     │  Develop  │     │  (cont)   │     │
│   └───────────┘     └───────────┘     └───────────┘     └───────────┘     │
│                                                                             │
│   ITERATIVE: Later phases may require revisiting earlier phases            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Phase 00: Initialization

### Purpose
Establish project foundation and team setup.

### Activities
- Create project repository
- Set up CI/CD pipeline
- Configure development environment
- Define team roles and responsibilities
- Establish communication channels
- Create initial project structure

### Deliverables
- Repository with initial structure
- CI/CD configuration
- Development environment documented
- Team charter

### AI Augmentation
- AI generates project scaffolding
- AI suggests directory structure
- AI creates initial configuration files

---

## Phase 01: Discovery

### Purpose
Gather and analyze business requirements.

### Activities
- Stakeholder interviews
- Requirement gathering
- User story creation
- Success metric definition
- Risk assessment
- PRD creation

### Deliverables
- Product Requirements Document (PRD)
- User personas
- Success metrics
- Risk register
- Initial project timeline

### Key Artifacts
```
PRD Structure:
├── Executive Summary
├── Stakeholders
├── User Stories (Epics → Features → Stories)
├── Functional Requirements
├── Non-Functional Requirements
├── Technical Constraints
├── Success Metrics
└── Timeline
```

### AI Augmentation
- AI assists in writing clear, measurable requirements
- AI generates acceptance criteria
- AI identifies gaps in requirements

---

## Phase 02: Models

### Purpose
Define domain models and content structures.

### Activities
- Domain entity modeling
- Content model design
- Information architecture
- Data flow diagrams
- API contract definitions

### Deliverables
- Domain model diagrams
- Content models (AEM Content Fragments, Experience Fragments)
- Information architecture
- API specifications

### AI Augmentation
- AI generates domain models from requirements
- AI suggests content structures
- AI creates entity relationship diagrams

---

## Phase 03: Architecture

### Purpose
Design technical architecture and component specifications.

### Activities
- System architecture design
- Component specifications
- Integration patterns
- Security architecture
- Performance architecture
- Deployment topology

### Deliverables
- System context diagram
- Component architecture diagram
- Service interfaces
- Integration contracts
- Deployment architecture
- Security specifications

### AI Augmentation
- AI generates component specifications
- AI creates architecture diagrams
- AI suggests integration patterns

---

## Phase 04: Development

### Purpose
Implement features according to specifications.

### Activities
- Sprint planning
- Code implementation
- Unit testing
- Code review
- Integration
- Documentation

### Deliverables
- Source code
- Unit tests
- API documentation
- Integration points working

### AI Augmentation
- AI generates code from specifications
- AI writes unit tests
- AI assists with code review
- AI generates documentation

---

## Phase 05: Testing

### Purpose
Verify implementation meets requirements.

### Activities
- Test planning
- Test case creation
- Integration testing
- Performance testing
- Security testing
- User acceptance testing

### Deliverables
- Test plans
- Test results
- Performance reports
- Security assessment
- UAT sign-off

### AI Augmentation
- AI generates test cases
- AI creates integration tests
- AI assists with test automation

---

## Phase 06/07: Operations

### Purpose
Deploy and maintain in production.

### Activities
- Deployment to production
- Monitoring setup
- Performance monitoring
- Incident management
- Continuous improvement
- Feature iteration

### Deliverables
- Deployed system
- Monitoring dashboards
- Runbooks
- Incident response procedures

### AI Augmentation
- AI assists with deployment automation
- AI helps analyze logs
- AI suggests improvements

---

## BMAD Artifacts by Phase

| Phase | Primary Artifacts | AI-Generated |
|-------|-----------------|--------------|
| 00 | Project structure, CI/CD config | ✓ |
| 01 | PRD, User personas, Metrics | ✓ |
| 02 | Domain models, Content models | ✓ |
| 03 | Architecture diagrams, Interfaces | ✓ |
| 04 | Source code, Unit tests | ✓ |
| 05 | Test cases, Reports | ✓ |
| 06 | Deployments, Runbooks | ✓ |

---

## BMAD in Context

### BMAD with BEAD
BEAD (Build, Execute, Analyze, Document) provides task tracking for BMAD phases:
- Each BMAD phase generates BEAD tasks
- Tasks are tracked through the development cycle
- AI agents use BEAD for persistent memory

### BMAD with GasTown
GasTown orchestrates AI agents within BMAD phases:
- Phase 04 (Development) uses multiple coding agents
- Phase 05 (Testing) uses tester agents
- Phase 06 (Operations) uses DevOps agents

### BMAD with TDD
Test-Driven Development integrates with BMAD:
- Phase 02: Write specifications (spec tests)
- Phase 04: RED-GREEN-REFACTOR cycle
- Phase 05: Verification

---

## When to Use BMAD

### BMAD is ideal for:
- Enterprise AEM projects
- Teams using AI coding assistants
- Complex workflows with multiple integrations
- Projects requiring documentation and audit trails

### Consider alternatives for:
- Small prototypes
- Quick fixes and patches
- Simple component implementations

---

## BMAD Quick Reference

| Phase | Focus | Key Question | Duration |
|-------|-------|--------------|----------|
| 00 | Setup | Can we start building? | 1-2 days |
| 01 | Discovery | What are we building? | 1-2 weeks |
| 02 | Models | What are the entities? | 1 week |
| 03 | Architecture | How will it work? | 1-2 weeks |
| 04 | Development | Build it! | 2-6 weeks |
| 05 | Testing | Does it work? | 1-2 weeks |
| 06 | Operations | Keep it running | Ongoing |

---

## References

- [Phase 00: Initialization](../course/02-bmad-phases/phase-00-initialization.md)
- [Phase 01: Discovery](../course/02-bmad-phases/phase-01-discovery.md)
- [Phase 02: Models](../course/02-bmad-phases/phase-02-models.md)
- [Phase 03: Architecture](../course/02-bmad-phases/phase-03-architecture.md)
- [Phase 04: Development](../course/02-bmad-phases/phase-04-development.md)
- [Phase 05: Testing](../course/02-bmad-phases/phase-05-testing.md)
