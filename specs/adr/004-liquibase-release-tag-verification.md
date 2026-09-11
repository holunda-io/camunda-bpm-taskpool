# ADR 004: Liquibase Release Tag Verification

- Name: ADR-004
- Status: Accepted
- Date: 2026-09-09

## Context

The Liquibase baseline and release tag identify the database schema release by
the major and minor components of the Polyflow version. A manually maintained
tag can drift from the version published by Maven. Such a drift makes the
Liquibase history misleading and weakens rollback and operational diagnostics.

## Decision

The Liquibase integration test derives the expected release tag from the
module's Maven version. It removes an optional `-SNAPSHOT` suffix and takes the
major and minor components.

For example, Maven version `4.7.1-SNAPSHOT` requires database tag `4.7`.
The build fails when the applied Liquibase tag does not equal the derived
value. The tag remains a literal Liquibase change because major-version changes
are deliberately performed manually.

## Consequences

- A release branch cannot publish a Liquibase artifact with a stale release
  tag.
- Patch versions do not require a database-schema tag change.
- A major-version release requires the maintainer to update the Liquibase tag
  deliberately.
