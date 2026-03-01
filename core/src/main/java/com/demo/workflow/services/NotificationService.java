package com.demo.workflow.services;

import java.util.Map;

/**
 * Notification Service Interface
 *
 * Provides multi-channel notification capabilities for workflow processes.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                   NOTIFICATION SERVICE                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                NOTIFICATION CHANNELS                    │  │
 * │   │                                                         │  │
 * │   │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌──────────┐  │  │
 * │   │   │  EMAIL  │  │  SLACK  │  │  TEAMS  │  │AEM INBOX │  │  │
 * │   │   └─────────┘  └─────────┘  └─────────┘  └──────────┘  │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │               TEMPLATE SUPPORT                          │  │
 * │   │                                                         │  │
 * │   │   Templates stored in:                                  │  │
 * │   │   /etc/notification/email/workflow/                     │  │
 * │   │                                                         │  │
 * │   │   Supported variables:                                  │  │
 * │   │   • ${payloadPath} - Content path                       │  │
 * │   │   • ${workflowTitle} - Workflow name                    │  │
 * │   │   • ${initiator} - User who started workflow            │  │
 * │   │   • ${currentStep} - Current step in workflow           │  │
 * │   │   • ${approvalLevel} - Approval hierarchy level         │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
public interface NotificationService {

    /**
     * Sends notification to specified recipients using the given template.
     *
     * @param recipients array of recipient identifiers (email, user ID, channel)
     * @param templatePath path to notification template
     * @param context map of context variables for template rendering
     */
    void sendNotification(String[] recipients, String templatePath, Map<String, Object> context);

    /**
     * Sends email notification.
     *
     * @param to recipient email addresses
     * @param subject email subject
     * @param body email body content
     * @param isHtml whether body is HTML formatted
     */
    void sendEmail(String[] to, String subject, String body, boolean isHtml);

    /**
     * Sends Slack notification.
     *
     * @param channel Slack channel or user
     * @param message notification message
     * @param attachments optional attachments
     */
    void sendSlackNotification(String channel, String message, Map<String, Object> attachments);

    /**
     * Sends notification to AEM Inbox.
     *
     * @param userId target user ID
     * @param title notification title
     * @param message notification message
     * @param actionPath path for notification action
     */
    void sendInboxNotification(String userId, String title, String message, String actionPath);
}
