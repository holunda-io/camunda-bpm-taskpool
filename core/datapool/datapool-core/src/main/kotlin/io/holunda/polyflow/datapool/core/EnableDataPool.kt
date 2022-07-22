package io.holunda.polyflow.datapool.core

import org.springframework.context.annotation.Import

/**
 * Starts data pool component.
 */
@MustBeDocumented
@Import(DataPoolCoreConfiguration::class)
annotation class EnablePolyflowDataPool
