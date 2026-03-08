# Environment Setup Guide
# Agentic Development for AEM Course

---

## Part 1: Clone the Course Repository

```bash
git clone https://github.com/your-org/agentic-aem-course.git
cd agentic-aem-course
```

---

## Part 2: Java & Maven Setup

### Install OpenJDK 11+

**macOS (using Homebrew)**
```bash
brew install openjdk@11
echo 'export PATH="/usr/local/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Windows (using Chocolatey)**
```powershell
choco install openjdk11
```

**Linux (Ubuntu/Debian)**
```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk
```

### Configure Maven

The project includes a `.mvn/jvm.config` file. Verify it exists:
```bash
cat .mvn/jvm.config
```

Expected content:
```
-Xmx2048m
-XX:MaxMetaspaceSize=512m
```

---

## Part 3: Docker Setup (Optional - for ClamAV)

### Install Docker Desktop
Download from: https://www.docker.com/products/docker-desktop

### Start Docker
```bash
# Verify Docker is running
docker ps

# If you see "Cannot connect to Docker daemon", start Docker Desktop
```

### Verify ClamAV (Optional Lab Feature)
```bash
# Test ClamAV container connectivity
docker run --rm -it clamav/clamav:latest clamdscan --version
```

---

## Part 4: IDE Configuration

### VS Code (Recommended)

1. Install VS Code: https://code.visualstudio.com/

2. Install Extensions:
   - Extension Pack for Java
   - AEM IDE (by Adobe)
   - YAML
   - Markdown All in One
   - XML

3. Open the project:
   ```bash
   code .
   ```

4. Configure Java:
   - File > Preferences > Settings
   - Search "java.configuration.runtimes"
   - Add Java 11 path

### IntelliJ IDEA

1. Open project as Maven project
2. Enable annotation processing:
   - Build, Execution, Deployment > Compiler > Annotation Processors
   - Enable annotation processing: ✓

---

## Part 5: AEM SDK Setup (Optional - for Local Testing)

### Download AEM SDK
1. Go to https://experience.adobe.com/#/downloads
2. Search "AEM SDK"
3. Download SDK for your OS

### Extract and Setup
```bash
# Extract SDK
tar -xzf aem-sdk-*.tar.gz
cd aem-sdk

# Start Author (quickstart)
./crx-quickstart/bin/start

# Wait 2-3 minutes for startup
# Access: http://localhost:4502
# Credentials: admin/admin
```

---

## Part 6: Build and Verify

### Build the Project
```bash
# Full build
mvn clean install

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Total time: 3-5 minutes
```

### Run Tests
```bash
# Run all tests
mvn test

# Expected: 165+ tests passing
```

### Package Verification
```bash
# Verify packages were created
ls -la all/target/*.zip
ls -la ui.apps/target/*.zip

# Expected:
# agentic-aem-course.all-1.0.0-SNAPSHOT.zip
# agentic-aem-course.ui.apps-1.0.0-SNAPSHOT.zip
```

---

## Part 7: AI Tool Setup (Optional)

### Claude Code CLI
```bash
# Install
npm install -g @anthropic-ai/claude-code

# Configure
claude config set API_KEY your-api-key

# Verify
claude --version
```

### Verify AI Context Loading
```bash
# Test that Claude can read course context
claude --print "
Read the course overview and tell me the learning objectives.
" < course/00-course-overview/README.md
```

---

## Part 8: Troubleshooting

### Common Issues

**Maven build fails with "Java version" error**
```bash
# Check Java version
java -version

# Set JAVA_HOME
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
```

**Docker not running**
```bash
# macOS: Start Docker Desktop application
# Linux: sudo systemctl start docker
```

**Tests fail with "AEM Context" errors**
- Ensure you're running tests with correct AEM Mock configuration
- Check that `pom.xml` includes aem-testing-commons dependency

**Port 4502 already in use**
```bash
# Find what's using the port
lsof -i :4502

# Kill the process or change AEM port in pom.xml
```

---

## Quick Verification Checklist

- [ ] `java -version` shows Java 11+
- [ ] `mvn -version` shows Maven 3.8+
- [ ] `git --version` shows Git 2.30+
- [ ] `docker --version` shows Docker 20+
- [ ] `mvn clean install` completes successfully
- [ ] `mvn test` runs 165+ tests
- [ ] IDE opens project without errors

---

## Next Steps

After completing environment setup:
1. Review [Prerequisites](PREREQUISITES.md)
2. Start with [Module 1: Introduction to Agentic Development](../01-prd/README.md)
3. Complete [Lab 1: Setup & Foundations](../labs/lab-01-setup-foundations/README.md)

---

## Support

- Course Issues: Submit via GitHub Issues
- AEM Documentation: https://experienceleague.adobe.com
- Troubleshooting: See [docs/TROUBLESHOOTING.md](../docs/TROUBLESHOOTING.md)
