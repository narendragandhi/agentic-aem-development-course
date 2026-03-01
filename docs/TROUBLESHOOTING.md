# Troubleshooting Guide

This guide covers common issues encountered when developing and deploying AEM workflows.

---

## Table of Contents

1. [Build Issues](#build-issues)
2. [OSGi Bundle Issues](#osgi-bundle-issues)
3. [Workflow Issues](#workflow-issues)
4. [Antivirus Integration Issues](#antivirus-integration-issues)
5. [Notification Issues](#notification-issues)
6. [Cloud Manager Issues](#cloud-manager-issues)

---

## Build Issues

### OSGi Config Validation Error

**Error:**
```
Package of type 'APPLICATION' is not supposed to contain OSGi bundles or configurations!
```

**Cause:** OSGi configurations are placed in `ui.apps` which has `packageType=application`.

**Solution:**
The project disables the `jackrabbit-packagetype` validator. If you see this error, ensure the validator is disabled in the parent pom.xml:

```xml
<validatorsSettings>
    <jackrabbit-packagetype>
        <isDisabled>true</isDisabled>
    </jackrabbit-packagetype>
</validatorsSettings>
```

---

### Test Compilation Error: Package Not Found

**Error:**
```
package org.apache.sling.hc.annotations does not exist
```

**Cause:** Using deprecated Sling Health Check annotations.

**Solution:** Use servlet-based health checks instead of annotation-based. See `AntivirusHealthCheck.java` for the correct pattern.

---

### Maven Build Fails with JDK Version

**Error:**
```
Fatal error compiling: invalid target release: 11
```

**Solution:**
```bash
# Check Java version
java -version

# Set JAVA_HOME to JDK 11
export JAVA_HOME=/path/to/jdk-11
```

---

## OSGi Bundle Issues

### Bundle Not Starting

**Symptoms:**
- Bundle shows "Installed" instead of "Active" in Felix Console
- Services not available

**Diagnosis:**
1. Open Felix Console: `http://localhost:4502/system/console/bundles`
2. Find your bundle and click on it
3. Check the "Imported Packages" section for unresolved imports

**Common Causes:**

| Issue | Solution |
|-------|----------|
| Missing dependency | Add to pom.xml `<dependency>` |
| Wrong version | Check SDK BOM for correct versions |
| Private package exported | Mark implementation packages as private in bnd |

**Check OSGi Console:**
```
http://localhost:4502/system/console/status-Bundlelist
```

---

### Service Not Registered

**Error:**
```
Caused by: org.osgi.service.component.ComponentException: Component descriptor missing
```

**Diagnosis:**
```bash
# Check if the component is listed
curl -u admin:admin "http://localhost:4502/system/console/components.json" | jq '.data[] | select(.name | contains("demo.workflow"))'
```

**Common Causes:**

1. **Missing @Component annotation**
   ```java
   @Component(service = MyService.class)
   public class MyServiceImpl implements MyService {
   ```

2. **Service not listed in @Component**
   ```java
   // Wrong
   @Component

   // Correct
   @Component(service = WorkflowProcess.class)
   ```

3. **Missing @Reference for dependencies**
   ```java
   @Reference
   private ResourceResolverFactory resolverFactory;
   ```

---

## Workflow Issues

### Workflow Not Triggering

**Symptoms:**
- Asset uploaded but workflow doesn't start
- No workflow instance created

**Diagnosis:**

1. **Check Launcher Configuration:**
   ```
   http://localhost:4502/libs/cq/workflow/admin/console/content/launchers.html
   ```

2. **Verify Launcher Settings:**
   - Path regex matches the asset path
   - Event type matches (e.g., `MODIFIED` for uploads)
   - Launcher is enabled

3. **Check Launcher Path:**
   ```
   /conf/global/settings/workflow/launcher/config/secure-asset-approval-launcher
   ```

**Common Issues:**

| Problem | Solution |
|---------|----------|
| Wrong path regex | Update `glob` property to match asset path |
| Wrong event type | Use `MODIFIED` for uploads, `CREATED` for new nodes |
| Launcher disabled | Set `enabled=true` |
| Condition not met | Check `condition` property |

---

### Workflow Stuck in Step

**Symptoms:**
- Workflow instance shows "RUNNING" but not progressing
- Work item not appearing in inbox

**Diagnosis:**

1. **Check Workflow Instance:**
   ```
   http://localhost:4502/libs/cq/workflow/admin/console/content/instances.html
   ```

2. **View Workflow History:**
   - Click on the instance
   - Check "History" tab
   - Look for error messages

3. **Check Logs:**
   ```bash
   tail -f crx-quickstart/logs/error.log | grep -i workflow
   ```

**Common Causes:**

| Issue | Solution |
|-------|----------|
| Exception in process step | Check error.log, fix the process code |
| Participant step with no user | Assign participant or configure participant chooser |
| External system timeout | Increase timeout, add retry logic |
| Missing ResourceResolver | Add proper service user mapping |

---

### ResourceResolver is Null

**Error:**
```
WorkflowException: Unable to obtain ResourceResolver
```

**Cause:** The workflow session cannot be adapted to ResourceResolver.

**Solution:**
1. Ensure service user mapping exists:
   ```json
   {
       "user.mapping": [
           "com.demo.workflow.core:workflow-service=[content-reader-service]"
       ]
   }
   ```

2. Create the service user in AEM:
   ```
   http://localhost:4502/crx/explorer/index.jsp
   ```

---

## Antivirus Integration Issues

### ClamAV Connection Refused

**Error:**
```
IOException: Connection refused (Connection refused)
```

**Diagnosis:**
1. Check if ClamAV is running:
   ```bash
   docker ps | grep clamav
   # or
   systemctl status clamd
   ```

2. Test connection:
   ```bash
   telnet localhost 3310
   ```

3. Check health endpoint:
   ```bash
   curl http://localhost:4502/bin/workflow/health/antivirus
   ```

**Solutions:**

| Cause | Solution |
|-------|----------|
| ClamAV not running | Start ClamAV service |
| Wrong host/port | Update OSGi config |
| Firewall blocking | Open port 3310 |
| Docker networking | Use container name or host.docker.internal |

---

### Circuit Breaker Open

**Error:**
```
IOException: Circuit breaker is OPEN - ClamAV service appears unavailable
```

**Cause:** Too many consecutive connection failures.

**Solution:**
1. Wait for the circuit to transition to HALF_OPEN (default: 30 seconds)
2. Fix the underlying connection issue
3. Manually reset via JMX or restart the bundle

---

### Scan Taking Too Long

**Symptoms:**
- Workflow step timing out
- Large files failing

**Solutions:**
1. Increase timeout in config:
   ```json
   {
       "readTimeout": 60000,
       "connectionTimeout": 5000
   }
   ```

2. Increase max file size:
   ```json
   {
       "maxFileSize": 268435456
   }
   ```

3. Consider async scanning for large files

---

## Notification Issues

### Emails Not Sending

**Diagnosis:**
1. Check Day CQ Mail Service configuration:
   ```
   http://localhost:4502/system/console/configMgr
   ```
   Search for "Day CQ Mail Service"

2. Verify SMTP settings:
   - Host
   - Port
   - Authentication
   - TLS/SSL

3. Check logs:
   ```bash
   grep -i "mail\|smtp\|email" crx-quickstart/logs/error.log
   ```

**Common Issues:**

| Problem | Solution |
|---------|----------|
| No SMTP configured | Configure Day CQ Mail Service |
| Authentication failed | Check credentials |
| SSL/TLS mismatch | Match port with security settings |
| Blocked by firewall | Open SMTP port |

---

### Slack Notifications Failing

**Error:**
```
Slack notification failed: 403 Forbidden
```

**Solutions:**
1. Verify webhook URL is correct
2. Check if webhook is still active in Slack admin
3. Ensure webhook URL is not expired
4. Use environment variable instead of hardcoding:
   ```json
   {
       "slack_webhook_url": "$[env:SLACK_WEBHOOK_URL]"
   }
   ```

---

## Cloud Manager Issues

### Pipeline Build Failing

**Symptoms:**
- Build fails during "Code Quality" step
- Test failures in pipeline

**Diagnosis:**
1. Check Cloud Manager logs
2. Download build artifacts
3. Review test reports

**Common Issues:**

| Error | Solution |
|-------|----------|
| Test failures | Fix failing tests locally first |
| Code quality gate | Address SonarQube issues |
| Package validation | Fix package structure |
| Missing dependencies | Check dependency scope |

---

### Deployment Failing

**Error:**
```
Package installation failed
```

**Solutions:**
1. Check for conflicting packages
2. Verify filter.xml paths
3. Check for locked content
4. Review replication queue

---

## Quick Diagnostics

### Essential Endpoints

| Purpose | URL |
|---------|-----|
| OSGi Bundles | `/system/console/bundles` |
| OSGi Components | `/system/console/components` |
| OSGi Config | `/system/console/configMgr` |
| Workflows | `/libs/cq/workflow/admin/console/content/models.html` |
| Workflow Instances | `/libs/cq/workflow/admin/console/content/instances.html` |
| Launchers | `/libs/cq/workflow/admin/console/content/launchers.html` |
| Health Check | `/bin/workflow/health/antivirus` |
| Error Logs | `/system/console/slinglog` |

### Log Levels for Debugging

Add to OSGi configuration:
```
org.slf4j.simpleLogger.log.com.demo.workflow=DEBUG
```

Or via Felix Console:
```
http://localhost:4502/system/console/slinglog
```

### Useful Commands

```bash
# Full rebuild
mvn clean install -PautoInstallSinglePackage

# Run tests only
mvn test -pl core

# Skip tests for quick deploy
mvn clean install -PautoInstallSinglePackage -DskipTests

# Check bundle status
curl -u admin:admin http://localhost:4502/system/console/bundles.json | jq '.data[] | select(.symbolicName | contains("demo"))'

# Tail error logs
tail -f crx-quickstart/logs/error.log
```

---

## Getting Help

1. **Check existing documentation:**
   - [Production Patterns](./PRODUCTION_PATTERNS.md)
   - [Improvements](./IMPROVEMENTS.md)

2. **AEM Resources:**
   - [AEM Cloud Service Documentation](https://experienceleague.adobe.com/docs/experience-manager-cloud-service.html)
   - [Workflow Administration](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/sites/administering/workflows-administering.html)

3. **Community:**
   - [AEM Community Forum](https://experienceleaguecommunities.adobe.com/t5/adobe-experience-manager/ct-p/adobe-experience-manager-community)
   - [Stack Overflow - AEM](https://stackoverflow.com/questions/tagged/aem)
