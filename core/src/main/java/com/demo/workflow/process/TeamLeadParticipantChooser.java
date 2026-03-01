package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic Participant Chooser for Team Lead Assignment
 *
 * Dynamically selects the appropriate team lead based on content type,
 * location, or other business rules.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │              TEAM LEAD PARTICIPANT CHOOSER                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                  ROUTING LOGIC                          │  │
 * │   │                                                         │  │
 * │   │   Content Path            →    Team Lead Group          │  │
 * │   │   ─────────────────────────────────────────────────     │  │
 * │   │   /content/marketing/*    →    marketing-leads          │  │
 * │   │   /content/products/*     →    product-leads            │  │
 * │   │   /content/support/*      →    support-leads            │  │
 * │   │   /content/dam/*          →    dam-leads                │  │
 * │   │   (default)               →    content-leads            │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                  OVERRIDE OPTIONS                       │  │
 * │   │                                                         │  │
 * │   │   • Metadata property: assignedTeamLead                 │  │
 * │   │   • Content property: cq:teamLead                       │  │
 * │   │   • Workflow argument: forceTeamLead                    │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = ParticipantStepChooser.class,
    property = {
        "chooser.label=Team Lead Participant Chooser"
    }
)
public class TeamLeadParticipantChooser implements ParticipantStepChooser {

    private static final Logger LOG = LoggerFactory.getLogger(TeamLeadParticipantChooser.class);

    private static final String DEFAULT_TEAM_LEAD_GROUP = "content-leads";

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Determining team lead for content: {}", payloadPath);

        // Check for explicit assignment in workflow metadata
        MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        String explicitAssignment = workflowMetadata.get("assignedTeamLead", String.class);
        if (explicitAssignment != null && !explicitAssignment.isEmpty()) {
            LOG.info("Using explicit team lead assignment: {}", explicitAssignment);
            return explicitAssignment;
        }

        // Check for force override in step arguments
        String forceTeamLead = metaDataMap.get("forceTeamLead", String.class);
        if (forceTeamLead != null && !forceTeamLead.isEmpty()) {
            LOG.info("Using forced team lead: {}", forceTeamLead);
            return forceTeamLead;
        }

        // Check content properties for assigned team lead
        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver != null) {
            Resource resource = resolver.getResource(payloadPath);
            if (resource != null) {
                Resource contentResource = resource.getChild("jcr:content");
                if (contentResource != null) {
                    String contentTeamLead = contentResource.getValueMap().get("cq:teamLead", String.class);
                    if (contentTeamLead != null && !contentTeamLead.isEmpty()) {
                        LOG.info("Using content-defined team lead: {}", contentTeamLead);
                        return contentTeamLead;
                    }
                }
            }
        }

        // Determine team lead based on content path
        String teamLead = determineTeamLeadByPath(payloadPath);
        LOG.info("Determined team lead by path: {} -> {}", payloadPath, teamLead);

        return teamLead;
    }

    /**
     * Determines the appropriate team lead group based on content path.
     */
    private String determineTeamLeadByPath(String path) {
        if (path.startsWith("/content/marketing/")) {
            return "marketing-leads";
        } else if (path.startsWith("/content/products/")) {
            return "product-leads";
        } else if (path.startsWith("/content/support/")) {
            return "support-leads";
        } else if (path.startsWith("/content/dam/")) {
            return "dam-leads";
        } else if (path.startsWith("/content/experience-fragments/")) {
            return "xf-leads";
        } else if (path.contains("/campaigns/")) {
            return "campaign-leads";
        }

        return DEFAULT_TEAM_LEAD_GROUP;
    }
}
