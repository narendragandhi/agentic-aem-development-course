package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow Delegation Process
 *
 * Enables workflow chaining by delegating to another workflow model.
 * Inspired by ACS AEM Commons "Workflow Delegation" process.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                WORKFLOW DELEGATION PROCESS                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │                 DELEGATION PATTERNS                     │  │
 * │   │                                                         │  │
 * │   │   Pattern 1: Sequential Delegation                      │  │
 * │   │   ┌──────────┐    ┌──────────┐    ┌──────────┐         │  │
 * │   │   │Workflow A│───▶│Workflow B│───▶│Workflow C│         │  │
 * │   │   └──────────┘    └──────────┘    └──────────┘         │  │
 * │   │                                                         │  │
 * │   │   Pattern 2: Conditional Delegation                     │  │
 * │   │                   ┌──────────┐                          │  │
 * │   │                ┌─▶│Workflow X│                          │  │
 * │   │   ┌──────────┐ │  └──────────┘                          │  │
 * │   │   │ Decision │─┤                                        │  │
 * │   │   └──────────┘ │  ┌──────────┐                          │  │
 * │   │                └─▶│Workflow Y│                          │  │
 * │   │                   └──────────┘                          │  │
 * │   │                                                         │  │
 * │   │   Pattern 3: Parallel Delegation                        │  │
 * │   │                ┌──────────┐                             │  │
 * │   │             ┌─▶│Workflow P│                             │  │
 * │   │   ┌──────┐  │  └──────────┘                             │  │
 * │   │   │Split │──┤                                           │  │
 * │   │   └──────┘  │  ┌──────────┐                             │  │
 * │   │             └─▶│Workflow Q│                             │  │
 * │   │                └──────────┘                             │  │
 * │   │                                                         │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   Use Cases:                                                   │
 * │   • Complex approval requiring multiple specialized workflows  │
 * │   • Asset processing pipelines                                 │
 * │   • Multi-stage content transformation                         │
 * │   • Conditional processing based on content type               │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Workflow Delegation Process",
        "process.description=Delegates to another workflow for chained processing"
    }
)
public class WorkflowDelegationProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowDelegationProcess.class);

    private static final String ARG_DELEGATE_MODEL = "delegateModel";
    private static final String ARG_TERMINATE_CURRENT = "terminateCurrent";
    private static final String ARG_COPY_METADATA = "copyMetadata";
    private static final String ARG_WAIT_FOR_COMPLETION = "waitForCompletion";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Starting workflow delegation for: {}", payloadPath);

        // Get configuration
        String delegateModelPath = metaDataMap.get(ARG_DELEGATE_MODEL, String.class);
        boolean terminateCurrent = metaDataMap.get(ARG_TERMINATE_CURRENT, false);
        boolean copyMetadata = metaDataMap.get(ARG_COPY_METADATA, true);
        boolean waitForCompletion = metaDataMap.get(ARG_WAIT_FOR_COMPLETION, false);

        if (delegateModelPath == null || delegateModelPath.isEmpty()) {
            throw new WorkflowException("Delegate workflow model path is required");
        }

        try {
            // Load the delegate workflow model
            WorkflowModel delegateModel = workflowSession.getModel(delegateModelPath);
            if (delegateModel == null) {
                throw new WorkflowException("Delegate workflow model not found: " + delegateModelPath);
            }

            // Prepare workflow data for delegate
            com.adobe.granite.workflow.exec.WorkflowData delegateWorkflowData =
                workflowSession.newWorkflowData("JCR_PATH", payloadPath);
            MetaDataMap delegateMetadata = delegateWorkflowData.getMetaDataMap();

            if (copyMetadata) {
                // Copy metadata from current workflow
                MetaDataMap currentMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
                for (String key : currentMetadata.keySet()) {
                    delegateMetadata.put(key, currentMetadata.get(key));
                }
            }

            // Add delegation tracking info
            delegateMetadata.put("delegatedFrom", workItem.getWorkflow().getWorkflowModel().getId());
            delegateMetadata.put("delegatedAt", System.currentTimeMillis());
            delegateMetadata.put("originalInitiator", workItem.getWorkflow().getInitiator());

            // Start the delegate workflow
            Workflow delegateWorkflow = workflowSession.startWorkflow(
                delegateModel,
                delegateWorkflowData
            );

            LOG.info("Started delegate workflow '{}' with ID: {}",
                delegateModel.getTitle(), delegateWorkflow.getId());

            // Store delegate workflow reference
            MetaDataMap currentMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
            currentMetadata.put("delegatedWorkflowId", delegateWorkflow.getId());
            currentMetadata.put("delegatedWorkflowModel", delegateModelPath);

            if (waitForCompletion) {
                // Wait for delegate workflow to complete (with timeout)
                waitForWorkflowCompletion(workflowSession, delegateWorkflow.getId(), 300000); // 5 min timeout
            }

            if (terminateCurrent) {
                LOG.info("Terminating current workflow after delegation");
                workflowSession.terminateWorkflow(workItem.getWorkflow());
            }

            LOG.info("Workflow delegation completed for: {}", payloadPath);

        } catch (Exception e) {
            LOG.error("Failed to delegate workflow for: " + payloadPath, e);
            throw new WorkflowException("Workflow delegation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Waits for a workflow to complete with timeout.
     */
    private void waitForWorkflowCompletion(WorkflowSession session, String workflowId, long timeoutMs)
            throws WorkflowException {

        long startTime = System.currentTimeMillis();
        long pollInterval = 5000; // 5 seconds

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                Workflow workflow = session.getWorkflow(workflowId);
                if (workflow == null) {
                    // Workflow not found, likely completed
                    return;
                }

                String state = workflow.getState();
                if ("COMPLETED".equals(state) || "ABORTED".equals(state)) {
                    LOG.info("Delegate workflow finished with state {}: {}", state, workflowId);
                    return;
                }

                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new WorkflowException("Interrupted while waiting for delegate workflow", e);
            }
        }

        LOG.warn("Timeout waiting for delegate workflow: {}", workflowId);
    }
}
