package io.holunda.camunda.datapool

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.dataentry.sender")
data class DataEntrySenderProperties(
  var enabled: Boolean = false,
  var type: DataEntrySenderType = DataEntrySenderType.simple
)

enum class DataEntrySenderType {
  simple,
  custom
}


