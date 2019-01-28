package io.holunda.camunda.taskpool.cockpit.service

import io.holunda.camunda.taskpool.api.task.TaskEvent
import org.axonframework.messaging.MetaData
import java.time.Instant

data class TaskEventWithMetaData(
  val event: TaskEvent,
  val instant: Instant,
  val metaData: MetaData
)



