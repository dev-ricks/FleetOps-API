# API Usage and Documentation

## Swagger/OpenAPI

- Swagger UI:
  - http://localhost:8080/swagger-ui.html
- OpenAPI:
  - JSON: http://localhost:8080/v3/api-docs
  - YAML: http://localhost:8080/v3/api-docs.yaml

The repo also includes `docs/openapi.yml`. Keep it aligned with controller changes. You can generate clients or server stubs if desired.

## Conventions

- JSON over HTTP(s).
- Standard HTTP status codes:
  - 200/201 for success
  - 400 for validation errors (ConstraintViolation mapped)
  - 401/403 for authZ failures
  - 404 for missing resources
  - 409 for conflicts where applicable
  - 500 for unexpected errors
- Error responses are structured JSON as defined by exception handlers.

## Authentication

- Bearer JWT tokens required on protected endpoints.
- Provide tokens in `Authorization: Bearer <token>` header.

## Versioning

- Prefer URL or header-based versioning if/when needed (e.g., `/api/v1/...`).
- Reflect version info in `docs/openapi.yml` and Swagger metadata.

## Operational Endpoints (Actuator)

Standard service health and info endpoints are exposed via Spring Boot Actuator:

- Health: http://localhost:8080/actuator/health
- Liveness: http://localhost:8080/actuator/health/liveness
- Readiness: http://localhost:8080/actuator/health/readiness
- Info: http://localhost:8080/actuator/info

See `src/main/resources/application.yml` under `management.*` for exposure configuration and security.

## Code-level Javadoc

Generate and view Javadoc (Windows PowerShell):

```powershell
mvn -q -DskipTests javadoc:javadoc
Start-Process "D:\work\source\java\FleetOps-API\target\site\apidocs\index.html"
```
