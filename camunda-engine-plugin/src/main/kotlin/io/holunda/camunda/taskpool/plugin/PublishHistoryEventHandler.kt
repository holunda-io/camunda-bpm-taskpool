package io.holunda.camunda.taskpool.plugin

import org.camunda.bpm.engine.impl.history.event.HistoryEvent
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import org.springframework.context.ApplicationEventPublisher

/**
 * Publishes history events via Spring application eventing.
 */
class PublishHistoryEventHandler(private val publisher: ApplicationEventPublisher) : HistoryEventHandler {

  override fun handleEvents(historyEvents: MutableList<HistoryEvent>) {
    historyEvents.forEach { handleEvent(it) }
  }

  override fun handleEvent(historyEvent: HistoryEvent) {
    publisher.publishEvent(historyEvent)
  }

}
