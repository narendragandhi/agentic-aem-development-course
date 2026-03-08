# Lab 18: Goose AI Agent Integration
# Comprehensive Lab - 4 hours

## Objective

Integrate Goose AI agent for autonomous development tasks. Learn to use Goose for code generation, testing, debugging, and complex engineering workflows in AEM projects.

---

## Prerequisites

- Lab 1-8 completed
- Lab 15 (Code Quality) helpful
- OpenAI/Anthropic API key (optional)
- Claude CLI or OpenAI key configured

---

## Overview

### What is Goose?

Goose is an open-source AI agent from Block that:
- Executes autonomous engineering tasks
- Reads/edits/tests code
- Uses any LLM (Claude, GPT-4, etc.)
- Extensible with MCP servers

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         GOOSE AI AGENT                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐           │
│    │  READ    │───▶│  THINK   │───▶│  ACT     │───▶│  TEST    │           │
│    │  Code    │    │  Plan    │    │  Execute  │    │  Verify  │           │
│    └──────────┘    └──────────┘    └──────────┘    └──────────┘           │
│                                                                     │       │
│       ◀──────────────────────────────────────────────────────────────┘       │
│                                                                             │
│    Capabilities:                                                            │
│    • Read & write files                                                    │
│    • Execute shell commands                                                │
│    • Run tests & debug                                                    │
│    • Use tools (MCP servers)                                              │
│    • Orchestrate workflows                                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Why Goose for AEM?

| Benefit | Description |
|---------|-------------|
| Autonomous | Completes tasks without constant guidance |
| Aware | Understands AEM patterns (OSGi, Sling, HTL) |
| Tool-enabled | Uses MCP for AEM-specific actions |
| Extensible | Custom recipes for AEM workflows |

---

## Part 1: Goose Installation (30 min)

### 1.1 Install Goose CLI

```bash
# macOS
brew install goose-cli

# Or download binary
curl -L -o goose https://github.com/block/goose/releases/latest/download/goose-darwin-arm64
chmod +x goose
sudo mv goose /usr/local/bin/

# Verify
goose --version
```

### 1.2 Configure Goose

Create `~/.config/goose/config.yaml`:

```yaml
# Goose Configuration
goose:
  # LLM Provider
  provider: anthropic  # or openai
  
  anthropic:
    api_key: ${ANTHROPIC_API_KEY}
    model: claude-sonnet-4-20250514
    
  openai:
    api_key: ${OPENAI_API_KEY}
    model: gpt-4o
    
  # Max tokens per request
  max_tokens: 100000
  
  # Temperature
  temperature: 0.7
  
  # Retry settings
  max_retries: 3
  retry_delay: 2s
```

### 1.3 Set Environment Variables

```bash
# Add to ~/.bashrc or ~/.zshrc
export ANTHROPIC_API_KEY="sk-ant-..."
export OPENAI_API_KEY="sk-..."

# Reload
source ~/.zshrc
```

---

## Part 2: AEM Recipe for Goose (45 min)

### 2.1 Create AEM Recipe

Create `agents/recipes/aem-recipe.yaml`:

```yaml
name: "AEM Workflow Developer"
description: "Autonomous agent for building AEM workflows"

# Model configuration
model:
  provider: anthropic
  model: claude-sonnet-4-20250514
  max_tokens: 80000

# Available tools
tools:
  - name: read
    description: "Read file contents"
  - name: write
    description: "Write/create files"
  - name: edit
    description: "Edit existing files"
  - name: bash
    description: "Execute shell commands"
  - name: mvn
    description: "Run Maven commands"
  - name: aem-deploy
    description: "Deploy to AEM"

# Context - files to load
context:
  files:
    - path: "course/01-prd/secure-asset-workflow-prd.md"
    - path: "course/02-domain-models/README.md"
    - path: "course/03-architecture/README.md"

# Instructions
instructions: |
  You are an AEM workflow development expert. Follow these rules:
  
  1. Always use TDD - write tests first, then implementation
  2. Follow AEM best practices:
     - Use @Component, @Service annotations
     - Inject dependencies with @Reference
     - Use Sling Models for HTL data
  3. Write comprehensive Javadoc
  4. Test before committing

# Allowed commands
allowed_commands:
  - mvn clean install
  - mvn test
  - mvn verify
  - docker
  - git

# Exit criteria
exit_criteria:
  - "mvn test passes"
  - "Code follows AEM patterns"
  - "Tests have >80% coverage"
```

### 2.2 Register Recipe

```bash
# Add recipe to Goose
goose recipe add agents/recipes/aem-recipe.yaml

# List recipes
goose recipe list
```

---

## Part 3: Using Goose for AEM Development (60 min)

### 3.1 Generate Service Implementation

```bash
# Run Goose to generate AntivirusScanService
goose run \
  --recipe aem-recipe \
  --task "Implement AntivirusScanService interface and implementation that:
    - Scans files using ClamAV
    - Returns ScanResult with status (CLEAN/INFECTED/ERROR)
    - Handles timeouts gracefully
    - Logs all scan operations
    - Follows OSGi service patterns
    - Write tests first (TDD)" \
  --context "project: secure-asset-workflow, module: core"
```

### 3.2 Debug Failing Tests

```bash
# Let Goose debug test failures
goose run \
  --recipe aem-recipe \
  --task "Debug why SecurityScannerServiceSpec tests are failing.
    Run the tests first to see the failures, then fix the issues." \
  --context "Test output: 3 failures in XSS detection"
```

### 3.3 Generate Integration Tests

```bash
# Generate integration tests
goose run \
  --recipe aem-recipe \
  --task "Write integration tests for QuarantineProcess that:
    - Tests moving assets to quarantine folder
    - Tests creating quarantine metadata
    - Tests audit logging
    - Uses AEM Mock context" \
  --context "module: integration-tests"
```

---

## Part 4: MCP Integration (45 min)

### 4.1 AEM MCP Server

Create MCP server for AEM:

```yaml
# mcp-servers/aem-mcp.yaml
name: aem-mcp
version: 1.0.0

description: AEM as a Cloud Service MCP Server

capabilities:
  - name: deploy
    description: Deploy package to AEM
    params:
      - name: path
        type: string
      - name: environment
        type: string
        default: author

  - name: query
    description: Query JCR content
    params:
      - name: query
        type: string
      - name: limit
        type: number

  - name: activate
    description: Activate content to publish
    params:
      - name: path
        type: string

  - name: workflow
    description: Trigger workflow
    params:
      - name: model
        type: string
      - name: payload
        type: string

config:
  aem_host: ${AEM_HOST}
  api_key: ${AEM_API_KEY}
```

### 4.2 Configure Goose with MCP

Update `~/.config/goose/config.yaml`:

```yaml
mcp_servers:
  - name: aem
    config: mcp-servers/aem-mcp.yaml
  - name: jira
    config: mcp-servers/jira-mcp.yaml
  - name: slack
    config: mcp-servers/slack-mcp.yaml
```

### 4.3 Use MCP in Goose

```bash
# Deploy to AEM using MCP
goose run \
  --recipe aem-recipe \
  --task "Deploy the built package to AEM author and verify:
    1. Use mcp.aem.deploy to upload package
    2. Use mcp.aem.query to verify installation
    3. Report status"
```

---

## Part 5: Workflow Automation (45 min)

### 5.1 Complete Feature Workflow

Create `workflows/aem-feature.yaml`:

```yaml
name: "AEM Feature Development"
description: "Complete workflow from task to deployed feature"

steps:
  - name: analyze
    description: "Analyze requirements and create plan"
    tool: context.load
    input: ["PRD", "Architecture"]
    
  - name: implement
    description: "Implement feature with TDD"
    tool: goose.run
    recipe: aem-recipe
    tasks:
      - "Write specification tests"
      - "Implement to pass tests"
      - "Refactor for quality"
      
  - name: test
    description: "Run all tests"
    tool: bash
    command: "mvn verify"
    
  - name: deploy
    description: "Deploy to AEM"
    tool: mcp.aem.deploy
    environment: author
    
  - name: verify
    description: "Verify deployment"
    tool: mcp.aem.query
    query: "SELECT * FROM dam:Asset WHERE ..."
    
  - name: report
    description: "Report completion"
    tool: slack.notify
    channel: "#aem-dev"
```

### 5.2 Run Workflow

```bash
# Run complete workflow
goose workflow run workflows/aem-feature.yaml \
  --input "feature: implement-quarantine-process"
```

---

## Part 6: Self-Improvement (30 min)

### 6.1 Goose Self-Test

```bash
# Run Goose's self-test to verify capabilities
goose self-test
```

### 6.2 Custom Recipe for This Course

```yaml
# agents/recipes/bmad-goose.yaml
name: "BMAD Agent Developer"
description: "AI agent following BMAD methodology"

instructions: |
  Follow the BMAD (Breakthrough Method for Agile Development):
  
  Phase 00: Initialize project
  Phase 01: Understand PRD requirements
  Phase 02: Create domain models
  Phase 03: Design architecture
  Phase 04: Implement with TDD
  Phase 05: Test thoroughly
  Phase 06: Deploy to operations
  
  Always use:
  - BEAD for task tracking
  - TDD (RED-GREEN-REFACTOR)
  - AEM best practices
  
  Track progress in BEAD tasks.
```

---

## Verification Checklist

- [ ] Goose installed and configured
- [ ] API keys set
- [ ] AEM recipe created
- [ ] Goose generates service code
- [ ] Goose debugs test failures
- [ ] MCP server configured
- [ ] Complete workflow runs

---

## Key Takeaways

1. **Goose is autonomous** - Give it a task, it completes it
2. **Recipes make it AEM-aware** - Custom prompts for AEM patterns
3. **MCP extends capabilities** - AEM, Jira, Slack integration
4. **Workflows automate** - End-to-end development pipelines

---

## Next Steps

1. Create custom recipes for your projects
2. Set up MCP servers for your tools
3. Build complete development workflows
4. Integrate with CI/CD
5. Share recipes with team

---

## References

- [Goose Documentation](https://block.github.io/goose/docs/)
- [Goose GitHub](https://github.com/block/goose)
- [MCP Servers](https://modelcontextprotocol.io/)
- [AEM Best Practices](https://experienceleague.adobe.com/docs/experience-manager-65/developing/bestpractices/best-practices.html)
