/**
 * Workflow Review Dialog JavaScript
 *
 * Handles the interactive functionality for the workflow review dialog
 * including decision handling, comment submission, and rejection reasons.
 */
(function(document, $) {
    "use strict";

    var WORKFLOW_REVIEW_SELECTOR = ".workflow-review-dialog";

    /**
     * Initialize workflow review dialog
     */
    function initWorkflowReview() {
        $(document).on("foundation-contentloaded", function() {
            $(WORKFLOW_REVIEW_SELECTOR).each(function() {
                new WorkflowReviewDialog(this);
            });
        });
    }

    /**
     * WorkflowReviewDialog class
     */
    function WorkflowReviewDialog(element) {
        this.$el = $(element);
        this.contentPath = this.$el.data("content-path");
        this.workflowId = this.$el.data("workflow-id");

        this.init();
    }

    WorkflowReviewDialog.prototype = {
        init: function() {
            this.bindEvents();
            this.initDecisionHandling();
        },

        bindEvents: function() {
            var self = this;

            // Decision radio button changes
            this.$el.on("change", "[name='./decision']", function() {
                self.handleDecisionChange($(this).val());
            });

            // View full content button
            this.$el.on("click", ".workflow-view-content-btn", function(e) {
                e.preventDefault();
                self.openContentPreview();
            });

            // Form submission
            this.$el.closest("form").on("submit", function(e) {
                if (!self.validateForm()) {
                    e.preventDefault();
                    return false;
                }
            });
        },

        initDecisionHandling: function() {
            // Check initial state
            var selectedDecision = this.$el.find("[name='./decision']:checked").val();
            if (selectedDecision) {
                this.handleDecisionChange(selectedDecision);
            }
        },

        handleDecisionChange: function(decision) {
            var $rejectionSection = this.$el.find(".workflow-rejection-section");

            if (decision === "reject") {
                $rejectionSection.show();
                $rejectionSection.find("[name='./rejectionReason']").attr("required", true);
                $rejectionSection.find("[name='./rejectionDetails']").attr("required", true);
            } else {
                $rejectionSection.hide();
                $rejectionSection.find("[name='./rejectionReason']").removeAttr("required");
                $rejectionSection.find("[name='./rejectionDetails']").removeAttr("required");
            }

            // Update button states
            this.updateActionButtons(decision);
        },

        updateActionButtons: function(decision) {
            var $submitBtn = this.$el.find("[data-action='submit-decision']");

            switch(decision) {
                case "approve":
                    $submitBtn.text("Approve Content").removeClass("coral-Button--warning").addClass("coral-Button--primary");
                    break;
                case "reject":
                    $submitBtn.text("Reject Content").removeClass("coral-Button--primary").addClass("coral-Button--warning");
                    break;
                case "request_changes":
                    $submitBtn.text("Request Changes").removeClass("coral-Button--warning").addClass("coral-Button--primary");
                    break;
                case "escalate":
                    $submitBtn.text("Escalate for Review").removeClass("coral-Button--warning").addClass("coral-Button--primary");
                    break;
            }
        },

        validateForm: function() {
            var decision = this.$el.find("[name='./decision']:checked").val();

            if (!decision) {
                this.showError("Please select a decision");
                return false;
            }

            if (decision === "reject") {
                var reason = this.$el.find("[name='./rejectionReason']").val();
                var details = this.$el.find("[name='./rejectionDetails']").val();

                if (!reason) {
                    this.showError("Please select a rejection reason");
                    return false;
                }
                if (!details || details.trim().length < 10) {
                    this.showError("Please provide rejection details (minimum 10 characters)");
                    return false;
                }
            }

            return true;
        },

        openContentPreview: function() {
            var previewUrl = this.contentPath + ".html?wcmmode=disabled";
            window.open(previewUrl, "_blank", "width=1200,height=800");
        },

        showError: function(message) {
            var ui = $(window).adaptTo("foundation-ui");
            if (ui) {
                ui.alert("Validation Error", message, "error");
            } else {
                alert(message);
            }
        }
    };

    // Initialize on document ready
    $(document).ready(initWorkflowReview);

})(document, Granite.$);
