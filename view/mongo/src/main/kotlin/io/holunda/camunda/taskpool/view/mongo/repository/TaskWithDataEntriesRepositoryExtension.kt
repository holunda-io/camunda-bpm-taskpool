package io.holunda.camunda.taskpool.view.mongo.repository

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


/**

db.tasks.aggregate([
{
$unwind: "$dataEntries"
},
{
$lookup:
{
from: "data-entries",
localField: "dataEntries",
foreignField: "_id",
as: "data_entries"
}
},
{
$match: { "data_entries": { $ne: [] } }
}
])


 */

@Repository
interface TaskWithDataEntriesRepository : TaskWithDataEntriesRepositoryExtension, MongoRepository<TaskWithDataEntriesDocument, String>


interface TaskWithDataEntriesRepositoryExtension {
  fun findAllFiltered(criteria: List<Criterion>, pageable: Pageable? = null): List<TaskWithDataEntriesDocument>
}

open class TaskWithDataEntriesRepositoryExtensionImpl(
  private val mongoTemplate: MongoTemplate
) : TaskWithDataEntriesRepositoryExtension {

  companion object : KLogging() {
    val DEFAULT_SORT = Sort(Sort.Direction.DESC, TaskWithDataEntriesDocument::dueDate.name)
  }

  override fun findAllFiltered(criteria: List<Criterion>, pageable: Pageable?): List<TaskWithDataEntriesDocument> {

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

    val aggregations = mutableListOf(
      Aggregation.lookup(DataEntryDocument.NAME, "dataEntriesRefs", "_id", "dataEntries"),
      Aggregation.sort(sort)
    ).apply {
      if (filterPropertyCriteria.isNotEmpty()) {
        this.add(Aggregation.match(Criteria().orOperator(*filterPropertyCriteria)))
      }
    }

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

