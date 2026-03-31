# Agentic Development for AEM: Building Enterprise Workflows

## Course Overview

This hands-on course demonstrates how AI agents can transform the entire software development lifecycle for Adobe Experience Manager (AEM) projects. Using a real-world **Secure Asset Approval Workflow** as our case study, you'll learn to leverage BMAD, BEAD, and GasTown frameworks to build enterprise-grade solutions with AI assistance.

---

## Course Versions

| Version | Duration | Labs | Best For |
|---------|----------|------|----------|
| [**Main Course**](../labs/README.md) | 24 hours | 8 labs | Fast-paced learning, experienced developers |
| [**Comprehensive**](../labs-comprehensive/README.md) | 50 hours | 15 labs | Detailed coverage, beginners |

---

## Course Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AGENTIC DEVELOPMENT LIFECYCLE                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐│
│   │     PRD     │───▶│    BMAD     │───▶│   BEAD      │───▶│  GasTown    ││
│   │  Business   │    │  Phases     │    │  AI Tasks   │    │  Orchestrate││
│   │  Requirements│   │  Planning   │    │  Tracking   │    │  Agents     ││
│   └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘│
│         │                  │                  │                  │         │
│         ▼                  ▼                  ▼                  ▼         │
│   ┌─────────────────────────────────────────────────────────────────────┐ │
│   │                      HANDS-ON LABS                                   │ │
│   │                                                                      │ │
│   │  Lab 1: PRD → BMAD Translation                                      │ │
│   │  Lab 2: Architecture Design with AI                                 │ │
│   │  Lab 3: Component Development Sprint                                │ │
│   │  Lab 4: Multi-Agent Testing Pipeline                                │ │
│   │  Lab 5: Deployment & Operations                                     │ │
│   └─────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Learning Objectives

By the end of this course, participants will be able to:

### 1. Understand Agentic Development
- [ ] Explain how AI agents differ from traditional automation
- [ ] Describe the roles of BMAD, BEAD, and GasTown in the SDLC
- [ ] Identify use cases for AI-assisted development in AEM

### 2. Create AI-Ready Requirements
- [ ] Write PRDs optimized for AI agent consumption
- [ ] Structure requirements for automated decomposition
- [ ] Define acceptance criteria that AI can validate

### 3. Apply BMAD Methodology
- [ ] Navigate all 6 BMAD phases for AEM projects
- [ ] Generate architecture documents with AI assistance
- [ ] Create component specifications from business requirements

### 4. Manage AI Agent Tasks with BEAD
- [ ] Create and track tasks in the BEAD system
- [ ] Define task dependencies and hierarchies
- [ ] Monitor AI agent progress across sessions

### 5. Orchestrate Multi-Agent Workflows
- [ ] Configure GasTown for team coordination
- [ ] Assign specialized agents to appropriate tasks
- [ ] Aggregate results from parallel agent work

### 6. Build Production AEM Workflows
- [ ] Implement enterprise workflow processes
- [ ] Integrate external services (antivirus, notifications)
- [ ] Deploy and test in AEM Cloud Service

---

## Target Audience

| Role | What You'll Learn |
|------|-------------------|
| **AEM Developers** | Accelerate component and workflow development with AI |
| **Technical Architects** | Design AI-assisted architecture workflows |
| **Project Managers** | Manage AI agent teams and track progress |
| **DevOps Engineers** | Automate CI/CD with agent orchestration |
| **Technical Leads** | Lead hybrid human-AI development teams |

---

## Prerequisites

### Technical Requirements
- Java 11+ development experience
- Familiarity with AEM concepts (Sling, OSGi, JCR)
- Basic understanding of workflows and DAM
- Git version control knowledge

### Environment Setup
- AEM Cloud Service SDK (2024.11+) or AEM 6.5.x
- Maven 3.8+
- Docker Desktop (for ClamAV integration)
- IDE with AEM support (VS Code + AEM extension, IntelliJ)

### AI Tools
- Claude Code CLI or similar AI coding assistant
- Access to Claude API (for GasTown orchestration)
- Adobe AEM Agent Skills (installed via skill marketplace)
- AEM MCP Server package (from Software Distribution Portal)

---

## Course Modules

### Module 0: Local Development with AI Tools (2 hours) *NEW*
- Setting up AI coding tools for AEM development
- AGENTS.md and project context generation
- Installing Adobe's AEM Agent Skills
- MCP Servers for runtime integration
- **Lab 0.1**: AI Tools Setup and Configuration
- **Lab 0.2**: Component creation with AI assistance

### Module 1: Introduction to Agentic Development (2 hours)
- What is agentic AI development?
- The BMAD-BEAD-GasTown ecosystem
- Case study introduction: Secure Asset Approval Workflow
- **Lab 1.1**: Environment setup and verification

### Module 2: Product Requirements with AI (3 hours)
- Writing AI-optimized PRDs
- Structured requirements for agent decomposition
- Acceptance criteria and validation rules
- **Lab 2.1**: Create PRD for Secure Asset Workflow
- **Lab 2.2**: AI-assisted requirements analysis

### Module 3: BMAD Phases 00-02 - Discovery & Design (4 hours)
- Phase 00: Project initialization
- Phase 01: Business discovery
- Phase 02: Model definition
- **Lab 3.1**: Generate user personas with AI
- **Lab 3.2**: Create content models from requirements
- **Lab 3.3**: Design information architecture

### Module 4: BMAD Phases 03-04 - Architecture & Development (6 hours)
- Phase 03: Technical architecture
- Phase 04: Development sprint
- Component specifications and contracts
- **Lab 4.1**: Generate architecture diagrams with AI
- **Lab 4.2**: Create workflow process specifications
- **Lab 4.3**: Implement AntivirusScanService with AI pair programming

### Module 5: BEAD Task Management (3 hours)
- Creating BEAD task hierarchies
- Dependency tracking and resolution
- Session persistence and context management
- **Lab 5.1**: Define BEAD tasks for workflow components
- **Lab 5.2**: Track implementation progress
- **Lab 5.3**: Handle task dependencies

### Module 6: GasTown Multi-Agent Orchestration (4 hours)
- Mayor AI and specialized agents
- Workflow definitions and triggers
- Result aggregation and reporting
- **Lab 6.1**: Configure GasTown for the project
- **Lab 6.2**: Run parallel agent development
- **Lab 6.3**: Coordinate testing and review agents

### Module 7: BMAD Phase 05 - Testing & Deployment (4 hours)
- AI-generated test cases
- Integration testing strategies
- Cloud Manager deployment
- **Lab 7.1**: Generate unit tests with AI
- **Lab 7.2**: Create integration test scenarios
- **Lab 7.3**: Deploy to AEM Cloud Service

### Module 8: Operations & Continuous Improvement (2 hours)
- Monitoring and observability
- Security considerations
- Iterating with AI feedback
- **Lab 8.1**: Set up monitoring dashboards
- **Lab 8.2**: Security review with AI agents

### Module 9: Test-Driven Agentic Development (3 hours)
- TDD fundamentals for AI agent workflows
- Writing specification tests that guide AI
- Red-Green-Refactor cycle with AI agents
- TDAD: Hybrid BMAD + TDD methodology
- **Lab 9.1**: Write specification tests first
- **Lab 9.2**: Create BEAD task with test references
- **Lab 9.3**: AI-driven implementation cycle

### Module 10: Advanced Security Scanning (4 hours)
- Basic security pattern detection (XSS, SQL injection)
- Document security (PDF JavaScript, Office macros)
- Comprehensive OWASP Top 10 coverage
- **Lab 10.1**: Implement basic security scanner
- **Lab 10.2**: Build document security scanner (PDF/Office)
- **Lab 10.3**: Implement OWASP pattern database

### Module 11: Multi-Agent Orchestration (3 hours)
- Building the agent orchestrator
- Workflow definition and execution
- Context sharing between agents
- Checkpoints and recovery
- **Lab 11.1**: Implement agent interface pattern
- **Lab 11.2**: Build workflow execution engine
- **Lab 11.3**: Create specialized AEM agents

### Module 12: Integration Testing (3 hours)
- AEM Mock framework
- Testing with real AEM context
- Service integration testing
- Workflow process testing
- **Lab 12.1**: Configure AEM Mock
- **Lab 12.2**: Create DAM asset tests
- **Lab 12.3**: Test service dependencies

### Module 13: Code Quality & Hygiene (3 hours)
- Static code analysis (Checkstyle, PMD, SpotBugs)
- Code coverage with JaCoCo
- Javadoc generation and validation
- Quality gates and CI/CD integration
- **Lab 13.1**: Configure linting tools
- **Lab 13.2**: Implement coverage requirements
- **Lab 13.3**: Set up quality pipeline

---

## Course Materials

### Included Resources
```
course/
├── 00-course-overview/        # This document
│   ├── README.md
│   ├── PREREQUISITES.md
│   └── ENVIRONMENT-SETUP.md
├── 01-prd/                    # Product Requirements
├── 05-ai-tools-setup/         # AI Tools Configuration (NEW)
│   ├── README.md
│   ├── 01-agents-md.md
│   ├── 02-agent-skills.md
│   ├── 03-mcp-servers.md
│   └── 04-ide-configuration.md
│   ├── secure-asset-workflow-prd.md
│   └── requirements-checklist.md
├── 02-bmad-phases/            # BMAD Phase Documents
│   ├── phase-00-initialization.md
│   ├── phase-01-discovery.md
│   ├── phase-02-models.md
│   ├── phase-03-architecture.md
│   ├── phase-04-development.md
│   └── phase-05-testing.md
├── 03-bead-tasks/             # BEAD Task Definitions
│   ├── .issues/
│   └── task-templates/
├── 04-gastown-orchestration/  # GasTown Configuration
│   ├── agents/
│   └── workflows/
├── labs/                      # Main Course Labs (26 hours)
│   ├── lab-00-ai-tools-setup/     # NEW: AI Tools Lab
│   ├── lab-01-setup-foundations/
│   ├── lab-02-bmad-architecture/
│   ├── lab-03-tdd-development/
│   ├── lab-04-security-scanner/
│   ├── lab-05-agent-orchestrator/
│   ├── lab-06-testing/
│   ├── lab-07-quality-deployment/
│   └── lab-08-capstone/
├── labs-comprehensive/        # Comprehensive Labs (50 hours)
│   ├── lab-01 through lab-15  # (15 detailed labs)
├── 06-instructor-guide/       # Teaching Materials
│   ├── facilitation-guide.md
│   ├── assessment-rubrics.md
│   └── common-issues.md
└── assets/                    # Diagrams and Images
    └── diagrams/
```

---

## Assessment & Certification

### Hands-On Assessment
- Complete all 8 lab exercises
- Successfully deploy the Secure Asset Workflow
- Demonstrate multi-agent orchestration

### Knowledge Assessment
- Multiple choice quiz on BMAD methodology
- Architecture design review
- Code review using AI agents

### Certification
Upon successful completion, participants receive:
- **Agentic AEM Developer** certification
- Digital badge for LinkedIn
- Access to advanced courses

---

## Time Investment

### Main Course (26 hours)
| Component | Duration |
|-----------|----------|
| Instruction | 21 hours |
| Hands-On Labs | 5 hours |
| **Total** | **26 hours** |

Recommended pace: 3-4 days intensive or 2 weeks part-time

### Comprehensive Course (50 hours)
| Component | Duration |
|-----------|----------|
| Video Lectures | 16 hours |
| Hands-On Labs | 26 hours |
| Self-Study | 6 hours |
| Assessment | 2 hours |
| **Total** | **50 hours** |

Recommended pace: 2 weeks intensive or 6 weeks part-time

---

## Support & Community

- **Discussion Forum**: Ask questions and share solutions
- **Office Hours**: Weekly live Q&A sessions
- **GitHub Repository**: Access to all course code
- **Slack Channel**: Connect with other learners

---

## Next Steps

1. Review [Prerequisites](./PREREQUISITES.md)
2. Complete [Environment Setup](./ENVIRONMENT-SETUP.md)
3. Begin [Module 1: Introduction](../01-prd/README.md)

---

*This course is part of the **Agentic Development for Enterprise** series.*
