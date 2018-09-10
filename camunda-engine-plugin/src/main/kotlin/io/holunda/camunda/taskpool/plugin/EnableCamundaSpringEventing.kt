package io.holunda.camunda.taskpool.plugin

import org.springframework.context.annotation.Import

/**
 * Activates the publishing of Camunda Events (Task, Execution) as Spring Events.
 */
@MustBeDocumented
@Import(CamundaEventingConfiguration::class)
annotation class EnableCamundaSpringEventing
