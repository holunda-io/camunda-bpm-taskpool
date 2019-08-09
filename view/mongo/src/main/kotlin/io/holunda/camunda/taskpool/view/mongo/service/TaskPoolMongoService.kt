package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.publisher.toMono
import java.util.concurrent.CompletableFuture

/**
 * Mongo-based projection.
 */
@Component
@ProcessingGroup(TaskPoolMongoService.PROCESSING_GROUP)
class TaskPoolMongoService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private var taskRepository: TaskRepository,
  private var dataEntryRepository: DataEntryRepository,
  private val configuration: EventProcessingConfiguration,
  private val readRepo: TaskWithDataEntriesRepository,
  private val mongoTemplate: ReactiveMongoTemplate
) : ReactiveTaskApi, ReactiveDataEntryApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.camunda.taskpool.view.mongo.service"
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
    taskRepository.findById(query.id).map { it.task() }.toFuture()

  /**
   * Retrieves a task with data entries for given task id.
   */
  @QueryHandler
  override fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<TaskWithDataEntries?> =
    taskRepository.findById(query.id).flatMap { tasksWithDataEntries(it.task()) }.toFuture()

  /**
   * Retrieves a list of tasks with correlated data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult> =
    this.readRepo.findAllFilteredForUser(
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
    mongoTemplate.aggregate(
      Aggregation.newAggregation(
        Aggregation.group("sourceReference.applicationName").count().`as`("count"),
        Aggregation.project().and("_id").`as`("application").and("count").`as`("taskCount")
      ),
      "tasks",
      ApplicationWithTaskCount::class.java
    ).collectList()
      .toFuture()

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCreatedEngineEvent) {
    logger.debug { "Task created $event received" }
    taskRepository.save(task(event).taskDocument())
      .flatMap { updateTaskForUserQuery(event.id) }
      .flatMap { updateTaskCountByApplicationQuery(event.sourceReference.applicationName) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAssignedEngineEvent) {
    logger.debug { "Task assigned $event received" }
    taskRepository.findById(event.id)
      .flatMap { taskRepository.save(task(event, it.task()).taskDocument()) }
      .flatMap { updateTaskForUserQuery(event.id) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCompletedEngineEvent) {
    logger.debug { "Task completed $event received" }
    taskRepository.deleteById(event.id)
      .flatMap { updateTaskForUserQuery(event.id) }
      .flatMap { updateTaskCountByApplicationQuery(event.sourceReference.applicationName) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskDeletedEngineEvent) {
    logger.debug { "Task deleted $event received" }
    taskRepository.deleteById(event.id)
      .flatMap { updateTaskForUserQuery(event.id) }
      .flatMap { updateTaskCountByApplicationQuery(event.sourceReference.applicationName) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAttributeUpdatedEngineEvent) {
    logger.debug { "Task attributes updated $event received" }
    taskRepository.findById(event.id)
      .flatMap { taskRepository.save(task(event, it.task()).taskDocument()) }
      .flatMap { updateTaskForUserQuery(event.id) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateGroupChanged) {
    logger.debug { "Task candidate groups changed $event received" }
    taskRepository.findById(event.id)
      .flatMap { taskRepository.save(task(event, it.task()).taskDocument()) }
      .flatMap { updateTaskForUserQuery(event.id) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateUserChanged) {
    logger.debug { "Task user groups changed $event received" }
    taskRepository.findById(event.id)
      .flatMap { taskRepository.save(task(event, it.task()).taskDocument()) }
      .flatMap { updateTaskForUserQuery(event.id) }
      .block()
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
    dataEntryRepository.save(event.toDocument())
      .flatMap { updateDataEntryQuery(QueryDataIdentity(entryType = event.entryType, entryId = event.entryId)) }
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
      .flatMap { updateDataEntryQuery(QueryDataIdentity(entryType = event.entryType, entryId = event.entryId)) }
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

  private fun querySingleApplicationWithTaskCount(applicationName: String): Mono<ApplicationWithTaskCount> =
    mongoTemplate.aggregate(
      Aggregation.newAggregation(
        Aggregation.match(Criteria.where("sourceReference.applicationName").isEqualTo(applicationName)),
        Aggregation.group("sourceReference.applicationName").count().`as`("count"),
        Aggregation.project().and("_id").`as`("application").and("count").`as`("taskCount")
      ),
      "tasks",
      ApplicationWithTaskCount::class.java
    ).toMono()
      .defaultIfEmpty(ApplicationWithTaskCount(applicationName, 0))

  private fun updateTaskForUserQuery(taskId: String): Mono<out Any> {
    return taskRepository.findById(taskId)
      .doOnNext { task -> updateMapFilterQuery(task.task(), TasksForUserQuery::class.java) }
      .flatMap { task ->
        tasksWithDataEntries(task)
          .doOnNext { updateMapFilterQuery(it, TasksWithDataEntriesForUserQuery::class.java) }
      }
  }

  private fun updateDataEntryQuery(identity: DataIdentity): Mono<out Any> {
    return dataEntryRepository.findByIdentity(identity)
      .map { it.dataEntry() }
      .doOnNext { dataEntry ->
        updateMapFilterQuery(dataEntry, DataEntriesForUserQuery::class.java)
      }
  }

  private fun updateTaskCountByApplicationQuery(applicationName: String): Mono<out Any> {
    return querySingleApplicationWithTaskCount(applicationName)
      .doOnNext { queryUpdateEmitter.emit(TaskCountByApplicationQuery::class.java, { true }, it) }
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
