package io.holunda.camunda.taskpool

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "camunda.taskpool.collector")
data class TaskCollectorProperties(

  @NestedConfigurationProperty
  var sender: TaskCollectorSenderProperties = TaskCollectorSenderProperties(),

  @NestedConfigurationProperty
  var enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties()
)

data class TaskCollectorSenderProperties(
  var enabled: Boolean = false
)

data class TaskCollectorEnricherProperties(
  var type: String = TaskCollectorEnricherType.processVariables.name
)

enum class TaskCollectorEnricherType {
  no,
  processVariables,
  custom
}


