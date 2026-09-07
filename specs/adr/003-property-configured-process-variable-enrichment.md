# ADR 003: Property-Configured Process Variable Enrichment

- Name: ADR-003
- Status: Accepted
- Date: 2026-09-07

## Context

`ProcessVariablesFilter` and `ProcessVariablesCorrelator` previously required a
Spring bean supplied in application code. This is unsuitable for a remotely
configured process engine: the process application needs to select payload
variables and business-data correlations through external Spring configuration,
without a deployment-specific configuration class.

The collector already provides empty fallback beans. The property-backed
configuration must integrate with those fallbacks and must not replace an
application-provided bean.

## Decision

Provide two opt-in Spring Boot auto-configurations:

- `polyflow.integration.collector.camunda.process-variables-filter` creates a
  `ProcessVariablesFilter` when `enabled=true`.
- `polyflow.integration.collector.camunda.process-variables-correlator` creates
  a `ProcessVariablesCorrelator` when `enabled=true`.

Both configurations run before their empty fallback configuration and are
conditional on no bean of the respective type already being present. The filter
configuration maps each configured item to a process-level filter, a task-level
filter, or both. Filters for one process compose: all matching filters must
include a variable. The correlator configuration uses the existing
`ProcessVariableCorrelation` and `CorrelationDefinition` model.

The property layout and remote-engine deployment guidance are documented in
[Remote Engine Process-Variable Configuration](../../docs/reference-guide/configuration/remote-engine-process-variable-configuration.md).

## Consequences

- Remote process-engine deployments can configure payload filtering and business
  correlations using environment-specific Spring properties.
- Both features remain disabled by default; the existing permissive filter and
  empty correlator remain in effect until enabled.
- A custom `ProcessVariablesFilter` or `ProcessVariablesCorrelator` bean remains
  the override mechanism for programmatic behavior.
- A configured process can combine its process-level boundary with task-level
  restrictions. This keeps remote-engine payload configuration declarative while
  retaining a custom bean as the escape hatch for dynamic filter behavior.
