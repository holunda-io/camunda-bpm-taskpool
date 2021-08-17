package io.holunda.polyflow.view.mongo

import org.springframework.context.annotation.Import

/**
 * Enables polyflow projection using Mongo DB as persistence.
 */
@MustBeDocumented
@Deprecated(message = "Please use EnablePolyflowMongoView instead", replaceWith = ReplaceWith("EnablePolyflowMongoView"))
@Import(TaskPoolMongoViewConfiguration::class)
annotation class EnableTaskPoolMongoView

/**
 * Enables polyflow projection using Mongo DB as persistence.
 */
@MustBeDocumented
@Import(TaskPoolMongoViewConfiguration::class)
annotation class EnablePolyflowMongoView
