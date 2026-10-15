# ADR 009: Process-Instance Event Enrichment

- Name: ADR-009
- Status: Accepted
- Date: 2026-10-14

## Context

Process-instance lifecycle events previously exposed only engine metadata. Consumers
reacting to start, suspend, resume, finish, or cancellation could not use the
selected process variables or business-data correlations already available on task
events.

Task enrichment already defines the configuration for selecting process variables
and creating correlations. Repeating those definitions for process instances would
allow the two event streams to disagree.

## Decision

Process-instance commands and events carry payload and correlations. The collector
uses the existing `task.enricher` mode, `ProcessVariablesFilter`, and
`ProcessVariablesCorrelator` configuration when enriching process-instance
commands. Process-level filters and global correlations apply; task-specific
filters and correlations do not, because a lifecycle event has no task definition.

The collector reads runtime variables first and falls back to historic variables
after Camunda has removed an ended instance. Old serialized events are upcast with
empty payload and correlation maps.

## Consequences

- One filter and correlator configuration governs task and process-instance data.
- End-event enrichment requires Camunda historic variables to retain a final
  variable snapshot after runtime deletion.
- Existing event streams remain readable and receive empty enrichment maps.
