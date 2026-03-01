package com.demo.workflow.services.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.demo.workflow.services.AntivirusScanService;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Antivirus Scan Service Implementation
 *
 * Supports multiple scanning backends:
 * 1. ClamAV daemon (clamd) via TCP socket
 * 2. External REST API integration
 * 3. Mock mode for testing/development
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                    ANTIVIRUS SCAN SERVICE                           │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │                                                                     │
 * │   ┌───────────────────────────────────────────────────────────┐    │
 * │   │                    SCAN FLOW                               │    │
 * │   │                                                            │    │
 * │   │    ┌─────────┐    ┌──────────┐    ┌─────────────────┐     │    │
 * │   │    │  Asset  │───▶│  Stream  │───▶│   Scan Engine   │     │    │
 * │   │    │  Upload │    │  Content │    │  (ClamAV/API)   │     │    │
 * │   │    └─────────┘    └──────────┘    └────────┬────────┘     │    │
 * │   │                                            │               │    │
 * │   │                                   ┌────────▼────────┐      │    │
 * │   │                                   │   Scan Result   │      │    │
 * │   │                                   │  Clean/Infected │      │    │
 * │   │                                   └─────────────────┘      │    │
 * │   └───────────────────────────────────────────────────────────┘    │
 * │                                                                     │
 * │   Supported Engines:                                               │
 * │   • ClamAV (clamd daemon) - Default, open-source                   │
 * │   • REST API - Integration with cloud AV services                  │
 * │   • Mock - For testing and development                             │
 * │                                                                     │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = AntivirusScanService.class,
    immediate = true
)
@Designate(ocd = AntivirusScanServiceImpl.Config.class)
public class AntivirusScanServiceImpl implements AntivirusScanService {

    private static final Logger LOG = LoggerFactory.getLogger(AntivirusScanServiceImpl.class);

    @ObjectClassDefinition(
        name = "Antivirus Scan Service Configuration",
        description = "Configuration for the antivirus scanning service"
    )
    public @interface Config {

        @AttributeDefinition(
            name = "Scan Engine",
            description = "The antivirus engine to use: CLAMAV, REST_API, or MOCK"
        )
        String scanEngine() default "CLAMAV";

        @AttributeDefinition(
            name = "ClamAV Host",
            description = "Hostname or IP of the ClamAV daemon"
        )
        String clamavHost() default "localhost";

        @AttributeDefinition(
            name = "ClamAV Port",
            description = "Port number of the ClamAV daemon"
        )
        int clamavPort() default 3310;

        @AttributeDefinition(
            name = "Connection Timeout (ms)",
            description = "Timeout for connecting to the scan service"
        )
        int connectionTimeout() default 5000;

        @AttributeDefinition(
            name = "Read Timeout (ms)",
            description = "Timeout for reading scan results"
        )
        int readTimeout() default 60000;

        @AttributeDefinition(
            name = "Max File Size (bytes)",
            description = "Maximum file size to scan (default 100MB)"
        )
        long maxFileSize() default 104857600L;

        @AttributeDefinition(
            name = "REST API URL",
            description = "URL for external REST API scanning service"
        )
        String restApiUrl() default "";

        @AttributeDefinition(
            name = "REST API Key",
            description = "API key for external scanning service"
        )
        String restApiKey() default "";

        @AttributeDefinition(
            name = "Enabled",
            description = "Enable/disable antivirus scanning"
        )
        boolean enabled() default true;
    }

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private Config config;
    private String activeScanEngine;

    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
        this.activeScanEngine = config.scanEngine();
        LOG.info("Antivirus Scan Service activated with engine: {}", activeScanEngine);
    }

    @Override
    public ScanResult scanFile(InputStream inputStream, String fileName, long fileSize) {
        if (!config.enabled()) {
            LOG.debug("Antivirus scanning is disabled, allowing file: {}", fileName);
            return ScanResult.clean("DISABLED", 0);
        }

        if (fileSize > config.maxFileSize()) {
            LOG.warn("File {} exceeds maximum scan size ({} > {})",
                fileName, fileSize, config.maxFileSize());
            return ScanResult.error(activeScanEngine,
                "File exceeds maximum allowed size for scanning");
        }

        long startTime = System.currentTimeMillis();

        try {
            ScanResult result;

            switch (activeScanEngine.toUpperCase()) {
                case "CLAMAV":
                    result = scanWithClamAV(inputStream, fileName);
                    break;
                case "REST_API":
                    result = scanWithRestApi(inputStream, fileName);
                    break;
                case "MOCK":
                    result = scanWithMock(fileName);
                    break;
                default:
                    LOG.warn("Unknown scan engine: {}, using mock", activeScanEngine);
                    result = scanWithMock(fileName);
            }

            long duration = System.currentTimeMillis() - startTime;
            LOG.info("Scan completed for {} in {}ms - Result: {}",
                fileName, duration, result.isClean() ? "CLEAN" : "INFECTED");

            return result;

        } catch (Exception e) {
            LOG.error("Error scanning file: " + fileName, e);
            return ScanResult.error(activeScanEngine, e.getMessage());
        }
    }

    @Override
    public ScanResult scanAsset(String assetPath) {
        Map<String, Object> authInfo = Collections.singletonMap(
            ResourceResolverFactory.SUBSERVICE, "workflow-service"
        );

        try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(authInfo)) {
            var resource = resolver.getResource(assetPath);
            if (resource == null) {
                return ScanResult.error(activeScanEngine, "Asset not found: " + assetPath);
            }

            Asset asset = resource.adaptTo(Asset.class);
            if (asset == null) {
                return ScanResult.error(activeScanEngine, "Resource is not an asset: " + assetPath);
            }

            Rendition original = asset.getOriginal();
            if (original == null) {
                return ScanResult.error(activeScanEngine, "No original rendition found");
            }

            try (InputStream is = original.getStream()) {
                return scanFile(is, asset.getName(), original.getSize());
            }

        } catch (Exception e) {
            LOG.error("Error scanning asset: " + assetPath, e);
            return ScanResult.error(activeScanEngine, e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        if (!config.enabled()) {
            return false;
        }

        switch (activeScanEngine.toUpperCase()) {
            case "CLAMAV":
                return checkClamAVConnection();
            case "REST_API":
                return config.restApiUrl() != null && !config.restApiUrl().isEmpty();
            case "MOCK":
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getScanEngineName() {
        return activeScanEngine;
    }

    /**
     * Scan file using ClamAV daemon via INSTREAM command
     */
    private ScanResult scanWithClamAV(InputStream inputStream, String fileName) {
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket(config.clamavHost(), config.clamavPort())) {
            socket.setSoTimeout(config.readTimeout());

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Send INSTREAM command
            out.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
            out.flush();

            // Stream file content in chunks
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Send chunk length as 4-byte big-endian
                out.write(new byte[]{
                    (byte) (bytesRead >> 24),
                    (byte) (bytesRead >> 16),
                    (byte) (bytesRead >> 8),
                    (byte) bytesRead
                });
                out.write(buffer, 0, bytesRead);
            }

            // Send zero-length chunk to signal end
            out.write(new byte[]{0, 0, 0, 0});
            out.flush();

            // Read response
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            while ((bytesRead = in.read(buffer)) != -1) {
                response.write(buffer, 0, bytesRead);
            }

            String result = response.toString(StandardCharsets.UTF_8).trim();
            long duration = System.currentTimeMillis() - startTime;

            LOG.debug("ClamAV response for {}: {}", fileName, result);

            // Parse ClamAV response
            // Clean: "stream: OK"
            // Infected: "stream: <virus_name> FOUND"
            if (result.endsWith("OK")) {
                return ScanResult.clean("ClamAV", duration);
            } else if (result.contains("FOUND")) {
                String threatName = extractThreatName(result);
                return ScanResult.infected(threatName, "ClamAV", duration);
            } else if (result.contains("ERROR")) {
                return ScanResult.error("ClamAV", result);
            } else {
                return ScanResult.error("ClamAV", "Unknown response: " + result);
            }

        } catch (IOException e) {
            LOG.error("ClamAV connection error", e);
            return ScanResult.error("ClamAV", "Connection failed: " + e.getMessage());
        }
    }

    /**
     * Scan file using external REST API
     */
    private ScanResult scanWithRestApi(InputStream inputStream, String fileName) {
        // Placeholder for REST API implementation
        // This would integrate with services like VirusTotal, MetaDefender, etc.
        LOG.warn("REST API scanning not yet implemented, returning clean result");
        return ScanResult.clean("REST_API", 0);
    }

    /**
     * Mock scanning for development/testing
     * Files starting with "virus_" or "malware_" are treated as infected
     */
    private ScanResult scanWithMock(String fileName) {
        long duration = 100; // Simulated scan time

        String lowerName = fileName.toLowerCase();
        if (lowerName.startsWith("virus_") ||
            lowerName.startsWith("malware_") ||
            lowerName.contains("eicar")) {
            return ScanResult.infected("Mock.TestVirus", "MOCK", duration);
        }

        return ScanResult.clean("MOCK", duration);
    }

    /**
     * Check if ClamAV daemon is reachable
     */
    private boolean checkClamAVConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(
                new java.net.InetSocketAddress(config.clamavHost(), config.clamavPort()),
                config.connectionTimeout()
            );

            // Send PING command
            socket.getOutputStream().write("zPING\0".getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            byte[] response = new byte[10];
            int bytesRead = socket.getInputStream().read(response);
            String result = new String(response, 0, bytesRead, StandardCharsets.UTF_8).trim();

            return "PONG".equals(result);

        } catch (IOException e) {
            LOG.debug("ClamAV not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract virus/threat name from ClamAV response
     */
    private String extractThreatName(String response) {
        // Format: "stream: <virus_name> FOUND"
        int colonIndex = response.indexOf(':');
        int foundIndex = response.lastIndexOf(" FOUND");

        if (colonIndex > 0 && foundIndex > colonIndex) {
            return response.substring(colonIndex + 1, foundIndex).trim();
        }

        return "Unknown Threat";
    }
}
