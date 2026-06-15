---
description: Node.js and TypeScript standards for the OneIdentity React/Vite packages, with DDD-oriented layering, early returns, and deterministic Vitest practices.
applyTo: "**/*.js,**/*.cjs,**/*.mjs,**/*.ts,**/*.tsx"
---

# Node.js Instructions

## Mandatory rules

-   Treat work as product code, not script-oriented glue.
-   Keep domain decisions in domain modules, hooks, or services; keep React components, routing, and adapters thin.
-   For `src/oneid/oneid-control-panel` and `src/oneid/oneid-fe`, preserve the existing React, Vite, TypeScript, MUI, and Emotion patterns already used in the package.
-   Reuse the repository's existing workspace scripts and package-level tooling. Do not introduce a new package manager, test runner, or build workflow when an existing one already exists.
-   Use ubiquitous language for domain names, props, errors, and test cases.
-   Add a concise purpose comment for new or changed core modules only when intent is not obvious.
-   Prefer early return and guard clauses.
-   Keep control flow straightforward and avoid clever abstractions.
-   Add unit tests for testable logic.

## Testing defaults

-   Use the package's existing test stack: `vitest` for React and TypeScript code, with `@testing-library/react` where UI behavior is involved.
-   Keep tests deterministic and isolated.
-   Co-locate tests with the source when that package already follows that pattern.
-   For modify tasks with existing tests: change implementation first, run the relevant package tests, and update tests only for intentional behavior changes.

## Tooling and validation

-   Prefer the scripts already defined in the touched package `package.json`.
-   Check the root `package.json` and the package `package.json` before suggesting or running workspace commands.
-   For the control panel and frontend packages, the usual checks are `yarn lint`, `yarn test`, `yarn test:coverage`, and `yarn build` when the change can affect the production bundle.
-   Keep formatting and linting changes minimal and aligned with the repository's existing ESLint and Prettier setup.

## Reference implementation

-   Use the package's existing tests and source files as the reference implementation for local patterns.
