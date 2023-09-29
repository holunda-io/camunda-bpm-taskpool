package io.holunda.polyflow.view.jpa

import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.filter.*
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasDataEntryPayloadAttribute
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasEntryId
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasEntryType
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasProcessingType
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasState
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasType
import io.holunda.polyflow.view.jpa.task.TaskEntity
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasBusinessKey
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasDueDate
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasDueDateAfter
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasDueDateBefore
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasFollowUpDate
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasFollowUpDateAfter
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasFollowUpDateBefore
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasPriority
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasProcessName
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasTaskPayloadAttribute
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeBusinessKey
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeDescription
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeName
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeProcessName
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeTextSearch
import io.holunda.polyflow.view.query.PageableSortableQuery
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Order
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.domain.Specification.where
import java.time.Instant

/**
 * Creates a JPQL specification out of predicate wrapper.
 */
fun List<Criterion>.toDataEntrySpecification(): Specification<DataEntryEntity>? {

  val attributeSpec = toDataEntryAttributeSpecification()
  val payloadSpec = toDataEntryPayloadSpecification()

  return when {
    attributeSpec != null && payloadSpec != null -> where(attributeSpec).and(payloadSpec)
    attributeSpec != null -> attributeSpec
    payloadSpec != null -> payloadSpec
    else -> null
  }
}

/**
 * Creates a JPQL specification out of predicate wrapper.
 */
fun List<Criterion>.toTaskSpecification(): Specification<TaskEntity>? {

  val attributeSpec = toTaskAttributeSpecification()
  val payloadSpec = toTaskPayloadSpecification()

  return when {
    attributeSpec != null && payloadSpec != null -> where(attributeSpec).and(payloadSpec)
    attributeSpec != null -> attributeSpec
    payloadSpec != null -> payloadSpec
    else -> null
  }
}

/**
 * Constructs page request.
 * @param page page number.
 * @param size page size
 * @param sort optional sort in format +filedName or -fieldName
 */
fun pageRequest(page: Int, size: Int, sort: String?): PageRequest {
  val sortCriteria = if (sort.isNullOrBlank()) {
    null
  } else {
    val direction = if (sort.substring(0, 1) == "+") {
      Direction.ASC
    } else {
      Direction.DESC
    }
    Sort.by(direction, sort.substring(1))
  }
  return if (sortCriteria != null) {
    PageRequest.of(page, size, sortCriteria)
  } else {
    PageRequest.of(page, size)
  }
}

fun pageRequest(page: Int, size: Int, sort: List<String>): PageRequest {
  val sortCriteria = sort.map { s ->
    val direction = if (s.substring(0, 1) == "+") {
      Direction.ASC
    } else {
      Direction.DESC
    }
    Order.by(s.substring(1)).with(direction)
  }.toList()


    return PageRequest.of(page, size, Sort.by(sortCriteria))
}

/**
 * Map sort string from the view API to implementation sort of the entities.
 */
fun PageableSortableQuery.mapTaskSort(): List<String> {
  return if (this.sort.isEmpty()) {
    // no sort is specified, we don't want unsorted results.
    listOf("-${TaskEntity::createdDate.name}")
  } else {
    sort.map {
      val direction = it!!.substring(0,1)
      val field = it!!.substring(1)
      when (field) {
        Task::name.name -> TaskEntity::name.name
        Task::description.name -> TaskEntity::description.name
        Task::assignee.name -> TaskEntity::assignee.name
        Task::createTime.name -> TaskEntity::createdDate.name
        Task::dueDate.name -> TaskEntity::dueDate.name
        Task::followUpDate.name -> TaskEntity::followUpDate.name
        Task::owner.name -> TaskEntity::owner.name
        Task::priority.name -> TaskEntity::priority.name
        Task::formKey.name -> TaskEntity::formKey.name
        Task::businessKey.name -> TaskEntity::businessKey.name
        Task::id.name -> TaskEntity::taskId.name
        Task::taskDefinitionKey.name -> TaskEntity::taskDefinitionKey.name
        else -> throw IllegalArgumentException("'$field' is not supported for sorting in JPA View")
      }.let { "$direction$it" }
      }
    }
}


/**
 * Specification for query on task attributes.
 */
internal fun List<Criterion>.toTaskAttributeSpecification(): Specification<TaskEntity>? {
  val relevant = this.filterIsInstance<Criterion.TaskCriterion>()
  // compose criteria with same name with OR and criteria with different names with AND
  val relevantByName = relevant.groupBy { it.name }
  val orComposedByName = relevantByName.map { (_, criteria) -> composeOr(criteria.map { it.toTaskSpecification() }) }

  return composeAnd(orComposedByName)
}

/**
 * Specification for query on data entry attributes.
 */
internal fun List<Criterion>.toDataEntryAttributeSpecification(): Specification<DataEntryEntity>? {
  val relevant = this.filterIsInstance<Criterion.DataEntryCriterion>()
  // compose criteria with same name with OR and criteria with different names with AND
  val relevantByName = relevant.groupBy { it.name }
  val orComposedByName = relevantByName.map { (_, criteria) -> composeOr(criteria.map { it.toDataEntrySpecification() }) }

  return composeAnd(orComposedByName)
}

/**
 * Specification on payload.
 */
internal fun List<Criterion>.toDataEntryPayloadSpecification(): Specification<DataEntryEntity>? {
  val relevant = this.filterIsInstance<Criterion.PayloadEntryCriterion>()
  // compose criteria with same name with OR and criteria with different names with AND
  val relevantByName = relevant.groupBy { it.name }
  val orComposedByName = relevantByName.map { (_, criteria) -> criteria.toOrDataEntrySpecification() }

  return composeAnd(orComposedByName)
}

/**
 * Specification on payload.
 */
internal fun List<Criterion>.toTaskPayloadSpecification(): Specification<TaskEntity>? {
  val relevant = this.filterIsInstance<Criterion.PayloadEntryCriterion>()
  // compose criteria with same name with OR and criteria with different names with AND
  val relevantByName = relevant.groupBy { it.name }
  val orComposedByName = relevantByName.map { (_, criteria) -> criteria.toOrTaskSpecification() }

  return composeAnd(orComposedByName)
}


/**
 * Creates JPA specification for the query of direct attributes of the task.
 */
internal fun Criterion.TaskCriterion.toTaskSpecification(): Specification<TaskEntity> {
  return when (this.operator) {
    EQUALS -> {
      when (this.name) {
        "processName" -> hasProcessName(this.value)
        Task::businessKey.name -> hasBusinessKey(this.value)
        Task::dueDate.name -> hasDueDate(Instant.parse(this.value))
        Task::followUpDate.name -> hasFollowUpDate(Instant.parse(this.value))
        Task::priority.name -> hasPriority(this.value.toInt())
        else -> throw IllegalArgumentException("JPA View found unsupported task attribute for equals comparison: ${this.name}.")
      }
    }

    LIKE -> {
      when (this.name) {
        "textSearch" -> likeTextSearch(this.value)
        Task::name.name -> likeName(this.value)
        Task::description.name -> likeDescription(this.value)
        "processName" -> likeProcessName(this.value)
        Task::businessKey.name -> likeBusinessKey(this.value)
        else -> throw IllegalArgumentException("JPA View found unsupported task attribute for like comparison: ${this.name}.")
      }
    }

    LESS -> {
      when (this.name) {
        Task::dueDate.name -> hasDueDateBefore(Instant.parse(this.value))
        Task::followUpDate.name -> hasFollowUpDateBefore(Instant.parse(this.value))
        else -> throw IllegalArgumentException("JPA View found unsupported task attribute for < comparison: ${this.name}.")
      }
    }

    GREATER -> {
      when (this.name) {
        Task::dueDate.name -> hasDueDateAfter(Instant.parse(this.value))
        Task::followUpDate.name -> hasFollowUpDateAfter(Instant.parse(this.value))
        else -> throw IllegalArgumentException("JPA View found unsupported task attribute for > comparison: ${this.name}.")
      }
    }

    else -> throw IllegalArgumentException("JPA View found unsupported comparison ${this.operator} for attribute ${this.name}.")
  }
}

/**
 * Creates JPA Specification for query of payload attributes based on JSON paths. All criteria must have the same path
 * and will be composed by the logical OR operator.
 */
internal fun List<Criterion.PayloadEntryCriterion>.toOrTaskSpecification(): Specification<TaskEntity> {
  require(this.isNotEmpty()) { "List of criteria must not be empty." }
  require(this.all { it.operator == EQUALS }) { "JPA View currently supports only equals as operator for filtering of payload attributes." }
  require(this.distinctBy { it.name }.size == 1) { "All criteria must have the same path." }

  return hasTaskPayloadAttribute(this.first().name, this.map { it.value })
}

/**
 * Creates JPA specification for the query of direct attributes of the data entry.
 */
internal fun Criterion.DataEntryCriterion.toDataEntrySpecification(): Specification<DataEntryEntity> {
  return when (this.name) {
    DataEntry::entryId.name -> hasEntryId(this.value)
    DataEntry::entryType.name -> hasEntryType(this.value)
    DataEntry::type.name -> hasType(this.value)
    "${DataEntry::state.name}.${DataEntryState::state.name}" -> hasState(this.value)
    "${DataEntry::state.name}.${DataEntryState::processingType.name}" -> hasProcessingType(ProcessingType.valueOf(this.value))
    else -> throw IllegalArgumentException("JPA View found unsupported data entry attribute: ${this.name}.")
  }
}

/**
 * Creates JPA Specification for query of payload attributes based on JSON paths. All criteria must have the same path
 * and will be composed by the logical OR operator.
 */
internal fun List<Criterion.PayloadEntryCriterion>.toOrDataEntrySpecification(): Specification<DataEntryEntity> {
  require(this.isNotEmpty()) { "List of criteria must not be empty." }
  require(this.all { it.operator == EQUALS }) { "JPA View currently supports only equals as operator for filtering of payload attributes." }
  require(this.distinctBy { it.name }.size == 1) { "All criteria must have the same path." }

  return hasDataEntryPayloadAttribute(this.first().name, this.map { it.value })
}


/**
 * Compose multiple specifications into one specification using conjunction.
 */
internal fun <T> composeAnd(specifications: List<Specification<T>?>): Specification<T>? {
  return when (specifications.size) {
    0 -> null
    1 -> specifications[0]
    else -> where(specifications[0]).and(composeAnd(specifications.subList(1, specifications.size)))
  }
}

/**
 * Compose multiple specifications into one specification using disjunction.
 */
internal fun <T> composeOr(specifications: List<Specification<T>?>): Specification<T>? {
  return when (specifications.size) {
    0 -> null
    1 -> specifications[0]
    else -> where(specifications[0]).or(composeOr(specifications.subList(1, specifications.size)))
  }
}

