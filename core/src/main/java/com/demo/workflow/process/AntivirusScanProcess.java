package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.demo.workflow.services.AntivirusScanService;
import com.demo.workflow.services.AntivirusScanService.ScanResult;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Calendar;

/**
 * Antivirus Scan Workflow Process
 *
 * Scans uploaded assets for malware/viruses before proceeding with approval workflow.
 * Infected files are flagged and routed to quarantine.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                    ANTIVIRUS SCAN WORKFLOW PROCESS                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │   ┌─────────────────────────────────────────────────────────────────┐  │
 * │   │                       SCAN WORKFLOW                              │  │
 * │   │                                                                  │  │
 * │   │    ┌──────────┐     ┌──────────┐     ┌────────────────────┐    │  │
 * │   │    │  Asset   │────▶│  Scan    │────▶│   Route Based      │    │  │
 * │   │    │  Upload  │     │  File    │     │   On Result        │    │  │
 * │   │    └──────────┘     └──────────┘     └─────────┬──────────┘    │  │
 * │   │                                                │               │  │
 * │   │                          ┌─────────────────────┼───────────┐   │  │
 * │   │                          │                     │           │   │  │
 * │   │                    ┌─────▼─────┐        ┌──────▼──────┐    │   │  │
 * │   │                    │   CLEAN   │        │  INFECTED   │    │   │  │
 * │   │                    │           │        │             │    │   │  │
 * │   │                    │ Continue  │        │ Quarantine  │    │   │  │
 * │   │                    │ Approval  │        │ & Notify    │    │   │  │
 * │   │                    └───────────┘        └─────────────┘    │   │  │
 * │   │                                                            │   │  │
 * │   └─────────────────────────────────────────────────────────────────┘  │
 * │                                                                         │
 * │   Metadata Set:                                                        │
 * │   • av.scanStatus    - CLEAN, INFECTED, ERROR, SKIPPED                 │
 * │   • av.scanEngine    - Engine used (ClamAV, VirusTotal, etc.)          │
 * │   • av.scanTime      - Timestamp of scan                               │
 * │   • av.threatName    - Name of detected threat (if infected)           │
 * │   • av.scanDuration  - Duration of scan in milliseconds                │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Antivirus Scan Process",
        "process.description=Scans uploaded files for malware before approval workflow"
    }
)
public class AntivirusScanProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(AntivirusScanProcess.class);

    // Workflow metadata keys
    private static final String META_SCAN_STATUS = "av.scanStatus";
    private static final String META_SCAN_ENGINE = "av.scanEngine";
    private static final String META_SCAN_TIME = "av.scanTime";
    private static final String META_THREAT_NAME = "av.threatName";
    private static final String META_SCAN_DURATION = "av.scanDuration";
    private static final String META_SCAN_DETAILS = "av.scanDetails";

    // Asset metadata properties (stored on the asset itself)
    private static final String PROP_AV_STATUS = "dam:avScanStatus";
    private static final String PROP_AV_SCAN_DATE = "dam:avScanDate";
    private static final String PROP_AV_THREAT = "dam:avThreatName";

    // Scan status values
    public static final String STATUS_CLEAN = "CLEAN";
    public static final String STATUS_INFECTED = "INFECTED";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_SKIPPED = "SKIPPED";

    @Reference
    private AntivirusScanService antivirusScanService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap processArgs)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting antivirus scan for: {}", payloadPath);

        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Unable to obtain ResourceResolver");
        }

        Resource assetResource = resolver.getResource(payloadPath);
        if (assetResource == null) {
            throw new WorkflowException("Asset not found at path: " + payloadPath);
        }

        Asset asset = assetResource.adaptTo(Asset.class);
        if (asset == null) {
            LOG.warn("Resource is not a DAM asset, skipping scan: {}", payloadPath);
            setWorkflowMetadata(workItem, STATUS_SKIPPED, "N/A", 0, null, "Not a DAM asset");
            return;
        }

        // Check if antivirus service is available
        if (!antivirusScanService.isAvailable()) {
            LOG.warn("Antivirus service not available, skipping scan for: {}", payloadPath);
            setWorkflowMetadata(workItem, STATUS_SKIPPED, "N/A", 0, null,
                "Antivirus service unavailable");
            return;
        }

        try {
            // Get the original rendition for scanning
            Rendition original = asset.getOriginal();
            if (original == null) {
                throw new WorkflowException("No original rendition found for asset: " + payloadPath);
            }

            // Perform the scan
            ScanResult result;
            try (InputStream inputStream = original.getStream()) {
                result = antivirusScanService.scanFile(
                    inputStream,
                    asset.getName(),
                    original.getSize()
                );
            }

            // Process the scan result
            String status = result.isClean() ? STATUS_CLEAN : STATUS_INFECTED;
            setWorkflowMetadata(workItem, status, result.getScanEngine(),
                result.getScanDurationMs(), result.getThreatName(), result.getDetails());

            // Store scan metadata on the asset
            storeAssetMetadata(resolver, assetResource, result);

            if (result.isClean()) {
                LOG.info("Asset {} passed antivirus scan (engine: {}, duration: {}ms)",
                    payloadPath, result.getScanEngine(), result.getScanDurationMs());
            } else {
                LOG.warn("THREAT DETECTED in asset {}: {} (engine: {})",
                    payloadPath, result.getThreatName(), result.getScanEngine());

                // For infected files, throw exception to halt workflow
                // The workflow model should route this to quarantine via OR_SPLIT
                throw new WorkflowException("Malware detected: " + result.getThreatName());
            }

        } catch (WorkflowException we) {
            // Re-throw workflow exceptions
            throw we;
        } catch (Exception e) {
            LOG.error("Error during antivirus scan for: " + payloadPath, e);
            setWorkflowMetadata(workItem, STATUS_ERROR, antivirusScanService.getScanEngineName(),
                0, null, "Scan error: " + e.getMessage());
            throw new WorkflowException("Antivirus scan failed", e);
        }
    }

    /**
     * Sets workflow metadata with scan results
     */
    private void setWorkflowMetadata(WorkItem workItem, String status, String engine,
                                      long duration, String threatName, String details) {
        MetaDataMap workflowMeta = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        workflowMeta.put(META_SCAN_STATUS, status);
        workflowMeta.put(META_SCAN_ENGINE, engine);
        workflowMeta.put(META_SCAN_TIME, System.currentTimeMillis());
        workflowMeta.put(META_SCAN_DURATION, duration);
        workflowMeta.put(META_SCAN_DETAILS, details);

        if (threatName != null) {
            workflowMeta.put(META_THREAT_NAME, threatName);
        }
    }

    /**
     * Stores scan metadata on the asset for future reference
     */
    private void storeAssetMetadata(ResourceResolver resolver, Resource assetResource,
                                     ScanResult result) {
        try {
            Resource metadataResource = assetResource.getChild("jcr:content/metadata");
            if (metadataResource != null) {
                ModifiableValueMap properties = metadataResource.adaptTo(ModifiableValueMap.class);
                if (properties != null) {
                    properties.put(PROP_AV_STATUS, result.isClean() ? STATUS_CLEAN : STATUS_INFECTED);
                    properties.put(PROP_AV_SCAN_DATE, Calendar.getInstance());

                    if (!result.isClean() && result.getThreatName() != null) {
                        properties.put(PROP_AV_THREAT, result.getThreatName());
                    }

                    resolver.commit();
                    LOG.debug("Stored antivirus metadata on asset: {}", assetResource.getPath());
                }
            }
        } catch (PersistenceException e) {
            LOG.warn("Failed to store antivirus metadata on asset", e);
        }
    }
}
