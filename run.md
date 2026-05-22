# IssueFlow - Run Instructions

## Requirements

- Java 21 or newer
- Docker Desktop / Docker Compose
- Maven wrapper is included in this repository (`mvnw` / `mvnw.cmd`)

## Start the PostgreSQL database

```bash
docker compose up -d
```

The app expects PostgreSQL on:

- Host: `localhost`
- Port: `5432`
- Database: `issueflow`
- Username: `issueflow`
- Password: `issueflow`

## Build the project

Linux/macOS:

```bash
chmod +x mvnw
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

## Run the application

```bash
./mvnw spring-boot:run
```

Or after packaging:

```bash
java -jar target/issueflow-0.0.1-SNAPSHOT.jar
```

The server runs on:

```text
http://localhost:8080
```

## Run tests

```bash
./mvnw test
```

## Authentication flow

Create the first user. The `password` field is optional. If it is not sent, the app uses the default password `secret` so that the user can log in.

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","fullName":"Admin User","role":"ADMIN","password":"secret"}'
```

Login and copy the returned `accessToken`:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}'
```

Use the token on protected endpoints:

```bash
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Example API flow

Create a developer:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"dev1","email":"dev1@example.com","fullName":"Dev One","role":"DEVELOPER","password":"secret"}'
```

Create a project:

```bash
curl -X POST http://localhost:8080/projects \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"name":"Sample Project","description":"Demo project","ownerId":1}'
```

Create a ticket. If `assigneeId` is omitted, the system auto-assigns the least-loaded developer:

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"title":"Fix login bug","description":"Login fails sometimes","status":"TODO","priority":"HIGH","type":"BUG","projectId":1}'
```

Get workload:

```bash
curl http://localhost:8080/projects/1/workload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Notes

- Standard API responses hide soft-deleted projects and tickets.
- `GET /projects/deleted` and `GET /tickets/deleted?projectId=1` return deleted records.
- `POST /projects/{id}/restore` and `POST /tickets/{id}/restore` restore soft-deleted records.
- CSV export endpoint: `GET /tickets/export?projectId=1`.
- CSV import endpoint: `POST /tickets/import` with multipart fields `projectId` and `file`.
- Attachments allow only `image/png`, `image/jpeg`, `application/pdf`, and `text/plain`, with a maximum size of 10 MB.
