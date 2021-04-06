package io.holunda.camunda.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.sourceReference
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
  private val collectorProperties: CamundaTaskpoolCollectorProperties,
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
  fun create(processInstance: HistoricProcessInstanceEventEntity): StartProcessInstanceCommand =
    StartProcessInstanceCommand(
      sourceReference = processInstance.sourceReference(repositoryService, collectorProperties.applicationName),
      processInstanceId = processInstance.processInstanceId,
      businessKey = processInstance.businessKey,
      startActivityId = processInstance.startActivityId,
      startUserId = processInstance.startUserId,
      superInstanceId = when {
        processInstance.superProcessInstanceId != null -> processInstance.superProcessInstanceId
        processInstance.superCaseInstanceId != null -> processInstance.superCaseInstanceId
        else -> null
      }
    )

  /**
   * Fires update process instance command.
   * See [HistoryEventTypes.PROCESS_INSTANCE_UPDATE]
   */
  @Order(ORDER)
  @EventListener(condition = "#processInstance.eventType.equals('update')")
  fun update(processInstance: HistoricProcessInstanceEventEntity): UpdateProcessInstanceCommand? = when (processInstance.state) {
    // suspend
    HistoricProcessInstance.STATE_SUSPENDED ->
      SuspendProcessInstanceCommand(
        sourceReference = processInstance.sourceReference(repositoryService, collectorProperties.applicationName),
        processInstanceId = processInstance.processInstanceId,
      )
    // resume
    HistoricProcessInstance.STATE_ACTIVE ->
      ResumeProcessInstanceCommand(
        sourceReference = processInstance.sourceReference(repositoryService, collectorProperties.applicationName),
        processInstanceId = processInstance.processInstanceId,
      )
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
    HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED ->
      CancelProcessInstanceCommand(
        sourceReference = processInstance.sourceReference(repositoryService, collectorProperties.applicationName),
        processInstanceId = processInstance.processInstanceId,
        businessKey = processInstance.businessKey,
        endActivityId = processInstance.endActivityId,
        superInstanceId = when {
          processInstance.superProcessInstanceId != null -> processInstance.superProcessInstanceId
          processInstance.superCaseInstanceId != null -> processInstance.superCaseInstanceId
          else -> null
        },
        deleteReason = processInstance.deleteReason
      )
    // finish
    HistoricProcessInstance.STATE_INTERNALLY_TERMINATED ->
      FinishProcessInstanceCommand(
        sourceReference = processInstance.sourceReference(repositoryService, collectorProperties.applicationName),
        processInstanceId = processInstance.processInstanceId,
        businessKey = processInstance.businessKey,
        endActivityId = processInstance.endActivityId,
        superInstanceId = when {
          processInstance.superProcessInstanceId != null -> processInstance.superProcessInstanceId
          processInstance.superCaseInstanceId != null -> processInstance.superCaseInstanceId
          else -> null
        }
      )
    else -> {
      logger.debug { "Unknown end process instance event received $processInstance" }
      null
    }
  }
}
