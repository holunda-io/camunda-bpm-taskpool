package io.holunda.camunda.taskpool.view.mongo.repository

import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.publisher.Flux

/**
 * Implementation of the change stream based on Mongo template.
 */
open class DataEntryUpdateRepositoryImpl(
  private val mongoTemplate: ReactiveMongoTemplate
) : DataEntryUpdateRepository {
  override fun getDataEntryUpdates(resumeToken: BsonValue?): Flux<ChangeStreamEvent<DataEntryDocument>> {
    return mongoTemplate.changeStream(DataEntryDocument.COLLECTION, changeStreamOptions(resumeToken), DataEntryDocument::class.java)
  }

  private fun changeStreamOptions(resumeToken: BsonValue?): ChangeStreamOptions {
    val builder = ChangeStreamOptions.builder()
    if (resumeToken != null) {
      builder.resumeToken(resumeToken)
    }
    return builder.build()
  }

}
