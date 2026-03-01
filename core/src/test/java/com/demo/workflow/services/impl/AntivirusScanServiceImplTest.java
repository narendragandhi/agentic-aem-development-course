package com.demo.workflow.services.impl;

import com.demo.workflow.services.AntivirusScanService;
import com.demo.workflow.services.AntivirusScanService.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AntivirusScanServiceImpl
 *
 * Tests cover:
 * - Mock mode scanning (positive and negative cases)
 * - File size limits
 * - Service availability checks
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
class AntivirusScanServiceImplTest {

    private AntivirusScanServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new AntivirusScanServiceImpl();

        // Create mock config using reflection
        AntivirusScanServiceImpl.Config mockConfig = createMockConfig(
            "MOCK",      // scanEngine
            "localhost", // clamavHost
            3310,        // clamavPort
            5000,        // connectionTimeout
            60000,       // readTimeout
            104857600L,  // maxFileSize (100MB)
            "",          // restApiUrl
            "",          // restApiKey
            true         // enabled
        );

        // Activate the service
        service.activate(mockConfig);
    }

    @Nested
    @DisplayName("Mock Mode Scanning Tests")
    class MockModeScanningTests {

        @Test
        @DisplayName("Should return CLEAN for normal files")
        void scanCleanFile() {
            InputStream input = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));

            ScanResult result = service.scanFile(input, "document.pdf", 12);

            assertTrue(result.isClean(), "Normal file should be clean");
            assertEquals("MOCK", result.getScanEngine());
            assertNull(result.getThreatName());
        }

        @Test
        @DisplayName("Should detect virus_ prefixed files as infected")
        void scanVirusPrefixedFile() {
            InputStream input = new ByteArrayInputStream("malicious content".getBytes(StandardCharsets.UTF_8));

            ScanResult result = service.scanFile(input, "virus_test.exe", 17);

            assertFalse(result.isClean(), "virus_ prefixed file should be infected");
            assertEquals("Mock.TestVirus", result.getThreatName());
            assertEquals("MOCK", result.getScanEngine());
        }

        @Test
        @DisplayName("Should detect malware_ prefixed files as infected")
        void scanMalwarePrefixedFile() {
            InputStream input = new ByteArrayInputStream("malicious content".getBytes(StandardCharsets.UTF_8));

            ScanResult result = service.scanFile(input, "malware_payload.bin", 17);

            assertFalse(result.isClean(), "malware_ prefixed file should be infected");
            assertEquals("Mock.TestVirus", result.getThreatName());
        }

        @Test
        @DisplayName("Should detect EICAR test files as infected")
        void scanEicarFile() {
            // EICAR test string (safe test pattern recognized by all AV)
            String eicarString = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
            InputStream input = new ByteArrayInputStream(eicarString.getBytes(StandardCharsets.UTF_8));

            ScanResult result = service.scanFile(input, "eicar_test.txt", eicarString.length());

            assertFalse(result.isClean(), "EICAR test file should be detected");
        }

        @Test
        @DisplayName("Should handle case-insensitive virus detection")
        void scanCaseInsensitiveDetection() {
            InputStream input = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));

            ScanResult result = service.scanFile(input, "VIRUS_UPPERCASE.exe", 7);

            assertFalse(result.isClean(), "Case should not matter for detection");
        }
    }

    @Nested
    @DisplayName("File Size Limit Tests")
    class FileSizeLimitTests {

        @Test
        @DisplayName("Should reject files exceeding max size")
        void rejectOversizedFile() {
            InputStream input = new ByteArrayInputStream("small".getBytes(StandardCharsets.UTF_8));
            long oversizedFileSize = 200 * 1024 * 1024L; // 200MB

            ScanResult result = service.scanFile(input, "large_file.zip", oversizedFileSize);

            assertFalse(result.isClean());
            assertTrue(result.getDetails().contains("exceeds maximum"));
        }

        @Test
        @DisplayName("Should accept files at max size limit")
        void acceptMaxSizeFile() {
            InputStream input = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
            long exactMaxSize = 100 * 1024 * 1024L; // 100MB (exactly at limit)

            ScanResult result = service.scanFile(input, "at_limit.zip", exactMaxSize);

            assertTrue(result.isClean());
        }
    }

    @Nested
    @DisplayName("Service State Tests")
    class ServiceStateTests {

        @Test
        @DisplayName("Should report available when enabled in mock mode")
        void availableWhenEnabled() {
            assertTrue(service.isAvailable());
        }

        @Test
        @DisplayName("Should return MOCK as scan engine name")
        void correctEngineName() {
            assertEquals("MOCK", service.getScanEngineName());
        }

        @Test
        @DisplayName("Should skip scanning when disabled")
        void skipWhenDisabled() throws Exception {
            // Create disabled config
            AntivirusScanServiceImpl disabledService = new AntivirusScanServiceImpl();
            AntivirusScanServiceImpl.Config disabledConfig = createMockConfig(
                "MOCK", "localhost", 3310, 5000, 60000, 104857600L, "", "", false
            );
            disabledService.activate(disabledConfig);

            InputStream input = new ByteArrayInputStream("virus_payload.exe".getBytes(StandardCharsets.UTF_8));
            ScanResult result = disabledService.scanFile(input, "virus_payload.exe", 17);

            assertTrue(result.isClean(), "Should allow all files when disabled");
            assertEquals("DISABLED", result.getScanEngine());
        }

        @Test
        @DisplayName("Should report unavailable when disabled")
        void unavailableWhenDisabled() throws Exception {
            AntivirusScanServiceImpl disabledService = new AntivirusScanServiceImpl();
            AntivirusScanServiceImpl.Config disabledConfig = createMockConfig(
                "MOCK", "localhost", 3310, 5000, 60000, 104857600L, "", "", false
            );
            disabledService.activate(disabledConfig);

            assertFalse(disabledService.isAvailable());
        }
    }

    @Nested
    @DisplayName("Scan Result Factory Tests")
    class ScanResultTests {

        @Test
        @DisplayName("ScanResult.clean() creates correct result")
        void cleanResultFactory() {
            ScanResult result = ScanResult.clean("TestEngine", 100);

            assertTrue(result.isClean());
            assertEquals("TestEngine", result.getScanEngine());
            assertEquals(100, result.getScanDurationMs());
            assertNull(result.getThreatName());
        }

        @Test
        @DisplayName("ScanResult.infected() creates correct result")
        void infectedResultFactory() {
            ScanResult result = ScanResult.infected("Trojan.Generic", "ClamAV", 250);

            assertFalse(result.isClean());
            assertEquals("Trojan.Generic", result.getThreatName());
            assertEquals("ClamAV", result.getScanEngine());
            assertEquals(250, result.getScanDurationMs());
        }

        @Test
        @DisplayName("ScanResult.error() creates correct result")
        void errorResultFactory() {
            ScanResult result = ScanResult.error("ClamAV", "Connection refused");

            assertFalse(result.isClean());
            assertEquals("ClamAV", result.getScanEngine());
            assertTrue(result.getDetails().contains("Connection refused"));
        }
    }

    /**
     * Helper to create mock config using proxy
     */
    private AntivirusScanServiceImpl.Config createMockConfig(
            String scanEngine, String clamavHost, int clamavPort,
            int connectionTimeout, int readTimeout, long maxFileSize,
            String restApiUrl, String restApiKey, boolean enabled) {

        return new AntivirusScanServiceImpl.Config() {
            @Override
            public String scanEngine() { return scanEngine; }
            @Override
            public String clamavHost() { return clamavHost; }
            @Override
            public int clamavPort() { return clamavPort; }
            @Override
            public int connectionTimeout() { return connectionTimeout; }
            @Override
            public int readTimeout() { return readTimeout; }
            @Override
            public long maxFileSize() { return maxFileSize; }
            @Override
            public String restApiUrl() { return restApiUrl; }
            @Override
            public String restApiKey() { return restApiKey; }
            @Override
            public boolean enabled() { return enabled; }
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AntivirusScanServiceImpl.Config.class;
            }
        };
    }
}
