package io.holunda.polyflow.client.processengineapi

import org.springframework.context.annotation.Import

/**
 * Starts camunda client component accepting interaction commands.
 */
@MustBeDocumented
@Import(EngineClientAutoConfiguration::class)
annotation class EnableEngineClient
