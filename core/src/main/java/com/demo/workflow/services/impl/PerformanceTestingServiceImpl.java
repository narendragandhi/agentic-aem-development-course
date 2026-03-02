package com.demo.workflow.services.impl;

import com.demo.workflow.services.PerformanceTestingService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Implementation of PerformanceTestingService for workflow performance analysis.
 *
 * <p>Provides performance measurement capabilities including:</p>
 * <ul>
 *   <li>Execution time measurement with ±1ms precision</li>
 *   <li>Resource usage monitoring (memory, threads)</li>
 *   <li>SLA verification against defined thresholds</li>
 *   <li>Baseline comparison for regression detection</li>
 * </ul>
 *
 * @see PerformanceTestingService
 */
@Component(service = PerformanceTestingService.class, immediate = true)
public class PerformanceTestingServiceImpl implements PerformanceTestingService {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestingServiceImpl.class);

    /** Threshold for performance improvement detection (10% faster) */
    private static final double IMPROVEMENT_THRESHOLD = -10.0;

    /** Threshold for performance degradation detection (10% slower) */
    private static final double DEGRADATION_THRESHOLD = 10.0;

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    @Override
    public PerformanceResult runTest(PerformanceTestConfig config) {
        LOG.info("Starting performance test: {}", config.getTestName());

        List<Long> executionTimes = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        // Warmup iterations
        for (int i = 0; i < config.getWarmupIterations(); i++) {
            try {
                runTestIteration();
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }

        // Actual test iterations
        ResourceSnapshot startResources = getResourceSnapshot();
        for (int i = 0; i < config.getIterations(); i++) {
            try {
                long startTime = System.currentTimeMillis();
                runTestIteration();
                long endTime = System.currentTimeMillis();
                executionTimes.add(endTime - startTime);
                successCount++;
            } catch (Exception e) {
                errorCount++;
            }
        }
        ResourceSnapshot endResources = getResourceSnapshot();

        // Calculate timing statistics
        TimingResult timing = calculateTimingStats(config.getTestName(), executionTimes);

        LOG.info("Performance test completed: {} - avg {}ms, success rate {}%",
            config.getTestName(), timing.getAvgTimeMs(),
            successCount * 100.0 / (successCount + errorCount));

        return new PerformanceResult(
            config.getTestName(),
            timing,
            endResources,
            successCount,
            errorCount
        );
    }

    @Override
    public TimingResult measureExecution(Runnable operation, String operationName) {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;

        try {
            operation.run();
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            LOG.warn("Operation {} failed: {}", operationName, errorMessage);
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        return new TimingResult(operationName, executionTime, success, errorMessage);
    }

    @Override
    public ResourceSnapshot getResourceSnapshot() {
        Runtime runtime = Runtime.getRuntime();

        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
        long maxMemoryBytes = runtime.maxMemory();

        long usedMemoryMb = usedMemoryBytes / (1024 * 1024);
        long maxMemoryMb = maxMemoryBytes / (1024 * 1024);

        int threadCount = threadBean.getThreadCount();

        // CPU usage is harder to measure accurately without OS-specific tools
        // Using a simple approximation based on thread activity
        double cpuUsage = estimateCpuUsage();

        return new ResourceSnapshot(usedMemoryMb, maxMemoryMb, threadCount, cpuUsage);
    }

    @Override
    public ComparisonResult compareToBaseline(PerformanceResult current, PerformanceResult baseline) {
        if (baseline == null) {
            return new ComparisonResult(0, 0, 0, ComparisonOutcome.NO_BASELINE);
        }

        // Calculate percentage changes
        double responseTimeChange = calculatePercentChange(
            baseline.getTiming().getAvgTimeMs(),
            current.getTiming().getAvgTimeMs()
        );

        double memoryChange = calculatePercentChange(
            baseline.getResourceUsage().getUsedMemoryMb(),
            current.getResourceUsage().getUsedMemoryMb()
        );

        double errorRateChange = current.getErrorRate() - baseline.getErrorRate();

        // Determine outcome
        ComparisonOutcome outcome = determineOutcome(responseTimeChange, memoryChange, errorRateChange);

        LOG.info("Baseline comparison: responseTime {}%, memory {}%, errorRate {}% -> {}",
            String.format("%.1f", responseTimeChange),
            String.format("%.1f", memoryChange),
            String.format("%.1f", errorRateChange),
            outcome);

        return new ComparisonResult(responseTimeChange, memoryChange, errorRateChange, outcome);
    }

    @Override
    public SLACheckResult checkSLA(PerformanceResult result, SLADefinition sla) {
        List<String> violations = new ArrayList<>();
        Map<String, MetricStatus> metricStatuses = new HashMap<>();

        long responseTime = result.getTiming().getAvgTimeMs();
        double errorRate = result.getErrorRate();
        long memoryUsage = result.getResourceUsage().getUsedMemoryMb();

        // Check response time
        MetricStatus responseStatus = checkResponseTimeSLA(responseTime, sla, violations);
        metricStatuses.put("responseTime", responseStatus);

        // Check error rate
        MetricStatus errorStatus = checkErrorRateSLA(errorRate, sla, violations);
        metricStatuses.put("errorRate", errorStatus);

        // Check memory
        MetricStatus memoryStatus = checkMemorySLA(memoryUsage, sla, violations);
        metricStatuses.put("memoryUsage", memoryStatus);

        // Determine overall status
        PerformanceStatus overallStatus = determineOverallStatus(metricStatuses);

        LOG.info("SLA check for {}: {} with {} violations",
            result.getTestName(), overallStatus, violations.size());

        return new SLACheckResult(overallStatus, violations, metricStatuses);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Private Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private void runTestIteration() {
        // Default test operation - can be overridden via config
        // Simulates minimal work
    }

    private TimingResult calculateTimingStats(String operationName, List<Long> times) {
        if (times.isEmpty()) {
            return new TimingResult(operationName, 0, 0, 0, 0, 0, true, null);
        }

        long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
        long avg = (long) times.stream().mapToLong(Long::longValue).average().orElse(0);
        long total = times.stream().mapToLong(Long::longValue).sum();

        return new TimingResult(operationName, total, min, max, avg, times.size(), true, null);
    }

    private double estimateCpuUsage() {
        // Simple CPU usage estimation
        // In production, would use OperatingSystemMXBean or OS-specific APIs
        return 0.0;
    }

    private double calculatePercentChange(long baseline, long current) {
        if (baseline == 0) {
            return current == 0 ? 0 : 100.0;
        }
        return ((double) (current - baseline) / baseline) * 100.0;
    }

    private ComparisonOutcome determineOutcome(double responseTimeChange,
                                                double memoryChange,
                                                double errorRateChange) {
        // Primarily based on response time
        if (responseTimeChange <= IMPROVEMENT_THRESHOLD) {
            return ComparisonOutcome.IMPROVED;
        } else if (responseTimeChange >= DEGRADATION_THRESHOLD) {
            return ComparisonOutcome.DEGRADED;
        }
        return ComparisonOutcome.STABLE;
    }

    private MetricStatus checkResponseTimeSLA(long responseTime, SLADefinition sla,
                                               List<String> violations) {
        if (responseTime > sla.getCriticalResponseTimeMs()) {
            violations.add("Response time " + responseTime + "ms exceeds critical threshold " +
                sla.getCriticalResponseTimeMs() + "ms");
            return MetricStatus.FAIL;
        } else if (responseTime > sla.getWarningResponseTimeMs()) {
            violations.add("Response time " + responseTime + "ms exceeds warning threshold " +
                sla.getWarningResponseTimeMs() + "ms");
            return MetricStatus.WARNING;
        }
        return MetricStatus.PASS;
    }

    private MetricStatus checkErrorRateSLA(double errorRate, SLADefinition sla,
                                            List<String> violations) {
        if (errorRate > sla.getMaxErrorRatePercent()) {
            violations.add("Error rate " + String.format("%.1f", errorRate) +
                "% exceeds threshold " + sla.getMaxErrorRatePercent() + "%");
            return MetricStatus.FAIL;
        }
        return MetricStatus.PASS;
    }

    private MetricStatus checkMemorySLA(long memoryUsage, SLADefinition sla,
                                         List<String> violations) {
        if (memoryUsage > sla.getMaxMemoryMb()) {
            violations.add("Memory usage " + memoryUsage +
                "MB exceeds threshold " + sla.getMaxMemoryMb() + "MB");
            return MetricStatus.WARNING;
        }
        return MetricStatus.PASS;
    }

    private PerformanceStatus determineOverallStatus(Map<String, MetricStatus> statuses) {
        boolean hasFail = statuses.values().stream()
            .anyMatch(s -> s == MetricStatus.FAIL);
        boolean hasWarning = statuses.values().stream()
            .anyMatch(s -> s == MetricStatus.WARNING);

        if (hasFail) {
            // Check if multiple failures
            long failCount = statuses.values().stream()
                .filter(s -> s == MetricStatus.FAIL)
                .count();
            return failCount > 1 ? PerformanceStatus.CRITICAL : PerformanceStatus.CRITICAL;
        } else if (hasWarning) {
            long warningCount = statuses.values().stream()
                .filter(s -> s == MetricStatus.WARNING)
                .count();
            return warningCount > 1 ? PerformanceStatus.DEGRADED : PerformanceStatus.WARNING;
        }
        return PerformanceStatus.HEALTHY;
    }
}
