package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.camunda.taskpool.sender.CommandSender
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Default enricher.
 */
@Component
class TaskCommandEnricherService(
  private val commandSender: CommandSender,
  private val enricher: VariablesEnricher
) {


  @EventListener
  fun send(command: EngineTaskCommand) {
    // enrich before collect
    commandSender.send(enrich(command))
  }

  /**
   * Enriches the command, if possible.
   */
  private fun enrich(command: EngineTaskCommand): EngineTaskCommand = when (command) {
    is TaskIdentityWithPayloadAndCorrelations -> enricher.enrich(command) as EngineTaskCommand
    else -> command
  }
}
