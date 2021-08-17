package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.filter.EQUALS
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasPayloadAttribute
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasProcessingType
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasState
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.domain.Specification.where

/**
 * Creates a JPQL specification out of predicate wrapper.
 */
fun List<Criterion>.toSpecification(): Specification<DataEntryEntity>? {

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
 * Specification for query on data entry attributes.
 */
internal fun List<Criterion>.toDataEntryAttributeSpecification(): Specification<DataEntryEntity>? {
  val relevant = this.filterIsInstance<Criterion.DataEntryCriterion>().map { it.toSpecification() }
  return composeAnd(relevant)
}

/**
 * Specification on payload.
 */
internal fun List<Criterion>.toDataEntryPayloadSpecification(): Specification<DataEntryEntity>? {
  val relevant = this.filterIsInstance<Criterion.PayloadEntryCriterion>().map { it.toSpecification() }
  return composeAnd(relevant)
}

/**
 * Creates JPA specification for the query of direct attributes of the data entry.
 */
internal fun Criterion.DataEntryCriterion.toSpecification(): Specification<DataEntryEntity> {
  return when (this.name) {
    "${DataEntry::state.name}.${DataEntryState::state.name}" -> hasState(this.value)
    "${DataEntry::state.name}.${DataEntryState::processingType.name}" -> hasProcessingType(ProcessingType.valueOf(this.value))
    else -> throw IllegalArgumentException("JPA View found unsupported data entry attribute: ${this.name}.")
  }
}

/**
 * Creates JPA Specification for query of payload attributes based on JSON paths.
 */
internal fun Criterion.PayloadEntryCriterion.toSpecification(): Specification<DataEntryEntity> {
  return when (this.operator) {
    EQUALS -> hasPayloadAttribute(this.name, this.value)
    else -> throw IllegalArgumentException("JPA View currently supports only equals as operator for filtering.")
  }
}


/**
 * Compose multiple specifications into one specification using conjunction.
 */
internal fun composeAnd(specifications: List<Specification<DataEntryEntity>?>): Specification<DataEntryEntity>? {
  return when (specifications.size) {
    0 -> null
    1 -> specifications[0]
    else -> where(specifications[0]).and(composeAnd(specifications.subList(1, specifications.size)))
  }
}

/**
 * Compose multiple specifications into one specification using disjunction.
 */
internal fun composeOr(specifications: List<Specification<DataEntryEntity>?>): Specification<DataEntryEntity>? {
  return when (specifications.size) {
    0 -> null
    1 -> specifications[0]
    else -> where(specifications[0]).or(composeOr(specifications.subList(1, specifications.size)))
  }
}

