import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const backendRoot = path.join(repoRoot, "backend");

const readBackendSource = (relativePath) => readFileSync(path.join(backendRoot, relativePath), "utf8");
const readRepoSource = (relativePath) => readFileSync(path.join(repoRoot, relativePath), "utf8");

describe("backend Flyway migration setup", () => {
  it("adds Flyway dependencies for Spring Boot and MySQL migrations", () => {
    const pom = readBackendSource("pom.xml");

    expect(pom).toContain("<artifactId>flyway-core</artifactId>");
    expect(pom).toContain("<artifactId>flyway-mysql</artifactId>");
  });

  it("routes migrations by database vendor and disables Spring SQL init", () => {
    const app = readBackendSource("src/main/resources/application.yml");
    const testApp = readBackendSource("src/test/resources/application-test.yml");

    expect(app).toContain("flyway:");
    expect(app).toContain("enabled: ${PLATFORM_FLYWAY_ENABLED:true}");
    expect(app).toContain("locations: classpath:db/migration/{vendor}");
    expect(app).toContain("baseline-on-migrate: ${PLATFORM_FLYWAY_BASELINE_ON_MIGRATE:true}");
    expect(testApp).toContain("locations: classpath:db/migration/{vendor}");
    expect(testApp).toContain("mode: never");
  });

  it("keeps MySQL and H2 baseline migrations under Flyway standard directories", () => {
    const mysqlMigration = "src/main/resources/db/migration/mysql/V1__init_schema.sql";
    const h2Migration = "src/main/resources/db/migration/h2/V1__init_schema.sql";

    expect(existsSync(path.join(backendRoot, mysqlMigration))).toBe(true);
    expect(existsSync(path.join(backendRoot, h2Migration))).toBe(true);

    const mysql = readBackendSource(mysqlMigration);
    const h2 = readBackendSource(h2Migration);

    [
      "CREATE TABLE `user`",
      "CREATE TABLE `job_category`",
      "CREATE TABLE `job`",
      "CREATE TABLE `ai_assessment_session`",
      "CREATE TABLE `ai_resume_history`",
      "CREATE TABLE `ai_question_history`",
      "CREATE TABLE `ai_interview_round_history`",
      "INSERT INTO `job_category`",
      "INSERT INTO `job`",
    ].forEach((snippet) => {
      expect(mysql).toContain(snippet);
    });

    [
      "CREATE TABLE user",
      "CREATE TABLE job_category",
      "CREATE TABLE job",
      "CREATE TABLE ai_assessment_session",
      "CREATE TABLE ai_resume_history",
      "CREATE TABLE ai_question_history",
      "CREATE TABLE ai_interview_round_history",
    ].forEach((snippet) => {
      expect(h2).toContain(snippet);
    });

    expect(mysql).not.toContain("DROP TABLE");
    expect(mysql).not.toContain("LOCK TABLES");
    expect(mysql).not.toContain("UNLOCK TABLES");
    expect(mysql).not.toContain("FOREIGN_KEY_CHECKS");
  });

  it("lets Flyway own schema initialization in both compose environments", () => {
    const devCompose = readRepoSource("docker-compose.yml");
    const prodCompose = readRepoSource("docker-compose.prod.yml");

    expect(devCompose).not.toContain("docker-entrypoint-initdb.d");
    expect(prodCompose).not.toContain("docker-entrypoint-initdb.d");
    expect(devCompose).toContain("dockerfile: Dockerfile.dev");
    expect(prodCompose).toContain("dockerfile: Dockerfile");
  });
});
