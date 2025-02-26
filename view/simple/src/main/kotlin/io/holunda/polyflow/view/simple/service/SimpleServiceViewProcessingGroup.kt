package io.holunda.polyflow.view.simple.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.eventhandling.TrackingEventProcessor
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Component responsible for offering replay functionality of the processor.
 */
@Component
class SimpleServiceViewProcessingGroup(
  private val configuration: EventProcessingConfiguration
) {

  companion object {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.simple"
  }

  /**
   * Configure to run a event replay to fill the simple task view with events on start-up.
   */
  fun restore() {
    this.configuration
      .eventProcessorByProcessingGroup(PROCESSING_GROUP, TrackingEventProcessor::class.java)
      .ifPresent {
        logger.info { "VIEW-SIMPLE-002: Starting simple view event replay." }
        it.shutDown()
        it.resetTokens()
        it.start()
      }
  }

}

