# MCP Servers for AEM Runtime Integration

## Overview

Model Context Protocol (MCP) servers provide runtime integration between AI coding tools and AEM instances. This enables AI agents to query logs, debug OSGi bundles, trace requests, and interact with the Dispatcher—all from within your IDE.

---

## Available MCP Servers

| Server | Source | Purpose |
|--------|--------|---------|
| **AEM Quickstart MCP** | AEM SDK Package | Logs, OSGi debugging, request tracing |
| **Dispatcher MCP** | Dispatcher SDK | Validation, caching, request tracing |

---

## AEM Quickstart MCP Server

### Prerequisites

- AEM SDK version `2026.2.24678.20260226T154829Z-260200` or newer
- Local AEM instance running on `localhost:4502`

### Installation

1. **Download the MCP Server Package**

   From [Software Distribution Portal](https://experience.adobe.com/#/downloads):
   ```
   com.adobe.aem:com.adobe.aem.mcp-server-contribs-content
   ```

2. **Install via Package Manager**

   Navigate to: `http://localhost:4502/crx/packmgr`

   Upload and install the downloaded package.

3. **Verify Installation**

   Test the MCP endpoint:
   ```bash
   curl -u admin:admin http://localhost:4502/bin/mcp
   ```

### Available Tools

| Tool | Description | Example Use |
|------|-------------|-------------|
| `aem-logs` | Retrieve filterable log entries | Debug errors, trace request flow |
| `diagnose-osgi-bundle` | Report bundle issues | Missing packages, unsatisfied refs |
| `recent-requests` | HTTP requests with Sling traces | Debug request processing |

### Tool: aem-logs

Retrieves log entries with filtering:

```json
{
  "tool": "aem-logs",
  "parameters": {
    "pattern": "ERROR",
    "level": "ERROR",
    "count": 50
  }
}
```

**Filters:**
- `pattern`: Regex pattern to match log messages
- `level`: Log level filter (ERROR, WARN, INFO, DEBUG)
- `count`: Maximum entries to return

### Tool: diagnose-osgi-bundle

Reports OSGi bundle issues:

```json
{
  "tool": "diagnose-osgi-bundle",
  "parameters": {
    "bundle": "com.example.core"
  }
}
```

**Returns:**
- Bundle state (Active, Installed, Resolved)
- Missing package imports
- Unsatisfied service references
- Configuration issues

### Tool: recent-requests

Returns HTTP requests with full Sling processing:

```json
{
  "tool": "recent-requests",
  "parameters": {
    "path": "/content/wknd",
    "count": 10
  }
}
```

**Returns:**
- Request URL and method
- Sling resource resolution chain
- Servlet/script selection
- Processing time breakdown

---

## Dispatcher MCP Server

### Prerequisites

- Docker Desktop 4.x or later
- AEM Dispatcher SDK from Software Distribution Portal

### Installation

**macOS/Linux:**

```bash
# Extract Dispatcher SDK
chmod +x aem-sdk-dispatcher-tools-<version>-unix.sh
./aem-sdk-dispatcher-tools-<version>-unix.sh

# Navigate to extracted directory
cd dispatcher-sdk-<version>

# Make MCP script executable
chmod +x ./bin/docker_run_mcp.sh

# Test the MCP server
./bin/docker_run_mcp.sh test
```

**Windows:**

```powershell
# Extract using the Windows installer
.\aem-sdk-dispatcher-tools-<version>-windows.exe

# Navigate to extracted directory
cd dispatcher-sdk-<version>

# Test the MCP server
.\bin\docker_run_mcp.ps1 test
```

### Available Tools

| Tool | Description |
|------|-------------|
| `validate` | Configuration syntax and best-practice validation |
| `lint` | Mode-aware static analysis |
| `sdk` | Workflow execution (validate, docker-test, diff-baseline, etc.) |
| `trace_request` | Runtime request behavior analysis |
| `inspect_cache` | Cache and docroot examination |
| `monitor_metrics` | Log metrics extraction |
| `tail_logs` | Real-time log streaming |

### Tool: validate

Validates Dispatcher configuration:

```json
{
  "tool": "validate",
  "parameters": {
    "config_path": "./dispatcher/src"
  }
}
```

### Tool: trace_request

Traces how a request is processed:

```json
{
  "tool": "trace_request",
  "parameters": {
    "url": "/content/wknd/us/en.html",
    "method": "GET"
  }
}
```

**Returns:**
- Filter chain processing
- Cache decisions (hit/miss/invalidation)
- Backend routing
- Header transformations

### Tool: inspect_cache

Examines cache state:

```json
{
  "tool": "inspect_cache",
  "parameters": {
    "path": "/content/wknd"
  }
}
```

**Returns:**
- Cached files list
- Cache headers
- TTL information
- Invalidation state

---

## IDE Configuration

### Cursor

Create or update `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "aem-cs-sdk": {
      "type": "streamable-http",
      "url": "http://localhost:4502/bin/mcp",
      "headers": {
        "Authorization": "Basic YWRtaW46YWRtaW4="
      }
    },
    "aem-dispatcher-mcp": {
      "command": "/path/to/dispatcher-sdk/bin/docker_run_mcp.sh",
      "env": {
        "DOCKER_API_VERSION": "1.43",
        "AEM_DEPLOYMENT_MODE": "cloud",
        "MCP_LOG_LEVEL": "trace",
        "MCP_LOG_FILE": "/tmp/dispatcher-mcp.log",
        "DISPATCHER_CONFIG_PATH": "/path/to/your/project/dispatcher/src"
      }
    }
  }
}
```

**Configuration Notes:**
- `Authorization`: Base64 encoded `admin:admin`
- `DISPATCHER_CONFIG_PATH`: Absolute path to your dispatcher source

### GitHub Copilot (IntelliJ)

1. Navigate to **Tools > GitHub Copilot > Model Context Protocol**
2. Click **Add MCP Server**
3. Enter configuration:

   **AEM Quickstart:**
   ```
   Name: aem-cs-sdk
   Type: HTTP
   URL: http://localhost:4502/bin/mcp
   Headers: Authorization: Basic YWRtaW46YWRtaW4=
   ```

   **Dispatcher:**
   ```
   Name: aem-dispatcher-mcp
   Type: Command
   Command: /path/to/dispatcher-sdk/bin/docker_run_mcp.sh
   ```

### Claude Code

Claude Code supports MCP servers via configuration:

```bash
# Add AEM MCP server
claude mcp add aem-cs-sdk --type http \
  --url http://localhost:4502/bin/mcp \
  --header "Authorization: Basic YWRtaW46YWRtaW4="

# Add Dispatcher MCP server
claude mcp add aem-dispatcher-mcp --type command \
  --command /path/to/dispatcher-sdk/bin/docker_run_mcp.sh
```

---

## Usage Examples

### Debug a Bundle Not Activating

```
AI: Check why bundle com.wknd.core isn't activating

[Uses diagnose-osgi-bundle tool]

Response: Bundle com.wknd.core is in INSTALLED state.
Missing packages:
- org.apache.sling.api.resource (version 2.12.0)
Unsatisfied references:
- ResourceResolverFactory (no service available)

Recommendation: Add dependency on org.apache.sling.api
```

### Trace a 404 Error

```
AI: Why is /content/wknd/us/en/about.html returning 404?

[Uses recent-requests tool]
[Uses trace_request tool]

Response: Request traced through Dispatcher:
1. Filter: /filter { /glob "*" /type "allow" } - PASSED
2. Cache check: MISS (no cached file)
3. Backend request: http://localhost:4502/content/wknd/us/en/about.html
4. Backend response: 404

Root cause: Page doesn't exist in AEM. Check content path.
```

### Analyze Cache Performance

```
AI: How is caching working for my site?

[Uses inspect_cache tool]
[Uses monitor_metrics tool]

Response: Cache analysis for /content/wknd:
- Total cached files: 2,847
- Cache hit ratio: 78%
- Average TTL: 300 seconds
- Recent invalidations: 3 in last hour

Recommendations:
1. Increase TTL for static assets
2. Add Vary header handling for personalized content
```

---

## Environment Variables

### Dispatcher MCP Server

| Variable | Description | Default |
|----------|-------------|---------|
| `DOCKER_API_VERSION` | Docker API version | 1.43 |
| `AEM_DEPLOYMENT_MODE` | cloud or onprem | cloud |
| `MCP_LOG_LEVEL` | trace, debug, info, warn, error | info |
| `MCP_LOG_FILE` | Log file path | stdout |
| `DISPATCHER_CONFIG_PATH` | Path to dispatcher src | required |

---

## Troubleshooting

### MCP Server Not Responding

1. **Verify AEM is running:**
   ```bash
   curl -u admin:admin http://localhost:4502/system/console/bundles.json
   ```

2. **Check MCP package installed:**
   ```bash
   curl -u admin:admin http://localhost:4502/bin/mcp
   ```

3. **Verify network connectivity:**
   ```bash
   nc -zv localhost 4502
   ```

### Dispatcher MCP Issues

1. **Check Docker is running:**
   ```bash
   docker info
   ```

2. **Verify Dispatcher image:**
   ```bash
   docker images | grep dispatcher
   ```

3. **Check script permissions:**
   ```bash
   ls -la /path/to/dispatcher-sdk/bin/docker_run_mcp.sh
   ```

### Authentication Errors

Ensure base64 encoding is correct:

```bash
# Generate auth header
echo -n "admin:admin" | base64
# Output: YWRtaW46YWRtaW4=
```

For different credentials:
```bash
echo -n "username:password" | base64
```

---

## Security Considerations

### Development Only

> **Warning**: MCP servers are intended for **local development only**.

- Never expose MCP endpoints to public networks
- Use strong credentials for production-like environments
- Review all commands before execution
- Rotate credentials after demos

### Network Isolation

Configure firewall rules:

```bash
# Only allow localhost access
iptables -A INPUT -p tcp --dport 4502 -s 127.0.0.1 -j ACCEPT
iptables -A INPUT -p tcp --dport 4502 -j DROP
```

---

## Next Steps

Continue to [IDE Configuration](./04-ide-configuration.md)
