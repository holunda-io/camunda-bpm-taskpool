# Prepare Camunda Enterprise access for Boot 4 artifacts

## What to build

Prepare the repository and documentation for the Spring Boot 4 line's Camunda Enterprise requirement without changing the active Spring Boot 3 dependency graph. This issue should make the Enterprise artifact source available and document the decision, but it must not switch runtime dependencies to Camunda Spring Boot 4 artifacts yet.

## Acceptance criteria

- [ ] Local and CI builds can access the Camunda Enterprise Maven repository without storing credentials in source control.
- [ ] The selected Enterprise 7.24.x patch version is documented as the target for the Boot 4 migration.
- [ ] The Spring Boot 4 `-4` Camunda starter artifact names are documented for the next issue.
- [ ] Existing active dependencies continue to use the Spring Boot 3-compatible Camunda artifacts.
- [ ] The repository still builds against the existing Spring Boot 3.5 baseline after this issue is merged.
- [ ] Documentation states that Spring Boot 4-compatible Polyflow artifacts require Camunda Enterprise dependencies.

## Blocked by

None - can start immediately
