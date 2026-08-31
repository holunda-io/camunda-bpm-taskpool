# Migrate Polyflow-owned serialization to Jackson 3

## What to build

Move Polyflow-owned serialization code to Jackson 3 while preserving observable payload compatibility where required. The default serialization stack should use Jackson 3, with Jackson 2 kept only behind the Camunda compatibility lane.

## Acceptance criteria

- [ ] Polyflow-owned modules use Jackson 3 coordinates and packages for core, databind, datatype, and module APIs.
- [ ] Jackson annotations remain on their supported `com.fasterxml.jackson.annotation` package where appropriate.
- [ ] Core event, command, data-entry, task, and query serialization tests pass with Jackson 3.
- [ ] Date/time, property ordering, polymorphic type handling, and Kotlin module behavior are explicitly tested or intentionally adjusted.
- [ ] No non-Camunda production code imports Jackson 2 core/databind/datatype/module packages.

## Blocked by

- Issue 2: Create the Spring Boot 4 baseline with Camunda Enterprise artifacts
- Issue 3: Isolate the Camunda Jackson 2 lane
