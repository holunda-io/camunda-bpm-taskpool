package io.holunda.polyflow.taskpool.core

import org.springframework.context.annotation.Import

/**
 * Starts task pool component.
 */
@MustBeDocumented
@EnablePolyflowTaskPool
@Deprecated("Please use new annotation", replaceWith = ReplaceWith("io.holunda.polyflow.taskpool.core.EnablePolyflowTaskPool"))
annotation class EnableTaskPool

/**
 * Starts task pool component.
 */
@MustBeDocumented
@Import(TaskPoolCoreConfiguration::class)
annotation class EnablePolyflowTaskPool
