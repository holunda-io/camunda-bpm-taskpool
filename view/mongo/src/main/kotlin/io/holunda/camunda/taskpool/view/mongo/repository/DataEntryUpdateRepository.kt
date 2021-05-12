package io.holunda.camunda.taskpool.view.mongo.repository

import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import reactor.core.publisher.Flux

/**
 * Repository for retrieving change updates.
 */
interface DataEntryUpdateRepository {
  /**
   * Retrieves change stream of data entry updates.
   * @param resumeToken resume token showing current position.
   * @return change event stream.
   */
  fun getDataEntryUpdates(resumeToken: BsonValue?): Flux<ChangeStreamEvent<DataEntryDocument>>
}
