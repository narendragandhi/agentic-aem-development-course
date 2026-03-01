package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.demo.workflow.services.LlmService;
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
        "process.label=Summarize Content Process (LLM)"
    }
)
public class SummarizeContentProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(SummarizeContentProcess.class);

    @Reference
    private LlmService llmService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        String payloadPath = workItem.getWorkflowData().getPayload().toString();

        try {
            ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
            if (resolver == null) {
                throw new WorkflowException("ResourceResolver is null");
            }
            Session jcrSession = resolver.adaptTo(Session.class);
            if (jcrSession == null) {
                throw new WorkflowException("JCR Session is null");
            }

            String contentPath = payloadPath + "/jcr:content";
            if (jcrSession.nodeExists(contentPath)) {
                Node contentNode = jcrSession.getNode(contentPath);
                if (contentNode.hasProperty("jcr:description")) {
                    String description = contentNode.getProperty("jcr:description").getString();
                    log.info("Found description to summarize for payload {}", payloadPath);

                    String summary = llmService.summarizeText(description);

                    if (summary != null && !summary.isEmpty()) {
                        contentNode.setProperty("summary", summary);
                        jcrSession.save();
                        log.info("Saved summary to 'summary' property on node {}", contentPath);
                    } else {
                        log.warn("LLM service returned an empty summary.");
                    }
                } else {
                    log.warn("Payload {} does not have a 'jcr:description' property to summarize.", payloadPath);
                }
            } else {
                log.warn("Payload {} does not have a jcr:content node.", payloadPath);
            }
        } catch (RepositoryException e) {
            throw new WorkflowException("Failed to process payload " + payloadPath, e);
        }
    }
}
