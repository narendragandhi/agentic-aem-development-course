# Lab 4: Complete Security Scanner (4 hours)

## Objective
Build a comprehensive security scanner covering basic patterns, document security (PDF/Office), and OWASP Top 10 vulnerabilities.

---

## Part 1: Basic Security Patterns (45 min)

*Covered in Lab 3 - XSS, SQL Injection, File Validation*

Review your implementation from Lab 3 before proceeding.

---

## Part 2: Document Security Scanner (1.5 hours)

### 2.1 PDF Security Threats

| Threat | Detection Method | Severity |
|--------|------------------|----------|
| JavaScript | `/JavaScript` pattern | HIGH |
| Launch Action | `/Launch` pattern | CRITICAL |
| Embedded Files | `/EmbeddedFile` pattern | MEDIUM |
| Auto-Execute | `/OpenAction` pattern | HIGH |

### 2.2 Implement PDF Scanner

```java
@Component(service = DocumentSecurityScanner.class, immediate = true)
public class DocumentSecurityScanner {

    private static final Pattern PDF_JAVASCRIPT = Pattern.compile(
        "/JavaScript|/JS\\s", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_LAUNCH_ACTION = Pattern.compile(
        "/Launch", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_EMBEDDED_FILE = Pattern.compile(
        "/EmbeddedFile|/FileAttachment", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_OPEN_ACTION = Pattern.compile(
        "/OpenAction", Pattern.CASE_INSENSITIVE);

    public List<SecurityFinding> scanPdf(InputStream content) {
        List<SecurityFinding> findings = new ArrayList<>();

        try {
            byte[] pdfBytes = readLimited(content, 10 * 1024 * 1024);
            String pdfContent = new String(pdfBytes, StandardCharsets.ISO_8859_1);

            if (PDF_JAVASCRIPT.matcher(pdfContent).find()) {
                findings.add(createFinding("PDF_JAVASCRIPT", Severity.HIGH,
                    "JavaScript detected in PDF"));
            }
            if (PDF_LAUNCH_ACTION.matcher(pdfContent).find()) {
                findings.add(createFinding("PDF_LAUNCH_ACTION", Severity.CRITICAL,
                    "Launch action can execute external programs"));
            }
            if (PDF_EMBEDDED_FILE.matcher(pdfContent).find()) {
                findings.add(createFinding("PDF_EMBEDDED_FILE", Severity.MEDIUM,
                    "Embedded file may contain malware"));
            }
            if (PDF_OPEN_ACTION.matcher(pdfContent).find()) {
                findings.add(createFinding("PDF_OPEN_ACTION", Severity.HIGH,
                    "Auto-execute action on document open"));
            }
        } catch (IOException e) {
            LOG.error("Error scanning PDF", e);
        }

        return findings;
    }
}
```

### 2.3 Office Document Scanning (OOXML)

Office documents (DOCX, XLSX, PPTX) are ZIP archives:

```java
public List<SecurityFinding> scanOfficeDocument(InputStream content, String mimeType) {
    List<SecurityFinding> findings = new ArrayList<>();

    try {
        byte[] zipBytes = readLimited(content, 50 * 1024 * 1024);
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes));

        ZipEntry entry;
        boolean hasMacros = false;
        boolean hasActiveX = false;
        boolean hasOleObjects = false;

        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName().toLowerCase();

            // VBA Macros
            if (name.contains("vbaproject.bin")) {
                hasMacros = true;
            }

            // Check XML content
            if (name.endsWith(".xml")) {
                String xml = readZipEntry(zis);
                if (xml.contains("activeX")) hasActiveX = true;
                if (xml.contains("oleObject")) hasOleObjects = true;
            }

            zis.closeEntry();
        }

        if (hasMacros) {
            findings.add(createFinding("OFFICE_MACROS", Severity.HIGH,
                "VBA macros detected - can execute code"));
        }
        if (hasActiveX) {
            findings.add(createFinding("OFFICE_ACTIVEX", Severity.CRITICAL,
                "ActiveX controls can execute arbitrary code"));
        }
        if (hasOleObjects) {
            findings.add(createFinding("OFFICE_OLE", Severity.HIGH,
                "OLE objects may contain embedded executables"));
        }

    } catch (IOException e) {
        LOG.error("Error scanning Office document", e);
    }

    return findings;
}
```

### 2.4 Write Tests

```java
@Test
void shouldDetectJavaScriptInPdf() {
    String pdfContent = "%PDF-1.4\n/JavaScript (alert('xss'))\n%%EOF";
    List<SecurityFinding> findings = scanner.scanPdf(
        new ByteArrayInputStream(pdfContent.getBytes()));

    assertTrue(findings.stream()
        .anyMatch(f -> f.getCategory().equals("PDF_JAVASCRIPT")));
}

@Test
void shouldDetectMacrosInOffice() throws IOException {
    byte[] docx = createMockOOXML("vbaProject.bin", "macro content");
    List<SecurityFinding> findings = scanner.scanOfficeDocument(
        new ByteArrayInputStream(docx), "application/vnd.openxmlformats...");

    assertTrue(findings.stream()
        .anyMatch(f -> f.getCategory().equals("OFFICE_MACROS")));
}
```

---

## Part 3: OWASP Security Patterns (1.5 hours)

### 3.1 OWASP Top 10 Coverage

| Category | Patterns to Detect |
|----------|-------------------|
| A01: Access Control | Path traversal, privilege escalation |
| A02: Crypto Failures | Hardcoded secrets, weak hashes, PII |
| A03: Injection | XSS, SQL, Command, LDAP, XXE |
| A07: Auth Failures | Token exposure, session leaks |
| A10: SSRF | Internal URLs, private IPs |

### 3.2 Implement Pattern Database

```java
@Component(service = OwaspSecurityPatterns.class, immediate = true)
public class OwaspSecurityPatterns {

    // A01: Broken Access Control
    private static final List<PatternDef> ACCESS_CONTROL = Arrays.asList(
        new PatternDef(
            Pattern.compile("\\.\\./|\\.\\.\\\\"),
            "PATH_TRAVERSAL", Severity.CRITICAL,
            "Path traversal attempt", "A01:2021", "CWE-22"
        ),
        new PatternDef(
            Pattern.compile("admin\\s*=\\s*true", Pattern.CASE_INSENSITIVE),
            "PRIVILEGE_ESCALATION", Severity.HIGH,
            "Privilege escalation attempt", "A01:2021", "CWE-269"
        )
    );

    // A02: Cryptographic Failures
    private static final List<PatternDef> CRYPTO = Arrays.asList(
        new PatternDef(
            Pattern.compile("password\\s*=\\s*['\"][^'\"]+['\"]", Pattern.CASE_INSENSITIVE),
            "HARDCODED_SECRET", Severity.CRITICAL,
            "Hardcoded password", "A02:2021", "CWE-798"
        ),
        new PatternDef(
            Pattern.compile("-----BEGIN.*PRIVATE KEY-----"),
            "PRIVATE_KEY", Severity.CRITICAL,
            "Private key exposed", "A02:2021", "CWE-321"
        ),
        new PatternDef(
            Pattern.compile("\\b(?:\\d{4}[- ]?){4}\\b"),
            "CREDIT_CARD", Severity.HIGH,
            "Credit card number detected", "A02:2021", "CWE-311"
        )
    );

    // A03: Injection (beyond XSS/SQL)
    private static final List<PatternDef> INJECTION = Arrays.asList(
        new PatternDef(
            Pattern.compile(";\\s*(?:cat|ls|rm|wget)\\s", Pattern.CASE_INSENSITIVE),
            "COMMAND_INJECTION", Severity.CRITICAL,
            "OS command injection", "A03:2021", "CWE-78"
        ),
        new PatternDef(
            Pattern.compile("<!ENTITY.*SYSTEM", Pattern.CASE_INSENSITIVE),
            "XXE", Severity.CRITICAL,
            "XML External Entity", "A03:2021", "CWE-611"
        ),
        new PatternDef(
            Pattern.compile("\\$\\{.*\\}"),
            "EXPRESSION_INJECTION", Severity.HIGH,
            "Expression language injection", "A03:2021", "CWE-917"
        )
    );

    // A10: SSRF
    private static final List<PatternDef> SSRF = Arrays.asList(
        new PatternDef(
            Pattern.compile("https?://(?:localhost|127\\.0\\.0\\.1|192\\.168\\.)"),
            "SSRF_INTERNAL", Severity.HIGH,
            "Internal URL access (SSRF)", "A10:2021", "CWE-918"
        )
    );

    public List<SecurityFinding> scanContent(String content, String location) {
        List<SecurityFinding> findings = new ArrayList<>();

        findings.addAll(scan(content, location, ACCESS_CONTROL));
        findings.addAll(scan(content, location, CRYPTO));
        findings.addAll(scan(content, location, INJECTION));
        findings.addAll(scan(content, location, SSRF));

        return findings;
    }

    private List<SecurityFinding> scan(String content, String location,
                                        List<PatternDef> patterns) {
        return patterns.stream()
            .filter(p -> p.pattern.matcher(content).find())
            .map(p -> new SecurityFinding(
                UUID.randomUUID().toString(),
                p.severity, p.category, p.description,
                location, p.pattern.pattern(), p.owasp, p.cwe
            ))
            .collect(Collectors.toList());
    }
}
```

### 3.3 Write OWASP Tests

```java
@Nested
@DisplayName("OWASP Top 10 Detection")
class OwaspDetection {

    @Test
    void shouldDetectPathTraversal() {
        List<SecurityFinding> findings = patterns.scanContent(
            "GET /files/../../../etc/passwd", "request");
        assertTrue(findings.stream()
            .anyMatch(f -> f.getCategory().equals("PATH_TRAVERSAL")));
    }

    @Test
    void shouldDetectHardcodedSecrets() {
        List<SecurityFinding> findings = patterns.scanContent(
            "password='MySecret123'", "config");
        assertTrue(findings.stream()
            .anyMatch(f -> f.getCategory().equals("HARDCODED_SECRET")));
    }

    @Test
    void shouldDetectCommandInjection() {
        List<SecurityFinding> findings = patterns.scanContent(
            "; rm -rf /", "input");
        assertTrue(findings.stream()
            .anyMatch(f -> f.getCategory().equals("COMMAND_INJECTION")));
    }

    @Test
    void shouldDetectSsrf() {
        List<SecurityFinding> findings = patterns.scanContent(
            "url=http://localhost:8080/admin", "param");
        assertTrue(findings.stream()
            .anyMatch(f -> f.getCategory().equals("SSRF_INTERNAL")));
    }
}
```

---

## Part 4: Integration (15 min)

### 4.1 Wire Everything Together

```java
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private volatile DocumentSecurityScanner documentScanner;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private volatile OwaspSecurityPatterns owaspPatterns;

    @Override
    public SecurityScanResult scanAsset(Asset asset) {
        List<SecurityFinding> findings = new ArrayList<>();

        // Basic metadata scan
        findings.addAll(scanMetadata(extractMetadata(asset)));

        // File type validation
        // ... existing code ...

        // Document-specific scanning
        String mimeType = asset.getMimeType();
        if (documentScanner != null && documentScanner.isScannableDocument(mimeType)) {
            try (InputStream content = asset.getOriginal().getStream()) {
                if (mimeType.equals("application/pdf")) {
                    findings.addAll(documentScanner.scanPdf(content));
                } else {
                    findings.addAll(documentScanner.scanOfficeDocument(content, mimeType));
                }
            }
        }

        // OWASP pattern scan on text content
        // ...

        return new SecurityScanResult(findings, calculateSeverity(findings), ...);
    }
}
```

---

## Verification Checklist

- [ ] Basic patterns: XSS, SQL injection (from Lab 3)
- [ ] PDF scanning: JavaScript, Launch, Embedded files
- [ ] Office scanning: Macros, ActiveX, OLE
- [ ] OWASP patterns: Path traversal, secrets, command injection, SSRF
- [ ] All components integrated
- [ ] 50+ tests passing

---

## Run All Tests

```bash
mvn test -pl core
# Expected: 165 tests passing
```

---

## Next Lab
[Lab 5: Agent Orchestrator](../lab-05-agent-orchestrator/README.md)
