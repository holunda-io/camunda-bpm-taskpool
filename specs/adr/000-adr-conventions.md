# ADR 000: ADR Conventions

- Name: ADR-000
- Status: Accepted
- Date: 2026-09-07

## Context

The project needs a consistent format for architecture decision records.

## Decision

We document architecture decisions as ADRs in `specs/adr`.

ADR file names use the format `XXX-short-title.md`, where:

- `XXX` is a zero-padded numeric identifier
- identifiers increase over time
- examples: `000-adr-conventions.md`, `001-command-mode-primitives.md`
- ADR's name is `ADR-XXX`

Each ADR must contain these header fields:

- `Name`
- `Status`
- `Date`

Each ADR must contain these sections:

- `Context`
- `Decision`
- `Consequences`

Additional custom sections are allowed when useful.

Each ADR should be concise and must not exceed 150 lines.

If one ADR references another ADR, it should use local links in markdown with a label containing the ADR name and link to the file.
Example: [ADR-000](./000-adr-conventions.md)

## Consequences

- ADRs have one local, predictable format
- ADR file names are ordered and easy to reference
- ADRs stay precise, short and focused
