# FleetOps API

FleetOps API is a Spring Boot service for managing fleet operations, including vehicles, drivers, inspections, and related workflows. It provides RESTful endpoints secured as an OAuth2 resource server and includes OpenAPI/Swagger UI for interactive API documentation.

## Features

- RESTful API built with Spring Boot 3
- Data persistence with Spring Data JPA (PostgreSQL runtime, H2 for tests)
- Schema migrations with Liquibase
- OAuth2/JWT Resource Server security
- API documentation via springdoc-openapi (Swagger UI)
- Dockerized runtime and Docker Compose for local infrastructure (PostgreSQL, Redis, Kafka, Zookeeper)
- Unit, slice, and integration tests

## Tech Stack

- Java 17
- Spring Boot 3.5.5
  - Web, Data JPA, Validation, Security, OAuth2 Resource Server
- PostgreSQL (runtime), H2 (tests)
- Liquibase for database migrations
- Redis and Kafka (via docker-compose)
- springdoc-openapi-starter-webmvc-ui for Swagger UI

## Project Structure

```text
.
├─ src/
│  ├─ main/
│  │  ├─ java/                  # Controllers, services, repositories, entities
│  │  ├─ resources/             # application.yml/properties, Liquibase changelogs, etc.
│  │  └─ proto/                 # (if applicable)
│  └─ test/…                    # Unit and integration tests (H2)
├─ docs/
│  ├─ openapi.yml               # API contract (source of truth for endpoints)
│  ├─ ARCHITECTURE.md
│  ├─ MIGRATIONS.md
│  └─ API.md
├─ Dockerfile
├─ docker-compose.yml
├─ pom.xml
└─ README.md
```

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.9+
- Docker and Docker Compose (optional for local infra)

### Build

```bash
mvn clean verify
```

This runs compilation and tests and produces `target/fleetops-api.jar`.

### Run (Local JVM)

Configure environment variables (or `application.properties`) for PostgreSQL and JWT validation:

- SPRING_DATASOURCE_URL: `jdbc:postgresql://localhost:5432/fleetops`
- SPRING_DATASOURCE_USERNAME: `postgres`
- SPRING_DATASOURCE_PASSWORD: `postgres`
- SPRING_JPA_HIBERNATE_DDL_AUTO: `none` (Liquibase manages schema)
- SPRING_LIQUIBASE_ENABLED: `true`
- SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI or JWK_SET_URI

Run via Maven:

```bash
mvn spring-boot:run
```

Or run the built JAR:

```bash
java -jar target/fleetops-api.jar
```

The API listens on port 8080 by default.

### Run (Docker)

1) Build the JAR:

```bash
mvn -DskipTests package
```

2) Build and run the image:

```bash
docker build -t fleetops-api:local .
docker run -p 8080:8080 \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/fleetops \
  --env SPRING_DATASOURCE_USERNAME=postgres \
  --env SPRING_DATASOURCE_PASSWORD=postgres \
  fleetops-api:local
```

### Run (Docker Compose, full stack)

Bring up the API and dependencies (PostgreSQL, Redis, Kafka, Zookeeper):

```bash
docker compose up --build
```

Ports:
- API: 8080
- PostgreSQL: 5432
- Redis: 6379
- Kafka: 9092
- Zookeeper: 2181

The service uses:
- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/fleetops`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=postgres`

as configured in `docker-compose.yml`.

## Database and Migrations (Liquibase)

- Liquibase manages schema changes via changesets.
- Place changesets under `src/main/resources/db/changelog/` and include them from the master changelog.
- On startup, Liquibase applies pending changes automatically.

See `docs/MIGRATIONS.md` for authoring and promotion guidelines.

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

A source-of-truth `docs/openapi.yml` is included; keep it in sync with controllers. See `docs/API.md` for details.

## Security

This service acts as an OAuth2 Resource Server and expects Bearer JWTs on protected endpoints.

Minimal configuration options:
- `spring.security.oauth2.resourceserver.jwt.issuer-uri=https://<issuer>/`
- or `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://<issuer>/.well-known/jwks.json`

Validation/exception handling is mapped to structured error responses (see tests around `GlobalControllerExceptionHandler`). See `SECURITY.md` for best practices.

## Testing

Run the full test suite:

```bash
mvn test
```

- Unit and slice tests use standard Spring Boot test starters.
- Integration tests use H2 and Spring test utilities.
- Security-related tests leverage `spring-security-test`.

## Troubleshooting

- Database connectivity: verify `SPRING_DATASOURCE_*` variables and DB availability.
- Liquibase errors: ensure changesets are included and formatted correctly; see `docs/MIGRATIONS.md`.
- JWT validation failures: check `issuer-uri` or `jwk-set-uri`, token audience/scope, and clock skew.

## Contributing

See `CONTRIBUTING.md` for branch strategy, commit messages, code style, testing, and PR process.

## License

Specify your license of choice (e.g., MIT, Apache-2.0). Add a `LICENSE` file in the repo root.
