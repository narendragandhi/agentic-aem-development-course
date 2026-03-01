package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.demo.workflow.services.NotificationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Notification Workflow Process
 *
 * Sends notifications via email, Slack, or other channels based on workflow events.
 * Inspired by ACS AEM Commons "Send Template'd E-mail" process.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                   NOTIFICATION PROCESS                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
 * │   │  Determine  │───▶│   Build     │───▶│    Send     │       │
 * │   │  Recipients │    │   Message   │    │   Notice    │       │
 * │   └─────────────┘    └─────────────┘    └─────────────┘       │
 * │                             │                                  │
 * │                             ▼                                  │
 * │                   ┌─────────────────┐                         │
 * │                   │  Notification   │                         │
 * │                   │    Channels     │                         │
 * │                   ├─────────────────┤                         │
 * │                   │ • Email         │                         │
 * │                   │ • Slack         │                         │
 * │                   │ • Teams         │                         │
 * │                   │ • AEM Inbox     │                         │
 * │                   └─────────────────┘                         │
 * │                                                                 │
 * │   Notification Types:                                          │
 * │   • revision_required  - Content needs revision                │
 * │   • publish_success    - Content published successfully        │
 * │   • asset_processed    - Asset processing complete             │
 * │   • rejection          - Content rejected at approval level    │
 * │   • escalation         - Content escalated to higher level     │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Notification Process",
        "process.description=Sends templated notifications via email, Slack, or other channels"
    }
)
public class NotificationProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationProcess.class);

    private static final String ARG_NOTIFICATION_TYPE = "notificationType";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_TEMPLATE_PATH = "templatePath";

    @Reference
    private NotificationService notificationService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        String notificationType = metaDataMap.get(ARG_NOTIFICATION_TYPE, "default");
        String level = metaDataMap.get(ARG_LEVEL, "");
        String templatePath = metaDataMap.get(ARG_TEMPLATE_PATH, getDefaultTemplatePath(notificationType));

        LOG.info("Sending notification type '{}' for payload: {}", notificationType, payloadPath);

        try {
            // Build notification context
            Map<String, Object> context = buildNotificationContext(workItem, notificationType, level);

            // Determine recipients based on notification type
            String[] recipients = determineRecipients(workItem, notificationType);

            // Send notification
            if (notificationService != null) {
                notificationService.sendNotification(recipients, templatePath, context);
            } else {
                // Fallback logging when service not available
                LOG.info("Notification would be sent to {} recipients for type: {}",
                    recipients.length, notificationType);
            }

            // Store notification metadata
            MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
            workflowMetadata.put("lastNotificationType", notificationType);
            workflowMetadata.put("lastNotificationTime", System.currentTimeMillis());

            LOG.info("Notification sent successfully for: {}", payloadPath);

        } catch (Exception e) {
            LOG.error("Failed to send notification for: " + payloadPath, e);
            throw new WorkflowException("Notification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the notification context with relevant workflow data.
     */
    private Map<String, Object> buildNotificationContext(WorkItem workItem, String notificationType, String level) {
        Map<String, Object> context = new HashMap<>();

        context.put("payloadPath", workItem.getWorkflowData().getPayload().toString());
        context.put("workflowTitle", workItem.getWorkflow().getWorkflowModel().getTitle());
        context.put("workflowId", workItem.getWorkflow().getId());
        context.put("notificationType", notificationType);
        context.put("currentStep", workItem.getNode().getTitle());

        if (!level.isEmpty()) {
            context.put("approvalLevel", level);
        }

        // Add initiator info
        context.put("initiator", workItem.getWorkflow().getInitiator());

        // Add workflow metadata
        MetaDataMap metadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        if (metadata.containsKey("validationErrors")) {
            context.put("validationErrors", metadata.get("validationErrors", String.class));
        }

        return context;
    }

    /**
     * Determines notification recipients based on notification type.
     */
    private String[] determineRecipients(WorkItem workItem, String notificationType) {
        switch (notificationType) {
            case "revision_required":
            case "rejection":
                // Notify the workflow initiator (author)
                return new String[]{workItem.getWorkflow().getInitiator()};

            case "publish_success":
                // Notify initiator and stakeholders
                return new String[]{
                    workItem.getWorkflow().getInitiator(),
                    "content-publishers@example.com"
                };

            case "asset_processed":
                // Notify asset owners
                return new String[]{"dam-users@example.com"};

            case "escalation":
                // Notify managers
                return new String[]{"managers@example.com"};

            default:
                return new String[]{workItem.getWorkflow().getInitiator()};
        }
    }

    /**
     * Returns the default template path for a notification type.
     */
    private String getDefaultTemplatePath(String notificationType) {
        return "/etc/notification/email/workflow/" + notificationType + ".html";
    }
}
