# ADR 003: Liquibase Baseline Composition

- Name: ADR-003
- Status: Accepted
- Date: 2026-09-09

## Context

The initial Liquibase delivery provided separate Axon Framework baseline SQL
files for the core and JPA View master changelogs. Those copies diverged by
database dialect, and including both masters could replay the same Axon DDL.
An application using the JPA View nevertheless requires the Axon persistence
objects.

## Decision

The JPA View master changelog includes the core master changelog and owns only
the Polyflow JPA View SQL. The core master is the single canonical source of
Axon Framework DDL.

The H2 SQL uses the H2 PostgreSQL-compatibility dialect already used by the
project's JPA view migrations, rather than MariaDB-specific column types.

Primary-key, foreign-key, unique-constraint, and index names use stable
uppercase prefixes (`PK_`, `FK_`, `UK_`, and `IDX_`) followed by the owning
table and the referenced object or indexed/constrained columns. Names remain
within Oracle's 30-character limit.

Schema-object and column names use `CAPITAL_CASE`; SQL keywords and data types
use lowercase.

Baseline SQL declares columns, primary keys, unique constraints, and foreign
keys in `create table` statements when the referenced table is already
available. `alter table` remains for later schema evolution that cannot be
expressed as part of object creation.

View definitions enumerate selected columns explicitly in every `union`
branch; they do not use `select *`.

The Liquibase module verifies the core and view master changelogs independently
through Spring Boot integration tests. PostgreSQL, MariaDB, and Microsoft SQL
Server use Testcontainers; H2 runs in embedded PostgreSQL-compatibility mode
because Testcontainers provides no H2 database module. Shared abstract core and
view suites keep the provisioning assertions consistent across these dialects.

## Consequences

- Fresh JPA View installations provision complete Axon and view schemas.
- A central master includes either the core master or the view master; the
  latter already includes the former.
- Axon DDL changes are made only in the core baseline going forward.
