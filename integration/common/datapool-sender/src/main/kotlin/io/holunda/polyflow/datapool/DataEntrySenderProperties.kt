package io.holunda.polyflow.datapool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for data entry sender (data pool)
 */
@ConfigurationProperties(prefix = "polyflow.integration.sender.data-entry")
data class DataEntrySenderProperties(
    var enabled: Boolean = false,
    var type: DataEntrySenderType = DataEntrySenderType.simple,
    @Value("\${spring.application.name:unset-application-name}")
  var applicationName: String
)

/**
 * Data entry sender type.
 */
enum class DataEntrySenderType {
  /**
   * Provided.
   */
  simple,
  /**
   * Custom = user-defined.
   */
  custom
}


