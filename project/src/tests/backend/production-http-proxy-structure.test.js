import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");

const readRepoSource = (relativePath) => readFileSync(path.join(repoRoot, relativePath), "utf8");

describe("production HTTP backend behind HTTPS gateway setup", () => {
  it("keeps separate dev and production compose files", () => {
    expect(existsSync(path.join(repoRoot, "docker-compose.yml"))).toBe(true);
    expect(existsSync(path.join(repoRoot, "docker-compose.prod.yml"))).toBe(true);

    const devCompose = readRepoSource("docker-compose.yml");
    const prodCompose = readRepoSource("docker-compose.prod.yml");

    expect(devCompose).toContain("dockerfile: Dockerfile.dev");
    expect(prodCompose).toContain("dockerfile: Dockerfile");
  });

  it("makes both compose modes communicate with the backend over container HTTP", () => {
    const devCompose = readRepoSource("docker-compose.yml");
    const prodCompose = readRepoSource("docker-compose.prod.yml");

    [devCompose, prodCompose].forEach((composeSource) => {
      expect(composeSource).toContain("PLATFORM_WEB_API_PROXY_TARGET: http://server:${PLATFORM_SERVER_HOST_PORT}");
      expect(composeSource).not.toContain("PLATFORM_WEB_API_PROXY_TARGET: https://server:${PLATFORM_SERVER_HOST_PORT}");
      expect(composeSource).not.toContain("PLATFORM_SSL_ENABLED");
    });
  });

  it("keeps the production backend private to the compose network", () => {
    const devCompose = readRepoSource("docker-compose.yml");
    const prodCompose = readRepoSource("docker-compose.prod.yml");
    const serverPortMapping = '- "${PLATFORM_SERVER_HOST_PORT}:${PLATFORM_SERVER_HOST_PORT}"';

    expect(devCompose).toContain(serverPortMapping);
    expect(prodCompose).not.toContain(serverPortMapping);
  });

  it("defaults the production web proxy target to the Spring Boot HTTP service", () => {
    const dockerfile = readRepoSource("project/Dockerfile");

    expect(dockerfile).toContain("ENV PLATFORM_WEB_API_PROXY_TARGET=http://server:8442");
    expect(dockerfile).not.toContain("ENV PLATFORM_WEB_API_PROXY_TARGET=https://server:8442");
  });

  it("proxies production nginx traffic to HTTP backend without SSL-only proxy options", () => {
    const nginxTemplate = readRepoSource("project/nginx.conf.template");

    expect(nginxTemplate).toContain("proxy_pass ${PLATFORM_WEB_API_PROXY_TARGET};");
    expect(nginxTemplate).not.toContain("proxy_ssl_verify off;");
    expect(nginxTemplate).toContain("proxy_set_header X-Forwarded-Host $host;");
    expect(nginxTemplate).toContain("proxy_set_header X-Forwarded-Port $server_port;");
    expect(nginxTemplate).toContain("map $http_x_forwarded_proto $proxy_forwarded_proto");
  });

  it("removes Spring Boot SSL configuration so TLS can terminate at an outer gateway", () => {
    const devApplication = readRepoSource("backend/src/main/resources/application-dev.yml");
    const testApplication = readRepoSource("backend/src/test/resources/application-test.yml");

    expect(devApplication).toContain("server:");
    expect(devApplication).toContain("port: ${PLATFORM_SERVER_PORT:8442}");
    [devApplication, testApplication].forEach((applicationSource) => {
      expect(applicationSource).not.toContain("ssl:");
      expect(applicationSource).not.toContain("PLATFORM_SSL_ENABLED");
      expect(applicationSource).not.toContain("springboot-local.p12");
    });
  });

  it("does not keep backend local HTTPS certificates in source resources", () => {
    expect(existsSync(path.join(repoRoot, "backend/src/main/resources/springboot-local.p12"))).toBe(false);
  });
});
