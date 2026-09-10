# ADR 005: Liquibase Application Schema Adoption

- Name: ADR-005
- Status: Accepted
- Date: 2026-09-09

## Context

Applications can already have a verified Polyflow schema and a
`DATABASECHANGELOG` populated by their own Liquibase changes, without any
Polyflow Liquibase history. They need a low-friction way to adopt the Polyflow
changelogs so that later releases are migrated normally. Requiring a separate
Liquibase CLI installation adds an unnecessary deployment dependency.

## Decision

The Liquibase module will provide Spring Boot auto-configuration for a
one-shot schema-adoption mode. It is created only when an explicit adoption
property is enabled. The adopter uses the application's configured Liquibase
root changelog and datasource, runs `changelog-sync`, applies the Polyflow
release tag, and exits instead of starting the application.

The required tag is derived from the Liquibase module's packaged build
properties. The module version must match `X.Y.Z` or `X.Y.Z-SNAPSHOT`; the
adopter uses `X.Y` as the database tag.

The adopter may perform a narrow precheck for existing Polyflow Liquibase
history, but it must allow unrelated application Liquibase history. It does not
attempt to infer schema equivalence: an engineer explicitly enables adoption
only after verifying the installed schema.

## Consequences

- Adoption requires no separately installed Liquibase binary.
- Existing customer changeset history remains intact.
- Deployments opt in explicitly and complete as a one-shot initialization
  process.
- Major and minor releases require correct packaged build metadata and matching
  database tags.
