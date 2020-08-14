package io.holunda.camunda.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.process.instance.EndProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.StartProcessInstanceCommand
import io.holunda.camunda.taskpool.collector.sourceReference
import io.holunda.camunda.taskpool.collector.task.TaskEventCollectorService
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects Camunda events and Camunda historic events and emits Process Instance Commands
 * @see [org.camunda.bpm.engine.impl.history.event.HistoryEventTypes]
 */
@Component
class ProcessInstanceEventCollectorService(
  private val collectorProperties: TaskCollectorProperties
) {

  companion object {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  /**
   * Fires create process instance command.
   */
  @Order(TaskEventCollectorService.ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('start')")
  fun create(processInstance: HistoricProcessInstanceEventEntity): StartProcessInstanceCommand = processInstance
    .toStartProcessInstanceCommand(collectorProperties.enricher.applicationName)

  /**
   * Fires end process instance command.
   */
  @Order(TaskEventCollectorService.ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('end')")
  fun end(processInstance: HistoricProcessInstanceEventEntity): EndProcessInstanceCommand = processInstance
    .toEndProcessInstanceCommand(collectorProperties.enricher.applicationName)
}

private fun HistoricProcessInstanceEventEntity.toStartProcessInstanceCommand(applicationName: String) = StartProcessInstanceCommand(
  sourceReference = this.sourceReference(applicationName),
  processInstanceId = this.processInstanceId,
  businessKey = this.businessKey,
  startActivityId = this.startActivityId,
  startUserId = this.startUserId,
  superInstanceId = when {
    this.superProcessInstanceId != null -> this.superProcessInstanceId
    this.superCaseInstanceId != null -> this.superCaseInstanceId
    else -> null
  }
)

private fun HistoricProcessInstanceEventEntity.toEndProcessInstanceCommand(applicationName: String) = EndProcessInstanceCommand(
  sourceReference = this.sourceReference(applicationName),
  processInstanceId = this.processInstanceId,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  deleteReason = this.deleteReason,
  superInstanceId = when {
    this.superProcessInstanceId != null -> this.superProcessInstanceId
    this.superCaseInstanceId != null -> this.superCaseInstanceId
    else -> null
  }
)

