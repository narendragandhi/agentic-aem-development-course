# Lab 6: Testing (3 hours)

## Objective
Master testing practices for AEM development including unit tests with AEM Mock, integration tests, and test patterns for workflow processes.

---

## Part 1: Unit Testing Foundations (45 min)

### 1.1 AEM Mock Framework

AEM Mock provides a simulated AEM runtime for unit testing:

```xml
<!-- pom.xml dependency -->
<dependency>
    <groupId>io.wcm</groupId>
    <artifactId>io.wcm.testing.aem-mock.junit5</artifactId>
    <version>5.3.0</version>
    <scope>test</scope>
</dependency>
```

### 1.2 Basic Test Structure

```java
package com.demo.workflow.services.impl;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
@DisplayName("SecurityScannerService Unit Tests")
class SecurityScannerServiceImplTest {

    private final AemContext context = new AemContext();
    private SecurityScannerService service;

    @BeforeEach
    void setUp() {
        // Register the service in the mock context
        service = context.registerInjectActivateService(
            new SecurityScannerServiceImpl()
        );
    }

    @Test
    @DisplayName("should scan metadata for XSS patterns")
    void shouldScanMetadataForXss() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("dc:title", "<script>alert('xss')</script>");

        List<SecurityFinding> findings = service.scanMetadata(metadata);

        assertFalse(findings.isEmpty());
        assertEquals("XSS", findings.get(0).getCategory());
    }
}
```

### 1.3 Testing with Sling Models

```java
@ExtendWith(AemContextExtension.class)
class AssetSlingModelTest {

    private final AemContext context = new AemContext();

    @BeforeEach
    void setUp() {
        // Load test content
        context.load().json("/test-content/asset.json", "/content/dam/test");
    }

    @Test
    void shouldAdaptResourceToAssetModel() {
        Resource resource = context.resourceResolver()
            .getResource("/content/dam/test/image.png");

        AssetModel model = resource.adaptTo(AssetModel.class);

        assertNotNull(model);
        assertEquals("image.png", model.getName());
    }
}
```

---

## Part 2: Testing Workflow Processes (45 min)

### 2.1 WorkflowProcess Test Setup

```java
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
@DisplayName("SecurityScanProcess Tests")
class SecurityScanProcessTest {

    private final AemContext context = new AemContext();

    @Mock
    private WorkItem workItem;

    @Mock
    private WorkflowSession workflowSession;

    @Mock
    private MetaDataMap metaDataMap;

    @Mock
    private WorkflowData workflowData;

    private SecurityScanProcess process;
    private SecurityScannerService scannerService;

    @BeforeEach
    void setUp() {
        // Register the real scanner service
        scannerService = context.registerInjectActivateService(
            new SecurityScannerServiceImpl()
        );

        // Create process with injected scanner
        process = new SecurityScanProcess();
        context.registerInjectActivateService(process);

        // Configure mock workflow data
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayloadType()).thenReturn("JCR_PATH");
    }

    @Test
    @DisplayName("should quarantine asset with critical threat")
    void shouldQuarantineCriticalThreat() throws Exception {
        // Setup test asset with malicious metadata
        context.create().asset("/content/dam/malicious.pdf",
            "/test-files/malicious.pdf", "application/pdf");

        when(workflowData.getPayload()).thenReturn("/content/dam/malicious.pdf");

        process.execute(workItem, workflowSession, metaDataMap);

        // Verify quarantine action
        verify(workflowSession).terminateWorkflow(any());
    }

    @Test
    @DisplayName("should approve clean asset")
    void shouldApproveCleanAsset() throws Exception {
        context.create().asset("/content/dam/clean.png",
            "/test-files/clean.png", "image/png");

        when(workflowData.getPayload()).thenReturn("/content/dam/clean.png");

        process.execute(workItem, workflowSession, metaDataMap);

        // Workflow should continue (no terminate)
        verify(workflowSession, never()).terminateWorkflow(any());
    }
}
```

### 2.2 Testing Workflow Step Arguments

```java
@Test
@DisplayName("should read process arguments")
void shouldReadProcessArguments() throws Exception {
    // Configure process arguments
    when(metaDataMap.get("PROCESS_ARGS", String.class))
        .thenReturn("scanDepth=deep,timeout=30");

    context.create().asset("/content/dam/test.pdf",
        "/test-files/test.pdf", "application/pdf");
    when(workflowData.getPayload()).thenReturn("/content/dam/test.pdf");

    process.execute(workItem, workflowSession, metaDataMap);

    // Verify deep scan was performed
    // (implementation-specific assertions)
}
```

---

## Part 3: Testing OSGi Services (45 min)

### 3.1 Testing Service Registration

```java
@ExtendWith(AemContextExtension.class)
class AgentOrchestratorTest {

    private final AemContext context = new AemContext();
    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = context.registerInjectActivateService(
            new AgentOrchestrator()
        );
    }

    @Test
    @DisplayName("should register built-in agents on activation")
    void shouldRegisterBuiltInAgents() {
        List<Agent> agents = orchestrator.listAgents();

        assertFalse(agents.isEmpty());
        assertTrue(agents.stream()
            .anyMatch(a -> a.getId().equals("aem-spec-writer")));
        assertTrue(agents.stream()
            .anyMatch(a -> a.getId().equals("test-runner")));
    }

    @Test
    @DisplayName("should execute workflow asynchronously")
    void shouldExecuteWorkflowAsync() throws Exception {
        WorkflowDefinition workflow = WorkflowDefinition.builder("Test")
            .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
            .addStep(WorkflowStep.of(2, "test-runner", "Run tests"))
            .build();

        WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

        // Wait for completion
        int attempts = 0;
        while (!execution.isComplete() && attempts < 50) {
            Thread.sleep(100);
            attempts++;
        }

        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals(2, execution.getStepResults().size());
    }
}
```

### 3.2 Testing Service References

```java
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class IntegratedScannerTest {

    private final AemContext context = new AemContext();

    @Mock
    private DocumentSecurityScanner documentScanner;

    @Mock
    private OwaspSecurityPatterns owaspPatterns;

    @BeforeEach
    void setUp() {
        // Register mock dependencies first
        context.registerService(DocumentSecurityScanner.class, documentScanner);
        context.registerService(OwaspSecurityPatterns.class, owaspPatterns);

        // Then register the service that depends on them
        context.registerInjectActivateService(
            new SecurityScannerServiceImpl()
        );
    }

    @Test
    void shouldInvokeDocumentScannerForPdf() {
        when(documentScanner.isScannableDocument("application/pdf"))
            .thenReturn(true);
        when(documentScanner.scanPdf(any()))
            .thenReturn(Collections.emptyList());

        // Test that PDF scanning is delegated
        // ...
    }
}
```

---

## Part 4: Integration Test Patterns (45 min)

### 4.1 Full Integration Test

```java
@ExtendWith(AemContextExtension.class)
@DisplayName("Security Workflow Integration Tests")
class SecurityWorkflowIntegrationTest {

    private final AemContext context = new AemContext();

    private SecurityScannerService scannerService;
    private DocumentSecurityScanner documentScanner;
    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        // Register all real services
        documentScanner = context.registerInjectActivateService(
            new DocumentSecurityScanner()
        );

        scannerService = context.registerInjectActivateService(
            new SecurityScannerServiceImpl()
        );

        orchestrator = context.registerInjectActivateService(
            new AgentOrchestrator()
        );

        // Load test content
        context.load().json("/test-content/dam-structure.json", "/content/dam");
    }

    @Test
    @DisplayName("should process complete security workflow")
    void shouldProcessCompleteWorkflow() throws Exception {
        // Create test asset
        context.create().asset(
            "/content/dam/test/document.pdf",
            getClass().getResourceAsStream("/test-files/clean.pdf"),
            "application/pdf"
        );

        // Get asset
        Resource assetResource = context.resourceResolver()
            .getResource("/content/dam/test/document.pdf");
        Asset asset = assetResource.adaptTo(Asset.class);

        // Run security scan
        SecurityScanResult result = scannerService.scanAsset(asset);

        // Verify results
        assertTrue(result.isClean());
        assertEquals(ThreatLevel.NONE, result.getThreatLevel());
    }

    @Test
    @DisplayName("should detect malicious PDF and quarantine")
    void shouldDetectMaliciousPdf() throws Exception {
        // Create asset with JavaScript
        String maliciousPdf = "%PDF-1.4\n/JavaScript (alert('xss'))\n%%EOF";
        context.create().asset(
            "/content/dam/test/malicious.pdf",
            new ByteArrayInputStream(maliciousPdf.getBytes()),
            "application/pdf"
        );

        Resource assetResource = context.resourceResolver()
            .getResource("/content/dam/test/malicious.pdf");
        Asset asset = assetResource.adaptTo(Asset.class);

        SecurityScanResult result = scannerService.scanAsset(asset);

        assertFalse(result.isClean());
        assertTrue(result.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("PDF_JAVASCRIPT")));
    }
}
```

### 4.2 Test Data Management

```java
// src/test/resources/test-content/dam-structure.json
{
    "jcr:primaryType": "sling:OrderedFolder",
    "test": {
        "jcr:primaryType": "sling:OrderedFolder"
    }
}
```

### 4.3 Helper Methods for Tests

```java
public class TestHelpers {

    public static byte[] createMockOOXML(String entryName, String content)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    public static byte[] getPngMagicBytes() {
        return new byte[]{
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
    }

    public static byte[] getExeMagicBytes() {
        return new byte[]{0x4D, 0x5A, 0x00, 0x00};
    }
}
```

---

## Part 5: Test Organization (30 min)

### 5.1 Test Naming Conventions

| Pattern | Usage |
|---------|-------|
| `*Spec.java` | Specification tests (TDD RED phase) |
| `*Test.java` | Unit tests |
| `*IT.java` | Integration tests |
| `*PerformanceTest.java` | Performance tests |

### 5.2 Test Categories

```java
@Tag("unit")
class SecurityScannerServiceTest { ... }

@Tag("integration")
class WorkflowIntegrationTest { ... }

@Tag("slow")
class PerformanceTest { ... }
```

### 5.3 Maven Surefire Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Spec.java</include>
        </includes>
        <excludes>
            <exclude>**/*IT.java</exclude>
        </excludes>
    </configuration>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.2.5</version>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <includes>
            <include>**/*IT.java</include>
        </includes>
    </configuration>
</plugin>
```

---

## Verification Checklist

- [ ] AEM Mock dependency configured
- [ ] Unit tests for services working
- [ ] Workflow process tests with mocks
- [ ] OSGi service registration tests
- [ ] Integration tests for full workflow
- [ ] Test helpers created
- [ ] 50+ tests passing

---

## Run Tests

```bash
# Unit tests only
mvn test -pl core

# Integration tests
mvn verify -pl core

# All tests with coverage
mvn verify -pl core -Pcoverage
```

---

## Next Lab
[Lab 7: Quality & Deployment](../lab-07-quality-deployment/README.md)
