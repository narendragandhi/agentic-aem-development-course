/**
 * Comment Thread JavaScript
 */
(function(document, $) {
    "use strict";

    $(document).on("foundation-contentloaded", function() {
        $(".workflow-comment-thread").each(function() {
            var $thread = $(this);
            var workflowId = $thread.data("workflow-id");

            // Add comment button
            $thread.on("click", "[data-action='add-comment']", function() {
                $thread.find(".new-comment-form").slideDown();
            });

            // Cancel comment
            $thread.on("click", "[data-action='cancel-comment']", function() {
                $thread.find(".new-comment-form").slideUp();
                $thread.find(".comment-textarea").text("");
            });

            // Submit comment
            $thread.on("click", "[data-action='submit-comment']", function() {
                var content = $thread.find(".comment-textarea").text().trim();
                if (content) {
                    $.ajax({
                        url: "/bin/workflow/comments",
                        method: "POST",
                        data: JSON.stringify({ workflowId: workflowId, content: content }),
                        contentType: "application/json",
                        success: function() {
                            location.reload();
                        }
                    });
                }
            });

            // Reply
            $thread.on("click", "[data-action='reply']", function() {
                var $comment = $(this).closest(".comment-item");
                var commentId = $comment.data("comment-id");
                // Show inline reply form
            });

            // Resolve
            $thread.on("click", "[data-action='resolve']", function() {
                var $comment = $(this).closest(".comment-item");
                var commentId = $comment.data("comment-id");
                $.post("/bin/workflow/comments/" + commentId + "/resolve", function() {
                    $comment.addClass("resolved");
                });
            });

            // Filter
            $thread.on("change", ".comment-filter", function() {
                var filter = $(this).val();
                $thread.find(".comment-item").each(function() {
                    var $item = $(this);
                    var show = filter === "all" ||
                        (filter === "resolved" && $item.hasClass("resolved")) ||
                        (filter === "unresolved" && !$item.hasClass("resolved"));
                    $item.toggle(show);
                });
            });
        });
    });

})(document, Granite.$);
