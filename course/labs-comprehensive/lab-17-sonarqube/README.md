# Lab 17: SonarQube Integration
# Comprehensive Lab - 3 hours

## Objective

Integrate SonarQube for continuous code quality and security analysis. Set up quality gates, security rules, and automated scanning in CI/CD pipelines.

---

## Prerequisites

- Lab 15 (Code Quality) completed
- Docker Desktop installed
- Maven 3.8+

---

## Overview

### SonarQube Benefits

| Feature | Benefit |
|---------|---------|
| Code Coverage | Track test coverage over time |
| Security Vulnerabilities | Detect OWASP Top 10 issues |
| Code Smells | Maintain clean, readable code |
| Technical Debt | Track and manage debt |
| Quality Gates | Enforce standards automatically |
| Historical Analysis | See trends over time |

---

## Part 1: SonarQube Setup (30 min)

### 1.1 Start SonarQube with Docker

```bash
# Create Docker network
docker network create sonarnetwork

# Start SonarQube
docker run -d \
  --name sonarqube \
  --network sonarnetwork \
  -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  sonarqube:latest

# Wait for startup (2-3 minutes)
docker logs -f sonarqube
```

### 1.2 Configure SonarQube

1. Open http://localhost:9000
2. Login: admin / admin
3. Create project: `secure-asset-workflow`
4. Generate token: `squ_xxxxx`

### 1.3 Add Token to Maven Settings

```xml
<!-- ~/.m2/settings.xml -->
<settings>
  <servers>
    <server>
      <id>sonarqube</id>
      <token>squ_xxxxx</token>
    </server>
  </servers>
</settings>
```

---

## Part 2: Maven Configuration (30 min)

### 2.1 Add SonarQube Plugin

Add to `pom.xml`:

```xml
<properties>
  <sonar.host.url>http://localhost:9000</sonar.host.url>
  <sonar.projectKey>secure-asset-workflow</sonar.projectKey>
  <sonar.organization>my-org</sonar.organization>
</properties>

<build>
  <plugins>
    <plugin>
      <groupId>org.sonarsource.scanner.maven</groupId>
      <artifactId>sonar-maven-plugin</artifactId>
      <version>3.11.0.3922</version>
    </plugin>
  </plugins>
</build>
```

### 2.2 Run Analysis

```bash
# Run SonarQube analysis
mvn clean verify sonar:sonar

# Or skip tests
mvn clean sonar:sonar -DskipTests
```

---

## Part 3: Quality Gates (30 min)

### 3.1 Create Quality Gate

In SonarQube UI:
1. Go to Quality Gates
2. Create: `AEM Project Gate`
3. Add conditions:

| Metric | Operator | Value |
|--------|----------|-------|
| Coverage | is less than | 80% |
| Critical Issues | is greater than | 0 |
| Security Hotspots | is greater than | 10 |
| Technical Debt | is greater than | 2d |
| Duplicated Lines | is greater than | 3% |

### 3.2 Assign to Project

```
Quality Gates → Select "AEM Project Gate" → Set as Default
```

### 3.3 Quality Gate Conditions in Maven

```xml
<!-- Fail build if quality gate fails -->
<properties>
  <sonar.qualitygate.wait>true</sonar.qualitygate.wait>
  <sonar.qualitygate.timeout>300</sonar.qualitygate.timeout>
</properties>
```

---

## Part 4: Security Rules (30 min)

### 4.1 Enable Security Rules

In SonarQube:
1. Go to Rules
2. Filter by: Security (OWASP Top 10)
3. Activate rules:

| Category | Rules to Activate |
|----------|-------------------|
| SQL Injection | S3649, S5144, S5145 |
| XSS | S5247, S5254 |
| Hardcoded Credentials | S2068, S2083 |
| Path Traversal | S5734, S5735 |

### 4.2 Security Categories

```
Security:
├── Vulnerability (bugs to fix)
├── Security Hotspot (review required)
└── Security Clean (safe code)
```

### 4.3 Configure Exclusions

```xml
<properties>
  <sonar.exclusions>
    **/test/**,
    **/mock/**,
    **/generated/**
  </sonar.exclusions>
  <sonar.coverage.exclusions>
    **/test/**
  </sonar.coverage.exclusions>
</properties>
```

---

## Part 5: CI/CD Integration (30 min)

### 5.1 GitHub Actions Workflow

Create `.github/workflows/sonarqube.yml`:

```yaml
name: SonarQube Analysis

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '11'
          
      - name: Cache Maven
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          
      - name: SonarQube Scan
        run: mvn clean verify sonar:sonar
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
          
      - name: Quality Gate Check
        if: always()
        run: |
          # Get quality gate status
          curl -s -u admin:${{ secrets.SONAR_TOKEN }} \
            "${{ secrets.SONAR_HOST_URL }}/api/qualitygates/project_status?projectKey=secure-asset-workflow"
```

### 5.2 Quality Gate Status in PR

```yaml
- name: SonarQube Quality Gate
  uses: sonarsource/sonarqube-quality-gate-action@master
  with:
    host: ${{ secrets.SONAR_HOST_URL }}
    token: ${{ secrets.SONAR_TOKEN }}
```

---

## Part 6: Custom Rules for AEM (30 min)

### 6.1 AEM-Specific Rules

Create custom rules for AEM patterns:

```java
// Custom SonarQube rule: Check for ResourceResolver leaks
public class ResourceResolverCheck extends SubscriptionVisitor {
  
  @Override
  public List<Kind> subscribedKinds() {
    return Arrays.asList(Kind.TRY_STATEMENT);
  }
  
  @Override
  public void visitNode(Tree tree) {
    TryStatement tree = (TryStatement) tree;
    
    // Check for ResourceResolver in try-with-resources
    if (!hasResourceResolverClose(tree)) {
      context.reportIssue(this, tree, 
        "ResourceResolver must be closed in try-with-resources");
    }
  }
  
  private boolean hasResourceResolverClose(TryStatement tree) {
    // Implementation
  }
}
```

### 6.2 Register Custom Rules

```xml
<!-- sonar-extension/pom.xml -->
<plugin>
  <artifactId>sonar-plugin-api</artifactId>
  <version>9.9.0.905</version>
</plugin>
```

---

## Part 7: Dashboard & Reporting (15 min)

### 7.1 Create Dashboard

In SonarQube:
1. Create Portfolio: `AEM Projects`
2. Add Projects to Portfolio
3. Create Custom Measures

### 7.2 Metrics to Track

| Metric | Target |
|--------|--------|
| Coverage | ≥ 80% |
| Critical Issues | 0 |
| Technical Debt | < 2 days |
| Duplication | < 3% |
| Security Hotspots | < 10 |

---

## Verification Checklist

- [ ] SonarQube running in Docker
- [ ] Maven analysis completes
- [ ] Quality gate configured
- [ ] Security rules active
- [ ] CI/CD pipeline configured
- [ ] Custom AEM rules added
- [ ] Dashboard created

---

## Key Takeaways

1. **SonarQube provides centralized quality tracking** - Single source of truth
2. **Quality gates prevent bad code** - Automatic enforcement
3. **Security rules catch vulnerabilities** - OWASP Top 10 coverage
4. **Historical analysis shows trends** - Track improvement over time

---

## Next Steps

1. Add SonarQube to all projects
2. Configure branch analysis
3. Set up PR decoration
4. Integrate with Slack notifications
5. Create AEM-specific rule pack

---

## References

- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Maven Scanner](https://sonarsource.github.io/sonar-scanner-maven/)
- [GitHub Integration](https://docs.sonarqube.org/latest/analysis/github-integration/)
