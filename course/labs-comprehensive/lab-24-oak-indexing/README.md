# Lab 24: Oak Indexing with Agentic AI
# Comprehensive Lab - 4 hours

## Objective

Use AI agents to analyze query patterns, generate Oak indexes, and validate performance using BMAD methodology with TDD.

---

## Prerequisites

- Lab 18 (Goose AI) completed
- Lab 19 (Functional/Regression) completed

---

## BMAD Phase Context

```
BMAD Integration:
├── Phase 01: AI analyzes query requirements from PRD
├── Phase 02: AI identifies query patterns from logs
├── Phase 03: AI designs index strategy
├── Phase 04: AI generates index definitions (TDD)
├── Phase 05: AI validates query performance
└── Phase 06: AI monitors index health
```

---

## Part 1: AI Query Analysis (Phase 01-02) - 30 min

### 1.1 Set Up BEAD Task for AI

```bash
# Create BEAD task for indexing
bead create --epic SAW --task "Oak Index Implementation" --agent ai-coder
```

### 1.2 AI Analyzes Query Requirements

**Prompt to Goose/Claude:**

```
Analyze this PRD and identify all query patterns that will require custom indexes:

From PRD (secure-asset-workflow-prd.md):
- Query assets by scanStatus (PENDING, CLEAN, INFECTED)
- Query assets by workflowId
- Query quarantine by date range
- Query assets by assignee
- Full-text search in asset metadata

For each query pattern:
1. Identify the query type (XPATH, JCR-SQL2, SQL)
2. Estimate result set size
3. Determine performance SLA
4. Recommend index type (Property vs Lucene)
```

### 1.3 AI Generates Query Analysis Report

```yaml
# .bead/tasks/saw-index-analysis.yaml
analysis:
  query_patterns:
    - id: Q1
      description: "Find all infected assets"
      query: "/jcr:root/content/dam//*[@scanStatus='INFECTED']"
      type: XPATH
      sla_ms: 1000
      index_recommendation: "Lucene - ordered property"
      
    - id: Q2
      description: "Find assets by workflow"
      query: "SELECT * FROM dam:Asset WHERE workflowId IS NOT NULL"
      type: JCR_SQL2
      sla_ms: 500
      index_recommendation: "Property index"
      
    - id: Q3
      description: "Quarantine by date"
      query: "/jcr:root/var/demo/quarantine/*[@quarantined > $DATE]"
      type: XPATH
      sla_ms: 500
      index_recommendation: "Lucene - ordered date"

  recommendations:
    - Create composite index for scanStatus + quarantined
    - Add property index for workflowId
    - Use Lucene for date range queries
```

---

## Part 2: AI Designs Index Strategy (Phase 03) - 30 min

### 2.1 AI Generates Index Specifications

```bash
# Use Goose to generate index specs
goose run --task "Design Oak indexes for the following query patterns:

1. Query: /jcr:root/content/dam//*[@scanStatus='INFECTED']
   - Need: scanStatus property index
   
2. Query: SELECT * FROM dam:Asset WHERE workflowId IS NOT NULL  
   - Need: workflowId property index
   
3. Query: /jcr:root/var/demo/quarantine/*[@quarantined > $DATE]
   - Need: Date index with ordering

Generate:
1. Index definitions in XML format (oak:IndexDefinition)
2. Property configurations
3. Aggregate rules for dam:Asset
4. Cost estimation

Use Lucene index type for best performance."
```

### 2.2 AI Creates Index Architecture

```yaml
# .bead/deliverables/index-architecture.yaml
index_design:
  primary_indexes:
    - name: demo-scan-status-index
      type: lucene
      rules:
        dam:Asset:
          properties:
            scanStatus: { ordered: true }
            scanResult: { }
            quarantined: { ordered: true }
            
    - name: demo-workflow-index
      type: property
      properties:
        - workflowId
        - workflowStatus
        - workflowAssignee
        
  aggregate_rules:
    dam:Asset:
      - jcr:content
      - jcr:content/metadata
```

---

## Part 3: AI-Driven TDD for Indexes (Phase 04) - 60 min

### 3.1 AI Writes Test Specs (RED)

```bash
goose run --task "Write JUnit tests for Oak indexes in src/test/java:

1. IndexSpecs.java - Performance tests that MUST pass:
   - testQueryByScanStatusUsesIndex()
   - testQueryByScanStatusSLA()
   - testWorkflowQueryPerformance()
   - testQuarantineDateRangeQuery()

2. IndexIntegrationTest.java:
   - testIndexExists()
   - testIndexIsValid()
   - testIndexedDocumentCount()

Use AEM Mock context and QueryEngine.
Assert performance SLAs as part of tests.

Output: Create test files that will FAIL until indexes are created."
```

### 3.2 AI Implements Indexes (GREEN)

```bash
goose run --task "Create Oak index definition files:

1. Location: ui.apps/src/main/content/jcr_root/oak:index/demo-scan-status/

2. Create .content.xml with:
   - compatVersion: 2
   - type: lucene
   - indexRules for dam:Asset
   - Properties: scanStatus, scanResult, quarantined
   
3. Create demo-workflow-index:
   - type: property  
   - properties: workflowId, workflowStatus

Use the spec from BEAD deliverable index-architecture.yaml
Follow oak:IndexDefinition nodetype."
```

### 3.3 Index Definition Output

```xml
<!-- oak:index/demo-scan-status/.content.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:oak="http://jackrabbit.apache.org/oak/ns/1.0"
    jcr:primaryType="oak:IndexDefinition"
    jcr:mixinTypes="[mix:title]"
    jcr:title="Demo Asset Scan Status Index"
    compatVersion="2"
    type="lucene"
    async="async"
    indexRules="oak:IndexRule"
    aggregates="oak:Aggregate">
    
    <indexRules jcr:primaryType="oak:IndexRule">
        <dam:Asset jcr:primaryType="oak:IndexRule" name="dam:Asset">
            <properties jcr:primaryType="nt:unstructured">
                <scanStatus
                    jcr:primaryType="oak:PropertyDefinition"
                    name="scanStatus"
                    propertyIndex="true"
                    ordered="true"
                    type="String"/>
                <quarantined
                    jcr:primaryType="oak:PropertyDefinition"
                    name="quarantined"
                    propertyIndex="true"
                    ordered="true"
                    type="Date"/>
            </properties>
        </dam:Asset>
    </indexRules>
</jcr:root>
```

---

## Part 4: AI Validates Performance (Phase 05) - 45 min

### 4.1 AI Runs Tests

```bash
# Run index performance tests
bead task: run-tests --spec IndexSpecs

# Expected: Tests PASS after indexes created
# If FAIL: Iterate back to Part 3
```

### 4.2 AI Analyzes Query Plans

```bash
goose run --task "For each query pattern, use QueryEngine to explain:

1. Get query plan for each of these queries:
   - /jcr:root/content/dam//*[@scanStatus='INFECTED']
   - SELECT * FROM dam:Asset WHERE workflowId IS NOT NULL
   
2. Verify indexes are being used

3. Report which indexes are used and their costs

Output: Create query-plan-analysis.md with findings"
```

### 4.3 AI Performance Validation

```yaml
# .bead/deliverables/index-validation.yaml
validation:
  query_plans:
    - query: "Q1 - Infected assets"
      index_used: "demo-scan-status-index"
      cost: 0.001
      within_sla: true
      
    - query: "Q2 - Workflow query"  
      index_used: "demo-workflow-index"
      cost: 0.002
      within_sla: true
      
  performance_results:
    - metric: "Query scanStatus"
      before_ms: 5000
      after_ms: 150
      improvement: "33x"
      
    - metric: "Workflow queries"
      before_ms: 2000
      after_ms: 50
      improvement: "40x"
```

---

## Part 5: GasTown Orchestration - 30 min

### 5.1 Configure GasTown Workflow

```yaml
# gastown.yaml
workflows:
  implement-indexes:
    name: "Oak Index Implementation"
    max_parallel: 2
    
    steps:
      - name: analyze-queries
        agent: ai-coder
        task: "Analyze query patterns from PRD and logs"
        output: query-analysis.yaml
        
      - name: design-indexes
        agent: ai-architect
        task: "Design index strategy"
        depends_on: analyze-queries
        output: index-architecture.yaml
        
      - name: implement-indexes
        agent: ai-coder
        task: "Create index definition files"
        depends_on: design-indexes
        output: oak:index/**
        
      - name: test-indexes
        agent: ai-tester
        task: "Run IndexSpecs tests"
        depends_on: implement-indexes
        quality_gate: tests_pass
        
      - name: validate-performance
        agent: ai-reviewer
        task: "Verify query plans and SLA"
        depends_on: test-indexes
```

### 5.2 Run GasTown Workflow

```bash
# Execute via GasTown
gastown run implement-indexes --epic SAW-001
```

---

## Part 6: AI Health Monitoring (Phase 06) - 15 min

### 6.1 AI Creates Health Check

```bash
goose run --task "Create OakIndexHealthCheck.java:

1. Implements HealthCheck interface
2. Checks all custom indexes for:
   - Index state (should be READY)
   - Index size (warn if > 1GB)
   - Document count
   - Last reindex time
   
3. Returns HealthCheckResult

4. Create as OSGi component in core module"
```

### 6.2 AI Sets Up Monitoring

```yaml
# .github/workflows/index-health.yml
name: Oak Index Health Check

on:
  schedule: [0 6 * * *]  # Daily

jobs:
  health-check:
    runs-on: ubuntu-latest
    steps:
      - name: AI Index Health Analysis
        run: |
          # Goose analyzes index health
          goose run --task "Check Oak indexes:
          1. Get index stats via JMX
          2. Report on index health
          3. Flag any issues
          Output: index-health-report.json"
```

---

## Complete BEAD Task

```yaml
# .bead/tasks/saw-index-impl.yaml
id: SAW-024
type: task
title: "Implement Oak Indexes for Query Optimization"
status: completed

description: |
  Create custom Oak indexes for secure asset workflow queries

dependencies:
  - SAW-020  # Query analysis complete

session_log:
  - timestamp: "2024-01-15T10:00:00Z"
    agent: ai-coder
    action: "Analyzed query patterns from PRD"
    
  - timestamp: "2024-01-15T10:30:00Z"
    agent: ai-architect  
    action: "Designed index strategy"
    
  - timestamp: "2024-01-15T11:00:00Z"
    agent: ai-coder
    action: "Wrote IndexSpecs tests (RED)"
    
  - timestamp: "2024-01-15T11:30:00Z"
    agent: ai-coder
    action: "Implemented index definitions (GREEN)"
    
  - timestamp: "2024-01-15T12:00:00Z"
    agent: ai-tester
    action: "Validated tests pass"

artifacts:
  - "oak:index/demo-scan-status/"
  - "oak:index/demo-workflow-index/"
  - "IndexSpecs.java"
  - "query-plan-analysis.md"

acceptance_criteria:
  - criteria: "All queries use indexes"
    status: met
  - criteria: "Query performance within SLA"
    status: met
  - criteria: "Tests pass"
    status: met
```

---

## Verification Checklist

- [ ] BEAD task created
- [ ] AI analyzed query patterns (Phase 01-02)
- [ ] AI designed index strategy (Phase 03)
- [ ] AI wrote tests RED → GREEN (Phase 04)
- [ ] GasTown workflow configured
- [ ] AI validated performance (Phase 05)
- [ ] Health checks implemented (Phase 06)

---

## Summary: Agentic AI Approach

| BMAD Phase | AI Agent Activity | Output |
|------------|------------------|--------|
| 01-02 | Analyzes PRD for query requirements | Query patterns |
| 03 | Designs index architecture | Index strategy |
| 04 (TDD RED) | Writes IndexSpecs tests | Failing tests |
| 04 (TDD GREEN) | Implements index definitions | Index XML files |
| 05 | Validates query performance | Performance report |
| 06 | Creates health monitoring | HealthCheck |

**This is the agentic AI difference**: AI agents do the work, tracked in BEAD, orchestrated by GasTown.
