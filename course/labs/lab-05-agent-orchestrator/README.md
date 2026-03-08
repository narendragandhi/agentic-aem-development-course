# Lab 5: Agent Orchestrator (3 hours)

## Objective
Build a multi-agent orchestration engine that coordinates specialized AI agents for TDD workflows, including workflow execution, context sharing, and checkpoints.

---

## Part 1: GasTown Concepts (30 min)

### 1.1 The Mayor Pattern

```
┌─────────────────────────────────────────────────────────────────┐
│                    AGENT ORCHESTRATOR                            │
│                       (The Mayor)                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐ │
│   │   Spec   │───▶│Developer │───▶│   Test   │───▶│ Reviewer │ │
│   │  Writer  │    │  Agent   │    │  Runner  │    │  Agent   │ │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘ │
│         │              │              │              │          │
│         └──────────────┴──────────────┴──────────────┘          │
│                              │                                   │
│                    ┌─────────▼─────────┐                        │
│                    │  SHARED CONTEXT   │                        │
│                    │  - specFile       │                        │
│                    │  - testCount      │                        │
│                    │  - phase          │                        │
│                    └───────────────────┘                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Key Concepts

| Concept | Description |
|---------|-------------|
| **Agent** | Specialized worker with specific capabilities |
| **Workflow** | Sequence of agent tasks with dependencies |
| **Context** | Shared state passed between agents |
| **Checkpoint** | Save point for recovery |

---

## Part 2: Define Agent Interface (30 min)

### 2.1 Core Interfaces

```java
package com.demo.workflow.agents;

/**
 * Agent interface for specialized workers.
 */
public interface Agent {
    /** Unique identifier */
    String getId();

    /** Human-readable role description */
    String getRole();

    /** What this agent does */
    String getDescription();

    /** Execute the agent's task */
    AgentResult execute(AgentContext context);
}

/**
 * Context passed to agents during execution.
 */
public class AgentContext {
    private final String taskName;
    private final Map<String, String> parameters;
    private final Map<String, Object> sharedContext;

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public Object getSharedValue(String key) {
        return sharedContext.get(key);
    }
}

/**
 * Result from agent execution.
 */
public class AgentResult {
    private final boolean success;
    private final Map<String, Object> outputs;
    private final String error;

    public static AgentResult success(Map<String, Object> outputs) {
        return new AgentResult(true, outputs, null);
    }

    public static AgentResult failure(String error) {
        return new AgentResult(false, Map.of(), error);
    }
}
```

---

## Part 3: Build the Orchestrator (1 hour)

### 3.1 Write Tests First

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentOrchestrator Tests")
class AgentOrchestratorTest {

    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new AgentOrchestrator();
        orchestrator.activate();
    }

    @Test
    @DisplayName("should register built-in agents on activation")
    void shouldRegisterBuiltInAgents() {
        List<Agent> agents = orchestrator.listAgents();

        assertFalse(agents.isEmpty());
        assertTrue(agents.stream().anyMatch(a -> a.getId().equals("aem-spec-writer")));
        assertTrue(agents.stream().anyMatch(a -> a.getId().equals("test-runner")));
    }

    @Test
    @DisplayName("should execute simple workflow")
    void shouldExecuteSimpleWorkflow() throws InterruptedException {
        WorkflowDefinition workflow = WorkflowDefinition.builder("TDD")
            .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
            .addStep(WorkflowStep.of(2, "test-runner", "Run tests"))
            .build();

        WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

        while (!execution.isComplete()) {
            Thread.sleep(100);
        }

        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals(2, execution.getStepResults().size());
    }

    @Test
    @DisplayName("should share context between steps")
    void shouldShareContextBetweenSteps() throws InterruptedException {
        WorkflowDefinition workflow = WorkflowDefinition.builder("Context")
            .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
            .addStep(WorkflowStep.of(2, "aem-developer", "Implement"))
            .build();

        WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

        while (!execution.isComplete()) {
            Thread.sleep(100);
        }

        // Step 2 should have access to step 1's outputs
        assertNotNull(execution.getStepResults().get(1));
        assertNotNull(execution.getStepResults().get(2));
    }
}
```

### 3.2 Implement Orchestrator

```java
@Component(service = AgentOrchestrator.class, immediate = true)
public class AgentOrchestrator {

    private static final Logger LOG = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Activate
    protected void activate() {
        LOG.info("Agent Orchestrator activated");
        registerBuiltInAgents();
    }

    @Deactivate
    protected void deactivate() {
        executor.shutdown();
    }

    // ═══════════════════════════════════════════════════════════════
    // Agent Registration
    // ═══════════════════════════════════════════════════════════════

    public void registerAgent(Agent agent) {
        agents.put(agent.getId(), agent);
        LOG.info("Registered agent: {}", agent.getId());
    }

    public Optional<Agent> getAgent(String id) {
        return Optional.ofNullable(agents.get(id));
    }

    public List<Agent> listAgents() {
        return new ArrayList<>(agents.values());
    }

    // ═══════════════════════════════════════════════════════════════
    // Workflow Execution
    // ═══════════════════════════════════════════════════════════════

    public WorkflowExecution executeWorkflow(WorkflowDefinition workflow) {
        WorkflowExecution execution = new WorkflowExecution(
            UUID.randomUUID().toString(),
            workflow,
            Instant.now()
        );

        CompletableFuture.runAsync(() -> runWorkflow(execution), executor);

        return execution;
    }

    private void runWorkflow(WorkflowExecution execution) {
        Map<String, Object> sharedContext = new HashMap<>();
        execution.setStatus(ExecutionStatus.RUNNING);

        for (WorkflowStep step : execution.getWorkflow().getSteps()) {
            LOG.info("Executing step {}: {}", step.getOrder(), step.getAction());

            AgentContext context = new AgentContext(
                step.getAction(),
                step.getParameters(),
                sharedContext
            );

            AgentResult result = executeAgent(step.getAgentId(), context);
            execution.addStepResult(step.getOrder(), result);

            if (result.isSuccess()) {
                sharedContext.putAll(result.getOutputs());
            } else if (step.isRequired()) {
                execution.setStatus(ExecutionStatus.FAILED);
                execution.setError(result.getError());
                return;
            }

            if (step.isCheckpoint()) {
                execution.setLastCheckpoint(step.getOrder());
            }
        }

        execution.setStatus(ExecutionStatus.COMPLETED);
    }

    public AgentResult executeAgent(String agentId, AgentContext context) {
        Agent agent = agents.get(agentId);
        if (agent == null) {
            return AgentResult.failure("Agent not found: " + agentId);
        }
        return agent.execute(context);
    }

    // ═══════════════════════════════════════════════════════════════
    // Built-in Agents
    // ═══════════════════════════════════════════════════════════════

    private void registerBuiltInAgents() {
        // Spec Writer Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "aem-spec-writer"; }
            @Override public String getRole() { return "TDD Specialist"; }
            @Override public String getDescription() {
                return "Creates specification tests before implementation";
            }
            @Override public AgentResult execute(AgentContext ctx) {
                return AgentResult.success(Map.of(
                    "specFile", ctx.getParameter("className") + "Spec.java",
                    "testCount", 15
                ));
            }
        });

        // Developer Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "aem-developer"; }
            @Override public String getRole() { return "Implementation Specialist"; }
            @Override public String getDescription() {
                return "Implements services to pass specification tests";
            }
            @Override public AgentResult execute(AgentContext ctx) {
                return AgentResult.success(Map.of(
                    "implFile", ctx.getParameter("className") + "Impl.java",
                    "linesOfCode", 250
                ));
            }
        });

        // Test Runner Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "test-runner"; }
            @Override public String getRole() { return "Test Executor"; }
            @Override public String getDescription() {
                return "Executes tests and validates TDD phases";
            }
            @Override public AgentResult execute(AgentContext ctx) {
                String phase = ctx.getParameter("phase");
                int passed = "red".equals(phase) ? 0 : 15;
                return AgentResult.success(Map.of(
                    "passed", passed,
                    "failed", 15 - passed,
                    "phase", phase != null ? phase : "green"
                ));
            }
        });

        // Code Reviewer Agent
        registerAgent(new Agent() {
            @Override public String getId() { return "code-reviewer"; }
            @Override public String getRole() { return "Quality Assurance"; }
            @Override public String getDescription() {
                return "Reviews code for quality and best practices";
            }
            @Override public AgentResult execute(AgentContext ctx) {
                return AgentResult.success(Map.of(
                    "score", 92,
                    "grade", "A",
                    "findings", 3
                ));
            }
        });

        // Documentation Writer
        registerAgent(new Agent() {
            @Override public String getId() { return "doc-writer"; }
            @Override public String getRole() { return "Documentation Specialist"; }
            @Override public String getDescription() {
                return "Generates Javadocs and BEAD task files";
            }
            @Override public AgentResult execute(AgentContext ctx) {
                return AgentResult.success(Map.of(
                    "beadFile", ctx.getParameter("taskName") + ".yaml",
                    "generated", true
                ));
            }
        });

        LOG.info("Registered {} built-in agents", agents.size());
    }
}
```

---

## Part 4: Workflow Definition (30 min)

### 4.1 Supporting Classes

```java
public class WorkflowDefinition {
    private final String name;
    private final String description;
    private final List<WorkflowStep> steps;

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String description = "";
        private final List<WorkflowStep> steps = new ArrayList<>();

        public Builder addStep(WorkflowStep step) {
            steps.add(step);
            return this;
        }

        public WorkflowDefinition build() {
            return new WorkflowDefinition(name, description, steps);
        }
    }
}

public class WorkflowStep {
    private final int order;
    private final String agentId;
    private final String action;
    private final Map<String, String> parameters;
    private final boolean required;
    private final boolean checkpoint;

    public static WorkflowStep of(int order, String agentId, String action) {
        return new WorkflowStep(order, agentId, action, Map.of(), true, false);
    }

    public static WorkflowStep checkpoint(int order, String agentId, String action) {
        return new WorkflowStep(order, agentId, action, Map.of(), true, true);
    }
}

public enum ExecutionStatus {
    PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
}
```

---

## Part 5: Example TDD Workflow (30 min)

### 5.1 Define a Complete TDD Workflow

```java
WorkflowDefinition tddWorkflow = WorkflowDefinition.builder("TDD Implementation")
    .description("Full TDD cycle for new feature")
    .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specifications"))
    .addStep(WorkflowStep.checkpoint(2, "test-runner", "Verify RED phase"))
    .addStep(WorkflowStep.of(3, "aem-developer", "Implement feature"))
    .addStep(WorkflowStep.checkpoint(4, "test-runner", "Verify GREEN phase"))
    .addStep(WorkflowStep.of(5, "code-reviewer", "Review implementation"))
    .addStep(WorkflowStep.of(6, "doc-writer", "Generate documentation"))
    .build();

WorkflowExecution execution = orchestrator.executeWorkflow(tddWorkflow);
```

---

## Verification Checklist

- [ ] Agent interface defined
- [ ] 5 built-in agents registered
- [ ] Workflow execution works async
- [ ] Context shared between steps
- [ ] Checkpoints recorded
- [ ] 12 tests passing

---

## Run Tests

```bash
mvn test -pl core -Dtest=AgentOrchestratorTest
# Expected: 12 tests passing
```

---

## Next Lab
[Lab 6: Testing](../lab-06-testing/README.md)
