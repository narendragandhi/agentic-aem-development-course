# Module 10: Test-Driven Agentic Development (TDAD)

## Overview

This module integrates Test-Driven Development (TDD) with BMAD methodology, creating a hybrid approach called **Test-Driven Agentic Development (TDAD)**. Tests become specifications that guide AI agents, providing deterministic validation while maintaining agentic flexibility.

---

## Architecture: BMAD + TDD Integration

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TEST-DRIVEN AGENTIC DEVELOPMENT (TDAD)                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   BMAD Phase                    TDD Integration                             │
│   ──────────────────────────────────────────────────────────────────────   │
│                                                                             │
│   ┌─────────────┐    ┌─────────────────────────────────────────────────┐  │
│   │ Phase 00    │───▶│ Define Test Strategy & Coverage Goals           │  │
│   │ Initialize  │    │ - Unit test targets (80%+)                      │  │
│   └─────────────┘    │ - Integration test scope                        │  │
│                      └─────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────┐    ┌─────────────────────────────────────────────────┐  │
│   │ Phase 01    │───▶│ Write Acceptance Tests from User Stories        │  │
│   │ Discovery   │    │ - BDD-style scenarios (Given/When/Then)         │  │
│   └─────────────┘    │ - Business rule validation tests                │  │
│                      └─────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────┐    ┌─────────────────────────────────────────────────┐  │
│   │ Phase 02    │───▶│ Create Contract Tests for Interfaces            │  │
│   │ Models      │    │ - API contracts (OpenAPI/AsyncAPI)              │  │
│   └─────────────┘    │ - Service interface tests                       │  │
│                      └─────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────┐    ┌─────────────────────────────────────────────────┐  │
│   │ Phase 03    │───▶│ Write Integration Tests for Architecture        │  │
│   │ Architecture│    │ - Component interaction tests                   │  │
│   └─────────────┘    │ - OSGi service binding tests                    │  │
│                      └─────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────┐    ┌─────────────────────────────────────────────────┐  │
│   │ Phase 04    │───▶│ RED → GREEN → REFACTOR Cycle with AI            │  │
│   │ Development │    │ - AI writes code to pass tests                  │  │
│   └─────────────┘    │ - Human reviews, AI refactors                   │  │
│                      └─────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────┐    ┌─────────────────────────────────────────────────┐  │
│   │ Phase 05    │───▶│ Mutation Testing & Coverage Analysis            │  │
│   │ Testing     │    │ - AI generates edge case tests                  │  │
│   └─────────────┘    │ - Verify test quality with mutation testing     │  │
│                      └─────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Why TDD + Agentic Development?

### The Problem with Pure Agentic Development
- AI can generate code that "looks right" but has subtle bugs
- Validation depends on human review
- No automated safety net for regressions
- Difficult to measure progress objectively

### The Problem with Pure TDD
- Writing tests first is time-consuming
- Test coverage doesn't guarantee correctness
- Hard to anticipate all edge cases
- Slower iteration cycles

### TDAD: The Best of Both Worlds

| Aspect | Pure TDD | Pure Agentic | TDAD Hybrid |
|--------|----------|--------------|-------------|
| **Specification** | Tests as specs | PRD as specs | Both |
| **Validation** | Automated | Manual review | Automated + Review |
| **Speed** | Slower | Faster | Optimized |
| **Reliability** | High | Variable | High |
| **AI Guidance** | Limited | PRD-based | Test + PRD |

---

## TDAD Workflow

### Step 1: Tests as AI Prompts

Tests become natural language specifications that guide AI behavior:

```java
// This test IS the prompt for the AI agent
@Test
@DisplayName("Should quarantine infected files to secure folder")
void shouldQuarantineInfectedFiles() {
    // Given an asset infected with malware
    Asset infectedAsset = createAsset("/content/dam/uploads/virus.exe");
    ScanResult result = ScanResult.infected("Trojan.Malware");

    // When the quarantine process runs
    quarantineService.processInfectedAsset(infectedAsset, result);

    // Then the asset should be moved to quarantine
    assertThat(assetExists("/content/dam/quarantine/virus.exe")).isTrue();
    assertThat(assetExists("/content/dam/uploads/virus.exe")).isFalse();

    // And metadata should be preserved
    Asset quarantined = getAsset("/content/dam/quarantine/virus.exe");
    assertThat(quarantined.getMetadata("quarantine.originalPath"))
        .isEqualTo("/content/dam/uploads/virus.exe");
    assertThat(quarantined.getMetadata("quarantine.threatName"))
        .isEqualTo("Trojan.Malware");
}
```

**AI Prompt Translation:**
> "Implement a quarantine service that moves infected assets to /content/dam/quarantine,
> deletes the original, and preserves the original path and threat name in metadata."

### Step 2: Red Phase - Write Failing Tests

```bash
# Run tests - they should fail (RED)
mvn test -Dtest=QuarantineServiceTest

# Output:
# Tests run: 5, Failures: 5, Errors: 0
# All tests failing as expected - proceed to GREEN phase
```

### Step 3: Green Phase - AI Implements

```markdown
## BEAD Task: Implement QuarantineService

**Context**: The following tests define the required behavior.
**Goal**: Write the minimum code to make all tests pass.
**Constraints**:
- Follow existing code patterns in the project
- Use OSGi @Component annotations
- Do not add functionality beyond what tests require

[Paste failing test output here]
```

### Step 4: Refactor Phase - AI Improves

```markdown
## BEAD Task: Refactor QuarantineService

**Context**: All tests are now passing.
**Goal**: Improve code quality without breaking tests.
**Focus Areas**:
- Extract reusable methods
- Improve error handling
- Add logging
- Optimize performance

**Constraint**: All existing tests must continue to pass.
```

---

## Practical Lab: TDD for AntivirusScanService

### Lab 10.1: Write Tests First

Create acceptance tests before any implementation:

```java
package com.demo.workflow.services.impl;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TDD Specification for AntivirusScanService
 *
 * These tests define the expected behavior. The AI agent
 * will implement the service to satisfy these tests.
 */
@DisplayName("AntivirusScanService Specification")
class AntivirusScanServiceSpec {

    // ═══════════════════════════════════════════════════════════════
    // SCAN BEHAVIOR
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning a clean file")
    class CleanFileScanning {

        @Test
        @DisplayName("should return CLEAN status")
        void shouldReturnCleanStatus() {
            // Given a clean PDF file
            InputStream cleanFile = getCleanPdfStream();

            // When scanned
            ScanResult result = service.scan(cleanFile, "document.pdf");

            // Then status should be CLEAN
            assertThat(result.getStatus()).isEqualTo(ScanStatus.CLEAN);
            assertThat(result.getThreatName()).isNull();
        }

        @Test
        @DisplayName("should complete within timeout")
        void shouldCompleteWithinTimeout() {
            InputStream cleanFile = getCleanPdfStream();

            assertTimeout(Duration.ofSeconds(30), () -> {
                service.scan(cleanFile, "document.pdf");
            });
        }
    }

    @Nested
    @DisplayName("When scanning an infected file")
    class InfectedFileScanning {

        @Test
        @DisplayName("should return INFECTED status with threat name")
        void shouldReturnInfectedStatus() {
            // Given the EICAR test file (standard AV test pattern)
            InputStream eicarFile = getEicarTestStream();

            // When scanned
            ScanResult result = service.scan(eicarFile, "eicar.com");

            // Then status should be INFECTED
            assertThat(result.getStatus()).isEqualTo(ScanStatus.INFECTED);
            assertThat(result.getThreatName()).isNotBlank();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ERROR HANDLING
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanner is unavailable")
    class ScannerUnavailable {

        @Test
        @DisplayName("should return ERROR status after retries")
        void shouldReturnErrorAfterRetries() {
            // Given ClamAV is not running
            stopClamAV();

            // When scan is attempted
            ScanResult result = service.scan(anyFile(), "test.pdf");

            // Then status should be ERROR
            assertThat(result.getStatus()).isEqualTo(ScanStatus.ERROR);
            assertThat(result.getErrorMessage()).contains("connection");
        }

        @Test
        @DisplayName("should attempt retry with exponential backoff")
        void shouldRetryWithBackoff() {
            // Given ClamAV fails intermittently
            configureClamAVToFailThenSucceed(2);

            // When scan is attempted
            ScanResult result = service.scan(anyFile(), "test.pdf");

            // Then should succeed after retries
            assertThat(result.getStatus()).isEqualTo(ScanStatus.CLEAN);
            assertThat(getRetryCount()).isEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FILE SIZE LIMITS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When file exceeds size limit")
    class FileSizeLimits {

        @Test
        @DisplayName("should skip scanning and return SKIPPED status")
        void shouldSkipLargeFiles() {
            // Given a file larger than configured limit
            InputStream largeFile = createLargeFile(500 * 1024 * 1024); // 500MB

            // When scan is attempted
            ScanResult result = service.scan(largeFile, "large-video.mp4");

            // Then should skip without error
            assertThat(result.getStatus()).isEqualTo(ScanStatus.SKIPPED);
            assertThat(result.getSkipReason()).contains("size limit");
        }
    }
}
```

### Lab 10.2: BEAD Task for AI Implementation

```yaml
# .issues/TASK-TDD-001.yaml
id: TASK-TDD-001
title: Implement AntivirusScanService to pass specification tests
type: implementation
status: in_progress
priority: high

context:
  specification_tests: core/src/test/java/.../AntivirusScanServiceSpec.java
  test_count: 7
  current_passing: 0

requirements:
  - All tests in AntivirusScanServiceSpec must pass
  - Use ClamAV INSTREAM protocol
  - Implement retry with exponential backoff
  - Respect file size limits from configuration

constraints:
  - Do not modify the test file
  - Follow existing OSGi patterns in the project
  - Minimum code to pass tests (YAGNI)

acceptance_criteria:
  - mvn test -Dtest=AntivirusScanServiceSpec passes with 0 failures
  - Code coverage > 80%
  - No new warnings or errors

ai_instructions: |
  1. Read the specification tests carefully
  2. Understand each test's requirements
  3. Implement the minimum code to pass each test
  4. Run tests after each implementation step
  5. Refactor only after all tests pass
```

### Lab 10.3: AI-Driven Red-Green-Refactor

**Prompt for AI Agent:**

```markdown
## Task: Implement AntivirusScanService using TDD

I have written specification tests that define the expected behavior.
Your task is to implement the service to make all tests pass.

### Current Test Status
```
Tests run: 7, Failures: 7, Errors: 0
```

### Specification Tests Location
`core/src/test/java/com/demo/workflow/services/impl/AntivirusScanServiceSpec.java`

### Instructions

1. **RED Phase**: Read and understand each failing test
2. **GREEN Phase**: Implement minimum code to pass tests
3. **REFACTOR Phase**: Improve code quality

### Constraints
- Do NOT modify the tests
- Follow existing project patterns
- Use OSGi @Component, @Reference, @Designate annotations
- Implement ClamAV INSTREAM protocol

### Workflow
After each change:
1. Run: `mvn test -Dtest=AntivirusScanServiceSpec`
2. Report: Which tests now pass?
3. Continue: Until all 7 tests pass

Begin implementation.
```

---

## Comparison: Traditional vs TDAD

### Traditional BMAD Flow
```
PRD → Architecture → Code → Manual Test → Review → Deploy
           ↑                    ↓
           └────── Bugs ────────┘
```

### TDAD Flow
```
PRD → Tests → Architecture → AI Code → Auto Test → Review → Deploy
                                ↑          ↓
                                └── FAIL ──┘ (immediate feedback)
```

### Benefits of TDAD

| Benefit | Description |
|---------|-------------|
| **Immediate Feedback** | AI knows instantly if code is correct |
| **Reduced Review Burden** | Tests validate before human review |
| **Documentation** | Tests document expected behavior |
| **Regression Safety** | Future changes are validated automatically |
| **AI Guardrails** | Tests constrain AI to expected behavior |

---

## Similar Frameworks

### 1. TDFlow (Test-Driven Agentic Workflow)
- Research framework from arXiv
- Multiple LLM sub-agents working to pass tests
- Repository-scale test resolution

### 2. Domain-Driven TDD for AI
- Combines DDD with TDD for AI agents
- Scenario-based testing with business language
- Tools: LangWatch Scenario framework

### 3. Burr Framework
- Python state machine library
- Built-in pytest integration
- Trace recording and replay for testing

### 4. Agentic Coding Handbook TDD Pattern
- Tests as natural language prompts
- "Write test, AI implements" workflow
- Continuous validation loop

---

## Assessment: TDD Integration

### Quiz Questions

1. What is the main advantage of writing tests before AI implementation?
   - A) Faster development
   - B) Tests act as unambiguous specifications for AI
   - C) Reduces code complexity
   - D) Eliminates need for human review

2. In TDAD, what should happen if an AI-generated change breaks existing tests?
   - A) Modify the tests to match the new code
   - B) Reject the change and retry
   - C) Deploy anyway and fix later
   - D) Add more tests

3. Which BMAD phase is most critical for writing acceptance tests?
   - A) Phase 00 - Initialization
   - B) Phase 01 - Discovery (User Stories)
   - C) Phase 03 - Architecture
   - D) Phase 05 - Testing

### Practical Exercise

**Task**: Apply TDAD to implement `NotificationService.sendSecurityAlert()`

1. Write 3 specification tests defining expected behavior
2. Create BEAD task for AI implementation
3. Run AI agent with failing tests
4. Verify all tests pass
5. Refactor for code quality

---

## Key Takeaways

1. **Tests = Specifications**: In TDAD, tests are the unambiguous requirements for AI
2. **Immediate Validation**: AI gets instant feedback on correctness
3. **Human Focus Shifts**: Humans write tests (define WHAT), AI writes code (define HOW)
4. **Safety Net**: Automated tests catch regressions immediately
5. **Hybrid Approach**: Combines BMAD's structure with TDD's rigor

---

## Resources

- [TDFlow Paper (arXiv)](https://arxiv.org/html/2510.23761v1)
- [Agentic Coding Handbook - TDD](https://tweag.github.io/agentic-coding-handbook/WORKFLOW_TDD/)
- [Domain-Driven TDD for AI](https://langwatch.ai/blog/from-scenario-to-finished-how-to-test-ai-agents-with-domain-driven-tdd)
- [Claude Code and TDD](https://thenewstack.io/claude-code-and-the-art-of-test-driven-development/)

---

*This module extends the Agentic AEM Development Course with Test-Driven Development integration.*
