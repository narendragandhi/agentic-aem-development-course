package com.demo.workflow.services;

import java.util.List;

/**
 * Service for AI-powered code review and quality analysis.
 *
 * <p>Provides automated code analysis including:</p>
 * <ul>
 *   <li>AEM anti-pattern detection</li>
 *   <li>OSGi configuration checks</li>
 *   <li>Resource leak detection</li>
 *   <li>Security vulnerability scanning</li>
 *   <li>Quality scoring</li>
 * </ul>
 */
public interface AICodeReviewService {

    /**
     * Review a single file for code quality issues.
     *
     * @param filePath Path to the file being reviewed
     * @param content The file content to analyze
     * @return Review result with findings
     */
    CodeReviewResult reviewFile(String filePath, String content);

    /**
     * Review multiple files as a batch.
     *
     * @param files List of files to review
     * @return Batch review result with all findings
     */
    BatchReviewResult reviewBatch(List<FileContent> files);

    /**
     * Get all available review rules.
     *
     * @return List of configured review rules
     */
    List<ReviewRule> getAvailableRules();

    /**
     * Calculate quality score from review results.
     *
     * @param results Batch review results
     * @return Quality score with grade
     */
    QualityScore calculateScore(BatchReviewResult results);

    // ═══════════════════════════════════════════════════════════════════
    // Inner Classes
    // ═══════════════════════════════════════════════════════════════════

    /**
     * File content for batch review.
     */
    class FileContent {
        private final String filePath;
        private final String content;

        public FileContent(String filePath, String content) {
            this.filePath = filePath;
            this.content = content;
        }

        public String getFilePath() { return filePath; }
        public String getContent() { return content; }
    }

    /**
     * Result of reviewing a single file.
     */
    class CodeReviewResult {
        private final String filePath;
        private final List<ReviewFinding> findings;
        private final long reviewDurationMs;

        public CodeReviewResult(String filePath, List<ReviewFinding> findings, long reviewDurationMs) {
            this.filePath = filePath;
            this.findings = findings;
            this.reviewDurationMs = reviewDurationMs;
        }

        public String getFilePath() { return filePath; }
        public List<ReviewFinding> getFindings() { return findings; }
        public long getReviewDurationMs() { return reviewDurationMs; }

        public boolean hasFindings() {
            return findings != null && !findings.isEmpty();
        }

        public int getFindingCount() {
            return findings != null ? findings.size() : 0;
        }
    }

    /**
     * Result of batch file review.
     */
    class BatchReviewResult {
        private final List<CodeReviewResult> results;
        private final int totalFiles;
        private final int totalFindings;
        private final long totalDurationMs;

        public BatchReviewResult(List<CodeReviewResult> results, long totalDurationMs) {
            this.results = results;
            this.totalFiles = results.size();
            this.totalFindings = results.stream()
                .mapToInt(CodeReviewResult::getFindingCount)
                .sum();
            this.totalDurationMs = totalDurationMs;
        }

        public List<CodeReviewResult> getResults() { return results; }
        public int getTotalFiles() { return totalFiles; }
        public int getTotalFindings() { return totalFindings; }
        public long getTotalDurationMs() { return totalDurationMs; }
    }

    /**
     * Individual finding from code review.
     */
    class ReviewFinding {
        private final String id;
        private final ReviewSeverity severity;
        private final ReviewCategory category;
        private final String ruleId;
        private final String message;
        private final int lineNumber;
        private final String codeSnippet;
        private final String suggestion;

        public ReviewFinding(String id, ReviewSeverity severity, ReviewCategory category,
                             String ruleId, String message, int lineNumber,
                             String codeSnippet, String suggestion) {
            this.id = id;
            this.severity = severity;
            this.category = category;
            this.ruleId = ruleId;
            this.message = message;
            this.lineNumber = lineNumber;
            this.codeSnippet = codeSnippet;
            this.suggestion = suggestion;
        }

        public String getId() { return id; }
        public ReviewSeverity getSeverity() { return severity; }
        public ReviewCategory getCategory() { return category; }
        public String getRuleId() { return ruleId; }
        public String getMessage() { return message; }
        public int getLineNumber() { return lineNumber; }
        public String getCodeSnippet() { return codeSnippet; }
        public String getSuggestion() { return suggestion; }
    }

    /**
     * Review rule definition.
     */
    class ReviewRule {
        private final String id;
        private final String name;
        private final String description;
        private final ReviewCategory category;
        private final ReviewSeverity defaultSeverity;
        private final boolean enabled;

        public ReviewRule(String id, String name, String description,
                          ReviewCategory category, ReviewSeverity defaultSeverity,
                          boolean enabled) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.defaultSeverity = defaultSeverity;
            this.enabled = enabled;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public ReviewCategory getCategory() { return category; }
        public ReviewSeverity getDefaultSeverity() { return defaultSeverity; }
        public boolean isEnabled() { return enabled; }
    }

    /**
     * Quality score and grade.
     */
    class QualityScore {
        private final int score;
        private final Grade grade;
        private final int criticalCount;
        private final int highCount;
        private final int mediumCount;
        private final int lowCount;

        public QualityScore(int score, int criticalCount, int highCount,
                            int mediumCount, int lowCount) {
            this.score = Math.max(0, Math.min(100, score));
            this.criticalCount = criticalCount;
            this.highCount = highCount;
            this.mediumCount = mediumCount;
            this.lowCount = lowCount;
            this.grade = calculateGrade(this.score);
        }

        private Grade calculateGrade(int score) {
            if (score >= 90) return Grade.A;
            if (score >= 80) return Grade.B;
            if (score >= 70) return Grade.C;
            if (score >= 60) return Grade.D;
            return Grade.F;
        }

        public int getScore() { return score; }
        public Grade getGrade() { return grade; }
        public int getCriticalCount() { return criticalCount; }
        public int getHighCount() { return highCount; }
        public int getMediumCount() { return mediumCount; }
        public int getLowCount() { return lowCount; }

        public boolean isPassing() {
            return grade == Grade.A || grade == Grade.B;
        }
    }

    /**
     * Review severity levels.
     */
    enum ReviewSeverity {
        CRITICAL(20),  // Major defect, blocks deployment
        HIGH(10),      // Significant issue, should fix
        MEDIUM(5),     // Moderate issue, recommended fix
        LOW(1);        // Minor issue, optional fix

        private final int pointDeduction;

        ReviewSeverity(int pointDeduction) {
            this.pointDeduction = pointDeduction;
        }

        public int getPointDeduction() {
            return pointDeduction;
        }
    }

    /**
     * Review finding categories.
     */
    enum ReviewCategory {
        AEM_ANTIPATTERN,
        OSGI_CONFIG,
        RESOURCE_LEAK,
        SECURITY,
        PERFORMANCE,
        WORKFLOW,
        BEST_PRACTICE
    }

    /**
     * Quality grade levels.
     */
    enum Grade {
        A("Excellent", true),
        B("Good", true),
        C("Acceptable", false),
        D("Poor", false),
        F("Failing", false);

        private final String description;
        private final boolean passing;

        Grade(String description, boolean passing) {
            this.description = description;
            this.passing = passing;
        }

        public String getDescription() { return description; }
        public boolean isPassing() { return passing; }
    }
}
