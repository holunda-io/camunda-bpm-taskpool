## Persistence

Polyflow ships the database objects required by the Axon Framework core and
the relational views in the `polyflow-liquibase` artifact. The artifact
supports H2, MariaDB, PostgreSQL, Oracle, Microsoft SQL Server, and Azure SQL.
Manage these objects through Liquibase; do not generate Polyflow DDL through
Hibernate or Maven.

Add the Liquibase artifact alongside the Polyflow modules used by your
application:

```xml

<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-liquibase</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

## Service-owned master changelog

Each service that owns a database keeps a master changelog, for example
`src/main/resources/db/changelog/db.changelog-master.xml`. The service includes
the required Polyflow changelogs from that master alongside its own changesets.
This makes the service master the single Liquibase entry point. It also means
that the service configuration stays the same across topologies: only the
contents of its master changelog change.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd">

  <!-- Producer: engine, aggregate persistence, and event processing. -->
  <include file="classpath:db/changelog/polyflow/polyflow-core-changelog.xml"/>

  <!-- Consumer: JPA projections and event processing. -->
  <!-- <include file="classpath:db/changelog/polyflow/polyflow-view-changelog.xml"/> -->

  <!-- Include the application's own changelogs here. -->
</databaseChangeLog>
```

There are two deployment changelogs:

- `polyflow-core-changelog.xml` is for the producer side: the engine,
  aggregate event and snapshot storage (`DOMAIN_EVENT_ENTRY` and
  `SNAPSHOT_EVENT_ENTRY`), and event-processing storage (`TOKEN_ENTRY` and
  `DEAD_LETTER_ENTRY`).
- `polyflow-view-changelog.xml` is for the consumer side: JPA view objects and
  the same event-processing storage. It does not provision aggregate or saga
  storage.

### Service topology configuration

Choose the includes according to the service's responsibility, not according
to the database product:

| Service deployment                                                     | Include in the service master changelog |
|------------------------------------------------------------------------|-----------------------------------------|
| Monolith: engine, core, event processing, and JPA views in one service | Core and view masters                   |
| Producer: engine, core aggregate model, and event processing           | Core master                             |
| Consumer: JPA views and event processing                               | View master                             |
| Central platform service that hosts both core and views                | Core and view masters                   |

For a monolith or central platform service, include both:

```xml

<include file="classpath:db/changelog/polyflow/polyflow-core-changelog.xml"/>
<include file="classpath:db/changelog/polyflow/polyflow-view-changelog.xml"/>
```

For separated services, the producer includes only the core master:

```xml

<include file="classpath:db/changelog/polyflow/polyflow-core-changelog.xml"/>
```

The consumer includes only the view master:

```xml

<include file="classpath:db/changelog/polyflow/polyflow-view-changelog.xml"/>
```

The common event-processing changeset has one logical Liquibase path, so
including both masters in one service applies it once. In a separated
deployment, each service applies event-processing storage to its own database.
Saga storage is intentionally not part of either deployment master; the
vendor-specific `axonframework-4-saga.sql` baseline remains available for
services that explicitly need it.

Configure the application to run this central master changelog. For Spring
Boot, each service uses the same configuration and leaves schema creation to
Liquibase:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
  jpa:
    hibernate:
      ddl-auto: validate
```

Schema changes shipped by a future Polyflow version are applied by upgrading
the `polyflow-liquibase` dependency and running the same central master
changelog.

If the database already contains Polyflow tables but has no Liquibase history,
adopt the verified existing schema before running updates. Follow
[Adopting an existing Polyflow schema](../../migration-guide/index.md#adopting-an-existing-polyflow-schema)
for the one-time `changelog-sync` and release-tag procedure.

### Application adoption mode

The Liquibase module provides a one-shot Spring Boot adoption mode for
deployments that cannot use the Liquibase CLI (see official docs of Liquibse).
It is explicitly enabled and uses the application's normal datasource and root changelog:

```yaml
polyflow:
  liquibase:
    adoption:
      enabled: true
```

The mode synchronizes the verified existing schema, applies the release tag,
and exits without starting the application. The tag is derived from the
packaged `polyflow-liquibase` build version: `4.7.1-SNAPSHOT` produces tag
`4.7`. It preserves unrelated application Liquibase history and does not try to
infer whether arbitrary existing tables are equivalent to the Polyflow schema.
Enable it only for the one-time adoption of a schema that an engineer has
already verified.

## Release tags

Each deployment master records its completed Polyflow release with Liquibase's
`tagDatabase` change. Baseline changeset IDs and tags use the major and minor
version only: `4.6`, not `4.6.2`. Polyflow follows semantic versioning, so a
persistence-structure change is released in a new minor version; patch
versions do not introduce a new schema version or release tag.

In a monolith, both masters record the same `4.6` tag. In separated
deployments, the producer and consumer databases each record that tag after
their respective baseline has completed.

The Liquibase integration build verifies that this tag matches the major and
minor components of the Maven release version. For example,
`4.7.1-SNAPSHOT` requires tag `4.7`; a mismatch fails the build. Major-version
tag changes are made deliberately as part of the release work.

## Schema object naming

Polyflow assigns explicit names to database objects so that database
diagnostics and administration output remain readable across supported
database products:

- Primary keys: `PK_<OWNING_TABLE>`
- Foreign keys: `FK_<OWNING_TABLE>_<REFERENCED_TABLE>`
- Unique constraints: `UK_<OWNING_TABLE>_<CONSTRAINED_COLUMNS>`
- Indexes: `IDX_<OWNING_TABLE>_<INDEXED_COLUMNS>`

Use concise, unambiguous abbreviations where needed to keep names within
Oracle's 30-character identifier limit.

Write schema-object and column names in `CAPITAL_CASE`. Write SQL keywords and
data types in lowercase, for example `create table PLF_TASK (...)`.

Baseline SQL declares columns and constraints in each `create table` statement
whenever the referenced object is already available. `alter table` is reserved
for later schema evolution that cannot be represented during initial object
creation.

View definitions enumerate their selected columns explicitly, including every
branch of a `union`; they do not use `select *`.
