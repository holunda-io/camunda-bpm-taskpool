package io.holunda.polyflow.view.mongo.data

import io.holunda.polyflow.view.mongo.changeStreamOptions
import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.publisher.Flux

/**
 * Implementation of the change stream based on Mongo template.
 */
open class DataEntryUpdateRepositoryImpl(
  private val mongoTemplate: ReactiveMongoTemplate
) : DataEntryUpdateRepository {
  override fun getDataEntryUpdates(resumeToken: BsonValue?): Flux<ChangeStreamEvent<DataEntryDocument>> {
    return mongoTemplate.changeStream(DataEntryDocument.COLLECTION, resumeToken.changeStreamOptions(), DataEntryDocument::class.java)
  }
}
