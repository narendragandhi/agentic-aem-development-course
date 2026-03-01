# Production Patterns for AEM Workflow Development

This document outlines production-ready patterns implemented in this project that are essential for enterprise AEM deployments.

---

## Table of Contents

1. [Resilience Patterns](#1-resilience-patterns)
2. [Health Monitoring](#2-health-monitoring)
3. [Notification System](#3-notification-system)
4. [Environment Configuration](#4-environment-configuration)
5. [Security Patterns](#5-security-patterns)
6. [Testing Patterns](#6-testing-patterns)

---

## 1. Resilience Patterns

### Circuit Breaker Pattern

The `ClamAVConnectionManager` implements the circuit breaker pattern to prevent cascading failures when external services are unavailable.

```
┌──────────────────────────────────────────────────────────────┐
│                 CIRCUIT BREAKER STATE DIAGRAM                │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│    ┌────────┐    failures >= threshold    ┌────────┐        │
│    │ CLOSED │ ─────────────────────────▶ │  OPEN  │        │
│    └────────┘                             └────────┘        │
│         ▲                                      │            │
│         │                                      │            │
│    success                             timeout elapsed      │
│         │                                      │            │
│         │         ┌───────────┐                │            │
│         └─────────│ HALF_OPEN │◀───────────────┘            │
│                   └───────────┘                             │
│                        │                                    │
│                   failure                                   │
│                        │                                    │
│                        └────────────────────────────────────┘
│                                (back to OPEN)               │
└──────────────────────────────────────────────────────────────┘
```

**Key Features:**
- **Failure Threshold**: After N consecutive failures, circuit opens
- **Timeout**: Circuit stays open for configurable duration
- **Half-Open Testing**: Allows single test request to check recovery
- **Metrics**: Tracks success rates and failure counts

**Implementation Location:** `core/src/main/java/com/demo/workflow/services/impl/ClamAVConnectionManager.java`

### Retry with Exponential Backoff

External service calls implement retry logic with exponential backoff to handle transient failures.

```java
// Exponential backoff: 1s, 2s, 4s, 8s... (capped at 10s)
long delay = retryDelayMs * (long) Math.pow(2, attempts - 1);
Thread.sleep(Math.min(delay, 10000));
```

**Configuration:**
- `maxRetries`: Maximum retry attempts
- `retryDelayMs`: Base delay between retries
- Backoff multiplier: 2x (exponential)
- Maximum delay cap: 10 seconds

---

## 2. Health Monitoring

### Health Check Servlet

A dedicated health check endpoint enables monitoring integration.

**Endpoint:** `GET /bin/workflow/health/antivirus`

**Response Format:**
```json
{
  "status": "OK",
  "available": true,
  "engine": "ClamAV",
  "details": "ClamAV daemon connected and responding",
  "timestamp": 1709301600000
}
```

**Status Codes:**
| HTTP Code | Status | Meaning |
|-----------|--------|---------|
| 200 | OK | Service healthy |
| 200 | WARN | Service available but degraded (e.g., MOCK mode) |
| 503 | ERROR | Service unavailable |

**Integration Points:**
- Load balancer health checks
- Kubernetes liveness/readiness probes
- Monitoring systems (Datadog, New Relic, CloudWatch)

**Implementation Location:** `core/src/main/java/com/demo/workflow/services/impl/AntivirusHealthCheck.java`

---

## 3. Notification System

### Multi-Channel Notifications

The `NotificationServiceImpl` provides enterprise notification capabilities.

**Supported Channels:**
- **Email**: Via AEM Day CQ Mail Service (MessageGateway)
- **Slack**: Via webhook integration

**Features:**
- Async processing with thread pool
- Retry logic for failed deliveries
- Security-specific notifications
- HTML email support

**Configuration:**
```json
{
    "enabled": true,
    "from_email": "aem-workflow@company.com",
    "security_recipients": "security@company.com",
    "slack_webhook_url": "$[env:SLACK_WEBHOOK_URL]",
    "slack_security_channel": "#security-alerts",
    "email_enabled": true,
    "slack_enabled": true,
    "thread_pool_size": 4,
    "max_retries": 3
}
```

**Security Alert Flow:**
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Malware    │────▶│ Notification │────▶│   Email     │
│  Detected   │     │   Service    │     │  Security   │
└─────────────┘     └──────────────┘     │   Team      │
                           │             └─────────────┘
                           │             ┌─────────────┐
                           └────────────▶│   Slack     │
                                         │  #security  │
                                         └─────────────┘
```

**Implementation Location:** `core/src/main/java/com/demo/workflow/services/impl/NotificationServiceImpl.java`

---

## 4. Environment Configuration

### Run Mode-Based Configuration

OSGi configurations are organized by environment:

```
ui.apps/src/main/content/jcr_root/apps/demo-workflow/osgiconfig/
├── config.author/          # Local development (author)
├── config.stage/           # Staging environment
└── config.prod/            # Production environment
```

### Environment Variables

Sensitive values use Cloud Manager environment variables:

```json
{
    "slack_webhook_url": "$[env:SLACK_WEBHOOK_URL_PROD]"
}
```

### Environment Differences

| Setting | Author (Dev) | Stage | Prod |
|---------|--------------|-------|------|
| Scan Mode | MOCK | CLAMAV | CLAMAV |
| ClamAV Host | localhost | clamav-stage.internal | clamav-prod.internal |
| Max File Size | 100MB | 100MB | 256MB |
| Retention Days | 7 | 14 | 90 |
| Thread Pool | 2 | 2 | 4 |

---

## 5. Security Patterns

### Service User Mapping

Workflow services use dedicated service users with minimal permissions:

```json
{
    "user.mapping": [
        "com.demo.workflow.core:workflow-service=[content-reader-service]",
        "com.demo.workflow.core:asset-processor=[content-writer-service]"
    ]
}
```

### Secure Quarantine

Infected assets are moved to a protected quarantine location:

```
/content/dam/quarantine/
├── 2024/
│   ├── 02/
│   │   ├── infected-file-1.exe
│   │   └── infected-file-2.pdf
```

**Retention Policy:**
- Dev: 7 days
- Stage: 14 days
- Prod: 90 days (for security audit trails)

### Audit Logging

All scan operations are logged with:
- Asset path
- Scan result (CLEAN/INFECTED/ERROR/SKIPPED)
- Threat name (if detected)
- Scan duration
- Scan engine used

---

## 6. Testing Patterns

### Unit Testing with Mockito

Workflow process tests use proper mock chains:

```java
@BeforeEach
void setUp() {
    // Mock both access paths used by the process
    lenient().when(workItem.getWorkflowData()).thenReturn(workflowData);
    lenient().when(workItem.getWorkflow()).thenReturn(workflow);
    lenient().when(workflow.getWorkflowData()).thenReturn(workflowData);
}
```

### Test Coverage Requirements

| Component | Min Coverage |
|-----------|--------------|
| Services | 80% |
| Workflow Processes | 70% |
| Servlets | 60% |

### Mock Mode for Development

The antivirus service supports MOCK mode for development:

```java
// Files starting with "virus_" are flagged as infected
if (fileName.toLowerCase().startsWith("virus_")) {
    return ScanResult.infected("Mock.TestVirus", "MOCK", scanTime);
}
```

---

## Cloud Manager Integration

### Pipeline Configuration

The `.cloudmanager/config.yaml` defines the CI/CD pipeline:

```yaml
pipelines:
  full-stack:
    steps:
      - Build
      - Unit Tests
      - Code Scanning
      - Deploy to Dev
      - Functional Tests
      - Deploy to Stage (manual approval)
      - Performance Tests
      - Deploy to Production (manual approval)
```

### Best Practices

1. **Never skip tests in pipeline** - Use -DskipTests only locally
2. **Use environment variables** for secrets
3. **Validate dispatcher config** before deployment
4. **Run performance tests** on stage before production
5. **Require manual approval** for production deployments

---

## Related Documentation

- [Troubleshooting Guide](./TROUBLESHOOTING.md)
- [Improvements Roadmap](./IMPROVEMENTS.md)
- [CLAUDE.md](../CLAUDE.md) - Project setup and conventions
