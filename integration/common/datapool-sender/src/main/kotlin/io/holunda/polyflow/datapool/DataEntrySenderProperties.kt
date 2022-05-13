package io.holunda.polyflow.datapool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Configuration properties for data entry sender (data pool)
 */
@ConfigurationProperties(prefix = "polyflow.integration.sender.data-entry")
@ConstructorBinding
data class DataEntrySenderProperties(
  /**
   * Flag to activate or de-activate the entire sender.
   */
  val enabled: Boolean = false,
  /**
   * Sender type. Defaults to "simple" and can be changed to "custom", if you want to use your own implementation.
   */
  val type: DataEntrySenderType = DataEntrySenderType.simple,
  /**
   * Application name used as a source of the data entries. Defaults to "spring.application.name".
   */
  @Value("\${spring.application.name:unset-application-name}")
  val applicationName: String
)

/**
 * Data entry sender type.
 */
enum class DataEntrySenderType {
  /**
   * Provided implementation.
   */
  simple,

  /**
   * Custom = user-defined.
   */
  custom
}


