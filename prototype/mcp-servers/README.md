# MCP Server Configuration for AEM Development
# Model Context Protocol (MCP) - Standardized tool access for AI agents

## AEM MCP Server

```yaml
# mcp-servers/aem-mcp.yaml
name: aem-mcp
version: 1.0.0
description: AEM as a Cloud Service MCP Server

capabilities:
  # Deploy package to AEM
  - name: deploy
    description: Deploy content package to AEM
    params:
      - name: packagePath
        type: string
        required: true
      - name: environment
        type: string
        default: author
        options: [author, publish]

  # Query JCR content
  - name: query
    description: Execute JCR query
    params:
      - name: statement
        type: string
        required: true
      - name: language
        type: string
        default: JCR-SQL2
        options: [JCR-SQL2, XPATH, JQOM]

  # Workflow operations
  - name: workflow
    description: Trigger or query workflow
    params:
      - name: action
        type: string
        required: true
        options: [start, terminate, query]
      - name: modelId
        type: string
      - name: payloadPath
        type: string

  # Package install
  - name: install
    description: Install content package
    params:
      - name: groupId
        type: string
      - name: artifactId
        type: string
      - name: version
        type: string

config:
  # AEM Cloud Service
  aem_host: ${AEM_HOST:-localhost}
  aem_port: ${AEM_PORT:-4502}
  aem_user: ${AEM_USER:-admin}
  aem_password: ${AEM_PASSWORD:-admin}
  
  # Cloud Manager (for deployment)
  cloud_manager_org: ${CM_ORG}
  cloud_manager_api_key: ${CM_API_KEY}

# Example usage with Goose
# goose run --task "Deploy package to AEM" --mcp aem-mcp
```

## JIRA MCP Server

```yaml
# mcp-servers/jira-mcp.yaml
name: jira-mcp
version: 1.0.0
description: JIRA MCP Server

capabilities:
  - name: create_issue
    params:
      - name: project
        type: string
      - name: summary
        type: string
      - name: description
        type: string
      - name: issueType
        type: string
        default: Task

  - name: transition_issue
    params:
      - name: issueKey
        type: string
      - name: transition
        type: string

  - name: add_comment
    params:
      - name: issueKey
        type: string
      - name: comment
        type: string

config:
  jira_host: ${JIRA_HOST}
  jira_user: ${JIRA_USER}
  jira_token: ${JIRA_TOKEN}
```

## Slack MCP Server

```yaml
# mcp-servers/slack-mcp.yaml
name: slack-mcp
version: 1.0.0
description: Slack MCP Server

capabilities:
  - name: send_message
    params:
      - name: channel
        type: string
      - name: text
        type: string
      - name: thread
        type: string

  - name: create_channel
    params:
      - name: name
        type: string
      - name: description
        type: string

config:
  slack_token: ${SLACK_TOKEN}
  default_channel: "#aem-dev"
```

---

## Integration with GasTown

```yaml
# .gastown/config.yaml
mcp_servers:
  - name: aem
    config: mcp-servers/aem-mcp.yaml
    
  - name: jira
    config: mcp-servers/jira-mcp.yaml
    
  - name: slack
    config: mcp-servers/slack-mcp.yaml

# Example workflow using MCP
workflows:
  deploy-and-notify:
    steps:
      - name: deploy
        tool: mcp.aem.deploy
        packagePath: target/core-1.0.0.zip
        environment: author
        
      - name: notify
        tool: mcp.slack.send_message
        channel: "#deployments"
        text: "Deployed successfully to AEM"
```
