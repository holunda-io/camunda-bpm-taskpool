package io.holunda.polyflow.datapool

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Holder for the enabled flag and the reference to the relevant [DataPoolSenderProperties].
 * The name is special in order to avoid name clashing with taskpool sender properties.
 */
@ConfigurationProperties(prefix = "polyflow.integration.sender")
data class DataPoolSenderProperties(
  /**
   * Global value to control the command gateway.
   */
  val enabled: Boolean = true,

  /**
   * Data entry properties.
   */
  @NestedConfigurationProperty
  val dataEntry: DataEntrySenderProperties = DataEntrySenderProperties(),
)
