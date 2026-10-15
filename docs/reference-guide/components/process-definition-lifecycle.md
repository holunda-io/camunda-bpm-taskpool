---
title: Process-definition lifecycle collection
---

## Purpose

Process-definition collection keeps Polyflow's process catalogue aligned with the
definitions deployed to Camunda. In addition to initial registration, it publishes
suspension, activation, and removal. A view can therefore stop offering a suspended
definition and remove a definition that was undeployed.

The feature is intended for process-starter catalogues and other consumers which
need deployment metadata but must not query each Camunda engine directly.

## Enable collection

The collector and corresponding sender are disabled by default. Enable both in the
process application:

```yaml
polyflow:
  integration:
    collector:
      camunda:
        application-name: ${spring.application.name}
        process-definition:
          enabled: true
    sender:
      enabled: true
      process-definition:
        enabled: true
```

The collector belongs to `polyflow-camunda-bpm-taskpool-collector`. It creates
commands locally; Taskpool Core persists them and emits events, which a Simple,
JPA, or Mongo view projects. In a distributed installation, route all four
process-definition event types to view applications, not only registration.

## Lifecycle events

| Camunda change | Polyflow event | Result in a view |
|---|---|---|
| A BPMN definition is deployed and collected | `ProcessDefinitionRegisteredEvent` | Creates or refreshes an `ACTIVE` definition. |
| A definition is suspended | `ProcessDefinitionSuspendedEvent` | Changes its state to `SUSPENDED`. |
| A definition is activated again | `ProcessDefinitionActivatedEvent` | Changes its state to `ACTIVE`. |
| A deployment is deleted | `ProcessDefinitionUnregisteredEvent` for every definition in the deployment | Removes every affected projection. |
| One definition is deleted by id | `ProcessDefinitionUnregisteredEvent` | Removes that definition's projection. |

One Camunda deployment may contain several BPMN processes. Deleting that deployment
therefore creates one unregistration event per process definition. Do not model a
deployment deletion as a single catalogue entry.

### Transaction safety and detection

Camunda Cockpit uses repository commands for deployment deletion and
single-definition deletion. The collector observes these internal commands before
they run, records the affected definition metadata, and publishes only after the
Camunda transaction has committed. A failing delete transaction consequently does
not remove a Polyflow projection.

The Camunda repository-command classes are internal Camunda APIs. The collector is
tested against the supported Camunda version, and also reconciles known definitions
with the repository. Reconciliation is the fallback for changes made outside the
normal command path; it is not a replacement for committed-transaction handling.

Registration is asynchronous after BPMN parsing. Ensure the Camunda job executor
is running, otherwise a newly deployed definition may not appear until the refresh
job is executed.

## Definition payload enrichment

Every process-definition lifecycle command and event has a typed `payload` map.
Use it for stable catalogue metadata that belongs to the definition, such as a
business domain, product category, support tier, or feature flag. It is distinct
from process-instance variables: a definition has no runtime variable scope.

Select the enrichment strategy with
`polyflow.integration.collector.camunda.process-definition.enricher.type`:

| Type | Behaviour |
|---|---|
| `no` | Default. Sends an empty payload. |
| `property` | Uses attributes declared in YAML or Spring properties. |
| `custom` | Disables built-in auto-configuration. The application must provide a `ProcessDefinitionCommandEnricher` bean. |

### Property enrichment

The `property` enricher combines global attributes with attributes scoped to the
definition key. Key-specific values are applied last and override equally named
global values. YAML retains scalar types, so strings, numbers, booleans, and lists
of strings can be used without converting them to strings.

```yaml
polyflow:
  integration:
    collector:
      camunda:
        process-definition:
          enabled: true
          enricher:
            type: property
            attributes:
              # Applied to every collected process definition.
              - values:
                  domain: approvals
                  retentionDays: 365
                  visible: true
                  tags: [case-management, external]
              # Applied after the global values for this process key.
              - process-definition-key: request-approval
                values:
                  domain: finance
                  visible: false
                  tags: [approval, regulated]
```

The payload for `request-approval` contains `domain: finance`,
`retentionDays: 365`, `visible: false`, and the key-specific `tags` list. Define
at most one attribute block for a process-definition key; that makes overrides
unambiguous and easy to review.

### Custom enrichment

Set `type: custom` when data must be derived from an application service, tenant
configuration, or another source. Register one application bean implementing
`ProcessDefinitionCommandEnricher`. The collector invokes it before sending every
lifecycle command. With `custom`, Polyflow intentionally does not create a
fallback enricher; a missing application bean is a configuration error.

Do not use the task process-variable filter or correlator for this purpose. Those
settings are reused for process-*instance* events only and cannot be evaluated for
a process definition.

## View model and querying

`ProcessDefinition` exposes the following additional fields:

| Field | Meaning |
|---|---|
| `state` | `ACTIVE` or `SUSPENDED`. Registered definitions start as `ACTIVE`. |
| `payload` | Typed metadata added by the selected definition enricher. |

The lifecycle itself is visible through projections rather than a separate query.
An unregistered definition is absent from query results. A suspended definition
remains queryable so an application can explain why it cannot be started; process
starter UIs should only offer definitions whose state is `ACTIVE`.

The Simple, JPA, and Mongo views apply the same lifecycle semantics. JPA stores
state and serialized payload in `PLF_PROC_DEF`; Mongo stores them in its
process-definition document. See [View API](view-api.md), [JPA View](view-jpa.md),
and [Mongo View](view-mongo.md) for storage-specific details.

### Event-stream compatibility

Process-definition events use revision 2 to carry the payload. The event upcaster
reads revision 1 registration events and supplies an empty payload, so an existing
event store can be replayed without rewriting historical events. Process-instance
lifecycle events use the same compatibility approach for their payload and
correlation maps. Applications should treat missing historic enrichment as an empty
map, not as a failed projection.

## Database upgrade

Liquibase 4.7 introduces the view-side state and payload columns. The changes are
additive: existing scripts and baselines are unchanged. Existing process
definitions are migrated to `ACTIVE`; payload remains `NULL`, which the view
deserializes as an empty map. Existing process-instance payload and correlation
columns follow the same empty-on-read rule.

Include both `polyflow-core-changelog.xml` and `polyflow-view-changelog.xml` as
appropriate for the application role. Both masters are tagged `4.7`, while the
view master contains the new schema evolution. Never edit a previously released
Liquibase script; upgrades must add a new changeset.

## Related documentation

- [Publishing Startable Process Definitions](startable-process-definitions.md)
- [Taskpool Collector](camunda-taskpool-collector.md)
- [View API](view-api.md)
- [Persistence configuration](../configuration/persistence.md)
