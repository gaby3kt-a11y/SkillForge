---
name: skillforge-dev-runbook
description: Runbook for setting up, running, and troubleshooting the SkillForge Spring Boot backend (Postgres/Flyway/JWT/Maven). Use when the user asks how to run the project locally, fix startup errors, resolve Flyway/database issues, or troubleshoot Maven/Spring Boot execution.
---

# SkillForge Dev Runbook

## Quick start (local)

### Prerequisites
- Java 17
- Maven
- PostgreSQL reachable on `localhost:5432`

### Database
Create a database named `skillforge`.

Defaults (override via env vars):
- `DB_USERNAME` (default `postgres`)
- `DB_PASSWORD` (default `root`)

### Run
If Maven plugin prefix resolution works:

```bash
mvn spring-boot:run
```

If you get `No plugin found for prefix 'spring-boot'`, use the fully-qualified goal:

```bash
mvn -f pom.xml org.springframework.boot:spring-boot-maven-plugin:3.3.0:run
```

## Test

```bash
mvn -f pom.xml test
```

## Formatting (Spotless)

```bash
mvn -f pom.xml spotless:apply
mvn -f pom.xml spotless:check
```

## Common troubleshooting

### Spring fails: missing `UserDetailsService` bean
**Symptom**: `JwtAuthenticationFilter required a bean of type 'UserDetailsService'`

**Fix**: Ensure there is a `UserDetailsService` implementation annotated as a Spring bean and backed by `UserRepository` (load by username; optional fallback to email).

### Flyway migrations not found
**Symptom**: Flyway starts but applies nothing / complains about missing migrations.

**Fix**:
- Ensure `spring.flyway.locations` matches the repository path (typically `classpath:db/migrations`).
- Ensure migrations are under `src/main/resources/db/migrations/` and named `V<version>__<desc>.sql`.

### Flyway migration SQL errors on Postgres
**Symptom**: errors about `INDEX ...` inside `CREATE TABLE`, MySQL-specific syntax, etc.

**Fix**:
- For Postgres, create indexes with separate `CREATE INDEX ...` statements (not inline `INDEX ...` in `CREATE TABLE`).
- Prefer `CREATE INDEX IF NOT EXISTS ...` where possible.

### JWT config mismatch
**Symptom**: startup errors about missing JWT properties or tokens failing validation.

**Fix**:
- Align `JwtUtils` property keys with `application.yaml`.
- Recommended keys:
  - `jwt.secret` (env `JWT_SECRET`)
  - `jwt.expiration` (env `JWT_EXPIRATION`, milliseconds)

### Cache provider mismatch (Caffeine)
**Symptom**: `spring.cache.type=caffeine` but cache provider classes missing.

**Fix**:
- Add `com.github.ben-manes.caffeine:caffeine` dependency, or change cache type to `simple`.

## Useful endpoints
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

