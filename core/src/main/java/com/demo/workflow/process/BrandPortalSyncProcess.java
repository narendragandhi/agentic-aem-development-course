package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Brand Portal Sync Workflow Process
 *
 * Synchronizes approved assets to Adobe Brand Portal for external distribution.
 * Inspired by ACS AEM Commons "Brand Portal Sync" process.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                  BRAND PORTAL SYNC PROCESS                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                     SYNC FLOW                           │  │
 * │   │                                                         │  │
 * │   │   AEM DAM                         Brand Portal          │  │
 * │   │   ┌──────────────────┐           ┌──────────────────┐  │  │
 * │   │   │                  │           │                  │  │  │
 * │   │   │  /content/dam/   │  ══════▶  │   Brand Portal   │  │  │
 * │   │   │    approved/     │   SYNC    │     Assets       │  │  │
 * │   │   │                  │           │                  │  │  │
 * │   │   └──────────────────┘           └──────────────────┘  │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                   SYNC OPTIONS                          │  │
 * │   │                                                         │  │
 * │   │   ┌───────────────┐  ┌───────────────┐                 │  │
 * │   │   │  FULL SYNC    │  │METADATA ONLY  │                 │  │
 * │   │   │  - Asset      │  │  - Properties │                 │  │
 * │   │   │  - Metadata   │  │  - Tags       │                 │  │
 * │   │   │  - Renditions │  │  - Rights     │                 │  │
 * │   │   └───────────────┘  └───────────────┘                 │  │
 * │   │                                                         │  │
 * │   │   ┌───────────────┐  ┌───────────────┐                 │  │
 * │   │   │  UNPUBLISH    │  │    DELETE     │                 │  │
 * │   │   │  - Remove     │  │  - Permanent  │                 │  │
 * │   │   │    from BP    │  │    removal    │                 │  │
 * │   │   └───────────────┘  └───────────────┘                 │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   Sync Conditions:                                             │
 * │   • Asset must be in approved folder                          │
 * │   • Asset must have required metadata                         │
 * │   • Asset must pass brand guidelines validation               │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Brand Portal Sync Process",
        "process.description=Synchronizes approved assets to Brand Portal"
    }
)
public class BrandPortalSyncProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BrandPortalSyncProcess.class);

    private static final String ARG_SYNC_TYPE = "syncType";
    private static final String ARG_INCLUDE_RENDITIONS = "includeRenditions";
    private static final String ARG_VALIDATE_METADATA = "validateMetadata";

    public enum SyncType {
        FULL, METADATA_ONLY, UNPUBLISH, DELETE
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting Brand Portal sync for: {}", payloadPath);

        // Get configuration
        String syncTypeStr = metaDataMap.get(ARG_SYNC_TYPE, "FULL");
        boolean includeRenditions = metaDataMap.get(ARG_INCLUDE_RENDITIONS, true);
        boolean validateMetadata = metaDataMap.get(ARG_VALIDATE_METADATA, true);

        SyncType syncType = SyncType.valueOf(syncTypeStr);

        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Unable to obtain ResourceResolver");
        }

        Resource assetResource = resolver.getResource(payloadPath);
        if (assetResource == null) {
            throw new WorkflowException("Asset not found: " + payloadPath);
        }

        Asset asset = assetResource.adaptTo(Asset.class);
        if (asset == null) {
            LOG.warn("Resource is not a DAM asset: {}", payloadPath);
            return;
        }

        try {
            // Validate before sync
            if (validateMetadata && !validateAssetForSync(asset)) {
                LOG.warn("Asset failed validation for Brand Portal sync: {}", payloadPath);
                MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
                workflowMetadata.put("brandPortalSyncStatus", "VALIDATION_FAILED");
                return;
            }

            // Perform sync based on type
            switch (syncType) {
                case FULL:
                    performFullSync(asset, includeRenditions);
                    break;
                case METADATA_ONLY:
                    performMetadataSync(asset);
                    break;
                case UNPUBLISH:
                    performUnpublish(asset);
                    break;
                case DELETE:
                    performDelete(asset);
                    break;
            }

            // Update metadata to track sync
            updateSyncMetadata(asset, syncType);

            LOG.info("Brand Portal sync completed for: {} (type: {})", payloadPath, syncType);

        } catch (Exception e) {
            LOG.error("Failed to sync to Brand Portal: " + payloadPath, e);
            throw new WorkflowException("Brand Portal sync failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates an asset meets Brand Portal requirements.
     */
    private boolean validateAssetForSync(Asset asset) {
        // Check required metadata
        String title = asset.getMetadataValue("dc:title");
        if (title == null || title.isEmpty()) {
            LOG.warn("Asset missing required title metadata");
            return false;
        }

        // Check asset is in approved folder
        String path = asset.getPath();
        if (!path.contains("/approved/") && !path.contains("/brand-portal/")) {
            LOG.warn("Asset not in approved folder for Brand Portal sync");
            return false;
        }

        // Check for usage rights
        String usageTerms = asset.getMetadataValue("xmpRights:UsageTerms");
        if (usageTerms == null || usageTerms.contains("internal only")) {
            LOG.warn("Asset usage terms do not permit Brand Portal distribution");
            return false;
        }

        return true;
    }

    /**
     * Performs full asset sync to Brand Portal.
     */
    private void performFullSync(Asset asset, boolean includeRenditions) {
        LOG.info("Performing full sync for: {}", asset.getPath());

        // In production, this would call the Brand Portal API
        // Example: brandPortalClient.publish(asset, includeRenditions);

        // For demo, we log the action
        LOG.info("Would sync asset binary and {} to Brand Portal",
            includeRenditions ? "all renditions" : "original only");
    }

    /**
     * Syncs only metadata changes to Brand Portal.
     */
    private void performMetadataSync(Asset asset) {
        LOG.info("Performing metadata sync for: {}", asset.getPath());

        // In production, this would sync metadata only
        // Example: brandPortalClient.syncMetadata(asset);
    }

    /**
     * Unpublishes asset from Brand Portal.
     */
    private void performUnpublish(Asset asset) {
        LOG.info("Unpublishing from Brand Portal: {}", asset.getPath());

        // In production, this would unpublish
        // Example: brandPortalClient.unpublish(asset);
    }

    /**
     * Permanently deletes asset from Brand Portal.
     */
    private void performDelete(Asset asset) {
        LOG.info("Deleting from Brand Portal: {}", asset.getPath());

        // In production, this would delete
        // Example: brandPortalClient.delete(asset);
    }

    /**
     * Updates asset metadata to track sync status.
     */
    private void updateSyncMetadata(Asset asset, SyncType syncType) {
        try {
            Resource metadataResource = asset.adaptTo(Resource.class)
                .getChild("jcr:content/metadata");

            if (metadataResource != null) {
                org.apache.sling.api.resource.ModifiableValueMap props =
                    metadataResource.adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class);

                if (props != null) {
                    props.put("dam:brandPortalLastSync", Calendar.getInstance());
                    props.put("dam:brandPortalSyncType", syncType.name());
                    props.put("dam:brandPortalPublished",
                        syncType != SyncType.UNPUBLISH && syncType != SyncType.DELETE);

                    metadataResource.getResourceResolver().commit();
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to update sync metadata for: " + asset.getPath(), e);
        }
    }
}
