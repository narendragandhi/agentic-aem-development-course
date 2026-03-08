# Agentic AEM Development Course (Comprehensive)

## Overview

An in-depth 50-hour course on Agentic Development for AEM using BMAD, BEAD, and GasTown methodologies. This comprehensive version includes additional labs, deeper explanations, and extended exercises.

> **Note:** For a streamlined 24-hour version, see the [main labs](../labs/README.md).

---

## Course Structure

| Lab | Title | Duration | Topics |
|-----|-------|----------|--------|
| 1 | [Environment Setup](lab-01-environment-setup/README.md) | 2 hours | Prerequisites, installation, verification |
| 2 | [PRD Creation](lab-02-prd-creation/README.md) | 3 hours | AI-optimized requirements, user stories |
| 3 | [Architecture](lab-03-architecture/README.md) | 4 hours | BMAD phases, domain models, system design |
| 4 | [Development](lab-04-development/README.md) | 4 hours | OSGi services, workflow processes |
| 5 | [BEAD Tracking](lab-05-bead-tracking/README.md) | 2 hours | Task files, progress tracking |
| 6 | [GasTown Orchestration](lab-06-gastown-orchestration/README.md) | 3 hours | Multi-agent patterns, Mayor pattern |
| 7 | [Testing](lab-07-testing/README.md) | 4 hours | Unit tests, AEM Mock |
| 8 | [Deployment](lab-08-deployment/README.md) | 3 hours | Cloud Manager, runmodes |
| 9 | [TDD Integration](lab-09-tdd-integration/README.md) | 4 hours | RED-GREEN-REFACTOR deep dive |
| 10 | [Security Scanner](lab-10-security-scanner/README.md) | 4 hours | XSS, SQL injection basics |
| 11 | [Document Security](lab-11-document-security/README.md) | 4 hours | PDF/Office scanning |
| 12 | [OWASP Patterns](lab-12-owasp-patterns/README.md) | 4 hours | Top 10 vulnerability detection |
| 13 | [Agent Orchestrator](lab-13-agent-orchestrator/README.md) | 4 hours | Advanced orchestration |
| 14 | [Integration Testing](lab-14-integration-testing/README.md) | 3 hours | End-to-end test patterns |
| 15 | [Code Quality](lab-15-code-quality/README.md) | 4 hours | Linting, coverage, Javadoc |
| 16 | [Visual Regression](lab-16-visual-regression/README.md) | 4 hours | Playwright, Percy - AI generated tests |
| 17 | [SonarQube](lab-17-sonarqube/README.md) | 3 hours | Quality gates, AI code analysis |
| 18 | [Goose AI](lab-18-goose-ai/README.md) | 4 hours | Autonomous AI agent, MCP, BMAD |
| 19 | [Functional & Regression](lab-19-functional-regression/README.md) | 4 hours | TDD, AI test generation |
| 20 | [AEM Cloud Service](lab-20-aem-cloud/README.md) | 4 hours | Cloud Manager, AI deployment |
| 21 | [Performance Profiling](lab-21-performance-profiling/README.md) | 4 hours | AI profiling, TDD baselines |
| 22 | [GraphQL for AEM](lab-22-graphql-aem/README.md) | 4 hours | AI schema generation, TDD |
| 23 | [Dispatcher Config](lab-23-dispatcher-config/README.md) | 4 hours | AI config generation, validation |
| 24 | [Oak Indexing](lab-24-oak-indexing/README.md) | 4 hours | AI analysis, TDD indexes |
| 25 | [Snyk Security](lab-25-snyk-security/README.md) | 3 hours | Dependency scanning, AI remediation |

**Total: ~89 hours**

---

## When to Use This Version

Choose the comprehensive course if you:
- Are new to AEM development
- Want detailed step-by-step explanations
- Need extended practice exercises
- Prefer a slower, thorough pace
- Want coverage of edge cases and advanced topics

---

## Comparison with Main Course

| Aspect | Main (24h) | Comprehensive (86h) |
|--------|------------|---------------------|
| Labs | 8 | 24 |
| Agentic AI | Partial | Full (All labs) |
| TDD | Yes | Yes (All labs) |
| BMAD | Yes | Yes |
| BEAD | Yes | Yes |
| GasTown | Yes | Yes |
| Goose AI | No | Yes |
| Cloud/GraphQL | No | Yes |

---

## Prerequisites

- Java 11+
- Maven 3.8+
- Git
- Docker (for ClamAV integration)
- IDE (VS Code or IntelliJ)

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

## Lab Dependencies

```
Lab 1-2 (Foundations: Prerequisites, PRD)
    │
    ▼
Lab 3-4 (Architecture & Development)
    │
    ├──▶ Lab 5-6 (BEAD & GasTown)
    │
    ▼
Lab 7-9 (Testing & TDD)
    │
    ▼
Lab 10-12 (Security)
    │
    ▼
Lab 13-14 (Advanced Orchestration & Integration)
    │
    ▼
Lab 15-19 (Quality & Testing)
    │
    ├── Lab 15: Code Quality (PMD, Checkstyle)
    ├── Lab 16: Visual Regression (Playwright)
    ├── Lab 17: SonarQube
    ├── Lab 18: Goose AI Agent ← Agentic AI Mastery
    └── Lab 19: Functional & Regression
    
    ▼
Lab 20-24 (Advanced AEM + Full Agentic)
    │
    ├── Lab 20: AEM Cloud Service
    ├── Lab 21: Performance Profiling (AI-driven)
    ├── Lab 22: GraphQL (AI schema generation)
    ├── Lab 23: Dispatcher Config (AI validation)
    └── Lab 24: Oak Indexing (AI TDD)
```

---

## Support

- Course Issues: Submit via GitHub Issues
- AEM Documentation: [Adobe Experience League](https://experienceleague.adobe.com)
- BMAD Reference: See `docs/bmad-methodology.md`
