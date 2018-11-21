package io.holunda.camunda.taskpool.cockpit

import org.springframework.context.annotation.Import

/**
 * Enables taskpool cokpit
 */
@MustBeDocumented
@Import(TaskPoolCockpitConfiguration::class)
annotation class EnableTaskPoolCockpit
