# Lab 8: Deployment & Operations
# From Development to Production

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 45 minutes |
| Difficulty | Intermediate |
| Prerequisites | Labs 1-7 completed |
| Outcome | Workflow deployed and operational |

## Learning Objectives

By the end of this lab, you will:
- [ ] Deploy to local AEM successfully
- [ ] Configure OSGi settings for different environments
- [ ] Verify workflow execution end-to-end
- [ ] Set up monitoring and logging

---

## Exercise 8.1: Final Build & Deploy (15 min)

### Step 1: Clean Build

```bash
# Navigate to project root
cd ~/aem-workflow-demo

# Clean build with all tests
mvn clean install

# Expected output:
# [INFO] BUILD SUCCESS
# All tests should pass
```

### Step 2: Deploy to AEM

```bash
# Deploy all packages
mvn clean install -PautoInstallSinglePackage

# Verify deployment
curl -s -u admin:admin \
  "http://localhost:4502/system/console/bundles.json" | \
  jq '.data[] | select(.symbolicName | contains("demo-workflow")) | {name: .name, state: .state}'
```

**Expected Output:**
```json
{
  "name": "AEM Workflow Demo - Core",
  "state": "Active"
}
```

### Step 3: Verify Package Installation

```bash
# Check packages
curl -s -u admin:admin \
  "http://localhost:4502/crx/packmgr/service.jsp?cmd=ls" | \
  grep "demo-workflow"

# Expected: ui.apps and ui.content packages installed
```

### Step 4: Verify Workflow Model

```bash
# Check workflow model exists
curl -s -u admin:admin \
  "http://localhost:4502/conf/global/settings/workflow/models/secure-asset-approval.json" | \
  jq '.["jcr:content"]["jcr:title"]'

# Expected: "Secure Asset Approval Workflow"

# Open in browser
open "http://localhost:4502/editor.html/conf/global/settings/workflow/models/secure-asset-approval.html"
```

#### Checkpoint 8.1
- [ ] Build successful
- [ ] Bundle Active
- [ ] Packages installed
- [ ] Workflow model visible

---

## Exercise 8.2: Configure OSGi Settings (10 min)

### Step 1: Access OSGi Console

```bash
open "http://localhost:4502/system/console/configMgr"
# Login: admin / admin
```

### Step 2: Configure Antivirus Service

Search for "Antivirus" and configure:

| Setting | Development Value | Production Value |
|---------|-------------------|------------------|
| Scan Engine | MOCK | CLAMAV |
| ClamAV Host | localhost | clamav.internal |
| ClamAV Port | 3310 | 3310 |
| Connection Timeout | 5000 | 5000 |
| Read Timeout | 60000 | 120000 |
| Max File Size | 104857600 | 104857600 |
| Enabled | true | true |

### Step 3: Configure Quarantine Process

| Setting | Value |
|---------|-------|
| Quarantine Path | /content/dam/quarantine |
| Notification Group | security-admins |
| Retention Days | 90 |
| Delete Original | false |
| Create Audit Log | true |

### Step 4: Verify Configuration

```bash
# Check configuration via API
curl -s -u admin:admin \
  "http://localhost:4502/system/console/configMgr/com.demo.workflow.services.impl.AntivirusScanServiceImpl.json" | \
  jq '.'
```

#### Checkpoint 8.2
- [ ] Antivirus service configured
- [ ] Quarantine process configured
- [ ] Settings verified

---

## Exercise 8.3: End-to-End Verification (15 min)

### Step 1: Create Test Folders

```bash
# Create upload folder via API
curl -u admin:admin -X POST \
  "http://localhost:4502/content/dam/secure-assets" \
  -F "jcr:primaryType=sling:Folder" \
  -F "jcr:title=Secure Assets"

# Verify folder
curl -s -u admin:admin \
  "http://localhost:4502/content/dam/secure-assets.json" | jq '.["jcr:title"]'
```

### Step 2: Upload Clean Test Asset

```bash
# Create a test PDF
echo "Test PDF content for workflow testing" > /tmp/test-document.pdf

# Upload asset
curl -u admin:admin -X POST \
  "http://localhost:4502/content/dam/secure-assets.createasset.html" \
  -F "file=@/tmp/test-document.pdf" \
  -F "fileName=test-document.pdf"

# Check asset
curl -s -u admin:admin \
  "http://localhost:4502/content/dam/secure-assets/test-document.pdf.json" | \
  jq '.["jcr:primaryType"]'
```

### Step 3: Monitor Workflow Execution

```bash
# Check workflow instances
curl -s -u admin:admin \
  "http://localhost:4502/etc/workflow/instances.json" | \
  jq '.[] | select(.payloadPath | contains("test-document")) | {state: .state, model: .modelId}'

# Open workflow console
open "http://localhost:4502/libs/cq/workflow/admin/console/content/instances.html"
```

### Step 4: Verify Scan Results

```bash
# Check asset metadata for scan status
curl -s -u admin:admin \
  "http://localhost:4502/content/dam/secure-assets/test-document.pdf/jcr:content/metadata.json" | \
  jq '{"avStatus": .["dam:avScanStatus"], "scanDate": .["dam:avScanDate"]}'

# Expected (mock mode):
# {
#   "avStatus": "CLEAN",
#   "scanDate": "2024-02-28T..."
# }
```

### Step 5: Test Infected File (Mock Mode)

```bash
# Create "infected" test file
echo "This is a fake virus for testing" > /tmp/virus_test.txt

# Upload (should trigger quarantine in mock mode)
curl -u admin:admin -X POST \
  "http://localhost:4502/content/dam/secure-assets.createasset.html" \
  -F "file=@/tmp/virus_test.txt" \
  -F "fileName=virus_test.txt"

# Wait a moment for workflow
sleep 5

# Check quarantine folder
curl -s -u admin:admin \
  "http://localhost:4502/content/dam/quarantine.1.json" | \
  jq 'keys'

# Should show date-based folders with quarantined file
```

#### Checkpoint 8.3
- [ ] Test folders created
- [ ] Clean file uploaded successfully
- [ ] Workflow executed
- [ ] Scan metadata present
- [ ] Infected file quarantined (mock mode)

---

## Exercise 8.4: Monitoring & Logging (5 min)

### Step 1: Configure Logging

```bash
# Enable debug logging for workflow
curl -u admin:admin -X POST \
  "http://localhost:4502/system/console/configMgr/org.apache.sling.commons.log.LogManager.factory.config" \
  -F "org.apache.sling.commons.log.level=DEBUG" \
  -F "org.apache.sling.commons.log.file=logs/workflow-demo.log" \
  -F "org.apache.sling.commons.log.names=com.demo.workflow"
```

### Step 2: View Logs

```bash
# Tail the workflow log
tail -f ~/aem-sdk/crx-quickstart/logs/workflow-demo.log

# Or view error log
tail -f ~/aem-sdk/crx-quickstart/logs/error.log | grep "demo.workflow"
```

### Step 3: Create Health Check

```bash
# Check bundle health
curl -s -u admin:admin \
  "http://localhost:4502/system/console/bundles/com.demo.workflow.core.json" | \
  jq '{state: .data[0].state, imports: .data[0].props | length}'

# Check ClamAV connectivity (when using real ClamAV)
echo "PING" | nc -w 2 localhost 3310
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] All tests passing
- [ ] Code reviewed
- [ ] Documentation updated
- [ ] OSGi configs prepared

### Deployment

- [ ] Package built successfully
- [ ] Bundle state: Active
- [ ] Workflow model visible
- [ ] Launcher configured

### Post-Deployment

- [ ] End-to-end test passed
- [ ] Logging configured
- [ ] Monitoring active
- [ ] Stakeholders notified

---

## Lab Deliverables

1. **Deployment log** - Build and deployment output
2. **Configuration screenshots** - OSGi settings
3. **Test results** - E2E verification
4. **Monitoring setup** - Log configuration

---

## Lab Completion Checklist

- [ ] Successful deployment to AEM
- [ ] OSGi configuration complete
- [ ] Clean file workflow verified
- [ ] Infected file quarantine verified
- [ ] Logging configured
- [ ] Health checks working

---

## Course Completion

Congratulations! You have completed all labs in the **Agentic Development for AEM** course.

### What You've Learned

1. **Environment Setup** - AEM SDK, Docker, ClamAV
2. **PRD Creation** - AI-optimized requirements
3. **Architecture Design** - Component specifications
4. **AI Development** - Pair programming with AI
5. **BEAD Tracking** - Task management for agents
6. **GasTown Orchestration** - Multi-agent workflows
7. **Testing** - Comprehensive test coverage
8. **Deployment** - Production-ready delivery

### Next Steps

- Explore advanced BMAD phases (Integrations, Operations)
- Implement additional workflow features
- Set up CI/CD with Cloud Manager
- Join the community for continued learning

---

## Certification

Complete the assessment in `course/08-assessments/` to receive your certification.
