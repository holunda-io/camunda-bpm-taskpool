package io.holunda.camunda.taskpool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "camunda.taskpool.collector")
class TaskCollectorProperties(

  @Value("\${spring.application.name}")
  springApplicationName: String,

  @NestedConfigurationProperty
  var sender: TaskSenderProperties = TaskSenderProperties(),

  @NestedConfigurationProperty
  var enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties(applicationName = springApplicationName)
)

data class TaskCollectorEnricherProperties(
  var type: String = TaskCollectorEnricherType.processVariables.name,
  var applicationName: String
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



