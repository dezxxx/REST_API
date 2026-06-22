# File Manager REST API

A RESTful API for managing users, files, and events (file upload history).  
Built on a pure Java servlet stack — **no Spring** — to demonstrate deep understanding of how web frameworks work under the hood.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Web | Servlet API (Javax), Tomcat 9 |
| ORM | Hibernate 6 |
| Migrations | Flyway 10 |
| Database | MySQL 8 |
| JSON | Jackson |
| Documentation | Swagger UI (OpenAPI 3.0) |
| Testing | JUnit 5 + Mockito |
| Build | Maven |

## Architecture

```
servlet/        ← HTTP layer: routing, request/response mapping
service/        ← Business logic: validation, existence checks, business rules
repository/impl/← Data access: HQL queries, Hibernate sessions
filter/         ← Cross-cutting: Basic Auth on all API endpoints
validation/     ← All entity validation in one place (Chain of Responsibility)
```

**Key design decisions:**
- `Repository<T>` — single generic interface, extended by `UserRepository`, `FileRepository`, `EventRepository`
- `EventRepository extends Repository<Event>` — adds `findByUserId`, `existsByUserAndFile`, `existsByFileId`
- `TransactionHelper` — centralizes all try/catch/rollback logic
- `EntityValidator` — all field validation in one class via fluent `ValidationChain`
- `AppContextListener` — wires all singletons at startup, stores in `ServletContext`
- JOIN FETCH in all HQL queries to avoid `LazyInitializationException`

## API Endpoints

Base URL: `http://localhost:8080/REST_API`

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/users` | Get all users |
| GET | `/users/{id}` | Get user by id |
| POST | `/users` | Create user |
| PUT | `/users/{id}` | Full update user |
| PATCH | `/users/{id}` | Partial update user |
| DELETE | `/users/{id}` | Delete user |
| GET | `/files` | Get all files |
| GET | `/files/{id}` | Get file by id |
| POST | `/files` | Create file |
| PUT | `/files/{id}` | Full update file |
| PATCH | `/files/{id}` | Partial update file |
| DELETE | `/files/{id}` | Delete file |
| GET | `/events` | Get all events |
| GET | `/events/{id}` | Get event by id |
| GET | `/events/user/{userId}` | Get events by user |
| POST | `/events` | Create event |
| PUT | `/events/{id}` | Full update event |
| PATCH | `/events/{id}` | Partial update event |
| DELETE | `/events/{id}` | Delete event |

Full interactive docs: `http://localhost:8080/REST_API/swagger-ui.html`

## Authentication

All API endpoints are protected with **HTTP Basic Auth**.

| Username | Password |
|----------|----------|
| admin | admin |

In Postman: `Authorization` tab → Type: `Basic Auth` → enter credentials.  
Swagger UI is accessible without authentication.

## Delete Behavior

**DELETE /users/{id}** — deletes the user and all their events (upload history). Files are not affected — they exist independently.

**DELETE /files/{id}** — deletes the file only if it has no associated events. If events reference this file, returns `409 Conflict`. First delete the events, then the file.

**DELETE /events/{id}** — deletes only the link between a user and a file. The user and the file remain intact.

This mirrors real-world semantics: a user is responsible for their history, a file is not.

## PUT vs PATCH

- **PUT** — full update: all required fields must be provided
- **PATCH** — partial update: only the fields you send are changed, the rest stay as-is

## Error Handling

| Status | When |
|--------|------|
| 400 | Invalid/missing JSON body, blank fields, field too long, missing ID in path |
| 401 | Missing or invalid Authorization header |
| 404 | Entity not found by id |
| 409 | Duplicate event (same user + file), or trying to delete a file that has events |
| 500 | Unexpected server error |

## Running Locally

**Requirements:** JDK 21, Maven, MySQL 8, Tomcat 9

1. Create database:
```sql
CREATE DATABASE file_manager_db;
```

2. Configure DB credentials in `AppContextListener` (host, user, password)

3. Build and deploy:
```bash
mvn clean package
# copy target/REST_API.war to Tomcat webapps/
```

Or use the included `deploy.bat` script for quick redeploy to local Tomcat.

## Testing

```bash
mvn test
```

53 tests covering all service classes and entity validation.
