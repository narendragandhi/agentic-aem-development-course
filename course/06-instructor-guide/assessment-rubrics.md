# Assessment Rubrics
# Agentic Development for AEM Course

---

## Overall Grading

| Component | Weight | Points |
|-----------|--------|--------|
| Lab Completion | 50% | 50 |
| Knowledge Quiz | 20% | 20 |
| Code Review | 20% | 20 |
| Participation | 10% | 10 |
| **Total** | 100% | **100** |

---

## Grade Scale

| Points | Grade | Description |
|--------|-------|-------------|
| 90-100 | A | Expert - Exceeds expectations |
| 80-89 | B | Proficient - Meets all expectations |
| 70-79 | C | Competent - Meets most expectations |
| 60-69 | D | Developing - Needs improvement |
| <60 | F | Not meeting minimum requirements |

---

## Lab Completion Rubric (50 points)

### Lab 1: Environment Setup (5 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| Complete setup | 3 | All tools installed and working |
| Build verification | 1 | `mvn clean install` succeeds |
| Git initialized | 1 | Repository configured |

### Lab 2: PRD Creation (5 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| PRD structure | 2 | All required sections present |
| User stories | 2 | Complete user stories with AC |
| AI optimization | 1 | Requirements are measurable |

### Lab 3: Architecture (8 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| Domain models | 3 | All entities defined with relationships |
| Component specs | 3 | Service interfaces defined |
| Diagrams | 2 | Architecture diagrams created |

### Lab 4: Development (10 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| Code quality | 3 | Clean, well-organized code |
| TDD adherence | 3 | Spec tests written first |
| AEM patterns | 2 | Proper OSGi, Sling usage |
| Documentation | 2 | Javadoc and comments |

### Lab 5: BEAD Tracking (5 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| Task hierarchy | 2 | Epic → Feature → Task |
| Status tracking | 2 | All updates logged |
| Dependencies | 1 | Dependencies documented |

### Lab 6: GasTown (5 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| Configuration | 2 | gastown.yaml complete |
| Workflow defined | 2 | Implementation workflow created |
| Agent setup | 1 | Agents configured |

### Labs 7-8: Testing & Deployment (12 points)

| Criteria | Points | Description |
|----------|--------|-------------|
| Test coverage | 4 | >80% coverage achieved |
| Tests passing | 4 | All tests pass |
| Deployment | 2 | Package deploys successfully |
| Smoke tests | 2 | End-to-end verification |

---

## Knowledge Quiz Rubric (20 points)

### Scoring

| Score | Points |
|-------|--------|
| 40/40 | 20 |
| 36-39 | 18 |
| 32-35 | 16 |
| 28-31 | 14 |
| 24-27 | 12 |
| 20-23 | 10 |
| <20 | 8 |

### Quiz Topics

| Topic | Questions |
|-------|-----------|
| BMAD Methodology | 10 |
| BEAD Task Management | 10 |
| GasTown Orchestration | 10 |
| AEM Workflow Development | 10 |

---

## Code Review Rubric (20 points)

### Criteria

| Criterion | Weight | Description |
|-----------|--------|-------------|
| Code Quality | 5 | Clean code, proper naming, no duplication |
| AEM Best Practices | 5 | OSGi annotations, Sling Models, HTL |
| Error Handling | 4 | Proper exception handling, logging |
| Security | 4 | Input validation, sanitization |
| Documentation | 2 | Javadoc, comments where needed |

### Code Review Checklist

```
☐ Code Quality
  ☐ Variable/method names are descriptive
  ☐ No magic numbers
  ☐ Single Responsibility Principle followed
  ☐ No code duplication
  
☐ AEM Best Practices
  ☐ @Component annotation used correctly
  ☐ @Reference for service injection
  ☐ Sling Models follow conventions
  ☐ HTL used for views
  
☐ Error Handling
  ☐ Exceptions caught and logged
  ☐ Null checks where needed
  ☐ Timeout handling for external calls
  ☐ Proper error messages
  
☐ Security
  ☐ Input validation
  ☐ XSS prevention
  ☐ SQL injection prevention
  ☐ No hardcoded credentials
  
☐ Documentation
  ☐ Public methods have Javadoc
  ☐ Complex logic has comments
  ☐ README updated if needed
```

---

## Participation Rubric (10 points)

| Level | Points | Description |
|-------|--------|-------------|
| Excellent | 10 | Active participation, helps others |
| Good | 8 | Regular participation |
| Adequate | 6 | Occasional participation |
| Poor | 4 | Rare participation |
| None | 0 | No participation |

---

## Capstone Project Assessment (100 points)

### Deliverables

| Deliverable | Points |
|-------------|--------|
| PRD Document | 15 |
| Domain Models | 10 |
| Architecture Documentation | 10 |
| Implementation (code) | 25 |
| Unit Tests | 15 |
| Integration Tests | 10 |
| Deployment | 10 |
| Presentation | 5 |

### Code Implementation Rubric (25 points)

| Criteria | Points |
|----------|--------|
| Complete functionality | 15 |
| Code quality | 5 |
| TDD approach | 5 |

### Test Rubric (25 points)

| Criteria | Points |
|----------|--------|
| Coverage > 80% | 8 |
| Tests passing | 8 |
| Test quality | 9 |

---

## Retake Policy

Students scoring below 70% must:
1. Review course materials
2. Complete additional exercises
3. Retake assessment

---

## Bonus Points

Opportunities for bonus points:
- Complete extra lab exercises (+5)
- Contribute course improvements (+5)
- Help fellow students (+3)
- Complete course early (+2)

---

## Regrade Requests

To request a regrade:
1. Submit in writing within 48 hours
2. Explain specific concern
3. Provide supporting evidence
