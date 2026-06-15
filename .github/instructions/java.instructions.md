---
description: Java project standards with DDD boundaries, readability-first design, and deterministic unit testing.
applyTo: "**/*.java"
---

# Java Instructions

## Mandatory rules

- Treat work as project-oriented (services/modules/components), not script-oriented.
- Apply DDD boundaries: keep domain logic inside domain services/entities/value objects.
- Keep infrastructure details (I/O, SDK calls, persistence wiring) outside core domain logic.
- Use ubiquitous language in class names, methods, and domain-level exceptions.
- Add concise purpose JavaDoc for new/changed core classes when intent is not obvious.
- Use emoji logs for key runtime transitions when logging is touched.
- Prefer early return and guard clauses.
- Prioritize readability and maintainability.
- Add unit tests for testable logic.

## Project context

- The `src/oneid` workspace is Quarkus-based and targets Java 21; prefer Quarkus CDI, REST, config, and testing patterns already used in the repository.
- Keep inbound HTTP code in `web/` or `web/controller/`, validation in `web/validator/`, application logic in `service/`, outbound integrations in `connector/`, and state/value types in `model/` and `exception/`.
- Reuse shared helpers from `oneid-common/*` when behavior is shared across modules instead of duplicating logic inside a service or lambda module.
- Keep infrastructure wiring and client setup outside core domain logic.

## Logging

- Prefer `io.quarkus.logging.Log` for runtime logging in Java application code.
- Avoid introducing new logger fields or switching a module to SLF4J or Log4j unless the surrounding module already uses a different logging approach.
- Log state transitions, recoverable failures, and external boundary events with concise, actionable messages.

## Testing defaults

- Use JUnit 5.
- Use BDD-like naming: `@DisplayName` and `given_when_then`.
- For Quarkus components, use `@QuarkusTest` and `@InjectMock` for injected collaborators.
- Use `io.restassured.RestAssured` for REST endpoint and controller tests.
- For AWS-related tests, implement the AWS dependency with LocalStack, using Quarkus Dev Services or Testcontainers patterns already present in the repository.
- Keep unit tests deterministic and isolated.
- For modify tasks with existing tests: change implementation first, run existing tests, and update tests only for intentional behavior changes.

## Reference implementation

- For code and test examples, use the existing Quarkus modules under `src/oneid/` as reference, especially `oneid-ecs-core`, `oneid-ecs-internal-idp`, and the lambda modules.
- Prefer the nearest package family as the example source: `web/controller`, `web/validator`, `service`, `connector`, and the matching `src/test/java` package.
