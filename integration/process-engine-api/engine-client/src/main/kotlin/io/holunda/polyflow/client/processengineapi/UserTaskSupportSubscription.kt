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

  /**
   * Subscribes user-task support to the Process Engine API task subscription service.
   */
  @PostConstruct
  fun subscribe() {
    userTaskSupport.subscribe(taskSubscriptionApi)
  }

  /**
   * Removes the user-task support subscription from the Process Engine API task service.
   */
  @PreDestroy
  fun unsubscribe() {
    userTaskSupport.unsubscribe(taskSubscriptionApi)
  }
}
