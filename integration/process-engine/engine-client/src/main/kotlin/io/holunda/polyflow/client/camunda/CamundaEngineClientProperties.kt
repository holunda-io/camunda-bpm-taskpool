package io.holunda.polyflow.client.camunda

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor.Companion.UNSET_APPLICATION_NAME
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties to configure Camunda to receive interaction commands via Axon.
 */
@ConfigurationProperties("polyflow.integration.client.camunda")
data class CamundaEngineClientProperties(

  /**
   * Denotes the (logical) name of the process application.
   * As Default, spring.application.name will be used
   */
  // The default is set by ApplicationNameBeanPostProcessor
  var applicationName: String = UNSET_APPLICATION_NAME
)
