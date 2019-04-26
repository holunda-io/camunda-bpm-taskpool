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
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*


@Repository
interface TaskWithDataEntriesRepository : TaskWithDataEntriesRepositoryExtension, MongoRepository<TaskWithDataEntriesDocument, String>


interface TaskWithDataEntriesRepositoryExtension {
  fun findAllFilteredForUser(user: User, criteria: List<Criterion>, pageable: Pageable? = null): List<TaskWithDataEntriesDocument>
}

open class TaskWithDataEntriesRepositoryExtensionImpl(
  private val mongoTemplate: MongoTemplate
) : TaskWithDataEntriesRepositoryExtension {

  companion object : KLogging() {
    val DEFAULT_SORT = Sort(Sort.Direction.DESC, TaskWithDataEntriesDocument::dueDate.name)
  }


  /**
  <pre>
  db.tasks.aggregate([
  { $unwind: "$dataEntriesRefs" },
  { $lookup: {
  from: "data-entries",
  localField: "dataEntriesRefs",
  foreignField: "_id",
  as: "data_entries" } },
  { $sort: { "dueDate": 1 }},
  { $match: { $and: [
  // { $or: [{ $or: [ { 'assignee' : "kermit" }, { 'candidateUsers' : "kermit" } ] }, { 'candidateGroups' : "other" } ] },
  { $or: [{ $or: [ { 'assignee' : "kermit" }, { 'candidateUsers' : "kermit" } ] }, { 'candidateGroups' : "other" } ] }
  // { $or: [ { 'businessKey': "3" } ] }
  ]

  }}
  ])
  </pre>

   */
  override fun findAllFilteredForUser(user: User, criteria: List<Criterion>, pageable: Pageable?): List<TaskWithDataEntriesDocument> {

    val sort = if (pageable != null) {
      pageable.getSortOr(DEFAULT_SORT)
    } else {
      DEFAULT_SORT
    }

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

    // { \$or: [{ \$or: [ { 'assignee' : ?0 }, { 'candidateUsers' : ?0 } ] }, { 'candidateGroups' : ?1} ] }
    val tasksForUserCriteria = Criteria()
      .orOperator(
        Criteria()
          .orOperator(
            Criteria.where("assignee").isEqualTo(user.username),
            Criteria.where("candidateUsers").isEqualTo(user.username)
          ),
        Criteria
          .where("candidateGroups")
          .`in`(user.groups)
      )

    val filterCriteria = if (filterPropertyCriteria.isNotEmpty()) {
      Criteria()
        .andOperator(
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


    val result: AggregationResults<TaskWithDataEntriesDocument> = mongoTemplate.aggregate(
      Aggregation.newAggregation(aggregations),
      "tasks",
      TaskWithDataEntriesDocument::class.java
    )

    return result.mappedResults
  }
}


fun value(criterion: Criterion): Any =
  when (criterion.name) {
    "priority" -> criterion.value.toInt()
    "createTime", "dueDate", "followUpDate" -> Instant.parse(criterion.value)
    else -> criterion.value
  }


@Document(collection = "tasks")
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

