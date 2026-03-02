# Lab 12: Comprehensive OWASP Security Patterns

## Objective
Build a comprehensive security pattern database covering the OWASP Top 10 2021 vulnerabilities, going beyond basic XSS and SQL injection to cover all major attack vectors.

---

## Prerequisites
- Completed Lab 10 (Security Scanner)
- Completed Lab 11 (Document Security)
- Familiarity with OWASP Top 10

---

## Learning Outcomes
After completing this lab, you will be able to:
1. Implement detection patterns for all OWASP Top 10 categories
2. Understand different injection attack vectors (Command, LDAP, XPath, XXE)
3. Detect cryptographic failures and exposed secrets
4. Identify SSRF and access control vulnerabilities
5. Map findings to OWASP and CWE references

---

## OWASP Top 10 2021 Coverage

| Category | Detection Patterns |
|----------|-------------------|
| A01: Broken Access Control | Path traversal, privilege escalation |
| A02: Cryptographic Failures | Hardcoded secrets, weak algorithms, PII |
| A03: Injection | XSS, SQL, Command, LDAP, XPath, XXE |
| A07: Auth Failures | Token exposure, session leakage |
| A10: SSRF | Internal URLs, credential embedding |

---

## Part 1: RED Phase - Write Specification Tests

### 1.1 Create Test File

Create `OwaspSecurityPatternsSpec.java`:

```java
package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService.SecurityFinding;
import com.demo.workflow.services.SecurityScannerService.Severity;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OWASP Security Patterns Specification")
class OwaspSecurityPatternsSpec {

    private OwaspSecurityPatterns patterns;

    @BeforeEach
    void setUp() {
        patterns = new OwaspSecurityPatterns();
    }

    // ═══════════════════════════════════════════════════════════════════
    // A01:2021 - Broken Access Control
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A01:2021 - Broken Access Control")
    class BrokenAccessControl {

        @Test
        @DisplayName("should detect path traversal ../")
        void shouldDetectPathTraversal() {
            String content = "GET /files/../../../etc/passwd HTTP/1.1";

            List<SecurityFinding> findings = patterns.scanContent(content, "request");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PATH_TRAVERSAL")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect sensitive file access")
        void shouldDetectSensitiveFileAccess() {
            String content = "file=/etc/passwd";

            List<SecurityFinding> findings = patterns.scanContent(content, "param");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SENSITIVE_FILE_ACCESS")));
        }

        @Test
        @DisplayName("should detect privilege escalation")
        void shouldDetectPrivilegeEscalation() {
            String content = "admin=true&role=superuser";

            List<SecurityFinding> findings = patterns.scanContent(content, "param");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PRIVILEGE_ESCALATION")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A02:2021 - Cryptographic Failures
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A02:2021 - Cryptographic Failures")
    class CryptographicFailures {

        @Test
        @DisplayName("should detect hardcoded passwords")
        void shouldDetectHardcodedPasswords() {
            String content = "password='MySecretPass123'";

            List<SecurityFinding> findings = patterns.scanContent(content, "code");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("HARDCODED_SECRET")));
        }

        @Test
        @DisplayName("should detect private keys")
        void shouldDetectPrivateKeys() {
            String content = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpA...\n-----END RSA PRIVATE KEY-----";

            List<SecurityFinding> findings = patterns.scanContent(content, "file");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PRIVATE_KEY_EXPOSURE")));
        }

        @Test
        @DisplayName("should detect weak hash algorithms")
        void shouldDetectWeakHashAlgorithms() {
            String content = "String hash = MD5(password);";

            List<SecurityFinding> findings = patterns.scanContent(content, "code");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("WEAK_HASH_ALGORITHM")));
        }

        @Test
        @DisplayName("should detect credit card numbers")
        void shouldDetectCreditCardNumbers() {
            String content = "Card: 4111-1111-1111-1111";

            List<SecurityFinding> findings = patterns.scanContent(content, "form");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("CREDIT_CARD_NUMBER")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A03:2021 - Injection (Command, LDAP, XXE)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A03:2021 - Advanced Injection")
    class AdvancedInjection {

        @Test
        @DisplayName("should detect OS command injection")
        void shouldDetectCommandInjection() {
            String content = "; cat /etc/passwd |";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("COMMAND_INJECTION")));
        }

        @Test
        @DisplayName("should detect expression language injection")
        void shouldDetectExpressionInjection() {
            String content = "${T(java.lang.Runtime).getRuntime().exec('cmd')}";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("EXPRESSION_INJECTION")));
        }

        @Test
        @DisplayName("should detect LDAP injection")
        void shouldDetectLdapInjection() {
            String content = ")(|(uid=*)";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("LDAP_INJECTION")));
        }

        @Test
        @DisplayName("should detect XXE attacks")
        void shouldDetectXxeAttacks() {
            String content = "<!ENTITY xxe SYSTEM \"file:///etc/passwd\">";

            List<SecurityFinding> findings = patterns.scanContent(content, "xml");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("XXE_EXTERNAL_ENTITY")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A10:2021 - Server-Side Request Forgery
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A10:2021 - SSRF")
    class ServerSideRequestForgery {

        @Test
        @DisplayName("should detect internal URL access")
        void shouldDetectInternalUrlAccess() {
            String content = "url=http://localhost:8080/admin";

            List<SecurityFinding> findings = patterns.scanContent(content, "param");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SSRF_INTERNAL_URL")));
        }

        @Test
        @DisplayName("should detect private IP access")
        void shouldDetectPrivateIpAccess() {
            String content = "fetch('http://192.168.1.1/api/internal')";

            List<SecurityFinding> findings = patterns.scanContent(content, "code");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SSRF_INTERNAL_URL")));
        }
    }
}
```

### 1.2 Run Tests (Should Fail)

```bash
mvn test -pl core -Dtest=OwaspSecurityPatternsSpec
```

---

## Part 2: GREEN Phase - Implement Patterns

### 2.1 Create Pattern Database

```java
package com.demo.workflow.services.impl;

@Component(service = OwaspSecurityPatterns.class, immediate = true)
public class OwaspSecurityPatterns {

    // ═══════════════════════════════════════════════════════════════════
    // A01:2021 - Broken Access Control Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> ACCESS_CONTROL_PATTERNS = Arrays.asList(
        new PatternDefinition(
            Pattern.compile("\\.\\./|\\.\\.\\\\", Pattern.CASE_INSENSITIVE),
            "PATH_TRAVERSAL",
            Severity.CRITICAL,
            "Path traversal attempt detected",
            "A01:2021 - Broken Access Control",
            "CWE-22"
        ),
        new PatternDefinition(
            Pattern.compile("/etc/passwd|/etc/shadow|/windows/system32", Pattern.CASE_INSENSITIVE),
            "SENSITIVE_FILE_ACCESS",
            Severity.CRITICAL,
            "Attempt to access sensitive system files",
            "A01:2021 - Broken Access Control",
            "CWE-22"
        ),
        new PatternDefinition(
            Pattern.compile("(?:admin|root|sudo)\\s*[=:]\\s*(?:true|1|yes)", Pattern.CASE_INSENSITIVE),
            "PRIVILEGE_ESCALATION",
            Severity.HIGH,
            "Potential privilege escalation attempt",
            "A01:2021 - Broken Access Control",
            "CWE-269"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // A02:2021 - Cryptographic Failures Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> CRYPTO_PATTERNS = Arrays.asList(
        new PatternDefinition(
            Pattern.compile("(?:password|passwd|pwd|secret|api[_-]?key)\\s*[=:]\\s*['\"][^'\"]{3,}['\"]",
                           Pattern.CASE_INSENSITIVE),
            "HARDCODED_SECRET",
            Severity.CRITICAL,
            "Hardcoded password or secret detected",
            "A02:2021 - Cryptographic Failures",
            "CWE-798"
        ),
        new PatternDefinition(
            Pattern.compile("-----BEGIN\\s+(?:RSA\\s+)?PRIVATE\\s+KEY-----", Pattern.CASE_INSENSITIVE),
            "PRIVATE_KEY_EXPOSURE",
            Severity.CRITICAL,
            "Private key embedded in content",
            "A02:2021 - Cryptographic Failures",
            "CWE-321"
        ),
        new PatternDefinition(
            Pattern.compile("(?:MD5|SHA-?1)\\s*\\(", Pattern.CASE_INSENSITIVE),
            "WEAK_HASH_ALGORITHM",
            Severity.MEDIUM,
            "Weak cryptographic hash algorithm detected",
            "A02:2021 - Cryptographic Failures",
            "CWE-328"
        ),
        new PatternDefinition(
            Pattern.compile("\\b(?:\\d{4}[- ]?){4}\\b"),
            "CREDIT_CARD_NUMBER",
            Severity.HIGH,
            "Potential credit card number detected",
            "A02:2021 - Cryptographic Failures",
            "CWE-311"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // A03:2021 - Injection Patterns (Command, LDAP, XXE)
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> INJECTION_PATTERNS = Arrays.asList(
        // Command Injection
        new PatternDefinition(
            Pattern.compile("(?:;|\\||&&|`|\\$\\()\\s*(?:cat|ls|dir|wget|curl|chmod|rm|bash|sh|cmd|powershell)",
                           Pattern.CASE_INSENSITIVE),
            "COMMAND_INJECTION",
            Severity.CRITICAL,
            "OS command injection detected",
            "A03:2021 - Injection",
            "CWE-78"
        ),
        // Expression Language Injection
        new PatternDefinition(
            Pattern.compile("\\$\\{.*?\\}", Pattern.CASE_INSENSITIVE),
            "EXPRESSION_INJECTION",
            Severity.HIGH,
            "Expression language injection (EL/OGNL)",
            "A03:2021 - Injection",
            "CWE-917"
        ),
        // LDAP Injection
        new PatternDefinition(
            Pattern.compile("\\)\\s*\\(\\|?\\s*\\(|\\*\\)\\)|\\)\\s*\\(!", Pattern.CASE_INSENSITIVE),
            "LDAP_INJECTION",
            Severity.HIGH,
            "LDAP injection pattern detected",
            "A03:2021 - Injection",
            "CWE-90"
        ),
        // XXE
        new PatternDefinition(
            Pattern.compile("<!ENTITY\\s+[^>]*SYSTEM\\s+['\"]", Pattern.CASE_INSENSITIVE),
            "XXE_EXTERNAL_ENTITY",
            Severity.CRITICAL,
            "XML External Entity (XXE) declaration",
            "A03:2021 - Injection",
            "CWE-611"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // A10:2021 - SSRF Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> SSRF_PATTERNS = Arrays.asList(
        new PatternDefinition(
            Pattern.compile("(?:http|https|ftp|file)://(?:localhost|127\\.0\\.0\\.1|10\\.|172\\.(?:1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.)",
                           Pattern.CASE_INSENSITIVE),
            "SSRF_INTERNAL_URL",
            Severity.HIGH,
            "Internal/private network URL detected (SSRF risk)",
            "A10:2021 - Server-Side Request Forgery",
            "CWE-918"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════

    public List<SecurityFinding> scanContent(String content, String location) {
        List<SecurityFinding> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return findings;
        }

        // Scan all pattern categories
        findings.addAll(scanPatterns(content, location, ACCESS_CONTROL_PATTERNS));
        findings.addAll(scanPatterns(content, location, CRYPTO_PATTERNS));
        findings.addAll(scanPatterns(content, location, INJECTION_PATTERNS));
        findings.addAll(scanPatterns(content, location, SSRF_PATTERNS));

        return findings;
    }

    private List<SecurityFinding> scanPatterns(String content, String location,
                                                List<PatternDefinition> patterns) {
        List<SecurityFinding> findings = new ArrayList<>();

        for (PatternDefinition def : patterns) {
            if (def.pattern.matcher(content).find()) {
                findings.add(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    def.severity,
                    def.category,
                    def.description,
                    location,
                    def.pattern.pattern(),
                    def.owaspRef,
                    def.cweRef
                ));
            }
        }

        return findings;
    }

    // Inner class for pattern definitions
    private static class PatternDefinition {
        final Pattern pattern;
        final String category;
        final Severity severity;
        final String description;
        final String owaspRef;
        final String cweRef;

        PatternDefinition(Pattern pattern, String category, Severity severity,
                          String description, String owaspRef, String cweRef) {
            this.pattern = pattern;
            this.category = category;
            this.severity = severity;
            this.description = description;
            this.owaspRef = owaspRef;
            this.cweRef = cweRef;
        }
    }
}
```

### 2.2 Run Tests

```bash
mvn test -pl core -Dtest=OwaspSecurityPatternsSpec
```

**Expected:** All tests pass

---

## Part 3: Pattern Reference Guide

### OWASP to CWE Mapping

| OWASP Category | CWE References |
|----------------|----------------|
| A01: Access Control | CWE-22, CWE-269, CWE-284 |
| A02: Cryptographic | CWE-321, CWE-328, CWE-798, CWE-311 |
| A03: Injection | CWE-78, CWE-79, CWE-89, CWE-90, CWE-611, CWE-917 |
| A07: Auth Failures | CWE-384, CWE-522 |
| A10: SSRF | CWE-918 |

---

## Verification Checklist

- [ ] All 31 specification tests pass
- [ ] A01 patterns: Path traversal, sensitive files, privilege escalation
- [ ] A02 patterns: Secrets, private keys, weak hashes, credit cards
- [ ] A03 patterns: Command, Expression, LDAP, XXE injection
- [ ] A10 patterns: Internal URLs, private IPs
- [ ] All findings have OWASP and CWE references
- [ ] Clean content returns no findings

---

## Bonus Challenges

1. **Add A04 Patterns:** Insecure Design (missing rate limits, etc.)
2. **Add A08 Patterns:** Deserialization vulnerabilities
3. **Add A09 Patterns:** Logging failures (PII in logs)
4. **Create Pattern Analytics:** Track most common finding types

---

## References

- [OWASP Top 10 2021](https://owasp.org/Top10/)
- [CWE Top 25](https://cwe.mitre.org/top25/archive/2021/2021_cwe_top25.html)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
