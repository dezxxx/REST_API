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
service/impl/   ← Business logic: validation calls, existence checks
repository/impl/← Data access: HQL queries, Hibernate sessions
filter/         ← Cross-cutting: Basic Auth on all API endpoints
validation/     ← All entity validation in one place (Chain of Responsibility)
```

**Key design decisions:**
- `Repository<T>` — single generic interface, many implementations (GenericRepository pattern)
- `EventRepository extends Repository<Event>` — the only extension, adds `findByUserId`
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
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user |
| GET | `/files` | Get all files |
| GET | `/files/{id}` | Get file by id |
| POST | `/files` | Create file |
| PUT | `/files/{id}` | Update file |
| DELETE | `/files/{id}` | Delete file |
| GET | `/events` | Get all events |
| GET | `/events/{id}` | Get event by id |
| GET | `/events/user/{userId}` | Get events by user |
| POST | `/events` | Create event |
| PUT | `/events/{id}` | Update event |
| DELETE | `/events/{id}` | Delete event |

Full interactive docs: `http://localhost:8080/REST_API/swagger-ui.html`

## Authentication

All API endpoints are protected with **HTTP Basic Auth**.

| Username | Password |
|----------|----------|
| admin | admin |

In Postman: `Authorization` tab → Type: `Basic Auth` → enter credentials.  
Swagger UI is accessible without authentication.

## Error Handling

| Status | When |
|--------|------|
| 400 | Invalid/missing JSON body, blank fields, field too long, missing ID in path |
| 401 | Missing or invalid Authorization header |
| 404 | Entity not found by id |
| 409 | Duplicate event — same user + file combination already exists |
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

40 tests covering all service implementations and entity validation.
