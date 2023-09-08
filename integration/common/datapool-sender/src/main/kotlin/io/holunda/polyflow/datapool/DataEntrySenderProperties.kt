package io.holunda.polyflow.datapool

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor.Companion.UNSET_APPLICATION_NAME
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
  // The default is set by ApplicationNameBeanPostProcessor
  var applicationName: String = UNSET_APPLICATION_NAME,
  /**
   * Serialize payload to `Map<String, Object>`. Defaults to true.
   */
  val serializePayload: Boolean = true
)


