package io.holunda.polyflow.view.jpa

import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.eqExclude
import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.eqInclude
import io.holunda.camunda.variable.serializer.FilterType
import io.holunda.camunda.variable.serializer.JsonPathFilterFunction
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Properties to configure JPA View.
 */
@ConfigurationProperties(prefix = "polyflow.view.jpa")
data class PolyflowJpaViewProperties(
  /**
   * Leg level during the path building for indexing the custom attributes. Set to -1 (no limit) as default.
   */
  val payloadAttributeLevelLimit: Int = -1,

  /**
   * Allows to specify the column length of the payload attribute values in order to trim values that are too long. This prevents exceptions when handling
   * the events, which can cause the application to infinitely retry.
   * When set to null, values will not be trimmed or validated.
   */
  val payloadAttributeColumnLength: Int?,

  /**
   * List of items to store in projection. Defaults to "DATA_ENTRY"
   */
  val storedItems: Set<StoredItem> = setOf(StoredItem.DATA_ENTRY),
  /**
   * Filters for the paths for indexing.
   */
  @NestedConfigurationProperty
  private val dataEntryFilters: PayloadAttributeFilterPaths = PayloadAttributeFilterPaths(),

  /**
   * Filters for the path for indexing.
   */
  @NestedConfigurationProperty
  private val taskFilters: PayloadAttributeFilterPaths = PayloadAttributeFilterPaths(),

  /**
   * Controls if DataEntryQueries should consider the payload attributes of correlated data entries. Defaults to "false".
   */
  val includeCorrelatedDataEntriesInDataEntryQueries: Boolean = false,

  /**
   * By default if an Event with a more recent timestamp was processed older events will be ignored. If this is set to "true"
   * all events will be processed. Note that this can cause issues as older events can override more recent changes. Defaults to "false"
   */
  val processOutdatedEvents: Boolean = false

) {
  /**
   * Extracts JSON path filters out of the properties.
   */
  fun dataEntryJsonPathFilters(): List<Pair<JsonPathFilterFunction, FilterType>> {
    return this.dataEntryFilters.include.map { eqInclude(it) }.plus(this.dataEntryFilters.exclude.map { eqExclude(it) })
  }

  /**
   * Extracts JSON path filters out of the properties.
   */
  fun taskJsonPathFilters(): List<Pair<JsonPathFilterFunction, FilterType>> {
    return this.taskFilters.include.map { eqInclude(it) }.plus(this.taskFilters.exclude.map { eqExclude(it) })
  }

}

/**
 * Config properties for the path filters.
 */
data class PayloadAttributeFilterPaths(
  /**
   * Include filter for path indexing (white listing)
   */
  val include: List<String> = listOf(),
  /**
   * Exclude filter for path indexing (black listing)
   */
  val exclude: List<String> = listOf()
)

/**
 * Stored item to configure what to save in the DB.
 */
enum class StoredItem {
  /**
   * Task.
   */
  TASK,

  /**
   * Process instance.
   */
  PROCESS_INSTANCE,

  /**
   * Process definition.
   */
  PROCESS_DEFINITION,

  /**
   * Data entry.
   */
  DATA_ENTRY
}
