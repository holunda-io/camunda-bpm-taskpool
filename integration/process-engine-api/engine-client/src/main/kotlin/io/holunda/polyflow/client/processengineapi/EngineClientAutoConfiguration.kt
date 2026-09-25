package io.holunda.polyflow.client.processengineapi

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Engine client configuration enabling the event handling of interaction commands.
 */
@ComponentScan
// TODO: how do we do this?, Maybe just @Order?
//@AutoConfigureAfter(CamundaBpmAutoConfiguration::class)
@EnableConfigurationProperties(EngineClientProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class EngineClientAutoConfiguration {

  /**
   * Creates the default support component for accessing Process Engine API user tasks.
   *
   * @return the user-task support component when an application has not supplied one.
   */
  @Bean
  @ConditionalOnMissingBean
  fun userTaskSupport(): UserTaskSupport = UserTaskSupport()

  /**
   * Creates the lifecycle-managed subscription for the configured Process Engine API task service.
   *
   * @param userTaskSupport the component to subscribe to the task service.
   * @param taskSubscriptionApi the Process Engine API task subscription service.
   * @return the subscription lifecycle component.
   */
  @Bean
  @ConditionalOnBean(TaskSubscriptionApi::class)
  fun userTaskSupportSubscription(
    userTaskSupport: UserTaskSupport,
    taskSubscriptionApi: TaskSubscriptionApi
  ) = UserTaskSupportSubscription(userTaskSupport, taskSubscriptionApi)
}
