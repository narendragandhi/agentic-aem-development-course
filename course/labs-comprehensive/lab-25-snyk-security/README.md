# Lab 25: Snyk Security Integration
# Comprehensive Lab - 3 hours

## Objective

Integrate Snyk for dependency vulnerability scanning and security analysis. Use AI to interpret results and remediate findings.

---

## Prerequisites

- Lab 17 (SonarQube) completed
- GitHub account

---

## BMAD Phase Context

```
Phase 05 (Testing) - Security Testing
├── Snyk for dependency scanning
├── AI analyzes vulnerabilities
├── Automated remediation
└── Integration with GasTown
```

---

## Part 1: Snyk Setup - 20 min

### 1.1 Sign Up & Install

```bash
# Install Snyk CLI
npm install -g snyk

# Authenticate
snyk auth

# Or with GitHub
snyk auth github
```

### 1.2 Configure for Maven Project

```bash
# Test Maven scanning
snyk test --maven
```

---

## Part 2: Snyk Configuration - 30 min

### 2.1 .snyk Policy

Create `.snyk`:

```yaml
# .snyk
version: v1.25.0
ignore:
  SNYK-JAVA-COMFAIRWAYS-1234567:
    - '*':
        reason: 'False positive in test environment'
        expires: '2024-12-31'
        
patch:
  SNYK-JAVA-ORGAPACHESLING-5678:
    - patch-aem-vulnerability:
        patched: '2024-01-15'
```

### 2.2 GitHub Integration

```yaml
# .github/workflows/snyk.yml
name: Snyk Security

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  snyk:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        
      - name: Run Snyk
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
          
      - name: Upload results
        uses: snyk/actions/upload-sarif@master
        with:
          sarif_file: snyk.sarif
```

---

## Part 3: AI + Snyk Analysis - 45 min

### 3.1 AI Analyzes Vulnerabilities

```bash
# Run Snyk and pipe to AI for analysis
snyk test --json | ai-analyze --security

# Or use Goose
goose run --task "Analyze Snyk vulnerability report:
1. Identify critical issues
2. Prioritize by severity
3. Suggest remediation steps
4. Create fix tasks in BEAD"
```

### 3.2 AI Remediation

```bash
goose run --task "For each Snyk vulnerability, generate fix:
1. Upgrade dependency version
2. Apply Snyk patch
3. Add to .snyk ignore if false positive
4. Write JUnit test to verify fix"
```

---

## Part 4: Snyk Code (SAST) - 30 min

### 4.1 Enable Snyk Code

```bash
# Enable code scanning
snyk auth
snyk code test
```

### 4.2 AI Code Security Review

```bash
goose run --task "Analyze Snyk Code findings:
1. Find security issues in Java code
2. Identify XSS, SQL injection risks
3. Generate fixes for each issue
4. Add to development backlog"
```

---

## Part 5: GasTown Integration - 30 min

### 5.1 Configure Quality Gate

```yaml
# gastown.yaml
quality_gates:
  security:
    snyk:
      critical_issues: 0
      high_issues: 0
      medium_issues: 10
    actions:
      - if: critical > 0
        then: fail-build
        message: "Critical vulnerabilities found"
      - if: high > 0
        then: notify-slack
        message: "High vulnerabilities need attention"
```

---

## Part 6: Complete Security Pipeline - 15 min

### 6.1 Full Pipeline

```yaml
# .github/workflows/security.yml
name: Full Security Scan

on: [push, pull_request]

jobs:
  snyk-deps:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
          
  snyk-code:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: snyk/actions/code@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
          
  sonar:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: sonarsource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

---

## Verification Checklist

- [ ] Snyk CLI installed
- [ ] Maven dependencies scanned
- [ ] GitHub integration configured
- [ ] AI analyzes vulnerabilities
- [ ] Snyk Code enabled
- [ ] GasTown quality gate configured

---

## BMAD Integration

| Phase | Activity |
|-------|----------|
| 01 | Define security requirements |
| 05 | Snyk scanning in CI/CD |
| 06 | Monitor vulnerabilities |

---

## Key Takeaways

1. **Snyk scans dependencies** - Maven, npm, etc.
2. **Snyk Code scans source** - Static analysis
3. **AI accelerates remediation** - Auto-analyze and fix

---

## References

- [Snyk Documentation](https://docs.snyk.io/)
- [Snyk for Maven](https://docs.snyk.io/scan-with-snyk/snyk-open-source/manage-vulnerabilities/getting-started-vulnerability-management)
