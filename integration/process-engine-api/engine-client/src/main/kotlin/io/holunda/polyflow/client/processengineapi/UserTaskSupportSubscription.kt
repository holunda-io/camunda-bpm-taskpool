package io.holunda.polyflow.client.processengineapi

import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

/**
 * Maintains the Process Engine API subscription required by engine-client features that query user-task state.
 */
class UserTaskSupportSubscription(
  private val userTaskSupport: UserTaskSupport,
  private val taskSubscriptionApi: TaskSubscriptionApi
) {

  @PostConstruct
  fun subscribe() {
    userTaskSupport.subscribe(taskSubscriptionApi)
  }

  @PreDestroy
  fun unsubscribe() {
    userTaskSupport.unsubscribe(taskSubscriptionApi)
  }
}
