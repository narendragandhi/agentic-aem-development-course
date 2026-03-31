# Agentic AEM Development Course

## Overview

A focused 24-hour course on Agentic Development for AEM using BMAD, BEAD, and GasTown methodologies. Build production-ready security workflows with TDD practices.

> **Note:** For a more detailed 50-hour version with additional labs and deeper coverage, see [labs-comprehensive](../labs-comprehensive/README.md).

---

## Course Structure

| Lab | Title | Duration | Topics |
|-----|-------|----------|--------|
| 0 | [AI Tools Setup](lab-00-ai-tools-setup/README.md) | 1.5 hours | AGENTS.md, Adobe Skills, MCP Servers, IDE config |
| 1 | [Setup & Foundations](lab-01-setup-foundations/README.md) | 3 hours | Environment, PRD, BMAD/BEAD/GasTown overview |
| 2 | [BMAD Architecture](lab-02-bmad-architecture/README.md) | 3 hours | Domain models, system design, service contracts |
| 3 | [TDD Development](lab-03-tdd-development/README.md) | 3 hours | RED-GREEN-REFACTOR, spec tests, BEAD tracking |
| 4 | [Security Scanner](lab-04-security-scanner/README.md) | 4 hours | XSS, SQL injection, PDF/Office, OWASP Top 10 |
| 5 | [Agent Orchestrator](lab-05-agent-orchestrator/README.md) | 3 hours | GasTown patterns, multi-agent workflows |
| 6 | [Testing](lab-06-testing/README.md) | 3 hours | AEM Mock, unit tests, integration tests |
| 7 | [Quality & Deployment](lab-07-quality-deployment/README.md) | 3 hours | Linting, coverage, Javadoc, Cloud deployment |
| 8 | [Capstone Project](lab-08-capstone/README.md) | 4 hours | End-to-end feature implementation |

**Total: 27.5 hours** (25.5 hours instruction + 2 hours buffer)

---

## Prerequisites

- Java 11+
- Maven 3.8+
- Git
- Docker (for optional ClamAV integration and Dispatcher MCP)
- IDE (VS Code, IntelliJ, or Cursor)
- AI Coding Tool (Claude Code, Cursor, or GitHub Copilot)

---

## Quick Start

```bash
# Clone repository
git clone https://github.com/your-org/agentic-aem-course.git
cd agentic-aem-course

# Build and verify
mvn clean install

# Expected: BUILD SUCCESS with 165+ tests passing
```

---

## Learning Path

```
┌─────────────────────────────────────────────────────────────────┐
│                      LEARNING PROGRESSION                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  AI TOOLS          FOUNDATIONS        CORE SKILLS      ADVANCED   │
│  ┌───────┐        ┌─────────┐        ┌─────────┐     ┌─────────┐ │
│  │ Lab 0 │───────▶│ Lab 1-2 │───────▶│ Lab 3-5 │────▶│ Lab 6-8 │ │
│  └───────┘        └─────────┘        └─────────┘     └─────────┘ │
│                                                                   │
│  - AGENTS.md       - Setup            - TDD cycle     - Testing   │
│  - MCP Servers     - PRD writing      - Security      - Quality   │
│  - Skills          - Architecture     - Orchestration - Deploy    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Frameworks

### BMAD (Breakthrough Method for Agile Development)
7-phase methodology: Init → Discovery → Models → Architecture → Development → Testing → Deployment

### BEAD (Build, Execute, Analyze, Document)
Task tracking format using YAML files to document TDD progress

### GasTown (Multi-Agent Orchestration)
The "Mayor" pattern for coordinating specialized AI agents in TDD workflows

---

## What You'll Build

**Secure Asset Approval Workflow** - A production-ready AEM workflow that:
- Scans uploaded assets for security threats
- Detects XSS, SQL injection, command injection
- Analyzes PDF/Office documents for malicious content
- Implements OWASP Top 10 pattern detection
- Routes threats through approval queues
- Provides complete audit trails

---

## Skills Gained

| Area | Skills |
|------|--------|
| **AI Tools** | AGENTS.md, Adobe Skills, MCP Servers, IDE integration |
| **Methodology** | BMAD phases, BEAD tracking, GasTown orchestration |
| **TDD** | Spec tests, RED-GREEN-REFACTOR, test organization |
| **Security** | XSS/SQLi detection, document scanning, OWASP patterns |
| **AEM** | OSGi services, workflow processes, Sling Models |
| **Quality** | Checkstyle, SpotBugs, PMD, JaCoCo, Javadoc |
| **DevOps** | Cloud Manager, runmodes, CI/CD pipelines |

---

## Assessment

Complete the Capstone Project (Lab 8) demonstrating:
- PRD-driven development
- Full TDD cycle adherence
- Security integration
- Quality standards compliance
- Agent orchestration usage

Grading: 100 points total (see Lab 8 for rubric)

---

## Support

- Course Issues: Submit via GitHub Issues
- AEM Documentation: [Adobe Experience League](https://experienceleague.adobe.com)
- BMAD Reference: See `docs/bmad-methodology.md`
