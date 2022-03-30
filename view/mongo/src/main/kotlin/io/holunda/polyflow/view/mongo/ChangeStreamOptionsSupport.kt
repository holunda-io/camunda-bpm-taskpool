package io.holunda.polyflow.view.mongo

import com.mongodb.client.model.changestream.OperationType
import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria

/**
 * Create change stream options, compatible with Cosmos DB.
 * Includes resume token, if it is not null.
 */
fun BsonValue?.changeStreamOptions(): ChangeStreamOptions {
  return ChangeStreamOptions
    .builder()
    .filter(
      Aggregation.newAggregation(
        Aggregation.match(Criteria.where("operationType").`in`(OperationType.INSERT.value, OperationType.UPDATE.value, OperationType.REPLACE.value)),
        Aggregation.project("fullDocument")
      )
    ).let { builder ->
      if (this != null) {
        builder.resumeToken(this)
      } else {
        builder
      }
    }.build()
}
