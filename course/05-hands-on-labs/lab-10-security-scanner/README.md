# Lab 10: Security Scanner Implementation

## Objective
Implement a security scanner service for AEM DAM using Test-Driven Development (TDD). This lab demonstrates TDD methodology applied to security-critical code with OWASP pattern detection.

---

## Prerequisites
- Completed Lab 09 (TDD Integration)
- Understanding of OWASP Top 10
- Java regex pattern matching

---

## Learning Outcomes
After completing this lab, you will be able to:
1. Write specification tests for security scanning
2. Implement XSS and SQL injection detection
3. Validate file types using magic bytes
4. Detect embedded scripts in documents
5. Apply TDD RED-GREEN-REFACTOR cycle

---

## Lab Structure

```
lab-10-security-scanner/
├── README.md                    # This file
├── spec/                        # Specification tests (provided)
│   └── SecurityScannerServiceSpec.java
├── solution/                    # Reference implementation
│   ├── SecurityScannerService.java
│   └── SecurityScannerServiceImpl.java
└── exercises/                   # Student exercises
    ├── exercise-1-xss.md
    ├── exercise-2-sql-injection.md
    ├── exercise-3-file-validation.md
    └── exercise-4-embedded-scripts.md
```

---

## Part 1: Understanding the Specification

### 1.1 Review the Test Specification

The specification tests define 15 behaviors across 4 categories:

| Category | Tests | Severity |
|----------|-------|----------|
| XSS Detection | 4 | CRITICAL |
| SQL Injection | 3 | CRITICAL |
| File Type Validation | 4 | HIGH/CRITICAL |
| Embedded Scripts | 4 | HIGH |

### 1.2 Test Categories

**XSS Detection Tests:**
```java
@Test
@DisplayName("should detect script tags in metadata")
void shouldDetectScriptTags() {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("dc:title", "<script>alert('xss')</script>");

    List<SecurityFinding> findings = service.scanMetadata(metadata);

    assertFalse(findings.isEmpty());
    assertTrue(findings.stream().anyMatch(f ->
        f.getCategory().equals("XSS") &&
        f.getSeverity() == Severity.CRITICAL));
}
```

**SQL Injection Tests:**
```java
@Test
@DisplayName("should detect UNION SELECT")
void shouldDetectUnionSelect() {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("filter", "1 UNION SELECT * FROM users");

    List<SecurityFinding> findings = service.scanMetadata(metadata);

    assertTrue(findings.stream().anyMatch(f ->
        f.getCategory().equals("SQL_INJECTION") &&
        f.getSeverity() == Severity.CRITICAL));
}
```

**File Validation Tests:**
```java
@Test
@DisplayName("should detect double extension attack")
void shouldDetectDoubleExtension() {
    byte[] content = "test".getBytes();
    InputStream stream = new ByteArrayInputStream(content);

    FileTypeValidation result = service.validateFileType(
        stream, "application/pdf", "document.pdf.exe");

    assertFalse(result.isValid());
    assertEquals(Severity.CRITICAL, result.getSeverity());
}
```

---

## Part 2: RED Phase - Verify Tests Fail

### 2.1 Create Empty Interface

```java
public interface SecurityScannerService {
    List<SecurityFinding> scanMetadata(Map<String, Object> metadata);
    FileTypeValidation validateFileType(InputStream content,
                                         String mimeType, String fileName);
    List<SecurityFinding> scanForEmbeddedScripts(InputStream content,
                                                   String mimeType);

    // Inner classes
    class SecurityFinding { /* fields */ }
    class FileTypeValidation { /* fields */ }
    enum Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }
}
```

### 2.2 Create Stub Implementation

```java
@Component(service = SecurityScannerService.class)
public class SecurityScannerServiceImpl implements SecurityScannerService {

    @Override
    public List<SecurityFinding> scanMetadata(Map<String, Object> metadata) {
        return Collections.emptyList(); // Will fail tests
    }

    @Override
    public FileTypeValidation validateFileType(InputStream content,
                                                String mimeType, String fileName) {
        return new FileTypeValidation(true, null, mimeType, null, null);
    }

    @Override
    public List<SecurityFinding> scanForEmbeddedScripts(InputStream content,
                                                          String mimeType) {
        return Collections.emptyList();
    }
}
```

### 2.3 Run Tests

```bash
mvn test -pl core -Dtest=SecurityScannerServiceSpec
```

**Expected Result:** 15 failures

---

## Part 3: GREEN Phase - Implement to Pass

### Exercise 1: XSS Detection

Implement pattern matching for XSS attacks:

```java
private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
    Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
    Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
    Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
    Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE)
);

private List<SecurityFinding> checkXssPatterns(String field, String value) {
    List<SecurityFinding> findings = new ArrayList<>();
    for (Pattern pattern : XSS_PATTERNS) {
        if (pattern.matcher(value).find()) {
            findings.add(new SecurityFinding(
                UUID.randomUUID().toString(),
                Severity.CRITICAL,
                "XSS",
                "Potential XSS pattern in: " + field,
                field,
                pattern.pattern(),
                "A7:2017 - Cross-Site Scripting",
                "CWE-79"
            ));
            break;
        }
    }
    return findings;
}
```

### Exercise 2: SQL Injection Detection

```java
private static final List<Pattern> SQL_PATTERNS = Arrays.asList(
    Pattern.compile("'\\s*OR\\s+\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),
    Pattern.compile("UNION\\s+SELECT", Pattern.CASE_INSENSITIVE),
    Pattern.compile("DROP\\s+TABLE", Pattern.CASE_INSENSITIVE)
);
```

### Exercise 3: File Type Validation

```java
private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();
static {
    MAGIC_BYTES.put("image/png", new byte[]{
        (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    });
    MAGIC_BYTES.put("image/jpeg", new byte[]{
        (byte)0xFF, (byte)0xD8, (byte)0xFF
    });
}

private boolean hasDoubleExtension(String fileName) {
    // Detect patterns like document.pdf.exe
}

private boolean hasNullByteInjection(String fileName) {
    return fileName.contains("%00") || fileName.contains("\u0000");
}
```

### Exercise 4: Embedded Script Detection

```java
private boolean isScriptCapableMimeType(String mimeType) {
    String lower = mimeType.toLowerCase();
    return lower.contains("svg") ||
           lower.contains("html") ||
           lower.contains("xml");
}

private boolean containsScriptTag(String content) {
    String lower = content.toLowerCase();
    return lower.contains("<script") || lower.contains("</script");
}
```

---

## Part 4: REFACTOR Phase

### 4.1 Code Quality Improvements

1. **Extract Constants:** Move patterns to class constants
2. **Add Logging:** Log security findings
3. **Documentation:** Add OWASP/CWE references

### 4.2 Run All Tests

```bash
mvn test -pl core
```

**Expected Result:** 66 tests passing (51 original + 15 new)

---

## Part 5: BEAD Task Integration

### 5.1 Create BEAD Task File

```yaml
task_id: SECURITY-001
title: "Implement SecurityScannerService"
status: completed

tdd:
  spec_file: "SecurityScannerServiceSpec.java"
  test_count: 15
  phase: green

build:
  interface: "SecurityScannerService.java"
  implementation: "SecurityScannerServiceImpl.java"

execute:
  command: "mvn test -pl core -Dtest=SecurityScannerServiceSpec"
  expected: "15 passing"
```

---

## Verification Checklist

- [ ] All 15 specification tests pass
- [ ] XSS patterns detected: `<script>`, `javascript:`, `on*=`
- [ ] SQL patterns detected: `OR 1=1`, `UNION SELECT`, `DROP TABLE`
- [ ] File validation: magic bytes, double extension, null byte
- [ ] Embedded scripts: SVG, HTML detection
- [ ] OWASP/CWE references in findings
- [ ] BEAD task file created

---

## Bonus Challenges

1. **Add Command Injection Detection:** Patterns like `; rm -rf`, `| cat /etc/passwd`
2. **Add Path Traversal Detection:** Patterns like `../`, `....//`
3. **Create Workflow Integration:** Add SecurityScanProcess to workflow

---

## References

- [OWASP Top 10](https://owasp.org/Top10/)
- [CWE Database](https://cwe.mitre.org/)
- [Magic Bytes Reference](https://en.wikipedia.org/wiki/List_of_file_signatures)
- [SecurityScannerService PRD](../../01-prd/security-scanner-prd.md)
