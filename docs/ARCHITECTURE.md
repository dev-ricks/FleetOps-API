# Architecture

## Overview

FleetOps API is a layered Spring Boot service:

- API Layer (Controllers): Request/response mapping, validation annotations, and OpenAPI annotations.
- Service Layer: Business logic, transactions, orchestration.
- Persistence Layer: Spring Data JPA repositories and entities.
- Cross-cutting: Security (JWT resource server), validation, exception handling, and logging.

## Domain Model (High-Level)

- Vehicle: Core asset managed by the system.
- Driver: Operators associated with vehicles.
- Inspection: Records for vehicle inspections.
- Additional aggregates may be introduced as the system evolves.

See entities under `src/main/java/com/fleetops/entity/`.

## Data Flow

1. Client calls REST endpoint.
2. Controller validates inputs and delegates to Service.
3. Service coordinates repositories and domain rules.
4. Repository uses JPA to interact with PostgreSQL.
5. Liquibase manages schema updates on startup.

## Error Handling

- Centralized via `GlobalControllerExceptionHandler` mapping:
  - Constraint violations to 400 with structured JSON body.
  - Resource not found to 404.
  - Unauthorized/Forbidden per Spring Security to 401/403.
  - Unexpected errors to 500 with correlation where applicable.

## Security

- Resource Server (JWT bearer):
  - Validate tokens via `issuer-uri` or `jwk-set-uri`.
  - Method/endpoint authorization via Spring Security config and annotations.
- Tests use `spring-security-test` for mock authentication.

### Security Architecture

- See the [Security Architecture Diagram](security/diagrams/security-architecture.md) for a detailed description of security layers and controls.
- View the [visual diagram (PNG)](security/assets/security-architecture.png) or [PlantUML source](security/assets/security-architecture.puml).
- For all security requirements, policies, and plans, see the [Security Documentation Index](../INDEX.md).

## Observability

- Logging via Spring Boot logging (Logback).
- Add request correlation IDs if required by ops standards.
- Metrics/tracing can be added with Micrometer/OpenTelemetry (optional).

## Dependencies and Infrastructure

- PostgreSQL as primary datastore.
- Redis, Kafka, Zookeeper available via docker-compose for future messaging/cache capabilities.
- API published on port 8080.

## Build and Packaging

- Maven for builds and dependency management.
- Spring Boot plugin produces `target/fleetops-api.jar`.
- Dockerfile uses `openjdk:17-jdk-slim` and runs the fat JAR.

## Extending the System

- Add new endpoints: define DTOs, controller methods with validation, service methods, and repository queries.
- Update schema: author Liquibase changesets and include them in the master changelog.
- Keep `docs/openapi.yml` synchronized with code changes and annotate controllers for OpenAPI where useful.
