package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.task.TaskAssignedEvent
import io.holunda.camunda.taskpool.api.task.TaskCompletedEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEvent
import io.holunda.camunda.taskpool.api.task.TaskDeletedEvent
import mu.KLogging
import org.axonframework.eventhandling.EventHandler

open class LoggingTaskHandler {

  companion object : KLogging()

  @EventHandler
  fun on(event: TaskCreatedEvent) {
    logger.info{ "Created $event"}
  }
  @EventHandler
  fun on(event: TaskAssignedEvent) {
    logger.info{ "Assigned $event"}
  }
  @EventHandler
  fun on(event: TaskCompletedEvent) {
    logger.info{ "Completed $event"}
  }
  @EventHandler
  fun on(event: TaskDeletedEvent) {
    logger.info{ "Deleted $event"}
  }

}
