# TDD for AEM Workflow Development

## Overview

This guide shows how to apply Test-Driven Agentic Development (TDAD) specifically to the Secure Asset Approval Workflow. Each workflow step is developed using the Red-Green-Refactor cycle.

---

## Workflow TDD Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WORKFLOW TDD DEVELOPMENT CYCLE                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   User Story                 Specification Test              Implementation │
│   ────────────               ──────────────────              ────────────── │
│                                                                             │
│   ┌─────────────┐           ┌─────────────────┐           ┌─────────────┐  │
│   │ "As a user  │    ──▶    │ @Test           │    ──▶    │ Workflow    │  │
│   │  I want     │   Write   │ shouldScanFile  │   AI      │ Process     │  │
│   │  uploads    │   Test    │ ForMalware()    │   Impl    │ Step Code   │  │
│   │  scanned"   │   First   │                 │           │             │  │
│   └─────────────┘           └─────────────────┘           └─────────────┘  │
│                                    │                             │         │
│                                    │         Run Tests           │         │
│                                    └──────────────────────────────┘         │
│                                          RED → GREEN                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Step-by-Step: TDD for Each Workflow Process

### 1. AntivirusScanProcess - TDD

#### User Story
> As a content administrator, I want uploaded files automatically scanned for malware so that infected files are detected before approval.

#### Specification Test (Write First)

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AntivirusScanProcess Specification")
class AntivirusScanProcessSpec {

    @Mock private WorkItem workItem;
    @Mock private WorkflowSession workflowSession;
    @Mock private WorkflowData workflowData;
    @Mock private AntivirusScanService scanService;
    @Mock private ResourceResolver resourceResolver;
    @Mock private Resource assetResource;
    @Mock private Asset asset;

    @InjectMocks
    private AntivirusScanProcess process;

    // ═══════════════════════════════════════════════════════════════════
    // SCAN BEHAVIOR SPECIFICATIONS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When processing an uploaded asset")
    class AssetProcessing {

        @Test
        @DisplayName("should scan the asset binary for malware")
        void shouldScanAssetBinary() throws Exception {
            // Given an asset at a specific path
            String assetPath = "/content/dam/uploads/document.pdf";
            InputStream mockStream = new ByteArrayInputStream("test".getBytes());

            setupMocks(assetPath, mockStream);
            when(scanService.scan(any(), anyString()))
                .thenReturn(ScanResult.clean());

            // When the process executes
            process.execute(workItem, workflowSession, new SimpleMetaDataMap());

            // Then the scan service should be called with the asset
            verify(scanService).scan(eq(mockStream), eq("document.pdf"));
        }

        @Test
        @DisplayName("should store scan result in workflow metadata")
        void shouldStoreScanResultInMetadata() throws Exception {
            String assetPath = "/content/dam/uploads/test.pdf";
            MetaDataMap metadata = new SimpleMetaDataMap();

            setupMocks(assetPath, new ByteArrayInputStream("test".getBytes()));
            when(workflowData.getMetaDataMap()).thenReturn(metadata);
            when(scanService.scan(any(), anyString()))
                .thenReturn(ScanResult.clean());

            process.execute(workItem, workflowSession, new SimpleMetaDataMap());

            // Verify metadata contains scan results
            assertEquals("CLEAN", metadata.get("av.scanStatus", String.class));
            assertNotNull(metadata.get("av.scanTime", Long.class));
        }
    }

    @Nested
    @DisplayName("When malware is detected")
    class MalwareDetection {

        @Test
        @DisplayName("should mark workflow metadata as INFECTED")
        void shouldMarkAsInfected() throws Exception {
            String assetPath = "/content/dam/uploads/virus.exe";
            MetaDataMap metadata = new SimpleMetaDataMap();

            setupMocks(assetPath, new ByteArrayInputStream("eicar".getBytes()));
            when(workflowData.getMetaDataMap()).thenReturn(metadata);
            when(scanService.scan(any(), anyString()))
                .thenReturn(ScanResult.infected("Trojan.TestVirus"));

            process.execute(workItem, workflowSession, new SimpleMetaDataMap());

            assertEquals("INFECTED", metadata.get("av.scanStatus", String.class));
            assertEquals("Trojan.TestVirus", metadata.get("av.threatName", String.class));
        }

        @Test
        @DisplayName("should route workflow to quarantine step")
        void shouldRouteToQuarantine() throws Exception {
            String assetPath = "/content/dam/uploads/malware.exe";
            MetaDataMap metadata = new SimpleMetaDataMap();

            setupMocks(assetPath, new ByteArrayInputStream("malware".getBytes()));
            when(workflowData.getMetaDataMap()).thenReturn(metadata);
            when(scanService.scan(any(), anyString()))
                .thenReturn(ScanResult.infected("Virus.Detected"));

            process.execute(workItem, workflowSession, new SimpleMetaDataMap());

            // Verify routing decision is stored
            assertEquals("quarantine", metadata.get("workflow.nextStep", String.class));
        }
    }

    @Nested
    @DisplayName("When scan service is unavailable")
    class ServiceUnavailable {

        @Test
        @DisplayName("should fail workflow with clear error message")
        void shouldFailWithClearError() throws Exception {
            setupMocks("/content/dam/test.pdf", new ByteArrayInputStream("test".getBytes()));
            when(scanService.scan(any(), anyString()))
                .thenThrow(new RuntimeException("ClamAV connection refused"));

            WorkflowException exception = assertThrows(WorkflowException.class, () ->
                process.execute(workItem, workflowSession, new SimpleMetaDataMap())
            );

            assertTrue(exception.getMessage().contains("scan"));
        }

        @Test
        @DisplayName("should set ERROR status in metadata before failing")
        void shouldSetErrorStatus() throws Exception {
            MetaDataMap metadata = new SimpleMetaDataMap();
            setupMocks("/content/dam/test.pdf", new ByteArrayInputStream("test".getBytes()));
            when(workflowData.getMetaDataMap()).thenReturn(metadata);
            when(scanService.scan(any(), anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

            try {
                process.execute(workItem, workflowSession, new SimpleMetaDataMap());
            } catch (WorkflowException e) {
                // Expected
            }

            assertEquals("ERROR", metadata.get("av.scanStatus", String.class));
        }
    }

    // Helper method
    private void setupMocks(String assetPath, InputStream stream) {
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn(assetPath);
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resourceResolver);
        when(resourceResolver.getResource(assetPath)).thenReturn(assetResource);
        when(assetResource.adaptTo(Asset.class)).thenReturn(asset);
        when(asset.getOriginal()).thenReturn(mock(Rendition.class));
        when(asset.getOriginal().getStream()).thenReturn(stream);
        when(asset.getName()).thenReturn(assetPath.substring(assetPath.lastIndexOf('/') + 1));
    }
}
```

#### BEAD Task for AI Implementation

```yaml
id: TASK-WF-SCAN-TDD
title: Implement AntivirusScanProcess using TDD
type: workflow-process
methodology: TDAD

specification_tests:
  file: AntivirusScanProcessSpec.java
  test_count: 6
  categories:
    - AssetProcessing (2 tests)
    - MalwareDetection (2 tests)
    - ServiceUnavailable (2 tests)

current_status:
  passing: 0
  failing: 6

implementation_file: AntivirusScanProcess.java

ai_instructions: |
  Implement the workflow process step by step:

  1. First make AssetProcessing tests pass
     - Get asset from payload path
     - Extract binary stream
     - Call scan service
     - Store result in metadata

  2. Then make MalwareDetection tests pass
     - Check scan result status
     - Store threat name if infected
     - Set routing decision

  3. Finally make ServiceUnavailable tests pass
     - Wrap in try-catch
     - Set ERROR status on failure
     - Throw WorkflowException with message

  Run tests after each section.
```

---

### 2. QuarantineProcess - TDD

#### User Story
> As a security administrator, I want infected files automatically moved to a quarantine folder so that they cannot be accessed or published.

#### Specification Test

```java
@DisplayName("QuarantineProcess Specification")
class QuarantineProcessSpec {

    @Nested
    @DisplayName("When quarantining an infected asset")
    class QuarantineInfected {

        @Test
        @DisplayName("should move asset to quarantine folder")
        void shouldMoveToQuarantineFolder() throws Exception {
            // Given an infected asset
            String originalPath = "/content/dam/uploads/virus.exe";
            setupInfectedAsset(originalPath, "Trojan.Malware");

            // When quarantine process runs
            process.execute(workItem, workflowSession, args);

            // Then asset should be in quarantine folder
            verify(session).move(
                eq(originalPath),
                startsWith("/content/dam/quarantine/")
            );
        }

        @Test
        @DisplayName("should preserve original path in metadata")
        void shouldPreserveOriginalPath() throws Exception {
            String originalPath = "/content/dam/uploads/infected.pdf";
            setupInfectedAsset(originalPath, "Virus.Test");

            process.execute(workItem, workflowSession, args);

            // Verify metadata contains original path
            verify(modifiableValueMap).put("dam:originalPath", originalPath);
        }

        @Test
        @DisplayName("should record threat name in metadata")
        void shouldRecordThreatName() throws Exception {
            setupInfectedAsset("/content/dam/test.exe", "Ransomware.WannaCry");

            process.execute(workItem, workflowSession, args);

            verify(modifiableValueMap).put("dam:threatName", "Ransomware.WannaCry");
        }

        @Test
        @DisplayName("should set quarantine timestamp")
        void shouldSetQuarantineTimestamp() throws Exception {
            setupInfectedAsset("/content/dam/test.exe", "Malware");

            process.execute(workItem, workflowSession, args);

            verify(modifiableValueMap).put(eq("dam:quarantineDate"), any(Calendar.class));
        }
    }

    @Nested
    @DisplayName("When quarantine folder doesn't exist")
    class QuarantineFolderCreation {

        @Test
        @DisplayName("should create quarantine folder hierarchy")
        void shouldCreateFolderHierarchy() throws Exception {
            when(resourceResolver.getResource("/content/dam/quarantine")).thenReturn(null);
            setupInfectedAsset("/content/dam/uploads/virus.exe", "Malware");

            process.execute(workItem, workflowSession, args);

            // Verify folder was created
            verify(resourceResolver).create(
                any(Resource.class),
                eq("quarantine"),
                anyMap()
            );
        }
    }

    @Nested
    @DisplayName("When handling naming conflicts")
    class NamingConflicts {

        @Test
        @DisplayName("should generate unique name if file exists")
        void shouldGenerateUniqueName() throws Exception {
            // Given quarantine already has virus.exe
            when(resourceResolver.getResource("/content/dam/quarantine/2024/01/15/virus.exe"))
                .thenReturn(mock(Resource.class));
            when(resourceResolver.getResource("/content/dam/quarantine/2024/01/15/virus_1.exe"))
                .thenReturn(null);

            setupInfectedAsset("/content/dam/uploads/virus.exe", "Malware");

            process.execute(workItem, workflowSession, args);

            // Should use _1 suffix
            verify(session).move(anyString(), endsWith("virus_1.exe"));
        }
    }
}
```

---

### 3. DynamicApproverAssignerProcess - TDD

#### User Story
> As a workflow administrator, I want approvers assigned based on asset metadata so that the right people review each asset type.

#### Specification Test

```java
@DisplayName("DynamicApproverAssigner Specification")
class DynamicApproverAssignerSpec {

    @Nested
    @DisplayName("When assigning approvers based on file type")
    class FileTypeRouting {

        @Test
        @DisplayName("should assign legal team for contract PDFs")
        void shouldAssignLegalForContracts() throws Exception {
            setupAssetWithMetadata("/content/dam/contracts/agreement.pdf", Map.of(
                "dc:type", "contract",
                "dam:MIMEType", "application/pdf"
            ));

            process.execute(workItem, workflowSession, args);

            assertEquals("legal-reviewers",
                workflowMetadata.get("workflow.assignee", String.class));
        }

        @Test
        @DisplayName("should assign marketing team for brand assets")
        void shouldAssignMarketingForBrandAssets() throws Exception {
            setupAssetWithMetadata("/content/dam/brand/logo.png", Map.of(
                "dc:type", "brand-asset",
                "dam:MIMEType", "image/png"
            ));

            process.execute(workItem, workflowSession, args);

            assertEquals("marketing-approvers",
                workflowMetadata.get("workflow.assignee", String.class));
        }

        @Test
        @DisplayName("should assign default approver when no rules match")
        void shouldAssignDefaultWhenNoMatch() throws Exception {
            setupAssetWithMetadata("/content/dam/misc/file.txt", Map.of(
                "dam:MIMEType", "text/plain"
            ));

            process.execute(workItem, workflowSession, args);

            assertEquals("content-approvers",
                workflowMetadata.get("workflow.assignee", String.class));
        }
    }

    @Nested
    @DisplayName("When assigning based on file size")
    class FileSizeRouting {

        @Test
        @DisplayName("should require senior approval for large files")
        void shouldRequireSeniorForLargeFiles() throws Exception {
            setupAssetWithSize("/content/dam/videos/promo.mp4", 500 * 1024 * 1024); // 500MB

            process.execute(workItem, workflowSession, args);

            assertEquals("senior-approvers",
                workflowMetadata.get("workflow.assignee", String.class));
            assertEquals("large-file",
                workflowMetadata.get("workflow.approvalLevel", String.class));
        }
    }

    @Nested
    @DisplayName("When handling approval chains")
    class ApprovalChains {

        @Test
        @DisplayName("should set multi-level approval for sensitive content")
        void shouldSetMultiLevelForSensitive() throws Exception {
            setupAssetWithMetadata("/content/dam/confidential/report.pdf", Map.of(
                "dam:confidentiality", "restricted"
            ));

            process.execute(workItem, workflowSession, args);

            List<String> approvers = workflowMetadata.get("workflow.approvalChain", List.class);
            assertEquals(3, approvers.size());
            assertTrue(approvers.contains("manager"));
            assertTrue(approvers.contains("legal"));
            assertTrue(approvers.contains("executive"));
        }
    }
}
```

---

## BEAD Task Template for Workflow TDD

```yaml
id: TASK-WF-{PROCESS_NAME}-TDD
title: Implement {ProcessName} using TDD
type: workflow-process
methodology: TDAD
bmad_phase: 04-development

user_story: |
  As a {role},
  I want {feature}
  So that {benefit}

specification:
  test_file: {ProcessName}Spec.java
  test_sections:
    - name: "{Scenario1}"
      tests: 3
    - name: "{Scenario2}"
      tests: 2
    - name: "{Scenario3}"
      tests: 2
  total_tests: 7

test_status:
  passing: 0
  failing: 7

implementation:
  file: {ProcessName}.java
  osgi_annotations:
    - "@Component(service = WorkflowProcess.class)"
    - "@Designate(ocd = Config.class)"

tdd_workflow:
  red_phase:
    - Read all specification tests
    - Understand expected behavior for each scenario
    - Identify required dependencies (@Reference services)

  green_phase:
    - Implement scenario by scenario
    - Run tests after each scenario
    - Minimum code to pass

  refactor_phase:
    - Extract common patterns
    - Add logging
    - Improve error messages
    - Ensure all tests still pass

acceptance:
  command: "mvn test -Dtest={ProcessName}Spec"
  expected: "Tests run: 7, Failures: 0"
```

---

## Complete Workflow TDD Sequence

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                 SECURE ASSET APPROVAL WORKFLOW - TDD ORDER                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Step 1: AntivirusScanProcess                                             │
│   ├── Write Spec Tests (6 tests)                                           │
│   ├── AI Implements to pass tests                                          │
│   └── Refactor                                                              │
│                                                                             │
│   Step 2: QuarantineProcess                                                 │
│   ├── Write Spec Tests (7 tests)                                           │
│   ├── AI Implements to pass tests                                          │
│   └── Refactor                                                              │
│                                                                             │
│   Step 3: DynamicApproverAssignerProcess                                   │
│   ├── Write Spec Tests (6 tests)                                           │
│   ├── AI Implements to pass tests                                          │
│   └── Refactor                                                              │
│                                                                             │
│   Step 4: NotificationProcess                                               │
│   ├── Write Spec Tests (5 tests)                                           │
│   ├── AI Implements to pass tests                                          │
│   └── Refactor                                                              │
│                                                                             │
│   Step 5: Integration Tests                                                 │
│   ├── Write end-to-end workflow tests                                      │
│   ├── Verify all processes work together                                   │
│   └── Test routing logic between steps                                     │
│                                                                             │
│   Total: ~30 specification tests covering entire workflow                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Benefits for Workflow Development

| Benefit | How It Helps Workflows |
|---------|----------------------|
| **Clear Contracts** | Each process step has defined inputs/outputs |
| **Routing Validation** | Tests verify correct workflow branching |
| **Metadata Handling** | Tests ensure data passes between steps |
| **Error Scenarios** | Edge cases covered before production |
| **Regression Safety** | Changes don't break existing behavior |
| **AI Guardrails** | AI can't deviate from expected workflow logic |

---

## Quick Start: Add TDD to Your Workflow

1. **Pick a workflow process** (e.g., AntivirusScanProcess)
2. **Write 5-7 specification tests** covering:
   - Happy path
   - Error handling
   - Edge cases
3. **Create BEAD task** referencing tests
4. **Run AI agent** to implement
5. **Verify all tests pass**
6. **Repeat for next process**

---

*This guide extends Module 10: Test-Driven Agentic Development with workflow-specific examples.*
