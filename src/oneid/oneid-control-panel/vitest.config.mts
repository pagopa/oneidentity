import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { config } from 'dotenv';

export default defineConfig({
  plugins: [react()],
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
