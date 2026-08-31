# Create the Spring Boot 4 baseline with Camunda Enterprise artifacts

## What to build

Create the first compiling Spring Boot 4 baseline for the multi-module build and switch the Camunda integration modules to Enterprise Spring Boot 4 artifacts in the same slice. This issue must be atomic so the repository never has a merged state that combines Spring Boot 3 with Camunda Spring Boot 4 artifacts.

## Acceptance criteria

- [ ] The managed Spring Boot BOM is bumped from 3.5.x to the selected Spring Boot 4.x patch version.
- [ ] Camunda dependency management uses the selected Enterprise 7.24.x patch version.
- [ ] Camunda Spring Boot starter dependencies use the Spring Boot 4 artifact names where applicable, such as `camunda-bpm-spring-boot-starter-4`.
- [ ] The OpenRewrite Spring Boot 4 recipe has been run and its diff reviewed.
- [ ] Temporary `spring-boot-starter-classic` and `spring-boot-starter-test-classic` bridges are added only if needed to get an initial baseline.
- [ ] `spring-boot-jackson2` is not added globally; it is reserved for Camunda-bound modules that need Jackson 2.
- [ ] The Camunda integration modules compile far enough to expose only code/API migration errors, not missing artifact or repository errors.
- [ ] The full reactor reaches `compile` or has a documented list of remaining compile failures grouped by migration workstream.

## Blocked by

- Issue 1: Prepare Camunda Enterprise access for Boot 4 artifacts
