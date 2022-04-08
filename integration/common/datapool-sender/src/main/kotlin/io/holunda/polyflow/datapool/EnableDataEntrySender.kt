package io.holunda.polyflow.datapool

import org.springframework.context.annotation.Import

/**
 * Enables the data entry sender.
 */
@MustBeDocumented
@Import(DataEntrySenderConfiguration::class)
annotation class EnableDataEntrySender
