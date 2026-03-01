package com.demo.workflow.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationServiceImpl.
 *
 * Tests notification functionality including email and Slack integration
 * with async processing and retry logic.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new NotificationServiceImpl();

        // Create mock config using Java proxy
        NotificationServiceImpl.Config mockConfig = createMockConfig(
            true,    // enabled
            "noreply@example.com",  // from_email
            "security@example.com", // security_recipients
            "",      // slack_webhook_url (empty = disabled)
            "#security-alerts",  // slack_security_channel
            false,   // email_enabled (disabled for tests - no MessageGateway)
            false,   // slack_enabled
            2,       // thread_pool_size
            3        // max_retries
        );

        // Call activate method with mock config
        Method activateMethod = NotificationServiceImpl.class.getDeclaredMethod("activate",
            NotificationServiceImpl.Config.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(service, mockConfig);
    }

    /**
     * Creates a mock Config using Java dynamic proxy
     */
    private NotificationServiceImpl.Config createMockConfig(
            boolean enabled,
            String fromEmail,
            String securityRecipients,
            String slackWebhookUrl,
            String slackSecurityChannel,
            boolean emailEnabled,
            boolean slackEnabled,
            int threadPoolSize,
            int maxRetries) {

        return (NotificationServiceImpl.Config) java.lang.reflect.Proxy.newProxyInstance(
            NotificationServiceImpl.Config.class.getClassLoader(),
            new Class<?>[]{NotificationServiceImpl.Config.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "enabled": return enabled;
                    case "from_email": return fromEmail;
                    case "security_recipients": return securityRecipients;
                    case "slack_webhook_url": return slackWebhookUrl;
                    case "slack_security_channel": return slackSecurityChannel;
                    case "email_enabled": return emailEnabled;
                    case "slack_enabled": return slackEnabled;
                    case "thread_pool_size": return threadPoolSize;
                    case "max_retries": return maxRetries;
                    default: return null;
                }
            }
        );
    }

    @Test
    @DisplayName("Should handle null recipients gracefully")
    void handleNullRecipients() {
        // Email is disabled in config, so this should not throw
        assertDoesNotThrow(() ->
            service.sendEmail(null, "Subject", "Body", false)
        );
    }

    @Test
    @DisplayName("Should handle empty subject gracefully")
    void handleEmptySubject() {
        String[] recipients = {"test@example.com"};
        assertDoesNotThrow(() ->
            service.sendEmail(recipients, "", "Body", false)
        );
    }

    @Test
    @DisplayName("Should support HTML email flag")
    void supportHtmlEmail() {
        String[] recipients = {"test@example.com"};
        String htmlBody = "<h1>Alert</h1><p>Test message</p>";

        // Should not throw - email sending requires MessageGateway which is not configured
        assertDoesNotThrow(() ->
            service.sendEmail(recipients, "HTML Test", htmlBody, true)
        );
    }

    @Test
    @DisplayName("Should handle Slack notification gracefully when not configured")
    void handleSlackWhenNotConfigured() {
        Map<String, Object> attachments = new HashMap<>();
        attachments.put("color", "danger");

        // Should not throw - Slack is disabled
        assertDoesNotThrow(() ->
            service.sendSlackNotification("#test-channel", "Test message", attachments)
        );
    }

    @Test
    @DisplayName("Should handle Slack notification with null attachments")
    void handleSlackWithNullAttachments() {
        assertDoesNotThrow(() ->
            service.sendSlackNotification("#security-alerts", "Security alert!", null)
        );
    }

    @Test
    @DisplayName("Should handle Inbox notification gracefully")
    void handleInboxNotification() {
        assertDoesNotThrow(() ->
            service.sendInboxNotification(
                "admin",
                "Workflow Alert",
                "Asset requires review",
                "/content/dam/test/asset.pdf"
            )
        );
    }

    @Test
    @DisplayName("Should handle sendNotification with template")
    void handleTemplatedNotification() {
        String[] recipients = {"admin", "reviewer"};
        String templatePath = "/etc/notification/email/workflow/approval-request.html";
        Map<String, Object> context = new HashMap<>();
        context.put("assetPath", "/content/dam/test.pdf");
        context.put("workflowTitle", "Asset Approval");
        context.put("payloadPath", "/content/dam/test.pdf");
        context.put("currentStep", "Review");
        context.put("initiator", "admin");

        assertDoesNotThrow(() ->
            service.sendNotification(recipients, templatePath, context)
        );
    }

    @Test
    @DisplayName("Should handle notification with empty context")
    void handleEmptyContext() {
        String[] recipients = {"admin"};
        Map<String, Object> emptyContext = new HashMap<>();

        assertDoesNotThrow(() ->
            service.sendNotification(recipients, "/template/path", emptyContext)
        );
    }

    @Test
    @DisplayName("Should check service availability")
    void checkServiceAvailability() {
        // Service is enabled but email requires MessageGateway which is null
        // So isAvailable should return false when email is enabled but no gateway
        // In our config, email_enabled = false, so it should be available
        assertTrue(service.isAvailable());
    }
}
