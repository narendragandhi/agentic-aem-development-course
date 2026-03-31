# AGENTS.md Setup

## Overview

The `AGENTS.md` file is a project-specific markdown file that AI coding tools automatically load at session start. Rather than using a static template, Adobe provides the `ensure-agents-md` skill that generates tailored context by analyzing your project's `pom.xml`.

---

## What Gets Generated

When you run `ensure-agents-md`, the skill:

1. **Reads your `pom.xml`** to discover project modules
2. **Detects add-ons** and framework usage (Commerce, Forms, etc.)
3. **Identifies coding conventions** from existing code
4. **Creates tailored documentation** for AI agents

### Generated Files

| File | Purpose |
|------|---------|
| `AGENTS.md` | Universal AI tool context (Cursor, Copilot, etc.) |
| `CLAUDE.md` | Claude Code specific instructions |
| `.aem-skills-config.yaml` | Project configuration for skills |

---

## AGENTS.md Structure

A typical generated AGENTS.md includes:

```markdown
# Project: WKND Sites

## Project Overview
Enterprise AEM implementation for WKND brand.

## Module Structure
- core/: OSGi bundle with Sling Models and services
- ui.apps/: AEM application components and configurations
- ui.content/: Sample content and templates
- ui.frontend/: Frontend build (webpack/npm)
- dispatcher/: Dispatcher configuration

## Detected Features
- AEM Core Components 2.23.0
- Sling Models
- HTL templating
- OSGi Declarative Services

## Coding Conventions
- Package: com.adobe.aem.guides.wknd
- Component group: WKND Sites
- Java version: 11

## Development Guidelines
- Use @Model annotation for Sling Models
- Follow AEM Best Practices for components
- HTL templates should use data-sly-* attributes
```

---

## Manual Creation

If you prefer to create AGENTS.md manually, include these sections:

### Essential Sections

```markdown
# Project: [Your Project Name]

## Project Overview
[Brief description of the project and its purpose]

## Module Structure
[List each Maven module and its purpose]

## AEM Version
- AEM as a Cloud Service / AEM 6.5.x
- SDK Version: [version]

## Dependencies
- AEM Core Components: [version]
- [Other key dependencies]

## Coding Conventions
- Base package: [your.package.name]
- Component group: [Your Components]
- Naming conventions: [camelCase/kebab-case]

## Domain Knowledge
[Project-specific terminology and business rules]
```

### AEM-Specific Guidance

```markdown
## Component Development
- Use HTL (HTML Template Language), not JSP
- Sling Models should be interface-based
- Dialogs use Granite UI/Coral UI

## Service Development
- Use OSGi Declarative Services (@Component)
- Configuration via @Designate/@ObjectClassDefinition
- Follow OSGi best practices for references

## Testing Requirements
- Unit tests with AEM Mocks
- Integration tests for complex workflows
- Minimum coverage: 80%
```

---

## Skill Configuration

The `ensure-agents-md` skill can be pre-configured with `.aem-skills-config.yaml`:

```yaml
# .aem-skills-config.yaml
configured: true
project: "wknd"
package: "com.adobe.aem.guides.wknd.core"
group: "WKND Components"

# Optional: Additional context
domain:
  - name: "Asset Management"
    description: "DAM workflows and asset processing"
  - name: "Commerce Integration"
    description: "Product catalog and checkout"

# Optional: Code style preferences
conventions:
  java:
    naming: camelCase
    max_line_length: 120
  htl:
    data_sly_style: attribute  # vs block
```

---

## Running the Skill

### Claude Code

```bash
# First time: Install the skills plugin
/plugin marketplace add adobe/skills#beta
/plugin install aem-cloud-service@adobe-skills

# Run the skill
ensure-agents-md
```

### NPX

```bash
# Install skills
npx skills add https://github.com/adobe/skills/tree/beta/skills/aem/cloud-service --all

# Run
npx skills run ensure-agents-md
```

### GitHub CLI

```bash
# Install upskill extension
gh extension install trieloff/gh-upskill

# Add skills
gh upskill adobe/skills --branch beta --path skills/aem/cloud-service --all

# Run
gh upskill run ensure-agents-md
```

---

## Best Practices

### Keep AGENTS.md Updated

- Regenerate when adding new modules
- Update after major dependency changes
- Review after architectural decisions

### Project-Specific Context

Add domain knowledge that helps AI understand your project:

```markdown
## Business Context
- WKND is an outdoor adventure brand
- Primary audience: outdoor enthusiasts 25-45
- Content types: adventures, articles, products

## Integration Points
- Commerce: Magento via CIF
- Analytics: Adobe Analytics
- Personalization: Adobe Target
```

### Security Considerations

**Do NOT include** in AGENTS.md:
- API keys or secrets
- Production URLs or credentials
- Sensitive business logic details
- Customer-specific data

---

## Verification

After generating AGENTS.md, verify it's being loaded:

### Claude Code
```bash
# Start a new session and check
claude --print-system-prompt | grep -A5 "AGENTS.md"
```

### Cursor
Open a new chat - AGENTS.md context should appear in the system prompt.

### GitHub Copilot
AGENTS.md is read as workspace context for suggestions.

---

## Troubleshooting

### AGENTS.md Not Detected

1. Ensure file is in project root
2. Check file permissions
3. Restart the AI tool/IDE
4. Verify file is not in `.gitignore`

### Incorrect Module Detection

1. Verify `pom.xml` is valid
2. Check module paths are correct
3. Re-run `ensure-agents-md` with verbose flag

### Missing Context

Add context manually to AGENTS.md:

```markdown
## Additional Context
[Your custom project context here]
```

---

## Next Steps

Continue to [Agent Skills Installation](./02-agent-skills.md)
