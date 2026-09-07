# Run final Spring Boot 4 verification and update docs

## What to build

Run the final validation for the compact Spring Boot 4 migration and update the migration/release documentation to match the verified path. The docs should state that Camunda remains provided, the collector stays on Jackson 2, and the rest of Polyflow defaults to Jackson 3.

## Acceptance criteria

- [ ] Full unit tests pass on the Spring Boot 4 line.
- [ ] Full integration tests pass on the Spring Boot 4 line, or any environment-specific exclusions are documented.
- [ ] Migration documentation no longer states that Camunda Enterprise is required merely for Polyflow's provided Camunda dependency.
- [ ] Migration documentation explains the Jackson 3 default and collector-scoped Jackson 2 compatibility lane.
- [ ] Release notes identify required consumer actions for Spring Boot 4 and Jackson behavior.

## Blocked by

- Issue 3: Fix observed Spring Boot 4 test and runtime breaks

