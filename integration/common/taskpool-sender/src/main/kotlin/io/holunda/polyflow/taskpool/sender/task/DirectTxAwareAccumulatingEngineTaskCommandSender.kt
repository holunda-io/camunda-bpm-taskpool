package io.holunda.polyflow.taskpool.sender.task

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import io.holunda.polyflow.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator

private val logger = KotlinLogging.logger {}

/**
 * Accumulates commands and sends them directly in the same transaction.
 */
class DirectTxAwareAccumulatingEngineTaskCommandSender(
  private val commandListGateway: CommandListGateway,
  engineTaskCommandAccumulator: EngineTaskCommandAccumulator,
  senderProperties: SenderProperties
) : TxAwareAccumulatingEngineTaskCommandSender(
  engineTaskCommandAccumulator = engineTaskCommandAccumulator,
  senderProperties = senderProperties
) {

  override fun send() {
    // iterate over messages and send them
    taskCommands.get().forEach { (taskId, taskCommands) ->
      val accumulatorName = engineTaskCommandAccumulator::class.simpleName
      logger.debug { "SENDER-005: Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName" }
      val commands = engineTaskCommandAccumulator.invoke(taskCommands)
      // handle messages for every task
      if (senderProperties.enabled && senderProperties.task.enabled) {
        commandListGateway.sendToGateway(commands)
        logger.trace {
          "SENDER-TRACE: sending commands for task [${commands.first().id}]: " + commands.joinToString(
            ", ",
            "'",
            "'",
            -1,
            "..."
          ) { it.eventName }
        }
      } else {
        logger.debug { "SENDER-004: Process task sending is disabled by property. Would have sent $commands." }
      }
    }
  }
}
