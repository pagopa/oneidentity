import globals from 'globals';
import pluginJs from '@eslint/js';
import tseslint from 'typescript-eslint';
import pluginReact from 'eslint-plugin-react';
import functional from 'eslint-plugin-functional';
import prettier from 'eslint-plugin-prettier/recommended';
import sonarjs from 'eslint-plugin-sonarjs';
import reactHooks from 'eslint-plugin-react-hooks';

export default tseslint.config([
  functional.configs.stylistic,
  pluginJs.configs.recommended,
  pluginReact.configs.flat.recommended,
  prettier,
  sonarjs.configs.recommended,
  ...tseslint.configs.strict,
  ...tseslint.configs.stylistic,
  {
    ignores: ['dist/', 'coverage/'],
  },
  {
    settings: {
      react: { version: 'detect' },
    },
    languageOptions: {
      globals: globals.browser,
      parser: tseslint.parser,
      parserOptions: {
        projectService: true,
      },
    },
    rules: {
      eqeqeq: ['error', 'smart'],
      'react/jsx-uses-react': 'off',
      'react/react-in-jsx-scope': 'off',
      'functional/immutable-data': 'error',
      '@typescript-eslint/consistent-type-definitions': ['warn', 'type'],
      '@typescript-eslint/array-type': ['warn', { default: 'generic' }],
    },
  },
  {
    plugins: {
      'react-hooks': reactHooks,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
    },
  },
  {
    files: ['*.mjs', '*.cjs', '*.js?(x)'],
    ...tseslint.configs.disableTypeChecked,
  },
]);
