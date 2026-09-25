As described in [Distribution using Axon Server (core component as part of process engine)](../../introduction/deployment.md#distribution-using-axon-server-core-component-as-part-of-process-engine) 
you might want to deploy your Polyflow Core Components (Taskpool Core and Datapool Core) inside your process application. If you are doing so,
there are two important decisions to make:

* How is the Camunda transaction related to Polyflow's transaction (Unit of Work)?
* How to isolate Polyflow Core components from each other if deployed multiple times?

## Transactional support of integration components

The integration components support different transactional behaviour in their command senders. After the task collector
collects commands from Camunda integration points, you can choose whether to pass them to the command-dispatching component
(command bus) in the same transaction or in a separate transaction.

Use `polyflow.integration.sender.task.send-within-transaction` to configure this behaviour:

```yaml

polyflow:
  integration:
    sender:
      enabled: true
      task:
        enabled: true
        type: tx
        send-within-transaction: true 

```

When set to `true`, the Camunda task lifecycle and command-dispatching components share a transaction. Commands are passed to
a `CommandListGateway`, which sends them one at a time. You can integrate success and failure handlers to
respond to command outcomes. To do so, implement the two interfaces and provide beans for them:

```kotlin

  @Bean
  @Primary
  fun myCommandSuccessHandler() = object : CommandErrorHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      logger.trace { "Everything went smooth" }
    }
  }

  /**
   * 
   */
  @Bean
  @Primary
  fun myCommandErrorHandler() = object : CommandSuccessHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      throw IllegalStateException("Something bad happened")
    }
  }

```
This lets you propagate an exception and prevent the initial transaction from committing if command dispatch fails.

Another way to minimise errors is to deploy Core components in the same deployment unit as the
process engine. The Polyflow examples demonstrate this in [Distributed with Axon Server Events Only](../../examples/scenarios/distributed-axon-server-local.md).

With this deployment strategy, every process-engine deployment includes the [Core components](../components/core-taskpool.md), which
maintain their state and receive commands from the integration components. This preserves locality for tasks
originating in a process engine.

## Isolating Polyflow Components

With this deployment strategy and Axon Server, multiple command handlers capable of receiving Engine Task Commands run at the same time.
Prevent Polyflow from registering these command handlers in Axon Server to avoid the conflict.

Because Polyflow uses the `Axon-Gateway-Extension` library, you can configure `DispatchAwareCommandBus` with the
following properties to limit registration of Polyflow command handlers in Axon Server:

```yaml

axon-gateway:
  command:
    dispatch-aware:
      enabled: true
      strategy:
        exclude-command-packages:
          - io.holunda.camunda.taskpool.api
          - io.holunda.camunda.datapool.api

```

As a result, the Polyflow command handlers (part of the Core components) are registered only on the command bus's local segment and do not
interfere with one another.
