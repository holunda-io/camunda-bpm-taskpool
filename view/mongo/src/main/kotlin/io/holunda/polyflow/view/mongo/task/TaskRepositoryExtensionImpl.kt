package io.holunda.polyflow.view.mongo.task

import com.mongodb.client.model.changestream.OperationType
import io.holunda.polyflow.view.mongo.changeStreamOptions
import io.holunda.polyflow.view.mongo.data.DataEntryDocument.Companion.authorizedPrincipals
import io.holunda.polyflow.view.query.task.ApplicationWithTaskCount
import mu.KLogging
import org.bson.BsonValue
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
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

  private val notDeleted = match(where("deleted").ne(true))

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
      .changeStream(TaskDocument.COLLECTION, resumeToken.changeStreamOptions(), TaskDocument::class.java)


  override fun findForUser(
    username: String,
    groupNames: Collection<String>,
    businessKey: String?,
    priorities: Collection<Int>?,
    pageable: Pageable?
  ): Flux<TaskDocument> {
    val query = Query.query(buildCriteriaForUser(username, groupNames, businessKey, priorities))
    if (pageable != null) {
      query.with(pageable)
    }
    return mongoTemplate.query(TaskDocument::class.java)
      .matching(query)
      .all()
  }

  private fun buildCriteriaForUser(
    username: String,
    groupNames: Collection<String>,
    businessKey: String?,
    priorities: Collection<Int>?
  ): Criteria {
    val andOperands = ArrayList<Criteria>()
    // Note: the query for _deleted not equal to true_ looks weird, but effectively means _null or false_ so it also captures old documents where _deleted_ is not set at all
    andOperands.add(where("deleted").ne(true))
    andOperands.add(where("authorizedPrincipals").`in`(authorizedPrincipals(setOf(username), groupNames.toSet())))
    if (businessKey != null) {
      andOperands.add(where("businessKey").`is`(businessKey))
    }
    if (priorities != null && !priorities.isEmpty()) {
      andOperands.add(where("priority").`in`(priorities))
    }
    return Criteria().andOperator(andOperands)
  }

  private fun matchApplicationName(applicationName: String) =
    match(where("sourceReference.applicationName").isEqualTo(applicationName))
}
