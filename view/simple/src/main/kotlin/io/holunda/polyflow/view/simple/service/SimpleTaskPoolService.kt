package io.holunda.polyflow.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryDeletedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.filter.createTaskPredicates
import io.holunda.polyflow.view.filter.filterByPredicate
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.query.task.*
import io.holunda.polyflow.view.simple.updateMapFilterQuery
import io.holunda.polyflow.view.sort.taskComparator
import io.holunda.polyflow.view.sort.taskWithDataEntriesComparator
import io.holunda.polyflow.view.task
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory implementation of the Task API.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class SimpleTaskPoolService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val tasks: ConcurrentHashMap<String, Task> = ConcurrentHashMap<String, Task>(),
  private val dataEntries: ConcurrentHashMap<String, DataEntry> = ConcurrentHashMap<String, DataEntry>()
) : TaskApi {

  companion object : KLogging()

  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  @Deprecated("Deprecated in favour of Optional-version of the same query.", replaceWith = ReplaceWith("SimpleTaskPoolService.query(TaskForIdQuery)"))
  fun legacyQuery(query: TaskForIdQuery): Task? {
    logger.warn { "You are using deprecated API, consider to switch to query(TaskWithDataEntriesForIdQuery): Optional<TaskWithDataEntries>" }
    return query(query).orElse(null)
  }

  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  override fun query(query: TaskForIdQuery): Optional<Task> {
    return Optional.ofNullable(tasks.values.firstOrNull { query.applyFilter(it) })
  }

  /**
   * Retrieves a  list of all tasks of a given process application.
   */
  @QueryHandler
  override fun query(query: TasksForApplicationQuery): TaskQueryResult {
    return TaskQueryResult(tasks.values.filter { query.applyFilter(it) })
  }

  /**
   * Retrieves a task with data entries for given task id.
   */
  @Deprecated(
    "Deprecated in favour of Optional-version of the same query.",
    replaceWith = ReplaceWith("SimpleTaskPoolService.query(TaskWithDataEntriesForIdQuery)")
  )
  @QueryHandler
  fun legacyQuery(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries? {
    logger.warn { "You are using deprecated API, consider to switch to query(TaskWithDataEntriesForIdQuery): Optional<TaskWithDataEntries>" }
    return query(query).orElse(null)
  }

  /**
   * Retrieves a task with data entries for given task id.
   */
  @QueryHandler
  override fun query(query: TaskWithDataEntriesForIdQuery): Optional<TaskWithDataEntries> {
    val task = tasks.values.firstOrNull { query.applyFilter(TaskWithDataEntries(it)) }
    return if (task != null) {
      Optional.of(TaskWithDataEntries.correlate(task, dataEntries))
    } else {
      Optional.empty()
    }
  }

  /**
   * Retrieves the count of tasks grouped by source application. Supports subscription queries.
   */
  @QueryHandler
  override fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount> =
    tasks.values.groupingBy { it.sourceReference.applicationName }.eachCount().map { ApplicationWithTaskCount(it.key, it.value) }


  /**
   * Retrieves a list of tasks with correlated data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult {

    val predicates = createTaskPredicates(toCriteria(query.filters))

    val filtered = tasks.values.filter { TasksForUserQuery(query.user).applyFilter(it) }
      .asSequence()
      .map { task -> TaskWithDataEntries.correlate(task, dataEntries) }
      .filter { filterByPredicate(it, predicates) }
      .toList()

    val comparator = taskWithDataEntriesComparator(query.sort)
    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return TasksWithDataEntriesQueryResult(elements = sorted).slice(query = query)
  }

  /**
   * Retrieves a list of all user tasks for current user's groups.
   */
  @QueryHandler
  override fun query(query: TasksWithDataEntriesForGroupQuery): TasksWithDataEntriesQueryResult {
    val predicates = createTaskPredicates(toCriteria(query.filters))

    val filtered = tasks.values.filter { TasksForGroupQuery(query.user).applyFilter(it) }
      .asSequence()
      .map { task -> TaskWithDataEntries.correlate(task, dataEntries) }
      .filter { filterByPredicate(it, predicates) }
      .toList()

    val comparator = taskWithDataEntriesComparator(query.sort)
    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return TasksWithDataEntriesQueryResult(elements = sorted).slice(query = query)
  }

  /**
   * Retrieves a list of all user tasks.
   */
  @QueryHandler
  override fun query(query: AllTasksWithDataEntriesQuery): TasksWithDataEntriesQueryResult {
    val predicates = createTaskPredicates(toCriteria(query.filters))

    val filtered = tasks.values.filter { AllTasksQuery().applyFilter(it) }
      .asSequence()
      .map { task -> TaskWithDataEntries.correlate(task, dataEntries) }
      .filter { filterByPredicate(it, predicates) }
      .toList()

    val comparator = taskWithDataEntriesComparator(query.sort)
    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return TasksWithDataEntriesQueryResult(elements = sorted).slice(query = query)

  }

  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  override fun query(query: TasksForUserQuery): TaskQueryResult {
    return queryForTasks(query)
  }

  /**
   * Retrieves a list of all user tasks for current user's groups.
   */
  @QueryHandler
  override fun query(query: TasksForGroupQuery): TaskQueryResult {
    return queryForTasks(query)
  }

  /**
   * Retrieves a list of all user tasks for current user's groups and current user being member of candidate users.
   */
  @QueryHandler
  override fun query(query: TasksForCandidateUserAndGroupQuery): TaskQueryResult {
    return queryForTasks(query)
  }


  /**
   * Retrieves a list of all user tasks.
   */
  @QueryHandler
  override fun query(query: AllTasksQuery): TaskQueryResult {
    return queryForTasks(query)
  }

  private fun queryForTasks(query: PageableSortableFilteredTaskQuery): TaskQueryResult {
    val predicates = createTaskPredicates(toCriteria(query.filters))
    val filtered = tasks.values.filter { query.applyFilter(it) }
      .filter { filterByPredicate(it, predicates) }
      .toList()
    val comparator = taskComparator(query.sort)
    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }
    return TaskQueryResult(elements = sorted).slice(query = query)
  }

  /**
   * Creates task.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCreatedEngineEvent) {
    logger.debug { "SIMPLE-VIEW-21: Task created $event received" }
    val task = task(event)
    tasks[task.id] = task
    updateFilteredQueryQuery(event.id)
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
      updateFilteredQueryQuery(event.id)
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
    updateFilteredQueryQuery(event.id)
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
    updateFilteredQueryQuery(event.id)
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
      updateFilteredQueryQuery(event.id)
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
      updateFilteredQueryQuery(event.id)
      updateTaskCountByApplicationQuery(tasks[event.id]!!.sourceReference.applicationName)
    }
  }

  /**
   * Changes task candidates users.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateUserChanged) {
    logger.debug { "SIMPLE-VIEW-27: Task user groups changed $event received" }
    if (tasks.containsKey(event.id)) {
      tasks[event.id] = task(event, tasks[event.id]!!)
      updateFilteredQueryQuery(event.id)
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
      updateFilteredQueryQuery(task.id)
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
      updateFilteredQueryQuery(task.id)
    }
  }

  /**
   * Delete data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryDeletedEvent) {
    logger.debug { "SIMPLE-VIEW-34: Business data entry deleted $event" }
    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries.remove(entryId)
    findTasksForDataEntry(entryId).forEach { taskId ->
      val task = tasks[taskId]!!
      updateFilteredQueryQuery(task.id)
    }
  }

  /**
   * Read-only stored data.
   */
  fun getDataEntries(): Map<String, DataEntry> = dataEntries.toMap()

  /**
   * Read-only stored data.
   */
  fun getTasks(): Map<String, Task> = tasks.toMap()

  private fun updateFilteredQueryQuery(taskId: String) {
    queryUpdateEmitter.updateMapFilterQuery(tasks, taskId, TasksForUserQuery::class.java)
    queryUpdateEmitter.updateMapFilterQuery(tasks, taskId, TasksForGroupQuery::class.java)
    queryUpdateEmitter.updateMapFilterQuery(tasks, taskId, TasksForCandidateUserAndGroupQuery::class.java)
    queryUpdateEmitter.updateMapFilterQuery(tasks, taskId, AllTasksQuery::class.java)

    tasks[taskId]
      ?.let { task -> TaskWithDataEntries.correlate(task, dataEntries) }
      ?.let { task ->
        queryUpdateEmitter.emit(TasksWithDataEntriesForUserQuery::class.java, { query -> query.applyFilter(task) }, task)
        queryUpdateEmitter.emit(TasksWithDataEntriesForGroupQuery::class.java, { query -> query.applyFilter(task) }, task)
        queryUpdateEmitter.emit(AllTasksWithDataEntriesQuery::class.java, { query -> query.applyFilter(task) }, task)
      }
  }

  private fun updateTaskCountByApplicationQuery(applicationName: String) {
    queryUpdateEmitter.emit(
      TaskCountByApplicationQuery::class.java,
      { true },
      ApplicationWithTaskCount(applicationName, tasks.values.count { it.sourceReference.applicationName == applicationName })
    )
  }

  /**
   * Finds tasks correlated with data entry of given id.
   */
  private fun findTasksForDataEntry(identity: String): List<String> = tasks
    .values
    .filter { it.correlationIdentities.contains(identity) }
    .map { it.id }
}

