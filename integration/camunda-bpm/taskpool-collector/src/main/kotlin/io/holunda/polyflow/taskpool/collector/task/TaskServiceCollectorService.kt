package io.holunda.polyflow.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommandFilter
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.formKey
import io.holunda.polyflow.taskpool.sourceReference
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.IdentityLinkType
import org.camunda.bpm.engine.task.Task
import org.springframework.context.ApplicationEventPublisher

/**
 * Service to collect tasks and fire the corresponding commands using Camunda Task Service.
 */
class TaskServiceCollectorService(
  private val taskService: TaskService,
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties,
  private val applicationEventPublisher: ApplicationEventPublisher,
  private val engineTaskCommandFilter: EngineTaskCommandFilter
) {

  /**
   * Collects tasks existing inm the Camunda task service
   * sends commands for all which are not filtered away by the filter.
   * In combination with [TaskAggregateEngineTaskCommandFilter] this can be used to initialize
   * the event store with tasks from the engine.
   */
  fun collectAndPopulateExistingTasks() {
    val engineTasks = loadExistingTasks()
    val commands = convertToCommands(engineTasks)
    val filtered = filterTasks(commands)
    filtered.forEach {
      applicationEventPublisher.publishEvent(it)
    }
  }

  private fun filterTasks(engineTasks: List<EngineTaskCommand>): List<EngineTaskCommand> {
    return engineTasks.filter { engineTaskCommandFilter.test(it) }
  }

  private fun loadExistingTasks(): List<Task> {
    return taskService
      .createTaskQuery()
      .list()
  }

  private fun convertToCommands(tasks: List<Task>): List<CreateTaskCommand> {
    return tasks
      .filterIsInstance<TaskEntity>()
      .map { task ->
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
      }
  }

}
