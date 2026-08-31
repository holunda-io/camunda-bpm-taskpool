# Migrate Mongo view tests and properties to Boot 4

## What to build

Migrate the Mongo view to Spring Boot 4 property names and Testcontainers 2-compatible dependencies. Mongo repository and change-stream tests should demonstrate that reactive Mongo support still works on the Boot 4 line.

## Acceptance criteria

- [ ] Test configuration uses `spring.mongodb.uri` instead of the old `spring.data.mongodb.uri` key.
- [ ] Mongo Testcontainers dependencies use the Spring Boot 4 / Testcontainers 2-compatible module coordinates.
- [ ] MongoDB container usages compile against the Testcontainers 2 API.
- [ ] Mongo repository tests pass.
- [ ] Mongo event-handler and change-stream tracking tests pass.

## Blocked by

- Issue 2: Create the Spring Boot 4 baseline with Camunda Enterprise artifacts
