package com.demo.workflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.demo.workflow.services.SecurityScannerService.ScanReport;
import com.demo.workflow.services.SecurityScannerService.SecurityIssue;
import com.demo.workflow.services.SecurityScannerService.SecurityStatus;
import com.demo.workflow.services.SecurityScannerService.Severity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

@DisplayName("SecurityScannerService")
class SecurityScannerServiceSpec {

    private SecurityScannerService scannerService;

    @BeforeEach
    void setUp() {
        scannerService = new SecurityScannerServiceImpl();
    }

    @Nested
    @DisplayName("ScanFunctionality")
    class ScanFunctionality {

        @Test
        @DisplayName("should scan asset and return secure status")
        void shouldScanAssetAndReturnSecureStatus() {
            ScanReport report = scannerService.scan("/content/dam/image.png", Map.of());
            assertEquals(SecurityStatus.SECURE, report.getStatus());
            assertTrue(report.getIssues().isEmpty());
        }

        @Test
        @DisplayName("should detect JavaScript vulnerabilities")
        void shouldDetectJavaScriptVulnerabilities() {
            ScanReport report = scannerService.scan("/content/dam/script.js", Map.of());
            assertEquals(SecurityStatus.VULNERABLE, report.getStatus());
            assertFalse(report.getIssues().isEmpty());
        }

        @Test
        @DisplayName("should detect critical sensitive data exposure")
        void shouldDetectCriticalSensitiveDataExposure() {
            ScanReport report = scannerService.scan("/content/dam/passwords.txt", Map.of());
            assertEquals(SecurityStatus.VULNERABLE, report.getStatus());
            assertTrue(report.getCriticalCount() > 0);
        }

        @Test
        @DisplayName("should return error for null path")
        void shouldReturnErrorForNullPath() {
            ScanReport report = scannerService.scan(null, Map.of());
            assertEquals(SecurityStatus.ERROR, report.getStatus());
        }

        @Test
        @DisplayName("should return error for empty path")
        void shouldReturnErrorForEmptyPath() {
            ScanReport report = scannerService.scan("", Map.of());
            assertEquals(SecurityStatus.ERROR, report.getStatus());
        }
    }

    @Nested
    @DisplayName("GetStatus")
    class GetStatus {

        @Test
        @DisplayName("should return scanning for unscanned asset")
        void shouldReturnScanningForUnscannedAsset() {
            SecurityStatus status = scannerService.getStatus("/content/dam/new.png");
            assertEquals(SecurityStatus.SCANNING, status);
        }

        @Test
        @DisplayName("should return cached status after scan")
        void shouldReturnCachedStatusAfterScan() {
            scannerService.scan("/content/dam/secure.pdf", Map.of());
            SecurityStatus status = scannerService.getStatus("/content/dam/secure.pdf");
            assertEquals(SecurityStatus.SECURE, status);
        }

        @Test
        @DisplayName("should return error for null path")
        void shouldReturnErrorForNullPath() {
            SecurityStatus status = scannerService.getStatus(null);
            assertEquals(SecurityStatus.ERROR, status);
        }
    }

    @Nested
    @DisplayName("ServiceAvailability")
    class ServiceAvailability {

        @Test
        @DisplayName("should report available")
        void shouldReportAvailable() {
            assertTrue(scannerService.isAvailable());
        }
    }
}
