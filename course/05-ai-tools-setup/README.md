# Local Development with AI Tools for AEM

## Module Overview

This module covers AI-assisted development for AEM Java stack projects using modern AI coding tools. You'll learn to leverage **Claude Code**, **Cursor**, **GitHub Copilot**, and specialized **MCP Servers** to accelerate AEM development while maintaining enterprise-grade quality.

> **Note**: This content is based on Adobe's official documentation for [Local Development with AI Tools](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/ai-in-aem/local-development-with-ai-tools). The feature is currently in **beta**.

---

## Learning Objectives

By the end of this module, you will be able to:

- [ ] Generate project-specific AGENTS.md files for AI coding tools
- [ ] Install and configure Adobe's AEM Agent Skills
- [ ] Set up MCP Servers for runtime AEM integration
- [ ] Configure Cursor, GitHub Copilot, and Claude Code for AEM development
- [ ] Use AI-assisted component creation and dispatcher configuration

---

## Core Components

The AI-assisted development approach for AEM combines four complementary components:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AI-ASSISTED AEM DEVELOPMENT STACK                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐        │
│   │   AGENTS.md     │    │  Agent Skills   │    │  MCP Servers    │        │
│   │   (Context)     │    │  (Workflows)    │    │  (Runtime)      │        │
│   │                 │    │                 │    │                 │        │
│   │  • Project info │    │  • Component    │    │  • AEM Quickstart│       │
│   │  • Module map   │    │    creation     │    │  • Dispatcher    │       │
│   │  • Add-on detect│    │  • Dispatcher   │    │  • Logs & Debug  │       │
│   │  • Best practices│   │  • Workflow     │    │  • OSGi Bundle   │       │
│   └────────┬────────┘    └────────┬────────┘    └────────┬────────┘        │
│            │                      │                      │                  │
│            └──────────────────────┴──────────────────────┘                  │
│                                   │                                         │
│                                   ▼                                         │
│                    ┌─────────────────────────────┐                          │
│                    │      AI Coding Tools        │                          │
│                    │  Claude Code | Cursor | Copilot                        │
│                    └─────────────────────────────┘                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Module Contents

| Section | Topic | Duration |
|---------|-------|----------|
| [1. AGENTS.md Setup](./01-agents-md.md) | Project-specific AI context files | 30 min |
| [2. Agent Skills](./02-agent-skills.md) | Installing and using Adobe's AEM skills | 45 min |
| [3. MCP Servers](./03-mcp-servers.md) | Runtime integration with AEM SDK | 60 min |
| [4. IDE Configuration](./04-ide-configuration.md) | Configuring Cursor, Copilot, and Claude Code | 30 min |

---

## Prerequisites

### Software Requirements

| Component | Version | Purpose |
|-----------|---------|---------|
| AEM SDK | 2026.2.24678+ | MCP server support |
| Docker Desktop | 4.x+ | Dispatcher MCP server |
| Node.js | 18+ | NPX skill installation |
| GitHub CLI | 2.x+ | gh-upskill extension |

### AI Tools (Choose One or More)

- **Claude Code**: Anthropic's CLI for Claude
- **Cursor**: AI-first code editor
- **GitHub Copilot**: Available in VS Code, IntelliJ, etc.

---

## Quick Start

### 1. Bootstrap AGENTS.md

The fastest way to start is using the `ensure-agents-md` skill:

```bash
# Using Claude Code
/plugin marketplace add adobe/skills#beta
/plugin install aem-cloud-service@adobe-skills

# Then run the skill
ensure-agents-md
```

This generates tailored AGENTS.md and CLAUDE.md files by analyzing your `pom.xml`.

### 2. Install Agent Skills

Choose your preferred installation method:

```bash
# Claude Code (recommended)
/plugin marketplace add adobe/skills#beta
/plugin install aem-cloud-service@adobe-skills

# NPX (works with any tool)
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service --all

# GitHub CLI
gh extension install trieloff/gh-upskill
gh upskill adobe/skills --branch beta --path skills/aem/cloud-service --all
```

### 3. Configure MCP Servers

Download and install the MCP server package from Software Distribution Portal:

```
com.adobe.aem:com.adobe.aem.mcp-server-contribs-content
```

Install via `/crx/packmgr` on your local AEM instance.

---

## Available Agent Skills

Adobe publishes specialized skills for AEM development:

| Skill | Description |
|-------|-------------|
| `ensure-agents-md` | Bootstraps project-specific AGENTS.md and CLAUDE.md files |
| `create-component` | Scaffolds complete AEM components with dialogs, HTL, Sling Models, tests, and clientlibs |
| `dispatcher` | AI-powered configuration assistant for authoring, advisory, incidents, performance, and security |
| `workflow` | Comprehensive AEM Workflow support including design, custom steps, debugging, and production incidents |

---

## Available MCP Server Tools

### AEM Quickstart MCP Server

| Tool | Description |
|------|-------------|
| `aem-logs` | Retrieves filterable log entries by pattern, level, and count |
| `diagnose-osgi-bundle` | Reports missing packages, unsatisfied references, configuration issues |
| `recent-requests` | Returns HTTP requests with full Sling processing traces |

### Dispatcher MCP Server

| Tool | Description |
|------|-------------|
| `validate` | Configuration syntax and best-practice validation |
| `lint` | Mode-aware static analysis |
| `sdk` | Workflow execution (validate, docker-test, diff-baseline, etc.) |
| `trace_request` | Runtime request behavior analysis |
| `inspect_cache` | Cache and docroot examination |
| `monitor_metrics` | Log metrics extraction |
| `tail_logs` | Real-time log streaming |

---

## Integration with Course Methodologies

This module integrates with the course's core methodologies:

### BMAD Integration
- Phase 03 (Architecture): Use `create-component` skill for component specifications
- Phase 04 (Development): AI-assisted implementation with MCP debugging

### BEAD Integration
- Tasks can reference specific skills: `uses: create-component`
- MCP tools enable runtime debugging during task execution

### GasTown Integration
- **AEM Coder** agent can leverage `create-component` skill
- **AEM DevOps** agent uses Dispatcher MCP for configuration
- **AEM Tester** uses `aem-logs` and `recent-requests` for debugging

---

## Beta Notice

> **Important**: This feature is currently in **beta**.
>
> - Feedback: aemcs-ai-ide-tools-feedback@adobe.com
> - Beta releases carry no warranty and are provided "AS IS"
> - No formal support obligations during beta period

---

## Next Steps

1. Continue to [AGENTS.md Setup](./01-agents-md.md)
2. Or jump to [Lab: AI Tools Setup](../labs/lab-00-ai-tools-setup/README.md)

---

## Additional Resources

- [Adobe Skills Repository (Beta)](https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service/skills)
- [Edge Delivery Services AI Tools](https://www.aem.live/developer/ai-coding-agents)
- [Software Distribution Portal](https://experience.adobe.com/#/downloads)
