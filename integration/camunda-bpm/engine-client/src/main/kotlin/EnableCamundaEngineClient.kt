package io.holunda.camunda.client

import org.springframework.context.annotation.Import

/**
 * Starts camunda client component.
 */
@MustBeDocumented
@Import(CamundaEngineClientConfiguration::class)
annotation class EnableCamundaEngineClient
