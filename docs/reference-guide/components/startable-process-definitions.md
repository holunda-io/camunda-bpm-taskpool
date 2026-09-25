---
title: Publishing Startable Process Definitions
pageId: startable-process-definitions
---

## Purpose

Polyflow can publish metadata for BPMN process definitions deployed in a Camunda
engine. A process-starter application can use this metadata to present only the
processes a signed-in user may start, including the key of the corresponding
start form.

Publishing a definition does not start a process instance. It supplies a
read-model entry; the process-starter application remains responsible for
rendering the form and invoking Camunda to start the selected process.

## Enable publishing

The process-definition collector is disabled by default. Enable both collection
and process-definition command sending in the process application:

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

The collector is part of
`polyflow-camunda-bpm-taskpool-collector`. The sender forwards the collected
commands to Taskpool Core, which emits the events consumed by a View
implementation. Configure one of the [View components](index.md) in the
application that serves process-starter queries.

When a BPMN deployment is parsed, the collector schedules an asynchronous
refresh. It reads the deployed definition and publishes its metadata. This also
means a process application needs a working Camunda job executor to process the
refresh job.

## Information published

For every registered definition, Polyflow publishes the following information:

| Field                                                                     | Source                                                    |
|---------------------------------------------------------------------------|-----------------------------------------------------------|
| `processDefinitionId`, `processDefinitionKey`, `processDefinitionVersion` | Camunda process definition                                |
| `processName`, `processDescription`, `processVersionTag`                  | BPMN process metadata                                     |
| `applicationName`                                                         | `polyflow.integration.collector.camunda.application-name` |
| `formKey`                                                                 | Camunda start form, if present                            |
| `startableFromTasklist`                                                   | Camunda's startable-in-tasklist setting                   |
| `candidateStarterUsers`, `candidateStarterGroups`                         | Camunda candidate starter identity links                  |

Model candidate starters on the BPMN process, for example with Camunda's
`camunda:candidateStarterUsers` and `camunda:candidateStarterGroups`
attributes. The collector copies the resulting user and group identifiers; it
does not resolve group memberships itself.

### Configure candidate starters in Camunda 7 Modeler

Camunda 7 Modeler supports candidate starters directly. Select the BPMN process (click the process or the canvas) and use **General → Candidate Starter
Configuration** in the properties panel to enter **Candidate Starter Users**
and **Candidate Starter Groups**. The modeler writes the corresponding
`camunda:candidateStarterUsers` and `camunda:candidateStarterGroups`
attributes on the `bpmn:process` element.

Do not configure these values on a User Task. A User Task's **Candidate Users**
and **Candidate Groups** control assignment of that task after a process has
started. The process-level **Candidate Starter** fields determine the values
that Polyflow publishes for startable process definitions.

Candidate starters are a Camunda 7 feature. They do not transfer unchanged to
Camunda 8 process models.

## Query definitions available to a user

Use `ProcessDefinitionsStartableByUserQuery` with the authenticated user's ID
and groups. A definition matches when all of the following are true:

1. Camunda marked it as startable from the tasklist.
2. The user is listed in `candidateStarterUsers`, or one of the user's groups is
   listed in `candidateStarterGroups`.

Definitions with no candidate starter users or groups therefore do not match
this query. Ensure the supplied user ID and group IDs use the same identifiers
as the BPMN model.

The In-Memory and MongoDB views return only the newest version for each
process-definition key. The query is also supported by the JPA view; callers
that use JPA should select the desired version when their process catalogue
contains multiple versions.

!!! warning
The current JPA view filters by candidate starter authorization but does not
filter `startableFromTasklist`. A process-starter application using that
view should apply this field as an additional filter until the view behavior
is aligned with the query contract.

With the View API client, a caller can issue the query as follows:

```kotlin
val definitions = processDefinitionQueryClient.query(
  ProcessDefinitionsStartableByUserQuery(
    user = User(username = "kermit", groups = setOf("muppetshow"))
  )
).get()
```

Each returned `ProcessDefinition` contains the `formKey` and `applicationName`.
The [Form URL Resolver](view-form-url-resolver.md) can turn these values into a
start-form URL according to its configured process template.

### Select the latest definition version

The In-Memory and MongoDB views return only the newest version for each
`processDefinitionKey`. The JPA view returns all authorized versions. When
using JPA, select the newest version after querying:

```kotlin
val latestDefinitions = processDefinitionQueryClient.query(query).get()
  .groupBy { it.processDefinitionKey }
  .mapValues { (_, versions) ->
    versions.maxBy { it.processDefinitionVersion }
  }
  .values
```

The JPA view also currently does not filter `startableFromTasklist`; apply that
filter in the caller before selecting versions when it is relevant to the
process-starter UI.

## Distributed with Kafka

In the Kafka deployment topology, process-definition collection and Taskpool
Core stay local to the process application. The collector sends a
`RegisterProcessDefinitionCommand` to the local command bus, and Taskpool Core
stores and emits a `ProcessDefinitionRegisteredEvent`. Kafka distributes that
event to independently deployed views after the local transaction commits.
Kafka is not used to distribute the registration command.

The Kafka example uses a custom payload-type router. It publishes only event
types explicitly mapped under `polyflow.axon.kafka.topics`; an unmapped
`ProcessDefinitionRegisteredEvent` is not sent to Kafka. Add a mapping for it,
preferably to a dedicated topic:

```yaml
polyflow:
  axon:
    kafka:
      topics:
        - payloadType: io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
          topic: polyflow-process-definition
```

Create the topic in the Kafka cluster. A single partition is normally
sufficient because deployments are infrequent; select the partition count,
replication factor, and retention period according to the deployment's
availability and recovery requirements.

The view application needs a Kafka message source that consumes
`polyflow-process-definition`, plus a tracking event processor for the process
definition projection. Configure that processor to use the new source:

```yaml
axon:
  eventhandling:
    processors:
      "[io.holunda.polyflow.view.jpa.service.process.definition]":
        source: kafkaMessageSourcePolyflowProcessDefinition
        mode: TRACKING
        threadCount: 1
        batchSize: 1
```

### Connect configuration and Spring beans

The `source` value in Axon's processor configuration is a Spring bean name. It
is not a Kafka topic name. The following names therefore have distinct roles:

| Name                                                      | Role                                             | Must match                                                |
|-----------------------------------------------------------|--------------------------------------------------|-----------------------------------------------------------|
| `polyflow-process-definition`                             | Kafka topic                                      | The producer route and the message source's `topics` list |
| `kafkaMessageSourcePolyflowProcessDefinition`             | Spring `StreamableKafkaMessageSource` bean name  | `axon.eventhandling.processors[...].source`               |
| `polyflowProcessDefinition`                               | Spring qualifier for the Kafka `ConsumerFactory` | The qualifier on the message-source parameter             |
| `io.holunda.polyflow.view.jpa.service.process.definition` | Axon processing group                            | The JPA process-definition projection                     |

The Kafka example keeps its topic names in a custom
`polyflow.axon.kafka` properties class. Add a corresponding property, for
example:

```yaml
polyflow:
  axon:
    kafka:
      topic-process-definitions: polyflow-process-definition
```

Then define a consumer factory and the source bean in the view application.
The bean name below is the value referenced by `source` in the preceding Axon
configuration:

```kotlin
@Bean
@Qualifier("polyflowProcessDefinition")
fun kafkaConsumerFactoryPolyflowProcessDefinition(
  properties: KafkaProperties
): ConsumerFactory<String, ByteArray> {
  properties.clientId = "polyflow-process-definition-$hostname"
  return DefaultConsumerFactory(properties.buildConsumerProperties())
}

@Bean("kafkaMessageSourcePolyflowProcessDefinition")
fun kafkaMessageSourcePolyflowProcessDefinition(
  kafkaProperties: KafkaProperties,
  extendedProperties: AxonKafkaExtendedProperties,
  @Qualifier("polyflowProcessDefinition")
  consumerFactory: ConsumerFactory<String, ByteArray>,
  kafkaFetcher: Fetcher<String, ByteArray, KafkaEventMessage>,
  @Qualifier("eventSerializer") serializer: Serializer,
  messageConverter: KafkaMessageConverter<String, ByteArray>
): StreamableKafkaMessageSource<String, ByteArray> =
  StreamableKafkaMessageSource
    .builder<String, ByteArray>()
    .topics(listOf(extendedProperties.topicProcessDefinitions))
    .consumerFactory(consumerFactory)
    .serializer(serializer)
    .fetcher(kafkaFetcher)
    .messageConverter(messageConverter)
    .bufferFactory {
      SortedKafkaMessageBuffer(kafkaProperties.fetcher.bufferSize)
    }
    .build()
```

`AxonKafkaExtendedProperties` must expose `topicProcessDefinitions` for the
example property above. This follows the existing `topicTasks` and
`topicDataEntries` fields in the Kafka example. The source and its consumer
factory should use the same serializer and Kafka client properties as the
existing task and data-entry sources.

Kafka publication is at-least-once. The view must tolerate a repeated event,
and topic retention must be long enough for a view to recover after downtime.
Kafka distributes events but does not replace the event store used by the
process application's Taskpool Core.

## Related components

- [Camunda BPM Engine Taskpool Collector](camunda-taskpool-collector.md)
- [Taskpool Sender](common-taskpool-sender.md)
- [View API](view-api.md)
- [View API Client](view-api-client.md)
- [Form URL Resolver](view-form-url-resolver.md)
