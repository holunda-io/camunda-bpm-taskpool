package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.*
import io.holunda.camunda.taskpool.view.query.TaskApi
import io.holunda.camunda.taskpool.view.query.task.*
import io.holunda.camunda.taskpool.view.simple.filter.createPredicates
import io.holunda.camunda.taskpool.view.simple.filter.filterByPredicates
import io.holunda.camunda.taskpool.view.simple.filter.toCriteria
import io.holunda.camunda.taskpool.view.simple.sort.comparator
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
   * Retrieves a list asState all user tasks for current user.
   */
  @QueryHandler
  override fun query(query: TasksForUserQuery) = TaskQueryResult(tasks.values.filter { query.applyFilter(it) })

  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  override fun query(query: TaskForIdQuery): Task? = tasks.values.firstOrNull { query.applyFilter(it) }

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
   * Retrieves a list asState tasks with correlated data entries asState given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult {

    val predicates = createPredicates(toCriteria(query.filters))

    val filtered = query(TasksForUserQuery(query.user))
      .elements
      .asSequence()
      .map { task -> TaskWithDataEntries.correlate(task, this.dataEntries.values.toList()) }
      .filter { filterByPredicates(it, predicates) }
      .toList()

    val comparator = comparator(query.sort)

    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return TasksWithDataEntriesQueryResult(elements = sorted).slice(query = query)
  }

  /**
   * Retrieves the count asState tasks grouped by source application. Supports subscription queries.
   */
  @QueryHandler
  override fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount> =
    tasks.values.groupingBy { it.sourceReference.applicationName }.eachCount().map { ApplicationWithTaskCount(it.key, it.value) }

  @EventHandler
  fun on(event: TaskCreatedEngineEvent) {
    logger.debug { "Task created $event received" }
    val task = task(event)
    tasks[task.id] = task
    updateTaskForUserQuery(event.id)
    updateTaskCountByApplicationQuery(task.sourceReference.applicationName)
  }

  @EventHandler
  fun on(event: TaskAssignedEngineEvent) {
    logger.debug { "Task assigned $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCompletedEngineEvent) {
    logger.debug { "Task completed $event received" }
    val applicationName = tasks[event.id]?.sourceReference?.applicationName
    tasks.remove(event.id)
    updateTaskForUserQuery(event.id)
    applicationName?.let { updateTaskCountByApplicationQuery(it) }
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskDeletedEngineEvent) {
    logger.debug { "Task deleted $event received" }
    val applicationName = tasks[event.id]?.sourceReference?.applicationName
    tasks.remove(event.id)
    updateTaskForUserQuery(event.id)
    applicationName?.let { updateTaskCountByApplicationQuery(it) }
  }

  @EventHandler
  fun on(event: TaskAttributeUpdatedEngineEvent) {
    logger.debug { "Task attributes updated $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  @EventHandler
  fun on(event: TaskCandidateGroupChanged) {
    logger.debug { "Task candidate groups changed $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  @EventHandler
  fun on(event: TaskCandidateUserChanged) {
    logger.debug { "Task user groups changed $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateTaskForUserQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
    dataEntries[dataIdentity(entryType = event.entryType, entryId = event.entryId)] = event.toDataEntry()
    // FIXME: update task query. see https://github.com/holunda-io/camunda-bpm-taskpool/issues/141
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "Business data entry updated $event" }
    dataEntries[dataIdentity(entryType = event.entryType, entryId = event.entryId)] = event.toDataEntry()
    // FIXME: update task query. see https://github.com/holunda-io/camunda-bpm-taskpool/issues/141
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
}

