# Security

## Overview

This service is an OAuth2 Resource Server validating JWT bearer tokens.

## Configuration

You can configure JWT validation using either:
- `spring.security.oauth2.resourceserver.jwt.issuer-uri=https://<issuer>/`
- or `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://<issuer>/.well-known/jwks.json`

Other recommended properties:
- `server.forward-headers-strategy=framework` if behind a proxy
- Ensure HTTPS is enforced in production

## Data Protection

- Secrets should be managed via environment variables or a secret manager; never commit secrets to VCS.
- Avoid logging sensitive data (tokens, PII).
- Consider encrypting at rest (DB-level) and in transit (TLS).

## Validation and Errors

- Bean Validation guards inputs at the edge; violations are mapped to 400 responses.
- Centralized exception handling returns structured error bodies.

## Dependencies

- Keep dependencies updated. Address CVEs promptly.
- Run `mvn versions:display-dependency-updates` periodically.

## Incident Response

- Rotate credentials and tokens if compromise is suspected.
- Review logs and audit trails for unusual access.
