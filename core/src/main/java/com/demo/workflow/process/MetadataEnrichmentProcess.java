package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata Enrichment Workflow Process
 *
 * Enriches asset metadata from various sources including:
 * - EXIF data extraction
 * - External API lookups
 * - Business rule application
 * - Auto-tagging based on folder structure
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                METADATA ENRICHMENT PROCESS                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌────────────────────────────────────────────────────────┐   │
 * │   │                   INPUT: Asset Path                    │   │
 * │   └────────────────────────┬───────────────────────────────┘   │
 * │                            │                                   │
 * │                            ▼                                   │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │              METADATA SOURCES                           │  │
 * │   │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌─────────┐ │  │
 * │   │  │   EXIF    │ │  External │ │  Business │ │  Folder │ │  │
 * │   │  │   Data    │ │    API    │ │   Rules   │ │  Tags   │ │  │
 * │   │  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └────┬────┘ │  │
 * │   │        └──────────┬──┴──────────┬──┴────────────┘      │  │
 * │   │                   ▼             ▼                      │  │
 * │   │           ┌─────────────────────────────┐              │  │
 * │   │           │     MERGED METADATA         │              │  │
 * │   │           └─────────────────────────────┘              │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                            │                                   │
 * │                            ▼                                   │
 * │   ┌────────────────────────────────────────────────────────┐   │
 * │   │              OUTPUT: Enriched Asset                    │   │
 * │   └────────────────────────────────────────────────────────┘   │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Metadata Enrichment Process",
        "process.description=Enriches asset metadata from multiple sources"
    }
)
public class MetadataEnrichmentProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataEnrichmentProcess.class);

    private static final String METADATA_PATH = "jcr:content/metadata";
    private static final String PN_ENRICHED = "enriched";
    private static final String PN_ENRICHED_DATE = "enrichedDate";
    private static final String PN_ENRICHMENT_SOURCE = "enrichmentSource";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting metadata enrichment for: {}", payloadPath);

        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Unable to obtain ResourceResolver");
        }

        Resource assetResource = resolver.getResource(payloadPath);
        if (assetResource == null) {
            throw new WorkflowException("Asset not found at path: " + payloadPath);
        }

        try {
            // Get or create metadata node
            Resource metadataResource = assetResource.getChild(METADATA_PATH);
            if (metadataResource == null) {
                LOG.warn("Metadata node not found for asset: {}", payloadPath);
                return;
            }

            ModifiableValueMap metadata = metadataResource.adaptTo(ModifiableValueMap.class);
            if (metadata == null) {
                throw new WorkflowException("Cannot modify metadata for: " + payloadPath);
            }

            // Collect enrichment data from various sources
            Map<String, Object> enrichmentData = new HashMap<>();

            // Source 1: Extract folder-based metadata
            enrichFromFolderStructure(payloadPath, enrichmentData);

            // Source 2: Apply business rules
            applyBusinessRules(assetResource, enrichmentData);

            // Source 3: Auto-generate descriptive metadata
            generateDescriptiveMetadata(assetResource, enrichmentData);

            // Apply enrichment data to asset
            for (Map.Entry<String, Object> entry : enrichmentData.entrySet()) {
                if (!metadata.containsKey(entry.getKey())) {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }

            // Mark asset as enriched
            metadata.put(PN_ENRICHED, true);
            metadata.put(PN_ENRICHED_DATE, Calendar.getInstance());
            metadata.put(PN_ENRICHMENT_SOURCE, "MetadataEnrichmentProcess");

            resolver.commit();
            LOG.info("Metadata enrichment completed for: {}", payloadPath);

        } catch (PersistenceException e) {
            LOG.error("Failed to persist metadata enrichment for: " + payloadPath, e);
            throw new WorkflowException("Metadata enrichment failed", e);
        }
    }

    /**
     * Extracts metadata from folder structure.
     * Example: /content/dam/brand/product-line/category/asset.jpg
     * -> brand, product-line, category as metadata
     */
    private void enrichFromFolderStructure(String assetPath, Map<String, Object> enrichmentData) {
        String[] pathParts = assetPath.split("/");

        // Skip /content/dam prefix
        if (pathParts.length > 4) {
            // Assuming structure: /content/dam/{brand}/{product-line}/{category}/...
            if (pathParts.length > 3) {
                enrichmentData.put("dam:brand", pathParts[3]);
            }
            if (pathParts.length > 4) {
                enrichmentData.put("dam:productLine", pathParts[4]);
            }
            if (pathParts.length > 5) {
                enrichmentData.put("dam:category", pathParts[5]);
            }
        }
    }

    /**
     * Applies business rules to derive metadata.
     */
    private void applyBusinessRules(Resource assetResource, Map<String, Object> enrichmentData) {
        String mimeType = assetResource.getValueMap().get("jcr:content/jcr:mimeType", String.class);

        if (mimeType != null) {
            // Categorize by asset type
            if (mimeType.startsWith("image/")) {
                enrichmentData.put("dam:assetClass", "Image");
            } else if (mimeType.startsWith("video/")) {
                enrichmentData.put("dam:assetClass", "Video");
            } else if (mimeType.equals("application/pdf")) {
                enrichmentData.put("dam:assetClass", "Document");
            }

            // Set usage rights based on folder
            String path = assetResource.getPath();
            if (path.contains("/approved/")) {
                enrichmentData.put("dam:usageTerms", "Approved for external use");
                enrichmentData.put("dam:status", "approved");
            } else if (path.contains("/internal/")) {
                enrichmentData.put("dam:usageTerms", "Internal use only");
                enrichmentData.put("dam:status", "internal");
            }
        }
    }

    /**
     * Generates descriptive metadata based on asset properties.
     */
    private void generateDescriptiveMetadata(Resource assetResource, Map<String, Object> enrichmentData) {
        String assetName = assetResource.getName();

        // Clean up filename to generate title if not present
        String generatedTitle = assetName
            .replaceAll("\\.[^.]+$", "")  // Remove extension
            .replaceAll("[-_]", " ")       // Replace separators with spaces
            .replaceAll("\\s+", " ")       // Normalize whitespace
            .trim();

        // Capitalize first letter of each word
        String[] words = generatedTitle.split(" ");
        StringBuilder titleBuilder = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                titleBuilder.append(Character.toUpperCase(word.charAt(0)))
                           .append(word.substring(1).toLowerCase())
                           .append(" ");
            }
        }

        enrichmentData.put("dc:titleGenerated", titleBuilder.toString().trim());
    }
}
