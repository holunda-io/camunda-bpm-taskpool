package io.holunda.camunda.client

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Engine client configuration enabling the event handling of interaction commands.
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(CamundaEngineClientProperties::class)
class CamundaEngineClientConfiguration
