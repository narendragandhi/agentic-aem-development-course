package com.demo.workflow.services.impl;

import com.demo.workflow.services.AICodeReviewService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of AICodeReviewService for automated code quality analysis.
 *
 * <p>Provides rule-based code analysis including:</p>
 * <ul>
 *   <li>AEM anti-pattern detection</li>
 *   <li>OSGi configuration checks</li>
 *   <li>Resource leak detection</li>
 *   <li>Security vulnerability scanning</li>
 *   <li>Quality scoring</li>
 * </ul>
 *
 * @see AICodeReviewService
 */
@Component(service = AICodeReviewService.class, immediate = true)
public class AICodeReviewServiceImpl implements AICodeReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(AICodeReviewServiceImpl.class);

    // ═══════════════════════════════════════════════════════════════════
    // Review Rules
    // ═══════════════════════════════════════════════════════════════════

    private static final List<ReviewRule> RULES = Arrays.asList(
        // AEM Antipatterns
        new ReviewRule("AEM001", "Missing @Reference",
            "OSGi service field without @Reference annotation",
            ReviewCategory.AEM_ANTIPATTERN, ReviewSeverity.HIGH, true),
        new ReviewRule("AEM002", "Hardcoded Path",
            "Hardcoded content path instead of configuration",
            ReviewCategory.AEM_ANTIPATTERN, ReviewSeverity.MEDIUM, true),

        // Resource Leaks
        new ReviewRule("LEAK001", "ResourceResolver Leak",
            "ResourceResolver opened but not closed",
            ReviewCategory.RESOURCE_LEAK, ReviewSeverity.HIGH, true),

        // Workflow Issues
        new ReviewRule("WF001", "Missing Exception Handling",
            "WorkflowProcess without try-catch block",
            ReviewCategory.WORKFLOW, ReviewSeverity.MEDIUM, true),
        new ReviewRule("WF002", "Synchronous HTTP Call",
            "Blocking HTTP call in workflow process",
            ReviewCategory.PERFORMANCE, ReviewSeverity.MEDIUM, true),
        new ReviewRule("WF003", "Missing Metadata Update",
            "Workflow process without metadata updates",
            ReviewCategory.WORKFLOW, ReviewSeverity.LOW, true),

        // Security
        new ReviewRule("SEC001", "Hardcoded Credentials",
            "Password or API key hardcoded in source",
            ReviewCategory.SECURITY, ReviewSeverity.CRITICAL, true),
        new ReviewRule("SEC002", "Missing Input Validation",
            "User input used without validation",
            ReviewCategory.SECURITY, ReviewSeverity.HIGH, true),
        new ReviewRule("SEC003", "Service User Resolution",
            "Service user without proper mapping",
            ReviewCategory.SECURITY, ReviewSeverity.MEDIUM, true)
    );

    // ═══════════════════════════════════════════════════════════════════
    // Detection Patterns
    // ═══════════════════════════════════════════════════════════════════

    // AEM Patterns
    private static final Pattern RESOURCE_RESOLVER_FACTORY_FIELD = Pattern.compile(
        "private\\s+ResourceResolverFactory\\s+\\w+", Pattern.MULTILINE);
    private static final Pattern REFERENCE_ANNOTATION = Pattern.compile(
        "@Reference[\\s\\n]+private\\s+ResourceResolverFactory");
    private static final Pattern HARDCODED_PATH = Pattern.compile(
        "/content/[a-zA-Z0-9/_-]+", Pattern.MULTILINE);
    private static final Pattern GET_RESOURCE_LITERAL = Pattern.compile(
        "getResource\\s*\\(\\s*\"/content/");

    // Resource Leak Patterns
    private static final Pattern RESOLVER_GET = Pattern.compile(
        "getServiceResourceResolver|getResourceResolver");
    private static final Pattern RESOLVER_CLOSE = Pattern.compile(
        "resolver\\.close\\(\\)|try\\s*\\([^)]*ResourceResolver");

    // Workflow Patterns
    private static final Pattern WORKFLOW_PROCESS = Pattern.compile(
        "implements\\s+WorkflowProcess");
    private static final Pattern TRY_CATCH = Pattern.compile(
        "try\\s*\\{");
    private static final Pattern HTTP_CLIENT_EXECUTE = Pattern.compile(
        "client\\.execute\\(|HttpClient.*execute");
    private static final Pattern METADATA_PUT = Pattern.compile(
        "getMetaDataMap\\(\\)\\.put|MetaDataMap.*put");

    // Security Patterns
    private static final Pattern HARDCODED_PASSWORD = Pattern.compile(
        "(PASSWORD|password|Password)\\s*=\\s*\"[^\"]+\"");
    private static final Pattern HARDCODED_API_KEY = Pattern.compile(
        "(API_KEY|apiKey|api_key|API_SECRET|secret)\\s*=\\s*\"[^\"]+\"");
    private static final Pattern SET_PASSWORD = Pattern.compile(
        "setPassword\\s*\\(\\s*\"[^\"]+\"\\s*\\)");
    private static final Pattern FORM_PARAM_DIRECT_USE = Pattern.compile(
        "@FormParam.*String\\s+(\\w+).*getResource\\s*\\(\\s*\\1\\s*\\)", Pattern.DOTALL);
    private static final Pattern USER_INPUT_NO_VALIDATION = Pattern.compile(
        "@FormParam.*getResource", Pattern.DOTALL);
    private static final Pattern SERVICE_USER_RESOLUTION = Pattern.compile(
        "SUBSERVICE.*getServiceResourceResolver", Pattern.DOTALL);

    @Override
    public CodeReviewResult reviewFile(String filePath, String content) {
        long startTime = System.currentTimeMillis();
        List<ReviewFinding> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return new CodeReviewResult(filePath, findings, 0);
        }

        // Run all checks
        findings.addAll(checkAemPatterns(content));
        findings.addAll(checkResourceLeaks(content));
        findings.addAll(checkWorkflowPatterns(content));
        findings.addAll(checkSecurityPatterns(content));

        long duration = System.currentTimeMillis() - startTime;
        LOG.debug("Reviewed {} in {}ms, found {} issues", filePath, duration, findings.size());

        return new CodeReviewResult(filePath, findings, duration);
    }

    @Override
    public BatchReviewResult reviewBatch(List<FileContent> files) {
        long startTime = System.currentTimeMillis();
        List<CodeReviewResult> results = new ArrayList<>();

        for (FileContent file : files) {
            results.add(reviewFile(file.getFilePath(), file.getContent()));
        }

        long duration = System.currentTimeMillis() - startTime;
        return new BatchReviewResult(results, duration);
    }

    @Override
    public List<ReviewRule> getAvailableRules() {
        return Collections.unmodifiableList(RULES);
    }

    @Override
    public QualityScore calculateScore(BatchReviewResult results) {
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (CodeReviewResult result : results.getResults()) {
            for (ReviewFinding finding : result.getFindings()) {
                switch (finding.getSeverity()) {
                    case CRITICAL:
                        criticalCount++;
                        break;
                    case HIGH:
                        highCount++;
                        break;
                    case MEDIUM:
                        mediumCount++;
                        break;
                    case LOW:
                        lowCount++;
                        break;
                }
            }
        }

        // Score = 100 - (CRITICAL × 20) - (HIGH × 10) - (MEDIUM × 5) - (LOW × 1)
        int score = 100
            - (criticalCount * ReviewSeverity.CRITICAL.getPointDeduction())
            - (highCount * ReviewSeverity.HIGH.getPointDeduction())
            - (mediumCount * ReviewSeverity.MEDIUM.getPointDeduction())
            - (lowCount * ReviewSeverity.LOW.getPointDeduction());

        return new QualityScore(score, criticalCount, highCount, mediumCount, lowCount);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Pattern Check Methods
    // ═══════════════════════════════════════════════════════════════════

    private List<ReviewFinding> checkAemPatterns(String content) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Check for missing @Reference on ResourceResolverFactory
        if (RESOURCE_RESOLVER_FACTORY_FIELD.matcher(content).find() &&
            !REFERENCE_ANNOTATION.matcher(content).find()) {
            findings.add(createFinding(
                "AEM001", "MISSING_REFERENCE",
                ReviewSeverity.HIGH,
                ReviewCategory.AEM_ANTIPATTERN,
                "ResourceResolverFactory field missing @Reference annotation",
                findLineNumber(content, "ResourceResolverFactory"),
                "Add @Reference annotation above the field"
            ));
        }

        // Check for hardcoded paths
        if (HARDCODED_PATH.matcher(content).find() || GET_RESOURCE_LITERAL.matcher(content).find()) {
            // Only flag if not in a constant with proper config
            if (!content.contains("@Property") && !content.contains("config.")) {
                findings.add(createFinding(
                    "AEM002", "HARDCODED_PATH",
                    ReviewSeverity.MEDIUM,
                    ReviewCategory.AEM_ANTIPATTERN,
                    "Hardcoded content path found - use OSGi configuration instead",
                    findLineNumber(content, "/content/"),
                    "Move path to OSGi configuration"
                ));
            }
        }

        return findings;
    }

    private List<ReviewFinding> checkResourceLeaks(String content) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Check for ResourceResolver opened but not closed
        if (RESOLVER_GET.matcher(content).find() && !RESOLVER_CLOSE.matcher(content).find()) {
            findings.add(createFinding(
                "LEAK001", "RESOURCE_RESOLVER_LEAK",
                ReviewSeverity.HIGH,
                ReviewCategory.RESOURCE_LEAK,
                "ResourceResolver opened but not closed - potential memory leak",
                findLineNumber(content, "getServiceResourceResolver"),
                "Use try-with-resources or ensure resolver.close() is called in finally block"
            ));
        }

        return findings;
    }

    private List<ReviewFinding> checkWorkflowPatterns(String content) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Only check workflow-related code
        if (!WORKFLOW_PROCESS.matcher(content).find()) {
            return findings;
        }

        // Check for missing exception handling
        if (!TRY_CATCH.matcher(content).find()) {
            findings.add(createFinding(
                "WF001", "MISSING_EXCEPTION_HANDLING",
                ReviewSeverity.MEDIUM,
                ReviewCategory.WORKFLOW,
                "WorkflowProcess execute method without exception handling",
                findLineNumber(content, "public void execute"),
                "Wrap workflow logic in try-catch and handle exceptions appropriately"
            ));
        }

        // Check for synchronous HTTP calls
        if (HTTP_CLIENT_EXECUTE.matcher(content).find()) {
            findings.add(createFinding(
                "WF002", "SYNC_HTTP_CALL",
                ReviewSeverity.MEDIUM,
                ReviewCategory.PERFORMANCE,
                "Synchronous HTTP call in workflow process may cause delays",
                findLineNumber(content, "execute"),
                "Consider using async HTTP client or moving to a separate job"
            ));
        }

        // Check for missing metadata updates
        if (!METADATA_PUT.matcher(content).find()) {
            findings.add(createFinding(
                "WF003", "MISSING_METADATA_UPDATE",
                ReviewSeverity.LOW,
                ReviewCategory.WORKFLOW,
                "Workflow process does not update workflow metadata with results",
                findLineNumber(content, "execute"),
                "Update workflow metadata with processing results for traceability"
            ));
        }

        return findings;
    }

    private List<ReviewFinding> checkSecurityPatterns(String content) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Check for hardcoded credentials
        if (HARDCODED_PASSWORD.matcher(content).find() ||
            HARDCODED_API_KEY.matcher(content).find() ||
            SET_PASSWORD.matcher(content).find()) {
            findings.add(createFinding(
                "SEC001", "HARDCODED_CREDENTIALS",
                ReviewSeverity.CRITICAL,
                ReviewCategory.SECURITY,
                "Hardcoded credentials found in source code - critical security risk",
                findCredentialLineNumber(content),
                "Move credentials to OSGi configuration or secret management system"
            ));
        }

        // Check for missing input validation
        if (USER_INPUT_NO_VALIDATION.matcher(content).find()) {
            findings.add(createFinding(
                "SEC002", "MISSING_INPUT_VALIDATION",
                ReviewSeverity.HIGH,
                ReviewCategory.SECURITY,
                "User input used directly without validation - potential injection risk",
                findLineNumber(content, "@FormParam"),
                "Validate and sanitize user input before use"
            ));
        }

        // Check for service user resolution (informational)
        if (SERVICE_USER_RESOLUTION.matcher(content).find()) {
            findings.add(createFinding(
                "SEC003", "SERVICE_USER_RESOLUTION",
                ReviewSeverity.MEDIUM,
                ReviewCategory.BEST_PRACTICE,
                "Service user resolution - ensure proper service user mapping is configured",
                findLineNumber(content, "SUBSERVICE"),
                "Verify service user mapping in repository initialization"
            ));
        }

        return findings;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private ReviewFinding createFinding(String id, String ruleId, ReviewSeverity severity,
                                        ReviewCategory category, String message,
                                        int lineNumber, String suggestion) {
        return new ReviewFinding(
            UUID.randomUUID().toString(),
            severity,
            category,
            ruleId,
            message,
            lineNumber,
            "", // code snippet could be extracted here
            suggestion
        );
    }

    private int findLineNumber(String content, String searchText) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(searchText)) {
                return i + 1;
            }
        }
        return 1;
    }

    private int findCredentialLineNumber(String content) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            if (line.contains("password") || line.contains("api_key") ||
                line.contains("apikey") || line.contains("secret")) {
                return i + 1;
            }
        }
        return 1;
    }
}
