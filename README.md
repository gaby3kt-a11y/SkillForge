# SkillForge (Backend)

Spring Boot backend for an e-learning platform (courses, enrollments, progress tracking, reviews, payments, certificates).

## Tech stack

- **Java**: 17
- **Framework**: Spring Boot 3.x (Web, Security, Validation, Cache, Mail)
- **DB**: PostgreSQL
- **Migrations**: Flyway (SQL)
- **Auth**: JWT (stateless)
- **Docs**: OpenAPI / Swagger UI (springdoc)

## Project structure

- `pom.xml`: Maven build
- `src/main/java/com/skillforge/SkillforgeApplication.java`: app entry point
- `src/main/resources/application.yaml`: app config
- `src/main/resources/db/migrations/`: Flyway migrations
- `src/main/resources/templates/`: Thymeleaf email templates

## API surface (currently implemented)

Only one controller is present right now:

- `CourseController` (`/api/courses`)
  - `GET /api/courses` (public, pagination + filtering)
  - `GET /api/courses/{id}` (public; enriches response if `userId` request attribute exists)
  - `POST /api/courses` (INSTRUCTOR)
  - `PUT /api/courses/{id}` (INSTRUCTOR + ownership)
  - `PATCH /api/courses/{id}` (INSTRUCTOR + ownership)
  - `DELETE /api/courses/{id}` (INSTRUCTOR or ADMIN)
  - `POST /api/courses/{courseId}/publish` (INSTRUCTOR + ownership)
  - `POST /api/courses/{courseId}/unpublish` (INSTRUCTOR + ownership)
  - `GET /api/courses/{courseId}/statistics` (INSTRUCTOR owner or ADMIN)
  - `GET /api/courses/instructor/my-courses` (INSTRUCTOR)

There are additional services and repositories (enrollment/progress, payments, certificates, wishlists, reviews, email), but **they do not yet have controllers** wired up.

## Running locally

### 1) Start PostgreSQL

Create a database named `skillforge` and ensure it’s reachable on `localhost:5432`.

Default connection (from `application.yaml`):

- **URL**: `jdbc:postgresql://localhost:5432/skillforge`
- **User**: `postgres` (override with `DB_USERNAME`)
- **Password**: `root` (override with `DB_PASSWORD`)

### 2) Configure environment variables (optional but recommended)

- `DB_USERNAME`: Postgres username (default: `postgres`)
- `DB_PASSWORD`: Postgres password (default: `root`)
- `JWT_SECRET`: JWT HMAC secret (default is provided for local dev)
- `JWT_EXPIRATION`: token expiry in ms (default: `86400000`)
- `JWT_REFRESH_EXPIRATION`: refresh expiry in ms (default: `604800000`)
- `MAIL_USERNAME` / `MAIL_PASSWORD`: SMTP credentials (Gmail defaults are shown in config)
- `SUPPORT_EMAIL`: support email shown in templates

### 3) Run the app

```bash
mvn spring-boot:run
```

The server starts on port **8080** by default.

### 4) Database migrations

Flyway is enabled and runs automatically on startup using migrations under `src/main/resources/db/migrations/`.

## Swagger / OpenAPI

When the app is running, open:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
mvn test
```

