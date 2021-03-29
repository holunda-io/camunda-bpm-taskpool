package io.holunda.camunda.taskpool.collector.task

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.collector.formKey
import io.holunda.camunda.taskpool.collector.sourceReference
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity
import org.camunda.bpm.engine.task.IdentityLinkType
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects Camunda events and Camunda historic events (event listener order is {@link TaskEventCollectorService#ORDER}) and emits Commands
 */
@Component
class TaskEventCollectorService(
  private val collectorProperties: TaskCollectorProperties,
  private val repositoryService: RepositoryService
) {

  private val logger = LoggerFactory.getLogger(TaskEventCollectorService::class.java)

  companion object {
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
  fun complete(task: DelegateTask) =
    CompleteTaskCommand(
      id = task.id,
      eventName = task.eventName
    )

  /**
   * Fires assign command.
   */
  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('assignment')")
  fun assign(task: DelegateTask) =
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
  fun delete(task: DelegateTask) =
    DeleteTaskCommand(
      id = task.id,
      deleteReason = task.deleteReason,
      eventName = task.eventName
    )

  /**
   * Fires update command.
   * The following attributes of the update event are skipped:
   * <ul>
   *     <li>parentTaskId</li>
   * </ul>
   */
  @Order(ORDER)
  @EventListener()
  fun update(changeEvent: HistoricTaskInstanceEventEntity): UpdateAttributeTaskCommand? =
    when (changeEvent.eventType) {
      "update" ->
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
      else -> null
    }

  /**
   * Fires update assignment command.
   */
  @Order(ORDER)
  @EventListener
  fun update(changeEvent: HistoricIdentityLinkLogEventEntity): UpdateAssignmentTaskCommand? =
    when (changeEvent.operationType) {
      "add" -> when {
        changeEvent.taskId != null && changeEvent.userId != null -> AddCandidateUsersCommand(
          id = changeEvent.taskId,
          candidateUsers = setOf(changeEvent.userId))
        changeEvent.taskId != null && changeEvent.groupId != null -> AddCandidateGroupsCommand(
          id = changeEvent.taskId,
          candidateGroups = setOf(changeEvent.groupId))
        else -> {
          logger.warn("Received unexpected identity link historic update event ${changeEvent.type} ${changeEvent.operationType} ${changeEvent.eventType} on ${changeEvent.taskId}")
          null
        }
      }
      "delete" -> when {
        changeEvent.taskId != null && changeEvent.userId != null -> DeleteCandidateUsersCommand(
          id = changeEvent.taskId,
          candidateUsers = setOf(changeEvent.userId))
        changeEvent.taskId != null && changeEvent.groupId != null -> DeleteCandidateGroupsCommand(
          id = changeEvent.taskId,
          candidateGroups = setOf(changeEvent.groupId))
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

