# Project Guidance

This file provides context and guidance for working with the Agentic AEM Development Course.

---

## Overview
A comprehensive course on Agentic Development for AEM using BMAD, BEAD, and GasTown methodologies. Features a Secure Asset Approval Workflow as the hands-on case study with production-ready patterns.

## Tech Stack
- **Java 11** with Maven
- **AEM as a Cloud Service SDK** (2024.11.x)
- **OSGi/Apache Sling** framework
- **bnd-maven-plugin** for OSGi bundle generation
- **filevault-package-maven-plugin** v1.3.6 for content packages
- **aemanalyser-maven-plugin** for Cloud Service validation

## Project Structure
```
aem-workflow-demo/
├── core/                    # Java bundle (OSGi)
│   └── src/main/java/com/demo/workflow/
│       ├── process/         # Workflow process steps
│       ├── models/          # Sling Models
│       ├── services/        # OSGi services
│       └── servlets/        # Sling Servlets
├── ui.apps/                 # Application package (packageType=application)
│   └── jcr_root/
│       └── apps/demo-workflow/
│           ├── components/  # AEM components
│           ├── clientlibs/  # Client libraries
│           └── install/     # Embedded bundles
├── ui.content/              # Content package (packageType=content)
│   └── jcr_root/
│       ├── content/demo-workflow/           # Sample content
│       └── conf/global/settings/workflow/   # Workflow models & launchers
├── all/                     # Container package (embeds all sub-packages)
└── docs/                    # Documentation
```

## Build Commands
```bash
# Full build
mvn clean install

# Build and deploy to local AEM (all-in-one package)
mvn clean install -PautoInstallSinglePackage

# Build and deploy individual packages
mvn clean install -PautoInstallPackage

# Build specific module
mvn clean install -pl core -am
```

## Important Paths
| Content Type | Path |
|--------------|------|
| Workflow Models | `/conf/global/settings/workflow/models/` |
| Workflow Launchers | `/conf/global/settings/workflow/launcher/config/` |
| Components | `/apps/demo-workflow/components/` |
| Client Libraries | `/apps/demo-workflow/clientlibs/` |
| Sample Content | `/content/demo-workflow/` |

## Coding Standards

### Java/OSGi
- Use `@Component` annotations for OSGi services
- Implement `WorkflowProcess` interface for workflow steps
- Use `@Model` annotations for Sling Models
- Package structure:
  - `com.demo.workflow.process` - Workflow processes (private)
  - `com.demo.workflow.services` - Service interfaces (exported)
  - `com.demo.workflow.services.impl` - Service implementations (private)
  - `com.demo.workflow.models` - Sling Models (exported)

### Content Packages
- **ui.apps**: Only `/apps` content, `packageType=application`
- **ui.content**: Content under `/content` and `/conf`, `packageType=content`
- **all**: Container package embedding ui.apps and ui.content

### Workflow Models
- Store editable models under `/conf/global/settings/workflow/models/`
- Use `cq:WorkflowModel` as primary type
- Reference workflows using `/conf/global/settings/workflow/models/<name>`

### Workflow Launchers
- Store under `/conf/global/settings/workflow/launcher/config/`
- Use `cq:WorkflowLauncher` as primary type
- Reference workflow models using full `/conf` path

## Testing
```bash
# Run unit tests
mvn test

# Run tests for specific module
mvn test -pl core
```

## Deployment Verification
After deployment, verify at:
- Workflow Models: `http://localhost:4502/libs/cq/workflow/admin/console/content/models.html`
- Workflow Instances: `http://localhost:4502/libs/cq/workflow/admin/console/content/instances.html`
- Package Manager: `http://localhost:4502/crx/packmgr/index.jsp`

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Package validation fails | Ensure `/conf` content is in ui.content, not ui.apps |
| Bundle not starting | Check OSGi console for missing imports |
| Workflow not triggering | Verify launcher path regex and event type |
| Namespace errors in validation | These are warnings for `cq:` types - safe to ignore |

## References
- [AEM Workflow Documentation](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/sites/administering/workflows-administering.html)
- [AEM Cloud Service SDK](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/implementing/developing/aem-as-a-cloud-service-sdk.html)
- [FileVault Package Maven Plugin](https://jackrabbit.apache.org/filevault-package-maven-plugin/)
