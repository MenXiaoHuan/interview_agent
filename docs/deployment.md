# Deployment Guide

## Profiles

- `dev`: local development, Swagger enabled, local CORS origin.
- `test`: automated tests.
- `prod`: production, Swagger disabled by default, no sensitive defaults.

## Required Production Environment Variables

- `SPRING_PROFILES_ACTIVE=prod`
- `PLATFORM_DB_URL`
- `PLATFORM_DB_USERNAME`
- `PLATFORM_DB_PASSWORD`
- `PLATFORM_REDIS_HOST`
- `PLATFORM_JWT_SECRET`
- `PLATFORM_DEEPSEEK_API_KEY`
- `PLATFORM_XUNFEI_APPID`
- `PLATFORM_XUNFEI_API_KEY`
- `PLATFORM_XUNFEI_API_SECRET`
- `PLATFORM_CORS_ALLOWED_ORIGINS`

## Environment Files

Do not commit real `.env` files. Commit only `.env.example` files.

- Root `.env`: used by Docker Compose. Create it from `.env.example`.
- `backend/.env`: used when running the backend directly from the terminal. Create it from `backend/.env.example`.
- `project/.env.local` or `project/.env.development.local`: used for frontend local overrides. Create it from `project/.env.example`.

Recommended local setup:

```bash
cp .env.example .env
cp backend/.env.example backend/.env
cp project/.env.example project/.env.local
```

Run the backend directly with:

```bash
set -a
source backend/.env
set +a
cd backend
./mvnw spring-boot:run
```

Run the frontend directly with:

```bash
cd project
npm run dev:h5
```

## Local Docker Compose

Run:

```bash
docker compose up
```

Docker Compose automatically reads the root `.env` file. Use it for local Docker-only values such as `MYSQL_ROOT_PASSWORD`, `PLATFORM_JWT_SECRET`, `PLATFORM_DEEPSEEK_API_KEY`, MinIO credentials, and CORS origins.

The backend listens on `http://localhost:8442` when `PLATFORM_SSL_ENABLED=false`.

## MinIO Avatar Storage

Local Docker Compose starts MinIO:

- API: `http://localhost:9000`
- Console: `http://localhost:9001`
- Default development user: `minioadmin`
- Default development password: `minioadmin`
- Default bucket: `interview-agent`

Production must provide:

- `PLATFORM_MINIO_ENDPOINT`
- `PLATFORM_MINIO_PUBLIC_URL`
- `PLATFORM_MINIO_ACCESS_KEY`
- `PLATFORM_MINIO_SECRET_KEY`
- `PLATFORM_MINIO_BUCKET`

Avatar URLs are stored as public MinIO URLs in `user.avatar_url`.

## Frontend API URL

Set `VITE_API_BASE_URL` in the frontend environment file. Local development uses `https://localhost:8442` by default.

Frontend variables are injected at Vite dev/build time. They are not read from the backend process. If the frontend runs inside Docker Compose, define `VITE_API_BASE_URL` on the frontend service. If the frontend runs directly with `npm run dev:h5`, define it in `project/.env.local` or `project/.env.development.local`.

## Frontend in Docker Compose

The `frontend` service injects `VITE_API_BASE_URL=http://localhost:8442`.
This only applies when the frontend is started by Docker Compose.
When running `npm run dev:h5` directly, `project/.env.development` keeps the local HTTPS default.

## Swagger

Swagger is controlled by `PLATFORM_SWAGGER_ENABLED`. Keep it disabled in production.
