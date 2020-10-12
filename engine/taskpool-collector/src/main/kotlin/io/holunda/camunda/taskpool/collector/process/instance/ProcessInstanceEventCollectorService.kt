package io.holunda.camunda.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.collector.sourceReference
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.history.HistoricProcessInstance
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
  private val collectorProperties: TaskCollectorProperties,
  private val repositoryService: RepositoryService
) {

  companion object : KLogging() {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }


  /**
   * Fires create process instance command.
   * See [HistoryEventTypes.PROCESS_INSTANCE_START]
   */
  @Order(ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('start')")
  fun create(processInstance: HistoricProcessInstanceEventEntity): StartProcessInstanceCommand = processInstance
    .toStartProcessInstanceCommand(repositoryService, collectorProperties.enricher.applicationName)

  /**
   * Fires update process instance command.
   * See [HistoryEventTypes.PROCESS_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('update')")
  fun update(processInstance: HistoricProcessInstanceEventEntity): UpdateProcessInstanceCommand? = when (processInstance.state) {
    // suspend
    HistoricProcessInstance.STATE_SUSPENDED -> processInstance.toSuspendProcessInstanceCommand(repositoryService, collectorProperties.enricher.applicationName)
    // resume
    HistoricProcessInstance.STATE_ACTIVE -> processInstance.toResumeProcessInstanceCommand(repositoryService, collectorProperties.enricher.applicationName)
    else -> {
      logger.debug { "Unknown update process instance event received $processInstance" }
      null
    }
  }


  /**
   * Fires end process instance command.
   * See [HistoryEventTypes.PROCESS_INSTANCE_END]
   */
  @Order(ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('end')")
  fun end(processInstance: HistoricProcessInstanceEventEntity): EndProcessInstanceCommand? = when (processInstance.state) {
    // cancel
    HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED -> processInstance.toCancelProcessInstanceCommand(repositoryService, collectorProperties.enricher.applicationName)
    // finish
    HistoricProcessInstance.STATE_INTERNALLY_TERMINATED -> processInstance.toFinishProcessInstanceCommand(repositoryService, collectorProperties.enricher.applicationName)
    else -> {
      logger.debug { "Unknown end process instance event received $processInstance" }
      null
    }
  }

}

private fun HistoricProcessInstanceEventEntity.toStartProcessInstanceCommand(repositoryService: RepositoryService, applicationName: String) = StartProcessInstanceCommand(
  sourceReference = this.sourceReference(repositoryService, applicationName),
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

private fun HistoricProcessInstanceEventEntity.toFinishProcessInstanceCommand(repositoryService: RepositoryService, applicationName: String) = FinishProcessInstanceCommand(
  sourceReference = this.sourceReference(repositoryService, applicationName),
  processInstanceId = this.processInstanceId,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  superInstanceId = when {
    this.superProcessInstanceId != null -> this.superProcessInstanceId
    this.superCaseInstanceId != null -> this.superCaseInstanceId
    else -> null
  }
)

private fun HistoricProcessInstanceEventEntity.toCancelProcessInstanceCommand(repositoryService: RepositoryService, applicationName: String) = CancelProcessInstanceCommand(
  sourceReference = this.sourceReference(repositoryService, applicationName),
  processInstanceId = this.processInstanceId,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  superInstanceId = when {
    this.superProcessInstanceId != null -> this.superProcessInstanceId
    this.superCaseInstanceId != null -> this.superCaseInstanceId
    else -> null
  },
  deleteReason = this.deleteReason
)


private fun HistoricProcessInstanceEventEntity.toSuspendProcessInstanceCommand(repositoryService: RepositoryService, applicationName: String) = SuspendProcessInstanceCommand(
  sourceReference = this.sourceReference(repositoryService, applicationName),
  processInstanceId = this.processInstanceId,
)


private fun HistoricProcessInstanceEventEntity.toResumeProcessInstanceCommand(repositoryService: RepositoryService, applicationName: String) = ResumeProcessInstanceCommand(
  sourceReference = this.sourceReference(repositoryService, applicationName),
  processInstanceId = this.processInstanceId,
)
