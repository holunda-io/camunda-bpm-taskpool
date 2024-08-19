package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.JpaPolyflowViewTaskService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryRepository
import io.holunda.polyflow.view.jpa.data.toDataEntry
import io.holunda.polyflow.view.jpa.task.TaskRepository
import io.holunda.polyflow.view.jpa.task.TaskEntity
import io.holunda.polyflow.view.jpa.task.toTask
import io.holunda.polyflow.view.jpa.task.update
import io.holunda.polyflow.view.jpa.task.toEntity
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasApplication
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.isAssignedTo
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.isAssigneeSet
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.isAuthorizedFor
import io.holunda.polyflow.view.jpa.update.updateTaskQuery
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.task.*
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

/**
 * Implementation of the Polyflow Task View API using JPA to create the persistence model.
 */
@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewTaskService(
  val taskRepository: TaskRepository,
  val dataEntryRepository: DataEntryRepository,
  val objectMapper: ObjectMapper,
  val queryUpdateEmitter: QueryUpdateEmitter,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) : TaskApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service.task"
  }

  @QueryHandler
  override fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult {
    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    val criteria = toCriteria(query.filters)
    val taskSpecification = criteria.toTaskWithDataEntriesSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()

    val pageRequest = pageRequest(query.page, query.size, sort)

    val userQuery = if (query.assignedToMeOnly) {
      isAssignedTo(query.user.username)
    } else {
      composeOr(
        listOf(
          isAuthorizedFor(authorizedPrincipals),
          isAssignedTo(query.user.username)
        )
      )
    }

    val page = taskRepository.findAll(taskSpecification.and(userQuery), pageRequest)
      .map { taskEntity ->
        TaskWithDataEntries(
          task = taskEntity.toTask(objectMapper),
          dataEntries = taskEntity.correlations.map { id ->
            dataEntryRepository.findAll(
              DataEntryRepository.isAuthorizedFor(authorizedPrincipals)
                .and(DataEntryRepository.hasEntryId(id.entryId))
                .and(DataEntryRepository.hasEntryType(id.entryType))
            )
          }.flatten().map { it.toDataEntry(objectMapper) }
        )
      }

    return TasksWithDataEntriesQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  @QueryHandler
  override fun query(query: TasksWithDataEntriesForGroupQuery): TasksWithDataEntriesQueryResult {
    val authorizedPrincipals = query.user.groups.map { group(it) }.toSet()
    val taskAuthorizationSpecification = composeAnd(
      listOf(
        isAuthorizedFor(authorizedPrincipals), isAssigneeSet(query.includeAssigned)
      )
    )
    val dataAuthorizationSpecification = DataEntryRepository.isAuthorizedFor(authorizedPrincipals)
    val criteria = toCriteria(query.filters)
    val taskSpecification = criteria.toTaskWithDataEntriesSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()
    val pageRequest = pageRequest(query.page, query.size, sort)

    val page = taskRepository.findAll(taskSpecification.and(taskAuthorizationSpecification), pageRequest)
      .map { taskEntity ->
        TaskWithDataEntries(
          task = taskEntity.toTask(objectMapper),
          dataEntries = taskEntity.correlations.map { id ->
            dataEntryRepository.findAll(
              dataAuthorizationSpecification
                .and(DataEntryRepository.hasEntryId(id.entryId))
                .and(DataEntryRepository.hasEntryType(id.entryType))
            )
          }.flatten().map { it.toDataEntry(objectMapper) }
        )
      }

    return TasksWithDataEntriesQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  override fun query(query: AllTasksWithDataEntriesQuery): TasksWithDataEntriesQueryResult {
    val criteria = toCriteria(query.filters)
    val taskSpecification = criteria.toTaskWithDataEntriesSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()
    val pageRequest = pageRequest(query.page, query.size, sort)

    val page = taskRepository.findAll(taskSpecification, pageRequest).map { taskEntity ->
      TaskWithDataEntries(
        task = taskEntity.toTask(objectMapper),
        dataEntries = taskEntity.correlations.map { id ->
          dataEntryRepository.findAll(
            DataEntryRepository.hasEntryId(id.entryId)
              .and(DataEntryRepository.hasEntryType(id.entryType))
          )
        }.flatten().map { it.toDataEntry(objectMapper) }
      )
    }

    return TasksWithDataEntriesQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  @QueryHandler
  override fun query(query: TasksForUserQuery): TaskQueryResult {
    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    val specification = toCriteria(query.filters).toTaskSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()
    val pageRequest = pageRequest(query.page, query.size, sort)
    val userQuery = if (query.assignedToMeOnly) {
      isAssignedTo(query.user.username)
    } else {
      composeOr(
        listOf(
          isAuthorizedFor(authorizedPrincipals),
          isAssignedTo(query.user.username)
        )
      )
    }

    val page = taskRepository
      .findAll(specification.and(userQuery), pageRequest)
      .map { taskEntity -> taskEntity.toTask(objectMapper) }

    return TaskQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  @QueryHandler
  override fun query(query: TasksForGroupQuery): TaskQueryResult {
    val authorizationSpecification = composeAnd(
      listOf(
        isAuthorizedFor(query.user.groups.map { group(it) }.toSet()),
        isAssigneeSet(query.includeAssigned)
      )
    )
    val taskSpecification = toCriteria(query.filters).toTaskSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()
    val pageRequest = pageRequest(query.page, query.size, sort)

    val page = taskRepository
      .findAll(taskSpecification.and(authorizationSpecification), pageRequest)
      .map { taskEntity -> taskEntity.toTask(objectMapper) }

    return TaskQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  @QueryHandler
  override fun query(query: TasksForCandidateUserAndGroupQuery): TaskQueryResult {
    val authorizationSpecification =
      composeAnd(
        listOf(
          composeOr(
            listOf(
              isAuthorizedFor(setOf(user(query.user.username)).plus(query.user.groups.map { group(it) }.toSet())),
            )
          ),
          isAssigneeSet(query.includeAssigned)
        )
      )
    val taskSpecification = toCriteria(query.filters).toTaskSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()
    val pageRequest = pageRequest(query.page, query.size, sort)

    val page = taskRepository
      .findAll(taskSpecification.and(authorizationSpecification), pageRequest)
      .map { taskEntity -> taskEntity.toTask(objectMapper) }

    return TaskQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  @QueryHandler
  override fun query(query: AllTasksQuery): TaskQueryResult {
    val specification = toCriteria(query.filters).toTaskSpecification()
    val sort = query.apply {
      sanitizeSort(Task::class)
    }.mapTaskSort()
    val pageRequest = pageRequest(query.page, query.size, sort)
    val page = taskRepository.findAll(specification, pageRequest).map { taskEntity -> taskEntity.toTask(objectMapper) }

    return TaskQueryResult(
      elements = page.toList(),
      totalElementCount = page.totalElements.toInt()
    )
  }

  @QueryHandler
  override fun query(query: TaskWithDataEntriesForIdQuery): Optional<TaskWithDataEntries> {
    return Optional.ofNullable(taskRepository.findByIdOrNull(query.id)?.let { taskEntity ->
      TaskWithDataEntries(
        task = taskEntity.toTask(objectMapper),
        dataEntries = taskEntity.correlations.map { id -> dataEntryRepository.findById(id) }.filter { it.isPresent }.map { it.get().toDataEntry(objectMapper) }
      )
    })
  }


  @QueryHandler
  override fun query(query: TaskForIdQuery): Optional<Task> {
    return Optional.ofNullable(taskRepository.findByIdOrNull(query.id)?.toTask(objectMapper))
  }


  @QueryHandler
  override fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount> {
    return taskRepository.getCountByApplication().map {
      ApplicationWithTaskCount(application = it.applicationName, taskCount = it.count.toInt())
    }
  }

  @QueryHandler
  override fun query(query: TasksForApplicationQuery): TaskQueryResult {
    return TaskQueryResult(elements = taskRepository.findAll(hasApplication(query.applicationName)).map {
      it.toTask(objectMapper)
    })
  }

  /**
   * Delivers task created event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCreatedEngineEvent, metaData: MetaData) {
    logger.debug { "Task created $event received" }
    if (isDisabledByProperty()) {
      return
    }

    taskRepository
      .findById(event.id)
      .ifEmpty {
        val updated = taskRepository.save(
          event.toEntity(
            objectMapper,
            polyflowJpaViewProperties.payloadAttributeLevelLimit,
            polyflowJpaViewProperties.taskJsonPathFilters()
          )
        )
        emitTaskUpdate(updated)

      }.ifPresent { entity ->
        logger.warn { "Will not create task '${event.id}' because it already exists in the database" }
        emitTaskUpdate(entity)
      }
  }

  /**
   * Delivers task assigned event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAssignedEngineEvent, metaData: MetaData) {
    logger.debug { "Task assigned $event received" }
    if (isDisabledByProperty()) {
      return
    }

    taskRepository
      .findById(event.id)
      .ifEmpty {
        logger.warn { "Cannot update task '${event.id}' because it does not exist in the database" }
      }.ifPresent { entity ->
        entity.assignee = event.assignee
        val updated = taskRepository.save(entity)
        emitTaskUpdate(updated)
      }
  }

  /**
   * Delivers task completed event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCompletedEngineEvent, metaData: MetaData) {
    logger.debug { "Task completed $event received" }
    if (isDisabledByProperty()) {
      return
    }

    taskRepository
      .findById(event.id)
      .ifEmpty {
        logger.warn { "Cannot complete task '${event.id}' because it does not exist in the database" }
      }
      .ifPresent { entity ->
        taskRepository.delete(entity)
        emitTaskUpdate(entity, deleted = true)
      }
  }

  /**
   * Delivers task deleted event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskDeletedEngineEvent, metaData: MetaData) {
    logger.debug { "Task deleted $event received" }
    if (isDisabledByProperty()) {
      return
    }

    taskRepository
      .findById(event.id)
      .ifEmpty {
        logger.warn { "Cannot delete task '${event.id}' because it does not exist in the database" }
      }
      .ifPresent { entity ->
        taskRepository.delete(entity)
        emitTaskUpdate(entity, deleted = true)
      }
  }

  /**
   * Delivers task attribute changed event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskAttributeUpdatedEngineEvent, metaData: MetaData) {
    logger.debug { "Task attributes updated $event received" }
    if (isDisabledByProperty()) {
      return
    }
    taskRepository
      .findById(event.id)
      .ifEmpty {
        logger.warn { "Cannot update task '${event.id}' because it does not exist in the database" }
      }
      .ifPresent { entity ->
        entity.update(event, objectMapper, polyflowJpaViewProperties.payloadAttributeLevelLimit, polyflowJpaViewProperties.taskJsonPathFilters())
        val updated = taskRepository.save(entity)
        emitTaskUpdate(updated)
      }
  }

  /**
   * Delivers task group changed event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateGroupChanged, metaData: MetaData) {
    logger.debug { "Task candidate groups changed $event received" }
    if (isDisabledByProperty()) {
      return
    }

    taskRepository
      .findById(event.id)
      .ifEmpty {
        logger.warn { "Cannot update task '${event.id}' because it does not exist in the database" }
      }.ifPresent { entity ->
        when (event.assignmentUpdateType) {
          CamundaTaskEventType.CANDIDATE_GROUP_ADD -> entity.authorizedPrincipals.add(group(event.groupId).toString())
          CamundaTaskEventType.CANDIDATE_GROUP_DELETE -> entity.authorizedPrincipals.remove(group(event.groupId).toString())
        }
        val updated = taskRepository.save(entity)
        emitTaskUpdate(updated)
      }
  }

  /**
   * Delivers task user changed event.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: TaskCandidateUserChanged, metaData: MetaData) {
    logger.debug { "Task user groups changed $event received" }
    if (isDisabledByProperty()) {
      return
    }

    taskRepository
      .findById(event.id)
      .ifEmpty {
        logger.warn { "Cannot update task '${event.id}' because it does not exist in the database" }
      }
      .ifPresent { entity ->

        when (event.assignmentUpdateType) {
          CamundaTaskEventType.CANDIDATE_USER_ADD -> entity.authorizedPrincipals.add(user(event.userId).toString())
          CamundaTaskEventType.CANDIDATE_USER_DELETE -> entity.authorizedPrincipals.remove(user(event.userId).toString())
        }

        val updated = taskRepository.save(entity)
        emitTaskUpdate(updated)
      }
  }

  private fun getMaxRevision(elementRevisions: List<Long>): RevisionValue =
    elementRevisions.maxByOrNull { it }?.let { RevisionValue(it) } ?: RevisionValue.NO_REVISION

  private fun emitTaskUpdate(entity: TaskEntity, deleted: Boolean = false) {
    queryUpdateEmitter.updateTaskQuery(
      TaskWithDataEntries(task = entity.toTask(objectMapper, deleted), dataEntries = listOf()) // FIXME
    )
  }

  private fun reportMissingFeature(query: PageableSortableQuery) {
    if (query.sort.isEmpty()) {
      logger.warn { "Sorting is currently not supported, but the sort was requested: ${query.sort}, see https://github.com/holunda-io/camunda-bpm-taskpool/issues/701" }
    }
    if (query.page != 1 || query.size != Int.MAX_VALUE) {
      logger.warn { "Paging is currently not supported by requested. Page: ${query.page}, Size: ${query.size}, see https://github.com/holunda-io/camunda-bpm-taskpool/issues/701" }
    }
  }

  /**
   * Executor on empty optional.
   */
  fun <T> Optional<T>.ifEmpty(execute: () -> Unit): Optional<T> = this.also {
    if (this.isEmpty) {
      execute.invoke()
    }
  }

  private fun isDisabledByProperty(): Boolean {
    return (!polyflowJpaViewProperties.storedItems.contains(StoredItem.TASK)).also {
      if (it) {
        logger.debug { "Task storage disabled by property." }
      }
    }
  }

}
