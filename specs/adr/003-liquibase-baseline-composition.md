# ADR 003: Liquibase Baseline Composition

- Name: ADR-003
- Status: Accepted
- Date: 2026-09-09

## Context

The initial Liquibase delivery grouped unrelated Axon Framework persistence
concerns in one core baseline. Applications can use aggregate persistence,
saga persistence, and event processing independently. A JPA View is an event
processor and needs token and dead-letter storage, but does not require
aggregate or saga persistence when it uses in-memory event and saga stores.

## Decision

There are two deployment masters. `polyflow-core-changelog.xml` is the producer
master and provides aggregate (`DOMAIN_EVENT_ENTRY`, `SNAPSHOT_EVENT_ENTRY`)
and event-processing (`TOKEN_ENTRY`, `DEAD_LETTER_ENTRY`) storage.
`polyflow-view-changelog.xml` is the consumer master and provides JPA projection
objects plus the same event-processing storage. It does not provision
aggregate or saga storage.

Both masters define the event-processing changeset with the same logical
Liquibase path. A monolith can include both masters and Liquibase applies that
changeset once; separated producer and consumer deployments each provision the
event-processing storage they require. Saga SQL remains a separate optional
baseline file, without a third deployment master.

Each deployment master finishes with a Liquibase `tagDatabase` change. Release
tags and baseline changeset IDs use the semantic-version major and minor
components only (for example `4.6`). A persistence change requires a minor
release; patch releases do not introduce a new schema tag or baseline ID.

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
through Spring Boot integration tests. The view suite verifies its transitive
event-processing tables as well as the projection tables. PostgreSQL, MariaDB,
and Microsoft SQL Server use Testcontainers; H2 runs in embedded
PostgreSQL-compatibility mode because Testcontainers provides no H2 database
module. Shared abstract core and view suites keep the provisioning assertions
consistent across these dialects.

## Consequences

- Fresh JPA View installations provision event-processing and view objects,
  but no aggregate or saga objects.
- A monolith includes both masters without replaying event-processing DDL.
- Saga storage is never created by the deployment masters.
- Future Axon DDL changes are made in the baseline for the corresponding
  capability.
