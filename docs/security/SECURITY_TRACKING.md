# Security Change Tracking – FleetOps API

> Start Date: 2025-11-11  
> Method: One PR per item (see `SECURITY_HARDENING_PLAN.md`)  
> Columns: Date | Phase | Code | Item | Status | PR | Notes

## Legend
Status values: Planned | In-Progress | Done | Deferred | N/A  
Code format: `SEC-P{phase}-{nn}` or `SEC-OPT-{nn}` for stretch items.

## Tracking Table
| Date | Phase | Code | Item | Status | PR | Notes |
|------|-------|------|------|--------|----|-------|
| 2025-11-11 | P1 | SEC-P1-01 | Add HSTS header | In-Progress |  | Branch: security/phase1_foundation/SEC-P1-01 |
| 2025-11-11 | P1 | SEC-P1-02 | Add CSP (report-only) | Planned |  | Collect violation reports first |
| 2025-11-11 | P1 | SEC-P1-03 | Add X-Content-Type-Options | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-04 | Add X-Frame-Options DENY | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-05 | Add Referrer-Policy | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-06 | Decide on X-XSS-Protection | Planned |  | Likely omit; legacy header |
| 2025-11-11 | P1 | SEC-P1-07 | Enforce HTTPS redirect | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-08 | Restrict actuator endpoints | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-09 | Implement Redis rate limiting | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-10 | Audit logging for auth events | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-11 | Correlation ID filter | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-12 | Add DTO validation constraints | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-13 | Global request body size limit | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-14 | Inject Clock abstraction | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-15 | Disable default error page | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-16 | Secure cookie baseline config | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-17 | Add CycloneDX SBOM plugin | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-18 | Add OWASP Dependency-Check | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-19 | Maven Enforcer rules | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-20 | License references in SBOM | Planned |  |  |
| 2025-11-11 | P1 | SEC-P1-21 | Create tracking file | Done | #PR_LINK | Initial

<!-- Additional rows for Phases 2-4 and optional items will be appended as work progresses. -->

## Notes
- Update this table immediately after merging each item.  
- Replace `#PR_LINK` with real PR numbers (e.g., `#45`).  
- For deferred items, briefly justify (e.g., infra-managed, not applicable).  

## Upcoming (Next 5 Planned)
1. SEC-P1-01 HSTS header (profile-based).  
2. SEC-P1-03 nosniff header.  
3. SEC-P1-04 frame options.  
4. SEC-P1-05 referrer policy.  
5. SEC-P1-11 correlation ID filter.  

## Change Log
| Date | Change | Author |
|------|--------|--------|
| 2025-11-11 | File created | automation |
| 2025-11-11 | Mark SEC-P1-01 In-Progress with branch | automation |
