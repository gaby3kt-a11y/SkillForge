# AGENTS.md

## Overview
This document provides essential knowledge for AI coding agents to be productive in the SkillForge backend codebase. SkillForge is a Spring Boot application for an e-learning platform, featuring courses, enrollments, progress tracking, payments, and more.

## Architecture
- **Tech Stack**:
  - Java 17
  - Spring Boot 3.x (Web, Security, Validation, Cache, Mail)
  - PostgreSQL (database)
  - Flyway (database migrations)
  - JWT (authentication)
  - OpenAPI/Swagger (API documentation)

- **Key Components**:
  - `controller/`: REST API endpoints (e.g., `CourseController` for `/api/courses`)
  - `service/`: Business logic layer
  - `repository/`: Data access layer (Spring Data JPA)
  - `model/`: Entity classes mapped to database tables
  - `dto/`: Data Transfer Objects for API requests/responses
  - `mapper/`: Converters between `model` and `dto`
  - `exception/`: Custom exception handling
  - `security/`: JWT-based authentication and authorization

- **Data Flow**:
  1. HTTP requests are handled by `controller` classes.
  2. Business logic is processed in `service` classes.
  3. Data is fetched/stored via `repository` interfaces.
  4. `mapper` classes convert between `model` and `dto`.

## Developer Workflows

### Building and Running
- **Run the application**:
  ```bash
  mvn spring-boot:run
  ```
  The server starts on port **8080** by default.

- **Database setup**:
  - Create a PostgreSQL database named `skillforge`.
  - Default connection settings (from `application.yaml`):
    - URL: `jdbc:postgresql://localhost:5432/skillforge`
    - User: `postgres`
    - Password: `root`

- **Environment variables** (optional):
  - `DB_USERNAME`, `DB_PASSWORD`: Database credentials
  - `JWT_SECRET`: JWT HMAC secret
  - `MAIL_USERNAME`, `MAIL_PASSWORD`: SMTP credentials

### Testing
- Run tests:
  ```bash
  mvn test
  ```

### Database Migrations
- Flyway migrations are located in `src/main/resources/db/migrations/`.
- Migrations run automatically on startup.

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Project-Specific Conventions
- **DTOs**:
  - Use `dto/` for request/response objects.
  - Example: `CourseRequestDTO` for creating/updating courses.

- **Mapping**:
  - Use `mapper/` for converting between `model` and `dto`.
  - Example: `CourseMapper` maps `Course` entities to `CourseResponseDTO`.

- **Error Handling**:
  - Centralized exception handling in `exception/`.
  - Example: `GlobalExceptionHandler` for custom error responses.

## Key Files and Directories
- `src/main/java/com/skillforge/SkillforgeApplication.java`: Application entry point.
- `src/main/resources/application.yaml`: Configuration file.
- `src/main/resources/templates/`: Thymeleaf templates for emails.
- `src/main/resources/db/migrations/`: Flyway migration scripts.
- `src/main/java/com/skillforge/controller/CourseController.java`: Example API controller.

## External Dependencies
- **PostgreSQL**: Database backend.
- **Flyway**: Database migration tool.
- **Spring Security**: JWT-based authentication.
- **Springdoc**: OpenAPI/Swagger integration.
- **Thymeleaf**: Email templates.

## Tips for AI Agents
- Follow the layered architecture (controller -> service -> repository).
- Use existing `mapper` classes for `model` <-> `dto` conversions.
- Adhere to the conventions in `exception/` for error handling.
- Refer to `README.md` for additional context on running and testing the application.
