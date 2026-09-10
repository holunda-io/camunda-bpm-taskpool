# AGENTS.md

## 1. Overview

Polyflow Taskpool is a modular library for building process platforms with task and business-data pools. It provides core domain capabilities, integrations, and
read-model views.

## 2. Folder Structure

- `bom`: parent and dependency-management POMs for published modules.
- `core`:
  - `taskpool`: task-pool API, events, and domain implementation.
  - `datapool`: data-pool API, events, and domain implementation.
  - `bus-jackson`, `spring-utils`: shared infrastructure modules.
- `integration`:
  - `camunda-bpm`: Camunda engine integrations, collectors, and Spring Boot modules.
  - `common`: reusable senders, serializers, and support integrations.
- `view`: query APIs and simple, JPA, MongoDB, and URL-resolver view implementations.
- `docs`: published documentation; user-facing feature documentation normally belongs in `docs/reference-guide`.
- `specs/adr`: authoritative architecture decision records; use the next numbered `XXX-short-title.md` file.
- `site`: generated documentation output; do not edit it as source content.

## 3. Core Behaviors & Patterns

- Maven modules are implemented primarily in Kotlin, with public APIs separated from core, integration, and view implementations.
- Components use Spring configuration and property classes for optional integration setup.
- Kotlin Logging is used for operational logging; messages commonly carry a stable component identifier.
- Tests are colocated in each module under `src/test` and mirror production packages.

## 4. Conventions

- Use Kotlin naming conventions: PascalCase types, camelCase members, and descriptive package names under `io.holunda`.
- Keep public API modules independent of concrete view and integration implementations.
- Keep documentation in Markdown and add published pages to `mkdocs.yml` navigation when they should be reachable in the site.
- Follow [ADR-000](specs/adr/000-adr-conventions.md) for ADR format and numbering.
- In Liquibase SQL, explicitly name primary keys, foreign keys, unique constraints, and indexes with `PK_`, `FK_`, `UK_`, and `IDX_` prefixes. Derive the remaining name from the owning table and referenced object or constrained/indexed columns; use concise names that fit Oracle's 30-character identifier limit.
- In a Liquibase baseline, define columns and constraints in `CREATE TABLE` whenever the referenced table has already been created. Reserve `ALTER TABLE` for schema evolution that cannot be expressed during initial object creation.
- In Liquibase SQL, write schema-object and column names in `CAPITAL_CASE`; write SQL keywords and data types in lowercase.
- In Liquibase view definitions, enumerate every selected column; do not use `SELECT *`, including within `UNION` branches.
- Keep the two Liquibase deployment masters aligned with the runtime sides: `core` is the producer side (aggregate plus event processing) and `view` is the consumer side (projections plus event processing). Saga baseline SQL remains optional and is not included by either master. Shared event-processing changesets must use one logical Liquibase path so a monolith applies them once.

## 5. Working Agreements

- Respond in English by default; keep technical terms in English and never translate code blocks.
- Before editing code, review related usages, flows, and recurring patterns; prefer minimal, simple changes without speculative compatibility paths.
- Every feature or change requires an ADR in `specs/adr` and user-facing documentation in `docs`, normally under `docs/reference-guide`; document presence,
  usage, and configuration.
- For a request introduced as `New feature`, clarify the scope until it is understood, create the ADR and user-facing documentation, then wait for an explicit
  implementation request. Do not implement during the planning phase.
- Create tests or run lint/format tasks only when explicitly requested. Do not add tests for guarantees already provided by the type system.
- Public Kotlin classes, objects, companion objects, and functions require KDoc. Before committing Kotlin changes, run Detekt for the changed module: `./mvnw -P detekt -pl <module-path> antrun:run@detekt`. The command applies the repository's `detekt.yml` and fails on documentation-rule violations.
