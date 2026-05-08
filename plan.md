# SkillForge — Project Plan / Overview

This document describes the **technologies**, **business domain**, and the **current scope** of the SkillForge codebase as it exists in this repository.

## Technologies

### Runtime & language

- **Java 17**
- **Spring Boot 3.x**

### Web & API

- **Spring Web (MVC)**: REST endpoints under `/api/*`
- **DTO-based API responses**:
  - `ApiResponse<T>` is used as a standard envelope for many controller responses and for global exception responses.
- **OpenAPI / Swagger UI** via `springdoc-openapi`:
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Persistence

- **PostgreSQL** (configured in `src/main/resources/application.yaml`)
- **Spring Data JPA (Hibernate)**
- **Flyway** SQL migrations (auto-run at startup):
  - `src/main/resources/db/migrations/`

### Security

- **Spring Security** with stateless sessions
- **JWT**:
  - `JwtAuthenticationFilter` parses `Authorization: Bearer <token>`
  - `JwtUtils` validates and extracts claims
- **UserDetails lookup**:
  - `CustomUserDetailsService` provides the `UserDetailsService` bean (loads by username, falls back to email)
- Method-level authorization via `@PreAuthorize` (example in `CourseController`)

### Caching

- **Spring Cache** with **Caffeine** as provider
- Cache names referenced in code/config include: `courses`, `users`, `enrollments`, `enrollmentStats`

### Email

- **Spring Mail** + **Thymeleaf** templates for transactional emails
- Async email sending via `@Async` in `EmailService`
- Templates live in `src/main/resources/templates/`

### Build tooling

- **Maven** (`pom.xml`)
- Annotation processors:
  - **Lombok**
  - **MapStruct** (configured for Spring component model)
- **Note (Maven plugin prefix)**:
  - If `mvn spring-boot:run` fails due to prefix resolution, run using fully qualified coordinates:
    - `mvn -f pom.xml org.springframework.boot:spring-boot-maven-plugin:3.3.0:run`

## Business domain

SkillForge is an **e-learning platform** backend, centered around the following core concepts:

### Users, roles, permissions

- `User` implements `UserDetails` and holds `Role`s (and via roles, `Permission`s).
- Roles are expected to follow the `ROLE_*` convention:
  - `ROLE_STUDENT`, `ROLE_INSTRUCTOR`, `ROLE_ADMIN`

### Courses (instructor-owned)

- A `Course` is created and managed by an instructor (`Course.instructor`).
- Courses can be **published/unpublished** and **soft-deleted** (`isPublished`, `isDeleted`).
- Courses contain a structure:
  - `Course` → `Module` → `Lesson`

### Enrollment & access

- `Enrollment` links a `User` (student) to a `Course`.
- Enrollment tracks:
  - `status` (`ACTIVE`, `COMPLETED`, `DROPPED`, `EXPIRED`)
  - `progressPercentage`
  - `expiresAt` (time-limited access)
  - `completionDate`, `lastAccessedAt`

### Lesson progress

- `LessonProgress` tracks per-lesson completion, watch time, resume position, optional quiz score, notes.
- Enrollment progress is derived from the ratio of completed lessons.

### Reviews & ratings

- `Review` is a user-to-course review with a 1–5 rating and optional comment.
- `ReviewService` provides rating distribution and updates course average rating.

### Payments

- `Payment` represents payment attempts and outcomes (pending/processing/completed/failed/refunded/etc.).
- Current implementation includes persistence + email receipt flows, but does **not** wire a payment gateway yet.

### Certificates

- When an enrollment completes (100% progress), a `Certificate` may be generated and emailed.

### Wishlist

- Users can add courses to a wishlist; publishing can trigger notifications.

## Current scope (what’s implemented vs missing)

### Recent alignment & “bootability” work

The repo includes a number of “plumbing” fixes to keep the project runnable and compiling:

- **Config alignment**:
  - Flyway location aligned to `db/migrations`
  - JWT properties aligned under `jwt.*`
  - Added `app.course-access-days`
- **Persistence/migrations**:
  - Flyway migrations were expanded/updated to include tables required by the JPA model and to be PostgreSQL-compatible.
- **Missing pieces filled in for compilation**:
  - Added `ModuleProgressDTO` and reshaped `CourseProgressDTO` to match usage in `EnrollmentService`
  - Added missing `CourseRepository` methods used by services (enrollment count updates, instructor course query)
- **Error responses**:
  - `GlobalExceptionHandler` returns `ApiResponse<Void>` rather than attempting to build Spring’s `ErrorResponse` interface.

### Implemented API endpoints

At the moment, the only discovered REST controller is:

- **Course Management**: `CourseController` (`/api/courses`)
  - Public:
    - `GET /api/courses` (pagination + filtering)
    - `GET /api/courses/{id}`
  - Requires auth (role-based):
    - `POST /api/courses` (INSTRUCTOR)
    - `PUT/PATCH /api/courses/{id}` (INSTRUCTOR + ownership)
    - `DELETE /api/courses/{id}` (INSTRUCTOR or ADMIN)
    - `POST /api/courses/{courseId}/publish` (INSTRUCTOR + ownership)
    - `POST /api/courses/{courseId}/unpublish` (INSTRUCTOR + ownership)
    - `GET /api/courses/{courseId}/statistics` (INSTRUCTOR owner or ADMIN)
    - `GET /api/courses/instructor/my-courses` (INSTRUCTOR)

### Business logic present (but not exposed as controllers yet)

These services exist and implement core flows, but lack REST controllers in this repo:

- `EnrollmentService`:
  - enrollments, renewals, dropping courses
  - lesson progress updates/completion
  - certificate generation + notifications
  - scheduled jobs for expirations and reminders
- `EmailService`: transactional email templates and senders
- `ReviewService`: rating distribution and rating updates

### Security endpoints status

- `SecurityConfig` permits `/api/auth/`**, but **no auth controller is implemented yet** in this repo.

### Not yet implemented (expected next)

Based on security configuration and services present, likely next additions include:

- **Auth endpoints** under `/api/auth/`**:
  - register/login/refresh/logout
  - email verification / password reset flows (templates + methods exist in `EmailService`)
- **Enrollment endpoints**:
  - enroll in course, list enrollments, progress endpoints
- **Payments endpoints**:
  - initiate payment, webhook processing, receipts/refunds
- **Wishlist endpoints**
- **Reviews endpoints**
- **Admin/instructor dashboards** endpoints (some DTOs and statistics exist)

## Key flows (how the system is intended to work)

### Course creation & publishing (instructor)

1. Instructor calls `POST /api/courses` with `CourseRequestDTO`.
2. `CourseService.createCourse()` validates instructor role and duplicate titles.
3. Course is persisted and a confirmation email is sent.
4. Instructor publishes course via `POST /api/courses/{courseId}/publish`.
5. Publishing validates readiness (must have modules/lessons) and can notify wishlisted users.

### Student enrollment & progress

1. Student enrolls via `EnrollmentService.enrollStudent()` (controller not present yet).
2. If course is paid, a `Payment` is created (gateway integration stub).
3. An `Enrollment` is created with an expiry (`app.course-access-days`).
4. A `LessonProgress` record is initialized for every lesson in the course.
5. Student progress is updated via:
  - `updateLessonProgress(...)` (watch time + position)
  - `completeLesson(...)` (mark complete)
6. When all lessons are completed, the enrollment becomes `COMPLETED` and a certificate can be generated + emailed.

### Scheduled maintenance

With scheduling enabled (`@EnableScheduling` on `SkillforgeApplication`), the code includes jobs such as:

- marking expired enrollments
- sending completion reminders

## Configuration and environment

### Database

Default connection (override via env vars):

- `spring.datasource.url`: `jdbc:postgresql://localhost:5432/skillforge`
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `root`)

### JWT

- `jwt.secret` (env: `JWT_SECRET`)
- `jwt.expiration` (env: `JWT_EXPIRATION`, milliseconds)
- `jwt.refresh-expiration` (env: `JWT_REFRESH_EXPIRATION`, milliseconds)

### Email

- `MAIL_USERNAME` / `MAIL_PASSWORD` are used for SMTP credentials (Gmail config shown by default).

### App settings

- `app.frontend-url`: used to generate links in emails and certificates
- `app.max-courses-per-user`: enrollment limit
- `app.course-access-days`: access period for enrollments
- `app.support-email`: displayed in emails

