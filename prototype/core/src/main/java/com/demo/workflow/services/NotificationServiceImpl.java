package com.demo.workflow.services;

import com.demo.workflow.services.NotificationService.Channel;
import com.demo.workflow.services.NotificationService.Notification;
import com.demo.workflow.services.NotificationService.NotificationStatus;
import com.demo.workflow.services.NotificationService.Priority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationServiceImpl implements NotificationService {

    private final List<Notification> history = new ArrayList<>();

    @Override
    public NotificationStatus send(String recipient, Channel channel, 
            String subject, String message) {
        return sendPriority(recipient, channel, subject, message, Priority.NORMAL);
    }

    @Override
    public NotificationStatus sendPriority(String recipient, Channel channel,
            String subject, String message, Priority priority) {
        if (recipient == null || recipient.isEmpty()) {
            return NotificationStatus.FAILED;
        }

        String notificationId = UUID.randomUUID().toString();
        Notification notification = new Notification(
            notificationId,
            recipient,
            channel,
            subject,
            message,
            priority,
            Instant.now(),
            NotificationStatus.SENT
        );

        history.add(notification);
        return NotificationStatus.SENT;
    }

    @Override
    public List<Notification> getHistory(String recipient) {
        if (recipient == null) {
            return List.of();
        }
        return history.stream()
            .filter(n -> n.getRecipient().equals(recipient))
            .toList();
    }

    @Override
    public boolean isAvailable(Channel channel) {
        return channel != null;
    }
}
