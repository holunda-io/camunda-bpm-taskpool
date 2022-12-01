package io.holunda.polyflow.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.*
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.IdentityLinkType
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects Camunda events and Camunda historic events (event listener order is {@link TaskEventCollectorService#ORDER}) and emits Commands
 */
@Component
class TaskEventCollectorService(
  private val collectorProperties: CamundaTaskpoolCollectorProperties,
  private val repositoryService: RepositoryService
) {


  companion object : KLogging() {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  /**
   * Fires create command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('create')")
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
      sourceReference = task.sourceReference(collectorProperties.applicationName)
    )

  /**
   * Fires complete.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('complete')")
  fun complete(task: DelegateTask): CompleteTaskCommand =
    CompleteTaskCommand(
      id = task.id,
      eventName = task.eventName
    )

  /**
   * Fires assign command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('assignment')")
  fun assign(task: DelegateTask): AssignTaskCommand =
    AssignTaskCommand(
      id = task.id,
      assignee = task.assignee,
      eventName = task.eventName
    )


  /**
   * Fires delete command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('delete')")
  fun delete(task: DelegateTask): DeleteTaskCommand =
    DeleteTaskCommand(
      id = task.id,
      deleteReason = task.deleteReason,
      eventName = task.eventName
    )

  /**
   * Tracing of collector.
   */
  @Order(ORDER)
  @EventListener
  fun all(task: DelegateTask) {
    if (logger.isTraceEnabled) {
      logger.trace("Received " + task.eventName + " event on task with id " + task.id)
      if (task is TaskEntity) {
        logger.trace("\tProperties: {}", task.propertyChanges.keys.joinToString(","))
        logger.trace("\tIdentity links: {}", task.getIdentityLinkChanges().joinToString(","))
      }
    }
  }

  /**
   * Fires update command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('update')")
  fun update(task: DelegateTask): UpdateAttributeTaskCommand? =
    if (!collectorProperties.task.useHistoricEventCollector) {
      logger.debug("Received task update for task {}", task.id)
      if (task is TaskEntity) {
        if (task.isAssigneeChange() || !task.hasChangedProperties()) {
          // this is already handled by assignment event, or it is an empty fired during taskService call of candidate update.
          null
        } else {
          task.toUpdateCommand(collectorProperties.applicationName)
        }
      } else {
        task.toUpdateCommand(collectorProperties.applicationName)
      }
    } else {
      null
    }


  /**
   * Fires update historic command.
   * The following attributes of the update event are skipped:
   * <ul>
   *     <li>parentTaskId</li>
   * </ul>
   */
  @Order(ORDER)
  @EventListener(condition = "#changeEvent.eventType.equals('update')")
  fun update(changeEvent: HistoricTaskInstanceEventEntity): UpdateAttributeTaskCommand? =
   //  if (collectorProperties.task.useHistoricEventCollector) {
      UpdateAttributeTaskCommand(
        id = changeEvent.taskId,
        description = changeEvent.description,
        dueDate = changeEvent.dueDate,
        followUpDate = changeEvent.followUpDate,
        name = changeEvent.name,
        owner = changeEvent.owner,
        priority = changeEvent.priority,
        taskDefinitionKey = changeEvent.taskDefinitionKey,
        sourceReference = changeEvent.sourceReference(repositoryService, collectorProperties.applicationName)
      )
//    } else {
//      null
//    }

  /**
   * Fires update assignment historic command.
   */
  @Order(ORDER)
  @EventListener
  fun update(changeEvent: HistoricIdentityLinkLogEventEntity): UpdateAssignmentTaskCommand? =
    when (changeEvent.operationType) {
      "add" -> when {
        changeEvent.taskId != null && changeEvent.userId != null -> AddCandidateUsersCommand(
          id = changeEvent.taskId,
          candidateUsers = setOf(changeEvent.userId)
        )

        changeEvent.taskId != null && changeEvent.groupId != null -> AddCandidateGroupsCommand(
          id = changeEvent.taskId,
          candidateGroups = setOf(changeEvent.groupId)
        )

        else -> {
          logger.warn("Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}")
          null
        }
      }

      "delete" -> when {
        changeEvent.taskId != null && changeEvent.userId != null -> DeleteCandidateUsersCommand(
          id = changeEvent.taskId,
          candidateUsers = setOf(changeEvent.userId)
        )

        changeEvent.taskId != null && changeEvent.groupId != null -> DeleteCandidateGroupsCommand(
          id = changeEvent.taskId,
          candidateGroups = setOf(changeEvent.groupId)
        )

        else -> {
          logger.warn("Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}")
          null
        }
      }

      else -> {
        logger.warn("Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}")
        null
      }
    }
}

