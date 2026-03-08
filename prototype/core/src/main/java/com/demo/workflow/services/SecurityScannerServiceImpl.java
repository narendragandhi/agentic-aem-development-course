package com.demo.workflow.services;

import com.demo.workflow.services.SecurityScannerService.ScanReport;
import com.demo.workflow.services.SecurityScannerService.SecurityIssue;
import com.demo.workflow.services.SecurityScannerService.SecurityStatus;
import com.demo.workflow.services.SecurityScannerService.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityScannerServiceImpl implements SecurityScannerService {

    private final Map<String, ScanReport> scanCache = new ConcurrentHashMap<>();
    private boolean available = true;

    @Override
    public ScanReport scan(String assetPath, Map<String, Object> options) {
        if (!available || assetPath == null || assetPath.isEmpty()) {
            return new ScanReport(SecurityStatus.ERROR, List.of(), 0, "system");
        }

        List<SecurityIssue> issues = new ArrayList<>();

        if (assetPath.contains(".js")) {
            issues.add(new SecurityIssue(
                UUID.randomUUID().toString(),
                "Java -Script file ensure no XSS vulnerabilities",
                Severity.MEDIUM,
                assetPath,
                "Sanitize all user inputs"
            ));
        }

        if (assetPath.contains("password") || assetPath.contains("secret")) {
            issues.add(new SecurityIssue(
                UUID.randomUUID().toString(),
                "Potential sensitive data exposure",
                Severity.CRITICAL,
                assetPath,
                "Encrypt sensitive data"
            ));
        }

        SecurityStatus status = issues.isEmpty() ? SecurityStatus.SECURE : SecurityStatus.VULNERABLE;
        ScanReport report = new ScanReport(status, issues, 100, "system");
        scanCache.put(assetPath, report);

        return report;
    }

    @Override
    public SecurityStatus getStatus(String assetPath) {
        if (assetPath == null) {
            return SecurityStatus.ERROR;
        }
        ScanReport report = scanCache.get(assetPath);
        return report != null ? report.getStatus() : SecurityStatus.SCANNING;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
