package io.holunda.camunda.taskpool.view.mongo

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.view.mongo")
data class TaskPoolMongoViewProperties(var changeTrackingMode: ChangeTrackingMode = ChangeTrackingMode.EVENT_HANDLER)

enum class ChangeTrackingMode {
  EVENT_HANDLER,
  CHANGE_STREAM,
  NONE
}
