import { fixupConfigRules, fixupPluginRules } from '@eslint/compat';
import typescriptEslint from '@typescript-eslint/eslint-plugin';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import _import from 'eslint-plugin-import';
import functional from 'eslint-plugin-functional';
import sonarjs from 'eslint-plugin-sonarjs';
import tsParser from '@typescript-eslint/parser';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import js from '@eslint/js';
import { FlatCompat } from '@eslint/eslintrc';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default [
  {
    files: ['**/*.ts', '**/*.tsx'],

    rules: {
      '@typescript-eslint/no-non-null-assertion': 'off',
    },
  },
  {
    ignores: [
      'locales/locales.ts',
      '**/__tests__/**/*',
      'eslint.config.mjs',
      'craco.config.js',
      'definitions/*',
      'src/react-app-env.d.ts',
      '**/Dangerfile.ts',
      'src/reportWebVitals.ts',
    ],
  },
  ...fixupConfigRules(
    compat.extends(
      'eslint:recommended',
      'plugin:@typescript-eslint/eslint-recommended',
      'plugin:@typescript-eslint/recommended',
      'plugin:react/recommended',
      'prettier'
    )
  ),
  {
    plugins: {
      '@typescript-eslint': fixupPluginRules(typescriptEslint),
      react: fixupPluginRules(react),
      'react-hooks': fixupPluginRules(reactHooks),
      import: fixupPluginRules(_import),
      functional,
      sonarjs,
    },

    languageOptions: {
      parser: tsParser,
      ecmaVersion: 'latest',
      sourceType: 'module',

      parserOptions: {
        project: 'tsconfig.json',
      },
    },

    settings: {
      react: {
        version: 'detect',
      },
    },

    rules: {
      'no-case-declarations': 'off',
      'no-inner-declarations': 'off',
      'prefer-const': 'error',
      curly: 'error',

      'spaced-comment': [
        'error',
        'always',
        {
          block: {
            balanced: true,
          },
        },
      ],

      radix: 'error',
      'one-var': ['error', 'never'],
      'object-shorthand': 'error',
      'no-var': 'error',
      'no-param-reassign': 'error',
      'no-underscore-dangle': 'error',
      'no-undef-init': 'error',
      'no-throw-literal': 'error',
      'no-new-wrappers': 'error',
      'no-eval': 'error',
      'no-console': 0,
      'no-caller': 'error',
      'no-bitwise': 'error',
      eqeqeq: ['error', 'smart'],
      'max-classes-per-file': ['error', 1],
      'guard-for-in': 'error',
      complexity: 'error',
      'arrow-body-style': 'error',
      'import/order': 'error',
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/explicit-module-boundary-types': 'off',
      '@typescript-eslint/no-inferrable-types': 'off',
      '@typescript-eslint/no-explicit-any': 'off',

      '@typescript-eslint/array-type': [
        'error',
        {
          default: 'generic',
        },
      ],

      '@typescript-eslint/await-thenable': 'error',
      '@typescript-eslint/consistent-type-assertions': 'error',
      '@typescript-eslint/dot-notation': 'error',

      '@typescript-eslint/member-delimiter-style': [
        'error',
        {
          multiline: {
            delimiter: 'semi',
            requireLast: true,
          },

          singleline: {
            delimiter: 'semi',
            requireLast: false,
          },
        },
      ],

      '@typescript-eslint/no-floating-promises': 'error',
      'no-unused-expressions': 'off',
      '@typescript-eslint/no-unused-expressions': ['error'],
      '@typescript-eslint/prefer-function-type': 'error',
      '@typescript-eslint/restrict-plus-operands': 'error',
      semi: 'off',
      '@typescript-eslint/semi': ['error'],
      '@typescript-eslint/unified-signatures': 'error',
      'react/prop-types': 'off',
      'react/display-name': 'off',
      'react/jsx-key': 'error',

      'react/jsx-no-bind': [
        'error',
        {
          allowArrowFunctions: true,
        },
      ],

      'react-hooks/rules-of-hooks': 'warn',
      'functional/no-let': 'error',
      'functional/immutable-data': 'warn',
      'sonarjs/no-small-switch': 'off',
      'sonarjs/no-duplicate-string': 'off',
      'sonarjs/no-nested-template-literals': 'warn',

      '@typescript-eslint/no-empty-function': [
        'error',
        {
          allow: ['arrowFunctions'],
        },
      ],

      'react/jsx-uses-react': 'off',
      'react/react-in-jsx-scope': 'off',
    },
  },
];
