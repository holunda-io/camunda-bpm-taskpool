# ADR 001: Design-First Feature Development

- Name: ADR-001
- Status: Accepted
- Date: 2026-09-07

## Context

The project needs a consistent way to turn feature and change requests into an agreed, user-facing design before implementation begins.

## Decision

Every feature or change must have an ADR in `specs/adr` before implementation starts. ADRs follow [ADR-000](./000-adr-conventions.md).

For a request introduced as `New feature`, first clarify its scope until the intended behavior is clear. Do not implement while clarification or design is in
progress.

Then create the next numbered ADR. It must describe the scope, decision, and consequences, and link to the accompanying user-facing documentation.

Add or update user-facing documentation in `docs`, normally under `docs/reference-guide`. It must explain:

- presence: what the feature provides and where users encounter it;
- usage: how users use it, with examples where useful;
- configuration: settings, defaults, and required setup.

Wait for an explicit request to implement after the ADR and user-facing documentation are complete.

## Consequences

- `specs/adr` is the authoritative location for ADRs.
- Feature design and user-facing documentation are reviewed before code changes begin.
- Implementation starts only after an explicit follow-up request.
