# Spring Boot 4 Release Notes

This release moves Polyflow's managed platform to Spring Boot `4.1.1` while retaining Java 17 as Polyflow's compilation target and Camunda `7.24.0` as the verified Camunda line.

## Breaking changes

- `spring.data.mongodb.uri` has moved to `spring.mongodb.uri` for applications using the MongoDB view.
- Spring Boot 4's Testcontainers 2 dependency management uses renamed artifacts. Consumer tests should replace the old `junit-jupiter`, `mongodb`, and `mariadb` Testcontainers artifact IDs with `testcontainers-junit-jupiter`, `testcontainers-mongodb`, and `testcontainers-mariadb` respectively.
- Custom JPA setup must be compatible with Jakarta Persistence 3 and Hibernate 7. Polyflow's DDL generation now uses current dialect names and Jakarta Persistence schema generation.

## Camunda and Jackson compatibility

- Camunda remains a consumer-provided dependency in the Polyflow collector, job-sender, and engine-client modules. The migration does not require Camunda Enterprise; the verified default remains Camunda Community Edition.
- Polyflow remains on Jackson 2 for this Spring Boot 4 migration. Consumers that call Polyflow JSON extension points or provide the qualified `payloadObjectMapper` bean should continue to use Jackson 2 mapper types from `com.fasterxml.jackson.*`.

## Upgrade checklist

- Upgrade the application to Spring Boot 4.
- Continue to provide the chosen Camunda runtime when using Polyflow's direct Camunda integration modules.
- Keep Polyflow-facing `ObjectMapper` and related imports on Jackson 2.
- Rename the MongoDB URI property and update Testcontainers artifact coordinates where applicable.
- Review custom JPA persistence descriptors, dialect names, and DDL generation.
- Run the application's unit and integration suites with its supported databases and messaging infrastructure.

For details and examples, see [Migrating to Spring Boot 4](spring-boot-4.md).
