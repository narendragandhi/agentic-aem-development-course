package com.demo.workflow.agents;

import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * GasTown Multi-Agent Orchestration Engine.
 *
 * <p>Coordinates multiple specialized AI agents to execute complex workflows.
 * The "Mayor" pattern coordinates agents, manages dependencies, handles
 * failures, and aggregates results.</p>
 *
 * <p>Key concepts:</p>
 * <ul>
 *   <li>Mayor AI - Orchestrator that coordinates agent execution</li>
 *   <li>Agents - Specialized workers with specific capabilities</li>
 *   <li>Workflow - Sequence of agent tasks with dependencies</li>
 *   <li>Context - Shared state passed between agents</li>
 * </ul>
 */
@Component(service = AgentOrchestrator.class, immediate = true)
public class AgentOrchestrator {

    private static final Logger LOG = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final Map<String, Agent> registeredAgents = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final List<WorkflowExecution> activeExecutions = new CopyOnWriteArrayList<>();

    @Activate
    protected void activate() {
        LOG.info("GasTown Agent Orchestrator activated");
        registerBuiltInAgents();
    }

    @Deactivate
    protected void deactivate() {
        executorService.shutdown();
        LOG.info("GasTown Agent Orchestrator deactivated");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Agent Registration
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Register an agent with the orchestrator.
     */
    public void registerAgent(Agent agent) {
        registeredAgents.put(agent.getId(), agent);
        LOG.info("Registered agent: {} ({})", agent.getId(), agent.getRole());
    }

    /**
     * Get a registered agent by ID.
     */
    public Optional<Agent> getAgent(String agentId) {
        return Optional.ofNullable(registeredAgents.get(agentId));
    }

    /**
     * List all registered agents.
     */
    public List<Agent> listAgents() {
        return new ArrayList<>(registeredAgents.values());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Workflow Execution
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Execute a workflow with the given configuration.
     */
    public WorkflowExecution executeWorkflow(WorkflowDefinition workflow) {
        LOG.info("Starting workflow: {}", workflow.getName());

        WorkflowExecution execution = new WorkflowExecution(
            UUID.randomUUID().toString(),
            workflow,
            Instant.now()
        );

        activeExecutions.add(execution);

        CompletableFuture.runAsync(() -> runWorkflow(execution), executorService)
            .exceptionally(ex -> {
                execution.setStatus(ExecutionStatus.FAILED);
                execution.setError(ex.getMessage());
                LOG.error("Workflow {} failed", execution.getId(), ex);
                return null;
            });

        return execution;
    }

    /**
     * Execute a single agent task.
     */
    public AgentResult executeAgent(String agentId, AgentContext context) {
        Agent agent = registeredAgents.get(agentId);
        if (agent == null) {
            return AgentResult.failure("Agent not found: " + agentId);
        }

        LOG.info("Executing agent: {} with context: {}", agentId, context.getTaskName());
        return agent.execute(context);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Internal Workflow Runner
    // ═══════════════════════════════════════════════════════════════════

    private void runWorkflow(WorkflowExecution execution) {
        WorkflowDefinition workflow = execution.getWorkflow();
        Map<String, Object> sharedContext = new HashMap<>();

        execution.setStatus(ExecutionStatus.RUNNING);

        for (WorkflowStep step : workflow.getSteps()) {
            LOG.info("Executing step {}: {} -> {}",
                step.getOrder(), step.getAgentId(), step.getAction());

            // Check preconditions
            if (!checkPreconditions(step, sharedContext)) {
                LOG.warn("Preconditions not met for step {}, skipping", step.getOrder());
                continue;
            }

            // Execute agent
            AgentContext context = new AgentContext(
                step.getAction(),
                step.getParameters(),
                sharedContext
            );

            AgentResult result = executeAgent(step.getAgentId(), context);

            // Record step result
            execution.addStepResult(step.getOrder(), result);

            // Handle result
            if (!result.isSuccess()) {
                if (step.isRequired()) {
                    execution.setStatus(ExecutionStatus.FAILED);
                    execution.setError("Required step failed: " + step.getAction());
                    LOG.error("Workflow failed at step {}: {}", step.getOrder(), result.getError());
                    return;
                } else {
                    LOG.warn("Optional step {} failed, continuing", step.getOrder());
                }
            } else {
                // Merge outputs to shared context
                sharedContext.putAll(result.getOutputs());
            }

            // Checkpoint
            if (step.isCheckpoint()) {
                LOG.info("Checkpoint reached at step {}", step.getOrder());
                execution.setLastCheckpoint(step.getOrder());
            }
        }

        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setCompletedAt(Instant.now());
        LOG.info("Workflow {} completed successfully", execution.getId());
    }

    private boolean checkPreconditions(WorkflowStep step, Map<String, Object> context) {
        for (String precondition : step.getPreconditions()) {
            if (!context.containsKey(precondition)) {
                return false;
            }
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Built-in Agents
    // ═══════════════════════════════════════════════════════════════════

    private void registerBuiltInAgents() {
        // AEM Spec Writer Agent
        registerAgent(new Agent() {
            @Override
            public String getId() { return "aem-spec-writer"; }

            @Override
            public String getRole() { return "TDD Specialist"; }

            @Override
            public String getDescription() {
                return "Creates specification tests before implementation";
            }

            @Override
            public AgentResult execute(AgentContext context) {
                LOG.info("AEM Spec Writer executing: {}", context.getTaskName());
                // In production, this would invoke Claude API
                return AgentResult.success(Map.of(
                    "specFile", context.getParameter("className") + "Spec.java",
                    "testCount", 15
                ));
            }
        });

        // AEM Developer Agent
        registerAgent(new Agent() {
            @Override
            public String getId() { return "aem-developer"; }

            @Override
            public String getRole() { return "Implementation Specialist"; }

            @Override
            public String getDescription() {
                return "Implements services to pass specification tests";
            }

            @Override
            public AgentResult execute(AgentContext context) {
                LOG.info("AEM Developer executing: {}", context.getTaskName());
                return AgentResult.success(Map.of(
                    "implementationFile", context.getParameter("className") + "Impl.java",
                    "linesOfCode", 250
                ));
            }
        });

        // Test Runner Agent
        registerAgent(new Agent() {
            @Override
            public String getId() { return "test-runner"; }

            @Override
            public String getRole() { return "Test Executor"; }

            @Override
            public String getDescription() {
                return "Executes tests and validates phases";
            }

            @Override
            public AgentResult execute(AgentContext context) {
                String phase = context.getParameter("phase");
                LOG.info("Test Runner executing {} phase", phase);

                // Simulate test execution
                if ("red".equals(phase)) {
                    return AgentResult.success(Map.of(
                        "passed", 0,
                        "failed", 15,
                        "phase", "red"
                    ));
                } else {
                    return AgentResult.success(Map.of(
                        "passed", 15,
                        "failed", 0,
                        "phase", "green"
                    ));
                }
            }
        });

        // Code Reviewer Agent
        registerAgent(new Agent() {
            @Override
            public String getId() { return "code-reviewer"; }

            @Override
            public String getRole() { return "Quality Assurance"; }

            @Override
            public String getDescription() {
                return "Reviews code for quality and best practices";
            }

            @Override
            public AgentResult execute(AgentContext context) {
                LOG.info("Code Reviewer executing: {}", context.getTaskName());
                return AgentResult.success(Map.of(
                    "score", 92,
                    "grade", "A",
                    "findings", 3
                ));
            }
        });

        // Documentation Writer Agent
        registerAgent(new Agent() {
            @Override
            public String getId() { return "documentation-writer"; }

            @Override
            public String getRole() { return "Documentation Specialist"; }

            @Override
            public String getDescription() {
                return "Generates documentation and BEAD task files";
            }

            @Override
            public AgentResult execute(AgentContext context) {
                LOG.info("Documentation Writer executing: {}", context.getTaskName());
                return AgentResult.success(Map.of(
                    "beadFile", context.getParameter("taskName") + ".yaml",
                    "generated", true
                ));
            }
        });

        LOG.info("Registered {} built-in agents", registeredAgents.size());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Inner Classes
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Agent interface for specialized workers.
     */
    public interface Agent {
        String getId();
        String getRole();
        String getDescription();
        AgentResult execute(AgentContext context);
    }

    /**
     * Context passed to agents for execution.
     */
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

    /**
     * Result from agent execution.
     */
    public static class AgentResult {
        private final boolean success;
        private final Map<String, Object> outputs;
        private final String error;

        private AgentResult(boolean success, Map<String, Object> outputs, String error) {
            this.success = success;
            this.outputs = outputs != null ? outputs : new HashMap<>();
            this.error = error;
        }

        public static AgentResult success(Map<String, Object> outputs) {
            return new AgentResult(true, outputs, null);
        }

        public static AgentResult failure(String error) {
            return new AgentResult(false, null, error);
        }

        public boolean isSuccess() { return success; }
        public Map<String, Object> getOutputs() { return outputs; }
        public String getError() { return error; }
    }

    /**
     * Workflow definition with steps.
     */
    public static class WorkflowDefinition {
        private final String name;
        private final String description;
        private final List<WorkflowStep> steps;

        public WorkflowDefinition(String name, String description, List<WorkflowStep> steps) {
            this.name = name;
            this.description = description;
            this.steps = steps;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<WorkflowStep> getSteps() { return steps; }

        public static Builder builder(String name) {
            return new Builder(name);
        }

        public static class Builder {
            private final String name;
            private String description = "";
            private final List<WorkflowStep> steps = new ArrayList<>();

            public Builder(String name) {
                this.name = name;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder addStep(WorkflowStep step) {
                this.steps.add(step);
                return this;
            }

            public WorkflowDefinition build() {
                return new WorkflowDefinition(name, description, steps);
            }
        }
    }

    /**
     * Single step in a workflow.
     */
    public static class WorkflowStep {
        private final int order;
        private final String agentId;
        private final String action;
        private final Map<String, String> parameters;
        private final List<String> preconditions;
        private final boolean required;
        private final boolean checkpoint;

        public WorkflowStep(int order, String agentId, String action,
                           Map<String, String> parameters, List<String> preconditions,
                           boolean required, boolean checkpoint) {
            this.order = order;
            this.agentId = agentId;
            this.action = action;
            this.parameters = parameters != null ? parameters : new HashMap<>();
            this.preconditions = preconditions != null ? preconditions : new ArrayList<>();
            this.required = required;
            this.checkpoint = checkpoint;
        }

        public int getOrder() { return order; }
        public String getAgentId() { return agentId; }
        public String getAction() { return action; }
        public Map<String, String> getParameters() { return parameters; }
        public List<String> getPreconditions() { return preconditions; }
        public boolean isRequired() { return required; }
        public boolean isCheckpoint() { return checkpoint; }

        public static WorkflowStep of(int order, String agentId, String action) {
            return new WorkflowStep(order, agentId, action, null, null, true, false);
        }

        public static WorkflowStep checkpoint(int order, String agentId, String action) {
            return new WorkflowStep(order, agentId, action, null, null, true, true);
        }
    }

    /**
     * Execution state of a workflow.
     */
    public static class WorkflowExecution {
        private final String id;
        private final WorkflowDefinition workflow;
        private final Instant startedAt;
        private Instant completedAt;
        private ExecutionStatus status;
        private String error;
        private int lastCheckpoint;
        private final Map<Integer, AgentResult> stepResults = new ConcurrentHashMap<>();

        public WorkflowExecution(String id, WorkflowDefinition workflow, Instant startedAt) {
            this.id = id;
            this.workflow = workflow;
            this.startedAt = startedAt;
            this.status = ExecutionStatus.PENDING;
        }

        public String getId() { return id; }
        public WorkflowDefinition getWorkflow() { return workflow; }
        public Instant getStartedAt() { return startedAt; }
        public Instant getCompletedAt() { return completedAt; }
        public ExecutionStatus getStatus() { return status; }
        public String getError() { return error; }
        public int getLastCheckpoint() { return lastCheckpoint; }
        public Map<Integer, AgentResult> getStepResults() { return stepResults; }

        public void setStatus(ExecutionStatus status) { this.status = status; }
        public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
        public void setError(String error) { this.error = error; }
        public void setLastCheckpoint(int lastCheckpoint) { this.lastCheckpoint = lastCheckpoint; }
        public void addStepResult(int step, AgentResult result) { stepResults.put(step, result); }

        public boolean isComplete() {
            return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED;
        }
    }

    /**
     * Workflow execution status.
     */
    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
