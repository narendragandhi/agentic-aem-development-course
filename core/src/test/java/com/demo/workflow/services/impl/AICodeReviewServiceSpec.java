package com.demo.workflow.services.impl;

import com.demo.workflow.services.AICodeReviewService;
import com.demo.workflow.services.AICodeReviewService.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Specification Tests for AICodeReviewService.
 *
 * Tests: 15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AICodeReviewService Specification")
class AICodeReviewServiceSpec {

    private AICodeReviewService service;

    @BeforeEach
    void setUp() {
        service = new AICodeReviewServiceImpl();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 1: AEM Pattern Detection (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When detecting AEM patterns")
    class AemPatternDetection {

        @Test
        @DisplayName("should detect missing @Reference annotation")
        void shouldDetectMissingReference() {
            String code = "@Component\n" +
                "public class MyService {\n" +
                "    private ResourceResolverFactory resolverFactory;\n" +
                "\n" +
                "    public void doWork() {\n" +
                "        resolverFactory.getServiceResourceResolver(null);\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("MyService.java", code);

            assertTrue(result.hasFindings());
            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.AEM_ANTIPATTERN &&
                f.getRuleId().contains("MISSING_REFERENCE")));
        }

        @Test
        @DisplayName("should detect ResourceResolver not closed")
        void shouldDetectResourceResolverLeak() {
            String code = "public void process() {\n" +
                "    ResourceResolver resolver = resolverFactory.getServiceResourceResolver(authInfo);\n" +
                "    Resource resource = resolver.getResource(\"/content/path\");\n" +
                "    // resolver never closed - leak!\n" +
                "}";

            CodeReviewResult result = service.reviewFile("LeakyService.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.RESOURCE_LEAK &&
                f.getSeverity() == ReviewSeverity.HIGH));
        }

        @Test
        @DisplayName("should detect hardcoded content paths")
        void shouldDetectHardcodedPaths() {
            String code = "public class MyComponent {\n" +
                "    private static final String PATH = \"/content/dam/mysite/images\";\n" +
                "\n" +
                "    public Resource getAsset() {\n" +
                "        return resolver.getResource(\"/content/mysite/en/page\");\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("MyComponent.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.AEM_ANTIPATTERN &&
                f.getMessage().toLowerCase().contains("hardcoded")));
        }

        @Test
        @DisplayName("should return empty for clean code")
        void shouldReturnEmptyForCleanCode() {
            String code = "@Component\n" +
                "public class CleanService {\n" +
                "    @Reference\n" +
                "    private ResourceResolverFactory resolverFactory;\n" +
                "\n" +
                "    @Activate\n" +
                "    @Modified\n" +
                "    protected void activate(Config config) {\n" +
                "        this.basePath = config.basePath();\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("CleanService.java", code);

            // Clean code should have no critical/high findings
            long criticalHigh = result.getFindings().stream()
                .filter(f -> f.getSeverity() == ReviewSeverity.CRITICAL ||
                             f.getSeverity() == ReviewSeverity.HIGH)
                .count();
            assertEquals(0, criticalHigh);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 2: Workflow Analysis (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When analyzing workflow code")
    class WorkflowAnalysis {

        @Test
        @DisplayName("should detect missing exception handling in WorkflowProcess")
        void shouldDetectMissingExceptionHandling() {
            String code = "@Component\n" +
                "public class MyWorkflowProcess implements WorkflowProcess {\n" +
                "    @Override\n" +
                "    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) {\n" +
                "        // No try-catch - workflow could fail silently\n" +
                "        doRiskyOperation();\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("MyWorkflowProcess.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.WORKFLOW &&
                f.getMessage().toLowerCase().contains("exception")));
        }

        @Test
        @DisplayName("should flag synchronous HTTP calls")
        void shouldFlagSynchronousHttpCalls() {
            String code = "public class SyncProcess implements WorkflowProcess {\n" +
                "    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) {\n" +
                "        HttpClient client = HttpClients.createDefault();\n" +
                "        HttpGet request = new HttpGet(\"http://api.example.com/data\");\n" +
                "        client.execute(request);  // Blocking call in workflow!\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("SyncWorkflow.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.PERFORMANCE &&
                f.getSeverity() == ReviewSeverity.MEDIUM));
        }

        @Test
        @DisplayName("should detect missing metadata updates")
        void shouldDetectMissingMetadataUpdates() {
            String code = "public class NoMetadataProcess implements WorkflowProcess {\n" +
                "    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) {\n" +
                "        try {\n" +
                "            processAsset(item);\n" +
                "            // Should update workflow metadata with result\n" +
                "        } catch (Exception e) {\n" +
                "            log.error(\"Failed\", e);\n" +
                "        }\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("NoMetadata.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.WORKFLOW &&
                f.getSuggestion() != null &&
                f.getSuggestion().toLowerCase().contains("metadata")));
        }

        @Test
        @DisplayName("should accept properly structured workflow")
        void shouldAcceptProperWorkflow() {
            String code = "@Component\n" +
                "public class GoodWorkflowProcess implements WorkflowProcess {\n" +
                "    @Reference\n" +
                "    private SomeService service;\n" +
                "\n" +
                "    @Override\n" +
                "    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) {\n" +
                "        try {\n" +
                "            String result = service.process(item);\n" +
                "            item.getWorkflowData().getMetaDataMap().put(\"result\", result);\n" +
                "        } catch (Exception e) {\n" +
                "            log.error(\"Workflow failed\", e);\n" +
                "            throw new WorkflowException(e);\n" +
                "        }\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("GoodWorkflow.java", code);

            // Should have no workflow-related high/critical findings
            long workflowIssues = result.getFindings().stream()
                .filter(f -> f.getCategory() == ReviewCategory.WORKFLOW)
                .filter(f -> f.getSeverity() == ReviewSeverity.HIGH ||
                             f.getSeverity() == ReviewSeverity.CRITICAL)
                .count();
            assertEquals(0, workflowIssues);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 3: Security Checks (3 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When checking security")
    class SecurityChecks {

        @Test
        @DisplayName("should detect hardcoded credentials")
        void shouldDetectHardcodedCredentials() {
            String code = "public class DatabaseService {\n" +
                "    private static final String PASSWORD = \"admin123\";\n" +
                "    private static final String API_KEY = \"sk-abc123xyz\";\n" +
                "\n" +
                "    public void connect() {\n" +
                "        connection.setPassword(\"secretPassword\");\n" +
                "    }\n" +
                "}";

            CodeReviewResult result = service.reviewFile("DatabaseService.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.SECURITY &&
                f.getSeverity() == ReviewSeverity.CRITICAL));
        }

        @Test
        @DisplayName("should flag missing input validation")
        void shouldFlagMissingInputValidation() {
            String code = "@POST\n" +
                "public void handleRequest(@FormParam(\"path\") String path) {\n" +
                "    Resource resource = resolver.getResource(path);\n" +
                "    // Direct use of user input without validation!\n" +
                "    processResource(resource);\n" +
                "}";

            CodeReviewResult result = service.reviewFile("UnsafeServlet.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.SECURITY &&
                f.getMessage().toLowerCase().contains("validation")));
        }

        @Test
        @DisplayName("should detect unsafe service user resolution")
        void shouldDetectUnsafeServiceUser() {
            String code = "public void process() {\n" +
                "    Map<String, Object> param = new HashMap<>();\n" +
                "    param.put(ResourceResolverFactory.SUBSERVICE, \"adminService\");\n" +
                "    // No service user mapping configured\n" +
                "    ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param);\n" +
                "}";

            CodeReviewResult result = service.reviewFile("ServiceUser.java", code);

            assertTrue(result.getFindings().stream().anyMatch(f ->
                f.getCategory() == ReviewCategory.SECURITY ||
                f.getCategory() == ReviewCategory.BEST_PRACTICE));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 4: Quality Scoring (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When calculating quality score")
    class QualityScoring {

        @Test
        @DisplayName("should calculate score with no findings as 100")
        void shouldCalculatePerfectScore() {
            BatchReviewResult batchResult = new BatchReviewResult(
                Collections.singletonList(
                    new CodeReviewResult("Clean.java", Collections.emptyList(), 100)
                ),
                100
            );

            QualityScore score = service.calculateScore(batchResult);

            assertEquals(100, score.getScore());
            assertEquals(Grade.A, score.getGrade());
            assertTrue(score.isPassing());
        }

        @Test
        @DisplayName("should deduct points for findings")
        void shouldDeductPointsForFindings() {
            List<ReviewFinding> findings = Arrays.asList(
                new ReviewFinding("1", ReviewSeverity.CRITICAL, ReviewCategory.SECURITY,
                    "SEC001", "Critical issue", 1, "", "Fix it"),
                new ReviewFinding("2", ReviewSeverity.HIGH, ReviewCategory.RESOURCE_LEAK,
                    "LEAK001", "High issue", 2, "", "Close it")
            );

            BatchReviewResult batchResult = new BatchReviewResult(
                Collections.singletonList(
                    new CodeReviewResult("Issues.java", findings, 100)
                ),
                100
            );

            QualityScore score = service.calculateScore(batchResult);

            // 100 - 20 (critical) - 10 (high) = 70
            assertEquals(70, score.getScore());
            assertEquals(Grade.C, score.getGrade());
        }

        @Test
        @DisplayName("should assign correct grade")
        void shouldAssignCorrectGrade() {
            // Create findings to get score of 65 (D grade)
            // 100 - 20 - 10 - 5 = 65
            List<ReviewFinding> findings = Arrays.asList(
                new ReviewFinding("1", ReviewSeverity.CRITICAL, ReviewCategory.SECURITY,
                    "SEC001", "Critical", 1, "", ""),
                new ReviewFinding("2", ReviewSeverity.HIGH, ReviewCategory.AEM_ANTIPATTERN,
                    "AEM001", "High", 2, "", ""),
                new ReviewFinding("3", ReviewSeverity.MEDIUM, ReviewCategory.BEST_PRACTICE,
                    "BP001", "Medium", 3, "", "")
            );

            BatchReviewResult batchResult = new BatchReviewResult(
                Collections.singletonList(
                    new CodeReviewResult("Mixed.java", findings, 100)
                ),
                100
            );

            QualityScore score = service.calculateScore(batchResult);

            assertEquals(Grade.D, score.getGrade());
            assertFalse(score.isPassing());
        }

        @Test
        @DisplayName("should handle empty file list")
        void shouldHandleEmptyFileList() {
            BatchReviewResult batchResult = new BatchReviewResult(
                Collections.emptyList(),
                0
            );

            QualityScore score = service.calculateScore(batchResult);

            assertEquals(100, score.getScore());
            assertEquals(Grade.A, score.getGrade());
        }
    }
}
