# BMAD Phase 00: Project Initialization
# Secure Asset Approval Workflow

## Phase Overview

This phase establishes the foundation for the Secure Asset Approval Workflow project, including environment setup, repository structure, and initial configurations.

---

## AI Agent Prompts for Phase 00

### Prompt 1: Project Scaffolding

```
You are an AEM Project Architect. Create the project structure for a
"Secure Asset Approval Workflow" based on the AEM Project Archetype.

Requirements:
- Use AEM Cloud Service compatible archetype
- Include core bundle for Java code
- Include ui.apps for application content
- Include ui.content for workflow models and launchers
- Use package group: com.demo.workflow

Generate the Maven archetype command and initial project structure.
```

### Prompt 2: Environment Configuration

```
You are an AEM DevOps Engineer. Configure the development environment
for the Secure Asset Approval Workflow project.

Set up:
1. Docker Compose for ClamAV antivirus daemon
2. Local AEM SDK with appropriate run modes
3. Maven settings for AEM development
4. IDE configuration for AEM projects

Provide step-by-step setup instructions.
```

---

## Deliverables Checklist

### D-00.1: Project Repository

- [ ] Git repository initialized
- [ ] Branch strategy defined (main, develop, feature/*)
- [ ] .gitignore configured for AEM projects
- [ ] README.md with project overview

### D-00.2: Maven Project Structure

```
secure-asset-workflow/
├── pom.xml                    # Parent POM
├── core/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/demo/workflow/
│           ├── process/       # Workflow processes
│           ├── services/      # OSGi services
│           └── servlets/      # Sling servlets
├── ui.apps/
│   ├── pom.xml
│   └── src/main/content/jcr_root/
│       └── apps/demo-workflow/
│           ├── components/
│           ├── clientlibs/
│           └── osgiconfig/
├── ui.content/
│   ├── pom.xml
│   └── src/main/content/jcr_root/
│       ├── conf/global/settings/workflow/
│       │   ├── models/
│       │   └── launcher/
│       └── content/dam/
└── all/
    └── pom.xml
```

### D-00.3: Development Environment

- [ ] AEM Cloud SDK installed and running
- [ ] Docker Desktop with ClamAV container
- [ ] Maven 3.8+ configured
- [ ] IDE with AEM extensions

### D-00.4: Docker Configuration

**docker-compose.yml**:

```yaml
version: '3.8'

services:
  clamav:
    image: clamav/clamav:latest
    container_name: clamav-daemon
    ports:
      - "3310:3310"
    volumes:
      - clamav-data:/var/lib/clamav
    environment:
      - CLAMAV_NO_FRESHCLAMD=false
    healthcheck:
      test: ["CMD", "clamdscan", "--ping"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped

volumes:
  clamav-data:
```

### D-00.5: Initial Configuration Files

**OSGi Config: Author Run Mode**

```json
// com.demo.workflow.services.impl.AntivirusScanServiceImpl.cfg.json
{
    "scanEngine": "CLAMAV",
    "clamavHost": "localhost",
    "clamavPort:Integer": 3310,
    "enabled:Boolean": true
}
```

**OSGi Config: Local Development**

```json
// com.demo.workflow.services.impl.AntivirusScanServiceImpl.cfg.json
{
    "scanEngine": "MOCK",
    "enabled:Boolean": true
}
```

---

## Hands-On Lab: Environment Setup

### Step 1: Clone the Repository

```bash
# Clone the starter repository
git clone https://github.com/your-org/aem-workflow-demo.git
cd aem-workflow-demo

# Create your feature branch
git checkout -b feature/secure-asset-workflow
```

### Step 2: Start ClamAV Container

```bash
# Navigate to docker directory
cd docker

# Start ClamAV daemon
docker-compose up -d clamav

# Verify ClamAV is running (may take 2-3 minutes for initial startup)
docker-compose logs -f clamav

# Test ClamAV connectivity
echo "PING" | nc localhost 3310
# Should respond with: PONG
```

### Step 3: Start AEM SDK

```bash
# Navigate to AEM SDK directory
cd ~/aem-sdk

# Start author instance
java -jar aem-sdk-quickstart-*.jar -r author,local -p 4502

# Wait for startup (watch the log)
tail -f crx-quickstart/logs/stdout.log
```

### Step 4: Build and Deploy

```bash
# Return to project directory
cd ~/aem-workflow-demo

# Full build and deploy
mvn clean install -PautoInstallSinglePackage

# Verify deployment
open http://localhost:4502/system/console/bundles
# Search for "demo-workflow" - should be Active
```

### Step 5: Verify Workflow Model

```bash
# Open AEM Workflow Models console
open http://localhost:4502/libs/cq/workflow/admin/console/content/models.html

# You should see "Secure Asset Approval Workflow" in the list
```

---

## Verification Checklist

| Item | Verification Command | Expected Result |
|------|---------------------|-----------------|
| AEM Running | `curl http://localhost:4502/system/console/status-productinfo.txt` | Version info displayed |
| Bundle Active | `curl -u admin:admin http://localhost:4502/system/console/bundles.json \| grep demo-workflow` | State: Active |
| ClamAV Running | `echo "PING" \| nc localhost 3310` | PONG |
| Workflow Model | Check Models console | Workflow visible |

---

## AI Agent Task Completion

When Phase 00 is complete, the AI agent should report:

```yaml
phase: "00-initialization"
status: "complete"
deliverables:
  - name: "project-repository"
    status: "created"
    verification: "git status shows clean working directory"
  - name: "maven-structure"
    status: "created"
    verification: "mvn validate passes"
  - name: "docker-environment"
    status: "running"
    verification: "ClamAV responds to PING"
  - name: "aem-deployment"
    status: "deployed"
    verification: "Bundle is Active"
blockers: []
next_phase: "01-discovery"
```

---

## Transition to Phase 01

Prerequisites for Phase 01:
- [ ] All deliverables verified
- [ ] Development environment stable
- [ ] Team has access to repository
- [ ] Stakeholder kickoff scheduled
