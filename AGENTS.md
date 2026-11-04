# AGENTS.md - oneidentity

## Repository

OneIdentity is an identity platform with Terraform-managed infrastructure under `src/infra/` and application services, frontends, and lambdas under `src/oneid/`.

Follow the conventions of the package or module being changed. Use the applicable file-specific guidance in `.github/instructions/`; keep this guide focused on repository-wide context and practices.

## Working practices

- Keep changes limited to the requested behavior and preserve unrelated work already present in the working tree.
- Do not run destructive operations, deploy, or apply infrastructure changes without explicit authorization.
- Do not commit or push changes unless explicitly requested.
- Do not expose secrets in source code, logs, command output, or documentation.
- Update or add tests when behavior changes and validate the affected area before reporting completion.

## Repository context

- Infrastructure stacks live in `src/infra/<environment>/<region>`; reusable Terraform modules live in `src/infra/modules/`.
- The established environment and region matrix is `dev/eu-south-1`, `uat/eu-south-1`, `prod/eu-south-1`, and `prod/eu-central-1`.
- The Maven workspace for Quarkus services and Java lambdas is rooted at `src/oneid/pom.xml`; use `src/oneid/mvnw` and Java 21.
- Frontend packages include `src/oneid/oneid-control-panel` and `src/oneid/oneid-fe`; follow their existing Yarn, React, Vite, TypeScript, MUI, and Emotion patterns.
- Python lambdas include `src/oneid/oneid-lambda-assertion`; keep packaging aligned with its deployment workflow.

## Validation

- For application changes, use the relevant package scripts and Maven workspace commands; prefer checks scoped to the affected package.
- For infrastructure changes, format and validate the targeted Terraform module or environment, then review a plan before any apply.
- For shell changes, run `bash -n` on changed scripts and ShellCheck when available.
- For Python changes, run `python -m compileall` on changed paths and the relevant tests when present.

## Commit messages

Use Conventional Commits, following the repository's commitlint configuration. Format messages as `type(scope): summary` (scope is optional); keep the summary concise and describe the change rather than the branch.
