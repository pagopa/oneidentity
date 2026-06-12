import { defineConfig } from 'vitest/config';
import { config } from 'dotenv';

export default defineConfig({
  test: {
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.mts'],
    globals: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
    },
    clearMocks: true,
    watch: false,
    silent: true,
    env: {
      ...config({ path: './.env.test.local' }).parsed,
    },
    include: ['**/*.test.ts?(x)'],
  },
});
