# Isolate the Camunda Jackson 2 lane

## What to build

Define and enforce the Jackson 2 compatibility lane for Camunda-bound code while keeping the application default JSON stack ready for Jackson 3. Camunda Spin, REST, and job serialization should continue to use Jackson 2 without leaking Jackson 2 concrete types across Polyflow-owned module boundaries.

## Acceptance criteria

- [ ] Camunda-bound modules that require Jackson 2 are explicitly identified and documented.
- [ ] Jackson 2 dependencies and `spring-boot-jackson2` are scoped to Camunda-bound modules only.
- [ ] No Camunda-bound module exports Jackson 2 `ObjectMapper`, `JsonNode`, `TypeReference`, serializer, deserializer, or module types as general application API.
- [ ] Boundaries between Jackson 2 and Jackson 3 lanes use strings, byte arrays, maps, or domain DTOs.
- [ ] Camunda Spin and job serialization paths have tests that prove existing payloads can still be read.

## Blocked by

- Issue 1: Prepare Camunda Enterprise access for Boot 4 artifacts
- Issue 2: Create the Spring Boot 4 baseline with Camunda Enterprise artifacts
