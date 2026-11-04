# Commit Message Instructions

## Format

`<type>(<scope>): <summary>`

## Types

- `feat`: New behavior
- `fix`: Bug fix
- `docs`: Documentation updates
- `refactor`: Internal restructuring
- `test`: Test changes
- `chore`: Tooling/maintenance

## Rules

- Use conventional commits and follow commitlint rules.
- Scope is optional.
- Type should reflect the changes and not the branch name.
- Use imperative mood.
- Keep summary concise (<= 72 chars preferred).
- Scope should match changed area (for example `terraform`, `scripts`, `workflows`, `docs`).
- Use an English body when context is needed.

## Examples

- `feat(terraform): add policy assignment module`
- `fix: pin checkout action by full SHA`
- `docs(copilot): align prompt and skill references`
- `chore: add changeset`
