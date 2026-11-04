---
description: Bash scripting standards for safe execution, guard clauses, and consistent runtime logs in repository automation scripts.
applyTo: "**/*.sh"
---

# Bash Instructions

## Mandatory rules

-   Use Bash only: `#!/usr/bin/env bash`.
-   Add a header comment with purpose and usage examples.
-   For human-facing runtime logs, use minimal, relevant emoji markers (`ℹ️ ✅ ⚠️ ❌`) when they improve visibility; keep machine-readable output free of decoration.
-   Prefer early return and simple, readable functions.
-   Apply these rules for both create and modify operations.

## Scope

-   Use this file for repository automation scripts, including `scripts/` and `.github/scripts/` helpers.
-   Keep reusable logic in the owning package or module when the script would otherwise grow into application code.

## Standard skeleton

```bash
#!/usr/bin/env bash
#
# Purpose: Explain what this script does.
# Usage examples:
#   ./script.sh --help
#   ./script.sh --input data.json
#   ./script.sh --input <file content>


set -euo pipefail
```

## Best practices

-   Quote parameter expansions (`"$var"`); leave them unquoted only when word splitting or pathname expansion is intentional.
-   Prefer Bash constructs such as `[[ ... ]]`, `$(...)`, and arrays when they make quoting and argument handling clearer.
-   Check for external commands with `command -v` when they are required at runtime, especially for optional dependencies.
-   Use guard clauses and functions for cohesive tasks; optimize for clear responsibilities rather than an arbitrary function length.
-   Parse and validate the arguments the script actually supports. Use `getopts` for short options; for long options, use a small `case` loop or an existing project parser.
-   Keep output useful and consistent: send diagnostics to stderr, keep normal output predictable, and avoid noisy logging.
-   Validate external input before using it in paths or commands; do not use `eval` to interpret input.
-   Handle expected failures explicitly. `set -euo pipefail` helps catch errors, but does not replace checking command results or using a `trap` when cleanup is needed.
-   Keep the implementation straightforward and avoid abstractions that do not reduce real complexity.
-   Prefer deterministic behavior over implicit environment assumptions.

## Validation

-   `bash -n <script>.sh`
-   `shellcheck -s bash <script>.sh` (if available)
