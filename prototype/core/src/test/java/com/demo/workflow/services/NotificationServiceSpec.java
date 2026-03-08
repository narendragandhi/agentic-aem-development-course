package com.demo.workflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.demo.workflow.services.NotificationService.Channel;
import com.demo.workflow.services.NotificationService.Notification;
import com.demo.workflow.services.NotificationService.NotificationStatus;
import com.demo.workflow.services.NotificationService.Priority;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("NotificationService")
class NotificationServiceSpec {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl();
    }

    @Nested
    @DisplayName("SendFunctionality")
    class SendFunctionality {

        @Test
        @DisplayName("should send email notification")
        void shouldSendEmailNotification() {
            NotificationStatus status = notificationService.send(
                "admin@example.com",
                Channel.EMAIL,
                "Alert",
                "Security issue detected"
            );
            assertEquals(NotificationStatus.SENT, status);
        }

        @Test
        @DisplayName("should send Slack notification")
        void shouldSendSlackNotification() {
            NotificationStatus status = notificationService.send(
                "#alerts",
                Channel.SLACK,
                "Warning",
                "High CPU usage"
            );
            assertEquals(NotificationStatus.SENT, status);
        }

        @Test
        @DisplayName("should fail for empty recipient")
        void shouldFailForEmptyRecipient() {
            NotificationStatus status = notificationService.send(
                "",
                Channel.EMAIL,
                "Test",
                "Message"
            );
            assertEquals(NotificationStatus.FAILED, status);
        }

        @Test
        @DisplayName("should send urgent priority notification")
        void shouldSendUrgentPriorityNotification() {
            NotificationStatus status = notificationService.sendPriority(
                "admin@example.com",
                Channel.SMS,
                "CRITICAL",
                "System down",
                Priority.URGENT
            );
            assertEquals(NotificationStatus.SENT, status);
        }
    }

    @Nested
    @DisplayName("HistoryFunctionality")
    class HistoryFunctionality {

        @Test
        @DisplayName("should retrieve notification history")
        void shouldRetrieveNotificationHistory() {
            notificationService.send("user@example.com", Channel.EMAIL, "Test", "Msg1");
            notificationService.send("user@example.com", Channel.EMAIL, "Test", "Msg2");
            
            List<Notification> history = notificationService.getHistory("user@example.com");
            assertEquals(2, history.size());
        }

        @Test
        @DisplayName("should return empty for unknown recipient")
        void shouldReturnEmptyForUnknownRecipient() {
            List<Notification> history = notificationService.getHistory("unknown@example.com");
            assertTrue(history.isEmpty());
        }

        @Test
        @DisplayName("should return empty for null recipient")
        void shouldReturnEmptyForNullRecipient() {
            List<Notification> history = notificationService.getHistory(null);
            assertTrue(history.isEmpty());
        }
    }

    @Nested
    @DisplayName("ServiceAvailability")
    class ServiceAvailability {

        @Test
        @DisplayName("should report email channel available")
        void shouldReportEmailChannelAvailable() {
            assertTrue(notificationService.isAvailable(Channel.EMAIL));
        }

        @Test
        @DisplayName("should report Slack channel available")
        void shouldReportSlackChannelAvailable() {
            assertTrue(notificationService.isAvailable(Channel.SLACK));
        }
    }
}
