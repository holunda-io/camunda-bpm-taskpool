# Run final Spring Boot 4 verification and update docs

## What to build

Run the final validation for the compact Spring Boot 4 migration and update the migration/release documentation to match the verified path. The docs should state that Camunda remains provided, the collector stays on Jackson 2, and the rest of Polyflow defaults to Jackson 3.

## Acceptance criteria

- [x] Full unit tests pass on the Spring Boot 4 line.
- [x] Full integration tests pass on the Spring Boot 4 line, or any environment-specific exclusions are documented.
- [x] Migration documentation no longer states that Camunda Enterprise is required merely for Polyflow's provided Camunda dependency.
- [x] Migration documentation explains the Jackson 3 default and collector-scoped Jackson 2 compatibility lane.
- [x] Release notes identify required consumer actions for Spring Boot 4 and Jackson behavior.

## Verification evidence

- `./mvnw test`: passed for all 32 reactor modules with Java 17.0.18 and Maven 3.9.2.
- `./mvnw -Pitest verify`: passed for all 32 reactor modules, including Camunda/H2 integration tests and Testcontainers-backed MariaDB and MongoDB tests. No environment-specific exclusions were required; Docker was available for the container-backed suites.
- `./mvnw -Pgenerate-sql -f view/jpa`: passed and generated non-empty H2, SQL Server, and PostgreSQL DDL files.
- `mkdocs build`: passed. The migration guide and release notes are included in the documentation navigation.

## Blocked by

- Issue 3: Fix observed Spring Boot 4 test and runtime breaks
