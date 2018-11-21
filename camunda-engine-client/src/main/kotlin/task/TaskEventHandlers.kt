package io.holunda.camunda.client.task

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.TaskClaimedEvent
import io.holunda.camunda.taskpool.api.task.TaskIdentity
import io.holunda.camunda.taskpool.api.task.TaskToBeCompletedEvent
import io.holunda.camunda.taskpool.api.task.TaskUnclaimedEvent
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.camunda.bpm.engine.ProcessEngineException
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
        logger.debug { "Claiming task $event" }
        val task = taskService.createTaskQuery().taskId(event.id).singleResult()
        if (task != null) {
          taskService.setAssignee(event.id, event.assignee)
        } else {
          logger.error { "CLIENT-004: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: ProcessEngineException) {
        logger.error("CLIENT-001: Error claiming task", e)
      }
    }
  }

  @EventHandler
  open fun on(event: TaskUnclaimedEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        logger.debug { "Unclaiming task $event" }
        val task = taskService.createTaskQuery().taskId(event.id).singleResult()
        if (task != null) {
          taskService.setAssignee(event.id, null)
        } else {
          logger.error { "CLIENT-005: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: ProcessEngineException) {
        logger.error("CLIENT-002: Error un-claiming task", e)
      }
    }
  }

  @EventHandler
  open fun on(event: TaskToBeCompletedEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        logger.debug { "Completing task $event" }
        val task = taskService.createTaskQuery().taskId(event.id).singleResult()
        if (task != null) {
          taskService.complete(event.id, event.payload)
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: ProcessEngineException) {
        logger.error("CLIENT-003: Error completing task", e)
      }
    }
  }


  internal fun isAddressedToMe(event: TaskIdentity) = taskCollectorProperties.enricher.applicationName == event.sourceReference.applicationName
}
