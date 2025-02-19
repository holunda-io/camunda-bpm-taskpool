package io.holunda.polyflow.client.camunda.task

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.client.camunda.CamundaEngineClientProperties
import org.axonframework.eventhandling.EventHandler
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.TaskService
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Handles task events controls Camunda Task Service.
 */
@Component
class TaskEventHandlers(
  private val taskService: TaskService,
  private val properties: CamundaEngineClientProperties
) {

  /**
   * Engine reaction to claim.
   */
  @EventHandler
  fun on(event: TaskClaimedEvent) {
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
        logger.error(e) { "CLIENT-001: Error claiming task" }
      }
    }
  }

  /**
   * Engine reaction to unclaim.
   */
  @EventHandler
  fun on(event: TaskUnclaimedEvent) {
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
        logger.error(e) { "CLIENT-002: Error un-claiming task" }
      }
    }
  }

  /**
   * Engine reaction to complete.
   */
  @EventHandler
  fun on(event: TaskToBeCompletedEvent) {
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
        logger.error(e) { "CLIENT-003: Error completing task" }
      }
    }
  }

  /**
   * Engine reaction to defer.
   */
  @EventHandler
  fun on(event: TaskDeferredEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        logger.debug { "Deferring task $event" }
        val task = taskService.createTaskQuery().taskId(event.id).singleResult()
        if (task != null) {
          if (task.followUpDate != event.followUpDate) {
            task.followUpDate = event.followUpDate
            taskService.saveTask(task)
          } else {
            logger.debug { "CLIENT-008: Task deferred event ignored because task with id ${event.id} had equal follow-up date set already." }
          }
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: ProcessEngineException) {
        logger.error(e) { "CLIENT-003: Error deferring task" }
      }
    }
  }

  /**
   * Engine reaction to undefer.
   */
  @EventHandler
  fun on(event: TaskUndeferredEvent) {
    // filter by application name.
    if (isAddressedToMe(event)) {
      try {
        logger.debug { "Deferring task $event" }
        val task = taskService.createTaskQuery().taskId(event.id).singleResult()
        if (task != null) {
          if (task.followUpDate != null) {
            task.followUpDate = null
            taskService.saveTask(task)
          } else {
            logger.debug { "CLIENT-007: Task undeferred event ignored because task with id ${event.id} was not deferred." }
          }
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: ProcessEngineException) {
        logger.error(e) { "CLIENT-003: Error deferring task" }
      }
    }
  }


  private fun isAddressedToMe(event: TaskIdentity) = properties.applicationName == event.sourceReference.applicationName
}
