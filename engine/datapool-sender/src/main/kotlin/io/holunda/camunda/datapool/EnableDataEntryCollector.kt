package io.holunda.camunda.datapool

import org.springframework.context.annotation.Import

/**
 * Enables the data entry collector.
 */
@MustBeDocumented
@Import(DataEntrySenderConfiguration::class)
annotation class EnableDataEntryCollector
