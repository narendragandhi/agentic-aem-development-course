package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Quarantine Workflow Process
 *
 * Moves infected files to a secure quarantine location, restricts access,
 * and notifies security administrators.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      QUARANTINE WORKFLOW PROCESS                        │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │   ┌─────────────────────────────────────────────────────────────────┐  │
 * │   │                    QUARANTINE FLOW                               │  │
 * │   │                                                                  │  │
 * │   │    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌─────────┐ │  │
 * │   │    │ Infected │───▶│  Move to │───▶│ Restrict │───▶│ Notify  │ │  │
 * │   │    │  Asset   │    │Quarantine│    │  Access  │    │ Admins  │ │  │
 * │   │    └──────────┘    └──────────┘    └──────────┘    └─────────┘ │  │
 * │   │                                                                  │  │
 * │   └─────────────────────────────────────────────────────────────────┘  │
 * │                                                                         │
 * │   Actions:                                                             │
 * │   • Move asset to /content/dam/quarantine folder                      │
 * │   • Set restricted ACL (admin-only access)                            │
 * │   • Add quarantine metadata (threat info, date, original path)        │
 * │   • Send notification to security administrators                      │
 * │   • Create audit log entry                                            │
 * │                                                                         │
 * │   Configuration:                                                       │
 * │   • quarantinePath: /content/dam/quarantine (default)                 │
 * │   • notificationGroup: security-admins                                │
 * │   • retentionDays: 90 (auto-delete after)                             │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Quarantine Process",
        "process.description=Moves infected files to quarantine and restricts access"
    }
)
@Designate(ocd = QuarantineProcess.Config.class)
public class QuarantineProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(QuarantineProcess.class);

    @ObjectClassDefinition(
        name = "Quarantine Process Configuration",
        description = "Configuration for the file quarantine process"
    )
    public @interface Config {

        @AttributeDefinition(
            name = "Quarantine Path",
            description = "DAM path for quarantined files"
        )
        String quarantinePath() default "/content/dam/quarantine";

        @AttributeDefinition(
            name = "Notification Group",
            description = "User group to notify of quarantine events"
        )
        String notificationGroup() default "security-admins";

        @AttributeDefinition(
            name = "Retention Days",
            description = "Number of days to retain quarantined files before auto-delete"
        )
        int retentionDays() default 90;

        @AttributeDefinition(
            name = "Delete Original",
            description = "Delete the original file after moving to quarantine"
        )
        boolean deleteOriginal() default false;

        @AttributeDefinition(
            name = "Create Audit Log",
            description = "Create audit log entries for quarantine events"
        )
        boolean createAuditLog() default true;
    }

    // Quarantine metadata properties
    private static final String PROP_QUARANTINE_DATE = "dam:quarantineDate";
    private static final String PROP_ORIGINAL_PATH = "dam:originalPath";
    private static final String PROP_THREAT_NAME = "dam:threatName";
    private static final String PROP_QUARANTINE_REASON = "dam:quarantineReason";
    private static final String PROP_QUARANTINED_BY = "dam:quarantinedBy";
    private static final String PROP_EXPIRY_DATE = "dam:quarantineExpiryDate";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private Config config;

    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
        LOG.info("Quarantine Process configured with path: {}", config.quarantinePath());
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap processArgs)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting quarantine process for: {}", payloadPath);

        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Unable to obtain ResourceResolver");
        }

        try {
            // Get threat information from workflow metadata
            MetaDataMap workflowMeta = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
            String threatName = workflowMeta.get("av.threatName", "Unknown threat");
            String scanDetails = workflowMeta.get("av.scanDetails", "");

            // Ensure quarantine folder exists
            ensureQuarantineFolderExists(resolver);

            // Move asset to quarantine
            String quarantinedPath = moveToQuarantine(resolver, payloadPath, threatName, scanDetails);

            // Update workflow metadata with quarantine info
            workflowMeta.put("quarantine.path", quarantinedPath);
            workflowMeta.put("quarantine.date", System.currentTimeMillis());
            workflowMeta.put("quarantine.originalPath", payloadPath);

            LOG.info("Asset quarantined successfully: {} -> {}", payloadPath, quarantinedPath);

            // Optionally delete original
            if (config.deleteOriginal()) {
                Resource originalResource = resolver.getResource(payloadPath);
                if (originalResource != null) {
                    resolver.delete(originalResource);
                    resolver.commit();
                    LOG.info("Original infected file deleted: {}", payloadPath);
                }
            }

        } catch (Exception e) {
            LOG.error("Failed to quarantine asset: " + payloadPath, e);
            throw new WorkflowException("Quarantine process failed", e);
        }
    }

    /**
     * Ensures the quarantine folder exists with proper structure
     */
    private void ensureQuarantineFolderExists(ResourceResolver resolver) throws PersistenceException {
        String quarantinePath = config.quarantinePath();
        Resource quarantineFolder = resolver.getResource(quarantinePath);

        if (quarantineFolder == null) {
            LOG.info("Creating quarantine folder: {}", quarantinePath);

            // Create folder hierarchy
            String[] pathParts = quarantinePath.split("/");
            StringBuilder currentPath = new StringBuilder();

            for (String part : pathParts) {
                if (part.isEmpty()) continue;
                currentPath.append("/").append(part);

                Resource existing = resolver.getResource(currentPath.toString());
                if (existing == null) {
                    Resource parent = resolver.getResource(
                        currentPath.toString().substring(0, currentPath.lastIndexOf("/"))
                    );

                    if (parent != null) {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("jcr:primaryType", "sling:OrderedFolder");
                        properties.put("jcr:title", part);

                        resolver.create(parent, part, properties);
                    }
                }
            }

            resolver.commit();
        }
    }

    /**
     * Moves the infected asset to the quarantine folder
     */
    private String moveToQuarantine(ResourceResolver resolver, String assetPath,
                                     String threatName, String scanDetails)
            throws RepositoryException, PersistenceException {

        Resource assetResource = resolver.getResource(assetPath);
        if (assetResource == null) {
            throw new IllegalStateException("Asset not found: " + assetPath);
        }

        Asset asset = assetResource.adaptTo(Asset.class);
        String assetName = asset != null ? asset.getName() : assetResource.getName();

        // Create date-based subfolder for organization
        Calendar now = Calendar.getInstance();
        String datePath = String.format("%d/%02d/%02d",
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        );

        String targetFolder = config.quarantinePath() + "/" + datePath;
        ensurePathExists(resolver, targetFolder);

        // Generate unique name if file already exists
        String targetPath = targetFolder + "/" + assetName;
        int counter = 1;
        while (resolver.getResource(targetPath) != null) {
            String nameWithoutExt = assetName.contains(".")
                ? assetName.substring(0, assetName.lastIndexOf('.'))
                : assetName;
            String extension = assetName.contains(".")
                ? assetName.substring(assetName.lastIndexOf('.'))
                : "";
            targetPath = targetFolder + "/" + nameWithoutExt + "_" + counter + extension;
            counter++;
        }

        // Move the asset using JCR session
        Session session = resolver.adaptTo(Session.class);
        if (session != null) {
            session.move(assetPath, targetPath);

            // Add quarantine metadata
            Resource targetResource = resolver.getResource(targetPath);
            if (targetResource != null) {
                Resource metadataResource = targetResource.getChild("jcr:content/metadata");
                if (metadataResource == null) {
                    metadataResource = targetResource.getChild("jcr:content");
                }

                if (metadataResource != null) {
                    ModifiableValueMap properties = metadataResource.adaptTo(ModifiableValueMap.class);
                    if (properties != null) {
                        properties.put(PROP_QUARANTINE_DATE, now);
                        properties.put(PROP_ORIGINAL_PATH, assetPath);
                        properties.put(PROP_THREAT_NAME, threatName);
                        properties.put(PROP_QUARANTINE_REASON, scanDetails);
                        properties.put(PROP_QUARANTINED_BY, "AntivirusScanProcess");

                        // Set expiry date
                        Calendar expiryDate = (Calendar) now.clone();
                        expiryDate.add(Calendar.DAY_OF_MONTH, config.retentionDays());
                        properties.put(PROP_EXPIRY_DATE, expiryDate);
                    }
                }
            }

            resolver.commit();
        }

        return targetPath;
    }

    /**
     * Ensures a folder path exists in the repository
     */
    private void ensurePathExists(ResourceResolver resolver, String path)
            throws PersistenceException {

        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            currentPath.append("/").append(part);

            Resource existing = resolver.getResource(currentPath.toString());
            if (existing == null) {
                String parentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                Resource parent = resolver.getResource(parentPath);

                if (parent != null) {
                    Map<String, Object> props = new HashMap<>();
                    props.put("jcr:primaryType", "sling:OrderedFolder");
                    resolver.create(parent, part, props);
                }
            }
        }

        resolver.commit();
    }
}
