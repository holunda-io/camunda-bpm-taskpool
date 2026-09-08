---
title: Remote Engine Process-Variable Configuration
---

### Purpose

This configuration is intended for a remote process engine: a process
application that runs Camunda and Taskpool Collector while Taskpool Core (or its
command handlers) is deployed separately. In this topology, process-specific
Java configuration is often undesirable. The collector can instead create its
process-variable filter and business-data correlator entirely from Spring
properties, so the configuration can be supplied by the environment, a mounted
configuration file, or a configuration service.

The feature controls two distinct outputs added to task commands:

| Component | What it controls | Result |
| --- | --- | --- |
| `ProcessVariablesFilter` | Which Camunda process variables may become task payload | A smaller, deliberate task payload |
| `ProcessVariablesCorrelator` | Which process-variable values identify business data | Task correlations of `entry-type` and `entryId` |

Filtering does not create correlations, and correlation does not add a variable
to the task payload. Configure both when the remote task platform needs both
the selected payload values and references to business data.

### Required collector setup

Process-variable enrichment must be active for filtering and correlation to be
applied to task commands:

```yaml
polyflow:
  integration:
    collector:
      camunda:
        task:
          enricher:
            type: process-variables
```

For a remote command destination, use a sender strategy that dispatches after
the Camunda transaction has committed. `txjob` writes a Camunda job and sends
the accumulated commands in its own transaction, which avoids sending a command
for an engine transaction that later rolls back:

```yaml
polyflow:
  integration:
    sender:
      task:
        enabled: true
        type: txjob
```

See [Taskpool Sender](../components/common-taskpool-sender.md) for sender and
transactional-delivery details.

### Complete remote-engine example

The following example configures an `approval` process without adding an
application-specific `@Bean`. The task payload contains only the selected
variables. Every `approval` task is correlated to a request, and its `approve`
task also carries a customer correlation.

```yaml
polyflow:
  integration:
    collector:
      camunda:
        task:
          enabled: true
          enricher:
            type: process-variables
            process-variables-filter:
              enabled: true
              filters:
                - process-definition-key: approval
                  filter-type: INCLUDE
                  process-variables:
                    - requestId
                    - applicant
                    - customerId
                  task-variables:
                    approve:
                      - requestId
                      - customerId
            process-variables-correlator:
              enabled: true
              correlations:
                - process-definition-key: approval
                  global-correlations:
                    - entry-id-variable-name: requestId
                      entry-type: request
                  correlations:
                    approve:
                      - entry-id-variable-name: customerId
                        entry-type: customer
    sender:
      task:
        enabled: true
        type: txjob
```

Spring Boot's relaxed binding also accepts kebab-case names, for example
`process-definition-key`, `filter-type`, and `entry-id-variable-name`.

### Process-variable filter

Enable the property-backed filter with:

```text
polyflow.integration.collector.camunda.task.enricher.process-variables-filter.enabled=true
```

The YAML snippets below are the contents of
`polyflow.integration.collector.camunda.task.enricher`.

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | Boolean | `false` | Creates the property-backed `ProcessVariablesFilter`. |
| `filters` | list | empty | Filter definitions, each global or scoped to one process definition. |
| `filters[].process-definition-key` | String | absent | Process definition key. Omit only for a global process-level filter. |
| `filters[].filter-type` | `INCLUDE` / `EXCLUDE` | `EXCLUDE` | Whether the listed variables are permitted or rejected. |
| `filters[].process-variables` | list of String | empty | Variables for every task in the process. |
| `filters[].task-variables` | map of task key to list of String | empty | Variables per task; makes the definition task-level. |

One definition can contain both `process-variables` and `task-variables`. A
task-level definition requires `process-definition-key`. When both fields are
supplied, the process-level rule applies to every task and the task-level rule
is an additional restriction for the task keys listed. A variable must pass all
applicable filters; in other words, the rules are combined as an allow-list
intersection (or, for exclusions, the union of excluded names).

#### Process-level filters

An `INCLUDE` filter makes the payload an allow-list. It is the usual safe choice
for a remote engine because variables such as internal state, technical IDs, or
large objects are not accidentally sent to the remote task platform.

```yaml
process-variables-filter:
  enabled: true
  filters:
    - process-definition-key: approval
      filter-type: INCLUDE
      process-variables: [requestId, applicant, customerId]
```

An `EXCLUDE` filter sends every variable except those named. It is useful when
the set of business variables is large and stable technical variables must stay
local:

```yaml
process-variables-filter:
  enabled: true
  filters:
    - process-definition-key: approval
      filter-type: EXCLUDE
      process-variables: [internalAudit, transientToken]
```

#### Task-level filters

Use task-level filtering when individual tasks need different payloads. It can
be combined with a process-level filter in the same definition. A task key
absent from `task-variables` is restricted only by the process-level rule.

```yaml
process-variables-filter:
  enabled: true
  filters:
    - process-definition-key: approval
      filter-type: INCLUDE
      process-variables: [requestId, applicant, customerId]
      task-variables:
        submit: [requestId, applicant]
        approve: [requestId, applicant, customerId]
```

#### Global filters and precedence

A process-level filter without `process-definition-key` is global. It applies only
to process definitions that do not have their own filter. A process-specific
filter takes precedence over the global filter.

```yaml
process-variables-filter:
  enabled: true
  filters:
    - filter-type: EXCLUDE
      process-variables: [internalAudit]
    - process-definition-key: approval
      filter-type: INCLUDE
      process-variables: [requestId, applicant]
```

### Process-variable correlation

Enable the property-backed correlator with:

```text
polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.enabled=true
```

The YAML snippets below are the contents of
`polyflow.integration.collector.camunda.task.enricher`.

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | Boolean | `false` | Creates the property-backed `ProcessVariablesCorrelator`. |
| `correlations` | list | empty | One correlation definition per process definition key. |
| `correlations[].process-definition-key` | String | required | Process definition key. |
| `correlations[].global-correlations` | list | empty | Correlations applied to every task of the process. |
| `correlations[].correlations` | map of task key to list | empty | Additional correlations applied only to the named task. |
| `*.entry-id-variable-name` | String | required | Camunda variable whose value becomes the business entry ID. |
| `*.entry-type` | String | required | Business-data entry type associated with that ID. |

A global correlation is useful for the primary business object of a process:

```yaml
process-variables-correlator:
  enabled: true
  correlations:
    - process-definition-key: approval
      global-correlations:
        - entry-id-variable-name: requestId
          entry-type: request
```

Add task-specific correlations for data that is relevant only at selected user
tasks:

```yaml
process-variables-correlator:
  enabled: true
  correlations:
    - process-definition-key: approval
      correlations:
        approve:
          - entry-id-variable-name: customerId
            entry-type: customer
        amend:
          - entry-id-variable-name: previousRequestId
            entry-type: request
```

If the configured process variable is absent for a task, no correlation is
created for that definition. Variable values are converted to their string
representation before they are stored as entry IDs.

### Defaults and custom beans

Both property configurations are opt-in. If a feature is disabled or omitted,
the collector retains its existing fallback behavior:

- the fallback filter does not remove variables;
- the fallback correlator creates no correlations.

An application-defined `ProcessVariablesFilter` or
`ProcessVariablesCorrelator` bean takes precedence over these property-backed
beans, even if the corresponding `enabled` property is `true`. Use a custom bean
when filter composition or correlation logic cannot be represented by the
property model.
