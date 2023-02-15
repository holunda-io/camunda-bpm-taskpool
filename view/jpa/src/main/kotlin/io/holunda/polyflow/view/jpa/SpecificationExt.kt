package io.holunda.polyflow.view.jpa

import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.filter.EQUALS
import io.holunda.polyflow.view.filter.LIKE
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasDataEntryPayloadAttribute
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasEntryId
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasEntryType
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasProcessingType
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasState
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasType
import io.holunda.polyflow.view.jpa.task.TaskEntity
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasBusinessKey
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasTaskPayloadAttribute
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeBusinessKey
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeDescription
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeName
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.domain.Specification.where

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
    Sort.by(Sort.Direction.fromOptionalString(sort.substring(0, 1)).orElse(Sort.DEFAULT_DIRECTION), sort.substring(1))
  }
  return if (sortCriteria != null) {
    PageRequest.of(page, size, sortCriteria)
  } else {
    PageRequest.of(page, size)
  }
}


/**
 * Specification for query on task attributes.
 */
internal fun List<Criterion>.toTaskAttributeSpecification(): Specification<TaskEntity>? {
  val relevant = this.filterIsInstance<Criterion.TaskCriterion>().map { it.toTaskSpecification() }
  return composeAnd(relevant)
}

/**
 * Specification for query on data entry attributes.
 */
internal fun List<Criterion>.toDataEntryAttributeSpecification(): Specification<DataEntryEntity>? {
  val relevant = this.filterIsInstance<Criterion.DataEntryCriterion>().map { it.toDataEntrySpecification() }
  return composeAnd(relevant)
}

/**
 * Specification on payload.
 */
internal fun List<Criterion>.toDataEntryPayloadSpecification(): Specification<DataEntryEntity>? {
  val relevant = this.filterIsInstance<Criterion.PayloadEntryCriterion>().map { it.toDataEntrySpecification() }
  return composeAnd(relevant)
}

/**
 * Specification on payload.
 */
internal fun List<Criterion>.toTaskPayloadSpecification(): Specification<TaskEntity>? {
  val relevant = this.filterIsInstance<Criterion.PayloadEntryCriterion>().map { it.toTaskSpecification() }
  return composeAnd(relevant)
}


/**
 * Creates JPA specification for the query of direct attributes of the task.
 */
internal fun Criterion.TaskCriterion.toTaskSpecification(): Specification<TaskEntity> {
  return when (this.operator) {
    EQUALS -> {
      when (this.name) {
        TaskEntity::businessKey.name -> hasBusinessKey(this.value)
        else -> throw IllegalArgumentException("JPA View found unsupported task attribute for equals comparison: ${this.name}.")
      }
    }
    LIKE -> {
      when (this.name) {
        TaskEntity::name.name -> likeName(this.value)
        TaskEntity::description.name -> likeDescription(this.value)
        TaskEntity::businessKey.name -> likeBusinessKey(this.value)
        else -> throw IllegalArgumentException("JPA View found unsupported task attribute for like comparison: ${this.name}.")
      }
    }
    else -> throw IllegalArgumentException("JPA View found unsupported comparison ${this.operator} for attribute ${this.name}.")
  }
}

/**
 * Creates JPA Specification for query of payload attributes based on JSON paths.
 */
internal fun Criterion.PayloadEntryCriterion.toTaskSpecification(): Specification<TaskEntity> {
  return when (this.operator) {
    EQUALS -> hasTaskPayloadAttribute(this.name, this.value)
    else -> throw IllegalArgumentException("JPA View currently supports only equals as operator for filtering of payload attributes.")
  }
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
 * Creates JPA Specification for query of payload attributes based on JSON paths.
 */
internal fun Criterion.PayloadEntryCriterion.toDataEntrySpecification(): Specification<DataEntryEntity> {
  return when (this.operator) {
    EQUALS -> hasDataEntryPayloadAttribute(this.name, this.value)
    else -> throw IllegalArgumentException("JPA View currently supports only equals as operator for filtering of payload attributes.")
  }
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

