package com.demo.workflow.services;

import com.demo.workflow.services.QuarantineService.QuarantineRecord;
import com.demo.workflow.services.QuarantineService.QuarantineStatus;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class QuarantineServiceImpl implements QuarantineService {

    private final Map<String, QuarantineRecord> quarantineStore = new ConcurrentHashMap<>();
    private boolean available = true;

    @Override
    public QuarantineStatus quarantine(String assetPath, String threatName) {
        if (!available) {
            return QuarantineStatus.ERROR;
        }
        if (assetPath == null || assetPath.isEmpty()) {
            return QuarantineStatus.ERROR;
        }

        String quarantineId = UUID.randomUUID().toString();
        String quarantinePath = "/var/quarantine/" + quarantineId;

        QuarantineRecord record = new QuarantineRecord(
            assetPath,
            quarantinePath,
            threatName,
            Instant.now(),
            "system"
        );

        quarantineStore.put(quarantineId, record);
        return QuarantineStatus.QUARANTINED;
    }

    @Override
    public QuarantineStatus release(String quarantineId) {
        if (!available) {
            return QuarantineStatus.ERROR;
        }
        if (quarantineId == null || !quarantineStore.containsKey(quarantineId)) {
            return QuarantineStatus.NOT_FOUND;
        }

        quarantineStore.remove(quarantineId);
        return QuarantineStatus.RELEASED;
    }

    @Override
    public QuarantineStatus delete(String quarantineId) {
        if (!available) {
            return QuarantineStatus.ERROR;
        }
        if (quarantineId == null || !quarantineStore.containsKey(quarantineId)) {
            return QuarantineStatus.NOT_FOUND;
        }

        quarantineStore.remove(quarantineId);
        return QuarantineStatus.DELETED;
    }

    @Override
    public QuarantineRecord getRecord(String quarantineId) {
        if (quarantineId == null) {
            return null;
        }
        return quarantineStore.get(quarantineId);
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
