package io.holunda.polyflow.taskpool.collector.task

import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.asAssignCommand
import io.holunda.polyflow.taskpool.asCompleteCommand
import io.holunda.polyflow.taskpool.asCreatedCommand
import io.holunda.polyflow.taskpool.asDeleteCommand
import io.holunda.polyflow.taskpool.asUpdateCommand
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationEventPublisher

private val logger = KotlinLogging.logger {}

/**
 * Collects Camunda events and Camunda historic events (event listener order is {@link TaskEventCollectorService#ORDER}) and emits Commands
 */
class TaskEventCollectorService(
  val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties, // must not be private to access in conditions of event handlers
  private val userTaskSupport: UserTaskSupport,
  private val applicationEventPublisher: ApplicationEventPublisher
) {

  /**
   * Static constants.
   */
  companion object {
    const val NAME = "taskEventCollectorService"

    // high order to be later than all other listeners and work on changed entity
    const val ORDER = Integer.MAX_VALUE - 100
  }

  @PostConstruct
  fun addUserTaskEventListener() {
    userTaskSupport.addHandler({ task, payload ->
      when (task.meta[TaskInformation.REASON]) {
        TaskInformation.CREATE -> applicationEventPublisher.publishEvent(task.asCreatedCommand(camundaTaskpoolCollectorProperties.applicationName, payload))
        TaskInformation.ASSIGN -> applicationEventPublisher.publishEvent(task.asAssignCommand())
        TaskInformation.UPDATE -> applicationEventPublisher.publishEvent(task.asUpdateCommand(camundaTaskpoolCollectorProperties.applicationName, payload))
        TaskInformation.COMPLETE -> applicationEventPublisher.publishEvent(task.asCompleteCommand())
        TaskInformation.DELETE -> applicationEventPublisher.publishEvent(task.asDeleteCommand())
        else -> logger.warn { "Received unexpected task update event ${task.meta.get(TaskInformation.REASON)} for task ${task.taskId}." }
      }
    })
  }
}

