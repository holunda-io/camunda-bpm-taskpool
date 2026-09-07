# Default to Jackson 3 while keeping the collector on Jackson 2

## What to build

Make Jackson 3 the default for Polyflow-owned serialization while keeping Jackson 2 in the Camunda collector path where Camunda Spin compatibility requires it. The completed slice should prove that Jackson 2 and 3 can coexist without leaking mapper-specific types across module boundaries.

## Acceptance criteria

- [ ] Non-Camunda Polyflow-owned serialization uses Jackson 3 packages and dependencies where migration is required.
- [ ] The collector/Camunda Spin serialization path remains on Jackson 2.
- [ ] Jackson 2 dependencies are not promoted to a global default for all modules.
- [ ] Jackson concrete types are not passed across the Jackson 2/3 boundary as public API.
- [ ] Serialization tests cover representative existing payloads for both the collector lane and the Polyflow-owned Jackson 3 lane.

## Blocked by

- Issue 1: Perform a minimal Spring Boot 4 migration pass

