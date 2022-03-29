package io.holunda.polyflow.view.mongo.task

import com.mongodb.client.model.changestream.OperationType
import io.holunda.polyflow.view.query.task.ApplicationWithTaskCount
import mu.KLogging
import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Implementation of the repository extensions.
 */
@Suppress("unused")
open class TaskRepositoryExtensionImpl(
  private val mongoTemplate: ReactiveMongoTemplate
) : TaskRepositoryExtension {

  companion object : KLogging()

  private val notDeleted =
    match(where("deleted").ne(true))

  private val countGroupedByApplicationName = arrayOf(
    group("sourceReference.applicationName").count().`as`("count"),
    project().and("_id").`as`("application").and("count").`as`("taskCount")
  )


  override fun findTaskCountsByApplication(): Flux<ApplicationWithTaskCount> =
    mongoTemplate
      .aggregate(
        newAggregation(
          notDeleted,
          *countGroupedByApplicationName
        ),
        TaskDocument.COLLECTION,
        ApplicationWithTaskCount::class.java
      )

  override fun findTaskCountForApplication(applicationName: String): Mono<ApplicationWithTaskCount> =
    mongoTemplate
      .aggregate(
        newAggregation(
          notDeleted,
          matchApplicationName(applicationName),
          *countGroupedByApplicationName
        ),
        TaskDocument.COLLECTION,
        ApplicationWithTaskCount::class.java
      )
      .singleOrEmpty()
      .defaultIfEmpty(ApplicationWithTaskCount(applicationName, 0))

  override fun getTaskUpdates(resumeToken: BsonValue?): Flux<ChangeStreamEvent<TaskDocument>> =
    mongoTemplate
      .changeStream(TaskDocument.COLLECTION, changeStreamOptions(resumeToken), TaskDocument::class.java)

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

  private fun matchApplicationName(applicationName: String) =
    match(where("sourceReference.applicationName").isEqualTo(applicationName))

}
