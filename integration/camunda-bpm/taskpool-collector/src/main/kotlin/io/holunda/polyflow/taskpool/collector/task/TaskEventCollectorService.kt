package io.holunda.polyflow.taskpool.collector.task

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.*
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.IdentityLinkType
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

private val logger = KotlinLogging.logger {}

/**
 * Collects Camunda events and Camunda historic events (event listener order is {@link TaskEventCollectorService#ORDER}) and emits Commands
 */
class TaskEventCollectorService(
  val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties, // must not be private to access in conditions of event handlers
  private val repositoryService: RepositoryService
) {


  companion object {
    const val NAME = "taskEventCollectorService"

    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  /**
   * Tracing of collector.
   */
  @Order(ORDER - 10)
  @EventListener
  fun all(task: DelegateTask) {
    if (logger.isTraceEnabled()) {
      logger.trace { "${"Received " + task.eventName + " event on task with id " + task.id}" }
      if (task is TaskEntity) {
        logger.trace { "${"\tProperties: {}"} ${task.propertyChanges.keys.joinToString(",")}" }
        logger.trace { "${"\tIdentity links: {}"} ${task.getIdentityLinkChanges().joinToString(",")}" }
      }
    }
  }

  /**
   * Fires create command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('create') && @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectTaskEvent('create')")
  fun create(task: DelegateTask): CreateTaskCommand =
    CreateTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId }.toSet(),
      candidateUsers = task.candidates.filter { it.userId != null && it.type == IdentityLinkType.CANDIDATE }.map { it.userId }.toSet(),
      createTime = task.createTime,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      formKey = task.formKey(),
      taskDefinitionKey = task.taskDefinitionKey,
      businessKey = task.execution.businessKey,
      sourceReference = task.sourceReference(camundaTaskpoolCollectorProperties.applicationName)
    )

  /**
   * Fires complete.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('complete') && @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectTaskEvent('complete')")
  fun complete(task: DelegateTask): CompleteTaskCommand =
    CompleteTaskCommand(
      id = task.id,
      eventName = task.eventName
    )

  /**
   * Fires assign command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('assignment') && @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectTaskEvent('assignment')")
  fun assign(task: DelegateTask) {
    // this method is intentionally empty to demonstrate that the assign event is captured.
    // we hence rely on historic identity link events to capture assignment via API and via listeners more accurately.
    // see implementation below
    if (logger.isTraceEnabled()) {
      logger.trace { "Task ${task.id} is assigned to ${task.assignee}." }
    }
  }

  /**
   * Fires delete command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('delete') && @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectTaskEvent('delete')")
  fun delete(task: DelegateTask): DeleteTaskCommand =
    DeleteTaskCommand(
      id = task.id,
      deleteReason = task.deleteReason,
      eventName = task.eventName
    )

  /**
   * Fires update command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('update') && @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectTaskEvent('update')")
  fun update(task: DelegateTask): UpdateAttributeTaskCommand? =
    if (task is TaskEntity) {
      if (task.isAssigneeChange()) {
        // this is already handled by assignment event
        null
      } else {
        task.toUpdateCommand(camundaTaskpoolCollectorProperties.applicationName)
      }
    } else {
      task.toUpdateCommand(camundaTaskpoolCollectorProperties.applicationName)
    }

  /**
   * Fires update historic attribute command.
   * This is used to detect all changes provided by the TaskListeners and collect all details to be projected
   * into the original intent.
   */
  @Order(ORDER)
  @EventListener(condition = "#changeEvent.eventType.equals('update') && @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectHistoryEvent('update')")
  fun update(changeEvent: HistoricTaskInstanceEventEntity): UpdateAttributesHistoricTaskCommand =
    UpdateAttributesHistoricTaskCommand(
      id = changeEvent.taskId,
      description = changeEvent.description,
      dueDate = changeEvent.dueDate,
      followUpDate = changeEvent.followUpDate,
      name = changeEvent.name,
      owner = changeEvent.owner,
      priority = changeEvent.priority,
      taskDefinitionKey = changeEvent.taskDefinitionKey,
      sourceReference = changeEvent.sourceReference(repositoryService, camundaTaskpoolCollectorProperties.applicationName)
    )

  /**
   * Fires update assignment historic command.
   * This is the only way to detect changes of identity links (candidate user/group change and remove).
   */
  @Order(ORDER)
  @EventListener(condition = "@taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectHistoryEvent('add-identity-link') || @taskEventCollectorService.camundaTaskpoolCollectorProperties.task.collectHistoryEvent('delete-identity-link')")
  fun update(changeEvent: HistoricIdentityLinkLogEventEntity): Any? =
    when {
      // user assignment. Is needed because the assignment out of a listener is undetected otherwise.
      changeEvent.operationType == "add" && changeEvent.type == "assignee" -> AssignTaskCommand(
        id = changeEvent.taskId,
        assignee = changeEvent.userId
      )
      // is the assignee is removed, the old value is contained in the userId, so we ignore it.
      changeEvent.operationType == "delete" && changeEvent.type == "assignee" -> AssignTaskCommand(
        id = changeEvent.taskId,
        assignee = null
      )

      changeEvent.operationType == "add" && changeEvent.type == "candidate" -> when {
        // candidate user add
        changeEvent.taskId != null && changeEvent.userId != null -> AddCandidateUsersCommand(
          id = changeEvent.taskId,
          candidateUsers = setOf(changeEvent.userId)
        )
        // candidate group add
        changeEvent.taskId != null && changeEvent.groupId != null -> AddCandidateGroupsCommand(
          id = changeEvent.taskId,
          candidateGroups = setOf(changeEvent.groupId)
        )

        else -> {
          logger.warn { "Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}" }
          null
        }
      }

      changeEvent.operationType == "delete" && changeEvent.type == "candidate" -> when {
        // candidate user delete
        changeEvent.taskId != null && changeEvent.userId != null -> DeleteCandidateUsersCommand(
          id = changeEvent.taskId,
          candidateUsers = setOf(changeEvent.userId)
        )
        // candidate group delete
        changeEvent.taskId != null && changeEvent.groupId != null -> DeleteCandidateGroupsCommand(
          id = changeEvent.taskId,
          candidateGroups = setOf(changeEvent.groupId)
        )

        else -> {
          logger.warn { "Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}" }
          null
        }
      }

      else -> {
        logger.warn { "Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}" }
        null
      }
    }
}

