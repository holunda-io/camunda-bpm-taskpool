package io.holunda.polyflow.client.camunda

import org.springframework.context.annotation.Import

/**
 * Starts camunda client component accepting interaction commands.
 */
@MustBeDocumented
@Import(CamundaEngineClientAutoConfiguration::class)
annotation class EnableCamundaEngineClient
