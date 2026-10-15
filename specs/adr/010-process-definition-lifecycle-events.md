# ADR-010: Process-definition lifecycle events

## Status

Accepted.

## Context

The process-definition collector currently emits only registration events. Consumers cannot react to a definition being suspended, activated, or deleted, and
registration events cannot carry collector-provided metadata.

## Decision

Process-definition commands and events carry an optional typed payload. The collector exposes a definition-specific enricher that receives a process-definition
command before it is sent to Taskpool Core.

The collector tracks definitions known to it and reconciles their repository state. State changes create suspended or activated commands. A pre-command
interceptor captures every definition in a `DeleteDeploymentCmd` by deployment ID before deletion, executes the command, and publishes one unregistration
command for each captured definition only at `TransactionState.COMMITTED`. The snapshot preserves deletion metadata. Reconciliation also detects changes made
outside that command path.

Process-variable filters and correlators are not used because a process definition has no process-instance variable scope.

## Consequences

Consumers can react to the full definition lifecycle and use enriched metadata. The interceptor depends on Camunda internal command types and is covered by
integration tests; reconciliation remains the fallback for changes not observed by it.

## View projections

The view API exposes the definition state and payload. Simple, JPA, and Mongo projections create definitions as active, change state on suspension and
activation, and remove the projection on unregistration. JPA persists state and serialized payload in `PLF_PROC_DEF`; database-specific Liquibase evolution
scripts supply the schema change.
