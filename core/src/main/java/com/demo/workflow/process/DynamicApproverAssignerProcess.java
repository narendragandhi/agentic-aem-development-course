package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Dynamic Approver Assigner Process"
    }
)
public class DynamicApproverAssignerProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(DynamicApproverAssignerProcess.class);

    private static final String FINANCE_APPROVERS_GROUP = "finance-approvers";
    private static final String DEFAULT_APPROVERS_GROUP = "content-approvers";
    private static final String APPROVER_GROUP_VARIABLE = "approverGroup";
    private static final String CATEGORY_PROPERTY = "category";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        WorkflowData workflowData = workItem.getWorkflowData();
        String payloadPath = workflowData.getPayload().toString();

        try {
            ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
            if (resolver == null) {
                throw new WorkflowException("ResourceResolver is null");
            }
            Session jcrSession = resolver.adaptTo(Session.class);
            if (jcrSession == null) {
                throw new WorkflowException("JCR Session is null");
            }

            if (jcrSession.nodeExists(payloadPath + "/jcr:content")) {
                Node contentNode = jcrSession.getNode(payloadPath + "/jcr:content");
                String approverGroup = DEFAULT_APPROVERS_GROUP;

                if (contentNode.hasProperty(CATEGORY_PROPERTY)) {
                    String category = contentNode.getProperty(CATEGORY_PROPERTY).getString();
                    log.info("Found category '{}' on payload {}", category, payloadPath);
                    if ("finance".equalsIgnoreCase(category)) {
                        approverGroup = FINANCE_APPROVERS_GROUP;
                    }
                } else {
                    log.info("No category property found on payload {}. Using default approver group.", payloadPath);
                }

                MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
                workflowMetadata.put(APPROVER_GROUP_VARIABLE, approverGroup);
                log.info("Set '{}' workflow variable to '{}'", APPROVER_GROUP_VARIABLE, approverGroup);
            } else {
                log.warn("Payload {} does not have a jcr:content node.", payloadPath);
            }
        } catch (RepositoryException e) {
            throw new WorkflowException("Failed to process payload " + payloadPath, e);
        }
    }
}
