package io.holunda.polyflow.taskpool.sender

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.sender.api.Task
import org.springframework.context.ApplicationEventPublisher

/**
 * Emits Task Commands
 */
class GenericTaskPublisher(private val applicationEventPublisher: ApplicationEventPublisher) {
  /**
   * Fires create command.
   */
  fun create(task: Task) =
    CreateTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidateGroups,
      candidateUsers = task.candidateUsers,
      createTime = task.createTime,
      description = task.description,
      dueDate = task.dueDate,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      formKey = task.formKey,
      taskDefinitionKey = task.taskDefinitionKey,
      businessKey = task.businessKey,
      sourceReference = task.sourceReference,
      payload = task.payload
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires complete.
   */
  fun complete(taskId: String) =
    CompleteTaskCommand(
      id = taskId
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires assign command.
   */
  fun assign(taskId: String, assignee: String) =
    AssignTaskCommand(
      id = taskId,
      assignee = assignee
    ).apply { applicationEventPublisher.publishEvent(this) }


  /**
   * Fires delete command.
   */
  fun delete(taskId: String, deleteReason: String) =
    DeleteTaskCommand(
      id = taskId,
      deleteReason = deleteReason
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires update command.
   */
  fun updateAttributes(task: Task) =
    UpdateAttributeTaskCommand(
      id = task.id,
      description = task.description,
      dueDate = task.dueDate,
      followUpDate = task.followUpDate,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      sourceReference = task.sourceReference,
      taskDefinitionKey = task.taskDefinitionKey
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires add candidate user command
   */
  fun addCandidateUser(taskId: String, userId: String) =
    AddCandidateUsersCommand(
      id = taskId,
      candidateUsers = setOf(userId)
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires add candidate group command
   */
  fun addCandidateGroup(taskId: String, groupId: String) =
    AddCandidateGroupsCommand(
      id = taskId,
      candidateGroups = setOf(groupId)
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires delete candidate user command
   */
  fun deleteCandidateUser(taskId: String, userId: String) =
    DeleteCandidateUsersCommand(
      id = taskId,
      candidateUsers = setOf(userId)
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires delete candidate group command
   */
  fun deleteCandidateGroup(taskId: String, groupId: String) =
    DeleteCandidateGroupsCommand(
      id = taskId,
      candidateGroups = setOf(groupId)
    ).apply { applicationEventPublisher.publishEvent(this) }
}
