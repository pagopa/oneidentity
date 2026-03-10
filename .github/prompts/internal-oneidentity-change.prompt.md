---
description: Plan and execute repo-specific changes in oneidentity using the repository's actual stack layout, environment matrix, and workflow conventions
name: internal-oneidentity-change
agent: internal-oneidentity-repo-specialist
argument-hint: area=<infra|workflow|frontend|java-service|python-lambda|mixed> change=<summary> [paths=<comma-separated>]
---

# OneIdentity Repo Task

## Context
Use this prompt when a change must follow the real `oneidentity` repository layout instead of only generic Terraform, workflow, Java, Node.js, or Python guidance.

## Required inputs
- **Area**: ${input:area:infra,workflow,frontend,java-service,python-lambda,mixed}
- **Change**: ${input:change}
- **Relevant paths**: ${input:paths}

## Instructions
1. Read `.github/skills/internal-oneidentity-repo-context/SKILL.md` before editing files.
2. Ground naming, environment choices, validation commands, and examples on the closest real files under `.github/workflows`, `src/infra`, and `src/oneid`.
3. Preserve the existing deployment matrix and naming conventions already used by Terraform stacks and GitHub Actions workflows.
4. Keep Terraform module changes in `src/infra/modules/*` when the logic is reusable, and keep environment wiring in `src/infra/<environment>/<region>`.
5. When frontend packages are touched, use the scripts already declared in the target `package.json`.
6. When Java or Quarkus services are touched, use the Maven wrapper and aggregate profiles already present in `src/oneid`.
7. When Python lambda packaging is touched, keep the workflow-compatible directory layout and dependency installation flow intact.
8. Report the target files used as grounding evidence plus the validation commands you ran.

## Minimal example
- Input: `area=infra change="Update the prod eu-south-1 API Gateway stack to use an existing module variable" paths=src/infra/prod/eu-south-1/main.tf`
- Expected output:
  - Changes grounded on the closest files in `src/infra/prod/eu-south-1` and `src/infra/modules/*`.
  - Existing environment and region naming preserved.
  - Relevant Terraform and repository validation commands executed and reported.

## Validation
- Run `bash .github/scripts/validate-copilot-customizations.sh --scope root --mode strict` after changing Copilot assets.
- Run the area-specific validation commands listed in `.github/skills/internal-oneidentity-repo-context/SKILL.md`.
