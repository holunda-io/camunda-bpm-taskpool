package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.mongo.filter.createPredicates
import io.holunda.camunda.taskpool.view.mongo.filter.filterByPredicates
import io.holunda.camunda.taskpool.view.mongo.filter.toCriteria
import io.holunda.camunda.taskpool.view.mongo.repository.*
import io.holunda.camunda.taskpool.view.mongo.sort.comparator
import io.holunda.camunda.taskpool.view.query.*
import io.holunda.camunda.taskpool.view.task
import mu.KLogging
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventProcessor
import org.axonframework.eventhandling.TrackingEventProcessor
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

/**
 * Mongo-based projection.
 */
@Component
@ProcessingGroup(TaskPoolMongoService.PROCESSING_GROUP)
open class TaskPoolMongoService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private var taskRepository: TaskRepository,
  private var dataEntryRepository: DataEntryRepository,
  private val configuration: EventProcessingConfiguration
) {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.camunda.taskpool.view.mongo.service"
  }

  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  open fun query(query: TasksForUserQuery): List<Task> =
    taskRepository
      .findAllForUser(
        query.user.username,
        query.user.groups
      )
      .map { it.task() }

  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  open fun query(query: DataEntryQuery): List<DataEntry> {
    return if (query.entryId != null) {
      val dataEntry = dataEntryRepository.findByIdentity(query.identity()).orElse(null)?.dataEntry()
      if (dataEntry != null) {
        listOf(dataEntry)
      } else {
        listOf()
      }
    } else {
      dataEntryRepository.findAllByEntryType(query.entryType).map { it.dataEntry() }
    }
  }

  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  open fun query(query: TaskForIdQuery): Task? = taskRepository.findById(query.id).orElse(null)?.task()

  /**
   * Retrieves a task with data entries for given task id.
   */
  @QueryHandler
  open fun query(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries? {
    val task = taskRepository.findById(query.id).orElse(null)?.task()
    return if (task != null) {
      tasksWithDataEntries(task)
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
      .map { task -> tasksWithDataEntries(task) }
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
    taskRepository.save(task(event).taskDocument())
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskAssignedEngineEvent) {
    logger.debug { "Task assigned $event received" }
    taskRepository.findById(event.id).ifPresent {
      taskRepository.save(task(event, it.task()).taskDocument())
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: TaskCompletedEngineEvent) {
    logger.debug { "Task completed $event received" }
    taskRepository.deleteById(event.id)
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskDeletedEngineEvent) {
    logger.debug { "Task deleted $event received" }
    taskRepository.deleteById(event.id)
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskAttributeUpdatedEngineEvent) {
    logger.debug { "Task attributes updated $event received" }
    taskRepository.findById(event.id).ifPresent {
      taskRepository.save(task(event, it.task()).taskDocument())
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: TaskCandidateGroupChanged) {
    logger.debug { "Task candidate groups changed $event received" }
    taskRepository.findById(event.id).ifPresent {
      taskRepository.save(task(event, it.task()).taskDocument())
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: TaskCandidateUserChanged) {
    logger.debug { "Task user groups changed $event received" }
    taskRepository.findById(event.id).ifPresent {
      taskRepository.save(task(event, it.task()).taskDocument())
      updateTaskForUserQuery(event.id)
    }
  }

  @EventHandler
  open fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
    dataEntryRepository.save(
      DataEntryDocument(
        identity = dataIdentity(entryType = event.entryType, entryId = event.entryId),
        entryType = event.entryType,
        payload = event.payload
      ))
    updateDataEntryQuery(event)
  }

  @EventHandler
  open fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "Business data entry updated $event" }
    dataEntryRepository.save(
      DataEntryDocument(
        identity = dataIdentity(entryType = event.entryType, entryId = event.entryId),
        entryType = event.entryType,
        payload = event.payload
      ))
    updateDataEntryQuery(event)
  }

  /**
   * Runs an event replay to fill the mongo task view with events.
   */
  open fun restore() {

    // not needed, will be called automatically, because of the global index stored in mongo DB.
    this.configuration
      .eventProcessorByProcessingGroup<EventProcessor>(TaskPoolMongoService.PROCESSING_GROUP)
      .ifPresent {
        if (it is TrackingEventProcessor) {
          logger.info { "VIEW-MONGO-002: Starting mongo view event replay." }
          it.shutDown()
          it.resetTokens()
          it.start()
        }
      }
  }


  private fun updateTaskForUserQuery(taskId: String) = updateMapFilterQuery(
    taskRepository.findById(taskId).map { it.task() }.orElse(null), TasksForUserQuery::class.java)

  private fun updateDataEntryQuery(identity: DataIdentity) = updateMapFilterQuery(
    dataEntryRepository.findByIdentity(identity).map { it.dataEntry() }.orElse(null), DataEntryQuery::class.java)

  private fun <T : Any, Q : FilterQuery<T>> updateMapFilterQuery(entry: T?, clazz: Class<Q>) {
    if (entry != null) {
      queryUpdateEmitter.emit(clazz, { query -> query.applyFilter(entry) }, entry)
    }
  }

  private fun tasksWithDataEntries(task: Task) =
    TaskWithDataEntries(
      task = task,
      dataEntries = this.dataEntryRepository.findAllById(
        task.correlations.map { dataIdentity(entryType = it.key, entryId = it.value.toString()) }).map { it.dataEntry() }
    )

  private fun tasksWithDataEntries(taskDocument: TaskDocument) =
    tasksWithDataEntries(taskDocument.task())
}

