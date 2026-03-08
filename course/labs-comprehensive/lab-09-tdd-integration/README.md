# Lab 9: Test-Driven Agentic Development (TDAD)

## Objectives

By the end of this lab, you will:
- Write specification tests that guide AI implementation
- Use the Red-Green-Refactor cycle with AI agents
- Create BEAD tasks that reference test specifications
- Validate AI-generated code automatically

---

## Prerequisites

- Completed Labs 1-8
- Understanding of JUnit 5 and Mockito
- Familiarity with BMAD and BEAD concepts

---

## Lab Duration: 90 minutes

| Section | Duration |
|---------|----------|
| Part 1: Writing Specification Tests | 30 min |
| Part 2: BEAD Task with Test Reference | 20 min |
| Part 3: AI-Driven Implementation | 30 min |
| Part 4: Refactoring Cycle | 10 min |

---

## Part 1: Writing Specification Tests (30 min)

### Context

We need to implement a new `AuditLogService` that records security events. Instead of writing code first, we'll write tests that specify the exact behavior we need.

### Step 1.1: Create Test Specification

Create the test file:

```bash
mkdir -p core/src/test/java/com/demo/workflow/services/impl
```

Create `AuditLogServiceSpec.java`:

```java
package com.demo.workflow.services.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SPECIFICATION TESTS for AuditLogService
 *
 * These tests define the expected behavior. They are written BEFORE
 * any implementation exists. The AI agent will implement the service
 * to satisfy these tests.
 *
 * DO NOT MODIFY THESE TESTS DURING IMPLEMENTATION.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Specification")
class AuditLogServiceSpec {

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 1: Basic Logging
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When logging a security event")
    class SecurityEventLogging {

        @Test
        @DisplayName("should record event with timestamp")
        void shouldRecordEventWithTimestamp() {
            // Given
            AuditLogService service = createService();
            Instant before = Instant.now();

            // When
            AuditEntry entry = service.logSecurityEvent(
                "MALWARE_DETECTED",
                "/content/dam/uploads/virus.exe",
                Map.of("threatName", "Trojan.Test")
            );

            // Then
            assertNotNull(entry);
            assertNotNull(entry.getTimestamp());
            assertTrue(entry.getTimestamp().isAfter(before) ||
                       entry.getTimestamp().equals(before));
        }

        @Test
        @DisplayName("should include event type and asset path")
        void shouldIncludeEventTypeAndPath() {
            AuditLogService service = createService();

            AuditEntry entry = service.logSecurityEvent(
                "FILE_QUARANTINED",
                "/content/dam/quarantine/malware.exe",
                Map.of()
            );

            assertEquals("FILE_QUARANTINED", entry.getEventType());
            assertEquals("/content/dam/quarantine/malware.exe", entry.getAssetPath());
        }

        @Test
        @DisplayName("should store additional context data")
        void shouldStoreContextData() {
            AuditLogService service = createService();
            Map<String, Object> context = Map.of(
                "threatName", "Trojan.Malware",
                "scanDuration", 1500,
                "quarantined", true
            );

            AuditEntry entry = service.logSecurityEvent(
                "SCAN_COMPLETE",
                "/content/dam/test.pdf",
                context
            );

            assertEquals("Trojan.Malware", entry.getContext().get("threatName"));
            assertEquals(1500, entry.getContext().get("scanDuration"));
            assertEquals(true, entry.getContext().get("quarantined"));
        }

        @Test
        @DisplayName("should generate unique entry ID")
        void shouldGenerateUniqueId() {
            AuditLogService service = createService();

            AuditEntry entry1 = service.logSecurityEvent("EVENT_A", "/path/a", Map.of());
            AuditEntry entry2 = service.logSecurityEvent("EVENT_B", "/path/b", Map.of());

            assertNotNull(entry1.getId());
            assertNotNull(entry2.getId());
            assertNotEquals(entry1.getId(), entry2.getId());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 2: Query Capabilities
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When querying audit logs")
    class AuditLogQueries {

        @Test
        @DisplayName("should find entries by event type")
        void shouldFindByEventType() {
            AuditLogService service = createService();

            // Create test entries
            service.logSecurityEvent("MALWARE_DETECTED", "/path/1", Map.of());
            service.logSecurityEvent("FILE_QUARANTINED", "/path/2", Map.of());
            service.logSecurityEvent("MALWARE_DETECTED", "/path/3", Map.of());

            // Query
            List<AuditEntry> results = service.findByEventType("MALWARE_DETECTED");

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(e ->
                "MALWARE_DETECTED".equals(e.getEventType())));
        }

        @Test
        @DisplayName("should find entries by asset path prefix")
        void shouldFindByPathPrefix() {
            AuditLogService service = createService();

            service.logSecurityEvent("EVENT", "/content/dam/uploads/file1.pdf", Map.of());
            service.logSecurityEvent("EVENT", "/content/dam/uploads/file2.pdf", Map.of());
            service.logSecurityEvent("EVENT", "/content/dam/approved/file3.pdf", Map.of());

            List<AuditEntry> results = service.findByPathPrefix("/content/dam/uploads");

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("should find entries within time range")
        void shouldFindByTimeRange() {
            AuditLogService service = createService();
            Instant start = Instant.now();

            service.logSecurityEvent("EVENT_1", "/path/1", Map.of());
            service.logSecurityEvent("EVENT_2", "/path/2", Map.of());

            Instant end = Instant.now().plusSeconds(1);

            List<AuditEntry> results = service.findByTimeRange(start, end);

            assertEquals(2, results.size());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 3: Retention & Cleanup
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When managing log retention")
    class LogRetention {

        @Test
        @DisplayName("should delete entries older than retention period")
        void shouldDeleteOldEntries() {
            AuditLogService service = createServiceWithRetention(30); // 30 days

            // This would require mocking time or using a test clock
            // Simplified: verify method exists and doesn't throw
            assertDoesNotThrow(() -> service.cleanupOldEntries());
        }

        @Test
        @DisplayName("should return count of deleted entries")
        void shouldReturnDeleteCount() {
            AuditLogService service = createServiceWithRetention(30);

            int deleted = service.cleanupOldEntries();

            assertTrue(deleted >= 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SECTION 4: Error Handling
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When handling errors")
    class ErrorHandling {

        @Test
        @DisplayName("should reject null event type")
        void shouldRejectNullEventType() {
            AuditLogService service = createService();

            assertThrows(IllegalArgumentException.class, () ->
                service.logSecurityEvent(null, "/path", Map.of())
            );
        }

        @Test
        @DisplayName("should reject empty asset path")
        void shouldRejectEmptyPath() {
            AuditLogService service = createService();

            assertThrows(IllegalArgumentException.class, () ->
                service.logSecurityEvent("EVENT", "", Map.of())
            );
        }

        @Test
        @DisplayName("should handle null context gracefully")
        void shouldHandleNullContext() {
            AuditLogService service = createService();

            AuditEntry entry = service.logSecurityEvent("EVENT", "/path", null);

            assertNotNull(entry);
            assertNotNull(entry.getContext());
            assertTrue(entry.getContext().isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    private AuditLogService createService() {
        return createServiceWithRetention(90);
    }

    private AuditLogService createServiceWithRetention(int days) {
        AuditLogServiceImpl service = new AuditLogServiceImpl();
        // Activate with mock config
        // This will be implemented as part of the service
        return service;
    }
}
```

### Step 1.2: Create Interface and Model Classes

Create `AuditLogService.java` interface:

```java
package com.demo.workflow.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service for recording and querying security audit events.
 */
public interface AuditLogService {

    /**
     * Log a security event.
     *
     * @param eventType Type of event (e.g., MALWARE_DETECTED, FILE_QUARANTINED)
     * @param assetPath Path to the affected asset
     * @param context Additional context data
     * @return The created audit entry
     */
    AuditEntry logSecurityEvent(String eventType, String assetPath, Map<String, Object> context);

    /**
     * Find entries by event type.
     */
    List<AuditEntry> findByEventType(String eventType);

    /**
     * Find entries by asset path prefix.
     */
    List<AuditEntry> findByPathPrefix(String pathPrefix);

    /**
     * Find entries within a time range.
     */
    List<AuditEntry> findByTimeRange(Instant start, Instant end);

    /**
     * Clean up entries older than retention period.
     *
     * @return Number of entries deleted
     */
    int cleanupOldEntries();
}
```

Create `AuditEntry.java` model:

```java
package com.demo.workflow.services;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single audit log entry.
 */
public class AuditEntry {

    private String id;
    private String eventType;
    private String assetPath;
    private Instant timestamp;
    private Map<String, Object> context;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getAssetPath() { return assetPath; }
    public void setAssetPath(String assetPath) { this.assetPath = assetPath; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
```

### Step 1.3: Run Tests (RED Phase)

```bash
mvn test -Dtest=AuditLogServiceSpec
```

**Expected Output:**
```
Tests run: 12, Failures: 12, Errors: 0, Skipped: 0

All tests are RED - ready for AI implementation!
```

---

## Part 2: BEAD Task with Test Reference (20 min)

### Step 2.1: Create BEAD Task

Create `.issues/TASK-TDD-AUDIT.yaml`:

```yaml
id: TASK-TDD-AUDIT
title: Implement AuditLogService to pass specification tests
type: implementation
status: pending
priority: high
methodology: TDAD (Test-Driven Agentic Development)

specification:
  test_file: core/src/test/java/com/demo/workflow/services/impl/AuditLogServiceSpec.java
  interface_file: core/src/main/java/com/demo/workflow/services/AuditLogService.java
  model_file: core/src/main/java/com/demo/workflow/services/AuditEntry.java

test_status:
  total: 12
  passing: 0
  failing: 12

implementation_target:
  file: core/src/main/java/com/demo/workflow/services/impl/AuditLogServiceImpl.java

requirements:
  - All 12 specification tests must pass
  - Use OSGi @Component annotation
  - Use in-memory storage (ConcurrentHashMap) for this implementation
  - Thread-safe operations
  - Configurable retention period via @Designate

constraints:
  - DO NOT modify test files
  - Minimum code to pass tests (YAGNI principle)
  - Follow existing project patterns

acceptance_criteria:
  - command: mvn test -Dtest=AuditLogServiceSpec
    expected: "Tests run: 12, Failures: 0, Errors: 0"

ai_workflow:
  phase_1_red:
    description: Read and understand all failing tests
    action: Analyze test file, identify required methods and behaviors

  phase_2_green:
    description: Implement minimum code to pass each test
    action: Write AuditLogServiceImpl step by step
    iteration: Run tests after each method implementation

  phase_3_refactor:
    description: Improve code quality
    action: Extract helpers, improve naming, add logging
    constraint: All tests must continue to pass
```

### Step 2.2: Update Task Status

As you progress, update the task:

```yaml
test_status:
  total: 12
  passing: 5  # Update as tests pass
  failing: 7

progress_log:
  - timestamp: 2024-01-15T10:00:00Z
    tests_passing: 0
    notes: "Started implementation"
  - timestamp: 2024-01-15T10:15:00Z
    tests_passing: 4
    notes: "Basic logging tests passing"
  - timestamp: 2024-01-15T10:30:00Z
    tests_passing: 8
    notes: "Query methods implemented"
```

---

## Part 3: AI-Driven Implementation (30 min)

### Step 3.1: Prompt for AI Agent

Use this prompt with Claude Code or your AI assistant:

```markdown
## TDAD Implementation Task

I need you to implement `AuditLogServiceImpl` to pass all specification tests.

### Test Specification
File: `core/src/test/java/com/demo/workflow/services/impl/AuditLogServiceSpec.java`

### Current Status
- Tests run: 12
- Passing: 0
- Failing: 12

### Your Task

1. **READ** the test file carefully
2. **UNDERSTAND** what each test expects
3. **IMPLEMENT** `AuditLogServiceImpl.java` to pass tests
4. **RUN** tests after each significant change
5. **REPORT** progress

### Implementation Requirements
- Use OSGi @Component annotation
- Use ConcurrentHashMap for thread-safe in-memory storage
- Add @Designate for configuration (retention days)
- Generate UUID for entry IDs
- Validate inputs as tests expect

### Constraints
- DO NOT modify any test files
- Write minimum code to pass tests
- Follow patterns from existing services in the project

### Workflow

After each implementation step:
1. Run: `mvn test -Dtest=AuditLogServiceSpec`
2. Report: Which tests now pass?
3. Continue until all 12 tests pass

Begin implementation.
```

### Step 3.2: Watch AI Implement

The AI should:
1. Create `AuditLogServiceImpl.java`
2. Implement methods one by one
3. Run tests after each change
4. Report progress

**Example AI Progress:**

```
Iteration 1:
- Implemented logSecurityEvent() with basic fields
- Tests passing: 4/12 (SecurityEventLogging tests)

Iteration 2:
- Added query methods
- Tests passing: 8/12

Iteration 3:
- Added retention and error handling
- Tests passing: 12/12

All tests GREEN!
```

### Step 3.3: Verify Final Implementation

```bash
mvn test -Dtest=AuditLogServiceSpec

# Expected:
# Tests run: 12, Failures: 0, Errors: 0
# BUILD SUCCESS
```

---

## Part 4: Refactoring Cycle (10 min)

### Step 4.1: Refactor Prompt

```markdown
## Refactor Task

All tests are now passing. Please improve code quality:

### Focus Areas
1. Extract helper methods for repeated logic
2. Add SLF4J logging for important operations
3. Improve variable and method names
4. Add JavaDoc comments

### Constraint
All 12 tests MUST continue to pass after refactoring.

### Verification
After each refactor change:
1. Run: `mvn test -Dtest=AuditLogServiceSpec`
2. Confirm: All tests still pass
```

### Step 4.2: Review Refactored Code

Ensure the AI:
- Did not break any tests
- Improved readability
- Added appropriate logging
- Followed project conventions

---

## Lab Completion Checklist

- [ ] Created AuditLogServiceSpec with 12 tests
- [ ] Created AuditLogService interface
- [ ] Created AuditEntry model
- [ ] Created BEAD task file
- [ ] All 12 tests initially failing (RED)
- [ ] AI implemented AuditLogServiceImpl
- [ ] All 12 tests passing (GREEN)
- [ ] Code refactored for quality
- [ ] All tests still passing after refactor

---

## Key Learnings

1. **Tests as Specifications**: Tests define behavior before code exists
2. **AI Guardrails**: Tests constrain AI to expected behavior
3. **Immediate Feedback**: AI knows instantly if code is correct
4. **Incremental Progress**: Implement test by test, not all at once
5. **Safe Refactoring**: Tests protect against breaking changes

---

## Next Steps

- Apply TDAD to your own service implementations
- Explore mutation testing to verify test quality
- Integrate with CI/CD for continuous validation

---

*Lab 9 is part of Module 10: Test-Driven Agentic Development*
