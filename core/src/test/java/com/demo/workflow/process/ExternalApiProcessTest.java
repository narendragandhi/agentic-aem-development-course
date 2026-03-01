package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.demo.workflow.services.ExternalSystemIntegrationService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Session;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ExternalApiProcessTest {

    private final AemContext context = new AemContext();

    @InjectMocks
    private ExternalApiProcess underTest;

    @Mock
    private ExternalSystemIntegrationService externalService;
    @Mock
    private WorkItem workItem;
    @Mock
    private WorkflowSession workflowSession;
    @Mock
    private MetaDataMap metaDataMap;
    @Mock
    private WorkflowData workflowData;
    @Mock
    private ResourceResolver resourceResolver;
    @Mock
    private Session jcrSession;
    @Mock
    private Node metadataNode;

    @BeforeEach
    void setUp() {
        context.registerService(ExternalSystemIntegrationService.class, externalService);
    }

    @Test
    void testExecute() throws Exception {
        // Given
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/dam/test.jpg");
        when(externalService.getData(anyString())).thenReturn("{\"key\":\"value\"}");
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.nodeExists(anyString())).thenReturn(true);
        when(jcrSession.getNode(anyString())).thenReturn(metadataNode);

        // When
        underTest.execute(workItem, workflowSession, metaDataMap);

        // Then
        verify(metadataNode).setProperty("externalData", "{\"key\":\"value\"}");
        verify(jcrSession).save();
    }
}
