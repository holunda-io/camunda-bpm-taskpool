package io.holunda.polyflow.datapool.core

import org.springframework.context.annotation.Import

/**
 * Starts data pool component.
 */
@MustBeDocumented
@EnablePolyflowDataPool
@Deprecated("Use new annotation", replaceWith = ReplaceWith("io.holunda.polyflow.datapool.core.EnablePolyflowDataPool"))
annotation class EnableDataPool

/**
 * Starts data pool component.
 */
@MustBeDocumented
@Import(DataPoolCoreConfiguration::class)
annotation class EnablePolyflowDataPool
