package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class DynamicApproverAssignerProcessTest {

    private final AemContext context = new AemContext();
    private DynamicApproverAssignerProcess underTest;

    @Mock
    private WorkItem workItem;
    @Mock
    private WorkflowSession workflowSession;
    @Mock
    private MetaDataMap metaDataMap;
    @Mock
    private WorkflowData workflowData;
    @Mock
    private Workflow workflow;
    @Mock
    private ResourceResolver resourceResolver;
    @Mock
    private Session jcrSession;
    @Mock
    private Node contentNode;
    @Mock
    private Property property;

    @BeforeEach
    void setUp() {
        underTest = new DynamicApproverAssignerProcess();
    }

    @Test
    void testExecute_financeCategory() throws Exception {
        // Given
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/test");
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.nodeExists(anyString())).thenReturn(true);
        when(jcrSession.getNode(anyString())).thenReturn(contentNode);
        when(contentNode.hasProperty("category")).thenReturn(true);
        when(contentNode.getProperty("category")).thenReturn(property);
        when(property.getString()).thenReturn("finance");
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(metaDataMap);

        // When
        underTest.execute(workItem, workflowSession, metaDataMap);

        // Then
        verify(metaDataMap).put("approverGroup", "finance-approvers");
    }

    @Test
    void testExecute_defaultCategory() throws Exception {
        // Given
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/test");
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.nodeExists(anyString())).thenReturn(true);
        when(jcrSession.getNode(anyString())).thenReturn(contentNode);
        when(contentNode.hasProperty("category")).thenReturn(false);
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(metaDataMap);

        // When
        underTest.execute(workItem, workflowSession, metaDataMap);

        // Then
        verify(metaDataMap).put("approverGroup", "content-approvers");
    }
}
