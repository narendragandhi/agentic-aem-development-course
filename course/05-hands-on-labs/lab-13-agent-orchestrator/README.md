# Lab 13: Building the Agent Orchestrator

## Objective
Implement a working multi-agent orchestration engine that coordinates specialized AI agents for complex development workflows, including agent registration, workflow execution, checkpoints, and shared context.

---

## Prerequisites
- Completed Labs 01-06 (GasTown concepts)
- Understanding of Java concurrency
- Familiarity with the CompletableFuture API

---

## Learning Outcomes
After completing this lab, you will be able to:
1. Implement the Agent interface pattern
2. Build workflow execution with dependency management
3. Handle shared context between agents
4. Implement checkpoints for recovery
5. Create specialized agents for AEM development

---

## Part 1: Understanding Multi-Agent Architecture

### 1.1 The "Mayor" Pattern

```
┌─────────────────────────────────────────────────────────────────┐
│                    AGENT ORCHESTRATOR (Mayor)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│   │ Spec Writer │  │  Developer  │  │ Test Runner │            │
│   │   Agent     │─▶│   Agent     │─▶│   Agent     │            │
│   └─────────────┘  └─────────────┘  └─────────────┘            │
│         │                │                │                     │
│         ▼                ▼                ▼                     │
│   ┌─────────────────────────────────────────────────┐          │
│   │              SHARED CONTEXT                      │          │
│   │  specFile: "SecuritySpec.java"                   │          │
│   │  testCount: 15                                   │          │
│   │  phase: "green"                                  │          │
│   └─────────────────────────────────────────────────┘          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Agent Lifecycle

1. **Registration** - Agents register their capabilities
2. **Workflow Definition** - Steps with dependencies defined
3. **Execution** - Mayor coordinates agent execution
4. **Context Sharing** - Outputs flow to next agents
5. **Checkpointing** - Save state for recovery

---

## Part 2: RED Phase - Write Specification Tests

### 2.1 Create Test File

Create `AgentOrchestratorTest.java`:

```java
package com.demo.workflow.agents;

import com.demo.workflow.agents.AgentOrchestrator.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentOrchestrator Tests")
class AgentOrchestratorTest {

    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new AgentOrchestrator();
        orchestrator.activate();
    }

    @AfterEach
    void tearDown() {
        orchestrator.deactivate();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Agent Registration Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When registering agents")
    class AgentRegistration {

        @Test
        @DisplayName("should register built-in agents on activation")
        void shouldRegisterBuiltInAgents() {
            List<Agent> agents = orchestrator.listAgents();

            assertFalse(agents.isEmpty());
            assertTrue(agents.stream().anyMatch(a -> a.getId().equals("aem-spec-writer")));
            assertTrue(agents.stream().anyMatch(a -> a.getId().equals("test-runner")));
        }

        @Test
        @DisplayName("should register custom agent")
        void shouldRegisterCustomAgent() {
            Agent customAgent = new Agent() {
                @Override public String getId() { return "custom-agent"; }
                @Override public String getRole() { return "Custom Role"; }
                @Override public String getDescription() { return "Custom agent"; }
                @Override public AgentResult execute(AgentContext context) {
                    return AgentResult.success(Map.of("custom", true));
                }
            };

            orchestrator.registerAgent(customAgent);

            Optional<Agent> found = orchestrator.getAgent("custom-agent");
            assertTrue(found.isPresent());
        }

        @Test
        @DisplayName("should find agent by ID")
        void shouldFindAgentById() {
            Optional<Agent> agent = orchestrator.getAgent("test-runner");

            assertTrue(agent.isPresent());
            assertEquals("Test Executor", agent.get().getRole());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Workflow Execution Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When executing workflows")
    class WorkflowExecution {

        @Test
        @DisplayName("should execute simple workflow")
        void shouldExecuteSimpleWorkflow() throws InterruptedException {
            WorkflowDefinition workflow = WorkflowDefinition.builder("TDD Implementation")
                .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
                .addStep(WorkflowStep.of(2, "test-runner", "Run RED phase"))
                .addStep(WorkflowStep.of(3, "aem-developer", "Implement"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            // Wait for completion
            while (!execution.isComplete()) {
                Thread.sleep(100);
            }

            assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
            assertEquals(3, execution.getStepResults().size());
        }

        @Test
        @DisplayName("should support checkpoints")
        void shouldSupportCheckpoints() throws InterruptedException {
            WorkflowDefinition workflow = WorkflowDefinition.builder("Checkpointed")
                .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
                .addStep(WorkflowStep.checkpoint(2, "test-runner", "Checkpoint"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            while (!execution.isComplete()) {
                Thread.sleep(100);
            }

            assertTrue(execution.getLastCheckpoint() >= 2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Context Sharing Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When sharing context between agents")
    class ContextSharing {

        @Test
        @DisplayName("should share outputs between steps")
        void shouldShareOutputsBetweenSteps() throws InterruptedException {
            WorkflowDefinition workflow = WorkflowDefinition.builder("Context")
                .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
                .addStep(WorkflowStep.of(2, "test-runner", "Run tests"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            while (!execution.isComplete()) {
                Thread.sleep(100);
            }

            // Both steps should have results
            assertNotNull(execution.getStepResults().get(1));
            assertNotNull(execution.getStepResults().get(2));
        }
    }
}
```

---

## Part 3: GREEN Phase - Implement Orchestrator

### 3.1 Create the Agent Interface

```java
package com.demo.workflow.agents;

public interface Agent {
    String getId();
    String getRole();
    String getDescription();
    AgentResult execute(AgentContext context);
}
```

### 3.2 Implement AgentOrchestrator

```java
@Component(service = AgentOrchestrator.class, immediate = true)
public class AgentOrchestrator {

    private static final Logger LOG = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final Map<String, Agent> registeredAgents = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Activate
    protected void activate() {
        LOG.info("Agent Orchestrator activated");
        registerBuiltInAgents();
    }

    @Deactivate
    protected void deactivate() {
        executorService.shutdown();
    }

    // Agent Registration
    public void registerAgent(Agent agent) {
        registeredAgents.put(agent.getId(), agent);
    }

    public Optional<Agent> getAgent(String agentId) {
        return Optional.ofNullable(registeredAgents.get(agentId));
    }

    public List<Agent> listAgents() {
        return new ArrayList<>(registeredAgents.values());
    }

    // Workflow Execution
    public WorkflowExecution executeWorkflow(WorkflowDefinition workflow) {
        WorkflowExecution execution = new WorkflowExecution(
            UUID.randomUUID().toString(),
            workflow,
            Instant.now()
        );

        CompletableFuture.runAsync(() -> runWorkflow(execution), executorService);

        return execution;
    }

    private void runWorkflow(WorkflowExecution execution) {
        WorkflowDefinition workflow = execution.getWorkflow();
        Map<String, Object> sharedContext = new HashMap<>();

        execution.setStatus(ExecutionStatus.RUNNING);

        for (WorkflowStep step : workflow.getSteps()) {
            // Execute agent
            AgentContext context = new AgentContext(
                step.getAction(),
                step.getParameters(),
                sharedContext
            );

            AgentResult result = executeAgent(step.getAgentId(), context);
            execution.addStepResult(step.getOrder(), result);

            if (result.isSuccess()) {
                sharedContext.putAll(result.getOutputs());
            }

            if (step.isCheckpoint()) {
                execution.setLastCheckpoint(step.getOrder());
            }
        }

        execution.setStatus(ExecutionStatus.COMPLETED);
    }

    // Built-in Agents
    private void registerBuiltInAgents() {
        // Spec Writer Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "aem-spec-writer"; }
            @Override public String getRole() { return "TDD Specialist"; }
            @Override public String getDescription() { return "Creates specification tests"; }
            @Override public AgentResult execute(AgentContext context) {
                return AgentResult.success(Map.of(
                    "specFile", context.getParameter("className") + "Spec.java",
                    "testCount", 15
                ));
            }
        });

        // Developer Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "aem-developer"; }
            @Override public String getRole() { return "Implementation Specialist"; }
            @Override public String getDescription() { return "Implements services"; }
            @Override public AgentResult execute(AgentContext context) {
                return AgentResult.success(Map.of(
                    "implementationFile", context.getParameter("className") + "Impl.java"
                ));
            }
        });

        // Test Runner Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "test-runner"; }
            @Override public String getRole() { return "Test Executor"; }
            @Override public String getDescription() { return "Runs tests"; }
            @Override public AgentResult execute(AgentContext context) {
                String phase = context.getParameter("phase");
                return AgentResult.success(Map.of(
                    "passed", "red".equals(phase) ? 0 : 15,
                    "phase", phase != null ? phase : "green"
                ));
            }
        });
    }
}
```

---

## Part 4: Supporting Classes

### 4.1 AgentContext

```java
public static class AgentContext {
    private final String taskName;
    private final Map<String, String> parameters;
    private final Map<String, Object> sharedContext;

    public AgentContext(String taskName, Map<String, String> parameters,
                       Map<String, Object> sharedContext) {
        this.taskName = taskName;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.sharedContext = sharedContext != null ? sharedContext : new HashMap<>();
    }

    public String getTaskName() { return taskName; }
    public String getParameter(String key) { return parameters.get(key); }
    public Object getSharedValue(String key) { return sharedContext.get(key); }
}
```

### 4.2 AgentResult

```java
public static class AgentResult {
    private final boolean success;
    private final Map<String, Object> outputs;
    private final String error;

    public static AgentResult success(Map<String, Object> outputs) {
        return new AgentResult(true, outputs, null);
    }

    public static AgentResult failure(String error) {
        return new AgentResult(false, null, error);
    }
}
```

### 4.3 WorkflowDefinition

```java
public static class WorkflowDefinition {
    private final String name;
    private final List<WorkflowStep> steps;

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private final List<WorkflowStep> steps = new ArrayList<>();

        public Builder addStep(WorkflowStep step) {
            this.steps.add(step);
            return this;
        }

        public WorkflowDefinition build() {
            return new WorkflowDefinition(name, "", steps);
        }
    }
}
```

---

## Part 5: Run Tests

```bash
mvn test -pl core -Dtest=AgentOrchestratorTest
```

**Expected:** 12 tests passing

---

## Verification Checklist

- [ ] All 12 orchestrator tests pass
- [ ] 5 built-in agents registered
- [ ] Workflows execute asynchronously
- [ ] Context shared between steps
- [ ] Checkpoints are recorded
- [ ] Failed steps are handled

---

## Bonus Challenges

1. **Add Parallel Execution:** Run independent steps concurrently
2. **Add Retry Logic:** Retry failed steps with backoff
3. **Add Event Listeners:** Publish step completion events
4. **Add Real AI Integration:** Connect to Claude API

---

## References

- [Java CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)
- [OSGi Declarative Services](https://osgi.org/specification/osgi.cmpn/7.0.0/service.component.html)
- [Multi-Agent Systems](https://en.wikipedia.org/wiki/Multi-agent_system)
