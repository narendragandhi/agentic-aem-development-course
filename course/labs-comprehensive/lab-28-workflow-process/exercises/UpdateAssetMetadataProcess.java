package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.WorkflowSession;
import com.adobe.granite.exec.WorkflowProcess;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;

import javax.jcr.Node;
import java.util.Calendar;

/**
 * Exercise: Implement a Process Step that updates asset metadata
 * 
 * TODO: Complete the implementation to:
 * 1. Get the payload path from workflow data
 * 2. Get the resource from the path
 * 3. Update metadata properties on the jcr:content node
 * 4. Add properties:
 *    - "workflowStatus" = "processed"
 *    - "processedBy" = workflow process name
 *    - "processedDate" = current time
 * 5. Commit the changes
 * 
 * Use the following structure:
 * - workItem.getWorkflowData().getPayload() to get payload path
 * - workflowSession.getSession().getResourceResolver() to get resolver
 * - resolver.getResource(payloadPath) to get resource
 * - resource.getChild("jcr:content") to get content node
 * - content.adaptTo(ModifiableValueMap.class) to get properties
 * - resolver.commit() to save
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Update Asset Metadata"
    }
)
public class UpdateAssetMetadataProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateAssetMetadataProcess.class);

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap processArgs) throws WorkflowException {
        
        // TODO: Get workflow data and payload
        WorkflowData workflowData = workItem.getWorkflowData();
        String payloadPath = null; // TODO: Get payload path
        
        LOG.info("Processing: {}", payloadPath);
        
        // TODO: Get ResourceResolver from session
        ResourceResolver resolver = null; // TODO: Get resolver
        
        try {
            // TODO: Get resource from path
            Resource resource = null; // TODO: Get resource
            
            if (resource == null) {
                LOG.warn("Resource not found: {}", payloadPath);
                return;
            }
            
            // TODO: Get jcr:content child
            Resource contentResource = null; // TODO: Get content
            
            if (contentResource == null) {
                LOG.info("No jcr:content, using resource directly");
                contentResource = resource;
            }
            
            // TODO: Get ModifiableValueMap and add properties
            ModifiableValueMap properties = null; // TODO: Get modifiable map
            
            if (properties != null) {
                // TODO: Add workflowStatus = "processed"
                // TODO: Add processedBy = "UpdateAssetMetadataProcess"
                // TODO: Add processedDate = Calendar.getInstance()
                
                // TODO: Commit changes
                // resolver.commit();
                
                LOG.info("Metadata updated successfully");
            }
            
        } catch (Exception e) {
            LOG.error("Error updating metadata", e);
            throw new WorkflowException("Failed to update asset metadata", e);
        }
    }
}
