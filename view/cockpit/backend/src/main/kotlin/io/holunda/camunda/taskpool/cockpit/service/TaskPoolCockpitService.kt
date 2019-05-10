package io.holunda.camunda.taskpool.cockpit.service

import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.api.task.TaskEvent
import io.holunda.camunda.taskpool.cockpit.service.TaskPoolCockpitService.Companion.PROCESSING_GROUP
import mu.KLogging
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.axonframework.eventhandling.TrackingEventProcessor
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Service collecting the events.
 */
@Suppress("unused")
@Component
@ProcessingGroup(PROCESSING_GROUP)
class TaskPoolCockpitService(
  private val configuration: EventProcessingConfiguration,
  private val queryUpdateEmitter: QueryUpdateEmitter
) {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.camunda.taskpool.cockpit.service"
  }

  private val events: MutableList<TaskEventWithMetaData> = mutableListOf()

  /**
   * Restore the projection.
   */
  fun restore() {
    this.configuration
      .eventProcessorByProcessingGroup(TaskPoolCockpitService.PROCESSING_GROUP, TrackingEventProcessor::class.java)
      .ifPresent {
        logger.info { "COCKPIT-001: Starting cockpit event replay." }
        it.shutDown()
        it.resetTokens()
        it.start()
      }
  }

  /**
   * Fill the projection.
   */
  @EventHandler
  fun on(event: TaskEvent, @Timestamp instant: Instant, metaData: MetaData) {
    val withMetaData = TaskEventWithMetaData(event = event, instant = instant, metaData = metaData)
    logger.info { "COCKPIT-002: Received task event $withMetaData" }
    events.add(withMetaData)
    queryUpdateEmitter.emit(QueryTaskEvents::class.java, { query -> query.apply(event.id) }, withMetaData)
  }

  /**
   * Retrieve events.
   */
  @QueryHandler
  fun getEventsAsList(query: QueryTaskEvents): List<TaskEventWithMetaData> {
    return events.filter { query.apply(it.event.id) }
  }


  /**
   * Find task reference.
   */
  fun findTaskReference(taskId: String): TaskReference {
    val taskWithMetadata = events.find { it.event.id == taskId }
      ?: throw IllegalArgumentException("No task with id $taskId found")
    return TaskReference(taskWithMetadata.event.id, taskWithMetadata.event.taskDefinitionKey, taskWithMetadata.event.sourceReference)
  }
}

/**
 * Task reference POJO.
 */
data class TaskReference(
  /**
   * task is.
   */
  val id: String,
  /**
   * task definition key.
   */
  val taskDefinitionKey: String,
  /**
   * Source reference.
   */
  val sourceReference: SourceReference
)
