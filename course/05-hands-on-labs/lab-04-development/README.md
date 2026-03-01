# Lab 4: AI-Assisted Development
# Building the Secure Asset Approval Workflow

## Lab Overview

| Attribute | Value |
|-----------|-------|
| Duration | 90 minutes |
| Difficulty | Intermediate |
| Prerequisites | Labs 1-3 completed |
| AI Tools | Claude Code, Copilot, or similar |

## Learning Objectives

By the end of this lab, you will:
- [ ] Use AI to generate AEM workflow components
- [ ] Implement the AntivirusScanService with AI assistance
- [ ] Create workflow processes using AI pair programming
- [ ] Review and refine AI-generated code
- [ ] Deploy and verify the implementation

---

## Lab Exercises

### Exercise 4.1: Generate AntivirusScanService Interface (15 min)

#### Step 1: Set Up Your AI Context

Open your AI coding assistant and establish context:

```
I'm building an antivirus scanning service for AEM.

Project context:
- AEM Cloud Service project
- Java 11
- OSGi Declarative Services
- The service will integrate with ClamAV for virus scanning

I need to create an interface called AntivirusScanService.

Requirements:
1. Method to scan an InputStream with filename and size
2. Method to scan an asset by JCR path
3. Method to check if the scanner is available
4. Method to get the current scan engine name
5. A ScanResult inner class with: clean (boolean), threatName, scanEngine, durationMs, details

Generate the interface with full JavaDoc.
```

#### Step 2: Review the Generated Interface

The AI should generate something like:

```java
package com.demo.workflow.services;

import java.io.InputStream;

/**
 * Antivirus Scanning Service
 * ...
 */
public interface AntivirusScanService {

    class ScanResult {
        private final boolean clean;
        private final String threatName;
        // ...
    }

    ScanResult scanFile(InputStream inputStream, String fileName, long fileSize);
    ScanResult scanAsset(String assetPath);
    boolean isAvailable();
    String getScanEngineName();
}
```

#### Step 3: Refine and Save

1. Review for completeness
2. Add any missing factory methods to ScanResult
3. Save to: `core/src/main/java/com/demo/workflow/services/AntivirusScanService.java`

```bash
# Verify compilation
mvn compile -pl core -q
```

#### Checkpoint 4.1
- [ ] Interface compiles without errors
- [ ] All required methods present
- [ ] ScanResult class is complete
- [ ] JavaDoc is comprehensive

---

### Exercise 4.2: Implement AntivirusScanServiceImpl (30 min)

#### Step 1: Request Implementation

```
Now implement AntivirusScanServiceImpl based on the interface.

Requirements:
1. Support three scan engines via OSGi config:
   - CLAMAV: TCP socket connection to ClamAV daemon
   - REST_API: Placeholder for cloud scanning service
   - MOCK: For testing (files starting with "virus_" are infected)

2. OSGi Configuration (@Designate):
   - scanEngine (String): CLAMAV, REST_API, MOCK
   - clamavHost (String): default "localhost"
   - clamavPort (int): default 3310
   - connectionTimeout (int): default 5000 ms
   - readTimeout (int): default 60000 ms
   - maxFileSize (long): default 100MB
   - enabled (boolean): default true

3. ClamAV integration:
   - Use INSTREAM command protocol
   - Send file in 8KB chunks
   - Parse response for OK/FOUND/ERROR

4. Error handling:
   - Connection failures should log and return error result
   - Timeout handling
   - File size validation

5. Include ASCII art diagram in class JavaDoc

Generate the complete implementation.
```

#### Step 2: Review ClamAV Protocol

The AI should implement the INSTREAM protocol correctly:

```java
private ScanResult scanWithClamAV(InputStream inputStream, String fileName) {
    try (Socket socket = new Socket(config.clamavHost(), config.clamavPort())) {
        socket.setSoTimeout(config.readTimeout());

        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        // Send INSTREAM command
        out.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
        out.flush();

        // Stream file content in chunks
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            // Send chunk length as 4-byte big-endian
            out.write(new byte[]{
                (byte) (bytesRead >> 24),
                (byte) (bytesRead >> 16),
                (byte) (bytesRead >> 8),
                (byte) bytesRead
            });
            out.write(buffer, 0, bytesRead);
        }

        // Send zero-length chunk to signal end
        out.write(new byte[]{0, 0, 0, 0});
        out.flush();

        // Read and parse response...
    }
}
```

#### Step 3: Verify and Refine

Ask the AI to check for issues:

```
Review this implementation for:
1. Resource leaks (streams, sockets)
2. Thread safety
3. Proper exception handling
4. Logging best practices

Also add:
- Connection health check method
- Exponential backoff for retries
```

#### Step 4: Save and Test

```bash
# Save the file
# Location: core/src/main/java/com/demo/workflow/services/impl/AntivirusScanServiceImpl.java

# Compile
mvn compile -pl core -q

# If using Docker for ClamAV:
docker-compose up -d clamav
sleep 30  # Wait for startup

# Test with mock mode first by modifying the config
```

#### Checkpoint 4.2
- [ ] Implementation compiles
- [ ] All three scan engines implemented
- [ ] OSGi configuration complete
- [ ] ClamAV protocol correct
- [ ] Error handling comprehensive

---

### Exercise 4.3: Create AntivirusScanProcess (20 min)

#### Step 1: Generate Workflow Process

```
Create an AEM workflow process called AntivirusScanProcess.

Requirements:
1. Implement WorkflowProcess interface
2. Inject AntivirusScanService via @Reference
3. Process logic:
   a. Get asset path from workflow payload
   b. Verify it's a DAM asset
   c. Get original rendition
   d. Call scanFile() on the rendition
   e. Set workflow metadata:
      - av.scanStatus (CLEAN/INFECTED/ERROR/SKIPPED)
      - av.scanEngine
      - av.scanTime
      - av.threatName (if infected)
      - av.scanDuration
   f. Store scan result on asset metadata
   g. If infected, throw WorkflowException

4. Include comprehensive logging
5. Add ASCII art diagram in class JavaDoc

Generate the complete implementation.
```

#### Step 2: Verify Workflow Metadata Handling

```java
private void setWorkflowMetadata(WorkItem workItem, String status, String engine,
                                  long duration, String threatName, String details) {
    MetaDataMap workflowMeta = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
    workflowMeta.put("av.scanStatus", status);
    workflowMeta.put("av.scanEngine", engine);
    workflowMeta.put("av.scanTime", System.currentTimeMillis());
    workflowMeta.put("av.scanDuration", duration);
    workflowMeta.put("av.scanDetails", details);

    if (threatName != null) {
        workflowMeta.put("av.threatName", threatName);
    }
}
```

#### Step 3: Save and Compile

```bash
# Save to: core/src/main/java/com/demo/workflow/process/AntivirusScanProcess.java

# Compile
mvn compile -pl core -q
```

#### Checkpoint 4.3
- [ ] Process compiles
- [ ] WorkflowProcess interface implemented correctly
- [ ] Metadata handling complete
- [ ] Error cases throw WorkflowException

---

### Exercise 4.4: Generate Unit Tests (15 min)

#### Step 1: Request Test Generation

```
Generate JUnit 5 tests for AntivirusScanServiceImpl.

Test cases needed:
1. scanFile with clean file returns CLEAN result
2. scanFile with infected file (mock mode) returns INFECTED
3. scanFile when scanner unavailable returns ERROR
4. isAvailable returns true when ClamAV responds to PING
5. isAvailable returns false when connection fails
6. scanFile exceeding maxFileSize returns ERROR
7. Mock mode correctly identifies "virus_*" files as infected

Use:
- @ExtendWith(MockitoExtension.class)
- Mock ResourceResolverFactory
- Test both happy path and error scenarios

Generate complete test class with all test methods.
```

#### Step 2: Review and Run Tests

```bash
# Save to: core/src/test/java/com/demo/workflow/services/impl/AntivirusScanServiceImplTest.java

# Run tests
mvn test -pl core -Dtest=AntivirusScanServiceImplTest

# Expected output:
# Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

#### Checkpoint 4.4
- [ ] All tests pass
- [ ] Coverage >80%
- [ ] Edge cases tested

---

### Exercise 4.5: Deploy and Verify (10 min)

#### Step 1: Build and Deploy

```bash
# Full build
mvn clean install

# Deploy to local AEM
mvn clean install -PautoInstallSinglePackage

# Check bundle status
curl -u admin:admin \
  "http://localhost:4502/system/console/bundles.json" | \
  jq '.data[] | select(.name | contains("demo-workflow"))'

# Expected: "state": "Active"
```

#### Step 2: Verify OSGi Configuration

```bash
# Open Felix Console
open "http://localhost:4502/system/console/configMgr"

# Search for "Antivirus"
# Verify configuration appears
```

#### Step 3: Test with Mock Mode

1. Upload a test file to `/content/dam/test/`
2. Check workflow instances
3. Verify metadata set on asset

```bash
# Check asset metadata
curl -u admin:admin \
  "http://localhost:4502/content/dam/test/sample.pdf/jcr:content/metadata.json" | \
  jq '.["dam:avScanStatus"]'

# Expected: "CLEAN"
```

#### Step 4: Test Infected File Detection

```bash
# Upload a file named "virus_test.exe" to DAM
# In mock mode, this should be flagged as infected

# Check quarantine folder
curl -u admin:admin \
  "http://localhost:4502/content/dam/quarantine.1.json"
```

#### Checkpoint 4.5
- [ ] Bundle is Active
- [ ] OSGi config visible
- [ ] Clean files pass through
- [ ] "virus_*" files detected (mock mode)

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Bundle not starting | Missing import | Check OSGi console for errors |
| Workflow not triggering | Launcher config | Verify path regex matches |
| ClamAV connection failed | Container not running | `docker-compose up -d clamav` |
| Scan timeout | Large file | Increase readTimeout config |

### Debug Commands

```bash
# Check bundle errors
curl -u admin:admin \
  "http://localhost:4502/system/console/bundles/com.demo.workflow.core" | \
  jq '.data[0].error'

# Check workflow status
curl -u admin:admin \
  "http://localhost:4502/libs/cq/workflow/admin/console/content/instances.html"

# View error log
tail -f crx-quickstart/logs/error.log
```

---

## Lab Completion Checklist

- [ ] AntivirusScanService interface created
- [ ] AntivirusScanServiceImpl with ClamAV/REST/Mock support
- [ ] AntivirusScanProcess workflow step
- [ ] Unit tests passing (>80% coverage)
- [ ] Bundle deployed and Active
- [ ] End-to-end test successful

---

## Next Lab

Proceed to [Lab 5: BEAD Task Management](../lab-05-bead-tracking/README.md)
