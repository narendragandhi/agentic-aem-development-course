package com.demo.aem.models;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AssetInfo Sling Model
 */
@ExtendWith(AemContextExtension.class)
class AssetInfoSpec {

    private AemContext context;

    @BeforeEach
    void setUp() {
        context = new AemContext();
        context.addModelsForClasses(AssetInfoImpl.class);
    }

    @Test
    void shouldMapResourcePropertiesToModel() {
        // Given
        Resource resource = context.create().resource("/content/dam/image.png",
            "jcr:title", "Sample Image",
            "jcr:description", "A beautiful sample image",
            "dc:format", "image/png",
            "jcr:data", 2048L,
            "jcr:createdBy", "admin");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertNotNull(assetInfo);
        assertEquals("Sample Image", assetInfo.getTitle());
        assertEquals("A beautiful sample image", assetInfo.getDescription());
        assertEquals("image/png", assetInfo.getMimeType());
        assertEquals(2048L, assetInfo.getFileSize());
        assertEquals("admin", assetInfo.getAuthor());
    }

    @Test
    void shouldUseResourceNameWhenTitleMissing() {
        // Given
        Resource resource = context.create().resource("/content/dam/photo.jpg");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertEquals("photo.jpg", assetInfo.getTitle());
    }

    @Test
    void shouldReturnZeroForMissingFileSize() {
        // Given
        Resource resource = context.create().resource("/content/dam/no-size.png");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertEquals(0L, assetInfo.getFileSize());
    }

    @Test
    void shouldReturnNullForMissingOptionalFields() {
        // Given
        Resource resource = context.create().resource("/content/dam/minimal.png");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertNull(assetInfo.getDescription());
        assertNull(assetInfo.getMimeType());
        assertNull(assetInfo.getAuthor());
        assertNull(assetInfo.getCreated());
    }

    @Test
    void shouldDetectPublishedStatus() {
        // Given - Published asset
        Resource published = context.create().resource("/content/dam/published.jpg",
            "jcr:title", "Published Image",
            "cq:lastReplicated", java.util.Calendar.getInstance());

        // Given - Unpublished asset
        Resource unpublished = context.create().resource("/content/dam/unpublished.jpg",
            "jcr:title", "Unpublished Image");

        // When/Then
        assertTrue(published.adaptTo(AssetInfo.class).isPublished());
        assertFalse(unpublished.adaptTo(AssetInfo.class).isPublished());
    }

    @Test
    void shouldReturnCorrectPath() {
        // Given
        Resource resource = context.create().resource("/content/dam/folder/asset.pdf");

        // When
        AssetInfo assetInfo = resource.adaptTo(AssetInfo.class);

        // Then
        assertEquals("/content/dam/folder/asset.pdf", assetInfo.getPath());
    }

    @Test
    void shouldHandleNullResource() {
        // Given - resource that doesn't exist
        Resource resource = context.resourceResolver().getResource("/content/dam/does-not-exist.png");

        // When
        AssetInfo assetInfo = resource != null ? resource.adaptTo(AssetInfo.class) : null;

        // Then
        assertNull(assetInfo);
    }
}
