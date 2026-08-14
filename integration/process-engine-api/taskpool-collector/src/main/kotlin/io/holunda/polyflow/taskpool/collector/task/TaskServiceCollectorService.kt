package io.holunda.polyflow.taskpool.collector.task

import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import io.holunda.polyflow.taskpool.asCreatedCommand
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.isCreateEvent
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationEventPublisher

/**
 * Service to collect tasks and fire the corresponding commands using Camunda Task Service.
 */
class TaskServiceCollectorService(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties,
  private val applicationEventPublisher: ApplicationEventPublisher,
  private val userTaskSupport: UserTaskSupport
) {

  @PostConstruct
  fun addUserTaskEventListener() {
    userTaskSupport.addHandler ({ task, payload ->
      if (task.isCreateEvent()) {
      applicationEventPublisher.publishEvent(task.asCreatedCommand(camundaTaskpoolCollectorProperties.applicationName, payload))
      }
    })
  }

}
