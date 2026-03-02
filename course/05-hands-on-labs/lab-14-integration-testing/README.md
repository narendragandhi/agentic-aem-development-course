# Lab 14: AEM Mock Integration Testing

## Objective
Build comprehensive integration tests using AEM Mocks that test services with real AEM context, Assets, and ResourceResolver, going beyond unit tests to validate actual AEM integration.

---

## Prerequisites
- Completed Labs 07-10 (Testing fundamentals)
- Understanding of AEM context (ResourceResolver, Asset API)
- Familiarity with io.wcm.testing.aem-mock

---

## Learning Outcomes
After completing this lab, you will be able to:
1. Configure AEM Mock for integration testing
2. Create mock DAM assets with metadata
3. Test services with real ResourceResolver
4. Validate workflow process integration
5. Test OSGi service dependencies

---

## Why Integration Testing?

| Test Type | Scope | Speed | Confidence |
|-----------|-------|-------|------------|
| Unit Tests | Single class | Fast | Low |
| **Integration Tests** | Multiple services + AEM context | Medium | High |
| E2E Tests | Full deployment | Slow | Highest |

---

## Part 1: Setting Up AEM Mock

### 1.1 Add Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.wcm</groupId>
    <artifactId>io.wcm.testing.aem-mock.junit5</artifactId>
    <version>5.1.2</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.apache.sling</groupId>
    <artifactId>org.apache.sling.testing.sling-mock.junit5</artifactId>
    <version>3.4.2</version>
    <scope>test</scope>
</dependency>
```

### 1.2 Basic Test Structure

```java
package com.demo.workflow.services.impl;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class SecurityScannerServiceIntegrationTest {

    private final AemContext context = new AemContext();

    @BeforeEach
    void setUp() {
        // Register services
        context.registerInjectActivateService(new SecurityScannerServiceImpl());
    }

    @Test
    void testWithRealAemContext() {
        // Test with actual AEM context
    }
}
```

---

## Part 2: RED Phase - Write Integration Tests

### 2.1 Create Integration Test File

Create `SecurityScannerServiceIntegrationTest.java`:

```java
package com.demo.workflow.services.impl;

import com.day.cq.dam.api.Asset;
import com.demo.workflow.services.SecurityScannerService;
import com.demo.workflow.services.SecurityScannerService.SecurityScanResult;
import com.demo.workflow.services.SecurityScannerService.Severity;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
@DisplayName("SecurityScannerService Integration Tests")
class SecurityScannerServiceIntegrationTest {

    private final AemContext context = new AemContext();
    private SecurityScannerService securityScanner;

    @BeforeEach
    void setUp() {
        // Load test content
        context.load().json("/test-content/dam-structure.json", "/content/dam");

        // Register service
        securityScanner = context.registerInjectActivateService(
            new SecurityScannerServiceImpl()
        );
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
            // Create test asset
            Asset asset = createTestAsset(
                "/content/dam/test/clean-image.png",
                "image/png",
                "Clean Image",
                "A safe description"
            );

            SecurityScanResult result = securityScanner.scanAsset(asset);

            assertTrue(result.getFindings().isEmpty());
            assertFalse(result.isBlocked());
        }

        @Test
        @DisplayName("should detect XSS in asset metadata")
        void shouldDetectXssInAssetMetadata() {
            Asset asset = createTestAsset(
                "/content/dam/test/xss-image.png",
                "image/png",
                "<script>alert('XSS')</script>",
                "Normal description"
            );

            SecurityScanResult result = securityScanner.scanAsset(asset);

            assertFalse(result.getFindings().isEmpty());
            assertTrue(result.getFindings().stream()
                .anyMatch(f -> f.getCategory().equals("XSS")));
            assertTrue(result.isBlocked());
        }

        @Test
        @DisplayName("should detect file type mismatch")
        void shouldDetectFileTypeMismatch() {
            // Create asset claiming to be PNG but with wrong content
            Asset asset = createTestAssetWithContent(
                "/content/dam/test/fake.png",
                "image/png",
                "This is not PNG content".getBytes()
            );

            SecurityScanResult result = securityScanner.scanAsset(asset);

            assertTrue(result.getFindings().stream()
                .anyMatch(f -> f.getCategory().equals("FILE_TYPE_MISMATCH")));
        }

        @Test
        @DisplayName("should detect script in SVG content")
        void shouldDetectScriptInSvg() {
            String svgWithScript = "<svg><script>alert('XSS')</script></svg>";
            Asset asset = createTestAssetWithContent(
                "/content/dam/test/malicious.svg",
                "image/svg+xml",
                svgWithScript.getBytes(StandardCharsets.UTF_8)
            );

            SecurityScanResult result = securityScanner.scanAsset(asset);

            assertTrue(result.getFindings().stream()
                .anyMatch(f -> f.getCategory().equals("EMBEDDED_SCRIPT")));
        }

        @Test
        @DisplayName("should handle missing original rendition")
        void shouldHandleMissingRendition() {
            // Create asset without content
            context.create().resource("/content/dam/test/empty-asset",
                "jcr:primaryType", "dam:Asset"
            );
            Asset asset = context.resourceResolver()
                .getResource("/content/dam/test/empty-asset")
                .adaptTo(Asset.class);

            // Should not throw exception
            SecurityScanResult result = securityScanner.scanAsset(asset);
            assertNotNull(result);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Workflow Integration Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When integrated with workflow")
    class WorkflowIntegration {

        @Test
        @DisplayName("should block critical findings")
        void shouldBlockCriticalFindings() {
            Asset asset = createTestAsset(
                "/content/dam/test/sql-attack.png",
                "image/png",
                "Safe title",
                "'; DROP TABLE users;--"
            );

            SecurityScanResult result = securityScanner.scanAsset(asset);

            assertTrue(result.isBlocked());
            assertEquals(Severity.CRITICAL, result.getOverallSeverity());
        }

        @Test
        @DisplayName("should report scan duration")
        void shouldReportScanDuration() {
            Asset asset = createTestAsset(
                "/content/dam/test/timing.png",
                "image/png",
                "Timing Test",
                "Description"
            );

            SecurityScanResult result = securityScanner.scanAsset(asset);

            assertTrue(result.getScanDurationMs() >= 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private Asset createTestAsset(String path, String mimeType,
                                   String title, String description) {
        // Create DAM asset structure
        context.create().resource(path,
            "jcr:primaryType", "dam:Asset"
        );
        context.create().resource(path + "/jcr:content",
            "jcr:primaryType", "dam:AssetContent"
        );
        context.create().resource(path + "/jcr:content/metadata",
            "jcr:primaryType", "nt:unstructured",
            "dc:title", title,
            "dc:description", description
        );
        context.create().resource(path + "/jcr:content/renditions",
            "jcr:primaryType", "nt:folder"
        );
        context.create().resource(path + "/jcr:content/renditions/original",
            "jcr:primaryType", "nt:file",
            "jcr:mimeType", mimeType
        );
        context.create().resource(path + "/jcr:content/renditions/original/jcr:content",
            "jcr:primaryType", "nt:resource",
            "jcr:data", new ByteArrayInputStream("test content".getBytes()),
            "jcr:mimeType", mimeType
        );

        return context.resourceResolver().getResource(path).adaptTo(Asset.class);
    }

    private Asset createTestAssetWithContent(String path, String mimeType, byte[] content) {
        context.create().resource(path,
            "jcr:primaryType", "dam:Asset"
        );
        context.create().resource(path + "/jcr:content/renditions/original/jcr:content",
            "jcr:primaryType", "nt:resource",
            "jcr:data", new ByteArrayInputStream(content),
            "jcr:mimeType", mimeType
        );

        return context.resourceResolver().getResource(path).adaptTo(Asset.class);
    }
}
```

### 2.2 Create Test Content JSON

Create `src/test/resources/test-content/dam-structure.json`:

```json
{
  "jcr:primaryType": "sling:OrderedFolder",
  "test": {
    "jcr:primaryType": "sling:OrderedFolder"
  }
}
```

---

## Part 3: GREEN Phase - Make Tests Pass

### 3.1 Ensure Service is Properly Annotated

```java
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {
    // ... implementation
}
```

### 3.2 Run Integration Tests

```bash
mvn test -pl core -Dtest=SecurityScannerServiceIntegrationTest
```

**Expected:** 7 tests passing

---

## Part 4: Testing Multiple Services Together

### 4.1 Test Service Dependencies

```java
@Nested
@DisplayName("When services interact")
class ServiceInteraction {

    @Test
    @DisplayName("should integrate with document scanner")
    void shouldIntegrateWithDocumentScanner() {
        // Register both services
        context.registerInjectActivateService(new DocumentSecurityScanner());
        SecurityScannerService scanner = context.registerInjectActivateService(
            new SecurityScannerServiceImpl()
        );

        // Create PDF asset
        String pdfContent = "%PDF-1.4\n/JavaScript (alert('xss'))\n%%EOF";
        Asset asset = createTestAssetWithContent(
            "/content/dam/test/script.pdf",
            "application/pdf",
            pdfContent.getBytes()
        );

        SecurityScanResult result = scanner.scanAsset(asset);

        // Should detect PDF JavaScript
        assertTrue(result.getFindings().stream()
            .anyMatch(f -> f.getCategory().contains("PDF")));
    }
}
```

---

## Part 5: Best Practices

### 5.1 Test Resource Cleanup

```java
@AfterEach
void tearDown() {
    // AemContext automatically cleans up
    // But for custom resources:
    context.resourceResolver().commit();
}
```

### 5.2 Loading Complex Content

```java
// Load from JSON
context.load().json("/test-content/complex-dam.json", "/content/dam");

// Load from classpath binary
context.load().binaryFile("/test-files/sample.pdf", "/content/dam/sample.pdf");
```

### 5.3 Testing OSGi Configuration

```java
@Test
void shouldUseConfiguredThreshold() {
    // Set OSGi config
    context.registerInjectActivateService(
        new SecurityScannerServiceImpl(),
        "maxFileSize", 1024 * 1024,
        "blockOnCritical", true
    );

    // Test with configured values
}
```

---

## Verification Checklist

- [ ] All 7 integration tests pass
- [ ] AemContext properly configured
- [ ] Test assets created with metadata
- [ ] Service injection working
- [ ] Workflow integration validated
- [ ] Cleanup happens automatically

---

## Bonus Challenges

1. **Add Workflow Process Tests:** Test WorkflowProcess implementations
2. **Add Servlet Tests:** Test SlingServlet with mock requests
3. **Add Sling Model Tests:** Test Sling Models with context
4. **Add Performance Tests:** Measure service response times

---

## References

- [wcm.io AEM Mock](https://wcm.io/testing/aem-mock/)
- [Sling Testing](https://sling.apache.org/documentation/development/sling-testing.html)
- [JUnit 5 Extensions](https://junit.org/junit5/docs/current/user-guide/#extensions)
