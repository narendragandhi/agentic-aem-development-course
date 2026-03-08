# Lab 21: Performance Profiling with AI
# Comprehensive Lab - 4 hours

## Objective

Implement performance profiling for AEM workflows using AI-assisted analysis. Use TDD to establish performance baselines and detect regressions.

---

## Prerequisites

- Lab 15 (Code Quality) completed
- Lab 19 (Functional/Regression) helpful

---

## BMAD Phase Context

```
Phase 05 (Testing) - Performance Testing Integration
├── AI generates performance test scenarios
├── TDD ensures performance baselines
├── Goose automates profiling runs
└── GasTown triggers profiling on changes
```

---

## Part 1: Performance Test Specification (TDD - RED) (30 min)

### 1.1 Write Performance Specs First

Create `core/src/test/java/com/demo/workflow/performance/PerformanceSpecs.java`:

```java
package com.demo.workflow.performance;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Performance Specifications - Must Pass")
class PerformanceSpecs {

    private static final long SCAN_SLA_MS = 5000;      // 5 seconds max
    private static final long WORKFLOW_INIT_MS = 2000;  // 2 seconds max
    private static final long QUERY_TIME_MS = 1000;     // 1 second max
    
    @Nested
    @DisplayName("AntivirusScanService")
    class AntivirusScanSpecs {
        
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 100})
        @DisplayName("Scan must complete within SLA for file sizes")
        void scanCompletesWithinSLA(int fileSizeMb) {
            long start = System.currentTimeMillis();
            
            // This will fail until implementation exists
            ScanResult result = scanService.scan(createTestFile(fileSizeMb));
            
            long duration = System.currentTimeMillis() - start;
            
            assertTrue(duration < SCAN_SLA_MS, 
                String.format("Scan of %dMB took %dms, exceeds SLA of %dms",
                    fileSizeMs, duration, SCAN_SLA_MS));
        }
        
        @Test
        @DisplayName("100 concurrent scans must complete within 30 seconds")
        void concurrentScanPerformance() {
            long start = System.currentTimeMillis();
            
            List<ScanResult> results = runConcurrent(100, () -> 
                scanService.scan(createTestFile(1)));
            
            long duration = System.currentTimeMillis() - start;
            
            assertTrue(duration < 30000,
                String.format("100 concurrent scans took %dms, exceeds 30s", duration));
        }
    }
    
    @Nested
    @DisplayName("WorkflowService")
    class WorkflowSpecs {
        
        @Test
        @DisplayName("Workflow initiation must complete within SLA")
        void workflowInitWithinSLA() {
            long start = System.currentTimeMillis();
            
            String workflowId = workflowService.initiateWorkflow(assetPath);
            
            long duration = System.currentTimeMillis() - start;
            
            assertTrue(duration < WORKFLOW_INIT_MS,
                String.format("Workflow init took %dms, exceeds SLA of %dms",
                    duration, WORKFLOW_INIT_MS));
        }
    }
    
    @Nested
    @DisplayName("Query Performance")
    class QuerySpecs {
        
        @Test
        @DisplayName("Asset queries must complete within SLA")
        void queryWithinSLA() {
            long start = System.currentTimeMillis();
            
            List<Asset> assets = assetService.findAssets(
                QueryBuilder.create()
                    .path("/content/dam")
                    .type("dam:Asset")
                    .build());
            
            long duration = System.currentTimeMillis() - start;
            
            assertTrue(duration < QUERY_TIME_MS,
                String.format("Query took %dms, exceeds SLA of %dms",
                    duration, QUERY_TIME_MS));
        }
    }
}
```

### 1.2 Run Tests (They Will Fail)

```bash
mvn test -Dtest=PerformanceSpecs

# Expected: FAILURES - Performance SLAs not met
# This is the RED phase - we have the specification, now implement
```

---

## Part 2: Performance Service Implementation (TDD - GREEN) (45 min)

### 2.1 AI Generate Performance Service

Use Goose or Claude to generate:

```bash
goose run --task "Implement PerformanceMonitoringService that:
- Tracks method execution times
- Stores performance metrics
- Provides SLA checking
- Emits events for slow operations
- Thread-safe for concurrent access
- Follows OSGi service patterns"
```

### 2.2 Performance Service Interface

```java
package com.demo.workflow.performance;

public interface PerformanceMonitoringService {
    
    /**
     * Record execution time for an operation.
     */
    void recordExecution(String operation, long durationMs);
    
    /**
     * Check if operation meets SLA.
     */
    boolean isWithinSLA(String operation, long durationMs);
    
    /**
     * Get performance metrics for an operation.
     */
    PerformanceMetrics getMetrics(String operation);
    
    /**
     * Get all metrics summary.
     */
    Map<String, PerformanceMetrics> getAllMetrics();
    
    /**
     * Reset metrics.
     */
    void reset();
}
```

### 2.3 Metrics Model

```java
@Model(adaptables = Resource.class)
public class PerformanceMetrics {
    
    private String operation;
    private long count;
    private long totalDurationMs;
    private long minDurationMs;
    private long maxDurationMs;
    private double avgDurationMs;
    private long p50DurationMs;
    private long p95DurationMs;
    private long p99DurationMs;
    
    // Getters and calculation methods
    public void record(long durationMs) {
        count++;
        totalDurationMs += durationMs;
        minDurationMs = Math.min(minDurationMs, durationMs);
        maxDurationMs = Math.max(maxDurationMs, durationMs);
        avgDurationMs = (double) totalDurationMs / count;
    }
}
```

---

## Part 3: AI-Assisted Profiling (30 min)

### 3.1 AI Profiling Analysis

Create `scripts/ai-profile.sh`:

```bash
#!/bin/bash
# AI-Assisted Performance Profiling

echo "=== AI Performance Profiling ==="

# 1. Profile with JProfiler
echo "Running JProfiler analysis..."
jprofiler -profile "CPU,Memory" \
  -export snapshot.json \
  -- $(which java) -jar app.jar

# 2. Generate heap dump
echo "Generating heap dump..."
jmap -dump:format=b,file=heap.hprof $(pgrep java)

# 3. AI Analysis
echo "Analyzing with AI..."
cat snapshot.json | ai-analyze --pattern "slow-methods" --suggest
```

### 3.2 AI Analysis Prompt

```bash
# Use Goose to analyze profiling results
goose run --task "Analyze the JProfiler snapshot and:
1. Identify top 10 slowest methods
2. Find memory allocation hotspots
3. Detect potential leaks
4. Suggest optimization opportunities
5. Create fix tickets in BEAD"

--context "files: [snapshot.json, heap.hprof]"
```

### 3.3 Common Optimizations AI Suggests

| Issue | AI Suggestion |
|-------|---------------|
| Slow queries | Add Oak index |
| High GC | Reduce object allocation |
| Thread contention | Use concurrent structures |
| N+1 queries | Batch load resources |
| Cache misses | Implement caching layer |

---

## Part 4: Performance Regression Tests (30 min)

### 4.1 Baseline Management

```java
@Tag("performance-regression")
class PerformanceRegressionTest {
    
    private static Map<String, Long> BASELINES = Map.of(
        "AntivirusScanService.scan.1mb", 500L,
        "AntivirusScanService.scan.10mb", 3000L,
        "WorkflowService.initiate", 1000L,
        "QueryService.findAssets", 500L
    );
    
    @Test
    void scanPerformanceRegression() {
        // Run scan
        long duration = measure(() -> 
            service.scan(testFile1mb));
        
        // Compare with baseline
        long baseline = BASELINES.get("AntivirusScanService.scan.1mb");
        
        assertTrue(duration < baseline * 1.1, 
            String.format("Regression: %dms > baseline %dms", 
                duration, baseline));
    }
}
```

### 4.2 CI Integration

```yaml
# .github/workflows/performance.yml
- name: Performance Tests
  run: |
    mvn test -Dtest=PerformanceRegressionTest \
      -Dbaseline.file=baselines/perf-baselines.json
      
- name: Update Baselines
  if: github.event_name == 'schedule'
  run: |
    mvn test -Dtest=PerformanceRegressionTest \
      -Dupdate.baselines=true
```

---

## Part 5: Caching Implementation (30 min)

### 5.1 Cache Service

```java
@Component
@Service(CacheService.class)
public class CacheServiceImpl implements CacheService {
    
    private Cache<String, CacheEntry> cache;
    
    @Activate
    void activate(CacheConfig config) {
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(config.maxSize())
            .expireAfterWrite(config.ttlMinutes(), TimeUnit.MINUTES)
            .build();
    }
    
    @Override
    public Optional<Object> get(String key) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.getValue());
        }
        return Optional.empty();
    }
    
    @Override
    public void put(String key, Object value) {
        cache.put(key, new CacheEntry(value, 
            System.currentTimeMillis()));
    }
}
```

### 5.2 Use in Workflow Service

```java
@Service
public class AssetQueryServiceImpl implements AssetQueryService {
    
    @Reference
    private CacheService cacheService;
    
    @Override
    public List<Asset> findAssets(Query query) {
        String cacheKey = generateCacheKey(query);
        
        // Try cache first
        Optional<List<Asset>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // Query and cache
        List<Asset> results = repository.find(query);
        cacheService.put(cacheKey, results);
        
        return results;
    }
}
```

---

## Part 6: Load Testing (30 min)

### 6.1 JMeter Test Plan

```xml
<!-- Load test for workflow -->
<testPlan>
  <hashTree>
    <ThreadGroup>
      <numThreads>50</numThreads>
      <rampTime>30</rampTime>
      <duration>300</duration>
    </ThreadGroup>
    <hashTree>
      <HTTPSamplerProxy>
        <url>/api/workflow/start</url>
        <method>POST</method>
        <postBody>
          {"assetPath": "/content/dam/test.pdf"}
        </postBody>
      </HTTPSamplerProxy>
    </hashTree>
  </hashTree>
</testPlan>
```

### 6.2 AI Load Test Generation

```bash
# Use Goose to generate load tests
goose run --task "Generate JMeter test plan for:
1. Concurrent asset uploads (10-100 users)
2. Workflow initiation
3. Asset queries
4. Report results with pass/fail"
```

---

## Verification Checklist

- [ ] Performance specs written (TDD RED)
- [ ] Monitoring service implemented (TDD GREEN)
- [ ] AI profiling analysis works
- [ ] Regression tests configured
- [ ] Caching implemented
- [ ] Load tests created

---

## Key Takeaways

1. **TDD for performance** - Write specs first, fail, then optimize
2. **AI accelerates profiling** - Let AI analyze dumps
3. **Baselines prevent regression** - Track over time
4. **Caching wins** - Often the best optimization

---

## BMAD Integration

| Phase | Activity |
|-------|----------|
| 01 | Define performance NFRs |
| 02 | Model performance requirements |
| 03 | Architecture for performance |
| 04 | Implement with monitoring |
| 05 | Performance testing & regression |
| 06 | Monitor in production |

---

## Next Steps

1. Add to GasTown quality gates
2. Set up performance alerting
3. Create performance dashboard
4. Integrate with SonarQube

---

## References

- [AEM Performance Optimization](https://experienceleague.adobe.com/docs/experience-manager-65/deploying/performance/performance-guidelines.html)
- [Oak Performance](https://experienceleague.adobe.com/docs/experience-manager-65/deploying/performance/performance-tuning.html)
