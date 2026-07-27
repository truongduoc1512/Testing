import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    globals: true,
    include: ["postman/integration-api/**/*.test.ts"],
    fileParallelism: false,
    isolate: false,
    pool: "threads",
    maxWorkers: 1,
    testTimeout: 20_000,
    hookTimeout: 20_000,
  },
});
