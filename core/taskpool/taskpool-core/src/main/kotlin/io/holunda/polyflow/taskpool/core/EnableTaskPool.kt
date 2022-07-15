package io.holunda.polyflow.taskpool.core

import org.springframework.context.annotation.Import

/**
 * Starts task pool component.
 */
@MustBeDocumented
@Import(TaskPoolCoreConfiguration::class)
annotation class EnablePolyflowTaskPool
