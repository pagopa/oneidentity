# AGENTS.md - oneidentity

This file is for GitHub Copilot and AI assistants working in this repository.

## Naming Policy
- Use GitHub Copilot terminology in repository-facing content.
- Do not mention internal runtime names in repository artifacts.
- Treat prompt frontmatter `name:` as the canonical command identifier.
- Repository-internal prompt, skill, and agent filenames must start with `internal-`.
- Repository-internal prompt, skill, and agent `name:` values must also start with `internal-`.

## Decision Priority
1. Apply repository non-negotiables from `.github/copilot-instructions.md`.
2. Apply explicit user requirements for the current task.
3. Load and apply `docs/architecture.md` for architecture boundaries, dependency rules, runtime/deployment flows, high-risk surfaces, unknowns, and area-specific validation.
4. Apply the selected agent behavior (agent-first routing).
5. Apply matching files under `.github/instructions/*.instructions.md` using `applyTo`.
6. Apply selected prompt constraints from `.github/prompts/*.prompt.md`.
7. Apply implementation details from referenced `.github/skills/*/SKILL.md`.
8. If no agent is explicitly selected, default to `TechAIImplementer`.

## Architecture Contract
- Load `docs/architecture.md` before structural, cross-file, refactoring, infrastructure, workflow, backend, frontend, Lambda, or documentation changes that affect repository architecture.
- Treat `docs/architecture.md` as the canonical source for system topology, technology stack, repository map, architectural boundaries, dependency direction, key flows, configuration assumptions, validation by area, and known unknowns.
- Keep AGENTS.md focused on assistant routing, naming policy, governance references, reusable instruction routing, and inventory. Do not duplicate architecture facts here.
- When an intentional change modifies architecture, update `docs/architecture.md` in the same change set.
- If this file and `docs/architecture.md` appear to conflict, use this file for assistant behavior and use `docs/architecture.md` for architecture facts. Report unresolved conflicts before editing.

## Agent Routing

### When to use each agent
- Use `TechAIPlanner` for ambiguous scope, tradeoff analysis, or multi-step design.
- Use `TechAIImplementer` for direct code/config changes and validation-first delivery.
- Use `internal-oneidentity-repo-specialist` for repo-specific work that depends on the actual `src/infra`, `src/oneid`, and `.github/workflows` conventions in this repository.
- Use `TechAIReviewer` for quality gates and defect/regression findings.
- Use `TechAITerraformGuardrails` for Terraform safety and policy guardrail reviews.
- Use `TechAIIAMLeastPrivilege` for role and permission scoping checks.
- Use `TechAIWorkflowSupplyChain` for workflow supply-chain hardening and CI checks.
- Use `TechAISecurityReviewer` as the security-focused review gate.
- Use `TechAIPREditor` when generating pull request content from the repository template.

### Agent composition
- For changes spanning multiple specialist domains, run each relevant specialist and aggregate findings.
- The standard chain for non-trivial work is: `TechAIPlanner` -> `TechAIImplementer` -> `TechAIReviewer` or a matching specialist.

## Governance References
- `.github/security-baseline.md`
- `.github/DEPRECATION.md`
- `.github/repo-profiles.yml`
- `.github/scripts/validate-copilot-customizations.sh`

## Prohibitions
- Apply all non-negotiables from `.github/copilot-instructions.md` plus:
- Never run destructive commands unless explicitly requested.
- Never skip validation after making changes.

## Repository Defaults
- AGENTS.md is the external bridge for assistant behavior and naming; keep runtime references abstract.
- Profile hint for reusable instruction selection: `infrastructure-heavy`.
- Resolve stack, path ownership, and validation from `docs/architecture.md`, target files, and explicit prompt inputs. The agent role remains behavioral, not language-specific.

### Default instruction routing
| Pattern | Instruction |
| --- | --- |
| `**/*.sh` | `bash.instructions.md` |
| `**/actions/**/action.y*ml,**/workflows/**/action.y*ml` | `github-action-composite.instructions.md` |
| `**/workflows/**` | `github-actions.instructions.md` |
| `**/*.java` | `java.instructions.md` |
| `**/authorizations/**/*.json,**/organization/**/*.json,**/src/**/*.json,**/data/**/*.json` | `json.instructions.md` |
| `**/*.md` | `markdown.instructions.md` |
| `**/*.js,**/*.cjs,**/*.mjs,**/*.ts,**/*.tsx` | `nodejs.instructions.md` |
| `**/*.py` | `python.instructions.md` |
| `**/*.sh,**/scripts/**/*.py,**/bin/**/*.py,**/*script*.py` | `scripts.instructions.md` |
| `**/*.tf` | `terraform.instructions.md` |
| `**/*.yml,**/*.yaml` | `yaml.instructions.md` |

### Preferred prompts
- `TechAICloudPolicy`
- `TechAITerraform`
- `internal-oneidentity-change`

### Preferred skills
- `TechAICloudPolicy`
- `TechAITerraformFeature`
- `TechAITerraformModule`
- `internal-oneidentity-repo-context`

### Validation routing
- Use `docs/architecture.md` for architecture and component-specific validation expectations.
- Apply file-type baselines from `.github/copilot-instructions.md` and matching `.github/instructions/*.instructions.md`.
- For repository-local Copilot customization changes, run `bash .github/scripts/validate-copilot-customizations.sh --scope root --mode strict`.

## Repository Inventory (Auto-generated)
This inventory reflects the desired managed baseline plus repository-owned internal Copilot assets already present in the target repository.

### Instructions
- `.github/instructions/bash.instructions.md`
- `.github/instructions/github-action-composite.instructions.md`
- `.github/instructions/github-actions.instructions.md`
- `.github/instructions/java.instructions.md`
- `.github/instructions/json.instructions.md`
- `.github/instructions/markdown.instructions.md`
- `.github/instructions/nodejs.instructions.md`
- `.github/instructions/python.instructions.md`
- `.github/instructions/scripts.instructions.md`
- `.github/instructions/terraform.instructions.md`
- `.github/instructions/yaml.instructions.md`

### Prompts
- `.github/prompts/internal-oneidentity-change.prompt.md`
- `.github/prompts/tech-ai-add-unit-tests.prompt.md`
- `.github/prompts/tech-ai-bash-script.prompt.md`
- `.github/prompts/tech-ai-cloud-policy.prompt.md`
- `.github/prompts/tech-ai-data-registry.prompt.md`
- `.github/prompts/tech-ai-github-action.prompt.md`
- `.github/prompts/tech-ai-github-composite-action.prompt.md`
- `.github/prompts/tech-ai-pr-description.prompt.md`
- `.github/prompts/tech-ai-python-script.prompt.md`
- `.github/prompts/tech-ai-python.prompt.md`
- `.github/prompts/tech-ai-terraform.prompt.md`

### Skills
- `.github/skills/internal-oneidentity-repo-context/SKILL.md`
- `.github/skills/tech-ai-cicd-workflow/SKILL.md`
- `.github/skills/tech-ai-cloud-policy/SKILL.md`
- `.github/skills/tech-ai-composite-action/SKILL.md`
- `.github/skills/tech-ai-data-registry/SKILL.md`
- `.github/skills/tech-ai-pr-editor/SKILL.md`
- `.github/skills/tech-ai-project-python/SKILL.md`
- `.github/skills/tech-ai-script-bash/SKILL.md`
- `.github/skills/tech-ai-script-python/SKILL.md`
- `.github/skills/tech-ai-terraform-feature/SKILL.md`
- `.github/skills/tech-ai-terraform-module/SKILL.md`

### Agents
- `.github/agents/internal-oneidentity-repo-specialist.agent.md`
- `.github/agents/tech-ai-pr-editor.agent.md`
- `.github/agents/tech-ai-github-workflow-supply-chain.agent.md`
- `.github/agents/tech-ai-iam-least-privilege.agent.md`
- `.github/agents/tech-ai-implementer.agent.md`
- `.github/agents/tech-ai-planner.agent.md`
- `.github/agents/tech-ai-reviewer.agent.md`
- `.github/agents/tech-ai-security-reviewer.agent.md`
- `.github/agents/tech-ai-terraform-guardrails.agent.md`
