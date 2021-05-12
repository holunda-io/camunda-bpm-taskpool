package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.mongo.service.Criterion
import io.holunda.camunda.taskpool.view.mongo.service.EQUALS
import io.holunda.camunda.taskpool.view.mongo.service.GREATER
import io.holunda.camunda.taskpool.view.mongo.service.LESS
import mu.KLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.core.publisher.Flux

/**
 * Repository extension.
 */
open class TaskWithDataEntriesRepositoryExtensionImpl(
  private val mongoTemplate: ReactiveMongoTemplate
) : TaskWithDataEntriesRepositoryExtension {

  companion object : KLogging() {
    val DEFAULT_SORT = Sort.by(Sort.Direction.DESC, TaskWithDataEntriesDocument::dueDate.name)
  }


  /**
   * Retrieves a list of tasks for user matching provided critera.
  <pre>
    db.tasks.aggregate([
    { $lookup: {
    from: "data-entries",
    localField: "dataEntriesRefs",
    foreignField: "_id",
    as: "data_entries" } },
    { $sort: { "dueDate": 1 }},
    { $match: { $and: [
    'deleted': { $ne: true },
    // { $or: [ { 'assignee' : "kermit" }, { 'candidateUsers' : "kermit" }, { 'candidateGroups' : { $in: [ "other" ] } } ] },
    { $or: [ { 'assignee' : "kermit" }, { 'candidateUsers' : "kermit" }, { 'candidateGroups' : { $in: [ "other" ] } } ] }
    /Tas/ { $or: [ { 'businessKey': "3" } ] }
    ]
    }}
    ])
  </pre>
   */
  override fun findAllFilteredForUser(user: User, criteria: List<Criterion>, pageable: Pageable?): Flux<TaskWithDataEntriesDocument> {

    val sort = pageable?.getSortOr(DEFAULT_SORT) ?: DEFAULT_SORT

    val filterPropertyCriteria = criteria.map {
      Criteria.where(
          when (it) {
              is Criterion.TaskCriterion -> it.name
              else -> "dataEntries.payload.${it.name}"
          }
      ).apply {
        when (it.operator) {
          EQUALS -> this.isEqualTo(it.typedValue())
          GREATER -> this.gt(it.typedValue())
          LESS -> this.lt(it.typedValue())
          else -> throw IllegalArgumentException("Unsupported operator ${it.operator}")
        }
      }
    }.toTypedArray()

    // { $or: [{ 'assignee': ?0 }, { 'candidateUsers': ?0 }, { 'candidateGroups': { $in: ?1} } ] }
    val tasksForUserCriteria = Criteria()
      .orOperator(
        Criteria.where("assignee").isEqualTo(user.username),
        Criteria.where("candidateUsers").isEqualTo(user.username),
        Criteria.where("candidateGroups").`in`(user.groups)
      )

    val filterCriteria = if (filterPropertyCriteria.isNotEmpty()) {
      Criteria()
        .andOperator(
          // Note: the query for _deleted not equal to true_ looks weird, but effectively means _null or false_ so it also captures old documents where _deleted_ is not set at all
          Criteria.where("deleted").ne(true),
          tasksForUserCriteria,
          Criteria()
            .orOperator(*filterPropertyCriteria))
    } else {
      tasksForUserCriteria
    }


    val aggregations = mutableListOf(
        Aggregation.lookup(DataEntryDocument.COLLECTION, "dataEntriesRefs", "_id", "dataEntries"),
        Aggregation.sort(sort),
        Aggregation.match(filterCriteria)
    )


    return mongoTemplate.aggregate(
        Aggregation.newAggregation(aggregations),
        TaskDocument.COLLECTION,
      TaskWithDataEntriesDocument::class.java
    )
  }
}
