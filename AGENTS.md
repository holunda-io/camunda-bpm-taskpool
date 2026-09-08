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

## 5. Working Agreements

- Respond in English by default; keep technical terms in English and never translate code blocks.
- Before editing code, review related usages, flows, and recurring patterns; prefer minimal, simple changes without speculative compatibility paths.
- Every feature or change requires an ADR in `specs/adr` and user-facing documentation in `docs`, normally under `docs/reference-guide`; document presence,
  usage, and configuration.
- For a request introduced as `New feature`, clarify the scope until it is understood, create the ADR and user-facing documentation, then wait for an explicit
  implementation request. Do not implement during the planning phase.
- Create tests or run lint/format tasks only when explicitly requested. Do not add tests for guarantees already provided by the type system.
