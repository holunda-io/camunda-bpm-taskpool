package io.holunda.polyflow.view.jpa

import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.eqExclude
import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.eqInclude
import io.holunda.camunda.variable.serializer.FilterType
import io.holunda.camunda.variable.serializer.JsonPathFilterFunction
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Properties to configure JPA View.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.view.jpa")
data class PolyflowJpaViewProperties(
  val payloadAttributeLevelLimit: Int = -1,
  val eventEmittingType: EventEmittingType = EventEmittingType.AFTER_COMMIT,
  @NestedConfigurationProperty
  val dataEntryFilters: PayloadAttributeFilterPaths
) {
  /**
   * Extracts JSON path filters out of the properties.
   */
  fun dataEntryJsonPathFilters(): List<Pair<JsonPathFilterFunction, FilterType>> {
    return this.dataEntryFilters.include.map { eqInclude(it) }.plus(this.dataEntryFilters.exclude.map { eqExclude(it) })
  }
}

/**
 * Config properties for the path filters.
 */
@ConstructorBinding
data class PayloadAttributeFilterPaths(
  val include: List<String> = listOf(),
  val exclude: List<String> = listOf()
)


/**
 * Style for emitting events.
 */
enum class EventEmittingType {
  /**
   * Couple emitting to the after commit phase of the current transaction, executing the event delivery.
   */
  AFTER_COMMIT,

  /**
   * Couple emitting to the before commit phase of the current transaction, executing the event delivery.
   */
  BEFORE_COMMIT,

  /**
   * Direct emitting without transactional coupling.
   */
  DIRECT
}
