package com.demo.workflow.services.impl;

import com.demo.workflow.services.AntivirusScanService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Health Check Servlet for Antivirus Scanning Service
 *
 * Provides a simple HTTP endpoint for monitoring:
 * GET /bin/workflow/health/antivirus
 *
 * Returns JSON response:
 * {
 *   "status": "OK" | "WARN" | "ERROR",
 *   "available": true | false,
 *   "engine": "ClamAV" | "MOCK" | etc,
 *   "details": "..."
 * }
 *
 * Can be integrated with:
 * - Load balancer health checks
 * - Monitoring systems (Datadog, New Relic, etc.)
 * - AEM Health Reports
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                    ANTIVIRUS HEALTH CHECK                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │   Endpoint: /bin/workflow/health/antivirus                             │
 * │                                                                         │
 * │   Response Codes:                                                       │
 * │   • 200 - Service healthy and available                                 │
 * │   • 503 - Service unavailable or degraded                              │
 * │                                                                         │
 * │   Checks Performed:                                                     │
 * │   ✓ Service registration                                                │
 * │   ✓ Scanner availability                                                │
 * │   ✓ Engine connectivity (ClamAV PING)                                  │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/workflow/health/antivirus",
        "sling.servlet.methods=GET"
    }
)
public class AntivirusHealthCheck extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AntivirusHealthCheck.class);

    @Reference
    private AntivirusScanService antivirusScanService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            HealthCheckResult result = performHealthCheck();

            // Set HTTP status based on health
            if (result.status == Status.ERROR) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
            }

            // Write JSON response
            out.println("{");
            out.println("  \"status\": \"" + result.status + "\",");
            out.println("  \"available\": " + result.available + ",");
            out.println("  \"engine\": \"" + escapeJson(result.engine) + "\",");
            out.println("  \"details\": \"" + escapeJson(result.details) + "\",");
            out.println("  \"timestamp\": " + System.currentTimeMillis());
            out.println("}");

        } catch (Exception e) {
            LOG.error("Health check failed with exception", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{");
            out.println("  \"status\": \"ERROR\",");
            out.println("  \"available\": false,");
            out.println("  \"details\": \"" + escapeJson(e.getMessage()) + "\"");
            out.println("}");
        }
    }

    /**
     * Perform the health check
     */
    private HealthCheckResult performHealthCheck() {
        HealthCheckResult result = new HealthCheckResult();

        // Check 1: Service registered
        if (antivirusScanService == null) {
            result.status = Status.ERROR;
            result.available = false;
            result.engine = "N/A";
            result.details = "AntivirusScanService not registered";
            return result;
        }

        result.engine = antivirusScanService.getScanEngineName();

        // Check 2: Service availability
        boolean available = antivirusScanService.isAvailable();
        result.available = available;

        if (!available) {
            result.status = Status.ERROR;
            result.details = "Antivirus scanner not available";
            return result;
        }

        // Check 3: Engine-specific checks
        switch (result.engine.toUpperCase()) {
            case "MOCK":
                result.status = Status.WARN;
                result.details = "Using MOCK scanner - not suitable for production";
                break;
            case "CLAMAV":
                result.status = Status.OK;
                result.details = "ClamAV daemon connected and responding";
                break;
            case "REST_API":
                result.status = Status.OK;
                result.details = "REST API scanner configured";
                break;
            default:
                result.status = Status.OK;
                result.details = "Scanner available: " + result.engine;
        }

        return result;
    }

    /**
     * Health check status
     */
    enum Status {
        OK,
        WARN,
        ERROR
    }

    /**
     * Health check result holder
     */
    static class HealthCheckResult {
        Status status = Status.OK;
        boolean available = false;
        String engine = "Unknown";
        String details = "";
    }

    /**
     * Escape JSON special characters
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}
