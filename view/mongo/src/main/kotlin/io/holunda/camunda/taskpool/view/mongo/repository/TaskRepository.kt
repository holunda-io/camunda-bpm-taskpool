package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.query.task.ApplicationWithTaskCount
import mu.KLogging
import org.bson.BsonValue
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


/**
 * Repository for task documents.
 */
@Repository
interface TaskRepository : ReactiveMongoRepository<TaskDocument, String>, TaskCountByApplicationRepositoryExtension {

  /**
   * Retrieves all tasks for user.
   */
  // Note: the query for _deleted not equal to true_ looks weird, but effectively means _null or false_ so it also captures old documents where _deleted_ is not set at all
  @Query("{ 'deleted': {\$ne: true}, \$or: [{ 'assignee': ?0 }, { 'candidateUsers': ?0 }, { 'candidateGroups': { \$in: ?1} } ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>, pageable: Pageable? = null): Flux<TaskDocument>

  /**
   * Retrieves a not deleted with given task.
   */
  @Query("{ '_id': ?0, 'deleted': {\$ne: true} }")
  fun findNotDeletedById(id: String): Mono<TaskDocument>
}

/**
 * Counts tasks.
 */
interface TaskCountByApplicationRepositoryExtension {
  /**
   * Retrieves counts grouped by application names.
   */
  fun findTaskCountsByApplication(): Flux<ApplicationWithTaskCount>

  /**
   * Retrieves a count of tasks for given application.
   */
  fun findTaskCountForApplication(applicationName: String): Mono<ApplicationWithTaskCount>

  /**
   * Retrieves a task updates flux.
   */
  fun getTaskUpdates(resumeToken: BsonValue? = null): Flux<ChangeStreamEvent<TaskDocument>>
}

/**
 * Implementation of the repository extension.
 */
@Suppress("unused")
open class TaskCountByApplicationRepositoryExtensionImpl(
  private val mongoTemplate: ReactiveMongoTemplate
) : TaskCountByApplicationRepositoryExtension {

  companion object : KLogging()

  override fun findTaskCountsByApplication(): Flux<ApplicationWithTaskCount> =
    mongoTemplate.aggregate(
      Aggregation.newAggregation(
        notDeleted,
        *countGroupedByApplicationName
      ),
      TaskDocument.COLLECTION,
      ApplicationWithTaskCount::class.java
    )

  override fun findTaskCountForApplication(applicationName: String): Mono<ApplicationWithTaskCount> =
    mongoTemplate.aggregate(
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
    mongoTemplate.changeStream(TaskDocument.COLLECTION, changeStreamOptions(resumeToken), TaskDocument::class.java)

  private fun changeStreamOptions(resumeToken: BsonValue?): ChangeStreamOptions {
    val builder = ChangeStreamOptions.builder()
    if (resumeToken != null) {
      builder.resumeToken(resumeToken)
    }
    return builder.build()
  }

  private fun matchApplicationName(applicationName: String) =
    Aggregation.match(Criteria.where("sourceReference.applicationName").isEqualTo(applicationName))

  private val notDeleted =
    Aggregation.match(Criteria.where("deleted").ne(true))

  private val countGroupedByApplicationName = arrayOf(
    Aggregation.group("sourceReference.applicationName").count().`as`("count"),
    Aggregation.project().and("_id").`as`("application").and("count").`as`("taskCount")
  )
}
