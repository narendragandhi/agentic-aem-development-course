# Lab 3: TDD Development (3 hours)

## Objective
Apply Test-Driven Development to build AEM services, following the RED-GREEN-REFACTOR cycle with AI assistance.

---

## Part 1: TDD Fundamentals (30 min)

### 1.1 The TDD Cycle

```
    ┌─────────────────────────────────────────┐
    │                                         │
    │   ┌─────┐    ┌───────┐    ┌──────────┐│
    │   │ RED │───▶│ GREEN │───▶│ REFACTOR ││
    │   └──┬──┘    └───────┘    └────┬─────┘│
    │      │                         │       │
    │      └─────────────────────────┘       │
    │                                         │
    └─────────────────────────────────────────┘

RED:      Write a failing test
GREEN:    Write minimal code to pass
REFACTOR: Improve code, keep tests green
```

### 1.2 Spec Tests vs Unit Tests

| Aspect | Spec Test | Unit Test |
|--------|-----------|-----------|
| Purpose | Define behavior | Verify implementation |
| Written | Before code | After/during code |
| Naming | `*Spec.java` | `*Test.java` |
| Focus | What it should do | How it does it |

---

## Part 2: RED Phase - Write Specifications (1 hour)

### 2.1 Create Service Interface

First, define the contract based on domain models:

```java
package com.demo.workflow.services;

public interface SecurityScannerService {

    SecurityScanResult scanAsset(Asset asset);

    List<SecurityFinding> scanMetadata(Map<String, Object> metadata);

    FileTypeValidation validateFileType(InputStream content,
                                         String mimeType, String fileName);

    List<SecurityFinding> scanForEmbeddedScripts(InputStream content,
                                                   String mimeType);

    // Inner classes
    class SecurityScanResult { /* ... */ }
    class SecurityFinding { /* ... */ }
    class FileTypeValidation { /* ... */ }
    enum Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }
}
```

### 2.2 Write Specification Tests

Create `SecurityScannerServiceSpec.java`:

```java
package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService;
import com.demo.workflow.services.SecurityScannerService.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityScannerService Specification")
class SecurityScannerServiceSpec {

    private SecurityScannerService service;

    @BeforeEach
    void setUp() {
        service = new SecurityScannerServiceImpl();
    }

    // ═══════════════════════════════════════════════════════════════
    // XSS Detection Specifications
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("XSS Detection")
    class XssDetection {

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

        @Test
        @DisplayName("should detect javascript: protocol")
        void shouldDetectJavascriptProtocol() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("link", "javascript:alert('xss')");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("XSS")));
        }

        @Test
        @DisplayName("should detect event handlers")
        void shouldDetectEventHandlers() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("description", "<img onerror='alert(1)'>");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("XSS")));
        }

        @Test
        @DisplayName("should return empty for clean metadata")
        void shouldReturnEmptyForCleanMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("dc:title", "Safe Title");
            metadata.put("dc:description", "Normal description");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SQL Injection Specifications
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SQL Injection Detection")
    class SqlInjection {

        @Test
        @DisplayName("should detect OR 1=1 pattern")
        void shouldDetectOr1Equals1() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filter", "' OR 1=1 --");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("SQL_INJECTION")));
        }

        @Test
        @DisplayName("should detect UNION SELECT")
        void shouldDetectUnionSelect() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("query", "1 UNION SELECT * FROM users");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("SQL_INJECTION")));
        }

        @Test
        @DisplayName("should detect DROP TABLE")
        void shouldDetectDropTable() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("input", "'; DROP TABLE users;--");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("SQL_INJECTION")));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // File Type Validation Specifications
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("File Type Validation")
    class FileTypeValidation {

        @Test
        @DisplayName("should detect double extension attack")
        void shouldDetectDoubleExtension() {
            byte[] content = "test".getBytes();
            InputStream stream = new ByteArrayInputStream(content);

            var result = service.validateFileType(
                stream, "application/pdf", "document.pdf.exe");

            assertFalse(result.isValid());
            assertEquals(Severity.CRITICAL, result.getSeverity());
        }

        @Test
        @DisplayName("should detect null byte injection")
        void shouldDetectNullByteInjection() {
            byte[] content = "test".getBytes();
            InputStream stream = new ByteArrayInputStream(content);

            var result = service.validateFileType(
                stream, "image/png", "image.png%00.exe");

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("should detect file type mismatch")
        void shouldDetectFileTypeMismatch() {
            // EXE magic bytes (MZ header)
            byte[] exeContent = new byte[]{0x4D, 0x5A, 0x00, 0x00};
            InputStream stream = new ByteArrayInputStream(exeContent);

            var result = service.validateFileType(
                stream, "image/png", "image.png");

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("should accept valid PNG")
        void shouldAcceptValidPng() {
            // PNG magic bytes
            byte[] pngHeader = new byte[]{
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
            };
            InputStream stream = new ByteArrayInputStream(pngHeader);

            var result = service.validateFileType(
                stream, "image/png", "image.png");

            assertTrue(result.isValid());
        }
    }
}
```

### 2.3 Run Tests (Verify RED)

```bash
mvn test -pl core -Dtest=SecurityScannerServiceSpec
# Expected: Compilation errors or test failures
```

---

## Part 3: GREEN Phase - Implement (1 hour)

### 3.1 Create Stub Implementation

```java
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {

    private static final Logger LOG = LoggerFactory.getLogger(
        SecurityScannerServiceImpl.class);

    // XSS Patterns
    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
        Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE)
    );

    // SQL Injection Patterns
    private static final List<Pattern> SQL_PATTERNS = Arrays.asList(
        Pattern.compile("'\\s*OR\\s+\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("UNION\\s+SELECT", Pattern.CASE_INSENSITIVE),
        Pattern.compile("DROP\\s+TABLE", Pattern.CASE_INSENSITIVE)
    );

    @Override
    public List<SecurityFinding> scanMetadata(Map<String, Object> metadata) {
        List<SecurityFinding> findings = new ArrayList<>();

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                findings.addAll(checkXss(entry.getKey(), value));
                findings.addAll(checkSqlInjection(entry.getKey(), value));
            }
        }

        return findings;
    }

    private List<SecurityFinding> checkXss(String field, String value) {
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return List.of(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    Severity.CRITICAL,
                    "XSS",
                    "XSS pattern detected in " + field,
                    field,
                    pattern.pattern(),
                    "A7:2017",
                    "CWE-79"
                ));
            }
        }
        return Collections.emptyList();
    }

    private List<SecurityFinding> checkSqlInjection(String field, String value) {
        for (Pattern pattern : SQL_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return List.of(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    Severity.CRITICAL,
                    "SQL_INJECTION",
                    "SQL injection pattern detected in " + field,
                    field,
                    pattern.pattern(),
                    "A1:2017",
                    "CWE-89"
                ));
            }
        }
        return Collections.emptyList();
    }

    // Implement remaining methods...
}
```

### 3.2 Run Tests (Verify GREEN)

```bash
mvn test -pl core -Dtest=SecurityScannerServiceSpec
# Expected: All tests pass
```

---

## Part 4: REFACTOR Phase (30 min)

### 4.1 Improve Code Quality

1. Extract pattern definitions to constants
2. Add comprehensive Javadocs
3. Add logging
4. Improve error handling

### 4.2 Verify Tests Still Pass

```bash
mvn test -pl core
# Expected: All tests still pass
```

---

## Part 5: BEAD Task Tracking

### 5.1 Create BEAD Task File

```yaml
# course/03-bead-tasks/tasks/SECURITY-001.yaml
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
  patterns:
    - XSS detection
    - SQL injection detection
    - File type validation

execute:
  command: "mvn test -pl core -Dtest=SecurityScannerServiceSpec"
  result: "15 passing"

analyze:
  coverage: "85%"
  complexity: "Low"

document:
  javadoc: true
  readme: true
```

---

## Verification Checklist

- [ ] Interface defined with all methods
- [ ] 15 specification tests written
- [ ] Tests fail initially (RED)
- [ ] Implementation passes all tests (GREEN)
- [ ] Code refactored for quality
- [ ] BEAD task file created

---

## Key Takeaways

1. **Tests first** - Define behavior before implementation
2. **Minimal code** - Only write enough to pass tests
3. **Refactor safely** - Tests protect against regressions
4. **Track progress** - BEAD files document completion

---

## Next Lab
[Lab 4: Security Scanner](../lab-04-security-scanner/README.md)
