package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Wraps command gateway and makes it configurable using collector properties.
 */
@Component
open class CommandGatewayProxy(
  private val gateway: CommandGateway,
  private val properties: TaskCollectorProperties
) {
  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  /**
   * Sends only if the sender property is enabled.
   */
  open fun send(command: Any) {

    if (properties.sender.enabled) {
      gateway.send<Any, Any?>(command) { m, r ->
        if (r.isExceptional) {
          logger.error("SENDER-004: Exception sending command $m, ${r.exceptionResult()}")
        } else {
          logger.debug("SENDER-004: Successfully submitted command $m, $r")
        }

      }
    } else {
      logger.debug("SENDER-003: Would have sent command $command")
    }
  }
}
