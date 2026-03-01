# Lab 7: AI-Generated Testing
# Comprehensive Test Coverage with AI Assistance

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 60 minutes |
| Difficulty | Intermediate |
| Prerequisites | Labs 1-6 completed |
| Outcome | Complete test suite for workflow |

## Learning Objectives

By the end of this lab, you will:
- [ ] Generate unit tests using AI
- [ ] Create integration tests for workflow
- [ ] Write end-to-end workflow tests
- [ ] Achieve >80% code coverage

---

## Exercise 7.1: Generate Unit Tests (20 min)

### Step 1: Prepare Test Context

Provide AI with the implementation context:

```
I need unit tests for AntivirusScanServiceImpl.

Here's the class structure:
- OSGi service with @Component annotation
- Three scan engines: ClamAV (TCP), REST API, Mock
- Configuration via @Designate with Config interface
- Methods: scanFile(), scanAsset(), isAvailable(), getScanEngineName()

Key behaviors to test:
1. Mock mode: files starting with "virus_" are infected
2. ClamAV mode: uses INSTREAM protocol
3. File size validation against maxFileSize config
4. Graceful handling of connection failures

Generate JUnit 5 tests with Mockito.
Include:
- @ExtendWith(MockitoExtension.class)
- @Mock annotations for dependencies
- @BeforeEach setup
- Descriptive test method names
```

### Step 2: Review Generated Tests

```java
@ExtendWith(MockitoExtension.class)
class AntivirusScanServiceImplTest {

    @Mock
    private ResourceResolverFactory resolverFactory;

    private AntivirusScanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AntivirusScanServiceImpl();
        // Inject mock config
    }

    @Test
    @DisplayName("scanFile returns CLEAN for normal file in mock mode")
    void scanFile_normalFile_mockMode_returnsClean() {
        // Arrange
        configureService("MOCK", true);
        InputStream content = new ByteArrayInputStream("Hello".getBytes());

        // Act
        ScanResult result = service.scanFile(content, "document.pdf", 1024);

        // Assert
        assertTrue(result.isClean());
        assertEquals("MOCK", result.getScanEngine());
        assertNull(result.getThreatName());
    }

    @Test
    @DisplayName("scanFile returns INFECTED for virus_ file in mock mode")
    void scanFile_virusFile_mockMode_returnsInfected() {
        // Arrange
        configureService("MOCK", true);
        InputStream content = new ByteArrayInputStream("malware".getBytes());

        // Act
        ScanResult result = service.scanFile(content, "virus_test.exe", 1024);

        // Assert
        assertFalse(result.isClean());
        assertEquals("Mock.TestVirus", result.getThreatName());
    }

    @Test
    @DisplayName("scanFile returns ERROR when file exceeds maxFileSize")
    void scanFile_oversizedFile_returnsError() {
        // Arrange
        configureService("MOCK", true, 100); // 100 byte limit
        InputStream content = new ByteArrayInputStream(new byte[200]);

        // Act
        ScanResult result = service.scanFile(content, "large.pdf", 200);

        // Assert
        assertFalse(result.isClean());
        assertTrue(result.getDetails().contains("exceeds"));
    }

    // Add more tests...
}
```

### Step 3: Add Edge Case Tests

```
Generate additional tests for edge cases:

1. Null/empty filename handling
2. Zero-byte file scanning
3. Service disabled via config
4. ClamAV connection timeout
5. Malformed ClamAV response
6. Concurrent scan requests

For each test, include:
- Clear @DisplayName
- Proper assertions
- Verification of logging behavior
```

### Step 4: Run Tests and Check Coverage

```bash
# Run tests with coverage
mvn test -pl core -Djacoco.skip=false

# Generate coverage report
mvn jacoco:report -pl core

# View coverage report
open core/target/site/jacoco/index.html

# Target: >80% line coverage for AntivirusScanServiceImpl
```

#### Checkpoint 7.1
- [ ] 10+ unit tests created
- [ ] All tests passing
- [ ] Coverage >80%
- [ ] Edge cases covered

---

## Exercise 7.2: Workflow Process Tests (20 min)

### Step 1: Generate AntivirusScanProcess Tests

```
Generate tests for AntivirusScanProcess workflow step.

Use AEM Mocks (io.wcm.testing.mock.aem) for:
- AemContext
- Mock WorkItem
- Mock WorkflowSession
- Mock Asset

Test scenarios:
1. Clean asset - workflow continues
2. Infected asset - WorkflowException thrown
3. Scanner unavailable - workflow handles gracefully
4. Non-DAM resource - skipped with warning
5. Workflow metadata correctly set

Include setup for:
- Creating test assets in mock repository
- Configuring mock AntivirusScanService
- Capturing workflow metadata updates
```

### Step 2: Create Test Asset Helper

```java
@ExtendWith(AemContextExtension.class)
class AntivirusScanProcessTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private AntivirusScanService scanService;

    private AntivirusScanProcess process;

    @BeforeEach
    void setUp() {
        // Register mock service
        context.registerService(AntivirusScanService.class, scanService);

        // Create process instance
        process = context.registerInjectActivateService(new AntivirusScanProcess());

        // Create test asset
        context.create().asset("/content/dam/test/sample.pdf",
            "/sample.pdf", "application/pdf");
    }

    @Test
    void execute_cleanAsset_setsCleanMetadata() throws Exception {
        // Arrange
        when(scanService.isAvailable()).thenReturn(true);
        when(scanService.scanFile(any(), any(), anyLong()))
            .thenReturn(ScanResult.clean("MOCK", 100));

        WorkItem workItem = createWorkItem("/content/dam/test/sample.pdf");
        MetaDataMap metaData = new SimpleMetaDataMap();

        // Act
        process.execute(workItem, mockSession, metaData);

        // Assert
        MetaDataMap wfMeta = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        assertEquals("CLEAN", wfMeta.get("av.scanStatus", String.class));
    }

    @Test
    void execute_infectedAsset_throwsException() {
        // Arrange
        when(scanService.isAvailable()).thenReturn(true);
        when(scanService.scanFile(any(), any(), anyLong()))
            .thenReturn(ScanResult.infected("Trojan.Test", "MOCK", 100));

        WorkItem workItem = createWorkItem("/content/dam/test/virus.exe");

        // Act & Assert
        WorkflowException exception = assertThrows(WorkflowException.class,
            () -> process.execute(workItem, mockSession, new SimpleMetaDataMap()));

        assertTrue(exception.getMessage().contains("Malware detected"));
    }
}
```

### Step 3: Test Quarantine Process

```
Generate tests for QuarantineProcess:

1. Asset moved to correct quarantine path (/content/dam/quarantine/YYYY/MM/DD/)
2. Original path preserved in metadata
3. Threat name stored
4. Expiry date calculated correctly
5. Duplicate filename handling
6. Missing quarantine folder auto-creation
```

#### Checkpoint 7.2
- [ ] Workflow process tests created
- [ ] AEM Mocks used correctly
- [ ] All scenarios covered
- [ ] Tests passing

---

## Exercise 7.3: Integration Tests (15 min)

### Step 1: Create Integration Test

```java
/**
 * Integration test for complete workflow execution.
 * Requires ClamAV container running.
 */
@Tag("integration")
class SecureAssetWorkflowIT {

    private static final String TEST_ASSET_PATH = "/content/dam/test-upload/";

    @Test
    @DisplayName("Complete workflow executes for clean asset")
    void workflow_cleanAsset_completesSuccessfully() {
        // This test requires:
        // 1. AEM running on localhost:4502
        // 2. ClamAV running on localhost:3310
        // 3. Workflow model deployed

        // Upload clean test asset
        String assetPath = uploadAsset("clean-test.pdf", getCleanPdfContent());

        // Wait for workflow to complete (with timeout)
        boolean completed = waitForWorkflow(assetPath, 60);

        assertTrue(completed, "Workflow should complete");

        // Verify asset metadata
        Map<String, String> metadata = getAssetMetadata(assetPath);
        assertEquals("CLEAN", metadata.get("dam:avScanStatus"));
    }

    @Test
    @DisplayName("Workflow quarantines infected asset")
    void workflow_infectedAsset_quarantined() {
        // Upload EICAR test file (safe AV test pattern)
        String assetPath = uploadAsset("eicar-test.txt", getEicarContent());

        // Wait for quarantine
        boolean quarantined = waitForQuarantine(assetPath, 60);

        assertTrue(quarantined, "File should be quarantined");

        // Verify original path is empty
        assertFalse(assetExists(assetPath));

        // Verify quarantine folder has file
        String quarantinePath = findInQuarantine("eicar-test.txt");
        assertNotNull(quarantinePath);
    }

    private String getEicarContent() {
        return "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
    }
}
```

### Step 2: Run Integration Tests

```bash
# Start ClamAV if not running
docker-compose up -d clamav

# Wait for ClamAV to be ready
until echo "PING" | nc -z localhost 3310; do sleep 5; done

# Run integration tests
mvn verify -pl core -Pintegration-tests

# Or with specific test
mvn verify -pl core -Dit.test=SecureAssetWorkflowIT
```

#### Checkpoint 7.3
- [ ] Integration test class created
- [ ] ClamAV integration tested
- [ ] Workflow end-to-end tested
- [ ] Tests passing with containers

---

## Exercise 7.4: Test Coverage Report (5 min)

### Generate Final Coverage Report

```bash
# Run all tests with coverage
mvn clean verify -pl core

# Generate aggregated report
mvn jacoco:report -pl core

# View report
open core/target/site/jacoco/index.html
```

### Coverage Targets

| Class | Target | Achieved |
|-------|--------|----------|
| AntivirusScanServiceImpl | 80% | |
| AntivirusScanProcess | 85% | |
| QuarantineProcess | 80% | |
| AssetApprovalParticipantChooser | 75% | |
| **Overall** | **80%** | |

---

## Lab Deliverables

1. **Unit test classes** - All service and process tests
2. **Integration test class** - End-to-end workflow test
3. **Coverage report** - Screenshot showing >80%
4. **Test execution log** - All tests passing

---

## Lab Completion Checklist

- [ ] Unit tests for all services
- [ ] Unit tests for all workflow processes
- [ ] Integration tests created
- [ ] All tests passing
- [ ] Coverage >80%

---

## Next Lab

Proceed to [Lab 8: Deployment](../lab-08-deployment/README.md)
