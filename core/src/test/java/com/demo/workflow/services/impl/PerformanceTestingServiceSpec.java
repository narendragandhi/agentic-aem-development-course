package com.demo.workflow.services.impl;

import com.demo.workflow.services.PerformanceTestingService;
import com.demo.workflow.services.PerformanceTestingService.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Specification Tests for PerformanceTestingService.
 *
 * Tests: 15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceTestingService Specification")
class PerformanceTestingServiceSpec {

    private PerformanceTestingService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceTestingServiceImpl();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 1: Timing Measurement (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When measuring timing")
    class TimingMeasurement {

        @Test
        @DisplayName("should measure execution time accurately")
        void shouldMeasureExecutionTimeAccurately() {
            Runnable operation = () -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            TimingResult result = service.measureExecution(operation, "sleep50ms");

            assertTrue(result.getExecutionTimeMs() >= 50);
            assertTrue(result.getExecutionTimeMs() < 150); // Allow some variance
            assertEquals("sleep50ms", result.getOperationName());
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("should track min/max/avg times")
        void shouldTrackMinMaxAvgTimes() {
            AtomicInteger counter = new AtomicInteger(0);
            PerformanceTestConfig config = new PerformanceTestConfig(
                "variableTest", 5, 0, 1);

            PerformanceResult result = service.runTest(config);

            assertNotNull(result.getTiming());
            assertTrue(result.getTiming().getMinTimeMs() <= result.getTiming().getAvgTimeMs());
            assertTrue(result.getTiming().getAvgTimeMs() <= result.getTiming().getMaxTimeMs());
        }

        @Test
        @DisplayName("should handle operation exceptions")
        void shouldHandleOperationExceptions() {
            Runnable failingOperation = () -> {
                throw new RuntimeException("Intentional failure");
            };

            TimingResult result = service.measureExecution(failingOperation, "failingOp");

            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorMessage());
            assertTrue(result.getErrorMessage().contains("Intentional failure"));
        }

        @Test
        @DisplayName("should support named operations")
        void shouldSupportNamedOperations() {
            Runnable operation = () -> {};

            TimingResult result = service.measureExecution(operation, "customOperationName");

            assertEquals("customOperationName", result.getOperationName());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 2: Resource Monitoring (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When monitoring resources")
    class ResourceMonitoring {

        @Test
        @DisplayName("should capture memory usage")
        void shouldCaptureMemoryUsage() {
            ResourceSnapshot snapshot = service.getResourceSnapshot();

            assertTrue(snapshot.getUsedMemoryMb() > 0);
            assertTrue(snapshot.getMaxMemoryMb() > 0);
            assertTrue(snapshot.getUsedMemoryMb() <= snapshot.getMaxMemoryMb());
        }

        @Test
        @DisplayName("should track thread count")
        void shouldTrackThreadCount() {
            ResourceSnapshot snapshot = service.getResourceSnapshot();

            assertTrue(snapshot.getThreadCount() > 0);
        }

        @Test
        @DisplayName("should calculate memory usage percent")
        void shouldCalculateMemoryUsagePercent() {
            ResourceSnapshot snapshot = service.getResourceSnapshot();

            double usagePercent = snapshot.getMemoryUsagePercent();
            assertTrue(usagePercent >= 0);
            assertTrue(usagePercent <= 100);
        }

        @Test
        @DisplayName("should have timestamp")
        void shouldHaveTimestamp() {
            ResourceSnapshot snapshot = service.getResourceSnapshot();

            assertNotNull(snapshot.getTimestamp());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 3: SLA Verification (4 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When checking SLA")
    class SLAVerification {

        @Test
        @DisplayName("should pass when metrics within target")
        void shouldPassWhenWithinTarget() {
            // Create result with good performance
            TimingResult timing = new TimingResult("test", 100, true, null);
            ResourceSnapshot resources = new ResourceSnapshot(256, 1024, 10, 25.0);
            PerformanceResult result = new PerformanceResult(
                "goodTest", timing, resources, 100, 0);

            // Define lenient SLA
            SLADefinition sla = new SLADefinition(200, 500, 1000, 5.0, 512);

            SLACheckResult check = service.checkSLA(result, sla);

            assertEquals(PerformanceStatus.HEALTHY, check.getStatus());
            assertTrue(check.isPassing());
            assertTrue(check.getViolations().isEmpty());
        }

        @Test
        @DisplayName("should warn when above warning threshold")
        void shouldWarnWhenAboveWarningThreshold() {
            // Create result with warning-level performance
            TimingResult timing = new TimingResult("test", 600, true, null);
            ResourceSnapshot resources = new ResourceSnapshot(256, 1024, 10, 25.0);
            PerformanceResult result = new PerformanceResult(
                "warningTest", timing, resources, 100, 0);

            // Define SLA where 600ms exceeds warning (500ms) but not critical (1000ms)
            SLADefinition sla = new SLADefinition(200, 500, 1000, 5.0, 512);

            SLACheckResult check = service.checkSLA(result, sla);

            assertEquals(PerformanceStatus.WARNING, check.getStatus());
            assertTrue(check.isPassing()); // Warning still passes
        }

        @Test
        @DisplayName("should fail when above critical threshold")
        void shouldFailWhenAboveCriticalThreshold() {
            // Create result with critical-level performance
            TimingResult timing = new TimingResult("test", 1500, true, null);
            ResourceSnapshot resources = new ResourceSnapshot(256, 1024, 10, 25.0);
            PerformanceResult result = new PerformanceResult(
                "criticalTest", timing, resources, 90, 10);

            // Define SLA where 1500ms exceeds critical (1000ms)
            SLADefinition sla = new SLADefinition(200, 500, 1000, 5.0, 512);

            SLACheckResult check = service.checkSLA(result, sla);

            assertEquals(PerformanceStatus.CRITICAL, check.getStatus());
            assertFalse(check.isPassing());
            assertFalse(check.getViolations().isEmpty());
        }

        @Test
        @DisplayName("should aggregate multiple metric checks")
        void shouldAggregateMultipleMetricChecks() {
            // Create result with multiple warning-level metrics
            TimingResult timing = new TimingResult("test", 600, true, null);
            ResourceSnapshot resources = new ResourceSnapshot(600, 1024, 10, 25.0);
            PerformanceResult result = new PerformanceResult(
                "multiWarning", timing, resources, 97, 3);

            // Define SLA
            SLADefinition sla = new SLADefinition(200, 500, 1000, 5.0, 512);

            SLACheckResult check = service.checkSLA(result, sla);

            assertNotNull(check.getMetricStatuses());
            assertFalse(check.getMetricStatuses().isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 4: Baseline Comparison (3 tests)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When comparing to baseline")
    class BaselineComparison {

        @Test
        @DisplayName("should detect performance improvement")
        void shouldDetectPerformanceImprovement() {
            // Current: faster than baseline
            TimingResult currentTiming = new TimingResult("test", 100, true, null);
            ResourceSnapshot currentResources = new ResourceSnapshot(256, 1024, 10, 25.0);
            PerformanceResult current = new PerformanceResult(
                "test", currentTiming, currentResources, 100, 0);

            // Baseline: slower
            TimingResult baselineTiming = new TimingResult("test", 200, true, null);
            ResourceSnapshot baselineResources = new ResourceSnapshot(512, 1024, 10, 25.0);
            PerformanceResult baseline = new PerformanceResult(
                "test", baselineTiming, baselineResources, 95, 5);

            ComparisonResult comparison = service.compareToBaseline(current, baseline);

            assertEquals(ComparisonOutcome.IMPROVED, comparison.getOutcome());
            assertTrue(comparison.isImproved());
            assertTrue(comparison.getResponseTimeChangePercent() < 0); // Negative = faster
        }

        @Test
        @DisplayName("should detect performance degradation")
        void shouldDetectPerformanceDegradation() {
            // Current: slower than baseline
            TimingResult currentTiming = new TimingResult("test", 400, true, null);
            ResourceSnapshot currentResources = new ResourceSnapshot(768, 1024, 10, 25.0);
            PerformanceResult current = new PerformanceResult(
                "test", currentTiming, currentResources, 90, 10);

            // Baseline: faster
            TimingResult baselineTiming = new TimingResult("test", 100, true, null);
            ResourceSnapshot baselineResources = new ResourceSnapshot(256, 1024, 10, 25.0);
            PerformanceResult baseline = new PerformanceResult(
                "test", baselineTiming, baselineResources, 100, 0);

            ComparisonResult comparison = service.compareToBaseline(current, baseline);

            assertEquals(ComparisonOutcome.DEGRADED, comparison.getOutcome());
            assertTrue(comparison.isDegraded());
            assertTrue(comparison.getResponseTimeChangePercent() > 0); // Positive = slower
        }

        @Test
        @DisplayName("should handle no baseline gracefully")
        void shouldHandleNoBaselineGracefully() {
            TimingResult currentTiming = new TimingResult("test", 100, true, null);
            ResourceSnapshot currentResources = new ResourceSnapshot(256, 1024, 10, 25.0);
            PerformanceResult current = new PerformanceResult(
                "test", currentTiming, currentResources, 100, 0);

            ComparisonResult comparison = service.compareToBaseline(current, null);

            assertEquals(ComparisonOutcome.NO_BASELINE, comparison.getOutcome());
        }
    }
}
