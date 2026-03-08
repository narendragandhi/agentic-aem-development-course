# Secure Asset Workflow - Production Ready

A complete, working demonstration of Agentic AI Development for AEM using BMAD/BEAD/GasTown methodology.

## ⚠️ Important - Adobe Repository Access Required

The full AEM SDK requires Adobe repository access. For local development without Adobe credentials, use the simplified version.

```bash
# Option 1: With Adobe Repo Access (Full Build)
# Add to settings.xml:
<servers>
  <server>
    <id>adobe</id>
    <username>YOUR_ADOBE_USERNAME</username>
    <password>YOUR_ADOBE_PASSWORD</password>
  </server>
</servers>

# Option 2: Without Adobe Access (Simplified - Tests Pass)
# See: prototype-simplified/
```

## What's Included

| Component | Status | Description |
|-----------|--------|-------------|
| **Working Code** | ✅ | Java 11, JUnit 5, TDD |
| **Tests** | ✅ 11 passing | Full TDD demonstration |
| **CLI Tools** | ✅ | BEAD, GasTown Python CLIs |
| **AI Prompts** | ✅ | Claude, Goose prompts |
| **Docker** | ✅ | ClamAV, AEM configs |
| **MCP** | ✅ | AEM, JIRA, Slack configs |
| **CI/CD** | ✅ | GitHub Actions |
| **AEM SDK** | ⚠️ | Requires Adobe repo |

## Quick Start (Simplified - Works Now)

```bash
# Clone and build
cd prototype

# Tests pass without Adobe dependencies
mvn test -Dtest=AntivirusScanServiceSpec

# Output:
# Tests run: 11, Failures: 0, Errors: 0
# BUILD SUCCESS
```

## Project Structure

```
prototype/
├── agents/                 # AI Agent Prompts (Claude, Goose)
├── bin/                   # CLI Tools (BEAD, GasTown)
├── core/                  # AEM OSGi Bundle
│   └── src/
│       ├── main/java/     # Services, Processes
│       └── test/java/    # JUnit Tests (*Spec.java)
├── docker/               # Docker Compose
├── mcp-servers/          # MCP Configurations
├── .bead/                # Task Tracking
├── .gastown/             # Orchestration
└── .github/workflows/   # CI/CD
```

## The Complete BMAD/BEAD/GasTown Workflow

### 1. BMAD - Methodology

```
Phase 00 → 01 → 02 → 03 → 04 → 05 → 06
   ↓       ↓    ↓    ↓    ↓    ↓    ↓
Init  Discovery Models Arch  Dev  Test Ops
```

### 2. BEAD - Task Tracking

```bash
# Create task
bead create task "Implement feature"

# Show task
bead show SAW-010

# Update progress
bead log SAW-010 "RED: Tests written"
bead update SAW-010 completed
```

### 3. GasTown - Orchestration

```bash
# Run workflow
gastown run implement-feature --task SAW-010
```

## TDD Cycle Demo

```bash
# 1. RED: Tests written first
mvn test -Dtest=AntivirusScanServiceSpec
# Tests FAIL (expected - implementation missing)

# 2. GREEN: Implementation added
# (AntivirusScanServiceImpl.java added)

mvn test -Dtest=AntivirusScanServiceSpec
# Tests PASS: 11 tests, 0 failures

# 3. REFACTOR: Improve code
# (Code optimized while keeping tests green)
```

## Test Results

```
[INFO] Running com.demo.workflow.services.AntivirusScanServiceSpec$ScanFunctionality
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.demo.workflow.services.AntivirusScanServiceSpec$PerformanceRequirements
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.demo.workflow.services.AntivirusScanServiceSpec$ErrorHandling
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.demo.workflow.services.AntivirusScanServiceSpec$ServiceAvailability
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

## AI Agent Integration

### Claude Code
```bash
# Use AI prompts
cat agents/AI_AGENT_PROMPTS.md
```

### Goose
```yaml
# Configure in .gastown/config.yaml
model: claude-3-opus
```

### MCP Servers
```bash
# Configure MCP for AEM, JIRA, Slack
# See mcp-servers/README.md
```

## Docker

```bash
cd docker
docker-compose up -d

# Services:
# - clamav:3310    Antivirus
# - aem-author:4502 AEM (if Adobe license)
# - postgres:5432   Database
```

## CI/CD

```yaml
# .github/workflows/ci-cd.yaml
jobs:
  - build:    mvn compile
  - test:     mvn test  
  - quality:  SpotBugs, Checkstyle
  - security: Snyk, SonarQube
  - deploy:   To AEM Cloud
```

---

## For Full AEM SDK Build

1. Get Adobe credentials from https://experience.adobe.com/
2. Add to `~/.m2/settings.xml`:
```xml
<servers>
  <server>
    <id>adobe</id>
    <username>YOUR_EMAIL</username>
    <password>YOUR_PASSWORD</password>
  </server>
</servers>
```

3. Run: `mvn clean install -Pfull`

---

**This prototype demonstrates modern Agentic AI Development for AEM with working code, tests, and CI/CD.**
