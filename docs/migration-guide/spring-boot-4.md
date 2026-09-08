# Migrating to Spring Boot 4

Polyflow's Spring Boot 4 line is built and tested with Spring Boot `4.1.1`, Java 17, and Camunda 7.24. The migration keeps the existing Camunda 7 Community Edition integration and changes Polyflow-owned JSON infrastructure to Jackson 3.

## Required consumer actions

1. Upgrade the application to Spring Boot 4 and use a Java version supported by Spring Boot 4. Polyflow itself continues to target Java 17.
2. If the application uses a Polyflow Camunda integration module directly, continue to provide the Camunda engine or Camunda Spring Boot starter. These dependencies remain Maven `provided` dependencies; they are not supplied transitively by those modules.
3. Migrate application code that interacts with Polyflow's JSON APIs from Jackson 2 core/databind/module types (`com.fasterxml.jackson.*`) to Jackson 3 types (`tools.jackson.*`). Jackson annotations remain in `com.fasterxml.jackson.annotation`.
4. Keep Jackson 2 only around the Camunda collector/Spin integration. Do not expose its `ObjectMapper`, `JsonNode`, modules, serializers, or other concrete Jackson 2 types to Polyflow's Jackson 3 lane.
5. Rename `spring.data.mongodb.uri` to `spring.mongodb.uri` in applications using the MongoDB view.
6. If the application maintains custom Testcontainers-based tests, use the Testcontainers 2 artifact names, such as `testcontainers-junit-jupiter`, `testcontainers-mongodb`, and `testcontainers-mariadb`.

See the [Spring Boot 4 release notes](spring-boot-4-release-notes.md) for a compact checklist of the release-level changes.

## Camunda remains a provided dependency

The Camunda engine and Camunda Spring Boot starter remain `provided` in Polyflow's collector, job-sender, and engine-client modules. The application embedding those modules chooses and supplies its Camunda runtime.

Camunda Enterprise is **not** required merely because Polyflow declares Camunda as `provided`, nor because the application moves to Spring Boot 4. The repository's default build and the verified migration use Camunda 7 Community Edition. Use the `camunda-ee` build profile only when intentionally testing or building against Enterprise Edition with a valid license.

## Jackson 3 default and Jackson 2 compatibility lane

Jackson 2 and Jackson 3 can coexist because their core packages differ:

- Jackson 3 core, databind, datatype, and module APIs use `tools.jackson.*`.
- Jackson annotations continue to use `com.fasterxml.jackson.annotation`.
- Jackson 2 core, databind, datatype, and module APIs use `com.fasterxml.jackson.*`.

Polyflow uses the following split:

| Lane | Owner and purpose | Rule |
|---|---|---|
| Jackson 3 (default) | Polyflow bus serialization, senders, variable serialization, JPA JSON conversion, and form URL resolution | Use Jackson 3 dependencies and `tools.jackson.*` mapper APIs. |
| Jackson 2 (compatibility) | Camunda collector/Spin serialization and legacy-payload compatibility tests | Keep this scoped to the Camunda boundary. |

Pass strings, byte arrays, maps, or domain DTOs across the boundary. Do not make a Jackson 2 `ObjectMapper` the default application mapper and do not pass concrete mapper types between the lanes.

Applications that inject or customize Polyflow's `payloadObjectMapper` must now provide a Jackson 3 `tools.jackson.databind.ObjectMapper`. A Jackson 2 mapper used by Camunda Spin is a separate object and is not a substitute.

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
