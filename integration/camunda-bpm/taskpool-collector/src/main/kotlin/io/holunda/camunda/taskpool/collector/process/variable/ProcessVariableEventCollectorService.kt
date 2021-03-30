package io.holunda.camunda.taskpool.collector.process.variable

import io.holunda.camunda.taskpool.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCommand
import io.holunda.camunda.taskpool.api.process.variable.UpdateProcessVariableCommand
import io.holunda.camunda.taskpool.collector.sourceReference
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
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
  fun create(variableEvent: HistoricVariableUpdateEventEntity) {
    val sourceReference = variableEvent.sourceReference(repositoryService, collectorProperties.applicationName)
    logger.debug { "Created variable ${variableEvent.variableName} for source ref: $sourceReference" }
    logger.debug { "Create event was $variableEvent" }
  }

  /**
   * Fires update process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('update')")
  fun update(variableEvent: HistoricVariableUpdateEventEntity) {

    logger.debug { "Update event was $variableEvent" }

    val command = UpdateProcessVariableCommand(
      variableInstanceId = variableEvent.variableInstanceId,
      variableName = variableEvent.variableName,
      revision = variableEvent.revision,
      scopeActivityInstanceId = variableEvent.scopeActivityInstanceId,
      sourceReference = variableEvent.sourceReference(repositoryService, collectorProperties.applicationName),
      value = HistoricVariableInstanceEntity(variableEvent).getTypedValue(true),
    )

    logger.debug { "Command $command" }
  }

  /**
   * Fires update detail process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('update-detail')")
  fun updateDetail(variableEvent: HistoricDetailVariableInstanceUpdateEntity) {
    logger.debug { "Update detail variable $variableEvent" }
  }

  /**
   * Fires delete process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_DELETE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('delete')")
  fun delete(variableEvent: HistoricVariableUpdateEventEntity) {
    logger.debug { "Delete variable $variableEvent" }
  }

  /**
   * Fires migrate process variable command.
   * See [HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#variableEvent.eventType.equals('migrate')")
  fun migrate(variableEvent: HistoricVariableUpdateEventEntity) {
    logger.debug { "Migrate variable $variableEvent" }
  }

}

// FIXME: move to enricher
@Component
class ProcessVariableEnricherService(
  private val commandListGateway: CommandListGateway,
  private val properties: CamundaTaskpoolCollectorProperties
) {
  companion object : KLogging()

  /**
   * Reacts on incoming process variable commands.
   * @param command command about process variable to send.
   */
  @EventListener
  fun handle(command: ProcessVariableCommand) {
    if (properties.processVariable.enabled) {
      commandListGateway.sendToGateway(listOf(command))
      logger.debug { "Sending update about process variable ${command.variableName}." }
    } else {
      logger.debug { "Process variable collecting has been disabled by property, skipping ${command.variableName}." }
    }
  }
}


