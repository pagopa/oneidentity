---
description: Security and reliability standards for GitHub Actions workflows with SHA pinning and least privilege.
applyTo: "**/workflows/**"
---

# GitHub Actions Instructions

## Security baseline

-   Prefer OIDC over long-lived secrets.
-   Pin actions to full-length commit SHAs.
-   For each pinned SHA, add an adjacent comment with release/tag and upstream release URL.
-   Keep `permissions` minimal.
-   Avoid `pull_request_target` for untrusted code.

## Workflow baseline

-   Set explicit `timeout-minutes`.
-   Set `concurrency` when jobs can conflict on shared targets.
-   Prefer reusable workflows (`workflow_call`) for repeated pipelines.
-   Use clear English step names.
-   For Terraform jobs: include `fmt -check`, use `-input=false`, and avoid concurrent apply on the same target.
-   Keep environment secrets in protected environments when possible.
-   Keep cache and artifact usage explicit and deterministic.
-   Use matrix strategy only when it improves confidence/cost tradeoff.

## Minimal example

```yaml
name: CI

on:
    push:
        branches: [main]

permissions:
    contents: read

concurrency:
    group: ci-${{ github.workflow }}-${{ github.ref }}
    cancel-in-progress: true

jobs:
    build:
        runs-on: ubuntu-latest
        timeout-minutes: 10
        steps:
            - name: Checkout
              uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2 https://github.com/actions/checkout/releases/tag/v6.0.2
```

Add `id-token: write` only to jobs that need to request an OIDC token.
