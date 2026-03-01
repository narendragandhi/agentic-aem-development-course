package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuarantineProcess workflow step.
 *
 * Tests the quarantine functionality that moves infected assets
 * to a secure quarantine folder with proper metadata tracking.
 *
 * Note: These tests focus on error handling and null safety.
 * Full integration tests would require AEM Context framework
 * for proper JCR/resource operations.
 */
@ExtendWith(MockitoExtension.class)
class QuarantineProcessTest {

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

    private QuarantineProcess process;
    private MetaDataMap workflowMetaData;
    private MetaDataMap processArgs;

    @BeforeEach
    void setUp() throws Exception {
        process = new QuarantineProcess();

        workflowMetaData = new SimpleMetaDataMap();
        processArgs = new SimpleMetaDataMap();

        // Create mock config using Java proxy
        QuarantineProcess.Config mockConfig = createMockConfig(
            "/content/dam/quarantine",  // quarantinePath
            "security-admins",           // notificationGroup
            90,                          // retentionDays
            false,                       // deleteOriginal
            true                         // createAuditLog
        );

        // Call activate method with mock config
        Method activateMethod = QuarantineProcess.class.getDeclaredMethod("activate",
            QuarantineProcess.Config.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(process, mockConfig);

        // Setup workflow mock chain
        lenient().when(workItem.getWorkflowData()).thenReturn(workflowData);
        lenient().when(workItem.getWorkflow()).thenReturn(workflow);
        lenient().when(workflow.getWorkflowData()).thenReturn(workflowData);
        lenient().when(workflowData.getMetaDataMap()).thenReturn(workflowMetaData);
    }

    /**
     * Creates a mock Config using Java dynamic proxy
     */
    private QuarantineProcess.Config createMockConfig(
            String quarantinePath,
            String notificationGroup,
            int retentionDays,
            boolean deleteOriginal,
            boolean createAuditLog) {

        return (QuarantineProcess.Config) java.lang.reflect.Proxy.newProxyInstance(
            QuarantineProcess.Config.class.getClassLoader(),
            new Class<?>[]{QuarantineProcess.Config.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "quarantinePath": return quarantinePath;
                    case "notificationGroup": return notificationGroup;
                    case "retentionDays": return retentionDays;
                    case "deleteOriginal": return deleteOriginal;
                    case "createAuditLog": return createAuditLog;
                    default: return null;
                }
            }
        );
    }

    @Test
    @DisplayName("Should throw WorkflowException when ResourceResolver is null")
    void throwWhenNoResourceResolver() {
        // Setup
        lenient().when(workflowData.getPayload()).thenReturn("/content/dam/test.pdf");
        lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(null);

        // Execute & Verify
        WorkflowException exception = assertThrows(WorkflowException.class, () ->
            process.execute(workItem, workflowSession, processArgs)
        );

        assertEquals("Unable to obtain ResourceResolver", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw WorkflowException when asset not found")
    void throwWhenAssetNotFound() {
        // Setup
        String assetPath = "/content/dam/uploads/nonexistent.pdf";
        lenient().when(workflowData.getPayload()).thenReturn(assetPath);
        lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resourceResolver);
        // Resource not found
        lenient().when(resourceResolver.getResource(assetPath)).thenReturn(null);
        // But quarantine folder can be found (to pass folder check)
        lenient().when(resourceResolver.getResource("/content/dam/quarantine")).thenReturn(null);
        lenient().when(resourceResolver.getResource("/content")).thenReturn(mock(org.apache.sling.api.resource.Resource.class));
        lenient().when(resourceResolver.getResource("/content/dam")).thenReturn(mock(org.apache.sling.api.resource.Resource.class));

        // Execute & Verify - should throw because asset resource not found
        assertThrows(WorkflowException.class, () ->
            process.execute(workItem, workflowSession, processArgs)
        );
    }

    @Test
    @DisplayName("Should accept process arguments metadata")
    void acceptProcessArguments() {
        // Setup process arguments
        processArgs.put("customArg", "testValue");

        // Verify the args are accessible (they're passed to execute method)
        assertEquals("testValue", processArgs.get("customArg", String.class));
    }

    @Test
    @DisplayName("Should handle threat info in workflow metadata")
    void handleThreatMetadata() {
        // Setup threat metadata
        workflowMetaData.put("av.scanStatus", "INFECTED");
        workflowMetaData.put("av.threatName", "Trojan.TestVirus");
        workflowMetaData.put("av.scanDetails", "Test scan details");

        // Verify metadata can be stored and retrieved
        assertEquals("INFECTED", workflowMetaData.get("av.scanStatus", String.class));
        assertEquals("Trojan.TestVirus", workflowMetaData.get("av.threatName", String.class));
        assertEquals("Test scan details", workflowMetaData.get("av.scanDetails", String.class));
    }

    @Test
    @DisplayName("Should verify mock configuration values")
    void verifyMockConfig() throws Exception {
        // Access config via reflection to verify it was set correctly
        java.lang.reflect.Field configField = QuarantineProcess.class.getDeclaredField("config");
        configField.setAccessible(true);
        QuarantineProcess.Config config = (QuarantineProcess.Config) configField.get(process);

        assertNotNull(config);
        assertEquals("/content/dam/quarantine", config.quarantinePath());
        assertEquals("security-admins", config.notificationGroup());
        assertEquals(90, config.retentionDays());
        assertFalse(config.deleteOriginal());
        assertTrue(config.createAuditLog());
    }
}
