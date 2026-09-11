# Migrating to Spring Boot 4

Polyflow's Spring Boot 4 line is built and tested with Spring Boot `4.1.1`, Java 17, and Camunda 7.24. The migration keeps the existing Camunda 7 Community Edition integration and preserves Polyflow's existing Jackson 2 JSON infrastructure.

## Required consumer actions

1. Upgrade the application to Spring Boot 4 and use a Java version supported by Spring Boot 4. Polyflow itself continues to target Java 17.
2. If the application uses a Polyflow Camunda integration module directly, continue to provide the Camunda engine or Camunda Spring Boot starter. These dependencies remain Maven `provided` dependencies; they are not supplied transitively by those modules.
3. Continue to use Jackson 2 types (`com.fasterxml.jackson.*`) when interacting with Polyflow JSON APIs or when providing the qualified `payloadObjectMapper` bean.
4. Rename `spring.data.mongodb.uri` to `spring.mongodb.uri` in applications using the MongoDB view.
5. If the application maintains custom Testcontainers-based tests, use the Testcontainers 2 artifact names, such as `testcontainers-junit-jupiter`, `testcontainers-mongodb`, and `testcontainers-mariadb`.

See the [Spring Boot 4 release notes](spring-boot-4-release-notes.md) for a compact checklist of the release-level changes.

## Camunda remains a provided dependency

The Camunda engine and Camunda Spring Boot starter remain `provided` in Polyflow's collector, job-sender, and engine-client modules. The application embedding those modules chooses and supplies its Camunda runtime.

Camunda Enterprise is **not** required merely because Polyflow declares Camunda as `provided`, nor because the application moves to Spring Boot 4. The repository's default build and the verified migration use Camunda 7 Community Edition. Use the `camunda-ee` build profile only when intentionally testing or building against Enterprise Edition with a valid license.

## Jackson 2 JSON support

Spring Boot 4 support preserves Polyflow's existing Jackson 2 contract for JSON extension points:

- Jackson core, databind, datatype, and module APIs used by Polyflow remain in `com.fasterxml.jackson.*`.
- Applications that inject or customize Polyflow's `payloadObjectMapper` should continue to provide a Jackson 2 `com.fasterxml.jackson.databind.ObjectMapper`.
- The Camunda collector/Spin path continues to use Jackson 2 as before.

## Persistence and test infrastructure changes

Spring Boot 4 manages Hibernate 7 and Testcontainers 2. The Polyflow JPA DDL generator no longer uses the old Hibernate Maven plugin. It now exercises Jakarta Persistence schema generation through a focused test and writes these files to `view/jpa/target`:

- `h2_ddl.sql`
- `mssql_ddl.sql`
- `pgsql_ddl.sql`

Generate them with:

```bash
./mvnw -Pgenerate-sql -f view/jpa
```

The persistence descriptor uses the Jakarta Persistence 3 namespace and current Hibernate dialect names. Consumers with copied or custom persistence descriptors should make the equivalent updates.

## Verification

The complete 32-module reactor was verified on macOS with Java 17.0.18 and Maven 3.9.2:

```bash
./mvnw test
./mvnw -Pitest verify
```

Both commands pass. The integration run includes the Camunda/H2 scenarios and the Testcontainers-backed MariaDB and MongoDB suites; no environment-specific exclusions were needed. A working Docker-compatible container runtime is required for the container-backed suites.
