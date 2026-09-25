# ADR 007: Filter Before Process-Variable Deserialization

- Name: ADR-007
- Status: Accepted
- Date: 2026-09-24

## Context

Task enrichment previously loaded every process variable as a deserialized
value before applying `ProcessVariablesFilter`. A remote standalone engine can
contain serialized values whose application classes are intentionally absent.
Loading an excluded value then fails before the filter can protect the
collector.

Correlation identifiers may be excluded from task payload while still being
needed to create a business-data correlation.

## Decision

When a payload filter can exclude a variable, the built-in process-variable
enricher first retrieves available variables with Camunda deserialization
disabled. It applies the configured payload filter to their names, adds variable
names referenced by configured correlations, and then retrieves only that union
with deserialization enabled. When no filter restricts the task, it preserves
the original single deserialized read.

The payload continues to contain only variables that pass the payload filter.
Correlation-only values are used only to create correlations. The historic-task
path retains its isolated command context and task-to-execution fallback for
both retrieval steps.

## Consequences

- Excluded complex values no longer require their Java classes in a remote
  engine deployment.
- Payload and correlation values must remain deserializable by the collector.
- The collector performs two variable lookups only when a payload filter can
  exclude variables for the task.
