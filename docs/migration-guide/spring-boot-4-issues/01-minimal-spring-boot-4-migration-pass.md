# Perform a minimal Spring Boot 4 migration pass

## What to build

Move the repository to Spring Boot 4 in one compact migration pass. Keep Camunda CE as a provided dependency, run the Boot 4 OpenRewrite recipe, apply only the compile/test fixes that are actually required, and avoid introducing enterprise-only or classic-starter assumptions unless the build proves they are necessary.

## Acceptance criteria

- [x] The managed Spring Boot version is bumped from 3.5.x to the selected Spring Boot 4.x patch version.
- [x] The Spring Boot 4 OpenRewrite recipe has been run and the diff reviewed.
- [x] Camunda CE dependencies remain provided/test-scoped as appropriate; no Enterprise or `-4` Camunda artifacts are introduced without concrete build evidence.
- [x] Temporary `spring-boot-starter-classic` and `spring-boot-starter-test-classic` are not added unless needed to make the migration compile.
- [x] The full reactor reaches `compile`, or any remaining compile failures are documented with the smallest next fix.

## Blocked by

None - can start immediately
