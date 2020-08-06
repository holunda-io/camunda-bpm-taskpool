package io.holunda.camunda.taskpool.collector

import mu.KLogging
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects Camunda events and Camunda historic events and emits Process Instance Commands
 * @see [org.camunda.bpm.engine.impl.history.event.HistoryEventTypes]
 */
@Component
class ProcessInstanceEventCollectorService {

  companion object: KLogging() {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  /**
   * Fires create process instance command.
   */
  @Order(TaskEventCollectorService.ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('start')")
  fun create(processInstance: HistoricProcessInstanceEventEntity) {
    logger.info { "CREATE INSTANCE" }
    // TODO
  }

  /**
   * Fires end process instance command.
   */
  @Order(TaskEventCollectorService.ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('end')")
  fun end(processInstance: HistoricProcessInstanceEventEntity) {
    logger.info { "END INSTANCE" }
    // TODO
  }

}
