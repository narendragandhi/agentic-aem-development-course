# Lab 29: AEM Context Testing - Expert Level

## Objective
Master AEM testing with AEM Context - the industry standard for unit testing AEM components.

---

## What You'll Learn
- AEM Context setup
- Mocking resources, pages, assets
- Injecting Sling Models
- Mock services
- JCR session mocking
- Workflow testing
- Integration with JUnit 5

---

## Part 1: Understanding AEM Context

### What is AEM Context?

AEM Context (from wcm.io) provides a powerful mocking framework for AEM development:

```
┌─────────────────────────────────────────────────────────────────┐
│                      AEM CONTEXT TESTING                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │  @ExtendWith │    │  AemContext  │    │   Mock       │     │
│  │  AemContext  │───▶│    Setup     │───▶│  Resources   │     │
│  │  Extension   │    │              │    │              │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
│                             │                   │              │
│                             ▼                   ▼              │
│                      ┌──────────────┐    ┌──────────────┐       │
│                      │   Mock      │    │    Test      │       │
│                      │   Services  │    │   Classes    │       │
│                      └──────────────┘    └──────────────┘       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.wcm</groupId>
    <artifactId>io.wcm.testing.aem</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

---

## Part 2: Basic AEM Context Tests

### Exercise 1: Resource Mocking

```java
package com.demo.aem.testing;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class ResourceMockingSpec {

    private AemContext context;
    private ResourceResolver resolver;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        resolver = context.resourceResolver();
    }

    @Test
    void shouldCreateSimpleResource() {
        // Given
        Resource resource = context.create().resource("/content/page");

        // Then
        assertNotNull(resource);
        assertEquals("/content/page", resource.getPath());
    }

    @Test
    void shouldCreateResourceWithProperties() {
        // Given
        Resource resource = context.create().resource("/content/page",
            "jcr:title", "Test Page",
            "jcr:description", "A test page",
            "sling:resourceType", "demo/page");

        // Then
        assertEquals("Test Page", resource.getValueMap().get("jcr:title"));
        assertEquals("demo/page", resource.getValueMap().get("sling:resourceType"));
    }

    @Test
    void shouldCreateHierarchicalResources() {
        // Given
        Resource parent = context.create().resource("/content/parent",
            "jcr:title", "Parent");
        Resource child = context.create().resource(parent, "child",
            "jcr:title", "Child");

        // Then
        assertTrue(parent.hasChild("child"));
        assertEquals("Child", child.getValueMap().get("jcr:title"));
    }

    @Test
    void shouldGetResourceFromResolver() {
        // Given
        context.create().resource("/content/dam/asset.png",
            "jcr:primaryType", "dam:Asset");

        // When
        Resource resource = resolver.getResource("/content/dam/asset.png");

        // Then
        assertNotNull(resource);
    }
}
```

### Exercise 2: Page Mocking

```java
package com.demo.aem.testing;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class PageMockingSpec {

    private AemContext context;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        context.pageBuilder().create("/content/mypite", "demo-template");
    }

    @Test
    void shouldCreatePage() {
        // Given - in setUp

        // When
        Page page = context.pageManager().getPage("/content/mypite");

        // Then
        assertNotNull(page);
        assertEquals("/content/mypite", page.getPath());
    }

    @Test
    void shouldCreatePageWithContent() {
        // Given
        context.pageBuilder().create("/content/mypage", "demo-template",
            "jcr:title", "My Page Title",
            "jcr:description", "Page description");

        // When
        Page page = context.pageManager().getPage("/content/mypage");

        // Then
        assertEquals("My Page Title", page.getTitle());
        assertEquals("Page description", page.getDescription());
    }

    @Test
    void shouldGetPageContent() {
        // Given
        context.pageBuilder().create("/content/mypage", "demo-template");

        // When
        Page page = context.pageManager().getPage("/content/mypage");
        Resource content = page.getContentResource();

        // Then
        assertNotNull(content);
        assertTrue(content.getPath().endsWith("/jcr:content"));
    }
}
```

---

## Part 3: Sling Models in Tests

### Exercise 3: Test Sling Models

```java
package com.demo.aem.testing;

import com.demo.aem.models.AssetInfo;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class SlingModelTestingSpec {

    private AemContext context;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        // Register the model class
        context.addModelsForClasses(AssetInfoImpl.class);
    }

    @Test
    void shouldAdaptResourceToModel() {
        // Given
        Resource resource = context.create().resource("/content/dam/image.png",
            "jcr:title", "Test Image",
            "jcr:description", "A test image",
            "dc:format", "image/png",
            "jcr:data", 1024L,
            "jcr:createdBy", "admin");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertNotNull(assetInfo);
        assertEquals("Test Image", assetInfo.getTitle());
        assertEquals("image/png", assetInfo.getMimeType());
        assertEquals(1024L, assetInfo.getFileSize());
        assertEquals("admin", assetInfo.getAuthor());
    }

    @Test
    void shouldUseFallbackWhenTitleMissing() {
        // Given
        Resource resource = context.create().resource("/content/dam/asset.jpg");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertEquals("asset.jpg", assetInfo.getTitle());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        Resource resource = context.create().resource("/content/dam/no-data.png");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertEquals(0L, assetInfo.getFileSize());
        assertNull(assetInfo.getDescription());
    }
}
```

---

## Part 4: Mocking Services

### Exercise 4: Service Injection

```java
package com.demo.aem.testing;

import com.demo.aem.services.AssetService;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceMockingSpec {

    private AemContext context;

    @Mock
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        
        // Register mock service
        context.registerService(AssetService.class, assetService);
        
        // Register model
        context.addModelsForClasses(AssetInfoWithServiceImpl.class);
        
        // Setup mock behavior
        when(assetService.getDefaultTitle(anyString())).thenReturn("Default Title");
    }

    @Test
    void shouldInjectMockedService() {
        // Given
        Resource resource = context.create().resource("/content/dam/test.png");

        // When
        AssetInfoWithService info = resource.adaptTo(AssetInfoWithService.class);

        // Then
        assertNotNull(info);
        verify(assetService).getDefaultTitle("test.png");
    }
}
```

---

## Part 5: Workflow Testing

### Exercise 5: Test Workflow Process

```java
package com.demo.workflow.testing;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class WorkflowProcessTestingSpec {

    private AemContext context;

    @Mock
    private WorkflowSession workflowSession;

    @Mock
    private WorkItem workItem;

    @Mock
    private WorkflowData workflowData;

    @Mock
    private MetaDataMap metaDataMap;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        
        // Create test asset
        context.create().resource("/content/dam/test-asset.png",
            "jcr:primaryType", "dam:Asset");
        
        // Mock workflow data
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/dam/test-asset.png");
    }

    @Test
    void shouldProcessAsset() throws WorkflowException {
        // Given
        UpdateAssetMetadataProcess process = new UpdateAssetMetadataProcess();
        
        // Note: In full test, you'd inject mocked services
        // This is a simplified test
        
        // Then - verify process can be instantiated
        assertNotNull(process);
    }

    @Test
    void shouldAccessPayloadResource() {
        // Given
        String payloadPath = "/content/dam/test-asset.png";
        
        // When
        Resource resource = context.resourceResolver().getResource(payloadPath);
        
        // Then
        assertNotNull(resource);
        assertEquals(payloadPath, resource.getPath());
    }
}
```

---

## Part 6: Advanced Mocking

### Exercise 6: DAM Asset Mocking

```java
package com.demo.aem.testing;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.aem.mock.resolver.MockValueMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class DamAssetTestingSpec {

    private AemContext context;

    @BeforeEach
    void setUp() {
        context = new AemContext();
    }

    @Test
    void shouldCreateDAMAsset() {
        // Given - Create asset structure
        Resource assetResource = context.create().resource("/content/dam/image.png",
            "jcr:primaryType", "dam:Asset",
            "jcr:title", "Image");

        Resource contentResource = context.create().resource(assetResource, "jcr:content",
            "jcr:primaryType", "dam:AssetContent",
            "dc:format", "image/png",
            "dam:fileName", "image.png");

        // When - Adapt to Asset
        Asset asset = assetResource.adaptTo(Asset.class);

        // Then
        assertNotNull(asset);
        assertEquals("Image", asset.getName());
    }

    @Test
    void shouldGetAssetMetadata() {
        // Given
        Resource content = context.create().resource("/content/dam/test.jpg/jcr:content",
            "jcr:primaryType", "dam:AssetContent",
            "dc:title", "Test Image",
            "dc:format", "image/jpeg",
            "dam:width", 1920,
            "dam:height", 1080);

        // When
        MockValueMap props = content.getValueMap();

        // Then
        assertEquals("Test Image", props.get("dc:title", String.class));
        assertEquals("image/jpeg", props.get("dc:format", String.class));
    }

    @Test
    void shouldMockRendition() {
        // Given
        Resource original = context.create().resource(
            "/content/dam/test.png/jcr:content/renditions/original",
            "jcr:primaryType", "dam:AssetRendition");

        // When
        Rendition rendition = original.adaptTo(Rendition.class);

        // Then
        assertNotNull(rendition);
    }
}
```

---

## Part 7: Integration with Mockito

### Exercise 7: Combined Testing

```java
package com.demo.aem.testing;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import javax.jcr.Session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(AemContextExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CombinedTestingSpec {

    private AemContext context;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        context = new AemContext();
    }

    @Test
    void shouldMockSession() {
        // Given
        Session session = context.resourceResolver().adaptTo(Session.class);

        // Then
        assertNotNull(session);
    }

    @Test
    void shouldCreateMultipleResources() {
        // Given
        context.create().resource("/content/page1", "jcr:title", "Page 1");
        context.create().resource("/content/page2", "jcr:title", "Page 2");

        // When
        Resource page1 = context.resourceResolver().getResource("/content/page1");
        Resource page2 = context.resourceResolver().getResource("/content/page2");

        // Then
        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals("Page 1", page1.getValueMap().get("jcr:title"));
    }
}
```

---

## Best Practices

| Practice | Recommendation |
|----------|----------------|
| Use AemContext Extension | Always use `@ExtendWith(AemContextExtension.class)` |
| Register Models | Use `context.addModelsForClasses()` |
| Mock Services | Register with `context.registerService()` |
| Clean Setup | Use `@BeforeEach` for fresh context |
| Use Strictness | Set `Strictness.LENIENT` for partial mocks |

---

## Verification Checklist

- [ ] Basic resource mocking
- [ ] Page mocking
- [ ] Sling Model testing
- [ ] Service mocking
- [ ] Workflow process testing
- [ ] DAM asset testing
- [ ] Combined with Mockito

---

## References

- [wcm.io AEM Testing](https://wcm.io/testing/)
- [AEM Mocks](https://github.com/Adobe-Consulting-Services/acs-aem-commons/tree/master/bundle/src/main/java/com/adobe/acs/util)
