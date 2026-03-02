package com.demo.workflow.agents;

import com.demo.workflow.agents.AgentOrchestrator.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GasTown Agent Orchestrator.
 */
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
            assertTrue(agents.stream().anyMatch(a -> a.getId().equals("aem-developer")));
            assertTrue(agents.stream().anyMatch(a -> a.getId().equals("test-runner")));
            assertTrue(agents.stream().anyMatch(a -> a.getId().equals("code-reviewer")));
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
            assertEquals("Custom Role", found.get().getRole());
        }

        @Test
        @DisplayName("should find agent by ID")
        void shouldFindAgentById() {
            Optional<Agent> agent = orchestrator.getAgent("test-runner");

            assertTrue(agent.isPresent());
            assertEquals("Test Executor", agent.get().getRole());
        }

        @Test
        @DisplayName("should return empty for unknown agent")
        void shouldReturnEmptyForUnknownAgent() {
            Optional<Agent> agent = orchestrator.getAgent("unknown-agent");

            assertFalse(agent.isPresent());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Agent Execution Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When executing agents")
    class AgentExecution {

        @Test
        @DisplayName("should execute agent successfully")
        void shouldExecuteAgentSuccessfully() {
            AgentContext context = new AgentContext(
                "Create spec tests",
                Map.of("className", "SecurityScanner"),
                new HashMap<>()
            );

            AgentResult result = orchestrator.executeAgent("aem-spec-writer", context);

            assertTrue(result.isSuccess());
            assertEquals("SecurityScannerSpec.java", result.getOutputs().get("specFile"));
        }

        @Test
        @DisplayName("should return failure for unknown agent")
        void shouldReturnFailureForUnknownAgent() {
            AgentContext context = new AgentContext("Task", Map.of(), new HashMap<>());

            AgentResult result = orchestrator.executeAgent("nonexistent", context);

            assertFalse(result.isSuccess());
            assertTrue(result.getError().contains("not found"));
        }

        @Test
        @DisplayName("should pass parameters to agent")
        void shouldPassParametersToAgent() {
            AgentContext context = new AgentContext(
                "Run tests",
                Map.of("phase", "green"),
                new HashMap<>()
            );

            AgentResult result = orchestrator.executeAgent("test-runner", context);

            assertTrue(result.isSuccess());
            assertEquals("green", result.getOutputs().get("phase"));
            assertEquals(15, result.getOutputs().get("passed"));
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
                .description("Implement feature using TDD")
                .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
                .addStep(WorkflowStep.of(2, "test-runner", "Run RED phase"))
                .addStep(WorkflowStep.of(3, "aem-developer", "Implement"))
                .addStep(WorkflowStep.of(4, "test-runner", "Run GREEN phase"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            // Wait for completion
            int attempts = 0;
            while (!execution.isComplete() && attempts < 50) {
                Thread.sleep(100);
                attempts++;
            }

            assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
            assertEquals(4, execution.getStepResults().size());
        }

        @Test
        @DisplayName("should track execution status")
        void shouldTrackExecutionStatus() {
            WorkflowDefinition workflow = WorkflowDefinition.builder("Quick Task")
                .addStep(WorkflowStep.of(1, "code-reviewer", "Review"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            assertNotNull(execution.getId());
            assertNotNull(execution.getStartedAt());
            assertNotNull(execution.getStatus());
        }

        @Test
        @DisplayName("should support checkpoints")
        void shouldSupportCheckpoints() throws InterruptedException {
            WorkflowDefinition workflow = WorkflowDefinition.builder("Checkpointed Workflow")
                .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
                .addStep(WorkflowStep.checkpoint(2, "test-runner", "RED phase checkpoint"))
                .addStep(WorkflowStep.of(3, "aem-developer", "Implement"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            // Wait for completion
            int attempts = 0;
            while (!execution.isComplete() && attempts < 50) {
                Thread.sleep(100);
                attempts++;
            }

            assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
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
            WorkflowDefinition workflow = WorkflowDefinition.builder("Context Sharing")
                .addStep(WorkflowStep.of(1, "aem-spec-writer", "Create specs"))
                .addStep(WorkflowStep.of(2, "documentation-writer", "Generate docs"))
                .build();

            AgentOrchestrator.WorkflowExecution execution = orchestrator.executeWorkflow(workflow);

            // Wait for completion
            int attempts = 0;
            while (!execution.isComplete() && attempts < 50) {
                Thread.sleep(100);
                attempts++;
            }

            // Both steps should have results
            assertNotNull(execution.getStepResults().get(1));
            assertNotNull(execution.getStepResults().get(2));
        }

        @Test
        @DisplayName("should access shared context in agent")
        void shouldAccessSharedContextInAgent() {
            Map<String, Object> sharedContext = new HashMap<>();
            sharedContext.put("previousResult", "success");

            AgentContext context = new AgentContext(
                "Continue work",
                Map.of(),
                sharedContext
            );

            assertEquals("success", context.getSharedValue("previousResult"));
        }
    }
}
