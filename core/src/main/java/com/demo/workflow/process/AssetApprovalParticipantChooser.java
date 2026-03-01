package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Asset Approval Participant Chooser
 *
 * Dynamically selects approvers based on asset properties:
 * - Asset type (image, video, document)
 * - Asset folder/path
 * - Metadata (brand, region, sensitivity level)
 * - File size thresholds
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                  ASSET APPROVAL PARTICIPANT CHOOSER                     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │   ┌─────────────────────────────────────────────────────────────────┐  │
 * │   │                    DECISION MATRIX                               │  │
 * │   │                                                                  │  │
 * │   │   Asset Type        │  Approval Level  │  Assigned Group        │  │
 * │   │   ─────────────────────────────────────────────────────────────  │  │
 * │   │   Image             │  Level 1         │  image-reviewers       │  │
 * │   │   Video             │  Level 1         │  video-reviewers       │  │
 * │   │   Document          │  Level 1         │  doc-reviewers         │  │
 * │   │   ─────────────────────────────────────────────────────────────  │  │
 * │   │   High Sensitivity  │  Level 2+        │  senior-approvers      │  │
 * │   │   Brand Assets      │  Level 2+        │  brand-managers        │  │
 * │   │   Legal Content     │  Level 2+        │  legal-reviewers       │  │
 * │   │                                                                  │  │
 * │   └─────────────────────────────────────────────────────────────────┘  │
 * │                                                                         │
 * │   Approval Levels:                                                     │
 * │   • Level 1: Initial review by content-type specialists               │
 * │   • Level 2: Manager/supervisor review                                │
 * │   • Level 3: Senior/director approval (for sensitive content)         │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = ParticipantStepChooser.class,
    property = {
        "chooser.label=Asset Approval Participant Chooser"
    }
)
@Designate(ocd = AssetApprovalParticipantChooser.Config.class)
public class AssetApprovalParticipantChooser implements ParticipantStepChooser {

    private static final Logger LOG = LoggerFactory.getLogger(AssetApprovalParticipantChooser.class);

    @ObjectClassDefinition(
        name = "Asset Approval Participant Chooser Configuration",
        description = "Maps asset types and properties to approval groups"
    )
    public @interface Config {

        @AttributeDefinition(
            name = "Image Reviewers Group",
            description = "Group for image asset review"
        )
        String imageReviewers() default "image-reviewers";

        @AttributeDefinition(
            name = "Video Reviewers Group",
            description = "Group for video asset review"
        )
        String videoReviewers() default "video-reviewers";

        @AttributeDefinition(
            name = "Document Reviewers Group",
            description = "Group for document asset review"
        )
        String documentReviewers() default "document-reviewers";

        @AttributeDefinition(
            name = "Default Reviewers Group",
            description = "Fallback group for unmatched assets"
        )
        String defaultReviewers() default "content-reviewers";

        @AttributeDefinition(
            name = "Senior Approvers Group",
            description = "Group for Level 2 approvals"
        )
        String seniorApprovers() default "senior-approvers";

        @AttributeDefinition(
            name = "Brand Managers Group",
            description = "Group for brand asset approvals"
        )
        String brandManagers() default "brand-managers";

        @AttributeDefinition(
            name = "Legal Reviewers Group",
            description = "Group for legal content review"
        )
        String legalReviewers() default "legal-reviewers";

        @AttributeDefinition(
            name = "Directors Group",
            description = "Group for Level 3 (final) approvals"
        )
        String directors() default "content-directors";

        @AttributeDefinition(
            name = "Large File Size Threshold (bytes)",
            description = "Files above this size require senior approval"
        )
        long largeFileThreshold() default 52428800L; // 50MB
    }

    private Config config;

    // MIME type to reviewer group mapping
    private Map<String, String> mimeTypeToGroup;

    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
        initializeMimeTypeMapping();
        LOG.info("Asset Approval Participant Chooser activated");
    }

    private void initializeMimeTypeMapping() {
        mimeTypeToGroup = new HashMap<>();

        // Image types
        mimeTypeToGroup.put("image/jpeg", config.imageReviewers());
        mimeTypeToGroup.put("image/png", config.imageReviewers());
        mimeTypeToGroup.put("image/gif", config.imageReviewers());
        mimeTypeToGroup.put("image/tiff", config.imageReviewers());
        mimeTypeToGroup.put("image/webp", config.imageReviewers());
        mimeTypeToGroup.put("image/svg+xml", config.imageReviewers());

        // Video types
        mimeTypeToGroup.put("video/mp4", config.videoReviewers());
        mimeTypeToGroup.put("video/quicktime", config.videoReviewers());
        mimeTypeToGroup.put("video/x-msvideo", config.videoReviewers());
        mimeTypeToGroup.put("video/webm", config.videoReviewers());

        // Document types
        mimeTypeToGroup.put("application/pdf", config.documentReviewers());
        mimeTypeToGroup.put("application/msword", config.documentReviewers());
        mimeTypeToGroup.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            config.documentReviewers());
        mimeTypeToGroup.put("application/vnd.ms-excel", config.documentReviewers());
        mimeTypeToGroup.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            config.documentReviewers());
        mimeTypeToGroup.put("application/vnd.ms-powerpoint", config.documentReviewers());
        mimeTypeToGroup.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
            config.documentReviewers());
    }

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.debug("Choosing participant for asset: {}", payloadPath);

        // Get approval level from workflow metadata or args
        MetaDataMap workflowMeta = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        int approvalLevel = args.get("approvalLevel", 1);

        // Check if level was already set in workflow
        String levelKey = "currentApprovalLevel";
        if (workflowMeta.containsKey(levelKey)) {
            approvalLevel = workflowMeta.get(levelKey, 1);
        }

        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            LOG.warn("Unable to get ResourceResolver, returning default group");
            return config.defaultReviewers();
        }

        Resource assetResource = resolver.getResource(payloadPath);
        if (assetResource == null) {
            LOG.warn("Asset not found: {}, returning default group", payloadPath);
            return config.defaultReviewers();
        }

        Asset asset = assetResource.adaptTo(Asset.class);
        if (asset == null) {
            LOG.warn("Resource is not an asset: {}", payloadPath);
            return config.defaultReviewers();
        }

        // Determine approver based on level and asset properties
        String participant = determineParticipant(asset, approvalLevel, payloadPath);

        LOG.info("Selected participant '{}' for asset {} at approval level {}",
            participant, payloadPath, approvalLevel);

        // Store current level for next steps
        workflowMeta.put(levelKey, approvalLevel);

        return participant;
    }

    /**
     * Determines the appropriate participant based on asset properties and approval level
     */
    private String determineParticipant(Asset asset, int approvalLevel, String path) {

        // Level 3: Always goes to directors
        if (approvalLevel >= 3) {
            return config.directors();
        }

        // Level 2: Check for special handling requirements
        if (approvalLevel == 2) {
            String specialGroup = checkSpecialHandling(asset, path);
            if (specialGroup != null) {
                return specialGroup;
            }
            return config.seniorApprovers();
        }

        // Level 1: Route based on content type
        String mimeType = asset.getMimeType();
        if (mimeType != null && mimeTypeToGroup.containsKey(mimeType)) {
            return mimeTypeToGroup.get(mimeType);
        }

        // Check by MIME type category
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return config.imageReviewers();
            }
            if (mimeType.startsWith("video/")) {
                return config.videoReviewers();
            }
            if (mimeType.startsWith("application/")) {
                return config.documentReviewers();
            }
        }

        return config.defaultReviewers();
    }

    /**
     * Checks for special handling requirements based on asset properties
     */
    private String checkSpecialHandling(Asset asset, String path) {

        // Check folder-based routing
        if (path.contains("/brand/") || path.contains("/branding/")) {
            return config.brandManagers();
        }

        if (path.contains("/legal/") || path.contains("/compliance/")) {
            return config.legalReviewers();
        }

        // Check metadata-based routing
        Resource metadataResource = asset.adaptTo(Resource.class);
        if (metadataResource != null) {
            Resource metadata = metadataResource.getChild("jcr:content/metadata");
            if (metadata != null) {
                ValueMap props = metadata.getValueMap();

                // Check sensitivity level
                String sensitivity = props.get("dam:sensitivityLevel", String.class);
                if ("high".equalsIgnoreCase(sensitivity) || "confidential".equalsIgnoreCase(sensitivity)) {
                    return config.seniorApprovers();
                }

                // Check for brand assets
                Boolean isBrandAsset = props.get("dam:isBrandAsset", Boolean.class);
                if (Boolean.TRUE.equals(isBrandAsset)) {
                    return config.brandManagers();
                }

                // Check for legal review flag
                Boolean requiresLegal = props.get("dam:requiresLegalReview", Boolean.class);
                if (Boolean.TRUE.equals(requiresLegal)) {
                    return config.legalReviewers();
                }
            }
        }

        // Check file size threshold
        if (asset.getOriginal() != null && asset.getOriginal().getSize() > config.largeFileThreshold()) {
            LOG.debug("Large file detected, routing to senior approvers");
            return config.seniorApprovers();
        }

        return null;
    }
}
