# AGENTS.md

## 1. Overview

Polyflow is a modular Kotlin library for building process platforms that collect process-engine events into task and business-data streams, then expose
read-optimized views. It supports a process application/process platform architecture, CQRS, and multiple deployment and persistence options.

## 2. Folder Structure

- `pom.xml` (`polyflow-root`): root Maven reactor, shared Java/Kotlin versions, build profiles, and release configuration.
- `bom/`
  - `parent/` (`polyflow-parent`): common parent POM; owns dependency/plugin management and composes the full reactor.
  - `datapool-dependencies/` (`polyflow-datapool-dependencies`): importable BOM for the Datapool API, collector, events, and core.
  - `taskpool-dependencies/` (`polyflow-taskpool-dependencies`): importable BOM for the full Taskpool stack, including Datapool, integrations, and views.
- `core/`: process-platform domain components.
  - `taskpool/` (`polyflow-taskpool-parent`): aggregator and dependency-management POM for Taskpool modules.
    - `taskpool-api/` (`polyflow-taskpool-api`): public task, process, and variable commands, source references, and sender-facing contracts.
    - `taskpool-event/` (`polyflow-taskpool-event`): versioned Taskpool domain events and event upcasters for serialized-event evolution.
    - `taskpool-core/` (`polyflow-taskpool-core`): Axon command handlers and event-sourced aggregates that maintain Taskpool state.
  - `datapool/` (`polyflow-datapool-parent`): aggregator and dependency-management POM for Datapool modules.
    - `datapool-api/` (`polyflow-datapool-api`): public business-data identities, commands, processing types, and shared value types.
    - `datapool-event/` (`polyflow-datapool-event`): Datapool domain events and mapping helpers.
    - `datapool-core/` (`polyflow-datapool-core`): Axon command handlers, event-sourced data-entry aggregates, and aggregate repository strategies.
  - `bus-jackson/` (`polyflow-bus-jackson`): Jackson modules, serializers, deserializers, and Spring configuration for Polyflow messages and queries on Axon
    buses.
  - `spring-utils/` (`polyflow-spring-utils`): reusable Spring infrastructure, including application-name processing.
- `integration/`: components intended for process-application deployments.
  - `common/` (`polyflow-integration-common-parent`): aggregator and dependency-management POM for engine-independent integration components.
    - `datapool-sender/` (`polyflow-datapool-sender`): sends business-data commands to the Datapool, with configurable payload serialization.
    - `tasklist-url-resolver/` (`polyflow-tasklist-url-resolver`): resolves a task-list URL for external consumers; it is not wired into other Polyflow
      components.
    - `taskpool-sender/` (`polyflow-taskpool-sender`): batches, serializes, and sends Taskpool commands, including transactional and job-based delivery options.
    - `variable-serializer/` (`polyflow-variable-serializer`): serializes and filters process-variable values used as command payloads.
  - `camunda-bpm/` (`polyflow-integration-camunda-bpm-engine-parent`): aggregator and dependency-management POM for Camunda Platform 7 adapters.
    - `engine-client/` (`polyflow-camunda-bpm-engine-client`): applies supported interaction events to a Camunda BPM engine.
    - `taskpool-collector/` (`polyflow-camunda-bpm-taskpool-collector`): Camunda engine plugin and collector that turns engine changes into Taskpool commands,
      with enrichment/correlation support.
    - `taskpool-job-sender/` (`polyflow-camunda-bpm-taskpool-job-sender`): Camunda job handler for deferred, transaction-safe Taskpool command delivery.
    - `springboot-autoconfigure/` (`polyflow-camunda-bpm-springboot-autoconfigure`): Spring Boot auto-configuration for the Camunda Taskpool integration.
    - `springboot-starter/` (`polyflow-camunda-bpm-springboot-starter`): convenience dependency that assembles the Camunda auto-configuration and required
      components.
  - `process-engine-api/` (`polyflow-integration-process-engine-api-parent`): aggregator and dependency-management POM for the generic process-engine API
    integration.
    - `engine-client/` (`polyflow-process-engine-api-engine-client`): generic process-engine client that handles process/task interaction events.
    - `taskpool-collector/` (`polyflow-process-engine-api-taskpool-collector`): generic collector that converts process-engine events into Taskpool commands.
    - `taskpool-job-sender/` (`polyflow-process-engine-api-taskpool-job-sender`): job handler for deferred delivery in generic process-engine deployments.
    - `springboot-autoconfigure/` (`polyflow-process-engine-api-springboot-autoconfigure`): dependency-composition module for generic engine Spring Boot
      auto-configuration.
    - `springboot-starter/` (`polyflow-process-engine-api-springboot-starter`): convenience dependency that assembles the generic engine integration.
- `view/`: read-model API and implementations consumed by a process platform.
  - `pom.xml` (`polyflow-view-parent`): aggregator and dependency-management POM for all view modules.
  - `view-api/` (`polyflow-view-api`): projection models plus query, filtering, sorting, paging, and reactive API contracts for tasks, process data, and data
    entries.
  - `view-api-client/` (`polyflow-view-api-client`): type-safe Axon `QueryGateway` clients and Kotlin extensions for the View API.
  - `simple/` (`polyflow-view-simple`): in-memory, event-driven projection implementation for Taskpool and Datapool views.
  - `jpa/` (`polyflow-view-jpa`): Spring Data JPA read projections, repository queries, entity mappings, and SQL-DDL generation support.
  - `mongo/` (`polyflow-view-mongo`): reactive Spring Data Mongo read projections, change tracking, and Axon token storage.
  - `form-url-resolver/` (`polyflow-form-url-resolver`): property-driven resolver for task, process-start, application, and data-entry form URLs.
- `docs/`: MkDocs source. Keep documentation aligned with public APIs and behavior.
  - `introduction/`: domain concepts, architecture, features, and deployment model.
  - `reference-guide/`: component and configuration reference material.
  - `examples/`: approval example, component descriptions, and deployment scenarios.
  - `developer-guide/`: setup, build, release, and contribution guidance.
- `mkdocs.yml`: documentation navigation and site configuration; `docs/requirements.txt` pins Python documentation dependencies.

## 3. Core Behaviors & Patterns

- Prefer minimal, focused changes. Add no speculative compatibility path, fallback, or abstraction; fail explicitly when an impossible state is reached.
- Use Kotlin idiomatically: `PascalCase` types, `camelCase` members, immutable `val` by default, and `data class`es for message/projection values. Preserve the
  package namespaces already used by the affected module.
- Follow `.editorconfig`: UTF-8, LF, two-space indentation, trailing-whitespace trimming, final newline, and a 160-character maximum line length. Markdown may
  retain trailing whitespace.
- Public Kotlin classes and functions require KDoc under the active Detekt rules. Keep comments concise and document non-obvious invariants or public contracts.
- Treat Axon event classes as durable contracts: preserve `@Revision` semantics and add/adjust upcasters when changing serialized event shape.
- Create tests only when explicitly requested; update focused existing tests when a behavior change requires it. Unit tests conventionally end in `Test` and run in
  Maven's `test` phase. Integration tests end in `ITest` or live under an `itest` package, so `test` alone is normally insufficient verification. Run
  `./mvnw integration-test failsafe:verify -Pitest` whenever the relevant change can affect integration behavior.
- Use `./mvnw clean install` for the normal full build. Run the narrowest relevant Maven module/test command first when practical; do not run release or deploy
  commands without explicit authorization.
- Update `docs/reference-guide/components/` or `docs/reference-guide/configuration/` for public component/configuration changes, and update examples or
  developer documentation when user-facing workflows change. Build documentation with `mkdocs build` when documentation changes need verification.
- Follow the documented Gitflow process for contribution work: branch from `develop`, use a `feature/<name>` branch, rebase before a pull request, and use
  semantic commit messages where practical.
- Ask before making a decision that materially changes the agreed product scope.
- For every new feature: introduce it, run a short scope discussion, document the agreed behavior with acceptance criteria, clarify the technical approach, then
  implement it. Do not start implementation before those steps are complete. A feature is complete only after its acceptance criteria also cover a short feature
  description.
- Maven modules are implemented primarily in Kotlin, with public APIs separated from core, integration, and view implementations.
- Each publishable component is its own Maven module, normally with `src/main/kotlin`, `src/main/resources`, and mirrored `src/test/kotlin` trees. Place a change
in the lowest-level module that owns its API or implementation; keep public APIs, event contracts, aggregate behavior, integrations, and views separated.
- Components use Spring configuration and property classes for optional integration setup.
- Kotlin Logging is used for operational logging; messages commonly carry a stable component identifier.
- Tests are colocated in each module under `src/test` and mirror production packages.
- Keep public API modules independent of concrete view and integration implementations.
- Keep documentation in Markdown and add published pages to `mkdocs.yml` navigation when they should be reachable in the site.
- Follow [ADR-000](specs/adr/000-adr-conventions.md) for ADR format and numbering.

## 5. Working Agreements

- Respond in English; keep technical terms in English and never alter fenced code blocks unless asked.
- Before changing code, inspect related usages, module dependencies, public APIs, event revisions/upcasters, and the corresponding reference documentation.
- Every feature or change requires an ADR in `specs/adr` and user-facing documentation in `docs`, normally under `docs/reference-guide`; document presence,
  usage, and configuration.
- For a request introduced as `New feature`, clarify the scope until it is understood, create the ADR and user-facing documentation, then wait for an explicit
  implementation request. Do not implement during the planning phase.
- Create tests or run lint/format tasks only when explicitly requested. Do not add tests for guarantees already provided by the type system.
