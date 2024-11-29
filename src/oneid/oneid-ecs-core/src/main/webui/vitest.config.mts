import { configDefaults, defineConfig } from 'vitest/config';
import { config } from 'dotenv';

export default defineConfig({
  test: {
    globals: true,
    setupFiles: './vitest.setup.mts',
    environment: 'jsdom',
    clearMocks: true,
    watch: false,
    silent: true,
    coverage: {
      provider: 'v8',
      skipFull: true,
      reportOnFailure: true,
      exclude: [...configDefaults.exclude, '**/*.test.ts?(x)', 'src/index.tsx'],
      include: ['src/**/*.ts?(x)'],
      thresholds: {
        lines: 80,
        branches: 80,
      },
    },
    env: {
      ...config({ path: './.env.test.local' }).parsed,
    },
    include: ['**/*.test.ts?(x)'],
  },
});
