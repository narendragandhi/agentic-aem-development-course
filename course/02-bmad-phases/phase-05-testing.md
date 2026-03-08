# BMAD Phase 05: Testing

## Overview

Phase 05 (Testing) ensures that the implementation meets requirements and maintains quality standards. This includes unit testing, integration testing, security testing, and performance testing.

---

## Objectives

- Verify implementation matches specifications
- Achieve required code coverage
- Identify and fix defects
- Validate security controls
- Measure performance metrics

---

## Test Pyramid

```
                    ▲
                   ╱ ╲
                  ╱   ╲
                 ╱  E2E ╲
                ╱─────────╲
               ╱───────────╲
              ╱ Integration╲
             ╱─────────────╲
            ╱───────────────╲
           ╱    Unit Tests   ╲
          ╱───────────────────╲
```

### Test Distribution

| Level | Percentage | Focus |
|-------|------------|-------|
| Unit Tests | 70% | Individual methods/classes |
| Integration Tests | 20% | Component interactions |
| E2E Tests | 10% | Full workflows |

---

## Activities

### 5.1 Unit Testing

Create unit tests for all service classes:

**Test Structure:**
```java
class AntivirusScanServiceImplTest {

    @Mock
    private ClamAVAdapter clamAVAdapter;

    @InjectMocks
    private AntivirusScanServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCleanResultForSafeFile() {
        // Given
        InputStream cleanFile = new ByteArrayInputStream("safe content".getBytes());
        when(clamAVAdapter.scan(any())).thenReturn(ScanResult.CLEAN);

        // When
        ScanResult result = service.scan(cleanFile, "document.pdf");

        // Then
        assertEquals(ScanResult.CLEAN, result);
    }

    @Test
    void shouldReturnInfectedForMalware() {
        // Given
        InputStream infectedFile = new ByteArrayInputStream("malware".getBytes());
        when(clamAVAdapter.scan(any())).thenReturn(ScanResult.INFECTED);

        // When
        ScanResult result = service.scan(infectedFile, "document.pdf");

        // Then
        assertEquals(ScanResult.INFECTED, result);
    }
}
```

### 5.2 Specification Tests (TDD)

Write specification tests following RED-GREEN-REFACTOR:

```java
@DisplayName("SecurityScannerService Specification")
class SecurityScannerServiceSpec {

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
                f.getCategory().equals("XSS")));
        }
    }
}
```

### 5.3 Integration Testing

Test component interactions:

```java
class WorkflowIntegrationTest {

    @ExtendWith(AemContextExtension.class)
    class AssetUploadWorkflowTest {

        @Mock
        private AntivirusScanService scanService;

        @Test
        void shouldInitiateWorkflowOnAssetUpload() {
            // Given
            String assetPath = "/content/dam/test/image.png";
            Asset asset = mock(Asset.class);
            when(asset.getPath()).thenReturn(assetPath);

            // When
            workflowLauncher.processAssetUpload(asset);

            // Then
            verify(scanService).scan(any(InputStream.class), eq("image.png"));
        }
    }
}
```

### 5.4 Security Testing

Validate security controls:

**XSS Testing:**
```java
@Test
void shouldSanitizeXSSInMetadata() {
    String maliciousInput = "<script>alert('xss')</script>";
    
    List<SecurityFinding> findings = scanner.scanMetadata(
        Map.of("title", maliciousInput)
    );
    
    assertTrue(findings.stream().anyMatch(f -> 
        f.getCategory().equals("XSS")));
}
```

**SQL Injection Testing:**
```java
@Test
void shouldDetectSqlInjectionPatterns() {
    String sqlInjection = "' OR 1=1 --";
    
    List<SecurityFinding> findings = scanner.scanMetadata(
        Map.of("filter", sqlInjection)
    );
    
    assertTrue(findings.stream().anyMatch(f -> 
        f.getCategory().equals("SQL_INJECTION")));
}
```

### 5.5 Performance Testing

Measure system performance:

```java
class PerformanceTest {

    @Test
    void shouldCompleteScanWithinSLA() {
        // Test file: 10MB
        InputStream testFile = createTestFile(10 * 1024 * 1024);
        
        long startTime = System.currentTimeMillis();
        ScanResult result = service.scan(testFile, "test.pdf");
        long duration = System.currentTimeMillis() - startTime;
        
        // NFR: Scan completes within 60 seconds
        assertTrue(duration < 60000, 
            "Scan took " + duration + "ms, exceeds 60s SLA");
    }
}
```

---

## Test Coverage Requirements

### Coverage Targets

| Metric | Target | Minimum |
|--------|--------|---------|
| Line Coverage | 80% | 70% |
| Branch Coverage | 75% | 60% |
| Method Coverage | 90% | 80% |

### Coverage Enforcement

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>CLASS</element>
                <limits>
                    <limit>
                        <metric>LINE</metric>
                        <value>80</value>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

---

## Test Automation

### CI/CD Integration

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Unit Tests
        run: mvn test
        
      - name: Run Integration Tests
        run: mvn verify -Pintegration-tests
        
      - name: Check Coverage
        run: mvn jacoco:check
        
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
```

---

## Deliverables

### Test Artifacts

| Artifact | Description |
|----------|-------------|
| Unit Tests | JUnit 5 tests for all services |
| Specification Tests | TDD-style behavior tests |
| Integration Tests | AEM Mock tests |
| Security Tests | XSS, SQLi validation tests |
| Coverage Report | JaCoCo HTML report |

### Test Results

- All unit tests passing
- 80%+ code coverage
- Integration tests passing
- Security tests validating controls
- Performance within SLAs

---

## AI Augmentation

### AI-Generated Tests

Use AI to generate comprehensive tests:

**Prompt:**
```
Generate JUnit 5 tests for AntivirusScanServiceImpl:
- Test clean file scanning
- Test infected file detection  
- Test connection failure handling
- Test timeout handling
- Test null input handling

Use Mockito for mocking dependencies.
Follow Arrange-Act-Assert pattern.
```

### AI Test Review

**Prompt:**
```
Review these tests and identify:
1. Missing edge cases
2. Potential flakiness
3. Missing assertions
4. Test data improvements
```

---

## Phase Transition

### Exit Criteria

Before moving to Phase 06 (Operations), ensure:

- [ ] Unit tests > 80% coverage
- [ ] All tests passing
- [ ] Integration tests passing
- [ ] Security tests validating controls
- [ ] Performance within SLAs
- [ ] No critical defects open

### Artifacts to Pass to Phase 06

- Test reports
- Coverage reports
- Defect status
- Test automation configuration

---

## Common Pitfalls

### Avoiding

1. **Testing Implementation, Not Behavior**
   - Focus on what, not how
   - Test through public interfaces

2. **Missing Edge Cases**
   - Test null inputs
   - Test error conditions
   - Test boundary values

3. **Flaky Tests**
   - Avoid timing dependencies
   - Use proper test isolation
   - Clean up test data

---

## Next Phase

[Phase 06: Operations](phase-06-operations.md)

In Phase 06, we'll deploy to production and establish monitoring.
