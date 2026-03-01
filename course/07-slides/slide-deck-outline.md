# Slide Deck Outlines
# Agentic Development for AEM Course

## Module 1: Introduction to Agentic Development

### Slide 1: Title
- **Title**: Agentic Development for AEM
- **Subtitle**: Building Enterprise Workflows with AI Agents
- **Visual**: Hero image with AI + AEM logos

### Slide 2: Agenda
- What is Agentic Development?
- The BMAD-BEAD-GasTown Ecosystem
- Course Overview
- Case Study Introduction

### Slide 3: Traditional vs Agentic Development
**Two columns:**
| Traditional | Agentic |
|-------------|---------|
| Human writes all code | AI generates, human reviews |
| Sequential execution | Parallel agent work |
| Context lost between sessions | Persistent memory (BEAD) |
| Single developer | Multi-agent teams |

### Slide 4: What is an AI Agent?
- **Definition**: Autonomous AI systems that can:
  - Reason about tasks
  - Use tools (read, write, execute)
  - Make decisions
  - Complete multi-step goals
- **Visual**: Agent loop diagram (Observe → Think → Act → Learn)

### Slide 5: The Three Pillars
```
     BMAD              BEAD            GasTown
   ┌───────┐        ┌───────┐        ┌───────┐
   │Strategy│   +   │Memory │   +    │Orchestr│
   │Planning│       │Tracking│       │ation   │
   └───────┘        └───────┘        └───────┘
```
- **BMAD**: Methodology (What to build)
- **BEAD**: Task Tracking (What's done/pending)
- **GasTown**: Coordination (Who does what)

### Slide 6: BMAD Overview
- Breakthrough Method for Agile Development
- 7 Phases: Init → Discovery → Models → Architecture → Development → Testing → Operations
- AI-augmented at every phase
- **Visual**: Phase diagram with icons

### Slide 7: BEAD Overview
- Build, Execute, Analyze, Document
- Git-backed task tracking
- AI agent persistent memory
- Dependency management
- **Visual**: Task hierarchy diagram

### Slide 8: GasTown Overview
- Multi-agent orchestration
- Mayor AI coordinates specialists
- Quality gates between phases
- Parallel execution
- **Visual**: Agent network diagram

### Slide 9: Course Structure
- 8 Modules, 8 Labs
- Case Study: Secure Asset Approval Workflow
- Hands-on with AI assistants
- Build a production-ready AEM workflow

### Slide 10: Case Study Introduction
- **Problem**: Malware in DAM, slow approvals
- **Solution**: Automated scanning + multi-level approval
- **Technologies**: AEM, ClamAV, OSGi, Workflow API
- **Visual**: High-level architecture diagram

---

## Module 2: Writing AI-Optimized PRDs

### Slide 1: Title
- **Title**: Writing AI-Optimized PRDs
- **Subtitle**: Requirements That Agents Can Understand

### Slide 2: Why PRD Structure Matters
- AI agents decompose requirements into tasks
- Ambiguous requirements = wrong implementations
- Structured format = predictable decomposition
- **Visual**: PRD → Tasks flow diagram

### Slide 3: PRD Anatomy
1. Executive Summary
2. User Stories (Epic → Feature → Story)
3. Acceptance Criteria
4. Non-Functional Requirements
5. Technical Architecture
6. Integration Points

### Slide 4: User Story Format
```
As a [ROLE]
I want [CAPABILITY]
So that [BENEFIT]
```
- Be specific about the role
- Make capability measurable
- Connect to business value

### Slide 5: Acceptance Criteria Best Practices
- **Specific**: Exact behaviors, not vague descriptions
- **Measurable**: Numbers, thresholds, outcomes
- **Testable**: Can be verified automatically
- **Independent**: Each criterion stands alone

### Slide 6: Good vs Bad Criteria
| Bad | Good |
|-----|------|
| "System should be fast" | "Response time < 100ms" |
| "Easy to use" | "Complete task in < 3 clicks" |
| "Secure" | "All inputs validated, XSS prevented" |

### Slide 7: AI Decomposition Example
**Input:**
```
As a security admin, I want uploaded files scanned
```

**AI Decomposition:**
- Task 1: AntivirusScanService interface
- Task 2: ClamAV integration
- Task 3: Workflow process step
- Task 4: Quarantine handling
- Task 5: Unit tests

### Slide 8: Lab Preview
- Analyze sample PRD
- Write new feature PRD
- AI-assisted validation
- Task decomposition preview

---

## Module 3: BMAD Architecture Phase

### Slide 1: Title
- **Title**: Architecture Design with AI
- **Subtitle**: From Requirements to Specifications

### Slide 2: Phase 03 Overview
- Input: PRD, User Stories
- Output: Component Specs, Diagrams, Contracts
- AI Role: Generate, Review, Refine
- **Visual**: Phase flow diagram

### Slide 3: Component Specification Template
```
## Component: [Name]
### Interface
### Implementation Strategy
### Configuration
### Error Handling
### Testing Approach
```

### Slide 4: AI-Generated Architecture
- Provide context (PRD, constraints)
- Request specific outputs (interface, config)
- Review and refine iteratively
- Validate against requirements
- **Visual**: AI conversation flow

### Slide 5: Integration Contracts
- Define protocol details
- Specify error handling
- Document timeout behavior
- Include test strategies
- **Visual**: ClamAV protocol diagram

### Slide 6: Deployment Architecture
- Environment matrix (Local → Prod)
- Configuration differences
- CI/CD pipeline design
- Security considerations

### Slide 7: Lab Preview
- Generate component specifications
- Create architecture diagrams
- Define integration contracts
- Design deployment architecture

---

## Module 4: AI-Assisted Development

### Slide 1: Title
- **Title**: AI Pair Programming
- **Subtitle**: Implementing with AI Assistance

### Slide 2: AI Development Workflow
1. Provide context (spec, patterns)
2. Request implementation
3. Review generated code
4. Refine with follow-ups
5. Test and iterate

### Slide 3: Effective AI Prompts
**Include:**
- Clear objective
- Technical constraints
- Existing patterns to follow
- Expected output format

**Avoid:**
- Vague requirements
- Missing context
- Unrealistic scope

### Slide 4: Code Review for AI Output
- Check: Security, Performance, Standards
- Verify: Error handling, Logging
- Validate: AEM best practices
- Test: Edge cases
- **Visual**: Review checklist

### Slide 5: Common Patterns
- OSGi Service Pattern
- Workflow Process Pattern
- Sling Model Pattern
- Configuration Pattern
- **Visual**: Code snippets

### Slide 6: Lab Preview
- Generate AntivirusScanService
- Implement workflow processes
- Create workflow model
- Deploy and verify

---

## Module 5: BEAD Task Management

### Slide 1: Title
- **Title**: BEAD Task Management
- **Subtitle**: Persistent Memory for AI Agents

### Slide 2: Why BEAD?
- AI sessions are stateless
- Context lost between sessions
- BEAD provides persistent memory
- Track what's done, what's pending
- **Visual**: Session continuity diagram

### Slide 3: Task Hierarchy
```
Epic
└── Feature
    └── Task
        └── Subtask
```
- Hierarchical organization
- Dependency tracking
- Progress aggregation

### Slide 4: Task Format
```yaml
id: "SAW-021"
type: task
title: "Implement Process"
status: in_progress
acceptance_criteria:
  - "Inject service"
  - "Handle errors"
```

### Slide 5: Session Tracking
- Start/update/complete actions
- Artifact recording
- Notes for continuity
- Handoff information

### Slide 6: Lab Preview
- Initialize BEAD
- Create task hierarchy
- Track progress
- Practice handoffs

---

## Module 6: GasTown Orchestration

### Slide 1: Title
- **Title**: Multi-Agent Orchestration
- **Subtitle**: Coordinating AI Teams with GasTown

### Slide 2: The Agent Team
```
        ┌─────────┐
        │  Mayor  │
        └────┬────┘
    ┌────────┼────────┐
    ▼        ▼        ▼
┌──────┐ ┌──────┐ ┌──────┐
│Coder │ │Tester│ │Review│
└──────┘ └──────┘ └──────┘
```

### Slide 3: Workflow Definition
- Steps with dependencies
- Agent assignments
- Quality gates
- Error handling

### Slide 4: Parallel Execution
- Multiple agents simultaneously
- Fan-out / Fan-in patterns
- 3x speedup typical
- **Visual**: Timeline comparison

### Slide 5: Quality Gates
- Code complete → Tests
- Tests pass → Review
- Review approved → Complete
- **Visual**: Gate flow diagram

### Slide 6: Lab Preview
- Configure GasTown
- Define workflows
- Simulate execution
- Practice orchestration

---

## Module 7: Testing

### Slide 1: Title
- **Title**: AI-Generated Testing
- **Subtitle**: Comprehensive Coverage with AI

### Slide 2: Test Pyramid
```
      E2E
     /   \
   Integration
   /         \
    Unit Tests
```
- AI generates all levels
- Focus on edge cases
- Coverage targets

### Slide 3: Unit Test Generation
- Provide class under test
- Specify scenarios
- AI generates test methods
- Review and run

### Slide 4: Integration Testing
- Workflow end-to-end
- External system integration
- Container-based testing

### Slide 5: Lab Preview
- Generate unit tests
- Create integration tests
- Achieve >80% coverage

---

## Module 8: Deployment & Operations

### Slide 1: Title
- **Title**: Deployment & Operations
- **Subtitle**: From Development to Production

### Slide 2: Deployment Pipeline
```
Build → Test → Stage → Prod
```
- Cloud Manager integration
- Environment configurations
- Quality gates

### Slide 3: Configuration Management
- OSGi configurations
- Environment-specific settings
- Secrets management

### Slide 4: Monitoring & Logging
- Health checks
- Performance monitoring
- Error tracking
- Audit logging

### Slide 5: Course Completion
- All 8 labs completed
- Working workflow deployed
- Ready for production
- Certification available

---

## Slide Design Guidelines

### Visual Style
- Clean, minimal design
- Blue and orange accent colors
- Monospace font for code
- Consistent iconography

### Code Blocks
- Syntax highlighting
- Maximum 15 lines per slide
- Focus on key concepts

### Diagrams
- ASCII art for code comments
- Vector diagrams for slides
- Consistent box/arrow style

### Animations
- Reveal bullet points sequentially
- Build diagrams step by step
- Highlight code segments
