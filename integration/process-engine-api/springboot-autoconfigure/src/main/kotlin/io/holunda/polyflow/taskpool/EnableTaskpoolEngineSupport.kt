package io.holunda.polyflow.taskpool

import org.springframework.context.annotation.Import

/**
 * Put this annotation on your process application configuration to enable
 * defaults of task pool engine components.
 */
@MustBeDocumented
@Import(TaskpoolEngineSupportConfiguration::class)
annotation class EnableTaskpoolEngineSupport
