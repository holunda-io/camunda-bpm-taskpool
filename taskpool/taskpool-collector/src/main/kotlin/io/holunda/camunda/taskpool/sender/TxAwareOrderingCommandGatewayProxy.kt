package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.WithTaskId
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Wraps command gateway and makes it configurable using collector properties.
 */
@Component
open class TxAwareOrderingCommandGatewayProxy(
  private val commandGatewayWrapper: AxonCommandGatewayWrapper,
  private val commandAccumulator: CommandAccumulator
) {
  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
  private val commands: ThreadLocal<MutableMap<String, MutableList<WithTaskId>>> = ThreadLocal.withInitial { mutableMapOf<String, MutableList<WithTaskId>>() }

  open fun send(command: WithTaskId) {

    // add command to list
    commands.get().getOrPut(command.id) { mutableListOf() }.add(command)
    // commands.set(commandMap)

    // register synchronization only once
    if (!registered.get()) {
      // send the result

      TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {
        /**
         * Send commands on commit only.
         */
        override fun afterCommit() {

          // iterate over messages and send them
          commands.get().forEach { (taskId: String, taskCommands: MutableList<WithTaskId>) ->
            logger.debug("SENDER-005: Handling commands for task $taskId")

            val commands = commandAccumulator.invoke(taskCommands)

            // handle messages for every task
            commands.forEach {
              commandGatewayWrapper.sendToGateway(it)
            }
          }
        }

        /**
         * Clean-up the thread on completion.
         */
        override fun afterCompletion(status: Int) {
          commands.remove()
          registered.remove()
        }
      })

      // mark as registered
      registered.set(true)
    }

  }
}

/**
 * Sends commands via AXON command gateway, only if the sender property is enabled.
 */
@Component
open class AxonCommandGatewayWrapper(
  private val commandGateway: CommandGateway,
  private val properties: TaskCollectorProperties
) {

  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  /**
   * Sends data to gateway. Ignore any errors, but log.
   */
  open fun sendToGateway(command: Any) {
    if (properties.sender.enabled) {
      commandGateway.send<Any, Any?>(command) { commandMessage, commandResultMessage ->
        if (commandResultMessage.isExceptional) {
          logger.error("SENDER-006: Sending command $commandMessage resulted in error ${commandResultMessage.exceptionResult()}")
        } else {
          logger.debug("SENDER-004: Successfully submitted command $commandMessage")
        }
      }
    } else {
      logger.debug("SENDER-003: Would have sent command $command")
    }
  }

}
