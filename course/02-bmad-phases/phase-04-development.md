# BMAD Phase 04: Development Sprint
# Secure Asset Approval Workflow

## Phase Overview

This phase covers the implementation of all workflow components using AI-assisted development. We'll leverage AI agents to accelerate coding while maintaining quality standards.

---

## AI Agent Prompts for Phase 04

### Prompt 1: Service Implementation

```
You are an AEM Java Developer. Implement the AntivirusScanService
based on the following specification.

Component Specification:
[Paste CS-01 from Phase 03]

Requirements:
1. Implement the interface with ClamAV TCP integration
2. Add REST API fallback capability
3. Include mock mode for testing
4. Use OSGi Declarative Services annotations
5. Include comprehensive logging
6. Handle all error scenarios gracefully

Output: Complete Java implementation with JavaDoc.
```

### Prompt 2: Workflow Process Implementation

```
You are an AEM Workflow Developer. Implement the AntivirusScanProcess
workflow step.

Requirements:
1. Implement WorkflowProcess interface
2. Inject AntivirusScanService dependency
3. Set workflow metadata with scan results
4. Store results on asset metadata
5. Throw WorkflowException for infected files
6. Include ASCII art diagram in class JavaDoc

Output: Complete Java implementation ready for deployment.
```

### Prompt 3: Workflow Model Creation

```
You are an AEM Content Developer. Create the workflow model XML
for the Secure Asset Approval Workflow.

Based on the workflow diagram from Phase 03, create:
1. .content.xml with all nodes
2. All transitions between nodes
3. OR_SPLIT conditions using JavaScript
4. Proper metadata for each process step

Output: Complete .content.xml file for the workflow model.
```

---

## Development Tasks

### Sprint 1: Core Services (Week 1)

| Task | Story Points | AI Assistance Level |
|------|--------------|---------------------|
| AntivirusScanService interface | 2 | High - Generate interface |
| AntivirusScanServiceImpl | 5 | High - Generate with review |
| Unit tests for scanner | 3 | High - Generate test cases |
| ClamAV integration testing | 3 | Medium - Guide debugging |

### Sprint 2: Workflow Processes (Week 1-2)

| Task | Story Points | AI Assistance Level |
|------|--------------|---------------------|
| AntivirusScanProcess | 3 | High - Generate |
| QuarantineProcess | 3 | High - Generate |
| AssetApprovalParticipantChooser | 5 | High - Generate |
| NotificationProcess updates | 2 | Medium - Extend existing |

### Sprint 3: Workflow Model & Config (Week 2)

| Task | Story Points | AI Assistance Level |
|------|--------------|---------------------|
| Workflow model XML | 5 | High - Generate |
| Workflow launcher | 2 | High - Generate |
| OSGi configurations | 2 | High - Generate |
| Integration testing | 5 | Medium - Guide |

---

## Hands-On Lab: AI-Assisted Development

### Lab 4.1: Implement AntivirusScanService with AI

**Objective**: Use AI to implement the antivirus scanning service.

**Duration**: 45 minutes

#### Step 1: Set Up Context (5 min)

Open your AI assistant and provide context:

```
I'm implementing an AntivirusScanService for AEM that:
- Integrates with ClamAV daemon via TCP socket
- Has REST API fallback
- Has mock mode for testing
- Uses OSGi Declarative Services

Here's my interface:
```java
package com.demo.workflow.services;

public interface AntivirusScanService {
    class ScanResult {
        // ... [paste the ScanResult class]
    }

    ScanResult scanFile(InputStream is, String fileName, long size);
    ScanResult scanAsset(String assetPath);
    boolean isAvailable();
    String getScanEngineName();
}
```

Generate the implementation class.
```

#### Step 2: Review Generated Code (10 min)

AI will generate the implementation. Review for:

- [ ] Correct OSGi annotations (@Component, @Reference)
- [ ] Proper ClamAV protocol implementation
- [ ] Error handling for all failure cases
- [ ] Configuration with @Designate and @ObjectClassDefinition
- [ ] Logging at appropriate levels

#### Step 3: Refine with Follow-Up Prompts (15 min)

```
Good implementation. Now:
1. Add connection pooling for ClamAV connections
2. Implement exponential backoff for retries
3. Add metrics collection for scan duration
4. Ensure thread safety for concurrent scans
```

#### Step 4: Create Unit Tests (10 min)

```
Generate JUnit 5 tests for AntivirusScanServiceImpl that:
1. Test successful scan with clean file
2. Test detection of infected file
3. Test ClamAV connection failure with fallback
4. Test mock mode behavior
5. Test configuration changes

Use Mockito for mocking dependencies.
```

#### Step 5: Integrate and Verify (5 min)

```bash
# Copy generated files to project
# Build the project
mvn clean install -pl core

# Check for compilation errors
# Review test results
```

---

### Lab 4.2: Implement Workflow Processes with AI

**Objective**: Generate workflow process steps using AI.

**Duration**: 60 minutes

#### Step 1: Generate AntivirusScanProcess (15 min)

```
Create an AEM WorkflowProcess implementation called AntivirusScanProcess.

Requirements:
- Inject AntivirusScanService
- Get asset from workflow payload
- Call scanFile for the asset's original rendition
- Set these workflow metadata keys:
  - av.scanStatus (CLEAN/INFECTED/ERROR/SKIPPED)
  - av.scanEngine
  - av.scanTime
  - av.threatName (if infected)
- Store scan status on asset metadata
- Throw WorkflowException if infected (routes to quarantine)

Include ASCII art diagram in the class JavaDoc showing the process flow.
```

#### Step 2: Generate QuarantineProcess (15 min)

```
Create QuarantineProcess that:
- Moves infected asset to /content/dam/quarantine/YYYY/MM/DD/
- Preserves original path in metadata
- Sets quarantine date and expiry date
- Has configurable retention period via OSGi
- Logs all quarantine actions for audit

Include error handling for:
- Asset not found
- Quarantine folder creation failure
- Permission issues
```

#### Step 3: Generate AssetApprovalParticipantChooser (15 min)

```
Create a ParticipantStepChooser that routes to groups based on:

Level 1:
- image/* -> image-reviewers
- video/* -> video-reviewers
- application/pdf, office docs -> document-reviewers
- default -> content-reviewers

Level 2:
- /brand/* path -> brand-managers
- /legal/* path -> legal-reviewers
- dam:sensitivityLevel=high -> senior-approvers
- file size > 50MB -> senior-approvers
- default -> senior-approvers

Level 3:
- all -> content-directors

Read approval level from process arguments.
```

#### Step 4: Review and Integrate (15 min)

1. Copy generated classes to project
2. Build and fix any issues
3. Deploy to local AEM
4. Verify in OSGi console

```bash
mvn clean install -PautoInstallSinglePackage
```

---

### Lab 4.3: Create Workflow Model with AI

**Objective**: Generate the complete workflow model XML.

**Duration**: 45 minutes

#### Step 1: Generate Workflow Model (20 min)

```
Create an AEM workflow model XML file for "Secure Asset Approval Workflow"
with these nodes:

1. START
2. AntivirusScanProcess (auto-advance)
3. OR_SPLIT: Check scan result
   - CLEAN -> continue to Level 1
   - INFECTED -> go to Quarantine
4. QuarantineProcess (for infected files)
5. SecurityNotification
6. Level 1 Review (DynamicParticipant)
7. OR_SPLIT: Level 1 Decision
   - approved -> Level 2
   - rejected -> notify + end
   - revise -> notify + end
8. Level 2 Review (DynamicParticipant)
9. OR_SPLIT: Level 2 Decision
   - approved -> publish
   - rejected -> notify + end
   - escalate -> Level 3
10. Level 3 Review (DynamicParticipant)
11. OR_SPLIT: Level 3 Decision
    - approved -> publish
    - rejected -> notify + end
12. ReplicationProcess
13. Success Notification
14. AuditLog
15. END (complete)
16. END (rejected)
17. END (quarantined)

Include all transitions with proper rule references.
```

#### Step 2: Generate Workflow Launcher (5 min)

```
Create a workflow launcher that triggers the Secure Asset Approval
Workflow when:
- Assets are created (eventType=1)
- In paths: /content/dam/secure-assets/*, /content/dam/uploads/*,
  /content/dam/pending-approval/*
- Node type: dam:Asset
- Run mode: author
- Enabled by default
```

#### Step 3: Deploy and Test (20 min)

```bash
# Build and deploy
mvn clean install -PautoInstallSinglePackage

# Open workflow models console
open http://localhost:4502/libs/cq/workflow/admin/console/content/models.html

# Edit the workflow model to verify structure
# Test by uploading an asset to /content/dam/secure-assets/
```

---

## Code Review Guidelines

### AI-Generated Code Review Checklist

| Category | Check | Pass/Fail |
|----------|-------|-----------|
| **Security** | No hardcoded credentials | |
| **Security** | Input validation present | |
| **Security** | Proper exception handling | |
| **Performance** | Resources properly closed | |
| **Performance** | No memory leaks | |
| **Performance** | Appropriate logging levels | |
| **AEM Standards** | Correct OSGi annotations | |
| **AEM Standards** | Sling Model best practices | |
| **AEM Standards** | Workflow process patterns | |
| **Code Quality** | Meaningful variable names | |
| **Code Quality** | Appropriate comments | |
| **Code Quality** | No code duplication | |

### Review Prompt for AI

```
Review this AEM workflow process code for:
1. Security vulnerabilities (injection, XSS, etc.)
2. Performance issues (resource leaks, inefficient operations)
3. AEM best practices compliance
4. Error handling completeness
5. Thread safety concerns

[Paste code here]

Provide specific feedback with line numbers and suggested fixes.
```

---

## Testing Strategy

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class AntivirusScanServiceImplTest {

    @Mock
    private ResourceResolverFactory resolverFactory;

    @InjectMocks
    private AntivirusScanServiceImpl service;

    @Test
    void scanFile_cleanFile_returnsCleanResult() {
        // Arrange
        InputStream cleanFile = new ByteArrayInputStream("clean content".getBytes());

        // Act
        ScanResult result = service.scanFile(cleanFile, "clean.pdf", 1024);

        // Assert
        assertTrue(result.isClean());
        assertNull(result.getThreatName());
    }

    @Test
    void scanFile_infectedFile_returnsInfectedResult() {
        // Arrange - EICAR test signature
        String eicar = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
        InputStream infectedFile = new ByteArrayInputStream(eicar.getBytes());

        // Act
        ScanResult result = service.scanFile(infectedFile, "virus.exe", eicar.length());

        // Assert
        assertFalse(result.isClean());
        assertNotNull(result.getThreatName());
    }
}
```

### Integration Tests

```java
@ExtendWith(AemContextExtension.class)
class AntivirusScanProcessIntegrationTest {

    private final AemContext context = new AemContext();

    @Test
    void execute_cleanAsset_setsCleanMetadata() throws Exception {
        // Create test asset
        context.create().asset("/content/dam/test.pdf", "/test.pdf", "application/pdf");

        // Create workflow context
        WorkItem workItem = createWorkItem("/content/dam/test.pdf");

        // Execute process
        AntivirusScanProcess process = context.registerInjectActivateService(
            new AntivirusScanProcess());
        process.execute(workItem, session, metaData);

        // Verify metadata set
        assertEquals("CLEAN",
            workItem.getWorkflow().getWorkflowData()
                .getMetaDataMap().get("av.scanStatus"));
    }
}
```

---

## AI Agent Task Completion

When Phase 04 is complete, the AI agent should report:

```yaml
phase: "04-development"
status: "complete"
deliverables:
  - name: "antivirus-scan-service"
    status: "implemented"
    files:
      - AntivirusScanService.java
      - AntivirusScanServiceImpl.java
    tests:
      unit: 12
      passing: 12
  - name: "workflow-processes"
    status: "implemented"
    files:
      - AntivirusScanProcess.java
      - QuarantineProcess.java
      - AssetApprovalParticipantChooser.java
    tests:
      unit: 18
      passing: 18
  - name: "workflow-model"
    status: "deployed"
    nodes: 21
    transitions: 24
  - name: "configurations"
    status: "deployed"
    configs:
      - AntivirusScanServiceImpl.cfg.json
      - QuarantineProcess.cfg.json
      - AssetApprovalParticipantChooser.cfg.json
build:
  status: "success"
  bundle_state: "Active"
blockers: []
next_phase: "05-testing"
```

---

## Transition to Phase 05

Prerequisites for Phase 05:
- [ ] All code implemented and reviewed
- [ ] Unit tests passing (>90% coverage)
- [ ] Bundle deployed and Active
- [ ] Workflow model visible in console
- [ ] Basic smoke test completed
