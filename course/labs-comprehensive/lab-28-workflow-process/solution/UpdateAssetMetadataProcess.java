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

import java.util.Calendar;

@Component(
    service = WorkflowProcess.class,
    property = { "process.label=Update Asset Metadata" }
)
public class UpdateAssetMetadataProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateAssetMetadataProcess.class);

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, 
            MetaDataMap processArgs) throws WorkflowException {
        
        WorkflowData workflowData = workItem.getWorkflowData();
        String payloadPath = workflowData.getPayload().toString();
        
        LOG.info("Processing: {}", payloadPath);
        
        ResourceResolver resolver = workflowSession.getSession().getResourceResolver();
        
        try {
            Resource resource = resolver.getResource(payloadPath);
            
            if (resource == null) {
                LOG.warn("Resource not found: {}", payloadPath);
                return;
            }
            
            Resource contentResource = resource.getChild("jcr:content");
            
            if (contentResource == null) {
                contentResource = resource;
            }
            
            ModifiableValueMap properties = contentResource.adaptTo(ModifiableValueMap.class);
            
            if (properties != null) {
                properties.put("workflowStatus", "processed");
                properties.put("processedBy", "UpdateAssetMetadataProcess");
                properties.put("processedDate", Calendar.getInstance());
                
                resolver.commit();
                
                LOG.info("Metadata updated successfully for: {}", payloadPath);
            }
            
        } catch (Exception e) {
            LOG.error("Error updating metadata", e);
            throw new WorkflowException("Failed to update asset metadata", e);
        }
    }
}
