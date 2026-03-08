package com.demo.workflow.services;

import java.time.Instant;
import java.util.List;

public interface NotificationService {

    enum Channel {
        EMAIL, SLACK, SMS, WEBHOOK
    }

    enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    enum NotificationStatus {
        SENT, FAILED, PENDING, RATE_LIMITED
    }

    class Notification {
        private final String notificationId;
        private final String recipient;
        private final Channel channel;
        private final String subject;
        private final String message;
        private final Priority priority;
        private final Instant sentAt;
        private final NotificationStatus status;

        public Notification(String notificationId, String recipient, Channel channel,
                String subject, String message, Priority priority, 
                Instant sentAt, NotificationStatus status) {
            this.notificationId = notificationId;
            this.recipient = recipient;
            this.channel = channel;
            this.subject = subject;
            this.message = message;
            this.priority = priority;
            this.sentAt = sentAt;
            this.status = status;
        }

        public String getNotificationId() { return notificationId; }
        public String getRecipient() { return recipient; }
        public Channel getChannel() { return channel; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
        public Priority getPriority() { return priority; }
        public Instant getSentAt() { return sentAt; }
        public NotificationStatus getStatus() { return status; }
    }

    NotificationStatus send(String recipient, Channel channel, String subject, String message);

    NotificationStatus sendPriority(String recipient, Channel channel, 
            String subject, String message, Priority priority);

    List<Notification> getHistory(String recipient);

    boolean isAvailable(Channel channel);
}
