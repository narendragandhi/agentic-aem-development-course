---
name: aem-workflow-development
description: Comprehensive guide for developing custom workflows in Adobe Experience Manager (AEM) as a Cloud Service. Use this skill when creating, implementing, or debugging workflow processes, workflow models, custom process steps, or any workflow-related development tasks in AEM Cloud Service. Based on the latest AEM SDK API (2025.x) and Granite Workflow APIs.
---

# AEM Workflow Development

Guide for developing custom workflows in AEM as a Cloud Service using the latest Granite Workflow APIs and best practices.

## Core Principles

### Use Granite Workflow APIs (Not Legacy CQ APIs)

**CRITICAL**: Always use `com.adobe.granite.workflow` packages, NOT the deprecated `com.day.cq.workflow` packages.

**Correct Imports:**
```java
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
```

**Incorrect (Deprecated) Imports:**
```java
// DO NOT USE - These are deprecated
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
```

### AEM Cloud Service Considerations

- AEM as a Cloud Service uses containerized architecture with transient instances
- Content is distributed using Sling Content Distribution (not traditional replication)
- Workflow instances run in ephemeral containers
- Always design workflows to be stateless and resilient
- Avoid long-running workflow processes (use external job processing if needed)

**Asset Processing in Cloud Service:**
- Traditional DAM Update Asset workflow has been replaced by **Asset Microservices**
- Asset processing (renditions, metadata extraction) is now handled by cloud-native microservices
- Custom asset processing should use **post-processing workflows** configured per folder
- Post-processing workflows run AFTER Asset Microservices complete their processing

**AEM Forms Workflows:**
- Forms-centric workflows can ONLY run on Author instances
- Adaptive Forms on Publish can submit data to workflows on Author
- This pattern enables approval workflows for form submissions
- Configure form submission action to trigger workflow on Author instance

## Maven Dependencies

### Current SDK Version
Latest AEM SDK API: `2025.11.23482.20251120T200914Z-251200`

### POM Configuration

```xml
<dependency>
    <groupId>com.adobe.aem</groupId>
    <artifactId>aem-sdk-api</artifactId>
    <version>2025.11.23482.20251120T200914Z-251200</version>
    <scope>provided</scope>
</dependency>
```

### Java Version Requirements
- **AEM Cloud Service (2025.x+)**: Java 21 (LTS)
- **Minimum Maven Version**: 3.8.6
- **Required Plugins**:
  - `aemanalyser-maven-plugin`: 1.6.6+
  - `maven-bundle-plugin`: 5.1.5+

### Java 21 Configuration
Create `.cloudmanager/java-version` file in project root:
```
21
```

Update `pom.xml`:
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

## Creating Custom Workflow Processes

### Step 1: Implement WorkflowProcess Interface

```java
package com.example.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Custom Asset Processing"
    }
)
public class CustomWorkflowProcess implements WorkflowProcess {
    
    private static final Logger LOG = LoggerFactory.getLogger(CustomWorkflowProcess.class);
    
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) 
            throws WorkflowException {
        
        try {
            // Get ResourceResolver from WorkflowSession
            ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
            
            // Get the workflow payload
            String payloadPath = workItem.getWorkflowData().getPayload().toString();
            LOG.info("Processing workflow for payload: {}", payloadPath);
            
            // Get the resource
            Resource resource = resolver.getResource(payloadPath);
            if (resource == null) {
                LOG.warn("Resource not found at path: {}", payloadPath);
                return;
            }
            
            // Read process arguments from MetaDataMap
            String customArg = metaDataMap.get("PROCESS_ARGS", String.class);
            LOG.info("Process arguments: {}", customArg);
            
            // Access workflow metadata
            MetaDataMap workflowMetadata = workItem.getWorkflow().getMetaDataMap();
            
            // Store data for next workflow step
            workflowMetadata.put("processedBy", "CustomWorkflowProcess");
            workflowMetadata.put("processedAt", System.currentTimeMillis());
            
            // Perform custom processing logic here
            performCustomProcessing(resource, resolver);
            
            LOG.info("Workflow processing completed successfully");
            
        } catch (Exception e) {
            LOG.error("Error in workflow process", e);
            throw new WorkflowException("Failed to process workflow", e);
        }
    }
    
    private void performCustomProcessing(Resource resource, ResourceResolver resolver) {
        // Custom business logic implementation
    }
}
```

### Step 2: OSGi Component Configuration

**Key Annotations:**
- `@Component(service = WorkflowProcess.class)` - Registers as OSGi service
- `property = {"process.label=..."}` - Sets display name in AEM Workflow console

**Additional Properties (Optional):**
```java
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Custom Process",
        "service.description=Performs custom asset processing",
        "service.vendor=Your Company"
    }
)
```

## Working with WorkflowSession

### Obtaining WorkflowSession

**From ResourceResolver:**
```java
WorkflowSession workflowSession = resourceResolver.adaptTo(WorkflowSession.class);
```

**From JCR Session:**
```java
WorkflowSession workflowSession = jcrSession.adaptTo(WorkflowSession.class);
```

### Key WorkflowSession Methods

```java
// Get workflow model
// NOTE: In AEM Cloud Service, use activation/deactivation workflows for pages
// Asset processing workflows have been replaced by Asset Microservices
WorkflowModel model = workflowSession.getModel("/var/workflow/models/request_for_activation");

// Create workflow data (payload)
WorkflowData workflowData = workflowSession.newWorkflowData("JCR_PATH", "/content/my-site/page");

// Start workflow programmatically
Map<String, Object> metadata = new HashMap<>();
metadata.put("customKey", "customValue");
Workflow workflow = workflowSession.startWorkflow(model, workflowData, metadata);

// Get active work items
ResultSet<WorkItem> activeItems = workflowSession.getActiveWorkItems();

// Complete work item
workflowSession.complete(workItem, route);

// Terminate workflow
workflowSession.terminateWorkflow(workflow);

// Get workflow by ID
Workflow wf = workflowSession.getWorkflow(workflowId);
```

## Working with WorkItem and Payload

### Accessing Payload

```java
// Get payload path
String payloadPath = workItem.getWorkflowData().getPayload().toString();

// Get payload type
String payloadType = workItem.getWorkflowData().getPayloadType();
// Common types: "JCR_PATH", "JCR_UUID"

// Get resource from payload
ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
Resource resource = resolver.getResource(payloadPath);
```

### Working with Workflow Metadata

```java
// Access workflow instance metadata
MetaDataMap workflowMetadata = workItem.getWorkflow().getMetaDataMap();

// Read metadata
String value = workflowMetadata.get("key", String.class);
Integer intValue = workflowMetadata.get("count", 0);

// Write metadata (available to subsequent steps)
workflowMetadata.put("status", "processed");
workflowMetadata.put("timestamp", new Date());

// Access process step arguments
String processArgs = metaDataMap.get("PROCESS_ARGS", String.class);
```

### Persisting Data Between Steps

```java
// Store data for next workflow step
private void persistData(WorkItem workItem, WorkflowSession workflowSession, 
                        String key, Object value) throws WorkflowException {
    MetaDataMap wfMetadata = workItem.getWorkflow().getMetaDataMap();
    wfMetadata.put(key, value);
    workflowSession.updateWorkflowData(workItem.getWorkflow(), 
                                       workItem.getWorkflow().getWorkflowData());
}

// Retrieve persisted data
private <T> T getPersistedData(WorkItem workItem, String key, Class<T> type) {
    return workItem.getWorkflow().getMetaDataMap().get(key, type);
}
```

## Programmatically Starting Workflows

```java
@Reference
private ResourceResolverFactory resolverFactory;

public void startWorkflow() throws WorkflowException {
    Map<String, Object> authInfo = new HashMap<>();
    authInfo.put(ResourceResolverFactory.SUBSERVICE, "workflow-service");
    
    try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(authInfo)) {
        WorkflowSession workflowSession = resolver.adaptTo(WorkflowSession.class);
        
        // Get workflow model (path changed in AEM 6.4+)
        // AEM 6.4+: /var/workflow/models/...
        // Pre-6.4: /etc/workflow/models/.../jcr:content/model
        
        // NOTE: DAM Update Asset workflow replaced by Asset Microservices in Cloud Service
        // Use post-processing workflows for custom asset operations
        String modelPath = "/var/workflow/models/request_for_activation";
        WorkflowModel model = workflowSession.getModel(modelPath);
        
        // Create workflow payload
        String pagePath = "/content/my-site/page";
        WorkflowData wfData = workflowSession.newWorkflowData("JCR_PATH", pagePath);
        
        // Optional: Add workflow metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("initiatedBy", "automated-process");
        metadata.put("priority", "high");
        
        // Start the workflow
        Workflow workflow = workflowSession.startWorkflow(model, wfData, metadata);
        
        LOG.info("Started workflow: {} for payload: {}", 
                 workflow.getId(), pagePath);
    }
}
```

## Error Handling and Best Practices

### Exception Handling

```java
@Override
public void execute(WorkItem workItem, WorkflowSession workflowSession, 
                   MetaDataMap metaDataMap) throws WorkflowException {
    try {
        // Workflow logic
        performWorkflowLogic(workItem, workflowSession);
        
    } catch (RepositoryException e) {
        LOG.error("JCR Repository error in workflow", e);
        throw new WorkflowException("Repository access failed", e);
        
    } catch (IllegalArgumentException e) {
        LOG.error("Invalid workflow arguments", e);
        throw new WorkflowException("Invalid process configuration", e);
        
    } catch (Exception e) {
        LOG.error("Unexpected error in workflow process", e);
        // Throwing WorkflowException will cause workflow to retry
        throw new WorkflowException("Workflow processing failed", e);
    }
}
```

### Resource Management

```java
// Always close ResourceResolvers when manually created
ResourceResolver resolver = null;
try {
    resolver = resolverFactory.getServiceResourceResolver(authInfo);
    // Use resolver
} finally {
    if (resolver != null && resolver.isLive()) {
        resolver.close();
    }
}

// When obtaining from WorkflowSession, no need to close
// (it's managed by the workflow engine)
ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
```

### Workflow Model Locations

**AEM 6.4+ / Cloud Service:**
```
/var/workflow/models/request_for_activation
/var/workflow/models/request_for_deactivation
/var/workflow/models/[custom-workflow-models]
```

**Pre-AEM 6.4 (legacy):**
```
/etc/workflow/models/dam/update_asset/jcr:content/model
```

**IMPORTANT - Asset Processing:**
- The traditional `/var/workflow/models/dam/update_asset` path may exist in the SDK but is NOT used in Cloud Service production
- Asset processing is handled by Asset Microservices
- For custom asset operations, configure post-processing workflows per folder in the DAM

### Common Workflow Models in AEM Cloud Service

**IMPORTANT: Asset Processing Changes**
- Traditional `/var/workflow/models/dam/update_asset` workflow has been replaced by **Asset Microservices** in AEM as a Cloud Service
- Asset processing is now handled by cloud-native microservices, not traditional workflows
- Use **post-processing workflows** for custom asset operations after microservices processing
- For custom asset processing, create workflows that run AFTER Asset Microservices complete

**Available Workflow Models:**
- `/var/workflow/models/request_for_activation` - Request for Activation (page publishing)
- `/var/workflow/models/request_for_deactivation` - Request for Deactivation
- Post-processing workflows for custom asset operations (configured per folder)
- Custom workflow models for non-asset content processing

## Testing Workflows

### Unit Testing with Mocks

```java
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomWorkflowProcessTest {
    
    @Test
    void testExecute() throws WorkflowException {
        // Arrange
        WorkItem workItem = mock(WorkItem.class);
        WorkflowSession workflowSession = mock(WorkflowSession.class);
        MetaDataMap metaDataMap = mock(MetaDataMap.class);
        WorkflowData workflowData = mock(WorkflowData.class);
        Workflow workflow = mock(Workflow.class);
        
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflowData.getPayload()).thenReturn("/content/dam/test.jpg");
        
        // Act
        CustomWorkflowProcess process = new CustomWorkflowProcess();
        process.execute(workItem, workflowSession, metaDataMap);
        
        // Assert - verify expected behavior
    }
}
```

## Workflow Participation Patterns

### Participant Steps

Workflows often require user interaction. Implement participant steps for approval, review, or decision-making:

**Participant Step Types:**
- **Participant Step**: Assigns work item to specific user/group
- **Dynamic Participant Step**: Determines assignee programmatically via script or service
- **Dialog Participant Step**: Presents dialog for user input before proceeding

**Example: Implementing Dynamic Participant Chooser**

```java
@Component(
    service = ParticipantStepChooser.class,
    property = {
        "chooser.label=Custom Participant Chooser"
    }
)
public class CustomParticipantChooser implements ParticipantStepChooser {
    
    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, 
                                 MetaDataMap metaDataMap) throws WorkflowException {
        
        // Determine assignee based on workflow context
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        
        // Example: Assign based on content path
        if (payloadPath.startsWith("/content/we-retail")) {
            return "content-approvers"; // Group ID
        } else if (payloadPath.startsWith("/content/wknd")) {
            return "admin"; // User ID
        }
        
        return "administrators"; // Default group
    }
}
```

### Workflow Inbox Integration

Users interact with workflows through the AEM Inbox:

**User Actions Available:**
- **Complete**: Approve and advance to next step
- **Delegate**: Reassign work item to another user
- **Step Back**: Return to previous workflow step
- **View Details**: See workflow metadata and history

**Accessing Workflow Items Programmatically:**

```java
// Get user's active work items
WorkflowSession wfSession = resourceResolver.adaptTo(WorkflowSession.class);
ResultSet<WorkItem> workItems = wfSession.getActiveWorkItems();

// Filter work items
while (workItems.hasNext()) {
    WorkItem item = workItems.next();
    String payload = item.getWorkflowData().getPayload().toString();
    String assignee = item.getCurrentAssignee();
    
    LOG.info("Work item: {} assigned to: {}", payload, assignee);
}
```

### Workflow Launcher Configuration

Automatically trigger workflows based on repository events:

**Launcher Properties:**
- **Event Type**: Created, Modified, Removed
- **Node Type**: dam:Asset, cq:Page, etc.
- **Path**: Repository path where launcher applies (supports wildcards)
- **Exclude List**: Paths to exclude from triggering
- **Run Modes**: Author, Publish, or both
- **Workflow Model**: Which workflow to start

**Example Configuration (via JCR):**
```xml
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="cq:WorkflowLauncher"
    description="Auto-process new assets in specific folder"
    eventType="1" 
    glob="/content/dam/auto-process/**"
    nodetype="dam:Asset"
    runModes="author"
    workflow="/var/workflow/models/custom-asset-workflow"/>
```

**Event Types:**
- `1` = Created
- `2` = Modified
- `4` = Removed

### Workflow Stages

Organize workflows into logical stages for better tracking:

```java
// Configure stages in workflow model metadata
// Stages appear in timeline and reporting

// In custom process, update current stage
MetaDataMap wfMetadata = workItem.getWorkflow().getMetaDataMap();
wfMetadata.put("currentStage", "Review");
wfMetadata.put("stageStartTime", System.currentTimeMillis());
```

**Best Practices for Stages:**
- Keep stage names consistent across workflows
- Limit to 5-7 stages for clarity
- Update stage at beginning of each major phase
- Use stages for reporting and dashboards

## Advanced Topics

### Asset Processing in Cloud Service

**Asset Microservices vs Traditional Workflows:**

In AEM as a Cloud Service, asset processing has fundamentally changed:

**Traditional (AEM 6.x and earlier):**
- Assets processed via DAM Update Asset workflow
- Synchronous processing in AEM instance
- Limited scalability

**Cloud Service (Current):**
- Asset processing handled by cloud-native Asset Microservices
- Automatic rendition generation, metadata extraction
- Horizontally scalable and highly performant
- Runs outside AEM instances

**For Custom Asset Processing:**

Use **post-processing workflows** that run AFTER Asset Microservices complete:

1. **Configure Processing Profile with Post-Processing Workflow:**
   - Navigate to Tools → Assets → Processing Profiles
   - Create or edit a processing profile
   - Add your custom workflow in the "Post-processing Workflow" section
   - Apply the profile to specific DAM folders

   **IMPORTANT**: Post-processing workflows MUST end with the **"DAM Update Asset Workflow Completed"** process step. This step signals to Asset Microservices that custom processing is complete. Without this step, the asset may appear stuck in "processing" state.

2. **Create Custom Post-Processing Workflow Step:**
```java
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Custom Post-Processing Step"
    }
)
public class CustomAssetPostProcessor implements WorkflowProcess {
    
    @Override
    public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap metaDataMap) 
            throws WorkflowException {
        
        // Asset has already been processed by Asset Microservices
        // Renditions, metadata extraction are complete
        
        String assetPath = workItem.getWorkflowData().getPayload().toString();
        ResourceResolver resolver = wfSession.adaptTo(ResourceResolver.class);
        
        Resource assetResource = resolver.getResource(assetPath);
        Asset asset = assetResource.adaptTo(Asset.class);
        
        // Perform custom operations AFTER microservices processing
        // Examples: custom metadata, external system integration, tagging
        
        LOG.info("Post-processing asset: {}", assetPath);
    }
}
```

3. **Asset Reprocessing:**
   - Use "Reprocess Assets" feature in Assets UI
   - Sends assets through entire microservices pipeline again
   - Useful after updating processing profiles

**For Bulk Asset Operations:**
- Use Asset Compute SDK for custom processing workers
- Configure as part of processing profiles
- Runs in Adobe I/O Runtime

### Workflow Variables (External Storage)

For sensitive data or large payloads, configure external storage:

```java
// At workflow model level, flag can be set:
// Property: userMetadataPersistenceEnabled = true
// This stores variables externally, not in JCR

// Access in code remains the same
MetaDataMap metadata = workItem.getWorkflow().getMetaDataMap();
```

### Synthetic Workflow (ACS Commons)

For high-performance bulk processing without full workflow engine overhead (primarily for SDK/local development):

```java
// Using ACS Commons Synthetic Workflow
// NOTE: This is useful for local development and SDK
// In Cloud Service, asset processing uses Asset Microservices
@Reference
private SyntheticWorkflowRunner syntheticWorkflowRunner;

public void processBulkContent(List<String> contentPaths, String workflowModelPath) {
    SyntheticWorkflowModel model = 
        syntheticWorkflowRunner.getSyntheticWorkflowModel(
            resourceResolver, 
            workflowModelPath,
            true // Track progress
        );
    
    for (String contentPath : contentPaths) {
        syntheticWorkflowRunner.execute(
            resourceResolver,
            contentPath,
            model,
            false, // Auto-save changes
            false  // Throw exceptions
        );
    }
}
```

### Workflow REST API

```bash
# Get workflow models
curl -u admin:admin http://localhost:4502/libs/cq/workflow/content/console/content/models.json

# Start workflow via REST (example: activation workflow)
curl -u admin:admin -X POST \
  http://localhost:4502/etc/workflow/instances \
  -d model=/var/workflow/models/request_for_activation \
  -d payloadType=JCR_PATH \
  -d payload=/content/my-site/page

# Get workflow instance details
curl -u admin:admin \
  http://localhost:4502/etc/workflow/instances/2025-02-01/activation_12345.json

# Complete work item
curl -u admin:admin -X POST \
  http://localhost:4502/etc/workflow/instances/{workflowId}/workItems/{workItemId} \
  -d route=Approve
```

## Common Patterns

### Email Notification in Workflow

```java
@Reference
private MessageGatewayService messageGatewayService;

@Reference
private MessageGateway<Email> emailService;

private void sendEmail(String recipient, String subject, String body) {
    Email email = new HtmlEmail();
    email.setSubject(subject);
    email.setMsg(body);
    
    try {
        emailService.send(email);
    } catch (Exception e) {
        LOG.error("Failed to send email", e);
    }
}
```

### Asset Processing Pattern

```java
private void processAsset(Resource assetResource) {
    Asset asset = assetResource.adaptTo(Asset.class);
    if (asset == null) {
        return;
    }
    
    // Get original rendition
    Rendition original = asset.getOriginal();
    
    // Process renditions
    for (Rendition rendition : asset.getRenditions()) {
        // Custom rendition processing
    }
    
    // Update metadata
    ModifiableValueMap metadata = assetResource.adaptTo(ModifiableValueMap.class);
    metadata.put("processedDate", new Date());
}
```

## Troubleshooting

### Workflow Administration

**Workflow Console Locations:**
- **Models**: `/libs/cq/workflow/admin/console/content/models.html`
- **Instances**: `/libs/cq/workflow/admin/console/content/instances.html`
- **Launchers**: `/libs/cq/workflow/admin/console/content/launchers.html`
- **Archive**: `/libs/cq/workflow/admin/console/content/archive.html`
- **Failures**: `/libs/cq/workflow/admin/console/content/failures.html`

**Workflow Instance States:**
- `RUNNING` - Workflow is actively executing
- `COMPLETED` - Workflow finished successfully
- `ABORTED` - Workflow was terminated
- `SUSPENDED` - Workflow is paused (can be resumed)
- `STALE` - Workflow progression failed (background job issue)

**Managing Workflow Instances:**

```java
// Suspend a running workflow
workflowSession.suspendWorkflow(workflow);

// Resume a suspended workflow
workflowSession.resumeWorkflow(workflow);

// Terminate a workflow
workflowSession.terminateWorkflow(workflow);

// Restart a workflow (creates new instance with same payload)
Workflow newWorkflow = workflowSession.restartWorkflow(workflow);
```

**Workflow Purging Configuration:**

Configure automatic cleanup of old workflow instances via OSGi:

```
PID: com.adobe.granite.workflow.purge.Scheduler

scheduledpurge.name = "Daily Completed Workflow Purge"
scheduledpurge.workflowStatus = "COMPLETED"
scheduledpurge.modelIds = ["/var/workflow/models/custom-workflow"]
scheduledpurge.daysold = 30
scheduledpurge.webjobs = false
```

**Properties:**
- `scheduledpurge.name` - Name for this purge configuration
- `scheduledpurge.workflowStatus` - Which status to purge (COMPLETED, ABORTED)
- `scheduledpurge.modelIds` - Specific models, or empty for all
- `scheduledpurge.daysold` - Days since completion before purging
- `scheduledpurge.webjobs` - Include web.jobs workflows

### Workflow Failure Handling

When workflows fail, use the Failures console:

**Available Actions:**
- **Failure Details** - View error message, step, and stack trace
- **Open History** - See complete workflow execution history
- **Retry Step** - Re-execute failed step after fixing cause
- **Terminate** - End workflow if error is unrecoverable
- **Terminate and Retry** - End and start new instance with same payload

**Logging Workflow Issues:**

```java
@Override
public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap args) 
        throws WorkflowException {
    
    try {
        // Workflow logic
        
    } catch (Exception e) {
        // Log detailed context for troubleshooting
        LOG.error("Workflow failed for payload: {}, model: {}, step: {}", 
            workItem.getWorkflowData().getPayload(),
            workItem.getWorkflow().getWorkflowModel().getId(),
            workItem.getNode().getTitle(),
            e);
        
        // Add failure context to workflow metadata
        workItem.getWorkflow().getMetaDataMap().put("failureReason", e.getMessage());
        workItem.getWorkflow().getMetaDataMap().put("failureTimestamp", new Date());
        
        throw new WorkflowException("Processing failed", e);
    }
}
```

### Common Issues

**Issue: "Process implementation not found"**
- Verify OSGi bundle is active
- Check `@Component` annotation has correct `service = WorkflowProcess.class`
- Ensure `process.label` property is set

**Issue: Cannot adapt WorkflowSession**
- Ensure using `com.adobe.granite.workflow.WorkflowSession`
- Not `com.day.cq.workflow.WorkflowSession` (deprecated)

**Issue: Workflow hangs or doesn't progress**
- Check for exceptions in logs
- Verify workflow has proper routes configured
- Ensure participant steps have assigned users/groups

**Issue: Java version mismatch**
- Ensure project uses Java 21 for AEM Cloud Service 2025.x
- Update `.cloudmanager/java-version` to `21`
- Update Maven compiler settings

**Issue: Workflow payload is null or incorrect**
- Verify payload type matches expected format (JCR_PATH vs JCR_UUID)
- Check if resource still exists at payload path
- Ensure launcher configuration matches node type

### Multi-Resource Support

Handle workflows that process multiple resources:

**Enable Multi-Resource Support:**
```java
// Workflow model metadata
workflowModel.getMetaDataMap().put("multiResourceSupport", true);
```

**Processing Multiple Resources:**
```java
@Override
public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap args) 
        throws WorkflowException {
    
    String payloadPath = workItem.getWorkflowData().getPayload().toString();
    ResourceResolver resolver = wfSession.adaptTo(ResourceResolver.class);
    
    // Check if this is a workflow package (multiple resources)
    Resource resource = resolver.getResource(payloadPath);
    
    if (resource != null && resource.isResourceType("cq/workflow/components/collection")) {
        // This is a workflow package - process all contained resources
        Resource collectionContent = resource.getChild("list");
        if (collectionContent != null) {
            for (Resource child : collectionContent.getChildren()) {
                String path = child.getValueMap().get("path", String.class);
                if (path != null) {
                    processResource(resolver.getResource(path));
                }
            }
        }
    } else {
        // Single resource
        processResource(resource);
    }
}

private void processResource(Resource resource) {
    // Process individual resource
    LOG.info("Processing resource: {}", resource.getPath());
}
```

## Content Fragment Workflows with UI Extensibility

AEM as a Cloud Service introduces **UI Extensibility** for integrating workflows with the Content Fragment Editor. This modern approach uses App Builder extensions instead of traditional AEM overlays.

### Overview

Content Fragment workflows allow authors to:
- Trigger approval workflows from the CF Editor
- Submit content for review without leaving the editor
- Track workflow status inline

### App Builder Extension Setup

**1. Create Extension Project:**

```bash
aio app init my-cf-workflow-extension --template @adobe/aem-cf-editor-ui-ext-tpl
```

**2. Register Action Bar Button:**

```javascript
// src/aem-cf-editor-1/web-src/src/components/ExtensionRegistration.js
import { register } from "@adobe/uix-guest";

function ExtensionRegistration() {
  const init = async () => {
    const guestConnection = await register({
      id: "cf-workflow-extension",
      methods: {
        actionBar: {
          getButtons() {
            return [
              {
                id: "start-approval-workflow",
                label: "Submit for Approval",
                icon: "Workflow",
                onClick: async () => {
                  const contentFragment = await guestConnection.host.contentFragment.getContentFragment();
                  // Trigger workflow via AEM API
                  await startWorkflow(contentFragment.path);
                }
              }
            ];
          }
        }
      }
    });
  };

  init().catch(console.error);
  return <></>;
}
```

**3. Backend Action to Start Workflow:**

```javascript
// src/aem-cf-editor-1/actions/start-workflow/index.js
const { Core } = require("@adobe/aio-sdk");
const { getAEMAccessToken } = require("../utils");

async function main(params) {
  const { contentFragmentPath, workflowModel } = params;

  const accessToken = await getAEMAccessToken(params);
  const aemHost = params.aemHost;

  // Start workflow via AEM REST API
  const response = await fetch(`${aemHost}/etc/workflow/instances`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({
      model: workflowModel || "/var/workflow/models/request_for_activation",
      payloadType: "JCR_PATH",
      payload: contentFragmentPath
    })
  });

  return {
    statusCode: response.ok ? 200 : response.status,
    body: await response.json()
  };
}

exports.main = main;
```

### Extension Configuration

**app.config.yaml:**

```yaml
extensions:
  aem/cf-editor/1:
    $include: src/aem-cf-editor-1/ext.config.yaml
    operations:
      view:
        - type: actionBar
          impl: ExtensionRegistration
```

### Deployment

```bash
# Deploy to Stage
aio app deploy

# Deploy to Production
aio app deploy --target production
```

## App Builder Extensions for Workflow UI

Beyond Content Fragments, App Builder enables custom workflow UI across AEM:

### Custom Inbox Extensions

Create custom views and actions in the AEM Inbox:

```javascript
// Custom inbox action
const inboxExtension = {
  id: "custom-bulk-approve",
  label: "Bulk Approve",
  icon: "CheckmarkCircle",
  onClick: async (selectedItems) => {
    for (const item of selectedItems) {
      await completeWorkItem(item.workItemId, "approve");
    }
  }
};
```

### External Workflow Dashboard

Build standalone dashboards that integrate with AEM workflows:

```javascript
// Fetch workflow instances
async function getWorkflowInstances(status = "RUNNING") {
  const response = await fetch(
    `${aemHost}/libs/cq/workflow/content/console/content/instances.json?status=${status}`,
    { headers: { Authorization: `Bearer ${accessToken}` } }
  );
  return response.json();
}

// Display in React component
function WorkflowDashboard() {
  const [instances, setInstances] = useState([]);

  useEffect(() => {
    getWorkflowInstances().then(setInstances);
  }, []);

  return (
    <Table>
      {instances.map(wf => (
        <Row key={wf.id}>
          <Cell>{wf.payload}</Cell>
          <Cell>{wf.state}</Cell>
          <Cell>{wf.currentAssignee}</Cell>
        </Row>
      ))}
    </Table>
  );
}
```

## Sling Jobs for Long-Running Processes

For operations that exceed workflow step timeouts or require guaranteed execution, use **Sling Jobs** instead of inline workflow processing.

### Why Use Sling Jobs?

- **Persistence**: Jobs survive container restarts
- **Retry Logic**: Built-in retry with configurable backoff
- **Scalability**: Distributed processing across cluster
- **Monitoring**: Job status tracking and reporting

### Creating a Job Consumer

```java
package com.example.core.jobs;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = JobConsumer.class,
    property = {
        JobConsumer.PROPERTY_TOPICS + "=com/example/workflow/longrunning"
    }
)
public class LongRunningJobConsumer implements JobConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(LongRunningJobConsumer.class);

    @Override
    public JobResult process(Job job) {
        String assetPath = job.getProperty("assetPath", String.class);
        String workflowId = job.getProperty("workflowId", String.class);

        LOG.info("Processing long-running job for: {}", assetPath);

        try {
            // Perform time-intensive operations
            performHeavyProcessing(assetPath);

            // Optionally: Signal workflow to continue
            if (workflowId != null) {
                signalWorkflowCompletion(workflowId);
            }

            return JobResult.OK;

        } catch (Exception e) {
            LOG.error("Job processing failed", e);
            // Return FAILED to trigger retry based on job configuration
            return JobResult.FAILED;
        }
    }

    private void performHeavyProcessing(String path) {
        // Video transcoding, large file processing, external API calls, etc.
    }

    private void signalWorkflowCompletion(String workflowId) {
        // Update workflow metadata or advance to next step
    }
}
```

### Workflow Step that Creates Jobs

```java
package com.example.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Async Job Dispatcher"
    }
)
public class AsyncJobDispatcherProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncJobDispatcherProcess.class);
    private static final String JOB_TOPIC = "com/example/workflow/longrunning";

    @Reference
    private JobManager jobManager;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payload = workItem.getWorkflowData().getPayload().toString();
        String workflowId = workItem.getWorkflow().getId();

        // Create job properties
        Map<String, Object> jobProps = new HashMap<>();
        jobProps.put("assetPath", payload);
        jobProps.put("workflowId", workflowId);
        jobProps.put("initiator", workItem.getWorkflow().getInitiator());

        // Add job to queue
        org.apache.sling.event.jobs.Job job = jobManager.addJob(JOB_TOPIC, jobProps);

        if (job != null) {
            LOG.info("Created async job {} for workflow {}", job.getId(), workflowId);

            // Store job ID in workflow metadata for tracking
            workItem.getWorkflow().getMetaDataMap().put("asyncJobId", job.getId());
        } else {
            throw new WorkflowException("Failed to create async job");
        }
    }
}
```

### Job Configuration (OSGi)

```
PID: org.apache.sling.event.jobs.QueueConfiguration~longrunning

queue.name = "Long Running Workflow Jobs"
queue.topics = ["com/example/workflow/longrunning"]
queue.type = "ORDERED"
queue.maxparallel = 3
queue.retries = 5
queue.retrydelay = 60000
queue.priority = "NORM"
```

## Multi-Step Approval Workflow Pattern

Complex approval workflows with multiple stages, escalation, and conditional routing.

### Workflow Model Structure

```
[Start] → [Initial Review] → [Department Approval] → [Final Approval] → [Publish] → [End]
                ↓                     ↓                      ↓
            [Reject]              [Reject]              [Reject]
                ↓                     ↓                      ↓
            [Notify Author] ← ← ← ← ←
```

### Hierarchical Participant Chooser

```java
package com.example.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Iterator;

@Component(
    service = ParticipantStepChooser.class,
    property = {
        "chooser.label=Hierarchical Approval Chooser"
    }
)
public class HierarchicalApprovalChooser implements ParticipantStepChooser {

    private static final Logger LOG = LoggerFactory.getLogger(HierarchicalApprovalChooser.class);

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession,
                                 MetaDataMap metaDataMap) throws WorkflowException {

        try {
            ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
            UserManager userManager = resolver.adaptTo(UserManager.class);

            // Get current approval level from workflow metadata
            MetaDataMap wfMetadata = workItem.getWorkflow().getMetaDataMap();
            int approvalLevel = wfMetadata.get("approvalLevel", 1);

            // Get content path for department routing
            String payloadPath = workItem.getWorkflowData().getPayload().toString();
            String department = extractDepartment(payloadPath);

            // Determine approver based on level and department
            String approverGroup = getApproverGroup(approvalLevel, department);

            // Increment approval level for next step
            wfMetadata.put("approvalLevel", approvalLevel + 1);

            LOG.info("Routing to {} for level {} approval", approverGroup, approvalLevel);
            return approverGroup;

        } catch (Exception e) {
            LOG.error("Failed to determine approver", e);
            return "administrators"; // Fallback
        }
    }

    private String extractDepartment(String path) {
        // Extract department from content path
        // e.g., /content/mysite/marketing/page → marketing
        String[] segments = path.split("/");
        if (segments.length > 3) {
            return segments[3];
        }
        return "default";
    }

    private String getApproverGroup(int level, String department) {
        switch (level) {
            case 1:
                return department + "-reviewers";      // marketing-reviewers
            case 2:
                return department + "-managers";       // marketing-managers
            case 3:
                return "content-governance";           // Final approval
            default:
                return "administrators";
        }
    }
}
```

### Escalation Handler

```java
package com.example.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Escalation Check Process"
    }
)
public class EscalationCheckProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(EscalationCheckProcess.class);
    private static final long ESCALATION_THRESHOLD_HOURS = 48;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        MetaDataMap wfMetadata = workItem.getWorkflow().getMetaDataMap();

        // Get step start time
        Date stepStartTime = wfMetadata.get("currentStepStartTime", Date.class);
        if (stepStartTime == null) {
            stepStartTime = new Date();
            wfMetadata.put("currentStepStartTime", stepStartTime);
            return;
        }

        // Check if escalation needed
        long hoursElapsed = TimeUnit.MILLISECONDS.toHours(
            System.currentTimeMillis() - stepStartTime.getTime()
        );

        if (hoursElapsed >= ESCALATION_THRESHOLD_HOURS) {
            LOG.warn("Workflow {} exceeded {} hour threshold, escalating",
                workItem.getWorkflow().getId(), ESCALATION_THRESHOLD_HOURS);

            // Mark for escalation - workflow model routes based on this
            wfMetadata.put("escalated", true);
            wfMetadata.put("escalationReason", "Approval timeout exceeded " + ESCALATION_THRESHOLD_HOURS + " hours");

            // Send escalation notification
            sendEscalationNotification(workItem);
        }
    }

    private void sendEscalationNotification(WorkItem workItem) {
        // Implement email/notification logic
        LOG.info("Sending escalation notification for workflow: {}",
            workItem.getWorkflow().getId());
    }
}
```

### Approval Status Tracking

```java
// Track approval history in workflow metadata
private void recordApprovalDecision(WorkItem workItem, String approver,
                                     String decision, String comments) {
    MetaDataMap metadata = workItem.getWorkflow().getMetaDataMap();

    // Build approval history
    String historyKey = "approvalHistory";
    String existingHistory = metadata.get(historyKey, "");

    String newEntry = String.format("[%s] %s: %s by %s - %s",
        new Date(),
        workItem.getNode().getTitle(),
        decision,
        approver,
        comments
    );

    metadata.put(historyKey, existingHistory + "\n" + newEntry);
    metadata.put("lastApprover", approver);
    metadata.put("lastDecision", decision);
    metadata.put("lastDecisionTime", new Date());
}
```

## Reference Documentation

For detailed API documentation, see [references/workflow-api-reference.md](references/workflow-api-reference.md)

For code examples and templates, see [scripts/](scripts/)

## Additional Resources

- AEM SDK JavaDoc: https://javadoc.io/doc/com.adobe.aem/aem-sdk-api/latest/
- AEM Cloud Service Documentation: https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service
- Granite Workflow Package: `com.adobe.granite.workflow`
- Workflow Execution Package: `com.adobe.granite.workflow.exec`
- Workflow Model Package: `com.adobe.granite.workflow.model`
