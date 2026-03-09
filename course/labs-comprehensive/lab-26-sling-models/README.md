# Lab 26: Sling Models - Expert Level

## Objective
Master Sling Models for AEM development - the modern way to create backend logic for AEM components.

---

## What You'll Learn
- Sling Models annotations (@Model, @Inject, @Named)
- Adapter patterns
- Request attributes vs resource properties
- Injecting OSGi services
- Custom adapters
- Exposing models to HTL

---

## Lab Structure

```
lab-26-sling-models/
├── README.md
├── spec/
│   └── SlingModelSpec.java
├── solution/
│   ├── AssetInfo.java
│   ├── AssetInfoImpl.java
│   ├── WorkflowStepInfo.java
│   └── WorkflowStepInfoImpl.java
└── exercises/
    ├── exercise-1-annotations.md
    ├── exercise-2-injection.md
    └── exercise-3-adapter.md
```

---

## Part 1: Understanding Sling Models

### What are Sling Models?

Sling Models replace the older `Use` classes and provide a clean way to map AEM resources to Java objects.

```
┌─────────────────────────────────────────────────────────────────┐
│                    SLING MODEL ARCHITECTURE                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────────┐         ┌──────────────┐                   │
│   │  HTL Template │────────▶│   Sling      │                   │
│   │  (View)       │         │   Model      │                   │
│   └──────────────┘         └──────┬───────┘                   │
│                                    │                            │
│                                    ▼                            │
│                          ┌──────────────┐                       │
│                          │  Resource /   │                       │
│                          │  Request      │                       │
│                          │  Properties   │                       │
│                          └──────────────┘                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Key Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Model` | Marks class as Sling Model | `@Model(adaptables = Resource.class)` |
| `@Inject` | Injects property/value | `@Inject String title` |
| `@Named` | Specifies property name | `@Named("jcr:title") String title` |
| `@Default` | Default value | `@Default(values = "default") String status` |
| `@Source` | Injection source | `@Source("request-attribute")` |
| `@PostConstruct` | Init method | `@PostConstruct public void init()` |

---

## Part 2: RED Phase - Write Tests

### Exercise 1: Create AssetInfo Interface

```java
package com.demo.aem.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = Resource.class)
public interface AssetInfo {
    
    String getTitle();
    
    String getDescription();
    
    String getMimeType();
    
    long getFileSize();
    
    String getPath();
    
    String getAuthor();
    
    java.util.Calendar getCreated();
    
    boolean isPublished();
}
```

### Exercise 2: Create AssetInfo Implementation with Tests

```java
package com.demo.aem.models.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

import com.demo.aem.models.AssetInfo;

@Model(adaptables = Resource.class, adapters = AssetInfo.class)
public class AssetInfoImpl implements AssetInfo {

    @Inject
    private Resource resource;

    @Inject
    @Named("jcr:title")
    private String title;

    @Inject
    @Named("jcr:description")
    private String description;

    @Inject
    @Named("dc:format")
    private String mimeType;

    @Inject
    @Named("jcr:data")
    private Long fileSize;

    @Inject
    @Named("jcr:createdBy")
    private String author;

    @Inject
    @Named("jcr:created")
    private java.util.Calendar created;

    @Inject
    @Named("cq:lastReplicated")
    private java.util.Calendar replicated;

    @Override
    public String getTitle() {
        return title != null ? title : resource.getName();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long getFileSize() {
        return fileSize != null ? fileSize : 0L;
    }

    @Override
    public String getPath() {
        return resource.getPath();
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public java.util.Calendar getCreated() {
        return created;
    }

    @Override
    public boolean isPublished() {
        return replicated != null;
    }
}
```

### Test the Model

```java
package com.demo.aem.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class AssetInfoSpec {

    private AemContext context = new AemContext();
    
    @BeforeEach
    void setUp() {
        context.addModelsForClasses(AssetInfo.class);
    }
    
    @Test
    void shouldMapResourceToModel() {
        // Given
        Resource resource = context.create().resource("/content/dam/image.png",
            "jcr:title", "Sample Image",
            "jcr:description", "A sample image",
            "dc:format", "image/png",
            "jcr:data", 1024L,
            "jcr:createdBy", "admin",
            "jcr:created", java.util.Calendar.getInstance());
        
        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);
        
        // Then
        assertNotNull(assetInfo);
        assertEquals("Sample Image", assetInfo.getTitle());
        assertEquals("image/png", assetInfo.getMimeType());
        assertEquals(1024L, assetInfo.getFileSize());
    }
    
    @Test
    void shouldReturnResourceNameWhenTitleMissing() {
        Resource resource = context.create().resource("/content/dam/asset.jpg");
        
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);
        
        assertEquals("asset.jpg", assetInfo.getTitle());
    }
}
```

---

## Part 3: GREEN Phase - Implementation

### Exercise 3: Injecting OSGi Services

```java
package com.demo.aem.models.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

import com.demo.aem.services.AssetService;
import com.demo.aem.models.AssetInfo;

@Model(adaptables = Resource.class)
public class AssetInfoWithServiceImpl implements AssetInfo {

    @Inject
    private Resource resource;

    @Reference
    private AssetService assetService;

    @Inject
    @Named("jcr:title")
    private String title;

    // ... other fields ...

    @Override
    public String getTitle() {
        if (title == null) {
            return assetService.getDefaultTitle(resource.getName());
        }
        return title;
    }
}
```

### Exercise 4: Request-Scoped Models

```java
@Model(adaptables = SlingHttpServletRequest.class)
public interface WorkflowStepModel {
    
    String getWorkflowTitle();
    
    String getStepName();
    
    String getAssignee();
    
    boolean isCompleted();
    
    String getStatus();
}
```

```java
@Model(adaptables = SlingHttpServletRequest.class)
public class WorkflowStepModelImpl implements WorkflowStepModel {

    @Inject
    private SlingHttpServletRequest request;

    @Inject
    @Source("request-attribute")
    private String workflowTitle;

    @Inject
    @Source("request-attribute")
    private String stepName;

    @Inject
    @Source("request-attribute")
    private String assignee;

    @Inject
    @Source("request-attribute")
    @Named("completed")
    private Boolean completed;

    @Override
    public String getWorkflowTitle() {
        return workflowTitle;
    }

    @Override
    public String getStepName() {
        return stepName;
    }

    @Override
    public String getAssignee() {
        return assignee;
    }

    @Override
    public boolean isCompleted() {
        return completed != null ? completed : false;
    }

    @Override
    public String getStatus() {
        return isCompleted() ? "COMPLETED" : "PENDING";
    }
}
```

### Exercise 5: Custom Adapter

```java
package com.demo.aem.models;

import org.apache.sling.api.resource.Resource;

public interface WorkflowMetadata {
    
    String getModelTitle();
    
    String getInitiator();
    
    java.util.List<String> getParticipants();
    
    java.util.Calendar getStartTime();
    
    String getStatus();
}
```

```java
package com.demo.aem.models;

import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

@Model(adaptables = Resource.class)
@Adapter(WorkflowMetadataAdapter.class)
public interface WorkflowMetadata {
    // interface methods
}
```

```java
package com.demo.aem.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.Adaptable;
import org.apache.sling.models.spi.AdapterFactory;

public class WorkflowMetadataAdapter implements AdapterFactory {
    
    @Override
    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
        if (adaptable instanceof Resource && type == WorkflowMetadata.class) {
            Resource resource = (Resource) adaptable;
            if (resource.getPath().startsWith("/var/workflow/models/")) {
                return (AdapterType) new WorkflowMetadataImpl(resource);
            }
        }
        return null;
    }
}
```

---

## Part 4: Exposing to HTL

### Register the Model

```xml
<!-- core/src/main/resources/SLING-INF/navigation/model.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="sling:Folder"
    forceCaching="true"/>
```

### Use in HTL

```html
<!-- ui.apps/src/main/content/jcr_root/apps/demo/components/assetinfo.html -->
<sly data-sly-use.assetInfo="com.demo.aem.models.AssetInfo"
     data-sly-use.workflowModel="com.demo.aem.models.WorkflowMetadata">
    
    <div class="asset-info">
        <h1>${assetInfo.title}</h1>
        <p>${assetInfo.description}</p>
        
        <div class="metadata">
            <span>Size: ${assetInfo.fileSize} bytes</span>
            <span>Type: ${assetInfo.mimeType}</span>
            <span>Author: ${assetInfo.author}</span>
            <span data-sly-test="${assetInfo.published}">Published ✓</span>
        </div>
    </div>
    
    <div class="workflow-info" data-sly-use.workflow="workflowModel">
        <h2>Workflow: ${workflow.modelTitle}</h2>
        <p>Status: ${workflow.status}</p>
    </div>
    
</sly>
```

---

## Part 5: Testing with AEM Context

### Complete Test Suite

```java
package com.demo.aem.models;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class AssetInfoCompleteSpec {

    private AemContext context;
    
    @BeforeEach
    void setUp() {
        context = new AemContext();
        context.addModelsForClasses(AssetInfoImpl.class);
    }
    
    @Test
    void shouldAdaptResourceToAssetInfo() {
        Resource resource = context.create().resource("/content/dam/test.png",
            "jcr:title", "Test Image",
            "dc:format", "image/png",
            "jcr:data", 2048L);
        
        AssetInfo info = resource.adaptTo(AssetInfo.class);
        
        assertNotNull(info);
        assertEquals("Test Image", info.getTitle());
        assertEquals("image/png", info.getMimeType());
    }
    
    @Test
    void shouldUseResourceNameAsFallbackForTitle() {
        Resource resource = context.create().resource("/content/dam/fallback.jpg");
        
        AssetInfo info = resource.adaptTo(AssetInfo.class);
        
        assertEquals("fallback.jpg", info.getTitle());
    }
    
    @Test
    void shouldReturnZeroForMissingFileSize() {
        Resource resource = context.create().resource("/content/dam/no-size.png");
        
        AssetInfo info = resource.adaptTo(AssetInfo.class);
        
        assertEquals(0L, info.getFileSize());
    }
    
    @Test
    void shouldDetectPublishedStatus() {
        Resource published = context.create().resource("/content/dam/published.jpg",
            "jcr:title", "Published",
            "cq:lastReplicated", java.util.Calendar.getInstance());
        
        Resource unpublished = context.create().resource("/content/dam/unpublished.jpg",
            "jcr:title", "Unpublished");
        
        assertTrue(published.adaptTo(AssetInfo.class).isPublished());
        assertFalse(unpublished.adaptTo(AssetInfo.class).isPublished());
    }
}
```

---

## Key Takeaways

1. **Sling Models are the modern approach** - Replace older Use classes
2. **Annotations drive injection** - Properties, resources, services
3. **Always test with AEM Context** - Unit test your models
4. **Adaptable is key** - Resource, Request, or SlingHttpServletRequest

---

## Verification Checklist

- [ ] AssetInfo interface created
- [ ] AssetInfoImpl with @Model annotation
- [ ] Properties injected with @Inject
- [ ] Resource fallback for missing title
- [ ] File size handling for null
- [ ] Published status detection
- [ ] HTL template using the model
- [ ] Unit tests passing

---

## Common Pitfalls

| Issue | Solution |
|-------|----------|
| Model not found | Add `@Model` annotation |
| Properties null | Check property name with `@Named` |
| Resource null | Use `@Self` for current resource |
| Service null | Add `@Reference` and bundle dependency |
| Test failures | Use `AemContext` for testing |

---

## Next Steps

- Lab 27: HTL Templates Deep Dive
- Lab 28: AEM Workflow Process Steps

---

## References

- [Sling Models Documentation](https://sling.apache.org/documentation/bundles/models.html)
- [AEM Developer Tutorial](https://experienceleague.adobe.com/docs/experience-manager-htl/using/htl.html)
