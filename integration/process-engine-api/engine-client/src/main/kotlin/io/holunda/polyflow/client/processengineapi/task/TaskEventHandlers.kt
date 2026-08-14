package io.holunda.polyflow.client.processengineapi.task

import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.TaskClaimedEvent
import io.holunda.camunda.taskpool.api.task.TaskDeferredEvent
import io.holunda.camunda.taskpool.api.task.TaskIdentity
import io.holunda.camunda.taskpool.api.task.TaskToBeCompletedEvent
import io.holunda.camunda.taskpool.api.task.TaskUnclaimedEvent
import io.holunda.camunda.taskpool.api.task.TaskUndeferredEvent
import io.holunda.polyflow.client.processengineapi.EngineClientProperties
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.sql.Date

private val logger = KotlinLogging.logger {}

/**
 * Handles task events controls Camunda Task Service.
 */
@Component
class TaskEventHandlers(
  private val taskService: UserTaskModificationApi,
  private val taskCompletionService: UserTaskCompletionApi,
  private val taskQuery: UserTaskSupport,
  private val properties: EngineClientProperties
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
        if (taskQuery.exists(event.id)) {
          taskService.update(
            ChangeAssignmentModifyTaskCmd.AssignTaskCmd(
              taskId = event.id, assignee = event.assignee
            )
          )
        } else {
          logger.error { "CLIENT-004: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) { //TODO: Exception?
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
        if (taskQuery.exists(event.id)) {
          taskService.update(
            ChangeAssignmentModifyTaskCmd.UnassignTaskCmd(
              taskId = event.id
            )
          )
        } else {
          logger.error { "CLIENT-005: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) { //TODO: Exception?
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
        if (taskQuery.exists(event.id)) {
          taskCompletionService.completeTask(CompleteTaskCmd(taskId = event.id, payload = event.payload))
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) { //TODO: Exception?
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
        if (taskQuery.exists(event.id)) {
          var followUpDate = taskQuery.getTaskInformation(event.id).meta.get("followUpDate").let { Date.valueOf(it) }
          if (followUpDate != event.followUpDate) {
//            TODO: wait for api version 1.6
//            taskService.update(SetFollowUpDateTaskCmd(taskId = event.id, followUpDate = followUpDate))
          } else {
            logger.debug { "CLIENT-008: Task deferred event ignored because task with id ${event.id} had equal follow-up date set already." }
          }
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) { //TODO: Exception?
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
        if (taskQuery.exists(event.id)) {
          val followUpDate = taskQuery.getTaskInformation(event.id).meta.get("followUpDate")
          if (followUpDate != null) {
//            TODO: wait for api version 1.6
//            taskService.update(ClearFollowUpDateTaskCmd(taskId = event.id)
          } else {
            logger.debug { "CLIENT-007: Task undeferred event ignored because task with id ${event.id} was not deferred." }
          }
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) { //TODO: Exception?
        logger.error(e) { "CLIENT-003: Error deferring task" }
      }
    }
  }


  private fun isAddressedToMe(event: TaskIdentity) = properties.applicationName == event.sourceReference.applicationName
}
