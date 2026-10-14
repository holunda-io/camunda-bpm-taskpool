# Process-definition lifecycle collection

Enable the process-definition collector to publish definition registration and lifecycle changes:

```yaml
polyflow:
  integration:
    collector:
      camunda:
        process-definition:
          enabled: true
```

The collector emits `ProcessDefinitionRegisteredEvent`, `ProcessDefinitionSuspendedEvent`, `ProcessDefinitionActivatedEvent`, and `ProcessDefinitionUnregisteredEvent`. For deployment deletion, it snapshots every definition in the deployment and publishes unregistration only after the Camunda transaction commits successfully. The collector additionally reconciles its known definitions with the repository so it can discover changes performed outside the normal command path.

Each command can be enriched before it is sent. Set `process-definition.enricher.type` to `property` to add configured attributes, `no` to disable enrichment, or `custom` to provide an application `ProcessDefinitionCommandEnricher` bean. Process-variable filter and correlator configuration does not apply: a process definition has no process-instance variable scope.
