package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
open class TaskPoolService(
  private val queryUpdateEmitter: QueryUpdateEmitter
) {

  companion object : KLogging()

  private val tasks = ConcurrentHashMap<String, Task>()

  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  open fun query(query: TasksForUserQuery): List<Task> = tasks.values
    // relevant tasks
    .filter {
      // assignee
      it.assignee == query.user.username
        // candidate user
        || (it.candidateUsers.contains(query.user.username))
        // candidate groups:
        || (it.candidateGroups.any { candidateGroup -> query.user.groups.contains(candidateGroup) })
    }

  @EventHandler
  open fun on(event: TaskCreatedEvent) {
    logger.debug { "Received task created $event" }
    val task = Task(
      id = event.id,
      sourceReference = event.sourceReference,
      dueDate = event.dueDate,
      correlations = event.correlations,
      payload = event.payload,
      description = event.description,
      businessKey = event.businessKey,
      formKey = event.formKey,
      priority = event.priority,
      assignee = event.assignee,
      candidateGroups = event.candidateGroups,
      candidateUsers = event.candidateUsers,
      name = event.name,
      owner = event.owner,
      taskDefinitionKey = event.taskDefinitionKey,
      createTime = event.createTime
    )
    tasks.put(task.id, task)
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskAssignedEvent) {
    logger.debug { "Received task assigned $event" }
    tasks[event.id] = Task(
      id = event.id,
      sourceReference = event.sourceReference,
      dueDate = event.dueDate,
      correlations = event.correlations,
      payload = event.payload,
      description = event.description,
      businessKey = event.businessKey,
      formKey = event.formKey,
      priority = event.priority,
      assignee = event.assignee,
      candidateGroups = event.candidateGroups,
      candidateUsers = event.candidateUsers,
      name = event.name,
      owner = event.owner,
      taskDefinitionKey = event.taskDefinitionKey,
      createTime = event.createTime
    )
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskCompletedEvent) {
    logger.debug { "Received task completed $event" }
    tasks.remove(event.id)
    updateTaskForUserQuery(event.id)

  }

  @EventHandler
  open fun on(event: TaskDeletedEvent) {
    logger.debug { "Received task deleted $event" }
    tasks.remove(event.id)
    updateTaskForUserQuery(event.id)
  }


  @EventHandler
  open fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
  }


  private fun updateTaskForUserQuery(taskId: String) {
    val task = tasks[taskId]!!
    queryUpdateEmitter.emit(
      TasksForUserQuery::class.java,
      { query ->
        task.assignee == query.user.username
          || task.candidateUsers.contains(query.user.username)
          || task.candidateGroups.any { group -> query.user.groups.contains(group) }
      },
      task
    )
  }

}

data class Task(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskIdentity, WithPayload, WithCorrelations
