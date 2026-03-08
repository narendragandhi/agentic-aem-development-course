# Lab 8: Capstone Project (4 hours)

## Objective
Apply all learned concepts to build a complete feature end-to-end: from PRD to deployed, tested code using BMAD, BEAD, GasTown, and TDD methodologies.

---

## Project: Automated Content Classification System

Build a system that automatically classifies uploaded assets and routes them through appropriate workflows based on content type, sensitivity, and compliance requirements.

---

## Part 1: Requirements (30 min)

### 1.1 Write the PRD

Create `capstone/content-classifier-prd.md`:

```markdown
# Content Classification System PRD

## Problem Statement
Content authors upload diverse assets without consistent tagging, making content discovery and compliance difficult.

## Solution
An automated classification system that:
- Analyzes uploaded content using AI/ML patterns
- Applies appropriate tags and categories
- Routes to compliance review when needed
- Integrates with existing security scanning

## User Stories

### US-1: Auto-Classification
**As a** content author
**I want** my uploads automatically classified
**So that** I don't need to manually tag every asset

**Acceptance Criteria:**
- [ ] Classification runs within 10 seconds of upload
- [ ] At least 3 categories assigned per asset
- [ ] Confidence score provided for each classification

### US-2: Compliance Routing
**As a** compliance officer
**I want** sensitive content flagged automatically
**So that** I can review before publication

**Acceptance Criteria:**
- [ ] PII detection routes to compliance queue
- [ ] Legal content identified and tagged
- [ ] Healthcare content flagged for HIPAA review

## Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1 | Classify images by content | P0 |
| FR-2 | Classify documents by topic | P0 |
| FR-3 | Detect sensitive categories | P0 |
| FR-4 | Apply DAM tags automatically | P1 |
| FR-5 | Route to appropriate workflow | P1 |
| FR-6 | Provide classification confidence | P2 |
```

### 1.2 Define Domain Model

```java
// Classification Categories
public enum ContentCategory {
    MARKETING, LEGAL, TECHNICAL, MEDICAL,
    FINANCIAL, HR, GENERAL
}

public enum SensitivityLevel {
    PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
}

// Classification Result
@ValueObject
public class ClassificationResult {
    private final List<CategoryScore> categories;
    private final SensitivityLevel sensitivity;
    private final List<String> suggestedTags;
    private final boolean requiresReview;
}

public record CategoryScore(
    ContentCategory category,
    double confidence
) {}
```

---

## Part 2: Architecture (30 min)

### 2.1 Component Design

```
┌─────────────────────────────────────────────────────────────────┐
│                 CONTENT CLASSIFICATION SYSTEM                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │  Classifier  │───▶│   Router     │───▶│   Tagger     │      │
│  │   Service    │    │   Service    │    │   Service    │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│         │                   │                   │               │
│         └───────────────────┴───────────────────┘               │
│                             │                                    │
│                    ┌────────▼────────┐                          │
│                    │ Classification  │                          │
│                    │    Workflow     │                          │
│                    └─────────────────┘                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Service Interfaces

```java
public interface ContentClassifierService {
    ClassificationResult classify(Asset asset);
    List<CategoryScore> classifyText(String content);
    List<CategoryScore> classifyImage(InputStream image);
}

public interface ClassificationRouterService {
    WorkflowAction determineRoute(ClassificationResult result);
    void applyTags(Asset asset, ClassificationResult result);
}
```

---

## Part 3: TDD Implementation (2 hours)

### 3.1 Write Specification Tests First

```java
@DisplayName("ContentClassifierService Specification")
class ContentClassifierServiceSpec {

    private ContentClassifierService service;

    @BeforeEach
    void setUp() {
        service = new ContentClassifierServiceImpl();
    }

    @Nested
    @DisplayName("Text Classification")
    class TextClassification {

        @Test
        @DisplayName("should classify legal content")
        void shouldClassifyLegalContent() {
            String content = "This agreement constitutes a binding contract...";

            List<CategoryScore> scores = service.classifyText(content);

            assertTrue(scores.stream()
                .anyMatch(s -> s.category() == ContentCategory.LEGAL
                    && s.confidence() > 0.7));
        }

        @Test
        @DisplayName("should classify medical content")
        void shouldClassifyMedicalContent() {
            String content = "Patient diagnosis indicates treatment protocol...";

            List<CategoryScore> scores = service.classifyText(content);

            assertTrue(scores.stream()
                .anyMatch(s -> s.category() == ContentCategory.MEDICAL));
        }

        @Test
        @DisplayName("should detect PII and mark as restricted")
        void shouldDetectPii() {
            String content = "SSN: 123-45-6789, Credit Card: 4111-1111-1111-1111";

            List<CategoryScore> scores = service.classifyText(content);

            // Should trigger sensitivity detection
            // Implementation will mark as RESTRICTED
        }
    }

    @Nested
    @DisplayName("Routing Decisions")
    class RoutingDecisions {

        @Test
        @DisplayName("should route medical content to HIPAA review")
        void shouldRouteMedicalToHipaa() {
            ClassificationResult result = ClassificationResult.builder()
                .addCategory(ContentCategory.MEDICAL, 0.9)
                .sensitivity(SensitivityLevel.CONFIDENTIAL)
                .build();

            ClassificationRouterService router =
                new ClassificationRouterServiceImpl();

            WorkflowAction action = router.determineRoute(result);

            assertEquals(WorkflowAction.COMPLIANCE_REVIEW, action);
        }

        @Test
        @DisplayName("should auto-approve public marketing content")
        void shouldAutoApprovePublicMarketing() {
            ClassificationResult result = ClassificationResult.builder()
                .addCategory(ContentCategory.MARKETING, 0.85)
                .sensitivity(SensitivityLevel.PUBLIC)
                .build();

            ClassificationRouterService router =
                new ClassificationRouterServiceImpl();

            WorkflowAction action = router.determineRoute(result);

            assertEquals(WorkflowAction.AUTO_APPROVE, action);
        }
    }
}
```

### 3.2 Implement Services

```java
@Component(service = ContentClassifierService.class, immediate = true)
public class ContentClassifierServiceImpl implements ContentClassifierService {

    private static final Logger LOG = LoggerFactory.getLogger(
        ContentClassifierServiceImpl.class);

    // Keyword patterns for classification
    private static final Map<ContentCategory, List<Pattern>> CATEGORY_PATTERNS =
        Map.of(
            ContentCategory.LEGAL, Arrays.asList(
                Pattern.compile("\\b(contract|agreement|liability|clause)\\b",
                    Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(terms|conditions|warranty|indemnif)\\b",
                    Pattern.CASE_INSENSITIVE)
            ),
            ContentCategory.MEDICAL, Arrays.asList(
                Pattern.compile("\\b(patient|diagnosis|treatment|prescription)\\b",
                    Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(medical|clinical|healthcare|HIPAA)\\b",
                    Pattern.CASE_INSENSITIVE)
            ),
            ContentCategory.FINANCIAL, Arrays.asList(
                Pattern.compile("\\b(revenue|profit|fiscal|quarterly)\\b",
                    Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(investment|portfolio|dividend)\\b",
                    Pattern.CASE_INSENSITIVE)
            )
        );

    @Override
    public List<CategoryScore> classifyText(String content) {
        List<CategoryScore> scores = new ArrayList<>();

        for (Map.Entry<ContentCategory, List<Pattern>> entry :
                CATEGORY_PATTERNS.entrySet()) {

            int matches = 0;
            for (Pattern pattern : entry.getValue()) {
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    matches++;
                }
            }

            if (matches > 0) {
                double confidence = Math.min(0.5 + (matches * 0.1), 1.0);
                scores.add(new CategoryScore(entry.getKey(), confidence));
            }
        }

        // Sort by confidence descending
        scores.sort((a, b) -> Double.compare(b.confidence(), a.confidence()));

        return scores;
    }

    @Override
    public ClassificationResult classify(Asset asset) {
        // Extract text content from asset
        String textContent = extractText(asset);

        List<CategoryScore> categories = classifyText(textContent);
        SensitivityLevel sensitivity = detectSensitivity(textContent);
        List<String> tags = generateTags(categories);
        boolean requiresReview = sensitivity.ordinal() >=
            SensitivityLevel.CONFIDENTIAL.ordinal();

        return new ClassificationResult(categories, sensitivity, tags, requiresReview);
    }

    private SensitivityLevel detectSensitivity(String content) {
        // PII patterns
        if (content.matches(".*\\d{3}-\\d{2}-\\d{4}.*") ||  // SSN
            content.matches(".*\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}.*")) {  // CC
            return SensitivityLevel.RESTRICTED;
        }
        return SensitivityLevel.PUBLIC;
    }

    private List<String> generateTags(List<CategoryScore> categories) {
        return categories.stream()
            .filter(c -> c.confidence() > 0.6)
            .map(c -> "classification:" + c.category().name().toLowerCase())
            .collect(Collectors.toList());
    }
}
```

### 3.3 Create Workflow Process

```java
@Component(
    service = WorkflowProcess.class,
    property = {"process.label=Content Classification Process"}
)
public class ContentClassificationProcess implements WorkflowProcess {

    @Reference
    private ContentClassifierService classifier;

    @Reference
    private ClassificationRouterService router;

    @Override
    public void execute(WorkItem workItem, WorkflowSession session,
                        MetaDataMap args) throws WorkflowException {

        String payloadPath = workItem.getWorkflowData()
            .getPayload().toString();

        try {
            ResourceResolver resolver = session.adaptTo(ResourceResolver.class);
            Resource resource = resolver.getResource(payloadPath);
            Asset asset = resource.adaptTo(Asset.class);

            // Classify
            ClassificationResult result = classifier.classify(asset);

            // Apply tags
            router.applyTags(asset, result);

            // Determine routing
            WorkflowAction action = router.determineRoute(result);

            // Store result in workflow metadata
            workItem.getWorkflowData().getMetaDataMap()
                .put("classificationResult", result);

            if (action == WorkflowAction.COMPLIANCE_REVIEW) {
                // Route to compliance queue
                session.complete(workItem,
                    session.getRoutes(workItem, false).get(0));
            }

        } catch (Exception e) {
            throw new WorkflowException("Classification failed", e);
        }
    }
}
```

---

## Part 4: Integration (45 min)

### 4.1 Create BEAD Task File

```yaml
# capstone/CLASSIFY-001.yaml
task_id: CLASSIFY-001
title: "Implement Content Classification System"
status: completed

tdd:
  spec_file: "ContentClassifierServiceSpec.java"
  test_count: 12
  phase: green

build:
  interface: "ContentClassifierService.java"
  implementation: "ContentClassifierServiceImpl.java"
  workflow_process: "ContentClassificationProcess.java"

execute:
  command: "mvn test -pl core -Dtest=ContentClassifier*"
  result: "12 passing"

analyze:
  coverage: "88%"
  complexity: "Medium"
  security_integration: true

document:
  prd: "content-classifier-prd.md"
  architecture: "classification-architecture.md"
  javadoc: true
```

### 4.2 Agent Orchestration

```java
// Define classification workflow
WorkflowDefinition classifyWorkflow = WorkflowDefinition.builder("Classification")
    .description("Automated content classification pipeline")
    .addStep(WorkflowStep.of(1, "aem-spec-writer", "Write classifier specs"))
    .addStep(WorkflowStep.checkpoint(2, "test-runner", "Verify RED phase"))
    .addStep(WorkflowStep.of(3, "aem-developer", "Implement classifier"))
    .addStep(WorkflowStep.checkpoint(4, "test-runner", "Verify GREEN phase"))
    .addStep(WorkflowStep.of(5, "code-reviewer", "Review implementation"))
    .addStep(WorkflowStep.of(6, "doc-writer", "Generate documentation"))
    .build();

WorkflowExecution execution = orchestrator.executeWorkflow(classifyWorkflow);
```

---

## Part 5: Quality & Deployment (15 min)

### 5.1 Run Quality Checks

```bash
# Full quality suite
mvn clean verify -Pquality -pl core

# Expected output:
# - Checkstyle: 0 violations
# - SpotBugs: 0 bugs
# - PMD: 0 violations
# - JaCoCo: >80% coverage
# - Tests: All passing
```

### 5.2 Final Verification

```bash
# Run all tests
mvn test -pl core
# Expected: 165+ tests passing

# Build complete package
mvn clean install
# Expected: BUILD SUCCESS
```

---

## Deliverables Checklist

- [ ] PRD document completed
- [ ] Domain models defined
- [ ] Architecture diagram created
- [ ] 12+ specification tests written
- [ ] Services implemented and tested
- [ ] Workflow process created
- [ ] BEAD task file documented
- [ ] Agent workflow defined
- [ ] Quality checks passing
- [ ] Code coverage >80%
- [ ] Javadoc generated

---

## Grading Rubric

| Criteria | Points |
|----------|--------|
| PRD completeness | 10 |
| Domain model design | 10 |
| TDD adherence (RED-GREEN-REFACTOR) | 20 |
| Test coverage | 15 |
| Code quality (linters passing) | 15 |
| Integration with security scanner | 10 |
| Agent orchestration usage | 10 |
| Documentation quality | 10 |
| **Total** | **100** |

---

## Congratulations!

You have completed the Agentic AEM Development Course. You now have skills in:

- **BMAD**: 7-phase methodology for structured development
- **BEAD**: Task tracking with Build, Execute, Analyze, Document
- **GasTown**: Multi-agent orchestration for TDD workflows
- **TDD**: RED-GREEN-REFACTOR cycle with specification tests
- **Security**: OWASP patterns, document scanning, threat detection
- **Quality**: Linting, coverage, documentation standards
- **AEM**: Workflow development, OSGi services, Cloud deployment

---

## Continue Learning

- Explore advanced agent patterns
- Add ML-based classification
- Implement real antivirus integration
- Build custom workflow UIs
- Contribute to the course repository
