package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.query.task.TaskApi
import io.holunda.camunda.taskpool.view.query.task.*
import io.holunda.camunda.taskpool.view.simple.filter.createTaskPredicates
import io.holunda.camunda.taskpool.view.simple.filter.filterByPredicate
import io.holunda.camunda.taskpool.view.simple.filter.toCriteria
import io.holunda.camunda.taskpool.view.simple.sort.taskComparator
import io.holunda.camunda.taskpool.view.simple.updateMapFilterQuery
import io.holunda.camunda.taskpool.view.task
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class TaskPoolService(
  private val queryUpdateEmitter: QueryUpdateEmitter
) : TaskApi {

  companion object : KLogging()

  private val tasks = ConcurrentHashMap<String, Task>()
  private val dataEntries = ConcurrentHashMap<String, DataEntry>()

  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  override fun query(query: TasksForUserQuery) = TaskQueryResult(tasks.values.filter { query.applyFilter(it) })

  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  override fun query(query: TaskForIdQuery): Task? = tasks.values.firstOrNull { query.applyFilter(it) }

  /**
   * Retrieves a  list of all tasks of a given process application.
   */
  @QueryHandler
  override fun query(query: TasksForApplicationQuery) = TaskQueryResult(tasks.values.filter { query.applyFilter(it) })

  /**
   * Retrieves a task with data entries for given task id.
   */
  @QueryHandler
  override fun query(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries? {
    val task = tasks.values.firstOrNull { query.applyFilter(TaskWithDataEntries(it)) }
    return if (task != null) {
      TaskWithDataEntries(task, this.dataEntries.values.toList())
    } else {
      null
    }
  }

  /**
   * Retrieves a list of tasks with correlated data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult {

    val predicates = createTaskPredicates(toCriteria(query.filters))

    val filtered = query(TasksForUserQuery(query.user))
      .elements
      .asSequence()
      .map { task -> TaskWithDataEntries.correlate(task, this.dataEntries.values.toList()) }
      .filter { filterByPredicate(it, predicates) }
      .toList()

    val comparator = taskComparator(query.sort)

    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return TasksWithDataEntriesQueryResult(elements = sorted).slice(query = query)
  }

  /**
   * Retrieves the count of tasks grouped by source application. Supports subscription queries.
   */
  @QueryHandler
  override fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount> =
    tasks.values.groupingBy { it.sourceReference.applicationName }.eachCount().map { ApplicationWithTaskCount(it.key, it.value) }

  /**
   * Creates task.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCreatedEngineEvent) {
    logger.debug { "SIMPLE-VIEW-21: Task created $event received" }
    val task = task(event)
    tasks[task.id] = task
    updateTaskForUserQuery(event.id)
    updateTaskCountByApplicationQuery(task.sourceReference.applicationName)
  }

  /**
   * Assigns task.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAssignedEngineEvent) {
    logger.debug { "SIMPLE-VIEW-22: Task assigned $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  /**
   * Completes task.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCompletedEngineEvent) {
    logger.debug { "SIMPLE-VIEW-23: Task completed $event received" }
    val applicationName = tasks[event.id]?.sourceReference?.applicationName
    tasks.remove(event.id)
    updateTaskForUserQuery(event.id)
    applicationName?.let { updateTaskCountByApplicationQuery(it) }
  }

  /**
   * Deletes task.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskDeletedEngineEvent) {
    logger.debug { "SIMPLE-VIEW-24: Task deleted $event received" }
    val applicationName = tasks[event.id]?.sourceReference?.applicationName
    tasks.remove(event.id)
    updateTaskForUserQuery(event.id)
    applicationName?.let { updateTaskCountByApplicationQuery(it) }
  }

  /**
   * Performs a task attribute update.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAttributeUpdatedEngineEvent) {
    logger.debug { "SIMPLE-VIEW-25: Task attributes updated $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  /**
   * Changes task candidate groups.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateGroupChanged) {
    logger.debug { "SIMPLE-VIEW-26: Task candidate groups changed $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  /**
   * Changes task candidatges users.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateUserChanged) {
    logger.debug { "SIMPLE-VIEW-27: Task user groups changed $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  /**
   * Creates a data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent) {
    logger.debug { "SIMPLE-VIEW-28: Business data entry created $event" }
    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries[entryId] = event.toDataEntry()

    findTasksForDataEntry(entryId).forEach { taskId ->
      val task = tasks[taskId]!!
      updateTaskForUserQuery(task.id)
    }
  }

  /**
   * Update data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "SIMPLE-VIEW-29: Business data entry updated $event" }
    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries[entryId] = event.toDataEntry(dataEntries[entryId])
    findTasksForDataEntry(entryId).forEach { taskId ->
      val task = tasks[taskId]!!
      updateTaskForUserQuery(task.id)
    }
  }

  private fun updateTaskForUserQuery(taskId: String) {
    queryUpdateEmitter.updateMapFilterQuery(tasks, taskId, TasksForUserQuery::class.java)

    val mapTasksWithDataEntries = TaskWithDataEntries.correlate(tasks.values.toList(), dataEntries.values.toList())
      .map { it.task.id to it }
      .toMap()

    queryUpdateEmitter.updateMapFilterQuery(mapTasksWithDataEntries, taskId, TasksWithDataEntriesForUserQuery::class.java)
  }

  private fun updateTaskCountByApplicationQuery(applicationName: String) {
    queryUpdateEmitter.emit(TaskCountByApplicationQuery::class.java, { true }, ApplicationWithTaskCount(applicationName, tasks.values.count { it.sourceReference.applicationName == applicationName }))
  }

  /**
   * Finds tasks correlated with data entry of given id.
   */
  private fun findTasksForDataEntry(identity: String): List<String> = tasks
    .values
    .filter { it.correlationIdentities.contains(identity) }
    .map { it.id }
}

