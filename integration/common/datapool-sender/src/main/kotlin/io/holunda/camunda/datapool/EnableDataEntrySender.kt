package io.holunda.camunda.datapool

import org.springframework.context.annotation.Import

/**
 * Enables the data entry sender.
 */
@MustBeDocumented
@Import(DataEntrySenderConfiguration::class)
annotation class EnableDataEntrySender

/**
 * Please use [EnableDataEntrySender] instead.
 */
@MustBeDocumented
@Deprecated(message = "Please use EnableDataEntrySender instead.", replaceWith = ReplaceWith("io.holunda.camunda.datapool.EnableDataEntrySender"), DeprecationLevel.ERROR)
@Import(DataEntrySenderConfiguration::class)
annotation class EnableDataEntryCollector
