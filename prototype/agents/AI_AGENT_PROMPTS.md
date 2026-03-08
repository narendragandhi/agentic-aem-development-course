# AI Agent Prompts
# Ready-to-use prompts for Claude Code, Goose, and other AI agents

---

## CLAUDE.md - Project Context

```markdown
# Secure Asset Workflow - AI Agent Context

You are working on an AEM project implementing a Secure Asset Approval Workflow.

## BMAD Methodology
This project follows the Breakthrough Method for Agile Development (BMAD):
- Phase 00: Initialization
- Phase 01: Discovery (PRD)
- Phase 02: Models
- Phase 03: Architecture
- Phase 04: Development (TDD)
- Phase 05: Testing
- Phase 06: Operations

## TDD Workflow
Always follow Test-Driven Development:
1. RED: Write tests first, see them fail
2. GREEN: Implement to make tests pass
3. REFACTOR: Improve code while keeping tests green

## Project Structure
prototype/
├── core/                    # OSGi bundle
│   └── src/
│       ├── main/java/.../
│       │   ├── services/    # Service interfaces
│       │   ├── process/    # Workflow processes
│       │   └── config/      # OSGi configs
│       └── test/java/.../  # JUnit 5 tests (*Spec.java)
├── docker/                  # Docker Compose
├── mcp-servers/           # MCP configurations
└── .bead/                 # Task tracking
```

## Coding Standards
- Java 11
- JUnit 5 for testing
- Mockito for mocking
- SLF4J for logging
- OSGi DS for dependency injection

## Key Services
- AntivirusScanService: Scans assets for malware
- AntivirusScanProcess: AEM workflow process step

## Build Commands
```bash
# Compile
mvn clean compile

# Test (TDD)
mvn test -Dtest=AntivirusScanServiceSpec

# Package
mvn package
```

Always maintain TDD discipline - tests first!
```

---

## Goose Recipe - AEM Developer

```yaml
name: "AEM Developer"
description: "AI agent for AEM development with TDD"

model:
  provider: anthropic
  model: claude-sonnet-4-20250514
  max_tokens: 80000

context:
  files:
    - CLAUDE.md
    - prototype/pom.xml

instructions: |
  You are an expert AEM developer following TDD methodology.
  
  Rules:
  1. Always write tests FIRST (RED phase)
  2. Then implement to pass tests (GREEN phase)
  3. Refactor while keeping tests green
  
  AEM Patterns:
  - Use @Component for OSGi services
  - Use @Reference for dependency injection
  - Use Sling Models for HTL data
  - Use WorkflowProcess for workflow steps
  
  Testing:
  - JUnit 5 with Mockito
  - Use AEM Mock for context
  - Test file naming: *Spec.java
  
  When given a task:
  1. Check BEAD for task details
  2. Write specification tests first
  3. Implement to pass tests
  4. Run tests to verify
  5. Update BEAD status

tools:
  - read
  - write
  - edit
  - bash
  - mvn

allowed_commands:
  - mvn clean compile
  - mvn test
  - mvn package
  - docker

exit_criteria:
  - "mvn test passes"
  - "Code follows AEM patterns"
  - "BEAD task updated"
```

---

## Claude Code - TDD Prompt

You are working on an AEM project with TDD methodology.

For any new feature:
1. First write JUnit 5 tests in src/test/java/*Spec.java
2. Run tests - they should FAIL (RED)
3. Implement in src/main/java/
4. Run tests - they should PASS (GREEN)
5. Refactor if needed

Example for adding a new method:

1. Write test first:
```java
@Test
void shouldDoSomething() {
    // Given
    MyService service = new MyServiceImpl();
    
    // When
    Result result = service.doSomething();
    
    // Then
    assertEquals(expected, result.getValue());
}
```

2. Then implement:
```java
public Result doSomething() {
    // Implementation
    return new Result(expected);
}
```

Remember: Test first, then implementation!

---

## Claude Code - BEAD Task Prompt

You are managing tasks using BEAD. Current task: SAW-010

To track progress:
1. Before starting: Update BEAD status to "in_progress"
2. After tests pass: Add session_log entry
3. When complete: Update status to "completed"

Example workflow:
```
# Start task
bead update SAW-010 in_progress
bead log SAW-010 "Starting implementation"

# After RED phase (tests written)
bead log SAW-010 "RED: Tests written"

# After GREEN phase (tests pass)
bead log SAW-010 "GREEN: Implementation complete"
bead update SAW-010 completed
```

Always update BEAD as you work!

---

## Claude Code - Security Review Prompt

Review the code for security issues:

1. XSS vulnerabilities
   - Check for unescaped output in HTL
   - Validate user input
   
2. SQL Injection
   - Check JCR queries for injection
   - Use parameterized queries
   
3. Path Traversal
   - Validate file paths
   - Check for ../ attacks
   
4. Authentication
   - Verify service user permissions
   - Check resource access

For each issue found:
- Severity: Critical/High/Medium/Low
- Location: File:line
- Fix: Suggested fix

---

## Claude Code - AEM Workflow Prompt

Create an AEM workflow process step:

1. Interface: implements WorkflowProcess
2. Annotations: @Component with properties
3. Dependencies: @Reference injection
4. Metadata: Use MetaDataMap for arguments
5. Payload: Get from WorkflowData

Example:
```java
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=My Process",
        "process.description=Does something"
    }
)
public class MyProcess implements WorkflowProcess {
    
    @Reference
    private MyService myService;
    
    @Override
    public void execute(WorkItem workItem, WorkflowSession session,
                       MetaDataMap args) throws Exception {
        String payload = workItem.getWorkflowData().getPayload().toString();
        // Process payload
    }
}
```

---

## Claude Code - Test Generation Prompt

Generate comprehensive JUnit 5 tests for this AEM service:

Requirements:
- Use JUnit 5 (@Test, @DisplayName, @Nested)
- Use Mockito for mocking
- Follow AAA pattern (Arrange-Act-Assert)
- Include edge cases
- Include error handling tests
- Test file name: [ClassName]Spec.java

Output format:
```java
package com.demo.workflow.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ServiceName Specifications")
 {
    
    //class ServiceNameSpec Tests here
}
```

---

## Claude Code - Integration Test Prompt

Generate AEM integration tests using AEM Mock:

Requirements:
- Use io.wcm.testing.aem-mock.junit5
- Use @AemContextExtension
- Test OSGi services in AEM context
- Test workflow processes
- Test Sling Models

Example:
```java
@ExtendWith(AemContextExtension.class)
class MyServiceIntegrationTest {
    
    @Inject
    private MyService service;
    
    @Test
    void shouldWorkInAemContext() {
        // Test with full AEM context
        Result result = service.doSomething();
        assertNotNull(result);
    }
}
```
