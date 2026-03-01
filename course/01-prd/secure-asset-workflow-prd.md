# Product Requirements Document (PRD)
# Secure Asset Approval Workflow for AEM

**Document Version:** 1.0
**Last Updated:** 2024-02-28
**Status:** Approved
**Owner:** Digital Asset Management Team

---

## Executive Summary

### Problem Statement

Organizations managing digital assets in AEM face significant security and governance challenges:

1. **Security Risk**: Uploaded files may contain malware that can compromise systems or spread to downstream consumers
2. **Compliance Gap**: No automated verification of assets before publication
3. **Approval Bottlenecks**: Manual, email-based approval processes cause delays
4. **Audit Trail Gaps**: Difficulty tracking who approved what and when
5. **Inconsistent Routing**: Assets not reaching appropriate reviewers based on content type

### Proposed Solution

Implement a **Secure Asset Approval Workflow** that automatically:
- Scans all uploaded assets for malware before any human interaction
- Routes clean assets through a configurable multi-level approval process
- Quarantines infected files and alerts security teams
- Maintains complete audit trails for compliance
- Dynamically assigns reviewers based on asset characteristics

### Success Metrics

| Metric | Current State | Target | Measurement |
|--------|---------------|--------|-------------|
| Malware Detection Rate | 0% (no scanning) | 100% | Scan completion rate |
| Approval Cycle Time | 5-7 days | 24-48 hours | Workflow duration |
| Compliance Audit Score | 60% | 95% | Quarterly audit |
| False Positive Rate | N/A | <1% | Quarantine review |
| User Satisfaction | 3.2/5 | 4.5/5 | Post-approval survey |

---

## Stakeholders

### Primary Stakeholders

| Role | Name/Team | Responsibility |
|------|-----------|----------------|
| Product Owner | Digital Asset Manager | Final approval, prioritization |
| Security Lead | InfoSec Team | Security requirements, AV integration |
| Content Operations | Marketing Ops | Workflow design, user acceptance |
| IT Architect | Enterprise Architecture | Technical design, integration |

### Secondary Stakeholders

| Role | Interest |
|------|----------|
| Content Authors | Upload experience, notification clarity |
| Reviewers/Approvers | Task management, decision interface |
| Legal/Compliance | Audit trail, retention policies |
| External Vendors | Integration requirements |

---

## User Stories

### Epic 1: Malware Protection

#### US-1.1: Automatic Virus Scanning
**As a** security administrator
**I want** all uploaded assets to be automatically scanned for malware
**So that** infected files never reach reviewers or get published

**Acceptance Criteria:**
- [ ] AC-1.1.1: Every asset uploaded to designated folders triggers a scan
- [ ] AC-1.1.2: Scan completes within 60 seconds for files up to 100MB
- [ ] AC-1.1.3: Scan results are stored as asset metadata
- [ ] AC-1.1.4: Workflow proceeds only after scan completion
- [ ] AC-1.1.5: System handles scanner unavailability gracefully

#### US-1.2: Quarantine Infected Files
**As a** security administrator
**I want** infected files moved to a secure quarantine location
**So that** they cannot be accessed or accidentally published

**Acceptance Criteria:**
- [ ] AC-1.2.1: Infected files moved to `/content/dam/quarantine/YYYY/MM/DD/`
- [ ] AC-1.2.2: Original file path preserved in metadata
- [ ] AC-1.2.3: Threat name and scan details recorded
- [ ] AC-1.2.4: Access restricted to security-admins group only
- [ ] AC-1.2.5: Quarantined files auto-deleted after 90 days (configurable)

#### US-1.3: Security Notifications
**As a** security administrator
**I want** immediate notification when malware is detected
**So that** I can investigate and take additional protective measures

**Acceptance Criteria:**
- [ ] AC-1.3.1: Email sent to security-admins within 1 minute of detection
- [ ] AC-1.3.2: Notification includes: filename, threat name, uploader, timestamp
- [ ] AC-1.3.3: Link to quarantine location provided
- [ ] AC-1.3.4: Optional Slack/Teams integration

---

### Epic 2: Multi-Level Approval

#### US-2.1: Content-Type Based Routing
**As a** content manager
**I want** assets routed to appropriate reviewers based on their type
**So that** specialists review content they understand

**Acceptance Criteria:**
- [ ] AC-2.1.1: Images route to `image-reviewers` group
- [ ] AC-2.1.2: Videos route to `video-reviewers` group
- [ ] AC-2.1.3: Documents route to `document-reviewers` group
- [ ] AC-2.1.4: Unknown types route to `content-reviewers` (fallback)
- [ ] AC-2.1.5: Routing rules configurable via OSGi

#### US-2.2: Level 1 Review
**As a** content reviewer
**I want** to review assets in my specialty area
**So that** I can approve, reject, or request revisions

**Acceptance Criteria:**
- [ ] AC-2.2.1: Reviewer sees pending tasks in AEM Inbox
- [ ] AC-2.2.2: Asset preview available without downloading
- [ ] AC-2.2.3: Three actions available: Approve, Reject, Request Revision
- [ ] AC-2.2.4: Comments required for Reject and Request Revision
- [ ] AC-2.2.5: Decision recorded with timestamp and user

#### US-2.3: Level 2 Manager Review
**As a** content manager
**I want** to review assets that passed Level 1
**So that** I can provide management approval or escalate

**Acceptance Criteria:**
- [ ] AC-2.3.1: Manager assigned based on asset metadata or folder
- [ ] AC-2.3.2: Level 1 reviewer comments visible
- [ ] AC-2.3.3: Four actions: Approve, Reject, Escalate to Director, Delegate
- [ ] AC-2.3.4: Large files (>50MB) auto-escalate to senior approvers
- [ ] AC-2.3.5: Brand assets route to brand-managers group

#### US-2.4: Level 3 Director Review
**As a** content director
**I want** to make final decisions on escalated or sensitive assets
**So that** high-risk content gets appropriate oversight

**Acceptance Criteria:**
- [ ] AC-2.4.1: Only escalated items reach Level 3
- [ ] AC-2.4.2: Full approval history visible
- [ ] AC-2.4.3: Two actions: Approve, Reject (final)
- [ ] AC-2.4.4: Cannot delegate at this level

---

### Epic 3: Notifications & Communication

#### US-3.1: Approval Request Notifications
**As a** reviewer
**I want** to be notified when I have items to review
**So that** I can act promptly

**Acceptance Criteria:**
- [ ] AC-3.1.1: Email notification within 5 minutes of assignment
- [ ] AC-3.1.2: Notification includes: asset name, requestor, deadline
- [ ] AC-3.1.3: Direct link to review task in AEM
- [ ] AC-3.1.4: Daily digest option for high-volume reviewers

#### US-3.2: Status Update Notifications
**As a** content author
**I want** to know the status of my uploaded assets
**So that** I can plan accordingly and address issues

**Acceptance Criteria:**
- [ ] AC-3.2.1: Notification on each status change (scan complete, approved, rejected)
- [ ] AC-3.2.2: Rejection includes feedback comments
- [ ] AC-3.2.3: Revision requests include specific guidance
- [ ] AC-3.2.4: Final approval includes publication confirmation

#### US-3.3: Escalation Notifications
**As a** manager
**I want** to know when items are escalated past me
**So that** I understand why my approval wasn't sufficient

**Acceptance Criteria:**
- [ ] AC-3.3.1: Escalation notification to original approvers
- [ ] AC-3.3.2: Reason for escalation included
- [ ] AC-3.3.3: Final outcome notification sent

---

### Epic 4: Audit & Compliance

#### US-4.1: Complete Audit Trail
**As a** compliance officer
**I want** a complete record of all workflow actions
**So that** I can demonstrate regulatory compliance

**Acceptance Criteria:**
- [ ] AC-4.1.1: Every action logged with user, timestamp, decision
- [ ] AC-4.1.2: Scan results preserved for asset lifetime
- [ ] AC-4.1.3: Audit logs exportable to CSV/JSON
- [ ] AC-4.1.4: Logs retained for 7 years minimum
- [ ] AC-4.1.5: Tamper-evident logging (checksums)

#### US-4.2: Compliance Reporting
**As a** compliance officer
**I want** to generate compliance reports
**So that** I can satisfy audit requirements

**Acceptance Criteria:**
- [ ] AC-4.2.1: Report on workflow completion rates
- [ ] AC-4.2.2: Report on average approval times by level
- [ ] AC-4.2.3: Report on rejection reasons
- [ ] AC-4.2.4: Report on quarantine activity

---

### Epic 5: Publication

#### US-5.1: Automatic Publication
**As a** content author
**I want** approved assets automatically published
**So that** they're immediately available for use

**Acceptance Criteria:**
- [ ] AC-5.1.1: Assets replicated to publish instance on final approval
- [ ] AC-5.1.2: Publication confirmation email sent
- [ ] AC-5.1.3: Publication failure triggers retry (3 attempts)
- [ ] AC-5.1.4: Failed publication alerts content operations

---

## Functional Requirements

### FR-1: Antivirus Integration

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1.1 | Integrate with ClamAV daemon via TCP socket | P1 |
| FR-1.2 | Support fallback to REST API scanning service | P2 |
| FR-1.3 | Provide mock scanning mode for development | P1 |
| FR-1.4 | Configure maximum file size for scanning | P1 |
| FR-1.5 | Handle scanner timeout gracefully | P1 |
| FR-1.6 | Support scanning of all common file types | P1 |

### FR-2: Workflow Engine

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-2.1 | Implement as AEM Workflow Model | P1 |
| FR-2.2 | Support configurable approval levels (1-3) | P1 |
| FR-2.3 | Enable parallel approval paths | P2 |
| FR-2.4 | Support workflow delegation | P2 |
| FR-2.5 | Provide workflow restart capability | P3 |
| FR-2.6 | Support bulk approval actions | P3 |

### FR-3: Participant Assignment

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-3.1 | Route by MIME type to specialized groups | P1 |
| FR-3.2 | Route by folder path (brand, legal, etc.) | P1 |
| FR-3.3 | Route by asset metadata (sensitivity level) | P2 |
| FR-3.4 | Support file size-based escalation | P2 |
| FR-3.5 | Allow manual participant override | P3 |

### FR-4: Notifications

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-4.1 | Send email notifications via AEM Mail Service | P1 |
| FR-4.2 | Support configurable notification templates | P2 |
| FR-4.3 | Enable Slack/Teams webhook integration | P3 |
| FR-4.4 | Provide notification preferences per user | P3 |

---

## Non-Functional Requirements

### NFR-1: Performance

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-1.1 | Scan completion time | <60s for 100MB file |
| NFR-1.2 | Workflow step transition | <5s |
| NFR-1.3 | Notification delivery | <5 minutes |
| NFR-1.4 | Concurrent workflow instances | 100+ |

### NFR-2: Reliability

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-2.1 | Workflow completion rate | >99% |
| NFR-2.2 | Scanner availability | >99.5% |
| NFR-2.3 | Data loss prevention | Zero workflow data loss |
| NFR-2.4 | Recovery from failure | Auto-retry + manual resume |

### NFR-3: Security

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-3.1 | Quarantine access control | Admin-only |
| NFR-3.2 | Audit log integrity | Tamper-evident |
| NFR-3.3 | Sensitive data handling | No PII in logs |
| NFR-3.4 | Scanner communication | TLS encrypted |

### NFR-4: Scalability

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-4.1 | Daily asset uploads | 10,000+ |
| NFR-4.2 | Concurrent scans | 10+ |
| NFR-4.3 | Historical workflow data | 5+ years |

---

## Technical Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           AEM AUTHOR INSTANCE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐      │
│  │   DAM Upload    │────▶│ Workflow        │────▶│ Replication     │      │
│  │   Listener      │     │ Launcher        │     │ Agent           │      │
│  └─────────────────┘     └────────┬────────┘     └─────────────────┘      │
│                                   │                                        │
│                                   ▼                                        │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                    SECURE ASSET APPROVAL WORKFLOW                   │   │
│  │                                                                     │   │
│  │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐          │   │
│  │   │ AV Scan  │─▶│ Level 1  │─▶│ Level 2  │─▶│ Publish  │          │   │
│  │   │ Process  │  │ Review   │  │ Review   │  │ Process  │          │   │
│  │   └────┬─────┘  └──────────┘  └──────────┘  └──────────┘          │   │
│  │        │                                                           │   │
│  │   ┌────▼─────┐                                                     │   │
│  │   │Quarantine│                                                     │   │
│  │   │ Process  │                                                     │   │
│  │   └──────────┘                                                     │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
           │                                              │
           │ TCP/IP                                       │ HTTP/S
           ▼                                              ▼
┌─────────────────────┐                     ┌─────────────────────┐
│   ClamAV Daemon     │                     │   AEM Publish       │
│   (Docker Container)│                     │   Instance          │
└─────────────────────┘                     └─────────────────────┘
```

### Component Overview

| Component | Type | Responsibility |
|-----------|------|----------------|
| AntivirusScanService | OSGi Service | Scanner integration |
| AntivirusScanProcess | Workflow Process | Scan orchestration |
| QuarantineProcess | Workflow Process | Infected file handling |
| AssetApprovalParticipantChooser | Participant Chooser | Dynamic routing |
| NotificationProcess | Workflow Process | Email/alert sending |
| AuditLogProcess | Workflow Process | Compliance logging |

---

## Integration Points

### External Systems

| System | Integration Type | Purpose |
|--------|------------------|---------|
| ClamAV | TCP Socket | Malware scanning |
| SMTP Server | Email | Notifications |
| Slack/Teams | Webhook | Alerts |
| SIEM | Syslog | Security monitoring |

### Internal Systems

| System | Integration Type | Purpose |
|--------|------------------|---------|
| AEM Inbox | Native | Task management |
| AEM DAM | Native | Asset operations |
| AEM Replication | Native | Publication |
| AEM Users/Groups | Native | Authorization |

---

## Configuration Requirements

### OSGi Configurations

1. **AntivirusScanServiceImpl**
   - Scanner engine selection (ClamAV/REST/Mock)
   - Connection parameters
   - Timeout settings
   - Maximum file size

2. **QuarantineProcess**
   - Quarantine folder path
   - Retention period
   - Notification group

3. **AssetApprovalParticipantChooser**
   - Reviewer group mappings
   - Escalation thresholds
   - Folder-based routing rules

### Workflow Launcher Configuration

| Setting | Value |
|---------|-------|
| Event Type | Node Added (1) |
| Node Type | dam:Asset |
| Glob Pattern | `/content/dam/(secure-assets|uploads|pending-approval)/.*` |
| Run Modes | author |
| Enabled | true |

---

## Acceptance Criteria Summary

### Minimum Viable Product (MVP)

- [ ] Antivirus scanning with ClamAV integration
- [ ] Quarantine process for infected files
- [ ] Two-level approval workflow (reviewer + manager)
- [ ] Email notifications for key events
- [ ] Basic audit logging

### Full Release

- [ ] All MVP features
- [ ] Three-level approval with director escalation
- [ ] Dynamic routing by content type
- [ ] Metadata-based routing (sensitivity, brand)
- [ ] Comprehensive audit reporting
- [ ] Slack/Teams integration

---

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| ClamAV unavailability | Medium | High | Fallback scanning, graceful degradation |
| Scanner false positives | Low | Medium | Quarantine review process, whitelist |
| Approval bottlenecks | Medium | Medium | SLA monitoring, escalation rules |
| Performance degradation | Low | High | Async scanning, resource limits |

---

## Timeline

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| Phase 1: Foundation | 2 weeks | AV scanning, quarantine |
| Phase 2: Workflow | 2 weeks | Multi-level approval |
| Phase 3: Integration | 1 week | Notifications, audit |
| Phase 4: Testing | 1 week | QA, UAT |
| Phase 5: Deployment | 1 week | Production rollout |

**Total Duration: 7 weeks**

---

## Appendices

### Appendix A: Glossary

| Term | Definition |
|------|------------|
| ClamAV | Open-source antivirus engine |
| DAM | Digital Asset Management |
| Quarantine | Secure isolation for infected files |
| Workflow Launcher | Trigger for automatic workflow start |
| Participant Chooser | Logic for dynamic task assignment |

### Appendix B: Related Documents

- AEM Workflow Best Practices
- ClamAV Integration Guide
- AEM Cloud Service Security Guidelines
- Enterprise Content Governance Policy

---

**Document Approval**

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | | | |
| Security Lead | | | |
| Technical Lead | | | |
| QA Lead | | | |
