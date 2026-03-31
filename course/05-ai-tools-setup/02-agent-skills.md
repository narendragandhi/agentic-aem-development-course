# Agent Skills Installation and Usage

## Overview

Agent Skills are reusable instruction sets that encode multi-step workflows for AEM development. Adobe publishes official skills via the [adobe/skills repository](https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service/skills) on the beta branch.

---

## Available Skills

| Skill | Description | Use Cases |
|-------|-------------|-----------|
| `ensure-agents-md` | Bootstraps project-specific AGENTS.md and CLAUDE.md files | Project setup, onboarding |
| `create-component` | Scaffolds complete AEM components | New component development |
| `dispatcher` | AI-powered Dispatcher configuration assistant | Config authoring, debugging, security |
| `workflow` | Comprehensive AEM Workflow support | Workflow design, debugging, incidents |

---

## Installation Methods

### Method 1: Claude Code (Recommended)

```bash
# Step 1: Add the Adobe skills marketplace
/plugin marketplace add adobe/skills#beta

# Step 2: Install AEM Cloud Service skills
/plugin install aem-cloud-service@adobe-skills

# Verify installation
/plugin list
```

### Method 2: NPX (Universal)

Works with any AI coding tool:

```bash
# Install all AEM Cloud Service skills
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service --all

# Or install specific skills
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service/skills/create-component

# List installed skills
npx skills list
```

### Method 3: GitHub CLI

```bash
# Step 1: Install the gh-upskill extension
gh extension install trieloff/gh-upskill

# Step 2: Add AEM skills
gh upskill adobe/skills --branch beta --path skills/aem/cloud-service --all

# List skills
gh upskill list
```

---

## Skill: ensure-agents-md

### Purpose

Bootstraps project-specific context files for AI coding tools.

### Usage

```bash
# Claude Code
ensure-agents-md

# NPX
npx skills run ensure-agents-md

# GitHub CLI
gh upskill run ensure-agents-md
```

### What It Does

1. Scans `pom.xml` for module structure
2. Detects AEM add-ons and frameworks
3. Identifies coding conventions
4. Generates `AGENTS.md` and `CLAUDE.md`
5. Creates `.aem-skills-config.yaml` if needed

### Example Output

```
Analyzing project structure...
Found modules: core, ui.apps, ui.content, ui.frontend
Detected: AEM Core Components 2.23.0
Detected: Sling Models, HTL
Generated: AGENTS.md (247 lines)
Generated: CLAUDE.md (89 lines)
Updated: .aem-skills-config.yaml
```

---

## Skill: create-component

### Purpose

Scaffolds complete AEM components with all required artifacts.

### Pre-Configuration

Optionally create `.aem-skills-config.yaml` in your project root:

```yaml
configured: true
project: "wknd"
package: "com.adobe.aem.guides.wknd.core"
group: "WKND Components"
```

If not configured, the skill auto-detects from `pom.xml` and existing components.

### Usage

Simply describe your component in natural language:

```
Create a hero component with:
- Title and subtitle fields
- Background image with mobile variant
- CTA button with link and target
- Optional video background
- Analytics tracking attributes
```

### Generated Artifacts

The skill generates a complete component structure:

```
ui.apps/src/main/content/jcr_root/apps/wknd/components/hero/
├── .content.xml           # Component definition
├── _cq_dialog/
│   └── .content.xml       # Touch UI dialog
├── _cq_editConfig.xml     # Edit configuration
├── hero.html              # HTL template
└── clientlib/             # Component-specific clientlibs
    ├── css/
    └── js/

core/src/main/java/com/adobe/aem/guides/wknd/core/models/
├── Hero.java              # Sling Model interface
└── impl/
    └── HeroImpl.java      # Sling Model implementation

core/src/test/java/com/adobe/aem/guides/wknd/core/models/impl/
└── HeroImplTest.java      # Unit tests
```

### Advanced Features

The skill handles complex patterns:

- **Multifield composites**: Repeated field groups
- **Conditional logic**: Show/hide fields based on selections
- **Nested structures**: Complex content hierarchies
- **Variants**: Multiple display modes
- **Responsive images**: Mobile/tablet/desktop variants

### Example: Complex Component

```
Create a product carousel component:
- Title with optional subtitle
- Multifield of products, each with:
  - Product image
  - Name and description
  - Price with sale price option
  - CTA button
- Auto-play toggle with interval setting
- Navigation style: dots or arrows
- Analytics: track impressions and clicks
```

---

## Skill: dispatcher

### Purpose

AI-powered Dispatcher configuration assistant covering multiple use cases.

### Capabilities

| Mode | Description |
|------|-------------|
| **Authoring** | Create new configurations |
| **Advisory** | Best practice recommendations |
| **Incident** | Troubleshoot production issues |
| **Performance** | Caching optimization |
| **Security** | Security hardening |

### Usage Examples

**Authoring Mode:**
```
Configure dispatcher caching for a new /api/* endpoint that should:
- Cache responses for 5 minutes
- Vary by Authorization header
- Bypass cache for POST requests
```

**Advisory Mode:**
```
Review my dispatcher configuration for security issues
```

**Incident Mode:**
```
I'm getting 403 errors on /content/dam assets after deployment.
Check request logs and dispatcher rules.
```

**Performance Mode:**
```
Optimize caching for my GraphQL endpoint at /graphql/execute.json
```

### Integration with MCP

When the Dispatcher MCP server is configured, the skill can:
- Validate configurations against running Dispatcher
- Trace actual request routing
- Inspect cache state
- Monitor real-time metrics

---

## Skill: workflow

### Purpose

Comprehensive AEM Workflow support for design, development, and operations.

### Capabilities

| Mode | Description |
|------|-------------|
| **Design** | Plan workflow structure and steps |
| **Development** | Create custom process steps |
| **Debugging** | Troubleshoot workflow issues |
| **Incidents** | Handle production workflow problems |

### Usage Examples

**Design Mode:**
```
Design an asset approval workflow with:
- Initial review by asset owner
- Legal review for licensed content
- Marketing approval
- Auto-publish on final approval
- Rejection returns to owner with comments
```

**Development Mode:**
```
Create a custom workflow step that:
- Extracts text from PDF assets
- Runs sentiment analysis
- Adds result as asset metadata
- Logs processing time to audit trail
```

**Debugging Mode:**
```
Workflow instance /var/workflow/instances/xxx is stuck at step 3.
Debug and identify the issue.
```

### Generated Artifacts

For custom workflow steps, the skill generates:

```
core/src/main/java/com/.../workflow/
├── MyWorkflowProcess.java     # Process implementation
└── MyWorkflowProcessTest.java # Unit tests

ui.apps/src/main/content/jcr_root/
└── var/workflow/models/
    └── my-workflow/
        └── .content.xml       # Workflow model definition
```

---

## Skills Best Practices

### 1. Provide Clear Context

Be specific in your requests:

```
# Good
Create a teaser component with title, description, image,
and CTA. Image should support different crops for mobile
and desktop. Include analytics data attributes.

# Less effective
Create a teaser component
```

### 2. Iterate Incrementally

Start simple, then add complexity:

```
# Step 1
Create a basic card component with title and image

# Step 2
Add a multifield for tags

# Step 3
Add responsive image variants
```

### 3. Review Generated Code

Always review AI-generated artifacts:
- Check OSGi annotations are correct
- Verify HTL follows project conventions
- Ensure tests cover edge cases
- Validate dialog fields match requirements

### 4. Version Control

Commit AGENTS.md and .aem-skills-config.yaml:

```bash
git add AGENTS.md CLAUDE.md .aem-skills-config.yaml
git commit -m "Add AI tools configuration"
```

---

## Troubleshooting

### Skills Not Found

```bash
# Verify installation
/plugin list  # Claude Code
npx skills list  # NPX
gh upskill list  # GitHub CLI

# Reinstall if needed
/plugin uninstall aem-cloud-service@adobe-skills
/plugin install aem-cloud-service@adobe-skills
```

### Configuration Not Detected

Ensure `.aem-skills-config.yaml` is in project root:

```yaml
configured: true
project: "your-project"
package: "com.your.package"
group: "Your Components"
```

### Skill Execution Fails

1. Check you're in the project root directory
2. Verify `pom.xml` is valid
3. Ensure required files exist
4. Check network connectivity to skill repository

---

## Next Steps

Continue to [MCP Servers Configuration](./03-mcp-servers.md)
