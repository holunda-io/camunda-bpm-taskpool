package io.holunda.polyflow.datapool.core

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.core.data-entry")
data class DataPoolProperties(
  /**
   * How many events lead to a snapshot of data entries.
   */
  val snapshotThreshold: Int = 5
)