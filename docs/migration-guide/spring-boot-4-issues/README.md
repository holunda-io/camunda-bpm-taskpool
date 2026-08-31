# Spring Boot 4 Migration Issue Breakdown

Local issue files for the Spring Boot 4 migration. Publish these in dependency order if they are later moved to GitHub issues.

| Order | Issue | Type | Blocked by |
|---:|---|---|---|
| 1 | [Prepare Camunda Enterprise access for Boot 4 artifacts](01-camunda-enterprise-boot4-artifacts.md) | AFK | None |
| 2 | [Create the Spring Boot 4 baseline with Camunda Enterprise artifacts](02-spring-boot-4-baseline-build.md) | AFK | Issue 1 |
| 3 | [Isolate the Camunda Jackson 2 lane](03-camunda-jackson-2-lane.md) | AFK | Issues 1, 2 |
| 4 | [Migrate Polyflow-owned serialization to Jackson 3](04-polyflow-jackson-3-serialization.md) | AFK | Issues 2, 3 |
| 5 | [Migrate JPA view and DDL generation to Boot 4 / Hibernate 7](05-jpa-view-hibernate-7.md) | AFK | Issue 2 |
| 6 | [Migrate Mongo view tests and properties to Boot 4](06-mongo-view-boot4-tests-properties.md) | AFK | Issue 2 |
| 7 | [Replace temporary classic starters with modular Boot 4 starters](07-modular-boot4-starters.md) | AFK | Issues 3, 4, 5, 6 |
| 8 | [Stabilize Camunda collector and job sender integration tests](08-camunda-integration-tests.md) | AFK | Issues 1, 2, 3 |
| 9 | [Run full migration verification and update release docs](09-verification-and-release-docs.md) | AFK | Issues 4, 5, 6, 7, 8 |
