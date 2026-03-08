# Lab 1: Environment Setup
# Agentic Development for AEM

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 45 minutes |
| Difficulty | Beginner |
| Prerequisites | None |
| Outcome | Fully configured development environment |

## Learning Objectives

By the end of this lab, you will have:
- [ ] AEM Cloud Service SDK running locally
- [ ] ClamAV antivirus daemon in Docker
- [ ] Project repository cloned and building
- [ ] AI coding assistant configured
- [ ] All tools verified working

---

## Exercise 1.1: Install Prerequisites (15 min)

### Step 1: Verify Java Installation

```bash
# Check Java version (must be 11+)
java -version

# Expected output:
# openjdk version "11.0.x" or higher

# If not installed, install via:
# macOS: brew install openjdk@11
# Linux: sudo apt install openjdk-11-jdk
# Windows: Download from adoptium.net
```

### Step 2: Verify Maven Installation

```bash
# Check Maven version (must be 3.8+)
mvn -version

# Expected output:
# Apache Maven 3.8.x or higher

# If not installed:
# macOS: brew install maven
# Linux: sudo apt install maven
# Windows: Download from maven.apache.org
```

### Step 3: Install Docker Desktop

```bash
# Verify Docker installation
docker --version
docker-compose --version

# Expected:
# Docker version 24.x or higher
# Docker Compose version 2.x or higher

# If not installed:
# Download from docker.com/products/docker-desktop
```

### Step 4: Install AI Coding Assistant

Choose one of the following:

**Option A: Claude Code (Recommended)**
```bash
# Install Claude Code CLI
npm install -g @anthropic-ai/claude-code

# Verify installation
claude --version

# Configure API key
claude config set api_key YOUR_API_KEY
```

**Option B: GitHub Copilot**
- Install VS Code extension: GitHub Copilot
- Sign in with GitHub account

**Option C: Cursor IDE**
- Download from cursor.sh
- Sign in and configure

#### Checkpoint 1.1
- [ ] Java 11+ installed
- [ ] Maven 3.8+ installed
- [ ] Docker Desktop running
- [ ] AI assistant configured

---

## Exercise 1.2: Set Up AEM SDK (15 min)

### Step 1: Download AEM SDK

```bash
# Create AEM SDK directory
mkdir -p ~/aem-sdk
cd ~/aem-sdk

# Download the SDK from Adobe Software Distribution
# https://experience.adobe.com/#/downloads/content/software-distribution/en/aemcloud.html

# After download, you should have:
# - aem-sdk-quickstart-2024.11.xxxxx.jar
# - license.properties (your Adobe license)
```

### Step 2: Start AEM Author Instance

```bash
# Navigate to SDK directory
cd ~/aem-sdk

# Start AEM with local run mode
java -jar aem-sdk-quickstart-*.jar -r author,local -p 4502

# Wait for startup (3-5 minutes)
# Watch for: "Quickstart started"

# Alternatively, run in background:
java -jar aem-sdk-quickstart-*.jar -r author,local -p 4502 &
tail -f crx-quickstart/logs/stdout.log
```

### Step 3: Verify AEM is Running

```bash
# Check AEM status
curl -s -o /dev/null -w "%{http_code}" http://localhost:4502/libs/granite/core/content/login.html

# Expected: 200

# Open in browser
open http://localhost:4502

# Login with: admin / admin
```

### Step 4: Configure AEM for Development

```bash
# Enable debug logging (optional)
curl -u admin:admin -X POST \
  http://localhost:4502/system/console/configMgr/org.apache.sling.commons.log.LogManager.factory.config \
  -d "org.apache.sling.commons.log.level=debug" \
  -d "org.apache.sling.commons.log.names=com.demo.workflow"
```

#### Checkpoint 1.2
- [ ] AEM SDK downloaded
- [ ] Author instance running on port 4502
- [ ] Login successful with admin/admin
- [ ] System console accessible

---

## Exercise 1.3: Start ClamAV Container (10 min)

### Step 1: Create Docker Compose File

Navigate to the project docker directory and create the compose file:

```bash
cd ~/aem-workflow-demo/docker
```

Create `docker-compose.yml`:

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

### Step 2: Start ClamAV

```bash
# Start ClamAV daemon
docker-compose up -d clamav

# Watch startup logs (takes 2-3 minutes for virus DB download)
docker-compose logs -f clamav

# Wait for: "Socket for clamd ready"
# Press Ctrl+C to exit logs
```

### Step 3: Verify ClamAV Connection

```bash
# Test ClamAV connectivity
echo "PING" | nc localhost 3310

# Expected response: PONG

# Test with a sample scan (should be clean)
echo "Hello World" | nc localhost 3310 -c 'echo -e "zINSTREAM\0"; cat; echo -e "\0\0\0\0"'
```

### Step 4: Test EICAR Detection

```bash
# Create EICAR test file (standard AV test signature)
echo 'X5O!P%@AP[4\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*' > /tmp/eicar.txt

# Scan using clamdscan
docker exec clamav-daemon clamdscan /tmp/eicar.txt 2>/dev/null || echo "Note: File not in container"

# Alternative: Use TCP directly
# This demonstrates the virus is detected
```

#### Checkpoint 1.3
- [ ] Docker Compose file created
- [ ] ClamAV container running
- [ ] PING/PONG test successful
- [ ] Container healthy

---

## Exercise 1.4: Clone and Build Project (5 min)

### Step 1: Clone Repository

```bash
# Clone the course repository
cd ~
git clone https://github.com/your-org/aem-workflow-demo.git
cd aem-workflow-demo

# Or if already cloned, update
git pull origin main
```

### Step 2: Build the Project

```bash
# Full Maven build
mvn clean install

# Expected output:
# [INFO] BUILD SUCCESS
# Total time: ~2 minutes
```

### Step 3: Deploy to AEM

```bash
# Deploy all packages to local AEM
mvn clean install -PautoInstallSinglePackage

# Expected output:
# [INFO] BUILD SUCCESS
# Packages installed to AEM
```

### Step 4: Verify Deployment

```bash
# Check bundle status
curl -s -u admin:admin \
  "http://localhost:4502/system/console/bundles.json" | \
  jq '.data[] | select(.symbolicName | contains("demo-workflow")) | {name: .name, state: .state}'

# Expected:
# {
#   "name": "AEM Workflow Demo - Core",
#   "state": "Active"
# }

# Check in browser
open "http://localhost:4502/system/console/bundles"
# Search for "demo-workflow"
```

#### Checkpoint 1.4
- [ ] Repository cloned
- [ ] Maven build successful
- [ ] Packages deployed to AEM
- [ ] Bundle state is "Active"

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Java not found | Not in PATH | Add to PATH or reinstall |
| Maven build fails | Missing dependencies | Run `mvn dependency:resolve` |
| AEM won't start | Port 4502 in use | Kill process on port or use different port |
| ClamAV timeout | DB download in progress | Wait 2-3 minutes |
| Bundle not active | Missing imports | Check OSGi console for errors |

### Debug Commands

```bash
# Check what's using port 4502
lsof -i :4502

# View AEM error log
tail -100 ~/aem-sdk/crx-quickstart/logs/error.log

# Restart ClamAV container
docker-compose restart clamav

# Check Docker container status
docker ps -a

# View Maven dependency tree
mvn dependency:tree -pl core
```

---

## Environment Verification Script

Run this script to verify your entire environment:

```bash
#!/bin/bash
# save as verify-environment.sh

echo "=== Environment Verification ==="
echo ""

# Check Java
echo -n "Java: "
java -version 2>&1 | head -1 || echo "NOT FOUND"

# Check Maven
echo -n "Maven: "
mvn -version 2>&1 | head -1 || echo "NOT FOUND"

# Check Docker
echo -n "Docker: "
docker --version || echo "NOT FOUND"

# Check AEM
echo -n "AEM: "
curl -s -o /dev/null -w "%{http_code}" http://localhost:4502/libs/granite/core/content/login.html 2>/dev/null || echo "NOT RUNNING"

# Check ClamAV
echo -n "ClamAV: "
echo "PING" | nc -w 2 localhost 3310 2>/dev/null || echo "NOT RUNNING"

# Check Bundle
echo -n "Bundle: "
curl -s -u admin:admin "http://localhost:4502/system/console/bundles.json" 2>/dev/null | \
  jq -r '.data[] | select(.symbolicName | contains("demo-workflow")) | .state' || echo "NOT DEPLOYED"

echo ""
echo "=== Verification Complete ==="
```

---

## Lab Completion Checklist

- [ ] Java 11+ installed and verified
- [ ] Maven 3.8+ installed and verified
- [ ] Docker Desktop running
- [ ] AEM Cloud SDK running on port 4502
- [ ] ClamAV container running on port 3310
- [ ] Project cloned and built successfully
- [ ] Bundle deployed and Active in AEM
- [ ] AI coding assistant configured

---

## Next Lab

Proceed to [Lab 2: PRD Creation](../lab-02-prd-creation/README.md)
