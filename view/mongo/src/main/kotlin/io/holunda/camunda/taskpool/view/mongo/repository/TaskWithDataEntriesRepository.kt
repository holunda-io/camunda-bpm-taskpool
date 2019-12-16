package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.mongo.service.Criterion
import io.holunda.camunda.taskpool.view.mongo.service.EQUALS
import io.holunda.camunda.taskpool.view.mongo.service.GREATER
import io.holunda.camunda.taskpool.view.mongo.service.LESS
import mu.KLogging
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*


/**
 * Reactive mongo repository for tasks with data entries.
 */
@Repository
interface TaskWithDataEntriesRepository : TaskWithDataEntriesRepositoryExtension, ReactiveMongoRepository<TaskWithDataEntriesDocument, String>

/**
 * Repository extension.
 */
interface TaskWithDataEntriesRepositoryExtension {

  /**
   * Find all tasks with data entries matching specified filter.
   */
  fun findAllFilteredForUser(user: User, criteria: List<Criterion>, pageable: Pageable? = null): Flux<TaskWithDataEntriesDocument>
}

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
          EQUALS -> this.isEqualTo(value(it))
          GREATER -> this.gt(value(it))
          LESS -> this.lt(value(it))
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
      Aggregation.lookup(DataEntryDocument.NAME, "dataEntriesRefs", "_id", "dataEntries"),
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


fun value(criterion: Criterion): Any =
  when (criterion.name) {
    "priority" -> criterion.value.toInt()
    "createTime", "dueDate", "followUpDate" -> Instant.parse(criterion.value)
    else -> criterion.value
  }


@Document(collection = TaskDocument.COLLECTION)
@TypeAlias("task")
data class TaskWithDataEntriesDocument(
  @Id
  val id: String,
  val sourceReference: ReferenceDocument,
  val taskDefinitionKey: String,
  val dataEntries: List<DataEntryDocument>,
  val payload: MutableMap<String, Any> = mutableMapOf(),
  val businessKey: String? = null,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null,
  val followUpDate: Date? = null
)

