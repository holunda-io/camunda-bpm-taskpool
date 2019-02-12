package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.*
import io.holunda.camunda.taskpool.view.mongo.filter.createPredicates
import io.holunda.camunda.taskpool.view.mongo.filter.filterByPredicates
import io.holunda.camunda.taskpool.view.mongo.filter.toCriteria
import io.holunda.camunda.taskpool.view.mongo.repository.TaskRepository
import io.holunda.camunda.taskpool.view.mongo.sort.comparator
import io.holunda.camunda.taskpool.view.query.*
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
@Component
@ProcessingGroup(TaskPoolService.PROCESSING_GROUP)
open class TaskPoolService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val taskRepository: TaskRepository
) {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.camunda.taskpool.view.mongo.service"
  }

  private val dataEntries = ConcurrentHashMap<String, DataEntry>()


  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  open fun query(query: TasksForUserQuery): List<Task> =
    taskRepository
      .findAll()
      .filter { query.applyFilter(it) }
      .collectList()
      .block() ?: listOf()

  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  open fun query(query: DataEntryQuery): List<DataEntry> = dataEntries.values.filter { query.applyFilter(it) }


  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  open fun query(query: TaskForIdQuery): Task? = taskRepository.findById(query.id).block()

  /**
   * Retrieves a task with data entries for given task id.
   */
  @QueryHandler
  open fun query(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries? {
    val task = taskRepository.findAll().filter { query.applyFilter(TaskWithDataEntries(it)) }.blockFirst()
    return if (task != null) {
      tasksWithDataEntries(task, this.dataEntries)
    } else {
      null
    }
  }

  /**
   * Retrieves a list of tasks with correlated data entries of given entry type (and optional id).
   */
  @QueryHandler
  open fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesResponse {

    val predicates = createPredicates(toCriteria(query.filters))

    val filtered = query(TasksForUserQuery(query.user))
      .asSequence()
      .map { task -> tasksWithDataEntries(task, this.dataEntries) }
      .filter { filterByPredicates(it, predicates) }
      .toList()

    val comparator = comparator(query.sort)

    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return slice(list = sorted, query = query)
  }

  fun slice(list: List<TaskWithDataEntries>, query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesResponse {
    val totalCount = list.size
    val offset = query.page * query.size
    return if (totalCount > offset) {
      TasksWithDataEntriesResponse(totalCount, list.slice(offset until Math.min(offset + query.size, totalCount)))
    } else {
      TasksWithDataEntriesResponse(totalCount, list)
    }
  }

  @EventHandler
  open fun on(event: TaskCreatedEngineEvent) {
    logger.debug { "Task created $event received" }
    val task = task(event)
    taskRepository.save(task).block()
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskAssignedEngineEvent) {
    logger.debug { "Task assigned $event received" }

    val task = taskRepository.findById { event.id }.block()
    if (task != null) {
      taskRepository.save(task(event, task)).block()
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: TaskCompletedEngineEvent) {
    logger.debug { "Task completed $event received" }
    taskRepository.deleteById(event.id).block()
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskDeletedEngineEvent) {
    logger.debug { "Task deleted $event received" }
    taskRepository.deleteById(event.id).block()
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskAttributeUpdatedEngineEvent) {
    logger.debug { "Task attributes updated $event received" }
    val task = taskRepository.findById { event.id }.block()
    if (task != null) {
      taskRepository.save(task(event, task)).block()
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: TaskCandidateGroupChanged) {
    logger.debug { "Task candidate groups changed $event received" }
    val task = taskRepository.findById { event.id }.block()
    if (task != null) {
      taskRepository.save(task(event, task)).block()
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: TaskCandidateUserChanged) {
    logger.debug { "Task user groups changed $event received" }
    val task = taskRepository.findById { event.id }.block()
    if (task != null) {
      taskRepository.save(task(event, task)).block()
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
    dataEntries[dataIdentity(entryType = event.entryType, entryId = event.entryId)] = DataEntry(
      entryType = event.entryType,
      entryId = event.entryId,
      payload = event.payload
    )
    updateDataEntryQuery(dataIdentity(entryType = event.entryType, entryId = event.entryId))
  }

  @EventHandler
  open fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "Business data entry updated $event" }
    dataEntries[dataIdentity(entryType = event.entryType, entryId = event.entryId)] = DataEntry(
      entryType = event.entryType,
      entryId = event.entryId,
      payload = event.payload
    )
    updateDataEntryQuery(dataIdentity(entryType = event.entryType, entryId = event.entryId))
  }

  private fun updateTaskForUserQuery(taskId: String) = updateMapFilterQuery(taskRepository.findById(taskId).block(), TasksForUserQuery::class.java)

  private fun updateDataEntryQuery(identity: String) = updateMapFilterQuery(
    if (dataEntries.contains(identity)) {
      dataEntries.getValue(identity)
    } else {
      null
    }, DataEntryQuery::class.java)

  private fun <T : Any, Q : FilterQuery<T>> updateMapFilterQuery(entry: T?, clazz: Class<Q>) {
    if (entry != null) {
      queryUpdateEmitter.emit(clazz, { query -> query.applyFilter(entry) }, entry)
    }

  }
}

