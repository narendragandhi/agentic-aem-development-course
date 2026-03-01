package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Calendar;

/**
 * Replication Workflow Process
 *
 * Handles content replication with tracking and workflow initiator attribution.
 * Inspired by ACS AEM Commons "Replicated By Workflow Initiator" and
 * "Set Replication Status" processes.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                   REPLICATION PROCESS                           │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                  REPLICATION ACTIONS                    │  │
 * │   │                                                         │  │
 * │   │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │  │
 * │   │  │ ACTIVATE │  │DEACTIVATE│  │  DELETE  │  │  TEST   │ │  │
 * │   │  │ (Publish)│  │(Unpublish)│ │  (Remove)│  │ (Verify)│ │  │
 * │   │  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                   PROCESS FLOW                          │  │
 * │   │                                                         │  │
 * │   │   ┌─────────┐    ┌─────────┐    ┌─────────┐            │  │
 * │   │   │Validate │───▶│Replicate│───▶│  Track  │            │  │
 * │   │   │ Payload │    │ Content │    │ Status  │            │  │
 * │   │   └─────────┘    └─────────┘    └─────────┘            │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   Features:                                                    │
 * │   • Tracks workflow initiator as replicated by user           │
 * │   • Updates replication status metadata                       │
 * │   • Supports synchronous and asynchronous replication         │
 * │   • Configurable agent selection                              │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Replication Process",
        "process.description=Replicates content with initiator tracking"
    }
)
public class ReplicationProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicationProcess.class);

    private static final String ARG_REPLICATION_ACTION = "replicationAction";
    private static final String ARG_TRACK_INITIATOR = "trackInitiator";
    private static final String ARG_SYNCHRONOUS = "synchronous";
    private static final String ARG_SUPPRESS_VERSIONS = "suppressVersions";
    private static final String ARG_AGENT_ID = "agentId";

    @Reference
    private Replicator replicator;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting replication for: {}", payloadPath);

        // Get configuration
        String actionStr = metaDataMap.get(ARG_REPLICATION_ACTION, "activate");
        boolean trackInitiator = metaDataMap.get(ARG_TRACK_INITIATOR, true);
        boolean synchronous = metaDataMap.get(ARG_SYNCHRONOUS, true);
        boolean suppressVersions = metaDataMap.get(ARG_SUPPRESS_VERSIONS, false);
        String agentId = metaDataMap.get(ARG_AGENT_ID, "");

        ReplicationActionType actionType = parseActionType(actionStr);
        String initiator = workItem.getWorkflow().getInitiator();

        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Unable to obtain ResourceResolver");
        }

        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            throw new WorkflowException("Unable to obtain JCR Session");
        }

        try {
            // Build replication options
            ReplicationOptions options = new ReplicationOptions();
            options.setSynchronous(synchronous);
            options.setSuppressVersions(suppressVersions);

            if (!agentId.isEmpty()) {
                options.setFilter(agent -> agent.getId().equals(agentId));
            }

            // Perform replication
            replicator.replicate(session, actionType, payloadPath, options);

            // Update workflow metadata
            MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
            workflowMetadata.put("replicationAction", actionType.getName());
            workflowMetadata.put("replicationTime", Calendar.getInstance().getTimeInMillis());
            workflowMetadata.put("replicatedPath", payloadPath);

            if (trackInitiator) {
                workflowMetadata.put("replicatedBy", initiator);
                LOG.info("Content replicated by workflow initiator: {}", initiator);
            }

            LOG.info("Replication {} completed for: {}", actionType.getName(), payloadPath);

        } catch (Exception e) {
            LOG.error("Replication failed for: " + payloadPath, e);
            throw new WorkflowException("Replication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the replication action type from string.
     */
    private ReplicationActionType parseActionType(String actionStr) {
        switch (actionStr.toLowerCase()) {
            case "activate":
            case "publish":
                return ReplicationActionType.ACTIVATE;
            case "deactivate":
            case "unpublish":
                return ReplicationActionType.DEACTIVATE;
            case "delete":
                return ReplicationActionType.DELETE;
            case "test":
                return ReplicationActionType.TEST;
            default:
                return ReplicationActionType.ACTIVATE;
        }
    }
}
