# Lab 00: AI Tools Setup for AEM Development (Comprehensive)

## Lab Overview

| Attribute | Value |
|-----------|-------|
| **Duration** | 3 hours |
| **Difficulty** | Beginner to Intermediate |
| **Prerequisites** | AEM SDK installed, Docker Desktop running |
| **Objectives** | Full configuration of AI coding tools for AEM development |

---

## Learning Objectives

By completing this lab, you will:

- [ ] Understand the AI-assisted development ecosystem for AEM
- [ ] Generate and customize project-specific AGENTS.md
- [ ] Install and master all Adobe AEM Agent Skills
- [ ] Configure both AEM Quickstart and Dispatcher MCP servers
- [ ] Set up multiple IDEs for AI-assisted AEM development
- [ ] Create components using AI assistance with full testing

---

## Part 1: Understanding the AI Development Stack (30 minutes)

### 1.1 The Four Components

AI-assisted AEM development combines four complementary components:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AI-ASSISTED AEM DEVELOPMENT STACK                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐                 │
│   │  AGENTS.md    │   │ Agent Skills  │   │  MCP Servers  │                 │
│   │               │   │               │   │               │                 │
│   │ • Context     │   │ • Workflows   │   │ • Logs        │                 │
│   │ • Conventions │   │ • Templates   │   │ • Debugging   │                 │
│   │ • Best pracs  │   │ • Patterns    │   │ • Tracing     │                 │
│   └───────┬───────┘   └───────┬───────┘   └───────┬───────┘                 │
│           │                   │                   │                          │
│           └───────────────────┴───────────────────┘                          │
│                               │                                              │
│                               ▼                                              │
│                    ┌─────────────────────┐                                   │
│                    │   AI Coding Tools   │                                   │
│                    │ Claude | Cursor | Copilot                               │
│                    └─────────────────────┘                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 How They Work Together

| Component | Role | Timing |
|-----------|------|--------|
| AGENTS.md | Provides project context | Session start |
| Agent Skills | Encodes multi-step workflows | On-demand |
| MCP Servers | Runtime integration | Real-time |
| IDE Config | Connects everything | Setup time |

### 1.3 Discussion Questions

Before proceeding, consider:

1. How might AGENTS.md improve AI code suggestions?
2. What advantages do MCP servers provide over static documentation?
3. Why are skills organized as multi-step workflows?

---

## Part 2: Installing Adobe AEM Skills (30 minutes)

### 2.1 Choose Your Primary Tool

Select one as your primary tool, but we'll configure all for comparison:

| Tool | Strengths | Best For |
|------|-----------|----------|
| Claude Code | CLI power, full context | Experienced developers |
| Cursor | Visual IDE, good integration | IDE-centric workflows |
| GitHub Copilot | Wide IDE support | Existing VS Code/IntelliJ users |

### 2.2 Install Skills (All Methods)

**Claude Code Installation:**

```bash
# Add Adobe skills marketplace
/plugin marketplace add adobe/skills#beta

# Install AEM Cloud Service skills
/plugin install aem-cloud-service@adobe-skills

# List installed plugins
/plugin list
```

**NPX Installation (Universal):**

```bash
# Navigate to project root
cd /path/to/agentic-aem-development-course

# Install all AEM skills
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service --all

# Verify installation
npx skills list
```

**GitHub CLI Installation:**

```bash
# Install gh-upskill extension (one-time)
gh extension install trieloff/gh-upskill

# Add AEM skills
gh upskill adobe/skills --branch beta --path skills/aem/cloud-service --all

# Verify installation
gh upskill list
```

### 2.3 Verify All Skills Available

Expected skills:
- `ensure-agents-md` - Project context generation
- `create-component` - Component scaffolding
- `dispatcher` - Dispatcher configuration
- `workflow` - AEM Workflow support

**Checkpoint 1:** Screenshot showing all four skills installed.

---

## Part 3: Generating AGENTS.md (30 minutes)

### 3.1 Understanding AGENTS.md

AGENTS.md provides AI tools with:
- Project structure and module organization
- Detected frameworks and dependencies
- Coding conventions and patterns
- Domain-specific context

### 3.2 Generate AGENTS.md

Navigate to project root and run:

```bash
# Claude Code
ensure-agents-md

# NPX
npx skills run ensure-agents-md

# GitHub CLI
gh upskill run ensure-agents-md
```

### 3.3 Review Generated Files

Check generated files:

```bash
ls -la AGENTS.md CLAUDE.md .aem-skills-config.yaml
```

Review content:

```bash
head -100 AGENTS.md
```

### 3.4 Customize .aem-skills-config.yaml

Create or update configuration:

```yaml
# .aem-skills-config.yaml
configured: true
project: "secure-asset-workflow"
package: "com.adobe.aem.guides.saw.core"
group: "SAW Components"

# Domain context
domain:
  - name: "Asset Management"
    description: "Secure asset approval and processing workflows"
  - name: "Antivirus Integration"
    description: "ClamAV-based virus scanning for uploaded assets"
  - name: "Security Scanning"
    description: "Content security analysis for XSS, injection attacks"

# Development conventions
conventions:
  java:
    naming: camelCase
    max_line_length: 120
    test_framework: junit5
  htl:
    data_sly_style: attribute
```

### 3.5 Enhance AGENTS.md Manually

Add project-specific sections:

```markdown
## Business Context
This project implements an enterprise secure asset approval workflow.
Key business requirements:
- All uploaded assets must be scanned for viruses
- Document assets require security analysis
- Approval workflow with multiple stages
- Audit trail for compliance

## Integration Points
- ClamAV: Antivirus scanning service
- Notification Service: Email and Slack alerts
- Cloud Manager: CI/CD deployment

## Security Requirements
- OWASP Top 10 compliance
- PII detection for documents
- Encrypted credential storage
```

**Checkpoint 2:** Show customized AGENTS.md with project-specific context.

---

## Part 4: AEM Quickstart MCP Server (45 minutes)

### 4.1 Prerequisites Check

```bash
# Verify AEM SDK version (must be 2026.2.24678+)
curl -s -u admin:admin http://localhost:4502/system/console/bundles.json | \
  grep -o '"version":"[^"]*"' | head -1

# Verify AEM is responding
curl -s -u admin:admin http://localhost:4502/system/console/status-productinfo.txt
```

### 4.2 Download MCP Server Package

1. Visit [Software Distribution Portal](https://experience.adobe.com/#/downloads)
2. Search: `com.adobe.aem.mcp-server-contribs-content`
3. Download latest version

### 4.3 Install Package

1. Open Package Manager: http://localhost:4502/crx/packmgr
2. Upload the downloaded package
3. Install and verify

### 4.4 Test MCP Endpoint

```bash
# Basic endpoint test
curl -u admin:admin http://localhost:4502/bin/mcp

# Test aem-logs tool
curl -u admin:admin -X POST http://localhost:4502/bin/mcp \
  -H "Content-Type: application/json" \
  -d '{"tool": "aem-logs", "parameters": {"level": "ERROR", "count": 10}}'

# Test diagnose-osgi-bundle
curl -u admin:admin -X POST http://localhost:4502/bin/mcp \
  -H "Content-Type: application/json" \
  -d '{"tool": "diagnose-osgi-bundle", "parameters": {"bundle": "org.apache.sling.api"}}'

# Test recent-requests
curl -u admin:admin -X POST http://localhost:4502/bin/mcp \
  -H "Content-Type: application/json" \
  -d '{"tool": "recent-requests", "parameters": {"count": 5}}'
```

### 4.5 Understand Each Tool

**aem-logs Tool:**
- Retrieves filtered log entries
- Parameters: `pattern`, `level`, `count`
- Use case: Debug errors, trace request flow

**diagnose-osgi-bundle Tool:**
- Reports bundle issues
- Parameters: `bundle` (symbolic name)
- Use case: Why isn't my bundle activating?

**recent-requests Tool:**
- HTTP requests with Sling traces
- Parameters: `path`, `count`
- Use case: Debug 404s, understand request routing

**Checkpoint 3:** Screenshots of all three MCP tools returning data.

---

## Part 5: Dispatcher MCP Server (30 minutes)

### 5.1 Prerequisites

```bash
# Verify Docker is running
docker info | head -5

# Check for Dispatcher SDK
ls /path/to/dispatcher-sdk/
```

### 5.2 Extract and Configure Dispatcher SDK

**macOS/Linux:**

```bash
# Make installer executable
chmod +x aem-sdk-dispatcher-tools-<version>-unix.sh

# Run installer
./aem-sdk-dispatcher-tools-<version>-unix.sh

# Navigate to extracted directory
cd dispatcher-sdk-<version>

# Make MCP script executable
chmod +x ./bin/docker_run_mcp.sh
```

### 5.3 Test Dispatcher MCP

```bash
# Run in test mode
./bin/docker_run_mcp.sh test
```

Expected output: Docker container starts, MCP server initializes.

### 5.4 Configure Dispatcher Path

Set environment variable pointing to your dispatcher config:

```bash
export DISPATCHER_CONFIG_PATH=/path/to/project/dispatcher/src
```

### 5.5 Test Dispatcher Tools

```bash
# In a separate terminal, start the MCP server
./bin/docker_run_mcp.sh serve

# Test validation
curl -X POST http://localhost:8080 \
  -H "Content-Type: application/json" \
  -d '{"tool": "validate"}'
```

**Checkpoint 4:** Dispatcher MCP server running and responding.

---

## Part 6: IDE Configuration (30 minutes)

### 6.1 Cursor Configuration

Create `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "aem-cs-sdk": {
      "type": "streamable-http",
      "url": "http://localhost:4502/bin/mcp",
      "headers": {
        "Authorization": "Basic YWRtaW46YWRtaW4="
      }
    },
    "aem-dispatcher-mcp": {
      "command": "/path/to/dispatcher-sdk/bin/docker_run_mcp.sh",
      "env": {
        "DOCKER_API_VERSION": "1.43",
        "AEM_DEPLOYMENT_MODE": "cloud",
        "MCP_LOG_LEVEL": "info",
        "DISPATCHER_CONFIG_PATH": "/path/to/project/dispatcher/src"
      }
    }
  }
}
```

### 6.2 Claude Code Configuration

```bash
# Add AEM MCP server
claude mcp add aem-cs-sdk --type http \
  --url http://localhost:4502/bin/mcp \
  --header "Authorization: Basic YWRtaW46YWRtaW4="

# Add Dispatcher MCP server
claude mcp add aem-dispatcher-mcp --type command \
  --command /path/to/dispatcher-sdk/bin/docker_run_mcp.sh

# Verify
claude mcp list
```

### 6.3 GitHub Copilot (IntelliJ)

1. Open Settings > Tools > GitHub Copilot > Model Context Protocol
2. Add AEM Quickstart MCP:
   - Name: `aem-cs-sdk`
   - Type: HTTP
   - URL: `http://localhost:4502/bin/mcp`
   - Authorization header

3. Add Dispatcher MCP:
   - Name: `aem-dispatcher-mcp`
   - Type: Command
   - Command path to `docker_run_mcp.sh`

### 6.4 Restart All IDEs

Close and reopen each configured IDE to load new settings.

### 6.5 Verification Test

In each IDE's AI chat, ask:

```
What OSGi bundles are currently in ERROR state on my AEM instance?
```

The AI should query the MCP server and return actual data.

**Checkpoint 5:** All IDEs showing MCP integration working.

---

## Part 7: AI-Assisted Component Creation (30 minutes)

### 7.1 Simple Component Test

Request in AI chat:

```
Create a notification banner component with:
- Banner type (info, warning, error, success)
- Title field
- Message rich text field
- Dismissible toggle
- Link URL and text
- Icon selection
```

### 7.2 Review Generated Artifacts

Verify generated files:

```
ui.apps/src/main/content/jcr_root/apps/saw/components/notificationbanner/
├── .content.xml
├── _cq_dialog/.content.xml
├── notificationbanner.html

core/src/main/java/com/adobe/aem/guides/saw/core/models/
├── NotificationBanner.java
└── impl/NotificationBannerImpl.java

core/src/test/java/com/adobe/aem/guides/saw/core/models/impl/
└── NotificationBannerImplTest.java
```

### 7.3 Build and Deploy

```bash
mvn clean install -PautoInstallSinglePackage
```

### 7.4 Verify in AEM

1. Open http://localhost:4502/editor.html/content/saw/en.html
2. Add the new component to a page
3. Configure and preview

### 7.5 Complex Component Test

Request a more complex component:

```
Create a product comparison table component with:
- Table title
- Multifield of products, each with:
  - Product name
  - Product image
  - Price
  - Feature list (multifield of feature name + checkmark)
- Maximum 4 products
- Responsive: cards on mobile, table on desktop
- Sort by price option
```

**Checkpoint 6:** Both components built and working in AEM.

---

## Part 8: Debugging with MCP (15 minutes)

### 8.1 Intentional Error

Create a bundle error by adding a bad import:

```java
// In any Java file, add:
import com.nonexistent.package.DoesNotExist;
```

Build and deploy.

### 8.2 Debug with AI

Ask the AI:

```
Why isn't my core bundle activating? Check the OSGi state and diagnose the issue.
```

The AI should:
1. Use `diagnose-osgi-bundle` tool
2. Identify the missing package
3. Suggest resolution

### 8.3 Request Tracing

Ask:

```
Trace the request for /content/saw/en.html and show me the processing pipeline
```

**Checkpoint 7:** Demonstrated AI debugging using MCP tools.

---

## Lab Validation

### Completion Checklist

- [ ] All four AEM skills installed and verified
- [ ] AGENTS.md generated with project-specific customizations
- [ ] AEM Quickstart MCP server installed and responding
- [ ] Dispatcher MCP server configured (or skipped if no Dispatcher SDK)
- [ ] At least one IDE fully configured with MCP integration
- [ ] Simple component created via AI assistance
- [ ] Complex component created via AI assistance
- [ ] Debug workflow demonstrated with MCP tools

### Knowledge Check

1. What's the difference between AGENTS.md and .aem-skills-config.yaml?
2. Name three tools available in the AEM Quickstart MCP server
3. How do skills like `create-component` detect your project conventions?
4. What environment variables are required for Dispatcher MCP?
5. Why would you use MCP servers instead of just reading documentation?

---

## Troubleshooting Guide

### Skills Installation Issues

| Problem | Solution |
|---------|----------|
| Skills not found | Verify internet connectivity, retry installation |
| Permission denied | Run with sudo or check npm permissions |
| Old skills version | Update: `npx skills update --all` |

### MCP Server Issues

| Problem | Solution |
|---------|----------|
| 404 on /bin/mcp | Package not installed, check Package Manager |
| Connection refused | AEM not running on expected port |
| Authentication error | Verify base64 encoding: `echo -n "admin:admin" \| base64` |
| Dispatcher MCP fails | Check Docker is running, verify DISPATCHER_CONFIG_PATH |

### IDE Integration Issues

| Problem | Solution |
|---------|----------|
| MCP not detected | Restart IDE, verify JSON syntax |
| AGENTS.md ignored | Check file is in project root, correct case |
| Slow responses | Check AEM performance, network latency |

---

## Summary

In this comprehensive lab, you've:

1. **Understood** the AI development ecosystem for AEM
2. **Installed** Adobe's AEM Agent Skills using multiple methods
3. **Generated** and customized project-specific context files
4. **Configured** both AEM and Dispatcher MCP servers
5. **Set up** multiple IDEs for AI-assisted development
6. **Created** components using AI with proper testing
7. **Debugged** AEM issues using MCP-powered AI assistance

These foundational skills will be applied throughout the remaining course.

---

## Next Steps

Proceed to [Lab 01: Setup and Foundations](../lab-01-environment-setup/README.md)

---

## Resources

- [Adobe Skills Repository (Beta)](https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service)
- [Adobe AI Tools Documentation](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/ai-in-aem/local-development-with-ai-tools)
- [Course Module: AI Tools Setup](../../05-ai-tools-setup/README.md)
- [Beta Feedback](mailto:aemcs-ai-ide-tools-feedback@adobe.com)
