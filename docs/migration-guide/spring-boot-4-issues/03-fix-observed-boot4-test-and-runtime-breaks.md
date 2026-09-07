# Fix observed Spring Boot 4 test and runtime breaks

## What to build

Fix the small set of Spring Boot 4 breaks discovered after the all-at-once migration pass. This issue is intentionally evidence-driven: update properties, Testcontainers coordinates, JPA/Hibernate DDL generation, starter declarations, or test infrastructure only where compile or focused tests show a real incompatibility.

## Acceptance criteria

- [ ] Boot 4 property renames discovered by tests are fixed.
- [ ] JPA/Hibernate compile or DDL generation breaks are fixed if they occur.
- [ ] MongoDB and Testcontainers test breaks are fixed if they occur.
- [ ] Starter modularization changes are made only where Boot 4 requires them or where they remove confirmed deprecation/runtime issues.
- [ ] Focused tests for changed modules pass.

## Blocked by

- Issue 1: Perform a minimal Spring Boot 4 migration pass
- Issue 2: Default to Jackson 3 while keeping the collector on Jackson 2

