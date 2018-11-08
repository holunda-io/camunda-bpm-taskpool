package io.holunda.camunda.client.task

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.TaskClaimedEvent
import io.holunda.camunda.taskpool.api.task.TaskIdentity
import io.holunda.camunda.taskpool.api.task.TaskToBeCompletedEvent
import io.holunda.camunda.taskpool.api.task.TaskUnclaimedEvent
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.stereotype.Component

@Component
class TaskEventHandlers(
  private val taskService: TaskService,
  private val taskCollectorProperties: TaskCollectorProperties
) {

  companion object : KLogging()

  @EventHandler
  open fun on(event: TaskClaimedEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        taskService.setAssignee(event.id, event.assignee)
      } catch (e: ProcessEngineException) {
        // FIXME: build compensation logic
        logger.error("CLIENT-001: Error claiming task", e)
      }
    }
  }

  @EventHandler
  open fun on(event: TaskUnclaimedEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        taskService.setAssignee(event.id, null)
      } catch (e: ProcessEngineException) {
        // FIXME: build compensation logic
        logger.error("CLIENT-002: Error un-claiming task", e)
      }
    }
  }

  @EventHandler
  open fun on(event: TaskToBeCompletedEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        taskService.complete(event.id, event.payload)
      } catch (e: ProcessEngineException) {
        // FIXME: build compensation logic
        logger.error("CLIENT-003: Error un-claiming task", e)
      }
    }
  }


  internal fun isAddressedToMe(event: TaskIdentity) = taskCollectorProperties.enricher.applicationName == event.sourceReference.applicationName
}
