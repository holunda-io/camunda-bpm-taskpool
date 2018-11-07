package io.holunda.camunda.taskpool.collector

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.task.IdentityLinkType
import org.camunda.bpm.engine.task.Task
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Collects Camunda events (event listener order is {@link TaskEventCollector#ORDER}) and emits Commands
 */
@Component
class TaskEventCollector(
  private val formService: FormService,
  private val repositoryService: RepositoryService,
  private val collectorProperties: TaskCollectorProperties
) {

  companion object : KLogging() {
    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('create')")
  fun create(task: DelegateTask): CreateTaskCommand =
    CreateTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.userId != null && it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      formKey = formService.getTaskFormKey(task.processDefinitionId, task.taskDefinitionKey),
      taskDefinitionKey = task.taskDefinitionKey,
      businessKey = task.execution.businessKey,
      sourceReference = task.sourceReference(repositoryService, collectorProperties.enricher.applicationName)
    )

  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('complete')")
  fun complete(task: DelegateTask) =
    CompleteTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.userId != null && it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      formKey = formService.getTaskFormKey(task.processDefinitionId, task.taskDefinitionKey),
      businessKey = task.execution.businessKey,
      sourceReference = task.sourceReference(repositoryService, collectorProperties.enricher.applicationName)
    )

  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('assignment')")
  fun assign(task: DelegateTask) =
    AssignTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.userId != null && it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      formKey = formService.getTaskFormKey(task.processDefinitionId, task.taskDefinitionKey),
      businessKey = task.execution.businessKey,
      sourceReference = task.sourceReference(repositoryService, collectorProperties.enricher.applicationName)
    )


  @Order(ORDER)
  @EventListener(condition = "#task.eventName.equals('delete')")
  fun delete(task: DelegateTask) =
    DeleteTaskCommand(
      id = task.id,
      assignee = task.assignee,
      candidateGroups = task.candidates.filter { it.groupId != null }.map { it.groupId },
      candidateUsers = task.candidates.filter { it.userId != null && it.type == IdentityLinkType.CANDIDATE }.map { it.userId },
      createTime = task.createTime,
      deleteReason = task.deleteReason,
      description = task.description,
      dueDate = task.dueDate,
      eventName = task.eventName,
      name = task.name,
      owner = task.owner,
      priority = task.priority,
      taskDefinitionKey = task.taskDefinitionKey,
      formKey = formService.getTaskFormKey(task.processDefinitionId, task.taskDefinitionKey),
      businessKey = task.execution.businessKey,
      sourceReference = task.sourceReference(repositoryService, collectorProperties.enricher.applicationName)
    )
}


fun DelegateTask.sourceReference(repositoryService: RepositoryService, applicationName: String): SourceReference =
  when {
    this.processDefinitionId != null -> ProcessReference(
      definitionId = this.processDefinitionId,
      instanceId = this.processInstanceId,
      executionId = this.executionId,
      definitionKey = this.processDefinitionKey(),
      name = this.processName(repositoryService),
      applicationName = applicationName
    )
    this.caseDefinitionId != null -> CaseReference(
      definitionId = this.caseDefinitionId,
      instanceId = this.caseInstanceId,
      executionId = this.caseExecutionId,
      definitionKey = this.caseDefinitionKey(),
      name = this.caseName(repositoryService),
      applicationName = applicationName
    )
    else -> throw IllegalArgumentException("No source reference found.")
  }

fun DelegateTask.processDefinitionKey(): String = extractKey(this.processDefinitionId)
fun DelegateTask.caseDefinitionKey(): String = extractKey(this.caseDefinitionId)
fun Task.processDefinitionKey(): String = extractKey(this.processDefinitionId)
fun Task.caseDefinitionKey(): String = extractKey(this.caseDefinitionId)

fun DelegateTask.caseName(repositoryService: RepositoryService): String {
  val caseDefinition = repositoryService.createCaseDefinitionQuery()
    .caseDefinitionId(this.caseDefinitionId)
    .singleResult()
    ?: throw IllegalArgumentException("Case definition could not be resolved" + this.caseDefinitionId)
  return caseDefinition.name
}

fun DelegateTask.processName(repositoryService: RepositoryService): String {
  val processDefinition = repositoryService.createProcessDefinitionQuery()
    .processDefinitionId(this.processDefinitionId)
    .singleResult()
    ?: throw IllegalArgumentException("Process definition could not be resolved" + this.processDefinitionId)
  return processDefinition.name

}

private fun extractKey(processDefinitionId: String?): String {
  if (processDefinitionId == null) {
    throw IllegalArgumentException("Process definition id must not be null.")
  }
  return processDefinitionId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
}
