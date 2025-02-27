### Purpose

Taskpool sender is a component usually deployed as a part of the process application that
is responsible for sending tasks collected e.G. by [Camunda Taskpool Collector](camunda-taskpool-collector.md) to [Taskpool Core Components](core-taskpool.md)
as commands.

### Features

* Allows fine-grained control of transactional behaviour during command sending
* Allows to integrate custom success and error handling

### Usage and configuration

!!! note
    If you are using `polyflow-camunda-bpm-collector` component, the sender component is included and activated by default, and you can skip this step.

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-taskpool-sender</artifactId>
  <version>${camunda-taskpool.version}</version>
</dependency>
```

Then activate the taskpool sender by providing the annotation on any Spring Configuration:

```java

@Configuration
@EnableTaskpoolSender
class MyDataEntryCollectorConfiguration {

}
```

In order to control sending of commands to command sender, the command sender activation property
`polyflow.integration.sender.task.enabled` is available. If disabled, the command sender
will log any command instead of aggregating sending it to the command gateway.

### Command sender types

Out of the box, Polyflow supplies several command senders to match your deployment scenario. The property 
`polyflow.integration.sender.task.type`  is used to switch between different commands senders.


| Sender type          | Property value | Description                                                                                                 |
|----------------------|----------------|-------------------------------------------------------------------------------------------------------------|
| Simple               | simple         | Simple command sender, used to send every command directly to Command Bus.                                  |
| Transactional Direct | tx             | Transactional accumulating command sender, sending accumulated commands along with the running transaction. |
| Transactional Job    | txjob          | Transactional accumulating command sender, writing a Camunda job to send the commands in a new transaction. |
| Custom               | custom         | Setting to provide your own sender implementation                                                           |


The default provided command sender (type: `tx`) is collects all task commands during one transaction, group them by task id
and accumulates the commands to the minimum reflecting the intent(s) of the task operation(s). It uses Axon Command Bus (encapsulated
by the `AxonCommandListGateway` for sending the result over to the Axon command gateway.

!!! note
    If you want to implement a custom command sending, please provide your own implementation of the interface `EngineTaskCommandSender`
    (register a Spring Component of the type) and set the property `polyflow.integration.sender.task.type` to `custom`.


### Command aggregation

The Spring event listeners receiving events from the Camunda Engine plugin are called before the engine commits the transaction.
Since all processing inside collector component and enricher is performed synchronously, the sender must waits until transaction to
be successfully committed before sending any commands to the Command Gateway. Otherwise, on any error
the transaction would be rolled-back and the command would create an inconsistency between the taskpool and the engine.

Depending on your deployment scenario, you may want to control the exact point in time, when the commands are sent to command gateway.
The property `polyflow.integration.sender.task.send-within-transaction` is designed to influence this. If set to `true`, the commands
are accumulated _before_ the process engine transaction is committed, otherwise commands are sent _after_ the process engine transaction is committed.

!!! warning
      Never send commands over **remote** messaging before the transaction is committed, since you may produce unexpected results if Camunda fails
      to commit the transaction.

If commands are delivered to a local component (this is the case if taskpool core is deployed in the same deployment as collector and sender components),
the sending transaction is spanned across the taskpool core component. In particular, this means that the command dispatch and emission of events
are happening inside the same transaction (unit of work). The default `Transactional Direct` command sender will send the commands inside this Unit of Work.
The `Transactional Job` command sender will write a Camunda Job and finish current Unit of Work without sending anything and rely on Camunda Job Worker to
perform the transmission of the commands to the Command Bus. This sender is in particular helpful, if you need to finish 

#### Serialization of payload

By default, the data entry sender will serialize payload of the `DataEntry` into a JSON-Map structure, in order to be received by projections (Data Pool View)
and storage of it, independent of the classes which might be not on the classpath of the projection (generic structure instead of a typed Java object structure).
This serialization can be disabled by the sender property `polyflow.integration.sender.task.serialize-payload=false`.


#### Handling command transmission

The commands sent via gateway (e.g. `AxonCommandListGateway`) are received by Command Handlers. The latter may accept or reject commands, depending
on the state of the aggregate and other components. The `AxonCommandListGateway` is informed about the command outcome. By default, it will log the outcome
to console (success is logged in `DEBUG` log level, errors are using `ERROR` log level).

In some situations it is required to take care of command outcome. A prominent example is to include a metric for command dispatching errors into monitoring.
For doing so, it is possible to provide own handlers for success and error command outcome. For this purpose, please provide a Spring Bean implementing
the `CommandSuccessHandler`and `CommandErrorHandler` accordingly.

Here is an example, how such a handler may look like:

```kotlin
@Bean
@Primary
fun taskCommandErrorHandler(): CommandErrorHandler = object : LoggingTaskCommandErrorHandler(logger) {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      logger.info { "<--------- CUSTOM ERROR HANDLER REPORT --------->" }
      super.apply(commandMessage, commandResultMessage)
      logger.info { "<------------------- END ----------------------->" }
    }
  }
```

### Message codes

> Please note that the logger root hierarchy is `io.holunda.camunda.taskpool.sender`

| Message Code | Severity | Logger*               | Description                                                                                        | Meaning                                |                
|--------------|----------|:----------------------|:---------------------------------------------------------------------------------------------------|:---------------------------------------|
| `SENDER-001` | `DEBUG`  | `.gateway`            | Sending command over gateway disabled by property. Would have sent command `payload`.              | Sending of any commands is disabled.   |   
| `SENDER-002` | `DEBUG`  | `.gateway`            | Successfully submitted command `payload`.                                                          | Logging the successfully sent command. | 
| `SENDER-003` | `ERROR`  | `.gateway`            | Sending command $commandMessage resulted in error                                                  | Error sending command.                 |  
| `SENDER-004` | `DEBUG`  | `.task`               | Process task sending is disabled by property. Would have sent $command.                            |                                        |                                        |
| `SENDER-005` | `DEBUG`  | `.task`               | Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName |                                        |  
| `SENDER-006` | `DEBUG`  | `.task`               | Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName |                                        | 
| `SENDER-007` | `DEBUG`  | `.process.definition` | Process definition sending is disabled by property. Would have sent $command.                      |                                        | 
| `SENDER-007` | `DEBUG`  | `.process.instance`   | Process instance sending is disabled by property. Would have sent $command.                        |                                        | 
| `SENDER-009` | `DEBUG`  | `.process.variable`   | Process variable sending is disabled by property. Would have sent $command.                        |                                        | 
| `SENDER-011` | `INFO`   |                       | Taskpool task commands will be distributed over command bus.                                       |                                        |
| `SENDER-012` | `INFO`   |                       | Taskpool task command distribution is disabled by property.                                        |                                        |
| `SENDER-013` | `INFO`   |                       | Taskpool process definition commands will be distributed over command bus.                         |                                        |
| `SENDER-014` | `INFO`   |                       | Taskpool process definition command distribution is disabled by property.                          |                                        |
| `SENDER-015` | `INFO`   |                       | Taskpool process instance commands will be distributed over command bus.                           |                                        |
| `SENDER-016` | `INFO`   |                       | Taskpool process instance command distribution is disabled by property.                            |                                        |
| `SENDER-017` | `INFO`   |                       | Taskpool process variable commands will be distributed over command bus.                           |                                        |
| `SENDER-018` | `INFO`   |                       | Taskpool process variable command distribution is disabled by property.                            |                                        |


