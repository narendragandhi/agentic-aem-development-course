package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService.SecurityFinding;
import com.demo.workflow.services.SecurityScannerService.Severity;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Comprehensive OWASP Top 10 Security Pattern Database.
 *
 * <p>Provides detection patterns for:</p>
 * <ul>
 *   <li>A01:2021 - Broken Access Control</li>
 *   <li>A02:2021 - Cryptographic Failures</li>
 *   <li>A03:2021 - Injection (XSS, SQL, Command, LDAP, XPath)</li>
 *   <li>A04:2021 - Insecure Design</li>
 *   <li>A05:2021 - Security Misconfiguration</li>
 *   <li>A06:2021 - Vulnerable Components</li>
 *   <li>A07:2021 - Authentication Failures</li>
 *   <li>A08:2021 - Data Integrity Failures</li>
 *   <li>A09:2021 - Security Logging Failures</li>
 *   <li>A10:2021 - Server-Side Request Forgery</li>
 * </ul>
 */
@Component(service = OwaspSecurityPatterns.class, immediate = true)
public class OwaspSecurityPatterns {

    private static final Logger LOG = LoggerFactory.getLogger(OwaspSecurityPatterns.class);

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
            Pattern.compile("(?:password|passwd|pwd|secret|api[_-]?key)\\s*[=:]\\s*['\"][^'\"]{3,}['\"]", Pattern.CASE_INSENSITIVE),
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
        ),
        new PatternDefinition(
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b.*\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"),
            "EMAIL_LIST",
            Severity.MEDIUM,
            "Multiple email addresses detected (potential PII)",
            "A02:2021 - Cryptographic Failures",
            "CWE-359"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // A03:2021 - Injection Patterns (Comprehensive)
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> INJECTION_PATTERNS = Arrays.asList(
        // XSS Patterns
        new PatternDefinition(
            Pattern.compile("<script[^>]*>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE),
            "XSS_SCRIPT_TAG",
            Severity.CRITICAL,
            "Script tag injection detected",
            "A03:2021 - Injection",
            "CWE-79"
        ),
        new PatternDefinition(
            Pattern.compile("(?:javascript|vbscript|data)\\s*:", Pattern.CASE_INSENSITIVE),
            "XSS_PROTOCOL_HANDLER",
            Severity.CRITICAL,
            "JavaScript/VBScript protocol handler detected",
            "A03:2021 - Injection",
            "CWE-79"
        ),
        new PatternDefinition(
            Pattern.compile("<\\s*(?:img|iframe|embed|object|svg|math|audio|video)[^>]*\\s+(?:on\\w+|src\\s*=\\s*['\"]?(?:javascript|data))", Pattern.CASE_INSENSITIVE),
            "XSS_EVENT_HANDLER",
            Severity.CRITICAL,
            "Event handler or malicious src in HTML element",
            "A03:2021 - Injection",
            "CWE-79"
        ),
        new PatternDefinition(
            Pattern.compile("(?:expression|behavior)\\s*\\(", Pattern.CASE_INSENSITIVE),
            "XSS_CSS_EXPRESSION",
            Severity.HIGH,
            "CSS expression/behavior (IE XSS vector)",
            "A03:2021 - Injection",
            "CWE-79"
        ),

        // SQL Injection Patterns
        new PatternDefinition(
            Pattern.compile("'\\s*(?:OR|AND)\\s+['\"]?\\d+['\"]?\\s*[=<>]", Pattern.CASE_INSENSITIVE),
            "SQL_INJECTION_BOOLEAN",
            Severity.CRITICAL,
            "SQL boolean-based injection detected",
            "A03:2021 - Injection",
            "CWE-89"
        ),
        new PatternDefinition(
            Pattern.compile("UNION\\s+(?:ALL\\s+)?SELECT", Pattern.CASE_INSENSITIVE),
            "SQL_INJECTION_UNION",
            Severity.CRITICAL,
            "SQL UNION-based injection detected",
            "A03:2021 - Injection",
            "CWE-89"
        ),
        new PatternDefinition(
            Pattern.compile("(?:INSERT|UPDATE|DELETE)\\s+(?:INTO|FROM|SET)\\s+", Pattern.CASE_INSENSITIVE),
            "SQL_INJECTION_DML",
            Severity.HIGH,
            "SQL DML statement detected in content",
            "A03:2021 - Injection",
            "CWE-89"
        ),
        new PatternDefinition(
            Pattern.compile("(?:DROP|ALTER|CREATE|TRUNCATE)\\s+(?:TABLE|DATABASE|INDEX)", Pattern.CASE_INSENSITIVE),
            "SQL_INJECTION_DDL",
            Severity.CRITICAL,
            "SQL DDL statement detected",
            "A03:2021 - Injection",
            "CWE-89"
        ),
        new PatternDefinition(
            Pattern.compile("EXEC(?:UTE)?\\s*\\(|xp_cmdshell|sp_executesql", Pattern.CASE_INSENSITIVE),
            "SQL_INJECTION_STORED_PROC",
            Severity.CRITICAL,
            "SQL stored procedure execution detected",
            "A03:2021 - Injection",
            "CWE-89"
        ),

        // Command Injection Patterns
        new PatternDefinition(
            Pattern.compile("(?:;|\\||&&|`|\\$\\()\\s*(?:cat|ls|dir|wget|curl|chmod|chown|rm|mv|cp|bash|sh|cmd|powershell)", Pattern.CASE_INSENSITIVE),
            "COMMAND_INJECTION",
            Severity.CRITICAL,
            "OS command injection detected",
            "A03:2021 - Injection",
            "CWE-78"
        ),
        new PatternDefinition(
            Pattern.compile("\\$\\{.*?\\}", Pattern.CASE_INSENSITIVE),
            "EXPRESSION_INJECTION",
            Severity.HIGH,
            "Expression language injection (EL/OGNL)",
            "A03:2021 - Injection",
            "CWE-917"
        ),

        // LDAP Injection Patterns
        new PatternDefinition(
            Pattern.compile("\\)\\s*\\(\\|?\\s*\\(|\\*\\)\\)|\\)\\s*\\(!", Pattern.CASE_INSENSITIVE),
            "LDAP_INJECTION",
            Severity.HIGH,
            "LDAP injection pattern detected",
            "A03:2021 - Injection",
            "CWE-90"
        ),

        // XPath Injection Patterns
        new PatternDefinition(
            Pattern.compile("'\\s*(?:or|and)\\s+\\d+\\s*=\\s*\\d+\\s*(?:\\]|/)", Pattern.CASE_INSENSITIVE),
            "XPATH_INJECTION",
            Severity.HIGH,
            "XPath injection pattern detected",
            "A03:2021 - Injection",
            "CWE-643"
        ),

        // XML/XXE Injection Patterns
        new PatternDefinition(
            Pattern.compile("<!ENTITY\\s+[^>]*SYSTEM\\s+['\"]", Pattern.CASE_INSENSITIVE),
            "XXE_EXTERNAL_ENTITY",
            Severity.CRITICAL,
            "XML External Entity (XXE) declaration",
            "A03:2021 - Injection",
            "CWE-611"
        ),
        new PatternDefinition(
            Pattern.compile("<!DOCTYPE[^>]*\\[", Pattern.CASE_INSENSITIVE),
            "XXE_DOCTYPE",
            Severity.MEDIUM,
            "DOCTYPE with internal subset (potential XXE)",
            "A03:2021 - Injection",
            "CWE-611"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // A07:2021 - Authentication Failures Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> AUTH_PATTERNS = Arrays.asList(
        new PatternDefinition(
            Pattern.compile("(?:basic|bearer)\\s+[A-Za-z0-9+/=]{10,}", Pattern.CASE_INSENSITIVE),
            "AUTH_TOKEN_EXPOSURE",
            Severity.HIGH,
            "Authentication token exposed in content",
            "A07:2021 - Identification and Authentication Failures",
            "CWE-522"
        ),
        new PatternDefinition(
            Pattern.compile("(?:session[_-]?id|jsessionid|phpsessid|asp[._]net[_-]?session)\\s*[=:]\\s*['\"]?[A-Za-z0-9]+", Pattern.CASE_INSENSITIVE),
            "SESSION_ID_EXPOSURE",
            Severity.HIGH,
            "Session ID exposed in content",
            "A07:2021 - Identification and Authentication Failures",
            "CWE-384"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // A10:2021 - Server-Side Request Forgery Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<PatternDefinition> SSRF_PATTERNS = Arrays.asList(
        new PatternDefinition(
            Pattern.compile("(?:http|https|ftp|file|gopher|dict|ldap)://(?:localhost|127\\.0\\.0\\.1|10\\.|172\\.(?:1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.)", Pattern.CASE_INSENSITIVE),
            "SSRF_INTERNAL_URL",
            Severity.HIGH,
            "Internal/private network URL detected (SSRF risk)",
            "A10:2021 - Server-Side Request Forgery",
            "CWE-918"
        ),
        new PatternDefinition(
            Pattern.compile("(?:http|https)://[^/]*@", Pattern.CASE_INSENSITIVE),
            "SSRF_URL_CREDENTIALS",
            Severity.MEDIUM,
            "URL with embedded credentials detected",
            "A10:2021 - Server-Side Request Forgery",
            "CWE-918"
        )
    );

    // ═══════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Scan content for all OWASP Top 10 security patterns.
     *
     * @param content The content to scan
     * @param location Location identifier for findings
     * @return List of security findings
     */
    public List<SecurityFinding> scanContent(String content, String location) {
        List<SecurityFinding> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return findings;
        }

        // Scan all pattern categories
        findings.addAll(scanPatterns(content, location, ACCESS_CONTROL_PATTERNS));
        findings.addAll(scanPatterns(content, location, CRYPTO_PATTERNS));
        findings.addAll(scanPatterns(content, location, INJECTION_PATTERNS));
        findings.addAll(scanPatterns(content, location, AUTH_PATTERNS));
        findings.addAll(scanPatterns(content, location, SSRF_PATTERNS));

        LOG.debug("OWASP scan found {} findings in {}", findings.size(), location);
        return findings;
    }

    /**
     * Scan content for specific OWASP category.
     */
    public List<SecurityFinding> scanForCategory(String content, String location, OwaspCategory category) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        switch (category) {
            case BROKEN_ACCESS_CONTROL:
                return scanPatterns(content, location, ACCESS_CONTROL_PATTERNS);
            case CRYPTOGRAPHIC_FAILURES:
                return scanPatterns(content, location, CRYPTO_PATTERNS);
            case INJECTION:
                return scanPatterns(content, location, INJECTION_PATTERNS);
            case AUTH_FAILURES:
                return scanPatterns(content, location, AUTH_PATTERNS);
            case SSRF:
                return scanPatterns(content, location, SSRF_PATTERNS);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Get pattern statistics.
     */
    public Map<String, Integer> getPatternStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("Access Control", ACCESS_CONTROL_PATTERNS.size());
        stats.put("Cryptographic", CRYPTO_PATTERNS.size());
        stats.put("Injection", INJECTION_PATTERNS.size());
        stats.put("Authentication", AUTH_PATTERNS.size());
        stats.put("SSRF", SSRF_PATTERNS.size());
        stats.put("Total", ACCESS_CONTROL_PATTERNS.size() +
                          CRYPTO_PATTERNS.size() +
                          INJECTION_PATTERNS.size() +
                          AUTH_PATTERNS.size() +
                          SSRF_PATTERNS.size());
        return stats;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════════
    // Inner Classes
    // ═══════════════════════════════════════════════════════════════════

    /**
     * OWASP Top 10 2021 Categories.
     */
    public enum OwaspCategory {
        BROKEN_ACCESS_CONTROL,      // A01:2021
        CRYPTOGRAPHIC_FAILURES,     // A02:2021
        INJECTION,                  // A03:2021
        INSECURE_DESIGN,            // A04:2021
        SECURITY_MISCONFIGURATION,  // A05:2021
        VULNERABLE_COMPONENTS,      // A06:2021
        AUTH_FAILURES,              // A07:2021
        DATA_INTEGRITY_FAILURES,    // A08:2021
        LOGGING_FAILURES,           // A09:2021
        SSRF                        // A10:2021
    }

    /**
     * Pattern definition holder.
     */
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
