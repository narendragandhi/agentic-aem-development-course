# Lab 20: AEM as a Cloud Service
# Comprehensive Lab - 4 hours

## Objective

Deploy and manage the secure asset workflow on AEM as a Cloud Service. Learn Cloud Manager pipelines, environment management, and Cloud-specific configurations.

---

## Prerequisites

- Lab 8 (Deployment) completed
- AEM Cloud Service SDK
- Cloud Manager access

---

## Overview

### AEM Cloud vs Classic

| Aspect | AEM Cloud | AEM 6.5 |
|--------|-----------|---------|
| Deployment | Cloud Manager | Manual/Cloud Manager |
| Storage | Cloud-native blob storage | Local filesystem |
| Indexing | Auto-indexing | Manual |
| Logging | Centralized | Local logs |
| Scaling | Automatic | Manual |
| Updates | Automatic | Manual |

---

## Part 1: Cloud Manager Setup (30 min)

### 1.1 Configure pom.xml for Cloud

Update `pom.xml`:

```xml
<properties>
    <aem.cloud.sdk.version>2024.11.0</aem.cloud.sdk.version>
    <aem.version>6.5.21</aem.version>
</properties>

<dependencyManagement>
    <dependency>
        <groupId>com.adobe.aem</groupId>
        <artifactId>aem-sdk-api</artifactId>
        <version>${aem.cloud.sdk.version}</version>
    </dependency>
</dependencyManagement>
```

### 1.2 Add Cloud Dependencies

```xml
<dependencies>
    <!-- AEM Cloud SDK -->
    <dependency>
        <groupId>com.adobe.aem</groupId>
        <artifactId>aem-sdk-api</artifactId>
    </dependency>
    
    <!-- Cloud Manager API -->
    <dependency>
        <groupId>com.adobe.cloudmanager</groupId>
        <artifactId>cloudmanager-api</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 1.3 Environment Configuration

Create `ui.config/src/main/content/jcr_root/apps/demo-workflow/osgiconfig/config.cloud/com.demo.workflow.services.impl.AntivirusScanServiceImpl.cfg.json`:

```json
{
  "scanEngine": "CLAMAV",
  "clamavHost": "${CLOUD_AEM_CLAMAV_HOST}",
  "clamavPort": 3310,
  "timeoutMs": 60000,
  "maxFileSizeMb": 100,
  "quarantinePath": "/var/demo-workflow/quarantine",
  "failOnError": false
}
```

---

## Part 2: Cloud Manager Pipeline (45 min)

### 2.1 Cloud Manager YAML

Create `.cloudmanager/config.yaml`:

```yaml
runtime:
  nodeVersion: "20"
  
program:
  id: "demo-program"
  name: "Demo Program"

project:
  name: "secure-asset-workflow"
  
environments:
  - name: "dev"
    type: "dev"
    deployment:
      git:
        branch: "main"
        
  - name: "stage"
    type: "stage"
    deployment:
      git:
        branch: "main"
      qualityGates:
        - name: "Security"
          criticalIssues: 0
        - name: "Performance"
          performanceBudget: "3s"
          
  - name: "prod"
    type: "prod"
    deployment:
      git:
        branch: "main"
      approval: "manual"

pipelines:
  - name: "deploy-dev"
    type: "deployment"
    environment: "dev"
    stages:
      - name: "build"
        script: |
          mvn clean package -DskipTests
      - name: "security"
        script: |
          mvn verify -Psecurity-scan
      - name: "deploy"
        script: |
          aio cloudmanager:deploy --environment dev
```

### 2.2 Build Configuration

Update `pom.xml` for Cloud Manager:

```xml
<build>
    <plugins>
        <!-- Cloud Manager Bundle Plugin -->
        <plugin>
            <groupId>com.adobe.cloudmanager</groupId>
            <artifactId>cloudmanager-maven-plugin</artifactId>
            <version>1.0.0</version>
            <configuration>
                <programId>demo-program</programId>
                <environmentId>dev</environmentId>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Part 3: Environment-Specific Config (30 min)

### 3.1 Run Mode Configs

Structure:

```
ui.config/src/main/content/jcr_root/apps/demo-workflow/osgiconfig/
├── config/                    # Author (local)
│   └── com.demo.workflow.Services.cfg.json
├── config.author/             # Author
│   └── com.demo.workflow.Services.cfg.json
├── config.publish/            # Publish
│   └── com.demo.workflow.Services.cfg.json
└── config.cloud.dev/           # Cloud Dev
│   └── com.demo.workflow.Services.cfg.json
└── config.cloud.stage/        # Cloud Stage
    └── com.demo.workflow.Services.cfg.json
```

### 3.2 Environment Variables

```json
{
  "clamavHost": "${ENV:CLAMAV_HOST}",
  "clamavPort": "${ENV:CLAMAV_PORT:3310}",
  "scanTimeout": "${ENV:SCAN_TIMEOUT:60000}",
  "logLevel": "${ENV:LOG_LEVEL:INFO}"
}
```

---

## Part 4: Cloud Logging (30 min)

### 4.1 Structured Logging

```java
import com.adobe.granite.logging.Log;

public class AntivirusScanServiceImpl implements AntivirusScanService {
    
    private static final Logger logger = LoggerFactory.getLogger(
        AntivirusScanServiceImpl.class);
    
    // Use structured logging for Cloud
    public void scanAsset(Asset asset) {
        MDC.put("assetPath", asset.getPath());
        MDC.put("assetSize", asset.getSize());
        
        logger.info("Starting asset scan",
            ImmutableMap.of(
                "asset", asset.getPath(),
                "size", asset.getSize(),
                "mimeType", asset.getMimeType()
            ));
        
        try {
            // scan logic
            logger.info("Scan completed",
                ImmutableMap.of(
                    "status", result.getStatus(),
                    "duration", duration
                ));
        } catch (Exception e) {
            logger.error("Scan failed",
                ImmutableMap.of(
                    "asset", asset.getPath(),
                    "error", e.getMessage()
                ), e);
        } finally {
            MDC.clear();
        }
    }
}
```

### 4.2 Log Retrieval

```bash
# Using Cloud Manager CLI
aio cloudmanager:logs:get --environment dev --service author

# Download logs
aio cloudmanager:logs:download --environment dev --date 2024-01-15
```

---

## Part 5: CI/CD for Cloud (30 min)

### 5.1 GitHub Actions

Create `.github/workflows/aem-cloud.yml`:

```yaml
name: AEM Cloud Deployment

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          
      - name: Build
        run: mvn clean package -DskipTests
        
      - name: Upload to Cloud Manager
        run: |
          aio cloudmanager:build:create \
            --program-id ${{ secrets.CM_PROGRAM_ID }} \
            --git-branch main
            
      - name: Deploy to Dev
        run: |
          aio cloudmanager:deployment:start \
            --program-id ${{ secrets.CM_PROGRAM_ID }} \
            --environment dev
            
      - name: Wait for Quality Gate
        run: |
          aio cloudmanager:pipeline:execute \
            --program-id ${{ secrets.CM_PROGRAM_ID }} \
            --pipeline-id ${{ secrets.CM_PIPELINE_ID }} \
            --wait
```

---

## Part 6: Cloud-Specific Features (30 min)

### 6.1 Immutable Content

Mark workflow models as immutable:

```xml
<!-- /apps/demo-workflow/workflow/models/.content.xml -->
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="sling:Folder"
    jcr:workspace="live"
    sling:resourceType="sling:Folder">
    
    <workflow-model jcr:primaryType="cq:WorkflowModel"
        jcr:title="Secure Asset Approval"
        sling:resourceType="granite/workflow/model"
        immutable="true"/>
</jcr:root>
```

### 6.2 Service User Configuration

```json
{
  "user.mapping": [
    "com.demo.workflow:service-workflow-service=[workflow-users]"
  ]
}
```

---

## Verification Checklist

- [ ] pom.xml configured for Cloud SDK
- [ ] Environment-specific configs created
- [ ] Cloud Manager pipeline configured
- [ ] Structured logging implemented
- [ ] GitHub Actions workflow created
- [ ] Deployment to Dev environment works
- [ ] Logs accessible via Cloud Manager

---

## Key Takeaways

1. **Cloud Manager handles deployment** - No manual uploads
2. **Environment configs** - Use Cloud-specific configurations
3. **Structured logging** - Essential for Cloud debugging
4. **Immutable content** - Mark workflow models properly

---

## Next Steps

1. Set up full CI/CD pipeline
2. Configure production deployment approval
3. Enable Adobe Analytics integration
4. Set up Alerting in Adobe Experience Cloud

---

## References

- [AEM Cloud Service Documentation](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/overview/introduction.html)
- [Cloud Manager Documentation](https://experienceleague.adobe.com/docs/experience-manager-cloud-manager/content/introduction.html)
