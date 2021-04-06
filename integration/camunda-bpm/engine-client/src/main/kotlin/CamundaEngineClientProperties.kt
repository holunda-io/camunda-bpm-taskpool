package io.holunda.camunda.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("polyflow.integration.client.camunda")
data class CamundaEngineClientProperties(
  val applicationName: String
)

