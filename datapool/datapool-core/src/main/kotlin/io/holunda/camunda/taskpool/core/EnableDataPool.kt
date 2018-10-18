package io.holunda.camunda.taskpool.core

import org.springframework.context.annotation.Import

/**
 * Starts task pool component.
 */
@MustBeDocumented
@Import(DataPoolCoreConfiguration::class)
annotation class EnableDataPool
