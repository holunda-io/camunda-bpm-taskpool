package io.holunda.camunda.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.camunda.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.sender.task.EngineTaskCommandSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Task command processor service.
 * This component is a sink of all engine task commands collected by the [TaskEventCollectorService]
 * and will enrich all commands which are implementing [TaskIdentityWithPayloadAndCorrelations] interface
 * and pass it over to sender, responsible for accumulation (reducing the amount of commands) and sending.
 */
@Component
class TaskCommandProcessor(
  private val engineTaskCommandSender: EngineTaskCommandSender,
  private val enricher: VariablesEnricher,
  private val collectorProperties: CamundaTaskpoolCollectorProperties
) {
  private val logger: Logger = LoggerFactory.getLogger(TaskCommandProcessor::class.java)

  @EventListener
  fun process(command: EngineTaskCommand) {
    if (collectorProperties.task.enabled) {
      // enrich and send
      engineTaskCommandSender.send(
        when (command) {
          is TaskIdentityWithPayloadAndCorrelations -> enricher.enrich(command)
          else -> command
        }
      )
    } else {
      logger.debug("COLLECTOR-008: Task command collecting is disabled by property, would have enriched and sent command $command.")
    }
  }
}
