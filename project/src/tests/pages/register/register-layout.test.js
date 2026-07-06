import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const pageName = path.basename(currentDir);
const source = readFileSync(
  path.join(currentDir, "..", "..", "..", "pages", pageName, "index.vue"),
  "utf8",
);

describe("register page layout", () => {
  it("uses the same split auth layout instead of the legacy floating form card", () => {
    expect(source).toContain("register-layout");
    expect(source).toContain("register-showcase");
    expect(source).toContain("register-panel");
    expect(source).toContain("register-grid-row");
    expect(source).not.toContain("cosmic-content-container");
    expect(source).not.toContain("cosmic-form-area");
    expect(source).toContain("handleRegister");
    expect(source).toContain('router-link to="/pages/login/index"');
  });
});
