package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.COMPLETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CREATE
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.EnrichedEngineTaskCommand
import io.holunda.camunda.taskpool.enricher.VariablesEnricher
import org.springframework.context.event.EventListener

/**
 * Leverages the simple task command sender, by executing it after the TX commit.
 */
class DefaultEnrichingCommandSender(
  private val commandGatewayProxy: TxAwareOrderingCommandGatewayProxy,
  private val enricher: VariablesEnricher
) : CommandSender {


  @EventListener
  override fun send(command: EngineTaskCommand) {
    // enrich before collect
    commandGatewayProxy.send(enrich(command))
  }

  /**
   * Enriches the command, if possible.
   * Currently, only CREATE and COMPLETE commands are enriched.
   */
  fun enrich(command: EngineTaskCommand) = when (command) {
    is EnrichedEngineTaskCommand -> when (command.eventName) {
      CREATE, COMPLETE -> enricher.enrich(command)
      else -> command
    }
    else -> command
  }
}
