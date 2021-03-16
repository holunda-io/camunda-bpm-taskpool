package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.api.task.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component


/**
 * Emits Task Commands
 */
@Component
class GenericTaskService(private val applicationEventPublisher: ApplicationEventPublisher) {

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
  fun complete(id: String) =
    CompleteTaskCommand(
      id = id
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires assign command.
   */
  fun assign(id: String, assignee: String) =
    AssignTaskCommand(
      id = id,
      assignee = assignee
    ).apply { applicationEventPublisher.publishEvent(this) }


  /**
   * Fires delete command.
   */
  fun delete(id: String, deleteReason: String) =
    DeleteTaskCommand(
      id = id,
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
      priority = task.priority
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires add candidate user command
   */
  fun addCandidateUser(id: String, userId: String) =
      AddCandidateUsersCommand(
        id = id,
        candidateUsers = setOf(userId)
      ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires add candidate group command
   */
  fun addCandidateGroup(id: String, groupId: String) =
    AddCandidateGroupsCommand(
      id = id,
      candidateGroups = setOf(groupId)
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires delete candidate user command
   */
  fun deleteCandidateUser(id: String, userId: String) =
    DeleteCandidateUsersCommand(
      id = id,
      candidateUsers = setOf(userId)
    ).apply { applicationEventPublisher.publishEvent(this) }

  /**
   * Fires delete candidate group command
   */
  fun deleteCandidateGroup(id: String, groupId: String) =
    DeleteCandidateGroupsCommand(
      id = id,
      candidateGroups = setOf(groupId)
    ).apply { applicationEventPublisher.publishEvent(this) }
}

