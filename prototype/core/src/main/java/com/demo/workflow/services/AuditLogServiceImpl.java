package com.demo.workflow.services;

import com.demo.workflow.services.AuditLogService.Action;
import com.demo.workflow.services.AuditLogService.AuditEntry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AuditLogServiceImpl implements AuditLogService {

    private final ConcurrentLinkedQueue<AuditEntry> entries = new ConcurrentLinkedQueue<>();
    private boolean available = true;

    @Override
    public void log(String userId, Action action, String resourcePath, Map<String, Object> metadata) {
        if (!available) {
            return;
        }
        AuditEntry entry = new AuditEntry(
            UUID.randomUUID().toString(),
            userId,
            action,
            resourcePath,
            metadata,
            Instant.now(),
            "127.0.0.1"
        );
        entries.add(entry);
    }

    @Override
    public List<AuditEntry> getEntriesByUser(String userId) {
        if (userId == null) {
            return List.of();
        }
        return entries.stream()
            .filter(e -> e.getUserId().equals(userId))
            .toList();
    }

    @Override
    public List<AuditEntry> getEntriesByResource(String resourcePath) {
        if (resourcePath == null) {
            return List.of();
        }
        return entries.stream()
            .filter(e -> e.getResourcePath() != null && e.getResourcePath().contains(resourcePath))
            .toList();
    }

    @Override
    public List<AuditEntry> getEntriesByAction(Action action) {
        if (action == null) {
            return List.of();
        }
        return entries.stream()
            .filter(e -> e.getAction() == action)
            .toList();
    }

    @Override
    public List<AuditEntry> getRecentEntries(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return entries.stream()
            .skip(Math.max(0, entries.size() - limit))
            .toList();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
