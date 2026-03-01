package com.demo.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Content Validation Workflow Process
 *
 * Validates content against configurable business rules before proceeding with approval.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                  CONTENT VALIDATION PROCESS                     │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
 * │   │   Load      │───▶│   Apply     │───▶│   Set       │       │
 * │   │   Content   │    │   Rules     │    │  Metadata   │       │
 * │   └─────────────┘    └─────────────┘    └─────────────┘       │
 * │                                                                 │
 * │   Validation Rules:                                            │
 * │   • Title is not empty                                         │
 * │   • Description exists and meets minimum length                │
 * │   • Required metadata fields are populated                     │
 * │   • Content does not contain restricted keywords               │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Content Validation Process",
        "process.description=Validates content against business rules before approval"
    }
)
public class ContentValidationProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(ContentValidationProcess.class);

    private static final String PN_TITLE = "jcr:title";
    private static final String PN_DESCRIPTION = "jcr:description";
    private static final int MIN_DESCRIPTION_LENGTH = 50;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        LOG.info("Starting content validation for workflow item: {}", workItem.getId());

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);

        if (resolver == null) {
            throw new WorkflowException("Unable to obtain ResourceResolver");
        }

        Resource resource = resolver.getResource(payloadPath);
        if (resource == null) {
            throw new WorkflowException("Content not found at path: " + payloadPath);
        }

        List<String> validationErrors = new ArrayList<>();
        boolean isValid = validateContent(resource, validationErrors);

        // Store validation results in workflow metadata
        MetaDataMap workflowMetadata = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        workflowMetadata.put("validationPassed", String.valueOf(isValid));
        workflowMetadata.put("validationErrors", String.join("; ", validationErrors));
        workflowMetadata.put("validatedAt", System.currentTimeMillis());
        workflowMetadata.put("validatedBy", "ContentValidationProcess");

        if (!isValid) {
            LOG.warn("Content validation failed for {}: {}", payloadPath, validationErrors);
            throw new WorkflowException("Content validation failed: " + String.join(", ", validationErrors));
        }

        LOG.info("Content validation passed for: {}", payloadPath);
    }

    /**
     * Validates content against business rules.
     *
     * @param resource the content resource to validate
     * @param errors list to collect validation errors
     * @return true if content is valid, false otherwise
     */
    private boolean validateContent(Resource resource, List<String> errors) {
        ValueMap properties = resource.getValueMap();
        boolean isValid = true;

        // Rule 1: Title is required
        String title = properties.get(PN_TITLE, String.class);
        if (title == null || title.trim().isEmpty()) {
            errors.add("Title is required");
            isValid = false;
        }

        // Rule 2: Description must meet minimum length
        String description = properties.get(PN_DESCRIPTION, String.class);
        if (description == null || description.length() < MIN_DESCRIPTION_LENGTH) {
            errors.add("Description must be at least " + MIN_DESCRIPTION_LENGTH + " characters");
            isValid = false;
        }

        // Rule 3: Check for required metadata (customizable)
        if (!validateRequiredMetadata(resource, errors)) {
            isValid = false;
        }

        // Rule 4: Check for restricted content
        if (!validateRestrictedContent(resource, errors)) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateRequiredMetadata(Resource resource, List<String> errors) {
        // Check for jcr:content child for page content
        Resource contentResource = resource.getChild("jcr:content");
        if (contentResource != null) {
            ValueMap props = contentResource.getValueMap();
            if (!props.containsKey("cq:lastModified")) {
                errors.add("Content must be saved before submission");
                return false;
            }
        }
        return true;
    }

    private boolean validateRestrictedContent(Resource resource, List<String> errors) {
        // Placeholder for restricted keyword checking
        // In production, this would check against a configurable list
        return true;
    }
}
