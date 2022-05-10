package io.holunda.polyflow.datapool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for data entry sender (data pool)
 */
@ConfigurationProperties(prefix = "polyflow.integration.sender.data-entry")
data class DataEntrySenderProperties(
  /**
   * Flag to activate or de-activate the entire sender.
   */
  var enabled: Boolean = false,
  /**
   * Sender type. Defaults to "simple" and can be changed to "custom", if you want to use your own implementation.
   */
  var type: DataEntrySenderType = DataEntrySenderType.simple,
  /**
   * Application name used as a source of the data entries. Defaults to "spring.application.name".
   */
  @Value("\${spring.application.name:unset-application-name}")
  var applicationName: String
)


