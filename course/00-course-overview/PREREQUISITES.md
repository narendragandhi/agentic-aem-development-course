# Prerequisites
# Agentic Development for AEM Course

## Technical Requirements

### Java Development
- **Java JDK 11 or higher** (OpenJDK 11+ recommended)
- Verify installation:
  ```bash
  java -version
  # Should output: openjdk version "11.0.x" or higher
  ```

### Build Tools
- **Maven 3.8 or higher**
- Verify installation:
  ```bash
  mvn -version
  # Should output: Apache Maven 3.8.x or higher
  ```

### Version Control
- **Git 2.30 or higher**
- Verify installation:
  ```bash
  git --version
  # Should output: git version 2.30.x or higher
  ```

### Container Runtime
- **Docker Desktop** (for ClamAV integration)
- Verify installation:
  ```bash
  docker --version
  # Should output: Docker version 20.x or higher
  docker-compose --version
  # Should output: docker-compose version 1.29.x or higher
  ```

### IDE Requirements
Choose one of the following:

**VS Code (Recommended)**
- VS Code 1.75 or higher
- Extensions:
  - Extension Pack for Java
  - AEM IDE
  - YAML
  - Markdown All in One

**IntelliJ IDEA**
- IntelliJ IDEA 2022.1 or higher
- Plugins:
  - AEM Support
  - YAML

### AEM Environment
- **Option 1**: AEM as a Cloud Service SDK (2024.11+)
- **Option 2**: AEM 6.5.x (6.5.17+ recommended)

Download from: [Adobe Software Distribution](https://experience.adobe.com/#/downloads)

---

## AI Tool Requirements

### Claude Code CLI (Recommended)
```bash
# Install via npm
npm install -g @anthropic-ai/claude-code

# Verify
claude --version
```

### Alternative AI Assistants
The course works with any AI coding assistant that can:
- Read and write files
- Execute shell commands
- Maintain conversation context

---

## Knowledge Prerequisites

### Required
- Java programming (intermediate level)
- Object-oriented programming concepts
- Basic understanding of web applications (HTTP, REST)
- Familiarity with command-line interfaces

### Recommended (Helpful but Not Required)
- AEM development experience
- OSGi concepts
- Workflow engines
- Test-driven development (TDD)
- CI/CD pipelines

### Pre-Course Self-Assessment
Before starting, you should be able to:
- [ ] Write a Java class with interfaces and implementations
- [ ] Use Maven to build a Java project
- [ ] Execute shell commands in terminal
- [ ] Read and write YAML files
- [ ] Use Git for version control (clone, commit, push)
- [ ] Explain what an API endpoint is
- [ ] Describe basic web security concepts (XSS, SQL injection)

---

## Environment Verification

Run these commands to verify your setup:

```bash
# 1. Java
java -version

# 2. Maven
mvn -version

# 3. Git
git --version

# 4. Docker
docker --version
docker-compose --version

# 5. Clone and build course project
git clone <course-repo-url>
cd agentic-aem-development-course
mvn clean install -DskipTests

# Expected: BUILD SUCCESS
```

---

## Support

If you encounter issues with prerequisites:
- Java/Maven: Check PATH configuration
- Docker: Ensure Docker Desktop is running
- AEM SDK: Verify correct version and license

For course-specific questions, see [TROUBLESHOOTING.md](../docs/TROUBLESHOOTING.md)
