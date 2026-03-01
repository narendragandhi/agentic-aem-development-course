# Instructor Facilitation Guide
# Agentic Development for AEM Course

## Course Delivery Overview

| Aspect | Recommendation |
|--------|----------------|
| Format | Blended (lecture + hands-on) |
| Class Size | 8-16 participants |
| Duration | 4 days (intensive) or 2 weeks (part-time) |
| Instructor : Student | 1:8 for labs |

---

## Day-by-Day Schedule

### Day 1: Foundations (6 hours)

| Time | Topic | Format | Materials |
|------|-------|--------|-----------|
| 9:00-9:30 | Welcome & Introductions | Lecture | Slides |
| 9:30-10:30 | What is Agentic Development? | Lecture + Discussion | Slides, Demos |
| 10:30-10:45 | Break | | |
| 10:45-12:00 | BMAD-BEAD-GasTown Overview | Lecture | Diagrams |
| 12:00-13:00 | Lunch | | |
| 13:00-14:30 | Lab 1: Environment Setup | Hands-on | Lab guide |
| 14:30-14:45 | Break | | |
| 14:45-16:00 | Case Study: Secure Asset Workflow | Lecture | PRD |
| 16:00-16:30 | Q&A and Day 1 Wrap-up | Discussion | |

### Day 2: Requirements & Architecture (6 hours)

| Time | Topic | Format | Materials |
|------|-------|--------|-----------|
| 9:00-10:30 | Writing AI-Optimized PRDs | Lecture + Exercise | PRD template |
| 10:30-10:45 | Break | | |
| 10:45-12:00 | Lab 2: PRD Creation | Hands-on | Lab guide |
| 12:00-13:00 | Lunch | | |
| 13:00-14:30 | BMAD Architecture Phase | Lecture | Phase 03 doc |
| 14:30-14:45 | Break | | |
| 14:45-16:00 | Lab 3: Architecture Design with AI | Hands-on | Lab guide |
| 16:00-16:30 | Review & Discussion | Group | |

### Day 3: Development (6 hours)

| Time | Topic | Format | Materials |
|------|-------|--------|-----------|
| 9:00-10:00 | AI Pair Programming Best Practices | Lecture | Examples |
| 10:00-10:15 | Break | | |
| 10:15-12:00 | Lab 4: AI-Assisted Development | Hands-on | Lab guide |
| 12:00-13:00 | Lunch | | |
| 13:00-14:30 | Lab 4: Continued | Hands-on | |
| 14:30-14:45 | Break | | |
| 14:45-16:00 | BEAD Task Management | Lecture + Demo | |
| 16:00-16:30 | Lab 5: BEAD Tracking | Hands-on | Lab guide |

### Day 4: Orchestration & Deployment (6 hours)

| Time | Topic | Format | Materials |
|------|-------|--------|-----------|
| 9:00-10:30 | GasTown Multi-Agent Orchestration | Lecture | Agent docs |
| 10:30-10:45 | Break | | |
| 10:45-12:00 | Lab 6: GasTown Configuration | Hands-on | Lab guide |
| 12:00-13:00 | Lunch | | |
| 13:00-14:30 | Lab 7: Testing & Review | Hands-on | Lab guide |
| 14:30-14:45 | Break | | |
| 14:45-15:30 | Lab 8: Deployment | Hands-on | Lab guide |
| 15:30-16:30 | Assessment & Course Wrap-up | Assessment | Quiz, Project |

---

## Teaching Tips

### Module 1: Introduction

**Key Messages:**
- Agentic AI is about autonomous task completion, not just chat
- BMAD provides structure, BEAD provides memory, GasTown provides coordination
- Humans remain in the loop for critical decisions

**Common Questions:**
1. "Will AI replace developers?"
   - *Answer*: No, it augments and accelerates. Critical thinking, architecture decisions, and creative problem-solving remain human responsibilities.

2. "How is this different from Copilot?"
   - *Answer*: Copilot assists line-by-line. Agentic systems complete entire tasks autonomously with reasoning and tool use.

### Module 2: PRD Creation

**Exercise Setup:**
Have students work in pairs to critique and improve the sample PRD.

**Evaluation Criteria:**
- User stories follow proper format
- Acceptance criteria are testable
- Non-functional requirements are measurable
- Architecture section is complete

### Module 3: Architecture

**Live Demo:**
Show AI generating a component specification in real-time:
1. Provide context from PRD
2. Ask for interface design
3. Request implementation considerations
4. Iterate on feedback

**Common Pitfalls:**
- Students accepting AI output without review
- Missing error handling considerations
- Ignoring AEM-specific patterns

### Module 4: Development

**Lab Monitoring:**
Walk around and observe students:
- Are they providing sufficient context to AI?
- Are they reviewing generated code critically?
- Are they testing incrementally?

**Intervention Points:**
- If code doesn't compile, help debug OSGi annotations
- If tests fail, review mock setup
- If deployment fails, check bundle status

### Module 5: BEAD

**Demonstration:**
Show the BEAD workflow:
```bash
# Create task
bd create task "Implement feature X"

# Start work
bd start <task-id>

# Update progress
bd update <task-id> --note "Completed interface"

# Complete
bd complete <task-id>
```

**Emphasize:**
- BEAD is the AI's persistent memory
- Without it, context is lost between sessions
- Tasks should be granular enough to complete in one session

### Module 6: GasTown

**Group Exercise:**
Have the class collectively design a workflow:
1. Break into roles (Mayor, Coder, Tester, Reviewer)
2. Walk through task assignment
3. Simulate handoffs and quality gates

**Key Concepts:**
- Mayor AI orchestrates, doesn't implement
- Specialized agents have focused capabilities
- Quality gates prevent bad code from progressing

---

## Assessment Rubrics

### Lab Completion (60%)

| Criteria | Points | Description |
|----------|--------|-------------|
| Lab 1-3 Complete | 15 | Environment, PRD, Architecture |
| Lab 4 Complete | 20 | All workflow components implemented |
| Lab 5-6 Complete | 15 | BEAD and GasTown configured |
| Lab 7-8 Complete | 10 | Tests passing, deployed |

### Knowledge Quiz (20%)

| Topic | Questions | Points |
|-------|-----------|--------|
| BMAD Methodology | 5 | 5 |
| BEAD Concepts | 5 | 5 |
| GasTown Agents | 5 | 5 |
| AEM Workflow | 5 | 5 |

### Code Review (20%)

| Criteria | Points |
|----------|--------|
| Code quality | 5 |
| AEM best practices | 5 |
| Error handling | 5 |
| Documentation | 5 |

---

## Common Issues & Solutions

### Technical Issues

| Issue | Solution |
|-------|----------|
| AEM won't start | Check Java version, port conflicts |
| ClamAV not responding | Wait 2-3 min for virus DB download |
| Bundle won't activate | Check OSGi console for missing imports |
| Workflow not triggering | Verify launcher path pattern |

### Learning Issues

| Issue | Solution |
|-------|----------|
| Student over-relying on AI | Require manual code review step |
| Student rejecting AI suggestions | Show successful examples |
| Context confusion | Review BMAD-BEAD-GasTown diagram |
| Falling behind | Provide completed code checkpoints |

---

## Participant Prerequisites Verification

Before the course, verify:

```bash
# Java version
java -version  # Should be 11+

# Maven version
mvn -version  # Should be 3.8+

# Docker
docker --version  # Required for ClamAV

# AEM SDK
ls ~/aem-sdk/*.jar  # Should have quickstart

# AI Tool
claude --version  # Or equivalent AI assistant
```

---

## Post-Course Resources

Share with participants:
1. GitHub repository with all course materials
2. BMAD documentation links
3. AEM Cloud Service documentation
4. Community Slack/Discord channel
5. Office hours schedule for follow-up questions

---

## Feedback Collection

End-of-course survey questions:
1. How would you rate the course overall? (1-5)
2. Which module was most valuable?
3. Which module needs improvement?
4. Was the pace appropriate?
5. Would you recommend this course?
6. What additional topics would you like covered?
