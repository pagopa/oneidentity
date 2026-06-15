---
description: Cross-language conventions for repository automation scripts with guard clauses, English runtime output, and thin orchestration.
applyTo: "**/*.sh,**/scripts/**/*.py,**/bin/**/*.py,**/*script*.py"
---

# Script Instructions

## Cross-language rules

-   Start with purpose and usage examples.
-   Use early return and guard clauses.
-   Keep logs and user-facing output in English.
-   Prefer readability over compact or clever code.
-   Keep scripts thin; move reusable business logic into the owning package, module, or library.
-   Prefer deterministic side effects and explicit inputs over hidden environment coupling.

## Scope

-   This file contains only cross-cutting rules.
-   Use `.github/instructions/bash.instructions.md` for Bash-specific requirements.
-   Use `.github/instructions/python.instructions.md` for Python-specific requirements.
