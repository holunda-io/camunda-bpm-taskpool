# ADR 008: MariaDB Physical Object Naming

- Name: ADR-008
- Status: Accepted
- Date: 2026-09-25

## Context

MariaDB on Linux can use case-sensitive table names. Hibernate's physical
naming strategy uses lowercase snake-case names, while Polyflow's MariaDB
Liquibase baselines created physical table and view names in uppercase.
Hibernate schema validation then could not find the mapped tables.

## Decision

MariaDB baselines use lowercase names for every Hibernate-managed table, view,
and sequence, including all references in foreign keys, indexes, and views.
This applies to core event storage, event-processing, JPA view projections,
and optional saga persistence. Axon's LOB columns use `blob`, which Hibernate
validates as `Types.BLOB`, rather than `longblob`, which MariaDB reports as
`Types.LONGVARBINARY`. The JPA view's `@Lob String` payload columns use
`tinytext`, which Hibernate validates as `Types.CLOB`, rather than `longtext`,
which MariaDB reports as `Types.LONGVARCHAR`. Column and constraint identifiers
retain the existing SQL naming convention.

## Consequences

Fresh MariaDB databases provisioned by Liquibase match Axon's JPA mappings on
case-sensitive hosts and pass Hibernate schema validation. This changes only
baselines; existing databases retain their established physical names and
require no migration from this change.
