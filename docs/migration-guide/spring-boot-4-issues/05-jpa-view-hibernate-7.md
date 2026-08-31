# Migrate JPA view and DDL generation to Boot 4 / Hibernate 7

## What to build

Migrate the JPA view and schema-generation path to the Hibernate version managed by Spring Boot 4. The JPA view should compile, repository tests should pass, and generated DDL should remain available or be replaced by a maintained equivalent.

## Acceptance criteria

- [ ] JPA view code compiles against Spring Boot 4 and Hibernate 7-managed APIs.
- [ ] Hibernate dialect and naming-strategy references are updated to supported classes.
- [ ] The DDL generation profile either works on Hibernate 7 or is replaced with a maintained schema-generation approach.
- [ ] Generated DDL for supported databases is reviewed for unexpected schema drift.
- [ ] JPA integration tests pass, including Testcontainers-backed database tests where applicable.

## Blocked by

- Issue 2: Create the Spring Boot 4 baseline with Camunda Enterprise artifacts
