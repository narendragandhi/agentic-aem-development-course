package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.demo.workflow.services.ExternalSystemIntegrationService;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=External API Integration Process"
    }
)
public class ExternalApiProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiProcess.class);
    private static final String METADATA_PROPERTY = "externalData";

    @Reference
    private ExternalSystemIntegrationService externalSystemIntegrationService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        String payloadPath = workItem.getWorkflowData().getPayload().toString();

        String externalData = externalSystemIntegrationService.getData(payloadPath);

        if (externalData != null) {
            try {
                ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
                if (resolver == null) {
                    throw new WorkflowException("ResourceResolver is null");
                }
                Session jcrSession = resolver.adaptTo(Session.class);
                if (jcrSession == null) {
                    throw new WorkflowException("JCR Session is null");
                }

                if (jcrSession.nodeExists(payloadPath + "/jcr:content/metadata")) {
                    Node metadataNode = jcrSession.getNode(payloadPath + "/jcr:content/metadata");
                    metadataNode.setProperty(METADATA_PROPERTY, externalData);
                    jcrSession.save();
                    log.info("Saved external data to {} on asset {}", METADATA_PROPERTY, payloadPath);
                } else {
                    log.warn("Payload {} does not have a metadata node.", payloadPath);
                }
            } catch (RepositoryException e) {
                throw new WorkflowException("Failed to write external data to payload " + payloadPath, e);
            }
        }
    }
}
