# Migrating to Spring Boot 4

Polyflow's Spring Boot 4 line is built and tested with Spring Boot `4.0.8`, Java 17, and Camunda 7.24. The migration keeps the existing Camunda 7 Community Edition integration and preserves Polyflow's existing Jackson 2 JSON infrastructure.

## Breaking changes

- `spring.data.mongodb.uri` has moved to `spring.mongodb.uri` for applications using the MongoDB view.
- Spring Boot 4's Testcontainers 2 dependency management uses renamed artifacts. Consumer tests should replace the old `junit-jupiter`, `mongodb`, and `mariadb` Testcontainers artifact IDs with `testcontainers-junit-jupiter`, `testcontainers-mongodb`, and `testcontainers-mariadb` respectively.
- Custom JPA setup must be compatible with Jakarta Persistence 3 and Hibernate 7. Polyflow's DDL generation now uses current dialect names and Jakarta Persistence schema generation.

## Required consumer actions

1. Upgrade the application to Spring Boot 4 and use a Java version supported by Spring Boot 4. Polyflow itself continues to target Java 17.
2. If the application uses a Polyflow Camunda integration module directly, continue to provide the Camunda engine or Camunda Spring Boot starter. These dependencies remain Maven `provided` dependencies; they are not supplied transitively by those modules.
3. Continue to use Jackson 2 types (`com.fasterxml.jackson.*`) when interacting with Polyflow JSON APIs or when providing the qualified `payloadObjectMapper` bean.
4. Rename `spring.data.mongodb.uri` to `spring.mongodb.uri` in applications using the MongoDB view.
5. If the application maintains custom Testcontainers-based tests, use the Testcontainers 2 artifact names, such as `testcontainers-junit-jupiter`, `testcontainers-mongodb`, and `testcontainers-mariadb`.

## Camunda remains a provided dependency

The Camunda engine and Camunda Spring Boot starter remain `provided` in Polyflow's collector, job-sender, and engine-client modules. The application embedding those modules chooses and supplies its Camunda runtime.

Camunda Enterprise is **not** required merely because Polyflow declares Camunda as `provided`, nor because the application moves to Spring Boot 4. The repository's default build and the verified migration use Camunda 7 Community Edition. Use the `camunda-ee` build profile only when intentionally testing or building against Enterprise Edition with a valid license.

## Jackson 2 JSON support

Spring Boot 4 support preserves Polyflow's existing Jackson 2 contract for JSON extension points:

- Jackson core, databind, datatype, and module APIs used by Polyflow remain in `com.fasterxml.jackson.*`.
- Applications that inject or customize Polyflow's `payloadObjectMapper` should continue to provide a Jackson 2 `com.fasterxml.jackson.databind.ObjectMapper`.
- The Camunda collector/Spin path continues to use Jackson 2 as before.
