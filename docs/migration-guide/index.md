This guide contains hints for upgrading to newer versions whenever there are breaking changes.

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
