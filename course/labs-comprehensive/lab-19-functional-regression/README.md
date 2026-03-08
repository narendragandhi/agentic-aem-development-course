# Lab 19: Functional & Regression Testing
# Comprehensive Lab - 4 hours

## Objective

Master functional testing and regression testing for AEM workflows. Learn to create comprehensive test suites that verify functionality and catch regressions before production.

---

## Prerequisites

- Lab 7 (Testing) completed
- Lab 14 (Integration Testing) completed
- AEM local instance or SDK

---

## Overview

### Testing Types Comparison

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     TESTING PYRAMID FOR AEM                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                            ▲                                                │
│                           ╱ ╲           E2E Tests (Playwright/Cypress)     │
│                          ╱   ╲          • Full workflow tests              │
│                         ╱─────╲         • Browser automation              │
│                        ╱       ╲        • User journey tests              │
│                       ╱─────────╲                                           │
│                      ╱           ╲       Functional Tests                 │
│                     ╱─────────────╲      • Service integration           │
│                    ╱               ╲      • API tests                     │
│                   ╱─────────────────╲     • Workflow tests                │
│                  ╱                   ╲                                       │
│                 ╱─────────────────────╲   Unit Tests                      │
│                ╱                       ╲  • Service logic                  │
│               ╱─────────────────────────╲ • Mock-based                   │
│              ╱                             ╲• Fast execution              │
│             ╱───────────────────────────────╲                            │
│            ╱ Regression Tests Layer (Continuous)╲                        │
│           ╱  • Baseline comparisons               ╲                       │
│          ╱  • Visual regression                    ╲                      │
│         ╱  • Performance baselines                  ╲                     │
│        ╱  • Security baselines                      ╲                     │
│       ╱────────────────────────────────────────────╲                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Part 1: Functional Testing (45 min)

### 1.1 Service Integration Tests

Create `core/src/test/java/com/demo/workflow/services/WorkflowFunctionalTest.java`:

```java
package com.demo.workflow.services;

import io.wcm.testing.mock.aem.AemContext;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowFunctionalTest {

    private AemContext context;
    private AntivirusScanService scanService;
    private SecurityScannerService securityService;
    private WorkflowService workflowService;

    @BeforeEach
    void setUp() throws Exception {
        context = new AemContextBuilder()
            .plugin(new WorkflowPlugin())
            .plugin(new DamPlugin())
            .build();
        
        scanService = context.registerInjectActivateService(
            new AntivirusScanServiceImpl());
        securityService = context.registerInjectActivateService(
            new SecurityScannerServiceImpl());
        workflowService = context.registerInjectActivateService(
            new WorkflowServiceImpl());
    }

    @Test
    void shouldScanCleanAsset() throws Exception {
        // Given: Clean asset in DAM
        Resource asset = context.create().resource("/content/dam/clean.pdf",
            "jcr:content/metadata",
            Map.of("dc:format", "application/pdf"));
        
        // When: Scan the asset
        ScanResult result = scanService.scanAsset(asset);
        
        // Then: Asset is clean
        assertEquals(ScanResult.Status.CLEAN, result.getStatus());
        assertNull(result.getThreatName());
    }

    @Test
    void shouldDetectMalware() throws Exception {
        // Given: Asset with malware signature
        Resource asset = context.create().resource("/content/dam/infected.pdf");
        // Upload infected file content
        
        // When: Scan the asset
        ScanResult result = scanService.scanAsset(asset);
        
        // Then: Asset is flagged
        assertEquals(ScanResult.Status.INFECTED, result.getStatus());
        assertEquals("Eicar-Test-Signature", result.getThreatName());
    }

    @Test
    void shouldDetectXSSInMetadata() throws Exception {
        // Given: Asset with malicious metadata
        Resource asset = context.create().resource("/content/dam/test.jpg",
            "jcr:content/metadata",
            Map.of("dc:title", "<script>alert('xss')</script>"));
        
        // When: Security scan runs
        List<SecurityFinding> findings = securityService.scanMetadata(
            asset.getChild("jcr:content/metadata").getValueMap());
        
        // Then: XSS detected
        assertFalse(findings.isEmpty());
        assertTrue(findings.stream().anyMatch(f -> 
            f.getCategory().equals("XSS")));
    }

    @Test
    void shouldInitiateWorkflow() throws Exception {
        // Given: Asset ready for workflow
        String assetPath = "/content/dam/test.pdf";
        
        // When: Workflow is initiated
        String workflowId = workflowService.initiateApprovalWorkflow(assetPath);
        
        // Then: Workflow created
        assertNotNull(workflowId);
        verifyWorkflowStarted(workflowId);
    }

    @Test
    void shouldRouteToQuarantine() throws Exception {
        // Given: Infected asset
        String assetPath = "/content/dam/infected.pdf";
        
        // When: Security scan completes with threat
        workflowService.processScanResult(assetPath, 
            new ScanResult(Status.INFECTED, "Malware"));
        
        // Then: Asset moved to quarantine
        assertAssetInQuarantine(assetPath);
        assertAuditLogEntry("ASSET_QUARANTINED", assetPath);
    }
}
```

### 1.2 Workflow Process Tests

Create `core/src/test/java/com/demo/workflow/process/WorkflowProcessFunctionalTest.java`:

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

class AntivirusScanProcessFunctionalTest {

    @Test
    void shouldExecuteFullScanWorkflow() throws Exception {
        // Given: Workflow process and mock dependencies
        WorkflowProcess process = new AntivirusScanProcess();
        
        WorkItem workItem = mock(WorkItem.class);
        WorkflowData workflowData = mock(WorkflowData.class);
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/dam/test.pdf");
        
        Map<String, Object> metadata = new HashMap<>();
        when(workflowData.getMetaDataMap()).thenReturn(metadata);
        
        // When: Process executes
        process.execute(workItem, null, metadata);
        
        // Then: Scan completed and metadata updated
        assertEquals("COMPLETED", metadata.get("scanStatus"));
        assertNotNull(metadata.get("scanResult"));
    }

    @Test
    void shouldHandleScanFailure() throws Exception {
        // Given: Antivirus service unavailable
        WorkflowProcess process = new AntivirusScanProcess();
        
        WorkItem workItem = mock(WorkItem.class);
        WorkflowData workflowData = mock(WorkflowData.class);
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/dam/test.pdf");
        
        Map<String, Object> metadata = new HashMap<>();
        when(workflowData.getMetaDataMap()).thenReturn(metadata);
        
        // Configure service to throw exception
        when(antivirusService.scan(any()))
            .thenThrow(new ServiceUnavailableException());
        
        // When: Process executes
        process.execute(workItem, null, metadata);
        
        // Then: Error handled gracefully
        assertEquals("ERROR", metadata.get("scanStatus"));
        assertNotNull(metadata.get("errorMessage"));
    }
}
```

---

## Part 2: Regression Testing (45 min)

### 2.1 Baseline Test Framework

Create `core/src/test/java/com/demo/workflow/regression/RegressionTestBase.java`:

```java
package com.demo.workflow.regression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInfo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class RegressionTestBase {

    protected static Path BASELINE_DIR = 
        Path.of("src/test/baselines");
    protected static Path RESULTS_DIR = 
        Path.of("target/regression-results");
    
    protected Map<String, Object> testResults = new HashMap<>();
    protected String currentTestName;

    @BeforeAll
    static void setup() throws Exception {
        Files.createDirectories(BASELINE_DIR);
        Files.createDirectories(RESULTS_DIR);
    }

    @BeforeEach
    void setUpRegression(TestInfo testInfo) {
        currentTestName = testInfo.getTestMethod()
            .map(m -> m.getName())
            .orElse("unknown");
    }

    protected void recordResult(String metric, Object value) {
        testResults.put(metric, value);
    }

    protected void compareWithBaseline(String testId, 
            Map<String, Object> currentResults) throws Exception {
        
        Path baselineFile = BASELINE_DIR.resolve(testId + ".json");
        
        // Load or create baseline
        Map<String, Object> baseline;
        if (Files.exists(baselineFile)) {
            baseline = loadJson(baselineFile);
        } else {
            baseline = new HashMap<>();
            saveJson(baselineFile, baseline);
        }
        
        // Compare results
        List<String> differences = new ArrayList<>();
        for (String key : currentResults.keySet()) {
            Object current = currentResults.get(key);
            Object expected = baseline.get(key);
            
            if (!Objects.equals(current, expected)) {
                differences.add(String.format(
                    "%s: expected=%s, actual=%s", 
                    key, expected, current));
            }
        }
        
        // Record for reporting
        recordResult(testId + "_differences", differences);
        recordResult(testId + "_passed", differences.isEmpty());
        
        // Assert no regressions
        assertTrue(differences.isEmpty(), 
            "Regressions detected: " + differences);
    }

    @AfterAll
    void tearDownRegression() throws Exception {
        // Save results
        Path resultFile = RESULTS_DIR.resolve(
            "regression-" + LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json");
        saveJson(resultFile, testResults);
    }
}
```

### 2.2 Performance Regression Tests

Create `core/src/test/java/com/demo/workflow/regression/PerformanceRegressionTest.java`:

```java
package com.demo.workflow.regression;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("regression")
class PerformanceRegressionTest extends RegressionTestBase {

    private static final Map<String, Long> BASELINE_TIMES = Map.of(
        "scanSmallFile", 500L,      // 500ms max
        "scanMediumFile", 5000L,    // 5s max
        "scanLargeFile", 30000L,   // 30s max
        "metadataScan", 100L,       // 100ms max
        "workflowInit", 2000L      // 2s max
    );

    @Test
    @DisplayName("Scan performance regression - small file")
    void scanSmallFilePerformance() {
        long start = System.currentTimeMillis();
        
        // Execute operation
        scanService.scan(createTestFile(1)); // 1MB
        
        long duration = System.currentTimeMillis() - start;
        recordResult("scanSmallFile", duration);
        
        assertTrue(duration < BASELINE_TIMES.get("scanSmallFile"),
            "Performance regression: " + duration + "ms > " + 
            BASELINE_TIMES.get("scanSmallFile") + "ms");
    }

    @Test
    @DisplayName("Scan performance regression - large file")
    void scanLargeFilePerformance() {
        long start = System.currentTimeMillis();
        
        scanService.scan(createTestFile(50)); // 50MB
        
        long duration = System.currentTimeMillis() - start;
        recordResult("scanLargeFile", duration);
        
        assertTrue(duration < BASELINE_TIMES.get("scanLargeFile"),
            "Performance regression: " + duration + "ms > " + 
            BASELINE_TIMES.get("scanLargeFile") + "ms");
    }

    @Test
    @DisplayName("Memory usage regression")
    void memoryUsageRegression() {
        System.gc();
        long before = getUsedMemory();
        
        // Run operations
        for (int i = 0; i < 100; i++) {
            scanService.scan(createTestFile(1));
        }
        
        long after = getUsedMemory();
        long delta = after - before;
        
        recordResult("memoryDelta", delta);
        
        // Max 100MB growth for 100 scans
        assertTrue(delta < 100 * 1024 * 1024,
            "Memory regression: " + (delta/1024/1024) + "MB");
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
```

### 2.3 API Regression Tests

Create `core/src/test/java/com/demo/workflow/regression/ApiRegressionTest.java`:

```java
package com.demo.workflow.regression;

import org.junit.jupiter.api.*;
import java.net.http.*;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@Tag("regression")
@Tag("api")
class ApiRegressionTest extends RegressionTestBase {

    private static final String API_BASE = 
        System.getProperty("api.base", "http://localhost:4502");
    private static HttpClient client = HttpClient.newHttpClient();

    @Test
    @DisplayName("API response time regression")
    void apiResponseTimeRegression() throws Exception {
        long start = System.currentTimeMillis();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE + "/api/workflow/models.json"))
            .GET()
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        long duration = System.currentTimeMillis() - start;
        recordResult("apiResponseTime", duration);
        
        assertEquals(200, response.statusCode());
        assertTrue(duration < 2000, 
            "API regression: " + duration + "ms > 2000ms");
    }

    @Test
    @DisplayName("API contract regression")
    void apiContractRegression() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE + "/api/workflow/status.json"))
            .GET()
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        
        Map<String, Object> current = parseJson(response.body());
        
        // Compare with baseline
        compareWithBaseline("api_contract", current);
    }

    @Test
    @DisplayName("API error handling regression")
    void apiErrorHandlingRegression() throws Exception {
        // Test 404
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE + "/api/nonexistent"))
            .GET()
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        
        recordResult("error404", response.statusCode());
        
        assertEquals(404, response.statusCode());
        
        // Verify error response format
        Map<String, Object> error = parseJson(response.body());
        assertTrue(error.containsKey("error"));
        assertTrue(error.containsKey("message"));
    }
}
```

---

## Part 3: Test Automation & CI/CD (30 min)

### 3.1 Regression Test Runner

Create `src/test/scripts/run-regression-tests.sh`:

```bash
#!/bin/bash
# Regression Test Runner

set -e

echo "=== Running Regression Tests ==="

# Run performance regression tests
echo "Running performance regression tests..."
mvn test -Dtest=PerformanceRegressionTest \
  -DfailIfNoTests=false \
  -Dregression=true

# Run API regression tests
echo "Running API regression tests..."
mvn test -Dtest=ApiRegressionTest \
  -DfailIfNoTests=false \
  -Dregression=true

# Run visual regression tests
echo "Running visual regression tests..."
npx playwright test --project=chromium \
  --grep @regression

# Generate report
echo "Generating regression report..."
mvn surefire-report:report

echo "=== Regression Tests Complete ==="
```

### 3.2 GitHub Actions Workflow

Create `.github/workflows/regression-tests.yml`:

```yaml
name: Regression Tests

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM
  pull_request:
    branches: [main]

jobs:
  regression-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          
      - name: Run regression tests
        run: |
          ./src/test/scripts/run-regression-tests.sh
          
      - name: Upload results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: regression-results
          path: |
            target/regression-results/
            target/surefire-reports/
            
      - name: Check for regressions
        run: |
          # Fail if any regression tests failed
          if grep -r "regression.*failed" target/regression-results/; then
            echo "REGRESSIONS DETECTED!"
            exit 1
          fi
```

---

## Part 4: Baseline Management (30 min)

### 4.1 Update Baselines

```bash
# Update performance baselines
mvn test -Dtest=PerformanceRegressionTest \
  -DupdateBaselines=true

# Update API baselines
mvn test -Dtest=ApiRegressionTest \
  -DupdateBaselines=true

# Update visual baselines
npx playwright test --update-snapshots
```

### 4.2 Baseline Review Process

```yaml
# Baseline Review Checklist
baseline_review:
  - name: "Performance baselines"
    frequency: "Weekly"
    owner: "Tech Lead"
    criteria:
      - "No unexplained increases"
      - "Outliers investigated"
      
  - name: "API contracts"
    frequency: "Per Release"
    owner: "API Owner"
    criteria:
      - "Backward compatible"
      - "Documentation updated"
      
  - name: "Visual baselines"
    frequency: "Per Sprint"
    owner: "UX Lead"
    criteria:
      - "Intentional changes only"
      - "Approved by designer"
```

---

## Part 5: Dashboard & Monitoring (15 min)

### 5.1 Regression Dashboard

Create `docs/regression-dashboard.md`:

```markdown
# Regression Test Dashboard

## Latest Results

| Category | Status | Trend |
|----------|--------|-------|
| Performance | ✓ Pass | ↓ 2% |
| API | ✓ Pass | → Stable |
| Visual | ⚠ Warning | ↑ 5% |
| Security | ✓ Pass | → Stable |

## Trending Issues

1. **Memory usage increased** - 10% higher than baseline
2. **API response time** - Slight degradation in /workflow/models

## Action Items

- [ ] Investigate memory increase
- [ ] Optimize /workflow/models endpoint
- [ ] Review visual changes with designer
```

---

## Verification Checklist

- [ ] Functional tests cover all workflows
- [ ] Regression tests run in CI/CD
- [ ] Baselines are stored in Git
- [ ] Dashboard shows trending data
- [ ] Alerts configured for regressions

---

## Key Takeaways

1. **Functional tests verify behavior** - Does it work correctly?
2. **Regression tests catch changes** - Did we break something?
3. **Baselines enable comparison** - Track over time
4. **Automation is essential** - Run on every change

---

## Next Steps

1. Add regression tests to all critical paths
2. Set up automated baseline updates
3. Configure regression alerts
4. Create regression dashboard
5. Integrate with AEM Cloud Manager

---

## References

- [AEM Testing Best Practices](https://experienceleague.adobe.com/docs/experience-manager-65/developing/testing/bestpractices.html)
- [Playwright](https://playwright.dev/)
- [JaCoCo Coverage](https://www.jacoco.org/jacoco/)
