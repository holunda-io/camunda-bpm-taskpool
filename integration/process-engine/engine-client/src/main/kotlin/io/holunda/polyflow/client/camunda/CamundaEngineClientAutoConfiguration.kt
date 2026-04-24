package io.holunda.polyflow.client.camunda

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

/**
 * Engine client configuration enabling the event handling of interaction commands.
 */
@ComponentScan
@AutoConfigureAfter(CamundaBpmAutoConfiguration::class)
@EnableConfigurationProperties(CamundaEngineClientProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class CamundaEngineClientAutoConfiguration
