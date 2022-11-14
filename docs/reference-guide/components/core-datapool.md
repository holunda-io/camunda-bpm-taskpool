## Purpose

The component is responsible for maintaining and storing the consistent state of the datapool
core concept of Business Data Entry.

The component receives all commands and emits events, if changes are performed on underlying entities.
The event stream is used to store all changes (purely event-sourced) and should be used by all other
parties interested in changes.

## Configuration

### Component activation

In order to activate Datapool Core component, please include the following dependency to your application

```xml
<dependency>
  <groupId>io.holunda.taskpool</grouId>
  <artifactId>camunda-bpm-datapool-core</artifactId>
  <version>${taskpool.version}</version>
</dependency>
```

and activate its configuration by adding the following to a Spring configuration:

```kotlin
@Configuration
@EnablePolyflowDataPool
class MyConfiguration
```

### Revision-Aware Projection

The in-memory data entry projection is supporting revision-aware projection queries. To activate this, you need
to activate the correlation of revision attributes between your data entries commands and the data entry events. To do so, please
activate the correlation provider by putting the following code snippet in the application containing the Datapool Core Component:

```kotlin
@Configuration
@EnablePolyflowDataPool
class MyConfiguration {

  @Bean
  fun revisionAwareCorrelationDataProvider(): CorrelationDataProvider {
    return MultiCorrelationDataProvider<CommandMessage<Any>>(
      listOf(
        MessageOriginProvider(),
        SimpleCorrelationDataProvider(RevisionValue.REVISION_KEY)
      )
    )
  }

}
```

By doing so, if a command is sending revision information, it will be passed to the resulting event and will be received by the projection, so the
latter will deliver revision information in query results. The use of `RevisionAwareQueryGateway` will allow querying for specific revisions in the data entry
projection, see documentation of `axon-gateway-extension` project.

### Strategies to optimize data entry access

The Business Data Entry is implemented using an `Aggregate` pattern and the corresponding projection as a part of the view component. By default, 
the [Datapool Sender](./common-datapool-sender.md) is used to send commands expressing the change of a Business Data Entry to the `Aggregate Root`, 
which is emitting the corresponding event. If you are dealing with Business Data Entries with a very long lifetime, the number of events emitted by 
the `Aggregate Root` may become large and impacts the load time of it (it is event-sourced). To improve the load time of the aggregate, we developed 
two strategies which can be applied: special repository and snapshotting.

Snapshotting uses standard Axon Snapshotting and will use the latest snapshot and additionally apply the events emitted after the last snapshot instead 
of replaying all events ever emitted by the aggregate.

The special repository uses the fact that `Data Entry Aggregate Root` state is not changed by update events and the first event it emits during creation 
already contains everything the aggregates require during loading.

In order to select the strategy best matching your use case, please consult the [configuration section](../configuration/core-datapool-aggregate-tuning.md). 
