package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.api.task.TaskAssignedEvent
import io.holunda.camunda.taskpool.api.task.TaskCompletedEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEvent
import io.holunda.camunda.taskpool.api.task.TaskDeletedEvent
import io.holunda.camunda.taskpool.view.*
import io.holunda.camunda.taskpool.view.query.DataEntryQuery
import io.holunda.camunda.taskpool.view.query.FilterQuery
import io.holunda.camunda.taskpool.view.query.TasksDataEntryForUserQuery
import io.holunda.camunda.taskpool.view.query.TasksForUserQuery
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
open class TaskPoolService(
  private val queryUpdateEmitter: QueryUpdateEmitter
) {

  companion object : KLogging()

  private val tasks = ConcurrentHashMap<String, Task>()
  private val dataEntries = ConcurrentHashMap<String, DataEntry>()

  /**
   * Retrieves a list of all user tasks for current user.
   */
  @QueryHandler
  open fun query(query: TasksForUserQuery): List<Task> = tasks.values.filter { query.applyFilter(it) }

  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  open fun query(query: DataEntryQuery): List<DataEntry> = dataEntries.values.filter { query.applyFilter(it) }

  /**
   * Retrieves a list of tasks with correlatated data entries of given entry type (and optional id).
   */
  @QueryHandler
  open fun query(query: TasksDataEntryForUserQuery): List<TasksWithDataEntries> = query(TasksForUserQuery(query.user)).map { task -> tasksWithDataEntries(task, this.dataEntries) }

  @EventHandler
  open fun on(event: TaskCreatedEvent) {
    logger.debug { "Received task created $event" }
    val task = task(event)
    tasks[task.id] = task
    updateTaskForUserQuery(event.id)
  }

  @EventHandler
  open fun on(event: TaskAssignedEvent) {
    logger.debug { "Received task assigned $event" }
    tasks[event.id] = task(event)
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

  private fun updateTaskForUserQuery(taskId: String) = updateMapFilterQuery(tasks, taskId, TasksForUserQuery::class.java)
  private fun updateDataEntryQuery(identity: String) = updateMapFilterQuery(dataEntries, identity, DataEntryQuery::class.java)

  private fun <T : Any, Q : FilterQuery<T>> updateMapFilterQuery(map: Map<String, T>, key: String, clazz: Class<Q>) {
    if (map.contains(key)) {
      val entry = map[key]!!
      queryUpdateEmitter.emit(clazz, { query -> query.applyFilter(entry) }, entry)
    }

  }
}

