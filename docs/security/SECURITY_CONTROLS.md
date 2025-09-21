# Security Controls Inventory

This document provides an inventory of the key security controls implemented or planned for the FleetOps API. Each control is mapped to a specific requirement, and its implementation status is tracked to ensure comprehensive coverage and accountability.

| Control ID    | Description                      | Requirement Reference | Implementation Status |
|---------------|----------------------------------|-----------------------|-----------------------|
| CTRL-API-01   | OAuth2 Resource Server           | REQ-AUTH-001          | Implemented           |
| CTRL-AUTH-01  | Token Management                 | REQ-AUTH-002          | Planned               |
| CTRL-AUTHZ-01 | Role-Based Access Control (RBAC) | REQ-AUTHZ-001         | Implemented           |
| CTRL-DATA-01  | Database Encryption              | REQ-DATA-001          | Planned               |
| CTRL-DATA-02  | TLS 1.2+ Enforcement             | REQ-DATA-002          | Implemented           |
| CTRL-INPUT-01 | Input Validation                 | REQ-INPUT-001         | Implemented           |
| CTRL-API-02   | Rate Limiting                    | REQ-API-001           | Planned               |
| CTRL-MON-01   | Security Event Logging           | REQ-MON-001           | Planned               |
| CTRL-IR-01    | Incident Response Playbooks      | REQ-IR-002            | Planned               |

Each control is described in terms of its purpose, the requirement it addresses, and its current implementation status. This inventory is reviewed regularly to ensure all critical security needs are met and to track progress on planned controls.
