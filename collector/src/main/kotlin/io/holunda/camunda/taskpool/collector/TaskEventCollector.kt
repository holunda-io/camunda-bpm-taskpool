package io.holunda.camunda.taskpool.collector

import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.task.IdentityLinkType
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects Camunda events (event listener order is {@link TaskEventCollector#ORDER}) and emits Commands
 */
@Component
class TaskEventCollector {

  companion object : KLogging() {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100;
  }

  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('create')")
  fun create(task: DelegateTask): CreateTaskCommand =
    CreateTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      deleteReason = task.deleteReason,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      processReference = if (task.processDefinitionId != null) {
        ProcessReference(
          processDefinitionId = task.processDefinitionId,
          processInstanceId = task.processInstanceId,
          executionId = task.executionId
        )
      } else {
        null
      },
      caseReference = if (task.caseDefinitionId != null) {
        CaseReference(
          caseDefinitionId = task.caseDefinitionId,
          caseInstanceId = task.caseInstanceId,
          caseExecutionId = task.caseExecutionId
        )
      } else {
        null
      }
    )

  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('complete')")
  fun complete(task: DelegateTask) =
    CompleteTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      deleteReason = task.deleteReason,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      processReference = if (task.processDefinitionId != null) {
        ProcessReference(
          processDefinitionId = task.processDefinitionId,
          processInstanceId = task.processInstanceId,
          executionId = task.executionId
        )
      } else {
        null
      },
      caseReference = if (task.caseDefinitionId != null) {
        CaseReference(
          caseDefinitionId = task.caseDefinitionId,
          caseInstanceId = task.caseInstanceId,
          caseExecutionId = task.caseExecutionId
        )
      } else {
        null
      }
    )

  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('assignment')")
  fun assign(task: DelegateTask) =
    AssignTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      deleteReason = task.deleteReason,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      processReference = if (task.processDefinitionId != null) {
        ProcessReference(
          processDefinitionId = task.processDefinitionId,
          processInstanceId = task.processInstanceId,
          executionId = task.executionId
        )
      } else {
        null
      },
      caseReference = if (task.caseDefinitionId != null) {
        CaseReference(
          caseDefinitionId = task.caseDefinitionId,
          caseInstanceId = task.caseInstanceId,
          caseExecutionId = task.caseExecutionId
        )
      } else {
        null
      }
    )


  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('delete')")
  fun delete(task: DelegateTask) =
    DeleteTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      deleteReason = task.deleteReason,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      processReference = if (task.processDefinitionId != null) {
        ProcessReference(
          processDefinitionId = task.processDefinitionId,
          processInstanceId = task.processInstanceId,
          executionId = task.executionId
        )
      } else {
        null
      },
      caseReference = if (task.caseDefinitionId != null) {
        CaseReference(
          caseDefinitionId = task.caseDefinitionId,
          caseInstanceId = task.caseInstanceId,
          caseExecutionId = task.caseExecutionId
        )
      } else {
        null
      }
    )
}
