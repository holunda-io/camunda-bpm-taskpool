# Preserve Jackson 2 support during Spring Boot 4 migration

## What to build

Keep Polyflow's existing Jackson 2 JSON infrastructure while migrating the build and Spring integration to Spring Boot 4. The completed slice should prove that Spring Boot 4 support preserves the existing Polyflow-facing JSON APIs.

## Acceptance criteria

- [x] Polyflow JSON APIs continue to use Jackson 2 packages and dependencies.
- [x] The qualified `payloadObjectMapper` contract remains `com.fasterxml.jackson.databind.ObjectMapper`.
- [x] The collector/Camunda Spin serialization path remains on Jackson 2.
- [x] No new JSON mapper package is introduced by the Spring Boot 4 migration.
- [x] Serialization tests cover representative existing Jackson 2 payloads.

## Blocked by

- Issue 1: Perform a minimal Spring Boot 4 migration pass
