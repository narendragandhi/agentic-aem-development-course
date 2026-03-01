package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Watermark Workflow Process
 *
 * Applies configurable watermarks to asset renditions for brand protection.
 * Inspired by ACS AEM Commons "Rendition Watermarker" process.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    WATERMARK PROCESS                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                  WATERMARK OPTIONS                      │  │
 * │   │                                                         │  │
 * │   │  Position:        │  Type:           │  Style:         │  │
 * │   │  ┌─────────────┐  │  ┌────────────┐  │  ┌───────────┐  │  │
 * │   │  │ TOP_LEFT    │  │  │   IMAGE    │  │  │  OPACITY  │  │  │
 * │   │  │ TOP_RIGHT   │  │  │   TEXT     │  │  │  SCALE    │  │  │
 * │   │  │ CENTER      │  │  │   TILED    │  │  │  ROTATION │  │  │
 * │   │  │ BOTTOM_LEFT │  │  └────────────┘  │  └───────────┘  │  │
 * │   │  │ BOTTOM_RIGHT│  │                   │                │  │
 * │   │  └─────────────┘  │                   │                │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                   PROCESSING FLOW                       │  │
 * │   │                                                         │  │
 * │   │   Asset          Renditions         Watermarked         │  │
 * │   │   ┌───┐          ┌───────┐          ┌───────────┐       │  │
 * │   │   │   │ ───────▶ │ cq5dam│ ───────▶ │  cq5dam   │       │  │
 * │   │   │   │          │ .web  │          │  .web.wm  │       │  │
 * │   │   │   │          ├───────┤          ├───────────┤       │  │
 * │   │   │   │          │ cq5dam│ ───────▶ │  cq5dam   │       │  │
 * │   │   │   │          │ .thumb│          │  .thumb.wm│       │  │
 * │   │   └───┘          └───────┘          └───────────┘       │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Watermark Process",
        "process.description=Applies watermarks to asset renditions"
    }
)
public class WatermarkProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(WatermarkProcess.class);

    private static final String ARG_WATERMARK_PATH = "watermarkPath";
    private static final String ARG_POSITION = "position";
    private static final String ARG_OPACITY = "opacity";
    private static final String ARG_RENDITION_PATTERN = "renditionPattern";

    private static final String DEFAULT_POSITION = "BOTTOM_RIGHT";
    private static final float DEFAULT_OPACITY = 0.5f;
    private static final String DEFAULT_RENDITION_PATTERN = "cq5dam.web.*";

    public enum WatermarkPosition {
        TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT, TILED
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting watermark process for: {}", payloadPath);

        // Get configuration
        String watermarkPath = metaDataMap.get(ARG_WATERMARK_PATH, String.class);
        String positionStr = metaDataMap.get(ARG_POSITION, DEFAULT_POSITION);
        float opacity = metaDataMap.get(ARG_OPACITY, DEFAULT_OPACITY);
        String renditionPattern = metaDataMap.get(ARG_RENDITION_PATTERN, DEFAULT_RENDITION_PATTERN);

        WatermarkPosition position = WatermarkPosition.valueOf(positionStr);

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
            LOG.warn("Resource is not a DAM asset: {}", payloadPath);
            return;
        }

        try {
            // Load watermark image
            BufferedImage watermarkImage = loadWatermark(resolver, watermarkPath);
            if (watermarkImage == null) {
                LOG.warn("Watermark image not found at: {}", watermarkPath);
                return;
            }

            // Process matching renditions
            Iterator<? extends Rendition> renditions = asset.listRenditions();
            int processedCount = 0;

            while (renditions.hasNext()) {
                Rendition rendition = renditions.next();
                if (matchesPattern(rendition.getName(), renditionPattern)) {
                    processRendition(asset, rendition, watermarkImage, position, opacity);
                    processedCount++;
                }
            }

            LOG.info("Watermark applied to {} renditions for: {}", processedCount, payloadPath);

        } catch (Exception e) {
            LOG.error("Failed to apply watermark to: " + payloadPath, e);
            throw new WorkflowException("Watermark process failed", e);
        }
    }

    /**
     * Loads the watermark image from the DAM.
     */
    private BufferedImage loadWatermark(ResourceResolver resolver, String watermarkPath) {
        if (watermarkPath == null || watermarkPath.isEmpty()) {
            LOG.debug("No watermark path specified, using default text watermark");
            return createTextWatermark("SAMPLE", 200, 50);
        }

        Resource watermarkResource = resolver.getResource(watermarkPath);
        if (watermarkResource == null) {
            return createTextWatermark("SAMPLE", 200, 50);
        }

        Asset watermarkAsset = watermarkResource.adaptTo(Asset.class);
        if (watermarkAsset == null) {
            return createTextWatermark("SAMPLE", 200, 50);
        }

        Rendition original = watermarkAsset.getOriginal();
        if (original == null) {
            return createTextWatermark("SAMPLE", 200, 50);
        }

        try (InputStream is = original.getStream()) {
            return ImageIO.read(is);
        } catch (Exception e) {
            LOG.error("Failed to load watermark image", e);
            return createTextWatermark("SAMPLE", 200, 50);
        }
    }

    /**
     * Creates a text-based watermark image.
     */
    private BufferedImage createTextWatermark(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawString(text, 10, 35);

        g2d.dispose();
        return image;
    }

    /**
     * Applies watermark to a rendition and creates a new watermarked rendition.
     */
    private void processRendition(Asset asset, Rendition rendition, BufferedImage watermark,
                                  WatermarkPosition position, float opacity) {
        try (InputStream is = rendition.getStream()) {
            BufferedImage original = ImageIO.read(is);
            if (original == null) {
                return;
            }

            BufferedImage watermarked = applyWatermark(original, watermark, position, opacity);

            // Save watermarked rendition
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(watermarked, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            String watermarkedName = rendition.getName().replace(".", ".watermarked.");
            asset.addRendition(watermarkedName, bais, "image/png");

            LOG.debug("Created watermarked rendition: {}", watermarkedName);

        } catch (Exception e) {
            LOG.error("Failed to process rendition: " + rendition.getName(), e);
        }
    }

    /**
     * Applies watermark to an image at the specified position.
     */
    private BufferedImage applyWatermark(BufferedImage original, BufferedImage watermark,
                                         WatermarkPosition position, float opacity) {

        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();

        // Draw original
        g2d.drawImage(original, 0, 0, null);

        // Set opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // Calculate position
        int x, y;
        int wmWidth = watermark.getWidth();
        int wmHeight = watermark.getHeight();
        int margin = 10;

        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin;
                break;
            case TOP_RIGHT:
                x = width - wmWidth - margin;
                y = margin;
                break;
            case CENTER:
                x = (width - wmWidth) / 2;
                y = (height - wmHeight) / 2;
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = height - wmHeight - margin;
                break;
            case BOTTOM_RIGHT:
            default:
                x = width - wmWidth - margin;
                y = height - wmHeight - margin;
                break;
        }

        if (position == WatermarkPosition.TILED) {
            // Tile watermark across image
            for (int ty = 0; ty < height; ty += wmHeight + 50) {
                for (int tx = 0; tx < width; tx += wmWidth + 50) {
                    g2d.drawImage(watermark, tx, ty, null);
                }
            }
        } else {
            g2d.drawImage(watermark, x, y, null);
        }

        g2d.dispose();
        return result;
    }

    /**
     * Checks if a rendition name matches the pattern.
     */
    private boolean matchesPattern(String name, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return name.matches(regex);
    }
}
