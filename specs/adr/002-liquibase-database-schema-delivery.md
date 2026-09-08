# ADR 002: Liquibase Database Schema Delivery

- Name: ADR-002
- Status: Accepted
- Date: 2026-09-07

## Context

Polyflow modules require database objects for the Axon Framework core and the
Polyflow views. Consumers need a supported way to create and evolve those
objects without extracting SQL from the library. The initial PostgreSQL
baseline must be extended to H2, MariaDB, and Oracle using vendor-specific SQL
derived from the scenario migrations where available.

## Decision

The `polyflow-liquibase` module is the distribution artifact for all
Polyflow-managed database requirements. Consumers apply the schema by
referencing the appropriate classpath master changelog:

- `db/changelog/polyflow/polyflow-core-changelog.xml` for Axon Framework core
  database objects;
- `db/changelog/polyflow/polyflow-view-changelog.xml` for Polyflow view
  database objects.

Every change to a Polyflow-managed database structure must be reflected in
Liquibase changes in this module. Add an ordered, immutable changeset to the
corresponding master changelog rather than modifying a changeset that may
already have been applied by consumers. Include any database-specific SQL
needed by the supported databases.

The supported database scope is H2, MariaDB, PostgreSQL, Oracle, Microsoft
SQL Server, and Azure SQL. Keep the baseline DDL as vendor-specific SQL files
selected by Liquibase's database type; do not translate the schema into
database-agnostic Liquibase change types. DB2 and MySQL are out of scope.

## Consequences

- The library ships its database requirements with a stable integration point
  for consumers.
- Schema changes are versioned and can be applied through Liquibase.
- Database-structure changes must not be completed without the matching
  Liquibase changes.
- Each supported database has explicit SQL baselines for the core and view
  master changelogs.
