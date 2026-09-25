# ADR 006: Liquibase Test Schema Provisioning

- Name: ADR-006
- Status: Accepted
- Date: 2026-09-25

## Context

The integration-test classpaths still contained Flyway solely to provision
Axon and JPA View tables. Polyflow now publishes these schemas through the
`polyflow-liquibase` artifact. Maintaining both test-only Flyway migrations
and the published Liquibase baselines duplicates the schema definition and can
let the two drift apart.

## Decision

Tests that need Polyflow aggregate or JPA View storage depend on
`polyflow-liquibase` with test scope and configure the matching core or view
Liquibase master changelog. Flyway dependencies and Flyway test configuration
are removed.

Camunda collector integration tests use a dedicated Liquibase test changelog
for their Camunda-owned tables. They do not provision Camunda tables through a
Polyflow deployment changelog or Flyway.

## Consequences

- Liquibase is the sole Polyflow schema provisioner in application and test
  environments.
- Tests exercise the same Polyflow schema baselines that consumers receive.
- Camunda test DDL remains owned by Camunda, while Liquibase provides its test
  execution mechanism.
