---
description: YAML formatting and clarity conventions for stable, maintainable repository configuration files.
applyTo: "**/*.yml,**/*.yaml"
---

# YAML Instructions

## Scope

-   Use these rules for repository YAML assets, including configuration files, metadata, and workflows.
-   When editing GitHub Actions YAML, defer to the more specific workflow instructions for SHA pinning, permissions, and dispatch behavior.

## Formatting

-   Use 2-space indentation.
-   Avoid tabs.
-   Keep key names stable and readable.

## Best practices

-   Quote values only when needed for correctness.
-   Keep anchors/aliases simple; prefer clarity.
-   Keep comments concise and in English.
-   Preserve existing key order and structure when the file is machine-managed.

## Validation

-   Validate syntax before commit.
-   Reuse existing schema/style in the target repository.
-   For GitHub Actions workflows, validate against the GitHub Actions schema when available.
