package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
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
  private val commands: ThreadLocal<MutableMap<String, MutableList<EngineTaskCommand>>> = ThreadLocal.withInitial { mutableMapOf<String, MutableList<EngineTaskCommand>>() }

  open fun send(command: EngineTaskCommand) {

    // add command to list
    commands.get().getOrPut(command.id) { mutableListOf() }.add(command)

    // register synchronization only once
    if (!registered.get()) {
      // send the result

      TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {
        /**
         * Send commands on commit only.
         */
        override fun afterCommit() {

          // iterate over messages and send them
          commands.get().forEach { (taskId: String, taskCommands: MutableList<EngineTaskCommand>) ->
            val accumulatorName = commandAccumulator::class.qualifiedName
            logger.debug("SENDER-005: Handling commands for task $taskId using command accumulator $accumulatorName")

            val commands = commandAccumulator.invoke(taskCommands)

            // handle messages for every task
            commandGatewayWrapper.sendToGateway(commands)
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
  open fun sendToGateway(commands: List<EngineTaskCommand>) {
    if (!commands.isEmpty()) {
      val nextCommand = commands.first()
      val remainingCommands = commands.subList(1, commands.size)

      if (properties.sender.enabled) {
        commandGateway.send<Any, Any?>(nextCommand) { commandMessage, commandResultMessage ->
          if (commandResultMessage.isExceptional) {
            logger.error("SENDER-006: Sending command $commandMessage resulted in error ${commandResultMessage.exceptionResult()}")
          } else {
            logger.debug("SENDER-004: Successfully submitted command $commandMessage")
          }
          sendToGateway(remainingCommands)
        }
      } else {
        logger.debug("SENDER-003: Would have sent command $nextCommand")
        sendToGateway(remainingCommands)
      }
    }
  }

}
