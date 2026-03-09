# Lab 28: AEM Workflow Process Steps - Expert Level

## Objective
Implement actual AEM Workflow Process Steps - the core of AEM workflow automation.

---

## What You'll Learn
- WorkflowProcess interface
- MetaDataMap for workflow data
- Process arguments (CQ Dialog)
- WorkflowSession management
- Payload handling
- Dynamic participant assignment
- Error handling

---

## Part 1: Understanding AEM Workflows

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    AEM WORKFLOW ARCHITECTURE                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │   Launcher   │───▶│    Model     │───▶│   Instance  │       │
│  │  (Trigger)   │    │  (Flow)     │    │  (Running)  │       │
│  └──────────────┘    └──────────────┘    └──────────────┘       │
│        │                   │                   │                │
│        ▼                   ▼                   ▼                │
│  ┌──────────────────────────────────────────────────────┐       │
│  │                  WORKFLOW STEPS                       │       │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐          │       │
│  │  │Process   │  │Partici-  │  │   OR     │          │       │
│  │  │Step      │  │pant Step  │  │   Split  │          │       │
│  │  └──────────┘  └──────────┘  └──────────┘          │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Types of Steps

| Step Type | Purpose | Implementation |
|-----------|---------|----------------|
| Process | Automated execution | `WorkflowProcess` |
| Participant | Human decision | N/A (config only) |
| OR Split | Conditional branch | N/A (config only) |
| AND Split | Parallel execution | N/A (config only) |
| Participant Chooser | Dynamic assignment | `ParticipantStepChooser` |

---

## Part 2: Implementing WorkflowProcess

### Exercise 1: Basic Process Step

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.WorkflowSession;
import com.adobe.granite.exec.WorkflowProcess;
import com.adobe.granite.jcr.annotation.Property;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite/workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;

/**
 * Basic process step that logs workflow information.
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Log Workflow Info"
    }
)
public class LogWorkflowInfoProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(LogWorkflowInfoProcess.class);

    @Property(name = "PROCESS_LABEL", value = "Log Workflow Info")
    private String processLabel;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap metaDataMap) throws WorkflowException {
        
        try {
            WorkflowData workflowData = workItem.getWorkflowData();
            String payloadPath = workflowData.getPayload().toString();
            String workflowModelId = workItem.getWorkflow().getModel().getId();
            String currentStepTitle = workItem.getCurrentStep().getTitle();
            
            LOG.info("=== Workflow Execution ===");
            LOG.info("Payload: {}", payloadPath);
            LOG.info("Model: {}", workflowModelId);
            LOG.info("Current Step: {}", currentStepTitle);
            LOG.info("=========================");
            
            // Get workflow metadata
            MetaDataMap workflowMeta = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
            String initiator = workflowMeta.get("userId", String.class);
            LOG.info("Initiator: {}", initiator);
            
        } catch (Exception e) {
            LOG.error("Error executing workflow process", e);
            throw new WorkflowException("Failed to log workflow info", e);
        }
    }
}
```

### Exercise 2: Asset Metadata Update Process

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.WorkflowSession;
import com.adobe.granite.exec.WorkflowProcess;
import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

/**
 * Updates asset metadata during workflow execution.
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Update Asset Metadata"
    }
)
public class UpdateAssetMetadataProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateAssetMetadataProcess.class);
    
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap processArgs) throws WorkflowException {
        
        WorkflowData workflowData = workItem.getWorkflowData();
        String payloadPath = workflowData.getPayload().toString();
        
        LOG.info("Updating metadata for: {}", payloadPath);
        
        // Get arguments from workflow dialog
        String status = processArgs.get("status", "pending-review");
        String category = processArgs.get("category", "general");
        boolean notify = processArgs.get("notifyAuthor", true);
        
        ResourceResolver resolver = workflowSession.getSession().getResourceResolver();
        
        try {
            Resource resource = resolver.getResource(payloadPath);
            if (resource == null) {
                LOG.warn("Resource not found: {}", payloadPath);
                return;
            }
            
            // Check if it's a content resource (not the jcr:content node)
            Resource contentResource = resource.getChild("jcr:content");
            if (contentResource == null) {
                contentResource = resource;
            }
            
            // Update metadata
            ModifiableValueMap properties = contentResource.adaptTo(ModifiableValueMap.class);
            if (properties != null) {
                properties.put("workflowStatus", status);
                properties.put("workflowCategory", category);
                properties.put("workflowProcessed", Calendar.getInstance());
                properties.put("workflowProcess", workItem.getWorkflow().getModel().getId());
                
                if (notify) {
                    properties.put("notifyOnComplete", true);
                }
                
                resolver.commit();
                LOG.info("Metadata updated successfully for: {}", payloadPath);
            }
            
        } catch (RepositoryException e) {
            LOG.error("Error updating metadata", e);
            throw new WorkflowException("Failed to update asset metadata", e);
        } catch (PersistenceException e) {
            LOG.error("Error persisting changes", e);
            throw new WorkflowException("Failed to save changes", e);
        }
    }
}
```

---

## Part 3: Antivirus Scan Process Step

### Exercise 3: Real Antivirus Process

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.WorkflowSession;
import com.adobe.granite.exec.WorkflowProcess;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;

import javax.jcr.Node;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Scans assets for viruses during upload workflow.
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Antivirus Scan"
    }
)
public class AntivirusScanProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(AntivirusScanProcess.class);
    
    @Reference
    private AntivirusService antivirusService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap processArgs) throws WorkflowException {
        
        WorkflowData workflowData = workItem.getWorkflowData();
        String payloadPath = workflowData.getPayload().toString();
        
        LOG.info("Starting antivirus scan for: {}", payloadPath);
        
        ResourceResolver resolver = workflowSession.getSession().getResourceResolver();
        
        try {
            Resource resource = resolver.getResource(payloadPath);
            if (resource == null) {
                throw new WorkflowException("Payload resource not found: " + payloadPath);
            }
            
            // Check if it's a DAM asset
            Resource contentResource = resource.getChild("jcr:content");
            if (contentResource == null) {
                LOG.info("Not a DAM asset, skipping scan");
                return;
            }
            
            // Get the original file (jcr:content/renditions/original)
            Resource originalRendition = contentResource.getChild("renditions/original");
            if (originalRendition == null) {
                LOG.info("No original rendition found, checking jcr:content");
                originalRendition = contentResource;
            }
            
            // Get file binary
            Node contentNode = originalRendition.adaptTo(Node.class);
            if (contentNode == null || !contentNode.hasProperty("jcr:data")) {
                LOG.warn("No binary data found for: {}", payloadPath);
                return;
            }
            
            // Perform scan
            InputStream inputStream = contentNode.getProperty("jcr:data").getBinary().getStream();
            AntivirusScanService.ScanResult result = antivirusService.scan(
                inputStream, 
                resource.getName()
            );
            
            // Update metadata with scan result
            ModifiableValueMap metadata = contentResource.adaptTo(ModifiableValueMap.class);
            
            switch (result.getStatus()) {
                case CLEAN:
                    metadata.put("avScanStatus", "clean");
                    metadata.put("avScanDate", Calendar.getInstance());
                    LOG.info("Asset is clean: {}", payloadPath);
                    break;
                    
                case INFECTED:
                    metadata.put("avScanStatus", "infected");
                    metadata.put("avScanThreat", result.getThreatName());
                    metadata.put("avScanDate", Calendar.getInstance());
                    
                    // Mark for quarantine
                    metadata.put("quarantineRequired", true);
                    LOG.warn("INFECTED: {} - Threat: {}", payloadPath, result.getThreatName());
                    break;
                    
                case ERROR:
                    metadata.put("avScanStatus", "error");
                    metadata.put("avScanError", "Scanner unavailable");
                    LOG.error("Scan error for: {}", payloadPath);
                    break;
                    
                default:
                    LOG.warn("Unknown scan status for: {}", payloadPath);
            }
            
            resolver.commit();
            
        } catch (Exception e) {
            LOG.error("Error during antivirus scan", e);
            throw new WorkflowException("Antivirus scan failed", e);
        }
    }
}
```

---

## Part 4: Dynamic Participant Assignment

### Exercise 4: Participant Step Chooser

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowSession;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamically assigns workflow to appropriate reviewer based on asset metadata.
 */
@Component(
    service = ParticipantStepChooser.class,
    property = {
        "chooser.label=Asset Review Assigner"
    }
)
public class AssetReviewAssigner implements ParticipantStepChooser {

    private static final Logger LOG = LoggerFactory.getLogger(AssetReviewAssigner.class);

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap metaDataMap) {
        
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Determining assignee for: {}", payloadPath);
        
        ResourceResolver resolver = workflowSession.getSession().getResourceResolver();
        
        try {
            Resource resource = resolver.getResource(payloadPath);
            if (resource == null) {
                return "admin";
            }
            
            Resource content = resource.getChild("jcr:content");
            if (content == null) {
                return "admin";
            }
            
            // Get asset metadata
            String assetType = content.getValueMap().get("dc:format", String.class);
            String department = content.getValueMap().get("department", String.class);
            String urgency = content.getValueMap().get("urgency", String.class);
            
            // Determine reviewer based on rules
            List<String> candidates = new ArrayList<>();
            
            // Department-based assignment
            if ("marketing".equalsIgnoreCase(department)) {
                candidates.add("group-marketing-reviewers");
            } else if ("legal".equalsIgnoreCase(department)) {
                candidates.add("group-legal-reviewers");
            } else if ("technical".equalsIgnoreCase(department)) {
                candidates.add("group-technical-reviewers");
            } else {
                candidates.add("group-general-reviewers");
            }
            
            // Urgency override
            if ("high".equalsIgnoreCase(urgency)) {
                // Add manager as backup
                candidates.add("group-managers");
            }
            
            // Return primary reviewer
            String assignee = candidates.get(0);
            LOG.info("Assigned to: {}", assignee);
            return assignee;
            
        } catch (Exception e) {
            LOG.error("Error determining assignee", e);
            return "admin"; // Fallback
        }
    }
}
```

---

## Part 5: Process with Dialog Options

### Exercise 5: Process with Arguments

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.WorkflowSession;
import com.adobe.granite.exec.WorkflowProcess;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;

import java.util.Calendar;

/**
 * Process step with configurable options.
 * 
 * Dialog configuration (in workflow model):
 * - process.label: Label shown in workflow
 * - example:property: Custom property
 * - example:mode: Processing mode
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Process Asset"
    }
)
public class ProcessAssetWithOptions implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessAssetWithOptions.class);

    // Argument names must match the dialog configuration
    private static final String ARG_PROCESS_TYPE = "processType";
    private static final String ARG_GENERATE_RENDITIONS = "generateRenditions";
    private static final String ARG_PUBLISH_AFTER = "publishAfterProcessing";
    private static final String ARG_NOTIFY_ON_COMPLETE = "notifyOnComplete";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap processArgs) throws WorkflowException {
        
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Processing asset: {} with options", payloadPath);
        
        // Get process arguments from workflow dialog
        String processType = processArgs.get(ARG_PROCESS_TYPE, "standard");
        boolean generateRenditions = processArgs.get(ARG_GENERATE_RENDITIONS, true);
        boolean publishAfter = processArgs.get(ARG_PUBLISH_AFTER, false);
        boolean notifyOnComplete = processArgs.get(ARG_NOTIFY_ON_COMPLETE, true);
        
        LOG.info("Process Type: {}", processType);
        LOG.info("Generate Renditions: {}", generateRenditions);
        LOG.info("Publish After: {}", publishAfter);
        LOG.info("Notify: {}", notifyOnComplete);
        
        ResourceResolver resolver = workflowSession.getSession().getResourceResolver();
        
        try {
            Resource resource = resolver.getResource(payloadPath);
            if (resource == null) {
                return;
            }
            
            Resource content = resource.getChild("jcr:content");
            if (content == null) {
                return;
            }
            
            ModifiableValueMap metadata = content.adaptTo(ModifiableValueMap.class);
            
            // Apply processing based on arguments
            metadata.put("processedBy", "ProcessAssetWithOptions");
            metadata.put("processType", processType);
            metadata.put("processedDate", Calendar.getInstance());
            metadata.put("generateRenditions", generateRenditions);
            metadata.put("publishAfterProcessing", publishAfter);
            metadata.put("notifyOnComplete", notifyOnComplete);
            
            resolver.commit();
            LOG.info("Asset processed successfully");
            
        } catch (Exception e) {
            LOG.error("Error processing asset", e);
            throw new WorkflowException("Processing failed", e);
        }
    }
}
```

---

## Part 6: Testing Workflow Processes

### Exercise 6: Unit Test

```java
package com.demo.workflow.process;

import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.WorkflowSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class UpdateAssetMetadataProcessSpec {

    private AemContext context;
    
    @Mock
    private WorkflowSession workflowSession;
    
    @Mock
    private WorkItem workItem;
    
    @Mock
    private WorkflowData workflowData;
    
    @Mock
    private MetaDataMap metaDataMap;
    
    @BeforeEach
    void setUp() {
        context = new AemContext();
        
        // Mock workflow data
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/dam/test.png");
    }
    
    @Test
    void shouldUpdateMetadata() throws Exception {
        // Given
        Resource resource = context.create().resource("/content/dam/test.png",
            "jcr:primaryType", "dam:Asset");
        Resource content = context.create().resource("/content/dam/test.png/jcr:content",
            "jcr:primaryType", "dam:AssetContent");
        
        // When
        UpdateAssetMetadataProcess process = new UpdateAssetMetadataProcess();
        // Note: In real test, you'd need to mock the service reference
        
        // Then - verify behavior
        assertNotNull(process);
    }
}
```

---

## Workflow Model XML

### Example Workflow Model

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:cq="http://www.day.com/jcr/cq/1.0"
    jcr:primaryType="cq:WorkflowModel"
    jcr:title="Secure Asset Approval"
    sling:resourceType="cq/workflow/model">

    <externalProcess1
        jcr:primaryType="cq:WorkflowProcess"
        process="com.demo.workflow.process.AntivirusScanProcess"
        processLabel="Antivirus Scan"/>

    <participant1
        jcr:primaryType="cq:Participant"
        assigneeType="user"
        participant="admin"
        stepName="Review Asset"
        stepTitle="Review and Approve"/>

    <process2
        jcr:primaryType="cq:WorkflowProcess"
        process="com.demo.workflow.process.PublishAssetProcess"
        process.label="Publish Asset"
        processType="publish"
        generateRenditions="true"/>

</jcr:root>
```

---

## Best Practices

| Practice | Recommendation |
|----------|----------------|
| Error Handling | Always wrap in try-catch, throw WorkflowException |
| Logging | Use SLF4J Logger |
| Session Management | Don't leak sessions |
| Payload Validation | Always check if payload exists |
| Transaction | Use resolver.commit() properly |
| Testing | Use AEM Context mocks |

---

## Verification Checklist

- [ ] Basic process step created
- [ ] Process with metadata updates
- [ ] Antivirus scan integration
- [ ] Dynamic participant chooser
- [ ] Process with dialog arguments
- [ ] Workflow model XML
- [ ] Unit test written

---

## Next Steps

- Lab 29: AEM Context Testing
- Lab 30: Content Fragment Models

---

## References

- [Workflow Process API](https://developer.adobe.com/experience-manager/reference-materials/6-5/granite-ui/javadoc/com/adobe/granite/exec/WorkflowProcess.html)
- [Workflow JavaDocs](https://developer.adobe.com/experience-manager/reference-materials/6-5/javadoc/com/adobe/granite/workflow/package-summary.html)
