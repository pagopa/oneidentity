import { configDefaults, defineConfig } from 'vitest/config';
import { config } from 'dotenv';

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    clearMocks: true,
    watch: false,
    silent: true,
    coverage: {
      provider: 'v8',
      reportOnFailure: true,
      exclude: [
        ...configDefaults.exclude,
        '**/*.test.ts?(x)',
        'src/__tests__/',
        'src/index.tsx',
        'src/reportWebVitals',
        'src/utils/constants.ts',
        'src/global.d.ts',
      ],
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
  resolve: {
    alias: {
      // Add all your absolute paths here
    },
  },
});
