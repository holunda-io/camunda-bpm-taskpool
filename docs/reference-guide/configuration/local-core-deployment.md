As described in [Distribution using Axon Server (core component as part of process engine)](../../introduction/deployment.md#distribution-using-axon-server-core-component-as-part-of-process-engine) 
you might want to deploy your Polyflow Core Components (Taskpool Core and Datapool Core) inside your process application. If you are doing so,
you should there are two important decisions to make:

* How the Camunda transaction is related to Transaction (Unit of Work) or Polyflow?
* How to isolate Polyflow Core components from each other if deployed multiple times?

## Transactional support of integration components

The integration components support different transactional behavior inside the command sender components. To be more precise, 
after the task collector has collected the commands from the integration points with Camunda, you can set up if the data is passed 
to the Command Dispatching component (command bus) inside the same transaction or in a separate transaction.

The relevant property to set this up is `polyflow.integration.sender.task.send-within-transaction`, like you can see in the following example: 

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

If set to true, the transaction will be shared between the Camunda Task Lifecycle and command dispatching components. The commands are passed to
a special `CommandListGateway` responsible for sending commands one by one. This component allows to integrate a success and failure handlers to 
react to command sending and any failures there. To do so, you need to implement two interfaces and provide bean factories for them:

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
By doing so, you can propagate the exception and prevent the initial transaction from commit, if something goes wrong during command dispatch.

Another approach for dealing with errors is to minimize their occurrence, by deploying the Core Components inside the same deployment unit as the 
process engine itself. To demonstrate this scenario, we created a scenario in Polyflow examples [Distributed with Axon Server Events Only](../../examples/scenarios/distributed-axon-server-local.md).

By using this deployment strategy, ever process engine deployment includes the [Core components](../components/core-taskpool.md) and are taking
care of maintaining their state and receiving commands from the integration components. By doing so, you preserve a mean of locality of the tasks
originated in a process engine.

## Isolating Polyflow Components

One of the problems that occurs if you use this deployment strategy with Axon Server is that you will get multiple Command Handlers in runtime
which are capable of receiving Engine Task Commands. A good way to solve this problem is to prevent Polyflow from registering the Command Handlers 
in Axon Server.

Since we are using the `Axon-Gateway-Extension` library in the Polyflow, you can make use of the `DispatchAwareCommandBus` by configuring the 
following properties, which limit the registration of Polyflow Command Handlers in Axon Server:

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

By doing so, the Polyflow Command Handlers (parts of the Core Components) are registered on the local segmet of the command bus only and don't 
interfere with each other.