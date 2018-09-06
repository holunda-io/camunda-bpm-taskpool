package io.holunda.camunda.taskpool.example.process

import mu.KLogging
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
open class LoggingTaskListener {

  companion object : KLogging()

  @EventListener(condition = "#task.eventName.equals('create')")
  open fun logTaskCreation(task: DelegateTask) {
    logger.info { "Created task ${task.id} of type ${task.taskDefinitionKey}" }
  }
}
