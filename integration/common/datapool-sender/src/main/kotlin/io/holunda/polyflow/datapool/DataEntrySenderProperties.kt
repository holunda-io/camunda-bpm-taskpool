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
  val enabled: Boolean = false,
  val type: DataEntrySenderType = DataEntrySenderType.simple,
  @Value("\${spring.application.name:unset-application-name}")
  val applicationName: String
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


