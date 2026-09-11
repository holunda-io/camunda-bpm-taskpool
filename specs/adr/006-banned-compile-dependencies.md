# ADR 004: No Spring Compile Dependencies in Published Libraries

- Name: ADR-004
- Status: Accepted
- Date: 2026-09-09

## Context

Polyflow libraries are consumed by Spring Boot applications. Declaring Spring Framework or Spring Boot dependencies with Maven's default `compile` scope makes
them transitive dependencies of consumers and can unintentionally constrain the application's dependency graph.

## Decision

The Maven Enforcer configuration rejects dependencies from `org.springframework` and `org.springframework.boot` with `compile` scope. Production code requiring
these APIs must declare them with `provided` scope. Test-scoped Spring dependencies remain permitted.

The rule is intentionally limited to Spring Framework and Spring Boot for now. Further standard libraries may be added as explicit rules when their
consumer-provided status is agreed.

## Consequences

- Consumers provide their own Spring Framework and Spring Boot dependencies.
- Published Polyflow POMs no longer re-export Spring compile dependencies.
- Tests can continue using Spring's test support without exceptions or workarounds.
