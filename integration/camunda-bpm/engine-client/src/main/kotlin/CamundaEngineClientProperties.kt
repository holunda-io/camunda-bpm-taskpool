package io.holunda.polyflow.client.camunda

import org.springframework.beans.factory.annotation.Value
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
  @Value("\${spring.application.name}")
  var applicationName: String
)

