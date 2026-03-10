---
name: internal-oneidentity-repo-context
description: Repository-specific topology, naming patterns, and validation guidance for working in the oneidentity repository.
---

# OneIdentity Repo Context

## When to use
- Changes that depend on the real `oneidentity` repository layout across `.github/workflows`, `src/infra`, and `src/oneid`.
- Repository-local Copilot customizations that must stay aligned with One Identity naming, environment, and validation patterns.
- Mixed-stack work that crosses Terraform, GitHub Actions, Node.js frontends, Quarkus services, and lambda packaging.

## Repository map
- `.github/workflows/*.yml` and `.github/workflows/*.yaml` define CI and deployment flows, including Terraform plans, frontend tests, and lambda deployments.
- `src/infra/<environment>/<region>` contains environment stacks such as `src/infra/dev/eu-south-1`, `src/infra/uat/eu-south-1`, `src/infra/prod/eu-south-1`, and `src/infra/prod/eu-central-1`.
- `src/infra/modules/*` contains reusable Terraform modules for backend, frontend, IAM, network, storage, DNS, monitoring, and related infrastructure.
- `src/oneid/oneid-control-panel` is a Node.js and Vite admin frontend with `yarn lint` and `yarn test:coverage` scripts.
- `src/oneid/oneid-fe` is the main frontend package.
- `src/oneid/oneid-ecs-core` and `src/oneid/oneid-ecs-internal-idp` are Quarkus services built from the Maven workspace rooted at `src/oneid/pom.xml`.
- `src/oneid/oneid-lambda-*` contains lambda projects, including Python packaging flows such as `oneid-lambda-assertion` and Quarkus-based Java lambdas such as `oneid-lambda-service-metadata`.

## Grounded conventions
- Keep the existing environment and region matrix already used in `.github/workflows/terraform-plan.yml`: `dev/eu-south-1`, `uat/eu-south-1`, `prod/eu-south-1`, and `prod/eu-central-1`.
- Reuse the existing short-code mappings already used in `.github/workflows/deploy-lambda-assertion.yml`: `dev -> d`, `uat -> u`, `prod -> p`, `eu-south-1 -> es-1`, `eu-central-1 -> ec-1`.
- Preserve Terraform resource naming derived from `local.project = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)` and follow the existing `format("%s-...", local.project)` pattern for resource names.
- Prefer module-level reuse before stack-local duplication: reusable logic belongs in `src/infra/modules/*`, while environment-specific values stay in `src/infra/<environment>/<region>`.
- Keep workflow path filters and dispatch behavior aligned with existing files. For example, `test-fe-admin-control-panel.yaml` scopes PR testing to the control-panel path, and `deploy-lambda-assertion.yml` builds from `src/oneid/oneid-lambda-assertion`.
- Keep pinned GitHub Action SHAs with adjacent release comments when workflow `uses:` entries are changed.

## Validation by area
- Copilot assets: `bash .github/scripts/validate-copilot-customizations.sh --scope root --mode strict`
- Terraform:
  - `terraform fmt -recursive`
  - `terraform validate` from each touched stack or module directory
- Frontend control panel:
  - `yarn install --frozen-lockfile`
  - `yarn lint`
  - `yarn test:coverage`
- Java and Quarkus services:
  - Use `src/oneid/mvnw` with the aggregate profile already wired for the touched service or workflow.
  - Keep Java 21 and the Maven workspace rooted at `src/oneid/pom.xml`.
- Python lambdas:
  - `python -m compileall <changed_lambda_paths>`
  - Keep `requirements.txt` and packaging steps compatible with the target workflow.

## Grounding checklist
- Inspect the closest workflow, stack, package, or service file before inventing names, examples, or validation steps.
- If multiple patterns exist, narrow the change to the specific path family you inspected instead of writing repo-wide generic guidance.
- Stop and report missing grounding when the target area does not have a stable pattern yet.

## Validation
- Run `bash .github/scripts/validate-copilot-customizations.sh --scope root --mode strict` after changing repository-local Copilot assets.
- Run the relevant area-specific checks from the "Validation by area" section.
