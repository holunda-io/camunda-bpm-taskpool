package io.holunda.polyflow.client.processengineapi.task

import dev.bpmcrafters.processengineapi.task.*
import dev.bpmcrafters.processengineapi.task.ChangeDatesModifyTaskCmd.SetFollowUpDateTaskCmd
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.client.processengineapi.EngineClientProperties
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.sql.Date
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

/**
 * Task event handlers delegating to user task completion and modification API.
 */
@Component
class TaskEventHandlers(
  private val userTaskModificationApi: UserTaskModificationApi,
  private val userTaskCompletionApi: UserTaskCompletionApi,
  private val userTaskSupport: UserTaskSupport,
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
        if (userTaskSupport.exists(event.id)) {
          userTaskModificationApi.update(
            ChangeAssignmentModifyTaskCmd.AssignTaskCmd(
              taskId = event.id, assignee = event.assignee
            )
          ).join()
        } else {
          logger.error { "CLIENT-004: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) {
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
        if (userTaskSupport.exists(event.id)) {
          userTaskModificationApi.update(
            ChangeAssignmentModifyTaskCmd.UnassignTaskCmd(taskId = event.id)
          ).join()
        } else {
          logger.error { "CLIENT-005: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) {
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
        if (userTaskSupport.exists(event.id)) {
          userTaskCompletionApi.completeTask(
            CompleteTaskCmd(taskId = event.id, payload = event.payload)
          ).join()
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) {
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
        if (userTaskSupport.exists(event.id)) {
          val followUpDate = userTaskSupport.getTaskInformation(event.id).meta.get("followUpDate")
          if (followUpDate != null && Date.valueOf(followUpDate) != event.followUpDate) {
            userTaskModificationApi.update(
              SetFollowUpDateTaskCmd(taskId = event.id, followUpDate = OffsetDateTime.parse(followUpDate))
            ).join()
          } else {
            logger.debug { "CLIENT-008: Task deferred event ignored because task with id ${event.id} had equal follow-up date set already." }
          }
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) {
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
        if (userTaskSupport.exists(event.id)) {
          val followUpDate = userTaskSupport.getTaskInformation(event.id).meta.get("followUpDate")
          if (followUpDate != null) {
            userTaskModificationApi.update(
              ChangeDatesModifyTaskCmd.ClearFollowUpDateTaskCmd(taskId = event.id)
            ).join()
          } else {
            logger.debug { "CLIENT-007: Task undeferred event ignored because task with id ${event.id} was not deferred." }
          }
        } else {
          logger.error { "CLIENT-006: Task with id ${event.id} was not found in the engine. Ignoring the event $event." }
        }
      } catch (e: RuntimeException) {
        logger.error(e) { "CLIENT-003: Error deferring task" }
      }
    }
  }


  private fun isAddressedToMe(event: TaskIdentity) = properties.applicationName == event.sourceReference.applicationName
}
