# Knowledge Assessment Quiz
# Agentic Development for AEM

## Instructions
- Total Questions: 40
- Time Limit: 60 minutes
- Passing Score: 80% (32/40)
- Open book: No

---

## Section 1: BMAD Methodology (10 Questions)

### Q1. What does BMAD stand for?
- A) Build, Manage, Analyze, Deploy
- B) Breakthrough Method for Agile Development
- C) Business Model Architecture Design
- D) Build, Monitor, Adapt, Deliver

**Answer: B**
*Explanation: BMAD is the Breakthrough Method for Agile Development, an AI-driven framework for the software development lifecycle.*

---

### Q2. How many phases does BMAD have?
- A) 5
- B) 6
- C) 7
- D) 8

**Answer: C**
*Explanation: BMAD has 7 phases: 00-Initialization, 01-Discovery, 02-Models, 03-Architecture, 04-Development, 05-Testing, 06-Integrations (Operations).*

---

### Q3. In BMAD, what is produced during Phase 03 (Architecture)?
- A) User stories and personas
- B) Component specifications and diagrams
- C) Unit tests and coverage reports
- D) Deployment configurations

**Answer: B**
*Explanation: Phase 03 produces technical architecture including component specifications, integration contracts, and architecture diagrams.*

---

### Q4. Which phase would you be in when implementing a new OSGi service?
- A) Phase 01 - Discovery
- B) Phase 02 - Models
- C) Phase 03 - Architecture
- D) Phase 04 - Development

**Answer: D**
*Explanation: Phase 04 (Development Sprint) is when actual code implementation occurs.*

---

### Q5. What is the primary input to BMAD Phase 01 (Discovery)?
- A) Component specifications
- B) Business requirements and stakeholder interviews
- C) Unit test results
- D) Deployment scripts

**Answer: B**
*Explanation: Phase 01 focuses on business discovery through stakeholder interviews and requirement gathering.*

---

### Q6. Which BMAD phase produces the PRD?
- A) Phase 00 - Initialization
- B) Phase 01 - Discovery
- C) Phase 02 - Models
- D) Phase 03 - Architecture

**Answer: B**
*Explanation: The PRD (Product Requirements Document) is a key deliverable of Phase 01 (Business Discovery).*

---

### Q7. What type of AI agents are involved in BMAD?
- A) Only code generation agents
- B) Specialized agents for each phase (PM, Architect, Developer, QA)
- C) Only testing agents
- D) Only deployment agents

**Answer: B**
*Explanation: BMAD uses specialized AI agents for each phase: PM Agent, Architect Agent, Developer Agent, QA Agent.*

---

### Q8. In BMAD, when should content models be defined?
- A) Phase 00
- B) Phase 01
- C) Phase 02
- D) Phase 04

**Answer: C**
*Explanation: Phase 02 (Model Definition) is where content models and information architecture are defined.*

---

### Q9. What is the relationship between BMAD phases?
- A) They must be executed strictly in sequence
- B) They can be executed in any order
- C) They are iterative with potential loops back
- D) Each phase is independent

**Answer: C**
*Explanation: BMAD phases are iterative; findings in later phases may require revisiting earlier phases.*

---

### Q10. Which phase involves CI/CD pipeline setup?
- A) Phase 03 - Architecture
- B) Phase 04 - Development
- C) Phase 05 - Testing
- D) Phase 06/07 - Operations

**Answer: C or D**
*Explanation: CI/CD is part of Phase 05 (Testing and Deployment) with ongoing operations in Phase 06/07.*

---

## Section 2: BEAD Task Management (10 Questions)

### Q11. What is the primary purpose of BEAD?
- A) Code compilation
- B) Persistent memory for AI agents
- C) User interface design
- D) Database management

**Answer: B**
*Explanation: BEAD provides persistent memory so AI agents can maintain context across sessions.*

---

### Q12. What is the correct task hierarchy in BEAD?
- A) Task → Feature → Epic
- B) Epic → Task → Feature
- C) Epic → Feature → Task → Subtask
- D) Feature → Epic → Task

**Answer: C**
*Explanation: BEAD hierarchy is Epic → Feature → Task → Subtask (most general to most specific).*

---

### Q13. Where are BEAD tasks stored?
- A) In a cloud database
- B) In Git-backed YAML files
- C) In browser localStorage
- D) In environment variables

**Answer: B**
*Explanation: BEAD uses Git-backed storage (YAML files) for version control and collaboration.*

---

### Q14. Which field in a BEAD task tracks completion status?
- A) priority
- B) type
- C) status
- D) assignee

**Answer: C**
*Explanation: The status field tracks task state (pending, in_progress, blocked, completed).*

---

### Q15. What is recorded in a BEAD task's session_log?
- A) Only error messages
- B) Timestamps, agent actions, and progress notes
- C) Only final results
- D) User credentials

**Answer: B**
*Explanation: session_log records timestamps, agent names, actions taken, and progress notes for continuity.*

---

### Q16. How does an AI agent use BEAD at session start?
- A) Ignores previous work
- B) Loads relevant tasks and their context
- C) Deletes all pending tasks
- D) Creates new random tasks

**Answer: B**
*Explanation: Agents load their assigned tasks, dependencies, and context from BEAD at session start.*

---

### Q17. What should be recorded when a task is completed?
- A) Only the completion date
- B) Artifacts, actual hours, and final notes
- C) Only the task ID
- D) Nothing, just change status

**Answer: B**
*Explanation: Completed tasks should record artifacts (files created), actual hours, and final notes.*

---

### Q18. How are task dependencies tracked in BEAD?
- A) In a separate dependency file
- B) In the dependencies field with task IDs and status
- C) Through email notifications
- D) Dependencies are not tracked

**Answer: B**
*Explanation: Each task has a dependencies field listing required tasks by ID with their status.*

---

### Q19. What is the purpose of the BEAD index file?
- A) Storing user passwords
- B) Quick lookup of all tasks and their status
- C) Compiling code
- D) Managing network connections

**Answer: B**
*Explanation: The index.yaml provides a quick lookup of all tasks without parsing individual files.*

---

### Q20. When should acceptance_criteria status be updated?
- A) Only at task completion
- B) As each criterion is met during implementation
- C) Never, they are read-only
- D) Only by the project manager

**Answer: B**
*Explanation: Acceptance criteria should be marked as met progressively during implementation.*

---

## Section 3: GasTown Orchestration (10 Questions)

### Q21. What is the role of the Mayor AI in GasTown?
- A) Writing all code
- B) Orchestrating and coordinating specialized agents
- C) Only reviewing code
- D) Only testing code

**Answer: B**
*Explanation: The Mayor AI orchestrates all specialized agents, assigns tasks, and monitors progress.*

---

### Q22. Which agent would implement an OSGi service?
- A) aem-tester
- B) aem-coder
- C) aem-reviewer
- D) aem-docs

**Answer: B**
*Explanation: The aem-coder agent specializes in Java/OSGi development.*

---

### Q23. What are quality gates in GasTown?
- A) Physical barriers
- B) Conditions that must be met before proceeding
- C) User interface elements
- D) Network firewalls

**Answer: B**
*Explanation: Quality gates are conditions (e.g., tests pass, review approved) that must be met to proceed.*

---

### Q24. How does GasTown achieve faster development?
- A) By skipping testing
- B) By running multiple agents in parallel
- C) By ignoring code review
- D) By using faster computers

**Answer: B**
*Explanation: GasTown runs multiple specialized agents in parallel (e.g., implementing 3 components simultaneously).*

---

### Q25. What triggers the aem-tester agent to run?
- A) User manual request only
- B) Quality gate: code_complete
- C) Random schedule
- D) Never, testing is optional

**Answer: B**
*Explanation: The aem-tester is triggered when the code_complete quality gate is reached.*

---

### Q26. What happens when a quality gate fails?
- A) Workflow continues anyway
- B) Task returns to previous step for fixes
- C) System crashes
- D) All work is deleted

**Answer: B**
*Explanation: Failed gates typically return the task to a previous step (e.g., back to implementation for fixes).*

---

### Q27. How many agents can typically run in parallel?
- A) Only 1
- B) Exactly 2
- C) Configurable (default: 3-4)
- D) Unlimited

**Answer: C**
*Explanation: GasTown's max_parallel_agents is configurable, typically 3-4 for balanced resource usage.*

---

### Q28. What is fan-out / fan-in in GasTown?
- A) Hardware configuration
- B) Parallel execution pattern where work splits then merges
- C) Error handling strategy
- D) User authentication flow

**Answer: B**
*Explanation: Fan-out distributes work to parallel agents; fan-in aggregates their results.*

---

### Q29. How does GasTown handle agent failures?
- A) Ignores them
- B) Configurable retry with max_retries and delay
- C) Immediately stops all work
- D) Deletes the failed task

**Answer: B**
*Explanation: GasTown has configurable error handling with retry counts and delays.*

---

### Q30. What information does the aem-reviewer agent produce?
- A) Compiled code
- B) Review findings with severity levels
- C) User documentation
- D) Test results

**Answer: B**
*Explanation: The aem-reviewer produces review_findings with severity (critical, high, medium, low, info).*

---

## Section 4: AEM Workflow Development (10 Questions)

### Q31. Which interface must a workflow process step implement?
- A) Servlet
- B) WorkflowProcess
- C) SlingModel
- D) ResourceProvider

**Answer: B**
*Explanation: Workflow process steps must implement com.adobe.granite.workflow.exec.WorkflowProcess.*

---

### Q32. How do you mark a process step for auto-advance?
- A) Set PROCESS_AUTO_ADVANCE="true" in metadata
- B) Return true from execute()
- C) Use @AutoAdvance annotation
- D) It's always automatic

**Answer: A**
*Explanation: In the workflow model, set PROCESS_AUTO_ADVANCE="true" in the step's metaData.*

---

### Q33. What is a ParticipantStepChooser used for?
- A) Choosing file types
- B) Dynamically selecting which user/group receives a task
- C) Selecting database connections
- D) Choosing log levels

**Answer: B**
*Explanation: ParticipantStepChooser dynamically determines the participant (user/group) for a task.*

---

### Q34. Where are workflow models stored in AEM Cloud Service?
- A) /apps/workflow/models
- B) /conf/global/settings/workflow/models
- C) /etc/workflow/models
- D) /content/workflow/models

**Answer: B**
*Explanation: In AEM Cloud Service, editable workflow models are stored under /conf/.*

---

### Q35. What triggers a workflow launcher?
- A) User button click only
- B) JCR events matching configured conditions
- C) Scheduled timer only
- D) Email receipt

**Answer: B**
*Explanation: Workflow launchers trigger on JCR events (create, modify, delete) matching path patterns.*

---

### Q36. How do you inject an OSGi service into a workflow process?
- A) Using @Inject
- B) Using @Reference
- C) Using new ServiceImpl()
- D) Using getService()

**Answer: B**
*Explanation: OSGi services are injected using @Reference annotation (Declarative Services).*

---

### Q37. What is the ClamAV INSTREAM command used for?
- A) Downloading virus definitions
- B) Streaming file content for scanning
- C) Configuring the scanner
- D) Checking scanner version

**Answer: B**
*Explanation: INSTREAM allows streaming file content to ClamAV for scanning without saving to disk.*

---

### Q38. What indicates an infected file in ClamAV response?
- A) "OK" at the end
- B) "FOUND" at the end
- C) "CLEAN" at the end
- D) Empty response

**Answer: B**
*Explanation: ClamAV responses ending in "FOUND" indicate detected malware (e.g., "stream: Eicar-Test-Signature FOUND").*

---

### Q39. How should workflow metadata be accessed?
- A) workItem.getMetaDataMap()
- B) workItem.getWorkflow().getWorkflowData().getMetaDataMap()
- C) System.getProperty()
- D) session.getAttribute()

**Answer: B**
*Explanation: Workflow-level metadata is accessed via workItem.getWorkflow().getWorkflowData().getMetaDataMap().*

---

### Q40. What type of node is used for branching in workflow models?
- A) PROCESS
- B) PARTICIPANT
- C) OR_SPLIT
- D) END

**Answer: C**
*Explanation: OR_SPLIT nodes are used for conditional branching based on metadata values.*

---

## Answer Key

| Q | Answer | Q | Answer | Q | Answer | Q | Answer |
|---|--------|---|--------|---|--------|---|--------|
| 1 | B | 11 | B | 21 | B | 31 | B |
| 2 | C | 12 | C | 22 | B | 32 | A |
| 3 | B | 13 | B | 23 | B | 33 | B |
| 4 | D | 14 | C | 24 | B | 34 | B |
| 5 | B | 15 | B | 25 | B | 35 | B |
| 6 | B | 16 | B | 26 | B | 36 | B |
| 7 | B | 17 | B | 27 | C | 37 | B |
| 8 | C | 18 | B | 28 | B | 38 | B |
| 9 | C | 19 | B | 29 | B | 39 | B |
| 10 | C/D | 20 | B | 30 | B | 40 | C |

---

## Scoring Guide

| Score | Grade | Result |
|-------|-------|--------|
| 36-40 | A | Excellent - Certified |
| 32-35 | B | Good - Certified |
| 28-31 | C | Pass - Review recommended |
| <28 | F | Retake required |
