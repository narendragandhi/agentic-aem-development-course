package com.demo.workflow.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Specifications for AntivirusScanService.
 * 
 * BMAD Phase 05 - Testing
 * 
 * These tests define expected behavior BEFORE implementation.
 * Run: mvn test -Dtest=AntivirusScanServiceSpec
 */
@DisplayName("AntivirusScanService Specifications")
class AntivirusScanServiceSpec {

    private AntivirusScanService service;

    @BeforeEach
    void setUp() {
        service = new AntivirusScanServiceImpl();
    }

    @Nested
    @DisplayName("Scan Functionality")
    class ScanFunctionality {

        @Test
        @DisplayName("should return CLEAN for safe content")
        void shouldReturnCleanForSafeContent() {
            InputStream safeContent = new ByteArrayInputStream(
                "This is safe content".getBytes(StandardCharsets.UTF_8));

            AntivirusScanService.ScanResult result = service.scan(safeContent, "safe.txt");

            assertEquals(AntivirusScanService.ScanStatus.CLEAN, result.getStatus());
            assertNull(result.getThreatName());
        }

        @Test
        @DisplayName("should return INFECTED for EICAR signature")
        void shouldReturnInfectedForEICAR() {
            String eicar = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
            InputStream infectedContent = new ByteArrayInputStream(eicar.getBytes(StandardCharsets.UTF_8));

            AntivirusScanService.ScanResult result = service.scan(infectedContent, "test.txt");

            assertEquals(AntivirusScanService.ScanStatus.INFECTED, result.getStatus());
            assertEquals("Eicar-Test-Signature", result.getThreatName());
        }

        @Test
        @DisplayName("should detect virus in filename")
        void shouldDetectVirusInFilename() {
            InputStream safeContent = new ByteArrayInputStream(
                "Safe file".getBytes(StandardCharsets.UTF_8));

            AntivirusScanService.ScanResult result = service.scan(safeContent, "virus_test.exe");

            assertEquals(AntivirusScanService.ScanStatus.INFECTED, result.getStatus());
        }

        @ParameterizedTest
        @ValueSource(strings = {"test.pdf", "document.docx", "image.png", "data.csv"})
        @DisplayName("should scan various file types")
        void shouldScanVariousFileTypes(String fileName) {
            InputStream content = new ByteArrayInputStream(
                "Safe content".getBytes(StandardCharsets.UTF_8));

            AntivirusScanService.ScanResult result = service.scan(content, fileName);

            assertNotNull(result);
            assertNotNull(result.getStatus());
        }
    }

    @Nested
    @DisplayName("Performance Requirements")
    class PerformanceRequirements {

        @Test
        @DisplayName("should complete scan within 5 seconds")
        void shouldCompleteWithinSLA() {
            byte[] content = new byte[1024 * 1024]; // 1MB
            InputStream inputStream = new ByteArrayInputStream(content);

            long start = System.currentTimeMillis();
            AntivirusScanService.ScanResult result = service.scan(inputStream, "large.bin");
            long duration = System.currentTimeMillis() - start;

            assertTrue(duration < 5000, 
                String.format("Scan took %dms, exceeds 5000ms SLA", duration));
        }

        @Test
        @DisplayName("should track scan duration")
        void shouldTrackScanDuration() {
            InputStream content = new ByteArrayInputStream(
                "test".getBytes(StandardCharsets.UTF_8));

            AntivirusScanService.ScanResult result = service.scan(content, "test.txt");

            assertTrue(result.getScanDurationMs() >= 0);
        }
    }

    @Nested
    @DisplayName("Service Availability")
    class ServiceAvailability {

        @Test
        @DisplayName("should report available")
        void shouldReportAvailable() {
            assertTrue(service.isAvailable());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should handle null input")
        void shouldHandleNullInput() {
            AntivirusScanService.ScanResult result = service.scan(null, "null.txt");
            
            assertEquals(AntivirusScanService.ScanStatus.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("should handle empty content")
        void shouldHandleEmptyContent() {
            InputStream empty = new ByteArrayInputStream(new byte[0]);
            
            AntivirusScanService.ScanResult result = service.scan(empty, "empty.txt");
            
            assertEquals(AntivirusScanService.ScanStatus.CLEAN, result.getStatus());
        }
    }
}
