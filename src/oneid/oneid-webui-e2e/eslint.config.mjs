import eslint from '@eslint/js';
import eslintPluginPrettierRecommended from 'eslint-plugin-prettier/recommended';
import tsParser from '@typescript-eslint/parser';
import tseslint from '@typescript-eslint/eslint-plugin';
import playwright from 'eslint-plugin-playwright';
import globals from 'globals';

export default [
  eslint.configs.recommended,
  {
    files: ['**/*.ts'], // Lint TypeScript files
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        sourceType: 'module' // Use ES modules
      },
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    plugins: {
      '@typescript-eslint': tseslint,
      playwright
    }
  },
  {
    files: ['tests/**/*.ts'], // Playwright test files
    rules: {
      // Override or add test-specific rules if needed
    }
  },
  eslintPluginPrettierRecommended
];
