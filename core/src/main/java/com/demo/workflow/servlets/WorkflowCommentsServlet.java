package com.demo.workflow.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * Servlet for managing workflow review comments
 *
 * Endpoints:
 * GET  /bin/workflow/comments?workflowId=xxx - List comments
 * POST /bin/workflow/comments - Create comment
 * POST /bin/workflow/comments/{id}/resolve - Resolve comment
 * DELETE /bin/workflow/comments/{id} - Delete comment
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/workflow/comments",
        "sling.servlet.methods=GET",
        "sling.servlet.methods=POST",
        "sling.servlet.methods=DELETE"
    }
)
public class WorkflowCommentsServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowCommentsServlet.class);
    private static final String COMMENTS_BASE_PATH = "/var/workflow/comments";
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String workflowId = request.getParameter("workflowId");
        if (workflowId == null || workflowId.isEmpty()) {
            sendError(response, 400, "Missing workflowId parameter");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();
        String commentsPath = COMMENTS_BASE_PATH + "/" + workflowId;
        Resource commentsResource = resolver.getResource(commentsPath);

        List<Map<String, Object>> comments = new ArrayList<>();

        if (commentsResource != null) {
            for (Resource commentResource : commentsResource.getChildren()) {
                comments.add(buildCommentMap(commentResource));
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(GSON.toJson(Collections.singletonMap("comments", comments)));
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String suffix = request.getRequestPathInfo().getSuffix();

        // Check if this is a resolve action
        if (suffix != null && suffix.endsWith("/resolve")) {
            handleResolve(request, response, suffix);
            return;
        }

        // Otherwise, create new comment
        handleCreate(request, response);
    }

    private void handleCreate(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {

        JsonObject body = GSON.fromJson(request.getReader(), JsonObject.class);
        String workflowId = body.get("workflowId").getAsString();
        String content = body.get("content").getAsString();
        String parentId = body.has("parentId") ? body.get("parentId").getAsString() : null;

        ResourceResolver resolver = request.getResourceResolver();
        String commentsPath = COMMENTS_BASE_PATH + "/" + workflowId;

        try {
            // Ensure comments folder exists
            Resource commentsFolder = ResourceUtil.getOrCreateResource(
                resolver,
                commentsPath,
                "sling:Folder",
                "sling:Folder",
                false
            );

            // Create comment node
            String commentId = "comment-" + System.currentTimeMillis();
            Map<String, Object> props = new HashMap<>();
            props.put("jcr:primaryType", "nt:unstructured");
            props.put("content", content);
            props.put("authorId", resolver.getUserID());
            props.put("authorName", resolver.getUserID()); // Would get display name in production
            props.put("authorRole", "Reviewer");
            props.put("created", Calendar.getInstance());
            props.put("resolved", false);
            props.put("edited", false);

            if (parentId != null) {
                props.put("parentId", parentId);
            }

            resolver.create(commentsFolder, commentId, props);
            resolver.commit();

            response.setContentType("application/json");
            response.getWriter().write(GSON.toJson(Collections.singletonMap("id", commentId)));

        } catch (PersistenceException e) {
            LOG.error("Failed to create comment", e);
            sendError(response, 500, "Failed to create comment");
        }
    }

    private void handleResolve(SlingHttpServletRequest request, SlingHttpServletResponse response,
                               String suffix) throws IOException {

        // Extract comment ID from suffix: /{commentId}/resolve
        String[] parts = suffix.split("/");
        if (parts.length < 2) {
            sendError(response, 400, "Invalid comment path");
            return;
        }

        String commentId = parts[1];
        ResourceResolver resolver = request.getResourceResolver();

        // Find the comment (would need workflowId in production)
        // For demo, search all workflow comments
        Resource varWorkflow = resolver.getResource(COMMENTS_BASE_PATH);
        if (varWorkflow != null) {
            for (Resource workflowFolder : varWorkflow.getChildren()) {
                Resource comment = workflowFolder.getChild(commentId);
                if (comment != null) {
                    try {
                        ModifiableValueMap props = comment.adaptTo(ModifiableValueMap.class);
                        if (props != null) {
                            props.put("resolved", true);
                            props.put("resolvedBy", resolver.getUserID());
                            props.put("resolvedAt", Calendar.getInstance());
                            resolver.commit();

                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":true}");
                            return;
                        }
                    } catch (PersistenceException e) {
                        LOG.error("Failed to resolve comment", e);
                    }
                }
            }
        }

        sendError(response, 404, "Comment not found");
    }

    @Override
    protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String suffix = request.getRequestPathInfo().getSuffix();
        if (suffix == null || suffix.isEmpty()) {
            sendError(response, 400, "Missing comment ID");
            return;
        }

        String commentId = suffix.substring(1); // Remove leading /
        ResourceResolver resolver = request.getResourceResolver();

        // Find and delete the comment
        Resource varWorkflow = resolver.getResource(COMMENTS_BASE_PATH);
        if (varWorkflow != null) {
            for (Resource workflowFolder : varWorkflow.getChildren()) {
                Resource comment = workflowFolder.getChild(commentId);
                if (comment != null) {
                    try {
                        resolver.delete(comment);
                        resolver.commit();

                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":true}");
                        return;
                    } catch (PersistenceException e) {
                        LOG.error("Failed to delete comment", e);
                    }
                }
            }
        }

        sendError(response, 404, "Comment not found");
    }

    private Map<String, Object> buildCommentMap(Resource commentResource) {
        Map<String, Object> comment = new HashMap<>();
        ValueMap props = commentResource.getValueMap();

        comment.put("id", commentResource.getName());
        comment.put("content", props.get("content", ""));
        comment.put("authorId", props.get("authorId", ""));
        comment.put("authorName", props.get("authorName", "Unknown"));
        comment.put("authorRole", props.get("authorRole", "User"));
        comment.put("resolved", props.get("resolved", false));
        comment.put("edited", props.get("edited", false));
        comment.put("parentId", props.get("parentId", String.class));

        Calendar created = props.get("created", Calendar.class);
        if (created != null) {
            comment.put("created", created.getTimeInMillis());
        }

        return comment;
    }

    private void sendError(SlingHttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(GSON.toJson(Collections.singletonMap("error", message)));
    }
}
