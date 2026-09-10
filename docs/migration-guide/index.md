This guide contains hints for upgrading to newer versions whenever there are breaking changes.

## Adopting an existing Polyflow schema

An installation that created Polyflow tables before adopting the
`polyflow-liquibase` artifact does not yet have Polyflow Liquibase history. It
may already have a `DATABASECHANGELOG` containing application-specific
changesets. Do not run the baseline changelog as an update against that
database: it would attempt to create objects that already exist. Instead,
establish the current schema as the Liquibase baseline once, then let Liquibase
apply all future changes.

Before adoption, take a database backup and verify that the existing schema
matches the Polyflow release currently used by the application. Table existence
alone is insufficient: columns, constraints, indexes, sequences, and views
must also match. Reconcile any differences before recording the baseline.

Use the same service-owned master changelog that the application will use
afterwards. Its includes depend on the service topology:

- the central master for a monolith;
- `polyflow-core-changelog.xml` for the producer-side database; or
- `polyflow-view-changelog.xml` for the consumer-side database.

### Standard Liquibase approach

Liquibase provides functionality of ChangeLog Sync which can be used to onboard. More details can be found
at [Liquibase Reference Documentation Site](https://docs.liquibase.com/pro/reference-guide-4-33/database-inspection-change-tracking-and-utility-commands/changelog-sync)

### Spring Boot adoption mode

The supported one-time adoption procedure is provided by the
`polyflow-liquibase` Spring Boot auto-configuration. Configure the datasource
and the service's normal master changelog as for a regular deployment.

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
  jpa:
    hibernate:
      ddl-auto: validate
```

Then enable adoption explicitly for a single startup (for example, using a dedicated config map):

```yaml
polyflow:
  liquibase:
    adoption:
      enabled: true
```

Alternatively, you might want to set it as environment variables:

``` 
POLYFLOW_LIQUIBASE_ADOPTION_ENABLED=true
```

When the property is enabled, the auto-configuration disables Spring Boot's
normal Liquibase update for that startup. It uses the configured datasource and
root changelog to run `changelog-sync`, records the Polyflow module version's
major and minor release tag, and closes the application context. For example,
module version `4.6.3` records tag `4.6`.

This operation preserves existing application-specific
`DATABASECHANGELOG` entries. It does not compare the existing schema with the
changelog: an engineer must verify the schema before enabling the property.
Remove the property after the successful one-time startup. Future normal
application starts use the same master changelog and apply only migrations
introduced after the adopted baseline.

Do not enable adoption to bypass a pending release migration; it is only for
the initial adoption of a verified existing schema.

## Migrating to 4.x

Version 4.x upgrades the Spring Boot dependency from 2.x to 3.x, which also requires upgrading Camunda to >=7.20 and Axon to >=4.7. It also means that Hibernate
6 is used now,
which changes the way database sequences are created for sequence generators.

Axon uses sequence generators for its tables and thus if you come from an older Hibernate version, you probably have a sequence called `hibernate_sequence` in
your database.
You can either create separate sequences per table and take care to have them start at the right value (recommended in
the [Axon migration guide](https://docs.axoniq.io/reference-guide/axon-framework/upgrading-to-4-7#step-3-1)) or set the JPA property
`hibernate.id.db_structure_naming_strategy`
to `legacy` to restore the old behavior. In the Spring application properties, you would have to set this property:

```properties
spring.jpa.properties.hibernate.id.db_structure_naming_strategy=legacy
```
