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

## Local Docker Compose

Run:

```bash
docker compose up
```

The backend listens on `http://localhost:8442` when `PLATFORM_SSL_ENABLED=false`.

## Frontend API URL

Set `VITE_API_BASE_URL` in the frontend environment file. Local development uses `https://localhost:8442` by default.

## Swagger

Swagger is controlled by `PLATFORM_SWAGGER_ENABLED`. Keep it disabled in production.
