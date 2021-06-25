package io.holunda.polyflow.view.mongo.task

import io.holunda.polyflow.view.query.task.ApplicationWithTaskCount
import mu.KLogging
import org.bson.BsonValue
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
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
    Aggregation.match(Criteria.where("deleted").ne(true))

  private val countGroupedByApplicationName = arrayOf(
    Aggregation.group("sourceReference.applicationName").count().`as`("count"),
    Aggregation.project().and("_id").`as`("application").and("count").`as`("taskCount")
  )


  override fun findTaskCountsByApplication(): Flux<ApplicationWithTaskCount> =
    mongoTemplate
      .aggregate(
        Aggregation.newAggregation(
          notDeleted,
          *countGroupedByApplicationName
        ),
        TaskDocument.COLLECTION,
        ApplicationWithTaskCount::class.java
      )

  override fun findTaskCountForApplication(applicationName: String): Mono<ApplicationWithTaskCount> =
    mongoTemplate
      .aggregate(
        Aggregation.newAggregation(
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
    return builder.build()
  }

  private fun matchApplicationName(applicationName: String) =
    Aggregation.match(Criteria.where("sourceReference.applicationName").isEqualTo(applicationName))

}
