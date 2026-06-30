package io.holunda.polyflow.client.camunda

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

/**
 * Engine client configuration enabling the event handling of interaction commands.
 */
@ComponentScan
// TODO: how do we do this?, Maybe just @Order?
//@AutoConfigureAfter(CamundaBpmAutoConfiguration::class)
@EnableConfigurationProperties(EngineClientProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class EngineClientAutoConfiguration
