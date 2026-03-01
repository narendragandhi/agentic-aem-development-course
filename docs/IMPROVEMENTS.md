# AEM Workflow Demo Enhancement Recommendations

This document outlines proposed enhancements to the AEM Workflow Demo project to better showcase the platform's capabilities for handling complex, real-world business processes.

---

## Completed Enhancements

### Project Structure Alignment with AEM Archetype (February 2026)

The project has been updated to follow the latest AEM Maven archetype patterns:

| Change | Details |
|--------|---------|
| **bnd-maven-plugin** | Replaced maven-bundle-plugin for OSGi bundle generation |
| **Package Types** | ui.apps uses `application`, ui.content uses `content` |
| **all Module** | Added container package that embeds all sub-packages |
| **Workflow Paths** | Models and launchers moved from `/var` and `/etc` to `/conf` |
| **AEM Analyzer** | Added aemanalyser-maven-plugin for Cloud Service validation |
| **Plugin Versions** | Updated filevault-package-maven-plugin to 1.3.6 |

### Dynamic Workflow Routing (Implemented)

`DynamicApproverAssignerProcess` has been implemented to route content to different approver groups based on content properties (e.g., category-based routing to finance-approvers vs content-approvers).

### External System Integration (Implemented)

`ExternalApiProcess` and `ExternalSystemIntegrationService` have been implemented to demonstrate REST API integration from workflow processes.

---

## Proposed Enhancements

## 1. Implement Dynamic, Data-Driven Workflow Routing

**Status:** IMPLEMENTED

The `DynamicApproverAssignerProcess` has been implemented in `core/src/main/java/com/demo/workflow/process/`. It routes content to different approver groups based on the `category` property:
- Finance content routes to `finance-approvers` group
- Default content routes to `content-approvers` group

**Future Enhancement:** Extend this to support configuration-driven routing rules stored in OSGi config or a configuration page.

## 2. Integrate with External Systems

**Status:** IMPLEMENTED

The following components have been implemented:
- `ExternalSystemIntegrationService` - OSGi service for HTTP-based API calls
- `ExternalApiProcess` - Workflow process step that calls external APIs
- `LlmService` - Service for LLM/AI integration
- `SummarizeContentProcess` - Workflow process for AI-powered content summarization

**Future Enhancement:** Add configuration for API endpoints, authentication, and retry policies via OSGi configurations.

## 3. Improve User Interaction with Custom Dialogs

To improve the experience for business users, it's crucial to provide them with context-relevant information and actions directly within their workflow inbox.

**Suggestion:** In the "Multi-Level Approval Workflow," when a manager is asked to approve content, replace the generic approval dialog with a custom one. This dialog can show relevant information, such as which fields have been changed, a preview of the content, or the reason for the approval request.

**Implementation Steps:**
1.  **Create a Custom Dialog:** Create a new AEM dialog definition (`cq:dialog`) under `/apps/demo-workflow/dialogs/approval-dialog`.
2.  **Add Relevant Fields:** The dialog can include:
    *   A path browser to the payload.
    *   Read-only fields displaying metadata from the content.
    *   A text area for users to provide comments.
    *   A dropdown for selecting a rejection reason.
3.  **Associate Dialog with Participant Step:** In the "Participant Step" of the workflow model, set the `PROCESS_ARGS` property to point to your new dialog path (e.g., `dialog=/apps/demo-workflow/dialogs/approval-dialog`).

## 4. Add Robust Error Handling and Escalation

Complex, long-running workflows must be resilient and able to handle exceptions and human delays gracefully.

**Suggestion:** Modify the "Multi-Level Approval Workflow" to include a timeout and an escalation path. If a "Team Lead" does not act on a task within a specified time (e.g., 24 hours), the task should be automatically escalated to their manager.

**Implementation Steps:**
1.  **Configure Timeout Handler:** In the "Participant Step" settings, navigate to the "Advanced Settings" and configure a "Timeout Handler."
2.  **Set Timeout:** Specify the time after which the handler should trigger (e.g., `24h`).
3.  **Implement Escalation Logic:** The handler can be configured to run a script or a custom process.
    *   **ECMA Script:** A simple script can use the workflow API to find the current work item and reassign it to a different user or group (the escalation path).
    *   **Custom Process:** For more complex logic, a custom Java process can be implemented to handle the escalation, potentially including sending notifications.
