package io.holunda.polyflow.taskpool.collector.process.variable

import io.holunda.camunda.taskpool.api.process.variable.TypedValueProcessVariableValue
import io.holunda.polyflow.taskpool.sender.process.variable.CreateSingleProcessVariableCommand
import io.holunda.polyflow.taskpool.sender.process.variable.DeleteSingleProcessVariableCommand
import io.holunda.polyflow.taskpool.sender.process.variable.UpdateSingleProcessVariableCommand
import io.holunda.polyflow.taskpool.sourceReference
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects process variable events from camunda and create corresponding process variable commands.
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
  private val collectorProperties: CamundaTaskpoolCollectorProperties,
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
  fun create(variableEvent: HistoricVariableUpdateEventEntity) =
    CreateSingleProcessVariableCommand(
      variableInstanceId = variableEvent.variableInstanceId,
      variableName = variableEvent.variableName,
      revision = variableEvent.revision,
      scopeActivityInstanceId = variableEvent.scopeActivityInstanceId,
      sourceReference = variableEvent.sourceReference(repositoryService, collectorProperties.applicationName),
      value = TypedValueProcessVariableValue(HistoricVariableInstanceEntity(variableEvent).getTypedValue(true)),
    )

  /**
   * Fires update process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('update')")
  fun update(variableEvent: HistoricVariableUpdateEventEntity) =
    UpdateSingleProcessVariableCommand(
      variableInstanceId = variableEvent.variableInstanceId,
      variableName = variableEvent.variableName,
      revision = variableEvent.revision,
      scopeActivityInstanceId = variableEvent.scopeActivityInstanceId,
      sourceReference = variableEvent.sourceReference(repositoryService, collectorProperties.applicationName),
      value = TypedValueProcessVariableValue(HistoricVariableInstanceEntity(variableEvent).getTypedValue(true)),
    )

  /**
   * Fires delete process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_DELETE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('delete')")
  fun delete(variableEvent: HistoricVariableUpdateEventEntity) =
    DeleteSingleProcessVariableCommand(
      variableInstanceId = variableEvent.variableInstanceId,
      variableName = variableEvent.variableName,
      revision = variableEvent.revision,
      scopeActivityInstanceId = variableEvent.scopeActivityInstanceId,
      sourceReference = variableEvent.sourceReference(repositoryService, collectorProperties.applicationName),
    )

  /**
   * Fires migrate process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('migrate')")
  fun migrate(variableEvent: HistoricVariableUpdateEventEntity) {
    logger.trace { "Migrate variable $variableEvent" }
  }

  /**
   * Fires update detail process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_UPDATE_DETAIL]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('update-detail')")
  fun updateDetail(variableEvent: HistoricDetailVariableInstanceUpdateEntity) {
    logger.trace { "Update detail variable $variableEvent" }
  }

}

