# Lab 22: GraphQL for AEM with AI
# Comprehensive Lab - 4 hours

## Objective

Implement GraphQL APIs for AEM DAM using AI-assisted development with TDD. Create query endpoints for the secure asset workflow and integrate with external applications.

---

## Prerequisites

- Lab 4 (Development) completed
- Lab 19 (Functional/Regression) helpful

---

## BMAD Phase Context

```
Phase 02 (Models) - GraphQL Schema Definition
Phase 03 (Architecture) - API Design
Phase 04 (Development) - GraphQL Implementation
Phase 05 (Testing) - API Testing
```

---

## Part 1: GraphQL Schema Specification (TDD - RED) (30 min)

### 1.1 Define Schema First

Create `core/src/test/graphql/schema.spec.graphql`:

```graphql
# GraphQL Schema for Secure Asset Workflow

type Query {
  """
  Get assets with security scan status
  """
  assets(
    filter: AssetFilter
    limit: Int = 10
    offset: Int = 0
  ): AssetConnection
  
  """
  Get single asset by path
  """
  asset(path: String!): Asset
  
  """
  Get workflow status for an asset
  """
  workflowStatus(assetPath: String!): WorkflowStatus
  
  """
  Get quarantine report
  """
  quarantineReport(
    from: Date
    to: Date
  ): QuarantineReport
}

type Mutation {
  """
  Initiate approval workflow
  """
  initiateWorkflow(input: WorkflowInput!): WorkflowResult
  
  """
  Approve asset
  """
  approveAsset(assetPath: String!, comment: String): ApprovalResult
  
  """
  Reject asset
  """
  rejectAsset(assetPath: String!, reason: String!): RejectionResult
}

type AssetConnection {
  edges: [AssetEdge!]!
  pageInfo: PageInfo!
  totalCount: Int!
}

type AssetEdge {
  node: Asset!
  cursor: String!
}

type Asset {
  path: String!
  name: String!
  mimeType: String!
  size: Int!
  scanStatus: ScanStatus!
  scanResult: ScanResult
  workflowStatus: WorkflowStatus
  metadata: AssetMetadata
  createdAt: DateTime!
  modifiedAt: DateTime!
}

enum ScanStatus {
  PENDING
  SCANNING
  CLEAN
  INFECTED
  ERROR
}

type ScanResult {
  scanId: String!
  threatName: String
  scannedAt: DateTime!
  duration: Int!
}

type WorkflowStatus {
  workflowId: String
  currentStep: String
  assignee: String
  dueDate: DateTime
}

input AssetFilter {
  scanStatus: ScanStatus
  mimeType: String
  fromDate: Date
  toDate: Date
}
```

### 1.2 Write GraphQL Tests First

```java
@GraphQLsTest
class AssetGraphQLTest {
    
    @Test
    @DisplayName("Query: assets returns assets with scan status")
    void queryAssetsWithScanStatus() {
        // Given: Assets in repository
        
        // When: Query assets
        AssetConnection result = graphQLClient.query(
            "{ assets { edges { node { path scanStatus } } } }");
        
        // Then: Verify structure
        assertNotNull(result.getEdges());
        assertTrue(result.getEdges().size() > 0);
        assertNotNull(result.getEdges().get(0).getNode().getScanStatus());
    }
    
    @Test
    @DisplayName("Query: filter by scanStatus")
    void queryFilterByScanStatus() {
        // When
        AssetConnection result = graphQLClient.query(
            "{ assets(filter: {scanStatus: INFECTED}) { edges { node { path } } } }");
        
        // Then: Only infected assets
        result.getEdges().forEach(edge -> 
            assertEquals(ScanStatus.INFECTED, 
                edge.getNode().getScanStatus()));
    }
    
    @Test
    @DisplayName("Mutation: initiate workflow")
    void mutationInitiateWorkflow() {
        // When
        WorkflowResult result = graphQLClient.mutate(
            "mutation { initiateWorkflow(input: {assetPath: \"/content/dam/test.pdf\"}) { workflowId status } }");
        
        // Then
        assertNotNull(result.getWorkflowId());
        assertEquals("STARTED", result.getStatus());
    }
}
```

---

## Part 2: GraphQL Schema Implementation (TDD - GREEN) (45 min)

### 2.1 AI Generate GraphQL Resolvers

```bash
goose run --task "Implement GraphQL resolvers for:
1. AssetQueryResolver - queries for assets, single asset
2. AssetMutationResolver - workflow mutations
3. WorkflowStatusResolver - status queries
Use AEM GraphQL and persist queries.
Follow OSGi service patterns."
```

### 2.2 Query Resolver

```java
@Component(service = QueryResolvers.class)
public class AssetQueryResolver implements QueryResolvers {
    
    @Reference
    private AssetService assetService;
    
    @Reference
    private GraphQLService graphQLService;
    
    @Override
    public DataFetcher<AssetConnection> getAssetsDataFetcher() {
        return dataFetchingEnvironment -> {
            AssetFilter filter = dataFetchingEnvironment.getArgument("filter");
            int limit = dataFetchingEnvironment.getArgument("limit");
            int offset = dataFetchingEnvironment.getArgument("offset");
            
            return assetService.findAssets(filter, limit, offset);
        };
    }
    
    @Override
    public DataFetcher<Asset> getAssetDataFetcher() {
        return dataFetchingEnvironment -> {
            String path = dataFetchingEnvironment.getArgument("path");
            return assetService.getAsset(path);
        };
    }
}
```

### 2.3 Persisted Queries

Create `ui.apps/src/main/content/jcr_root/conf/global/settings/graphql/persisted-queries/my-project/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="nt:unlocked">
    
    <assets-by-status
        jcr:title="Get Assets by Scan Status"
        jcr:description="Query assets filtered by scan status"
        sling:resourceType="graphql/persistedquery">
        <query><![CDATA[
            query AssetsByStatus($status: ScanStatus!) {
                assets(filter: {scanStatus: $status}) {
                    edges {
                        node {
                            path
                            name
                            scanStatus
                        }
                    }
                }
            }
        ]]></query>
    </assets-by-status>
    
</jcr:root>
```

---

## Part 3: Content Fragment Integration (30 min)

### 3.1 Content Fragment Models

```graphql
# Extend with Content Fragments
type Query {
  assetReviews(filter: ReviewFilter): [AssetReview!]!
}

type AssetReview @model {
  assetReference: String!
  scanStatus: ScanStatus!
  reviewer: String
  decision: Decision
  comments: String
  reviewedAt: DateTime
}

enum Decision {
  APPROVED
  REJECTED
  PENDING
}
```

### 3.2 Enable GraphQL for Content Fragments

```xml
<!-- /conf/global/settings/dam/cfm/models/asset-review/.content.xml -->
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="dam:Model">
    <name>Asset Review</name>
    <graphQLEnabled>true</graphQLEnabled>
</jcr:root>
```

---

## Part 4: Security & Caching (30 min)

### 4.1 Authentication

```java
@Component
public class GraphQLSecurity {
    
    @Reference
    private TokenValidator tokenValidator;
    
    public void validateRequest(ResourceResolver resolver, 
            DataFetchingEnvironment env) {
        
        String token = env.getGraphQLContext().get("token");
        if (!tokenValidator.isValid(token)) {
            throw new AccessDeniedException("Invalid token");
        }
        
        // Check permissions
        if (!resolver.hasPermission("/content/dam")) {
            throw new AccessDeniedException("Read access denied");
        }
    }
}
```

### 4.2 Caching

```java
@Component(service = GraphQLCache.class)
public class GraphQLCacheImpl implements GraphQLCache {
    
    private Cache<String, Object> queryCache;
    
    @Activate
    void activate(CacheConfig config) {
        queryCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }
    
    @Override
    public Optional<Object> getCachedResult(String query, 
            Map<String, Object> variables) {
        String key = generateKey(query, variables);
        return Optional.ofNullable(queryCache.getIfPresent(key));
    }
}
```

---

## Part 5: API Testing (30 min)

### 5.1 Integration Tests

```java
@GraphQLIntegrationTest
class GraphQLApiTest {
    
    @Test
    @DisplayName("Full workflow via GraphQL")
    void fullWorkflowViaGraphQL() {
        // 1. Upload asset
        String assetPath = uploadAsset();
        
        // 2. Initiate workflow via GraphQL
        WorkflowResult workflowResult = client.mutate(
            "mutation { initiateWorkflow(input: {assetPath: \"" + assetPath + "\"}) { workflowId } }");
        
        assertNotNull(workflowResult.getWorkflowId());
        
        // 3. Query workflow status
        WorkflowStatus status = client.query(
            "{ workflowStatus(assetPath: \"" + assetPath + "\") { currentStep assignee } }");
        
        assertNotNull(status.getCurrentStep());
        
        // 4. Approve
        ApprovalResult approval = client.mutate(
            "mutation { approveAsset(assetPath: \"" + assetPath + "\", comment: \"Approved\") { result } }");
        
        assertEquals("APPROVED", approval.getResult());
    }
}
```

### 5.2 Contract Testing

```yaml
# consumer-contract.yml
provider: AEM GraphQL
consumer: Mobile App

interactions:
  - given:
      state: "Assets exist"
    upon:
      request:
        method: POST
        path: "/graphql/execute.json/my-project/assets"
        body:
          query: "{ assets { edges { node { path } } } }"
    should:
      respond:
        status: 200
        body:
          data:
            assets:
              edges:
                - node:
                    path: "/content/dam/test.pdf"
```

---

## Part 6: External Integration (30 min)

### 6.1 GraphiQL Client

```javascript
// React GraphiQL integration
import { GraphiQL } from 'graphiql';
import { createGraphQLClient } from '@aem/graphql-client';

const client = createGraphQLClient({
  endpoint: '/graphql/execute.json/my-project',
  auth: tokenProvider
});

<GraphiQL 
  fetcher={client.fetch}
  defaultQuery="{ assets(limit: 5) { edges { node { path name } } } }"
/>
```

### 6.2 API Documentation

```bash
# Use Goose to generate documentation
goose run --task "Generate GraphQL API documentation from schema:
1. Query documentation
2. Mutation documentation  
3. Type references
4. Code examples in JavaScript, iOS Swift, Android Kotlin"
```

---

## Verification Checklist

- [ ] Schema defined (TDD RED)
- [ ] Resolvers implemented (TDD GREEN)
- [ ] Content Fragments integrated
- [ ] Security configured
- [ ] Caching implemented
- [ ] Integration tests passing
- [ ] External client examples

---

## BMAD Integration

| Phase | Activity |
|-------|----------|
| 01 | Define API requirements |
| 02 | Design GraphQL schema |
| 03 | Architecture for GraphQL |
| 04 | Implement resolvers |
| 05 | Test GraphQL API |
| 06 | Monitor in production |

---

## Key Takeaways

1. **Schema-first design** - Define types before implementation
2. **TDD for APIs** - Write tests in GraphQL
3. **Persisted queries** - Performance for production
4. **Security critical** - Validate all requests

---

## Next Steps

1. Add real-time subscriptions
2. Integrate with Adobe Experience Platform
3. Set up GraphQL caching at CDN
4. Monitor query performance

---

## References

- [AEM GraphQL](https://experienceleague.adobe.com/docs/experience-manager-65/developing/headless/graphql-api/graphql.html)
- [GraphQL Specification](https://graphql.org/)
