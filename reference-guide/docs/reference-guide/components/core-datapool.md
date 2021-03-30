## Datapool Core

### Purpose

The component is responsible for maintaining and storing the consistent state of the datapool
core concept of Business Data Entry.

The component receives all commands and emits events, if changes are performed on underlying entities.
The event stream is used to store all changes (purely event-sourced) and should be used by all other
parties interested in changes.

### Configuration

#### Component activation

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
@EnableDataPool
class MyConfiguration
```

#### Revision-Aware Projection

The in-memory data entry projection is supporting revision-aware projection queries. To activate this, you need
to activate the correlation of revision attributes between your data entries commands and the data entry events. To do so, please
activate the correlation provider by putting the following code snippet in the application containing the Datapool Core Component:

```kotlin
@Configuration
@EnableDataPool
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
latter will deliver revision information in query results. The use of `RevisionAwareQueryGateway` will allow to query for specific revisions in the data entry
projection, see documentation of `axon-gateway-extension` project.
