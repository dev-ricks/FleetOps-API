# Contributing

## Branching and Workflow

- Main branch is protected.
- Use feature branches: `feature/<short-description>`, `fix/<short-description>`, `chore/<short-description>`, `documentation/<short-description>`, `ci-cd/<short-description>`.
- Open PRs early; prefer small, focused changes.

## Commit Messages

- Follow this template exactly:

```
<category>(<scope>): <short summary>

# Body (optional):
# - Explain the reason for the change
# - Provide additional context
# - Reference related issues/tickets (e.g. "Closes #123")
```

- Categories (choose one):
  - `feature`             – a new feature
  - `fix`                 – a bug fix
  - `performance`         – performance improvements
  - `refactor`            – restructuring code without changing behavior
  - `test`                – adding or improving tests
  - `documentation`       – documentation changes (JavaDocs, README, API docs)
  - `build`               – build system changes (Maven, Gradle, Docker, etc.)
  - `ci-cd`               – CI/CD pipeline changes
  - `chore`               – routine maintenance (dependencies, cleanup, no code logic changes)
  - `configuration`       – application or system configuration changes
  - `infrastructure`      – infrastructure changes (Terraform, AWS, Kubernetes, networking, etc.)
  - `security`            – authentication, authorization, or vulnerability fixes
  - `style`               – formatting, naming, style-only changes
  - `revert`              – reverting a previous commit

- `<scope>` is optional but recommended, e.g. `api`, `db`, `security`, `docs`.

- Examples:
  - `feature(api): add /vehicles/scores endpoint`
  - `fix(validation): normalize license plate input`
  - `documentation(docs): add actuator and javadoc instructions`
  - `ci-cd(workflow): add Maven build and test pipeline`

### Optional: Configure a commit message template

We provide `.gitmessage.txt` at the repo root. To use it (Windows PowerShell):

```powershell
git config commit.template .gitmessage.txt
```

## Code Style

- Follow standard Java/Spring conventions.
- Use Lombok where configured; document any non-obvious Lombok usage in Javadoc.
- Add Javadoc for public classes and critical methods.

### Principles

- Minimalism: keep code concise, avoid unnecessary verbosity, and remove dead code.
- Self-descriptive naming: choose clear class/method/variable names so code explains itself.
- Structure over spacing: avoid inserting extra blank lines to separate minor blocks; let logical grouping and small methods convey structure.
- DRY and Clean Code: do not repeat yourself; factor common logic and prefer clarity over cleverness.
- Comments conservatively: add comments only when they provide context not obvious from good naming and structure.
- Ternary operator (`?:`): use only when it expresses an obvious this-or-that choice; otherwise prefer `if` statements for readability.
- One thing per line: if a line does more than one conceptual thing, extract variables and/or methods to make each step explicit.
- Single Responsibility: each component, class, and method should do one thing and do it well; keep methods small and cohesive.

## Tests

We follow clear patterns for professional, maintainable tests.

### Unit tests (AAA: Arrange–Act–Assert)
- Structure each test into three clearly labeled blocks (use comments `// Arrange`, `// Act`, `// Assert`).
- Keep tests deterministic and isolated; prefer mocks/stubs for dependencies.
- Name tests descriptively: `method_underTest_condition_expectedOutcome`.
- Assert one behavior per test where practical; avoid asserting unrelated concerns in a single test.
- Cover edge cases, error paths, and input validation.

### Integration tests (GWT: Given–When–Then)
- Organize tests with comments `// Given`, `// When`, `// Then` to document scenario flow.
- Use realistic data and the real wiring (e.g., Spring context, H2 DB) where appropriate.
- Prefer stable, idempotent setups and teardown; avoid test inter-dependencies.
- Validate request/response mappings, validation rules, security boundaries, and persistence.

### General testing guidance
- Prefer readability over cleverness in tests; tests are documentation.
- Mock only what you own; for Spring services, mock collaborator services/repositories as needed.
- Keep performance reasonable; tests must be fast and reliable in CI.
- Track security-related scenarios (authZ/authN) with `spring-security-test` where relevant.
- `mvn verify` should pass before requesting review.

## PR Review Checklist

Use this checklist to ensure quality, consistency, and readiness for merge.

- [ ] Scope & Clarity
  - The PR has a clear purpose and limited scope; description explains the change.
  - Breaking changes are explicitly called out with migration steps.

- [ ] Commit Standards
  - Commit messages follow `<category>(<scope>): <short summary>` with approved categories.
  - Commits are focused; no unrelated changes.

- [ ] Build & CI
  - `mvn verify` passes locally.
  - GitHub Actions build is green; no flaky tests.

- [ ] Testing Quality
  - Unit tests follow AAA (Arrange–Act–Assert) with clear comments and cover happy paths and edge cases.
  - Integration tests follow GWT (Given–When–Then) with clear scenario comments.
  - Tests are deterministic, isolated, and fast; mocks/stubs used appropriately.
  - Security-related scenarios (authN/authZ) covered where applicable.

- [ ] Documentation
  - README and relevant docs updated as needed (e.g., features, configuration, endpoints).
  - Javadoc added/updated for public types and critical methods.
  - API changes reflected in `docs/openapi.yml` and annotations.

- [ ] API & Validation
  - Request/response models validated (Bean Validation) and mapped correctly.
  - Errors are handled by `GlobalControllerExceptionHandler` with consistent structure.

- [ ] Data & Migrations
  - Liquibase changesets included for schema changes; rollback considered.
  - Queries reviewed for performance (indexes, N+1 avoidance) and correctness.

- [ ] Security & Privacy
  - Authentication/authorization rules enforced as intended.
  - No sensitive data logged; configs/secrets handled securely.

- [ ] Performance & Reliability
  - Critical paths assessed for efficiency; avoid premature optimization.
  - Idempotency and error handling considered for write operations.

- [ ] Configuration & Environments
  - New properties documented (README/SECURITY) and have sensible defaults.
  - Docker/Compose updated if needed.

- [ ] Observability & Logging
  - Logging is actionable and minimal; no noisy or sensitive logs.
  - Actuator/metrics considerations if applicable.

- [ ] Style & Code Quality
  - Code adheres to Code Style Principles (minimalism, naming, DRY, single responsibility).
  - No dead code; comments used only where necessary.

- [ ] Risk & Rollback
  - Risks identified with mitigation/rollback plan if needed.

