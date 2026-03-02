package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService.SecurityFinding;
import com.demo.workflow.services.SecurityScannerService.Severity;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Specification Tests for OWASP Security Patterns.
 *
 * Comprehensive coverage of OWASP Top 10 2021 detection patterns.
 */
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
        @DisplayName("should detect Windows path traversal")
        void shouldDetectWindowsPathTraversal() {
            String content = "GET /files/..\\..\\windows\\system32\\config HTTP/1.1";

            List<SecurityFinding> findings = patterns.scanContent(content, "request");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PATH_TRAVERSAL")));
        }

        @Test
        @DisplayName("should detect sensitive file access attempts")
        void shouldDetectSensitiveFileAccess() {
            String content = "file=/etc/passwd";

            List<SecurityFinding> findings = patterns.scanContent(content, "param");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SENSITIVE_FILE_ACCESS")));
        }

        @Test
        @DisplayName("should detect privilege escalation attempts")
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
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect API keys in content")
        void shouldDetectApiKeys() {
            String content = "api_key=\"sk_live_abcdef123456\"";

            List<SecurityFinding> findings = patterns.scanContent(content, "config");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("HARDCODED_SECRET")));
        }

        @Test
        @DisplayName("should detect private keys")
        void shouldDetectPrivateKeys() {
            String content = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAK...\n-----END RSA PRIVATE KEY-----";

            List<SecurityFinding> findings = patterns.scanContent(content, "file");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PRIVATE_KEY_EXPOSURE")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
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
        @DisplayName("should detect potential credit card numbers")
        void shouldDetectCreditCardNumbers() {
            String content = "Card: 4111-1111-1111-1111";

            List<SecurityFinding> findings = patterns.scanContent(content, "form");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("CREDIT_CARD_NUMBER")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A03:2021 - Injection (XSS)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A03:2021 - XSS Injection")
    class XssInjection {

        @Test
        @DisplayName("should detect script tags")
        void shouldDetectScriptTags() {
            String content = "<script>alert('XSS')</script>";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("XSS_SCRIPT_TAG")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect javascript protocol")
        void shouldDetectJavascriptProtocol() {
            String content = "<a href=\"javascript:alert('XSS')\">Click</a>";

            List<SecurityFinding> findings = patterns.scanContent(content, "html");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("XSS_PROTOCOL_HANDLER")));
        }

        @Test
        @DisplayName("should detect event handlers in elements")
        void shouldDetectEventHandlers() {
            String content = "<img src='x' onerror='alert(1)'>";

            List<SecurityFinding> findings = patterns.scanContent(content, "html");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("XSS_EVENT_HANDLER")));
        }

        @Test
        @DisplayName("should detect data URI XSS")
        void shouldDetectDataUriXss() {
            String content = "<iframe src=\"data:text/html,<script>alert(1)</script>\">";

            List<SecurityFinding> findings = patterns.scanContent(content, "html");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("XSS_PROTOCOL_HANDLER")));
        }

        @Test
        @DisplayName("should detect CSS expressions")
        void shouldDetectCssExpressions() {
            String content = "style=\"width: expression(alert('XSS'))\"";

            List<SecurityFinding> findings = patterns.scanContent(content, "css");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("XSS_CSS_EXPRESSION")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A03:2021 - Injection (SQL)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A03:2021 - SQL Injection")
    class SqlInjection {

        @Test
        @DisplayName("should detect boolean-based SQL injection")
        void shouldDetectBooleanBasedSqlInjection() {
            String content = "' OR 1=1 --";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SQL_INJECTION_BOOLEAN")));
        }

        @Test
        @DisplayName("should detect UNION-based SQL injection")
        void shouldDetectUnionBasedSqlInjection() {
            String content = "' UNION SELECT username, password FROM users--";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SQL_INJECTION_UNION")));
        }

        @Test
        @DisplayName("should detect DDL injection")
        void shouldDetectDdlInjection() {
            String content = "'; DROP TABLE users;--";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SQL_INJECTION_DDL")));
        }

        @Test
        @DisplayName("should detect stored procedure execution")
        void shouldDetectStoredProcExecution() {
            String content = "'; EXEC xp_cmdshell('dir');--";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SQL_INJECTION_STORED_PROC")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A03:2021 - Injection (Command, LDAP, XPath)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A03:2021 - Other Injection Types")
    class OtherInjections {

        @Test
        @DisplayName("should detect OS command injection")
        void shouldDetectCommandInjection() {
            String content = "; cat /etc/passwd |";

            List<SecurityFinding> findings = patterns.scanContent(content, "input");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("COMMAND_INJECTION")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
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
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // A07:2021 - Authentication Failures
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A07:2021 - Authentication Failures")
    class AuthenticationFailures {

        @Test
        @DisplayName("should detect exposed auth tokens")
        void shouldDetectExposedAuthTokens() {
            String content = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";

            List<SecurityFinding> findings = patterns.scanContent(content, "header");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("AUTH_TOKEN_EXPOSURE")));
        }

        @Test
        @DisplayName("should detect exposed session IDs")
        void shouldDetectExposedSessionIds() {
            String content = "session_id=abc123def456";

            List<SecurityFinding> findings = patterns.scanContent(content, "cookie");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SESSION_ID_EXPOSURE")));
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

        @Test
        @DisplayName("should detect URL with embedded credentials")
        void shouldDetectUrlWithCredentials() {
            String content = "http://admin:password@internal-server/";

            List<SecurityFinding> findings = patterns.scanContent(content, "config");

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("SSRF_URL_CREDENTIALS")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Pattern Statistics
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Pattern Statistics")
    class PatternStatistics {

        @Test
        @DisplayName("should have comprehensive pattern coverage")
        void shouldHaveComprehensivePatternCoverage() {
            Map<String, Integer> stats = patterns.getPatternStats();

            assertTrue(stats.get("Total") >= 25, "Should have at least 25 patterns");
            assertTrue(stats.get("Injection") >= 10, "Should have at least 10 injection patterns");
        }

        @Test
        @DisplayName("should return empty list for clean content")
        void shouldReturnEmptyForCleanContent() {
            String content = "This is a normal, safe text content without any malicious patterns.";

            List<SecurityFinding> findings = patterns.scanContent(content, "text");

            assertTrue(findings.isEmpty());
        }

        @Test
        @DisplayName("should handle null content gracefully")
        void shouldHandleNullContent() {
            List<SecurityFinding> findings = patterns.scanContent(null, "test");

            assertTrue(findings.isEmpty());
        }

        @Test
        @DisplayName("should handle empty content gracefully")
        void shouldHandleEmptyContent() {
            List<SecurityFinding> findings = patterns.scanContent("", "test");

            assertTrue(findings.isEmpty());
        }
    }
}
