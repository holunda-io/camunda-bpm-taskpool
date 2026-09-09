This guide contains hints for upgrading to newer versions whenever there are breaking changes.

## Adopting an existing Polyflow schema

An installation that created Polyflow tables before adopting the
`polyflow-liquibase` artifact does not have a Liquibase history. Do not run the
baseline changelog as an update against that database: it would attempt to
create objects that already exist. Instead, establish the current schema as the
Liquibase baseline once, then let Liquibase apply all future changes.

Before adoption, take a database backup and verify that the existing schema
matches the Polyflow release currently used by the application. Table existence
alone is insufficient: columns, constraints, indexes, sequences, and views
must also match. Reconcile any differences before recording the baseline.

Use the same master changelog that the application will use afterwards:

- the central master for a monolith;
- `polyflow-core-changelog.xml` for the producer-side database; or
- `polyflow-view-changelog.xml` for the consumer-side database.

Run Liquibase's `changelog-sync` command with that master. It writes the
current changelog's changesets to `DATABASECHANGELOG` without executing their
DDL:

```bash
liquibase --changelog-file=db/changelog/db.changelog-master.xml changelog-sync
```

Then apply the release tag that corresponds to the adopted Polyflow schema.
Tags use the major and minor version, so an application at `4.6.3` is tagged
as `4.6`:

```bash
liquibase tag 4.6
```

Afterward, use the same master changelog for normal `update` executions.
Liquibase will skip the synchronized baseline and apply changes introduced by
later Polyflow releases. Do not use `changelog-sync` to bypass a pending
release migration; it is only for the initial adoption of a verified existing
schema.

### Application adoption mode

When a deployment cannot install the Liquibase CLI, the `polyflow-liquibase`
module can perform the same one-time operation during Spring Boot startup. Add
the following explicit setting to the deployment that uses the verified
schema:

```yaml
polyflow:
  liquibase:
    adoption:
      enabled: true
```

The application uses its normal datasource and configured Liquibase root changelog, runs
`changelog-sync`, tags the database with the module version's major and minor
components, and exits. For example, module version `4.6.3` creates tag `4.6`.
The mode permits existing application-specific changes in `DATABASECHANGELOG`;
it does not attempt to decide whether the schema is safe to adopt. An engineer
must verify it first. Remove the setting after the successful one-time run,
then use normal application startup for future migrations.

## Migrating to 4.x

Version 4.x upgrades the Spring Boot dependency from 2.x to 3.x, which also requires upgrading Camunda to >=7.20 and Axon to >=4.7. It also means that Hibernate 6 is used now,
which changes the way database sequences are created for sequence generators.

Axon uses sequence generators for its tables and thus if you come from an older Hibernate version, you probably have a sequence called `hibernate_sequence` in your database.
You can either create separate sequences per table and take care to have them start at the right value (recommended in
the [Axon migration guide](https://docs.axoniq.io/reference-guide/axon-framework/upgrading-to-4-7#step-3-1)) or set the JPA property `hibernate.id.db_structure_naming_strategy`
to `legacy` to restore the old behavior. In the Spring application properties, you would have to set this property:

```properties
spring.jpa.properties.hibernate.id.db_structure_naming_strategy=legacy
```
