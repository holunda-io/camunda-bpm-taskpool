package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.mongo.ChangeTrackingMode
import io.holunda.camunda.taskpool.view.mongo.TaskPoolMongoViewProperties
import io.holunda.camunda.taskpool.view.mongo.repository.*
import io.holunda.camunda.taskpool.view.query.FilterQuery
import io.holunda.camunda.taskpool.view.query.ReactiveDataEntryApi
import io.holunda.camunda.taskpool.view.query.ReactiveTaskApi
import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
import io.holunda.camunda.taskpool.view.query.data.DataEntryForIdentityQuery
import io.holunda.camunda.taskpool.view.query.data.QueryDataIdentity
import io.holunda.camunda.taskpool.view.query.task.*
import io.holunda.camunda.taskpool.view.task
import mu.KLogging
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventProcessor
import org.axonframework.eventhandling.TrackingEventProcessor
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.util.concurrent.CompletableFuture
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Mongo-based projection.
 */
@Component
@ProcessingGroup(TaskPoolMongoService.PROCESSING_GROUP)
class TaskPoolMongoService(
  private val properties: TaskPoolMongoViewProperties,
  private val taskRepository: TaskRepository,
  private val dataEntryRepository: DataEntryRepository,
  private val taskWithDataEntriesRepository: TaskWithDataEntriesRepository,
  @Autowired(required = false)
  private val taskChangeTracker: TaskChangeTracker?,
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val configuration: EventProcessingConfiguration
) : ReactiveTaskApi, ReactiveDataEntryApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.camunda.taskpool.view.mongo.service"
  }

  private var taskCountByApplicationSubscription: Disposable? = null
  private var taskUpdateSubscription: Disposable? = null
  private var taskWithDataEntriesUpdateSubscription: Disposable? = null

  /**
   * Register for change stream.
   */
  @PostConstruct
  fun trackChanges() {
    if (properties.changeTrackingMode == ChangeTrackingMode.CHANGE_STREAM) {
      taskCountByApplicationSubscription = taskChangeTracker!!.trackTaskCountsByApplication()
        .subscribe { queryUpdateEmitter.emit(TaskCountByApplicationQuery::class.java, { true }, it) }
      taskUpdateSubscription = taskChangeTracker.trackTaskUpdates()
        .subscribe { queryUpdateEmitter.emit(TasksForUserQuery::class.java, { query -> query.applyFilter(it) }, it) }
      taskWithDataEntriesUpdateSubscription = taskChangeTracker.trackTaskWithDataEntriesUpdates()
        .subscribe { queryUpdateEmitter.emit(TasksWithDataEntriesForUserQuery::class.java, { query -> query.applyFilter(it) }, it) }
    }
  }

  /**
   * Unregister for change stream.
   */
  @PreDestroy
  fun stopTracking() {
    if (properties.changeTrackingMode == ChangeTrackingMode.CHANGE_STREAM) {
      taskCountByApplicationSubscription?.dispose()
      taskUpdateSubscription?.dispose()
      taskWithDataEntriesUpdateSubscription?.dispose()
    }
  }

  /**
   * Retrieves a list of all data entries for current user.
   */
  @QueryHandler
  override fun query(query: DataEntriesForUserQuery): CompletableFuture<DataEntriesQueryResult> =
    dataEntryRepository
      .findAllForUser(
        username = query.user.username,
        groupNames = query.user.groups
      ).map { it.dataEntry() }
      .collectList()
      .map { DataEntriesQueryResult(it).slice(query) }
      .toFuture()

  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  override fun query(query: TasksForUserQuery): CompletableFuture<TaskQueryResult> =
    taskRepository.findAllForUser(
      username = query.user.username,
      groupNames = query.user.groups
    ).map { it.task() }
      .collectList()
      .map { TaskQueryResult(it) }
      .toFuture()

  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery): CompletableFuture<DataEntriesQueryResult> =
    (if (query.entryId != null)
      dataEntryRepository.findByIdentity(query.identity())
        .map { it.dataEntry() }
        .map { listOf(it) }
        .defaultIfEmpty(listOf())
    else
      dataEntryRepository.findAllByEntryType(query.entryType)
        .map { it.dataEntry() }
        .collectList()
      ).map { DataEntriesQueryResult(it) }
      .toFuture()

  /**
   * Retrieves a task for given task id.
   */
  @QueryHandler
  override fun query(query: TaskForIdQuery): CompletableFuture<Task?> =
    taskRepository.findNotDeletedById(query.id).map { it.task() }.toFuture()

  /**
   * Retrieves a task with data entries for given task id.
   */
  @QueryHandler
  override fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<TaskWithDataEntries?> =
    taskRepository.findNotDeletedById(query.id).flatMap { tasksWithDataEntries(it.task()) }.toFuture()

  /**
   * Retrieves a list of tasks with correlated data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult> =
    this.taskWithDataEntriesRepository.findAllFilteredForUser(
      user = query.user,
      criteria = toCriteria(query.filters),
      pageable = PageRequest.of(query.page, query.size, sort(query.sort))
    ).map { it.taskWithDataEntries() }
      .collectList()
      // FIXME: replace by mongo paging
      .map { TasksWithDataEntriesQueryResult(it).slice(query = query) }
      .toFuture()

  @QueryHandler
  override fun query(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>> =
    taskRepository.findTaskCountsByApplication().collectList().toFuture()

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCreatedEngineEvent) {
    logger.debug { "Task created $event received" }
    val task = task(event)
    taskRepository.save(task.taskDocument())
      .then(updateTaskForUserQuery(task)
        .and(updateTaskCountByApplicationQuery(task.sourceReference.applicationName)))
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAssignedEngineEvent) {
    logger.debug { "Task assigned $event received" }
    taskRepository.findNotDeletedById(event.id)
      .flatMap {
        val resultingTask = task(event, it.task())
        taskRepository.save(resultingTask.taskDocument())
          .and(updateTaskForUserQuery(resultingTask))
      }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCompletedEngineEvent) {
    logger.debug { "Task completed $event received" }
    deleteTask(event.id, event.sourceReference.applicationName)
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskDeletedEngineEvent) {
    logger.debug { "Task deleted $event received" }
    deleteTask(event.id, event.sourceReference.applicationName)
  }

  private fun deleteTask(id: String, applicationName: String) {
    taskRepository.findNotDeletedById(id)
      .map { it.copy(deleted = true) }
      .flatMap { taskDocument ->
        if (properties.changeTrackingMode == ChangeTrackingMode.CHANGE_STREAM) {
          taskRepository.save(taskDocument)
        } else {
          taskRepository.delete(taskDocument)
            .then(updateTaskForUserQuery(taskDocument.task())
              .and(updateTaskCountByApplicationQuery(applicationName)))
        }
      }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAttributeUpdatedEngineEvent) {
    logger.debug { "Task attributes updated $event received" }
    taskRepository.findNotDeletedById(event.id)
      .flatMap {
        val resultingTask = task(event, it.task())
        taskRepository.save(resultingTask.taskDocument())
          .and(updateTaskForUserQuery(resultingTask))
      }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateGroupChanged) {
    logger.debug { "Task candidate groups changed $event received" }
    taskRepository.findNotDeletedById(event.id)
      .flatMap {
        val resultingTask = task(event, it.task())
        taskRepository.save(resultingTask.taskDocument())
          .and(updateTaskForUserQuery(resultingTask))
      }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateUserChanged) {
    logger.debug { "Task user groups changed $event received" }
    taskRepository.findNotDeletedById(event.id)
      .flatMap {
        val resultingTask = task(event, it.task())
        taskRepository.save(resultingTask.taskDocument())
          .and(updateTaskForUserQuery(resultingTask))
      }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
    dataEntryRepository.save(event.toDocument())
      .then(updateDataEntryQuery(QueryDataIdentity(entryType = event.entryType, entryId = event.entryId)))
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "Business data entry updated $event" }
    dataEntryRepository.findById(dataIdentityString(entryType = event.entryType, entryId = event.entryId))
      .map { oldEntry -> event.toDocument(oldEntry) }
      .switchIfEmpty { Mono.just(event.toDocument(null)) }
      .flatMap { dataEntryRepository.save(it) }
      .then(updateDataEntryQuery(QueryDataIdentity(entryType = event.entryType, entryId = event.entryId)))
      .block()
  }

  /**
   * Runs an event replay to fill the mongo task view with events.
   * Just kept as example. Not needed, will be called automatically, because of the global index stored in mongo DB.
   */
  @Suppress("UNUSED")
  fun restore() =
    this.configuration
      .eventProcessorByProcessingGroup<EventProcessor>(PROCESSING_GROUP)
      .ifPresent {
        if (it is TrackingEventProcessor) {
          logger.info { "VIEW-MONGO-002: Starting mongo view event replay." }
          it.shutDown()
          it.resetTokens()
          it.start()
        }
      }

  private inline fun ifChangeTrackingByEventHandler(block: () -> Mono<Void>): Mono<Void> =
    if (properties.changeTrackingMode == ChangeTrackingMode.EVENT_HANDLER) block() else Mono.empty()

  private fun updateTaskForUserQuery(task: Task): Mono<Void> = ifChangeTrackingByEventHandler {
    updateMapFilterQuery(task, TasksForUserQuery::class.java)
    tasksWithDataEntries(task)
      .doOnNext { updateMapFilterQuery(it, TasksWithDataEntriesForUserQuery::class.java) }
      .then()
  }

  private fun updateDataEntryQuery(identity: DataIdentity): Mono<Void> = ifChangeTrackingByEventHandler {
    dataEntryRepository.findByIdentity(identity)
      .map { it.dataEntry() }
      .doOnNext { dataEntry ->
        updateMapFilterQuery(dataEntry, DataEntriesForUserQuery::class.java)
      }
      .then()
  }

  private fun updateTaskCountByApplicationQuery(applicationName: String): Mono<Void> = ifChangeTrackingByEventHandler {
    taskRepository.findTaskCountForApplication(applicationName)
      .doOnNext { queryUpdateEmitter.emit(TaskCountByApplicationQuery::class.java, { true }, it) }
      .then()
  }

  private fun <T : Any, Q : FilterQuery<T>> updateMapFilterQuery(entry: T?, clazz: Class<Q>) {
    if (entry != null) {
      queryUpdateEmitter.emit(clazz, { query -> query.applyFilter(entry) }, entry)
    }
  }

  private fun tasksWithDataEntries(task: Task) =
    this.dataEntryRepository.findAllById(task.correlations.map { dataIdentityString(entryType = it.key, entryId = it.value.toString()) })
      .map { it.dataEntry() }
      .collectList()
      .map { TaskWithDataEntries(task = task, dataEntries = it) }

  private fun tasksWithDataEntries(taskDocument: TaskDocument) =
    tasksWithDataEntries(taskDocument.task())
}


internal fun sort(sort: String?): Sort =
  if (sort != null && sort.length > 1) {
    val attribute = sort.substring(1)
      .replace("task.", "")
    when (sort.substring(0, 1)) {
      "+" -> Sort(Sort.Direction.ASC, attribute)
      "-" -> Sort(Sort.Direction.DESC, attribute)
      else -> Sort.unsorted()
    }
  } else {
    Sort.unsorted()
  }
