package com.demo.aem.models.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

import com.demo.aem.models.AssetInfo;

/**
 * Solution: AssetInfoImpl - Complete implementation
 */
@Model(adaptables = Resource.class, adapters = AssetInfo.class)
public class AssetInfoImpl implements AssetInfo {

    @Self
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
