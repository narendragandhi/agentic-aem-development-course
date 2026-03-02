package com.demo.workflow.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service for workflow performance testing and analysis.
 *
 * <p>Provides performance measurement capabilities including:</p>
 * <ul>
 *   <li>Execution time measurement</li>
 *   <li>Resource usage monitoring</li>
 *   <li>SLA verification</li>
 *   <li>Baseline comparison</li>
 * </ul>
 */
public interface PerformanceTestingService {

    /**
     * Run a complete performance test with the given configuration.
     *
     * @param config Test configuration
     * @return Performance test results
     */
    PerformanceResult runTest(PerformanceTestConfig config);

    /**
     * Measure execution time of a single operation.
     *
     * @param operation The operation to measure
     * @param operationName Name for the operation
     * @return Timing result
     */
    TimingResult measureExecution(Runnable operation, String operationName);

    /**
     * Get current resource usage snapshot.
     *
     * @return Current resource metrics
     */
    ResourceSnapshot getResourceSnapshot();

    /**
     * Compare results against a baseline.
     *
     * @param current Current performance results
     * @param baseline Baseline to compare against
     * @return Comparison result
     */
    ComparisonResult compareToBaseline(PerformanceResult current, PerformanceResult baseline);

    /**
     * Check if performance meets SLA requirements.
     *
     * @param result Performance results to check
     * @param sla SLA definition
     * @return SLA check result
     */
    SLACheckResult checkSLA(PerformanceResult result, SLADefinition sla);

    // ═══════════════════════════════════════════════════════════════════
    // Inner Classes
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Configuration for a performance test.
     */
    class PerformanceTestConfig {
        private final String testName;
        private final int iterations;
        private final int warmupIterations;
        private final int concurrentUsers;

        public PerformanceTestConfig(String testName, int iterations,
                                     int warmupIterations, int concurrentUsers) {
            this.testName = testName;
            this.iterations = iterations;
            this.warmupIterations = warmupIterations;
            this.concurrentUsers = concurrentUsers;
        }

        public String getTestName() { return testName; }
        public int getIterations() { return iterations; }
        public int getWarmupIterations() { return warmupIterations; }
        public int getConcurrentUsers() { return concurrentUsers; }
    }

    /**
     * Results of a performance test.
     */
    class PerformanceResult {
        private final String testName;
        private final TimingResult timing;
        private final ResourceSnapshot resourceUsage;
        private final int successCount;
        private final int errorCount;
        private final Instant timestamp;

        public PerformanceResult(String testName, TimingResult timing,
                                 ResourceSnapshot resourceUsage,
                                 int successCount, int errorCount) {
            this.testName = testName;
            this.timing = timing;
            this.resourceUsage = resourceUsage;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.timestamp = Instant.now();
        }

        public String getTestName() { return testName; }
        public TimingResult getTiming() { return timing; }
        public ResourceSnapshot getResourceUsage() { return resourceUsage; }
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public Instant getTimestamp() { return timestamp; }

        public double getSuccessRate() {
            int total = successCount + errorCount;
            return total > 0 ? (double) successCount / total * 100 : 100.0;
        }

        public double getErrorRate() {
            return 100.0 - getSuccessRate();
        }
    }

    /**
     * Timing measurement results.
     */
    class TimingResult {
        private final String operationName;
        private final long executionTimeMs;
        private final long minTimeMs;
        private final long maxTimeMs;
        private final long avgTimeMs;
        private final int sampleCount;
        private final boolean success;
        private final String errorMessage;

        public TimingResult(String operationName, long executionTimeMs,
                            long minTimeMs, long maxTimeMs, long avgTimeMs,
                            int sampleCount, boolean success, String errorMessage) {
            this.operationName = operationName;
            this.executionTimeMs = executionTimeMs;
            this.minTimeMs = minTimeMs;
            this.maxTimeMs = maxTimeMs;
            this.avgTimeMs = avgTimeMs;
            this.sampleCount = sampleCount;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        // Single execution constructor
        public TimingResult(String operationName, long executionTimeMs,
                            boolean success, String errorMessage) {
            this(operationName, executionTimeMs, executionTimeMs,
                 executionTimeMs, executionTimeMs, 1, success, errorMessage);
        }

        public String getOperationName() { return operationName; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public long getMinTimeMs() { return minTimeMs; }
        public long getMaxTimeMs() { return maxTimeMs; }
        public long getAvgTimeMs() { return avgTimeMs; }
        public int getSampleCount() { return sampleCount; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Resource usage snapshot.
     */
    class ResourceSnapshot {
        private final long usedMemoryMb;
        private final long maxMemoryMb;
        private final int threadCount;
        private final double cpuUsagePercent;
        private final Instant timestamp;

        public ResourceSnapshot(long usedMemoryMb, long maxMemoryMb,
                                int threadCount, double cpuUsagePercent) {
            this.usedMemoryMb = usedMemoryMb;
            this.maxMemoryMb = maxMemoryMb;
            this.threadCount = threadCount;
            this.cpuUsagePercent = cpuUsagePercent;
            this.timestamp = Instant.now();
        }

        public long getUsedMemoryMb() { return usedMemoryMb; }
        public long getMaxMemoryMb() { return maxMemoryMb; }
        public int getThreadCount() { return threadCount; }
        public double getCpuUsagePercent() { return cpuUsagePercent; }
        public Instant getTimestamp() { return timestamp; }

        public double getMemoryUsagePercent() {
            return maxMemoryMb > 0 ? (double) usedMemoryMb / maxMemoryMb * 100 : 0;
        }
    }

    /**
     * SLA definition with thresholds.
     */
    class SLADefinition {
        private final long targetResponseTimeMs;
        private final long warningResponseTimeMs;
        private final long criticalResponseTimeMs;
        private final double maxErrorRatePercent;
        private final long maxMemoryMb;

        public SLADefinition(long targetResponseTimeMs, long warningResponseTimeMs,
                             long criticalResponseTimeMs, double maxErrorRatePercent,
                             long maxMemoryMb) {
            this.targetResponseTimeMs = targetResponseTimeMs;
            this.warningResponseTimeMs = warningResponseTimeMs;
            this.criticalResponseTimeMs = criticalResponseTimeMs;
            this.maxErrorRatePercent = maxErrorRatePercent;
            this.maxMemoryMb = maxMemoryMb;
        }

        public long getTargetResponseTimeMs() { return targetResponseTimeMs; }
        public long getWarningResponseTimeMs() { return warningResponseTimeMs; }
        public long getCriticalResponseTimeMs() { return criticalResponseTimeMs; }
        public double getMaxErrorRatePercent() { return maxErrorRatePercent; }
        public long getMaxMemoryMb() { return maxMemoryMb; }
    }

    /**
     * SLA check result.
     */
    class SLACheckResult {
        private final PerformanceStatus status;
        private final List<String> violations;
        private final Map<String, MetricStatus> metricStatuses;

        public SLACheckResult(PerformanceStatus status, List<String> violations,
                              Map<String, MetricStatus> metricStatuses) {
            this.status = status;
            this.violations = violations;
            this.metricStatuses = metricStatuses;
        }

        public PerformanceStatus getStatus() { return status; }
        public List<String> getViolations() { return violations; }
        public Map<String, MetricStatus> getMetricStatuses() { return metricStatuses; }

        public boolean isPassing() {
            return status == PerformanceStatus.HEALTHY ||
                   status == PerformanceStatus.WARNING;
        }
    }

    /**
     * Comparison result against baseline.
     */
    class ComparisonResult {
        private final double responseTimeChangePercent;
        private final double memoryChangePercent;
        private final double errorRateChangePercent;
        private final ComparisonOutcome outcome;

        public ComparisonResult(double responseTimeChangePercent,
                                double memoryChangePercent,
                                double errorRateChangePercent,
                                ComparisonOutcome outcome) {
            this.responseTimeChangePercent = responseTimeChangePercent;
            this.memoryChangePercent = memoryChangePercent;
            this.errorRateChangePercent = errorRateChangePercent;
            this.outcome = outcome;
        }

        public double getResponseTimeChangePercent() { return responseTimeChangePercent; }
        public double getMemoryChangePercent() { return memoryChangePercent; }
        public double getErrorRateChangePercent() { return errorRateChangePercent; }
        public ComparisonOutcome getOutcome() { return outcome; }

        public boolean isImproved() {
            return outcome == ComparisonOutcome.IMPROVED;
        }

        public boolean isDegraded() {
            return outcome == ComparisonOutcome.DEGRADED;
        }
    }

    /**
     * Performance status levels.
     */
    enum PerformanceStatus {
        HEALTHY,    // All metrics within target
        WARNING,    // Some metrics above warning threshold
        DEGRADED,   // Significant performance degradation
        CRITICAL    // Severe performance issues
    }

    /**
     * Individual metric status.
     */
    enum MetricStatus {
        PASS,
        WARNING,
        FAIL
    }

    /**
     * Comparison outcome.
     */
    enum ComparisonOutcome {
        IMPROVED,   // Better than baseline
        STABLE,     // Within acceptable variance
        DEGRADED,   // Worse than baseline
        NO_BASELINE // No baseline available
    }
}
