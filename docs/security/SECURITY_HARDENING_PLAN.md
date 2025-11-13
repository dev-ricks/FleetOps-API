# FleetOps API Security Hardening Plan

> Version: 1.0  
> Generated: 2025-11-11  
> Horizon: 4 Weeks (Phased)  
> Scope: Application & Build Pipeline (Single Service)  

## 1. Purpose
This document provides a phased, atomic checklist to elevate FleetOps-API to a professional security baseline. Each item is intentionally a single change to enable clear audit trails (one PR per item). 

## 2. Assumptions
- Stack: Spring Boot 3 (Resource Server, JWT), Java 17, PostgreSQL, Redis, Kafka, Liquibase, Docker Compose.  
- Current state includes basic exception handling and initial security documentation under `docs/security/`.  
- CI pipeline exists (per README badge).  
- No confidential secrets stored in repo—future secret management uses environment variables / vault.

## 3. Success Criteria
By end of Phase 4:  
- Mandatory headers, rate limiting, audit logging, RBAC, JWT hardening implemented.  
- Dependency, container, secret, and SBOM scans automated in CI.  
- Tests cover authz paths + negative JWT scenarios.  
- Tracking file (`SECURITY_TRACKING.md`) reflects completion status for every item.  

## 4. Execution Guidelines
- One PR per checklist item (reference item code in commit message: e.g., `SEC-P1-07`).  
- Update `SECURITY_TRACKING.md` after merge (Status -> Done + PR link).  
- If an item is N/A, mark and briefly justify.  
- Avoid combining changes; if blocked, create an issue and move forward.  

## 5. Phase Overview
| Phase | Theme | Focus |
|-------|-------|-------|
| 1 | Foundation & Baseline | Headers, exposure, input validation, tooling baseline |
| 2 | AuthZ & Data Protection | Role/scope enforcement, data integrity & confidentiality |
| 3 | Observability & Assurance | Testing, scanning, structured security telemetry |
| 4 | Advanced Hardening & Compliance | Adaptive controls, provenance, governance |

---
## 6. Detailed Checklist
Each item is atomic. Use markdown checkbox to track locally; mirror status in `SECURITY_TRACKING.md`.

### Phase 1 – Foundation & Baseline (Week 1)
- [X] SEC-P1-01 Add Strict-Transport-Security (HSTS) (conditional on HTTPS deployment).
- [ ] SEC-P1-02 Add Content-Security-Policy header (report-only mode initial).
- [ ] SEC-P1-03 Add X-Content-Type-Options: nosniff.
- [ ] SEC-P1-04 Add X-Frame-Options: DENY.
- [ ] SEC-P1-05 Add Referrer-Policy: no-referrer.
- [ ] SEC-P1-06 Decide on X-XSS-Protection (document decision; add only if scanner requires).
- [ ] SEC-P1-07 Enforce HTTPS redirect (requiresChannel) for sensitive endpoints.
- [ ] SEC-P1-08 Restrict actuator endpoint exposure (authenticated admin role only).
- [ ] SEC-P1-09 Implement Redis-backed rate limiting for public API endpoints.
- [ ] SEC-P1-10 Add centralized audit logging for auth success/failure events.
- [ ] SEC-P1-11 Implement correlation ID filter (inject + return X-Correlation-ID).
- [ ] SEC-P1-12 Add validation annotations to all request DTO fields lacking constraints.
- [ ] SEC-P1-13 Add global request body size limit (configure server / custom filter).
- [ ] SEC-P1-14 Replace non-injected time usage with java.time + Clock abstraction.
- [ ] SEC-P1-15 Disable default HTML error page (ensure JSON error format only).
- [ ] SEC-P1-16 Explicitly configure secure cookie policy (even if no cookies yet; set baseline).
- [ ] SEC-P1-17 Integrate CycloneDX SBOM generation into Maven build.
- [ ] SEC-P1-18 Add OWASP Dependency-Check plugin to Maven build.
- [ ] SEC-P1-19 Add Maven Enforcer rules (Java version, ban snapshots, dependency convergence).
- [ ] SEC-P1-20 Include license references in SBOM artifact.
- [ ] SEC-P1-21 Create `SECURITY_TRACKING.md` file (tracking template).

### Phase 2 – Authorization & Data Protection (Week 2)
- [ ] SEC-P2-01 Add method-level `@PreAuthorize` for fine-grained access control.
- [ ] SEC-P2-02 Create `RBAC_MATRIX.md` documenting role → permission mapping.
- [ ] SEC-P2-03 Implement custom JWT claim validation (issuer, audience, nbf) converter.
- [ ] SEC-P2-04 Enforce scopes (e.g., `fleet.read`, `fleet.write`) on endpoints.
- [ ] SEC-P2-05 Add per-entity ownership checks in service layer (driver/vehicle visibility).
- [ ] SEC-P2-06 Externalize secrets fully (env vars; remove any placeholders in code).
- [ ] SEC-P2-07 Add JPA AttributeConverter for encryption of sensitive PII fields.
- [ ] SEC-P2-08 Implement secure password hashing utility (future-proofing) using BCrypt/Argon2.
- [ ] SEC-P2-09 Add `RETENTION_POLICY.md` documenting data lifecycle.
- [ ] SEC-P2-10 Implement soft-delete on recoverable entities (if requirements justify).
- [ ] SEC-P2-11 Add optimistic locking (@Version) to entities to prevent lost updates.
- [ ] SEC-P2-12 Cap pagination size (validate page/size params).
- [ ] SEC-P2-13 Implement per-user request throttling (distinct from IP rate limiting).
- [ ] SEC-P2-14 Review and enforce Redis key TTLs for transient data.
- [ ] SEC-P2-15 Add Liquibase changelog integrity measures (e.g., validCheckSum when needed).
- [ ] SEC-P2-16 Configure Hikari leak detection (leakDetectionThreshold).
- [ ] SEC-P2-17 Lock down Jackson polymorphism / disable unsafe default typing.
- [ ] SEC-P2-18 Set Jackson FAIL_ON_UNKNOWN_PROPERTIES (decide strictness; document).
- [ ] SEC-P2-19 Uniform 404/403 strategy to avoid resource enumeration oracle.

### Phase 3 – Observability & Assurance (Week 3)
- [ ] SEC-P3-01 Add dedicated structured security event logger (JSON layout).
- [ ] SEC-P3-02 Add alert threshold for repeated auth failures (stub integration).
- [ ] SEC-P3-03 Export security metrics (login success/failure counters).
- [ ] SEC-P3-04 Add integration tests for RBAC decisions.
- [ ] SEC-P3-05 Add negative JWT tests (expired, wrong signature, audience mismatch).
- [ ] SEC-P3-06 Testcontainers integration test for Redis rate limiting.
- [ ] SEC-P3-07 Testcontainers Kafka auth/ACL simulation stub.
- [ ] SEC-P3-08 Log anomaly if user request count > threshold/window (placeholder heuristic).
- [ ] SEC-P3-09 Extend `API.md` with endpoint-specific security notes.
- [ ] SEC-P3-10 Publish SBOM artifact in CI workflow.
- [ ] SEC-P3-11 Add CI dependency vulnerability scan stage (Dependency-Check/Snyk CLI free tier).
- [ ] SEC-P3-12 Integrate SpotBugs (static analysis) into Maven build.
- [ ] SEC-P3-13 Add secret scanning (gitleaks) to CI.
- [ ] SEC-P3-14 Add container image scan (Trivy) to CI.
- [ ] SEC-P3-15 Harden Dockerfile (non-root user, explicit UID, minimize layers).
- [ ] SEC-P3-16 Require auth for actuator health (except liveness) if not already enforced.
- [ ] SEC-P3-17 Implement log redaction filter for sensitive fields.
- [ ] SEC-P3-18 Propagate correlation ID through Kafka message headers.
- [ ] SEC-P3-19 Cross-reference incident response process in README (link existing plan).
- [ ] SEC-P3-20 Deny TRACE by default; restrict OPTIONS unless needed for CORS.

### Phase 4 – Advanced Hardening & Compliance (Week 4)
- [ ] SEC-P4-01 Enforce (non-report) Content-Security-Policy.
- [ ] SEC-P4-02 Add automated JWT key rotation monitor (JWK metadata TTL check).
- [ ] SEC-P4-03 Implement optional request signature verification layer (ext clients) stub.
- [ ] SEC-P4-04 Add IP allow/deny list for admin endpoints.
- [ ] SEC-P4-05 Implement adaptive rate limiting (dynamic adjustments on abuse).
- [ ] SEC-P4-06 Add Kafka authentication config (SASL/SCRAM or mTLS placeholder).
- [ ] SEC-P4-07 Add defensive input anomaly counting (RASP-style heuristics).
- [ ] SEC-P4-08 Embed build provenance metadata into `/actuator/info` (commit, SBOM hash).
- [ ] SEC-P4-09 Document database encryption-at-rest strategy (infra responsibility note).
- [ ] SEC-P4-10 Add backup integrity verification process to disaster recovery plan.
- [ ] SEC-P4-11 Implement security context invalidation on role/scope changes (cache eviction).
- [ ] SEC-P4-12 Add endpoint (future) to publish server JWK / public key if becomes token issuer.
- [ ] SEC-P4-13 Configure Renovate (or Dependabot) for automated dependency update PRs.
- [ ] SEC-P4-14 Implement tamper-evident logging prototype (hash chain or external sink).
- [ ] SEC-P4-15 Add load/performance test for rate limiter resilience.
- [ ] SEC-P4-16 Update data flow diagram to reflect new encryption & logging points.
- [ ] SEC-P4-17 Add vulnerability triage SLA table (augment requirements spec).
- [ ] SEC-P4-18 Automate scheduled security test run (cron in CI pipeline).
- [ ] SEC-P4-19 Add compliance mapping checklist (SOC2-style) to `SECURITY_CONTROLS.md`.
- [ ] SEC-P4-20 Final review: verify all items logged with status & links.

### Stretch / Optional (Post Month)
- [ ] SEC-OPT-01 Integrate Open Policy Agent (OPA) for externalized authz decisions.
- [ ] SEC-OPT-02 Runtime container threat detection (Falco) plan document.
- [ ] SEC-OPT-03 Multi-tenant isolation strategy doc.
- [ ] SEC-OPT-04 Fuzz testing harness for JSON deserialization.
- [ ] SEC-OPT-05 Chaos security test scenario (token replay, clock skew).
- [ ] SEC-OPT-06 Slack/email vulnerability notification integration.

---
## 7. Tracking Template (Reference for `SECURITY_TRACKING.md`)
Suggested columns:
```
Date | Phase | Code | Item | Status | PR | Notes
2025-11-12 | P1 | SEC-P1-01 | Add HSTS header | Done | #42 | Enabled only on prod profile
```

## 8. Dependency & Tooling Additions Summary
Phase 1: CycloneDX, OWASP Dependency-Check, Maven Enforcer.  
Phase 3: SpotBugs, gitleaks (CI), Trivy (CI).  
Phase 4: Renovate/Dependabot config.  

## 9. Risk Prioritization Rationale
1. Misconfiguration & missing headers (fast to fix, high scanner impact).  
2. Authorization gaps (potential data leakage).  
3. Supply chain & dependency risk (ongoing exposure).  
4. Advanced adaptive protections & provenance (maturity uplift).  

## 10. Rollback Considerations
- Each change isolated; revert by reverting its specific commit/PR.  
- Maintain feature toggles (profiles or config flags) for rate limiters, CSP enforcement, and adaptive controls.  

## 11. Open Questions / To Clarify
- Source of JWT (internal vs external IdP) → influences rotation monitoring depth.  
- Exact PII fields needing encryption (Driver license? personal address?).  
- Production deployment environment capabilities (Ingress-managed headers?).  

## 12. References
- OWASP ASVS v5 Mapping (future)  
- NIST SP 800-53 (selected access control principles)  
- CycloneDX: https://cyclonedx.org/  
- Trivy: https://aquasecurity.github.io/trivy/  
- Gitleaks: https://github.com/gitleaks/gitleaks  

---
## 13. Next Immediate Actions
1. Implement SEC-P1-01 → SEC-P1-04 (headers) in `SecurityConfig`.  
2. Add CycloneDX + Dependency-Check to `pom.xml` (SEC-P1-17, SEC-P1-18).  
3. Create `SECURITY_TRACKING.md` (SEC-P1-21) and start logging progress.  

---
*End of Plan*

