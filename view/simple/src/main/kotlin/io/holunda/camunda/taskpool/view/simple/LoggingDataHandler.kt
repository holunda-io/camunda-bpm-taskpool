package io.holunda.camunda.taskpool.view.simple

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
class LoggingDataHandler {

  companion object : KLogging()

  @EventHandler
  fun on(event: DataEntryCreatedEvent) {
    logger.info { "Business data entry created $event" }
  }
}
