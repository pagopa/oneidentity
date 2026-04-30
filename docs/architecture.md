# Architecture

## 1. Purpose

This repository owns the `oneidentity` platform: an AWS-hosted identity system with Quarkus-based backend services, browser frontends, support Lambdas, and Terraform-managed infrastructure.

## 2. System overview

`oneidentity` is a hybrid application-and-infrastructure repository.

- Application code lives under `src/oneid/`.
- The main backend stack is Java 21 + Quarkus.
- Shared Java libraries are grouped under `oneid-common`.
- Browser applications exist as separate React/Vite packages.
- Infrastructure is defined with Terraform under `src/infra/` and is organized by environment and AWS region.
- CI/CD is split between quality gates, Terraform plan/apply workflows, ECS deployment workflows, and per-Lambda deployment workflows.

ASCII overview:

```text
Browser clients
  -> CloudFront / frontend assets
  -> API Gateway / DNS
  -> ECS services (core, internal IdP)
  -> shared AWS services and support Lambdas

Terraform env roots
  -> shared modules (IAM, network, frontend, storage, backend, Cognito, monitoring)
```

## 3. Current vs intended architecture

| Area | Current architecture | Intended architecture | Status | Evidence |
| --- | --- | --- | --- | --- |
| Repository shape | Hybrid repo with application code in `src/oneid/` and Terraform infrastructure in `src/infra/`. | Not explicitly documented. | Evidenced | `src/oneid/`, `src/infra/`, `README.md` |
| Backend runtime | Java 21 + Quarkus services with shared `oneid-common` libraries. | Not explicitly documented. | Documented | `README.md`, `src/oneid/pom.xml`, `src/oneid/oneid-ecs-core/pom.xml` |
| Frontend runtime | Separate browser packages exist for user and control-panel experiences. | Not explicitly documented. | Evidenced | `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json` |
| Infrastructure topology | Environment/region Terraform roots instantiate shared modules for backend, frontend, storage, IAM, network, and identity services. | Not explicitly documented. | Evidenced | `src/infra/dev/eu-south-1/main.tf`, `src/infra/modules/*` |
| Deployment topology | CI/CD uses workflow-specific paths for Terraform, ECS deploys, Lambda deploys, and e2e/quality gates. | Not explicitly documented. | Evidenced | `.github/workflows/terraform-plan.yml`, `.github/workflows/deploy-oneid-core.yml`, `.github/workflows/deploy-lambda-*.yml`, `.github/workflows/test-fe-e2e.yml`, `.github/workflows/code-review.yml` |

## 4. Technology stack

| Area | Technology | Status | Evidence |
| --- | --- | --- | --- |
| Language | Java 21 | Documented | `README.md`, `src/oneid/pom.xml` |
| Language | TypeScript | Evidenced | `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json`, `src/oneid/oneid-webui-e2e/package.json` |
| Runtime | Quarkus 3.x | Evidenced | `src/oneid/pom.xml`, `src/oneid/oneid-ecs-core/pom.xml` |
| Runtime | React + Vite browser apps | Evidenced | `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json` |
| Build | Maven wrapper with profile-based aggregates | Evidenced | `src/oneid/pom.xml`, `.github/workflows/code-review.yml` |
| Build | Yarn package-local frontend/e2e scripts | Evidenced | `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json`, `src/oneid/oneid-webui-e2e/package.json` |
| Test | JUnit / Quarkus test stack | Evidenced | `src/oneid/oneid-ecs-core/pom.xml` |
| Test | Vitest | Evidenced | `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json` |
| Test | Playwright e2e | Evidenced | `src/oneid/oneid-webui-e2e/package.json`, `.github/workflows/test-fe-e2e.yml` |
| IaC / Deploy | Terraform | Evidenced | `src/infra/.terraform-version`, `src/infra/dev/eu-south-1/main.tf`, `.github/workflows/terraform-plan.yml` |
| IaC / Deploy | AWS ECS, Lambda, API Gateway, CloudFront, Cognito, S3, DynamoDB, SQS, SNS, KMS, Route53 | Evidenced | `src/infra/dev/eu-south-1/main.tf`, `src/infra/modules/backend/ecs.tf` |
| Automation | GitHub Actions | Evidenced | `.github/workflows/*.yml` |

## 5. Repository map

| Path | Responsibility | Notes |
| --- | --- | --- |
| `src/oneid/` | Application workspace | Hybrid Java + Node area for backend, frontend, and support packages. |
| `src/oneid/oneid-common/` | Shared Java libraries | Contains `connector`, `model`, `utils`, and `producer`. |
| `src/oneid/oneid-ecs-core/` | Primary Quarkus backend runtime | Depends on `oneid-common` and AWS integrations. |
| `src/oneid/oneid-ecs-internal-idp/` | Internal IdP runtime | Separate deploy path; details should be validated before major refactors. |
| `src/oneid/oneid-fe/` | User-facing browser frontend | React/Vite package with Vitest and ESLint scripts. |
| `src/oneid/oneid-control-panel/` | Admin/control-panel frontend | React/Vite package with its own package-local tooling. |
| `src/oneid/oneid-webui-e2e/` | End-to-end UI tests | Playwright-based validation. |
| `src/oneid/oneid-lambda-*/` | Support Lambda modules | Multiple operational Lambdas with dedicated deploy workflows. |
| `src/infra/` | Terraform infrastructure | Shared modules plus env/region roots. |
| `src/infra/modules/` | Reusable Terraform building blocks | Shared high-blast-radius infrastructure surfaces. |
| `src/infra/{dev,uat,prod}/` | Environment and region entrypoints | Root stacks instantiate shared modules. |
| `.github/workflows/` | CI/CD and operational automation | Includes quality gates, Terraform, ECS, and Lambda deployment flows. |
| `docs/` | Supporting diagrams and documentation assets | `docs/architecture.md` is the canonical architecture contract. |

## 6. Architectural boundaries

The repository is divided into a small number of stable architectural areas.

### Application runtime

- `oneid-ecs-core` is the primary Quarkus backend service.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-ecs-core/pom.xml`
- `oneid-ecs-internal-idp` is a separate runtime/deployable unit.
  - Status: Evidenced
  - Evidence: `src/oneid/pom.xml`, `.github/workflows/deploy-internal-idp.yml`
- The repository also contains many support Lambda modules for auxiliary identity flows and integrations.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-lambda-*/`, `.github/workflows/deploy-lambda-*.yml`

### Shared Java base

- `oneid-common` owns shared connector, model, utils, and producer libraries.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-common/pom.xml`
- `oneid-ecs-core` depends on `oneid-common` artifacts.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-ecs-core/pom.xml`

### Browser applications

- `oneid-fe` and `oneid-control-panel` are dedicated browser applications built with React/Vite.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json`
- The exact packaging/deployment handoff between these apps and the backend/infra layers is only partially visible from the inspected files.
  - Status: Inferred
  - Evidence: `src/oneid/oneid-fe/package.json`, `src/infra/dev/eu-south-1/main.tf`

### Infrastructure

- Terraform env roots compose shared modules for IAM, DNS, network, frontend delivery, storage, SNS, SQS, backend, and Cognito.
  - Status: Evidenced
  - Evidence: `src/infra/dev/eu-south-1/main.tf`
- Shared modules under `src/infra/modules/` are reused across environments and regions.
  - Status: Evidenced
  - Evidence: `src/infra/modules/*`, `.github/workflows/terraform-plan.yml`

### Test and quality layers

- Java verification is centered on Maven profiles, especially `oneid-all`.
  - Status: Evidenced
  - Evidence: `src/oneid/pom.xml`, `.github/workflows/code-review.yml`
- Browser e2e coverage is owned by the Playwright package `oneid-webui-e2e`.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-webui-e2e/package.json`, `.github/workflows/test-fe-e2e.yml`

## 7. Dependency rules

### Allowed direction

- Environment/region Terraform roots may depend on shared modules under `src/infra/modules/`.
- Runtime services may depend on `oneid-common` shared libraries.
- Browser apps may depend on their own package-local frontend libraries and backend APIs.
- Quality workflows may call the application and infrastructure entrypoints they validate or deploy.

### Avoid / forbidden

- Do not make shared Terraform modules depend on environment-specific roots.
- Do not move application-specific logic into `oneid-common` unless it is genuinely reusable across services.
- Do not assume browser packaging is interchangeable with backend packaging; the current handoff is not fully documented.
- Do not change workflow/env/region deployment matrices casually; they encode real operational topology.
- Lambda-to-core or core-to-Lambda coupling rules beyond current build/deploy wiring are Unknown / To verify.

## 8. Key flows

### Runtime flow

1. Browser clients reach DNS/API entrypoints and frontend delivery layers managed by Terraform.
2. Requests are routed toward backend runtime services, including `oneid-ecs-core` and optionally the internal IdP path.
3. Backend services interact with AWS-managed identity and data services such as Cognito, DynamoDB, S3, KMS, SQS, and SNS.
4. Support Lambdas provide auxiliary capabilities such as client registration, metadata handling, cache invalidation, and status/integration flows.

Evidence: `src/infra/dev/eu-south-1/main.tf`, `src/infra/modules/backend/ecs.tf`, `src/oneid/oneid-ecs-core/pom.xml`

### Build/test flow

1. Java verification runs through `src/oneid/mvnw -f src/oneid/pom.xml -P oneid-all` in CI.
2. Backend tests use the Quarkus/JUnit stack configured in service POMs.
3. Browser packages expose package-local `build`, `test`, and `lint` commands.
4. UI e2e coverage runs with Playwright in `src/oneid/oneid-webui-e2e/`.

Evidence: `.github/workflows/code-review.yml`, `src/oneid/pom.xml`, `src/oneid/oneid-fe/package.json`, `src/oneid/oneid-control-panel/package.json`, `.github/workflows/test-fe-e2e.yml`

### Deployment/operations flow

1. Terraform plan runs across env/region roots for non-main branches.
2. ECS deployment workflows build images, push to ECR, render task definitions, and deploy services per environment/region.
3. Lambda modules have dedicated deployment workflows.
4. Additional operational workflows exist for metadata upload and region switching.

Evidence: `.github/workflows/terraform-plan.yml`, `.github/workflows/deploy-oneid-core.yml`, `.github/workflows/deploy-lambda-*.yml`, `.github/workflows/upload-metadata.yml`, `.github/workflows/switch-region.yml`

## 9. Configuration and environment

- GitHub Actions workflows assume AWS OIDC role assumption and environment-scoped variables.
  - Evidence: `.github/workflows/terraform-plan.yml`, `.github/workflows/deploy-oneid-core.yml`
- Maven builds requiring `it.pagopa.maven:depcheck` need a generated `src/oneid/settings.xml` with GitHub Packages credentials.
  - Evidence: `README.md`, `.github/workflows/code-review.yml`, `.github/workflows/deploy-oneid-core.yml`
- Terraform configuration is separated by environment and region under `src/infra/{dev,uat,prod}/`.
  - Evidence: `src/infra/dev/eu-south-1/main.tf`, `src/infra/prod/eu-south-1/main.tf`, `src/infra/prod/eu-central-1/main.tf`
- Local development supports Quarkus dev mode and Docker Compose.
  - Evidence: `README.md`, `src/oneid/docker-compose.yaml`
- Mock assets under `src/oneid/docker_mock/` are demo-only and should not be treated as production secrets.
  - Evidence: `README.md`

## 10. Testing and validation

| Change type | Suggested validation | Evidence |
| --- | --- | --- |
| Shared Java library or Quarkus backend changes | `src/oneid/mvnw -B clean verify -f src/oneid/pom.xml -P oneid-all -s src/oneid/settings.xml` | `.github/workflows/code-review.yml` |
| `oneid-fe` changes | `yarn test` and `yarn lint` in `src/oneid/oneid-fe/` | `src/oneid/oneid-fe/package.json` |
| `oneid-control-panel` changes | `yarn test` and `yarn lint` in `src/oneid/oneid-control-panel/` | `src/oneid/oneid-control-panel/package.json` |
| UI flow changes | `yarn playwright test` in `src/oneid/oneid-webui-e2e/` | `src/oneid/oneid-webui-e2e/package.json`, `.github/workflows/test-fe-e2e.yml` |
| Terraform env/module changes | Run the same `terraform plan` flow used by `.github/workflows/terraform-plan.yml` for affected env/region roots | `.github/workflows/terraform-plan.yml` |
| ECS deployment pipeline changes | Review `deploy-oneid-core.yml` and validate image build + task definition assumptions before merge | `.github/workflows/deploy-oneid-core.yml` |

## 11. Architectural decisions visible in the repo

- Decision: Keep shared Java code in a dedicated multi-module base (`oneid-common`) instead of duplicating connector/model/utils logic in each runtime service.
  - Status: Evidenced
  - Evidence: `src/oneid/oneid-common/pom.xml`, `src/oneid/oneid-ecs-core/pom.xml`
  - Trade-off: Reuse improves consistency, but changes in `oneid-common` have broad impact.
  - Related ADR: None found

- Decision: Use profile-based Maven aggregates to control which service combinations are built together.
  - Status: Evidenced
  - Evidence: `src/oneid/pom.xml`
  - Trade-off: Keeps builds targeted, but module inclusion differences between profiles require care.
  - Related ADR: None found

- Decision: Reuse Terraform modules across env/region roots rather than duplicating full stacks.
  - Status: Evidenced
  - Evidence: `src/infra/dev/eu-south-1/main.tf`, `src/infra/modules/*`
  - Trade-off: Promotes consistency, but module edits have cross-environment blast radius.
  - Related ADR: None found

- Decision: Separate quality gates by concern instead of relying on a single pipeline.
  - Status: Evidenced
  - Evidence: `.github/workflows/code-review.yml`, `.github/workflows/test-fe-e2e.yml`, `.github/workflows/terraform-plan.yml`
  - Trade-off: Improves clarity per domain, but operational behavior is spread across workflows.
  - Related ADR: None found

- Decision: Model production infrastructure as multi-region while lower environments remain narrower.
  - Status: Evidenced
  - Evidence: `.github/workflows/terraform-plan.yml`
  - Trade-off: Improves resilience for prod, but increases deployment and change-management complexity.
  - Related ADR: None found

## 12. Architecture contract usage

`AGENTS.md` is the assistant routing and governance bridge. This file is the architecture contract it loads for architectural facts and validation context.

- Read this file before structural, cross-file, refactoring, infrastructure, workflow, backend, frontend, Lambda, or documentation changes that affect repository architecture.
- Do not change module boundaries, dependency direction, runtime flows, deployment flows, or validation commands without updating this file.
- If a requested change conflicts with this architecture, explain the conflict before editing.
- Do not treat this file as immutable. If the requested change intentionally changes architecture, propose the architecture update explicitly.
- Treat `src/oneid/oneid-common/`, `src/infra/modules/`, and deployment workflows under `.github/workflows/` as high-risk shared surfaces.
- When frontend packaging/deployment details are unclear, stop and mark the uncertainty instead of inferring a new contract.

## 13. Last verified

- Date: 2026-04-30
- Agent/tool: GitHub Copilot
- Files inspected: README, AGENTS bridge, selected Maven POMs, frontend package manifests, Terraform env/module entrypoints, and CI/CD workflows
- Commands considered/run: No repository tests were run; validation evidence was taken from existing workflow and manifest commands
- Confidence: Medium

## 14. Unknown / To verify

- Exact packaging and deployment handoff for `oneid-fe` and `oneid-control-panel`.
- Whether all `oneid-lambda-*` modules are active production components or a mix of active and legacy paths.
- Operational intent and safety constraints of `.github/workflows/switch-region.yml`.
- Operational role and required preconditions of `.github/workflows/upload-metadata.yml`.
