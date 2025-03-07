package io.holunda.polyflow.taskpool.collector.task

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

/**
 * Task command processor service.
 * This component is a sink of all engine task commands collected by the [TaskEventCollectorService]
 * and will enrich all commands which are implementing [TaskIdentityWithPayloadAndCorrelations] interface
 * and pass it over to sender, responsible for accumulation (reducing the amount of commands) and sending.
 */
class TaskCommandProcessor(
  private val engineTaskCommandSender: EngineTaskCommandSender,
  private val enricher: VariablesEnricher,
  private val taskAssigner: TaskAssigner
) {

  /**
   * Receives engine task command and delivers it to the sender.
   * @param command engine task command delivered by Spring Eventing.
   */
  @EventListener
  fun process(command: EngineTaskCommand) {
    when (command) {
      is TaskIdentityWithPayloadAndCorrelations -> enricher.enrich(command)
      else -> command
    }.let {
      taskAssigner.setAssignment(it)
    }.also { commandToSend ->
      if (logger.isTraceEnabled()) {
        logger.trace {"COLLECTOR-008: Sending engine task command: $commandToSend." }
      }
      // enrich and send
      engineTaskCommandSender.send(commandToSend)
    }
  }
}
