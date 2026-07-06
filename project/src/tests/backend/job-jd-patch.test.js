import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const patchPath = path.join(
  repoRoot,
  "backend/src/main/resources/sql/patches/20260706_update_job_jds.sql",
);
const initSqlPath = path.join(repoRoot, "backend/src/main/resources/sql/interview_agent.sql");

describe("job JD patch", () => {
  it("updates all current jobs with structured JD content", () => {
    expect(existsSync(patchPath)).toBe(true);

    const patch = readFileSync(patchPath, "utf8");
    for (let id = 1; id <= 16; id += 1) {
      expect(patch).toContain(`WHERE id = ${id};`);
    }
    expect(patch.match(/岗位职责/g)?.length).toBeGreaterThanOrEqual(16);
    expect(patch.match(/任职要求/g)?.length).toBeGreaterThanOrEqual(16);
    expect(patch.match(/加分项/g)?.length).toBeGreaterThanOrEqual(16);
  });

  it("seeds job categories and mainstream JD jobs in the initial schema", () => {
    const initSql = readFileSync(initSqlPath, "utf8");

    expect(initSql).toContain("Seed data for job categories and mainstream JDs");
    expect(initSql).toContain("INSERT INTO `job_category`");
    expect(initSql).toContain("INSERT INTO `job`");
    expect(initSql).toContain("机器学习工程师");
    expect(initSql).toContain("自动化测试工程师");
    expect(initSql).toContain("传感器开发工程师");
    expect(initSql.match(/岗位职责/g)?.length).toBeGreaterThanOrEqual(16);
    expect(initSql.match(/任职要求/g)?.length).toBeGreaterThanOrEqual(16);
    expect(initSql.match(/加分项/g)?.length).toBeGreaterThanOrEqual(16);
  });
});
