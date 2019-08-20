package io.holunda.camunda.datapool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.dataentry.sender")
data class DataEntrySenderProperties(
  var enabled: Boolean = false,
  var type: DataEntrySenderType = DataEntrySenderType.simple,
  @Value("\${spring.application.name}")
  var applicationName: String
)

enum class DataEntrySenderType {
  simple,
  custom
}


