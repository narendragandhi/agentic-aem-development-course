# Lab 1: Setup & Foundations (3 hours)

## Objective
Set up your development environment and create an AI-optimized Product Requirements Document (PRD) for the Secure Asset Approval Workflow.

---

## Part 1: Environment Setup (1 hour)

### 1.1 Prerequisites Checklist

```bash
# Verify Java 11+
java -version

# Verify Maven 3.8+
mvn -version

# Verify Git
git --version

# Verify Docker (for ClamAV)
docker --version
```

### 1.2 Clone the Project

```bash
git clone https://github.com/your-org/agentic-aem-course.git
cd agentic-aem-course
```

### 1.3 Build and Verify

```bash
# Full build
mvn clean install

# Expected: BUILD SUCCESS with 165+ tests passing
```

### 1.4 IDE Setup

**VS Code:**
```bash
code .
# Install: Extension Pack for Java, AEM IDE
```

**IntelliJ:**
- Import as Maven project
- Enable annotation processing

---

## Part 2: Understanding BMAD-BEAD-GasTown (30 min)

### 2.1 The Agentic Development Stack

```
┌─────────────────────────────────────────────────────────────────┐
│                    AGENTIC DEVELOPMENT STACK                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐     │
│   │   PRD   │───▶│  BMAD   │───▶│  BEAD   │───▶│ GasTown │     │
│   │ What    │    │ How     │    │ Track   │    │ Execute │     │
│   └─────────┘    └─────────┘    └─────────┘    └─────────┘     │
│                                                                  │
│   Business       Architecture    Task           Multi-Agent     │
│   Requirements   & Design        Management     Orchestration   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Key Concepts

| Framework | Purpose | Key Artifact |
|-----------|---------|--------------|
| **PRD** | Define what to build | Requirements document |
| **BMAD** | 7-phase methodology | Architecture docs |
| **BEAD** | Task tracking | YAML task files |
| **GasTown** | Agent coordination | Workflow definitions |

---

## Part 3: Creating the PRD (1.5 hours)

### 3.1 PRD Template

Create `course/01-prd/secure-asset-workflow-prd.md`:

```markdown
# Secure Asset Approval Workflow PRD

## 1. Overview

### 1.1 Problem Statement
Organizations uploading assets to AEM DAM face security risks from:
- Malicious files disguised as images/documents
- Metadata containing injection attacks (XSS, SQL)
- Macros and scripts in Office/PDF documents

### 1.2 Solution
An automated security scanning workflow that:
- Scans all uploaded assets before publication
- Detects and quarantines threats
- Provides audit trail for compliance

---

## 2. User Stories

### US-1: Content Author Upload
**As a** content author
**I want** my uploaded assets automatically scanned
**So that** I don't accidentally publish malicious content

**Acceptance Criteria:**
- [ ] Scan triggers within 5 seconds of upload
- [ ] Author receives notification of scan result
- [ ] Clean assets proceed to approval queue

### US-2: Security Admin Review
**As a** security administrator
**I want** to review flagged assets before deletion
**So that** I can verify threats and avoid false positives

**Acceptance Criteria:**
- [ ] Dashboard shows all quarantined assets
- [ ] Each finding includes OWASP/CWE reference
- [ ] Admin can approve or permanently delete

### US-3: Compliance Officer Audit
**As a** compliance officer
**I want** complete audit logs of all scans
**So that** I can demonstrate regulatory compliance

**Acceptance Criteria:**
- [ ] All scan events logged with timestamps
- [ ] Logs retained for configurable period
- [ ] Export to CSV/JSON supported

---

## 3. Functional Requirements

### FR-1: Security Scanning
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1.1 | Detect XSS patterns in metadata | P0 |
| FR-1.2 | Detect SQL injection patterns | P0 |
| FR-1.3 | Validate file type (magic bytes) | P0 |
| FR-1.4 | Detect embedded scripts (SVG/HTML) | P0 |
| FR-1.5 | Scan PDF for JavaScript/Launch actions | P1 |
| FR-1.6 | Scan Office docs for macros | P1 |
| FR-1.7 | OWASP Top 10 pattern coverage | P1 |

### FR-2: Workflow Actions
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-2.1 | Auto-approve clean assets | P0 |
| FR-2.2 | Quarantine critical threats | P0 |
| FR-2.3 | Route medium threats for review | P1 |
| FR-2.4 | Send notifications on completion | P1 |

### FR-3: Audit & Reporting
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-3.1 | Log all scan events | P0 |
| FR-3.2 | Track user actions on flagged assets | P1 |
| FR-3.3 | Generate compliance reports | P2 |

---

## 4. Non-Functional Requirements

### NFR-1: Performance
- Scan completion: <10 seconds for files <10MB
- Throughput: 100 concurrent scans

### NFR-2: Security
- No sensitive data in logs
- Quarantine path not web-accessible

### NFR-3: Scalability
- Horizontal scaling via AEM instances
- Stateless service design

---

## 5. Technical Constraints

- AEM as a Cloud Service (2024.11+)
- Java 11
- OSGi Declarative Services
- No external dependencies without approval

---

## 6. Success Metrics

| Metric | Target |
|--------|--------|
| Threat detection rate | >95% |
| False positive rate | <5% |
| Scan latency (p95) | <5s |
| System availability | 99.9% |
```

### 3.2 PRD Review Checklist

- [ ] Clear problem statement
- [ ] Measurable acceptance criteria
- [ ] Prioritized requirements (P0/P1/P2)
- [ ] Non-functional requirements defined
- [ ] Success metrics specified
- [ ] Technical constraints documented

---

## Verification

Run the build to ensure environment is correct:

```bash
mvn clean test -pl core
# Expected: 165 tests passing
```

---

## Key Takeaways

1. **PRDs drive AI agents** - Clear requirements = better AI output
2. **Acceptance criteria are tests** - Each criterion becomes a spec test
3. **Prioritization guides development** - P0 first, then P1, then P2

---

## Next Lab
[Lab 2: BMAD Architecture](../lab-02-bmad-architecture/README.md)
