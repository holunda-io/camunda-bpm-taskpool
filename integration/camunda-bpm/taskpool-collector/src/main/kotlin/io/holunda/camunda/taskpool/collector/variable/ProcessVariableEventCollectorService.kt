package io.holunda.camunda.taskpool.collector.variable

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.collector.sourceReference
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 *
 *
 * <code>
 *  VARIABLE_INSTANCE_CREATE("variable-instance", "create"),
 *  VARIABLE_INSTANCE_UPDATE("variable-instance", "update"),
 *  VARIABLE_INSTANCE_MIGRATE("variable-instance", "migrate"),
 *  VARIABLE_INSTANCE_UPDATE_DETAIL("variable-instance", "update-detail"),
 *  VARIABLE_INSTANCE_DELETE("variable-instance", "delete"),
 * </code>
 */
@Component
class ProcessVariableEventCollectorService(
  private val collectorProperties: TaskCollectorProperties,
  private val repositoryService: RepositoryService
) {

  companion object : KLogging() {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  /**
   * Fires create process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_CREATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('create')")
  fun create(variableEvent: HistoricVariableUpdateEventEntity) {
    val sourceReference = variableEvent.sourceReference(repositoryService, collectorProperties.applicationName)
    logger.debug { "Created variable ${variableEvent.variableName} for source ref: $sourceReference" }
  }

  /**
   * Fires update process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('update')")
  fun update(variableEvent: HistoricVariableUpdateEventEntity) {
    logger.debug { "Update variable $variableEvent" }
  }

  /**
   * Fires update detail process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('update-detail')")
  fun updateDetail(variableEvent: HistoricDetailVariableInstanceUpdateEntity) {
    logger.debug { "Created variable $variableEvent" }
  }

  /**
   * Fires delete process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_DELETE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('delete')")
  fun delete(variableEvent: HistoricVariableUpdateEventEntity) {
    logger.debug { "Created variable $variableEvent" }
  }

  /**
   * Fires migrate process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('migrate')")
  fun migrate(variableEvent: HistoricVariableUpdateEventEntity) {
    logger.debug { "Update variable $variableEvent" }
  }

}
