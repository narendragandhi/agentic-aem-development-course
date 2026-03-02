package com.demo.workflow.services.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.demo.workflow.services.SecurityScannerService;
import com.demo.workflow.services.SecurityScannerService.*;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for SecurityScannerService with AEM Mock context.
 *
 * Tests the service against realistic AEM Asset structures.
 */
@ExtendWith(AemContextExtension.class)
@DisplayName("SecurityScannerService Integration Tests")
class SecurityScannerServiceIntegrationTest {

    private final AemContext context = new AemContext();
    private SecurityScannerService service;

    @BeforeEach
    void setUp() {
        service = new SecurityScannerServiceImpl();
        context.registerService(SecurityScannerService.class, service);

        // Load test content
        context.load().json("/test-content/dam-structure.json", "/content/dam");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Asset Scanning Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning DAM assets")
    class AssetScanning {

        @Test
        @DisplayName("should scan asset with clean metadata")
        void shouldScanAssetWithCleanMetadata() {
            // Create mock asset with clean metadata
            Asset asset = createMockAsset(
                "/content/dam/test/clean-image.png",
                "image/png",
                "Clean Image Title",
                "A safe description",
                createPngBytes()
            );

            SecurityScanResult result = service.scanAsset(asset);

            assertNotNull(result);
            assertFalse(result.isBlocked());
            assertTrue(result.getFindings().isEmpty() ||
                       !result.hasCriticalFindings());
        }

        @Test
        @DisplayName("should detect XSS in asset title")
        void shouldDetectXssInAssetTitle() {
            Asset asset = createMockAsset(
                "/content/dam/test/malicious.png",
                "image/png",
                "<script>alert('xss')</script>",
                "Normal description",
                createPngBytes()
            );

            SecurityScanResult result = service.scanAsset(asset);

            assertTrue(result.hasCriticalFindings());
            assertTrue(result.isBlocked());
            assertTrue(result.getFindings().stream()
                .anyMatch(f -> f.getCategory().equals("XSS")));
        }

        @Test
        @DisplayName("should detect file type mismatch")
        void shouldDetectFileTypeMismatch() {
            // PNG bytes but declared as JPEG
            Asset asset = createMockAsset(
                "/content/dam/test/suspicious.jpg",
                "image/jpeg",
                "Normal Title",
                "Normal description",
                createPngBytes()  // PNG magic bytes
            );

            SecurityScanResult result = service.scanAsset(asset);

            assertTrue(result.hasHighFindings() || result.hasCriticalFindings());
            assertTrue(result.getFindings().stream()
                .anyMatch(f -> f.getCategory().equals("FILE_TYPE_MISMATCH")));
        }

        @Test
        @DisplayName("should scan SVG for embedded scripts")
        void shouldScanSvgForEmbeddedScripts() {
            String maliciousSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\">" +
                "<script>alert('xss')</script>" +
                "<rect width=\"100\" height=\"100\"/>" +
                "</svg>";

            Asset asset = createMockAsset(
                "/content/dam/test/malicious.svg",
                "image/svg+xml",
                "Vector Image",
                "An SVG file",
                maliciousSvg.getBytes(StandardCharsets.UTF_8)
            );

            SecurityScanResult result = service.scanAsset(asset);

            assertTrue(result.hasHighFindings());
            assertTrue(result.getFindings().stream()
                .anyMatch(f -> f.getCategory().equals("EMBEDDED_SCRIPT")));
        }

        @Test
        @DisplayName("should measure scan duration")
        void shouldMeasureScanDuration() {
            Asset asset = createMockAsset(
                "/content/dam/test/image.png",
                "image/png",
                "Test Image",
                "Description",
                createPngBytes()
            );

            SecurityScanResult result = service.scanAsset(asset);

            assertTrue(result.getScanDurationMs() >= 0);
            assertTrue(result.getScanDurationMs() < 5000); // Should complete in <5s
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Workflow Integration Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When integrated with workflow")
    class WorkflowIntegration {

        @Test
        @DisplayName("should provide actionable findings for workflow routing")
        void shouldProvideActionableFindingsForWorkflowRouting() {
            Asset asset = createMockAsset(
                "/content/dam/uploads/user-file.pdf",
                "application/pdf",
                "'; DROP TABLE users; --",  // SQL injection in title
                "Normal description",
                createPdfBytes()
            );

            SecurityScanResult result = service.scanAsset(asset);

            // Workflow can use these to route
            assertNotNull(result.getOverallSeverity());
            assertNotNull(result.isBlocked());

            // Findings have enough detail for audit
            for (SecurityFinding finding : result.getFindings()) {
                assertNotNull(finding.getId());
                assertNotNull(finding.getCategory());
                assertNotNull(finding.getSeverity());
                assertNotNull(finding.getDescription());
            }
        }

        @Test
        @DisplayName("should handle batch scanning efficiently")
        void shouldHandleBatchScanningEfficiently() {
            long startTime = System.currentTimeMillis();

            // Scan multiple assets
            for (int i = 0; i < 10; i++) {
                Asset asset = createMockAsset(
                    "/content/dam/batch/file-" + i + ".png",
                    "image/png",
                    "File " + i,
                    "Description " + i,
                    createPngBytes()
                );
                service.scanAsset(asset);
            }

            long duration = System.currentTimeMillis() - startTime;

            // 10 scans should complete in under 2 seconds
            assertTrue(duration < 2000,
                "Batch scanning took too long: " + duration + "ms");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private Asset createMockAsset(String path, String mimeType,
                                   String title, String description,
                                   byte[] content) {
        Asset asset = mock(Asset.class);
        Rendition original = mock(Rendition.class);

        when(asset.getPath()).thenReturn(path);
        when(asset.getName()).thenReturn(path.substring(path.lastIndexOf('/') + 1));
        when(asset.getMimeType()).thenReturn(mimeType);
        when(asset.getMetadataValue("dc:title")).thenReturn(title);
        when(asset.getMetadataValue("dc:description")).thenReturn(description);
        when(asset.getOriginal()).thenReturn(original);
        when(original.getStream()).thenReturn(new ByteArrayInputStream(content));

        return asset;
    }

    private byte[] createPngBytes() {
        return new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };
    }

    private byte[] createJpegBytes() {
        return new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };
    }

    private byte[] createPdfBytes() {
        return "%PDF-1.4\n%test".getBytes(StandardCharsets.UTF_8);
    }
}
