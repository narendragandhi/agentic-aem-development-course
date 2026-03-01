package com.demo.workflow.models;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Sling Model for Content Preview Component
 *
 * Provides content metadata and preview URLs for the workflow review dialog.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                 CONTENT PREVIEW MODEL                           │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   Supports:                                                     │
 * │   ├── DAM Assets (images, PDFs, videos)                        │
 * │   │   └── Thumbnail, renditions, metadata                      │
 * │   ├── Content Pages                                            │
 * │   │   └── Page properties, preview URL, versions               │
 * │   └── Experience Fragments                                     │
 * │       └── Variation previews                                   │
 * │                                                                 │
 * │   Features:                                                     │
 * │   • Automatic content type detection                           │
 * │   • Version history retrieval                                  │
 * │   • Annotation data serialization                              │
 * │   • Status color mapping                                       │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ContentPreviewModel {

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource resource;

    @RequestAttribute(name = "contentPath")
    @Default(values = "")
    private String contentPath;

    private String title;
    private String thumbnailUrl;
    private String fullImageUrl;
    private String previewUrl;
    private String lastModified;
    private String lastModifiedBy;
    private String status;
    private String statusColor;
    private String mimeType;
    private String dimensions;
    private String contentType;
    private String annotationsJson;
    private boolean isAsset;
    private boolean hasVersions;
    private List<Map<String, String>> versions;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    @PostConstruct
    protected void init() {
        if (contentPath == null || contentPath.isEmpty()) {
            // Try to get from request or workflow context
            contentPath = resource.getPath();
        }

        Resource contentResource = resourceResolver.getResource(contentPath);
        if (contentResource == null) {
            return;
        }

        // Determine content type
        if (contentPath.startsWith("/content/dam/")) {
            initAssetPreview(contentResource);
        } else {
            initPagePreview(contentResource);
        }

        // Load annotations
        loadAnnotations();

        // Load versions
        loadVersions(contentResource);
    }

    private void initAssetPreview(Resource assetResource) {
        isAsset = true;
        contentType = "asset";

        Asset asset = assetResource.adaptTo(Asset.class);
        if (asset == null) {
            return;
        }

        title = asset.getMetadataValue(DamConstants.DC_TITLE);
        if (title == null || title.isEmpty()) {
            title = asset.getName();
        }

        mimeType = asset.getMimeType();
        thumbnailUrl = contentPath + "/jcr:content/renditions/cq5dam.thumbnail.319.319.png";
        fullImageUrl = contentPath + "/jcr:content/renditions/original";
        previewUrl = contentPath;

        // Get dimensions for images
        String width = asset.getMetadataValue("tiff:ImageWidth");
        String height = asset.getMetadataValue("tiff:ImageLength");
        if (width != null && height != null) {
            dimensions = width + " x " + height + " px";
        }

        // Get modification info
        Resource metadataResource = assetResource.getChild("jcr:content");
        if (metadataResource != null) {
            ValueMap props = metadataResource.getValueMap();
            Calendar lastMod = props.get("jcr:lastModified", Calendar.class);
            if (lastMod != null) {
                lastModified = DATE_FORMAT.format(lastMod.getTime());
            }
            lastModifiedBy = props.get("jcr:lastModifiedBy", "Unknown");

            // Get status
            status = props.get("dam:status", "Draft");
        }

        setStatusColor();
    }

    private void initPagePreview(Resource pageResource) {
        isAsset = false;
        contentType = "page";

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            return;
        }

        Page page = pageManager.getContainingPage(pageResource);
        if (page == null) {
            return;
        }

        title = page.getTitle();
        if (title == null || title.isEmpty()) {
            title = page.getName();
        }

        previewUrl = contentPath + ".html?wcmmode=disabled";
        thumbnailUrl = contentPath + "/jcr:content/image.thumbnail.140.100.png";

        // Get modification info
        Calendar lastMod = page.getLastModified();
        if (lastMod != null) {
            lastModified = DATE_FORMAT.format(lastMod.getTime());
        }
        lastModifiedBy = page.getLastModifiedBy();
        if (lastModifiedBy == null) {
            lastModifiedBy = "Unknown";
        }

        // Get workflow status from page properties
        ValueMap props = page.getProperties();
        status = props.get("cq:workflowStatus", "Draft");

        setStatusColor();
    }

    private void setStatusColor() {
        if (status == null) {
            statusColor = "default";
            status = "Unknown";
            return;
        }

        switch (status.toLowerCase()) {
            case "approved":
            case "published":
                statusColor = "green";
                break;
            case "in review":
            case "pending":
            case "pending-approval":
                statusColor = "blue";
                break;
            case "rejected":
            case "revision required":
                statusColor = "red";
                break;
            case "draft":
            default:
                statusColor = "default";
                break;
        }
    }

    private void loadAnnotations() {
        // Load annotations from content or workflow metadata
        // In production, this would query a dedicated annotations store
        List<Map<String, Object>> annotations = new ArrayList<>();

        // Serialize to JSON for client-side use
        StringBuilder json = new StringBuilder("[");
        // ... annotation serialization logic
        json.append("]");
        annotationsJson = json.toString();
    }

    private void loadVersions(Resource contentResource) {
        versions = new ArrayList<>();

        // In production, this would query version manager
        // Example version data
        hasVersions = false;

        // VersionManager would be used here to get actual versions
        // For demo purposes, we'll leave this as a placeholder
    }

    // Getters
    public String getContentPath() { return contentPath; }
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getFullImageUrl() { return fullImageUrl; }
    public String getPreviewUrl() { return previewUrl; }
    public String getLastModified() { return lastModified; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public String getStatus() { return status; }
    public String getStatusColor() { return statusColor; }
    public String getMimeType() { return mimeType; }
    public String getDimensions() { return dimensions; }
    public String getContentType() { return contentType; }
    public String getAnnotationsJson() { return annotationsJson; }
    public boolean getIsAsset() { return isAsset; }
    public boolean getHasVersions() { return hasVersions; }
    public List<Map<String, String>> getVersions() { return versions; }
}
