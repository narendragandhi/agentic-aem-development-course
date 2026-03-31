# Lab 00: AI Tools Setup for AEM Development

## Lab Overview

| Attribute | Value |
|-----------|-------|
| **Duration** | 90 minutes |
| **Difficulty** | Beginner |
| **Prerequisites** | AEM SDK installed, Docker Desktop running |
| **Objectives** | Configure AI coding tools for AEM development |

---

## Learning Objectives

By completing this lab, you will:

- [ ] Generate project-specific AGENTS.md using Adobe skills
- [ ] Install and verify Adobe's AEM Agent Skills
- [ ] Configure MCP servers for AEM runtime integration
- [ ] Test AI-assisted component creation

---

## Prerequisites

Before starting, ensure you have:

- [ ] AEM SDK (2026.2.24678+) running on localhost:4502
- [ ] Docker Desktop 4.x+ running
- [ ] Node.js 18+ installed
- [ ] One of: Claude Code, Cursor, or GitHub Copilot configured
- [ ] The course project cloned and buildable

Verify your environment:

```bash
# Check AEM is running
curl -s -u admin:admin http://localhost:4502/system/console/bundles.json | head -c 100

# Check Docker
docker info | head -5

# Check Node.js
node --version
```

---

## Exercise 1: Install Adobe AEM Skills (20 minutes)

### Step 1.1: Choose Your Installation Method

**Option A: Claude Code**

```bash
# Add Adobe skills marketplace
/plugin marketplace add adobe/skills#beta

# Install AEM Cloud Service skills
/plugin install aem-cloud-service@adobe-skills

# Verify installation
/plugin list
```

**Option B: NPX**

```bash
# Install all AEM skills
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service --all

# Verify installation
npx skills list
```

**Option C: GitHub CLI**

```bash
# Install gh-upskill extension
gh extension install trieloff/gh-upskill

# Add AEM skills
gh upskill adobe/skills --branch beta --path skills/aem/cloud-service --all

# Verify installation
gh upskill list
```

### Step 1.2: Verify Skills Are Available

You should see these skills listed:
- `ensure-agents-md`
- `create-component`
- `dispatcher`
- `workflow`

**Checkpoint:** Screenshot your skill list showing all four skills installed.

---

## Exercise 2: Generate AGENTS.md (20 minutes)

### Step 2.1: Navigate to Project Root

```bash
cd /path/to/agentic-aem-development-course
```

### Step 2.2: Run ensure-agents-md Skill

**Claude Code:**
```bash
ensure-agents-md
```

**NPX:**
```bash
npx skills run ensure-agents-md
```

**GitHub CLI:**
```bash
gh upskill run ensure-agents-md
```

### Step 2.3: Review Generated Files

The skill should generate:

```
agentic-aem-development-course/
├── AGENTS.md              # AI context file
├── CLAUDE.md              # Claude-specific instructions
└── .aem-skills-config.yaml # Project configuration
```

Open and review `AGENTS.md`:

```bash
cat AGENTS.md
```

### Step 2.4: Customize Configuration

If not present, create `.aem-skills-config.yaml`:

```yaml
configured: true
project: "secure-asset-workflow"
package: "com.adobe.aem.guides.saw.core"
group: "SAW Components"
```

**Checkpoint:** Verify AGENTS.md contains your project modules and detected dependencies.

---

## Exercise 3: Install AEM MCP Server (25 minutes)

### Step 3.1: Download MCP Server Package

1. Go to [Software Distribution Portal](https://experience.adobe.com/#/downloads)
2. Search for: `com.adobe.aem.mcp-server-contribs-content`
3. Download the latest version

### Step 3.2: Install Package

1. Open Package Manager: http://localhost:4502/crx/packmgr
2. Click "Upload Package"
3. Select the downloaded package
4. Click "Install"

### Step 3.3: Verify MCP Endpoint

```bash
curl -u admin:admin http://localhost:4502/bin/mcp
```

Expected response: JSON describing available MCP tools.

### Step 3.4: Test MCP Tools

**Test aem-logs:**
```bash
curl -u admin:admin -X POST http://localhost:4502/bin/mcp \
  -H "Content-Type: application/json" \
  -d '{"tool": "aem-logs", "parameters": {"level": "ERROR", "count": 5}}'
```

**Test diagnose-osgi-bundle:**
```bash
curl -u admin:admin -X POST http://localhost:4502/bin/mcp \
  -H "Content-Type: application/json" \
  -d '{"tool": "diagnose-osgi-bundle", "parameters": {"bundle": "com.adobe.aem.guides.saw.core"}}'
```

**Checkpoint:** Take screenshots showing successful MCP responses.

---

## Exercise 4: Configure IDE Integration (15 minutes)

### Step 4.1: Configure Your IDE

**Cursor** - Create `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "aem-cs-sdk": {
      "type": "streamable-http",
      "url": "http://localhost:4502/bin/mcp",
      "headers": {
        "Authorization": "Basic YWRtaW46YWRtaW4="
      }
    }
  }
}
```

**Claude Code:**

```bash
claude mcp add aem-cs-sdk --type http \
  --url http://localhost:4502/bin/mcp \
  --header "Authorization: Basic YWRtaW46YWRtaW4="
```

**GitHub Copilot (IntelliJ):**

Navigate to Tools > GitHub Copilot > Model Context Protocol and add:
- Name: `aem-cs-sdk`
- Type: HTTP
- URL: `http://localhost:4502/bin/mcp`
- Header: `Authorization: Basic YWRtaW46YWRtaW4=`

### Step 4.2: Restart Your IDE

Close and reopen your IDE to load the new configuration.

### Step 4.3: Verify Integration

In your AI chat, ask:
```
What bundles are currently installed on my AEM instance?
```

The AI should use the MCP server to query AEM and return actual bundle information.

**Checkpoint:** Screenshot showing AI using MCP to query AEM.

---

## Exercise 5: AI-Assisted Component Creation (10 minutes)

### Step 5.1: Test create-component Skill

In your AI coding tool, request:

```
Create a simple alert component with:
- Alert type field (info, warning, error)
- Title text field
- Message text area
- Dismissible checkbox
```

### Step 5.2: Review Generated Files

The AI should generate:
- Component definition (`.content.xml`)
- Touch UI dialog
- HTL template
- Sling Model interface and implementation
- Unit test

### Step 5.3: Verify Build

```bash
mvn clean install -DskipTests
```

**Checkpoint:** Verify the component builds without errors.

---

## Lab Validation

### Checklist

Complete all checkpoints:

- [ ] Skills installed and listed (Exercise 1)
- [ ] AGENTS.md generated with project context (Exercise 2)
- [ ] MCP server responding to requests (Exercise 3)
- [ ] IDE integration working (Exercise 4)
- [ ] Component generation successful (Exercise 5)

### Self-Assessment Questions

1. What is the purpose of AGENTS.md?
2. Name three MCP tools available from the AEM Quickstart server.
3. How does the create-component skill know your project's package name?
4. What's the difference between AGENTS.md and CLAUDE.md?

---

## Troubleshooting

### Skills Not Found

```bash
# Reinstall skills
/plugin uninstall aem-cloud-service@adobe-skills
/plugin install aem-cloud-service@adobe-skills
```

### MCP Server Returns 404

1. Verify package is installed in Package Manager
2. Check AEM logs for errors:
   ```bash
   tail -f crx-quickstart/logs/error.log
   ```

### AGENTS.md Not Detected

1. Verify file is in project root
2. Check file name case: must be `AGENTS.md`
3. Restart IDE after adding file

### Component Build Fails

1. Check generated package matches your project
2. Verify parent POM includes new modules
3. Run `mvn clean install -X` for debug output

---

## Additional Challenges

### Challenge 1: Dispatcher MCP (Optional)

Set up the Dispatcher MCP server:

1. Extract Dispatcher SDK
2. Run `./bin/docker_run_mcp.sh test`
3. Add to IDE MCP configuration
4. Test with: "Validate my dispatcher configuration"

### Challenge 2: Custom AGENTS.md Sections

Add custom sections to AGENTS.md:

```markdown
## Business Context
This project implements a secure asset approval workflow
for enterprise digital asset management.

## Security Requirements
- All assets must be scanned for viruses
- PII detection required for document assets
- Audit trail mandatory for compliance
```

### Challenge 3: Workflow Skill

Use the workflow skill to design a new approval workflow:

```
Design a content review workflow with:
- Author submits content
- Manager reviews and approves/rejects
- Legal review for external content
- Final publish step
```

---

## Summary

In this lab, you:

1. **Installed Adobe's AEM Agent Skills** for AI-assisted development
2. **Generated AGENTS.md** to provide project context to AI tools
3. **Configured MCP servers** for runtime AEM integration
4. **Tested IDE integration** with MCP tools
5. **Created a component** using AI assistance

These tools will be used throughout the remaining course labs to accelerate development.

---

## Next Steps

Proceed to [Lab 01: Setup and Foundations](../lab-01-setup-foundations/README.md)

---

## Resources

- [Adobe Skills Repository](https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service)
- [MCP Server Documentation](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/ai-in-aem/local-development-with-ai-tools)
- [Course Module: AI Tools Setup](../../05-ai-tools-setup/README.md)
