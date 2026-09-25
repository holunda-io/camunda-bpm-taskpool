# ADR-001: Adapter-Neutral Process Engine API Integration

- Name: Adapter-Neutral Process Engine API Integration
- Status: Accepted
- Date: 2026-08-14

## Context

This decision follows the ADR format defined in [ADR-000](./000-adr-conventions.md).

Polyflow currently provides a Camunda Platform 7-specific integration. It does not yet provide an integration based on the Process Engine API.

The new Process Engine API integration must work with any compatible Process Engine API adapter while keeping Polyflow's published integration artifacts
adapter-neutral. A concrete adapter is an application choice and is needed only by tests that require an executable engine.

The new Taskpool collector has two possible task-delivery designs:

1. Subscribe `UserTaskSupport` to the Process Engine API and register the collector as a handler on that support component.
2. Register the collector itself as a user-task subscriber through the Process Engine API.

## Decision

The Process Engine API integration remains adapter-neutral in production.

- Production modules depend on `dev.bpm-crafters.process-engine-api:process-engine-api` only; they must not depend on a concrete Process Engine API adapter,
  directly or through an inherited parent-POM dependency.
- Applications choose and provide their own compatible adapter, for example a Camunda Platform 7 embedded adapter.
- Concrete adapter dependencies are permitted only in test scope, declared by the individual module that needs an executable engine. The Process Engine API
  parent POM must not declare an adapter dependency, including with test scope.
- The Taskpool collector uses the second delivery design: it registers directly with `TaskSubscriptionApi` using `SubscribeForTaskCmd` rather than receiving
  task deliveries through `UserTaskSupport`.
- The collector owns subscription creation and teardown, and supplies both the task-delivery and task-termination handlers. Delivery is translated to Taskpool
  create, assign, update, and complete commands; termination is translated to deletion.
- `UserTaskSupport` remains available for concerns that require its local task state, such as the engine client validating task existence and process-variable
  assignment support. It is not the collector's delivery intermediary.

## Implementation Constraints

- The collector subscription must explicitly define its Process Engine API restrictions, optional task-description key, and requested payload variables.
- The collector creates its `TaskSubscription` during startup, retains it, and unregisters it during shutdown.
- A task delivery is mapped according to its Process Engine API reason. A delivery with `COMPLETE` maps to a Taskpool completion command; a termination callback
  maps to a Taskpool deletion command. Implementations must not infer completion from termination.
- The collector owns any local state needed to distinguish create, assignment, and update deliveries when an adapter does not provide reliable reasons; it must
  not depend on `UserTaskSupport` for that classification.
- Engine-client and process-variable-assignment features own their separate `UserTaskSupport` provisioning and subscription when they require local task state.
- The implementation must test direct subscription registration, delivery-to-command mapping, termination-to-deletion mapping, unsubscription, and the absence
  of a concrete adapter in each published artifact's production dependency tree.

## Consequences

The published integration artifacts no longer select or transitively expose a specific process-engine implementation. Consumers must add their chosen adapter
explicitly. Module-local tests may use a concrete adapter without exposing it to consumers or sibling modules.

The collector becomes self-contained and can use subscription restrictions and payload selection at the Process Engine API boundary. Its lifecycle and delivery
path are explicit:

```text
Process Engine API adapter -> TaskSubscriptionApi -> Polyflow collector subscription -> Taskpool sender
```

The direct collector must preserve behavior previously supplied by `UserTaskSupport`, particularly assignment/change classification where an adapter does not
provide reliable event reasons. It must also preserve the adapter's delivery and transaction semantics; direct registration does not make delivery transactional
by itself.

The shared Taskpool-core importer filter must recognize both the adapter-neutral `processengineapi` configuration namespace and the existing Camunda BPM
namespace while both integrations are supported.

## Alternatives Considered

Keeping the adapter starter as a production dependency is rejected because it leaks Camunda Platform 7 through the generic integration. Using
`UserTaskSupport` as the collector's delivery intermediary is rejected because subscription ownership remains implicit; it remains appropriate for
engine-client features that need task lookup and state.
