package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic Participant Chooser for Manager Assignment
 *
 * Selects the appropriate manager based on the team lead who completed
 * the previous step or organizational hierarchy.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │               MANAGER PARTICIPANT CHOOSER                       │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │              HIERARCHY MAPPING                          │  │
 * │   │                                                         │  │
 * │   │   Team Lead Group      →    Manager Group               │  │
 * │   │   ─────────────────────────────────────────────────     │  │
 * │   │   marketing-leads      →    marketing-managers          │  │
 * │   │   product-leads        →    product-managers            │  │
 * │   │   support-leads        →    support-managers            │  │
 * │   │   dam-leads            →    dam-managers                │  │
 * │   │   content-leads        →    content-managers            │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │              ESCALATION PATH                            │  │
 * │   │                                                         │  │
 * │   │   If team lead escalates:                               │  │
 * │   │   • Look up manager from org hierarchy                  │  │
 * │   │   • Fall back to department manager                     │  │
 * │   │   • Default to general content-managers                 │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = ParticipantStepChooser.class,
    property = {
        "chooser.label=Manager Participant Chooser"
    }
)
public class ManagerParticipantChooser implements ParticipantStepChooser {

    private static final Logger LOG = LoggerFactory.getLogger(ManagerParticipantChooser.class);

    private static final String DEFAULT_MANAGER_GROUP = "content-managers";

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Determining manager for content: {}", payloadPath);

        MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();

        // Check for explicit manager assignment
        String explicitManager = workflowMetadata.get("assignedManager", String.class);
        if (explicitManager != null && !explicitManager.isEmpty()) {
            LOG.info("Using explicit manager assignment: {}", explicitManager);
            return explicitManager;
        }

        // Get the team lead who completed the previous step
        String previousParticipant = workflowMetadata.get("level1Participant", String.class);
        if (previousParticipant != null) {
            String manager = getManagerForTeamLead(previousParticipant);
            if (manager != null) {
                LOG.info("Determined manager from team lead hierarchy: {} -> {}", previousParticipant, manager);
                return manager;
            }
        }

        // Determine manager based on content path
        String manager = determineManagerByPath(payloadPath);
        LOG.info("Determined manager by path: {} -> {}", payloadPath, manager);

        return manager;
    }

    /**
     * Gets the manager group for a given team lead group.
     */
    private String getManagerForTeamLead(String teamLeadGroup) {
        switch (teamLeadGroup) {
            case "marketing-leads":
                return "marketing-managers";
            case "product-leads":
                return "product-managers";
            case "support-leads":
                return "support-managers";
            case "dam-leads":
                return "dam-managers";
            case "xf-leads":
                return "xf-managers";
            case "campaign-leads":
                return "campaign-managers";
            case "content-leads":
                return "content-managers";
            default:
                return null;
        }
    }

    /**
     * Determines the appropriate manager group based on content path.
     */
    private String determineManagerByPath(String path) {
        if (path.startsWith("/content/marketing/")) {
            return "marketing-managers";
        } else if (path.startsWith("/content/products/")) {
            return "product-managers";
        } else if (path.startsWith("/content/support/")) {
            return "support-managers";
        } else if (path.startsWith("/content/dam/")) {
            return "dam-managers";
        } else if (path.startsWith("/content/experience-fragments/")) {
            return "xf-managers";
        } else if (path.contains("/campaigns/")) {
            return "campaign-managers";
        }

        return DEFAULT_MANAGER_GROUP;
    }
}
