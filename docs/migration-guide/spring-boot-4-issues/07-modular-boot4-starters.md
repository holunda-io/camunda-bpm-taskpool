# Replace temporary classic starters with modular Boot 4 starters

## What to build

Remove temporary classic starter bridges and declare the specific Spring Boot 4 starter and test-starter dependencies each module needs. The dependency graph should be explicit, modular, and free of broad classic starter fallbacks.

## Acceptance criteria

- [ ] No `spring-boot-starter-classic` dependency remains.
- [ ] No `spring-boot-starter-test-classic` dependency remains.
- [ ] Modules using JPA, MongoDB, Reactor, web MVC, validation, or other Boot technologies declare the matching Boot 4 modular starters.
- [ ] Tests declare matching modular test starters where Boot 4 test auto-configuration requires them.
- [ ] The full reactor compiles without relying on classic starter bridges.

## Blocked by

- Issue 3: Isolate the Camunda Jackson 2 lane
- Issue 4: Migrate Polyflow-owned serialization to Jackson 3
- Issue 5: Migrate JPA view and DDL generation to Boot 4 / Hibernate 7
- Issue 6: Migrate Mongo view tests and properties to Boot 4

