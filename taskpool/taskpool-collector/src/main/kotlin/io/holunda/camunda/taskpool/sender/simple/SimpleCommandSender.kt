package io.holunda.camunda.taskpool.sender.simple

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.EnrichedEngineTaskCommand
import io.holunda.camunda.taskpool.enricher.VariablesEnricher
import io.holunda.camunda.taskpool.sender.CommandSender
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SimpleCommandSender(
  private val gateway: CommandGateway,
  private val properties: TaskCollectorProperties,
  private val enricher: VariablesEnricher
) : CommandSender {

  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  override fun send(command: Any) {

    // Enrich, if possible.
    val payload = when (command) {
      is EnrichedEngineTaskCommand -> enricher.enrich(command)
      else -> command
    }

    if (properties.sender.enabled) {
      gateway.send<Any, Any?>(payload) { m, r ->
        if (r.isExceptional) {
          logger.error("SENDER-004: Exception sending command $m, ${r.exceptionResult()}")
        } else {
          logger.debug("SENDER-004: Successfully submitted command $m, $r")
        }

      }
    } else {
      logger.debug("SENDER-003: Would have sent command $payload")
    }
  }

}
