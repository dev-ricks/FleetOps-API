# Contributing

## Branching and Workflow

- Main branch is protected.
- Use feature branches: `feat/<short-description>`, `fix/<short-description>`, `chore/<short-description>`.
- Open PRs early; prefer small, focused changes.

## Commit Messages

- Use conventional commits where possible:
  - `feat: add vehicle assignment endpoint`
  - `fix: correct validation on driver license`
  - `test: add service layer unit tests`
  - `chore: bump springdoc to 2.8.0`

## Code Style

- Follow standard Java/Spring conventions.
- Use Lombok where configured; document any non-obvious Lombok usage in Javadoc.
- Add Javadoc for public classes and critical methods.

## Tests

- Ensure unit tests for services and repositories where applicable.
- Controller tests for request/response mapping and validation.
- Keep H2-based integration tests fast and reliable.
- `mvn verify` should pass before requesting review.

## PR Review Checklist

- [ ] Code compiles and tests pass
- [ ] New endpoints documented (Swagger annotations and/or `docs/openapi.yml`)
- [ ] Validation and error mappings covered by tests
- [ ] Liquibase changesets included (if schema changed)
- [ ] Security/authorization considered and tested
