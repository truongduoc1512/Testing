# Docker Test Environment

## Requirements

- Docker Desktop
- Docker Compose

## Start environment

Run:

```powershell
.\scripts\start-test-env.ps1
```

Or manually:

```bash
docker compose up -d
```

## Stop environment

```bash
docker compose down
```

## Check running containers

```bash
docker ps
```

## Services

| Service | Port |
|----------|------|
| Nginx | 80 |
| Backend API | 8080 |
| Swagger | 8082 |
| phpMyAdmin | 8081 |
| AI Service | 8000 |
| MySQL | 3307 |