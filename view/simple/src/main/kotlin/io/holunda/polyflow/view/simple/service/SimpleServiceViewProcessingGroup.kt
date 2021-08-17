package io.holunda.polyflow.view.simple.service

import mu.KLogging
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.eventhandling.TrackingEventProcessor
import org.springframework.stereotype.Component

@Component
class SimpleServiceViewProcessingGroup(
  private val configuration: EventProcessingConfiguration
) {


  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.simple"
  }

  /**
   * Configure to run a event replay to fill the simple task view with events on start-up.
   */
  fun restore() {
    this.configuration
      .eventProcessorByProcessingGroup(PROCESSING_GROUP, TrackingEventProcessor::class.java)
      .ifPresent {
        SimpleTaskPoolService.logger.info { "VIEW-SIMPLE-002: Starting simple view event replay." }
        it.shutDown()
        it.resetTokens()
        it.start()
      }
  }

}

