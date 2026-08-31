# Run full migration verification and update release docs

## What to build

Perform the final Spring Boot 4 migration verification and update consumer-facing documentation. The completed migration should be releasable with clear notes about Spring Boot 4, Camunda Enterprise requirements, and Jackson 2/3 coexistence.

## Acceptance criteria

- [ ] Full unit tests pass on the Spring Boot 4 line.
- [ ] Full integration tests pass on the Spring Boot 4 line.
- [ ] Verification is run on Java 17 and Java 21.
- [ ] Migration documentation describes Camunda Enterprise-only support for the Boot 4 line.
- [ ] Migration documentation describes the Jackson 3 default and the scoped Camunda Jackson 2 lane.
- [ ] Release notes identify required consumer actions for Camunda dependencies, Spring Boot version, and Jackson behavior.

## Blocked by

- Issue 4: Migrate Polyflow-owned serialization to Jackson 3
- Issue 5: Migrate JPA view and DDL generation to Boot 4 / Hibernate 7
- Issue 6: Migrate Mongo view tests and properties to Boot 4
- Issue 7: Replace temporary classic starters with modular Boot 4 starters
- Issue 8: Stabilize Camunda collector and job sender integration tests

