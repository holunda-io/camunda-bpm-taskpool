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

## Central master changelog

Keep a master changelog in the application that owns the database, for
example `src/main/resources/db/changelog/db.changelog-master.xml`. Include
the Polyflow masters from that file, together with the application's own
changesets. This makes the application master the single Liquibase entry
point.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd">

  <!-- Include when the application uses Polyflow core with a JPA/JDBC event store. -->
  <include file="classpath:db/changelog/polyflow/polyflow-core-changelog.xml"/>

  <!-- Alternatively, include this when the application uses the Polyflow JPA
       View. It includes the required core changelog itself. -->
  <!-- <include file="classpath:db/changelog/polyflow/polyflow-view-changelog.xml"/> -->

  <!-- Include the application's own changelogs here. -->
</databaseChangeLog>
```

Include `polyflow-core-changelog.xml` for a core-only application. Include
`polyflow-view-changelog.xml` for an application that uses the JPA View; it
includes the core changelog and provisions both sets of objects. Do not include
both in a new master changelog.

Configure the application to run this central master changelog. For Spring
Boot, use the following configuration and leave schema creation to Liquibase:

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

Baseline SQL declares columns and constraints in each `CREATE TABLE` statement
whenever the referenced object is already available. `ALTER TABLE` is reserved
for later schema evolution that cannot be represented during initial object
creation.

View definitions enumerate their selected columns explicitly, including every
branch of a `UNION`; they do not use `select *`.
