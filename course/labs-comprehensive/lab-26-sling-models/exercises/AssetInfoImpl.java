package com.demo.aem.models.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

import com.demo.aem.models.AssetInfo;

/**
 * Exercise: Implement AssetInfoImpl
 * 
 * TODO: Complete the implementation by:
 * 1. Adding @Model annotation (adaptables = Resource.class, adapters = AssetInfo.class)
 * 2. Injecting required fields with @Inject
 * 3. Using @Named for JCR property names
 * 4. Implementing all getter methods
 * 5. Adding fallback logic (e.g., use resource name if title is null)
 * 
 * Property mapping:
 * - jcr:title -> title
 * - jcr:description -> description
 * - dc:format -> mimeType
 * - jcr:data -> fileSize
 * - jcr:createdBy -> author
 * - jcr:created -> created
 * - cq:lastReplicated -> replicated (for isPublished check)
 */
@Model(adaptables = Resource.class, adapters = AssetInfo.class)
public class AssetInfoImpl implements AssetInfo {

    // TODO: Add @Inject fields for:
    // - Resource (use @Self for current resource)
    // - String title (mapped from jcr:title)
    // - String description (mapped from jcr:description)
    // - String mimeType (mapped from dc:format)
    // - Long fileSize (mapped from jcr:data)
    // - String author (mapped from jcr:createdBy)
    // - Calendar created (mapped from jcr:created)
    // - Calendar replicated (mapped from cq:lastReplicated)
    
    // HINT: Use @Named("property-name") for JCR property mapping
    // HINT: Use @Self for Resource injection
    
    @Inject
    private Resource resource;
    
    // TODO: Add more @Inject fields here...
    
    @Override
    public String getTitle() {
        // TODO: Return title if not null, otherwise return resource.getName()
        return null; // Replace with implementation
    }

    @Override
    public String getDescription() {
        // TODO: Return description
        return null;
    }

    @Override
    public String getMimeType() {
        // TODO: Return mimeType
        return null;
    }

    @Override
    public long getFileSize() {
        // TODO: Return fileSize, or 0 if null
        return 0;
    }

    @Override
    public String getPath() {
        // TODO: Return resource.getPath()
        return null;
    }

    @Override
    public String getAuthor() {
        // TODO: Return author
        return null;
    }

    @Override
    public java.util.Calendar getCreated() {
        // TODO: Return created date
        return null;
    }

    @Override
    public boolean isPublished() {
        // TODO: Return true if replicated is not null
        return false;
    }
}
