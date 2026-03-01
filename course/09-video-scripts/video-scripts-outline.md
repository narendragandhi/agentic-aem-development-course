# Video Script Outlines
# Agentic Development for AEM Course

## Production Guidelines

| Aspect | Specification |
|--------|---------------|
| Format | 1080p, 30fps |
| Length | 5-15 minutes per video |
| Style | Screen recording + talking head |
| Audio | Clear narration, minimal music |
| Captions | Auto-generated + reviewed |

---

## Module 1: Introduction to Agentic Development

### Video 1.1: Welcome & Course Overview
**Duration:** 5 minutes

```
[SCENE: Instructor on camera]
INTRO:
"Welcome to Agentic Development for AEM. I'm [Name], and in this
course, you'll learn how to leverage AI agents to transform your
AEM development workflow."

[SCENE: Screen showing course outline]
BODY:
"Over the next 8 modules, we'll build a complete Secure Asset
Approval Workflow - a real-world enterprise feature that includes:
- Automatic malware scanning
- Multi-level approval routing
- And complete audit trails

But more importantly, you'll learn a methodology that you can
apply to ANY AEM development project."

[SCENE: Back to instructor]
CLOSE:
"Let's start by understanding what makes agentic development
different from traditional AI-assisted coding."
```

---

### Video 1.2: What is Agentic Development?
**Duration:** 10 minutes

```
[SCENE: Animated diagram building]
INTRO:
"Traditional AI coding assistants help you write code line by line.
Agentic AI is fundamentally different."

[SCENE: Side-by-side comparison]
COMPARISON:
"With traditional tools, you prompt, get a response, and move on.
With agentic systems:
- The AI reasons about the entire task
- Uses tools to read files, run commands, make changes
- Maintains context across sessions
- Works alongside other specialized agents"

[SCENE: Agent loop animation]
EXPLANATION:
"An AI agent follows an observe-think-act loop:
1. Observe: What's the current state?
2. Think: What should I do next?
3. Act: Execute using available tools
4. Learn: Update understanding based on results"

[SCENE: Demo clip]
DEMO:
"Here's a quick example. I'll ask Claude Code to implement a
service, and watch how it:
- Reads existing code for patterns
- Generates the implementation
- Runs the build to verify
- Fixes any issues automatically"

[SCENE: Instructor wrap-up]
CLOSE:
"This autonomous capability is what makes agentic development
so powerful for complex projects like AEM workflows."
```

---

### Video 1.3: The BMAD-BEAD-GasTown Ecosystem
**Duration:** 12 minutes

```
[SCENE: Three pillars diagram]
INTRO:
"Agentic development needs three things:
A methodology, a memory system, and an orchestrator.
That's BMAD, BEAD, and GasTown."

[SCENE: BMAD phases animation]
BMAD SECTION:
"BMAD - the Breakthrough Method for Agile Development -
gives us structure. It has 7 phases from project initialization
through to operations. Each phase has AI-augmented activities
and clear deliverables."

[SCENE: BEAD task hierarchy]
BEAD SECTION:
"BEAD solves the memory problem. AI sessions are stateless -
when you start a new chat, the AI doesn't remember previous work.
BEAD stores tasks in Git-backed YAML files, creating persistent
memory that survives across sessions."

[SCENE: GasTown agent network]
GASTOWN SECTION:
"GasTown orchestrates multiple agents. Instead of one AI doing
everything, we have specialists:
- A coder agent for implementation
- A tester agent for test generation
- A reviewer agent for code review
The Mayor AI coordinates them all."

[SCENE: Integration diagram]
INTEGRATION:
"Together, they form a complete system:
BMAD defines WHAT to build
BEAD tracks WHAT'S DONE
GasTown coordinates WHO DOES WHAT"

[SCENE: Instructor summary]
CLOSE:
"In the following modules, we'll use all three as we build
our Secure Asset Approval Workflow."
```

---

## Module 2: Writing AI-Optimized PRDs

### Video 2.1: PRD Structure for AI Agents
**Duration:** 8 minutes

```
[SCENE: PRD document on screen]
INTRO:
"The quality of your requirements directly impacts the quality
of AI-generated implementations. Let's look at how to structure
PRDs for optimal AI consumption."

[SCENE: Highlighting sections]
STRUCTURE:
"An AI-optimized PRD has these key sections:
1. Executive Summary - context setting
2. User Stories - the WHAT
3. Acceptance Criteria - the HOW TO VERIFY
4. Technical Architecture - the HOW TO BUILD"

[SCENE: User story example]
USER STORIES:
"User stories should follow the standard format:
'As a [role], I want [capability], so that [benefit]'

But for AI agents, be SPECIFIC about the role.
Not just 'user' - say 'content author' or 'security admin'.
The AI uses this to understand permissions and context."

[SCENE: Good vs bad criteria comparison]
ACCEPTANCE CRITERIA:
"Acceptance criteria are where most PRDs fail for AI usage.
BAD: 'System should be fast'
GOOD: 'Scan completes within 60 seconds for files up to 100MB'

The AI needs measurable, testable criteria to verify its work."

[SCENE: Instructor summary]
CLOSE:
"In the lab, you'll analyze our sample PRD and write your own
feature requirements using these principles."
```

---

## Module 3: Architecture Design with AI

### Video 3.1: AI-Generated Specifications
**Duration:** 10 minutes

```
[SCENE: Live coding session]
INTRO:
"Now let's see AI architecture generation in action.
I'll show you how to generate component specifications
using effective prompting."

[SCENE: Terminal/IDE with AI assistant]
DEMO:
"I'm going to ask Claude to generate a specification for
our AntivirusScanService.

First, I provide context:
'I'm designing an AEM service that integrates with ClamAV
for malware scanning...'

Then, I specify what I want:
'Generate a component specification including interface
definition, configuration schema, and error handling.'

Watch how the AI produces a structured specification..."

[SCENE: Reviewing output]
REVIEW:
"Let's review what it generated:
- Clean interface with ScanResult return type
- Comprehensive configuration options
- Error handling matrix
- Even suggested testing strategies"

[SCENE: Refinement prompt]
REFINEMENT:
"But we can improve this. I'll ask:
'Add connection pooling considerations and thread safety notes.'

The AI refines the specification with these additions..."

[SCENE: Instructor wrap-up]
CLOSE:
"This iterative approach - context, generate, review, refine -
is the key to effective AI-assisted architecture."
```

---

## Module 4: AI-Assisted Development

### Video 4.1: Implementing with AI Pair Programming
**Duration:** 15 minutes

```
[SCENE: IDE with split view]
INTRO:
"This is where agentic development really shines.
We'll implement the AntivirusScanService using AI pair programming."

[SCENE: Starting implementation]
STEP 1:
"I start by providing the specification we created in Module 3.
Then I ask for the implementation with specific requirements:
- OSGi service with configuration
- ClamAV TCP integration
- Mock mode for testing"

[SCENE: Code generation in progress]
GENERATION:
"Watch the AI generate the implementation...
Notice how it:
- Uses proper OSGi annotations
- Implements the ClamAV INSTREAM protocol
- Adds comprehensive error handling
- Includes logging at appropriate levels"

[SCENE: Reviewing generated code]
REVIEW:
"Now let's review critically. I'm looking for:
- Security issues
- Resource leaks
- AEM-specific patterns
- Missing edge cases"

[SCENE: Running build]
VERIFICATION:
"Let's verify it compiles:
'mvn compile -pl core'

Build successful. But we're not done - let's ask
for improvements..."

[SCENE: Refinement]
REFINEMENT:
"I ask: 'Add connection health checking and exponential backoff.'
The AI adds these features to our implementation."

[SCENE: Final code]
CLOSE:
"In 15 minutes, we have a production-quality service that would
have taken hours to write manually. In the lab, you'll do this
yourself for the workflow processes."
```

---

## Module 5: BEAD Task Management

### Video 5.1: Setting Up Persistent Memory
**Duration:** 8 minutes

```
[SCENE: Terminal]
INTRO:
"Let's set up BEAD for our project and see how it provides
persistent memory for AI agents."

[SCENE: Directory creation]
SETUP:
"First, we create the BEAD structure:
mkdir -p .bead/.issues
touch .bead/config.yaml

The config file defines our project settings, task types,
and agent assignments."

[SCENE: Creating tasks]
TASK CREATION:
"Now I'll create our epic and tasks.
Notice the hierarchical structure:
- Epic: Secure Asset Workflow (SAW-001)
- Feature: Antivirus Integration (SAW-010)
- Task: Implement Service (SAW-012)"

[SCENE: Session tracking demo]
SESSION TRACKING:
"When an agent starts work, it updates the session_log:
- timestamp
- agent name
- action taken
- progress notes

This creates a breadcrumb trail that persists across sessions."

[SCENE: Context loading simulation]
CONTEXT LOADING:
"When a new session starts, the agent loads:
- Its assigned tasks
- Their dependencies
- Related context files

This means it picks up exactly where it left off."

[SCENE: Instructor summary]
CLOSE:
"BEAD transforms AI from a stateless assistant to a team member
with memory. You'll practice this in Lab 5."
```

---

## Module 6: GasTown Multi-Agent Orchestration

### Video 6.1: Coordinating Agent Teams
**Duration:** 12 minutes

```
[SCENE: Agent network diagram]
INTRO:
"Now let's orchestrate multiple agents using GasTown.
We'll configure the Mayor AI and our specialized agents."

[SCENE: Configuration file]
CONFIGURATION:
"The gastown.yaml file defines our team:
- Mayor AI: The orchestrator
- aem-coder: Development specialist
- aem-tester: Testing specialist
- aem-reviewer: Code review specialist

Each agent has specific skills, tools, and context files."

[SCENE: Workflow definition]
WORKFLOW:
"Workflows define the sequence of agent activities:
1. Mayor receives task from BEAD
2. Assigns to aem-coder for implementation
3. On completion, triggers aem-tester
4. Tests pass? Trigger aem-reviewer
5. Review approved? Task complete"

[SCENE: Parallel execution visualization]
PARALLEL:
"The real power is parallel execution.
Instead of implementing three processes sequentially,
we spin up three coder agents simultaneously.

What would take 45 minutes sequentially
completes in 15 minutes with parallel agents."

[SCENE: Quality gates]
QUALITY GATES:
"Quality gates prevent bad code from progressing:
- code_complete: Build must succeed
- tests_pass: All tests green, coverage >80%
- review_approved: No blocking findings

Failed gates return work to the previous step."

[SCENE: Simulation run]
DEMO:
"Let me run a simulated workflow...
Watch the timeline as agents coordinate their work."

[SCENE: Instructor summary]
CLOSE:
"GasTown transforms development from a solo activity
to a coordinated team effort. Even though the team is AI."
```

---

## Module 7: Testing

### Video 7.1: AI-Generated Test Suites
**Duration:** 10 minutes

```
[SCENE: Test file structure]
INTRO:
"AI excels at generating comprehensive test suites.
Let's create tests for our AntivirusScanService."

[SCENE: Prompting for tests]
GENERATION:
"I provide the implementation and ask for tests:
'Generate JUnit 5 tests with Mockito for all scenarios:
- Clean file scanning
- Infected file detection
- Connection failures
- Timeout handling'

The AI generates test classes with descriptive names
and thorough assertions."

[SCENE: Running tests]
EXECUTION:
"Let's run them:
mvn test -pl core

All 12 tests passing. Let's check coverage..."

[SCENE: Coverage report]
COVERAGE:
"Opening the JaCoCo report...
AntivirusScanServiceImpl: 87% coverage.

The AI covered all major paths. Let's add edge cases..."

[SCENE: Edge case generation]
EDGE CASES:
"I ask for additional edge cases:
'Add tests for null inputs, zero-byte files, and concurrent access.'

The AI generates these additional scenarios."

[SCENE: Instructor summary]
CLOSE:
"AI-generated tests give you comprehensive coverage quickly.
But always review - AI might miss domain-specific edge cases
that require human insight."
```

---

## Module 8: Deployment

### Video 8.1: From Code to Production
**Duration:** 8 minutes

```
[SCENE: Build process]
INTRO:
"Our workflow is implemented and tested. Let's deploy it
to AEM and verify everything works end-to-end."

[SCENE: Maven build]
BUILD:
"Full build with deployment:
mvn clean install -PautoInstallSinglePackage

Bundle deployed successfully. Let's verify..."

[SCENE: OSGi console]
VERIFICATION:
"In the OSGi console, our bundle is Active.
The workflow model is visible in the console.
Launcher is configured for the upload paths."

[SCENE: End-to-end test]
E2E TEST:
"Let's test the complete workflow:
1. Upload a clean file - should pass through approval
2. Upload a 'virus_' file - should be quarantined

Both scenarios work as expected."

[SCENE: Monitoring setup]
MONITORING:
"Finally, we configure logging for production monitoring.
Debug logs for development, info/warn for production."

[SCENE: Course completion]
CLOSE:
"Congratulations! You've built a complete enterprise workflow
using agentic development. The techniques you've learned -
BMAD, BEAD, and GasTown - apply to any AEM project.

Complete the assessment to earn your certification.
Thank you for learning with us!"
```

---

## Video Production Checklist

### Pre-Production
- [ ] Script reviewed and timed
- [ ] Screen recordings planned
- [ ] Demo environment ready
- [ ] Code samples tested

### Production
- [ ] Clear audio recording
- [ ] 1080p screen capture
- [ ] Talking head segments
- [ ] No sensitive data visible

### Post-Production
- [ ] Edited for pacing
- [ ] Captions added
- [ ] Chapter markers set
- [ ] Thumbnail created
