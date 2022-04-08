package io.holunda.polyflow.client.camunda

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

/**
 * Engine client configuration enabling the event handling of interaction commands.
 */
@ComponentScan
@AutoConfigureAfter(CamundaBpmAutoConfiguration::class)
@EnableConfigurationProperties(CamundaEngineClientProperties::class)
class CamundaEngineClientAutoConfiguration
