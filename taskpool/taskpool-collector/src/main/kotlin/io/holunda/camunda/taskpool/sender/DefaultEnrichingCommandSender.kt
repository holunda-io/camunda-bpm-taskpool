package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
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
   * Currently, only CREATE commands is enriched.
   */
  private fun enrich(command: EngineTaskCommand): EngineTaskCommand = when (command) {
    is CreateTaskCommand -> enricher.enrich(command)
    else -> command
  }
}
