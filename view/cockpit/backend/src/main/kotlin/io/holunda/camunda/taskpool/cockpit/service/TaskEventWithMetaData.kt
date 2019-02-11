package io.holunda.camunda.taskpool.cockpit.service

import io.holunda.camunda.taskpool.api.task.TaskEvent
import org.axonframework.messaging.MetaData
import java.time.Instant

/**
 * Task event holder.
 */
data class TaskEventWithMetaData(
  /**
   * Task event.
   */
  val event: TaskEvent,
  /**
   * Timestamp.
   */
  val instant: Instant,
  /**
   * Meta data.
   */
  val metaData: MetaData
)



