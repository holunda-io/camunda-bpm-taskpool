package io.holunda.polyflow.view.mongo.data

import com.mongodb.client.model.changestream.OperationType
import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria.where
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
    builder.filter(
      newAggregation(
        match(where("operationType").`in`(OperationType.INSERT.value, OperationType.UPDATE.value, OperationType.REPLACE.value)),
        project("fullDocument")
      )
    )
    return builder.build()
  }
}
