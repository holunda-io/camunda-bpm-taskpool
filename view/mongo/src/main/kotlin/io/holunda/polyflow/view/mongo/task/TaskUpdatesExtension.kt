package io.holunda.polyflow.view.mongo.repository

import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import reactor.core.publisher.Flux

/**
 * Task updates.
 */
interface TaskUpdatesExtension {
  /**
   * Retrieves a task updates flux.
   */
  fun getTaskUpdates(resumeToken: BsonValue? = null): Flux<ChangeStreamEvent<TaskDocument>>
}
