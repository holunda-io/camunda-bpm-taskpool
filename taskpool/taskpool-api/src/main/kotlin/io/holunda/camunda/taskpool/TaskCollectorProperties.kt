package io.holunda.camunda.taskpool

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "camunda.taskpool.collector")
data class TaskCollectorProperties(

  @NestedConfigurationProperty
  var sender: TaskSenderProperties = TaskSenderProperties(),

  @NestedConfigurationProperty
  var enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties()
)

data class TaskCollectorEnricherProperties(
  var type: String = TaskCollectorEnricherType.processVariables.name,
  var applicationName: String = "\${spring.application.name}"
)

enum class TaskCollectorEnricherType {
  no,
  processVariables,
  custom
}

data class TaskSenderProperties(
  var enabled: Boolean = false,
  var type: TaskSenderType = TaskSenderType.simple
)

enum class TaskSenderType {
  simple,
  custom
}



