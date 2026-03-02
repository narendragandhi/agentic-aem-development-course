package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService;
import com.demo.workflow.services.SecurityScannerService.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Specification Tests for SecurityScannerService.
 *
 * Tests: 15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityScannerService Specification")
class SecurityScannerServiceSpec {

    private SecurityScannerService service;

    @BeforeEach
    void setUp() {
        service = new SecurityScannerServiceImpl();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 1: XSS Detection (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning metadata for XSS")
    class XssDetection {

        @Test
        @DisplayName("should detect script tags in metadata")
        void shouldDetectScriptTags() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("dc:title", "<script>alert('xss')</script>");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertFalse(findings.isEmpty());
            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("XSS") &&
                f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect javascript: protocol")
        void shouldDetectJavascriptProtocol() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("dc:description", "Click <a href='javascript:void(0)'>here</a>");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("XSS")));
        }

        @Test
        @DisplayName("should detect event handlers")
        void shouldDetectEventHandlers() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("customField", "<img src=x onerror=alert(1)>");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should return empty for clean metadata")
        void shouldReturnEmptyForClean() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("dc:title", "My Safe Document");
            metadata.put("dc:description", "A normal description with no scripts");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 2: SQL Injection Detection (3 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning for SQL injection")
    class SqlInjectionDetection {

        @Test
        @DisplayName("should detect OR 1=1 pattern")
        void shouldDetectOrPattern() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("searchQuery", "' OR 1=1 --");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("SQL_INJECTION")));
        }

        @Test
        @DisplayName("should detect UNION SELECT")
        void shouldDetectUnionSelect() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filter", "1 UNION SELECT * FROM users");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("SQL_INJECTION") &&
                f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect DROP TABLE")
        void shouldDetectDropTable() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("input", "'; DROP TABLE users; --");

            List<SecurityFinding> findings = service.scanMetadata(metadata);

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("SQL_INJECTION")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 3: File Type Validation (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When validating file types")
    class FileTypeValidationTests {

        @Test
        @DisplayName("should accept matching file type")
        void shouldAcceptMatchingType() {
            // PNG magic bytes
            byte[] pngHeader = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            InputStream content = new ByteArrayInputStream(pngHeader);

            FileTypeValidation result = service.validateFileType(
                content, "image/png", "image.png");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("should reject mismatched header and extension")
        void shouldRejectMismatch() {
            // PNG magic bytes but .jpg extension
            byte[] pngHeader = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            InputStream content = new ByteArrayInputStream(pngHeader);

            FileTypeValidation result = service.validateFileType(
                content, "image/jpeg", "image.jpg");

            assertFalse(result.isValid());
            assertEquals(Severity.HIGH, result.getSeverity());
        }

        @Test
        @DisplayName("should detect double extension attack")
        void shouldDetectDoubleExtension() {
            byte[] content = "test".getBytes();
            InputStream stream = new ByteArrayInputStream(content);

            FileTypeValidation result = service.validateFileType(
                stream, "application/pdf", "document.pdf.exe");

            assertFalse(result.isValid());
            assertEquals(Severity.CRITICAL, result.getSeverity());
        }

        @Test
        @DisplayName("should detect null byte injection")
        void shouldDetectNullByte() {
            byte[] content = "test".getBytes();
            InputStream stream = new ByteArrayInputStream(content);

            FileTypeValidation result = service.validateFileType(
                stream, "application/pdf", "document.pdf%00.exe");

            assertFalse(result.isValid());
            assertEquals(Severity.CRITICAL, result.getSeverity());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 4: Embedded Script Detection (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning for embedded scripts")
    class EmbeddedScriptDetection {

        @Test
        @DisplayName("should detect JavaScript in SVG")
        void shouldDetectJsInSvg() {
            String svgWithScript = "<svg><script>alert('xss')</script></svg>";
            InputStream content = new ByteArrayInputStream(
                svgWithScript.getBytes(StandardCharsets.UTF_8));

            List<SecurityFinding> findings = service.scanForEmbeddedScripts(
                content, "image/svg+xml");

            assertTrue(findings.stream().anyMatch(f ->
                f.getCategory().equals("EMBEDDED_SCRIPT") &&
                f.getSeverity() == Severity.HIGH));
        }

        @Test
        @DisplayName("should detect JavaScript in HTML")
        void shouldDetectJsInHtml() {
            String htmlWithScript = "<html><body><script src='evil.js'></script></body></html>";
            InputStream content = new ByteArrayInputStream(
                htmlWithScript.getBytes(StandardCharsets.UTF_8));

            List<SecurityFinding> findings = service.scanForEmbeddedScripts(
                content, "text/html");

            assertFalse(findings.isEmpty());
        }

        @Test
        @DisplayName("should return empty for safe image")
        void shouldReturnEmptyForSafeImage() {
            byte[] pngHeader = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47};
            InputStream content = new ByteArrayInputStream(pngHeader);

            List<SecurityFinding> findings = service.scanForEmbeddedScripts(
                content, "image/png");

            assertTrue(findings.isEmpty());
        }

        @Test
        @DisplayName("should detect onload handlers in SVG")
        void shouldDetectOnloadInSvg() {
            String svgWithOnload = "<svg onload='alert(1)'></svg>";
            InputStream content = new ByteArrayInputStream(
                svgWithOnload.getBytes(StandardCharsets.UTF_8));

            List<SecurityFinding> findings = service.scanForEmbeddedScripts(
                content, "image/svg+xml");

            assertTrue(findings.stream().anyMatch(f ->
                f.getSeverity() == Severity.HIGH));
        }
    }
}
