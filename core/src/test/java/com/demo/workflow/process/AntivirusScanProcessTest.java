package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.demo.workflow.services.AntivirusScanService;
import com.demo.workflow.services.AntivirusScanService.ScanResult;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AntivirusScanProcess workflow step
 *
 * These tests validate the workflow process behavior without
 * requiring a full AEM context.
 */
@ExtendWith(MockitoExtension.class)
class AntivirusScanProcessTest {

    @Mock
    private AntivirusScanService antivirusScanService;

    @Mock
    private WorkItem workItem;

    @Mock
    private WorkflowSession workflowSession;

    @Mock
    private Workflow workflow;

    @Mock
    private WorkflowData workflowData;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Resource assetResource;

    @Mock
    private Resource metadataResource;

    @Mock
    private Asset asset;

    @Mock
    private Rendition originalRendition;

    @InjectMocks
    private AntivirusScanProcess process;

    private MetaDataMap workflowMetaData;
    private MetaDataMap processArgs;

    @BeforeEach
    void setUp() {
        workflowMetaData = new SimpleMetaDataMap();
        processArgs = new SimpleMetaDataMap();

        // Setup the workflow mock chain - IMPORTANT: mock both access paths
        // The process uses workItem.getWorkflowData() directly for payload
        lenient().when(workItem.getWorkflowData()).thenReturn(workflowData);

        // The process uses workItem.getWorkflow().getWorkflowData().getMetaDataMap() for metadata
        lenient().when(workItem.getWorkflow()).thenReturn(workflow);
        lenient().when(workflow.getWorkflowData()).thenReturn(workflowData);
        lenient().when(workflowData.getMetaDataMap()).thenReturn(workflowMetaData);

        lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resourceResolver);
    }

    @Test
    @DisplayName("Should scan clean file and set CLEAN status")
    void scanCleanFile() throws Exception {
        // Setup
        String assetPath = "/content/dam/test/clean-document.pdf";
        setupFullMockChain(assetPath, "clean-document.pdf", 1024L);

        when(antivirusScanService.isAvailable()).thenReturn(true);
        when(antivirusScanService.scanFile(any(InputStream.class), eq("clean-document.pdf"), eq(1024L)))
            .thenReturn(ScanResult.clean("MOCK", 50));

        // Execute
        process.execute(workItem, workflowSession, processArgs);

        // Verify
        assertEquals("CLEAN", workflowMetaData.get("av.scanStatus", String.class));
        assertEquals("MOCK", workflowMetaData.get("av.scanEngine", String.class));
        assertNotNull(workflowMetaData.get("av.scanTime", Long.class));
        assertEquals(50L, workflowMetaData.get("av.scanDuration", Long.class));
    }

    @Test
    @DisplayName("Should detect infected file and throw WorkflowException")
    void scanInfectedFile() throws Exception {
        // Setup
        String assetPath = "/content/dam/test/virus_payload.exe";
        setupFullMockChain(assetPath, "virus_payload.exe", 2048L);

        when(antivirusScanService.isAvailable()).thenReturn(true);
        when(antivirusScanService.scanFile(any(InputStream.class), eq("virus_payload.exe"), eq(2048L)))
            .thenReturn(ScanResult.infected("Trojan.Malware", "MOCK", 75));

        // Execute & Verify
        WorkflowException exception = assertThrows(WorkflowException.class, () ->
            process.execute(workItem, workflowSession, processArgs)
        );

        assertTrue(exception.getMessage().contains("Malware detected"));
        assertEquals("INFECTED", workflowMetaData.get("av.scanStatus", String.class));
        assertEquals("Trojan.Malware", workflowMetaData.get("av.threatName", String.class));
    }

    @Test
    @DisplayName("Should skip scan when antivirus service unavailable")
    void skipWhenServiceUnavailable() throws Exception {
        // Setup
        String assetPath = "/content/dam/test/document.pdf";
        setupFullMockChain(assetPath, "document.pdf", 1024L);

        when(antivirusScanService.isAvailable()).thenReturn(false);

        // Execute - should not throw
        process.execute(workItem, workflowSession, processArgs);

        // Verify
        assertEquals("SKIPPED", workflowMetaData.get("av.scanStatus", String.class));
        verify(antivirusScanService, never()).scanFile(any(), any(), anyLong());
    }

    @Test
    @DisplayName("Should throw WorkflowException when asset not found")
    void throwWhenAssetNotFound() {
        // Setup - resource not found
        String assetPath = "/content/dam/nonexistent/file.pdf";
        when(workflowData.getPayload()).thenReturn(assetPath);
        when(resourceResolver.getResource(assetPath)).thenReturn(null);

        // Execute & Verify
        WorkflowException exception = assertThrows(WorkflowException.class, () ->
            process.execute(workItem, workflowSession, processArgs)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should throw WorkflowException when ResourceResolver unavailable")
    void throwWhenNoResourceResolver() {
        // Setup
        when(workflowData.getPayload()).thenReturn("/content/dam/test.pdf");
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(null);

        // Execute & Verify
        assertThrows(WorkflowException.class, () ->
            process.execute(workItem, workflowSession, processArgs)
        );
    }

    @Test
    @DisplayName("Should handle scan error and set ERROR status")
    void handleScanError() throws Exception {
        // Setup
        String assetPath = "/content/dam/test/document.pdf";
        setupFullMockChain(assetPath, "document.pdf", 1024L);

        when(antivirusScanService.isAvailable()).thenReturn(true);
        when(antivirusScanService.getScanEngineName()).thenReturn("ClamAV");
        when(antivirusScanService.scanFile(any(InputStream.class), anyString(), anyLong()))
            .thenThrow(new RuntimeException("Connection refused"));

        // Execute & Verify
        assertThrows(WorkflowException.class, () ->
            process.execute(workItem, workflowSession, processArgs)
        );

        assertEquals("ERROR", workflowMetaData.get("av.scanStatus", String.class));
    }

    @Test
    @DisplayName("Should skip non-DAM resources gracefully")
    void skipNonDamResource() throws Exception {
        // Setup - resource exists but is not an Asset
        String path = "/content/page/test";
        when(workflowData.getPayload()).thenReturn(path);
        when(resourceResolver.getResource(path)).thenReturn(assetResource);
        when(assetResource.adaptTo(Asset.class)).thenReturn(null);

        // Execute - should not throw
        process.execute(workItem, workflowSession, processArgs);

        // Verify
        assertEquals("SKIPPED", workflowMetaData.get("av.scanStatus", String.class));
    }

    /**
     * Helper to set up the complete mock chain for asset scanning.
     * Uses lenient stubs as not all paths are used in every test.
     */
    private void setupFullMockChain(String assetPath, String fileName, long fileSize) {
        // Workflow data
        lenient().when(workflowData.getPayload()).thenReturn(assetPath);

        // Resource resolution
        lenient().when(resourceResolver.getResource(assetPath)).thenReturn(assetResource);
        lenient().when(assetResource.adaptTo(Asset.class)).thenReturn(asset);
        lenient().when(assetResource.getPath()).thenReturn(assetPath);
        lenient().when(assetResource.getChild("jcr:content/metadata")).thenReturn(metadataResource);

        // Asset
        lenient().when(asset.getName()).thenReturn(fileName);
        lenient().when(asset.getOriginal()).thenReturn(originalRendition);

        // Rendition
        lenient().when(originalRendition.getStream()).thenReturn(
            new ByteArrayInputStream("test content".getBytes())
        );
        lenient().when(originalRendition.getSize()).thenReturn(fileSize);
    }
}
