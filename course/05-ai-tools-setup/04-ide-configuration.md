# IDE Configuration for AEM AI Development

## Overview

This guide covers configuring popular AI coding tools for AEM development, including Claude Code, Cursor, and GitHub Copilot. Each tool has specific configuration requirements for optimal AEM integration.

---

## Claude Code

### Installation

```bash
# Install via npm
npm install -g @anthropic/claude-code

# Or via Homebrew (macOS)
brew install claude-code

# Verify installation
claude --version
```

### AEM Skills Setup

```bash
# Add Adobe skills marketplace
/plugin marketplace add adobe/skills#beta

# Install AEM Cloud Service skills
/plugin install aem-cloud-service@adobe-skills

# List installed plugins
/plugin list
```

### MCP Server Configuration

```bash
# Add AEM Quickstart MCP
claude mcp add aem-cs-sdk --type http \
  --url http://localhost:4502/bin/mcp \
  --header "Authorization: Basic YWRtaW46YWRtaW4="

# Add Dispatcher MCP
claude mcp add aem-dispatcher-mcp --type command \
  --command /path/to/dispatcher-sdk/bin/docker_run_mcp.sh

# List MCP servers
claude mcp list
```

### Project Configuration

Create `CLAUDE.md` in your project root:

```markdown
# AEM Project: WKND

## Project Context
This is an AEM Cloud Service project using standard archetype structure.

## Development Commands
- Build: `mvn clean install -PautoInstallPackage`
- Test: `mvn test`
- Deploy: `mvn clean install -PautoInstallSinglePackage`

## Key Directories
- core/: Java OSGi bundle
- ui.apps/: AEM application
- ui.content/: Content packages
- dispatcher/: Dispatcher configuration

## Coding Standards
- Java 11 compatible
- OSGi Declarative Services
- Sling Models for components
- HTL for templates
```

### Recommended Settings

```json
{
  "claude": {
    "model": "claude-sonnet-4-20250514",
    "maxTokens": 4096,
    "temperature": 0.1
  }
}
```

---

## Cursor

### Installation

Download from [cursor.com](https://cursor.com) and install for your platform.

### AEM Extension Setup

1. Open Extensions (Cmd+Shift+X / Ctrl+Shift+X)
2. Search for and install:
   - **AEM IDE Extension**
   - **Java Extension Pack**
   - **XML Tools**

### MCP Configuration

Create or update `~/.cursor/mcp.json`:

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

### Skills Integration

Install skills via NPX for Cursor:

```bash
# Install all AEM skills
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service --all

# Generate AGENTS.md
npx skills run ensure-agents-md
```

### Cursor Settings

In Settings (Cmd+, / Ctrl+,), configure:

```json
{
  "cursor.ai.model": "claude-sonnet-4-20250514",
  "cursor.ai.contextWindow": 16000,
  "cursor.ai.useAgentsmd": true,
  "editor.formatOnSave": true,
  "java.format.settings.url": "./.vscode/eclipse-formatter.xml"
}
```

### Workspace Configuration

Create `.cursor/settings.json` in your project:

```json
{
  "ai.contextFiles": [
    "AGENTS.md",
    "CLAUDE.md",
    "pom.xml",
    ".aem-skills-config.yaml"
  ],
  "ai.ignoreFiles": [
    "**/target/**",
    "**/node_modules/**",
    "**/*.class"
  ]
}
```

---

## GitHub Copilot

### Installation

**VS Code:**
1. Open Extensions (Cmd+Shift+X / Ctrl+Shift+X)
2. Search "GitHub Copilot"
3. Install and sign in with GitHub

**IntelliJ:**
1. Open Settings > Plugins
2. Search "GitHub Copilot"
3. Install and restart
4. Sign in via Tools > GitHub Copilot > Login

### MCP Configuration (IntelliJ)

1. Navigate to **Tools > GitHub Copilot > Model Context Protocol**
2. Click **Add Server**
3. Configure each server:

**AEM Quickstart:**
```
Name: aem-cs-sdk
Type: HTTP (Streamable)
URL: http://localhost:4502/bin/mcp
Headers:
  Authorization: Basic YWRtaW46YWRtaW4=
```

**Dispatcher:**
```
Name: aem-dispatcher-mcp
Type: Stdio Command
Command: /path/to/dispatcher-sdk/bin/docker_run_mcp.sh
Working Directory: /path/to/your/project
Environment Variables:
  DOCKER_API_VERSION=1.43
  DISPATCHER_CONFIG_PATH=/path/to/project/dispatcher/src
```

### Skills Integration

Use GitHub CLI for Copilot skills:

```bash
# Install gh-upskill extension
gh extension install trieloff/gh-upskill

# Add AEM skills
gh upskill adobe/skills --branch beta --path skills/aem/cloud-service --all
```

### Copilot Settings

Configure in `.github/copilot-instructions.md`:

```markdown
# AEM Development Instructions

## Context
This is an AEM Cloud Service project following Adobe best practices.

## Code Generation Rules
- Use OSGi Declarative Services annotations
- Prefer Sling Models for component logic
- Use HTL (not JSP) for templates
- Follow AEM naming conventions

## Testing Requirements
- Include unit tests for all Sling Models
- Use AEM Mocks framework
- Minimum 80% coverage for new code

## Security
- Never hardcode credentials
- Use OSGi configurations for secrets
- Follow OWASP guidelines
```

---

## VS Code (Without Copilot)

### AEM Extensions

Install these extensions:

| Extension | Purpose |
|-----------|---------|
| AEM IDE Extension | Component sync, dialogs |
| Java Extension Pack | Java development |
| XML Tools | XML editing |
| HTL Language Support | HTL syntax |

### Tasks Configuration

Create `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "AEM: Build and Deploy",
      "type": "shell",
      "command": "mvn clean install -PautoInstallSinglePackage",
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "problemMatcher": "$maven"
    },
    {
      "label": "AEM: Build Core Bundle",
      "type": "shell",
      "command": "mvn clean install -PautoInstallBundle -pl core",
      "group": "build",
      "problemMatcher": "$maven"
    },
    {
      "label": "AEM: Run Tests",
      "type": "shell",
      "command": "mvn test",
      "group": "test",
      "problemMatcher": "$maven"
    }
  ]
}
```

### Launch Configuration

Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug AEM (Remote)",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    },
    {
      "type": "java",
      "name": "Run Tests",
      "request": "launch",
      "mainClass": "",
      "projectName": "core"
    }
  ]
}
```

---

## IntelliJ IDEA

### AEM Plugin Setup

1. Open Settings > Plugins
2. Install:
   - **AEM Support**
   - **Adobe Experience Manager (Helix)**

### Project Import

1. File > Open
2. Select `pom.xml`
3. Choose "Open as Project"
4. Wait for Maven import

### Run Configurations

Create run configuration for deployment:

1. Run > Edit Configurations
2. Add Maven configuration:
   - Command: `clean install -PautoInstallSinglePackage`
   - Working directory: `$ProjectFileDir$`

### Debugging

Enable remote debugging:

```bash
# Start AEM with debug port
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar aem-author-p4502.jar
```

Then attach IntelliJ debugger to port 5005.

---

## Common Configuration

### AGENTS.md Placement

Ensure AGENTS.md is in project root:

```
my-aem-project/
├── AGENTS.md          # AI context (all tools)
├── CLAUDE.md          # Claude-specific context
├── .aem-skills-config.yaml
├── pom.xml
├── core/
├── ui.apps/
└── ...
```

### Git Configuration

Add to `.gitignore`:

```gitignore
# AI tool caches
.cursor/cache/
.claude/
.copilot/

# Keep configuration
!.cursor/settings.json
```

Add to version control:
```bash
git add AGENTS.md CLAUDE.md .aem-skills-config.yaml
git commit -m "Add AI tools configuration"
```

### Environment Variables

Set system-wide or in shell profile:

```bash
# AEM configuration
export AEM_AUTHOR_URL="http://localhost:4502"
export AEM_AUTHOR_USER="admin"
export AEM_AUTHOR_PASSWORD="admin"

# Dispatcher SDK
export DISPATCHER_SDK_PATH="/path/to/dispatcher-sdk"

# Java
export JAVA_HOME="/path/to/java11"
```

---

## Verification Checklist

After configuration, verify each component:

### 1. AGENTS.md Loading
```bash
# Claude Code
claude --print-system-prompt | grep -i "agents"

# Cursor - open new chat, check context

# Copilot - check instructions are applied
```

### 2. Skills Available
```bash
# Claude Code
/plugin list

# NPX
npx skills list

# GitHub CLI
gh upskill list
```

### 3. MCP Servers Connected
```bash
# Test AEM MCP
curl -u admin:admin http://localhost:4502/bin/mcp

# Test Dispatcher MCP (should start Docker)
/path/to/dispatcher-sdk/bin/docker_run_mcp.sh test
```

### 4. End-to-End Test

Try creating a component:

```
Create a simple text component with a title field
```

The AI should:
1. Detect your project structure
2. Use correct package names
3. Generate HTL, dialog, and Sling Model
4. Place files in correct locations

---

## Troubleshooting

### AGENTS.md Not Found

- Verify file is in project root
- Check file name is exactly `AGENTS.md` (case-sensitive)
- Restart IDE/tool after adding

### MCP Connection Failed

- Verify AEM is running
- Check authorization header encoding
- Ensure MCP package is installed

### Skills Not Executing

- Verify internet connectivity
- Check GitHub authentication
- Try reinstalling skills

### Performance Issues

- Reduce context window size
- Add more files to ignore list
- Close unused IDE tabs

---

## Next Steps

Complete the hands-on lab: [Lab: AI Tools Setup](../labs/lab-00-ai-tools-setup/README.md)
