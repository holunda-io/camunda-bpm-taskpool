## Persistence

Polyflow ships the PostgreSQL database objects required by the Axon Framework
core and the relational views in the `polyflow-liquibase` artifact. Manage
these objects through Liquibase; do not generate Polyflow DDL through
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

  <!-- Include when the application uses the Polyflow JPA View. -->
  <include file="classpath:db/changelog/polyflow/polyflow-view-changelog.xml"/>

  <!-- Include the application's own changelogs here. -->
</databaseChangeLog>
```

Include only the Polyflow masters required by the modules in use. The masters
can safely be included together for an application that uses both the core and
the JPA View.

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

Polyflow currently supplies PostgreSQL changelogs. Schema changes shipped by a
future Polyflow version are applied by upgrading the `polyflow-liquibase`
dependency and running the same central master changelog.
