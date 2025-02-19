---
title: Datapool Sender
pageId: engine-datapool-sender
---
### Purpose

Datapool sender is a component usually deployed as a part of the process application (but not necessary) that
is responsible for collecting the Business Data Events fired by the application in order to allow for creation of
a business data projection. In doing so, it collects and transmits it to Datapool Core.

### Features
 * Provides an API to submit arbitrary changes of business entities
 * Provides an API to track changes (aka. Audit Log)
 * Authorization on business entries
 * Transmission of business entries commands

### Usage and configuration


```xml
    <dependency>
      <groupId>io.holunda.polyflow</groupId>
      <artifactId>polyflow-datapool-sender</artifactId>
      <version>${camunda-taskpool.version}</version>
    </dependency>
```

Then activate the datapool sender by providing the annotation on any Spring Configuration:

```java

@Configuration
@EnableDataEntrySender
class MyDataEntryCollectorConfiguration {

}

```

### Command transmission

In order to control sending of commands to command gateway, the command sender activation property
`polyflow.integration.sender.data-entry.enabled` (default is `true`) is available. If disabled, the command sender
will log any command instead of sending it to the command gateway.

### Command sender types

Out of the box, Polyflow supplies two command senders to match your deployment scenario. The property
`polyflow.integration.sender.data-entry.type`  is used to switch between different commands senders.


| Sender type          | Property value | Description                                                                                                 |
|----------------------|----------------|-------------------------------------------------------------------------------------------------------------|
| Simple               | simple         | Simple command sender, used to send every command directly to Command Bus.                                  |
| Transactional Direct | tx             | Transactional accumulating command sender, sending accumulated commands along with the running transaction. |
| Custom               | custom         | Setting to provide your own sender implementation                                                           |

!!! note
       If you want to implement a custom command sending, please provide your own implementation of the interface `DataEntryCommandSender`
       (register a Spring Component of the type) and set the property `polyflow.integration.sender.data-entry.type` to `custom`.

#### Serialization of payload

By default, the data entry sender will serialize payload of the `DataEntry` into a JSON-Map structure, in order to be received by projections (Data Pool View) 
and storage of it, independent of the classes which might be not on the classpath of the projection (generic structure instead of a typed Java object structure).
This serialization can be disabled by the sender property `polyflow.integration.sender.data-entry.serialize-payload=false`. 

#### Handling command transmission

The commands sent by the `Datapool Sender` are received by Command Handlers. The latter may accept or reject commands, depending
on the state of the aggregate and other components. The `SimpleDataEntryCommandSender` is informed about the command outcome. By default, it will log the outcome
to console (success is logged in `DEBUG` log level, errors are using `ERROR` log level).

In some situations it is required to take care of command outcome. A prominent example is to include a metric for command dispatching errors into monitoring. For doing so,
it is possible to provide own handlers for success and error command outcome.

For Data Entry Command Sender (as a part of `Datapool Sender`) please provide a Spring Bean implementing the `io.holunda.polyflow.datapool.sender.DataEntryCommandSuccessHandler`
 and `io.holunda.polyflow.datapool.sender.DataEntryCommandErrorHandler` accordingly.


```kotlin
  @Bean
  @Primary
  fun dataEntryCommandSuccessHandler() = object: DataEntryCommandResultHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      // do something here
      logger.info { "Success" }
    }
  }

  @Bean
  @Primary
  fun dataEntryCommandErrorHandler() = object: DataEntryCommandErrorHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      // do something here
      logger.error { "Error" }
    }
  }
```
