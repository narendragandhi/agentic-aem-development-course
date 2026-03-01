package com.demo.workflow.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.util.*;

/**
 * Servlet for managing visual annotations on content
 *
 * Endpoints:
 * GET  /bin/workflow/annotations?workflowId=xxx&contentPath=xxx - Get annotations
 * POST /bin/workflow/annotations - Save annotations
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/workflow/annotations",
        "sling.servlet.methods=GET",
        "sling.servlet.methods=POST"
    }
)
public class WorkflowAnnotationsServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowAnnotationsServlet.class);
    private static final String ANNOTATIONS_BASE_PATH = "/var/workflow/annotations";
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String workflowId = request.getParameter("workflowId");
        String contentPath = request.getParameter("contentPath");

        if (workflowId == null || contentPath == null) {
            sendError(response, 400, "Missing required parameters");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();
        String annotationsPath = buildAnnotationsPath(workflowId, contentPath);
        Resource annotationsResource = resolver.getResource(annotationsPath);

        List<Map<String, Object>> annotations = new ArrayList<>();

        if (annotationsResource != null) {
            String annotationsJson = annotationsResource.getValueMap().get("annotations", "[]");
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            annotations = GSON.fromJson(annotationsJson, listType);
        }

        response.setContentType("application/json");
        response.getWriter().write(GSON.toJson(Collections.singletonMap("annotations", annotations)));
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        JsonObject body = GSON.fromJson(request.getReader(), JsonObject.class);
        String workflowId = body.get("workflowId").getAsString();
        String contentPath = body.get("contentPath").getAsString();
        JsonArray annotations = body.getAsJsonArray("annotations");

        ResourceResolver resolver = request.getResourceResolver();
        String annotationsPath = buildAnnotationsPath(workflowId, contentPath);

        try {
            // Ensure parent folders exist
            String parentPath = annotationsPath.substring(0, annotationsPath.lastIndexOf('/'));
            ResourceUtil.getOrCreateResource(
                resolver,
                parentPath,
                "sling:Folder",
                "sling:Folder",
                false
            );

            // Get or create annotations resource
            Resource annotationsResource = resolver.getResource(annotationsPath);
            Map<String, Object> props = new HashMap<>();
            props.put("jcr:primaryType", "nt:unstructured");
            props.put("workflowId", workflowId);
            props.put("contentPath", contentPath);
            props.put("annotations", annotations.toString());
            props.put("lastModified", Calendar.getInstance());
            props.put("lastModifiedBy", resolver.getUserID());

            if (annotationsResource == null) {
                Resource parent = resolver.getResource(parentPath);
                String nodeName = annotationsPath.substring(annotationsPath.lastIndexOf('/') + 1);
                resolver.create(parent, nodeName, props);
            } else {
                ModifiableValueMap mvp = annotationsResource.adaptTo(ModifiableValueMap.class);
                if (mvp != null) {
                    mvp.putAll(props);
                }
            }

            resolver.commit();

            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true}");

        } catch (PersistenceException e) {
            LOG.error("Failed to save annotations", e);
            sendError(response, 500, "Failed to save annotations");
        }
    }

    /**
     * Builds a safe path for storing annotations based on workflow and content path.
     */
    private String buildAnnotationsPath(String workflowId, String contentPath) {
        // Create a safe node name from content path
        String safePath = contentPath
            .replace("/content/", "")
            .replace("/", "_");

        return ANNOTATIONS_BASE_PATH + "/" + workflowId + "/" + safePath;
    }

    private void sendError(SlingHttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(GSON.toJson(Collections.singletonMap("error", message)));
    }
}
