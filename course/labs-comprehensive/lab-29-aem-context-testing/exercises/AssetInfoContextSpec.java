package com.demo.aem.testing;

import com.demo.aem.models.AssetInfo;
import com.demo.aem.models.impl.AssetInfoImpl;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercise: Write tests for Sling Models using AEM Context
 * 
 * TODO: Complete the tests below
 */
@ExtendWith(AemContextExtension.class)
class AssetInfoContextSpec {

    private AemContext context;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        // TODO: Register the AssetInfoImpl model
    }

    @Test
    void shouldMapTitleFromResource() {
        // Given: Create a resource with jcr:title property
        // Resource resource = context.create().resource("/content/dam/test.png",
        //     "jcr:title", "Test Title");

        // When: Adapt to AssetInfo
        // AssetInfo info = resource.adaptTo(AssetInfo.class);

        // Then: Verify title is mapped
        // assertEquals("Test Title", info.getTitle());
        
        // TODO: Write this test
        assertTrue(true); // Placeholder
    }

    @Test
    void shouldHandleMissingTitle() {
        // Given: Create resource without title
        // Resource resource = context.create().resource("/content/dam/no-title.png");

        // When: Adapt to AssetInfo
        // AssetInfo info = resource.adaptTo(AssetInfo.class);

        // Then: Should use resource name as fallback
        // assertEquals("no-title.png", info.getTitle());
        
        // TODO: Write this test
        assertTrue(true);
    }

    @Test
    void shouldReturnZeroForMissingFileSize() {
        // TODO: Write this test
        assertTrue(true);
    }

    @Test
    void shouldDetectPublishedStatus() {
        // Given: Create published asset
        // Resource published = context.create().resource("/content/dam/published.png",
        //     "cq:lastReplicated", Calendar.getInstance());
        
        // Given: Create unpublished asset  
        // Resource unpublished = context.create().resource("/content/dam/unpublished.png");

        // Then: Check published status
        // assertTrue(published.adaptTo(AssetInfo.class).isPublished());
        // assertFalse(unpublished.adaptTo(AssetInfo.class).isPublished());
        
        // TODO: Write this test
        assertTrue(true);
    }
}
